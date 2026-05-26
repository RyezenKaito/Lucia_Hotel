package model;

import fixtures.TestDataFixtures;
import model.entities.DatPhong;
import model.entities.KhachHang;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho model DatPhong (tầng validation / business rules).
 */
@DisplayName("DatPhong Model – Unit Tests")
class DatPhongModelTest {

    @Test
    @DisplayName("DP_01 – Tạo DatPhong hợp lệ thành công")
    void DP_01_taoDatPhongHopLe() {
        DatPhong dp = TestDataFixtures.datPhongHopLe();

        assertNotNull(dp);
        assertEquals("DP001", dp.getMaDat());
        assertNotNull(dp.getKhachHang());
        assertEquals("CHO_XACNHAN", dp.getTrangThai());
        assertEquals(2, dp.getSoNguoi());
    }

    @Test
    @DisplayName("DP_02 – Trạng thái mặc định khi tạo qua constructor đầy đủ là CHO_XACNHAN")
    void DP_02_trangThaiMacDinh() {
        KhachHang kh = TestDataFixtures.khachHangHopLe();
        DatPhong dp = new DatPhong(
                "DP002",
                LocalDateTime.now(),
                kh,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertEquals("CHO_XACNHAN", dp.getTrangThai());
    }

    @Test
    @DisplayName("DP_03 – Cập nhật trạng thái sang DA_XACNHAN")
    void DP_03_capNhatTrangThai() {
        DatPhong dp = TestDataFixtures.datPhongHopLe();
        dp.setTrangThai("DA_XACNHAN");

        assertEquals("DA_XACNHAN", dp.getTrangThai());
    }

    @Test
    @DisplayName("DP_04 – Ngày check-in phải trước ngày check-out")
    void DP_04_checkInTruocCheckOut() {
        DatPhong dp = TestDataFixtures.datPhongHopLe();

        assertNotNull(dp.getNgayCheckIn());
        assertNotNull(dp.getNgayCheckOut());
        assertTrue(dp.getNgayCheckIn().isBefore(dp.getNgayCheckOut()),
                "Ngày check-in phải trước ngày check-out");
    }

    @Test
    @DisplayName("DP_05 – Gán khách hàng cho đơn đặt phòng")
    void DP_05_ganKhachHang() {
        DatPhong dp = new DatPhong("DP003");
        KhachHang kh = TestDataFixtures.khachHangHopLe();
        dp.setKhachHang(kh);

        assertNotNull(dp.getKhachHang());
        assertEquals("KH001", dp.getKhachHang().getMaKH());
        assertEquals("Lê Văn An", dp.getKhachHang().getTenKH());
    }

    @Test
    @DisplayName("DP_06 – toString trả về maDat")
    void DP_06_toStringTraVeMaDat() {
        DatPhong dp = new DatPhong("DP004");
        assertEquals("DP004", dp.toString());
    }

    @Test
    @DisplayName("DP_07 – getMaDatPhong() tương đương getMaDat()")
    void DP_07_getMaDatPhongTuongDuong() {
        DatPhong dp = new DatPhong("DP005");
        assertEquals(dp.getMaDat(), dp.getMaDatPhong());
    }

    @Test
    @DisplayName("DP_08 – Số người mặc định là 0 khi tạo rỗng")
    void DP_08_soNguoiMacDinh() {
        DatPhong dp = new DatPhong();
        assertEquals(0, dp.getSoNguoi());
    }

    @Test
    @DisplayName("DP_09 – Cập nhật số người")
    void DP_09_capNhatSoNguoi() {
        DatPhong dp = TestDataFixtures.datPhongHopLe();
        dp.setSoNguoi(4);
        assertEquals(4, dp.getSoNguoi());
    }

    @Test
    @DisplayName("DP_10 – Cập nhật ngày thanh toán")
    void DP_10_capNhatNgayThanhToan() {
        DatPhong dp = TestDataFixtures.datPhongHopLe();
        LocalDateTime now = LocalDateTime.now();
        dp.setNgayThanhToan(now);
        assertEquals(now, dp.getNgayThanhToan());
    }
}
