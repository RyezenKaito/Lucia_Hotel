package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import model.entities.NhanVien;

import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private NhanVien staff;
    private JButton activeMenuButton = null;

    public MainFrame(NhanVien staff) {
        this.staff = staff;
        init();
    }
    public MainFrame() {
        init();
    }

    private void init() {
        setTitle("Khách sạn Lucia Star");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Layout chính
        add(createSidebar(), BorderLayout.WEST);
        add(createHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Thêm các Panel vào CardLayout
        mainPanel.add(new TrangChuPanel(), "dashboard");
        mainPanel.add(createScrollPane(new TaoDonDatPhongPanel()), "booking");
        mainPanel.add(createScrollPane(new KhachHangPanel()), "customers");
        mainPanel.add(new CheckInPanel(), "checkin");
        mainPanel.add(createScrollPane(new CheckOutPanel()), "checkout");
        mainPanel.add(createScrollPane(new HoaDonPanel()), "invoices");
        mainPanel.add(createScrollPane(new ThemDichVuPanel()), "service");

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

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(menuItem("Trang chủ", "dashboard"));
        sidebar.add(menuItem("Đặt phòng", "booking"));
        sidebar.add(menuItem("Nhận phòng", "checkin"));
        sidebar.add(menuItem("Trả phòng", "checkout"));
        sidebar.add(menuItem("Khách hàng", "customers"));
        sidebar.add(menuItem("Dịch vụ", "service"));
        sidebar.add(menuItem("Hóa đơn", "invoices"));

        return sidebar;
    }

    private JButton menuItem(String text, String cardName) {
    JButton btn = new JButton(text);
    btn.setMaximumSize(new Dimension(250, 50));
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    btn.setContentAreaFilled(false);
    btn.setOpaque(true);
    
    // Màu mặc định ban đầu
    btn.setBackground(new Color(50, 30, 28)); 
    btn.setForeground(new Color(212, 175, 55));
    
    btn.setFocusPainted(false);
    btn.setBorder(new EmptyBorder(10, 25, 10, 10));
    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    if (text.equals("Trang chủ")) {
        setActiveStyle(btn);
        activeMenuButton = btn;
    }

    btn.addActionListener(e -> {
        // trạng thái bình thường
        if (activeMenuButton != null) {
            setNormalStyle(activeMenuButton);
        }      
        // Cập nhật nút mới thành trạng thái Active
        setActiveStyle(btn);
        activeMenuButton = btn;
        
        //  Chuyển card
        cardLayout.show(mainPanel, cardName);
    });

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent e) {
            // Chỉ hiệu ứng hover nếu nút đó không phải nút đang chọn
            if (btn != activeMenuButton) {
                btn.setBackground(new Color(74, 45, 42));
                btn.setBorder(new MatteBorder(0, 5, 0, 0, new Color(212, 175, 55)));
            }
        }
        public void mouseExited(java.awt.event.MouseEvent e) {
            // Nếu không phải nút đang chọn thì trả về màu bình thường
            if (btn != activeMenuButton) {
                setNormalStyle(btn);
            }
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

        JLabel title = new JLabel("RECEPTION DESK");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(74, 45, 42));

        JLabel date = new JLabel("Hệ thống quản lý Grand Palace Hotel");
        date.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        date.setForeground(new Color(140, 140, 140));

        left.add(title);
        left.add(date);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 20));
        right.setOpaque(false);

        JLabel user = new JLabel("👤 " + (staff != null ? staff.getHoTen() : "Admin"));
        user.setFont(new Font("Segoe UI", Font.BOLD, 14));

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