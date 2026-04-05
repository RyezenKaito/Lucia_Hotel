package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.BangGiaDichVu;
import model.entities.BangGiaDichVu_ChiTiet;
import model.entities.DichVu;

public class BangGiaDichVuDAO {

    /**
     * Lấy danh sách tất cả bảng giá từ bảng BGDichVu
     */
    public List<BangGiaDichVu> getAllBangGia() {
        List<BangGiaDichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM BGDichVu";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBangGia(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy chi tiết của một bảng giá từ bảng BG_ChiTiet
     */
    public List<BangGiaDichVu_ChiTiet> getChiTietByMa(String maBG) {
        List<BangGiaDichVu_ChiTiet> list = new ArrayList<>();
        String sql = "SELECT * FROM BG_ChiTiet WHERE maBG = ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBG);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapChiTiet(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Thêm mới bảng giá thông tin
     */
    public boolean insertBangGia(BangGiaDichVu bg) {
        String sql = "INSERT INTO BGDichVu (maBG, tenBG, ngayAD, ngayHet, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bg.getMaBangGia());
            ps.setString(2, bg.getTenBangGia());
            ps.setDate(3, new java.sql.Date(bg.getNgayApDung().getTime()));
            ps.setDate(4, new java.sql.Date(bg.getNgayHetHieuLuc().getTime()));
            ps.setInt(5, bg.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /**
     * Thêm chi tiết bảng giá
     */
    public boolean insertChiTiet(BangGiaDichVu_ChiTiet ct) {
        String sql = "INSERT INTO BG_ChiTiet (maBG, maDV, giaApDung) VALUES (?, ?, ?)";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ct.getMaBangGia().getMaBangGia());
            ps.setString(2, ct.getMaDichVu().getMaDV());
            ps.setDouble(3, ct.getGiaDichVu());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /**
     * Xóa bảng giá (Transaction: Xóa chi tiết trước)
     */
    public boolean deleteBangGia(String maBG) {
        String sqlChiTiet = "DELETE FROM BG_ChiTiet WHERE maBG = ?";
        String sqlThongTin = "DELETE FROM BGDichVu WHERE maBG = ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlChiTiet);
                 PreparedStatement ps2 = conn.prepareStatement(sqlThongTin)) {
                ps1.setString(1, maBG);
                ps1.executeUpdate();
                ps2.setString(1, maBG);
                ps2.executeUpdate();
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                return false;
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /**
     * Map dữ liệu từ ResultSet sang Object BangGiaDichVu
     */
    private BangGiaDichVu mapBangGia(ResultSet rs) throws SQLException {
        BangGiaDichVu bg = new BangGiaDichVu();
        bg.setMaBangGia(rs.getString("maBG"));
        bg.setTenBangGia(rs.getString("tenBG"));
        bg.setNgayApDung(rs.getDate("ngayAD"));
        bg.setNgayHetHieuLuc(rs.getDate("ngayHet"));
        bg.setTrangThai(rs.getInt("trangThai"));
        return bg;
    }

    /**
     * Map dữ liệu từ ResultSet sang Object BangGiaDichVu_ChiTiet
     */
    private BangGiaDichVu_ChiTiet mapChiTiet(ResultSet rs) throws SQLException {
        BangGiaDichVu_ChiTiet ct = new BangGiaDichVu_ChiTiet();
        ct.setMaBangGia(new BangGiaDichVu(rs.getString("maBG")));
        ct.setMaDichVu(new DichVu(rs.getString("maDV")));
        ct.setGiaDichVu(rs.getDouble("giaApDung"));
        return ct;
    }
    
    /**
     * Lưu toàn bộ bảng giá và chi tiết dịch vụ đi kèm
     */
    public boolean insertFullBangGia(BangGiaDichVu thongTin, List<BangGiaDichVu_ChiTiet> dsChiTiet) {
        Connection con = null;
        try {
            con = ConnectDatabase.getInstance().getConnection();
            con.setAutoCommit(false);

            String sqlThongTin = "INSERT INTO BGDichVu (maBG, tenBG, ngayAD, ngayHet, trangThai) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = con.prepareStatement(sqlThongTin)) {
                ps1.setString(1, thongTin.getMaBangGia());
                ps1.setString(2, thongTin.getTenBangGia());
                ps1.setDate(3, new java.sql.Date(thongTin.getNgayApDung().getTime()));
                ps1.setDate(4, new java.sql.Date(thongTin.getNgayHetHieuLuc().getTime()));
                ps1.setInt(5, thongTin.getTrangThai());
                ps1.executeUpdate();
            }

            String sqlChiTiet = "INSERT INTO BG_ChiTiet (maBG, maDV, giaApDung) VALUES (?, ?, ?)";
            try (PreparedStatement ps2 = con.prepareStatement(sqlChiTiet)) {
                for (BangGiaDichVu_ChiTiet ct : dsChiTiet) {
                    ps2.setString(1, thongTin.getMaBangGia());
                    ps2.setString(2, ct.getMaDichVu().getMaDV());
                    ps2.setDouble(3, ct.getGiaDichVu());
                    ps2.addBatch();
                }
                ps2.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật toàn bộ bảng giá và chi tiết dịch vụ đi kèm
     */
    public boolean updateFullBangGia(BangGiaDichVu bg, List<BangGiaDichVu_ChiTiet> dsChiTiet) {
        Connection con = null;
        try {
            con = ConnectDatabase.getInstance().getConnection();
            con.setAutoCommit(false);

            String sqlUpdateBG = "UPDATE BGDichVu SET tenBG = ?, ngayAD = ?, ngayHet = ? WHERE maBG = ?";
            try (PreparedStatement ps1 = con.prepareStatement(sqlUpdateBG)) {
                ps1.setString(1, bg.getTenBangGia());
                ps1.setDate(2, new java.sql.Date(bg.getNgayApDung().getTime()));
                ps1.setDate(3, new java.sql.Date(bg.getNgayHetHieuLuc().getTime()));
                ps1.setString(4, bg.getMaBangGia());
                ps1.executeUpdate();
            }

            String sqlDeleteDetails = "DELETE FROM BG_ChiTiet WHERE maBG = ?";
            try (PreparedStatement ps2 = con.prepareStatement(sqlDeleteDetails)) {
                ps2.setString(1, bg.getMaBangGia());
                ps2.executeUpdate();
            }

            String sqlInsertDetails = "INSERT INTO BG_ChiTiet (maBG, maDV, giaApDung) VALUES (?, ?, ?)";
            try (PreparedStatement ps3 = con.prepareStatement(sqlInsertDetails)) {
                for (BangGiaDichVu_ChiTiet ct : dsChiTiet) {
                    ps3.setString(1, bg.getMaBangGia());
                    ps3.setString(2, ct.getMaDichVu().getMaDV());
                    ps3.setDouble(3, ct.getGiaDichVu());
                    ps3.addBatch();
                }
                ps3.executeBatch();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public BangGiaDichVu getBangGiaByMa(String maBG) {
        String sql = "SELECT * FROM BGDichVu WHERE maBG = ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBangGia(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
