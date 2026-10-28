# Application Protocol Lab — Agent Constitution

## Dự Án & Người Dùng

- **Dự án:** Application Protocol Lab (`quynhpv127001/Application-Protocol-Lab`)
- **Mục tiêu:** Bách khoa toàn thư thực hành về các giao thức Application-to-Application (A2A) và Frontend-to-Backend (FE-BE) trên nền tảng Java 21, Spring Boot 3.3.x, Thymeleaf UI.
- **Tài khoản Git cá nhân:** `quynhpv127001` (Email: `phamquynhltbn12@gmail.com`).
- **Phạm vi an toàn:** CHỈ áp dụng các thiết lập và tự động hóa bên trong thư mục dự án này.

## Nguyên Tắc Vận Hành Cốt Lõi

1. **User Chỉ Làm 1 Việc Duy Nhất (1-Click Mandate):**
   Mọi Pull Request đều phải được Agent URL-encode Tiêu đề (`title`) và Thân bài (`body`) đầy đủ qua link GitHub compare:
   `https://github.com/quynhpv127001/Application-Protocol-Lab/compare/main...<branch>?quick_pull=1&title=<ENCODED_TITLE>&body=<ENCODED_BODY>`
   User chỉ việc bấm link, bấm Merge, và gõ "ok". Khi nhận "ok", Agent tự động checkout `main`, pull `main`, xóa branch local và xóa branch remote.
2. **Author Duy Nhất:** Mọi commit chỉ mang tác giả `quynhpv127001 <phamquynhltbn12@gmail.com>`. Không chèn thêm trailer Co-authored-by.
3. **Mỗi Phase Bắt Buộc Có UI (Thymeleaf):**
   Giao diện đặt tại `src/main/resources/templates/phaseX-<protocol>.html`. Toàn bộ nội dung và thẻ `<script>` BẮT BUỘC nằm trong `<div id="pageContent">`. WebController mapping bắt buộc truyền đủ `title` và `activeTab` để tránh lỗi HTTP 500 (TemplateProcessingException).
4. **Tối Ưu Việc Test (Copy-Paste Ready):**
   File `<protocol>.md` trong mỗi package phải có hướng dẫn Postman Native, raw payload (JSON-RPC, GraphQL, cURL, grpcurl) để người dùng copy-paste chạy ngay, không bắt gõ tay.
5. **Mock In-Memory:** Lưu trữ dữ liệu in-memory (`ConcurrentHashMap`), không tự ý tạo thêm hạ tầng nặng.
6. **Môi Trường Java 21:** Luôn sử dụng JDK 21 (`JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64`).
7. **Viết Cho Chuyên Gia Đọc:** Ngắn gọn, súc tích, đi thẳng vào giải pháp kỹ thuật, zero fluff.
