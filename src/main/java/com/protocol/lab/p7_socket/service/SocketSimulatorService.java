package com.protocol.lab.p7_socket.service;

import com.protocol.lab.p7_socket.framing.LengthFieldFrameCodec;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SocketSimulatorService {

    @Data
    @Builder
    public static class SocketLogEvent {
        private String eventId;
        private String protocol; // "TCP" or "UDP"
        private String direction; // "CLIENT -> SERVER" or "SERVER -> CLIENT"
        private String summary;
        private String rawBytesHex;
        private Instant timestamp;
    }

    private final List<SocketLogEvent> socketLogs = new CopyOnWriteArrayList<>();

    /**
     * Mô phỏng hiện tượng TCP Sticky Packet (Nhiều tin nhắn bị dính chùm vào 1 TCP Segment)
     * và cách giải quyết bằng LengthFieldFrameCodec.
     */
    public Map<String, Object> simulateTcpStickyPackets(List<String> rawMessages) {
        // 1. Kịch bản lỗi: Gửi không Header (Dính gói)
        StringBuilder stickyRawStream = new StringBuilder();
        for (String msg : rawMessages) {
            stickyRawStream.append(msg);
        }

        // 2. Kịch bản chuẩn Backend: Đóng gói Length-Field Framing [Len (4B)][Payload]
        ByteArrayOutputStream framedOutputStream = new ByteArrayOutputStream();
        for (String msg : rawMessages) {
            byte[] frame = LengthFieldFrameCodec.encode(msg);
            framedOutputStream.writeBytes(frame);
        }

        byte[] framedBytes = framedOutputStream.toByteArray();
        ByteBuffer serverReceiveBuffer = ByteBuffer.allocate(framedBytes.length * 2);
        serverReceiveBuffer.put(framedBytes);

        // Server bóc tách chính xác từng message độc lập
        List<String> decodedMessages = LengthFieldFrameCodec.decodeStream(serverReceiveBuffer);

        SocketLogEvent logEvent = SocketLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .protocol("TCP")
                .direction("CLIENT -> SERVER")
                .summary(String.format("Transmitted %d frames via Length-Field Framing. Successfully parsed %d distinct messages.",
                        rawMessages.size(), decodedMessages.size()))
                .rawBytesHex(bytesToHex(framedBytes))
                .timestamp(Instant.now())
                .build();
        socketLogs.add(logEvent);

        Map<String, Object> result = new HashMap<>();
        result.put("inputMessages", rawMessages);
        result.put("stickyWithoutFraming", stickyRawStream.toString());
        result.put("framedHex", bytesToHex(framedBytes));
        result.put("decodedMessages", decodedMessages);
        result.put("frameResolvedSuccessfully", decodedMessages.size() == rawMessages.size());
        return result;
    }

    /**
     * Mô phỏng truyền tin UDP Datagram (Connectionless, Unordered)
     */
    public Map<String, Object> simulateUdpDatagram(String message) {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        SocketLogEvent logEvent = SocketLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .protocol("UDP")
                .direction("CLIENT -> SERVER (Port 9192)")
                .summary(String.format("Sent UDP Datagram (%d bytes). No 3-way handshake, no ACK overhead.", payload.length))
                .rawBytesHex(bytesToHex(payload))
                .timestamp(Instant.now())
                .build();
        socketLogs.add(logEvent);

        Map<String, Object> result = new HashMap<>();
        result.put("protocol", "UDP");
        result.put("payload", message);
        result.put("sizeBytes", payload.length);
        result.put("hexDump", bytesToHex(payload));
        result.put("status", "DELIVERED");
        return result;
    }

    public List<SocketLogEvent> getSocketLogs() {
        return List.copyOf(socketLogs);
    }

    public void clearLogs() {
        socketLogs.clear();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
