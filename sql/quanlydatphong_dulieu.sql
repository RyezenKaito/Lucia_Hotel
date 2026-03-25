USE QuanLyDatPhong;
GO

-- 1. CHÈN KHÁCH HÀNG
INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCanCuocCongDan) VALUES
('KH00000001', N'Nguyễn Minh Tuấn', '0901230001', '1990-01-01', '001234567890'),
('KH00000002', N'Trần Thị Ngọc Anh', '0901230002', '1992-02-02', '001234567891'),
('KH00000003', N'Lê Hoàng Nam', '0901230003', '1994-03-03', '001234567892'),
('KH00000004', N'Phạm Quốc Bảo', '0901230004', '1996-04-04', '001234567893'),
('KH00000005', N'Đỗ Thị Mai Linh', '0901230005', '1998-05-05', '001234567894');

-- 2. CHÈN NHÂN VIÊN (Mã 8 chữ số, vai trò viết đầy đủ)
INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, diaChi, ngayVaoLam, trinhDo, heSoLuong, luongCoBan, matKhau, vaiTro, maQuanLy) VALUES 
('20260001', N'Trần Thị Mai', '0907654321', N'TP.HCM', GETDATE(), N'DAIHOC', 3.0, 15000000, '123', N'QUANLY', NULL),
('20260002', N'Nguyễn Văn An', '0901234567', N'Hà Nội', GETDATE(), N'CAODANG', 2.5, 8000000, '123', N'NHANVIEN', '20260001');

-- 3. CHÈN LOẠI PHÒNG (Thông tin chung)
INSERT INTO LoaiPhong_ThongTin (tenLoaiPhong, tenLoaiHienThi, sucChua, ngayApDung, ngayHetHieuLuc, trangThai) 
VALUES 
('SINGLE', N'Phòng Đơn', 1, '2025-01-01', '2026-12-31', N'CONHIEULUC'),
('DOUBLE', N'Phòng Đôi', 2, '2025-01-01', '2026-12-31', N'CONHIEULUC'),
('TWIN',   N'Phòng Hai Giường', 2, '2025-01-01', '2026-12-31', N'CONHIEULUC'),
('TRIPLE', N'Phòng Ba Người', 3, '2025-01-01', '2026-12-31', N'CONHIEULUC'),
('FAMILY', N'Phòng Gia Đình', 4, '2025-01-01', '2026-12-31', N'CONHIEULUC');

-- 4. CHÈN GIÁ LOẠI PHÒNG (Chi tiết)
INSERT INTO LoaiPhong_ChiTiet (maChiTietLoaiPhong, tenLoaiPhong, giaPhong) VALUES 
('CTLP01', 'SINGLE', 300000),
('CTLP02', 'DOUBLE', 500000),
('CTLP03', 'TWIN', 550000),
('CTLP04', 'TRIPLE', 700000),
('CTLP05', 'FAMILY', 1000000);

-- 5. CHÈN DỊCH VỤ (Loại dịch vụ viết đầy đủ)
INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, loaiDichVu, mieuTa) VALUES 
('DV01', N'Nước suối', 20000, N'NUOCUONG', N'Chai 500ml'),
('DV02', N'Ăn sáng', 50000, N'THUCAN', N'Buffet'),
('DV03', N'Giặt ủi', 100000, N'MAYGIAT', N'Gói giặt ủi');

-- 6. CHÈN PHÒNG
INSERT INTO Phong (maPhong, tenPhong, tenLoaiPhong, tinhTrang, soPhong) VALUES 

-- Tầng 1
('P101', N'Phòng 101', 'SINGLE', N'CONTRONG', 101),
('P102', N'Phòng 102', 'DOUBLE', N'CONTRONG', 102),
('P103', N'Phòng 103', 'TWIN',   N'CONTRONG', 103),
('P104', N'Phòng 104', 'TRIPLE', N'CONTRONG', 104),
('P105', N'Phòng 105', 'FAMILY', N'CONTRONG', 105),

