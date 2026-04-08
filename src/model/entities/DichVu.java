package model.entities;

import java.util.Objects;

/**
 * Class thực thể đại diện cho bảng DV trong Database LuciaHT
 */
public class DichVu {
    private String maDV;
    private String tenDV;
    private double gia;
    private String loaiDV;
    private String mieuTa;
    private String donVi;
    private int trangThai; // 0: active, 1: hidden

    public DichVu() {
    }

    public DichVu(String maDV) {
        this.maDV = maDV;
    }

    public DichVu(String maDV, String tenDV, double gia, String loaiDV, String mieuTa, String donVi, int trangThai) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.gia = gia;
        this.loaiDV = loaiDV;
        this.mieuTa = mieuTa;
        this.donVi = donVi;
        this.trangThai = trangThai;
    }

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public String getLoaiDV() {
        return loaiDV;
    }

    public void setLoaiDV(String loaiDV) {
        this.loaiDV = loaiDV;
    }

    public String getMieuTa() {
        return mieuTa;
    }

    public void setMieuTa(String mieuTa) {
        this.mieuTa = mieuTa;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public int getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(int trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVu dichVu = (DichVu) o;
        return Objects.equals(maDV, dichVu.maDV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDV);
    }

    @Override
    public String toString() {
        return tenDV != null ? tenDV : maDV;
    }

    // --- Legacy compatibility ---
    public String getMaDichVu() { return maDV; }
    public String getTenDichVu() { return tenDV; }
    public double getDonGia() { return gia; }
    public double getGiaDichVu() { return gia; }
    public String getLoaiDichVu() { return loaiDV; }
    public String getDonViTinh() { return donVi; }
}