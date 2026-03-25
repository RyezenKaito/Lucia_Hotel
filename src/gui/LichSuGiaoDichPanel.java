package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LichSuGiaoDichPanel extends JPanel {

    public LichSuGiaoDichPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245,239,230));

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
    }

    // ===== HEADER =====
    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245,239,230));
        panel.setBorder(new EmptyBorder(20,30,10,30));

        JLabel title = new JLabel("Service History");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(92,50,44));
        title.setAlignmentX(Component.LEFT_ALIGNMENT); // 👈 FIX

        JLabel sub = new JLabel("8 service records · Total: 5.710.000 đ");
        sub.setFont(new Font("Serif", Font.PLAIN, 14));
        sub.setForeground(new Color(150,120,90));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT); // 👈 FIX

        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(15));

        JPanel summary = createSummary();
        summary.setAlignmentX(Component.LEFT_ALIGNMENT); // 👈 FIX
        panel.add(summary);

        return panel;
    }

    // ===== SUMMARY CARDS =====
    private JPanel createSummary() {
        JPanel panel = new JPanel(new GridLayout(1,4,15,0));
        panel.setOpaque(false);

        panel.add(createCard("Minibar", "2 records", "955.000 đ", new Color(220,210,200)));
        panel.add(createCard("Giặt ủi", "2 records", "485.000 đ", new Color(200,210,225)));
        panel.add(createCard("Spa", "2 records", "3.300.000 đ", new Color(200,220,200)));
        panel.add(createCard("Ăn tối", "2 records", "970.000 đ", new Color(230,210,180)));

        return panel;
    }

    private JPanel createCard(String title, String count, String price, Color tagColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,210,200)),
                new EmptyBorder(15,15,15,15)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel name = new JLabel(title);
        name.setOpaque(true);
        name.setBackground(tagColor);
        name.setBorder(new EmptyBorder(5,10,5,10));

        JLabel records = new JLabel(count);
        records.setForeground(new Color(150,120,90));

        top.add(name, BorderLayout.WEST);
        top.add(records, BorderLayout.EAST);

        JLabel money = new JLabel(price);
        money.setFont(new Font("Serif", Font.BOLD, 16));
        money.setBorder(new EmptyBorder(10,0,0,0));

        card.add(top);
        card.add(money);

        return card;
    }

    // ===== CENTER =====
    private JPanel createCenter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245,239,230));
        panel.setBorder(new EmptyBorder(10,30,20,30));

        panel.add(createFilter(), BorderLayout.NORTH);
        panel.add(createTable(), BorderLayout.CENTER);

        return panel;
    }

    // ===== SEARCH + FILTER =====
    private JPanel createFilter() {
        JPanel panel = new JPanel(new BorderLayout(10,0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,210,200)),
                new EmptyBorder(10,15,10,15)
        ));

        JTextField search = new JTextField("Search by room, guest name or booking ID...");
        search.setBorder(null);
        search.setForeground(Color.GRAY);

        panel.add(search, BorderLayout.CENTER);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        btnGroup.setOpaque(false);

        String[] filters = {"All", "Minibar", "Giặt ủi", "Spa", "Ăn tối"};

        for (int i = 0; i < filters.length; i++) {
            JButton btn = new JButton(filters[i]);
            btn.setFocusPainted(false);

            if (i == 0) {
                btn.setBackground(new Color(92,50,44));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(240,235,230));
            }

            btnGroup.add(btn);
        }

        panel.add(btnGroup, BorderLayout.EAST);

        return panel;
    }

    // ===== TABLE =====
    private JScrollPane createTable() {

        String[] cols = {"ID","ROOM","BOOKING","GUEST","TYPE","DESCRIPTION","QTY","UNIT PRICE","TOTAL","DATE & TIME"};

        Object[][] data = {
                {"SV001","101","BK001","Nguyễn Văn An","Minibar","Beer (330ml x2), Juice (250ml x1)","3","85.000 đ","255.000 đ","2026-03-15 21:30"},
                {"SV002","101","BK001","Nguyễn Văn An","Room Dining","Club Sandwich + Fried Rice","1","320.000 đ","320.000 đ","2026-03-15 12:45"},
                {"SV003","201","BK003","Lê Minh Tuấn","Laundry","Shirt x3, Trouser x2","5","45.000 đ","225.000 đ","2026-03-14 09:00"}
        };

        DefaultTableModel model = new DefaultTableModel(data, cols);
        JTable table = new JTable(model);

        table.setRowHeight(40);
        table.setFont(new Font("Serif", Font.PLAIN, 14));

        // header style
        table.getTableHeader().setBackground(new Color(92,50,44));
        table.getTableHeader().setForeground(new Color(230,180,120));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220,210,200)));

        return scroll;
    }
}