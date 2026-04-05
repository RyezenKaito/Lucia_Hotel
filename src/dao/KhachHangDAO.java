package dao;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import connectDatabase.ConnectDatabase;
import model.entities.KhachHang;

/**
 * DAO cho bảng KH (KhachHang).
 */
public class KhachHangDAO {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─────────────────────────────────────────────────────────────────────────
    //  LẤY TOÀN BỘ
    // ─────────────────────────────────────────────────────────────────────────
    public List<KhachHang> getAll() {
        List<KhachHang> ds = new ArrayList<>();
        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            ResultSet rs   = con.createStatement()
                               .executeQuery("SELECT * FROM KH");
            while (rs.next()) ds.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAll KH: " + e.getMessage());
        }
        return ds;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  THÊM
    // ─────────────────────────────────────────────────────────────────────────
    public boolean insert(KhachHang kh) {
        String sql = "INSERT INTO KH "
                   + "(maKH, tenKH, soDT, ngaySinh, soCCCD) "
                   + "VALUES (?,?,?,?,?)";
        try {
            Connection con         = ConnectDatabase.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, kh.getMaKH());
            pstmt.setString(2, kh.getTenKH());
            pstmt.setString(3, kh.getSoDT());
            if (kh.getNgaySinh() != null)
                pstmt.setDate(4, Date.valueOf(kh.getNgaySinh()));
            else
                pstmt.setNull(4, Types.DATE);
            pstmt.setString(5, kh.getSoCCCD());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("insert KH: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CẬP NHẬT
    // ─────────────────────────────────────────────────────────────────────────
    public boolean update(KhachHang kh) {
        String sql = "UPDATE KH "
                   + "SET tenKH=?, soDT=?, ngaySinh=?, soCCCD=? "
                   + "WHERE maKH=?";
        try {
            Connection con         = ConnectDatabase.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, kh.getTenKH());
            pstmt.setString(2, kh.getSoDT());
            if (kh.getNgaySinh() != null)
                pstmt.setDate(3, Date.valueOf(kh.getNgaySinh()));
            else
                pstmt.setNull(3, Types.DATE);
            pstmt.setString(4, kh.getSoCCCD());
            pstmt.setString(5, kh.getMaKH());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("update KH: " + e.getMessage());
            return false;
        }
    }    
    
    // ─────────────────────────────────────────────────────────────────────────
    //  XÓA
    // ─────────────────────────────────────────────────────────────────────────
    public boolean delete(String maKH) {
    	String sql = "DELETE from KH WHERE maKH = ?";
    	try {
    		Connection con = ConnectDatabase.getInstance().getConnection();
    		PreparedStatement pstmt = con.prepareStatement(sql);
    		pstmt.setString(1, maKH);
    		return pstmt.executeUpdate() > 0;
    	} catch(SQLException e) {
    		System.err.println("delete KH: " + e.getMessage());
    		return false;
    	}
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    //  TÌM THEO MÃ (LIKE)
    // ─────────────────────────────────────────────────────────────────────────
    public KhachHang findKhachHangByID(String keyword) {
        String sql = "SELECT * FROM KH WHERE maKH LIKE ?";
        try {
            Connection con         = ConnectDatabase.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SINH MÃ KH TIẾP THEO
    // ─────────────────────────────────────────────────────────────────────────
    public String getNextMaKH() {
        String sql = "SELECT MAX(maKH) FROM KH";
        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            ResultSet  rs  = con.createStatement().executeQuery(sql);
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);
                int    num  = Integer.parseInt(last.substring(2)) + 1;
                return String.format("KH%08d", num);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "KH00000001";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ĐẾM KHÁCH CÓ SINH NHẬT HÔM NAY
    // ─────────────────────────────────────────────────────────────────────────
    public int getBirthdayTodayCount() {
        String sql = "SELECT COUNT(*) FROM KH "
                   + "WHERE DAY(ngaySinh)   = DAY(GETDATE()) "
                   + "  AND MONTH(ngaySinh) = MONTH(GETDATE())";
        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            ResultSet  rs  = con.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DANH SÁCH KHÁCH SINH NHẬT HÔM NAY + PHÒNG ĐANG Ở
    // ─────────────────────────────────────────────────────────────────────────
    public List<String[]> getBirthdayTodayWithRoom() {
        Map<String, String[]>    custMap = new LinkedHashMap<>();
        Map<String, List<String>> roomMap = new HashMap<>();

        String sql =
            "SELECT kh.maKH, kh.tenKH, kh.soDT, kh.ngaySinh, " +
            "       p.maPhong " +
            "FROM   KH kh " +
            "LEFT JOIN DatPhong dp " +
            "       ON  kh.maKH        = dp.maKH " +
            "       AND dp.ngayCheckIn  IS NOT NULL " +
            "       AND dp.ngayCheckOut IS NULL " +
            "LEFT JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat " +
            "LEFT JOIN Phong           p    ON ctdp.maPhong  = p.maPhong " +
            "WHERE DAY(kh.ngaySinh)   = DAY(GETDATE()) " +
            "  AND MONTH(kh.ngaySinh) = MONTH(GETDATE())";

        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            ResultSet  rs  = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                String maKH = rs.getString("maKH");
                if (!custMap.containsKey(maKH)) {
                    Date d = rs.getDate("ngaySinh");
                    String nsStr = d != null
                            ? d.toLocalDate().format(DATE_FMT) : "";
                    custMap.put(maKH, new String[]{
                            maKH,
                            rs.getString("tenKH"),
                            rs.getString("soDT"),
                            nsStr
                    });
                    roomMap.put(maKH, new ArrayList<>());
                }
                String maPhong = rs.getString("maPhong");
                if (maPhong != null) roomMap.get(maKH).add(maPhong);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String[]> result = new ArrayList<>();
        for (String maKH : custMap.keySet()) {
            String[]      data  = custMap.get(maKH);
            List<String>  rooms = roomMap.get(maKH);
            String        roomStr = rooms.isEmpty() ? "Chưa đặt phòng"
                                                    : String.join(", ", rooms);
            result.add(new String[]{data[0], data[1], data[2], data[3], roomStr});
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER: ánh xạ ResultSet → KhachHang
    // ─────────────────────────────────────────────────────────────────────────
    private KhachHang mapRow(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang(
                rs.getString("maKH"),
                rs.getString("tenKH"),
                rs.getString("soCCCD"),
                rs.getString("soDT")
        );
        Date d = rs.getDate("ngaySinh");
        if (d != null) kh.setNgaySinh(d.toLocalDate());
        return kh;
    }
}