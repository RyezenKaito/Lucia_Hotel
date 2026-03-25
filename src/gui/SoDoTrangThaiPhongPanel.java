package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SoDoTrangThaiPhongPanel extends JPanel {

    private final int DAYS = 7;
    private final int CELL_WIDTH = 100;
    private final int CELL_HEIGHT = 45;
    private final LocalDate dateNow = LocalDate.now();
    private final DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("EEEE\n dd/MM/yyyy");

    public SoDoTrangThaiPhongPanel() {
        init();
    }
    
    private void init() {
    	setLayout(new BorderLayout());
        setBackground(new Color(245,239,230));

        add(createHeader(), BorderLayout.NORTH);
        add(createTimeline(), BorderLayout.CENTER);
	}

    // ================= HEADER =================
    private JPanel createHeader(){

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(245,239,230));
        header.setBorder(new EmptyBorder(20,20,10,20));

        JLabel title = new JLabel("Lịch Trình Phòng");
        title.setFont(new Font("Serif",Font.BOLD,26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT); 

        JLabel sub = new JLabel("Gantt-style room availability and booking schedule.");
        sub.setForeground(new Color(150,120,90));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT); // ✅ FIX

        JPanel toolbar = createToolbar();
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT); // ✅ FIX

        header.add(title);
        header.add(sub);
        header.add(Box.createVerticalStrut(10));
        header.add(toolbar);

        return header;
    }

    // ================= TOOLBAR =================
    private JPanel createToolbar(){

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);

        left.add(new JButton("‹ Tiếp"));

        JButton today = new JButton("Hôm nay");
        today.setBackground(new Color(92,50,44));
        today.setForeground(Color.WHITE);
        left.add(today);

        left.add(new JButton("Trở về›"));

        JLabel date = new JLabel("14 March 2026 – 27 March 2026");
        left.add(Box.createHorizontalStrut(20));
        left.add(date);

        panel.add(left, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        legend.setOpaque(false);

        legend.add(legendItem("Booked", new Color(190,155,90)));
        legend.add(legendItem("Occupied", new Color(165,55,55)));
        legend.add(legendItem("Pending", new Color(200,200,200)));
        legend.add(legendItem("Available", new Color(120,150,120)));

        panel.add(legend, BorderLayout.EAST);

        return panel;
    }

    private JPanel legendItem(String name, Color color){

        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        item.setOpaque(false);

        JLabel box = new JLabel();
        box.setOpaque(true);
        box.setBackground(color);
        box.setPreferredSize(new Dimension(15,15));

        JLabel text = new JLabel(name);

        item.add(box);
        item.add(text);

        return item;
    }

    // ================= TIMELINE =================
    private JScrollPane createTimeline(){

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // ✅ FIX
        panel.setBackground(Color.WHITE);

        panel.add(createHeaderRow());

        panel.add(createFloor("TẦNG 1", new String[]{
                "101-Single","102-Single","103-Double","104-Double","105-Single"
        }));

        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);

        return scroll;
    }

    // ================= HEADER ROW =================
    private JPanel createHeaderRow(){

        JPanel row = new JPanel(new GridLayout(1, DAYS + 1));

        row.setPreferredSize(new Dimension(0, CELL_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, CELL_HEIGHT));

        row.add(headerCell("PHÒNG"));

        for(int i=0;i<7;i++){
        	LocalDate date = dateNow.plusDays(i);
        	String label = date.format(dtfDate);
            row.add(headerCell(label));
        }

        return row;
    }

    private JLabel headerCell(String text){

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(92,50,44));
        lbl.setForeground(new Color(230,200,150));
        lbl.setFont(new Font("Serif",Font.BOLD,13));
        lbl.setBorder(BorderFactory.createLineBorder(new Color(70,40,35)));

        return lbl;
    }

    // ================= FLOOR =================
    private JPanel createFloor(String title, String[] rooms){

        JPanel floor = new JPanel();
        floor.setLayout(new BoxLayout(floor, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(title, SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(floor.getWidth(), 30));
        label.setOpaque(true);
        label.setBackground(new Color(245,245,245));
        label.setForeground(new Color(150,120,90));
        label.setBorder(new EmptyBorder(5,10,5,10));

        floor.add(label);

        for(String r : rooms){
            String[] p = r.split("-");
            floor.add(createRoomRow(p[0], p[1]));
        }

        return floor;
    }

    // ================= ROOM ROW =================
    private JPanel createRoomRow(String room, String type){

        JPanel row = new JPanel(new GridLayout(1, DAYS + 1));
        row.setPreferredSize(new Dimension(0, CELL_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, CELL_HEIGHT));

        JPanel info = new JPanel(new GridLayout(2,1));
        info.setBackground(new Color(245,245,245));
        info.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        JLabel r = new JLabel(room);
        r.setFont(new Font("Serif",Font.BOLD,14));

        JLabel t = new JLabel(type);
        t.setFont(new Font("Serif",Font.PLAIN,11));
        t.setForeground(Color.GRAY);

        info.add(r);
        info.add(t);

        row.add(info);

        for(int i = 0; i < DAYS; i++){

            JPanel cell = new JPanel(new BorderLayout());
            cell.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));

            if(room.equals("101") && i < 3){
                cell.setBackground(new Color(165,55,55));
                cell.add(createText("An"));

            } else if(room.equals("103") && i >= 2 && i < 5){
                cell.setBackground(new Color(190,155,90));
                cell.add(createText("Bích"));

            } else {
                cell.setBackground(new Color(220,225,220));
            }

            row.add(cell);
        }

        return row;
    }

    private JLabel createText(String text){
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }
}