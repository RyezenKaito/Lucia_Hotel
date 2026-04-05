package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import dao.PhongDAO;
import dao.DichVuDAO;
import dao.DatPhongDAO;
import model.entities.Phong;
import model.entities.DichVu;

public class ThemDichVuPanel extends JPanel {
    // Palette màu Luxury
    private final Color COLOR_PRIMARY = new Color(92, 50, 44);     // Nâu đậm
    private final Color COLOR_ACCENT = new Color(205, 175, 125);   // Vàng đồng
    private final Color COLOR_BG = new Color(248, 245, 240);       // Kem nhạt
    private final Color COLOR_CARD = Color.WHITE;

    private PhongDAO phongDAO = new PhongDAO();
    private DichVuDAO dichVuDAO = new DichVuDAO();
    private DatPhongDAO datPhongDAO = new DatPhongDAO();

    private String selectedMaPhong = ""; 
    private String currentCategory = "Minibar";
    private Map<DichVu, Integer> cart = new LinkedHashMap<>(); 
    
    private JPanel roomContainer, serviceContainer, billContainer;
    private JLabel lblTotalBill, lblRoomTitle;

    public ThemDichVuPanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm dịch vụ vào phòng");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(COLOR_PRIMARY);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; 
        gbc.weighty = 1.0;

        // Left Panel (65%)
        gbc.gridx = 0; gbc.weightx = 0.65;
        body.add(createLeftPanel(), gbc);

