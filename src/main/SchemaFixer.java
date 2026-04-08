package main;

import connectDatabase.ConnectDatabase;
import java.sql.Connection;
import java.sql.Statement;

public class SchemaFixer {
    public static void main(String[] args) {
        String sql = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'DV' AND COLUMN_NAME = 'trangThai') " +
                     "ALTER TABLE DV ADD trangThai INT DEFAULT 0;";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
            System.out.println("SCHEMA_FIX_SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SCHEMA_FIX_FAILED: " + e.getMessage());
        }
    }
}
