# Trình tự xuất hiện quá mức (Overrepresented Sequences)

## Tóm tắt

Một **thư viện giải trình tự hiệu năng cao bình thường** sẽ chứa **tập hợp các trình tự đa dạng**, trong đó mỗi trình tự riêng lẻ chỉ chiếm **một phần rất nhỏ trong tổng số reads**.

Việc phát hiện **một trình tự đơn lẻ xuất hiện với tần suất rất cao (overrepresented)** có thể:

- Mang **ý nghĩa sinh học quan trọng**, hoặc
- Cho thấy **thư viện bị nhiễm tạp (contamination)**, hoặc
- Cho thấy **mức độ đa dạng của thư viện thấp hơn mong đợi**

---

## Cơ chế hoạt động

### Ngưỡng báo cáo

Mô-đun này sẽ **liệt kê tất cả các trình tự** chiếm:

- **hơn 0.1% tổng số reads**

---

### Phạm vi phân tích

Để **tiết kiệm bộ nhớ**, FastQC chỉ:

- Theo dõi các trình tự xuất hiện trong **100.000 reads đầu tiên**
- Sau đó **tiếp tục theo dõi các trình tự này trong toàn bộ tệp**

Điều này có nghĩa là:

- Một trình tự xuất hiện nhiều nhưng **không nằm ở phần đầu tệp** có thể **không được phát hiện**

---

### Đối chiếu nguồn tạp nhiễm

Đối với mỗi trình tự được phát hiện, FastQC sẽ:

- **So sánh với cơ sở dữ liệu các tác nhân tạp nhiễm phổ biến (common contaminants)**

Sau đó chương trình sẽ:

- Báo cáo **kết quả khớp tốt nhất**

**Điều kiện khớp**

- Độ dài tối thiểu: **20 bp**
- Cho phép tối đa: **1 sai lệch (mismatch)**

> **Lưu ý:**  
> Kết quả khớp **không nhất thiết là nguồn gốc chính xác của tạp nhiễm**, nhưng sẽ giúp **định hướng nguyên nhân**.  
> Ví dụ: các **Adapter sequencing thường có trình tự rất giống nhau**.

---

### Xử lý đoạn đọc dài

Tương tự mô-đun **Duplicate Sequences**:

- Các **đoạn đọc dài hơn 50 bp** sẽ được **cắt xuống 50 bp** trước khi phân tích

Nguyên nhân:

- Các đoạn đọc dài thường chứa **nhiều lỗi giải trình tự**
- Điều này có thể **làm giảm giả tạo tỷ lệ trùng lặp quan sát được**

---

## Cảnh báo (Warning)

Mô-đun sẽ đưa ra **cảnh báo** nếu:

- Có **bất kỳ trình tự nào chiếm hơn 0.1% tổng số reads**

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- Có **bất kỳ trình tự nào chiếm hơn 1% tổng số reads**

---

## Các lý do phổ biến dẫn đến cảnh báo

Mô-đun này **thường xuyên được kích hoạt khi phân tích các thư viện small RNA**.

Trong các thư viện này:

- Các trình tự **không bị phân mảnh ngẫu nhiên**
- Một số trình tự (ví dụ: **miRNA cụ thể**) có thể **xuất hiện tự nhiên với tần suất rất cao**

Do đó:

- **Việc xuất hiện các trình tự overrepresented trong trường hợp này là bình thường**.