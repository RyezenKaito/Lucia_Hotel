package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ChiTietPhongFrame extends JDialog {

    private final Color COLOR_TEXT_MAIN = new Color(50, 50, 50);
    private final Color COLOR_TEXT_SUB = new Color(160, 160, 160);
    private final Color COLOR_STATUS_GREEN = new Color(0, 190, 100); // Chỉnh màu xanh dịu hơn tí

    public ChiTietPhongFrame(JFrame parent, String maPhong, String loaiPhong, String tang, String trangThai) {
        super(parent, true);
        setUndecorated(true); 
        
        setSize(450, 320); // Tăng nhẹ kích thước để không gian thở (breathable)
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new EmptyBorder(15, 30, 30, 30)); // Căn chỉnh padding đều hơn

        // --- 1. NÚT ĐÓNG (FIX LỖI "KÌ") ---
        JPanel pnlTopControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlTopControl.setOpaque(false);
        
        // Tạo nút X với hiệu ứng vẽ vòng tròn bao quanh khi hover
        JButton btnClose = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ vòng tròn mờ phía sau icon khi hover (tùy chọn)
                if (getModel().isRollover()) {
                    g2.setColor(new Color(245, 245, 245));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                
                // Vẽ lại Icon
                super.paintComponent(g);
                g2.dispose();
            }
        };

        // Load và scale icon exit.png
        try {
            ImageIcon closeIcon = new ImageIcon(getClass().getResource("/icon/exit.png"));
            Image img = closeIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            btnClose.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btnClose.setText("✕"); // Fallback nếu không tìm thấy file icon
        }

        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.setFocusPainted(false);      
        btnClose.setContentAreaFilled(false); 
        btnClose.setBorderPainted(false);     
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlTopControl.add(btnClose);

        // --- 2. HEADER ---
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(0, 10, 20, 10));

        JLabel lblName = new JLabel("Chi tiết phòng " + maPhong);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblName.setForeground(COLOR_TEXT_MAIN);
        
        JLabel lblSub = new JLabel("Thông tin chi tiết về phòng");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(COLOR_TEXT_SUB);
        
        pnlHeader.add(lblName);
        pnlHeader.add(lblSub);

        // --- 3. BODY CONTENT ---
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        addRow(pnlBody, "Loại phòng:", loaiPhong, gbc, 0, false);
        addRow(pnlBody, "Tầng:", tang, gbc, 1, false);
        addRow(pnlBody, "Trạng thái:", trangThai, gbc, 2, true);

        pnlMain.add(pnlTopControl); 
        pnlMain.add(pnlHeader);
        pnlMain.add(pnlBody);

        add(pnlMain);
    }

    private void addRow(JPanel pnl, String label, String value, GridBagConstraints gbc, int row, boolean isStatus) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setForeground(COLOR_TEXT_SUB);
        pnl.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        if (isStatus) {
            JLabel lblStatus = new JLabel(value, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_STATUS_GREEN);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };
            lblStatus.setForeground(Color.WHITE);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblStatus.setPreferredSize(new Dimension(85, 26)); 
            
            JPanel pWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pWrapper.setOpaque(false); pWrapper.add(lblStatus);
            pnl.add(pWrapper, gbc);
        } else {
            JLabel lblVal = new JLabel(value, SwingConstants.RIGHT);
            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblVal.setForeground(COLOR_TEXT_MAIN);
            pnl.add(lblVal, gbc);
        }
    }

//    public static void main(String[] args) {
//        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
//        SwingUtilities.invokeLater(() -> {
//            ChiTietPhongFrame dialog = new ChiTietPhongFrame(null, "103", "Phòng Deluxe", "Tầng 1", "Trống");
//            dialog.setVisible(true);
//        });
//    }
}