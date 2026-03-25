package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CheckOutPanel extends JPanel {

    public CheckOutPanel() {

        setLayout(new BorderLayout(20,20));
        setBackground(new Color(245,240,232));
        setBorder(new EmptyBorder(30,40,30,40));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        
        Box row = Box.createHorizontalBox();

        JLabel title = new JLabel("Khách trả phòng");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(new Color(70,40,30));

        JLabel subtitle = new JLabel("Thực hiện thủ tục trả phòng cho khách và lập hóa đơn.");
        subtitle.setFont(new Font("Serif", Font.PLAIN, 16));
        subtitle.setForeground(new Color(160,120,80));

        row.add(title);
        row.add(Box.createVerticalStrut(5));
        header.add(row);
        row = Box.createHorizontalBox();
        row.add(subtitle);
        row.add(Box.createVerticalStrut(5));
        header.add(row);
        header.add(Box.createVerticalStrut(10));
        header.add(createLateCheckoutPolicyPanel());

        return header;
    }

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(20,20));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel leftPanel = new JPanel(new BorderLayout(15,15));
        leftPanel.setOpaque(false);

        leftPanel.add(createSearchPanel(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15,15));
        contentPanel.setOpaque(false);

        contentPanel.add(createGuestInfoPanel(), BorderLayout.NORTH);
        contentPanel.add(createServiceTablePanel(), BorderLayout.CENTER);

        leftPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel billing = createBillingPanel();
        billing.setPreferredSize(new Dimension(340,600));

        main.add(leftPanel, BorderLayout.CENTER);
        main.add(billing, BorderLayout.EAST);

        return main;
    }
    

    private JPanel createSearchPanel() {

    	JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(225, 220, 210), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel searchTitle = new JLabel("Tìm kiếm khách trả phòng");
        searchTitle.setFont(new Font("Serif", Font.BOLD, 20));
        searchTitle.setForeground(new Color(60, 45, 35));

        JTextField searchField = new JTextField("BK001");
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 200, 190)),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JButton searchBtn = new JButton("Tìm kiếm");
        searchBtn.setBackground(new Color(75, 50, 45));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchBtn.setPreferredSize(new Dimension(100, 35));

        JPanel topSearch = new JPanel(new BorderLayout(10, 0));
        topSearch.setOpaque(false);
        topSearch.add(searchField, BorderLayout.CENTER);
        topSearch.add(searchBtn, BorderLayout.EAST);

        JPanel rooms = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        rooms.setOpaque(false);
        rooms.setPreferredSize(new Dimension(600, 120)); 

        JLabel lblText = new JLabel("Phòng trả hôm nay: ");
        lblText.setFont(new Font("Serif", Font.PLAIN, 15));
        lblText.setForeground(new Color(160, 140, 120));
        rooms.add(lblText);

        String[] roomList = {
            "Room 101 — An", "Room 201 — Tuấn", "Room 203 — Nam", "Room 302 — Minh",
            "Room 401 — Long", "Room 502 — Lan"
        };

        for (String r : roomList) {
            JButton rb = new JButton(r);
            rb.setFont(new Font("Serif", Font.PLAIN, 14));
            rb.setForeground(new Color(120, 60, 50));
            rb.setBackground(new Color(250, 245, 240));
            rb.setFocusPainted(false);
            rb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 120, 110), 1),
                new EmptyBorder(5, 12, 5, 12)
            ));
            
            rooms.add(rb);
        }

        panel.add(searchTitle, BorderLayout.NORTH);
        panel.add(topSearch, BorderLayout.CENTER);
        panel.add(rooms, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLeftPanel() {

        JPanel left = new JPanel(new BorderLayout(20,20));
        left.setOpaque(false);

        left.add(createGuestInfoPanel(), BorderLayout.NORTH);
        left.add(createServiceTablePanel(), BorderLayout.CENTER);

        return left;
    }

    private JPanel createGuestInfoPanel() {

        JPanel panel = new JPanel(new BorderLayout(0,15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 20, 10));
        
        JLabel title = new JLabel("Thông tin đặt phòng");
        title.setForeground(new Color(60, 45, 35));
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel grid = new JPanel(new GridLayout(3, 2, 5, 5));
        grid.setBackground(Color.WHITE);
        grid.setOpaque(false);
        
        

        grid.add(createInfoBox("Guest","Nguyễn Văn An"));
        grid.add(createInfoBox("Room","Single — Room 101"));
        grid.add(createInfoBox("Check-in","2026-03-14"));
        grid.add(createInfoBox("Check-out Date","2026-03-17"));
        grid.add(createInfoBox("Duration","3 night(s)"));
        grid.add(createInfoBox("Guests","1 person(s)"));

        panel.add(title, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);

        return panel;
    	
    }

    private JPanel createInfoBox(String label, String value) {

        JPanel box = new JPanel(new BorderLayout());
        box.setBorder(new LineBorder(Color.LIGHT_GRAY));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif",Font.PLAIN,12));
        l.setBorder(new EmptyBorder(5,5,0,5));

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif",Font.BOLD,14));
        v.setBorder(new EmptyBorder(0,5,5,5));

        box.add(l,BorderLayout.NORTH);
        box.add(v,BorderLayout.CENTER);

        return box;
    }
    private JPanel createBillingPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,210,200)),
                new EmptyBorder(20,20,20,20)
        ));
        panel.setPreferredSize(new Dimension(260,0));

        JLabel title = new JLabel("Chi tiết hóa đơn", SwingConstants.LEFT);
        title.setFont(new Font("Serif",Font.BOLD,22));
        title.setForeground(new Color(70,40,30));

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        panel.add(createBillRow("<html>Room Fee (3N × 1.200.000 đ)</html>", "3.600.000 đ", Color.BLACK));
        panel.add(createDivider());

        panel.add(createBillRow("Service Fee", "575.000 đ", Color.BLACK));
        panel.add(createDivider());

        panel.add(createBillRow("Late Checkout (+30%)", "360.000 đ", new Color(170,60,60)));
        panel.add(createDivider());

        panel.add(createBillRow("Deposit Paid", "-600.000 đ", new Color(0,130,100)));

        panel.add(Box.createVerticalStrut(15));
        panel.add(createDividerBold());
        panel.add(Box.createVerticalStrut(10));

        panel.add(createBillRow("Tổng tiền: ", "3.935.000 đ", Color.BLACK));
        panel.add(createDivider());

        panel.add(Box.createVerticalStrut(25));

        JLabel payTitle = new JLabel("HÌNH THỨC THANH TOÁN", SwingConstants.CENTER);
        payTitle.setFont(new Font("SansSerif",Font.BOLD,14));
        payTitle.setForeground(new Color(160,120,80));

        panel.add(payTitle);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createPaymentOption("💵 Tiền mặt", false));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createPaymentOption("💳 Chuyển khoản", false));

        panel.add(Box.createVerticalStrut(20));

        JButton payBtn = new JButton("💳  Thanh Toán");
        payBtn.setBackground(new Color(92,50,44));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFocusPainted(false);
        payBtn.setFont(new Font("Serif",Font.BOLD,16));
        payBtn.setBorder(new EmptyBorder(14,10,14,10));
        payBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        payBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));

        panel.add(payBtn);

        panel.add(Box.createVerticalStrut(10));

        JButton printBtn = new JButton("🖨  In hóa đơn");
        printBtn.setBackground(new Color(240,235,228));
        printBtn.setFocusPainted(false);
        printBtn.setFont(new Font("Serif",Font.BOLD,15));

        // padding giống nút trên
        printBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220,210,200)),
                new EmptyBorder(14,10,14,10)
        ));

        printBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        printBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));

        panel.add(printBtn);

        return panel;
    }
    
    private JPanel createServiceTablePanel() {
    	
    	JLabel lblTiTle = new JLabel("Dịch vụ sử dụng");
    	lblTiTle.setFont(new Font("SansSerif", Font.BOLD, 20));
    	lblTiTle.setForeground(new Color(60, 45, 35));
    	lblTiTle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0) );

        String[] columns = {"Ngày", "Dịch vụ", "Mô tả", "Số lượng", "Thành tiền"};

        Object[][] data = {
            {"2026-03-14", "Breakfast", "Buffet", 2, "200.000 đ"},
            {"2026-03-15", "Laundry", "Clothes", 1, "50.000 đ"},
            {"2026-03-16", "Spa", "Massage", 1, "325.000 đ"}
        };

        JTable table = new JTable(data, columns);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(235, 230, 220));
        table.setGridColor(new Color(230, 225, 215));
        table.setShowVerticalLines(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
        		new LineBorder(Color.WHITE),
        		new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(Color.WHITE);
        panel.add(lblTiTle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBillRow(String name, String value, Color valueColor){

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        // 👉 giảm chiều cao dòng
        row.setBorder(new EmptyBorder(4,0,4,0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel left = new JLabel(name);
        left.setFont(new Font("SansSerif", Font.PLAIN, 13)); // nhỏ lại
        left.setForeground(new Color(130, 130, 130));

        JLabel right = new JLabel(value);
        right.setFont(new Font("SansSerif", Font.BOLD, 14)); // nhỏ lại
        right.setForeground(valueColor);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);

        return row;
    }
    private JSeparator createDivider(){
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230,220,210));
        return sep;
    }

    private JSeparator createDividerBold(){
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(120,80,60));
        return sep;
    }
    
    private JPanel createPaymentOption(String text, boolean selected){

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);

        // Kích thước giống button
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Border + bo góc + padding
        box.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,210,200), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        box.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Radio
        JRadioButton radio = new JRadioButton();
        radio.setSelected(selected);
        radio.setOpaque(false);

        // Label
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(60, 60, 60));
        
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        content.setOpaque(false);
        content.add(radio);
        content.add(label);

        box.add(content, BorderLayout.CENTER);

        // Hover nhẹ
        box.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!radio.isSelected())
                    box.setBackground(new Color(248,248,248));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!radio.isSelected())
                    box.setBackground(Color.WHITE);
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                radio.setSelected(true);
            }
        });

        return box;
    }

    private JPanel createLateCheckoutPolicyPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Chính Sách Trả Phòng Muộn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(150,100,60));
        container.add(lblTitle, BorderLayout.NORTH);

        // Panel chứa 4 thẻ chính sách
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);

        // Thêm các thẻ dựa theo ảnh
        cardsPanel.add(createPolicyCard(
            "TRẢ PHÒNG TIÊU CHUẨN", 
            "Trước 12:00", 
            "Không phụ phí", 
            new Color(240, 248, 245), 
            new Color(60, 120, 90)    
        ));

        cardsPanel.add(createPolicyCard(
            "TRẢ PHÒNG MUỘN", 
            "12:00 – 15:00", 
            "+30% giá phòng", 
            new Color(252, 248, 235), // Nền vàng nhạt
            new Color(180, 140, 60)   // Chữ vàng/nâu
        ));

        cardsPanel.add(createPolicyCard(
            "TRẢ PHÒNG MUỘN", 
            "15:00 – 18:00", 
            "+50% giá phòng", 
            new Color(252, 248, 235), // Cùng màu nền
            new Color(180, 140, 60)
        ));

        cardsPanel.add(createPolicyCard(
            "TRẢ PHÒNG MUỘN", 
            "Sau 18:00", 
            "+100% (1 đêm)", 
            new Color(250, 240, 240), // Nền hồng nhạt
            new Color(165, 55, 55)    // Chữ đỏ sẫm
        ));

        container.add(cardsPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel createPolicyCard(String title, String time, String surcharge, Color bgColor, Color textColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bgColor);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 50), 1), 
            new EmptyBorder(20, 10, 20, 10)
        ));

        JLabel lblT = new JLabel(title);
        lblT.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 180));

        JLabel lblTime = new JLabel(time);
        lblTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTime.setForeground(textColor);

        JLabel lblS = new JLabel(surcharge);
        lblS.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblS.setForeground(new Color(120, 120, 120));

        card.add(lblT);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTime);
        card.add(Box.createVerticalStrut(10));
        card.add(lblS);

        return card;
    }

}