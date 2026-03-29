package model.entities;

import model.enums.TenLoaiPhong;

public class LoaiPhong {
    private TenLoaiPhong tenLoaiPhong;
    private int sucChua;
    private double donGia;

    public LoaiPhong() {
    }

    public LoaiPhong(TenLoaiPhong tenLoaiPhong, int sucChua, double donGia) {
        this.tenLoaiPhong = tenLoaiPhong;
        this.sucChua = sucChua;
        this.donGia = donGia;
    }

    public LoaiPhong(TenLoaiPhong tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    

	public LoaiPhong(TenLoaiPhong tenLoaiPhong, int sucChua) {
		super();
		this.tenLoaiPhong = tenLoaiPhong;
		this.sucChua = sucChua;
	}

	public TenLoaiPhong getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(TenLoaiPhong tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }

    public int getSucChua() { return sucChua; }
    public void setSucChua(int sucChua) { this.sucChua = sucChua; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}