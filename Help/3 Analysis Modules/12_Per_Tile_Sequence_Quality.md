# Chất lượng trình tự theo từng ô (Per Tile Sequence Quality)

## Tóm tắt

Biểu đồ này chỉ xuất hiện trong kết quả phân tích nếu bạn đang sử dụng thư viện Illumina còn giữ nguyên **mã định danh trình tự gốc (sequence identifiers)**.  

Trong các mã định danh này có chứa thông tin về **ô (tile)** trên bề mặt **flowcell**, nơi mỗi đoạn đọc được sinh ra.

Biểu đồ **Per Tile Sequence Quality** cho phép kiểm tra điểm chất lượng từ mỗi ô trên tất cả các bazơ nhằm xác định xem liệu có sự sụt giảm chất lượng chỉ liên quan đến một phần cụ thể của flowcell hay không.

---

## Cách đọc biểu đồ

### Nội dung

Biểu đồ hiển thị **mức độ sai lệch so với chất lượng trung bình** của mỗi ô.

### Màu sắc

Kết quả được thể hiện theo **thang màu nhiệt độ từ lạnh đến nóng**:

- **Màu lạnh (xanh lam):**  
  Những vị trí có chất lượng **bằng hoặc cao hơn mức trung bình** của lượt chạy.

- **Màu nóng (vàng/đỏ):**  
  Những ô có **chất lượng thấp hơn** so với các ô khác tại cùng vị trí bazơ.

### Mục tiêu

Một biểu đồ lý tưởng nên có **màu xanh lam trên toàn bộ bề mặt**, cho thấy chất lượng trình tự ổn định giữa các ô.

---

## Nguyên nhân gây lỗi

Các vấn đề về chất lượng theo ô có thể xuất phát từ:

- **Sự cố tạm thời:**  
  Ví dụ như **bọt khí đi qua flowcell** trong quá trình giải trình tự.

- **Sự cố lâu dài:**  
  - Vết bẩn trên bề mặt flowcell  
  - Mảnh vụn bị kẹt trong **lane** của flowcell  

Những yếu tố này có thể ảnh hưởng đến khả năng đọc tín hiệu của một số ô cụ thể.

---

## Cảnh báo (Warning)

Mô-đun sẽ đưa ra cảnh báo nếu:

> Bất kỳ ô nào có **điểm Phred trung bình thấp hơn 2 đơn vị** so với mức trung bình chung của bazơ đó trên tất cả các ô.

---

## Thất bại (Failure)

Mô-đun sẽ báo lỗi nếu:

> Bất kỳ ô nào có **điểm Phred trung bình thấp hơn 5 đơn vị** so với mức trung bình chung của bazơ đó trên tất cả các ô.

---

## Các lý do phổ biến dẫn đến cảnh báo

### 1. Sự cố cục bộ trên flowcell

Các cảnh báo thường xuất hiện khi có sự cố chỉ ảnh hưởng đến **một phần nhỏ của flowcell**, chẳng hạn như:

- bọt khí
- vết bẩn
- vật cản trong lane

Những lỗi này thường chỉ xuất hiện trong **một vài chu kỳ hoặc một số ô nhất định**.

### 2. Flowcell bị quá tải (Overloaded)

Sự biến động lớn về điểm Phred giữa các ô cũng có thể xảy ra khi **flowcell bị nạp quá nhiều cụm trình tự (clusters)**.

**Dấu hiệu nhận biết:**

- Các **điểm nóng (hotspots)** xuất hiện **rải rác trên toàn bộ flowcell**
- Không tập trung tại một khu vực hoặc chu kỳ cụ thể

---

## Cách xử lý

- Có thể **bỏ qua** các lỗi nhẹ chỉ ảnh hưởng đến **một số ít ô trong 1–2 chu kỳ**.
- Cần đặc biệt chú ý khi:
  - Độ lệch điểm Phred **lớn**
  - Lỗi **kéo dài qua nhiều chu kỳ liên tiếp**

Trong những trường hợp này, dữ liệu có thể cần được **lọc hoặc đánh giá lại chất lượng thư viện** trước khi phân tích tiếp.