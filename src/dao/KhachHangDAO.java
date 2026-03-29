package dao;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import connectDatabase.ConnectDatabase;
import model.entities.KhachHang;

/**
 * DAO cho bảng KhachHang.
 *
 * Tên cột DB (quanlydatphong_taobang.sql):
 *   maKhachHang | hoTen | soDienThoai | ngaySinh | soCanCuocCongDan
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
                               .executeQuery("SELECT * FROM KhachHang");
            while (rs.next()) ds.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAll KhachHang: " + e.getMessage());
        }
        return ds;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  THÊM
    // ─────────────────────────────────────────────────────────────────────────
    public boolean insert(KhachHang kh) {
        String sql = "INSERT INTO KhachHang "
                   + "(maKhachHang, hoTen, soDienThoai, ngaySinh, soCanCuocCongDan) "
                   + "VALUES (?,?,?,?,?)";
        try {
            Connection con         = ConnectDatabase.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, kh.getMaKhachHang());
            pstmt.setString(2, kh.getHoTen());
            pstmt.setString(3, kh.getSoDienThoai());
            if (kh.getNgaySinh() != null)
                pstmt.setDate(4, Date.valueOf(kh.getNgaySinh()));
            else
                pstmt.setNull(4, Types.DATE);
            pstmt.setString(5, kh.getCCCD());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("insert KhachHang: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CẬP NHẬT  (đã sửa đúng tên cột & thứ tự param)
    // ─────────────────────────────────────────────────────────────────────────
    public boolean update(KhachHang kh) {
        String sql = "UPDATE KhachHang "
                   + "SET hoTen=?, soDienThoai=?, ngaySinh=?, soCanCuocCongDan=? "
                   + "WHERE maKhachHang=?";
        try {
            Connection con         = ConnectDatabase.getInstance().getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, kh.getHoTen());
            pstmt.setString(2, kh.getSoDienThoai());
            if (kh.getNgaySinh() != null)
                pstmt.setDate(3, Date.valueOf(kh.getNgaySinh()));
            else
                pstmt.setNull(3, Types.DATE);
            pstmt.setString(4, kh.getCCCD());
            pstmt.setString(5, kh.getMaKhachHang());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("update KhachHang: " + e.getMessage());
            return false;
        }
    }    
    
    // ─────────────────────────────────────────────────────────────────────────
    //  XÓA
    // ─────────────────────────────────────────────────────────────────────────
    public boolean delete(String maKH) {
    	String sql = "DELETE from KhachHang WHERE maKhachHang = ?";
    	try {
    		Connection con = ConnectDatabase.getInstance().getConnection();
    		PreparedStatement pstmt = con.prepareStatement(sql);
    		pstmt.setString(1, maKH);
    		return pstmt.executeUpdate() > 0;
    	} catch(SQLException e) {
    		System.err.println("delete Khachhang: " + e.getMessage());
    		return false;
    	}
    }
    

    // ─────────────────────────────────────────────────────────────────────────
    //  TÌM THEO MÃ (LIKE)
    // ─────────────────────────────────────────────────────────────────────────
    public KhachHang findKhachHangByID(String keyword) {
        String sql = "SELECT * FROM KhachHang WHERE maKhachHang LIKE ?";
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
    //  SINH MÃ KH TIẾP THEO  → KH00000001, KH00000002, ...
    // ─────────────────────────────────────────────────────────────────────────
    public String getNextMaKH() {
        String sql = "SELECT MAX(maKhachHang) FROM KhachHang";
        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            ResultSet  rs  = con.createStatement().executeQuery(sql);
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);           // VD: "KH00000005"
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
        String sql = "SELECT COUNT(*) FROM KhachHang "
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
    //  Trả về List<String[]>: [maKH, hoTen, soDienThoai, ngaySinh, dsPhong]
    // ─────────────────────────────────────────────────────────────────────────
    public List<String[]> getBirthdayTodayWithRoom() {
        // Dùng LinkedHashMap để giữ thứ tự chèn
        Map<String, String[]>    custMap = new LinkedHashMap<>();
        Map<String, List<String>> roomMap = new HashMap<>();

        String sql =
            "SELECT kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.ngaySinh, " +
            "       p.maPhong " +
            "FROM   KhachHang kh " +
            "LEFT JOIN DatPhong dp " +
            "       ON  kh.maKhachHang        = dp.maKhachHang " +
            "       AND dp.ngayCheckInThucTe  IS NOT NULL " +
            "       AND dp.ngayCheckOutThucTe IS NULL " +
            "LEFT JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
            "LEFT JOIN Phong           p    ON ctdp.maPhong  = p.maPhong " +
            "WHERE DAY(kh.ngaySinh)   = DAY(GETDATE()) " +
            "  AND MONTH(kh.ngaySinh) = MONTH(GETDATE())";

        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            ResultSet  rs  = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                String maKH = rs.getString("maKhachHang");
                if (!custMap.containsKey(maKH)) {
                    Date d = rs.getDate("ngaySinh");
                    String nsStr = d != null
                            ? d.toLocalDate().format(DATE_FMT) : "";
                    custMap.put(maKH, new String[]{
                            maKH,
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
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
                rs.getString("maKhachHang"),
                rs.getString("hoTen"),
                rs.getString("soCanCuocCongDan"),
                rs.getString("soDienThoai")
        );
        Date d = rs.getDate("ngaySinh");
        if (d != null) kh.setNgaySinh(d.toLocalDate());
        return kh;
    }
    
}