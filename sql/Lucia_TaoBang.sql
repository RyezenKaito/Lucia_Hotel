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
    maKH     VARCHAR(20)    PRIMARY KEY,
    tenKH    NVARCHAR(100)  NOT NULL,
    soDT     VARCHAR(10),
    ngaySinh DATE,
    soCCCD   VARCHAR(12),
    email    NVARCHAR(100)  NULL   -- Dùng để gửi xác nhận đặt phòng / hóa đơn
);

CREATE TABLE NV (
    maNV        VARCHAR(9)    PRIMARY KEY,
    hoTen       NVARCHAR(100) NOT NULL,
    soDT        VARCHAR(10),
    soCCCD      VARCHAR(12),
    diaChi      NVARCHAR(200),
    ngaySinh    DATE,
    ngayVaoLam  DATE,
    trinhDo     NVARCHAR(20)  CHECK (trinhDo IN (N'THCS', N'THPT', N'CAODANG', N'DAIHOC', N'TREN_DAIHOC')),
    mk          VARCHAR(100),
    role        NVARCHAR(20)  CHECK (role IN (N'NHAN_VIEN', N'QUAN_LY', N'ADMIN')),
    trangThai   NVARCHAR(20)  DEFAULT 'CON_LAM' CHECK (trangThai IN ('CON_LAM', 'DA_NGHI')),
    maQL        VARCHAR(9)    NULL,
    CONSTRAINT FK_NV_QL    FOREIGN KEY (maQL) REFERENCES NV(maNV),
    CONSTRAINT CK_MaNV_Chuan CHECK (maNV = 'ADMIN' OR maNV LIKE 'LUCIA[0-9]%')
);

CREATE TABLE LoaiPhong (
    maLoaiPhong VARCHAR(20)    PRIMARY KEY,   -- SINGLE, DOUBLE, TWIN, TRIPLE, FAMILY
    tenLoaiPhong NVARCHAR(50)  NOT NULL,       -- Tên hiển thị tiếng Việt (VD: "Phòng đơn")
    gia         DECIMAL(18,2)  NOT NULL,
    sucChua     INT            NOT NULL        -- Sức chứa tối đa (người)
);

CREATE TABLE DV (
    maDV     VARCHAR(20)   PRIMARY KEY,
    tenDV    NVARCHAR(100) NOT NULL,
    gia      DECIMAL(18,2),
    -- Dùng ASCII key tránh lỗi collation khi so sánh
    loaiDV   NVARCHAR(20)  CHECK (loaiDV IN (N'THUC_PHAM', N'GIAI_TRI', N'SUC_KHOE', N'TIEN_ICH')),
    mieuTa   NVARCHAR(255),
    donVi    NVARCHAR(20),
    trangThai BIT DEFAULT 0   -- 0: Đang hoạt động, 1: Ẩn
);

-- =============================
-- 2. NGHIỆP VỤ ĐẶT PHÒNG
-- =============================

