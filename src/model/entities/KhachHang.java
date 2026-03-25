package model.entities;

public class KhachHang {
	private String maKhachHang;
	private String hoTen;
	private String CCCD;
	private String soDienThoai;
	
	
	public KhachHang(String maKhachHang) {
		this.maKhachHang = maKhachHang;}

	public KhachHang(String maKhachHang, String hoTen, String cCCD, String soDienThoai) {
		super();
		this.maKhachHang = maKhachHang;
		this.hoTen = hoTen;
		CCCD = cCCD;
		this.soDienThoai = soDienThoai;
	}
	
	//============GETTER/SETTER==========
	public String getHoTen() {
		return hoTen;
	}

	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}

	public String getCCCD() {
		return CCCD;
	}

	public void setCCCD(String cCCD) {
		CCCD = cCCD;
	}

	public String getSoDienThoai() {
		return soDienThoai;
	}

	public void setSoDienThoai(String soDienThoai) {
		this.soDienThoai = soDienThoai;
	}

	public String getMaKhachHang() {
		return maKhachHang;
	}
	
	
}
