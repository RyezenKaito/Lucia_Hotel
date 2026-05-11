# PLAN CHI TIẾT SỬA ĐỔI HỆ THỐNG QUẢN LÝ KHÁCH SẠN

## I. THAY ĐỔI UI/GIAO DIỆN

### 1. Xóa cột "Phương thức thanh toán"
- **Vị trí**: Bảng HóaĐơn
- **Hành động**: Xóa hoàn toàn cột này khỏi giao diện
- **Không làm**: Không thay đổi cơ sở dữ liệu

### 2. Chuyển đổi "Loại DV" từ hằng số sang biến số
- **Vị trí**: Bảng Dịch vụ
- **Yêu cầu hiện tại**: Loại DV đang là hằng số cố định
- **Yêu cầu**: 
  - Chuyển thành biến số (có thể thêm/xóa loại DV)
  - Tạo nút "Thêm loại DV mới" trong giao diện quản lý
  - Cho phép người dùng nhập tên loại DV mới
  - Lưu vào SQL (tạo bảng LoaiDV nếu chưa có)

### 3. Thay đổi hành động "Xóa" cho các bảng
- **Áp dụng ẩn (soft delete) cho**:
  - KH (Khách hàng): Bấm xóa → ẩn bản ghi
  - Phòng: Bấm xóa → ẩn bản ghi
  - NV (Nhân viên): Bấm xóa → ẩn bản ghi
  - **Cách làm**: Thêm cột `is_deleted` (TRUE/FALSE) trong SQL, không xóa thực sự dữ liệu

- **Bỏ chức năng xóa hoàn toàn cho**:
  - DV (Dịch vụ): Không hiển thị nút xóa
  - ĐặtPhòng: Không hiển thị nút xóa
  - HóaĐơn: Không hiển thị nút xóa
  - **Lý do**: Liên quan đến doanh thu, không được xóa

---

## II. THÊM CỘT/TRƯỜNG DỮ LIỆU MỚI TRONG SQL

### 1. Bảng ĐặtPhòng
- **Thêm cột**: `so_nguoi` (Số người ở)
- **Kiểu dữ liệu**: INT
- **Giới hạn**: > 0
- **Bắt buộc**: YES

### 2. Bảng HóaĐơn (hoặc bảng Phòng/ĐặtPhòng)
- **Thêm cột**: `phu_thu` (Phụ thu)
  - **Kiểu dữ liệu**: DECIMAL(10, 2)
  - **Giá trị mặc định**: 0
  - **Mô tả**: Tiền phụ thu thêm (nếu có)

- **Thêm cột**: `phu_phi_tra_muon` (Phí trả muộn)
  - **Kiểu dữ liệu**: DECIMAL(10, 2)
  - **Giá trị mặc định**: 0
  - **Mô tả**: Phí phạt khi trả phòng quá giờ quy định

### 3. Bảng ĐặtPhòng
- **Thêm cột**: `ngay_thanh_toan` (Ngày thanh toán)
  - **Kiểu dữ liệu**: DATETIME (hoặc DATE)
  - **Giá trị mặc định**: NULL
  - **Mô tả**: Lưu lại ngày/giờ khách thanh toán (trả phòng)
  - **Cập nhật**: Khi nhấn "Thanh toán" hoặc "Trả phòng", ghi lại ngày giờ hiện tại

---

## III. SỬA LỖI VỀ GIÁ

### 1. Lỗi giá áp dụng của dịch vụ
- **Hiện tượng**: Giá dịch vụ không được áp dụng đúng (cần xác định lỗi cụ thể)
- **Cần kiểm tra**:
  - Giá trong bảng BangGiaDV có phải giá hiện tại không?
  - Ngày áp dụng của BangGiaDV có đúng không?
  - Khi tính tiền dịch vụ, có lấy giá từ BangGiaDV không?
- **Sửa**: Đảm bảo luôn lấy giá từ BangGiaDV với điều kiện ngày hiện tại nằm trong khoảng hiệu lực

