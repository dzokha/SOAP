# Nội dung Kmer (Kmer Content)

## Tóm tắt

Việc phân tích các trình tự xuất hiện quá mức (overrepresented sequences) có thể phát hiện sự gia tăng của bất kỳ trình tự nào bị trùng lặp hoàn toàn. Tuy nhiên, có một số trường hợp mà phương pháp này không hiệu quả:

- **Trình tự dài với chất lượng kém:**  
  Các lỗi giải trình tự ngẫu nhiên có thể làm thay đổi một vài base trong chuỗi, khiến số lượng các trình tự trùng lặp chính xác (exact duplicates) giảm đáng kể và khó phát hiện.

- **Trình tự bộ phận (Partial sequence):**  
  Nếu một đoạn trình tự ngắn xuất hiện ở nhiều vị trí khác nhau trong dữ liệu, nó sẽ không được phát hiện bởi biểu đồ thành phần từng bazơ (*Per Base Sequence Content*) hoặc phân tích trình tự trùng lặp.

Do đó, mô-đun **Kmer Content** được sử dụng để phát hiện các đoạn trình tự ngắn có sự phân bố bất thường trong các vị trí của đoạn đọc.

---

## Cơ chế hoạt động

Mô-đun Kmer giả định rằng bất kỳ đoạn trình tự nhỏ nào cũng **không nên có định kiến về vị trí (positional bias)** khi xuất hiện trong một thư viện đa dạng.

Mặc dù có thể có lý do sinh học khiến một số Kmer nhất định bị làm giàu hoặc cạn kiệt trong toàn bộ dữ liệu, nhưng những thay đổi này **nên ảnh hưởng đồng đều đến tất cả các vị trí trong trình tự**, thay vì tập trung ở một vị trí cụ thể.

---

## Phương pháp

- Mô-đun này đo lường số lượng của mỗi **đoạn 7-mer** tại từng vị trí trong các đoạn đọc của thư viện.
- Sau đó sử dụng **phép thử nhị thức (binomial test)** để tìm kiếm các sai lệch đáng kể so với sự phân bố đồng đều.

---

## Hiển thị

- Bất kỳ Kmer nào có sự làm giàu **không đồng đều theo vị trí** sẽ được báo cáo.
- **6 Kmer có độ lệch cao nhất** sẽ được vẽ biểu đồ để hiển thị sự phân bố của chúng theo từng vị trí trong đoạn đọc.

---

## Tối ưu hóa

Để đảm bảo tốc độ xử lý:

- Chỉ **2% tổng số đoạn đọc** trong thư viện được sử dụng để phân tích.
- Kết quả sau đó được **suy rộng cho toàn bộ dữ liệu**.
- Các trình tự dài hơn **500 bp** sẽ được **cắt ngắn xuống còn 500 bp** trước khi phân tích.

---

## Cảnh báo (Warning)

Mô-đun sẽ đưa ra cảnh báo nếu có bất kỳ Kmer nào mất cân bằng với giá trị:
