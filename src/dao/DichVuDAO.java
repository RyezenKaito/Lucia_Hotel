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
        String sql = "SELECT * FROM DV";

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
        String sql = "SELECT * FROM DV WHERE loaiDV = ?";
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
        String sql = "UPDATE DV SET tenDV = ?, gia = ?, loaiDV = ?, mieuTa = ?, donVi = ? WHERE maDV = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, dv.getTenDV());
            pstmt.setDouble(2, dv.getGia());
            pstmt.setString(3, dv.getLoaiDV());
            pstmt.setString(4, dv.getMieuTa());
            pstmt.setString(5, dv.getDonVi());
            pstmt.setString(6, dv.getMaDV());

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
        String sql = "INSERT INTO DV (maDV, tenDV, gia, loaiDV, mieuTa, donVi) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, dv.getMaDV());
            pstmt.setString(2, dv.getTenDV());
            pstmt.setDouble(3, dv.getGia());
            pstmt.setString(4, dv.getLoaiDV());
            pstmt.setString(5, dv.getMieuTa());
            pstmt.setString(6, dv.getDonVi());

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
        return dv;
    }
}