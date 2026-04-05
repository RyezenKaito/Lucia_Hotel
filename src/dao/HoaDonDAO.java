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
            while(rs.next()) {
                dsHoaDon.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
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
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) { e.printStackTrace(); }
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
            ps.setTimestamp(4, hd.getNgayTaoHD() != null ? Timestamp.valueOf(hd.getNgayTaoHD()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setDouble(5, hd.getTienPhong());
            ps.setDouble(6, hd.getTienDV());
            ps.setDouble(7, hd.getTienCoc());
            ps.setDouble(8, hd.getThueVAT());
            ps.setDouble(9, hd.getTongTien());

            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
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
}
