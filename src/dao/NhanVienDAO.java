package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDatabase.ConnectDatabase;
import model.entities.NhanVien;

public class NhanVienDAO {
	public List<NhanVien> getAll() {
		List<NhanVien> ds = new ArrayList<NhanVien>();
		try {
			Connection con = ConnectDatabase.getInstance().getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(null);
			while(rs.next()) {
				ds.add(new NhanVien(null));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;
	}
	/***
	 * xac thuc tai khoan cua nhan vien
	 * @param staffID
	 * @param password
	 * @return 
	 */
	public boolean authenticate(String staffID, String password) {
		boolean flag = false;
		try {
			Connection con= ConnectDatabase.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM NhanVien WHERE maNhanVien =?");
			
			ps.setString(1, staffID);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				if(password.equals(rs.getString("matKhau"))) {
					flag = true;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
}
