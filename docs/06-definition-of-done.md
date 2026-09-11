# Tiêu chí hoàn thành

## Ticket code

- Đạt acceptance criteria, không âm thầm thay đổi nghiệp vụ hoặc thêm phạm vi.
- Kiểm tra dữ liệu và quyền ở backend; các ràng buộc database cần thiết được bảo vệ.
- Build và test liên quan chạy đạt. CI bắt buộc đạt sau khi được thiết lập ở RM-002.
- Có test hành vi quan trọng: tình huống đúng, sai, quyền truy cập; concurrency nếu ticket có nguy cơ.
- API, migration, dữ liệu mẫu và README cập nhật khi thay đổi yêu cầu chúng.
- Tự review diff và ghi kết quả. AI review có thể bổ sung, không thay thế bằng chứng chạy thực tế.
- PR ghi thay đổi, cách kiểm tra, giới hạn; không chứa secret hoặc dữ liệu cá nhân thật.
- Merge vào main sau khi kiểm tra; demo được phần mình tuyên bố đã hoàn thành.

## Ticket tài liệu

Đúng yêu cầu, các quyết định nhất quán, liên kết hợp lệ; không cần tạo test giả hoặc cài runtime chỉ để đọc tài liệu.

## MVP

Luồng chủ trọ và người thuê chạy qua API/database thật. Dữ liệu không mất sau restart.
Một người thuê không đọc/sửa được dữ liệu của người khác. Có dữ liệu mẫu và hướng dẫn chạy từ checkout mới.
Nếu thiếu runtime/môi trường, ghi Not run và lý do; không được tính là đã pass gate.
