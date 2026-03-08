# Thành phần bazơ trên từng vị trí (Per Base Sequence Content)

## Tóm tắt

Biểu đồ **Per Base Sequence Content** thể hiện **tỷ lệ của từng loại trong bốn loại bazơ DNA tiêu chuẩn (A, T, G, C)** tại mỗi vị trí dọc theo **chiều dài đoạn đọc**.

Trong một **thư viện giải trình tự ngẫu nhiên**, bạn sẽ mong đợi rằng **không có sự khác biệt lớn giữa các bazơ khác nhau**. Vì vậy:

- Các **đường biểu diễn trong đồ thị** nên chạy **song song với nhau**.
- Tỷ lệ của mỗi bazơ sẽ phản ánh **thành phần GC tổng thể của bộ gene**.

Tuy nhiên, trong mọi trường hợp, **sự chênh lệch giữa các bazơ không nên quá lớn**.

---

## Lưu ý quan trọng

Một số loại thư viện giải trình tự sẽ **luôn tạo ra thành phần trình tự bị định kiến (biased)**, thường xuất hiện **ở phần đầu của đoạn đọc**.

Các trường hợp phổ biến gồm:

- **Thư viện được tạo bằng mồi lục phân ngẫu nhiên (Random Hexamers)**  
  (bao gồm **hầu hết các thư viện RNA-Seq**)

- **Thư viện được phân mảnh bằng Transposase (Tagmentation)**

Các kỹ thuật này **thừa hưởng định kiến nội tại tại vị trí bắt đầu của đoạn đọc**.

Mặc dù đây là **định kiến kỹ thuật có thật**, nhưng:

- Nó **không thể khắc phục bằng trimming**
- Trong hầu hết các trường hợp **không ảnh hưởng đến phân tích hạ nguồn**

Tuy nhiên, hiện tượng này **vẫn sẽ kích hoạt cảnh báo hoặc lỗi** trong mô-đun này.

---

## Cảnh báo (Warning)

Mô-đun sẽ đưa ra **cảnh báo** nếu:

- Sự khác biệt giữa **A và T**, hoặc **G và C** **lớn hơn 10%** tại bất kỳ vị trí nào.

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- Sự khác biệt giữa **A và T**, hoặc **G và C** **lớn hơn 20%** tại bất kỳ vị trí nào.

---

## Các lý do phổ biến dẫn đến cảnh báo

Có một số **kịch bản phổ biến** thường gây ra cảnh báo hoặc lỗi trong mô-đun này.

### 1. Trình tự xuất hiện quá nhiều (Overrepresented sequences)

Nếu mẫu bị **nhiễm các đoạn Adapter dimers hoặc rRNA**, các trình tự này sẽ:

- Làm **lệch thành phần bazơ tổng thể**
- Khiến **trình tự của chúng lộ rõ trên biểu đồ**

---

### 2. Phân mảnh bị định kiến (Biased fragmentation)

Như đã đề cập, các thư viện như:

- **RNA-Seq**
- **Tagmentation**

thường bị **định kiến ở khoảng 12 bp đầu tiên của đoạn đọc**.

Phần lớn thư viện **RNA-Seq sẽ “thất bại” trong mô-đun này**, nhưng:

- Đây **không phải vấn đề có thể sửa chữa**
- Và **không ảnh hưởng đến việc đo lường biểu hiện gene**

---

### 3. Thư viện có thành phần đặc thù

Một số thư viện **vốn dĩ không cân bằng về thành phần bazơ**.

Ví dụ:

- **Thư viện xử lý bằng Sodium Bisulphite** (dùng nghiên cứu **DNA methylation**)

Trong phương pháp này:

- Hầu hết **Cytosine (C)** sẽ được **chuyển thành Thymine (T)**

Do đó:

- Biểu đồ sẽ **gần như không có Cytosine**
- Điều này **có thể gây lỗi trong FastQC**, nhưng **lại hoàn toàn bình thường đối với loại thư viện này**

---

### 4. Cắt tỉa Adapter quá mức

Nếu bạn **cắt tỉa Adapter quá mạnh tay**, điều này có thể tạo ra **sự sai lệch thành phần bazơ ở cuối đoạn đọc**.

Nguyên nhân:

- Một số trình tự **tình cờ khớp với một đoạn ngắn của Adapter** sẽ bị loại bỏ
- Chỉ còn lại **các trình tự không khớp**

Điều này tạo ra **sự thay đổi đột ngột ở cuối đoạn đọc**, thường chỉ là **sai số kỹ thuật (spurious effect)**.