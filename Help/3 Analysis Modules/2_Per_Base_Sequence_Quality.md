# Chất lượng trình tự trên từng bazơ (Per Base Sequence Quality)

## Tóm tắt

Phần này cung cấp **cái nhìn tổng quan về phạm vi giá trị chất lượng (quality values)** của tất cả các bazơ tại mỗi vị trí trong tệp **FastQ**.

Biểu đồ được vẽ theo dạng **Box-Whisker (biểu đồ hộp và râu)** cho mỗi vị trí. Các thành phần của biểu đồ bao gồm:

- **Đường màu đỏ ở giữa**: Giá trị trung vị (**Median**).  
- **Hộp màu vàng**: Khoảng trải giữa (**Inter-quartile range: 25–75%**).  
- **Các đường râu trên và dưới**: Đại diện cho các điểm **10% và 90%**.  
- **Đường màu xanh lam**: Giá trị chất lượng trung bình (**Mean**).

Trục **Y** hiển thị **điểm chất lượng (Quality scores)**.  
Điểm càng cao thì việc **xác định bazơ (base call)** càng chính xác.

Nền của đồ thị được chia thành **3 vùng chất lượng**:

- 🟢 **Vùng xanh lá cây**: Chất lượng rất tốt  
- 🟠 **Vùng màu cam**: Chất lượng chấp nhận được  
- 🔴 **Vùng màu đỏ**: Chất lượng kém  

Chất lượng đọc bazơ trên hầu hết các hệ máy sẽ **giảm dần về cuối quá trình chạy**, vì vậy việc các điểm chất lượng **rơi vào vùng màu cam ở cuối đoạn đọc là điều phổ biến**.

> [!NOTE]  
> FastQC sẽ **tự động xác định phương pháp mã hóa điểm chất lượng** trong tệp FastQ.  
> Trong một số trường hợp hiếm hoi (khi dữ liệu quá tốt), phần mềm có thể **đoán sai phương pháp mã hóa**.  
> **Tiêu đề của biểu đồ** sẽ hiển thị loại mã hóa mà FastQC đang sử dụng.  
>
> Kết quả sẽ **không hiển thị** nếu tệp đầu vào là **BAM/SAM không chứa thông tin điểm chất lượng**.

---

## Cảnh báo (Warning)

Cảnh báo sẽ được đưa ra nếu:

- **Điểm tứ phân vị dưới (Lower Quartile)** của bất kỳ bazơ nào **nhỏ hơn 10**,  
  **HOẶC**
- **Giá trị trung vị (Median)** của bất kỳ bazơ nào **nhỏ hơn 25**.

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- **Điểm tứ phân vị dưới** của bất kỳ bazơ nào **nhỏ hơn 5**,  
  **HOẶC**
- **Giá trị trung vị** của bất kỳ bazơ nào **nhỏ hơn 20**.

---

## Các lý do phổ biến dẫn đến cảnh báo

### 1. Sự suy giảm chất lượng theo thời gian

Đây là **nguyên nhân phổ biến nhất**, đặc biệt trong các **chu kỳ giải trình tự dài**.  
Phản ứng hóa học giải trình tự thường **kém đi khi chiều dài đoạn đọc tăng lên**.

**Cách khắc phục**

- Thực hiện **quality trimming** để cắt bỏ phần cuối của đoạn đọc dựa trên điểm chất lượng trung bình.
- Thường bước này được **kết hợp với việc loại bỏ trình tự adapter**.

---

### 2. Mất chất lượng tạm thời

Đôi khi lỗi xảy ra **giữa quá trình chạy**, ví dụ:

- **bọt khí đi qua flowcell**

Sau đó chất lượng **có thể phục hồi trở lại**.

**Cách khắc phục**

- **Không nên trimming**, vì có thể làm mất các đoạn trình tự tốt phía sau.
- Thay vào đó nên **masking (che) các bazơ lỗi** trong bước **mapping** hoặc **assembly**.

---

### 3. Độ dài đoạn đọc không đồng nhất

Nếu thư viện có các **đoạn đọc với chiều dài khác nhau**, một số vị trí có thể có **coverage rất thấp**, dẫn đến **cảnh báo hoặc lỗi**.

**Lời khuyên**

- Kiểm tra mô-đun **Sequence Length Distribution**
- Xem **bao nhiêu trình tự thực sự gây ra lỗi** trước khi quyết định xử lý.