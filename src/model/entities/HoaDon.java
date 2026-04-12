package model.entities;

import java.time.LocalDateTime;

public class HoaDon {
    private String maHD;
    private LocalDateTime ngayTaoHD;
    private DatPhong datPhong;
    private NhanVien nhanVien;
    private double tienPhong;
    private double tienDV;
    private double tienCoc;
    private double thueVAT;
    private double tongTien;
    
    // New fields
    private String loaiHD;
    private String trangThaiThanhToan;
    private String phuongThucThanhToan;
    private LocalDateTime ngayThanhToan;
    private String ghiChuThanhToan;

    public HoaDon() {
    }

    public HoaDon(String maHD) {
        this.maHD = maHD;
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public LocalDateTime getNgayTaoHD() { return ngayTaoHD; }
    public void setNgayTaoHD(LocalDateTime ngayTaoHD) { this.ngayTaoHD = ngayTaoHD; }

    public DatPhong getDatPhong() { return datPhong; }
    public void setDatPhong(DatPhong datPhong) { this.datPhong = datPhong; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public double getTienPhong() { return tienPhong; }
    public void setTienPhong(double tienPhong) { this.tienPhong = tienPhong; }

    public double getTienDV() { return tienDV; }
    public void setTienDV(double tienDV) { this.tienDV = tienDV; }

    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }

    public double getThueVAT() { return thueVAT; }
    public void setThueVAT(double thueVAT) { this.thueVAT = thueVAT; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getLoaiHD() { return loaiHD; }
    public void setLoaiHD(String loaiHD) { this.loaiHD = loaiHD; }
    public String getTenLoaiHD() { return model.enums.LoaiHoaDon.fromName(loaiHD).getDisplayName(); }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }
    public String getTenTrangThaiThanhToan() { return model.enums.TrangThaiThanhToan.fromName(trangThaiThanhToan).getDisplayName(); }

    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public String getTenPhuongThucThanhToan() { return phuongThucThanhToan != null ? model.enums.PhuongThucThanhToan.fromName(phuongThucThanhToan).getDisplayName() : "—"; }

    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }

    public String getGhiChuThanhToan() { return ghiChuThanhToan; }
    public void setGhiChuThanhToan(String ghiChuThanhToan) { this.ghiChuThanhToan = ghiChuThanhToan; }

    @Override
    public String toString() { return maHD; }
}
