package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class CheckInPanel extends JPanel {

    // ===== COLOR =====
    Color pageBg = new Color(240, 242, 245);
    Color panelBg = Color.WHITE;
    Color primaryColor = new Color(92, 54, 46);
    Color accentColor = new Color(150, 100, 40);
    Color borderColor = new Color(220, 220, 220);

    private JLabel lblName, lblPhone, lblID, lblRoomType, lblCheckIn, lblGuests;

    private JPanel selectedRoom = null;

    public CheckInPanel() {
        setLayout(new BorderLayout());
        setBackground(pageBg);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        // ===== TITLE =====
        JLabel title = new JLabel("Thủ Tục Nhận Phòng");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(primaryColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Tìm kiếm đơn đặt phòng bằng mã hoặc số điện thoại để gán phòng.");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subtitle.setForeground(new Color(120, 120, 120));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(title);
        container.add(Box.createVerticalStrut(5));
        container.add(subtitle);
        container.add(Box.createVerticalStrut(25));

        // ===== STEP 1 =====
        JPanel step1 = createStyledPanel("1. TÌM KIẾM ĐƠN ĐẶT PHÒNG");

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchBar.setOpaque(false);

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(450, 40));
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        JButton searchBtn = new JButton("Tìm Kiếm");
        stylePrimaryButton(searchBtn);

        searchBar.add(new JLabel("Mã đặt phòng / SĐT:"));
        searchBar.add(txtSearch);
        searchBar.add(searchBtn);

        JPanel quickSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quickSearch.setOpaque(false);
        quickSearch.add(new JLabel("Gợi ý hôm nay: "));
        quickSearch.add(createSmallBtn("BK002 - Bích"));
        quickSearch.add(createSmallBtn("BK005 - Lan"));

        step1.add(searchBar);
        step1.add(quickSearch);

        container.add(step1);
        container.add(Box.createVerticalStrut(20));

        // ===== STEP 2 =====
        JPanel step2Container = new JPanel(new GridLayout(1, 2, 20, 0));
        step2Container.setOpaque(false);

        JPanel guestInfo = createStyledPanel("2A. THÔNG TIN KHÁCH HÀNG");
        guestInfo.add(createRow("Họ tên:", lblName = new JLabel("---")));
        guestInfo.add(createRow("SĐT:", lblPhone = new JLabel("---")));
        guestInfo.add(createRow("CCCD:", lblID = new JLabel("---")));

        JPanel bookingInfo = createStyledPanel("2B. CHI TIẾT ĐẶT PHÒNG");
        bookingInfo.add(createRow("Loại phòng:", lblRoomType = new JLabel("---")));
        bookingInfo.add(createRow("Thời gian:", lblCheckIn = new JLabel("---")));
        bookingInfo.add(createRow("Số khách:", lblGuests = new JLabel("---")));

        step2Container.add(guestInfo);
        step2Container.add(bookingInfo);

        container.add(step2Container);
        container.add(Box.createVerticalStrut(20));

        // ===== STEP 3 =====
        JPanel step3 = createStyledPanel("3. CHỌN PHÒNG VÀ HOÀN TẤT");

        JPanel roomSelection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        roomSelection.setOpaque(false);

        roomSelection.add(createRoomCard("202", "Phòng Đơn"));
        roomSelection.add(createRoomCard("203", "Phòng Đơn"));
        roomSelection.add(createRoomCard("301", "Phòng Đôi"));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        styleSecondaryButton(btnCancel);

        JButton btnConfirm = new JButton("XÁC NHẬN NHẬN PHÒNG");
        stylePrimaryButton(btnConfirm);
        btnConfirm.setPreferredSize(new Dimension(260, 45));	
        btnCancel.setPreferredSize(btnConfirm.getPreferredSize());
        
        actionPanel.add(btnConfirm);
        actionPanel.add(btnCancel);
        

        step3.add(new JLabel("Các phòng trống phù hợp:"));
        step3.add(roomSelection);
        step3.add(Box.createVerticalStrut(10));
        step3.add(actionPanel);

        container.add(step3);

        add(new JScrollPane(container), BorderLayout.CENTER);
    }

    // ===== PANEL STYLE =====
    private JPanel createStyledPanel(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(panelBg);

        p.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                new CompoundBorder(
                        new LineBorder(new Color(230,230,230), 1, true),
                        new EmptyBorder(20, 25, 20, 25)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(accentColor);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        p.add(lblTitle);
        return p;
    }

    // ===== ROW =====
    private JPanel createRow(String label, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        // 👉 Tăng chiều cao row
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(120, 120, 120));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // 👉 Làm value nổi bật hơn
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        valueLabel.setForeground(new Color(40, 40, 40));

        // 👉 Padding trong row
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                new EmptyBorder(8, 5, 8, 5)
        ));

        p.add(lbl, BorderLayout.WEST);
        p.add(valueLabel, BorderLayout.EAST);

        return p;
    }

    // ===== BUTTON =====
    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(primaryColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 22, 12, 22));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(primaryColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(primaryColor);
            }
        });
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(new Color(220,220,220));
        btn.setForeground(Color.DARK_GRAY);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
    }

    private JButton createSmallBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.PLAIN, 14));
        btn.setForeground(new Color(120, 60, 50));
        btn.setBackground(new Color(250, 245, 240));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180, 120, 110), 1),
            new EmptyBorder(5, 12, 5, 12)
        ));

        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(235, 235, 235));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(250, 250, 250));
            }
        });

        return btn;
    }

    // ===== ROOM CARD =====
    private JPanel createRoomCard(String id, String type) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setPreferredSize(new Dimension(120, 80));
        p.setBackground(new Color(235, 250, 235));
        p.setBorder(new LineBorder(new Color(120, 200, 120), 1, true));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblId = new JLabel(id, SwingConstants.CENTER);
        lblId.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel lblType = new JLabel(type, SwingConstants.CENTER);
        lblType.setFont(new Font("SansSerif", Font.PLAIN, 11));

        p.add(lblId);
        p.add(lblType);

        // CLICK chọn phòng
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (selectedRoom != null) {
                    selectedRoom.setBackground(new Color(235, 250, 235));
                }
                p.setBackground(new Color(180, 230, 180));
                selectedRoom = p;
            }

            public void mouseEntered(MouseEvent e) {
                if (p != selectedRoom)
                    p.setBackground(new Color(210, 240, 210));
            }

            public void mouseExited(MouseEvent e) {
                if (p != selectedRoom)
                    p.setBackground(new Color(235, 250, 235));
            }
        });

        return p;
    }
}