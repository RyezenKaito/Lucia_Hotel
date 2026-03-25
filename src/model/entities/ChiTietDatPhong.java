package model.entities;

import java.time.LocalDateTime;

public class ChiTietDatPhong {
	private DatPhong datPhong;
	private Phong phong;
	private LocalDateTime ngayCheckInThucTe;
	private LocalDateTime ngayCheckOutThucTe;
	
	
	
	public ChiTietDatPhong(DatPhong datPhong, Phong phong) {
		super();
		this.datPhong = datPhong;
		this.phong = phong;
	}
	public ChiTietDatPhong(DatPhong datPhong, Phong phong, LocalDateTime ngayCheckInThucTe,
			LocalDateTime ngayCheckOutThucTe) {
		super();
		this.datPhong = datPhong;
		this.phong = phong;
		this.ngayCheckInThucTe = ngayCheckInThucTe;
		this.ngayCheckOutThucTe = ngayCheckOutThucTe;
	}
	public ChiTietDatPhong(LocalDateTime ngayCheckInThucTe, LocalDateTime ngayCheckOutThucTe) {
		super();
		this.ngayCheckInThucTe = ngayCheckInThucTe;
		this.ngayCheckOutThucTe = ngayCheckOutThucTe;
	}
	public LocalDateTime getNgayCheckInThucTe() {
		return ngayCheckInThucTe;
	}
	public void setNgayCheckInThucTe(LocalDateTime ngayCheckInThucTe) {
		this.ngayCheckInThucTe = ngayCheckInThucTe;
	}
	public LocalDateTime getNgayCheckOutThucTe() {
		return ngayCheckOutThucTe;
	}
	public void setNgayCheckOutThucTe(LocalDateTime ngayCheckOutThucTe) {
		this.ngayCheckOutThucTe = ngayCheckOutThucTe;
	}
	public DatPhong getDatPhong() {
		return datPhong;
	}
	public Phong getPhong() {
		return phong;
	}
	
	
}
