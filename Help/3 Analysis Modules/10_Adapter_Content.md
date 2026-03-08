# Nội dung Adapter (Adapter Content)

## Tóm tắt

Mô-đun **Kmer Content** có thể phân tích tổng quát các **K-mer** để phát hiện **sự phân bố không đồng đều**, bao gồm cả **các trình tự adapter xuất hiện ở cuối đoạn đọc**.

Tuy nhiên, nếu thư viện chứa **các trình tự xuất hiện quá mức** (ví dụ: **adapter dimers**), biểu đồ K-mer có thể bị **các trình tự này chi phối**, khiến việc quan sát các sai lệch khác trở nên khó khăn.

Vì vậy, mô-đun **Adapter Content** được thiết kế để:

- **Tìm kiếm trực tiếp các trình tự adapter đã được định nghĩa trước**
- Giúp đánh giá **liệu thư viện có cần thực hiện adapter trimming hay không**

---

## Cơ chế hoạt động

### Biểu đồ lũy kế

Biểu đồ hiển thị **tỷ lệ phần trăm lũy kế của các đoạn đọc chứa adapter** tại mỗi vị trí.

Khi một **trình tự adapter** được phát hiện tại một vị trí:

- Nó sẽ được **tính là xuất hiện cho đến hết chiều dài đoạn đọc**

Do đó:

- **Tỷ lệ phần trăm trên biểu đồ chỉ có thể tăng hoặc giữ nguyên** khi tiến về cuối đoạn đọc.

---

### Các trình tự đặc biệt khác

Ngoài các **adapter sequencing thông thường**, cấu hình mặc định của FastQC còn tìm kiếm:

**PolyA**

- Rất hữu ích khi phân tích **thư viện RNA-Seq**

**PolyG**

- Một **lỗi kỹ thuật phổ biến trên hệ máy Illumina 2 màu**
- Ví dụ: **NextSeq** hoặc **NovaSeq**

Nguyên nhân:

- **Tín hiệu từ các cụm DNA (clusters) bị mất**, dẫn đến chuỗi **PolyG giả tạo**

---

### Tùy chỉnh danh sách adapter

Bạn có thể **thay đổi danh sách các trình tự được tìm kiếm** bằng cách chỉnh sửa tệp:
