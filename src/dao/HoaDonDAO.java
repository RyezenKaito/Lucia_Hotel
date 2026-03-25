package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.ChiTietHoaDon;
import model.entities.DatPhong;
import model.entities.DichVu;
import model.entities.HoaDon;
import model.entities.NhanVien;
import model.entities.Phong;
import model.enums.PhuongThucThanhToan;
import model.enums.TrangThaiThanhToan;

public class HoaDonDAO {
	public List<HoaDon> getAll() {
		List<HoaDon>  dsHoaDon = new ArrayList<HoaDon>();
		try {
			Connection con = ConnectDatabase.getInstance().getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"SELECT * FROM HoaDon h "
					+ "JOIN ChiTietHoaDon ct ON h.maHoaDon=ct.maHoaDon "
					+ "JOIN DatPhong dp ON h.maDatPhong=dp.maDatPhong "
					+ "JOIN KhachHang k ON k.maKhachHang=dp.maKhachHang ");
			while(rs.next()) {
				
				TrangThaiThanhToan trangThai = TrangThaiThanhToan.valueOf(rs.getString("trangThai").replace(" ", "_"));
	            PhuongThucThanhToan phuongThuc = PhuongThucThanhToan.valueOf(rs.getString("hinhThucThanhToan").replace(" ", "_"));
	            	            
				dsHoaDon.add(
					new HoaDon(
						rs.getString("maHoaDon"),
						new DatPhong(rs.getString("maDatPhong")),
						new NhanVien(rs.getString("maNhanVien")),
						trangThai,
						rs.getTimestamp("ngayLap").toLocalDateTime(),
						phuongThuc,
						new ChiTietHoaDon(
								new DichVu(rs.getString("maDichVu")), new Phong(rs.getString("maPhong")))
				));
			}
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
		}
		return dsHoaDon;
		
	}
}
