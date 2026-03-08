# Phân phối độ dài trình tự (Sequence Length Distribution)

## Tóm tắt

Một số **máy giải trình tự hiệu năng cao** tạo ra các **đoạn trình tự có độ dài đồng nhất**, nhưng một số nền tảng khác có thể tạo ra **các đoạn đọc với độ dài thay đổi rất lớn**.

Ngay cả khi **thư viện ban đầu có độ dài đồng nhất**, một số **pipeline phân tích** vẫn có thể thực hiện:

- **Quality trimming** để loại bỏ các bazơ có chất lượng thấp ở cuối đoạn đọc

Điều này dẫn đến **các đoạn trình tự có độ dài khác nhau**.

Mô-đun **Sequence Length Distribution** tạo ra **biểu đồ thể hiện sự phân bố độ dài của các đoạn trình tự** trong tệp đã được phân tích.

Trong nhiều trường hợp:

- Biểu đồ sẽ hiển thị **một đỉnh duy nhất** tại **một độ dài cố định**

Tuy nhiên, đối với các **tệp FastQ có độ dài thay đổi**, biểu đồ sẽ hiển thị:

- **Số lượng tương đối của các đoạn trình tự với các kích thước khác nhau**

---

## Cảnh báo (Warning)

Mô-đun sẽ đưa ra **cảnh báo** nếu:

- **Không phải tất cả các trình tự có cùng độ dài**

---

## Thất bại (Failure)

Mô-đun sẽ **báo lỗi** nếu:

- Có **bất kỳ trình tự nào có độ dài bằng 0 (zero length)**

---

## Các lý do phổ biến dẫn đến cảnh báo

### 1. Nền tảng giải trình tự có độ dài đọc biến thiên

Đối với một số **nền tảng giải trình tự**, việc có **độ dài đoạn đọc khác nhau là hoàn toàn bình thường**.

Ví dụ:

- **PacBio**
- **Nanopore**

Trong các trường hợp này, **cảnh báo của mô-đun có thể được bỏ qua**.

---

### 2. Đã thực hiện trimming trước khi chạy FastQC

Nếu dữ liệu đã được xử lý trước bằng các bước như:

- **Quality trimming**
- **Adapter trimming**

thì việc xuất hiện **các đoạn đọc có độ dài khác nhau** là **hoàn toàn dễ hiểu**.

Do đó, **cảnh báo từ mô-đun này không nhất thiết cho thấy có vấn đề với dữ liệu**.