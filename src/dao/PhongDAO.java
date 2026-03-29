package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connectDatabase.ConnectDatabase;
import model.entities.LoaiPhong;
import model.entities.Phong;
import model.enums.TenLoaiPhong;
import model.enums.TrangThaiPhong;

public class PhongDAO {

    private TrangThaiPhong findEnumByString(String text) {
        if (text == null) return TrangThaiPhong.CONTRONG;
        text = text.trim().toUpperCase();
        if (text.equals("CONTRONG")) return TrangThaiPhong.CONTRONG;
        if (text.equals("DANGSUDUNG")) return TrangThaiPhong.DACOKHACH;
        if (text.equals("BAN")) return TrangThaiPhong.BAN;
        return TrangThaiPhong.CONTRONG; 
    }

    private String trangThaiToString(TrangThaiPhong ttp) {
        if (ttp == TrangThaiPhong.CONTRONG) return "CONTRONG"; 
        if (ttp == TrangThaiPhong.DACOKHACH) return "DANGSUDUNG";
        return "BAN"; 
    }

    public List<Phong> getAll() {
        List<Phong> ds = new ArrayList<>();
        String sql = "SELECT p.maPhong, p.tinhTrang, p.soTang, l.tenLoaiPhong, l.sucChua, l.donGia " +
                     "FROM Phong p JOIN LoaiPhong l ON p.tenLoaiPhong = l.tenLoaiPhong";
        
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                TenLoaiPhong tenLoai = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong").trim());
                LoaiPhong lp = new LoaiPhong(tenLoai, rs.getInt("sucChua"), rs.getDouble("donGia"));
                TrangThaiPhong tt = findEnumByString(rs.getString("tinhTrang"));
                
                Phong p = new Phong(rs.getString("maPhong"), lp, tt, rs.getInt("soTang"));
                ds.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean insert(Phong p) {
        String sql = "INSERT INTO Phong(maPhong, tenLoaiPhong, tinhTrang, soTang) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getMaPhong().trim());
            pstmt.setString(2, p.getLoaiPhong().getTenLoaiPhong().toString().trim());
            pstmt.setString(3, trangThaiToString(p.getTrangThai()));
            pstmt.setInt(4, p.getSoTang());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace();
            return false; 
        }
    }

    public boolean update(Phong p) {
        String sql = "UPDATE Phong SET tenLoaiPhong=?, tinhTrang=?, soTang=? WHERE maPhong=?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getLoaiPhong().getTenLoaiPhong().toString().trim());
            pstmt.setString(2, trangThaiToString(p.getTrangThai()));
            pstmt.setInt(3, p.getSoTang());
            pstmt.setString(4, p.getMaPhong().trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace();
            return false; 
        }
    }

    public boolean delete(String maPhong) {
        String sql = "DELETE FROM Phong WHERE maPhong=?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maPhong.trim());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace();
            return false; 
        }
    }
}