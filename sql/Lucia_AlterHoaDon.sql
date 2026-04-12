USE LuciaHT;
GO

-- Thêm các cột mới vào bảng HoaDon để hỗ trợ nghiệp vụ thanh toán checkout
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('HoaDon') AND name = 'tienDV')
BEGIN
    ALTER TABLE HoaDon ADD tienDV DECIMAL(18,2) DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('HoaDon') AND name = 'tienCoc')
BEGIN
    ALTER TABLE HoaDon ADD tienCoc DECIMAL(18,2) DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('HoaDon') AND name = 'thueVAT')
BEGIN
    ALTER TABLE HoaDon ADD thueVAT DECIMAL(18,2) DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('HoaDon') AND name = 'tongTien')
BEGIN
    ALTER TABLE HoaDon ADD tongTien DECIMAL(18,2) DEFAULT 0;
END
GO

PRINT N'ALTER bảng HoaDon thành công!';
GO

ALTER TABLE HoaDon ADD 
    trangThaiThanhToan NVARCHAR(20) DEFAULT N'CHUA_THANH_TOAN' 
        CHECK (trangThaiThanhToan IN (
            N'CHUA_THANH_TOAN',   -- Hóa đơn vừa tạo khi đặt phòng
            N'DA_THANH_TOAN',     -- Khách đã thanh toán đầy đủ
            N'THANH_TOAN_MOT_PHAN' -- Khách đã đặt cọc nhưng chưa trả hết
        )),
    phuongThucThanhToan NVARCHAR(30) NULL
        CHECK (phuongThucThanhToan IN (
            N'TIEN_MAT',
            N'THE_TIN_DUNG', 
            N'CHUYEN_KHOAN',
            N'VI_DIEN_TU'
        )),
    ngayThanhToan DATETIME NULL,       -- Thời điểm thanh toán (NULL = chưa thanh toán)
    ghiChuThanhToan NVARCHAR(500) NULL  -- Ghi chú thêm (VD: mã giao dịch, lý do hoàn tiền)
;

PRINT N'ALTER bảng HoaDon thành công!';
GO