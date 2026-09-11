# Quyết định 0001: Quy tắc hợp đồng và phiếu thu MVP

- **Trạng thái:** Đề xuất cho RM-001
- **Phạm vi:** Thiết kế dữ liệu MVP, chưa phải triển khai
- **Ngày:** 2026-09-11

## Bối cảnh

Tài liệu phạm vi và quy tắc nghiệp vụ yêu cầu cho thuê nguyên phòng, hợp đồng không chồng nhau, kỳ thu tháng đầy đủ, lưu snapshot đơn giá, nhiều lần thanh toán và bảo vệ khi request đồng thời. Các tài liệu cũng nói rõ một số chi tiết đời thực chưa khảo sát. Cần một quy ước nhất quán để thiết kế schema mà không biến giả định thành cam kết nghiệp vụ.

## Quyết định

### Ngày bắt đầu và kết thúc thuê

Khoảng thuê dùng nửa kín `[start_date, end_date)`:

- `start_date` là ngày nhận thuê và được tính trong hợp đồng.
- `end_date` là ngày kết thúc và không được tính trong hợp đồng.
- `end_date = NULL` nghĩa là hợp đồng chưa có ngày kết thúc.
- Nếu có ngày kết thúc, bắt buộc `end_date > start_date`.
- Hai hợp đồng cùng phòng có thể nối tiếp nhau khi hợp đồng trước kết thúc đúng ngày hợp đồng sau bắt đầu; không được giao nhau.

PostgreSQL sẽ bảo vệ quy tắc bằng exclusion constraint trên `daterange`, không chỉ bằng kiểm tra ở service. Chỉ `ACTIVE` và `ENDED` tham gia constraint: `DRAFT` chưa giữ phòng vì chưa kích hoạt, `CANCELLED` không giữ phòng, còn `ENDED` giữ khoảng lịch sử đã thuê để không tạo dữ liệu chồng mâu thuẫn. Lease chuyển sang `ENDED` phải có `end_date`. `version` và audit event bảo vệ việc cập nhật hợp đồng khỏi ghi đè im lặng.

`lease_tenants` có partial UNIQUE để bảo đảm **tối đa một** `REPRESENTATIVE` mỗi lease. Khi activation, transaction phải kiểm tra **đúng một** representative, representative có `tenants.user_id` khác NULL và user đó có role `TENANT`; thiếu hoặc thừa đều bị từ chối. `OCCUPANT` có thể không có tài khoản. Quyền tra cứu MVP chỉ cấp cho representative có tài khoản, không cấp cho occupant.

**Yêu cầu đã có trong tài liệu:** MVP cho thuê nguyên phòng, phải ngăn khoảng thuê chồng nhau, đặt phòng tương lai và trả phòng/gia hạn phức tạp nằm ngoài luồng chính.

**Giả định MVP cần xác nhận bằng khảo sát người dùng thật:** khoảng thuê nửa kín, ngày chuyển tiếp được dùng đồng thời như trên, hợp đồng không có ngày kết thúc được biểu diễn bằng `NULL`, và `ENDED` tiếp tục giữ lịch sử overlap. Đây không phải kết luận pháp lý hay kết quả khảo sát chủ trọ.

### Kỳ thu

MVP chỉ phát hành kỳ tháng đầy đủ:

- `period_start` là ngày đầu tháng.
- `period_end` là ngày đầu tháng kế tiếp và không được tính trong kỳ.
- Một invoice duy nhất cho một `lease` và `period_start`; database đặt `UNIQUE (lease_id, period_start)`.
- Trước khi issue, toàn bộ `[period_start, period_end)` phải nằm trong `[lease.start_date, lease.end_date)`; lease chưa có end date chỉ cần kỳ không bắt đầu trước `start_date`.
- Nếu tháng đầu hoặc tháng cuối chỉ giao với một phần thời gian thuê, hệ thống từ chối issue với lỗi kỳ thuê không đầy đủ; không prorate và không phát hành phiếu tháng lẻ.
- Tháng lẻ, prorating, kỳ tùy ý và cách tính tháng đầu/cuối chưa được hỗ trợ.
- `due_date` được lưu trên invoice. Tạm thời không áp đặt một số ngày cố định ngoài ràng buộc `due_date >= period_start`; quy tắc thực tế sẽ được chốt sau khảo sát.

**Yêu cầu đã có trong tài liệu:** MVP hỗ trợ kỳ tháng đầy đủ; không cho kỳ thu đảo thứ tự hoặc chồng; phiếu gốc bị trùng phải bị chặn bằng database.

**Giả định MVP cần xác nhận bằng khảo sát người dùng thật:** `period_end` loại trừ, invoice tháng dùng ngày đầu tháng làm khóa kỳ, và ngày đến hạn không sớm hơn ngày đầu kỳ.

### Độ chính xác tiền và làm tròn

- Tiền lưu bằng `numeric(19,2)` theo VND, đơn vị nhỏ nhất là đồng; không dùng `float`/`double`.
- Số lượng điện/nước dùng `numeric(19,3)` để không mất phần lẻ của chỉ số.
- Mỗi dòng tính `quantity * unit_price`, sau đó làm tròn về đồng bằng `HALF_UP` trước khi cộng các dòng.
- Tổng invoice bằng tổng các `invoice_lines.amount` đã làm tròn. Không tính lại invoice đã phát hành từ bảng giá hiện tại.
- Giá được chọn theo kỳ sử dụng: chọn rate có hiệu lực tại `period_start` trong phạm vi đúng `property_id + code`. Không lấy rate tại ngày phát hành nếu ngày phát hành muộn hơn đầu kỳ.
- MVP chỉ cho đổi giá từ ngày đầu một kỳ tháng tiếp theo; không tạo khoảng hiệu lực chồng trong cùng `property_id + code`.
- `unit_price`, `quantity`, `amount`, tiền thuê, tiền cọc và payment đều không âm; payment phải lớn hơn 0.
- Đơn giá và mô tả phí được snapshot trên invoice line tại thời điểm phát hành. Bảng `charge_rates` chỉ áp dụng cho kỳ phát hành sau.

