import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Lucia_Hotel;integratedSecurity=true;encrypt=false;";
        try (Connection con = DriverManager.getConnection(url);
             Statement stmt = con.createStatement()) {
            
            System.out.println("--- Test DatPhong ---");
            ResultSet rs = stmt.executeQuery("SELECT dp.maDat, dp.trangThai, ctdp.maPhong, p.tinhTrang FROM DatPhong dp JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat JOIN Phong p ON ctdp.maPhong = p.maPhong WHERE p.tinhTrang = 'DANGSUDUNG'");
            while (rs.next()) {
                System.out.println(rs.getString("maDat") + " - " + rs.getString("trangThai") + " - " + rs.getString("maPhong") + " - " + rs.getString("tinhTrang"));
            }

            System.out.println("--- Test maCTDP subquery ---");
            ResultSet rs2 = stmt.executeQuery("SELECT ctdp.maPhong, ctdp.maCTDP FROM ChiTietDatPhong ctdp JOIN Phong p ON ctdp.maPhong = p.maPhong JOIN DatPhong dp ON ctdp.maDat = dp.maDat WHERE p.tinhTrang = N'DANGSUDUNG' AND dp.trangThai = N'DA_CHECKIN'");
            while (rs2.next()) {
                System.out.println("maPhong: " + rs2.getString("maPhong") + " - maCTDP: " + rs2.getString("maCTDP"));
            }

            System.out.println("--- Test subquery with ORDER BY ---");
            ResultSet rs3 = stmt.executeQuery("SELECT ctdp.maPhong, (SELECT TOP 1 c2.maCTDP FROM ChiTietDatPhong c2 JOIN DatPhong d2 ON c2.maDat = d2.maDat WHERE c2.maPhong = ctdp.maPhong AND d2.trangThai = N'DA_CHECKIN' ORDER BY d2.ngayCheckIn DESC) as topCTDP FROM ChiTietDatPhong ctdp JOIN Phong p ON ctdp.maPhong = p.maPhong JOIN DatPhong dp ON ctdp.maDat = dp.maDat WHERE p.tinhTrang = N'DANGSUDUNG' AND dp.trangThai = N'DA_CHECKIN'");
            while (rs3.next()) {
                System.out.println("maPhong: " + rs3.getString("maPhong") + " - topCTDP: " + rs3.getString("topCTDP"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
