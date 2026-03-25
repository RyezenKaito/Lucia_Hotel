package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class DanhSachDatPhongPanel extends JPanel {

    public DanhSachDatPhongPanel() {

        setLayout(new BorderLayout(15,15));
        setBackground(new Color(245,241,234));
        setBorder(new EmptyBorder(20,20,20,20));

        add(createTopPanel(),BorderLayout.NORTH);
        add(createTable(),BorderLayout.CENTER);
    }

    private JPanel createTopPanel(){

        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(new Color(245,241,234));

        JTextField search = new JTextField("Search by name, booking ID or phone...");
        search.setPreferredSize(new Dimension(350,40));
        search.setFont(new Font("Segoe UI",Font.PLAIN,14));
        search.setBorder(new LineBorder(new Color(200,200,200)));

        panel.add(search,BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        filterPanel.setOpaque(false);

        String[] filters={
                "All","Pending","Confirmed","Checked In","Checked Out","Cancelled"
        };

        for(String f:filters){

            JButton btn = new JButton(f);
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setBorder(new LineBorder(new Color(210,210,210)));
            btn.setPreferredSize(new Dimension(110,35));

            if(f.equals("All")){
                btn.setBackground(new Color(90,55,45));
                btn.setForeground(Color.WHITE);
            }

            filterPanel.add(btn);
        }

        panel.add(filterPanel,BorderLayout.EAST);

        return panel;
    }

    private JScrollPane createTable(){

        String[] columns={
                "BOOKING ID","GUEST NAME","PHONE","ROOM",
                "CHECK-IN","CHECK-OUT","GUESTS","TOTAL","STATUS","ACTIONS"
        };

        Object[][] data={
                {"BK001","Nguyễn Văn An","0901234567","Single #101",
                        "2026-03-14","2026-03-17","1","3.600.000 đ","Checked In",""},
                {"BK002","Trần Thị Bích","0912345678","Double #103",
                        "2026-03-16","2026-03-19","2","5.400.000 đ","Confirmed",""},
                {"BK003","Lê Minh Tuấn","0923456789","Double #201",
                        "2026-03-13","2026-03-16","2","5.400.000 đ","Checked In",""},
                {"BK004","Phạm Hoài Nam","0934567890","Deluxe #203",
                        "2026-03-15","2026-03-18","2","8.400.000 đ","Checked In",""}
        };

        JTable table = new JTable(data,columns);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI",Font.PLAIN,14));

        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,14));
        table.getTableHeader().setBackground(new Color(95,55,45));
        table.getTableHeader().setForeground(new Color(220,180,120));
        table.getTableHeader().setPreferredSize(new Dimension(0,40));

        table.setGridColor(new Color(230,230,230));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(6).setCellRenderer(center);
        table.getColumnModel().getColumn(7).setCellRenderer(center);

        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(210,210,210)));

        // Giới hạn chiều cao 10 dòng
        int rowHeight = table.getRowHeight();
        int headerHeight = table.getTableHeader().getPreferredSize().height;
        int maxRows = 10;

        int tableHeight = (rowHeight * maxRows) + headerHeight;

        scroll.setPreferredSize(new Dimension(0, tableHeight));

        return scroll;
    }
}
