USE LuciaHT;
GO

-- Xóa constraint cũ (tên tự sinh bởi SQL Server)
ALTER TABLE Phong DROP CONSTRAINT CK__Phong__tinhTrang__4AB81AF0;
GO

-- Thêm constraint mới có BAOTRI
ALTER TABLE Phong ADD CONSTRAINT CK_Phong_tinhTrang
    CHECK (tinhTrang IN (N'BAN', N'CONTRONG', N'DANGSUDUNG', N'BAOTRI'));
GO
