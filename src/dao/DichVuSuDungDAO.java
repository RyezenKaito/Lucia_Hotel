package dao;

import java.util.ArrayList;
import java.util.List;
import model.entities.DichVuSuDung;

public class DichVuSuDungDAO {
    // Boilerplate DAO cho bảng vừa mới tạo
    /**
     * Lấy danh sách dịch vụ sử dụng theo mã hóa đơn
     */
    public List<DichVuSuDung> findByMaHD(String maHD) {
        List<DichVuSuDung> ds = new ArrayList<>();
        String sql = "SELECT dv.*, d.tenDV, d.maDV, dv.giaDV, dv.soLuong, dv.ngaySuDung " +
                "FROM DichVuSuDung dv " +
                "JOIN DichVu d ON dv.maDV = d.maDV " +
                "JOIN ChiTietHoaDon cthd ON dv.maCTHD = cthd.maCTHD " +
                "WHERE cthd.maHD = ?";

        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.entities.DichVu d = new model.entities.DichVu();
                d.setMaDV(rs.getString("maDV"));
                d.setTenDV(rs.getString("tenDV"));
                d.setGia(rs.getDouble("giaDV"));

                DichVuSuDung dvsd = new DichVuSuDung();
                dvsd.setDichVu(d);
                dvsd.setGiaDV(rs.getDouble("giaDV"));
                dvsd.setSoLuong(rs.getInt("soLuong"));
                dvsd.setNgaySuDung(rs.getTimestamp("ngaySuDung") != null
                        ? rs.getTimestamp("ngaySuDung").toLocalDateTime().toLocalDate()
                        : null);
                ds.add(dvsd);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
