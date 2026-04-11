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
