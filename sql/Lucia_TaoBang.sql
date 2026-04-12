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
    email    NVARCHAR(100)  NULL   -- DÃ¹ng Ä‘á»ƒ gá»­i xÃ¡c nháº­n Ä‘áº·t phÃ²ng / hÃ³a Ä‘Æ¡n
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
    tenLoaiPhong NVARCHAR(50)  NOT NULL,       -- TÃªn hiá»ƒn thá»‹ tiáº¿ng Viá»‡t (VD: "PhÃ²ng Ä‘Æ¡n")
    gia         DECIMAL(18,2)  NOT NULL,
    sucChua     INT            NOT NULL        -- Sá»©c chá»©a tá»‘i Ä‘a (ngÆ°á»i)
);

CREATE TABLE DV (
    maDV     VARCHAR(20)   PRIMARY KEY,
    tenDV    NVARCHAR(100) NOT NULL,
    gia      DECIMAL(18,2) NULL,
    -- DÃ¹ng ASCII key trÃ¡nh lá»—i collation khi so sÃ¡nh
    loaiDV   NVARCHAR(20)  CHECK (loaiDV IN (N'THUC_PHAM', N'GIAI_TRI', N'SUC_KHOE', N'TIEN_ICH')),
    mieuTa   NVARCHAR(255),
    donVi    NVARCHAR(20),
    trangThai BIT DEFAULT 0   -- 0: Đang phục vụ, 1: Tạm ngưng phục vụ
);

-- =============================
-- 2. NGHIá»†P Vá»¤ Äáº¶T PHÃ’NG
-- =============================

CREATE TABLE Phong (
    maPhong   VARCHAR(10)   PRIMARY KEY,
    tenPhong  NVARCHAR(50),
    -- Bá» CHECK trÃ¹ng vá»›i FK; LoaiPhong FK Ä‘Ã£ Ä‘áº£m báº£o giÃ¡ trá»‹ há»£p lá»‡
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
    -- DÃ¹ng DATETIME thay DATE Ä‘á»ƒ lÆ°u giá» check-in/out chÃ­nh xÃ¡c
    ngayCheckIn  DATETIME,
    ngayCheckOut DATETIME,
    trangThai    NVARCHAR(20) NOT NULL DEFAULT N'CHO_XACNHAN'
                 CHECK (trangThai IN (
                     N'CHO_XACNHAN',  -- Vá»«a Ä‘áº·t, chá» nhÃ¢n viÃªn xÃ¡c nháº­n
                     N'DA_XACNHAN',   -- NV Ä‘Ã£ xÃ¡c nháº­n, chá» khÃ¡ch Ä‘áº¿n
                     N'DA_CHECKIN',   -- KhÃ¡ch Ä‘ang á»Ÿ
                     N'DA_CHECKOUT',  -- KhÃ¡ch Ä‘Ã£ tráº£ phÃ²ng, hÃ³a Ä‘Æ¡n Ä‘Ã£ xuáº¥t
                     N'DA_HUY'        -- ÄÆ¡n bá»‹ há»§y
                 )),
    CONSTRAINT FK_DatPhong_KH FOREIGN KEY (maKH) REFERENCES KH(maKH)
);

CREATE TABLE ChiTietDatPhong (
    maCTDP   VARCHAR(20)   PRIMARY KEY,
    maPhong  VARCHAR(10),
    maDat    VARCHAR(20),
    giaCoc   DECIMAL(18,2) DEFAULT 0,  -- Tiá»n cá»c riÃªng tá»«ng phÃ²ng (khÃ´ng pháº£i hÃ³a Ä‘Æ¡n)
    soNguoi  INT,
    ghiChu   NVARCHAR(255),
    CONSTRAINT FK_CTDP_Phong    FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT FK_CTDP_DatPhong FOREIGN KEY (maDat)   REFERENCES DatPhong(maDat)
);

-- =============================
-- 3. HOÃ ÄÆ N & Dá»ŠCH Vá»¤
-- =============================

