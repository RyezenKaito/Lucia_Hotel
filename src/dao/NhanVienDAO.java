package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.enums.trinhDo;

public class NhanVienDAO {

    // ── Helper: map ResultSet → NhanVien ─────────────────────────────────────
    private NhanVien mapRow(ResultSet rs) throws Exception {
        // 1. Khởi tạo bằng constructor
        NhanVien nv = new NhanVien(
                rs.getString("maNV"),
                rs.getString("hoTen"),
                rs.getString("diaChi"),
                parseTrinhDo(rs.getString("trinhDo")),
                rs.getDate("ngayVaoLam") != null ? rs.getDate("ngayVaoLam").toLocalDate() : null,
                rs.getFloat("heSoLuong"),
                rs.getInt("luongCB"),
                parseVaiTro(rs.getString("role")));

        // 2. Gán các trường bổ sung để hiển thị đầy đủ trên Table và Form
        nv.setSoDT(rs.getString("soDT"));
        nv.setMaQL(rs.getString("maQL"));
        nv.setMatKhau(rs.getString("mk"));

        // 3. Gán Ngày sinh (Mới thêm)
        if (rs.getDate("ngaySinh") != null) {
            nv.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
        }

        return nv;
    }

    private ChucVu parseVaiTro(String value) {
        if (value == null)
            return ChucVu.NHAN_VIEN;
        switch (value.trim().toUpperCase()) {
            case "QL":
            case "QUANLY":
            case "QUAN_LY":
                return ChucVu.QUAN_LY;
            case "NV":
            case "NHANVIEN":
            case "NHAN_VIEN":
                return ChucVu.NHAN_VIEN;
            default:
                return ChucVu.NHAN_VIEN;
        }
    }

    private trinhDo parseTrinhDo(String value) {
        if (value == null)
            return trinhDo.THCS;
        try {
            return trinhDo.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return trinhDo.THCS;
        }
    }

    private String toVaiTroString(ChucVu cv) {
        if (cv == null)
            return "NV";
        return cv == ChucVu.QUAN_LY ? "QL" : "NV";
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────
    public List<NhanVien> getAll() {
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT * FROM NV";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                ds.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    // ── TÌM KIẾM THEO TÊN, MÃ, SĐT (HÀM BẠN YÊU CẦU) ──────────────────────────
    public List<NhanVien> findByKeyword(String keyword) {
        List<NhanVien> ds = new ArrayList<>();
        // Sử dụng LIKE với % để tìm kiếm tương đối
        String sql = "SELECT * FROM NV WHERE maNV LIKE ? OR hoTen LIKE ? OR soDT LIKE ?";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            String searchStr = "%" + keyword + "%";
            ps.setString(1, searchStr);
            ps.setString(2, searchStr);
            ps.setString(3, searchStr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    // ── READ BY ID ────────────────────────────────────────────────────────────
    public NhanVien getById(String maNV) {
        String sql = "SELECT * FROM NV WHERE maNV = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── INSERT ────────────────────────────────────────────────────────────────
    public boolean insert(NhanVien nv) {
        String sql = "INSERT INTO NV "
                + "(maNV, hoTen, soDT, ngayVaoLam, heSoLuong, mk, role, diaChi, trinhDo, luongCB, maQL, ngaySinh) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getHoTen());
            ps.setString(3, nv.getSoDT());
            ps.setDate(4, nv.getNgayVaoLamDate() != null ? Date.valueOf(nv.getNgayVaoLamDate())
                    : Date.valueOf(java.time.LocalDate.now()));
            ps.setFloat(5, nv.getHeSoLuong());
            ps.setString(6, nv.getMatKhau() != null ? nv.getMatKhau() : "123");
            ps.setString(7, toVaiTroString(nv.getRole()));
            ps.setString(8, nv.getDiaChi());
            ps.setString(9, nv.getTrinhDo() != null ? nv.getTrinhDo().name() : "THCS");
            ps.setInt(10, nv.getLuongCB());
            ps.setString(11, nv.getMaQL());
            ps.setDate(12, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public boolean update(NhanVien nv) {
        String sql = "UPDATE NV SET "
                + "hoTen=?, soDT=?, ngayVaoLam=?, "
                + "heSoLuong=?, role=?, maQL=?, diaChi=?, luongCB=?, ngaySinh=? "
                + "WHERE maNV=?";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getSoDT());
            ps.setDate(3, nv.getNgayVaoLamDate() != null ? Date.valueOf(nv.getNgayVaoLamDate()) : null);
            ps.setFloat(4, nv.getHeSoLuong());
            ps.setString(5, toVaiTroString(nv.getRole()));
            ps.setString(6, nv.getMaQL());
            ps.setString(7, nv.getDiaChi());
            ps.setInt(8, nv.getLuongCB());
            ps.setDate(9, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(10, nv.getMaNV());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public boolean delete(String maNV) {
        String sql = "DELETE FROM NV WHERE maNV=?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Count Manager
    public int countManager() {
        String sql = "SELECT COUNT(*) FROM NV WHERE role IN ('QUANLY', 'QL', 'QUAN_LY')";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next())
                return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── AUTHENTICATE ──────────────────────────────────────────────────────────
    public boolean authenticate(String staffID, String password) {
        String sql = "SELECT mk FROM NV WHERE maNV=?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString("mk").equals(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── GENERATE ID ───────────────────────────────────────────────────────────
    public String generateMaNV() {
        String prefix = "LUCIA";
        String sql = "SELECT MAX(maNV) FROM NV";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String last = rs.getString(1);
                if (last != null && last.startsWith(prefix)) {
                    int num = Integer.parseInt(last.replace(prefix, ""));
                    return prefix + String.format("%04d", num + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefix + "0001";
    }
}