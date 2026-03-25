package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class ChiTietDatPhongFrame extends JFrame {

    private final Color BACKGROUND_COLOR = new Color(245, 240, 230); // Màu kem nhạt
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_DARK = new Color(50, 45, 42); // Màu nâu đậm
    private final Color TEXT_LIGHT = new Color(140, 140, 140);
    private final Color ACCENT_GOLD = new Color(193, 154, 107);
    private final Color SUCCESS_GREEN = new Color(60, 120, 80);
    private final Color BUTTON_DARK = new Color(50, 30, 28);
    
    private String nameCustomer, phone, id, roomType, checkInDate, checkOutDate;
    private int customerNumber;
    private double deposit, amount;

    public ChiTietDatPhongFrame(String nameCustomer, String phone, String id, String roomType, String checkInDate,
                                String checkOutDate, int customerNumber, double deposit, double amount) {
        this.nameCustomer = nameCustomer;
        this.phone = phone;
        this.id = id;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.customerNumber = customerNumber;
        this.deposit = deposit;
        this.amount = amount;
        init();
    }
    

    public ChiTietDatPhongFrame() throws HeadlessException {
    	init();
	}


    private void init() {
        setTitle("Xác nhận đặt phòng");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new GridBagLayout());

 
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 210, 200), 1),
            new EmptyBorder(25, 35, 25, 35) 
        ));
        card.setMaximumSize(new Dimension(450, 700));

        // 1. Tiêu đề (Làm gọn lại)
        JLabel titleLabel = new JLabel("Đặt Phòng Thành Công!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Giảm size font một chút
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bookingIdLabel = new JLabel("Mã: #BK-" + (int)(Math.random()*10000));
        bookingIdLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        bookingIdLabel.setForeground(ACCENT_GOLD);
        bookingIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Phần thông tin chi tiết (Giảm khoảng cách padding)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_COLOR);
        infoPanel.setBorder(new EmptyBorder(20, 0, 20, 0)); // Giảm khoảng cách trên dưới

        DecimalFormat df = new DecimalFormat("#,###");
        
        // Thêm các dòng dữ liệu
        infoPanel.add(createDetailRow("Khách hàng", nameCustomer));
        infoPanel.add(createDetailRow("Số điện thoại", phone));
        infoPanel.add(createDetailRow("ID / Passport", id));
        infoPanel.add(createDetailRow("Loại phòng", roomType));
        infoPanel.add(createDetailRow("Lưu trú", checkInDate + " ➔ " + checkOutDate));
        infoPanel.add(createDetailRow("Tiền cọc", df.format(deposit) + " đ"));
        
        JPanel totalRow = createDetailRow("TỔNG TIỀN", df.format(amount) + " đ");
        JLabel lblPrice = (JLabel) totalRow.getComponent(1);
        lblPrice.setForeground(new Color(180, 60, 60));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoPanel.add(totalRow);

        // 3. Các nút bấm (Làm nút gọn hơn)
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10)); 
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setMaximumSize(new Dimension(350, 150));

        buttonPanel.add(createStyledButton("NHẬN PHÒNG NGAY", BUTTON_DARK, Color.WHITE));
        buttonPanel.add(createStyledButton("TẠO ĐẶT PHÒNG MỚI", ACCENT_GOLD, Color.WHITE));
        buttonPanel.add(createStyledButton("ĐÓNG", Color.WHITE, TEXT_DARK));


        card.add(titleLabel);
        card.add(bookingIdLabel);
        card.add(infoPanel);
        card.add(buttonPanel);
        
        add(card);
        pack();
        if(getWidth() < 500) setSize(500, getHeight());
        
        setLocationRelativeTo(null);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD_COLOR);
        row.setMaximumSize(new Dimension(1000, 45));
        row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(245, 245, 245)));

        JLabel lblLeft = new JLabel(label.toUpperCase());
        lblLeft.setForeground(TEXT_LIGHT);
        lblLeft.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLeft.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel lblRight = new JLabel(value);
        lblRight.setForeground(TEXT_DARK);
        lblRight.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRight.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lblLeft, BorderLayout.WEST);
        row.add(lblRight, BorderLayout.EAST);
        return row;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45));
        
        // Hiệu ứng viền
        if (bg.equals(Color.WHITE)) {
            btn.setBorder(new LineBorder(new Color(220, 210, 200), 1));
        } else {
            btn.setBorder(null);
        }
        return btn;
    }
}