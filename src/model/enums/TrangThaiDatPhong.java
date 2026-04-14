package model.enums;

public enum TrangThaiDatPhong {
    CHO_XACNHAN("Chờ xác nhận"),
    DA_XACNHAN("Đã xác nhận"),
    DA_CHECKIN("Đã nhận phòng"),
    DA_CHECKOUT("Đã trả phòng"),
    DA_HUY("Đã hủy");

    private final String thongTinTrangThai;

    TrangThaiDatPhong(String thongTinTrangThai) {
        this.thongTinTrangThai = thongTinTrangThai;
    }

    public String getThongTinTrangThai() {
        return thongTinTrangThai;
    }
}
