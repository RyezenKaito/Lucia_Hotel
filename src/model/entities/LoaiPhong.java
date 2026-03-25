package model.entities;

import model.enums.TenLoaiPhong;

public class LoaiPhong {
	private TenLoaiPhong tenLoaiPhong;
	private int sucChua;
	public LoaiPhong(TenLoaiPhong tenLoaiPhong, int sucChua) {
		super();
		this.tenLoaiPhong = tenLoaiPhong;
		this.sucChua = sucChua;
	}
	public LoaiPhong(TenLoaiPhong tenLoaiPhong) {
		super();
		this.tenLoaiPhong = tenLoaiPhong;
	}
	public TenLoaiPhong getTenLoaiPhong() {
		return tenLoaiPhong;
	}
	public void setTenLoaiPhong(TenLoaiPhong tenLoaiPhong) {
		this.tenLoaiPhong = tenLoaiPhong;
	}
	public int getSucChua() {
		return sucChua;
	}
	public void setSucChua(int sucChua) {
		this.sucChua = sucChua;
	}
}
