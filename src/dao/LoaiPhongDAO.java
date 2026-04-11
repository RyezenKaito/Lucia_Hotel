package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connectDatabase.ConnectDatabase;
import model.entities.LoaiPhong;
import model.enums.TenLoaiPhong;

public class LoaiPhongDAO {

    private TenLoaiPhong findEnumByString(String loaiPhong) {
        loaiPhong = loaiPhong.toUpperCase();
        if (loaiPhong.equals("DOUBLE")) {
            return TenLoaiPhong.DOUBLE;
        } else if (loaiPhong.equals("FAMILY"))
            return TenLoaiPhong.FAMILY;
        else if (loaiPhong.equals("SINGLE"))
            return TenLoaiPhong.SINGLE;
        else if (loaiPhong.equals("TRIPLE"))
            return TenLoaiPhong.TRIPLE;
        else if (loaiPhong.equals("TWIN"))
            return TenLoaiPhong.TWIN;
        else
            return null;
    }

    /**
     * Lấy toàn bộ tên loại phòng để hiển thị lên ComboBox
     */
    public String[] fetchAllRoomTypeNames() {
        List<String> list = new ArrayList<>();
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT maLoaiPhong FROM LoaiPhong")) {

            while (rs.next()) {
                list.add(findEnumByString(rs.getString("maLoaiPhong")).getDisplayName());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tất cả loại phòng: " + e.getMessage());
        }
        return list.toArray(new String[0]);
    }

    /**
     * Lấy toàn bộ thực thể LoaiPhong
     */
    public List<LoaiPhong> getAll() {
        List<LoaiPhong> ds = new ArrayList<>();
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM LoaiPhong")) {

            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Tìm theo mã loại phòng
     */
    public LoaiPhong findByID(String maLoaiPhong) {
        String sql = "SELECT * FROM LoaiPhong WHERE maLoaiPhong = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maLoaiPhong);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private LoaiPhong mapRow(ResultSet rs) throws SQLException {
        LoaiPhong lp = new LoaiPhong();
        lp.setMaLoaiPhong(rs.getString("maLoaiPhong"));
        lp.setGia(rs.getDouble("gia"));
        lp.setSucChua(rs.getInt("sucChua"));
        return lp;
    }
}