# Socket TCP & UDP Protocol Lab (`p7_socket`)

Giao thức tầng truyền vận (Transport Layer - L4). Khảo sát cấu trúc Byte Stream của TCP, hiện tượng Sticky Packets / Packet Fragmentation, cơ chế Length-Field Framing, và giao tiếp phi kết nối UDP Datagram.

---

## 1. Bản Chất Kỹ Thuật (The System/Backend Way)
- **TCP Byte Stream**: TCP là dòng byte liên tục, không có khái niệm gói tin ứng dụng (Application Message). Giao thức dưới có thể gom nhiều write lại thành 1 segment qua Nagle's algorithm (Sticky Packet - Dính gói) hoặc bẻ nhỏ nếu vượt quá MSS/MTU (Fragmentation - Xé gói).
- **Length-Field Framing**: Prepend 4 bytes biểu diễn độ dài payload theo thứ tự Big-Endian `[4-Byte Length][Payload Bytes]`. Phía nhận kiểm tra buffer đủ `Length` bytes mới bóc tách thành message hoàn chỉnh.
- **UDP Datagram**: Truyền tin phi kết nối, không bắt tay 3 bước, không kiểm soát tắc nghẽn, lý tưởng cho DNS, Video Streaming, Gaming.

---

## 2. Copy-Paste cURL Test Payloads

### 2.1 Kiểm Tra TCP Sticky Packets & Length-Field Framing
```bash
curl -X POST http://localhost:8080/api/socket/tcp/sticky-test \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      "ORDER:ORD-1001:AMOUNT=500",
      "AUTH:TOKEN_USER_8821",
      "PAYMENT:STATUS=SUCCESS"
    ]
  }'
```

### 2.2 Gửi Gói Tin UDP Datagram
```bash
curl -X POST http://localhost:8080/api/socket/udp/send \
  -H "Content-Type: application/json" \
  -d '{
    "message": "GPS_BEACON:LAT=21.0285:LNG=105.8542:SPEED=60km/h"
  }'
```

### 2.3 Xem Lịch Sử L4 Event Log & Hex Dump
```bash
curl http://localhost:8080/api/socket/logs
```

---

## 3. Lệnh Terminal Native (Netcat `nc`)
```bash
# Gửi UDP packet trực tiếp qua netcat:
echo -n "HELLO_UDP_DATAGRAM" | nc -u -w1 localhost 9192

# Gửi TCP packet qua netcat:
echo "HELLO_TCP_STREAM" | nc -w1 localhost 9191
```
