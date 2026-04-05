package gui;

import dao.KhachHangDAO;
import model.entities.KhachHang;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Dialog THÊM khách hàng mới.
 * – Dùng JDialog (modal = true) → khoá toàn bộ màn hình nền.
 * – Chọn ngày sinh bằng 3 JComboBox (Ngày / Tháng / Năm).
 * – Validate đầy đủ theo ràng buộc nghiệp vụ.
 */
public class KhachHangDialog extends JDialog {

    // ── Màu chủ đề ──────────────────────────────────────────────────────────
    private static final Color C_HDR_BG   = new Color(50, 30, 28);
    private static final Color C_ACCENT   = new Color(212, 175, 55);
    private static final Color C_PRIMARY  = new Color(60, 40, 35);
    private static final Color C_BG       = new Color(248, 246, 242);
    private static final Color C_ERROR    = new Color(200, 55, 55);
    private static final Color C_FOCUS    = new Color(212, 175, 55);
    private static final Color C_CANCEL   = new Color(230, 230, 230);

    // ── Trường nhập liệu ────────────────────────────────────────────────────
    private JTextField txtMaKH;
    private JTextField txtTenKH;
    private JTextField txtCCCD;
    private JTextField txtSDT;

    // ── Date picker ─────────────────────────────────────────────────────────
    private JComboBox<Integer> cbNgay  = new JComboBox<>();
    private JComboBox<String>  cbThang = new JComboBox<>();
    private JComboBox<Integer> cbNam   = new JComboBox<>();

    // ── Error labels ────────────────────────────────────────────────────────
    private JLabel errTen, errNgaySinh, errCCCD, errSDT;

    private final KhachHangDAO    dao;
    private final KhachHangPanel  panel;

    // ────────────────────────────────────────────────────────────────────────
    public KhachHangDialog(Frame owner, KhachHangPanel panel) {
        super(owner, "Thêm khách hàng mới", true);   // modal = true
        this.dao   = new KhachHangDAO();
        this.panel = panel;
        buildUI();
        pack();
        setMinimumSize(new Dimension(600, 680));
        setResizable(false);
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(() -> txtTenKH.requestFocusInWindow());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  XÂY DỰNG GIAO DIỆN
    // ════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildForm(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel();
        h.setBackground(C_HDR_BG);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(new EmptyBorder(26, 32, 20, 32));


        JLabel title = lbl("Thêm khách hàng mới", 22, Font.BOLD, Color.WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = lbl("Vui lòng điền đầy đủ thông tin bên dưới", 13, Font.PLAIN,
                         new Color(255, 255, 0));
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_ACCENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(LEFT_ALIGNMENT);

        h.add(Box.createVerticalStrut(8));
        h.add(title);
        h.add(Box.createVerticalStrut(4));
        h.add(sub);
        h.add(Box.createVerticalStrut(14));
        h.add(sep);
        return h;
    }

    // ── Form ────────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(C_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(22, 32, 8, 32));

        txtMaKH = makeField();
        txtMaKH.setText(dao.getNextMaKH());
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(230, 232, 238));
        txtMaKH.setForeground(new Color(100, 110, 130));
        
        txtTenKH = makeField();
        txtTenKH.requestFocus();
        txtCCCD  = makeField();
        txtSDT   = makeField();
        
        errTen      = errLbl();
        errNgaySinh = errLbl();
        errCCCD     = errLbl();
        errSDT      = errLbl();

        // Khởi tạo date picker (mặc định 25 năm trước)
        initDatePicker(LocalDate.now().minusYears(25));