CREATE TABLE Phong (
    maPhong   VARCHAR(10)   PRIMARY KEY,
    tenPhong  NVARCHAR(50),
    -- Bỏ CHECK trùng với FK; LoaiPhong FK đã đảm bảo giá trị hợp lệ
    loaiPhong VARCHAR(20),
    tinhTrang NVARCHAR(20)  CHECK (tinhTrang IN (N'BAN', N'CONTRONG', N'DANGSUDUNG')),
    soPhong   INT,
    soTang    INT,
    CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (loaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);

CREATE TABLE DatPhong (
    maDat        VARCHAR(20)  PRIMARY KEY,
    ngayDat      DATETIME     NOT NULL DEFAULT GETDATE(),
    maKH         VARCHAR(20),
    -- Dùng DATETIME thay DATE để lưu giờ check-in/out chính xác
    ngayCheckIn  DATETIME,
    ngayCheckOut DATETIME,
    trangThai    NVARCHAR(20) NOT NULL DEFAULT N'CHO_XACNHAN'
                 CHECK (trangThai IN (
                     N'CHO_XACNHAN',  -- Vừa đặt, chờ nhân viên xác nhận
                     N'DA_XACNHAN',   -- NV đã xác nhận, chờ khách đến
                     N'DA_CHECKIN',   -- Khách đang ở
                     N'DA_CHECKOUT',  -- Khách đã trả phòng, hóa đơn đã xuất
                     N'DA_HUY'        -- Đơn bị hủy
                 )),
    CONSTRAINT FK_DatPhong_KH FOREIGN KEY (maKH) REFERENCES KH(maKH)
);

CREATE TABLE ChiTietDatPhong (
    maCTDP   VARCHAR(20)   PRIMARY KEY,
    maPhong  VARCHAR(10),
    maDat    VARCHAR(20),
    giaCoc   DECIMAL(18,2) DEFAULT 0,  -- Tiền cọc riêng từng phòng (không phải hóa đơn)
    soNguoi  INT,
    ghiChu   NVARCHAR(255),
    CONSTRAINT FK_CTDP_Phong    FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT FK_CTDP_DatPhong FOREIGN KEY (maDat)   REFERENCES DatPhong(maDat)
);

-- =============================
-- 3. HOÁ ĐƠN & DỊCH VỤ
-- =============================

CREATE TABLE HoaDon (
    maHD                 VARCHAR(20)   PRIMARY KEY,
    maDat                VARCHAR(20),
    maNV                 VARCHAR(9),
    ngayTaoHD            DATETIME      DEFAULT GETDATE(),  -- Thời điểm xuất hóa đơn (checkout)
    tienPhong            DECIMAL(18,2) DEFAULT 0,
    tienDV               DECIMAL(18,2) DEFAULT 0,
    tienCoc              DECIMAL(18,2) DEFAULT 0,          -- Tổng tiền cọc đã thu (từ ChiTietDatPhong)
    thueVAT              DECIMAL(18,2) DEFAULT 0,
    tongTien             DECIMAL(18,2) DEFAULT 0,          -- = tienPhong + tienDV + thueVAT - tienCoc

    -- Phân loại hóa đơn
    loaiHD               NVARCHAR(30)  NOT NULL DEFAULT N'HOA_DON_PHONG'
                         CHECK (loaiHD IN (
                             N'HOA_DON_PHONG',      -- Hóa đơn thanh toán khi checkout (loại chính)
                             N'HOA_DON_HOAN_TIEN'   -- Hóa đơn hoàn tiền (VD: hủy đặt phòng đã cọc)
                         )),

    -- Trạng thái thanh toán
    trangThaiThanhToan   NVARCHAR(30)  NOT NULL DEFAULT N'CHUA_THANH_TOAN'
                         CHECK (trangThaiThanhToan IN (
                             N'CHUA_THANH_TOAN',     -- Hóa đơn đã xuất nhưng chưa thu tiền
                             N'DA_THANH_TOAN_COC',   -- Đã thu cọc, chưa thanh toán toàn bộ
                             N'DA_THANH_TOAN'        -- Đã thu đủ tiền
                         )),

    -- Thông tin thanh toán (điền khi DA_THANH_TOAN)
    phuongThucThanhToan  NVARCHAR(30)  NULL
                         CHECK (phuongThucThanhToan IN (
                             N'TIEN_MAT', N'THE_TIN_DUNG', N'CHUYEN_KHOAN', N'VI_DIEN_TU'
                         )),
    ngayThanhToan        DATETIME      NULL,   -- Thời điểm thu đủ tiền → bằng chứng khi khiếu nại
    ghiChuThanhToan      NVARCHAR(500) NULL,   -- Mã giao dịch, lý do hoàn tiền, v.v.

    CONSTRAINT FK_HoaDon_DatPhong FOREIGN KEY (maDat) REFERENCES DatPhong(maDat),
    CONSTRAINT FK_HoaDon_NV       FOREIGN KEY (maNV)  REFERENCES NV(maNV)
);

CREATE TABLE ChiTietHoaDon (
    maCTHD         VARCHAR(20)   PRIMARY KEY,
    maHD           VARCHAR(20),
    maCTDP         VARCHAR(20),
    thoiGianLuuTru DECIMAL(5,2),   -- Số đêm lưu trú
    thanhTien      DECIMAL(18,2),  -- Tiền phòng + dịch vụ của dòng này
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD)   REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_CTDP   FOREIGN KEY (maCTDP) REFERENCES ChiTietDatPhong(maCTDP)
);
-- Lưu ý: bỏ soLuongPhong vì mỗi CTDP đã gắn với 1 phòng cụ thể → đếm dòng là đủ

CREATE TABLE DichVuSuDung (
    maDV         VARCHAR(20),
    maCTHD       VARCHAR(20),
    ngaySuDung   DATE,
    soLuong      INT,
    giaDV        DECIMAL(18,2),
    trangThai    BIT DEFAULT 0,  -- 0: chưa tính vào HĐ, 1: đã tính vào HĐ
    CONSTRAINT PK_DVSD   PRIMARY KEY (maDV, maCTHD),
    CONSTRAINT FK_DVSD_DV   FOREIGN KEY (maDV)   REFERENCES DV(maDV),
    CONSTRAINT FK_DVSD_CTHD FOREIGN KEY (maCTHD) REFERENCES ChiTietHoaDon(maCTHD)
);

-- =============================
-- 4. BẢNG GIÁ DỊCH VỤ
-- =============================

CREATE TABLE BangGiaDV_Header (
    maBangGia      VARCHAR(20)    PRIMARY KEY,
    tenBangGia     NVARCHAR(100),
    ngayApDung     DATE,
    ngayHetHieuLuc DATE,
    trangThai      BIT DEFAULT 1  -- 1: đang áp dụng, 0: hết hiệu lực
);

CREATE TABLE BangGiaDV_Detail (
    maBangGia VARCHAR(20),
    maDV      VARCHAR(20),
    giaDV     DECIMAL(18,2),
    ghiChu    NVARCHAR(255),
    CONSTRAINT PK_BGDV_Detail  PRIMARY KEY (maBangGia, maDV),
    CONSTRAINT FK_BGDV_Header  FOREIGN KEY (maBangGia) REFERENCES BangGiaDV_Header(maBangGia),
    CONSTRAINT FK_BGDV_DV      FOREIGN KEY (maDV)      REFERENCES DV(maDV)
);
GO
