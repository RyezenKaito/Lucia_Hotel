package dao;

import connectDatabase.ConnectDatabase;
import fixtures.TestDataFixtures;
import model.entities.DatPhong;
import model.entities.KhachHang;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho DatPhongDAO (luồng đặt phòng) sử dụng Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatPhongDAO – Unit Tests (Mockito)")
class DatPhongDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<ConnectDatabase> mockedConnectDatabase;

    @Mock
    private ConnectDatabase mockDbInstance;

    @BeforeEach
    void setUp() throws SQLException {
        mockedConnectDatabase = Mockito.mockStatic(ConnectDatabase.class);
        mockedConnectDatabase.when(ConnectDatabase::getInstance).thenReturn(mockDbInstance);
        when(mockDbInstance.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedConnectDatabase.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DP_DAO_01 – insert() thành công
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DP_DAO_01 – insert() với DatPhong hợp lệ trả về true")
    void DP_DAO_01_insert_hopLe_traTrue() throws SQLException {
        DatPhong dp = TestDataFixtures.datPhongHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        DatPhongDAO dao = new DatPhongDAO();
        boolean result = dao.insert(dp);

        assertTrue(result, "insert() phải trả về true khi DB thực hiện thành công");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("DP_DAO_02 – insert() dùng SQL INSERT INTO DatPhong")
    void DP_DAO_02_insert_goiDungSql() throws SQLException {
        DatPhong dp = TestDataFixtures.datPhongHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        DatPhongDAO dao = new DatPhongDAO();
        dao.insert(dp);

        verify(mockConnection).prepareStatement(contains("INSERT INTO DatPhong"));
    }

    @Test
    @DisplayName("DP_DAO_03 – insert() trả về false khi DB không cập nhật dòng nào")
    void DP_DAO_03_insert_khongCapNhat_traFalse() throws SQLException {
        DatPhong dp = TestDataFixtures.datPhongHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        DatPhongDAO dao = new DatPhongDAO();
        boolean result = dao.insert(dp);

        assertFalse(result, "insert() phải trả về false khi DB không cập nhật dòng nào");
    }

    @Test
    @DisplayName("DP_DAO_04 – insert() đặt trangThai mặc định là CHO_XACNHAN khi null")
    void DP_DAO_04_insert_trangThaiMacDinh() throws SQLException {
        DatPhong dp = TestDataFixtures.datPhongHopLe();
        dp.setTrangThai(null);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        DatPhongDAO dao = new DatPhongDAO();
        dao.insert(dp);

        // Xác minh rằng "CHO_XACNHAN" được set làm giá trị trangThai
        verify(mockPreparedStatement).setString(eq(6), eq("CHO_XACNHAN"));
    }

    @Test
    @DisplayName("DP_DAO_05 – insert() set soNguoi = 1 khi soNguoi <= 0")
    void DP_DAO_05_insert_soNguoiDefault() throws SQLException {
        DatPhong dp = TestDataFixtures.datPhongHopLe();
        dp.setSoNguoi(0);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        DatPhongDAO dao = new DatPhongDAO();
        dao.insert(dp);

        verify(mockPreparedStatement).setInt(eq(7), eq(1));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DP_DAO_06 – findKhachHangByIdDatPhong()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DP_DAO_06 – findKhachHangByIdDatPhong() trả về null khi không tìm thấy maDat")
    void DP_DAO_06_findKhachHangByIdDatPhong_khongTim_traNull() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        DatPhongDAO dao = new DatPhongDAO();
        KhachHang kh = dao.findKhachHangByIdDatPhong("DP_KHONG_TON_TAI");

        assertNull(kh, "Phải trả về null khi không tìm thấy đơn đặt phòng");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DP_DAO_07 – getMaDatPhongCheckInHomNay()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DP_DAO_07 – getMaDatPhongCheckInHomNay() trả về danh sách rỗng khi không có đơn")
    void DP_DAO_07_getMaDatPhongCheckInHomNay_rong() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        DatPhongDAO dao = new DatPhongDAO();
        java.util.List<String> ds = dao.getMaDatPhongCheckInHomNay();

        assertNotNull(ds);
        assertTrue(ds.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DP_DAO_08 – insert() exception handling
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DP_DAO_08 – insert() trả về false khi có SQLException")
    void DP_DAO_08_insert_sqlException_traFalse() throws SQLException {
        DatPhong dp = TestDataFixtures.datPhongHopLe();

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        DatPhongDAO dao = new DatPhongDAO();
        boolean result = dao.insert(dp);

        assertFalse(result, "insert() phải trả về false khi có SQLException");
    }
}
