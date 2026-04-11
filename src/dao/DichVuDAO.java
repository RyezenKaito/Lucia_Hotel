package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.DichVu;

public class DichVuDAO {

    /**
     * Lấy tất cả dịch vụ từ bảng DV
     */
    public List<DichVu> getAll() {
        List<DichVu> ds = new ArrayList<>();
        String sql = "SELECT * FROM DV WHERE trangThai = 0";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy danh sách dịch vụ theo loại
     */
    public List<DichVu> getByType(String loaiDV) {
        List<DichVu> ds = new ArrayList<>();
        String sql = "SELECT * FROM DV WHERE loaiDV = ? AND trangThai = 0";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loaiDV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Cập nhật thông tin dịch vụ
     */
    public boolean update(DichVu dv) {
        String sql = "UPDATE DV SET tenDV = ?, gia = ?, loaiDV = ?, mieuTa = ?, donVi = ?, trangThai = ? WHERE maDV = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, dv.getTenDV());
            pstmt.setDouble(2, dv.getGia());
            pstmt.setString(3, dv.getLoaiDV());
            pstmt.setString(4, dv.getMieuTa());
            pstmt.setString(5, dv.getDonVi());
            pstmt.setInt(6, dv.getTrangThai());
            pstmt.setString(7, dv.getMaDV());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm mới dịch vụ
     */
    public boolean insert(DichVu dv) {
        String sql = "INSERT INTO DV (maDV, tenDV, gia, loaiDV, mieuTa, donVi, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, dv.getMaDV());
            pstmt.setString(2, dv.getTenDV());
            pstmt.setDouble(3, dv.getGia());
            pstmt.setString(4, dv.getLoaiDV());
            pstmt.setString(5, dv.getMieuTa());
            pstmt.setString(6, dv.getDonVi());
            pstmt.setInt(7, 0); // Active by default

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy thông tin dịch vụ theo mã dịch vụ (ID)
     */
    public DichVu getServiceByID(String ma) {
        String sql = "SELECT * FROM DV WHERE maDV = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DichVu mapRow(ResultSet rs) throws SQLException {
        DichVu dv = new DichVu();
        dv.setMaDV(rs.getString("maDV"));
        dv.setTenDV(rs.getString("tenDV"));
        dv.setGia(rs.getDouble("gia"));
        dv.setLoaiDV(rs.getString("loaiDV"));
        dv.setMieuTa(rs.getString("mieuTa"));
        dv.setDonVi(rs.getString("donVi"));
        try {
            dv.setTrangThai(rs.getInt("trangThai"));
        } catch (SQLException e) {
            dv.setTrangThai(0); // Graceful fallback
        }
        return dv;
    }

    /**
     * Kiểm tra mã dịch vụ đã tồn tại trong CSDL chưa
     */
    public boolean exists(String maDV) {
        String sql = "SELECT COUNT(*) FROM DV WHERE maDV = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa dịch vụ theo mã (Ẩn đi, không xóa vĩnh viễn)
     */
    public boolean delete(String maDV) {
        String sql = "UPDATE DV SET trangThai = 1 WHERE maDV = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sinh mã dịch vụ tiếp theo (DV001, DV002, ...)
     */
    public String generateNextMaDV() {
        String sql = "SELECT maDV FROM DV WHERE maDV LIKE 'DV%'";
        int max = 0;
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String last = rs.getString(1);
                if (last != null && last.length() > 2) {
                    try {
                        int num = Integer.parseInt(last.substring(2));
                        if (num > max)
                            max = num;
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("DV%03d", max + 1);
    }
}