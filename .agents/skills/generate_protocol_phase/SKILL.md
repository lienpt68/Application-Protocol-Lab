---
name: generate_protocol_phase
description: Sử dụng skill này khi khởi tạo hoặc phát triển một Phase giao thức mới trong dự án Application Protocol Lab (HTTP, WebSocket, Socket.IO, GraphQL, gRPC, MQTT, Socket TCP/UDP, MCP). Kích hoạt khi: "tạo phase mới", "giao thức mới", "triển khai phase", "implement protocol", "thêm phase".
---

# Skill: Hướng Dẫn Xây Dựng Phase Giao Thức Mới

Skill này tổng hợp toàn bộ quy chuẩn kiến trúc, bài học thực tế và chuẩn hóa giao tiếp FE-BE / A2A trong dự án **Application Protocol Lab**. Mọi Antigravity Agent khi phát triển Phase mới BẮT BUỘC tuân thủ 100% các nguyên tắc sau:

---

## 1. Cấu Trúc Mã Nguồn (Modular Architecture)

- **Package độc lập:** Mỗi Phase là một package riêng biệt nằm dưới `com.protocol.lab.pX_<tên_giao_thức>`:
  - `p1_http`: HTTP/REST, SSE, Long-Polling, File Streaming, Connection Pooling.
  - `p2_websocket`: Raw WebSocket, STOMP, Heartbeat, Room Broadcast.
  - `p3_socketio`: Netty-SocketIO, Auto-fallback (Polling -> WS), Room/Namespace.
  - `p4_graphql`: Schema IDL, Queries, Mutations, @BatchMapping (DataLoader), Query Depth Security.
  - `p5_grpc`: Protobuf IDL, Unary, Server/Client/Bidi Streaming, Interceptors, Deadlines.
  - `p6_mqtt`: Mosquitto, QoS 0/1/2, LWT, Retained Messages.
  - `p7_socket`: Low-level TCP NIO / UDP, Xử lý Sticky Packets (Frame Delimiter).
  - `p8_mcp`: Model Context Protocol, JSON-RPC, AI Tools & Resources.
- **Tính độc lập:** Các package tuyệt đối KHÔNG import chéo mã nguồn của nhau.
- **Mocking In-Memory:** Lưu trữ dữ liệu giả lập hoàn toàn trên RAM (sử dụng `ConcurrentHashMap`, `AtomicLong`). KHÔNG tự ý tạo thêm hạ tầng nặng (như Redis, MySQL, RabbitMQ) trừ khi có yêu cầu cụ thể.

---

## 2. Giao Diện Thymeleaf UI & Phòng Chống Lỗi HTTP 500

Mỗi Phase BẮT BUỘC phải có một màn hình kiểm thử trực quan trên web:
1. **File Template:** Đặt tại `src/main/resources/templates/phaseX-<protocol>.html`.
2. **Cú pháp Layout BẮT BUỘC:**
   ```html
   <!DOCTYPE html>
   <html lang="vi" xmlns:th="http://www.thymeleaf.org" th:replace="~{layout :: main(~{::#pageContent})}">
   <body>
   <div id="pageContent" class="fade-in">
       <!-- Toàn bộ nội dung giao diện -->
       
       <!-- QUAN TRỌNG: Toàn bộ thẻ <script> BẮT BUỘC phải nằm bên trong #pageContent -->
       <script>
           // Mã JS điều khiển giao diện
       </script>
   </div>
   </body>
   </html>
   ```
   *Cảnh báo:* Nếu đặt `<script>` ra ngoài thẻ `<div id="pageContent">`, Thymeleaf layout sẽ bỏ qua và frontend sẽ mất toàn bộ tính năng tương tác.
3. **WebController Mapping:**
   Mỗi khi bổ sung controller endpoint trong `com.protocol.lab.ui.WebController`:
   ```java
   @GetMapping("/phaseX")
   public String phaseX(Model model) {
       model.addAttribute("title", "Phase X: Tên Giao Thức");
       model.addAttribute("activeTab", "phaseX");
       return "phaseX-<protocol>";
   }
   ```
   *Cảnh báo:* Thiếu `title` hoặc `activeTab` sẽ khiến layout ném ngoại lệ `TemplateProcessingException` dẫn đến **HTTP 500**.
4. **Menu Sidebar (`layout.html`):** Bổ sung mục menu tương ứng kèm icon kỹ thuật và liên kết `th:classappend="${activeTab == 'phaseX' ? 'active' : ''}"`.

---

## 3. Tài Liệu Hướng Dẫn Kiểm Thử (Copy-Paste Ready)

Trong thư mục package của mỗi Phase, BẮT BUỘC phải có 2 file Markdown:
1. **`README.md`**: Giới thiệu tổng quan về giao thức, kiến trúc giải pháp, điểm nhấn kỹ thuật của Backend/Data Engineer (so sánh trade-off, cơ chế tối ưu).
2. **`<tên_giao_thức>.md`** (VD: `graphql.md`, `grpc.md`):
   - **Tối ưu cực hạn việc Test:** Hướng dẫn chi tiết từng bước (Bước 1: Làm gì, Bước 2: Điền gì).
   - **Ưu tiên Postman Native:** Luôn cung cấp hướng dẫn Postman trước (Postman WebSocket, Socket.IO, GraphQL, gRPC).
   - **Raw Payload sẵn sàng:** Cung cấp sẵn các đoạn JSON-RPC, GraphQL Query/Variables, lệnh cURL/grpcurl để người dùng chỉ việc **Copy -> Paste thẳng vào Tool**, tuyệt đối không bắt người dùng tự gõ tay hay ghép nối JSON.
   - **Không văn tự thừa thãi:** Đi thẳng vào kỹ thuật, zero fluff.

---

## 4. Quản Lý Tiến Trình & Cổng Mạng (Tránh BindException)

- Cổng chính Spring Boot: `8080`.
- Nếu Phase cần mở Server riêng (như Netty Socket.IO port `8085`, TCP server port `9090`):
  - Phải quản lý vòng đời chặt chẽ qua `@Component` implements `CommandLineRunner` (start) và `@PreDestroy` (stop).
  - Trước khi khởi động, kiểm tra và dọn sạch cổng kẹt nếu có: `lsof -t -i:<PORT> | xargs -r kill -9`.

---

## 5. Môi Trường Thực Thi Java 21 & Maven

- Dự án sử dụng **Java 21** và **Spring Boot 3.3.x**.
- Khi máy chủ có nhiều phiên bản JDK (ví dụ Java 25 EA), luôn chỉ định rõ:
  ```bash
  JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64 mvn <goals>
  ```
- `pom.xml` bắt buộc cấu hình `maven-compiler-plugin` với `annotationProcessorPaths` cho Lombok để đảm bảo biên dịch `@Data`, `@Slf4j` thành công 100%.
