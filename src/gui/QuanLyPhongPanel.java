package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class QuanLyPhongPanel extends JPanel {

    public QuanLyPhongPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(245, 240, 230)); // Màu nền kem nhạt
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý phòng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 30, 25));

        titleContainer.add(lblTitle);
        titleContainer.add(Box.createVerticalStrut(5));

        headerPanel.add(titleContainer, BorderLayout.WEST);

        // --- Tabs Navigation ---
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        tabPanel.setOpaque(false);
        tabPanel.add(createTabButton("Room Types", true));
        tabPanel.add(createTabButton("Room List", false));
        tabPanel.add(createTabButton("Amenities", false));

        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(tabPanel, BorderLayout.CENTER);
        topSection.add(new JSeparator(), BorderLayout.SOUTH);

        // --- Room Cards Grid ---
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 25, 25));
        gridPanel.setOpaque(false);

        gridPanel.add(createRoomTypeCard("Single", "Cozy single room with classic wooden furnishings", "1.200.000", "1", "5", "20%", "2", "1", new String[]{"101", "102", "105"}));
        gridPanel.add(createRoomTypeCard("Double", "Elegant double room with twin or queen bed", "1.800.000", "2", "6", "20%", "1", "1", new String[]{"103", "104", "201", "202", "205"}));
        gridPanel.add(createRoomTypeCard("Deluxe", "Spacious deluxe room with bathtub and city view", "2.800.000", "2", "5", "30%", "2", "2", new String[]{"203", "204", "301", "302", "305", "403", "404"}));
        gridPanel.add(createRoomTypeCard("Suite", "Luxurious suite with separate living room and jacuzzi", "4.500.000", "4", "4", "40%", "2", "1", new String[]{"303", "304", "401", "402", "405"}));

        // ScrollPane bọc Grid
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 15));
        btn.setForeground(active ? new Color(74, 45, 42) : new Color(150, 130, 110));
        btn.setContentAreaFilled(false);
        btn.setBorder(active ? new MatteBorder(0, 0, 3, 0, new Color(74, 45, 42)) : null);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createRoomTypeCard(String title, String desc, String price, String cap, String total, String weekend, String avail, String occ, String[] rooms) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(220, 210, 190), 1), new EmptyBorder(20, 25, 20, 25)));

        // Card Header (Title & Edit Button)
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblName = new JLabel(title);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(new Color(50, 30, 25));
        
        JButton btnEdit = new JButton("✎");
        btnEdit.setOpaque(false);
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorder(new LineBorder(new Color(200, 180, 150)));
        btnEdit.setBackground(new Color(245, 240, 230));

        head.add(lblName, BorderLayout.WEST);
        head.add(btnEdit, BorderLayout.EAST);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblDesc.setForeground(new Color(140, 120, 100));

        // Stats Box
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatItem("BASE PRICE", price + " đ/night"));
        statsPanel.add(createStatItem("CAPACITY", cap + " person(s)"));
        statsPanel.add(createStatItem("TOTAL ROOMS", total));
        statsPanel.add(createStatItem("WEEKEND +", weekend));
        statsPanel.add(createStatItem("AVAILABLE", avail));
        statsPanel.add(createStatItem("OCCUPIED", occ));

        // Rooms List
        JPanel roomFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        roomFlow.setOpaque(false);
        JLabel lblRoomTag = new JLabel("ROOMS ");
        lblRoomTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRoomTag.setForeground(new Color(180, 160, 140));
        
        JPanel roomsContainer = new JPanel(new BorderLayout());
        roomsContainer.setOpaque(false);
        roomsContainer.add(lblRoomTag, BorderLayout.NORTH);
        
        for (String r : rooms) {
            JLabel tag = new JLabel(r);
            tag.setOpaque(true);
            tag.setBackground(new Color(74, 110, 95)); // Màu mặc định như trong ảnh
            tag.setForeground(Color.WHITE);
            tag.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tag.setBorder(new EmptyBorder(5, 10, 5, 10));
            roomFlow.add(tag);
        }
        roomsContainer.add(roomFlow, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(lblDesc);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(statsPanel);

        card.add(head, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(roomsContainer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createStatItem(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(250, 248, 245));
        p.setBorder(new CompoundBorder(new LineBorder(new Color(235, 230, 220)), new EmptyBorder(8, 10, 8, 10)));
        
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblL.setForeground(new Color(180, 160, 140));
        
        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblV.setForeground(new Color(74, 45, 42));
        lblV.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(lblL, BorderLayout.NORTH);
        p.add(lblV, BorderLayout.CENTER);
        return p;
    }
    
//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Kiểm tra Giao diện Quản lý phòng");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1200, 800);
//            frame.setLocationRelativeTo(null);
//            QuanLyPhongPanel roomPanel = new QuanLyPhongPanel();
//            frame.add(roomPanel);
//            frame.setVisible(true);
//        });
//    }
}