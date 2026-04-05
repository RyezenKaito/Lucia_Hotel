package gui;

import dao.KhachHangDAO;
import model.entities.KhachHang;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KhachHangPanel extends JPanel {

    private final KhachHangDAO dao = new KhachHangDAO();
    private DefaultTableModel  dtm;
    private JTable             tbl;
    private JTextField         txtSearch;

    // Cards thống kê – cập nhật sau khi load
    private JLabel lblTongKH;
    private JLabel lblSinhNhat;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ════════════════════════════════════════════════════════════════════════
    public KhachHangPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 241, 234));
        setBorder(new EmptyBorder(10, 30, 10, 20));
        add(createHeader(),  BorderLayout.NORTH);
        add(createTable(),   BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ════════════════════════════════════════════════════════════════════════
    public JPanel createHeader() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);

        // ── Dòng tiêu đề + nút Thêm ──────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lblTitle = new JLabel("Danh sách khách hàng");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 32));
        lblTitle.setForeground(new Color(60, 40, 30));

        JButton btnAdd = new JButton("+ Thêm khách hàng");
        btnAdd.setBackground(new Color(60, 40, 35));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(185, 38));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new KhachHangDialog(owner, this).setVisible(true);
        });

        top.add(lblTitle, BorderLayout.WEST);
        top.add(btnAdd,   BorderLayout.EAST);

        // ── Cards thống kê ────────────────────────────────────────────────
        JPanel cardRow = new JPanel(new GridLayout(1, 2, 15, 0));
        cardRow.setOpaque(false);
        cardRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Card 1: tổng số khách
        lblTongKH   = new JLabel("0");
        cardRow.add(buildStatCard("👥  Tổng khách hàng", lblTongKH,
                                  new Color(50, 30, 28), false));

        // Card 2: sinh nhật HÔM NAY  (thay INTERNATIONAL)
        lblSinhNhat = new JLabel("0");
        JPanel bdCard = buildStatCard(
                "🎂  Sinh nhật hôm nay", lblSinhNhat,
                new Color(180, 80, 40), true);
        cardRow.add(bdCard);

        // ── Thanh tìm kiếm ────────────────────────────────────────────────
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        txtSearch = new JTextField("Tìm kiếm tên, SĐT, mã KH...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("SansSerif", Font.ITALIC, 14));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    public void insertUpdate (javax.swing.event.DocumentEvent e) { search(); }
                    public void removeUpdate (javax.swing.event.DocumentEvent e) { search(); }
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
                });
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().startsWith("Tìm kiếm")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                    txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText("Tìm kiếm tên, SĐT, mã KH...");
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setFont(new Font("SansSerif", Font.ITALIC, 14));
                }
            }
        });
        searchPanel.add(new JLabel("🔍 "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        // ── Gộp lại ───────────────────────────────────────────────────────
        main.add(top);
        main.add(Box.createVerticalStrut(20));
        main.add(cardRow);
        main.add(Box.createVerticalStrut(20));
        main.add(searchPanel);
        main.add(Box.createVerticalStrut(25));
        return main;
    }

    /** Tạo stat card; nếu clickable = true thì click → birthday dialog */
    private JPanel buildStatCard(String title, JLabel valueLabel,
                                  Color valueColor, boolean clickable) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 225, 215), 1, true),
                new EmptyBorder(15, 20, 15, 20)));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setForeground(new Color(130, 120, 110));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(valueColor);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);

        if (clickable) {
            JLabel hint = new JLabel("Nhấp để xem chi tiết →");
            hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
            hint.setForeground(new Color(160, 130, 100));
            textPanel.add(Box.createVerticalStrut(3));
            textPanel.add(hint);

            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    showBirthdayDialog();
                }
                @Override public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(255, 248, 235));
                }
                @Override public void mouseExited(MouseEvent e) {
                    card.setBackground(Color.WHITE);
                }
            });
        }

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BẢNG DANH SÁCH
    // ════════════════════════════════════════════════════════════════════════
    private JScrollPane createTable() {
        String[] cols = {"STT", "Mã KH", "Họ tên", "CCCD", "Số điện thoại", "Ngày sinh"};
        dtm = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl = new JTable(dtm);
        tbl.setRowHeight(40);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tbl.setGridColor(new Color(240, 240, 240));
        tbl.setShowVerticalLines(false);
        tbl.setSelectionBackground(new Color(255, 243, 210));
        tbl.setSelectionForeground(Color.BLACK);

        JTableHeader header = tbl.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(new Color(90, 55, 45));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 40));
        
        // --- CHỐNG KÉO THẢ VÀ THAY ĐỔI KÍCH THƯỚC CỘT ---
        header.setReorderingAllowed(false); // Khóa không cho đổi vị trí cột (Kéo thả)
        header.setResizingAllowed(false);   // Khóa không cho kéo giãn kích thước cột (Tùy chọn)

        // Renderer: căn giữa + highlight sinh nhật hôm nay
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    setBackground(Color.WHITE);
                    Object ns = table.getValueAt(row, 5);
                    if (ns != null && !ns.toString().isEmpty()) {
                        try {
                            LocalDate d = LocalDate.parse(ns.toString(), DATE_FMT);
                            LocalDate t = LocalDate.now();
                            if (d.getDayOfMonth() == t.getDayOfMonth()
                                    && d.getMonthValue() == t.getMonthValue()) {
                                setBackground(new Color(255, 248, 215)); // vàng nhạt
                            }
                        } catch (Exception ignored) {}
                    }
                }
                return this;
            }
        });

        tbl.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tbl.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tbl.setRowSelectionInterval(row, row);
                        createPopupMenu().show(tbl, e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);

        initData();
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NẠP DỮ LIỆU (public để Dialog gọi refresh)
    // ════════════════════════════════════════════════════════════════════════
    public void initData() {
        dtm.setRowCount(0);
        List<KhachHang> ds = dao.getAll();
        int stt = 1;
        long bdToday = 0;

        for (KhachHang kh : ds) {
            String nsStr = kh.getNgaySinh() != null
                    ? kh.getNgaySinh().format(DATE_FMT) : "";
            if (kh.isBirthdayToday()) bdToday++;
            dtm.addRow(new Object[]{
                    stt++,
                    kh.getMaKhachHang(),
                    kh.getHoTen(),
                    kh.getCCCD(),
                    kh.getSoDienThoai(),
                    nsStr
            });
        }

        if (lblTongKH   != null) lblTongKH  .setText(String.valueOf(ds.size()));
        if (lblSinhNhat != null) {
            lblSinhNhat.setText(String.valueOf(bdToday));
            lblSinhNhat.setForeground(bdToday > 0
                    ? new Color(180, 80, 40) : new Color(160, 160, 160));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  POPUP SINH NHẬT  (public để MainFrame gọi)
    // ════════════════════════════════════════════════════════════════════════
    public void showBirthdayDialog() {
        List<String[]> list = dao.getBirthdayTodayWithRoom();

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "🎂  Khách có sinh nhật hôm nay", true);
        dlg.setSize(780, 460);
        dlg.setLocationRelativeTo(owner);
        dlg.setLayout(new BorderLayout());

        // ── Header ──────────────────────────────────────────────────────────
        JPanel hdr = new JPanel();
        hdr.setBackground(new Color(50, 30, 28));
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setBorder(new EmptyBorder(18, 28, 16, 28));

        JLabel t1 = new JLabel("🎂  Danh sách khách có sinh nhật hôm nay");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t1.setForeground(Color.WHITE);
        t1.setAlignmentX(LEFT_ALIGNMENT);

        LocalDate today = LocalDate.now();
        JLabel t2 = new JLabel(String.format("Ngày %02d / %02d / %04d  –  %d khách",
                today.getDayOfMonth(), today.getMonthValue(),
                today.getYear(), list.size()));
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t2.setForeground(new Color(212, 175, 55));
        t2.setAlignmentX(LEFT_ALIGNMENT);

        hdr.add(t1);
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(t2);
        dlg.add(hdr, BorderLayout.NORTH);

        // ── Bảng ────────────────────────────────────────────────────────────
        String[] cols = {"STT", "Mã KH", "Họ và tên", "Số điện thoại",
                         "Ngày sinh", "Phòng đang ở"};
        DefaultTableModel bdDtm = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        if (list.isEmpty()) {
            // không có khách nào
        } else {
            int i = 1;
            for (String[] row : list)
                bdDtm.addRow(new Object[]{i++, row[0], row[1], row[2], row[3], row[4]});
        }

        JTable bdTbl = new JTable(bdDtm);
        bdTbl.setRowHeight(36);
        bdTbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bdTbl.setGridColor(new Color(240, 238, 232));
        bdTbl.setShowVerticalLines(false);
        bdTbl.setSelectionBackground(new Color(255, 243, 210));

        JTableHeader bdHdr = bdTbl.getTableHeader();
        bdHdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        bdHdr.setBackground(new Color(90, 55, 45));
        bdHdr.setForeground(Color.WHITE);
        bdHdr.setPreferredSize(new Dimension(100, 36));

        DefaultTableCellRenderer ctr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 250, 246));
                    // phòng trống → màu khác
                    if (c == 5 && v != null && v.toString().equals("Chưa đặt phòng"))
                        setForeground(new Color(160, 160, 160));
                    else
                        setForeground(new Color(40, 30, 20));
                }
                return this;
            }
        };
        bdTbl.setDefaultRenderer(Object.class, ctr);

        JScrollPane sc = new JScrollPane(bdTbl);
        sc.setBorder(new EmptyBorder(12, 16, 8, 16));
        sc.getViewport().setBackground(Color.WHITE);
        dlg.add(sc, BorderLayout.CENTER);

        // ── Footer: nút đóng ────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        footer.setBackground(new Color(248, 246, 242));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 215, 205)));

        if (list.isEmpty()) {
            JLabel noData = new JLabel("Không có khách nào có sinh nhật hôm nay.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noData.setForeground(new Color(150, 140, 130));
            footer.add(noData);
        }

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setBackground(new Color(60, 40, 35));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 25, 20), 1, true),
                new EmptyBorder(8, 20, 8, 20)));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        dlg.getRootPane().setDefaultButton(btnClose);

        footer.add(btnClose);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  POPUP MENU CHUỘT PHẢI
    // ════════════════════════════════════════════════════════════════════════
    private JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem itemSua = new JMenuItem("Sửa thông tin");
        itemSua.setBackground(new Color(34, 52, 78));
        itemSua.setForeground(Color.WHITE);
        itemSua.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JMenuItem itemXoa = new JMenuItem("Xóa khách hàng");
        itemXoa.setBackground(new Color(180, 50, 50));
        itemXoa.setForeground(Color.WHITE);
        itemXoa.setFont(new Font("Segoe UI", Font.BOLD, 13));

        popup.add(itemSua);
        popup.addSeparator();
        popup.add(itemXoa);

        itemSua.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row == -1) return;

            String maKH  = tbl.getValueAt(row, 1).toString();
            String tenKH = tbl.getValueAt(row, 2).toString();
            String cccd  = tbl.getValueAt(row, 3).toString();
            String sdt   = tbl.getValueAt(row, 4).toString();
            String nsStr = tbl.getValueAt(row, 5).toString();

            KhachHang kh = new KhachHang(maKH, tenKH, cccd, sdt);
            if (!nsStr.isEmpty()) {
                try { kh.setNgaySinh(LocalDate.parse(nsStr, DATE_FMT)); }
                catch (Exception ignored) {}
            }
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new SuaThongTinKhachHangDialog(owner, kh, this).setVisible(true);
        });

        itemXoa.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row == -1) return;
            String maKH  = tbl.getValueAt(row, 1).toString();
            String tenKH = tbl.getValueAt(row, 2).toString();
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa khách hàng \"" + tenKH + "\" (" + maKH + ")?\n" 
                    + "Hành động này không thể hoàn tác.",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
            	if(dao.delete(maKH)) {
            		JOptionPane.showMessageDialog(KhachHangPanel.this,
            				"Đã xóa khách hàng" + maKH + "Thành công.",
            				"Thông báo", JOptionPane.INFORMATION_MESSAGE);
            		initData(); // Reload bảng dữ liệu từ DB
            	} else {
            		JOptionPane.showMessageDialog(KhachHangPanel.this, 
            				"Xóa thất bại - khách hàng này có thể đang đặt phòng.", 
            				"Lỗi.", JOptionPane.ERROR_MESSAGE);
            	}
            }
        });

        return popup;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TÌM KIẾM
    // ════════════════════════════════════════════════════════════════════════
    private void search() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.startsWith("tìm kiếm")) { initData(); return; }

        dtm.setRowCount(0);
        int stt = 1;
        for (KhachHang kh : dao.getAll()) {
            if (kh.getHoTen().toLowerCase().contains(kw)
                    || kh.getSoDienThoai().contains(kw)
                    || kh.getMaKhachHang().toLowerCase().contains(kw)) {
                String nsStr = kh.getNgaySinh() != null
                        ? kh.getNgaySinh().format(DATE_FMT) : "";
                dtm.addRow(new Object[]{
                        stt++,
                        kh.getMaKhachHang(),
                        kh.getHoTen(),
                        kh.getCCCD(),
                        kh.getSoDienThoai(),
                        nsStr
                });
            }
        }
    }
}