### 2. Bảng giá DV - Ngày hết hạn
- **Quy tắc mới**: 
  - Khi tạo BangGiaDV, nếu không nhập "ngày hết hạn" thì tự động = ngày hiện tại + 1 (ngày hôm sau)
  - **Xử lý**: Thêm logic trong form hoặc SQL trigger
- **Ví dụ**:
  - Hôm nay là 2026-05-11 → ngày hết hạn mặc định = 2026-05-12

---

## IV. SỬA VỀ DOANH THU

### 1. Tạo đơn đặt phòng mới
- **Vấn đề**: Trong SQL không cập nhật doanh thu khi tạo đơn ĐặtPhòng
- **Cần làm**:
  - Khi tạo ĐặtPhòng, tính doanh thu tối thiểu (giá phòng × số ngày)
  - Cập nhật cột `doanh_thu` hoặc `tong_tien` vào SQL
  - **Công thức**: `doanh_thu = gia_phong × so_dem + tong_dich_vu` (nếu có dịch vụ)
  - Không để doanh_thu = NULL hoặc 0 khi đã có đơn đặt

### 2. Khi thanh toán (Trả phòng)
- **Vấn đề**: Chưa lưu ngày thanh toán vào SQL
- **Cần làm**:
  - Khi nhấn "Thanh toán" hoặc "Trả phòng", lưu `ngay_thanh_toan = GETDATE()` vào cột trong SQL
  - Tính toán các khoản phụ thu, phí trả muộn nếu có
  - **Công thức tổng tiền**:
    ```
    tong_tien = gia_phong × so_dem + tong_dich_vu + phu_thu + phu_phi_tra_muon
    ```
  - Cập nhật toàn bộ thông tin thanh toán vào SQL

---

## V. DANH SÁCH KIỂM TRA TRƯỚC KHI PROMPT AI

### Câu hỏi cần làm rõ:

1. **Bảng LoaiDV có tồn tại chưa?** 
   - Nếu chưa → AI cần tạo bảng này
   - Nếu rồi → AI chỉ cập nhật form UI

2. **Cơ sở dữ liệu hiện tại sử dụng gì?**
   - SQL Server? MySQL? PostgreSQL?
   - → Cú pháp SQL sẽ khác

3. **Lỗi giá dịch vụ cụ thể là gì?**
   - Giá quá cao? Quá thấp? Không tính?
   - → Cần xem code hiện tại để AI sửa đúng

4. **"Ẩn" có nghĩa là gì?**
   - Không hiển thị trong danh sách nhưng vẫn giữ dữ liệu?
   - Hay là xóa hoàn toàn?
   - → Thường là soft delete (thêm cột `is_deleted`)

5. **Phí trả muộn tính như thế nào?**
   - % phí? Giá cố định? Tính theo giờ?
   - → Cần có công thức rõ ràng

---

## VI. CẤU TRÚC PROMPT CHO AI (ANTIGRAVITY)

### **Phần 1: Giới thiệu**
```
Tôi có 1 hệ thống quản lý khách sạn cần sửa chữa.
QUAN TRỌNG: Chỉ làm ĐÚNG những gì tôi yêu cầu dưới đây. 
Không tự sửa code ẩn, không thêm tính năng không yêu cầu.
```

### **Phần 2: Liệt kê thay đổi UI**
```
THAY ĐỔI GIAO DIỆN:
1. Xóa cột "Phương thức thanh toán" khỏi bảng HóaĐơn
2. Thêm nút "Thêm loại DV mới" trong quản lý Dịch vụ
3. Thay đổi nút "Xóa":
   - KH, Phòng, NV: Bấm xóa → ẩn (soft delete)
   - DV, ĐặtPhòng, HóaĐơn: Xóa nút "Xóa" hoàn toàn
```

