package model.entities;

import model.enums.DonViTinh;

public class DichVu {
	private String maDichVu;
	private String tenDichVu;
	private String moTa;
	private DonViTinh donViTinh;
	private BangGiaDichVu bangGia;
	public DichVu(String maDichVu, String tenDichVu, String moTa, DonViTinh donViTinh, BangGiaDichVu bangGia) {
		super();
		this.maDichVu = maDichVu;
		this.tenDichVu = tenDichVu;
		this.moTa = moTa;
		this.donViTinh = donViTinh;
		this.bangGia = bangGia;
	}
	public DichVu(String maDichVu) {
		super();
		this.maDichVu = maDichVu;
	}
	public String getTenDichVu() {
		return tenDichVu;
	}
	public void setTenDichVu(String tenDichVu) {
		this.tenDichVu = tenDichVu;
	}
	public String getMoTa() {
		return moTa;
	}
	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}
	public DonViTinh getDonViTinh() {
		return donViTinh;
	}
	public void setDonViTinh(DonViTinh donViTinh) {
		this.donViTinh = donViTinh;
	}
	public BangGiaDichVu getBangGia() {
		return bangGia;
	}
	public void setBangGia(BangGiaDichVu bangGia) {
		this.bangGia = bangGia;
	}
	public String getMaDichVu() {
		return maDichVu;
	}
	
}
