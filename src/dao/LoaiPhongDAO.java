package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.LoaiPhong;
import model.enums.TenLoaiPhong;

public class LoaiPhongDAO {	
	public String[] fetchAllRoomTypeNames() {
	    String[] ds = new String[100];
	    int i = 0;
	    try (Connection con = ConnectDatabase.getInstance().getConnection();
	         Statement stmt = con.createStatement();
	         ResultSet rs = stmt.executeQuery("SELECT tenLoaiPhong FROM LoaiPhong_ThongTin")) {

	        while (rs.next() && i < 100) {
	            ds[i++] = rs.getString("tenLoaiPhong");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    String[] result = new String[i];
	    System.arraycopy(ds, 0, result, 0, i);
	    return result; 
	}
	public List<LoaiPhong> getAll() {
		List<LoaiPhong> ds = new ArrayList<LoaiPhong>();
		try {
			Connection con= ConnectDatabase.getInstance().getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM LoaiPhong");
			
			while(rs.next()) {
				TenLoaiPhong tlp = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong").trim());
				ds.add(new LoaiPhong(tlp,Integer.parseInt(rs.getString("sucChua"))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;
	}
}
