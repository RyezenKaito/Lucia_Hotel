package model.entities;

import model.enums.TrangThaiPhong;

public class Phong {
	private String maPhong;
	private LoaiPhong loaiPhong;
	private TrangThaiPhong trangThai;
	private int soPhong;
	private int soTang;
	public Phong(String maPhong, LoaiPhong loaiPhong, TrangThaiPhong trangThai, int soTang) {
		super();
		this.maPhong = maPhong;
		this.loaiPhong = loaiPhong;
		this.trangThai = trangThai;
		this.soTang = soTang;
	}
	public int getSoPhong() {
		return soPhong;
	}
	public void setSoPhong(int soPhong) {
		this.soPhong = soPhong;
	}
	public int getSoTang() {
		return soTang;
	}
	public void setSoTang(int soTang) {
		this.soTang = soTang;
	}
	public Phong(String maPhong) {
		super();
		this.maPhong = maPhong;
	}
	public LoaiPhong getLoaiPhong() {
		return loaiPhong;
	}
	public void setLoaiPhong(LoaiPhong loaiPhong) {
		this.loaiPhong = loaiPhong;
	}
	public TrangThaiPhong getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(TrangThaiPhong trangThai) {
		this.trangThai = trangThai;
	}
	public String getMaPhong() {
		return maPhong;
	}
	
	
}
