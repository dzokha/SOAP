# Số liệu thống kê cơ bản (Basic Statistics)

## Tóm tắt

Mô-đun **Basic Statistics** tạo ra các **số liệu thống kê đơn giản** về thành phần của tệp dữ liệu đã được phân tích.

## Các thông số bao gồm

- **Filename (Tên tệp)**  
  Tên gốc của tệp tin được phân tích.

- **File type (Loại tệp)**  
  Cho biết tệp chứa **dữ liệu đọc bazơ thực tế (base calls)** hay **dữ liệu không gian màu (colorspace)** cần được chuyển đổi.

- **Encoding (Mã hóa)**  
  Cho biết loại **mã hóa ASCII** nào được sử dụng cho các **giá trị chất lượng (quality values)** trong tệp này.

- **Total Sequences (Tổng số trình tự)**  
  Tổng số lượng **trình tự đã được xử lý**. Có hai giá trị được báo cáo:
  - **Actual (Thực tế)**
  - **Estimated (Ước tính)**

  **Lưu ý:** Hiện tại hai giá trị này **luôn giống nhau**. Trong tương lai, chương trình có thể chỉ phân tích **một phần dữ liệu** để tăng tốc độ. Tuy nhiên, vì các trình tự lỗi thường **không phân bố đều**, nên tính năng này **tạm thời bị tắt**.

- **Filtered Sequences (Trình tự đã lọc)**  
  Nếu chạy ở **chế độ Casava**, các trình tự bị **đánh dấu lọc bỏ** sẽ được loại ra khỏi tất cả các phân tích.  
  Số lượng các trình tự bị loại bỏ sẽ được báo cáo tại đây.

  **Lưu ý:** Mục **Total Sequences** ở trên **không bao gồm** các trình tự đã lọc này.

- **Sequence Length (Chiều dài trình tự)**  
  Cung cấp **độ dài của trình tự ngắn nhất và dài nhất** trong tập dữ liệu.  
  Nếu tất cả các trình tự có **cùng độ dài**, chỉ **một giá trị duy nhất** sẽ được hiển thị.

- **%GC**  
  Tỷ lệ phần trăm tổng số bazơ **G (Guanine)** và **C (Cytosine)** trên **tổng số tất cả các bazơ** trong tất cả các trình tự.

## Cảnh báo (Warning)

Mô-đun **Basic Statistics** **không bao giờ đưa ra cảnh báo**.

## Thất bại (Failure)

Mô-đun **Basic Statistics** **không bao giờ báo lỗi**.