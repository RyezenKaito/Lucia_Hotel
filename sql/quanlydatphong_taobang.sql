

--DROP DATABASE QuanLyDatPhong;
-- Kiểm tra và tạo Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'QuanLyDatPhong')
BEGIN
    CREATE DATABASE QuanLyDatPhong;
END
GO

USE QuanLyDatPhong;
GO

-- =============================================
-- 1. TẠO CÁC BẢNG DANH MỤC (MASTER DATA)
-- =============================================

-- Bảng Khách Hàng
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15),
    ngaySinh DATE,
    soCanCuocCongDan VARCHAR(20)
);

-- Bảng Nhân Viên
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(8) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(15),
    diaChi NVARCHAR(200),
    ngayVaoLam DATE,
    trinhDo NVARCHAR(50) CHECK (trinhDo IN (N'THCS', N'THPT', N'CAODANG', N'DAIHOC')),
    heSoLuong DECIMAL(5,2),
    luongCoBan DECIMAL(18,2),
    matKhau VARCHAR(100),
    vaiTro NVARCHAR(20) CHECK (vaiTro IN (N'NHANVIEN', N'QUANLY')),
    maQuanLy VARCHAR(8),
    CONSTRAINT FK_NhanVien_QuanLy FOREIGN KEY (maQuanLy) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT CK_MaNhanVien_ChuaSo CHECK (maNhanVien LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]')
);

-- Bảng Loại Phòng (Thông tin chung)
CREATE TABLE LoaiPhong_ThongTin (
    tenLoaiPhong VARCHAR(50) PRIMARY KEY,
    tenLoaiHienThi NVARCHAR(100),
    sucChua INT CHECK (sucChua > 0), -- Thêm ở đây
    ngayApDung DATE,
    ngayHetHieuLuc DATE,
    trangThai NVARCHAR(20) CHECK (trangThai IN (N'CONHIEULUC', N'HETHAN'))
);

-- Bảng Loại Phòng (Chi tiết giá)
CREATE TABLE LoaiPhong_ChiTiet (
    maChiTietLoaiPhong VARCHAR(20) PRIMARY KEY,
    tenLoaiPhong VARCHAR(50),
    giaPhong DECIMAL(18,2),
    CONSTRAINT FK_LoaiPhong_ChiTiet FOREIGN KEY (tenLoaiPhong) REFERENCES LoaiPhong_ThongTin(tenLoaiPhong)
);

-- Bảng Dịch Vụ
CREATE TABLE DichVu (
    maDichVu VARCHAR(20) PRIMARY KEY,
    tenDichVu NVARCHAR(100) NOT NULL,
    giaDichVu DECIMAL(18,2),
    loaiDichVu NVARCHAR(50) CHECK (loaiDichVu IN (N'THUCAN', N'MAYGIAT', N'NUOCUONG')),
    mieuTa NVARCHAR(255)
);

-- =============================================
-- 2. TẠO CÁC BẢNG NGHIỆP VỤ PHÒNG
-- =============================================

-- Bảng Phòng
CREATE TABLE Phong (
    maPhong VARCHAR(20) PRIMARY KEY,
    tenPhong NVARCHAR(50),
    tenLoaiPhong VARCHAR(50),
    tinhTrang NVARCHAR(20) CHECK (tinhTrang IN (N'BAN', N'CONTRONG', N'DANGSUDUNG')),
    soPhong INT,
    CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (tenLoaiPhong) REFERENCES LoaiPhong_ThongTin(tenLoaiPhong)
);

-- Bảng Đặt Phòng
CREATE TABLE DatPhong (
    maDatPhong VARCHAR(20) PRIMARY KEY,
    ngayDatPhong DATETIME DEFAULT GETDATE(),
    maKhachHang VARCHAR(20),
    ngayCheckInDuKien DATETIME,
    ngayCheckInThucTe DATETIME,
    ngayCheckOutDuKien DATETIME,
    ngayCheckOutThucTe DATETIME,
    CONSTRAINT FK_DatPhong_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang)
);

-- Bảng Chi Tiết Đặt Phòng
CREATE TABLE ChiTietDatPhong (
    maChiTietDatPhong VARCHAR(20) PRIMARY KEY,
    maPhong VARCHAR(20),
    maDatPhong VARCHAR(20),
    CONSTRAINT FK_CTDP_Phong FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT FK_CTDP_DatPhong FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong)
);

-- =============================================
-- 3. TẠO CÁC BẢNG BẢNG GIÁ & TÀI CHÍNH
-- =============================================

-- Bảng Giá Dịch Vụ (Thông tin chung)
CREATE TABLE BangGiaDichVu_ThongTin (
    maBangGia VARCHAR(20) PRIMARY KEY,
    tenBangGia NVARCHAR(100),
    ngayApDung DATE,
    ngayHetHieuLuc DATE,
    trangThai BIT -- 1: Đang sử dụng, 0: Ngưng sử dụng
);

-- Bảng Giá Dịch Vụ (Chi tiết)
CREATE TABLE BangGiaDichVu_ChiTiet (
    maChiTietBangGia VARCHAR(20) PRIMARY KEY,
    maBangGia VARCHAR(20),
    maDichVu VARCHAR(20),
    giaDichVu DECIMAL(18,2),
    donViTinh NVARCHAR(20),
    soLuong INT,
    CONSTRAINT FK_BGDV_ThongTin FOREIGN KEY (maBangGia) REFERENCES BangGiaDichVu_ThongTin(maBangGia),
    CONSTRAINT FK_BGDV_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
);

-- Bảng Hóa Đơn
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    ngayTaoHoaDon DATETIME DEFAULT GETDATE(),
    maNhanVien VARCHAR(8),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);

-- Bảng Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(20),
    maDatPhong VARCHAR(20),
    soGioO DECIMAL(5,2),
    soNgayO DECIMAL(5,2),
    tongTienThanhToan DECIMAL(18,2),
    PRIMARY KEY (maHoaDon, maDatPhong),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_CTHD_DatPhong FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong)
);

-- Bảng Dịch Vụ Đã Sử Dụng
CREATE TABLE DichVuDaSuDung (
    maDichVuSuDung VARCHAR(20) PRIMARY KEY,
    maDichVu VARCHAR(20),
    maDatPhong VARCHAR(20),
    ngaySuDung DATETIME,
    soLuong INT,
    tongTienDichVu DECIMAL(18,2),
    CONSTRAINT FK_DVSD_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT FK_DVSD_DatPhong FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong)
);

GO
