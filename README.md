# SOAP: Sequence Optimization and Alignment Program

## Giải pháp toàn diện về Kiểm soát chất lượng (QC) và Phân tích trình tự DNA

**SOAP** là một ứng dụng mã nguồn mở được thiết kế để phân tích, đánh giá và tối ưu hóa dữ liệu **giải trình tự hiệu năng cao (High-Throughput Sequencing)**.  

Với khả năng xử lý mạnh mẽ các định dạng dữ liệu thô như **FastQ** và **BAM**, SOAP cung cấp cái nhìn chi tiết về chất lượng thư viện, giúp các nhà nghiên cứu phát hiện sớm các sai sót kỹ thuật trước khi tiến hành các phân tích hạ nguồn phức tạp.


# Giới thiệu hệ thống

Trong kỷ nguyên **giải trình tự thế hệ mới (NGS)**, dữ liệu thô thường chứa đựng các **định kiến kỹ thuật (biases)**, tạp nhiễm adapter hoặc suy giảm chất lượng hóa chất.

SOAP đóng vai trò như một **"màng lọc thông minh"**, thực hiện một chuỗi các thử nghiệm thống kê nhằm đảm bảo **tính toàn vẹn và độ tin cậy của dữ liệu trình tự**.

Mỗi báo cáo từ SOAP được trực quan hóa thông qua hệ thống **chỉ báo trạng thái**:

- ✅ **Pass (Đạt):** Dữ liệu nằm trong ngưỡng phân phối chuẩn.
- ⚠️ **Warning (Cảnh báo):** Có dấu hiệu bất thường (ví dụ: định kiến mồi ngẫu nhiên hoặc nhiễm adapter nhẹ).
- ❌ **Fail (Thất bại):** Dữ liệu có sai lệch nghiêm trọng, cần xử lý hoặc giải trình tự lại.

# Các tính năng cốt lõi

## 1. Phân tích chất lượng theo thời gian thực

SOAP đo lường **điểm chất lượng Phred** tại mỗi chu kỳ giải trình tự, giúp xác định chính xác **thời điểm tín hiệu bắt đầu suy giảm** trong quá trình chạy máy.


## 2. Nhận diện tạp nhiễm (Contamination Detection)

Hệ thống tự động đối chiếu các **trình tự xuất hiện quá mức (overrepresented sequences)** với cơ sở dữ liệu các tác nhân tạp nhiễm phổ biến, bao gồm:

- Adapter dimers  
- Primer sequences  
- Các trình tự rRNA  

Điều này giúp phát hiện sớm các nguồn nhiễm có thể ảnh hưởng đến kết quả phân tích.


## 3. Tối ưu hóa phân phối GC

SOAP đánh giá **hàm lượng GC** của thư viện:

\[
%GC
\]

và so sánh với **mô hình phân phối chuẩn lý thuyết của sinh vật mục tiêu**, từ đó phát hiện các nguồn DNA lạ hoặc sai lệch trong quá trình chuẩn bị thư viện.


## 4. Phân tích K-mer và Định kiến vị trí

SOAP sử dụng **phép thử nhị thức (binomial test)** để phát hiện sự tích tụ bất thường của các **đoạn trình tự ngắn (K-mer)** tại các vị trí cố định trong đoạn đọc.

Phân tích này giúp:

- Phát hiện lỗi trong **quá trình chuẩn bị thư viện**
- Xác định **định kiến mồi (primer bias)**
- Nhận diện **adapter chưa được cắt bỏ**


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


# Cài đặt và Sử dụng

Để bắt đầu với SOAP, vui lòng xem hướng dẫn chi tiết trong **tài liệu cài đặt của dự án**.

Tài liệu bao gồm:

- Cài đặt môi trường
- Chạy phân tích QC
- Xuất báo cáo HTML
- Tích hợp vào pipeline NGS


# Đóng góp và Liên hệ

Chúng tôi luôn chào đón các đóng góp từ cộng đồng **nghiên cứu sinh học và phát triển phần mềm**.

Nếu bạn phát hiện lỗi hoặc có ý tưởng cải tiến, vui lòng:

- Mở một **Issue** trong kho mã nguồn
- Gửi **Pull Request** cho các tính năng mới

Hoặc liên hệ trực tiếp qua email: `dzokha1010@gmail.com`


# Tác giả

Dự án được duy trì và phát triển bởi:

**Nguyễn Văn Kha**  
Chuyên viên, Phòng Sở hữu trí tuệ

Sở Khoa học và Công nghệ TP. Cần Thơ

Việt Nam
