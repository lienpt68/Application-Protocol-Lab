# Application Protocol Lab

Hệ thống lab chuẩn hóa các mô hình giao tiếp Application-to-Application (A2A) và Frontend-to-Backend (FE-BE), tập trung vào các bài toán kỹ thuật phân tán: Timeout, Connection Pooling, Resilience/Retry, N+1 Query, và Low-level byte framing.

## Kiến trúc & Nguyên tắc
* **Nền tảng:** Java 21 | Spring Boot 3.3.x | Maven.
* **Tính độc lập:** Các package (`p1_http` $\rightarrow$ `p8_mcp`) cô lập 100%, không import chéo source code.
* **Hạ tầng tinh gọn:** Mock in-memory (`ConcurrentHashMap`), không phụ thuộc database/broker ngoài.
* **Giao diện:** Tích hợp Thymeleaf UI tại `http://localhost:8080/` cho từng giao thức.

## Ma trận Giao thức

| Phase | Package | Giao thức | Cổng / Endpoint | Đặc trưng kỹ thuật cốt lõi |
| :---: | :--- | :--- | :--- | :--- |
| **P1** | `p1_http` | HTTP/REST | `:8080/api/v1` | Connection Pool, Connect/Read Timeout, SSE, Long-polling |
| **P2** | `p2_websocket` | WebSocket / STOMP | `ws://:8080/ws` | Simple In-Memory Broker, Heartbeat (Ping/Pong), Handshake Auth |
| **P3** | `p3_socketio` | Socket.IO | `:9092` | Auto-fallback (Polling $\leftrightarrow$ WS), Rooms & Namespaces |
| **P4** | `p4_graphql` | GraphQL | `:8080/graphql` | `@BatchMapping` (DataLoader giải quyết N+1), Max Query Depth |
| **P5** | `p5_grpc` | gRPC / Protobuf | `:9090` | 4 mô hình Streaming, Deadlines/Cancellation, Trace Interceptor |
| **P6** | `p6_mqtt` | MQTT | `:1883` | Pub/Sub, QoS 0/1/2, Last Will (LWT), Retained Messages |
| **P7** | `p7_socket` | TCP/UDP Socket | `:9001` (TCP) / `:9002` (UDP) | Java NIO, Xử lý Sticky Packets (Length-Field Framing) |
| **P8** | `p8_mcp` | Model Context Protocol | `:8080/mcp` | JSON-RPC 2.0, AI Tools & Resources Integration |

## Khởi chạy & Kiểm thử
```bash
# Khởi chạy toàn bộ hệ thống (Web UI tại http://localhost:8080)
./mvnw spring-boot:run
```
*Chi tiết các lệnh kiểm thử (cURL, grpcurl, Postman Native, raw JSON-RPC payload) được đặt trực tiếp trong file `README.md` / `<protocol>.md` của từng package tương ứng.*
