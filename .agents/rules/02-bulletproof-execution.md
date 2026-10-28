# Rule: Bulletproof Execution

## SHELL SCRIPT

- Bắt đầu mọi script: `set -euo pipefail`.
- KHÔNG lồng heredoc trong `ssh ... bash -c '...'`. Dùng file tạm `/tmp/xxx.sh` rồi `bash /tmp/xxx.sh`.
- KHÔNG dùng `echo "password" | sudo -S`. Cấu hình NOPASSWD trong sudoers hoặc dùng ssh key.
- Luôn `visudo -c` sau khi sửa sudoers. Nếu fail → tự rollback.
- Escape đúng biến: `$VAR` vs `\$VAR` trong heredoc, ssh, sudo.

## IDEMPOTENT

- Script chạy lần 2 phải cho kết quả giống lần 1, không lỗi.
- Check trước khi tạo: `id user 2>/dev/null || useradd user`.
- Check trước khi xóa: `[ -f file ] && rm file`.

## CUNG CẤP 2 DẠNG

1. **1-Click Script:** Copy-paste chạy 1 lần xong hết. Cho người muốn nhanh.
2. **Step-by-step:** Từng bước có checkpoint xác nhận. Cho người muốn hiểu.

## TEST CASES

- Mọi lab phải có **tối thiểu 5 test cases** với input/output thực tế.
- Bao gồm cả happy path và attack/edge cases.
- Format: `### Test N: [Mô tả]` → `bash command` → `Kết quả: ✅/❌ [giải thích]`.

## CẤM

- Script mà chạy lần đầu fail → không chấp nhận.
- Thiếu error handling → không chấp nhận.
- Hardcode IP/path không có biến → không chấp nhận.
