package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import model.entities.NhanVien;
import model.enums.ChucVu;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private NhanVien staff;
    private JButton activeMenuButton = null;
    private boolean isAdmin = false;
    private JPanel serviceSubMenuPanel;
    private boolean isServiceExpanded = false;

    public MainFrame(NhanVien staff) {
        this.staff = staff;
        this.isAdmin = (staff != null && staff.getRole() == ChucVu.QUAN_LY);
        init();
    }
    
    public MainFrame() {
        init();
    }

    private void init() {
        setTitle("Khách sạn Lucia Star - " + (isAdmin ? "Quản lý" : "Nhân viên"));
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Nạp các Panel
        mainPanel.add(new TrangChuPanel(), "dashboard");
//        mainPanel.add(new TaoDonDatPhongPanel(), "booking");
        mainPanel.add(createScrollPane(new KhachHangPanel()), "customers");
        mainPanel.add(createScrollPane(new NhanVienPanel(isAdmin)), "staff");
        mainPanel.add(new CheckInPanel(), "checkin");
        mainPanel.add(createScrollPane(new CheckOutPanel()), "checkout");
        mainPanel.add(createScrollPane(new HoaDonPanel()), "invoices");
        mainPanel.add(createScrollPane(new ThemDichVuPanel()), "service");
        mainPanel.add(new BangGiaDichVuPanel(), "servicePrice");
        
        // TRUYỀN QUYỀN IS_ADMIN VÀO ĐÂY
        mainPanel.add(createScrollPane(new QuanLyPhongPanel(isAdmin)), "rooms");
        	
        add(mainPanel, BorderLayout.CENTER);
    }

    private JScrollPane createScrollPane(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(new Color(50, 30, 28));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Tăng khoảng cách Strut cho thoáng menu
        sidebar.add(Box.createVerticalStrut(20));
        
        // Cập nhật lại Icon và Text đúng chuẩn đẹp
        sidebar.add(menuItem("Trang chủ", "dashboard"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Đặt phòng", "booking"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Nhận phòng", "checkin"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Trả phòng", "checkout"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Khách hàng", "customers"));
        sidebar.add(Box.createVerticalStrut(5));
        
        
        JButton btnServiceMain = createDropdownHeader("Dịch vụ");
        sidebar.add(btnServiceMain);

        serviceSubMenuPanel = new JPanel();
        serviceSubMenuPanel.setLayout(new BoxLayout(serviceSubMenuPanel, BoxLayout.Y_AXIS));
        serviceSubMenuPanel.setBackground(new Color(40, 24, 22)); // Màu tối hơn một chút để phân biệt
        serviceSubMenuPanel.setVisible(false); // Mặc định ẩn

        serviceSubMenuPanel.add(subMenuItem("Sử dụng dịch vụ", "service"));
        serviceSubMenuPanel.add(subMenuItem("Bảng giá dịch vụ", "servicePrice"));
        
        sidebar.add(serviceSubMenuPanel);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Hóa đơn", "invoices"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Nhân viên", "staff"));

        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(menuItem("Phòng", "rooms"));


        sidebar.add(Box.createVerticalGlue());
        
        // Phần thông tin vai trò ở cuối Sidebar
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 55, 50));
        sep.setMaximumSize(new Dimension(220, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        JLabel lblRole = new JLabel(isAdmin ? "  Chế độ: Quản lý" : "  Chế độ: Nhân viên");
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblRole.setForeground(isAdmin ? new Color(212, 175, 55) : new Color(160, 160, 160));
        lblRole.setBorder(new EmptyBorder(0, 20, 15, 0));
        sidebar.add(lblRole);

        return sidebar;
    }
    

    private JButton createDropdownHeader(String text) {
        JButton btn = menuItem(text, null); // Tận dụng style của menuItem
        // Ghi đè hành động để đóng/mở thay vì chuyển Card
        for (java.awt.event.ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
        
        btn.addActionListener(e -> {
            isServiceExpanded = !isServiceExpanded;
            serviceSubMenuPanel.setVisible(isServiceExpanded);
            btn.setText(text + (isServiceExpanded ? "  ▲" : "  ▼")); // Thêm mũi tên cho đẹp
            revalidate(); // Cập nhật lại layout sidebar
        });
        btn.setText(text + "  ▼");
        return btn;
    }

    // Hàm tạo nút Con (Sử dụng DV, Bảng giá)
    private JButton subMenuItem(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(250, 40)); // Thấp hơn nút chính
        btn.setPreferredSize(new Dimension(250, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(40, 24, 22)); 
        btn.setForeground(new Color(180, 150, 100)); // Màu nhạt hơn một chút
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0, 45, 0, 10)); // Thụt lề vào trong
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            cardLayout.show(mainPanel, cardName);
            if (activeMenuButton != null) setNormalStyle(activeMenuButton);
            btn.setBackground(new Color(74, 45, 42)); // High-light nút con
            activeMenuButton = btn;
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeMenuButton) btn.setBackground(new Color(60, 35, 32));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeMenuButton) btn.setBackground(new Color(40, 24, 22));
            }
        });

        return btn;
    }    
    
    private JButton menuItem(String text, String cardName) {
        JButton btn = new JButton(text);
        // GIỮ NGUYÊN CHIỀU CAO 50 NHƯ BẠN MUỐN
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(50, 30, 28)); 
        btn.setForeground(new Color(212, 175, 55));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 25, 10, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (text.contains("Trang chủ")) {
            setActiveStyle(btn);
            activeMenuButton = btn;
        }

        btn.addActionListener(e -> {
            if (activeMenuButton != null) setNormalStyle(activeMenuButton);
            setActiveStyle(btn);
            activeMenuButton = btn;
            if(cardName != null) {
                cardLayout.show(mainPanel, cardName);
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeMenuButton) {
                    btn.setBackground(new Color(74, 45, 42));
                    btn.setBorder(new MatteBorder(0, 5, 0, 0, new Color(212, 175, 55)));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeMenuButton) setNormalStyle(btn);
            }
        });
        return btn;
    }

    private void setActiveStyle(JButton btn) {
        btn.setBackground(new Color(92, 55, 51)); 
        btn.setForeground(Color.WHITE); 
        btn.setBorder(new MatteBorder(0, 8, 0, 0, new Color(212, 175, 55))); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
    }
    
    private void setNormalStyle(JButton btn) {
        btn.setBackground(new Color(50, 30, 28));
        btn.setForeground(new Color(212, 175, 55));
        btn.setBorder(new EmptyBorder(10, 25, 10, 10));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(100, 80));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(15, 25, 10, 0));

        JLabel title = new JLabel(isAdmin ? "ADMIN PANEL" : "RECEPTION DESK");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(74, 45, 42));

        JLabel date = new JLabel("Hệ thống quản lý khách sạn Lucia Star");
        date.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        date.setForeground(new Color(140, 140, 140));

        left.add(title);
        left.add(date);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 20));
        right.setOpaque(false);

        String name = (staff != null ? staff.getHoTen() : "Admin");
        String roleTag = isAdmin ? "[Quản lý]" : " [Nhân viên]";
        JLabel user = new JLabel(name + roleTag);
        user.setFont(new Font("Segoe UI", Font.BOLD, 14));
        user.setForeground(isAdmin ? new Color(160, 120, 40) : new Color(74, 45, 42));

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(new Color(180, 60, 60));
        btnLogout.setBorder(new CompoundBorder(new LineBorder(new Color(230, 210, 200)), new EmptyBorder(8, 15, 8, 15)));
        btnLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Xác nhận đăng xuất?", "Thông báo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                this.dispose();
                new DangNhapJFrame().setVisible(true);
            }
        });

        right.add(user);
        right.add(btnLogout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }
}