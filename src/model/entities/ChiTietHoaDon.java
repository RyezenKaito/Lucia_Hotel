package model.entities;

public class ChiTietHoaDon {
	private HoaDon maHoaDon;
	private DichVu maDichVu;
	private Phong phong;
	public ChiTietHoaDon(HoaDon maHoaDon, DichVu maDichVu, Phong phong) {
		this.maHoaDon = maHoaDon;
		this.maDichVu = maDichVu;
		this.phong = phong;
	}
	public ChiTietHoaDon(HoaDon maHoaDon) {
		super();
		this.maHoaDon = maHoaDon;
	}
	public ChiTietHoaDon(DichVu maDichVu, Phong phong) {
		super();
		this.maDichVu = maDichVu;
		this.phong = phong;
	}
	public HoaDon getMaHoaDon() {
		return maHoaDon;
	}
	public void setMaHoaDon(HoaDon maHoaDon) {
		this.maHoaDon = maHoaDon;
	}
	public DichVu getMaDichVu() {
		return maDichVu;
	}
	public void setMaDichVu(DichVu maDichVu) {
		this.maDichVu = maDichVu;
	}
	public Phong getPhong() {
		return phong;
	}
	public void setPhong(Phong phong) {
		this.phong = phong;
	}
	
	
	
}
