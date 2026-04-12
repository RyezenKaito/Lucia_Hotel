USE LuciaHT;
GO

-- KH
INSERT INTO KH (maKH, tenKH, soDT, ngaySinh, soCCCD, email) VALUES
('KH001', N'Nguyễn Minh Tuấn',   '0901230001', '1990-01-01', '001234567890', 'tuan.nguyen@email.com'),
('KH002', N'Trần Thị Ngọc Anh',  '0901230002', '1992-02-02', '001234567891', 'anh.tran@email.com');

-- NV
-- Thứ tự cột: maNV, hoTen, soDT, soCCCD, diaChi, ngaySinh, ngayVaoLam, trinhDo, mk, role, trangThai, maQL
INSERT INTO NV VALUES
('ADMIN',   N'LUCIA OWNER',         NULL,          NULL,           NULL,       NULL,         NULL,         NULL,        '123', N'ADMIN',     NULL,        NULL),
('LUCIA001',N'Lưu Anh',             '0355558888',  '079205348888', N'TP.HCM', '2005-05-05', '2024-02-01', N'DAIHOC',   '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002'),
('LUCIA002',N'Phạm Lê Khánh An',   '0365755828',  '079205083937', N'TP.HCM', '2005-06-07', '2024-02-01', N'DAIHOC',   '123', N'QUAN_LY',   'CON_LAM',   'ADMIN'),
('LUCIA003',N'Trần Văn A',          '0344448888',  '079205828290', N'TP.HCM', '2005-02-02', '2024-02-01', N'CAODANG',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002');

-- LoaiPhong (thêm tenLoaiPhong)
-- Thứ tự cột: maLoaiPhong, tenLoaiPhong, gia, sucChua
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, gia, sucChua) VALUES
('SINGLE', N'Phòng đơn',        300000, 1),
('DOUBLE', N'Phòng đôi',        500000, 2),
('TWIN',   N'Phòng 2 giường đơn', 600000, 2),
('TRIPLE', N'Phòng ba',         700000, 3),
('FAMILY', N'Phòng gia đình',  1000000, 4);

-- Phong
INSERT INTO Phong (maPhong, tenPhong, loaiPhong, tinhTrang, soPhong, soTang) VALUES
('P101', N'Phòng 101', 'SINGLE', N'CONTRONG',    101, 1),
('P102', N'Phòng 102', 'DOUBLE', N'DANGSUDUNG',  102, 1),
('P201', N'Phòng 201', 'FAMILY', N'CONTRONG',    201, 2);

-- DatPhong (thêm trangThai)
-- Thứ tự cột: maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut, trangThai
INSERT INTO DatPhong (maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut, trangThai) VALUES
('DP001', '2026-03-20 14:00', 'KH001', '2026-03-20 14:00', '2026-03-22 12:00', N'DA_CHECKOUT'),
('DP002', '2026-03-21 15:00', 'KH002', '2026-03-21 15:00', '2026-03-23 12:00', N'DA_CHECKIN');

-- ChiTietDatPhong
INSERT INTO ChiTietDatPhong (maCTDP, maPhong, maDat, giaCoc, soNguoi, ghiChu) VALUES
('CTDP001', 'P101', 'DP001', 100000, 1, N'Không hút thuốc'),
('CTDP002', 'P102', 'DP002', 150000, 2, N'Trẻ em đi kèm');

-- DV (loaiDV đổi sang ASCII key)
-- Thứ tự cột: maDV, tenDV, gia, loaiDV, mieuTa, donVi, trangThai
INSERT INTO DV (maDV, tenDV, gia, loaiDV, mieuTa, donVi, trangThai) VALUES
('DV001', N'Nước suối',  20000,  N'THUC_PHAM', N'Chai 500ml',    N'Chai', 0),
('DV002', N'Ăn sáng',    50000,  N'THUC_PHAM', N'Buffet',        N'Suất', 0),
('DV003', N'Giặt ủi',   100000,  N'TIEN_ICH',  N'Gói giặt ủi',  N'Lần',  0);

-- HoaDon
-- BUG CŨ: INSERT không khai báo tên cột mà chỉ có 5 giá trị → sẽ lỗi vì bảng có nhiều cột hơn.
-- Sửa: khai báo rõ tên cột và bổ sung đủ loaiHD, trangThaiThanhToan, ngayThanhToan
INSERT INTO HoaDon (maHD, maDat, maNV, ngayTaoHD, tienPhong, tienDV, tienCoc, thueVAT, tongTien,
                    loaiHD, trangThaiThanhToan, phuongThucThanhToan, ngayThanhToan) VALUES
('HD001', 'DP001', 'LUCIA002', '2026-03-22 12:30', 600000, 90000, 100000, 0, 590000,
 N'HOA_DON_PHONG', N'CHUA_THANH_TOAN', N'TIEN_MAT',      '2026-03-22 12:45'),
('HD002', 'DP002', 'ADMIN',    '2026-03-23 11:00', 1000000, 100000, 150000, 0, 950000,
 N'HOA_DON_PHONG', N'CHUA_THANH_TOAN', NULL,           NULL);

-- ChiTietHoaDon (bỏ soLuongPhong vì đã xóa cột ra khỏi bảng)
-- Thứ tự cột: maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien
INSERT INTO ChiTietHoaDon (maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien) VALUES
('CTHD001', 'HD001', 'CTDP001', 2, 600000),
('CTHD002', 'HD002', 'CTDP002', 2, 1000000);

-- DichVuSuDung
-- Thứ tự cột: maDV, maCTHD, ngaySuDung, soLuong, giaDV, trangThai
INSERT INTO DichVuSuDung (maDV, maCTHD, ngaySuDung, soLuong, giaDV, trangThai) VALUES
('DV001', 'CTHD001', '2026-03-21', 2,  20000, 1),
('DV002', 'CTHD001', '2026-03-21', 1,  50000, 1),
('DV003', 'CTHD002', '2026-03-22', 1, 100000, 1);

-- BangGiaDV_Header
INSERT INTO BangGiaDV_Header (maBangGia, tenBangGia, ngayApDung, ngayHetHieuLuc, trangThai) VALUES
('BG001', N'Bảng giá tiêu chuẩn 2026', '2026-01-01', '2026-12-31', 1);

-- BangGiaDV_Detail
INSERT INTO BangGiaDV_Detail (maBangGia, maDV, giaDV, ghiChu) VALUES
('BG001', 'DV001',  20000, N'Giá tiêu chuẩn'),
('BG001', 'DV002',  50000, N'Giá tiêu chuẩn'),
('BG001', 'DV003', 100000, N'Giá tiêu chuẩn');
GO
