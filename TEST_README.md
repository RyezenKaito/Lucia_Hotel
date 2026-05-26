# 🧪 Lucia Hotel – Unit Test Suite

## Tổng quan

Bộ unit test này kiểm thử luồng quản lý nhân viên và đặt phòng của hệ thống khách sạn Lucia theo bảng test case sau:

| STT | Test Case ID | Chức năng | Dữ liệu đầu vào | Kết quả mong muốn | Status |
|-----|--------------|-----------|-----------------|-------------------|--------|
| 1 | NV_01 | Thêm nhân viên | Nhập đầy đủ thông tin hợp lệ | Đối tượng tạo thành công | ✅ Pass |
| 2 | NV_02 | Validate hoTen trống | hoTen = "" | Ném `IllegalArgumentException` | ✅ Pass |
| 3 | NV_03 | Tìm kiếm nhân viên | Keyword "Mai" | Trả về nhân viên chứa "Mai" | ✅ Pass |

---

## Công nghệ sử dụng

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|---------|
| Java | 17 | Ngôn ngữ lập trình |
| JUnit 5 (Jupiter) | 5.10.2 | Test framework |
| Mockito | 5.11.0 | Mock/Stub database layer |
| Maven | 3.x | Build tool & dependency management |

---

## Cấu trúc thư mục

```
src/
  test/
    java/
      fixtures/
        TestDataFixtures.java     ← Dữ liệu mẫu dùng chung cho tất cả test
      model/
        NhanVienModelTest.java    ← NV_01, NV_02, NV_03 (18 test cases)
        DatPhongModelTest.java    ← Validation DatPhong model (10 test cases)
        KhachHangModelTest.java   ← Validation KhachHang model (9 test cases)
      dao/
        NhanVienDAOTest.java      ← DAO layer NhanVien với Mockito (13 test cases)
        DatPhongDAOTest.java      ← DAO layer DatPhong với Mockito (8 test cases)
```

**Tổng số test cases: 58**

---

## Hướng dẫn chạy test

### Yêu cầu

- Java 17+ đã được cài đặt
- Maven 3.6+ đã được cài đặt
- Không cần kết nối SQL Server (test dùng Mockito)

### Chạy toàn bộ test suite

```bash
mvn test
```

### Chạy một class test cụ thể

```bash
# Chỉ chạy test model NhanVien (NV_01, NV_02, NV_03)
mvn test -Dtest=NhanVienModelTest

# Chỉ chạy test DAO NhanVien
mvn test -Dtest=NhanVienDAOTest

# Chỉ chạy test model DatPhong
mvn test -Dtest=DatPhongModelTest

# Chỉ chạy test model KhachHang
mvn test -Dtest=KhachHangModelTest

# Chỉ chạy test DAO DatPhong
mvn test -Dtest=DatPhongDAOTest
```

### Chạy một test case cụ thể

```bash
# Chạy đúng test NV_01
mvn test -Dtest="NhanVienModelTest#NV_01_taoNhanVienHopLe_thanhCong"

# Chạy tất cả test bắt đầu bằng NV_0
mvn test -Dtest="NhanVienModelTest#NV_0*"
```

### Xem kết quả chi tiết

Sau khi chạy, báo cáo XML sẽ ở `target/surefire-reports/`.

---

## Chi tiết các Test Case

### 📁 `NhanVienModelTest` – Validation tầng Model

