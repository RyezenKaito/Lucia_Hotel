package model.enums;

/**
 * Enum đại diện cho các loại dịch vụ, ánh xạ giữa mã ASCII (DB) và tên hiển thị (UI)
 */
public enum LoaiDichVu {
    THUC_PHAM("THUC_PHAM", "Thực phẩm"),
    GIAI_TRI("GIAI_TRI", "Giải trí"),
    SUC_KHOE("SUC_KHOE", "Sức khỏe"),
    TIEN_ICH("TIEN_ICH", "Tiện ích");

    private final String dbKey;
    private final String displayName;

    LoaiDichVu(String dbKey, String displayName) {
        this.dbKey = dbKey;
        this.displayName = displayName;
    }

    public String getDbKey() {
        return dbKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển từ mã DB sang Enum
     */
    public static LoaiDichVu fromDbKey(String key) {
        for (LoaiDichVu l : values()) {
            if (l.dbKey.equalsIgnoreCase(key)) return l;
        }
        return THUC_PHAM; // Mặc định
    }

    /**
     * Chuyển từ tên hiển thị sang Enum
     */
    public static LoaiDichVu fromDisplayName(String name) {
        for (LoaiDichVu l : values()) {
            if (l.displayName.equalsIgnoreCase(name)) return l;
        }
        return THUC_PHAM;
    }
}
