package model.entities;

import java.util.Objects;

/**
 * Class thực thể đại diện cho bảng DichVu trong Database
 */
public class DichVu {
    private String maDichVu;    // maDichVu (Primary Key)
    private String tenDichVu;   // tenDichVu
    private double giaDichVu;   // giaDichVu (Tương ứng cột giaDichVu trong DB)
    private String loaiDichVu;  // loaiDichVu (Dùng để lọc theo Tab: NUOCUONG, THUCAN, ...)
    private String mieuTa;      // mieuTa

    // Constructor không tham số
    public DichVu() {
    }

    // THÊM MỚI: Constructor chỉ nhận 1 tham số là maDichVu
    public DichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    // Constructor đầy đủ tham số
    public DichVu(String maDichVu, String tenDichVu, double giaDichVu, String loaiDichVu, String mieuTa) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.giaDichVu = giaDichVu;
        this.loaiDichVu = loaiDichVu;
        this.mieuTa = mieuTa;
    }

    // Getter và Setter
    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public double getGiaDichVu() {
        return giaDichVu;
    }

    public void setGiaDichVu(double giaDichVu) {
        this.giaDichVu = giaDichVu;
    }

    public String getLoaiDichVu() {
        return loaiDichVu;
    }

    public void setLoaiDichVu(String loaiDichVu) {
        this.loaiDichVu = loaiDichVu;
    }

    public String getMieuTa() {
        return mieuTa;
    }

    public void setMieuTa(String mieuTa) {
        this.mieuTa = mieuTa;
    }

    // Ghi đè phương thức equals và hashCode để hỗ trợ lưu trữ trong Map/Set (như biến cart trong GUI)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVu dichVu = (DichVu) o;
        return Objects.equals(maDichVu, dichVu.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDichVu);
    }

    // Phương thức toString hỗ trợ debug
    @Override
    public String toString() {
        return "DichVu{" +
                "maDichVu='" + maDichVu + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", giaDichVu=" + giaDichVu +
                ", loaiDichVu='" + loaiDichVu + '\'' +
                '}';
    }
}