package model.entities;

public class ChiTietHoaDon {
    private String maCTHD;
    private HoaDon hoaDon;
    private DichVu dichVu;
    private int soLuong;
    private double donGiaTaiThiemDiem;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(String maCTHD, HoaDon hoaDon, DichVu dichVu, int soLuong, double donGiaTaiThiemDiem) {
        this.maCTHD = maCTHD;
        this.hoaDon = hoaDon;
        this.dichVu = dichVu;
        this.soLuong = soLuong;
        this.donGiaTaiThiemDiem = donGiaTaiThiemDiem;
    }

    public String getMaCTHD() {
        return maCTHD;
    }

    public void setMaCTHD(String maCTHD) {
        this.maCTHD = maCTHD;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGiaTaiThiemDiem() {
        return donGiaTaiThiemDiem;
    }

    public void setDonGiaTaiThiemDiem(double donGiaTaiThiemDiem) {
        this.donGiaTaiThiemDiem = donGiaTaiThiemDiem;
    }

    @Override
    public String toString() {
        return maCTHD;
    }
}