**Yêu cầu đã có trong tài liệu:** phải dùng BigDecimal/VND; tài liệu đề xuất làm tròn từng dòng về đồng bằng HALF_UP rồi cộng tổng; thay đổi giá không làm tính lại phiếu đã phát hành.

**Giả định MVP cần xác nhận bằng khảo sát người dùng thật:** precision `(19,2)` và `(19,3)`, coi đồng là đơn vị nhỏ nhất, và áp dụng HALF_UP cho từng dòng thay vì chỉ làm tròn tổng. Quy tắc thuế/phí phát sinh và cách làm tròn ngoài VND chưa thuộc MVP.

### Thanh toán, dư nợ và request đồng thời

Mỗi lần tiền thực nhận tạo một `payments` riêng. Trạng thái `UNPAID`, `PARTIALLY_PAID`, `PAID` là trạng thái suy ra từ tổng payment `CONFIRMED` so với tổng invoice; không cho client gửi trạng thái authoritative.

Khi ghi nhận payment, transaction thực hiện theo đúng thứ tự:

1. Khóa row invoice bằng `SELECT ... FOR UPDATE`.
2. Tìm request theo `(invoice_id, idempotency_key)`.
3. Nếu đã có: cùng `request_fingerprint` thì trả kết quả payment cũ, kể cả invoice hiện đã `PAID`; khác fingerprint thì trả `409 CONFLICT`, không ghi thêm.
4. Nếu chưa có: kiểm tra invoice `ISSUED`, amount > 0 và tổng payment hiện tại chưa vượt dư nợ.
5. Insert payment `CONFIRMED` cùng fingerprint trong transaction rồi commit.

Như vậy retry hợp lệ sau khi phiếu đã trả đủ vẫn thành công theo nghĩa idempotent, còn cùng key nhưng đổi amount hoặc payload không thể tạo payment mới. Constraint trigger database có thể kiểm tra tổng không vượt `invoice.total_amount` như hàng rào phòng thủ; không được khẳng định trigger tổng hợp tự bảo đảm an toàn đồng thời. Bảo vệ đồng thời đến từ việc mọi đường ghi khóa cùng invoice theo cùng thứ tự.

Meter reading dùng CHECK không âm, `meter_type` dài đủ cho `ELECTRICITY`, unique theo lease/loại/ngày và transaction khóa lease. MVP không cho nhập hồi tố: reading mới phải có `reading_date` sau mốc mới nhất của cùng lease và loại công tơ, đồng thời giá trị mới phải `>=` mốc đó. Khi tạo invoice line điện/nước, reading đầu và cuối phải cùng lease, cùng loại meter, ngày đầu trước ngày cuối, và quantity bằng hiệu hai chỉ số; không được dùng lại hoặc chồng khoảng sử dụng đã chốt. Nếu sau này cho nhập hồi tố, phải kiểm tra cả reading liền trước và liền sau cùng transaction và xử lý lại invoice bị ảnh hưởng bằng nghiệp vụ riêng.

Hợp đồng dùng exclusion constraint để chặn overlap; invoice dùng unique constraint để chặn duplicate. Đây là các hàng rào database bên cạnh validation và transaction ở application.

**Yêu cầu đã có trong tài liệu:** payment > 0, không vượt dư nợ; cần idempotency; giữ nhiều dòng payment; chỉ chủ trọ ghi nhận tiền; cần transaction/concurrency control. Chỉ số mới phải >= chỉ số trước.

**Giả định MVP cần xác nhận bằng khảo sát người dùng thật:** payment chỉ phân bổ cho một invoice, không có hoàn tiền/đảo payment, request fingerprint gồm các trường amount/invoice được phép, và khóa lease là đủ cho thứ tự chỉ số. Constraint trigger chỉ là defense-in-depth, không phải cơ chế đồng thời duy nhất.

## Hệ quả

- Invoice `ISSUED`, line, payment, meter reading và audit event không được sửa/xóa bằng luồng thường; muốn sửa sai cần thiết kế adjustment riêng.
- Schema cần PostgreSQL với `btree_gist` cho exclusion constraint và migration versioned bằng Flyway ở RM-002.
- Các constraint liên dòng phải có test PostgreSQL cho overlap, duplicate invoice, giảm chỉ số, thanh toán vượt nợ và hai request đồng thời. Unit test một mình không đủ chứng minh các quy tắc này.
- Nếu khảo sát người dùng thật thay đổi quy tắc tháng lẻ, ngày kết thúc, precision hoặc rounding, cần tạo decision mới và migration/logic tương ứng; không sửa ngầm lịch sử invoice đã phát hành.

## Phạm vi chưa quyết định

Chưa quyết định cách tính tháng đầu/cuối, tiền cọc và hoàn cọc, phí phát sinh ngoài điện/nước, điều chỉnh phiếu, đối soát ngân hàng, nhiều tài khoản trên một hợp đồng và quy tắc ngày đến hạn theo từng chủ trọ. Các mục này không được tự suy ra từ thiết kế MVP.