CREATE TABLE HoaDon (
    maHD                 VARCHAR(20)   PRIMARY KEY,
    maDat                VARCHAR(20),
    maNV                 VARCHAR(9),
    ngayTaoHD            DATETIME      DEFAULT GETDATE(),  -- Thá»i Ä‘iá»ƒm xuáº¥t hÃ³a Ä‘Æ¡n (checkout)
    tienPhong            DECIMAL(18,2) DEFAULT 0,
    tienDV               DECIMAL(18,2) DEFAULT 0,
    tienCoc              DECIMAL(18,2) DEFAULT 0,          -- Tá»•ng tiá»n cá»c Ä‘Ã£ thu (tá»« ChiTietDatPhong)
    thueVAT              DECIMAL(18,2) DEFAULT 0,
    tongTien             DECIMAL(18,2) DEFAULT 0,          -- = tienPhong + tienDV + thueVAT - tienCoc

    -- PhÃ¢n loáº¡i hÃ³a Ä‘Æ¡n
    loaiHD               NVARCHAR(30)  NOT NULL DEFAULT N'HOA_DON_PHONG'
                         CHECK (loaiHD IN (
                             N'HOA_DON_PHONG',      -- HÃ³a Ä‘Æ¡n thanh toÃ¡n khi checkout (loáº¡i chÃ­nh)
                             N'HOA_DON_HOAN_TIEN'   -- HÃ³a Ä‘Æ¡n hoÃ n tiá»n (VD: há»§y Ä‘áº·t phÃ²ng Ä‘Ã£ cá»c)
                         )),

    -- Tráº¡ng thÃ¡i thanh toÃ¡n
    trangThaiThanhToan   NVARCHAR(30)  NOT NULL DEFAULT N'CHUA_THANH_TOAN'
                         CHECK (trangThaiThanhToan IN (
                             N'CHUA_THANH_TOAN',     -- HÃ³a Ä‘Æ¡n Ä‘Ã£ xuáº¥t nhÆ°ng chÆ°a thu tiá»n
                             N'DA_THANH_TOAN_COC',   -- ÄÃ£ thu cá»c, chÆ°a thanh toÃ¡n toÃ n bá»™
                             N'DA_THANH_TOAN'        -- ÄÃ£ thu Ä‘á»§ tiá»n
                         )),

    -- ThÃ´ng tin thanh toÃ¡n (Ä‘iá»n khi DA_THANH_TOAN)
    phuongThucThanhToan  NVARCHAR(30)  NULL
                         CHECK (phuongThucThanhToan IN (
                             N'TIEN_MAT', N'THE_TIN_DUNG', N'CHUYEN_KHOAN', N'VI_DIEN_TU'
                         )),
    ngayThanhToan        DATETIME      NULL,   -- Thá»i Ä‘iá»ƒm thu Ä‘á»§ tiá»n â†’ báº±ng chá»©ng khi khiáº¿u náº¡i
    ghiChuThanhToan      NVARCHAR(500) NULL,   -- MÃ£ giao dá»‹ch, lÃ½ do hoÃ n tiá»n, v.v.

    CONSTRAINT FK_HoaDon_DatPhong FOREIGN KEY (maDat) REFERENCES DatPhong(maDat),
    CONSTRAINT FK_HoaDon_NV       FOREIGN KEY (maNV)  REFERENCES NV(maNV)
);

CREATE TABLE ChiTietHoaDon (
    maCTHD         VARCHAR(20)   PRIMARY KEY,
    maHD           VARCHAR(20),
    maCTDP         VARCHAR(20),
    thoiGianLuuTru DECIMAL(5,2),   -- Sá»‘ Ä‘Ãªm lÆ°u trÃº
    thanhTien      DECIMAL(18,2),  -- Tiá»n phÃ²ng + dá»‹ch vá»¥ cá»§a dÃ²ng nÃ y
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD)   REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_CTDP   FOREIGN KEY (maCTDP) REFERENCES ChiTietDatPhong(maCTDP)
);
-- LÆ°u Ã½: bá» soLuongPhong vÃ¬ má»—i CTDP Ä‘Ã£ gáº¯n vá»›i 1 phÃ²ng cá»¥ thá»ƒ â†’ Ä‘áº¿m dÃ²ng lÃ  Ä‘á»§

CREATE TABLE DichVuSuDung (
    maDV         VARCHAR(20),
    maCTHD       VARCHAR(20),
    ngaySuDung   DATE,
    soLuong      INT,
    giaDV        DECIMAL(18,2),
    trangThai    BIT DEFAULT 0,  -- 0: chÆ°a tÃ­nh vÃ o HÄ, 1: Ä‘Ã£ tÃ­nh vÃ o HÄ
    CONSTRAINT PK_DVSD   PRIMARY KEY (maDV, maCTHD),
    CONSTRAINT FK_DVSD_DV   FOREIGN KEY (maDV)   REFERENCES DV(maDV),
    CONSTRAINT FK_DVSD_CTHD FOREIGN KEY (maCTHD) REFERENCES ChiTietHoaDon(maCTHD)
);

-- =============================
-- 4. Báº¢NG GIÃ Dá»ŠCH Vá»¤
-- =============================

CREATE TABLE BangGiaDV_Header (
    maBangGia      VARCHAR(20)    PRIMARY KEY,
    tenBangGia     NVARCHAR(100),
    ngayApDung     DATE,
    ngayHetHieuLuc DATE,
    trangThai      BIT DEFAULT 1  -- 1: Ä‘ang Ã¡p dá»¥ng, 0: háº¿t hiá»‡u lá»±c
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
