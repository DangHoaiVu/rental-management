# Backlog v0.1

Tất cả ticket ở trạng thái BACKLOG, trừ việc chuẩn bị tài liệu khởi tạo đã hoàn thành.
Mã RM là mã theo dõi trong tài liệu, chưa phải issue đã tạo trên GitHub.
Ưu tiên thứ tự phụ thuộc; không khởi tạo tất cả module cùng lúc.

| Mã | Nhiệm vụ | Phụ thuộc | Điều kiện nghiệm thu chính |
| --- | --- | --- | --- |
| RM-001 | Thiết kế dữ liệu và chốt giả định | Bộ tài liệu này | ERD, từ điển dữ liệu, ràng buộc, ngày thuê, làm tròn, giới hạn MVP nhất quán |
| RM-002 | Dựng nền backend và CI | RM-001 | Boot chạy, kết nối PostgreSQL, migration đầu, Maven Wrapper, test smoke, CI chạy được, README đúng |
| RM-003 | Xác thực và phân quyền | RM-002 | Đăng nhập/logout/me, không tự chọn quyền chủ trọ, test truy cập chéo |
| RM-004 | Khu trọ và phòng | RM-003 | API CRUD phù hợp, mã trùng bị từ chối, tìm kiếm/phân trang, dữ liệu mẫu |
| RM-005 | Người thuê và hợp đồng | RM-004 | Liên kết tài khoản, chỉ số bàn giao, ngăn chồng khoảng thuê bằng test DB |
| RM-006 | Cấu hình phí và chỉ số | RM-005 | Phí theo đơn vị rõ, chỉ số hợp lệ, kỳ đọc không chồng, lịch sử không bị ghi đè |
| RM-007 | Phiếu thu tháng | RM-006 | Draft/issue, chi tiết tính tiền, snapshot đơn giá, ngăn trùng, tenant chỉ xem của mình |
| RM-008 | Ghi nhận thanh toán | RM-007 | Trả một phần/đủ, idempotency, ngăn trả vượt nợ, lịch sử và kiểm tra concurrent requests |
| RM-009 | Giao diện luồng cốt lõi | RM-003, làm tăng dần với RM-004..008 | Login, khu/phòng/hợp đồng, lập phiếu, tenant xem, ghi nhận thanh toán qua API thật |
| RM-010 | Demo và bàn giao MVP | RM-008, RM-009 | Luồng hai tài khoản, test cần thiết đạt, CI, hướng dẫn demo, giới hạn được ghi rõ |

RM-009 không đợi toàn bộ backend hoàn thành: tách thành các ticket UI nhỏ ngay khi API tương ứng sẵn sàng.
Bắt đầu UI sau RM-004 để có luồng tạo/xem phòng đầu tiên, rồi mở rộng theo hợp đồng và thu tiền.
Báo hỏng, thông báo, trả phòng/hoàn cọc thuộc backlog sau MVP.

## Ticket đầu tiên: RM-001

Mục tiêu: thiết kế đủ dữ liệu cho MVP, chưa code Spring Boot.
Đầu vào: docs/01-scope.md, docs/02-business-rules.md.
Đầu ra: docs/data-model.md (ERD Mermaid và từ điển dữ liệu), docs/decisions/0001-billing-and-lease-rules.md.
Cần mô tả: User, Property, Room, Tenant, Lease, LeaseTenant, MeterReading, ChargeRate, Invoice, InvoiceLine, Payment.
Tên entity là gợi ý; chỉ giữ các bảng có trách nhiệm cần thiết và giải thích khi gộp/tách.
Tiêu chí: quyền sở hữu rõ; snapshot đơn giá; một invoice mỗi lease/kỳ; giữ nhiều payment;
ngăn hợp đồng chồng nhau; quy định ngày đầu/cuối; precision và rounding; không xóa lịch sử.
Kiểm tra bằng ví dụ giấy: hai người thuê khác nhau, đổi giá kỳ sau, trả hai lần, gửi request trùng.
Commit dự kiến: docs(db): design rental management data model
