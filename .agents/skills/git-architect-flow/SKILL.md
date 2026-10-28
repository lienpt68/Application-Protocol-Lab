---
name: git-architect-flow
description: Tự động hóa 100% quy trình Gitflow chuẩn Solution Architect cho dự án Application-Protocol-Lab. Người dùng chỉ làm 1 việc duy nhất: bấm link merge PR trên web và báo 'ok'. Kích hoạt khi: "push git", "đẩy git", "commit", "tạo pr", "merge", "pull shark", "code xong".
---

# Skill: Git Architect Flow (1-Click Automation)

Skill này quy định quy trình làm việc Git chuẩn mực giữa Agent và Developer. Mọi Agent Antigravity khi thao tác Git trên repository này BẮT BUỘC tuân thủ các quy tắc sau:

---

## 1. NGUYÊN TẮC VÀNG: USER CHỈ LÀM ĐÚNG 1 VIỆC

Developer không phải gõ lệnh Git, không phải tự viết PR description, và không phải chuyển đổi branch thủ công.

### Trách nhiệm của Agent (Tự động 100%):
1. **Tạo Feature Branch:** Luôn tách branch mới từ `main`: `feat/<tên-chức-năng>` hoặc `fix/<tên-lỗi>`. Tuyệt đối không commit trực tiếp lên `main`.
2. **Commit Chuẩn:**
   - Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
   - Tác giả duy nhất: `quynhpv127001 <phamquynhltbn12@gmail.com>` (không thêm trailer co-author).
3. **Đẩy lên Remote:** `git push -u origin <branch-name>`.
4. **BẮT BUỘC TẠO PRE-FILLED URL (METHOD 1):**
   - Agent **BẮT BUỘC** phải URL-encode Tiêu đề (`title`) và Thân bài mô tả (`body`) vào query parameters của link GitHub compare:
     ```text
     https://github.com/quynhpv127001/Application-Protocol-Lab/compare/main...<branch>?quick_pull=1&title=<ENCODED_TITLE>&body=<ENCODED_BODY>
     ```
   - Nội dung `body` phải có sẵn:
     - `## 📌 Overview`: Tóm tắt các thay đổi kiến trúc hoặc bài lab.
     - `## 🏷️ Type of Change`: Tích sẵn `[x]` vào loại thay đổi phù hợp.
     - `## 📋 Checklist`: Tích sẵn `[x]` các tiêu chí kiểm thử và clean code.
   - **CẤM:** Tuyệt đối không gửi link trần (`pull/new/<branch>`) vì sẽ làm xuất hiện khung template trống, ép User phải gõ hoặc copy-paste.
   - Định dạng xuất link cho User:
     ```text
     👉 [Nhấn vào đây để mở và merge PR (Đã điền sẵn 100% nội dung)](<PREFILLED_URL>)
     ```
5. **Dọn dẹp tự động khi nhận "ok":**
   Ngay khi User báo *"ok"* hoặc *"đã merge"*, Agent **BẮT BUỘC** thực hiện ngay:
   ```bash
   git checkout main && git pull origin main
   git branch -D <branch-name>
   git push origin --delete <branch-name>
   ```
   Sau đó cập nhật file theo dõi tiến độ và báo cáo trạng thái ngắn gọn cho User.

---

## 2. CHECKLIST KIỂM ĐỊNH MỖI LẦN PUSH

- [ ] Kiểm tra thư mục thực thi thuộc `/home/pvq/Documents/pvq/code/Application-Protocol-Lab`.
- [ ] Không bao giờ push trực tiếp vào `main`.
- [ ] Đã chạy và verify unit test thành công 100% trước khi commit.
- [ ] Commit message chuẩn Conventional Commits.
- [ ] Tác giả duy nhất `quynhpv127001`.
- [ ] Link mở PR bắt buộc dùng dạng compare URL nhúng sẵn 100% `title` và `body`.
- [ ] Đã dọn dẹp sạch sẽ nhánh cũ trước khi tạo nhánh mới (`git branch -a` chỉ còn `* main`).
