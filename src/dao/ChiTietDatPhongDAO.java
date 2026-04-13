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

    /**
     * Tự động phát sinh mã chi tiết đặt phòng
     */
    public String generateMaCTDP() {
        String sql = "SELECT maCTDP FROM ChiTietDatPhong WHERE maCTDP LIKE 'CTDP%'";
        int max = 0;
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String last = rs.getString(1);
                if (last != null && last.length() > 4) {
                    try {
                        int num = Integer.parseInt(last.substring(4));
                        if(num > max) max = num;
                    } catch(Exception ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("CTDP%03d", max + 1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INSERT (dùng chung Connection cho transaction)
    // ─────────────────────────────────────────────────────────────────────────
    public boolean insertWithConnection(Connection con, String maCTDP, String maPhong,
                                        String maDat, double giaCoc, int soNguoi, String ghiChu) throws SQLException {
        String sql = "INSERT INTO ChiTietDatPhong(maCTDP, maPhong, maDat, giaCoc, soNguoi, ghiChu) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTDP);
            ps.setString(2, maPhong);
            ps.setString(3, maDat);
            ps.setDouble(4, giaCoc);
            ps.setInt(5, soNguoi);
            ps.setString(6, ghiChu);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CẬP NHẬT CHI TIẾT ĐẶT PHÒNG THEO MÃ ĐẶT (dùng chung Connection)
    // ─────────────────────────────────────────────────────────────────────────
    public boolean updateByMaDat(Connection con, String maDat, String maPhong,
                                  double giaCoc, int soNguoi, String ghiChu) throws SQLException {
        String sql = "UPDATE ChiTietDatPhong SET maPhong=?, giaCoc=?, soNguoi=?, ghiChu=? WHERE maDat=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setDouble(2, giaCoc);
            ps.setInt(3, soNguoi);
            ps.setString(4, ghiChu);
            ps.setString(5, maDat);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CẬP NHẬT THÔNG TIN CHUNG (KHÔNG ĐỔI MÃ PHÒNG) CHO MULTI-ROOM
    // ─────────────────────────────────────────────────────────────────────────
    public boolean updateInfoByMaDat(Connection con, String maDat,
                                  double giaCoc, int soNguoi, String ghiChu) throws SQLException {
        int roomCount = 1;
        try (PreparedStatement psCount = con.prepareStatement("SELECT COUNT(*) FROM ChiTietDatPhong WHERE maDat=?")) {
            psCount.setString(1, maDat);
            ResultSet rsCount = psCount.executeQuery();
            if (rsCount.next()) roomCount = Math.max(1, rsCount.getInt(1));
        }

        double cdpCoc = giaCoc / roomCount;
        int cdpNguoi = soNguoi / roomCount;

        String sql = "UPDATE ChiTietDatPhong SET giaCoc=?, soNguoi=?, ghiChu=? WHERE maDat=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, cdpCoc);
            ps.setInt(2, cdpNguoi);
            ps.setString(3, ghiChu);
            ps.setString(4, maDat);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // [THÊM MỚI] LẤY TỔNG TIỀN CỌC CỦA MỘT ĐƠN ĐẶT PHÒNG
    // ─────────────────────────────────────────────────────────────────────────
    public double getTongCocByMaDat(String maDat) {
        String sql = "SELECT SUM(giaCoc) FROM ChiTietDatPhong WHERE maDat = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDat);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // [THÊM MỚI] LẤY KIỂU PHÒNG THEO MÃ ĐẶT 
    // ─────────────────────────────────────────────────────────────────────────
    public java.util.List<String> getMaPhongByMaDat(String maDat) {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT maPhong FROM ChiTietDatPhong WHERE maDat = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDat);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // [THÊM MỚI] XÓA TOÀN BỘ CHI TIẾT ĐẶT PHÒNG DỰA TRÊN MÃ ĐẶT
    // ─────────────────────────────────────────────────────────────────────────
    public boolean deleteByMaDat(String maDat) {
        String sql = "DELETE FROM ChiTietDatPhong WHERE maDat = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDat);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}