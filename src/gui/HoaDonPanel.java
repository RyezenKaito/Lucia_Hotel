package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.ChiTietDatPhongDAO;
import dao.DatPhongDAO;
import dao.HoaDonDAO;
import model.entities.ChiTietDatPhong;
import model.entities.HoaDon;
import model.entities.KhachHang;

import java.awt.*;
import java.util.List;

public class HoaDonPanel extends JPanel {
	private JTable tblHoaDon;
	private DefaultTableModel dfmHoaDon;
	private HoaDonDAO hoaDonDAO = new HoaDonDAO();
	private DatPhongDAO datPhongDAO = new DatPhongDAO();
	private ChiTietDatPhongDAO ctDatPhongDAO = new ChiTietDatPhongDAO();

    public HoaDonPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 240, 235));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(245, 240, 235));
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("Invoice List");
        title.setFont(new Font("Serif", Font.BOLD, 26));

        JLabel sub = new JLabel("4 total invoices");
        sub.setForeground(Color.GRAY);

        header.add(title);
        header.add(sub);
        header.add(Box.createVerticalStrut(15));
        header.add(createSummary());
        header.add(Box.createVerticalStrut(10));
        header.add(createSearchBar());

        return header;
    }

    // ================= SUMMARY =================
    private JPanel createSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(new Color(245, 240, 235));

        panel.add(createBox("TOTAL OUTSTANDING", "34.740.000 đ"));
        panel.add(createBox("PAID", "0 invoices"));
        panel.add(createBox("PENDING", "4 invoices"));

        return panel;
    }

    private JPanel createBox(String title, String value) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        box.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel t = new JLabel(title);
        t.setForeground(new Color(160, 120, 80));

        JLabel v = new JLabel(value);
        v.setFont(new Font("Serif", Font.BOLD, 18));

        box.add(t);
        box.add(Box.createVerticalStrut(10));
        box.add(v);

        return box;
    }

    // ================= SEARCH =================
    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(245, 240, 235));

        JTextField search = new JTextField("Search by invoice ID, guest name or room...");
        search.setPreferredSize(new Dimension(300, 35));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(new Color(245, 240, 235));

        String[] filters = {"All", "Pending", "Paid", "Cancelled"};

        for (String f : filters) {
            JButton btn = new JButton(f);
            if (f.equals("All")) {
                btn.setBackground(new Color(92, 50, 44));
                btn.setForeground(Color.WHITE);
            }
            filterPanel.add(btn);
        }

        panel.add(search, BorderLayout.CENTER);
        panel.add(filterPanel, BorderLayout.EAST);

        return panel;
    }

    // ================= TABLE =================
    private JScrollPane createTable() {

        String[] columns = {
                "Mã hóa đơn", "Mã đặt phòng", "Họ tên khách hàng", "Phòng",
                "Ngày nhận phòng", "Ngày trả phòng", "Số ngày",
                "Giá phòng", "Giá dịch vụ", "Tiền cọc",
                "Tổng tiền", "Trạng thái"
        };

        dfmHoaDon = new DefaultTableModel(null, columns);

        tblHoaDon = new JTable(dfmHoaDon);
        tblHoaDon.setRowHeight(30);
        
        tblHoaDon.getTableHeader().setBackground(new Color(92,50,44));
        tblHoaDon.getTableHeader().setForeground(new Color(230,180,120));
        tblHoaDon.getTableHeader().setFont(new Font("Serif", Font.BOLD, 13));
        tblHoaDon.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JScrollPane scroll = new JScrollPane(tblHoaDon);
        
//        initData();
        return scroll;
    }
    
//    private void initData() {
//    	dfmHoaDon.setRowCount(0);
//    	List<HoaDon> dsHoaDon = hoaDonDAO.getAll();  	
//    	
//    	for (HoaDon hoaDon : dsHoaDon) {
//    		KhachHang kh = datPhongDAO.findKhachHangByIdDatPhong(hoaDon.getDatPhong().getMaDatPhong());
//    		ChiTietDatPhong ctDP = ctDatPhongDAO.findChiTietDatPhongById(hoaDon.getDatPhong().getMaDatPhong(), hoaDon.getCthd().getPhong().getMaPhong());
//    		
//			String[] row = {hoaDon.getMaHoaDon(), hoaDon.getDatPhong().getMaDatPhong(), kh.getHoTen(),
//					hoaDon.getCthd().getPhong().getMaPhong(), ctDP.getNgayCheckInThucTe().toString(),
//					ctDP.getNgayCheckOutThucTe().toString(), String.valueOf(1),String.valueOf(1000000000),
//					String.valueOf(1000000000),hoaDon.getDatPhong().getTienDatCoc() +"",String.valueOf(10000000),
//					hoaDon.getTrangThai().toString()
//					};
//			dfmHoaDon.addRow(row);
//		}
//    }
}
