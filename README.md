# Rental Management

Dự án cá nhân của Đặng Hoài Vũ: quản lý nhà trọ dành cho chủ trọ và người thuê.

## Trạng thái thật

Đã có phạm vi, quy tắc nghiệp vụ, thiết kế dữ liệu RM-001 và nền backend RM-002.
`backend/` dùng Spring Boot 4.1.1, Java 21, Maven Wrapper, PostgreSQL, Flyway, JPA và Actuator.
Docker Compose chạy PostgreSQL local; migration V1 tạo `properties` và `rooms`.
CI GitHub Actions chạy `mvn verify` trên pull request vào `main` và push lên `main`.

RM-003 đang triển khai identity/authentication: user, role, session bearer token lưu trong PostgreSQL,
login/logout/me và password hash BCrypt. Chưa có đăng ký công khai, CRUD tài sản, authorization theo lease,
tenant, invoice hoặc payment. RM-004 hiện đã có CRUD khu trọ/phòng cơ bản, chỉ landlord sở hữu mới được thao tác.
RM-005 hiện đã có tenant profile, lease DRAFT/ACTIVE, representative/occupant, handover meter values,
meter reading tuần tự và PostgreSQL exclusion constraint chống lease overlap.
RM-006 hiện đã có charge rate versioned theo property/code, hiệu lực từ đầu tháng và lookup theo kỳ sử dụng;
overlap của khoảng giá do PostgreSQL exclusion constraint chặn.
RM-007 hiện đã có invoice DRAFT/ISSUED, kỳ tháng đầy đủ, snapshot rent/charge rate/line amount và unique lease-period.
RM-008 hiện đã có payment một phần/toàn bộ, lịch sử nhiều payment, idempotency fingerprint,
khóa pessimistic theo invoice và chống thanh toán vượt dư nợ.

Chưa có CRUD, authentication, authorization, hợp đồng, billing, payment hoặc giao diện.
Test tích hợp dùng Testcontainers PostgreSQL và cần Docker đang chạy; không dùng H2.
Các mã RM là mã tài liệu nội bộ, chưa phải issue GitHub.

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

Mở thư mục gốc chứa `AGENTS.md`. RM-002 đã tạo backend nhưng chưa triển khai nghiệp vụ.
Dùng một repo rental-management; dự kiến backend/ và frontend/ trong cùng repo để thay đổi API/UI cùng PR.
Không ghi đè hay khởi tạo lại một repo có sẵn. Các hướng dẫn khởi tạo Git nằm trong CONTRIBUTING.md.

## Chạy backend trên Windows PowerShell

Docker Compose tự đọc file `.env` nếu có, nhưng Spring Boot không tự đọc `.env`. `.env.example` chỉ là mẫu; khi chạy ứng dụng trực tiếp, đặt biến môi trường trong PowerShell:

```powershell
Copy-Item .env.example .env
$env:POSTGRES_DB = "rental_management"
$env:POSTGRES_USER = "rental"
$env:POSTGRES_PASSWORD = "change-me-local-only"
$env:DB_URL = "jdbc:postgresql://localhost:5432/rental_management"
$env:DB_USERNAME = "rental"
$env:DB_PASSWORD = "change-me-local-only"

docker compose up -d postgres
docker compose ps
.\backend\mvnw.cmd -f backend\pom.xml verify
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
Invoke-WebRequest http://localhost:8080/actuator/health
```

Health endpoint chỉ công khai trạng thái tổng quát; chi tiết database không được mở ở runtime mặc định. Dừng PostgreSQL mà giữ volume bằng `docker compose stop postgres`; `docker compose down` cũng không xóa volume nếu không dùng `--volumes`.

RM-002 đến RM-008 chưa được kiểm chứng local khi thiếu Docker; Testcontainers, Flyway trên PostgreSQL và health/auth/property/lease/rate/invoice/payment integration tests cần chạy lại trên máy có Docker.

## Quyết định còn cần khảo sát

Cách tính tháng đầu/cuối, làm tròn từng phí, tiền cọc và hoàn cọc, thời điểm chủ trọ chốt số điện nước.
Giả định MVP được ghi riêng để có thể bắt đầu thiết kế mà không giả vờ đã khảo sát chủ trọ.
