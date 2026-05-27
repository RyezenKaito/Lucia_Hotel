import dao.DatPhongDAO;
import java.time.LocalDate;
import java.util.List;

public class TestDAO {
    public static void main(String[] args) {
        DatPhongDAO dao = new DatPhongDAO();
        List<Object[]> rows = dao.getDonCheckInByDate(LocalDate.now(), false);
        System.out.println("Rows: " + rows.size());
        for (Object[] r : rows) {
            System.out.println(r[0] + " - " + r[1]);
        }
    }
}
