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
INSERT INTO Phong (maPhong, tenLoaiPhong, tinhTrang, soTang) VALUES 

-- Tầng 1
('P101', 'SINGLE', N'CONTRONG',1),
('P102', 'DOUBLE', N'CONTRONG',1),
('P103', 'TWIN',   N'CONTRONG',1),
('P104', 'TRIPLE', N'CONTRONG',1),
('P105', 'FAMILY', N'CONTRONG',1),

-- Tầng 2
('P201', 'SINGLE', N'CONTRONG',2),
('P202', 'DOUBLE', N'CONTRONG',2),
('P203', 'TWIN',   N'CONTRONG',2),
('P204', 'TRIPLE', N'CONTRONG',2),
('P205', 'FAMILY', N'CONTRONG',2),

-- Tầng 3
('P301', 'SINGLE', N'CONTRONG',3),
('P302', 'DOUBLE', N'CONTRONG',3),
('P303', 'TWIN',   N'CONTRONG',3),
('P304', 'TRIPLE', N'CONTRONG',3),
('P305', 'FAMILY', N'CONTRONG',3),

-- Tầng 4
('P401', 'SINGLE', N'CONTRONG',4),
('P402', 'DOUBLE', N'CONTRONG',4),
('P403', 'TWIN',   N'CONTRONG',4),
('P404', 'TRIPLE', N'CONTRONG',4),
('P405', 'FAMILY', N'CONTRONG',4);

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
INSERT INTO HoaDon (maHoaDon,maDatPhong, ngayTaoHoaDon, maNhanVien) VALUES 
('HD001','DP01', GETDATE(), '20260002'),
('HD002','DP02', GETDATE(), '20260002'),
('HD003','DP02', GETDATE(), '20260001'),
('HD004','DP03', GETDATE(), '20260001'),
('HD005','DP01', GETDATE(), '20260002');

-- 10. CHI TIẾT HÓA ĐƠN
INSERT INTO ChiTietHoaDon (maHoaDon, soGioO, soNgayO) VALUES 
('HD001', 0, 2),
('HD002', 0, 2),
('HD003', 0, 3),
('HD004', 0, 1),
('HD005', 0, 2);

-- 11. DỊCH VỤ ĐÃ SỬ DỤNG
INSERT INTO DichVuDaSuDung (maDichVuSuDung, maDichVu, maDatPhong, ngaySuDung, soLuong, tongTienDichVu) VALUES 
('DVSD01', 'DV01', 'DP01', GETDATE(), 2, 40000),
('DVSD02', 'DV02', 'DP01', GETDATE(), 1, 50000),
('DVSD03', 'DV03', 'DP02', GETDATE(), 1, 100000),
('DVSD04', 'DV01', 'DP03', GETDATE(), 3, 60000),
('DVSD05', 'DV02', 'DP04', GETDATE(), 2, 100000);