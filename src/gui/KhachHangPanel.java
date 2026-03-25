package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import dao.KhachHangDAO;
import java.awt.*;
import model.entities.KhachHang;

public class KhachHangPanel extends JPanel {
    private KhachHangDAO khachHangDAO = new KhachHangDAO();
    private DefaultTableModel dtmKhachHang;
    private JTable tblKhachHang;

    public KhachHangPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 241, 234));
        // Tăng padding bên ngoài panel chính
        setBorder(new EmptyBorder(10, 30, 10, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
    }

    public JPanel createHeader() {
        // Sử dụng BoxLayout trục Y để các thành phần xếp chồng và dễ tạo khoảng cách
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setOpaque(false);

        // --- 1. PHẦN TRÊN: TIÊU ĐỀ VÀ NÚT ADD ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        // Khống chế chiều cao header trên cùng
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Bên trái: Tiêu đề
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Customer List");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 32));
        lblTitle.setForeground(new Color(60, 40, 30));

        JLabel lblSubTitle = new JLabel("12 registered guests");
        lblSubTitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubTitle.setForeground(Color.GRAY);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubTitle);

        // Bên phải: Nút Add (Đã chỉnh kích thước)
        JButton btnAdd = new JButton("+ Add Customer");
        btnAdd.setBackground(new Color(60, 40, 35));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(160, 20)); 
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(btnAdd, BorderLayout.EAST);

        // --- 2. PHẦN GIỮA: CÁC THẺ THỐNG KÊ (CARDS) ---
        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardPanel.setOpaque(false);
        // Khống chế chiều cao vùng card
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        cardPanel.add(createStatCard("TOTAL GUESTS", "12", Color.BLACK));
        cardPanel.add(createStatCard("VIP GUESTS (5+ STAYS)", "3", new Color(180, 150, 100)));
        cardPanel.add(createStatCard("INTERNATIONAL", "2", new Color(50, 80, 120)));
        cardPanel.add(createStatCard("TOTAL REVENUE", "253.600.000 đ", new Color(40, 100, 80)));

        // --- 3. PHẦN DƯỚI: THANH TÌM KIẾM ---
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JTextField txtSearch = new JTextField("Tìm kiếm bằng tên, số điện thoại...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("SansSerif", Font.ITALIC, 14));
        txtSearch.setForeground(Color.GRAY);

        JLabel lblSearchIcon = new JLabel("🔍 ");
        searchPanel.add(lblSearchIcon, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        // Gộp các phần vào pnlMain kèm khoảng cách (Strut)
        pnlMain.add(topPanel);
        pnlMain.add(Box.createVerticalStrut(20)); // Khoảng cách sau tiêu đề
        pnlMain.add(cardPanel);
        pnlMain.add(Box.createVerticalStrut(20)); // Khoảng cách sau cards
        pnlMain.add(searchPanel);
        pnlMain.add(Box.createVerticalStrut(25)); // KHOẢNG CÁCH GIỮA SEARCH VÀ TABLE

        return pnlMain;
    }

    private JPanel createStatCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTitle.setForeground(Color.LIGHT_GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblValue.setForeground(valueColor);

        card.add(lblTitle);
        card.add(lblValue);
        return card;
    }

    private JScrollPane createTable() {
        String[] columns = {"Số thứ tự", "Mã khách hàng", "Họ tên", "CCCD", "Số điện thoại"};
        dtmKhachHang = new DefaultTableModel(null, columns);
        tblKhachHang = new JTable(dtmKhachHang);

        tblKhachHang.setRowHeight(40);
        tblKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tblKhachHang.setGridColor(new Color(240, 240, 240));
        tblKhachHang.setShowVerticalLines(false);

        JTableHeader header = tblKhachHang.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(new Color(90, 55, 45));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 40));

        DefaultTableCellRenderer dftcr = new DefaultTableCellRenderer();
        dftcr.setHorizontalAlignment(SwingConstants.CENTER);
        tblKhachHang.setDefaultRenderer(Object.class, dftcr);

        JScrollPane scroll = new JScrollPane(tblKhachHang);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);

//        initData();
        return scroll;
    }

    public void initData() {
        dtmKhachHang.setRowCount(0);
        java.util.List<KhachHang> dsKhachHang = khachHangDAO.getAll();

        int stt = 1; // Thường bắt đầu từ 1
        for (KhachHang khachHang : dsKhachHang) {
            Object[] row = {
                stt++, 
                khachHang.getMaKhachHang(), 
                khachHang.getHoTen(), 
                khachHang.getCCCD(), 
                khachHang.getSoDienThoai()
            };
            dtmKhachHang.addRow(row);
        }
    }
}