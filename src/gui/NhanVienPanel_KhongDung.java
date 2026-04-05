package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.time.format.DateTimeFormatter;

import dao.NhanVienDAO;
import model.entities.NhanVien;
import model.enums.ChucVu;

public class NhanVienPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private DefaultTableModel dtmNhanVien;
    private JTable tblNhanVien;
    private JTextField txtSearch;
    private JLabel lblTotalCount, lblReceptionistCount, lblManagerCount;
    private final boolean isAdmin;

    public NhanVienPanel(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(248, 248, 250));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        add(createHeaderSection(), BorderLayout.NORTH);
        add(createTableContainer(), BorderLayout.CENTER);

        initData();
    }

    // --- 1. HEADER SECTION ---
    private JPanel createHeaderSection() {
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        // Hàng 1: Title (Trái) - Button Thêm (Phải)
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách Nhân Viên");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 32));
        lblTitle.setForeground(new Color(60, 40, 30));
        row1.add(lblTitle, BorderLayout.WEST);

        if (isAdmin) {
            RoundedButton btnAdd = new RoundedButton("+ Thêm nhân viên", 15);
            btnAdd.setBackground(new Color(70, 45, 40));
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setPreferredSize(new Dimension(160, 40));
            btnAdd.addActionListener(e -> showFormDialog(null));
            row1.add(btnAdd, BorderLayout.EAST);
        }

        // Hàng 2: Search (Trái) - Stats (Phải)
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(15, 0, 10, 0));

        JPanel searchWrapper = new JPanel(new BorderLayout(10, 0));
        searchWrapper.setBackground(Color.WHITE);
        searchWrapper.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 225, 225), 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        searchWrapper.setPreferredSize(new Dimension(380, 45));

        JLabel lblIcon = new JLabel("🔍");

        String placeholder = "Tìm theo mã hoặc tên...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtSearch.setForeground(Color.GRAY);

        // Focus vào thì xóa placeholder
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(placeholder);
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (txtSearch.getText().equals(placeholder)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtSearch.getText().equals(placeholder)) {
                    performSearch(txtSearch.getText().trim());
                }
            }
        });


        searchWrapper.add(lblIcon, BorderLayout.WEST);
        searchWrapper.add(txtSearch, BorderLayout.CENTER);
        row2.add(searchWrapper, BorderLayout.WEST);

        JPanel pnlStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlStats.setOpaque(false);
        lblTotalCount = new JLabel("0", JLabel.CENTER);
        lblReceptionistCount = new JLabel("0", JLabel.CENTER);
        lblManagerCount = new JLabel("0", JLabel.CENTER);

        pnlStats.add(new StatCard("Tổng", lblTotalCount, Color.BLACK));
        pnlStats.add(new StatCard("Lễ tân", lblReceptionistCount, new Color(0, 102, 204)));
        pnlStats.add(new StatCard("Quản lý", lblManagerCount, new Color(204, 102, 0)));
        row2.add(pnlStats, BorderLayout.EAST);

        pnlHeader.add(row1);
        pnlHeader.add(row2);
        return pnlHeader;
    }

    // --- 2. LOGIC TÌM KIẾM VÀ CẬP NHẬT BẢNG ---
    private void performSearch(String keyword) {
        List<NhanVien> list = nhanVienDAO.findByKeyword(keyword); 
        updateTableData(list);
    }

    private void updateTableData(List<NhanVien> ds) {
        dtmNhanVien.setRowCount(0);
        if (ds == null) return;
        int stt = 1;
        for (NhanVien nv : ds) {
            String ngayVao = (nv.getNgayVaoLamDate() != null) ? nv.getNgayVaoLamDate().format(FMT) : "";
            String chucVuStr = (nv.getRole() == ChucVu.QUAN_LY) ? "Quản lý" : "Nhân viên";
            
            // QUAN TRỌNG: Cần gán nv.getSoDT() vào đúng cột số 3 (index 3)
            Object[] row;
            if (isAdmin) {
                row = new Object[]{
                    stt++, 
                    nv.getMaNV(), 
                    nv.getHoTen(), 
                    nv.getSoDT(),       // HIỆN SĐT TẠI ĐÂY
                    ngayVao, 
                    nv.getHeSoLuong(), 
                    chucVuStr, 
                    "" // Cột Thao tác (Renderer sẽ vẽ nút Xóa)
                };
            } else {
                row = new Object[]{
                    stt++, 
                    nv.getMaNV(), 
                    nv.getHoTen(), 
                    nv.getSoDT(),       // HIỆN SĐT TẠI ĐÂY
                    ngayVao, 
                    nv.getHeSoLuong(), 
                    chucVuStr
                };
            }
            dtmNhanVien.addRow(row);
        }
    }

    // --- 3. BẢNG DỮ LIỆU ---
    private JScrollPane createTableContainer() {
        String[] cols = isAdmin 
            ? new String[]{"STT", "Mã NV", "Họ tên", "SĐT", "Ngày vào", "Hệ số", "Chức vụ", "Thao tác"}
            : new String[]{"STT", "Mã NV", "Họ tên", "SĐT", "Ngày vào", "Hệ số", "Chức vụ"};

        dtmNhanVien = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return isAdmin && c == 7; }
        };

        tblNhanVien = new JTable(dtmNhanVien);
        
        tblNhanVien.setRowHeight(52);
        tblNhanVien.setShowGrid(false);
        tblNhanVien.setShowHorizontalLines(true);
        tblNhanVien.setGridColor(new Color(235, 235, 235));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
                return c;
            }
        };
        tblNhanVien.setDefaultRenderer(Object.class, centerRenderer);

        tblNhanVien.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int row = tblNhanVien.getSelectedRow();
                int col = tblNhanVien.getSelectedColumn();

                if (row == -1) return;

                // ❌ Nếu click vào cột "Thao tác" (nút Xóa) thì bỏ qua
                if (isAdmin && col == 7) return;

                // ❌ Nếu không phải admin → chặn luôn
                if (!isAdmin) {
                    JOptionPane.showMessageDialog(null, "Bạn không có quyền chỉnh sửa!");
                    return;
                }

                // Lấy dữ liệu nhân viên
                String maNV = tblNhanVien.getValueAt(row, 1).toString();
                NhanVien nv = nhanVienDAO.getById(maNV);

                // ❌ (OPTION) Nếu bạn muốn chỉ cho sửa quản lý thì bật cái này
                /*
                if (nv.getRole() == ChucVu.NHAN_VIEN) {
                    JOptionPane.showMessageDialog(null, "Không được sửa nhân viên!");
                    return;
                }
                */

                // ✅ Cho phép sửa
                showFormDialog(nv);
            }
        });



        if (isAdmin) {
            TableColumn actionCol = tblNhanVien.getColumnModel().getColumn(7);
            actionCol.setCellRenderer(new DeleteButtonRenderer());
            actionCol.setCellEditor(new DeleteButtonEditor(this));
        }

        JTableHeader header = tblNhanVien.getTableHeader();
        header.setBackground(new Color(85, 55, 45));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));
        
        // KHÓA CỘT
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        tblNhanVien.setDragEnabled(false);
        tblNhanVien.setAutoCreateColumnsFromModel(false);
        JScrollPane scrollPane = new JScrollPane(tblNhanVien);
        scrollPane.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    public void initData() {
        List<NhanVien> ds = nhanVienDAO.getAll();
        updateTableData(ds);
        if (ds != null) {
            long qlCount = ds.stream().filter(n -> n.getRole() == ChucVu.QUAN_LY).count();
            lblTotalCount.setText(String.valueOf(ds.size()));
            lblManagerCount.setText(String.valueOf(qlCount));
            lblReceptionistCount.setText(String.valueOf(ds.size() - qlCount));
        }
    }

    public void showFormDialog(NhanVien nv) {
        new NhanVienForm((JFrame) SwingUtilities.getWindowAncestor(this), nv).setVisible(true);
        initData(); // Reload lại bảng sau khi đóng Form
    }

    public void confirmDelete(String ma, String ten) {

        NhanVien nv = nhanVienDAO.getById(ma);

        // ❌ Không cho xóa quản lý
        if (nv.getRole() == ChucVu.QUAN_LY) {
            JOptionPane.showMessageDialog(this, "Không được xóa quản lý!");
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa nhân viên: " + ten + "?",
                "Cảnh báo",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            nhanVienDAO.delete(ma);
            initData();
        }
    }


    // --- INNER CLASS: THẺ THỐNG KÊ ---
    private class StatCard extends JPanel {
        public StatCard(String title, JLabel count, Color c) {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(130, 55));
            setBackground(Color.WHITE);
            setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true), 
                new EmptyBorder(8, 12, 8, 12)
            ));
            
            JLabel t = new JLabel(title); 
            t.setFont(new Font("SansSerif", Font.PLAIN, 12));
            t.setForeground(Color.GRAY);
            
            count.setFont(new Font("SansSerif", Font.BOLD, 22));
            count.setForeground(c);
            
            add(t, BorderLayout.NORTH); 
            add(count, BorderLayout.CENTER);
        }
    }
}

