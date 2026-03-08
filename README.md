# SOAP: Sequence Optimization and Alignment Program

## Giải pháp toàn diện về Kiểm soát chất lượng (QC) và Phân tích trình tự DNA

**SOAP** là một ứng dụng mã nguồn mở được thiết kế để phân tích, đánh giá và tối ưu hóa dữ liệu **giải trình tự hiệu năng cao (High-Throughput Sequencing)**.  

Với khả năng xử lý mạnh mẽ các định dạng dữ liệu thô như **FastQ** và **BAM**, SOAP cung cấp cái nhìn chi tiết về chất lượng thư viện, giúp các nhà nghiên cứu phát hiện sớm các sai sót kỹ thuật trước khi tiến hành các phân tích hạ nguồn phức tạp.

---

# Giới thiệu hệ thống

Trong kỷ nguyên **giải trình tự thế hệ mới (NGS)**, dữ liệu thô thường chứa đựng các **định kiến kỹ thuật (biases)**, tạp nhiễm adapter hoặc suy giảm chất lượng hóa chất.

SOAP đóng vai trò như một **"màng lọc thông minh"**, thực hiện một chuỗi các thử nghiệm thống kê nhằm đảm bảo **tính toàn vẹn và độ tin cậy của dữ liệu trình tự**.

Mỗi báo cáo từ SOAP được trực quan hóa thông qua hệ thống **chỉ báo trạng thái**:

- ✅ **Pass (Đạt):** Dữ liệu nằm trong ngưỡng phân phối chuẩn.
- ⚠️ **Warning (Cảnh báo):** Có dấu hiệu bất thường (ví dụ: định kiến mồi ngẫu nhiên hoặc nhiễm adapter nhẹ).
- ❌ **Fail (Thất bại):** Dữ liệu có sai lệch nghiêm trọng, cần xử lý hoặc giải trình tự lại.

---

# Các tính năng cốt lõi

## 1. Phân tích chất lượng theo thời gian thực

SOAP đo lường **điểm chất lượng Phred** tại mỗi chu kỳ giải trình tự, giúp xác định chính xác **thời điểm tín hiệu bắt đầu suy giảm** trong quá trình chạy máy.

---

## 2. Nhận diện tạp nhiễm (Contamination Detection)

Hệ thống tự động đối chiếu các **trình tự xuất hiện quá mức (overrepresented sequences)** với cơ sở dữ liệu các tác nhân tạp nhiễm phổ biến, bao gồm:

- Adapter dimers  
- Primer sequences  
- Các trình tự rRNA  

Điều này giúp phát hiện sớm các nguồn nhiễm có thể ảnh hưởng đến kết quả phân tích.

---

## 3. Tối ưu hóa phân phối GC

SOAP đánh giá **hàm lượng GC** của thư viện:

\[
%GC
\]

và so sánh với **mô hình phân phối chuẩn lý thuyết của sinh vật mục tiêu**, từ đó phát hiện các nguồn DNA lạ hoặc sai lệch trong quá trình chuẩn bị thư viện.

---

## 4. Phân tích K-mer và Định kiến vị trí

SOAP sử dụng **phép thử nhị thức (binomial test)** để phát hiện sự tích tụ bất thường của các **đoạn trình tự ngắn (K-mer)** tại các vị trí cố định trong đoạn đọc.

Phân tích này giúp:

- Phát hiện lỗi trong **quá trình chuẩn bị thư viện**
- Xác định **định kiến mồi (primer bias)**
- Nhận diện **adapter chưa được cắt bỏ**

---

# Công nghệ sử dụng

Dự án được xây dựng dựa trên các tiêu chuẩn hiện đại về **hiệu năng và khả năng mở rộng**:

**Backend**

- Java 21  
- Spring Boot 3  
- Tối ưu hóa **đa luồng xử lý tệp tin lớn**

**Frontend**

- React  
- Vite  
- TypeScript  
- Giao diện nhanh, trực quan và dễ sử dụng

**Data Visualization**

- D3.js  
- Trực quan hóa các **biểu đồ sinh tin học chuyên sâu**

**Infrastructure**

- Cloudflare DNS  
- GitHub Actions (CI/CD tự động)

---

# Cài đặt và Sử dụng

Để bắt đầu với SOAP, vui lòng xem hướng dẫn chi tiết trong **tài liệu cài đặt của dự án**.

Tài liệu bao gồm:

- Cài đặt môi trường
- Chạy phân tích QC
- Xuất báo cáo HTML
- Tích hợp vào pipeline NGS

---

# Đóng góp và Liên hệ

Chúng tôi luôn chào đón các đóng góp từ cộng đồng **nghiên cứu sinh học và phát triển phần mềm**.

Nếu bạn phát hiện lỗi hoặc có ý tưởng cải tiến, vui lòng:

- Mở một **Issue** trong kho mã nguồn
- Gửi **Pull Request** cho các tính năng mới

Hoặc liên hệ trực tiếp qua email:

```

[dzokha1010@gmail.com](mailto:dzokha1010@gmail.com)

```

---

# Tác giả

Dự án được duy trì và phát triển bởi:

**Nguyen Van Kha**  
Chuyên viên Công nghệ thông tin  
Cần Thơ, Việt Nam




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

