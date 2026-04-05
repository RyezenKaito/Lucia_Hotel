package dao;

import java.sql.*;
import connectDatabase.ConnectDatabase;
import model.entities.ChiTietDatPhong;
import model.entities.DatPhong;
import model.entities.Phong;

public class ChiTietDatPhongDAO {

    /**
     * Tìm chi tiết đặt phòng dựa trên mã đặt và mã phòng
     */
    public ChiTietDatPhong findChiTietDatPhong(String maDat, String maPhong) {
        String sql = "SELECT * FROM ChiTietDatPhong WHERE maDat = ? AND maPhong = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, maDat);
            pstmt.setString(2, maPhong);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm mới chi tiết đặt phòng
     */
    public boolean insert(ChiTietDatPhong ctdp) {
        String sql = "INSERT INTO ChiTietDatPhong (maCTDP, maPhong, maDat, giaCoc, soNguoi, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, ctdp.getMaCTDP());
            pstmt.setString(2, ctdp.getPhong().getMaPhong());
            pstmt.setString(3, ctdp.getDatPhong().getMaDat());
            pstmt.setDouble(4, ctdp.getGiaCoc());
            pstmt.setInt(5, ctdp.getSoNguoi());
            pstmt.setString(6, ctdp.getGhiChu());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private ChiTietDatPhong mapRow(ResultSet rs) throws SQLException {
        ChiTietDatPhong ctdp = new ChiTietDatPhong();
        ctdp.setMaCTDP(rs.getString("maCTDP"));
        String maPhong = rs.getString("maPhong");
        ctdp.setPhong(new PhongDAO().getAll().stream()
                .filter(p -> p.getMaPhong().equals(maPhong))
                .findFirst().orElse(new Phong(maPhong)));
        ctdp.setDatPhong(new DatPhong(rs.getString("maDat")));
        ctdp.setGiaCoc(rs.getDouble("giaCoc"));
        ctdp.setSoNguoi(rs.getInt("soNguoi"));
        ctdp.setGhiChu(rs.getString("ghiChu"));
        return ctdp;
    }
}
