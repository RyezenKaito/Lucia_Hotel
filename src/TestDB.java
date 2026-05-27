import connectDatabase.ConnectDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection con = ConnectDatabase.getInstance().getConnection();
            if (con == null) return;
            
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT TOP 1 maDV FROM DV");
            String maDV = "";
            if (rs.next()) {
                maDV = rs.getString(1);
            }
            
            String sql = "MERGE INTO DichVuSuDung AS target " +
                "USING (VALUES (?, ?, ?, ?)) AS source (maDV, maCTDP, soLuong, giaDV) " +
                "ON target.maDV = source.maDV AND target.maCTDP = source.maCTDP " +
                "WHEN MATCHED THEN " +
                "    UPDATE SET target.soLuong = target.soLuong + source.soLuong, " +
                "              target.ngaySuDung = GETDATE() " +
                "WHEN NOT MATCHED THEN " +
                "    INSERT (maDV, maCTDP, ngaySuDung, soLuong, giaDV, trangThai) " +
                "    VALUES (source.maDV, source.maCTDP, GETDATE(), source.soLuong, source.giaDV, 0);";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDV);
            ps.setString(2, "CTDP0333");
            ps.setInt(3, 1);
            ps.setDouble(4, 50000);
            
            ps.addBatch();
            int[] rows = ps.executeBatch();
            System.out.println("Batch executed, row counts: " + java.util.Arrays.toString(rows));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
