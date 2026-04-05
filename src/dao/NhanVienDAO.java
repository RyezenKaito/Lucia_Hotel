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
        NhanVien nv = new NhanVien(
                rs.getString("maNV"),
                rs.getString("hoTen"),
                rs.getString("diaChi"),
                parseTrinhDo(rs.getString("trinhDo")),
                rs.getDate("ngayVaoLam") != null ? rs.getDate("ngayVaoLam").toLocalDate() : null,
                parseVaiTro(rs.getString("role")));

        nv.setSoDT(rs.getString("soDT"));
        nv.setMaQL(rs.getString("maQL"));
        nv.setMatKhau(rs.getString("mk"));

        // SDT, CCCD (nullable từ DB)
        String cccdVal = rs.getString("soCCCD");
        if (cccdVal != null && !cccdVal.isBlank()) {
            try {
                nv.setCccd(cccdVal.trim());
            } catch (Exception ignored) {
            }
        }

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
            case "ADMIN":
                return ChucVu.ADMIN;
            case "NV":
            case "NHANVIEN":
            case "NHAN_VIEN":
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
        switch (cv) {
            case QUAN_LY:
                return "QL";
            case ADMIN:
                return "ADMIN";
            default:
                return "NV";
        }
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

    // ── TÌM KIẾM THEO TÊN, MÃ, SĐT ──────────────────────────────────────────
    public List<NhanVien> findByKeyword(String keyword) {
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT * FROM NV WHERE maNV LIKE ? OR hoTen LIKE ? OR soDT LIKE ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String searchStr = "%" + keyword + "%";
            ps.setString(1, searchStr);
            ps.setString(2, searchStr);
            ps.setString(3, searchStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(mapRow(rs));
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
                + "(maNV, hoTen, soDT, ngayVaoLam, mk, role, diaChi, trinhDo, maQL, ngaySinh, soCCCD) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getHoTen());
            ps.setString(3, nv.getSoDT());
            ps.setDate(4, nv.getNgayVaoLamDate() != null
                    ? Date.valueOf(nv.getNgayVaoLamDate())
                    : Date.valueOf(java.time.LocalDate.now()));
            ps.setString(5, nv.getMatKhau() != null ? nv.getMatKhau() : "123");
            ps.setString(6, toVaiTroString(nv.getRole()));
            ps.setString(7, nv.getDiaChi());
            ps.setString(8, nv.getTrinhDo() != null ? nv.getTrinhDo().name() : "THCS");
            ps.setString(9, nv.getMaQL());
            ps.setDate(10, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(11, nv.getCccd());

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
                + "role=?, maQL=?, diaChi=?, ngaySinh=?, soCCCD=? "
                + "WHERE maNV=?";

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getSoDT());
            ps.setDate(3, nv.getNgayVaoLamDate() != null ? Date.valueOf(nv.getNgayVaoLamDate()) : null);
            ps.setString(4, toVaiTroString(nv.getRole()));
            ps.setString(5, nv.getMaQL());
            ps.setString(6, nv.getDiaChi());
            ps.setDate(7, nv.getNgaySinh() != null ? Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(8, nv.getCccd());
            ps.setString(9, nv.getMaNV());

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
        String sql = "SELECT maNV FROM NV WHERE maNV LIKE 'LUCIA%'";
        int max = 0;
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String last = rs.getString(1);
                if (last != null && last.length() > 5) {
                    try {
                        int num = Integer.parseInt(last.substring(5));
                        if(num > max) max = num;
                    } catch(Exception ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("LUCIA%03d", max + 1);
    }
}