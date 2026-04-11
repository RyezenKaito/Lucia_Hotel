--CREATE DATABASE QuanLyDatPhong;
--GO
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'LuciaHT')
BEGIN
    CREATE DATABASE LuciaHT;
END
GO

USE LuciaHT;
GO
-- =============================
-- 1. MASTER DATA
-- =============================

CREATE TABLE KH (
    maKH VARCHAR(20) PRIMARY KEY,
    tenKH NVARCHAR(100) NOT NULL,
    soDT VARCHAR(10),
    ngaySinh DATE,
    soCCCD VARCHAR(12)
);

CREATE TABLE NV (
    maNV VARCHAR(9) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDT VARCHAR(10),
	soCCCD VARCHAR(12),
    diaChi NVARCHAR(200),
	ngaySinh DATE,
    ngayVaoLam DATE,
    trinhDo NVARCHAR(20) CHECK (trinhDo IN (N'THCS', N'THPT', N'CAODANG', N'DAIHOC',N'TREN_DAIHOC')),
    mk VARCHAR(100),
    role NVARCHAR(20) CHECK (role IN (N'NHAN_VIEN', N'QUAN_LY', N'ADMIN')),
    trangThai NVARCHAR(20) DEFAULT 'CON_LAM' CHECK (trangThai IN ('CON_LAM', 'DA_NGHI')),
    maQL VARCHAR(9) NULL,
    CONSTRAINT FK_NV_QL FOREIGN KEY (maQL) REFERENCES NV(maNV),
    CONSTRAINT CK_MaNV_Chuan CHECK (maNV = 'ADMIN' OR maNV LIKE 'LUCIA[0-9]%')
);

CREATE TABLE LoaiPhong (
    maLoaiPhong VARCHAR(20) PRIMARY KEY,
    gia DECIMAL(18,2) NOT NULL,
    sucChua INT NOT NULL,
);

CREATE TABLE DV (
    maDV VARCHAR(20) PRIMARY KEY,
    tenDV NVARCHAR(100) NOT NULL,
    gia DECIMAL(18,2),
    loaiDV NVARCHAR(20) CHECK (loaiDV IN (N'Thực phẩm', N'Giải trí', N'Sức khỏe', N'Tiện ích')),
    mieuTa NVARCHAR(255),
    donVi NVARCHAR(20),
    trangThai INT DEFAULT 0 -- 0: Active, 1: Hidden
);

-- =============================
-- 2. NGHIỆP VỤ ĐẶT PHÒNG
-- =============================

CREATE TABLE Phong (
    maPhong VARCHAR(10) PRIMARY KEY,
    tenPhong NVARCHAR(50),
    loaiPhong VARCHAR(20) CHECK(loaiPhong IN ('DOUBLE','SINGLE','TRIPLE','TWIN','FAMILY')),
    tinhTrang NVARCHAR(20) CHECK (tinhTrang IN (N'BAN', N'CONTRONG', N'DANGSUDUNG')),
    soPhong INT,
	soTang INT,
    CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (loaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);

CREATE TABLE DatPhong (
    maDat VARCHAR(20) PRIMARY KEY,
    ngayDat DATE,
    maKH VARCHAR(20),
    ngayCheckIn DATE,
    ngayCheckOut DATE,
    CONSTRAINT FK_DatPhong_KH FOREIGN KEY (maKH) REFERENCES KH(maKH)
);

CREATE TABLE ChiTietDatPhong (
    maCTDP VARCHAR(20) PRIMARY KEY,
    maPhong VARCHAR(10),
    maDat VARCHAR(20),
    giaCoc DECIMAL(18,2),
    soNguoi INT,
    ghiChu NVARCHAR(255),
    CONSTRAINT FK_CTDP_Phong FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT FK_CTDP_DatPhong FOREIGN KEY (maDat) REFERENCES DatPhong(maDat)
);

-- =============================
-- 3. HOÁ ĐƠN & DỊCH VỤ
-- =============================

CREATE TABLE HoaDon (
    maHD VARCHAR(20) PRIMARY KEY,
    maDat VARCHAR(20),
    maNV VARCHAR(9),
    ngayTaoHD DATE,
    tienPhong DECIMAL(18,2),
    CONSTRAINT FK_HoaDon_DatPhong FOREIGN KEY (maDat) REFERENCES DatPhong(maDat),
    CONSTRAINT FK_HoaDon_NV FOREIGN KEY (maNV) REFERENCES NV(maNV)
);

CREATE TABLE ChiTietHoaDon (
    maCTHD VARCHAR(20) PRIMARY KEY,
    maHD VARCHAR(20),
    maCTDP VARCHAR(20),
    thoiGianLuuTru DECIMAL(5,2),
    soLuongPhong INT,
    thanhTien DECIMAL(18,2),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_CTDP FOREIGN KEY (maCTDP) REFERENCES ChiTietDatPhong(maCTDP)
);

CREATE TABLE DichVuSuDung (
    maDV VARCHAR(20),
    maCTHD VARCHAR(20),
    ngaySuDung DATE,
    soLuong INT,
    giaDV DECIMAL(18,2),
    trangThai BIT,
    CONSTRAINT PK_DVSD PRIMARY KEY (maDV, maCTHD),
    CONSTRAINT FK_DVSD_DV FOREIGN KEY (maDV) REFERENCES DV(maDV),
    CONSTRAINT FK_DVSD_CTHD FOREIGN KEY (maCTHD) REFERENCES ChiTietHoaDon(maCTHD)
);

-- =============================
-- 4. BẢNG GIÁ DỊCH VỤ
-- =============================

CREATE TABLE BangGiaDV_Header (
    maBangGia VARCHAR(20) PRIMARY KEY,
    tenBangGia NVARCHAR(100),
    ngayApDung DATE,
    ngayHetHieuLuc DATE,
    trangThai BIT
);

CREATE TABLE BangGiaDV_Detail (
    maBangGia VARCHAR(20),
    maDV VARCHAR(20),
    giaDV DECIMAL(18,2),
    ghiChu NVARCHAR(255),
    CONSTRAINT PK_BGDV_Detail PRIMARY KEY (maBangGia, maDV),
    CONSTRAINT FK_BGDV_Detail_Header FOREIGN KEY (maBangGia) REFERENCES BangGiaDV_Header(maBangGia),
    CONSTRAINT FK_BGDV_Detail_DV FOREIGN KEY (maDV) REFERENCES DV(maDV)
);
GO
