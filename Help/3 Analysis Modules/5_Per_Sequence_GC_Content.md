# Hàm lượng GC trên mỗi trình tự (Per Sequence GC Content)

## Tóm tắt

Mô-đun **Per Sequence GC Content** đo lường **hàm lượng GC trên toàn bộ chiều dài của từng trình tự** trong tệp và **so sánh nó với một mô hình phân phối chuẩn (normal distribution)** của hàm lượng GC.

Trong một **thư viện giải trình tự ngẫu nhiên**, bạn thường sẽ thấy **phân phối GC dạng hình chuông (normal distribution)**.  

- **Đỉnh trung tâm của phân phối** tương ứng với **hàm lượng GC tổng thể của bộ gene nguồn**.

Vì FastQC **không biết trước hàm lượng GC của bộ gene**, nên:

- **Giá trị GC xuất hiện nhiều nhất (modal GC)** sẽ được tính toán từ **dữ liệu thực tế**.
- Giá trị này sau đó được dùng để **xây dựng phân phối tham chiếu**.

---

## Các dấu hiệu bất thường

### 1. Phân phối có hình dạng bất thường

Nếu phân phối **không có dạng hình chuông thông thường**, điều này có thể cho thấy:

- **Thư viện bị nhiễm tạp (contamination)**
- Có **một nhóm trình tự bị định kiến (biased subset)** trong dữ liệu

---

### 2. Phân phối chuẩn nhưng bị lệch

Trong một số trường hợp, phân phối vẫn có **dạng hình chuông**, nhưng **bị lệch sang trái hoặc phải**.

Điều này có thể chỉ ra:

- **Sai lệch hệ thống không phụ thuộc vào vị trí bazơ**

Nếu đường cong vẫn là **một phân phối chuẩn hợp lý**, mô-đun **sẽ không báo lỗi**, vì FastQC **không biết trước hàm lượng GC thực tế của bộ gene** mà bạn đang giải trình tự.

---

## Cảnh báo (Warning)

Cảnh báo sẽ được đưa ra nếu:

- **Tổng sai lệch so với phân phối chuẩn** chiếm **hơn 15% tổng số reads**.

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- **Tổng sai lệch so với phân phối chuẩn** chiếm **hơn 30% tổng số reads**.

---

## Các lý do phổ biến dẫn đến cảnh báo

Cảnh báo trong mô-đun này thường **liên quan đến vấn đề trong quá trình chuẩn bị thư viện**.

### 1. Các đỉnh nhọn (Sharp peaks)

Trên một **đường cong GC vốn dĩ trơn tru**, sự xuất hiện của **đỉnh nhọn** thường là dấu hiệu của:

- **Một loại tạp nhiễm cụ thể**
- Ví dụ: **Adapter dimers**

Những trình tự này thường cũng sẽ xuất hiện trong mô-đun **Overrepresented Sequences**.

---

### 2. Các đỉnh rộng (Broader peaks)

Nếu xuất hiện **một đỉnh rộng hoặc nhiều đỉnh lớn**, điều này có thể cho thấy:

- **Nhiễm DNA từ một loài khác**
- Tức là **contamination with a different species**