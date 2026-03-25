package model.entities;

import java.time.LocalDateTime;

public class BangGiaPhong {
	private String maBangGia;
	private LoaiPhong loaiPhong;
	private LocalDateTime ngayBatDau;
	private LocalDateTime ngayKetThuc;
	private double donGia;
	
	public LoaiPhong getLoaiPhong() {
		return loaiPhong;
	}
	public void setLoaiPhong(LoaiPhong loaiPhong) {
		this.loaiPhong = loaiPhong;
	}
	public LocalDateTime getNgayBatDau() {
		return ngayBatDau;
	}
	public void setNgayBatDau(LocalDateTime ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}
	public LocalDateTime getNgayKetThuc() {
		return ngayKetThuc;
	}
	public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}
	public double getDonGia() {
		return donGia;
	}
	public void setDonGia(double donGia) {
		this.donGia = donGia;
	}
	public String getMaBangGia() {
		return maBangGia;
	}
	public BangGiaPhong(String maBangGia, LoaiPhong loaiPhong, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc,
			double donGia) {
		super();
		this.maBangGia = maBangGia;
		this.loaiPhong = loaiPhong;
		this.ngayBatDau = ngayBatDau;
		this.ngayKetThuc = ngayKetThuc;
		this.donGia = donGia;
	}
	public BangGiaPhong(String maBangGia) {
		this.maBangGia = maBangGia;
	}
	public BangGiaPhong(String maBangGia, LoaiPhong loaiPhong, double donGia) {
		this.maBangGia = maBangGia;
		this.loaiPhong = loaiPhong;
		this.donGia = donGia;
	}

	public BangGiaPhong() {
		super();
	}
	
	
	
}
