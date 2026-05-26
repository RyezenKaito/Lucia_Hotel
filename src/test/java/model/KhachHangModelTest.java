package model;

import fixtures.TestDataFixtures;
import model.entities.KhachHang;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho model KhachHang.
 */
@DisplayName("KhachHang Model – Unit Tests")
class KhachHangModelTest {

    @Test
    @DisplayName("KH_01 – Tạo KhachHang hợp lệ thành công")
    void KH_01_taoKhachHangHopLe() {
        KhachHang kh = TestDataFixtures.khachHangHopLe();

        assertNotNull(kh);
        assertEquals("KH001", kh.getMaKH());
        assertEquals("Lê Văn An", kh.getTenKH());
        assertEquals("001234567890", kh.getSoCCCD());
        assertEquals("0987654321", kh.getSoDT());
        assertEquals(LocalDate.of(1990, 7, 10), kh.getNgaySinh());
    }

    @Test
    @DisplayName("KH_02 – Constructor đầy đủ lưu đúng dữ liệu")
    void KH_02_constructorDayDu() {
        KhachHang kh = new KhachHang("KH002", "Nguyễn Thị B", "098765432100", "0911111111",
                LocalDate.of(1985, 3, 22));

        assertEquals("KH002", kh.getMaKH());
        assertEquals("Nguyễn Thị B", kh.getTenKH());
        assertEquals(LocalDate.of(1985, 3, 22), kh.getNgaySinh());
    }

    @Test
    @DisplayName("KH_03 – isBirthdayToday trả về false khi ngày sinh null")
    void KH_03_isBirthdayToday_ngaySinhNull() {
        KhachHang kh = new KhachHang("KH003");
        assertFalse(kh.isBirthdayToday());
    }

    @Test
    @DisplayName("KH_04 – isBirthdayThisMonth trả về false khi ngày sinh null")
    void KH_04_isBirthdayThisMonth_ngaySinhNull() {
        KhachHang kh = new KhachHang("KH004");
        assertFalse(kh.isBirthdayThisMonth());
    }

    @Test
    @DisplayName("KH_05 – isBirthdayToday trả về true khi sinh nhật hôm nay")
    void KH_05_isBirthdayToday_hienTai() {
        LocalDate today = LocalDate.now();
        KhachHang kh = new KhachHang("KH005", "Trần C", "111111111111", "0922222222",
                today.minusYears(30));   // Cùng ngày/tháng, khác năm

        assertTrue(kh.isBirthdayToday(),
                "isBirthdayToday phải true khi ngày/tháng sinh trùng hôm nay");
    }

    @Test
    @DisplayName("KH_06 – isBirthdayThisMonth trả về true khi sinh trong tháng này")
    void KH_06_isBirthdayThisMonth_thangNay() {
        LocalDate today = LocalDate.now();
        KhachHang kh = new KhachHang("KH006", "Trần D", "222222222222", "0933333333",
                today.minusYears(25));

        assertTrue(kh.isBirthdayThisMonth());
    }

    @Test
    @DisplayName("KH_07 – Setter cập nhật tên khách hàng")
    void KH_07_setterCapNhatTen() {
        KhachHang kh = TestDataFixtures.khachHangHopLe();
        kh.setTenKH("Tên Mới");
        assertEquals("Tên Mới", kh.getTenKH());
    }

    @Test
    @DisplayName("KH_08 – isDeleted mặc định là false")
    void KH_08_isDeletedMacDinh() {
        KhachHang kh = TestDataFixtures.khachHangHopLe();
        assertFalse(kh.isDeleted());
    }

    @Test
    @DisplayName("KH_09 – Setter isDeleted cập nhật đúng")
    void KH_09_setDeleted() {
        KhachHang kh = TestDataFixtures.khachHangHopLe();
        kh.setDeleted(true);
        assertTrue(kh.isDeleted());
    }
}
