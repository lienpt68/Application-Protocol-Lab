package com.protocol.lab.p7_socket.framing;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * LengthFieldFrameCodec:
 * Giải quyết bài toán TCP Sticky Packets (Dính gói) và Packet Fragmentation (Xé gói).
 * Cấu trúc Frame:
 * +-------------------+-----------------------+
 * | Length (4 Bytes)  | Payload (Length Bytes)|
 * +-------------------+-----------------------+
 */
public class LengthFieldFrameCodec {

    public static final int HEADER_LENGTH = 4; // 4 bytes Integer

    /**
     * Đóng gói payload thành Frame có header độ dài 4 byte
     */
    public static byte[] encode(String message) {
        byte[] payloadBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH + payloadBytes.length);
        buffer.putInt(payloadBytes.length);
        buffer.put(payloadBytes);
        return buffer.array();
    }

    /**
     * Giải mã dòng byte stream nhận được, bóc tách chính xác từng message độc lập
     */
    public static List<String> decodeStream(ByteBuffer streamBuffer) {
        List<String> messages = new ArrayList<>();
        streamBuffer.flip();

        while (streamBuffer.remaining() >= HEADER_LENGTH) {
            streamBuffer.mark();
            int messageLength = streamBuffer.getInt();

            if (messageLength < 0 || messageLength > 10 * 1024 * 1024) { // Bảo vệ chống OOM DOS (Max 10MB)
                throw new IllegalStateException("Invalid frame length: " + messageLength);
            }

            if (streamBuffer.remaining() < messageLength) {
                // Chưa nhận đủ payload (Xé gói / Fragmentation), rollback chờ gói tiếp theo
                streamBuffer.reset();
                break;
            }

            byte[] payloadBytes = new byte[messageLength];
            streamBuffer.get(payloadBytes);
            messages.add(new String(payloadBytes, StandardCharsets.UTF_8));
        }

        streamBuffer.compact();
        return messages;
    }
}
