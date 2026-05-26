package fixtures;

import model.entities.DatPhong;
import model.entities.KhachHang;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.enums.TrangThaiNV;
import model.enums.trinhDo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dữ liệu mẫu (fixtures) dùng chung cho toàn bộ bộ test.
 */
public class TestDataFixtures {

    // ─── NhanVien ─────────────────────────────────────────────────────────────

    /** Tạo NhanVien hợp lệ – dùng cho NV_01 */
    public static NhanVien nhanVienHopLe() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA001");
        nv.setHoTen("Trần Thị Mai");
        nv.setSoDT("0901234567");
        nv.setCccd("012345678901");
        nv.setNgaySinh(LocalDate.of(1995, 5, 20));
        nv.setNgayVaoLamDate(LocalDate.of(2020, 1, 1));
        nv.setRole(ChucVu.NHAN_VIEN);
        nv.setTrinhDo(trinhDo.DAIHOC);
        nv.setTrangThai(TrangThaiNV.CON_LAM);
        nv.setMatKhau("123456");
        nv.setDiaChi("123 Đường ABC, TP.HCM");
        return nv;
    }

    /** Tạo NhanVien thứ hai (có tên chứa "Mai") – dùng cho NV_03 */
    public static NhanVien nhanVienMai() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA002");
        nv.setHoTen("Nguyễn Văn Mai");
        nv.setSoDT("0912345678");
        nv.setCccd("098765432100");
        nv.setNgaySinh(LocalDate.of(1993, 3, 15));
        nv.setNgayVaoLamDate(LocalDate.of(2021, 6, 1));
        nv.setRole(ChucVu.NHAN_VIEN);
        nv.setTrinhDo(trinhDo.CAODANG);
        nv.setTrangThai(TrangThaiNV.CON_LAM);
        nv.setMatKhau("abcdef");
        nv.setDiaChi("456 Đường XYZ, Hà Nội");
        return nv;
    }

    /** NhanVien không có tên – dùng cho NV_02 */
    public static String hoTenTrong() {
        return "";
    }

    // ─── KhachHang ────────────────────────────────────────────────────────────

    /** Tạo KhachHang hợp lệ */
    public static KhachHang khachHangHopLe() {
        return new KhachHang(
                "KH001",
                "Lê Văn An",
                "001234567890",
                "0987654321",
                LocalDate.of(1990, 7, 10)
        );
    }

    // ─── DatPhong ─────────────────────────────────────────────────────────────

    /** Tạo DatPhong hợp lệ */
    public static DatPhong datPhongHopLe() {
        KhachHang kh = khachHangHopLe();
        DatPhong dp = new DatPhong(
                "DP001",
                LocalDateTime.now(),
                kh,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)
        );
        dp.setSoNguoi(2);
        return dp;
    }
}
