package model.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DatPhong {
	private String maDatPhong;
	private LocalDateTime ngayDat;
	private LocalDateTime ngayCheckInDuKien;
	private LocalDateTime ngayCheckOutDuKien;
	private double tienDatCoc;
	private KhachHang khachHang;
	private NhanVien nhanVien;
	private ChiTietDatPhong ctDatPhong;
	
	public DatPhong(String maDatPhong, KhachHang khachHang) {
		super();
		this.maDatPhong = maDatPhong;
		this.khachHang = khachHang;
	}
	public DatPhong(String maDatPhong) {
		this.maDatPhong = maDatPhong;
	}
	public DatPhong(String maDatPhong, LocalDateTime ngayDat, LocalDateTime ngayCheckInDuKien,
			LocalDateTime ngayCheckOutDuKien, double tienDatCoc, KhachHang khachHang, NhanVien nhanVien,
			ChiTietDatPhong ctDatPhong) {
		super();
		this.maDatPhong = maDatPhong;
		this.ngayDat = ngayDat;
		this.ngayCheckInDuKien = ngayCheckInDuKien;
		this.ngayCheckOutDuKien = ngayCheckOutDuKien;
		this.tienDatCoc = tienDatCoc;
		this.khachHang = khachHang;
		this.nhanVien = nhanVien;
		this.ctDatPhong= ctDatPhong;
	}
	public LocalDateTime getNgayDat() {
		return ngayDat;
	}
	public void setNgayDat(LocalDateTime ngayDat) {
		this.ngayDat = ngayDat;
	}
	public LocalDateTime getNgayCheckInDuKien() {
		return ngayCheckInDuKien;
	}
	public void setNgayCheckInDuKien(LocalDateTime ngayCheckInDuKien) {
		this.ngayCheckInDuKien = ngayCheckInDuKien;
	}
	public LocalDateTime getNgayCheckOutDuKien() {
		return ngayCheckOutDuKien;
	}
	public void setNgayCheckOutDuKien(LocalDateTime ngayCheckOutDuKien) {
		this.ngayCheckOutDuKien = ngayCheckOutDuKien;
	}
	public double getTienDatCoc() {
		return tienDatCoc;
	}
	public void setTienDatCoc(double tienDatCoc) {
		this.tienDatCoc = tienDatCoc;
	}
	public KhachHang getKhachHang() {
		return khachHang;
	}
	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}
	public NhanVien getNhanVien() {
		return nhanVien;
	}
	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}
	public String getMaDatPhong() {
		return maDatPhong;
	}
	public ChiTietDatPhong getCtDatPhong() {
		return ctDatPhong;
	}
	public void setCtDatPhong(ChiTietDatPhong ctDatPhong) {
		this.ctDatPhong = ctDatPhong;
	}
	
	public int tinhSoNgay() {
		return (int) ChronoUnit.DAYS.between(ngayCheckInDuKien, ngayCheckOutDuKien);
	}
	
	
}
