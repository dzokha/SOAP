# Điểm chất lượng trên mỗi trình tự (Per Sequence Quality Scores)

## Tóm tắt

Báo cáo **Per Sequence Quality Scores** cho phép bạn kiểm tra xem có **một nhóm nhỏ các trình tự (subset)** nào có **giá trị chất lượng thấp trên toàn bộ chiều dài của chúng** hay không.

Trong thực tế, đôi khi sẽ xuất hiện **một nhóm trình tự có chất lượng kém đồng nhất**, thường do các lỗi như:

- **Lỗi hình ảnh (imaging errors)**
- Ví dụ: các trình tự nằm ở **rìa trường nhìn của máy giải trình tự**

Tuy nhiên, những trình tự này **chỉ nên chiếm một tỷ lệ nhỏ trong tổng dữ liệu**.

Nếu **một tỷ lệ đáng kể các trình tự** trong một lần chạy có **chất lượng tổng thể thấp**, điều này có thể cho thấy **một vấn đề mang tính hệ thống**. Ví dụ:

- Chỉ xảy ra với **một phần của lần chạy**
- Chẳng hạn như **một đầu của flowcell**

> [!NOTE]  
> Kết quả từ mô-đun này **sẽ không hiển thị** nếu tệp đầu vào là **BAM/SAM không chứa thông tin điểm chất lượng**.

---

## Cảnh báo (Warning)

Cảnh báo sẽ được đưa ra nếu **giá trị chất lượng trung bình (Mean Quality)** xuất hiện phổ biến nhất **nhỏ hơn 27**.

- Giá trị này tương ứng với **tỷ lệ lỗi khoảng 0.2%**.

---

## Thất bại (Failure)

Lỗi sẽ được báo nếu **giá trị chất lượng trung bình phổ biến nhất** **nhỏ hơn 20**.

- Giá trị này tương ứng với **tỷ lệ lỗi khoảng 1%**.

---

## Các lý do phổ biến dẫn đến cảnh báo

Mô-đun này nhìn chung **khá ổn định**, vì vậy khi xuất hiện lỗi hoặc cảnh báo, điều đó thường **phản ánh sự suy giảm chất lượng tổng quát của quá trình giải trình tự**.

### 1. Các lần chạy dài (Long sequencing runs)

Trong các lần chạy dài, **chất lượng tổng thể của các trình tự có thể giảm dần theo thời gian**.

**Cách khắc phục**

- Thực hiện **quality trimming** để loại bỏ phần cuối có chất lượng thấp của các đoạn đọc.

---

### 2. Phân phối hai đỉnh (Bi-modal) hoặc phân phối phức tạp

Nếu biểu đồ **xuất hiện hai đỉnh (bi-modal)** hoặc có **hình dạng bất thường**, bạn nên:

- Đánh giá kết quả **kết hợp với mô-đun Per-tile Quality** (nếu có).

Việc này giúp xác định:

- Liệu sự suy giảm chất lượng của một nhóm trình tự có phải do **lỗi vật lý trên flowcell**  
- Hay do **vấn đề liên quan đến hóa chất trong quá trình giải trình tự**.