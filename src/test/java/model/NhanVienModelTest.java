package model;

import fixtures.TestDataFixtures;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.enums.TrangThaiNV;
import model.enums.trinhDo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho model NhanVien (tầng validation / business rules).
 *
 * | STT | Test Case ID | Chức năng                 | Dữ liệu đầu vào                            | Kết quả mong muốn                      |
 * |-----|--------------|---------------------------|--------------------------------------------|----------------------------------------|
 * |  1  | NV_01        | Thêm nhân viên            | Nhập đầy đủ thông tin hợp lệ               | Đối tượng tạo thành công               |
 * |  2  | NV_02        | Validate hoTen trống      | hoTen = ""                                 | Ném IllegalArgumentException           |
 * |  3  | NV_03        | Tìm kiếm theo tên         | Keyword "Mai"                              | Lọc được nhân viên chứa "Mai" trong tên|
 */
@DisplayName("NhanVien Model – Unit Tests")
class NhanVienModelTest {

    // ─────────────────────────────────────────────────────────────────────────
    // NV_01 – Tạo nhân viên với thông tin hợp lệ
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_01 – Tạo NhanVien với dữ liệu hợp lệ thành công")
    void NV_01_taoNhanVienHopLe_thanhCong() {
        NhanVien nv = TestDataFixtures.nhanVienHopLe();

        assertNotNull(nv, "Đối tượng NhanVien không được null");
        assertEquals("LUCIA001", nv.getMaNV());
        assertEquals("Trần Thị Mai", nv.getHoTen());
        assertEquals("0901234567", nv.getSoDT());
        assertEquals("012345678901", nv.getCccd());
        assertEquals(LocalDate.of(1995, 5, 20), nv.getNgaySinh());
        assertEquals(ChucVu.NHAN_VIEN, nv.getRole());
        assertEquals(trinhDo.DAIHOC, nv.getTrinhDo());
        assertEquals(TrangThaiNV.CON_LAM, nv.getTrangThai());
    }

    @Test
    @DisplayName("NV_01b – Tên nhân viên được lưu đúng")
    void NV_01b_hoTenDuocLuuDung() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA003");
        nv.setHoTen("Nguyễn Thị Hoa");

