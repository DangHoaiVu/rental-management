# Phạm vi MVP v0.1

## Chủ thể

Một chủ trọ (LANDLORD) và các người thuê (TENANT). Chưa có nền tảng SaaS cho nhiều chủ trọ.
LANDLORD quản lý toàn bộ dữ liệu thuộc cơ sở của mình. TENANT chỉ truy cập hợp đồng và khoản thu được cấp quyền.
Một tài khoản đại diện người thuê cho mỗi hợp đồng trong MVP. Người ở cùng là thông tin cư trú, chưa có tài khoản riêng.
Chủ trọ tạo/mời người thuê. Không cho đăng ký công khai tự chọn LANDLORD.

## Phải có trong MVP

- Đăng nhập, đăng xuất, lấy thông tin phiên, quyền theo vai trò và theo hợp đồng.
- Tạo khu trọ, phòng; xem, tìm kiếm và cập nhật phòng chưa bị ràng buộc bởi nghiệp vụ.
- Quản lý thông tin người thuê tối thiểu, hợp đồng, ngày bắt đầu/kết thúc, tiền thuê và tiền cọc ghi nhận.
- Chỉ số điện/nước, cấu hình phí, phiếu thu nháp và phát hành theo kỳ.
- Người thuê tra cứu phiếu thu, lịch sử khoản tiền đã được xác nhận.
- Chủ trọ ghi nhận thanh toán toàn bộ hoặc một phần, xem số tiền còn nợ.
- Lưu người thực hiện và thời điểm cho phát hành phiếu, ghi nhận thanh toán và thay đổi hợp đồng.

## Sau MVP

Báo hỏng/ảnh, thông báo, trả phòng và quyết toán cọc, nhiều tài khoản cùng hợp đồng, tính tiền tháng lẻ,
đối soát ngân hàng, tìm trọ công khai, ký điện tử, AI, nhiều chủ trọ độc lập.
Chưa hứa thời gian cố định: ước lượng theo từng ticket sau khi kiểm tra môi trường và phần đã làm.

## Demo nghiệm thu MVP

1. Dùng tài khoản chủ trọ tạo khu A, phòng P101 và hợp đồng với người thuê thử nghiệm.
2. Tạo/chốt chỉ số và phát hành một phiếu thu tháng đầy đủ.
3. Người thuê đăng nhập và thấy đúng chi tiết phiếu của mình.
4. Người thuê khác không truy cập được phiếu dù thay ID trên URL/API.
5. Chủ trọ ghi nhận hai lần thanh toán; hệ thống hiện trả một phần rồi trả đủ.
6. Tải lại trang/khởi động lại ứng dụng, dữ liệu và lịch sử vẫn còn.
7. Có test quan trọng, README chạy được, dữ liệu mẫu giả và kết quả CI có thể kiểm tra.
