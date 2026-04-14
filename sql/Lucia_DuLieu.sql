USE LuciaHT;
GO

-- KH
INSERT INTO KH (maKH, tenKH, soDT, ngaySinh, soCCCD, email) VALUES
('KH001', N'Nguyễn Minh Tuấn',   '0909030001', '1990-01-01', '030904567890', 'tuan.nguyen@email.com'),
('KH002', N'Trần Thị Ngọc Anh',  '0919230002', '1992-02-02', '031924567891', 'anh.tran@email.com'),
('KH003', N'Lê Văn Đạt',         '0909530003', '1995-10-15', '090954567892', 'dat.le@email.com'),
('KH004', N'Phạm Thu Thủy',      '0919330004', '1993-05-20', '091934567893', 'thuy.pham@email.com'),
('KH005', N'Hoàng Lương Khang',  '0908830005', '1988-12-05', '030884567894', 'khang.hoang@email.com'),
('KH006', N'Đặng Thanh Mai',     '0919930006', '1999-08-30', '031994567895', 'mai.dang@email.com'),
('KH007', N'Vũ Hải Yến',         '0918530007', '1985-03-12', '091854567896', 'yen.vu@email.com'),
('KH008', N'Bùi Quốc Anh',       '0909130008', '1991-11-25', '090914567897', 'anh.bui@email.com'),
('KH009', N'Đỗ Hữu Thiện',       '0909430009', '1994-07-07', '030944567898', 'thien.do@email.com'),
('KH010', N'Ngô Gia Bảo',        '0909730010', '1997-09-09', '030974567899', 'bao.ngo@email.com');

