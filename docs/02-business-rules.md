# Quy tắc nghiệp vụ dự thảo

Các quy tắc dưới đây là quyết định kỹ thuật/nghiệp vụ cho MVP, không phải quy định pháp luật hoặc kết quả khảo sát.
Không đưa đơn giá cố định ngoài đời vào code. Chủ trọ khai báo giá đã thỏa thuận; cần khảo sát trước khi dùng thật.

## Phòng và hợp đồng

- Mã phòng duy nhất trong một khu trọ. Sức chứa > 0, tiền thuê >= 0.
- MVP cho thuê nguyên phòng, một hợp đồng có hiệu lực tại một thời điểm. Ngăn khoảng thuê chồng nhau.
- Cần định nghĩa rõ ngày kết thúc bao gồm hay không trong RM-001; chưa tự suy đoán trong code.
- Hợp đồng có ngày bắt đầu, giá thuê, người đại diện và chỉ số bàn giao; không cho chỉ số âm.
- Đặt phòng tương lai, trả phòng và gia hạn phức tạp nằm ngoài luồng MVP.
- Không xóa phòng/người thuê/hợp đồng đang được lịch sử tài chính tham chiếu; dùng trạng thái lưu trữ phù hợp.

## Kỳ thu và điện nước

- MVP hỗ trợ kỳ tháng đầy đủ. Tháng lẻ chưa tự tính; giao diện và tài liệu phải nói rõ giới hạn.
- Chỉ số mới >= chỉ số kỳ trước. Không chấp nhận kỳ thu đảo thứ tự hoặc khoảng đọc chồng nhau.
- Một phiếu gốc cho mỗi hợp đồng và kỳ tháng; bảo vệ bằng ràng buộc database, không chỉ kiểm tra if.
- Phí mỗi dòng lưu quantity, unitPrice, unit và amount. Snapshot tên phí/đơn giá/chỉ số lúc phát hành.
- Thay đổi đơn giá cho kỳ sau không được tính lại phiếu đã phát hành.
- Dùng BigDecimal, VND; đề xuất làm tròn từng dòng về đồng bằng HALF_UP rồi cộng tổng. Ghi nhận quyết định ở RM-001.
- Phiếu DRAFT được chỉnh; ISSUED khóa nội dung. Sửa sai phiếu đã phát hành cần thiết kế điều chỉnh riêng.
- Chưa triển khai nghiệp vụ điều chỉnh thì từ chối thao tác, không sửa ngầm số tiền hoặc xóa phiếu.

## Thanh toán

- Người thuê xem phiếu và trạng thái; chỉ chủ trọ ghi nhận tiền thực nhận trong MVP.
- Mỗi khoản thanh toán > 0 và không vượt dư nợ trong MVP. Có transaction/concurrency control để tránh ghi vượt nợ.
- Gửi lại cùng một yêu cầu không tạo hai khoản thanh toán; thiết kế idempotency key và lưu kết quả.
- Mỗi lần trả giữ một bản ghi riêng. Trạng thái UNPAID/PARTIALLY_PAID/PAID suy ra từ tổng tiền xác nhận.
- Quá hạn là thuộc tính theo ngày đến hạn và dư nợ; không làm mất trạng thái trả một phần.
- Tiền cọc được ghi riêng; chưa tự khấu trừ vào phiếu tháng. Quyết toán/hoàn cọc là chức năng sau MVP.
- Không coi ảnh chuyển khoản hoặc thao tác phía client là bằng chứng đã nhận tiền.

## Quyền và thời gian

- Lấy người dùng từ phiên xác thực; không tin userId/role do client khai là quyền truy cập.
- Kiểm tra quyền trên cả API đọc và ghi, bao gồm danh sách, chi tiết và lịch sử.
- TENANT chỉ xem hợp đồng của mình, kể cả lịch sử hợp đồng cũ được phép; không thấy người thuê kế tiếp.
- Ngày kinh doanh theo Asia/Ho_Chi_Minh; lưu timestamp có múi giờ rõ ràng, date cho ngày thuê/kỳ thu.
- Demo dùng dữ liệu giả. Chỉ thu thập dữ liệu thật khi có nhu cầu và cách bảo vệ phù hợp.
