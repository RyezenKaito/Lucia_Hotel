
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.DatPhong;
import model.entities.HoaDon;

public class HoaDonDAO {

    /**
     * Lấy toàn bộ hóa đơn từ bảng HoaDon
     */
    public List<HoaDon> getAll() {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dsHoaDon.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
        }
        return dsHoaDon;
    }

    /**
     * Tìm hóa đơn theo mã
     */
    public HoaDon getById(String maHD) {
        String sql = "SELECT * FROM HoaDon WHERE maHD = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy hóa đơn theo mã: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm hóa đơn theo mã đặt phòng
     */
    public HoaDon getByMaDat(String maDat) {
        String sql = "SELECT * FROM HoaDon WHERE maDat = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDat);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy hóa đơn theo mã đặt phòng: " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm mới hóa đơn
     */
    public boolean insert(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (maHD, maDat, maNV, ngayTaoHD, tienPhong, tienDV, tienCoc, thueVAT, tongTien) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setString(2, hd.getDatPhong().getMaDat());
            ps.setString(3, hd.getNhanVien().getMaNV());
            ps.setTimestamp(4, hd.getNgayTaoHD() != null ? Timestamp.valueOf(hd.getNgayTaoHD())
                    : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setDouble(5, hd.getTienPhong());
            ps.setDouble(6, hd.getTienDV());
            ps.setDouble(7, hd.getTienCoc());
            ps.setDouble(8, hd.getThueVAT());
            ps.setDouble(9, hd.getTongTien());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm hóa đơn: " + e.getMessage());
            return false;
        }
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getString("maHD"));
        hd.setNgayTaoHD(rs.getTimestamp("ngayTaoHD") != null ? rs.getTimestamp("ngayTaoHD").toLocalDateTime() : null);
        hd.setDatPhong(new DatPhong(rs.getString("maDat")));
        hd.setNhanVien(new NhanVienDAO().getById(rs.getString("maNV")));
        hd.setTienPhong(rs.getDouble("tienPhong"));
        hd.setTienDV(rs.getDouble("tienDV"));
        hd.setTienCoc(rs.getDouble("tienCoc"));
        hd.setThueVAT(rs.getDouble("thueVAT"));
        hd.setTongTien(rs.getDouble("tongTien"));
        return hd;
    }

    /**
     * Tự động phát sinh mã hóa đơn
     */
    public String generateMaHD() {
        String sql = "SELECT maHD FROM HoaDon WHERE maHD LIKE 'HD%'";
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
            System.err.println("Lỗi khi phát sinh mã hóa đơn: " + e.getMessage());
        }
        return String.format("HD%03d", max + 1);
    }
}
