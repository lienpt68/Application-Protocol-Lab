# Phase 4: GraphQL (The Flexible Data Query Protocol)

GraphQL là ngôn ngữ truy vấn API và runtime thực thi truy vấn do Meta phát triển, giải quyết 2 bài toán kinh điển của REST API:
- **Over-fetching**: Client nhận thừa các trường dữ liệu không cần thiết.
- **Under-fetching**: Client phải gọi nhiều endpoints liên tiếp (N+1 HTTP calls) để gom đủ dữ liệu cho một màn hình.

### Kiến trúc & Điểm nhấn kỹ thuật trong Module:
- **Spring for GraphQL (Spring Boot 3.3.x)**: Tích hợp `@QueryMapping`, `@MutationMapping`, `@SchemaMapping`.
- **The Data Engineer Way — Triệt tiêu N+1 Query (`@BatchMapping`)**: Sử dụng cơ chế DataLoader để gom toàn bộ các thực thể cha và nạp các thực thể con trong 1 lần truy vấn duy nhất.
- **Bảo mật truy vấn**: Tích hợp `MaxQueryDepthInstrumentation` chặn các truy vấn lồng nhau quá 8 tầng, ngăn chặn tấn công DoS và Cyclic Queries.
- **Công cụ tích hợp sẵn**: GraphiQL IDE sẵn sàng tại `http://localhost:8080/graphiql`.

> 👉 **Hướng dẫn Test siêu tốc**: Mở file [graphql.md](graphql.md)
