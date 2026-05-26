package dao;

import connectDatabase.ConnectDatabase;
import fixtures.TestDataFixtures;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.enums.TrangThaiNV;
import model.enums.trinhDo;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho NhanVienDAO sử dụng Mockito để giả lập kết nối cơ sở dữ liệu.
 *
 * Các test case tương ứng với bảng test:
 *  NV_01 – insert() trả về true khi DB thực hiện thành công
 *  NV_02 – NhanVien với hoTen rỗng không thể được tạo (validation ở Model)
 *  NV_03 – findByKeyword("Mai") trả về danh sách chứa nhân viên tên "Mai"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NhanVienDAO – Unit Tests (Mockito)")
class NhanVienDAOTest {

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
        // lenient: NV_02 verifies model validation and does not hit the DB
        Mockito.lenient().when(mockDbInstance.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedConnectDatabase.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NV_01 – insert() thành công
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_01 – insert() với NhanVien hợp lệ trả về true")
    void NV_01_insert_nhanVienHopLe_traTrueLuuDB() throws SQLException {
        NhanVien nv = TestDataFixtures.nhanVienHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.insert(nv);

        assertTrue(result, "insert() phải trả về true khi DB thực hiện thành công");
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("NV_01b – insert() gọi prepareStatement với SQL INSERT")
    void NV_01b_insert_goiPrepareStatementDung() throws SQLException {
        NhanVien nv = TestDataFixtures.nhanVienHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        NhanVienDAO dao = new NhanVienDAO();
        dao.insert(nv);

        verify(mockConnection).prepareStatement(contains("INSERT INTO NV"));
    }

    @Test
    @DisplayName("NV_01c – insert() trả về false khi DB không cập nhật dòng nào")
    void NV_01c_insert_dbKhongCapNhat_traFalse() throws SQLException {
        NhanVien nv = TestDataFixtures.nhanVienHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.insert(nv);

        assertFalse(result, "insert() phải trả về false khi DB không cập nhật dòng nào");
    }

    @Test
    @DisplayName("NV_01d – insert() trả về false khi SQLException được ném")
    void NV_01d_insert_sqlException_traFalse() throws SQLException {
        NhanVien nv = TestDataFixtures.nhanVienHopLe();

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test error"));

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.insert(nv);

        assertFalse(result, "insert() phải trả về false khi có SQLException");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NV_02 – Validation hoTen tại tầng Model (không cần DB)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_02 – hoTen trống không qua được validation Model trước khi đến DAO")
    void NV_02_hoTenTrong_khongQuaValidationModel() {
        NhanVien nv = new NhanVien();
        nv.setMaNV("LUCIA020");

        // Validation xảy ra tại Model, không liên quan đến DB
        assertThrows(
                IllegalArgumentException.class,
                () -> nv.setHoTen(""),
                "Model phải ném IllegalArgumentException trước khi DAO được gọi"
        );

        // Đảm bảo DB không bị gọi khi validation thất bại
        verifyNoInteractions(mockConnection);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NV_03 – findByKeyword("Mai")
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_03 – findByKeyword(\"Mai\") trả về danh sách nhân viên chứa \"Mai\"")
    void NV_03_findByKeyword_Mai_traVeDanhSachChuaMai() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Giả lập 2 hàng kết quả: "Trần Thị Mai" và "Nguyễn Văn Mai"
        when(mockResultSet.next())
                .thenReturn(true)   // hàng 1
                .thenReturn(true)   // hàng 2
                .thenReturn(false); // hết dữ liệu

        // Hàng 1 – Trần Thị Mai
        when(mockResultSet.getString("maNV")).thenReturn("LUCIA001", "LUCIA002");
        when(mockResultSet.getString("hoTen")).thenReturn("Trần Thị Mai", "Nguyễn Văn Mai");
        when(mockResultSet.getString("diaChi")).thenReturn("HCM", "HN");
        when(mockResultSet.getString("trinhDo")).thenReturn("DAIHOC", "CAODANG");
        when(mockResultSet.getDate("ngayVaoLam"))
                .thenReturn(Date.valueOf(LocalDate.of(2020, 1, 1)),
                        Date.valueOf(LocalDate.of(2021, 6, 1)));
        when(mockResultSet.getString("role")).thenReturn("NHAN_VIEN", "NHAN_VIEN");
        when(mockResultSet.getString("soDT")).thenReturn("0901234567", "0912345678");
        when(mockResultSet.getString("maQL")).thenReturn(null, null);
        when(mockResultSet.getString("mk")).thenReturn("123456", "abcdef");
        when(mockResultSet.getString("soCCCD")).thenReturn(null, null);
        when(mockResultSet.getDate("ngaySinh")).thenReturn(null, null);
        when(mockResultSet.getString("trangThai")).thenReturn("CON_LAM", "CON_LAM");

        NhanVienDAO dao = new NhanVienDAO();
        List<NhanVien> dsKetQua = dao.findByKeyword("Mai");

        assertEquals(2, dsKetQua.size(), "Phải tìm thấy đúng 2 nhân viên chứa 'Mai'");
        assertTrue(dsKetQua.stream().anyMatch(nv -> "Trần Thị Mai".equals(nv.getHoTen())),
                "Phải có nhân viên 'Trần Thị Mai'");
        assertTrue(dsKetQua.stream().anyMatch(nv -> "Nguyễn Văn Mai".equals(nv.getHoTen())),
                "Phải có nhân viên 'Nguyễn Văn Mai'");
    }

    @Test
    @DisplayName("NV_03b – findByKeyword() dùng LIKE %keyword%")
    void NV_03b_findByKeyword_dungLikePattern() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        NhanVienDAO dao = new NhanVienDAO();
        dao.findByKeyword("Mai");

        verify(mockPreparedStatement).setString(1, "%Mai%");
        verify(mockPreparedStatement).setString(2, "%Mai%");
        verify(mockPreparedStatement).setString(3, "%Mai%");
    }

    @Test
    @DisplayName("NV_03c – findByKeyword() trả về danh sách rỗng khi không có kết quả")
    void NV_03c_findByKeyword_khongCoKetQua_danhSachRong() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        NhanVienDAO dao = new NhanVienDAO();
        List<NhanVien> dsKetQua = dao.findByKeyword("XYZ_KHONG_TON_TAI");

        assertNotNull(dsKetQua);
        assertTrue(dsKetQua.isEmpty(), "Phải trả về danh sách rỗng khi không tìm thấy");
    }

    @Test
    @DisplayName("NV_03d – findByKeyword() với SQL query chứa maNV, hoTen và soDT")
    void NV_03d_findByKeyword_sqlQueryChinhXac() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        NhanVienDAO dao = new NhanVienDAO();
        dao.findByKeyword("test");

        verify(mockConnection).prepareStatement(
                argThat(sql -> sql.contains("maNV LIKE") && sql.contains("hoTen LIKE") && sql.contains("soDT LIKE"))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_04 – update() trả về true khi DB cập nhật thành công")
    void NV_04_update_thanhCong() throws SQLException {
        NhanVien nv = TestDataFixtures.nhanVienHopLe();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.update(nv);

        assertTrue(result);
        verify(mockConnection).prepareStatement(contains("UPDATE NV SET"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // delete() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_05 – delete() (soft delete) trả về true khi thành công")
    void NV_05_delete_softDelete_thanhCong() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.delete("LUCIA001");

        assertTrue(result);
        verify(mockConnection).prepareStatement(contains("is_deleted = 1"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // authenticate() tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("NV_06 – authenticate() trả về true khi mật khẩu đúng")
    void NV_06_authenticate_matKhauDung_traTrue() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("mk")).thenReturn("123456");

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.authenticate("LUCIA001", "123456");

        assertTrue(result, "authenticate() phải trả về true khi mật khẩu khớp");
    }

    @Test
    @DisplayName("NV_07 – authenticate() trả về false khi mật khẩu sai")
    void NV_07_authenticate_matKhauSai_traFalse() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("mk")).thenReturn("correct_password");

        NhanVienDAO dao = new NhanVienDAO();
        boolean result = dao.authenticate("LUCIA001", "wrong_password");

        assertFalse(result, "authenticate() phải trả về false khi mật khẩu không khớp");
    }
}