        form.add(fieldBlock("Mã khách hàng",       txtMaKH,  null,        "Chỉ nhập 8 chữ số "));
        form.add(Box.createVerticalStrut(13));
        form.add(fieldBlock("Họ và tên",             txtTenKH, errTen,      null));
        form.add(Box.createVerticalStrut(13));
        form.add(dateBlock());
        form.add(Box.createVerticalStrut(13));
        form.add(fieldBlock("Số căn cước công dân",  txtCCCD,  errCCCD,     "Đúng 12 chữ số"));
        form.add(Box.createVerticalStrut(13));
        form.add(fieldBlock("Số điện thoại",          txtSDT,   errSDT,      "Đúng 10 chữ số"));
        return form;
    }

   private JPanel maKHBlock() {
       JPanel b = new JPanel();
       b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
       b.setOpaque(false);
       b.setAlignmentX(LEFT_ALIGNMENT);

       JLabel lbl = lbl("Mã khách hàng", 13, Font.BOLD, new Color(250,250, 30));
       lbl.setAlignmentX(LEFT_ALIGNMENT);

       JLabel hint = lbl("    Chỉ nhập phần số (tối đa 8 chữ số, VD: 00000001)", 11,
                         Font.ITALIC, new Color(160, 145, 125));
       hint.setAlignmentX(LEFT_ALIGNMENT);

       // Hàng input: [KH] + [________]
       JPanel row = new JPanel(new BorderLayout(0, 0));
       row.setOpaque(false);
       row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
       row.setAlignmentX(LEFT_ALIGNMENT);

       // Prefix "KH" – trông như phần đầu của input box
       JLabel prefix = new JLabel(" KH ");
       prefix.setFont(new Font("Segoe UI", Font.BOLD, 15));
       prefix.setForeground(new Color(212, 175, 55));
       prefix.setOpaque(true);
       prefix.setBackground(new Color(60, 40, 35));
       prefix.setBorder(new CompoundBorder(
               new MatteBorder(1, 1, 1, 0, new Color(60, 40, 35)),
               new EmptyBorder(0, 10, 0, 8)));
       prefix.setPreferredSize(new Dimension(52, 42));

       // Ô nhập số
       txtMaKH.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
       txtMaKH.setBorder(new CompoundBorder(
               new LineBorder(new Color(210, 200, 190), 1, true),
               new EmptyBorder(5, 12, 5, 12)));

       row.add(prefix,  BorderLayout.WEST);
       row.add(txtMaKH, BorderLayout.CENTER);


       b.add(lbl);
       b.add(Box.createVerticalStrut(2));
       b.add(hint);
       b.add(Box.createVerticalStrut(5));
       b.add(row);
       b.add(Box.createVerticalStrut(2));
       return b;
   }


    /** Khối 1 trường: label + hint + input + error */
    private JPanel fieldBlock(String label, JTextField field,
                               JLabel errLabel, String hint) {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        b.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = lbl(label, 13, Font.BOLD, new Color(60, 40, 30));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        b.add(lbl);

        if (hint != null) {
            JLabel h = lbl("    " + hint, 11, Font.ITALIC, new Color(160, 145, 125));
            h.setAlignmentX(LEFT_ALIGNMENT);
            b.add(Box.createVerticalStrut(2));
            b.add(h);
        }
        b.add(Box.createVerticalStrut(5));
        field.setAlignmentX(LEFT_ALIGNMENT);
        b.add(field);

        if (errLabel != null) {
            errLabel.setAlignmentX(LEFT_ALIGNMENT);
            b.add(Box.createVerticalStrut(2));
            b.add(errLabel);
        }
        return b;
    }

    /** Khối chọn ngày sinh */
    private JPanel dateBlock() {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        b.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = lbl("Ngày sinh", 13, Font.BOLD, new Color(60, 40, 30));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel hint = lbl("    Phải đủ 16 tuổi trở lên", 11, Font.ITALIC,
                          new Color(160, 145, 125));
        hint.setAlignmentX(LEFT_ALIGNMENT);

        JPanel picker = buildDatePickerRow();
        picker.setAlignmentX(LEFT_ALIGNMENT);

        errNgaySinh.setAlignmentX(LEFT_ALIGNMENT);

        b.add(lbl);
        b.add(Box.createVerticalStrut(2));
        b.add(hint);
        b.add(Box.createVerticalStrut(5));
        b.add(picker);
        b.add(Box.createVerticalStrut(2));
        b.add(errNgaySinh);
        return b;
    }

    // ── Footer ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        f.setBackground(C_BG);
        f.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 215, 205)));

        JButton btnHuy  = makeBtn("Hủy",                 C_CANCEL,  new Color(70, 50, 40));
        JButton btnThem = makeBtn("Thêm khách hàng", C_PRIMARY, Color.WHITE);

        btnHuy.addActionListener(e  -> dispose());
        btnThem.addActionListener(e -> handleSubmit());
        getRootPane().setDefaultButton(btnThem);

        f.add(btnHuy);
        f.add(btnThem);
        return f;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE PICKER
    // ════════════════════════════════════════════════════════════════════════
    private void initDatePicker(LocalDate init) {
        // Tháng
        for (int i = 1; i <= 12; i++) cbThang.addItem("Tháng " + i);
        styleCombo(cbThang, 120);

        // Năm (từ năm hiện tại - 16 đến 1920)
        int maxYear = LocalDate.now().getYear() - 16;
        for (int y = maxYear; y >= 1920; y--) cbNam.addItem(y);
        styleCombo(cbNam, 90);

        styleCombo(cbNgay, 70);

        // Gán giá trị ban đầu
        cbThang.setSelectedIndex(init.getMonthValue() - 1);
        cbNam.setSelectedItem(init.getYear() > maxYear ? maxYear : init.getYear());
        refreshDays(init.getDayOfMonth());

        // Listener cập nhật ngày khi tháng/năm thay đổi
        cbThang.addActionListener(e -> refreshDays(-1));
        cbNam  .addActionListener(e -> refreshDays(-1));
    }

    private JPanel buildDatePickerRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        row.add(cbNgay);
        row.add(sepLabel("/"));
        row.add(cbThang);
        row.add(sepLabel("/"));
        row.add(cbNam);
        return row;
    }

    private void refreshDays(int keepDay) {
        int month = cbThang.getSelectedIndex() + 1;
        int year  = cbNam.getSelectedItem() != null
                  ? (Integer) cbNam.getSelectedItem() : 2000;
        int max   = YearMonth.of(year, month).lengthOfMonth();

        Integer prev = (Integer) cbNgay.getSelectedItem();
        cbNgay.removeAllItems();
        for (int d = 1; d <= max; d++) cbNgay.addItem(d);

        int target = keepDay > 0 ? keepDay : (prev != null ? prev : 1);
        cbNgay.setSelectedItem(Math.min(target, max));
    }

    private LocalDate getPickedDate() {
        return LocalDate.of(
                (Integer) cbNam  .getSelectedItem(),
                cbThang.getSelectedIndex() + 1,
                (Integer) cbNgay .getSelectedItem()
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    //  VALIDATE & SUBMIT
    // ════════════════════════════════════════════════════════════════════════
    private void handleSubmit() {
        clearErrors();
        boolean ok = true;

        String maKH = txtMaKH.getText().trim();
        String ten  = txtTenKH.getText().trim();
        String cccd = txtCCCD .getText().trim();
        String sdt  = txtSDT  .getText().trim();

        // 2. Tên không rỗng
        if (ten.isEmpty()) { showErr(errTen, "Tên khách hàng không được để trống"); ok = false; }

        // 3. Ngày sinh – đủ 16 tuổi
        LocalDate ns = getPickedDate();
        if (LocalDate.now().minusYears(16).isBefore(ns)) {
            showErr(errNgaySinh, "Khách hàng phải đủ 16 tuổi trở lên");
            ok = false;
        }
        // 4. CCCD – 12 chữ số
        if (cccd.isEmpty())            { showErr(errCCCD, "Số CCCD không được để trống"); ok = false; }
        else if (!cccd.matches("\\d{12}")) { showErr(errCCCD, "CCCD phải gồm đúng 12 chữ số"); ok = false; }

        // 5. SĐT – 10 chữ số
        if (sdt.isEmpty())             { showErr(errSDT, "Số điện thoại không được để trống"); ok = false; }
        else if (!sdt.matches("\\d{10}"))  { showErr(errSDT, "Số điện thoại phải gồm đúng 10 chữ số"); ok = false; }

        if (!ok) return;

        KhachHang kh = new KhachHang(maKH, toTitleCase(ten), cccd, sdt, ns);
        if (dao.insert(kh)) {
            JOptionPane.showMessageDialog(this,
                    "Thêm khách hàng thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            if (panel != null) panel.initData();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌  Thêm thất bại – kiểm tra lại dữ liệu.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FACTORY / UTILITY
    // ════════════════════════════════════════════════════════════════════════
    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(new Color(40, 30, 20));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 200, 190), 1, true),
                new EmptyBorder(5, 12, 5, 12)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.isEditable())
                    f.setBorder(new CompoundBorder(
                            new LineBorder(C_FOCUS, 2, true),
                            new EmptyBorder(5, 12, 5, 12)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(new Color(210, 200, 190), 1, true),
                        new EmptyBorder(5, 12, 5, 12)));
            }
        });
        return f;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 24, 10, 24)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited (java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private void styleCombo(JComboBox<?> cb, int width) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(width, 38));
        cb.setBorder(new LineBorder(new Color(210, 200, 190), 1, true));
    }

    private JLabel lbl(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        return l;
    }

    private JLabel errLbl() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        l.setForeground(C_ERROR);
        return l;
    }

    private JLabel sepLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(new Color(150, 130, 110));
        return l;
    }

    private void showErr(JLabel lbl, String msg) {
        if (lbl != null) lbl.setText("⚠  " + msg);
    }

    private void clearErrors() {
        for (JLabel l : new JLabel[]{errTen, errNgaySinh, errCCCD, errSDT})
            if (l != null) l.setText(" ");
    }
    
    private String toTitleCase(String input) {
    	if(input == null || input.isEmpty()) return input;
    	StringBuilder sb = new StringBuilder();
    	boolean nextUpper = true;
    	for (char ch : input.toCharArray()) {
    		if(Character.isWhitespace(ch)) {
    			nextUpper = true;
    			sb.append(ch);
    		} else if (nextUpper) {
    			sb.append(Character.toUpperCase(ch));
    			nextUpper = false;
    		} else {
    			sb.append(Character.toLowerCase(ch));
    		}
    	}
    	return sb.toString();
    }
}