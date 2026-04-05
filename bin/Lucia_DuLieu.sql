USE LuciaHT;
GO

-- KH
INSERT INTO KH VALUES
('KH01', N'Nguyễn Minh Tuấn', '0901230001', '1990-01-01', '001234567890'),
('KH02', N'Trần Thị Ngọc Anh', '0901230002', '1992-02-02', '001234567891');

-- NV
INSERT INTO NV VALUES
('LUCIA0001', N'Trần Thị Mai', '0907654321', N'TP.HCM','2005-01-01' ,'2024-01-01', N'DAIHOC', 3.0, 15000000, '123', N'QL', NULL),
('LUCIA0002', N'Nguyễn Văn An', '0901234567', N'TP.HCM','2005-05-05' ,'2024-02-01', N'CAODANG', 2.5, 8000000, '123', N'NV', 'LUCIA0001');

-- LoaiPhong
INSERT INTO LoaiPhong VALUES
('SINGLE', 300000, 1),
('DOUBLE', 500000, 2),
('FAMILY', 1000000, 4);

-- Phong
INSERT INTO Phong VALUES
('P101', N'Phòng 101', 'SINGLE', N'CONTRONG', 101,1),
('P102', N'Phòng 102', 'DOUBLE', N'DANGSUDUNG', 102,1),
('P201', N'Phòng 201', 'FAMILY', N'CONTRONG', 201,2);

-- DatPhong
INSERT INTO DatPhong VALUES
('DP01', '2026-03-20', 'KH01', '2026-03-20', '2026-03-22'),
('DP02', '2026-03-21', 'KH02', '2026-03-21', '2026-03-23');

-- ChiTietDatPhong
INSERT INTO ChiTietDatPhong VALUES
('CTDP01', 'P101', 'DP01', 100000, 1, N'Không hút thuốc'),
('CTDP02', 'P102', 'DP02', 150000, 2, N'Trẻ em đi kèm');

-- DV
INSERT INTO DV VALUES
('DV01', N'Nước suối', 20000, N'NUOCUONG', N'Chai 500ml', N'Chai'),
('DV02', N'Ăn sáng', 50000, N'THUCAN', N'Buffet', N'Suất'),
('DV03', N'Giặt ủi', 100000, N'MAYGIAT', N'Gói giặt ủi', N'Lần');

-- HoaDon
INSERT INTO HoaDon VALUES
('HD01', 'DP01', 'LUCIA0002', '2026-03-22', 600000),
('HD02', 'DP02', 'LUCIA0001', '2026-03-23', 1000000);

-- ChiTietHoaDon
INSERT INTO ChiTietHoaDon VALUES
('CTHD01', 'HD01', 48, 1, 600000),
('CTHD02', 'HD02', 48, 1, 1000000);

-- DichVuSuDung
INSERT INTO DichVuSuDung VALUES
('DV01', 'HD01', '2026-03-21', 2, 20000, 1),
('DV02', 'HD01', '2026-03-21', 1, 50000, 1),
('DV03', 'HD02', '2026-03-22', 1, 100000, 1);

-- BangGiaDV_Header
INSERT INTO BangGiaDV_Header VALUES
('BG01', N'Bảng giá tiêu chuẩn 2026', '2026-01-01', '2026-12-31', 1);

-- BangGiaDV_Detail
INSERT INTO BangGiaDV_Detail VALUES
('BG01', 'DV01', 20000, N'Giá tiêu chuẩn'),
('BG01', 'DV02', 50000, N'Giá tiêu chuẩn'),
('BG01', 'DV03', 100000, N'Giá tiêu chuẩn');
GO