// --- CÁC CLASS HỖ TRỢ BUTTON TRONG BẢNG ---

class DeleteButtonRenderer extends JPanel implements TableCellRenderer {

    public DeleteButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        String chucVu = table.getValueAt(row, 6).toString();

        // ❌ Nếu là quản lý → trả về ô trống hoàn toàn
        if (chucVu.equals("Quản lý")) {
            JPanel empty = new JPanel();
            empty.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return empty;
        }

        // ✅ Nhân viên → có nút Xóa
        this.removeAll();
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        RoundedButton btn = new RoundedButton("Xóa", 10);
        btn.setPreferredSize(new Dimension(75, 30));
        btn.setBackground(new Color(220, 70, 70));
        btn.setForeground(Color.WHITE);

        add(btn);
        return this;
    }
}


class DeleteButtonEditor extends AbstractCellEditor implements TableCellEditor {

    private JPanel panel;
    private String ma, ten;
    private NhanVienPanel parent;

    public DeleteButtonEditor(NhanVienPanel p) {
        this.parent = p;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        panel.setOpaque(true);

        RoundedButton btn = new RoundedButton("Xóa", 10);
        btn.setPreferredSize(new Dimension(75, 30));
        btn.setBackground(new Color(220, 70, 70));
        btn.setForeground(Color.WHITE);

        btn.addActionListener(e -> {
            fireEditingStopped();
            parent.confirmDelete(ma, ten);
        });

        panel.add(btn);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));
    }

    @Override
    public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {

        String chucVu = t.getValueAt(r, 6).toString();

        // ❌ Nếu là QUẢN LÝ → không cho hiện nút luôn
        if (chucVu.equals("Quản lý")) {
            JPanel empty = new JPanel();
            empty.setBackground(t.getSelectionBackground());
            return empty;
        }

        // ✅ Nhân viên → bình thường
        ma = t.getValueAt(r, 1).toString();
        ten = t.getValueAt(r, 2).toString();

        panel.setBackground(t.getSelectionBackground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}


class RoundedButton extends JButton {
    private int r;
    public RoundedButton(String t, int r) { super(t); this.r = r; setOpaque(false); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isRollover() ? getBackground().brighter() : getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
        super.paintComponent(g); g2.dispose();
    }
}