package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connectDatabase.ConnectDatabase;
import model.entities.LoaiPhong;

public class LoaiPhongDAO {    
    
    /**
     * Lấy toàn bộ tên loại phòng để hiển thị lên ComboBox
     */
    public String[] fetchAllRoomTypeNames() {
        List<String> list = new ArrayList<>();
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT maLoaiPhong FROM LoaiPhong")) {

            while (rs.next()) {
                list.add(rs.getString("maLoaiPhong"));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            
            while(rs.next()) {
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
            if (rs.next()) return mapRow(rs);
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