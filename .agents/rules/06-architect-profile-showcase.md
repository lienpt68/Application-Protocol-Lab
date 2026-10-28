# Rule: Architect Profile & Repository Showcase

## MỤC TIÊU CỐT LÕI

Xây dựng và duy trì sự hiện diện trên GitHub ở đẳng cấp **Software Architect / Solution Architect**. Mọi tài liệu và kho lưu trữ cá nhân công khai phải toát lên tư duy thiết kế hệ thống phân tán, năng lực kiểm soát hạ tầng tầng sâu (Low-Level Internals) và tác phong kỹ thuật kỷ luật.

---

## 1. TIÊU CHUẨN PROFILE README (`quynhpv127001/quynhpv127001`)

- **Định vị chuyên gia:** Rõ ràng hành trình: Java Senior Developer + DevOps Engineer + DBA $\rightarrow$ **Software & Solution Architect**.
- **Triết lý kiến trúc bắt buộc:**
  1. *Code Is Single Source of Truth:* Tài liệu chỉ là tham chiếu; kiến trúc kiểm chứng bằng code thực thi.
  2. *Low-Level to High-Level:* Nắm vững từ Kernel Syscalls, JVM Memory, DB Storage Engines đến DDD, PACELC, Multi-Region HA/DR.
  3. *Bulletproof & Zero-Trace:* 100% lab thực hành phải idempotent và có script cleanup sạch sẽ.
- **Sơ đồ trực quan (Visual Architecture):** Bắt buộc có sơ đồ kiến trúc động bằng Mermaid.js mô phỏng luồng dữ liệu (Data Pipeline / Event-Driven / Gateway).
- **Thẩm mỹ cao cấp:** Sử dụng bảng biểu, icon kỹ thuật, badges đồng bộ màu sắc tối (Dark Theme / Slate / Emerald / Cyan). Tuyệt đối không dùng icon hoạt hình hoặc màu sắc lòe loẹt.

---

## 2. TIÊU CHUẨN 6 PINNED REPOSITORIES (KHO CHỦ LỰC)

Mọi repository được chọn để ghim (Pin) trên profile phải thỏa mãn **Bộ 4 Tiêu Chuẩn Vàng**:

| Tiêu chuẩn | Yêu cầu kỹ thuật |
| :--- | :--- |
| **1. C4 / Mermaid Diagram** | README phải có sơ đồ kiến trúc hệ thống (System Context hoặc Container Diagram) vẽ bằng Mermaid code block. |
| **2. Architecture Rationale** | Phải có phần giải thích lý do lựa chọn công nghệ và bảng phân tích đánh đổi (Trade-off Matrix: CAP, Latency vs Consistency, Cost/TCO). |
| **3. Benchmark & Evidence** | Phải có số liệu đo lường thực tế (Throughput RPS, Latency p99, RAM allocation, GC pause time). Không nói chung chung. |
| **4. 1-Click Run & Rollback** | Có script chạy thử nghiệm tự động và script dọn dẹp nguyên trạng 100% (`zero-trace-rollback`). |

### Danh mục 6 Repository mục tiêu đề xuất:
1. `Software-Architecture`: Kho báu kiến trúc 35 chuyên đề (Flagship).
2. `Enterprise-Linux-Hardening`: An ninh hạ tầng, granular sudo, cô lập SSH và chống "tự hủy" server.
3. `Event-Driven-Microservices`: Kiến trúc hướng sự kiện, Kafka streaming, Debezium CDC, Transactional Outbox Pattern.
4. `Database-Engine-Internals`: Tuning Oracle/PostgreSQL, MVCC, WAL, B+Tree, execution plan deep-dive.
5. `Inbound-Outbound-Proxy-Gateway`: Kiến trúc 4 tầng Nginx / Squid Proxy, bảo vệ tài nguyên mạng.
6. `High-Performance-Java-Runtime`: Virtual Threads (Loom), JMM, Concurrency, GC tuning (ZGC/G1).

---

## 3. KỶ LUẬT GIT & ĐÓNG GÓP HÀNG NGÀY

- **Conventional Commits:** 100% commit tuân thủ `feat:`, `fix:`, `refactor:`, `perf:`, `docs:`, `chore:`.
- **Feature Branch & PRs:** Không commit cẩu thả vào `main`. Mọi tính năng đều thông qua branch `feat/*`, mở PR có mô tả kiến trúc để vừa giữ commit log đẹp, vừa tích lũy huy hiệu **Pull Shark**, **Pair Extraordinaire** và **YOLO**.
- **Security Boundary:** Tuyệt đối không để rò rỉ mã nguồn hoặc thông tin dự án mật của công ty ra GitHub cá nhân. Mọi cấu hình cá nhân chỉ áp dụng tại `/home/pvq/Documents/pvq/code`.