### **Phần 3: Thay đổi SQL**
```
THAY ĐỔI CƠ SỞ DỮ LIỆU:
1. Bảng ĐặtPhòng:
   - Thêm cột: so_nguoi (INT, bắt buộc)
   - Thêm cột: ngay_thanh_toan (DATETIME, NULL)
   
2. Bảng HóaĐơn (hoặc ĐặtPhòng):
   - Thêm cột: phu_thu (DECIMAL 10,2, mặc định 0)
   - Thêm cột: phu_phi_tra_muon (DECIMAL 10,2, mặc định 0)

3. Bảng LoaiDV (tạo nếu chưa có):
   - id (INT, PRIMARY KEY)
   - ten_loai (VARCHAR 100)
   - [...]

4. Bảng KH, Phòng, NV:
   - Thêm cột: is_deleted (BIT, mặc định 0)
```

### **Phần 4: Sửa logic doanh thu**
```
LOGIC DOANH THU:
1. Tạo đơn ĐặtPhòng:
   - Tính: doanh_thu = gia_phong × so_dem + tong_dich_vu
   - Lưu vào SQL ngay, không để NULL

2. Khi thanh toán (trả phòng):
   - Lưu: ngay_thanh_toan = GETDATE()
   - Tính: tong_tien = gia_phong × so_dem + tong_dich_vu + phu_thu + phu_phi_tra_muon
   - Cập nhật đầy đủ vào SQL
```

### **Phần 5: Sửa lỗi giá**
```
SỬA LỖI GIÁ DỊCH VỤ:
[Chèn phần này sau khi xác định lỗi cụ thể]
- [Mô tả lỗi cụ thể nào]
- [Cách sửa]
- [Code cũ nếu có]
```

### **Phần 6: Nút bấm "Thêm loại DV"**
```
THÊM LOẠI DV MỚI:
1. UI: Thêm nút "Thêm loại DV" trong trang quản lý Dịch vụ
2. Form: Hiển thị popup/modal yêu cầu nhập:
   - Tên loại DV (text input, bắt buộc)
   - [Nút "Lưu" và "Hủy"]
3. Logic: Khi nhấn "Lưu":
   - Validate: Tên loại DV không được để trống, không trùng
   - INSERT vào bảng LoaiDV
   - Refresh danh sách LoaiDV trong form
   - Hiển thị thông báo thành công
```

### **Phần 7: Quy tắc ngày hết hạn BangGiaDV**
```
NGÀY HẾT HẠN BẢNG GIÁ:
- Khi tạo BangGiaDV:
  - Nếu người dùng không nhập "ngày hết hạn" → tự động = GETDATE() + 1 ngày
  - Nếu người dùng nhập → lấy giá trị người dùng nhập
  - SQL: INSERT [...]  ngay_het_han = ISNULL(@ngay_het_han, DATEADD(day, 1, GETDATE()))
```

### **Phần 8: Chỉ dẫn quan trọng**
```
⚠️ TUÂN THỦ TUYỆT ĐỐI:
✓ CHỈ sửa những gì tôi liệt kê ở trên
✓ Không thay đổi logic khác
✓ Không thêm tính năng mới không yêu cầu
✓ Không tự fix lỗi ẩn
✓ Khi code xong, liệt kê CHI TIẾT những gì đã sửa
✓ Nếu phát hiện lỗi khác, HỎI TÔI trước khi sửa

🚫 KHÔNG ĐƯỢC:
✗ Sửa code ẩn (không yêu cầu)
✗ Thay đổi cấu trúc dữ liệu ngoài yêu cầu
✗ Xóa hàm/tính năng không yêu cầu
✗ Thêm thư viện/package mới không xin phép
```

---

## VII. BƯỚC THỰC HIỆN CỤ THỂ

