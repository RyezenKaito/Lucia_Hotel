package model.entities;

import java.time.LocalDateTime;

public class DatPhong {
    private String maDat;
    private LocalDateTime ngayDat;
    private KhachHang khachHang;
    private LocalDateTime ngayCheckIn;
    private LocalDateTime ngayCheckOut;

    public DatPhong() {
    }

    public DatPhong(String maDat) {
        this.maDat = maDat;
    }

    public DatPhong(String maDat, LocalDateTime ngayDat, KhachHang khachHang, LocalDateTime ngayCheckIn, LocalDateTime ngayCheckOut) {
        this.maDat = maDat;
        this.ngayDat = ngayDat;
        this.khachHang = khachHang;
        this.ngayCheckIn = ngayCheckIn;
        this.ngayCheckOut = ngayCheckOut;
    }

    public String getMaDat() {
        return maDat;
    }

    public void setMaDat(String maDat) {
        this.maDat = maDat;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public LocalDateTime getNgayCheckIn() {
        return ngayCheckIn;
    }

    public void setNgayCheckIn(LocalDateTime ngayCheckIn) {
        this.ngayCheckIn = ngayCheckIn;
    }

    public LocalDateTime getNgayCheckOut() {
        return ngayCheckOut;
    }

    public void setNgayCheckOut(LocalDateTime ngayCheckOut) {
        this.ngayCheckOut = ngayCheckOut;
    }

    @Override
    public String toString() {
        return maDat;
    }

    // --- Legacy compatibility getters ---
    public String getMaDatPhong() { return maDat; }
}
