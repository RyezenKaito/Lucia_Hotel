package model.entities;

import model.enums.TrangThaiPhong;
import java.util.List;

public class Phong {
    private String maPhong;
    private LoaiPhong loaiPhong;
    private TrangThaiPhong trangThai;
    private int soTang;

    public static final int MAX_TANG = 10;
    public static final int MAX_PHONG_MOI_TANG = 10;

    public static String phatSinhMaPhong(int tang, List<Phong> danhSachHienTai) {
        if (tang <= 0 || tang > MAX_TANG) return "TẦNG KHÔNG HỢP LỆ";
        
        int maxSTT = 0;
        int count = 0;
        
        for (Phong p : danhSachHienTai) {
            if (p.getSoTang() == tang) {
                count++;
                try {
                    String ma = p.getMaPhong().trim();
                    if (ma.length() >= 2) {
                        int stt = Integer.parseInt(ma.substring(ma.length() - 2));
                        if (stt > maxSTT) maxSTT = stt;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (count >= MAX_PHONG_MOI_TANG) return "FULL"; 
        return String.format("P%d%02d", tang, maxSTT + 1);
    }

    public Phong(String maPhong, LoaiPhong loaiPhong, TrangThaiPhong trangThai, int soTang) {
        this.maPhong = maPhong;
        this.loaiPhong = loaiPhong;
        this.trangThai = trangThai;
        this.soTang = soTang;
    }
    
    
    
    public Phong(String maPhong) {
		super();
		this.maPhong = maPhong;
	}

	public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public int getSoTang() { return soTang; }
    public void setSoTang(int soTang) { this.soTang = soTang; }

    public LoaiPhong getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(LoaiPhong loaiPhong) { this.loaiPhong = loaiPhong; }

    public TrangThaiPhong getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiPhong trangThai) { this.trangThai = trangThai; }
}