| ID | Tên test | Mô tả |
|----|----------|-------|
| NV_01 | `NV_01_taoNhanVienHopLe_thanhCong` | Tạo NhanVien đầy đủ thông tin hợp lệ |
| NV_01b | `NV_01b_hoTenDuocLuuDung` | Tên nhân viên được lưu đúng |
| NV_01c | `NV_01c_soDTHopLe` | SĐT 10 chữ số bắt đầu bằng 0 |
| NV_01d | `NV_01d_cccd12ChuSoHopLe` | CCCD 12 chữ số hợp lệ |
| NV_01e | `NV_01e_cccd9ChuSoHopLe` | CMND cũ 9 chữ số hợp lệ |
| NV_01f | `NV_01f_ngaySinhHopLe` | Ngày sinh > 18 tuổi |
| NV_01g | `NV_01g_maNVDinhDang` | Mã NV theo định dạng `LUCIA` + số |
| NV_02 | `NV_02_hoTenTrong_nemException` | `hoTen = ""` ném `IllegalArgumentException` |
| NV_02b | `NV_02b_hoTenNull_nemException` | `hoTen = null` ném exception |
| NV_02c | `NV_02c_hoTenKhoangTrang_nemException` | `hoTen = "   "` ném exception |
| NV_02d | `NV_02d_soDTSai_nemException` | SĐT không đúng định dạng |
| NV_02e | `NV_02e_cccdSai_nemException` | CCCD không đúng định dạng |
| NV_02f | `NV_02f_tuoiDuoi18_nemException` | Tuổi < 18 ném exception |
| NV_02g | `NV_02g_matKhauTrong_nemException` | Mật khẩu rỗng ném exception |
| NV_02h | `NV_02h_ngayVaoLamSauHomNay_nemException` | Ngày vào làm sau hôm nay |
| NV_03 | `NV_03_locNhanVienTheoKeyword_Mai` | Lọc danh sách chứa "Mai" |
| NV_03b | `NV_03b_keywordKhongKhop_danhSachRong` | Keyword không khớp → list rỗng |
| NV_03c | `NV_03c_timKiemKhongPhanBietHoaThuong` | Tìm kiếm không phân biệt hoa thường |

### 📁 `NhanVienDAOTest` – DAO layer với Mockito

| ID | Tên test | Mô tả |
|----|----------|-------|
| NV_01 | `NV_01_insert_nhanVienHopLe_traTrueLuuDB` | `insert()` trả về `true` khi DB thành công |
| NV_01b | `NV_01b_insert_goiPrepareStatementDung` | `insert()` dùng SQL `INSERT INTO NV` |
| NV_01c | `NV_01c_insert_dbKhongCapNhat_traFalse` | `insert()` trả về `false` khi 0 rows |
| NV_01d | `NV_01d_insert_sqlException_traFalse` | `insert()` trả về `false` khi SQLException |
| NV_02 | `NV_02_hoTenTrong_khongQuaValidationModel` | Model validation ngăn trước khi đến DAO |
| NV_03 | `NV_03_findByKeyword_Mai_traVeDanhSachChuaMai` | `findByKeyword("Mai")` trả về đúng 2 kết quả |
| NV_03b | `NV_03b_findByKeyword_dungLikePattern` | SQL dùng `%keyword%` |
| NV_03c | `NV_03c_findByKeyword_khongCoKetQua_danhSachRong` | Không có kết quả → list rỗng |
| NV_03d | `NV_03d_findByKeyword_sqlQueryChinhXac` | SQL chứa `maNV LIKE`, `hoTen LIKE`, `soDT LIKE` |
| NV_04 | `NV_04_update_thanhCong` | `update()` thành công |
| NV_05 | `NV_05_delete_softDelete_thanhCong` | Soft delete (`is_deleted = 1`) |
| NV_06 | `NV_06_authenticate_matKhauDung_traTrue` | Đăng nhập mật khẩu đúng |
| NV_07 | `NV_07_authenticate_matKhauSai_traFalse` | Đăng nhập mật khẩu sai |

### 📁 `DatPhongModelTest` – Validation DatPhong

| ID | Tên test | Mô tả |
|----|----------|-------|
| DP_01 | `DP_01_taoDatPhongHopLe` | Tạo DatPhong hợp lệ |
| DP_02 | `DP_02_trangThaiMacDinh` | Trạng thái mặc định là `CHO_XACNHAN` |
| DP_03 | `DP_03_capNhatTrangThai` | Cập nhật sang `DA_XACNHAN` |
| DP_04 | `DP_04_checkInTruocCheckOut` | Check-in < Check-out |
| DP_05 | `DP_05_ganKhachHang` | Gán KhachHang cho đơn |
| DP_06 | `DP_06_toStringTraVeMaDat` | `toString()` trả về `maDat` |
| DP_07 | `DP_07_getMaDatPhongTuongDuong` | `getMaDatPhong()` tương đương `getMaDat()` |
| DP_08 | `DP_08_soNguoiMacDinh` | `soNguoi` mặc định là 0 |
| DP_09 | `DP_09_capNhatSoNguoi` | Cập nhật số người |
| DP_10 | `DP_10_capNhatNgayThanhToan` | Cập nhật ngày thanh toán |