-- NV
-- Thứ tự cột: maNV, hoTen, soDT, soCCCD, diaChi, ngaySinh, ngayVaoLam, trinhDo, mk, role, trangThai, maQL
INSERT INTO NV VALUES
('ADMIN',   N'LUCIA OWNER',         NULL,          NULL,           NULL,       NULL,         NULL,         NULL,        '123', N'ADMIN',     NULL,        NULL),
('LUCIA001',N'Phạm Lê Khánh An',   '0365755828',  '079090909090', N'TP.HCM', '1990-06-07', '2024-02-01', N'DAIHOC',   '123', N'QUAN_LY',   'CON_LAM',   'ADMIN'),
('LUCIA002',N'Hoàng Văn Hưng',       '0365755829',  '079090909091', N'Hà Nội', '1990-04-12', '2024-02-01', N'DAIHOC',   '123', N'QUAN_LY',   'CON_LAM',   'ADMIN'),
('LUCIA003',N'Lưu Anh',             '0355558888',  '079300348888', N'TP.HCM', '2000-05-05', '2024-02-15', N'DAIHOC',   '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA001'),
('LUCIA004',N'Trần Văn A',          '0344448888',  '079200828290', N'Đà Nẵng', '2000-02-02', '2024-03-01', N'CAODANG',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA001'),
('LUCIA005',N'Nguyễn Thị Bằng',      '0344448889',  '079300828291', N'Cần Thơ', '2000-11-20', '2024-03-15', N'CAODANG',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA001'),
('LUCIA006',N'Lê Minh Cường',        '0344448890',  '079200828292', N'Hải Phòng', '2000-08-14', '2024-04-01', N'THPT',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA001'),
('LUCIA007',N'Phan Thị Duyên',       '0344448891',  '079105828293', N'Nha Trang', '2005-01-10', '2024-04-10', N'DAIHOC',   '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA001'),
('LUCIA008',N'Võ Thanh Tú',          '0344448892',  '079203828294', N'Vũng Tàu', '2003-07-22', '2024-05-01', N'CAODANG',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002'),
('LUCIA009',N'Đinh Khải Hoàn',       '0344448893',  '079201828295', N'TP.HCM', '2001-09-09', '2024-05-15', N'DAIHOC',   '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002'),
('LUCIA010',N'Trịnh Ngọc Hà',        '0344448894',  '079197828296', N'Đà Lạt', '1997-12-25', '2024-06-01', N'CAODANG',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002'),
('LUCIA011',N'Lý Quang Vinh',        '0344448895',  '079200828297', N'Hà Tĩnh', '2000-03-30', '2024-06-15', N'THPT',  '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002'),
('LUCIA012',N'Bùi Tuấn Phong',       '0344448896',  '079096828298', N'TP.HCM', '1996-05-12', '2024-07-01', N'DAIHOC',   '123', N'NHAN_VIEN', 'CON_LAM',   'LUCIA002');

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
('P101', N'Phòng 101', 'SINGLE', N'CONTRONG',   101, 1),
('P102', N'Phòng 102', 'DOUBLE', N'CONTRONG', 102, 1),
('P103', N'Phòng 103', 'TWIN',   N'CONTRONG',   103, 1),
('P104', N'Phòng 104', 'TRIPLE', N'CONTRONG',   104, 1),
('P105', N'Phòng 105', 'FAMILY', N'CONTRONG',   105, 1),
('P201', N'Phòng 201', 'SINGLE', N'CONTRONG',   201, 2),
('P202', N'Phòng 202', 'DOUBLE', N'CONTRONG',   202, 2),
('P203', N'Phòng 203', 'TWIN',   N'CONTRONG',   203, 2),
('P204', N'Phòng 204', 'TRIPLE', N'CONTRONG',   204, 2),
('P205', N'Phòng 205', 'FAMILY', N'CONTRONG',   205, 2),
('P301', N'Phòng 301', 'SINGLE', N'CONTRONG',   301, 3),
('P302', N'Phòng 302', 'DOUBLE', N'CONTRONG',   302, 3),
('P303', N'Phòng 303', 'TWIN',   N'CONTRONG',   303, 3),
('P304', N'Phòng 304', 'TRIPLE', N'CONTRONG',   304, 3),
('P305', N'Phòng 305', 'FAMILY', N'CONTRONG',   305, 3),
('P401', N'Phòng 401', 'SINGLE', N'CONTRONG',   401, 4),
('P402', N'Phòng 402', 'DOUBLE', N'CONTRONG',   402, 4),
('P403', N'Phòng 403', 'TWIN',   N'CONTRONG',   403, 4),
('P404', N'Phòng 404', 'TRIPLE', N'CONTRONG',   404, 4),
('P405', N'Phòng 405', 'FAMILY', N'CONTRONG',   405, 4),
('P501', N'Phòng 501', 'SINGLE', N'CONTRONG',   501, 5),
('P502', N'Phòng 502', 'DOUBLE', N'CONTRONG',   502, 5),
('P503', N'Phòng 503', 'TWIN',   N'CONTRONG',   503, 5),
('P504', N'Phòng 504', 'TRIPLE', N'CONTRONG',   504, 5),
('P505', N'Phòng 505', 'FAMILY', N'CONTRONG',   505, 5);

-- DatPhong (thêm trangThai)
-- Thứ tự cột: maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut, trangThai
INSERT INTO DatPhong (maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut, trangThai) VALUES
('DP001', '2026-03-20 14:00', 'KH001', '2026-03-20 14:00', '2026-03-22 12:00', N'DA_CHECKOUT'),
('DP002', '2026-03-21 15:00', 'KH002', '2026-03-21 15:00', '2026-03-23 12:00', N'DA_CHECKOUT');

-- ChiTietDatPhong
INSERT INTO ChiTietDatPhong (maCTDP, maPhong, maDat, giaCoc, soNguoi, ghiChu) VALUES
('CTDP001', 'P101', 'DP001', 300000, 1, N'Không hút thuốc'),
('CTDP002', 'P102', 'DP002', 500000, 2, N'Trẻ em đi kèm');

-- DV (loaiDV đổi sang ASCII key)
-- Thứ tự cột: maDV, tenDV, gia, loaiDV, mieuTa, donVi, trangThai
INSERT INTO DV (maDV, tenDV, gia, loaiDV, mieuTa, donVi, trangThai) VALUES
('DV001', N'Nước suối',  NULL,  N'THUC_PHAM', N'Chai 500ml',    N'Chai', 0),
('DV002', N'Cơm chiên hải sản', NULL,  N'THUC_PHAM', N'Phần 1 người', N'Suất', 0),
('DV003', N'Giặt ủi',   NULL,  N'TIEN_ICH',  N'Gói giặt ủi',  N'Lần',  0),
('DV004', N'Nước ngọt lon', NULL, N'THUC_PHAM', N'Coca/Pepsi 330ml', N'Lon', 0),
('DV005', N'Bia Heineken',  NULL, N'THUC_PHAM', N'Bia lon 330ml',    N'Lon', 0),
('DV006', N'Mì ly',         NULL, N'THUC_PHAM', N'Các loại mì ly',   N'Ly',  0),
('DV007', N'Thuê xe máy',  NULL, N'TIEN_ICH',  N'Xe ga/xe số/ngày', N'Ngày',0),
('DV008', N'Đưa đón sân bay',NULL,N'TIEN_ICH', N'1 chiều/xe 4 chỗ', N'Chuyến',0),
('DV009', N'Bò bít tết',   NULL, N'THUC_PHAM', N'Bò Mỹ kèm khoai tây',N'Phần', 0),
('DV010', N'Bữa tối tại phòng',NULL,N'THUC_PHAM',N'Menu VIP 2 người',N'Suất', 0),
('DV011', N'Spa thư giãn', NULL, N'SUC_KHOE', N'Gói massage 60 phút', N'Lượt', 0),
('DV012', N'Karaoke VIP',  NULL, N'GIAI_TRI', N'Phòng 2 giờ kèm nước', N'Phòng', 0);

-- HoaDon
-- BUG CŨ: INSERT không khai báo tên cột mà chỉ có 5 giá trị → sẽ lỗi vì bảng có nhiều cột hơn.
-- Sửa: khai báo rõ tên cột và bổ sung đủ loaiHD, trangThaiThanhToan, ngayThanhToan
INSERT INTO HoaDon (maHD, maDat, maNV, ngayTaoHD, tienPhong, tienDV, tienCoc, thueVAT, tongTien,
                    loaiHD, trangThaiThanhToan, phuongThucThanhToan, ngayThanhToan) VALUES
('HD001', 'DP001', 'LUCIA002', '2026-03-22 12:30', 600000, 120000, 300000, 0, 620000,
 N'HOA_DON_PHONG', N'DA_THANH_TOAN', N'TIEN_MAT', '2026-03-22 12:45'),
('HD002', 'DP002', 'ADMIN',    '2026-03-23 11:00', 1000000, 100000, 500000, 0, 950000,
 N'HOA_DON_PHONG', N'DA_THANH_TOAN', N'TIEN_MAT', '2026-03-23 13:45');

-- ChiTietHoaDon (bỏ soLuongPhong vì đã xóa cột ra khỏi bảng)
-- Thứ tự cột: maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien
INSERT INTO ChiTietHoaDon (maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien) VALUES
('CTHD001', 'HD001', 'CTDP001', 2, 600000),
('CTHD002', 'HD002', 'CTDP002', 2, 1000000);

-- DichVuSuDung
-- Thứ tự cột: maDV, maCTHD, ngaySuDung, soLuong, giaDV, trangThai
INSERT INTO DichVuSuDung (maDV, maCTHD, ngaySuDung, soLuong, giaDV, trangThai) VALUES
('DV001', 'CTHD001', '2026-03-21', 2,  20000, 1),
('DV002', 'CTHD001', '2026-03-21', 1,  80000, 1),
('DV003', 'CTHD002', '2026-03-22', 1, 100000, 1);

-- BangGiaDV_Header
INSERT INTO BangGiaDV_Header (maBangGia, tenBangGia, ngayApDung, ngayHetHieuLuc, trangThai) VALUES
('BG001', N'Bảng giá tiêu chuẩn 2026', '2026-01-01', '2026-12-31', 1);

-- BangGiaDV_Detail
INSERT INTO BangGiaDV_Detail (maBangGia, maDV, giaDV, ghiChu) VALUES
('BG001', 'DV001',  20000, N'Giá tiêu chuẩn'),
('BG001', 'DV002',  80000, N'Giá tiêu chuẩn'),
('BG001', 'DV003', 100000, N'Giá tiêu chuẩn'),
('BG001', 'DV004',  25000, N'Giá tiêu chuẩn'),
('BG001', 'DV005',  35000, N'Giá tiêu chuẩn'),
('BG001', 'DV006',  20000, N'Giá tiêu chuẩn'),
('BG001', 'DV007', 150000, N'Giá tiêu chuẩn'),
('BG001', 'DV008', 300000, N'Giá tiêu chuẩn'),
('BG001', 'DV009', 150000, N'Giá tiêu chuẩn'),
('BG001', 'DV010', 250000, N'Giá tiêu chuẩn');
GO
