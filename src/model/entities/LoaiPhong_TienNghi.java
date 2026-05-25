package model.entities;

public class LoaiPhong_TienNghi {
    private String maLoaiPhong;
    private String maTienNghi;

    public LoaiPhong_TienNghi(String maLoaiPhong, String maTienNghi) {
        this.maLoaiPhong = maLoaiPhong;
        this.maTienNghi = maTienNghi;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getMaTienNghi() {
        return maTienNghi;
    }

    public void setMaTienNghi(String maTienNghi) {
        this.maTienNghi = maTienNghi;
    }

}
