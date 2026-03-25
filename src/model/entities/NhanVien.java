package model.entities;

import java.time.LocalDateTime;

import model.enums.ChucVu;

public class NhanVien {
	private String maNhanVien;
	private String hoTen;
	private String soDienThoai;
	private LocalDateTime ngayVaoLam;
	private float heSoLuong;
	private String matKhau;
	private ChucVu chucVu;
	public String getHoTen() {
		return hoTen;
	}
	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}
	public String getSoDienThoai() {
		return soDienThoai;
	}
	public void setSoDienThoai(String soDienThoai) {
		this.soDienThoai = soDienThoai;
	}
	public LocalDateTime getNgayVaoLam() {
		return ngayVaoLam;
	}
	public void setNgayVaoLam(LocalDateTime ngayVaoLam) {
		this.ngayVaoLam = ngayVaoLam;
	}
	public float getHeSoLuong() {
		return heSoLuong;
	}
	public void setHeSoLuong(float heSoLuong) {
		this.heSoLuong = heSoLuong;
	}
	public String getMatKhau() {
		return matKhau;
	}
	public void setMatKhau(String matKhau) {
		this.matKhau = matKhau;
	}
	public ChucVu getChucVu() {
		return chucVu;
	}
	public void setChucVu(ChucVu chucVu) {
		this.chucVu = chucVu;
	}
	public String getMaNhanVien() {
		return maNhanVien;
	}
	public NhanVien(String maNhanVien, String hoTen, String soDienThoai, LocalDateTime ngayVaoLam, float heSoLuong,
			String matKhau, ChucVu chucVu) {
		super();
		this.maNhanVien = maNhanVien;
		this.hoTen = hoTen;
		this.soDienThoai = soDienThoai;
		this.ngayVaoLam = ngayVaoLam;
		this.heSoLuong = heSoLuong;
		this.matKhau = matKhau;
		this.chucVu = chucVu;
	}
	public NhanVien(String maNhanVien) {
		super();
		this.maNhanVien = maNhanVien;
	}
	
	
}
