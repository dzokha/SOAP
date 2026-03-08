# Hàm lượng N trên từng vị trí (Per Base N Content)

## Tóm tắt

Nếu **máy giải trình tự** không thể xác định một bazơ với **độ tin cậy đủ cao**, nó sẽ thay thế bazơ đó bằng **ký tự `N`** thay vì các ký tự **A, T, G, C** thông thường.

Mô-đun **Per Base N Content** vẽ biểu đồ **tỷ lệ phần trăm các lần xác định bazơ là `N` tại mỗi vị trí** dọc theo trình tự.

Việc xuất hiện **một tỷ lệ rất thấp các ký tự `N`** trong trình tự **không phải là điều hiếm gặp**, đặc biệt là **ở phần cuối của đoạn đọc**.

Tuy nhiên, nếu tỷ lệ này **tăng lên trên mức vài phần trăm**, điều đó cho thấy **quy trình phân tích không thể diễn giải dữ liệu đủ tốt** để xác định bazơ hợp lệ.

---

## Cảnh báo (Warning)

Mô-đun sẽ đưa ra **cảnh báo** nếu:

- **Bất kỳ vị trí nào** có **hàm lượng `N` > 5%**

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- **Bất kỳ vị trí nào** có **hàm lượng `N` > 20%**

---

## Các lý do phổ biến dẫn đến cảnh báo

### 1. Suy giảm chất lượng tổng thể

Đây là **nguyên nhân phổ biến nhất** dẫn đến **tỷ lệ `N` cao**.

Khi **chất lượng giải trình tự giảm**, thuật toán **base calling** có thể **không đủ tự tin để xác định bazơ**, dẫn đến việc gán ký tự `N`.

Vì vậy, kết quả của mô-đun này nên được **đánh giá cùng với các mô-đun đánh giá chất lượng khác**, chẳng hạn như:

- **Per Base Sequence Quality**
- **Per Sequence Quality Scores**

> **Lưu ý:**  
> Hãy kiểm tra **độ bao phủ (coverage)** của từng vị trí.  
> Có khả năng **vị trí cuối cùng trong phân tích chỉ chứa rất ít trình tự**, dẫn đến việc **kích hoạt lỗi một cách không chính xác**.

---

### 2. Lỗi cục bộ ở đầu trình tự

Một trường hợp khác thường gặp là:

- **Tỷ lệ `N` cao tại một vài vị trí ở đầu thư viện**
- Trong khi **chất lượng tổng thể của dữ liệu vẫn tốt**

Điều này có thể xảy ra khi:

- **Thư viện có thành phần trình tự cực kỳ định kiến (biased)**

Trong trường hợp này:

- **Thuật toán base calling** có thể **bị nhầm lẫn**
- Và đưa ra **kết quả xác định bazơ kém**

Bạn có thể **xác nhận nguyên nhân này** bằng cách:

- So sánh với kết quả của mô-đun **Per Base Sequence Content**.