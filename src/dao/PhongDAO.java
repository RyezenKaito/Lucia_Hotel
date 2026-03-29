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

    // --- 1. BỘ DỊCH THUẬT: TỪ SQL -> JAVA (Enum) ---
    private TrangThaiPhong findEnumByString(String text) {
        if (text == null) return TrangThaiPhong.CONTRONG;
        text = text.trim().toUpperCase();
        
        // So khớp với đúng 3 chữ trong CSDL của bạn
        if (text.equals("CONTRONG")) return TrangThaiPhong.CONTRONG;
        if (text.equals("DANGSUDUNG")) return TrangThaiPhong.DACOKHACH;
        if (text.equals("BAN")) return TrangThaiPhong.BAN;
        
        return TrangThaiPhong.CONTRONG; // Mặc định
    }

    // --- 2. BỘ DỊCH THUẬT: TỪ JAVA (Enum) -> SQL ---
    private String trangThaiToString(TrangThaiPhong ttp) {
        if (ttp == TrangThaiPhong.CONTRONG) return "CONTRONG"; // Trả về đúng mật khẩu SQL cần
        if (ttp == TrangThaiPhong.DACOKHACH) return "DANGSUDUNG";
        return "BAN"; 
    }

    public List<Phong> getDanhSachPhongTrong(String tenLoai, Date checkIn, Date checkOut) {
        List<Phong> list = new ArrayList<>();
        String sql = "SELECT maPhong, tenLoaiPhong, tinhTrang, soTang FROM Phong " +
                     "WHERE tenLoaiPhong = ? AND maPhong NOT IN (" +
                     "    SELECT dp.maPhong FROM ChiTietDatPhong dp " +
                     "    JOIN DatPhong d ON dp.maDatPhong = d.maDatPhong " +
                     "    WHERE (d.ngayCheckInDuKien < ? AND d.ngayCheckOutDuKien > ?))";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, tenLoai.trim());
            ps.setDate(2, checkOut); 
            ps.setDate(3, checkIn);  

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maP = rs.getString("maPhong").trim();
                TenLoaiPhong tlp = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong").trim());
                TrangThaiPhong tt = findEnumByString(rs.getString("tinhTrang")); 
                int tang = rs.getInt("soTang");

                list.add(new Phong(maP, new LoaiPhong(tlp), tt, tang));
            }
        } catch (Exception e) {
            System.err.println("Lỗi getDanhSachPhongTrong: ");
            e.printStackTrace();
        }
        return list;
    }

    public List<Phong> getAll() {
        List<Phong> ds = new ArrayList<>();
        String sql = "SELECT maPhong, tenLoaiPhong, tinhTrang, soTang FROM Phong";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                TenLoaiPhong tlp = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong").trim());
                TrangThaiPhong ttp = findEnumByString(rs.getString("tinhTrang")); 
                ds.add(new Phong(rs.getString("maPhong").trim(), new LoaiPhong(tlp), ttp, rs.getInt("soTang")));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return ds;
    }

    // Lưu ý: Nếu có dùng hàm này để search theo từ khóa Tiếng Việt, nhớ truyền đúng biến. 
    // Nhưng hiện tại giao diện đã dùng TableRowSorter để search nên không bị ảnh hưởng.
    public List<Phong> getByTrangThai(String trangThaiTiengViet) {
        List<Phong> ds = new ArrayList<>();
        String sql = "SELECT maPhong, tenLoaiPhong, tinhTrang, soTang FROM Phong WHERE tinhTrang = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setNString(1, trangThaiTiengViet.trim());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TenLoaiPhong tlp = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong").trim());
                TrangThaiPhong ttp = findEnumByString(rs.getString("tinhTrang"));
                ds.add(new Phong(rs.getString("maPhong").trim(), new LoaiPhong(tlp), ttp, rs.getInt("soTang")));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return ds;
    }
 // --- HÀM LẤY SỨC CHỨA TỪ BẢNG LoaiPhong_ThongTin ---
    public int getSucChua(String tenLoaiPhong) {
        int sucChua = 0;
        String sql = "SELECT sucChua FROM LoaiPhong_ThongTin WHERE tenLoaiPhong = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, tenLoaiPhong.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                sucChua = rs.getInt("sucChua");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucChua;
    }
    public boolean insert(Phong p) {
        String sql = "INSERT INTO Phong(maPhong, tenLoaiPhong, tinhTrang, soTang) VALUES (?,?,?,?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getMaPhong().trim());
            pstmt.setString(2, p.getLoaiPhong().getTenLoaiPhong().toString().trim());
            
            // Gửi đúng "mật khẩu" DANGSUDUNG, CONTRONG, BAN xuống SQL
            pstmt.setString(3, trangThaiToString(p.getTrangThai())); 
            
            pstmt.setInt(4, p.getSoTang());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { 
            System.err.println("LỖI INSERT PHÒNG:");
            e.printStackTrace();
            return false; 
        }
    }

    public boolean update(Phong p) {
        String sql = "UPDATE Phong SET tenLoaiPhong=?, tinhTrang=?, soTang=? WHERE maPhong=?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getLoaiPhong().getTenLoaiPhong().toString().trim());
            
            // Gửi đúng "mật khẩu" xuống SQL
            pstmt.setString(2, trangThaiToString(p.getTrangThai()));
            
            pstmt.setInt(3, p.getSoTang());
            pstmt.setString(4, p.getMaPhong().trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("LỖI UPDATE PHÒNG:");
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
            System.err.println("LỖI DELETE PHÒNG:");
            e.printStackTrace();
            return false; 
        }
    }
}