USE LuciaHT;
GO
ALTER TABLE DatPhong ADD
    trangThai NVARCHAR(20) DEFAULT N'CHO_XACNHAN'
        CHECK (trangThai IN (
            N'CHO_XACNHAN',   -- Mới tạo đơn, chờ xác nhận
            N'DA_XACNHAN',    -- Nhân viên xác nhận, chờ check-in
            N'DA_CHECKIN',    -- Khách đã nhận phòng
            N'DA_CHECKOUT',   -- Khách đã trả phòng (đã có hóa đơn)
            N'DA_HUY'         -- Đơn bị hủy
        ))
;
PRINT N'ALTER bảng DatPhong thành công!';
GO