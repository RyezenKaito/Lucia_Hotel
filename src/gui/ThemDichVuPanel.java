package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ThemDichVuPanel extends JPanel {

    public ThemDichVuPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245,239,230));

        add(createHeader(), BorderLayout.NORTH);
        add(createMain(), BorderLayout.CENTER);
    }

    // ================= HEADER =================
    private JPanel createHeader(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20,30,10,20));
        panel.setBackground(new Color(245,239,230));

        JLabel title = new JLabel("Thêm dịch vụ vào phòng");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Chọn phòng và thêm dịch vụ vào hóa đơn");
        sub.setForeground(new Color(150,120,90));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(sub);

        return panel;
    }

    // ================= MAIN =================
    private JPanel createMain(){

        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(new EmptyBorder(15,15,15,15));
        main.setBackground(new Color(245,239,230));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,0,0,0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        // ===== LEFT =====
        gbc.gridx = 0;
        gbc.weightx = 0.8;
        main.add(createLeft(), gbc);

        // ===== RIGHT =====
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        main.add(createRight(), gbc);

        return main;
    }

    // ================= LEFT =================
    private JPanel createLeft(){

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(new Color(245,239,230));

        left.add(createRoomSelect());
        left.add(Box.createVerticalStrut(10));
        left.add(createTabs());
        left.add(createServiceList());

        return left;
    }

    // ================= ROOM SELECT =================
    private JPanel createRoomSelect(){

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,210,200)),
                new EmptyBorder(15,15,15,15)
        ));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Chọn phòng");
        title.setFont(new Font("Serif", Font.BOLD, 18));

        panel.add(title, BorderLayout.NORTH);

        JPanel rooms = new JPanel(new FlowLayout(FlowLayout.LEFT,15,10));
        rooms.setOpaque(false);

        rooms.add(roomCard("101","Single","An", true));
        rooms.add(roomCard("201","Double","Tuấn", false));
        rooms.add(roomCard("203","Deluxe","Nam", false));
        rooms.add(roomCard("302","Deluxe","Minh", false));
        rooms.add(roomCard("401","Suite","Long", false));

        panel.add(rooms, BorderLayout.CENTER);

        return panel;
    }

    private JPanel roomCard(String room, String type, String name, boolean active){

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(100,80));
        card.setBorder(new LineBorder(active ? new Color(92,50,44) : new Color(200,200,200),2));
        card.setBackground(new Color(250,250,250));

        JLabel r = new JLabel(room);
        r.setFont(new Font("Serif", Font.BOLD, 16));
        r.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel(type);
        t.setFont(new Font("Serif", Font.PLAIN, 12));
        t.setForeground(Color.GRAY);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel n = new JLabel(name);
        n.setFont(new Font("Serif", Font.PLAIN, 12));
        n.setForeground(new Color(150,80,50));
        n.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(r);
        card.add(t);
        card.add(n);
        card.add(Box.createVerticalGlue());

        return card;
    }

    // ================= TABS =================
    private JPanel createTabs(){

        JPanel tabs = new JPanel(new GridLayout(1,4));
        tabs.setBorder(new LineBorder(new Color(200,200,200)));

        tabs.add(tab("Minibar", true));
        tabs.add(tab("Laundry", false));
        tabs.add(tab("Spa", false));
        tabs.add(tab("Room Dining", false));

        return tabs;
    }

    private JPanel tab(String name, boolean active){

        JPanel t = new JPanel();
        t.setBackground(active ? new Color(92,50,44) : Color.WHITE);

        JLabel lbl = new JLabel(name);
        lbl.setForeground(active ? new Color(230,200,150) : new Color(120,90,60));
        lbl.setFont(new Font("Serif", Font.BOLD, 14));

        t.add(lbl);
        return t;
    }

    // ================= SERVICE LIST =================
    private JPanel createServiceList(){

        JPanel panel = new JPanel(new GridLayout(2,2,15,15));
        panel.setBorder(new EmptyBorder(15,0,0,0));
        panel.setBackground(new Color(245,239,230));

        panel.add(serviceCard("Local Beer (330ml)", "55.000 đ", true));
        panel.add(serviceCard("Imported Beer (330ml)", "95.000 đ", false));
        panel.add(serviceCard("Soft Drink (330ml)", "25.000 đ", false));
        panel.add(serviceCard("Wine (750ml)", "250.000 đ", false));

        return panel;
    }

    private JPanel serviceCard(String name, String price, boolean hasQty){

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new LineBorder(new Color(220,210,200)));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200,100));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(10,10,10,10));
        info.setOpaque(false);

        JLabel n = new JLabel(name);
        n.setFont(new Font("Serif", Font.BOLD, 14));

        JLabel p = new JLabel(price);
        p.setForeground(new Color(150,80,50));

        info.add(n);
        info.add(p);

        card.add(info, BorderLayout.CENTER);

        if(hasQty){
            JPanel qty = new JPanel();
            qty.add(new JButton("-"));
            qty.add(new JLabel("1"));
            qty.add(new JButton("+"));
            card.add(qty, BorderLayout.EAST);
        }else{
            JButton add = new JButton("+ Thêm");
            add.setBackground(new Color(190,155,90));
            card.add(add, BorderLayout.EAST);
        }

        return card;
    }

    // ================= RIGHT (BILL) =================
    private JPanel createRight(){

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setBackground(new Color(245,239,230));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(400, 320));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,210,200)),
                new EmptyBorder(20,20,20,20)
        ));

        // ===== TITLE =====
        JLabel title = new JLabel("Bill for Room 101");
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        // ===== ITEM =====
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel("Local Beer (330ml)");
        name.setFont(new Font("Serif", Font.BOLD, 14));

        JLabel price = new JLabel("55.000 đ");
        price.setFont(new Font("Serif", Font.BOLD, 14));

        item.add(name, BorderLayout.WEST);
        item.add(price, BorderLayout.EAST);

        panel.add(item);

        // ===== SUB TEXT =====
        JLabel sub = new JLabel("55.000 đ × 1");
        sub.setForeground(new Color(150,120,90));
        sub.setFont(new Font("Serif", Font.PLAIN, 12));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(sub);
        panel.add(Box.createVerticalStrut(10));

        // ===== LINE =====
        JSeparator line1 = new JSeparator();
        line1.setForeground(new Color(200,200,200));
        line1.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(line1);
        panel.add(Box.createVerticalStrut(10));

        // ===== LINE ĐẬM =====
        JSeparator line2 = new JSeparator();
        line2.setForeground(new Color(92,50,44));
        line2.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(line2);
        panel.add(Box.createVerticalStrut(15));

        // ===== TOTAL =====
        JPanel total = new JPanel(new BorderLayout());
        total.setOpaque(false);
        total.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel totalText = new JLabel("Total");
        totalText.setFont(new Font("Serif", Font.BOLD, 16));

        JLabel totalPrice = new JLabel("55.000 đ");
        totalPrice.setFont(new Font("Serif", Font.BOLD, 16));

        total.add(totalText, BorderLayout.WEST);
        total.add(totalPrice, BorderLayout.EAST);

        panel.add(total);
        panel.add(Box.createVerticalStrut(20));

        // ===== BUTTON ADD =====
        JButton add = new JButton("Thêm Vào Hóa Đơn");
        add.setBackground(new Color(92,50,44));
        add.setForeground(Color.WHITE);
        add.setFocusPainted(false);
        add.setBorder(new EmptyBorder(12,0,12,0));
        add.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        add.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ===== BUTTON CLEAR =====
        JButton clear = new JButton("Làm Mới");
        clear.setForeground(new Color(150,50,50));
        clear.setBackground(new Color(245, 245, 245));
        clear.setBorder(new LineBorder(new Color(224, 224, 224), 2));
        clear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        clear.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        Dimension btnSize = new Dimension(Integer.MAX_VALUE, 45);

	
	    add.setMaximumSize(btnSize);
	    add.setPreferredSize(new Dimension(300,45));
	    add.setBorder(new EmptyBorder(12,0,12,0));
	
	    // CLEAR
	    clear.setMaximumSize(btnSize);
	    clear.setPreferredSize(new Dimension(300,45));
	    clear.setBorder(new EmptyBorder(12,0,12,0));   

        panel.add(add);
        panel.add(Box.createVerticalStrut(10));
        panel.add(clear);

        wrapper.add(panel);

        return wrapper;
    }
}