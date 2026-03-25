package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connectDatabase.ConnectDatabase;
import model.entities.KhachHang;

public class DatPhongDAO {
	public KhachHang findKhachHangByIdDatPhong(String ma) {
		try {
			Connection con = ConnectDatabase.getInstance().getConnection();
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM DatPhong WHERE maDatPhong=?");
			pstmt.setString(1, ma);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				KhachHangDAO khDAO = new KhachHangDAO();
				KhachHang kh = khDAO.findKhachHangByID(rs.getString("maKhachHang").toString());
				return kh;
			}
		
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
}