### 📁 `DatPhongDAOTest` – DAO layer DatPhong

| ID | Tên test | Mô tả |
|----|----------|-------|
| DP_DAO_01 | `DP_DAO_01_insert_hopLe_traTrue` | `insert()` trả về `true` |
| DP_DAO_02 | `DP_DAO_02_insert_goiDungSql` | `insert()` dùng `INSERT INTO DatPhong` |
| DP_DAO_03 | `DP_DAO_03_insert_khongCapNhat_traFalse` | `insert()` trả về `false` khi 0 rows |
| DP_DAO_04 | `DP_DAO_04_insert_trangThaiMacDinh` | `trangThai = null` → set `CHO_XACNHAN` |
| DP_DAO_05 | `DP_DAO_05_insert_soNguoiDefault` | `soNguoi <= 0` → set 1 |
| DP_DAO_06 | `DP_DAO_06_findKhachHangByIdDatPhong_khongTim_traNull` | Không tìm thấy → `null` |
| DP_DAO_07 | `DP_DAO_07_getMaDatPhongCheckInHomNay_rong` | Không có đơn → list rỗng |
| DP_DAO_08 | `DP_DAO_08_insert_sqlException_traFalse` | SQLException → `false` |

### 📁 `KhachHangModelTest` – Validation KhachHang

| ID | Tên test | Mô tả |
|----|----------|-------|
| KH_01 | `KH_01_taoKhachHangHopLe` | Tạo KhachHang với dữ liệu hợp lệ |
| KH_02 | `KH_02_constructorDayDu` | Constructor đầy đủ lưu đúng |
| KH_03 | `KH_03_isBirthdayToday_ngaySinhNull` | `isBirthdayToday` → `false` khi null |
| KH_04 | `KH_04_isBirthdayThisMonth_ngaySinhNull` | `isBirthdayThisMonth` → `false` khi null |
| KH_05 | `KH_05_isBirthdayToday_hienTai` | `isBirthdayToday` → `true` khi sinh hôm nay |
| KH_06 | `KH_06_isBirthdayThisMonth_thangNay` | `isBirthdayThisMonth` → `true` tháng này |
| KH_07 | `KH_07_setterCapNhatTen` | Setter cập nhật tên |
| KH_08 | `KH_08_isDeletedMacDinh` | `isDeleted` mặc định là `false` |
| KH_09 | `KH_09_setDeleted` | `setDeleted(true)` hoạt động đúng |

---

## Kiến trúc test

```
┌─────────────────────────────────────────────────┐
│              Unit Test Suite                    │
├─────────────────┬───────────────────────────────┤
│  Model Tests    │       DAO Tests               │
│  (Pure POJO)    │    (Mockito Stubs)             │
│                 │                               │
│  NhanVien       │   NhanVienDAO                 │
│  DatPhong       │   DatPhongDAO                 │
│  KhachHang      │                               │
├─────────────────┴───────────────────────────────┤
│            TestDataFixtures                     │
│    (Dữ liệu mẫu dùng chung - Fixtures)         │
└─────────────────────────────────────────────────┘
```

- **Model Tests**: Không cần DB, kiểm tra validation logic thuần Java
- **DAO Tests**: Dùng `Mockito.mockStatic` để mock `ConnectDatabase.getInstance()` và giả lập `Connection`, `PreparedStatement`, `ResultSet`
- **TestDataFixtures**: Cung cấp dữ liệu mẫu nhất quán cho tất cả test class

---

## Lưu ý

- Các test **không cần** kết nối SQL Server thực tế
- Các test **không cần** chạy JavaFX
- File `pom.xml` tự động loại trừ các package cần JavaFX (`gui/`, `background/`, `main/`, `model/utils/`) khỏi quá trình biên dịch test
