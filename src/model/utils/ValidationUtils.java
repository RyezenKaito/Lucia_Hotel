package model.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Lớp tiện ích chứa toàn bộ Regex và logic kiểm tra dữ liệu đầu vào.
 * Tất cả đều là static để có thể gọi trực tiếp từ mọi class khác.
 */
public final class ValidationUtils {

    // Chống khởi tạo class này (vì đây là Utility Class)
    private ValidationUtils() {
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * 1. CÁC HẰNG SỐ REGEX (Dùng cho hàm .matches())
     * ══════════════════════════════════════════════════════════════════
     */

    /**
     * * HỌ VÀ TÊN:
     * - Chỉ cho phép chữ cái (hỗ trợ 100% tiếng Việt), khoảng trắng, nháy đơn, gạch
     * nối.
     */
    public static final String REGEX_NAME = "^[\\p{L}\\s\\'-]+$";

    /**
     * CHỐNG LẶP KÝ TỰ BẤT THƯỜNG:
     * - Chặn chuỗi có 3 ký tự giống nhau liên tiếp (VD: "Vănnn", "Hooo").
     */
    public static final String REGEX_SPAM_CHAR = ".*(.)\\1{2,}.*";

    /**
     * SỐ ĐIỆN THOẠI DI ĐỘNG & MÁY BÀN (Chuẩn Việt Nam mới nhất):
     * - Di động (10 số): 03x, 05x, 07x, 08x, 09x
     */
    public static final String REGEX_PHONE_VN = "^(03|05|07|08|09)\\d{8}$";

    /**
     * CĂN CƯỚC CÔNG DÂN (CCCD):
     * - Đúng 12 chữ số.
     */
    public static final String REGEX_CCCD_FORMAT = "^\\d{12}$";

    /*
     * ══════════════════════════════════════════════════════════════════
     * 2. CÁC HÀM KIỂM TRA LOGIC NHANH
     * ══════════════════════════════════════════════════════════════════
     */

    /**
     * Kiểm tra độ dài Họ và Tên (Tối thiểu 2 từ, Họ >= 1, Tên >= 2 ký tự)
     */
    public static boolean isValidNameLength(String ten) {
        if (ten == null || ten.trim().isEmpty())
            return false;
        String[] parts = ten.trim().split("\\s+");
        if (parts.length < 2)
            return false;
        return parts[0].length() >= 1 && parts[parts.length - 1].length() >= 1;
    }

    /**
     * Kiểm tra mã tỉnh từ 001 - 096
     * không có 009,013,016, 018, 021, 024, 028, 029, 032, 039, 041, 043, 047, 050,
     * 053,
     * 055, 057, 059, 061, 063, 065, 069, 071, 073, 076, 078, 081, 085, 088, 090.
     */
    private static final Set<String> PROVINCE_CODES = Set.of(
            "001", "002", "004", "006", "008", "010", "011", "012", "014", "015",
            "017", "019", "020", "022", "024", "025", "026", "027", "030", "031",
            "033", "034", "035", "036", "037", "038", "040", "042", "044", "045",
            "046", "048", "049", "051", "052", "054", "056", "058", "060", "062",
            "064", "066", "067", "068", "070", "072", "074", "075", "077", "079",
            "080", "082", "083", "084", "086", "087", "089", "091", "092", "093",
            "094", "095", "096");

    // ── [ĐÃ SỬA Ở ĐÂY] ──────────────────────────────────────────────
    public static boolean isValidProvinceCode(String cccd) {
        // Dùng luôn hằng số REGEX_CCCD_FORMAT để đảm bảo chuỗi không null và đủ 12 số
        // Tránh lỗi StringIndexOutOfBoundsException khi dùng substring()
        if (cccd == null || !cccd.matches(REGEX_CCCD_FORMAT)) {
            return false;
        }
        return PROVINCE_CODES.contains(cccd.substring(0, 3));
    }
    // ────────────────────────────────────────────────────────────────

    /**
     * Kiểm tra Mã thế kỷ và Giới tính (Số thứ 4 của CCCD) so với Năm sinh thực tế.
     */
    public static boolean isValidCCCDCenturyAndGender(String cccd, int namSinh) {
        if (cccd == null || cccd.length() < 4)
            return false;

        int theKyGioiTinh = cccd.charAt(3) - '0';

        // Quy định mã thế kỷ & giới tính của Bộ Công An (Nam chẵn, Nữ lẻ)
        if (namSinh >= 1900 && namSinh <= 1999)
            return (theKyGioiTinh == 0 || theKyGioiTinh == 1);
        if (namSinh >= 2000 && namSinh <= 2099)
            return (theKyGioiTinh == 2 || theKyGioiTinh == 3);
        return false;
    }

    /**
     * Kiểm tra 2 số năm sinh (Số thứ 5 và 6 của CCCD) so với Năm sinh thực tế.
     */
    public static boolean isValidCCCDBirthYear(String cccd, int namSinh) {
        if (cccd == null || cccd.length() < 6)
            return false;

        String maNamSinh = cccd.substring(4, 6);
        String maNamSinhThucTe = String.format("%02d", namSinh % 100);

        return maNamSinh.equals(maNamSinhThucTe);
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * 3. HÀM CHUẨN HÓA DỮ LIỆU
     * ══════════════════════════════════════════════════════════════════
     */

    /**
     * Chuẩn hóa chuỗi Tên (Viết hoa chữ cái đầu mỗi từ, xóa khoảng trắng thừa).
     */
    public static String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty())
            return "";

        // Xóa khoảng trắng thừa ở 2 đầu và giữa các chữ
        input = input.trim().replaceAll("\\s+", " ");
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;

        for (char ch : input.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                nextUpper = true;
                sb.append(ch);
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(ch));
                nextUpper = false;
            } else {
                sb.append(Character.toLowerCase(ch));
            }
        }
        return sb.toString();
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * 4. HÀM TIỆN ÍCH GIAO DIỆN (UI UTILITIES)
     * ══════════════════════════════════════════════════════════════════
     */

    /**
     * Gắn bộ lọc vào TextField để CHỈ CHO PHÉP NHẬP SỐ và giới hạn độ dài.
     */
    public static void applyNumericOnlyFilter(javafx.scene.control.TextField textField, int maxLength) {
        if (textField == null)
            return;

        // Tạo bộ lọc: Chặn nếu ký tự mới thêm vào KHÔNG phải là số, hoặc tổng độ dài
        // vượt quá maxLength
        javafx.scene.control.TextFormatter<String> formatter = new javafx.scene.control.TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change; // Trả về bình thường nếu không có nội dung thay đổi
            }

            String newText = change.getControlNewText();
            // Nếu chuỗi mới rỗng (người dùng đang xóa) HOẶC chuỗi mới toàn là số và không
            // vượt quá giới hạn
            if (newText.isEmpty() || (newText.matches("\\d+") && newText.length() <= maxLength)) {
                return change; // Hợp lệ, cho phép thay đổi
            }

            return null; // Trả về null nghĩa là từ chối thay đổi (không cho nhập)
        });

        textField.setTextFormatter(formatter);
    }

    /**
     * Chặn các dãy số CCCD "rác" hoặc giả mạo phổ biến.
     * - Chặn 12 chữ số giống hệt nhau (111..., 222...).
     * - Chặn chuỗi tiến/lùi đơn giản (0123..., 9876...).
     */
    public static boolean isCCCDInBlacklist(String cccd) {
        if (cccd == null || cccd.length() != 12)
            return false;

        // 1. Chặn chuỗi lặp 12 chữ số giống hệt nhau
        if (cccd.matches("^(\\d)\\1{11}$"))
            return true;

        // 2. Chặn chuỗi tiến/lùi đơn giản
        String ascending = "01234567890123456789";
        String descending = "98765432109876543210";
        if (ascending.contains(cccd) || descending.contains(cccd))
            return true;

        return false;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * 5. HÀM KIỂM TRA TRÙNG LẶP SĐT / CCCD TRONG DB
     * ══════════════════════════════════════════════════════════════════
     */

    public static boolean isDuplicateSDT(String sdt, String excludeId) {
        if (sdt == null || sdt.isBlank()) return false;
        String sql = "SELECT maNV as id FROM NV WHERE soDT = ? UNION ALL " +
                     "SELECT maKH as id FROM KH WHERE soDT = ?";
        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            ps.setString(2, sdt);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String existingId = rs.getString("id");
                    if (existingId != null && !existingId.trim().equals(excludeId)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isDuplicateCCCD(String cccd, String excludeId) {
        if (cccd == null || cccd.isBlank()) return false;
        String sql = "SELECT maNV as id FROM NV WHERE soCCCD = ? UNION ALL " +
                     "SELECT maKH as id FROM KH WHERE soCCCD = ?";
        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cccd);
            ps.setString(2, cccd);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String existingId = rs.getString("id");
                    if (existingId != null && !existingId.trim().equals(excludeId)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}