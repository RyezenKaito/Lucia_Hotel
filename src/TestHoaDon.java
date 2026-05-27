import dao.HoaDonDAO;
import model.entities.HoaDon;
import java.util.List;

public class TestHoaDon {
    public static void main(String[] args) {
        try {
            System.out.println("Start testing HoaDon");
            HoaDonDAO dao = new HoaDonDAO();
            List<HoaDon> list = dao.getAllWithKhachHang();
            System.out.println("Size: " + list.size());
            dao.DichVuSuDungDAO dvsdDAO = new dao.DichVuSuDungDAO();
            
            for (HoaDon hd : list) {
                if ("DA_THANH_TOAN_COC".equals(hd.getTrangThaiThanhToan()) &&
                        hd.getDatPhong() != null &&
                        "DA_CHECKIN".equals(hd.getDatPhong().getTrangThai())) {
                    hd.setTrangThaiThanhToan("CHUA_THANH_TOAN");
                }

                double currentSumPhong = dao.getTongTienPhongCurrent(hd.getMaHD());
                double tongCocHD = new dao.ChiTietHoaDonDAO().getTongCocByMaHD(hd.getMaHD());
                if (currentSumPhong > 0 || tongCocHD > 0)
                    hd.setTienCoc(tongCocHD);

                java.util.List<model.entities.DichVuSuDung> listDV = dvsdDAO.findByMaHD(hd.getMaHD());
                double totalTienDV = listDV.stream().mapToDouble(model.entities.DichVuSuDung::getThanhTien).sum();

                double phuPhiTraMuon = hd.getPhuPhiTraMuon();
                double phuThu = hd.getPhuThu();
                double vatAmount = (currentSumPhong + totalTienDV + phuPhiTraMuon + phuThu) * hd.getThueVAT();
                double tcp = currentSumPhong + totalTienDV + phuPhiTraMuon + phuThu + vatAmount;
                double ttt = Math.max(0, tcp - hd.getTienCoc());

                hd.setTienPhong(currentSumPhong);
                hd.setTienDV(totalTienDV);
                hd.setTongCP(tcp);
                hd.setTongTien(ttt);
                dao.tinhDoanhThu(hd);
            }
            System.out.println("Test done successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
