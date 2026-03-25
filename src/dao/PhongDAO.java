package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.LoaiPhong;
import model.entities.Phong;
import model.enums.TenLoaiPhong;
import model.enums.TrangThaiPhong;

public class PhongDAO {
	public List<Phong> getAll() {
		List<Phong> ds = new ArrayList<Phong>();
		try {
			Connection con = ConnectDatabase.getInstance().getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM Phong");
			while(rs.next()) {
				TenLoaiPhong tlp = TenLoaiPhong.valueOf(rs.getString("tenLoaiPhong"));
				TrangThaiPhong ttp= TrangThaiPhong.valueOf(rs.getString("tinhTrang"));
				ds.add(new Phong(rs.getString("maPhong"), new LoaiPhong(tlp),ttp, rs.getInt("soTang")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;
	}
}
