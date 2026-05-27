import dao.DatPhongDAO;
import model.entities.DichVu;
import java.util.HashMap;
import java.util.Map;

public class TestAddService {
    public static void main(String[] args) {
        try {
            DatPhongDAO datPhongDAO = new DatPhongDAO();
            Map<DichVu, Integer> cart = new HashMap<>();
            
            // Using a valid DV code from database, assume DV02 exists
            DichVu dv = new DichVu("DV02");
            dv.setGia(100000.0);
            cart.put(dv, 2);
            
            boolean result = datPhongDAO.saveServiceOrder("P302", cart);
            System.out.println("Add service result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
