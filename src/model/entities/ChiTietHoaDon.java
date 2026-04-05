package model.entities;

public class ChiTietHoaDon {
    private String maCTHD;
    private HoaDon hoaDon;
    private ChiTietDatPhong chiTietDatPhong;
    private double thoiGianLuuTru;
    private int soLuongPhong;
    private double thanhTien;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(String maCTHD, HoaDon hoaDon, ChiTietDatPhong chiTietDatPhong, double thoiGianLuuTru, int soLuongPhong, double thanhTien) {
        this.maCTHD = maCTHD;
        this.hoaDon = hoaDon;
        this.chiTietDatPhong = chiTietDatPhong;
        this.thoiGianLuuTru = thoiGianLuuTru;
        this.soLuongPhong = soLuongPhong;
        this.thanhTien = thanhTien;
    }

    public String getMaCTHD() { return maCTHD; }
    public void setMaCTHD(String maCTHD) { this.maCTHD = maCTHD; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public ChiTietDatPhong getChiTietDatPhong() { return chiTietDatPhong; }
    public void setChiTietDatPhong(ChiTietDatPhong chiTietDatPhong) { this.chiTietDatPhong = chiTietDatPhong; }

    public double getThoiGianLuuTru() { return thoiGianLuuTru; }
    public void setThoiGianLuuTru(double thoiGianLuuTru) { this.thoiGianLuuTru = thoiGianLuuTru; }

    public int getSoLuongPhong() { return soLuongPhong; }
    public void setSoLuongPhong(int soLuongPhong) { this.soLuongPhong = soLuongPhong; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    @Override
    public String toString() {
        return maCTHD;
    }
}
