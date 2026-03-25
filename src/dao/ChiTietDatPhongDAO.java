package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connectDatabase.ConnectDatabase;
import model.entities.ChiTietDatPhong;

public class ChiTietDatPhongDAO {
	public ChiTietDatPhong findChiTietDatPhongById(String maDP, String maPhong) {
		try {
			Connection con = ConnectDatabase.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM ChiTietDatPhong WHERE maDatPhong=? AND maPhong=?");
			pstmt.setString(1, maDP);
			pstmt.setString(2, maPhong);
			try {
				ResultSet rs = pstmt.executeQuery();
				if(rs.next()) {
					return new ChiTietDatPhong(
							rs.getTimestamp("ngayCheckInThucTe").toLocalDateTime(), 
							rs.getTimestamp("ngayCheckOutThucTe").toLocalDateTime());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
}
