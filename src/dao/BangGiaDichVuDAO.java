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
     * Lấy danh sách tất cả bảng giá từ bảng BangGiaDV_Header
     */
    public List<BangGiaDichVu> getAllBangGia() {
        List<BangGiaDichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaDV_Header ORDER BY ngayApDung DESC";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBangGia(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy chi tiết của một bảng giá từ bảng BangGiaDV_Detail
     */
    public List<BangGiaDichVu_ChiTiet> getChiTietByMa(String maBG) {
        List<BangGiaDichVu_ChiTiet> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaDV_Detail WHERE maBangGia = ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBG);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapChiTiet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới bảng giá thông tin
     */
    public boolean insertBangGia(BangGiaDichVu bg) {
        String sql = "INSERT INTO BangGiaDV_Header (maBangGia, tenBangGia, ngayApDung, ngayHetHieuLuc, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bg.getMaBangGia());
            ps.setString(2, bg.getTenBangGia());
            ps.setDate(3, new java.sql.Date(bg.getNgayApDung().getTime()));
            ps.setDate(4, new java.sql.Date(bg.getNgayHetHieuLuc().getTime()));
            ps.setInt(5, bg.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm chi tiết bảng giá
     */
    public boolean insertChiTiet(BangGiaDichVu_ChiTiet ct) {
        String sql = "INSERT INTO BangGiaDV_Detail (maBangGia, maDV, giaDV) VALUES (?, ?, ?)";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ct.getMaBangGia().getMaBangGia());
            ps.setString(2, ct.getMaDichVu().getMaDV());
            ps.setDouble(3, ct.getGiaDichVu());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa bảng giá (Transaction: Xóa chi tiết trước)
     */
    public boolean deleteBangGia(String maBG) {
        String sqlChiTiet = "DELETE FROM BangGiaDV_Detail WHERE maBangGia = ?";
        String sqlThongTin = "DELETE FROM BangGiaDV_Header WHERE maBangGia = ?";
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Map dữ liệu từ ResultSet sang Object BangGiaDichVu
     */
    private BangGiaDichVu mapBangGia(ResultSet rs) throws SQLException {
        BangGiaDichVu bg = new BangGiaDichVu();
        bg.setMaBangGia(rs.getString("maBangGia"));
        bg.setTenBangGia(rs.getString("tenBangGia"));
        bg.setNgayApDung(rs.getDate("ngayApDung"));
        bg.setNgayHetHieuLuc(rs.getDate("ngayHetHieuLuc"));
        bg.setTrangThai(rs.getInt("trangThai"));
        return bg;
    }

    /**
     * Map dữ liệu từ ResultSet sang Object BangGiaDichVu_ChiTiet
     */
    private BangGiaDichVu_ChiTiet mapChiTiet(ResultSet rs) throws SQLException {
        BangGiaDichVu_ChiTiet ct = new BangGiaDichVu_ChiTiet();
        ct.setMaBangGia(new BangGiaDichVu(rs.getString("maBangGia")));
        ct.setMaDichVu(new DichVu(rs.getString("maDV")));
        ct.setGiaDichVu(rs.getDouble("giaDV"));
        return ct;
    }

    /**
     * Lưu toàn bộ bảng giá và chi tiết dịch vụ đi kèm (FIX: đóng connection đúng
     * cách)
     */
    public boolean insertFullBangGia(BangGiaDichVu thongTin, List<BangGiaDichVu_ChiTiet> dsChiTiet) {
        try (Connection con = ConnectDatabase.getInstance().getConnection()) {
            con.setAutoCommit(false);

            String sqlThongTin = "INSERT INTO BangGiaDV_Header (maBangGia, tenBangGia, ngayApDung, ngayHetHieuLuc, trangThai) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = con.prepareStatement(sqlThongTin)) {
                ps1.setString(1, thongTin.getMaBangGia());
                ps1.setString(2, thongTin.getTenBangGia());
                ps1.setDate(3, new java.sql.Date(thongTin.getNgayApDung().getTime()));
                ps1.setDate(4, new java.sql.Date(thongTin.getNgayHetHieuLuc().getTime()));
                ps1.setInt(5, thongTin.getTrangThai());
                ps1.executeUpdate();
            }

            String sqlChiTiet = "INSERT INTO BangGiaDV_Detail (maBangGia, maDV, giaDV) VALUES (?, ?, ?)";
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật toàn bộ bảng giá và chi tiết dịch vụ đi kèm
     * FIX: thêm trangThai vào UPDATE + đóng connection đúng cách
     */
    public boolean updateFullBangGia(BangGiaDichVu bg, List<BangGiaDichVu_ChiTiet> dsChiTiet) {
        try (Connection con = ConnectDatabase.getInstance().getConnection()) {
            con.setAutoCommit(false);

            String sqlUpdateBG = "UPDATE BangGiaDV_Header SET tenBangGia = ?, ngayApDung = ?, ngayHetHieuLuc = ?, trangThai = ? WHERE maBangGia = ?";
            try (PreparedStatement ps1 = con.prepareStatement(sqlUpdateBG)) {
                ps1.setString(1, bg.getTenBangGia());
                ps1.setDate(2, new java.sql.Date(bg.getNgayApDung().getTime()));
                ps1.setDate(3, new java.sql.Date(bg.getNgayHetHieuLuc().getTime()));
                ps1.setInt(4, bg.getTrangThai());
                ps1.setString(5, bg.getMaBangGia());
                ps1.executeUpdate();
            }

            String sqlDeleteDetails = "DELETE FROM BangGiaDV_Detail WHERE maBangGia = ?";
            try (PreparedStatement ps2 = con.prepareStatement(sqlDeleteDetails)) {
                ps2.setString(1, bg.getMaBangGia());
                ps2.executeUpdate();
            }

            String sqlInsertDetails = "INSERT INTO BangGiaDV_Detail (maBangGia, maDV, giaDV) VALUES (?, ?, ?)";
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy bảng giá theo mã
     */
    public BangGiaDichVu getBangGiaByMa(String maBG) {
        String sql = "SELECT * FROM BangGiaDV_Header WHERE maBangGia = ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBG);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapBangGia(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật trạng thái bảng giá (bật/tắt)
     */
    public boolean updateTrangThai(String maBangGia, int trangThai) {
        String sql = "UPDATE BangGiaDV_Header SET trangThai = ? WHERE maBangGia = ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trangThai);
            ps.setString(2, maBangGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tìm các bảng giá bị xung đột thời gian (gối đầu) với một khoảng [ngayAD,
     * ngayHH]
     * Loại trừ bảng giá chính mình (theo maBG, dùng khi sửa)
     */
    public List<BangGiaDichVu> findConflicts(java.sql.Date ngayAD, java.sql.Date ngayHH, String excludeMaBG) {
        List<BangGiaDichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaDV_Header WHERE trangThai = 0 AND ngayApDung <= ? AND ngayHetHieuLuc >= ?";
        if (excludeMaBG != null && !excludeMaBG.isEmpty()) {
            sql += " AND maBangGia <> ?";
        }
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, ngayHH);
            ps.setDate(2, ngayAD);
            if (excludeMaBG != null && !excludeMaBG.isEmpty()) {
                ps.setString(3, excludeMaBG);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBangGia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Sinh mã bảng giá tiếp theo (BG0001, BG0002, ...)
     */
    public String generateNextMaBangGia() {
        // Cải thiện SQL để tìm mã lớn nhất dựa trên độ dài và giá trị thực tế (tránh lỗi BG0010 < BG0002)
        String sql = "SELECT TOP 1 maBangGia FROM BangGiaDV_Header WHERE maBangGia LIKE 'BG%' ORDER BY LEN(maBangGia) DESC, maBangGia DESC";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String last = rs.getString("maBangGia");
                String numPart = last.replaceAll("[^0-9]", "");
                try {
                    int num = numPart.isEmpty() ? 0 : Integer.parseInt(numPart);
                    num++;
                    return String.format("BG%04d", num);
                } catch (NumberFormatException e) {
                    // fallback
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "BG0001";
    }

    /**
     * Soft delete: set trangThai = 0 thay vì xóa vật lý
     */
    public boolean softDeleteBangGia(String maBG) {
        // Sửa: Dùng trạng thái 1 (Ngưng áp dụng) cho soft delete vì DB là kiểu BIT
        return updateTrangThai(maBG, 1);
    }

    /**
     * Kiểm tra mã bảng giá đã tồn tại chưa
     */
    public boolean exists(String maBG) {
        // Sử dụng UPPER và TRIM để đối soát tuyệt đối chính xác trong mọi trường hợp collation/padding
        String sql = "SELECT COUNT(*) FROM BangGiaDV_Header WHERE UPPER(LTRIM(RTRIM(maBangGia))) = UPPER(?)";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBG.trim());
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
     * Kiểm tra xung đột thời gian khi thêm mới bảng giá
     */
    public boolean checkOverlap(java.sql.Date ngayAD, java.sql.Date ngayHH) {
        String sql = "SELECT COUNT(*) FROM BangGiaDV_Header WHERE trangThai = 0 AND ngayApDung <= ? AND ngayHetHieuLuc >= ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, ngayHH);
            ps.setDate(2, ngayAD);
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
     * Kiểm tra xung đột thời gian khi cập nhật bảng giá (loại trừ chính nó)
     */
    public boolean checkOverlapUpdate(String maBG, java.sql.Date ngayAD, java.sql.Date ngayHH) {
        String sql = "SELECT COUNT(*) FROM BangGiaDV_Header WHERE trangThai = 0 AND ngayApDung <= ? AND ngayHetHieuLuc >= ? AND maBangGia <> ?";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, ngayHH);
            ps.setDate(2, ngayAD);
            ps.setString(3, maBG);
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
     * Tìm các bảng giá xung đột thời gian NGHIÊM NGẶT (trangThai != -1, tức chưa
     * xóa mềm).
     * Dùng cho logic chặn cứng: không cho phép tồn tại 2 bảng giá chồng lấn thời
     * gian.
     * 
     * @param excludeMaBG mã bảng giá cần loại trừ (khi sửa), null nếu thêm mới
     */
    public List<BangGiaDichVu> findStrictConflicts(java.sql.Date ngayAD, java.sql.Date ngayHH, String excludeMaBG) {
        List<BangGiaDichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaDV_Header WHERE trangThai = 0 AND ngayApDung <= ? AND ngayHetHieuLuc >= ?";
        if (excludeMaBG != null && !excludeMaBG.isEmpty()) {
            sql += " AND maBangGia <> ?";
        }
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, ngayHH);
            ps.setDate(2, ngayAD);
            if (excludeMaBG != null && !excludeMaBG.isEmpty()) {
                ps.setString(3, excludeMaBG);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBangGia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy giá dịch vụ từ bảng giá đang hiệu lực (active) tại thời điểm hiện tại.
     * Trả về Map<maDV, giaDV>. Nếu không có bảng giá active, trả map rỗng.
     */
    public java.util.Map<String, Double> getActivePriceMap() {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        // Tìm bảng giá có trangThai = 0 (Đang áp dụng) và hôm nay nằm trong [ngayApDung, ngayHetHieuLuc]
        String sql = "SELECT d.maDV, d.giaDV FROM BangGiaDV_Detail d " +
                "INNER JOIN BangGiaDV_Header h ON d.maBangGia = h.maBangGia " +
                "WHERE h.trangThai = 0 AND CAST(GETDATE() AS DATE) BETWEEN h.ngayApDung AND h.ngayHetHieuLuc";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("maDV"), rs.getDouble("giaDV"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Cập nhật trạng thái bảng giá dựa trên tên trạng thái từ ComboBox UI
     * 
     * @param maBG         Mã bảng giá cần cập nhật
     * @param tenTrangThai "Đang áp dụng", "Chờ áp dụng", hoặc "Ngưng áp dụng"
     * @return true nếu cập nhật thành công
     */
    public boolean updateTrangThaiTheoTen(String maBG, String tenTrangThai) {
        // Ánh xạ tên trạng thái sang giá trị số trong Database
        // Dựa trên logic của bạn: trangThai = 1 là Ngưng áp dụng (hoặc hết hạn), 0 là
        // Hoạt động
        int trangThaiInt = 0;

        if (tenTrangThai.equals("Ngưng áp dụng")) {
            trangThaiInt = 1;
        } else if (tenTrangThai.equals("Đang áp dụng") || tenTrangThai.equals("Chờ áp dụng")) {
            trangThaiInt = 0;
        }

        String sql = "UPDATE BangGiaDV_Header SET trangThai = ? WHERE maBangGia = ?";

        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, trangThaiInt);
            ps.setString(2, maBG);

            int rowAffected = ps.executeUpdate();

            // Nếu chuyển sang "Ngưng áp dụng", có thể bạn muốn cập nhật luôn ngày hết hạn
            // là hôm nay
            if (rowAffected > 0 && tenTrangThai.equals("Ngưng áp dụng")) {
                updateNgayHetHanKhiDung(maBG);
            }

            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hỗ trợ cập nhật ngày hết hạn về thời điểm hiện tại khi người dùng chọn "Ngưng
     * áp dụng" thủ công
     */
    private void updateNgayHetHanKhiDung(String maBG) {
        String sql = "UPDATE BangGiaDV_Header SET ngayHetHieuLuc = GETDATE() WHERE maBangGia = ? AND ngayHetHieuLuc > GETDATE()";
        try (Connection conn = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBG);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
