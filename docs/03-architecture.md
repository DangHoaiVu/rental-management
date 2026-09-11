# Kiến trúc đề xuất

## Quyết định

Java + Spring Boot, PostgreSQL, Maven Wrapper, REST API. Một backend triển khai thành một ứng dụng.
Một repo chứa tài liệu, backend/ và frontend/. Frontend chưa chốt framework; chọn ở ticket UI trước khi scaffold.
Chọn phiên bản Java/Spring Boot còn được hỗ trợ và tương thích ở RM-002, ghi version thực vào README/pom.xml.

## Bốn lớp trong một project Maven

Package gốc dự kiến: vn.hoaivu.rentalmanagement. Lớp khởi chạy ở package gốc.

| Package | Trách nhiệm |
| --- | --- |
| domain.<module> | Đối tượng, quy tắc, trạng thái, không phụ thuộc Spring/JPA |
| application.<module> | Use case, port/interface, điều phối nghiệp vụ |
| infrastructure.<module> | JPA entity, adapter database, triển khai port |
| api.<module> | Controller, request/response DTO, validation đầu vào |
| config | Ghép dependency, bảo mật, transaction, cấu hình ứng dụng |

Module: identity, property, room, lease, billing, payment; maintenance ở bản sau.
Không đồng nhất module với microservice hoặc Maven subproject.

## Chiều phụ thuộc

application phụ thuộc domain; infrastructure triển khai interface của application; api gọi application.
config ghép các lớp. Domain không import Spring/JPA; Application không import implementation Infrastructure.
Có thể dùng cơ chế transaction của Spring ở lớp điều phối/config với quyết định được ghi rõ.
Không tạo một interface cho mọi class nếu không có nhu cầu thay thế hoặc ranh giới rõ ràng.
Domain model và JPA entity tách riêng khi persistence cần annotation; mapping phải nhỏ và có kiểm tra phù hợp.
Các lớp là quy tắc về trách nhiệm, không phải lý do tạo thêm class rỗng.

## Nguyên tắc tích hợp

- API dưới /api/v1, phản hồi lỗi thống nhất và có mô tả OpenAPI.
- DTO nhận dữ liệu giới hạn trường được phép, không nhận thẳng entity database.
- Migration database dùng Flyway; không sửa migration đã dùng ở môi trường chung.
- Quy tắc trùng/chồng lấn cần transaction và cơ chế database phù hợp, được kiểm thử trên PostgreSQL.
- Bản đầu không cần RabbitMQ/Redis/AI. Chọn xác thực session hoặc token ở RM-003 với chính sách lưu trữ,
  hết hạn, logout và CSRF phù hợp; không tự thêm JWT chỉ vì dự án mẫu dùng JWT.
- Tách cấu hình local/test/demo; không hardcode secret. Có .env.example nếu môi trường thực sự dùng .env.
- RM-002 tạo CI build/test có thật. Đây chưa phải một pipeline đã cài đặt.
