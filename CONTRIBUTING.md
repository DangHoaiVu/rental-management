# Quy trình phát triển cá nhân

## Một nhiệm vụ đi qua những bước nào?

Ticket -> làm rõ yêu cầu -> thiết kế vừa đủ -> branch -> code/test -> PR -> tự review + AI review -> merge -> demo.
Board đề xuất: Backlog, Ready, In Progress, Review, Done. Mỗi thời điểm chỉ làm một ticket chính.
Ready: rõ đầu vào, kết quả, quyền, lỗi cần xử lý và phụ thuộc. Done: xem docs/06-definition-of-done.md.
Gặp vướng mắc, ghi điều đã thử, bằng chứng lỗi và câu hỏi; không tự đoán nghiệp vụ nhạy cảm.

## Branch và repository

Một repo rental-management. main chứa kết quả đã kiểm tra; mỗi ticket có branch ngắn hạn.
Không cần dev khi chỉ có một người. Ví dụ feat/RM-004-room-management, fix/RM-007-duplicate-invoice.
RM-004 là mã backlog; khi có GitHub Issue #12, ghi liên kết #12 thật, không tự giả định RM-004 = #4.
Dùng PR kể cả khi làm một mình để giữ diff, mục tiêu, kết quả kiểm tra và quyết định.
Tự review và ghi đúng là self-review. Không bắt buộc một approval từ người khác khi chưa có reviewer.
Khi CI có thật, cấu hình kiểm tra bắt buộc nếu gói/quyền repo hỗ trợ; không khai báo gate chưa tồn tại.
Sau khi squash merge, xóa branch đã hoàn tất. Commit cuối trên main phải mô tả thay đổi thực tế.

## Commit convention

Theo Conventional Commits: <type>(<scope>): <description>. Scope có thể bỏ với thay đổi toàn repo.
Quy ước riêng của dự án: chủ đề tiếng Anh, ngắn, động từ hiện tại, ưu tiên không quá 72 ký tự.

| Type | Dùng khi |
| --- | --- |
| feat | Bổ sung hành vi/tính năng |
| fix | Sửa lỗi |
| refactor | Sắp xếp code, không đổi hành vi |
| test | Bổ sung hoặc sửa kiểm thử |
| docs | Tài liệu |
| chore | Công việc bảo trì/cấu hình chung |
| build | Cấu hình build hoặc dependency |
| ci | Pipeline kiểm tra/triển khai |

Scope gợi ý: auth, property, room, lease, billing, payment, maintenance, api, db.
Ví dụ theo giai đoạn, không phải lịch sử đã tạo:

    docs: define rental management scope and development workflow
    chore(backend): initialize spring boot project
    feat(room): add room creation and availability listing
    feat(billing): issue monthly invoices with rate snapshots
    fix(payment): prevent duplicate payment recording
    test(lease): reject overlapping rental agreements

Một commit chứa một thay đổi logic hoàn chỉnh; có thể gồm implementation, test và migration liên quan.
Không tách giả tạo chỉ để tăng số commit; không ghi update, done hoặc fix bug.
Thay đổi phá vỡ API cần ghi dấu ! hoặc footer BREAKING CHANGE và mô tả ảnh hưởng.

## Khởi tạo Git trên máy cá nhân

Chỉ thực hiện trong thư mục dự án mới sau khi đọc nội dung. Chưa có lệnh nào dưới đây được chạy trong bộ này.
Kiểm tra danh tính Git, tự cấu hình đúng tên/email của bạn nếu chưa có; không dùng danh tính người khác.

    git config --get user.name
    git config --get user.email
    git init -b main
    git status --short
    git add README.md AGENTS.md CONTRIBUTING.md docs .github .gitignore .gitattributes .editorconfig
    git diff --cached --check
    git diff --cached
    git commit -m "docs: define rental management scope and development workflow"

Sau đó tạo repo GitHub trống trong đúng tài khoản, chọn visibility theo ý bạn và dùng URL thật của repo đó làm origin.
Không copy URL của bạn học. Không tự ghi một URL chưa tạo vào cấu hình.
Commit đầu là bootstrap; từ nhiệm vụ tiếp theo dùng branch/PR.

## Trước mỗi commit và PR

Xem git status, diff, diff đã stage. Chạy kiểm tra tương ứng và ghi đúng kết quả.
Không push dữ liệu người thuê thật, password, API key, .env hoặc tài liệu công ty vào repo.
Sau khi có backend, mỗi PR code phải build/test theo Maven Wrapper và các job CI được thiết lập ở RM-002.
PR tài liệu chỉ cần kiểm tra tài liệu phù hợp; không phải chạy DB nếu không có ảnh hưởng.

## Nguồn quy ước

https://www.conventionalcommits.org/en/v1.0.0/
https://docs.github.com/en/get-started/using-github/github-flow
