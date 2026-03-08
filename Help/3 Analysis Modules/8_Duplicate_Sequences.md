# Trình tự trùng lặp (Duplicate Sequences)

## Tóm tắt

Trong một **thư viện trình tự đa dạng**, hầu hết các trình tự **chỉ xuất hiện một lần**.

- **Tỷ lệ trùng lặp tăng nhẹ** có thể cho thấy **độ bao phủ (coverage) rất cao** của trình tự mục tiêu.  
- **Tỷ lệ trùng lặp cao** thường là dấu hiệu của **định kiến làm giàu**, ví dụ như **khuếch đại PCR quá mức**.

---

## Cơ chế hoạt động

### Phạm vi phân tích

Để **giảm tải bộ nhớ**, FastQC chỉ:

- Theo dõi các trình tự xuất hiện trong **100.000 trình tự đầu tiên** của mỗi tệp
- Sau đó **theo dõi các trình tự này xuyên suốt toàn bộ dữ liệu**

---

### Nhóm dữ liệu

Các trình tự có **hơn 10 bản sao** sẽ được:

- **Nhóm lại thành các bin (nhóm dữ liệu)**

Điều này giúp **biểu đồ hiển thị rõ ràng hơn**.

---

### Xử lý trình tự dài

Vì thuật toán yêu cầu **khớp chính xác toàn bộ trình tự**, nên:

- Các **đoạn đọc dài hơn 50 bp** sẽ bị **cắt ngắn xuống 50 bp**

Nguyên nhân:

- Các đoạn đọc dài thường chứa **nhiều lỗi giải trình tự**
- Điều này có thể làm **giảm giả tạo tỷ lệ trùng lặp quan sát được**

---

## Cách đọc biểu đồ

- **Trục X**: Mức độ trùng lặp của trình tự  
- **Trục Y**: Tỷ lệ phần trăm của thư viện được tạo thành từ các trình tự ở mức trùng lặp đó  

### Dự báo mất mát dữ liệu

Con số ở **đầu biểu đồ** cho biết:

- **Tổng lượng dữ liệu dự kiến sẽ bị mất** nếu thực hiện **loại bỏ trùng lặp (deduplication)**.

---

## Cảnh báo (Warning)

Cảnh báo sẽ được đưa ra nếu:

- **Các trình tự không duy nhất (non-unique sequences)** chiếm **hơn 30% tổng số trình tự**

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- **Các trình tự không duy nhất** chiếm **hơn 50% tổng số trình tự**

---

## Các lý do phổ biến dẫn đến cảnh báo

Mô-đun này **giả định thư viện là đa dạng và không được làm giàu**.  
Bất kỳ sự sai khác nào so với giả định này **đều có thể tạo ra trùng lặp**.

---

### 1. Trùng lặp kỹ thuật và sinh học

Có hai nguồn trùng lặp chính:

**Trùng lặp kỹ thuật**

- Do **lỗi trong quá trình PCR**
- Thường gọi là **PCR artefacts**

**Trùng lặp sinh học**

- Do **các mảnh DNA giống hệt nhau được chọn ngẫu nhiên** trong quá trình chuẩn bị thư viện

> **Lưu ý:**  
> Với dữ liệu **FastQ thô**, **không thể phân biệt hai loại trùng lặp này**.

---

### 2. Cạn kiệt tính đa dạng của thư viện

Cảnh báo có thể đơn giản cho thấy:

- Bạn đang **giải trình tự lặp lại cùng một nội dung nhiều lần**
- Điều này **lãng phí công suất giải trình tự**

---

### 3. Đặc thù của một số loại thư viện

Một số loại thư viện **tự nhiên có tỷ lệ trùng lặp cao**.

**RNA-Seq**

- Các **gene biểu hiện mạnh** có thể xuất hiện **rất nhiều bản sao**
- Tỷ lệ trùng lặp cao trong trường hợp này **là bình thường**

**ChIP-Seq**

- Các **vùng DNA được làm giàu** thường có **tỷ lệ trùng lặp cao hơn**

**Thư viện có điểm bắt đầu cố định**

Ví dụ:

- Thư viện dựa trên **restriction enzyme**
- **Small RNA libraries** không phân mảnh

Các thư viện này thường có:

- **điểm bắt đầu đọc cố định**
- dẫn đến **mức độ trùng lặp rất lớn**

---

## Giải pháp

Đối với các loại thư viện có khả năng trùng lặp cao, nên sử dụng:

**UMI (Unique Molecular Identifiers)**

- Đây là **barcode ngẫu nhiên** gắn vào từng phân tử DNA
- Giúp **phân biệt chính xác trùng lặp kỹ thuật và trùng lặp sinh học**