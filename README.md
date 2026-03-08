# SOAP
SOAP: Sequence Optimization and Alignment Program

# FastQC: Kiểm soát chất lượng dữ liệu giải trình tự (QC)

**Ứng dụng phân tích và kiểm soát chất lượng cho các tệp FastQ**

FastQC là một công cụ được thiết kế để phát hiện các vấn đề tiềm ẩn trong các tập dữ liệu **giải trình tự hiệu năng cao (High-Throughput Sequencing)**. Chương trình thực hiện một loạt các phân tích trên một hoặc nhiều tệp trình tự thô ở định dạng **FastQ** hoặc **BAM** và tạo ra một báo cáo tổng hợp kết quả.

FastQC sẽ làm nổi bật bất kỳ khu vực nào mà thư viện dữ liệu có dấu hiệu bất thường, giúp người dùng có cái nhìn chi tiết hơn về chất lượng dữ liệu trước khi tiến hành các bước phân tích sâu hơn.

Công cụ này **không phụ thuộc vào bất kỳ công nghệ giải trình tự cụ thể nào**, vì vậy có thể được sử dụng để kiểm tra các thư viện từ nhiều loại thí nghiệm khác nhau, ví dụ:

- Genomic Sequencing
- ChIP-Seq
- RNA-Seq
- BS-Seq
- và các loại dữ liệu NGS khác

---

# Giới thiệu (Introduction)

Hầu hết các máy giải trình tự hiện đại đều tạo ra dữ liệu đầu ra ở **định dạng FastQ**.  

Định dạng này kết hợp:

- **Base calls (xác định bazơ)** – trình tự nucleotide được đọc
- **Quality scores (điểm chất lượng)** – giá trị được mã hóa cho từng bazơ, biểu thị mức độ tin cậy của máy giải trình tự đối với độ chính xác của bazơ đó.

Trước khi thực hiện các bước phân tích sâu hơn (alignment, variant calling, expression analysis…), việc **kiểm tra chất lượng dữ liệu thô** là một bước rất quan trọng. Điều này giúp phát hiện sớm các vấn đề tiềm ẩn có thể ảnh hưởng đến kết quả phân tích sau này.

FastQC tiếp nhận tệp **FastQ** và thực hiện một chuỗi các phép kiểm tra để tạo ra **báo cáo QC toàn diện**.

Mỗi phép kiểm tra sẽ được đánh dấu theo ba mức:

- **Pass** – Dữ liệu đạt tiêu chuẩn
- **Warning** – Có dấu hiệu bất thường cần xem xét
- **Fail** – Sai lệch rõ rệt so với dữ liệu chuẩn

---

> [!IMPORTANT]  
> Các cảnh báo hoặc thậm chí là lỗi **không nhất thiết có nghĩa là dữ liệu của bạn có vấn đề**.  
> Chúng chỉ cho thấy dữ liệu có đặc điểm khác với một tập dữ liệu chuẩn thông thường.  
> Trong nhiều trường hợp, **đặc tính sinh học của mẫu** chính là nguyên nhân tạo ra các sai lệch (bias) này.

---

# Các chế độ sử dụng FastQC

FastQC có thể được sử dụng theo hai cách:

### 1. Ứng dụng đồ họa tương tác (Graphical Interface)

- Cho phép người dùng mở và xem **nhiều tệp cùng lúc**
- Hiển thị các biểu đồ QC trực quan
- Phù hợp để kiểm tra dữ liệu nhanh trước khi phân tích

### 2. Chế độ không tương tác (Command Line)

- Chạy FastQC thông qua **dòng lệnh**
- Phù hợp để tích hợp vào **pipeline phân tích dữ liệu NGS**
- Tự động tạo **báo cáo HTML** cho mỗi tệp được xử lý

---

# Tính tương thích hệ thống

FastQC là một ứng dụng **đa nền tảng**, được viết bằng **Java**.

Về lý thuyết, chương trình có thể chạy trên bất kỳ hệ điều hành nào có **Java Runtime Environment (JRE)** phù hợp.

FastQC hiện đã được kiểm thử ổn định trên:

- **Windows**
- **macOS**
- **Linux**

với các phiên bản **JRE từ v1.6 đến v21**.

---

# Cài đặt (Installation)

Vui lòng tham khảo hướng dẫn cài đặt chi tiết trong **kho lưu trữ của dự án**.

---

# Đóng góp ý kiến (Contributions)

Mọi ý kiến đóng góp hoặc phản hồi về FastQC đều được trân trọng.

Bạn có thể:

- Gửi báo cáo lỗi hoặc đề xuất tính năng thông qua **GitHub Issue Tracker**
- Hoặc liên hệ trực tiếp qua email:

