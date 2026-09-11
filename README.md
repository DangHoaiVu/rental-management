# Rental Management

Dự án cá nhân của Đặng Hoài Vũ: quản lý nhà trọ dành cho chủ trọ và người thuê.

## Trạng thái thật

Đã có phạm vi đề xuất, quy tắc nghiệp vụ, backlog và quy trình phát triển.
Chưa có ứng dụng Spring Boot, giao diện, database chạy được hoặc CI.
Chưa tạo repository từ xa, issue hoặc commit GitHub. Các mã RM bên dưới là mã tài liệu nội bộ.

## Mục tiêu bản đầu

Một chủ trọ quản lý một hoặc vài khu trọ. Người thuê xem dữ liệu liên quan đến hợp đồng của mình.
Luồng demo: đăng nhập -> tạo phòng -> ghi nhận thuê -> lập phiếu thu -> người thuê xem -> ghi nhận thanh toán.
Đây là công cụ quản lý việc thuê; bản đầu không có tìm kiếm phòng công khai.

## Đọc theo thứ tự

1. [Phạm vi](docs/01-scope.md)
2. [Nghiệp vụ](docs/02-business-rules.md)
3. [Kiến trúc](docs/03-architecture.md)
4. [Backlog](docs/04-backlog.md)
5. [Cách làm việc](CONTRIBUTING.md)
6. [Quy tắc cho AI](AGENTS.md)
7. [Lệnh giao việc cho AI](docs/05-ai-workflow.md)
8. [Tiêu chí hoàn thành](docs/06-definition-of-done.md)

## Bắt đầu trong VS Code

Giải nén vào thư mục mới và mở thư mục gốc chứa AGENTS.md. Đọc RM-001 trước.
Chưa chạy lệnh Spring Boot: skeleton và hướng dẫn chạy sẽ được tạo ở RM-002.
Dùng một repo rental-management; dự kiến backend/ và frontend/ trong cùng repo để thay đổi API/UI cùng PR.
Không ghi đè hay khởi tạo lại một repo có sẵn. Các hướng dẫn khởi tạo Git nằm trong CONTRIBUTING.md.

## Quyết định còn cần khảo sát

Cách tính tháng đầu/cuối, làm tròn từng phí, tiền cọc và hoàn cọc, thời điểm chủ trọ chốt số điện nước.
Giả định MVP được ghi riêng để có thể bắt đầu thiết kế mà không giả vờ đã khảo sát chủ trọ.
