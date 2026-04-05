package model.entities;

import java.time.LocalDate;

public class DichVuSuDung {
    private DichVu dichVu;
    private ChiTietHoaDon chiTietHoaDon;
    private LocalDate ngaySuDung;
    private int soLuong;
    private double giaDV;
    private boolean trangThai;

    public DichVuSuDung() {
    }

    public DichVuSuDung(DichVu dichVu, ChiTietHoaDon chiTietHoaDon, LocalDate ngaySuDung, int soLuong, double giaDV, boolean trangThai) {
        this.dichVu = dichVu;
        this.chiTietHoaDon = chiTietHoaDon;
        this.ngaySuDung = ngaySuDung;
        this.soLuong = soLuong;
        this.giaDV = giaDV;
        this.trangThai = trangThai;
    }

    public DichVu getDichVu() { return dichVu; }
    public void setDichVu(DichVu dichVu) { this.dichVu = dichVu; }

    public ChiTietHoaDon getChiTietHoaDon() { return chiTietHoaDon; }
    public void setChiTietHoaDon(ChiTietHoaDon chiTietHoaDon) { this.chiTietHoaDon = chiTietHoaDon; }

    public LocalDate getNgaySuDung() { return ngaySuDung; }
    public void setNgaySuDung(LocalDate ngaySuDung) { this.ngaySuDung = ngaySuDung; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getGiaDV() { return giaDV; }
    public void setGiaDV(double giaDV) { this.giaDV = giaDV; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    
    public double getThanhTien() { return giaDV * soLuong; }
}