-- Tầng 2
('P201', N'Phòng 201', 'SINGLE', N'CONTRONG', 201),
('P202', N'Phòng 202', 'DOUBLE', N'CONTRONG', 202),
('P203', N'Phòng 203', 'TWIN',   N'CONTRONG', 203),
('P204', N'Phòng 204', 'TRIPLE', N'CONTRONG', 204),
('P205', N'Phòng 205', 'FAMILY', N'CONTRONG', 205),

-- Tầng 3
('P301', N'Phòng 301', 'SINGLE', N'CONTRONG', 301),
('P302', N'Phòng 302', 'DOUBLE', N'CONTRONG', 302),
('P303', N'Phòng 303', 'TWIN',   N'CONTRONG', 303),
('P304', N'Phòng 304', 'TRIPLE', N'CONTRONG', 304),
('P305', N'Phòng 305', 'FAMILY', N'CONTRONG', 305),

-- Tầng 4
('P401', N'Phòng 401', 'SINGLE', N'CONTRONG', 401),
('P402', N'Phòng 402', 'DOUBLE', N'CONTRONG', 402),
('P403', N'Phòng 403', 'TWIN',   N'CONTRONG', 403),
('P404', N'Phòng 404', 'TRIPLE', N'CONTRONG', 404),
('P405', N'Phòng 405', 'FAMILY', N'CONTRONG', 405);

-- 7. CHÈN ĐẶT PHÒNG
INSERT INTO DatPhong (maDatPhong, ngayDatPhong, maKhachHang, ngayCheckInDuKien, ngayCheckInThucTe, ngayCheckOutDuKien, ngayCheckOutThucTe) VALUES 
('DP01', GETDATE(), 'KH00000001', '2026-03-20', '2026-03-20', '2026-03-22', NULL),
('DP02', GETDATE(), 'KH00000002', '2026-03-21', '2026-03-21', '2026-03-23', NULL),
('DP03', GETDATE(), 'KH00000003', '2026-03-22', '2026-03-22', '2026-03-25', NULL),
('DP04', GETDATE(), 'KH00000004', '2026-03-23', '2026-03-23', '2026-03-24', NULL),
('DP05', GETDATE(), 'KH00000005', '2026-03-24', '2026-03-24', '2026-03-26', NULL);

-- 8. CHI TIẾT ĐẶT PHÒNG
INSERT INTO ChiTietDatPhong (maChiTietDatPhong, maPhong, maDatPhong) VALUES 
('CTDP01', 'P101', 'DP01'),
('CTDP02', 'P102', 'DP02'),
('CTDP03', 'P201', 'DP03'),
('CTDP04', 'P202', 'DP04'),
('CTDP05', 'P301', 'DP05');

-- 9. HÓA ĐƠN
INSERT INTO HoaDon (maHoaDon, ngayTaoHoaDon, maNhanVien) VALUES 
('HD001', GETDATE(), '20260002'),
('HD002', GETDATE(), '20260002'),
('HD003', GETDATE(), '20260001'),
('HD004', GETDATE(), '20260001'),
('HD005', GETDATE(), '20260002');

-- 10. CHI TIẾT HÓA ĐƠN
INSERT INTO ChiTietHoaDon (maHoaDon, maDatPhong, soGioO, soNgayO) VALUES 
('HD001', 'DP01', 0, 2),
('HD002', 'DP02', 0, 2),
('HD003', 'DP03', 0, 3),
('HD004', 'DP04', 0, 1),
('HD005', 'DP05', 0, 2);

-- 11. DỊCH VỤ ĐÃ SỬ DỤNG
INSERT INTO DichVuDaSuDung (maDichVuSuDung, maDichVu, maDatPhong, ngaySuDung, soLuong, tongTienDichVu) VALUES 
('DVSD01', 'DV01', 'DP01', GETDATE(), 2, 40000),
('DVSD02', 'DV02', 'DP01', GETDATE(), 1, 50000),
('DVSD03', 'DV03', 'DP02', GETDATE(), 1, 100000),
('DVSD04', 'DV01', 'DP03', GETDATE(), 3, 60000),
('DVSD05', 'DV02', 'DP04', GETDATE(), 2, 100000);