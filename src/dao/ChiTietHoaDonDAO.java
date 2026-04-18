package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.ChiTietHoaDon;

public class ChiTietHoaDonDAO {

    /**
     * Tự động phát sinh mã ChiTietHoaDon
     */
    public String generateMaCTHD() {
        String sql = "SELECT maCTHD FROM ChiTietHoaDon WHERE maCTHD LIKE 'CTHD%'";
        int max = 0;
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String last = rs.getString(1);
                if (last != null && last.length() > 4) {
                    try {
                        int num = Integer.parseInt(last.substring(4));
                        if (num > max) max = num;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("CTHD%03d", max + 1);
    }

    /**
     * Thêm mới một dòng ChiTietHoaDon (link HoaDon ↔ ChiTietDatPhong)
     * @param maCTHD Mã chi tiết hóa đơn
     * @param maHD   Mã hóa đơn
     * @param maCTDP Mã chi tiết đặt phòng (1 phòng cụ thể)
     * @param thoiGianLuuTru Số đêm lưu trú
     * @param thanhTien Tổng tiền phòng của dòng này
     */
    public boolean insert(String maCTHD, String maHD, String maCTDP,
                          double thoiGianLuuTru, double thanhTien) {
        String sql = "INSERT INTO ChiTietHoaDon (maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTHD);
            ps.setString(2, maHD);
            ps.setString(3, maCTDP);
            ps.setDouble(4, thoiGianLuuTru);
            ps.setDouble(5, thanhTien);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi insert ChiTietHoaDon: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insert dùng chung Connection (transaction)
     */
    public boolean insertWithConnection(Connection con, String maCTHD, String maHD,
                                        String maCTDP, double thoiGianLuuTru, double thanhTien) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTHD);
            ps.setString(2, maHD);
            ps.setString(3, maCTDP);
            ps.setDouble(4, thoiGianLuuTru);
            ps.setDouble(5, thanhTien);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách ChiTietHoaDon theo maHD
     */
    public List<ChiTietHoaDon> getByMaHD(String maHD) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHD = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon c = new ChiTietHoaDon();
                c.setMaCTHD(rs.getString("maCTHD"));
                c.setMaHD(rs.getString("maHD"));
                c.setMaCTDP(rs.getString("maCTDP"));
                c.setThoiGianLuuTru(rs.getDouble("thoiGianLuuTru"));
                c.setThanhTien(rs.getDouble("thanhTien"));
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy maCTHD (đầu tiên) theo maHD – dùng cho DichVuSuDung
     */
    public String getMaCTHDByMaHD(String maHD) {
        String sql = "SELECT TOP 1 maCTHD FROM ChiTietHoaDon WHERE maHD = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy maCTHD theo maCTDP – tìm dòng CTHD chứa phòng cụ thể
     */
    public String getMaCTHDByMaCTDP(String maCTDP) {
        String sql = "SELECT TOP 1 maCTHD FROM ChiTietHoaDon WHERE maCTDP = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCTDP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ChiTietHoaDon> getAll() {
        return new ArrayList<>();
    }

    /**
     * Cập nhật thời gian lưu trú và thành tiền khi trả phòng
     */
    public boolean updateLuuTruVaTien(String maCTHD, double thoiGianLuuTru, double thanhTien) {
        String sql = "UPDATE ChiTietHoaDon SET thoiGianLuuTru = ?, thanhTien = ? WHERE maCTHD = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, thoiGianLuuTru);
            ps.setDouble(2, thanhTien);
            ps.setString(3, maCTHD);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách phòng đã trả trong 1 hóa đơn.
     * Dùng cho popup chi tiết HoaDonView.
     * Trả về list Object[] = {maPhong, tenPhong, tenLoaiPhong, thoiGianLuuTru, thanhTien, giaCoc}
     */
    public List<Object[]> getDanhSachPhongDaTra(String maHD) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT p.maPhong, p.tenPhong, p.loaiPhong, " +
            "       cthd.thoiGianLuuTru, cthd.thanhTien, ctdp.giaCoc, ctdp.maCTDP " +
            "FROM ChiTietHoaDon cthd " +
            "JOIN ChiTietDatPhong ctdp ON cthd.maCTDP = ctdp.maCTDP " +
            "JOIN Phong p ON ctdp.maPhong = p.maPhong " +
            "WHERE cthd.maHD = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maLoai = rs.getString("loaiPhong");
                String displayLoai = maLoai;
                try {
                    displayLoai = model.enums.TenLoaiPhong.valueOf(maLoai).getDisplayName();
                } catch (Exception ignored) {}

                list.add(new Object[]{
                    rs.getString("maPhong"),
                    rs.getString("tenPhong"),
                    displayLoai,
                    rs.getDouble("thoiGianLuuTru"),
                    rs.getDouble("thanhTien"),
                    rs.getDouble("giaCoc"),
                    rs.getString("maCTDP")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tính tổng tiền cọc đã trừ từ các phòng trong hóa đơn.
     */
    public double getTongCocByMaHD(String maHD) {
        String sql =
            "SELECT SUM(ctdp.giaCoc) FROM ChiTietHoaDon cthd " +
            "JOIN ChiTietDatPhong ctdp ON cthd.maCTDP = ctdp.maCTDP " +
            "WHERE cthd.maHD = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}

