package model.entities;

import java.time.LocalDateTime;

import model.enums.PhuongThucThanhToan;
import model.enums.TrangThaiThanhToan;

public class HoaDon {
	private String maHoaDon;
	private DatPhong datPhong;
	private NhanVien nhanVien;
	private TrangThaiThanhToan trangThai;
	private LocalDateTime ngayLapHoaDon;
	private PhuongThucThanhToan hinhThucThanhToan;
	private ChiTietHoaDon cthd;
	public HoaDon(String maHoaDon, DatPhong datPhong, NhanVien nhanVien, TrangThaiThanhToan trangThai,
			LocalDateTime ngayLapHoaDon, PhuongThucThanhToan hinhThucThanhToan,  ChiTietHoaDon cthd) {
		this.maHoaDon = maHoaDon;
		this.datPhong = datPhong;
		this.nhanVien = nhanVien;
		this.trangThai = trangThai;
		this.ngayLapHoaDon = ngayLapHoaDon;
		this.hinhThucThanhToan = hinhThucThanhToan;
		this.cthd = cthd;
	}
	public HoaDon(String maHoaDon) {
		this.maHoaDon = maHoaDon;
	}
	public DatPhong getDatPhong() {
		return datPhong;
	}
	public void setDatPhong(DatPhong datPhong) {
		this.datPhong = datPhong;
	}
	public NhanVien getNhanVien() {
		return nhanVien;
	}
	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}
	public TrangThaiThanhToan getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(TrangThaiThanhToan trangThai) {
		this.trangThai = trangThai;
	}
	public LocalDateTime getNgayLapHoaDon() {
		return ngayLapHoaDon;
	}
	public void setNgayLapHoaDon(LocalDateTime ngayLapHoaDon) {
		this.ngayLapHoaDon = ngayLapHoaDon;
	}
	public PhuongThucThanhToan getHinhThucThanhToan() {
		return hinhThucThanhToan;
	}
	public void setHinhThucThanhToan(PhuongThucThanhToan hinhThucThanhToan) {
		this.hinhThucThanhToan = hinhThucThanhToan;
	}
	public String getMaHoaDon() {
		return maHoaDon;
	}
	public ChiTietHoaDon getCthd() {
		return cthd;
	}
	public void setCthd(ChiTietHoaDon cthd) {
		this.cthd = cthd;
	}
}
