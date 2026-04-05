package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dao.*;
import model.entities.Phong;

public class TrangChuPanel extends JPanel {
	private PhongDAO phongDAO = new PhongDAO();

    public TrangChuPanel() {
        setLayout(new BorderLayout(25, 25));
        setBackground(new Color(248, 249, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Phần thống kê (Thẻ Modern)
        JPanel stats = new JPanel(new GridLayout(1, 4, 25, 0));
        stats.setOpaque(false);
        stats.add(createStatCard("TỔNG PHÒNG", "--", "🏠", new Color(50, 30, 28)));
        stats.add(createStatCard("ĐÃ CÓ KHÁCH", "--", "👥", new Color(165, 55, 55)));
        stats.add(createStatCard("PHÒNG TRỐNG", "--", "✨", new Color(66, 130, 90)));
        stats.add(createStatCard("DOANH THU", "--", "💰", new Color(212, 175, 55)));

        // Phần nội dung sơ đồ phòng
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(new LineBorder(new Color(235, 235, 235), 1, true));
        
        JScrollPane scrollGrid = new JScrollPane(roomGrid());
        scrollGrid.setBorder(null);
        center.add(scrollGrid, BorderLayout.CENTER);

        add(stats, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, String icon, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(235, 235, 235), 1, true), new EmptyBorder(20, 20, 20, 20)));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblIcon.setForeground(accentColor);
        card.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(new Color(140, 140, 140));

        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblV.setForeground(new Color(45, 45, 45));

        textPanel.add(lblT);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblV);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel roomGrid() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        loadRoomData(panel);
        return panel;
    }

    private JPanel createFloor(String title, String[] rooms) {
        JPanel floor = new JPanel(new BorderLayout());
        floor.setOpaque(false);
        floor.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(100, 100, 100));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        row.setOpaque(false);

        for (String r : rooms) {
            String[] p = r.split("-");
            row.add(roomCard(p[0], "Phòng đơn", p[1], 350000));
        }

        floor.add(lbl, BorderLayout.NORTH);
        floor.add(row, BorderLayout.CENTER);
        return floor;
    }
    
    private void loadRoomData(JPanel panel) {
		List<Phong> list = phongDAO.getAll();
		Map<Integer, List<Phong>> roomsByFloor = new TreeMap<>();
	    for (Phong p : list) {
	        int floorNum = p.getSoTang();
	        roomsByFloor.computeIfAbsent(floorNum, k -> new ArrayList<>()).add(p);
	    }
	    
	    for (Map.Entry<Integer, List<Phong>> entry : roomsByFloor.entrySet()) {
	        int floorNum = entry.getKey();
	        List<Phong> roomsInFloor = entry.getValue();
	        String[] roomStrings = new String[roomsInFloor.size()];
	        for (int i = 0; i < roomsInFloor.size(); i++) {
	            Phong p = roomsInFloor.get(i);
	            roomStrings[i] = p.getMaPhong() + "-" + p.getTrangThai();
	        }

	        panel.add(createFloor("TẦNG " + floorNum, roomStrings));
	    }
	}

    private JPanel roomCard(String name, String type, String status, double price) {
        Color colorTop, colorBottom;
        switch (status) {
            case "Trống": colorTop = new Color(85, 170, 110); colorBottom = new Color(60, 130, 90); break;
            case "Bẩn": colorTop = new Color(200, 80, 80); colorBottom = new Color(165, 55, 55); break;
            case "Đang ở": colorTop = new Color(220, 180, 100); colorBottom = new Color(190, 155, 90); break;
            default: colorTop = new Color(160, 160, 160); colorBottom = new Color(120, 120, 120); break;
        }

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.setPaint(new GradientPaint(0, 0, colorTop, 0, getHeight(), colorBottom));
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.dispose();
            }
        };
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // Nhấp đúp chuột
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(TrangChuPanel.this);
                    String tang = "Tầng " + name.substring(1, 2); 
                   
                    ChiTietPhongFrame dialog = new ChiTietPhongFrame(parentFrame,name, type, tang, status);
                    dialog.setVisible(true);
                }
            }

            // Hiệu ứng đổi con trỏ chuột khi rê vào để người dùng biết có thể nhấn
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        card.setPreferredSize(new Dimension(160, 100));
        card.setLayout(new GridLayout(4, 1));
        card.setBorder(new EmptyBorder(10, 15, 10, 15));
        card.setOpaque(false);

        JLabel lblN = new JLabel(name); lblN.setForeground(Color.WHITE); lblN.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lblT = new JLabel(type); lblT.setForeground(new Color(255, 255, 255, 200)); lblT.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel lblP = new JLabel(String.format("%,.0f đ", price)); lblP.setForeground(Color.WHITE); lblP.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblS = new JLabel(status); lblS.setForeground(Color.WHITE); lblS.setFont(new Font("Segoe UI", Font.PLAIN, 10)); lblS.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblN); card.add(lblT); card.add(lblP); card.add(lblS);
        return card;
    }
}