### **Bước 1: Xác nhận thông tin (trước khi yêu cầu AI)**
- [ ] Xác định CSDL (SQL Server/MySQL/PostgreSQL)
- [ ] Xác định ngôn lập trình backend (C#/.NET/Node.js/Python)
- [ ] Xác định framework frontend (React/Vue/Angular)
- [ ] Xác định file cấu hình SQL hiện tại
- [ ] Xác định vị trí các file code (route, controller, service)

### **Bước 2: Prompt AI - Phần 1 (Thay đổi SQL)**
- Yêu cầu AI tạo migration/script SQL
- AI tạo file SQL (`migration_[timestamp].sql`)
- Bạn review, test trên database test trước

### **Bước 3: Prompt AI - Phần 2 (Backend logic)**
- Yêu cầu AI sửa service/controller
- Sửa logic tính doanh thu, giá dịch vụ
- Thêm endpoint cho "Thêm loại DV"

### **Bước 4: Prompt AI - Phần 3 (Frontend UI)**
- Yêu cầu AI xóa cột, thêm nút
- Sửa form, logic hiển thị nút xóa
- Test UI xem các thay đổi có đúng không

### **Bước 5: Test toàn bộ**
- Test tạo ĐặtPhòng → kiểm tra doanh thu
- Test thanh toán → kiểm tra ngày_thanh_toan, tính phí
- Test soft delete → kiểm tra dữ liệu không bị xóa thực sự
- Test thêm loại DV mới → kiểm tra hiển thị trong form

---

## VIII. TEMPLATE PROMPT CHO AI (SAO CHÉP TRỰC TIẾP)

```
---START PROMPT---

🎯 NHIỆM VỤ: Sửa hệ thống quản lý khách sạn theo yêu cầu cụ thể

⚠️ NGUYÊN TẮC VÀNG:
- Chỉ làm ĐÚNG những gì liệt kê dưới đây
- KHÔNG tự fix lỗi ẩn, KHÔNG thêm tính năng ngoài yêu cầu
- Khi code xong, liệt kê CHI TIẾT từng thay đổi

📋 THAY ĐỔI YÊU CẦU:

=== PHẦN 1: UI ===
1. Xóa cột "Phương thức thanh toán" từ bảng HóaĐơn
2. Thêm nút "Thêm loại DV mới" trong trang quản lý Dịch vụ
3. Nút xóa:
   - KH, Phòng, NV: Ẩn bản ghi (soft delete, thêm cột is_deleted)
   - DV, ĐặtPhòng, HóaĐơn: Xóa nút "Xóa" hoàn toàn

=== PHẦN 2: DATABASE ===
- Thêm vào bảng ĐặtPhòng:
  - so_nguoi INT NOT NULL
  - ngay_thanh_toan DATETIME NULL

- Thêm vào bảng HóaĐơn:
  - phu_thu DECIMAL(10,2) DEFAULT 0
  - phu_phi_tra_muon DECIMAL(10,2) DEFAULT 0

- Thêm vào bảng KH, Phòng, NV:
  - is_deleted BIT DEFAULT 0

- Tạo bảng LoaiDV nếu chưa có

=== PHẦN 3: LOGIC ===
1. Khi tạo ĐặtPhòng: Tính và lưu doanh_thu = gia_phong × so_dem + tong_dich_vu
2. Khi thanh toán: Lưu ngay_thanh_toan = hiện tại, tính tong_tien = gia_phong × so_dem + tong_dich_vu + phu_thu + phu_phi_tra_muon
3. Ngày hết hạn BangGiaDV: Nếu không nhập = GETDATE() + 1 ngày
4. Nút "Thêm loại DV": Form popup nhập tên loại, validate, INSERT vào LoaiDV

🔍 LIỆT KỀ CHI TIẾT TỪNG THAY ĐỔI SAU CHỈNH SỬA

---END PROMPT---
```

---

## IX. GHI CHÚ QUAN TRỌNG

1. **Luôn chuẩn bị câu hỏi chi tiết** trước khi prompt AI
2. **Test từng bước** không nên chỉnh toàn bộ một lần
3. **Giữ backup** database trước khi apply thay đổi
4. **Documen lại** những gì AI sửa để reference sau
5. **Tránh câu lệnh mơ hồ** - càng cụ thể càng tốt
