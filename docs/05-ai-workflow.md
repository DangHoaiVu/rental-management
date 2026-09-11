# Giao việc cho AI và tự kiểm tra

## Prompt cho nhiệm vụ đầu tiên

Đọc AGENTS.md, README.md, CONTRIBUTING.md và docs/01-scope.md đến docs/04-backlog.md.
Thực hiện riêng RM-001: thiết kế dữ liệu MVP quản lý nhà trọ.
Trước khi viết, tóm tắt nghiệp vụ và giả định đang thiếu; tự chọn giả định đơn giản phù hợp MVP và ghi rõ.
Tạo ERD Mermaid, từ điển dữ liệu và bản ghi quyết định theo đầu ra của ticket.
Chưa tạo ứng dụng, chưa thêm dependency hay module ngoài phạm vi.
Đối chiếu các ràng buộc trùng kỳ thu, thuê chồng thời gian, giá snapshot và thanh toán nhiều lần.
Cuối cùng giải thích dễ hiểu từng quan hệ và 3 câu hỏi để tôi kiểm tra mình đã hiểu thiết kế.
Báo chính xác file đã thay đổi, phần đã kiểm tra, điểm cần khảo sát và đề xuất commit.

## Prompt cho một ticket code sau này

Đọc AGENTS.md và ticket [mã thật]. Kiểm tra code hiện tại trước khi sửa.
Thực hiện đúng acceptance criteria; giữ nguyên phần không liên quan.
Đề xuất kế hoạch ngắn, rồi triển khai trong phạm vi đã giao, không cần hỏi lại các lựa chọn thường lệ.
Chạy kiểm tra thật theo ticket và các gate của repo. Báo command/result, không nói pass nếu chưa chạy.
Giải thích luồng request, transaction và quyền truy cập của chức năng.
Review diff, đề xuất commit Conventional Commits dựa trên thay đổi thực tế.

## Lượt review riêng

Đọc diff của ticket [mã thật] và yêu cầu gốc. Tập trung lỗi hành vi, quyền truy cập, cạnh tranh dữ liệu,
validation, transaction và thiếu kiểm thử. Mỗi phát hiện nêu vị trí, tình huống gây lỗi, ảnh hưởng và cách sửa.
Không khen chung chung, không khẳng định an toàn chỉ vì có test. Đây là AI review, không phải human approval.

## Phần người học phải làm

Tự chạy demo; thử trường hợp sai; đọc diff; trả lời được Controller gọi gì, rule nằm đâu, dữ liệu lưu ở đâu,
vì sao người thuê khác bị từ chối. Chỗ chưa hiểu thì yêu cầu AI giải thích trên code cụ thể.
Có thể nhờ AI viết nhiều code, nhưng chỉ nhận hoàn thành khi hiểu luồng và có bằng chứng kiểm tra.
Nếu chỉ chạy được bằng dữ liệu giả, cập nhật trạng thái là mock; không ghi đã tích hợp.