        assertEquals("Nguyễn Thị Hoa", nv.getHoTen());
    }

    @Test
    @DisplayName("NV_01c – Số điện thoại hợp lệ (10 chữ số, bắt đầu 0)")
    void NV_01c_soDTHopLe() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA004");
        assertDoesNotThrow(() -> nv.setSoDT("0912345678"));
        assertEquals("0912345678", nv.getSoDT());
    }

    @Test
    @DisplayName("NV_01d – CCCD 12 chữ số hợp lệ")
    void NV_01d_cccd12ChuSoHopLe() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA005");
        assertDoesNotThrow(() -> nv.setCccd("012345678901"));
        assertEquals("012345678901", nv.getCccd());
    }

    @Test
    @DisplayName("NV_01e – CCCD 9 chữ số (CMND cũ) hợp lệ")
    void NV_01e_cccd9ChuSoHopLe() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA006");
        assertDoesNotThrow(() -> nv.setCccd("012345678"));
        assertEquals("012345678", nv.getCccd());
    }

    @Test
    @DisplayName("NV_01f – Ngày sinh hợp lệ (trên 18 tuổi)")
    void NV_01f_ngaySinhHopLe() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA007");
        LocalDate ngaySinhHopLe = LocalDate.now().minusYears(25);
        assertDoesNotThrow(() -> nv.setNgaySinh(ngaySinhHopLe));
        assertEquals(ngaySinhHopLe, nv.getNgaySinh());
    }

    @Test
    @DisplayName("NV_01g – Mã NV theo định dạng LUCIA + số")
    void NV_01g_maNVDinhDang() {
        NhanVien nv = new NhanVien();
        assertDoesNotThrow(() -> nv.setMaNV("LUCIA099"));
        assertEquals("LUCIA099", nv.getMaNV());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NV_02 – Validate họ tên trống
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_02 – Họ tên trống (\"\") ném IllegalArgumentException")
    void NV_02_hoTenTrong_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA010");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> nv.setHoTen(TestDataFixtures.hoTenTrong()),
                "Phải ném exception khi họ tên để trống"
        );
        assertTrue(ex.getMessage().contains("rỗng"),
                "Thông báo lỗi phải đề cập đến 'rỗng': " + ex.getMessage());
    }

    @Test
    @DisplayName("NV_02b – Họ tên null ném IllegalArgumentException")
    void NV_02b_hoTenNull_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA011");

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setHoTen(null),
                "Phải ném exception khi họ tên là null"
        );
    }

    @Test
    @DisplayName("NV_02c – Họ tên chỉ chứa khoảng trắng ném IllegalArgumentException")
    void NV_02c_hoTenKhoangTrang_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA012");

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setHoTen("   "),
                "Phải ném exception khi họ tên chỉ là khoảng trắng"
        );
    }

    @Test
    @DisplayName("NV_02d – SĐT không đúng định dạng ném IllegalArgumentException")
    void NV_02d_soDTSai_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA013");

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setSoDT("123456"),
                "SĐT không đủ 10 chữ số hoặc không bắt đầu bằng 0"
        );
    }

    @Test
    @DisplayName("NV_02e – CCCD không đúng định dạng ném IllegalArgumentException")
    void NV_02e_cccdSai_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA014");

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setCccd("12345"),
                "CCCD phải 9 hoặc 12 chữ số"
        );
    }

    @Test
    @DisplayName("NV_02f – Ngày sinh dưới 18 tuổi ném IllegalArgumentException")
    void NV_02f_tuoiDuoi18_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA015");
        LocalDate ngaySinhDuoi18 = LocalDate.now().minusYears(17);

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setNgaySinh(ngaySinhDuoi18),
                "Phải ném exception khi nhân viên dưới 18 tuổi"
        );
    }

    @Test
    @DisplayName("NV_02g – Mật khẩu trống ném IllegalArgumentException")
    void NV_02g_matKhauTrong_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA016");

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setMatKhau(""),
                "Mật khẩu trống phải ném exception"
        );
    }

    @Test
    @DisplayName("NV_02h – Ngày vào làm sau hôm nay ném IllegalArgumentException")
    void NV_02h_ngayVaoLamSauHomNay_nemException() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA017");

        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setNgayVaoLamDate(LocalDate.now().plusDays(1)),
                "Ngày vào làm sau hôm nay phải ném exception"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NV_03 – Tìm kiếm nhân viên theo keyword (model-level filter)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_03 – Lọc danh sách nhân viên chứa \"Mai\" trong tên")
    void NV_03_locNhanVienTheoKeyword_Mai() {
        java.util.List<NhanVien> dsNV = java.util.Arrays.asList(
                TestDataFixtures.nhanVienHopLe(),   // "Trần Thị Mai"
                TestDataFixtures.nhanVienMai(),      // "Nguyễn Văn Mai"
                buildNhanVienKhac()                  // "Phạm Văn Bình" – không chứa "Mai"
        );

        String keyword = "Mai";
        java.util.List<NhanVien> ketQua = dsNV.stream()
                .filter(nv -> nv.getHoTen() != null
                        && nv.getHoTen().toLowerCase().contains(keyword.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(2, ketQua.size(), "Phải tìm thấy đúng 2 nhân viên có 'Mai' trong tên");
        assertTrue(ketQua.stream().anyMatch(nv -> nv.getHoTen().contains("Trần Thị Mai")),
                "Phải có nhân viên tên 'Trần Thị Mai'");
        assertTrue(ketQua.stream().anyMatch(nv -> nv.getHoTen().contains("Nguyễn Văn Mai")),
                "Phải có nhân viên tên 'Nguyễn Văn Mai'");
    }

    @Test
    @DisplayName("NV_03b – Keyword không khớp trả về danh sách rỗng")
    void NV_03b_keywordKhongKhop_danhSachRong() {
        java.util.List<NhanVien> dsNV = java.util.Arrays.asList(
                TestDataFixtures.nhanVienHopLe(),
                TestDataFixtures.nhanVienMai()
        );

        java.util.List<NhanVien> ketQua = dsNV.stream()
                .filter(nv -> nv.getHoTen() != null
                        && nv.getHoTen().toLowerCase().contains("xyz"))
                .collect(java.util.stream.Collectors.toList());

        assertTrue(ketQua.isEmpty(), "Keyword không khớp phải trả về danh sách rỗng");
    }

    @Test
    @DisplayName("NV_03c – Tìm kiếm không phân biệt hoa thường")
    void NV_03c_timKiemKhongPhanBietHoaThuong() {
        java.util.List<NhanVien> dsNV = java.util.Arrays.asList(
                TestDataFixtures.nhanVienHopLe(),   // "Trần Thị Mai"
                buildNhanVienKhac()
        );

        java.util.List<NhanVien> ketQua = dsNV.stream()
                .filter(nv -> nv.getHoTen() != null
                        && nv.getHoTen().toLowerCase().contains("mai"))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(1, ketQua.size());
        assertEquals("Trần Thị Mai", ketQua.get(0).getHoTen());
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private NhanVien buildNhanVienKhac() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA009");
        nv.setHoTen("Phạm Văn Bình");
        nv.setSoDT("0933333333");
        nv.setNgaySinh(LocalDate.of(1988, 1, 1));
        nv.setRole(ChucVu.NHAN_VIEN);
        nv.setTrangThai(TrangThaiNV.CON_LAM);
        return nv;
    }
}
