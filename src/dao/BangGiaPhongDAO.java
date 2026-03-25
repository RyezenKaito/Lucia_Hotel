package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import connectDatabase.ConnectDatabase;
import model.entities.BangGiaPhong;
import model.entities.LoaiPhong;
import model.enums.TenLoaiPhong;

public class BangGiaPhongDAO {
	public BangGiaPhong getPriceByNameRoomType(String s) {
		BangGiaPhong bg = null;
		try {
			Connection con = ConnectDatabase.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM LoaiPhong_ThongTin lptt \r\n"
					+ "JOIN LoaiPhong_ChiTiet lpct on lpct.tenLoaiPhong = lptt.tenLoaiPhong\r\n"
					+ "WHERE lptt.tenLoaiPhong = ?");
					
			stmt.setString(1, s);
			ResultSet rs = stmt.executeQuery();
			
			
			if(rs.next()) {
				TenLoaiPhong tl = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong"));
				bg = new BangGiaPhong(rs.getString("tenLoaiPhong"), 
						new LoaiPhong(tl, rs.getInt("sucChua")), 
						rs.getDouble("giaPhong"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bg;
	}

}