        // Right Panel (35%)
        gbc.gridx = 1; gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 25, 0, 0);
        body.add(createRightPanel(), gbc);

        add(body, BorderLayout.CENTER);

        refreshRooms();
        refreshServices("Minibar");
    }

    private JPanel createLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setOpaque(false);

        // Khu vực chọn phòng - Khống chế chiều cao
        JPanel roomBox = new JPanel(new BorderLayout());
        roomBox.setBackground(COLOR_CARD);
        roomBox.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)), new EmptyBorder(10,15,10,15)));
        roomBox.setPreferredSize(new Dimension(0, 135)); 

        JLabel lbl = new JLabel("Chọn phòng đang sử dụng");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setBorder(new EmptyBorder(0,0,5,0));
        roomBox.add(lbl, BorderLayout.NORTH);

        roomContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        roomContainer.setOpaque(false);
        
        JScrollPane roomScroll = new JScrollPane(roomContainer);
        roomScroll.setBorder(null);
        roomScroll.setOpaque(false);
        roomScroll.getViewport().setOpaque(false);
        roomBox.add(roomScroll, BorderLayout.CENTER);

        // Khu vực dịch vụ
        JPanel serviceBox = new JPanel(new BorderLayout());
        serviceBox.setOpaque(false);

        JPanel tabs = new JPanel(new GridLayout(1, 4, 8, 0));
        tabs.setOpaque(false);
        tabs.setPreferredSize(new Dimension(0, 40));
        String[] cats = {"Ẩm thực", "Giải trí", "Sức khỏe", "Tiện ích"};
        for (String c : cats) tabs.add(createTabButton(c));

        serviceContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        serviceContainer.setBackground(COLOR_BG);
        
        JScrollPane serviceScroll = new JScrollPane(serviceContainer);
        serviceScroll.setBorder(new EmptyBorder(10,0,0,0));
        serviceScroll.getVerticalScrollBar().setUnitIncrement(16);

        serviceBox.add(tabs, BorderLayout.NORTH);
        serviceBox.add(serviceScroll, BorderLayout.CENTER);

        p.add(roomBox, BorderLayout.NORTH);
        p.add(serviceBox, BorderLayout.CENTER);
        return p;
    }

    private JPanel createTabButton(String name) {
        boolean isSelected = currentCategory.equals(name);
        JPanel btn = new JPanel(new GridBagLayout());
        btn.setBackground(isSelected ? COLOR_PRIMARY : COLOR_CARD);
        btn.setBorder(new LineBorder(new Color(220,220,220)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(name);
        lbl.setForeground(isSelected ? Color.WHITE : Color.GRAY);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.add(lbl);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                currentCategory = name;
                refreshServices(name);
                Container parent = btn.getParent();
                parent.removeAll();
                for (String c : new String[]{"Ẩm thực", "Giải trí", "Sức khỏe", "Tiện ích"}) 
                    parent.add(createTabButton(c));
                parent.revalidate(); parent.repaint();
            }
        });
        return btn;
    }

    private JPanel createRightPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_CARD);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(220,210,200)), new EmptyBorder(20,20,20,20)));

        lblRoomTitle = new JLabel("Hóa đơn: ---");
        lblRoomTitle.setFont(new Font("Serif", Font.BOLD, 20));
        lblRoomTitle.setBorder(new EmptyBorder(0,0,10,0));

        billContainer = new JPanel();
        billContainer.setLayout(new BoxLayout(billContainer, BoxLayout.Y_AXIS));
        billContainer.setBackground(COLOR_CARD);

        JScrollPane scroll = new JScrollPane(billContainer);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(235,235,235)));

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15,0,0,0));

        lblTotalBill = new JLabel("Tổng: 0 đ");
        lblTotalBill.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotalBill.setForeground(COLOR_PRIMARY);

        JButton btnSave = new JButton("XÁC NHẬN THÊM");
        btnSave.setBackground(COLOR_PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSave.setPreferredSize(new Dimension(0, 45));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> handleConfirmSave());

        footer.add(lblTotalBill, BorderLayout.NORTH);
        footer.add(btnSave, BorderLayout.SOUTH);

        p.add(lblRoomTitle, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(footer, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createServiceCard(DichVu dv) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 70));
        card.setBackground(COLOR_CARD);
        card.setBorder(new LineBorder(new Color(230, 230, 230)));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(10, 12, 10, 5));
        
        JLabel name = new JLabel("<html><body style='width: 80px'><b>" + dv.getTenDV() + "</b></body></html>");
        name.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        JLabel price = new JLabel(String.format("%,.0f đ", dv.getGia()));
        price.setForeground(new Color(160, 100, 60));
        price.setFont(new Font("SansSerif", Font.BOLD, 12));

        info.add(name); info.add(price);

        JButton btnAdd = new JButton("+");
        btnAdd.setPreferredSize(new Dimension(45, 0));
        btnAdd.setBackground(COLOR_ACCENT);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnAdd.setBorder(null);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAdd.addActionListener(e -> {
            cart.put(dv, cart.getOrDefault(dv, 0) + 1);
            updateBillUI();
        });

        card.add(info, BorderLayout.CENTER);
        card.add(btnAdd, BorderLayout.EAST);
        return card;
    }

    private JPanel createRoomCard(Phong p) {
        boolean isSelected = selectedMaPhong.equals(p.getMaPhong());
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setPreferredSize(new Dimension(85, 65)); // Kích thước gọn
        card.setBackground(isSelected ? new Color(250, 240, 230) : COLOR_CARD);
        card.setBorder(new LineBorder(isSelected ? COLOR_PRIMARY : new Color(230, 230, 230), isSelected ? 2 : 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel id = new JLabel(p.getMaPhong(), SwingConstants.CENTER);
        id.setFont(new Font("SansSerif", Font.BOLD, 15));
        
        String guest = datPhongDAO.getTenKhachHienTai(p.getMaPhong());
        JLabel name = new JLabel(guest != null ? guest : "...", SwingConstants.CENTER);
        name.setFont(new Font("SansSerif", Font.PLAIN, 10));
        name.setForeground(new Color(150, 100, 80));

        card.add(id); card.add(name);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedMaPhong = p.getMaPhong();
                lblRoomTitle.setText("Hóa đơn: P." + selectedMaPhong);
                refreshRooms();
            }
        });
        return card;
    }

    private void updateBillUI() {
        billContainer.removeAll();
        double total = 0;
        for (Map.Entry<DichVu, Integer> entry : cart.entrySet()) {
            DichVu dv = entry.getKey();
            int qty = entry.getValue();
            double sub = dv.getGia() * qty;
            total += sub;

            JPanel item = new JPanel(new BorderLayout());
            item.setOpaque(false);
            item.setMaximumSize(new Dimension(1000, 40));
            item.setBorder(new EmptyBorder(8, 5, 8, 5));

            JLabel left = new JLabel(qty + " x " + dv.getTenDV());
            JLabel right = new JLabel(String.format("%,.0f đ", sub));
            
            item.add(left, BorderLayout.WEST);
            item.add(right, BorderLayout.EAST);
            billContainer.add(item);
        }
        lblTotalBill.setText(String.format("Tổng cộng: %,.0f đ", total));
        billContainer.revalidate(); billContainer.repaint();
    }

    private void refreshRooms() {
        roomContainer.removeAll();
        for (Phong p : phongDAO.getAll()) {
            if (p.getTinhTrang() == model.enums.TrangThaiPhong.DACOKHACH) roomContainer.add(createRoomCard(p));
        }
        roomContainer.revalidate(); roomContainer.repaint();
    }

    private void refreshServices(String cat) {
        serviceContainer.removeAll();
        List<DichVu> list = dichVuDAO.getByType(cat);
        for (DichVu dv : list) serviceContainer.add(createServiceCard(dv));
        
        serviceContainer.revalidate(); serviceContainer.repaint();
    }

    private void handleConfirmSave() {
        if (selectedMaPhong.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng!"); return; 
        }
        if (cart.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Chưa chọn dịch vụ!"); return; 
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thêm dịch vụ vào phòng " + selectedMaPhong + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (datPhongDAO.saveServiceOrder(selectedMaPhong, cart)) {
                JOptionPane.showMessageDialog(this, "Thành công!");
                cart.clear(); updateBillUI();
            }
        }
    }

//    public static void main(String[] args) {
//        // Thiết lập Look and Feel của hệ thống để giao diện mượt hơn
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // Chạy giao diện trên Event Dispatch Thread
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Hệ thống Quản lý Khách sạn - Test Dịch Vụ");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            
//            // Thiết lập kích thước cửa sổ (thường là full màn hình hoặc 1280x800)
//            frame.setSize(1200, 800);
//            frame.setMinimumSize(new Dimension(1000, 700));
//            
//            // Khởi tạo Panel của bạn
//            ThemDichVuPanel servicePanel = new ThemDichVuPanel();
//            
//            // Thêm vào Frame
//            frame.add(servicePanel);
//            
//            // Căn giữa màn hình
//            frame.setLocationRelativeTo(null);
//            
//            // Hiển thị
//            frame.setVisible(true);
//        });
//    }
}