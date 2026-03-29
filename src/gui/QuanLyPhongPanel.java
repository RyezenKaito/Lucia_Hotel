package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import dao.PhongDAO;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import model.entities.Phong;
import model.enums.TrangThaiPhong;

public class QuanLyPhongPanel extends JPanel {
    private PhongDAO phongDAO = new PhongDAO();
    private DefaultTableModel dtmPhong;
    private JTable tblPhong;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private JLabel lblValTongPhong, lblValPhongTrong, lblValCoKhach, lblValBaoTri;
    
    private final Color C_BG = new Color(245, 241, 234);
    private final Color C_BROWN_DARK = new Color(60, 40, 35);
    
    private boolean isAdmin;

    public QuanLyPhongPanel(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout());
        setBackground(C_BG);
        setBorder(new EmptyBorder(15, 30, 15, 30)); 

        add(createHeader(), BorderLayout.NORTH);
        add(createTableArea(), BorderLayout.CENTER);
        
        initData(); 
    }

    public JPanel createHeader() {
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Danh sách phòng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28)); 
        lblTitle.setForeground(C_BROWN_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubTitle = new JLabel("Quản lý và theo dõi trạng thái danh sách phòng");
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubTitle.setForeground(new Color(120, 110, 100));
        lblSubTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2)); 
        titlePanel.add(lblSubTitle);

        lblValTongPhong = new JLabel("0");
        lblValPhongTrong = new JLabel("0");
        lblValCoKhach = new JLabel("0");
        lblValBaoTri = new JLabel("0");

        JPanel cardRow = new JPanel(new GridLayout(1, 4, 15, 0));
        cardRow.setOpaque(false);
        cardRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardRow.setBorder(new EmptyBorder(20, 0, 20, 0));

        cardRow.add(createStatCard("TỔNG SỐ PHÒNG", lblValTongPhong, new Color(50, 50, 50)));
        cardRow.add(createStatCard("PHÒNG TRỐNG", lblValPhongTrong, new Color(40, 110, 70)));
        cardRow.add(createStatCard("ĐANG CÓ KHÁCH", lblValCoKhach, new Color(180, 100, 40)));
        cardRow.add(createStatCard("ĐANG BẢO TRÌ", lblValBaoTri, new Color(150, 50, 50)));

        JPanel actionRow = new JPanel(new BorderLayout(20, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setBorder(new EmptyBorder(0, 0, 15, 0));

        RoundedPanel searchBox = new RoundedPanel(30); 
        searchBox.setLayout(new BorderLayout(10, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(new EmptyBorder(8, 20, 8, 20));

        JLabel lblSearchIcon = new JLabel("Tìm kiếm:"); 
        lblSearchIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearchIcon.setForeground(new Color(100, 100, 100));
        
        JTextField txtSearch = new JTextField("Nhập mã phòng hoặc loại phòng...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        txtSearch.setForeground(Color.GRAY);

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().startsWith("Nhập")) {
                    txtSearch.setText(""); txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtSearch.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText("Nhập mã phòng hoặc loại phòng..."); txtSearch.setFont(new Font("Segoe UI", Font.ITALIC, 14)); txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.startsWith("Nhập") || text.trim().isEmpty()) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim()));
            }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        if (isAdmin) {
            JButton btnAdd = createStyledButton("+ Thêm mới", C_BROWN_DARK);
            btnAdd.addActionListener(e -> new PhongDialog(this).setVisible(true));
            pnlButtons.add(btnAdd);
        }

        actionRow.add(searchBox, BorderLayout.CENTER);
        actionRow.add(pnlButtons, BorderLayout.EAST);

        pnlMain.add(titlePanel);
        pnlMain.add(cardRow);
        pnlMain.add(actionRow);

        return pnlMain;
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(baseColor.darker());
                else if (getModel().isRollover()) g2.setColor(baseColor.brighter());
                else g2.setColor(baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(130, 42));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color valueColor) {
        RoundedPanel card = new RoundedPanel(20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(15, 22, 15, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(160, 150, 140));

        lblValue.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblValue.setForeground(valueColor);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(lblValue);
        return card;
    }

    private JScrollPane createTableArea() {
        String[] columns = {"STT", "Mã phòng", "Loại phòng", "Đơn giá", "Sức chứa", "Trạng thái", "Tầng"};
        dtmPhong = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhong = new JTable(dtmPhong);
        rowSorter = new TableRowSorter<>(dtmPhong);
        tblPhong.setRowSorter(rowSorter);

        tblPhong.setRowHeight(45);
        tblPhong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblPhong.setSelectionBackground(new Color(235, 230, 220));
        tblPhong.setShowVerticalLines(false);
        tblPhong.setGridColor(new Color(240, 240, 240));

        JTableHeader header = tblPhong.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(C_BROWN_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 45));
        
        header.setReorderingAllowed(false); 
        header.setResizingAllowed(false);   

        tblPhong.getColumnModel().getColumn(0).setPreferredWidth(50);  
        tblPhong.getColumnModel().getColumn(1).setPreferredWidth(100); 
        tblPhong.getColumnModel().getColumn(2).setPreferredWidth(150); 
        tblPhong.getColumnModel().getColumn(3).setPreferredWidth(150); 
        tblPhong.getColumnModel().getColumn(4).setPreferredWidth(100); 
        tblPhong.getColumnModel().getColumn(5).setPreferredWidth(150); 
        tblPhong.getColumnModel().getColumn(6).setPreferredWidth(80);  

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<columns.length; i++) {
            if(i != 2) tblPhong.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        tblPhong.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                if (value != null) {
                    String s = value.toString();
                    if (s.equals("Còn trống")) c.setForeground(new Color(40, 150, 70));
                    else if (s.equals("Đã có khách")) c.setForeground(new Color(200, 60, 40));
                    else c.setForeground(Color.GRAY);
                }
                return c;
            }
        });

        if (isAdmin) {
            setupPopupMenu(); 
        }
        
        JScrollPane scroll = new JScrollPane(tblPhong);
        scroll.setBorder(new LineBorder(new Color(225, 220, 210)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(4, 4, 4, 4) 
        ));
        popup.setBackground(Color.WHITE);

        JMenuItem mnuSua = createModernMenuItem("Chỉnh sửa phòng", new Color(227, 242, 253), new Color(25, 118, 210));
        JMenuItem mnuXoa = createModernMenuItem("Xóa phòng", new Color(255, 235, 238), new Color(211, 47, 47));

        mnuSua.addActionListener(e -> actionEditRoom());
        mnuXoa.addActionListener(e -> actionDeleteRoom());

        popup.add(mnuSua); 
        
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230));
        popup.add(sep);
        
        popup.add(mnuXoa);
        
        tblPhong.setComponentPopupMenu(popup);
    }

    private JMenuItem createModernMenuItem(String text, Color hoverBgColor, Color hoverFgColor) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.BOLD, 14));
        item.setBackground(Color.WHITE);
        item.setForeground(new Color(70, 70, 70)); 
        item.setPreferredSize(new Dimension(200, 40)); 
        item.setBorder(new EmptyBorder(0, 15, 0, 15));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        
        item.addChangeListener(e -> {
            if (item.isArmed()) {
                item.setBackground(hoverBgColor);
                item.setForeground(hoverFgColor);
            } else {
                item.setBackground(Color.WHITE);
                item.setForeground(new Color(70, 70, 70));
            }
        });
        return item;
    }

    private void actionEditRoom() {
        int r = tblPhong.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng trong bảng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int m = tblPhong.convertRowIndexToModel(r);
        new PhongDialog(this, 
            dtmPhong.getValueAt(m,1).toString(), 
            dtmPhong.getValueAt(m,2).toString(), 
            dtmPhong.getValueAt(m,5).toString(), 
            Integer.parseInt(dtmPhong.getValueAt(m,6).toString())
        ).setVisible(true);
    }

    private void actionDeleteRoom() {
        int r = tblPhong.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng trong bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tblPhong.convertRowIndexToModel(r);
        String ma = dtmPhong.getValueAt(modelRow, 1).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phòng [" + ma + "] không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (phongDAO.delete(ma)) {
                JOptionPane.showMessageDialog(this, "Xóa phòng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                initData(); 
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại! Phòng này có thể đang tồn tại hóa đơn hoặc dữ liệu liên quan.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void initData() {
        dtmPhong.setRowCount(0);
        List<Phong> ds = phongDAO.getAll();
        int tr=0, ck=0, bt=0, stt=1;
        if (ds != null) {
            for (Phong p : ds) {
                String s = "";
                if (p.getTrangThai() == TrangThaiPhong.CONTRONG) { s = "Còn trống"; tr++; }
                else if (p.getTrangThai() == TrangThaiPhong.DACOKHACH) { s = "Đã có khách"; ck++; }
                else { s = "Đang bảo trì"; bt++; }
                
                String loai = p.getLoaiPhong().getTenLoaiPhong().toString();
                double gia = p.getLoaiPhong().getDonGia();
                int sucChua = p.getLoaiPhong().getSucChua();
                
                dtmPhong.addRow(new Object[]{
                    stt++, 
                    p.getMaPhong(), 
                    loai, 
                    String.format("%,.0f đ", gia), 
                    sucChua + " người", 
                    s, 
                    p.getSoTang()
                });
            }
        }
        lblValTongPhong.setText(String.valueOf(ds != null ? ds.size() : 0));
        lblValPhongTrong.setText(String.valueOf(tr));
        lblValCoKhach.setText(String.valueOf(ck));
        lblValBaoTri.setText(String.valueOf(bt));
    }
}

class RoundedPanel extends JPanel {
    private int radius;
    public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.setColor(new Color(225, 220, 210));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
    }
}