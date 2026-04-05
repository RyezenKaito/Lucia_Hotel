package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.DatPhong;
import model.entities.KhachHang;

public class DatPhongDAO {
    
    /**
     * Tìm khách hàng dựa trên mã đặt phòng
     */
    public KhachHang findKhachHangByIdDatPhong(String maDat) {
        String sql = "SELECT maKH FROM DatPhong WHERE maDat = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDat);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return new KhachHangDAO().findKhachHangByID(rs.getString("maKH"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách mã đặt phòng cần Check-in trong hôm nay
     */
    public List<String> getMaDatPhongCheckInHomNay() {
        List<String> dsMa = new ArrayList<>();
        String sql = "SELECT maDat FROM DatPhong " +
                     "WHERE CAST(ngayCheckIn AS DATE) = CAST(GETDATE() AS DATE) " +
                     "AND ngayCheckIn IS NOT NULL"; // logic có thể cần điều chỉnh tùy nghiệp vụ
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dsMa.add(rs.getString("maDat"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsMa;
    }

    /**
     * Tìm thông tin tổng hợp để hiển thị lên giao diện CheckInPanel
     */
    public DatPhong findDatPhongDetail(String keyword) {
        String sql = "SELECT dp.*, kh.tenKH, kh.soDT, kh.soCCCD " +
                     "FROM DatPhong dp JOIN KH kh ON dp.maKH = kh.maKH " +
                     "WHERE dp.maDat = ? OR kh.soDT = ?";
        
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, keyword);
            pstmt.setString(2, keyword);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                KhachHang kh = new KhachHang(
                    rs.getString("maKH"),
                    rs.getString("tenKH"),
                    rs.getString("soCCCD"),
                    rs.getString("soDT")
                );

                DatPhong dp = new DatPhong();
                dp.setMaDat(rs.getString("maDat"));
                dp.setNgayDat(rs.getTimestamp("ngayDat") != null ? rs.getTimestamp("ngayDat").toLocalDateTime() : null);
                dp.setKhachHang(kh);
                dp.setNgayCheckIn(rs.getTimestamp("ngayCheckIn") != null ? rs.getTimestamp("ngayCheckIn").toLocalDateTime() : null);
                dp.setNgayCheckOut(rs.getTimestamp("ngayCheckOut") != null ? rs.getTimestamp("ngayCheckOut").toLocalDateTime() : null);
                
                return dp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert đơn đặt phòng mới
     */
    public boolean insert(DatPhong dp) {
        String sql = "INSERT INTO DatPhong(maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut) VALUES (?,?,?,?,?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, dp.getMaDat());
            pstmt.setTimestamp(2, dp.getNgayDat() != null ? Timestamp.valueOf(dp.getNgayDat()) : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, dp.getKhachHang().getMaKH());
            pstmt.setTimestamp(4, dp.getNgayCheckIn() != null ? Timestamp.valueOf(dp.getNgayCheckIn()) : null);
            pstmt.setTimestamp(5, dp.getNgayCheckOut() != null ? Timestamp.valueOf(dp.getNgayCheckOut()) : null);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy tên khách hàng đang ở trong phòng (Dựa trên ChiTietDatPhong join DatPhong join KH)
     */
    public String getTenKhachHienTai(String maPhong) {
        String sql = "SELECT kh.tenKH FROM KH kh " +
                     "JOIN DatPhong dp ON kh.maKH = dp.maKH " +
                     "JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat " +
                     "WHERE ctdp.maPhong = ? AND dp.ngayCheckOut IS NULL"; // Giả định đơn chưa checkout là đang ở
        
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("tenKH");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Lấy mã hóa đơn đang hoạt động của một phòng
     */
    public String getMaHDByMaPhong(String maPhong) {
        String sql = "SELECT h.maHD FROM HoaDon h " +
                     "JOIN DatPhong dp ON h.maDat = dp.maDat " +
                     "JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat " +
                     "WHERE ctdp.maPhong = ? AND dp.ngayCheckOut IS NULL";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("maHD");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lưu giỏ hàng dịch vụ vào database
     */
    public boolean saveServiceOrder(String maPhong, java.util.Map<model.entities.DichVu, Integer> cart) {
        String maHD = getMaHDByMaPhong(maPhong);
        if (maHD == null) return false;

        String sql = "INSERT INTO DichVuSuDung (maDV, maHD, ngaySuDung, soLuong, giaDV, trangThai) VALUES (?, ?, GETDATE(), ?, ?, N'Chưa thanh toán')";
        
        Connection con = null;
        try {
            con = ConnectDatabase.getInstance().getConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (java.util.Map.Entry<model.entities.DichVu, Integer> entry : cart.entrySet()) {
                    ps.setString(1, entry.getKey().getMaDV());
                    ps.setString(2, maHD);
                    ps.setInt(3, entry.getValue());
                    ps.setDouble(4, entry.getKey().getGia());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }
}