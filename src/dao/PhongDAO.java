package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connectDatabase.ConnectDatabase;
import model.entities.LoaiPhong;
import model.entities.Phong;
import model.enums.TrangThaiPhong;

public class PhongDAO {

    /**
     * Chuyển đổi từ text (DB) sang Enum
     */
    private TrangThaiPhong findEnumByString(String text) {
        if (text == null)
            return TrangThaiPhong.CONTRONG;
        text = text.trim().toUpperCase();
        if (text.equals("CONTRONG"))
            return TrangThaiPhong.CONTRONG;
        if (text.equals("DANGSUDUNG"))
            return TrangThaiPhong.DACOKHACH;
        if (text.equals("BAN"))
            return TrangThaiPhong.BAN;
        return TrangThaiPhong.CONTRONG;
    }

    /**
     * Chuyển đổi từ Enum sang text (DB)
     */
    private String trangThaiToString(TrangThaiPhong ttp) {
        if (ttp == TrangThaiPhong.CONTRONG)
            return "CONTRONG";
        if (ttp == TrangThaiPhong.DACOKHACH)
            return "DANGSUDUNG";
        return "BAN";
    }

    public List<Phong> getAll() {
        List<Phong> ds = new ArrayList<>();
        String sql = "SELECT p.*, l.gia, l.sucChua FROM Phong p " +
                "JOIN LoaiPhong l ON p.loaiPhong = l.maLoaiPhong " +
                "ORDER BY p.soTang, p.soPhong";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    private Phong mapRow(ResultSet rs) throws SQLException {
        LoaiPhong lp = new LoaiPhong();
        lp.setMaLoaiPhong(rs.getString("loaiPhong"));
        lp.setGia(rs.getDouble("gia"));
        lp.setSucChua(rs.getInt("sucChua"));

        Phong p = new Phong();
        p.setMaPhong(rs.getString("maPhong"));
        p.setTenPhong(rs.getString("tenPhong"));
        p.setLoaiPhong(lp);
        p.setTinhTrang(findEnumByString(rs.getString("tinhTrang")));
        p.setSoPhong(rs.getInt("soPhong"));
        p.setSoTang(rs.getInt("soTang"));
        return p;
    }

    public boolean insert(Phong p) {
        String sql = "INSERT INTO Phong(maPhong, tenPhong, loaiPhong, tinhTrang, soPhong, soTang) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getMaPhong());
            pstmt.setString(2, p.getTenPhong());
            pstmt.setString(3, p.getLoaiPhong().getMaLoaiPhong());
            pstmt.setString(4, trangThaiToString(p.getTinhTrang()));
            pstmt.setInt(5, p.getSoPhong());
            pstmt.setInt(6, p.getSoTang());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Phong p) {
        String sql = "UPDATE Phong SET tenPhong=?, loaiPhong=?, tinhTrang=?, soPhong=?, soTang=? WHERE maPhong=?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, p.getTenPhong());
            pstmt.setString(2, p.getLoaiPhong().getMaLoaiPhong());
            pstmt.setString(3, trangThaiToString(p.getTinhTrang()));
            pstmt.setInt(4, p.getSoPhong());
            pstmt.setInt(5, p.getSoTang());
            pstmt.setString(6, p.getMaPhong());
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
            pstmt.setString(1, maPhong);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * [ĐÃ SỬA] Lấy danh sách phòng trống theo loại và khoảng thời gian.
     * Loại trừ các phòng đã có đơn đặt trùng ngày ở các trạng thái ACTIVE
     * (CHO_XACNHAN, DA_XACNHAN, DA_CHECKIN). Bỏ qua DA_HUY và DA_CHECKOUT
     * vì các đơn này đã kết thúc -> phòng có thể được đặt lại.
     */
    public List<Phong> getDanhSachPhongTrong(String maLoaiPhong, java.sql.Date dIn, java.sql.Date dOut) {
        List<Phong> ds = new ArrayList<>();
        String sql = "SELECT p.*, l.gia, l.sucChua FROM Phong p " +
                "JOIN LoaiPhong l ON p.loaiPhong = l.maLoaiPhong " +
                "WHERE l.maLoaiPhong = ? " +
                "  AND p.tinhTrang <> N'BAN' " +   // Phòng đang bảo trì/cấm bán thì không hiện
                "  AND p.maPhong NOT IN (" +
                "    SELECT ctdp.maPhong FROM ChiTietDatPhong ctdp " +
                "    JOIN DatPhong dp ON ctdp.maDat = dp.maDat " +
                "    WHERE dp.ngayCheckIn < ? AND dp.ngayCheckOut > ? " +
                "      AND dp.trangThai NOT IN (N'DA_CHECKOUT', N'DA_HUY')" +
                "  )";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, maLoaiPhong);
            pstmt.setDate(2, dOut);
            pstmt.setDate(3, dIn);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Cập nhật trạng thái phòng (dùng cho nghiệp vụ Check-in / Check-out)
     */
    public boolean updateTrangThai(String maPhong, String trangThai) {
        String sql = "UPDATE Phong SET tinhTrang = ? WHERE maPhong = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, trangThai);
            pstmt.setString(2, maPhong);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTrangThaiWithCon(Connection con, String maPhong, String trangThai) {
        String sql = "UPDATE Phong SET tinhTrang = ? WHERE maPhong = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, trangThai);
            pstmt.setString(2, maPhong);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách phòng trống theo mã loại phòng
     * Trả về List chuỗi "maPhong - tenPhong" dùng cho ComboBox
     */
    public List<String> getPhongTrongByLoai(String maLoaiPhong) {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT maPhong, tenPhong FROM Phong WHERE loaiPhong = ? AND tinhTrang = N'CONTRONG'";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLoaiPhong);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                ds.add(rs.getString("maPhong") + " - " + rs.getString("tenPhong"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * [ĐÃ SỬA] Lấy tất cả Phong trống theo NHIỀU loại và khoảng thời gian check-in/out.
     * QUY TẮC LỌC:
     *   1) Phòng KHÔNG bị "BAN" (bảo trì).
     *   2) Phòng KHÔNG nằm trong đơn đặt đang hoạt động có khoảng thời gian trùng lặp.
     *      Đơn "hoạt động" = CHO_XACNHAN, DA_XACNHAN, DA_CHECKIN.
     *      Đơn "không hoạt động" (DA_CHECKOUT, DA_HUY) -> được phép đặt lại.
     *   3) Điều kiện trùng lặp: (existing.checkIn < newCheckOut) AND (existing.checkOut > newCheckIn)
     *      => Đảm bảo: chỉ được đặt SAU ngày trả của đơn cũ.
     *
     * Chú ý: KHÔNG filter theo tinhTrang = 'CONTRONG' nữa vì một phòng đang có khách
     * checkin hôm nay (tinhTrang='DANGSUDUNG') nhưng ngày trả là mai — hoàn toàn có thể
     * được đặt cho ngày kia. Chỉ cần khoảng ngày không chồng lấn.
     */
    public List<Phong> getPhongTrongByMultiLoai(List<String> maLoaiPhongs,
            java.time.LocalDate checkIn,
            java.time.LocalDate checkOut) {
        List<Phong> ds = new ArrayList<>();
        if (maLoaiPhongs == null || maLoaiPhongs.isEmpty())
            return ds;

        String placeholders = String.join(",", maLoaiPhongs.stream()
                .map(x -> "?").collect(java.util.stream.Collectors.toList()));

        String sql = "SELECT p.*, l.gia, l.sucChua FROM Phong p " +
                "JOIN LoaiPhong l ON p.loaiPhong = l.maLoaiPhong " +
                "WHERE p.loaiPhong IN (" + placeholders + ") " +
                "  AND p.tinhTrang <> N'BAN' " +
                "  AND p.maPhong NOT IN (" +
                "    SELECT ctdp.maPhong FROM ChiTietDatPhong ctdp " +
                "    JOIN DatPhong dp ON ctdp.maDat = dp.maDat " +
                "    WHERE dp.ngayCheckIn < ? AND dp.ngayCheckOut > ? " +
                "      AND dp.trangThai NOT IN (N'DA_CHECKOUT', N'DA_HUY')" +
                "  ) " +
                "ORDER BY p.loaiPhong, p.maPhong";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            for (String loai : maLoaiPhongs)
                ps.setString(idx++, loai);
            ps.setDate(idx++, java.sql.Date.valueOf(checkOut));
            ps.setDate(idx, java.sql.Date.valueOf(checkIn));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
}