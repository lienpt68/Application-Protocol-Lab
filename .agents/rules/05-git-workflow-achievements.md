# Rule: Git Workflow & GitHub Achievements

## BẢO VỆ DỰ ÁN CÔNG TY (SECURITY BOUNDARY)

- **PHẠM VI CHO PHÉP:** CHỈ áp dụng các quy tắc tự động hóa Git này bên trong thư mục:
  `/home/pvq/Documents/pvq/code`
- **CẤM:** Tuyệt đối không can thiệp, không thay đổi git config toàn cục (`~/.gitconfig`), không áp dụng hook cho bất kỳ thư mục nào khác ngoài phạm vi trên. Mọi dự án ngoài phạm vi này là bảo mật của công ty.
- **Git Identity cho dự án cá nhân:**
  - `user.name = quynhpv127001`
  - `user.email = phamquynhltbn12@gmail.com`
  - Luôn cấu hình dạng local repository (`git config --local`).

## QUY TRÌNH GIT TỐI ƯU THÀNH TỰU (ARCHITECT WORKFLOW)

Mỗi khi thao tác code và Git trong các repo thuộc `/home/pvq/Documents/pvq/code`:
1. **Trunk-based / Feature Branch:** Tuyệt đối không push trực tiếp lên `main`. Luôn tách branch `feat/<ten-task>`.
2. **Author Duy Nhất:** Toàn bộ commit thuộc về tác giả chính `quynhpv127001` (Không chèn trailer Co-authored-by vì huy hiệu Pair Extraordinaire đã đạt đủ điều kiện từ PR #1).
3. **Pull Request & Merge:**
   - Luôn hoàn thành task qua PR để tăng bộ đếm **Pull Shark** hữu cơ.
   - Merge không qua review trung gian để giữ vững **YOLO**.
   - Liên kết Issue (`Closes #<id>`) nếu cần kích hoạt **Quickdraw**.
4. **Clean-up:** Dọn dẹp sạch sẽ nhánh local và remote sau khi merge. Giữ git graph chuẩn mực.
