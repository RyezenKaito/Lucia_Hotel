package gui;

import dao.KhachHangDAO;
import model.entities.KhachHang;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Dialog SỬA thông tin khách hàng.
 * – JDialog modal → khoá toàn bộ màn hình nền.
 * – Chọn ngày sinh bằng 3 JComboBox.
 * – Validate đầy đủ.
 */
public class SuaThongTinKhachHangDialog extends JDialog {

    // ── Màu chủ đề ──────────────────────────────────────────────────────────
    private static final Color C_HDR_BG  = new Color(34, 52, 78);
    private static final Color C_ACCENT  = new Color(90, 150, 220);
    private static final Color C_PRIMARY = new Color(34, 52, 78);
    private static final Color C_BG      = new Color(245, 247, 250);
    private static final Color C_ERROR   = new Color(200, 55, 55);
    private static final Color C_FOCUS   = new Color(90, 150, 220);
    private static final Color C_CANCEL  = new Color(230, 230, 230);

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

    private final KhachHangDAO   dao;
    private final KhachHangPanel panel;

    // ────────────────────────────────────────────────────────────────────────
    public SuaThongTinKhachHangDialog(Frame owner, KhachHang kh, KhachHangPanel panel) {
        super(owner, "Cập nhật thông tin khách hàng", true);  // modal = true
        this.dao   = new KhachHangDAO();
        this.panel = panel;
        buildUI(kh);
        pack();
        setMinimumSize(new Dimension(600, 700));
        setResizable(false);
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(() -> txtTenKH.requestFocusInWindow());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  XÂY DỰNG GIAO DIỆN
    // ════════════════════════════════════════════════════════════════════════
    private void buildUI(KhachHang kh) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildHeader(kh),  BorderLayout.NORTH);
        add(buildForm(kh),    BorderLayout.CENTER);
        add(buildFooter(kh),  BorderLayout.SOUTH);
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader(KhachHang kh) {
        JPanel h = new JPanel();
        h.setBackground(C_HDR_BG);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(new EmptyBorder(26, 32, 20, 32));

        JLabel title = lbl("Cập nhật thông tin khách hàng", 21, Font.BOLD, Color.WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = lbl("Mã khách hàng: " + kh.getMaKhachHang(),
                         13, Font.PLAIN, C_ACCENT);
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
    private JPanel buildForm(KhachHang kh) {
        JPanel form = new JPanel();
        form.setBackground(C_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(22, 32, 8, 32));

        // Mã KH – chỉ đọc
        txtMaKH = makeField(kh.getMaKhachHang());
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(230, 232, 238));
        txtMaKH.setForeground(new Color(100, 110, 130));

        txtTenKH = makeField(nvl(kh.getHoTen()));
        txtTenKH.requestFocus();
        txtCCCD  = makeField(nvl(kh.getCCCD()));
        txtSDT   = makeField(nvl(kh.getSoDienThoai()));

        errTen      = errLbl();
        errNgaySinh = errLbl();
        errCCCD     = errLbl();
        errSDT      = errLbl();

        // Date picker – lấy giá trị hiện tại của khách hoặc mặc định 25 năm trước
        LocalDate initDate = kh.getNgaySinh() != null
                ? kh.getNgaySinh()
                : LocalDate.now().minusYears(25);
        initDatePicker(initDate);

        form.add(fieldBlock("Mã khách hàng",       txtMaKH,  null,        "Không thể chỉnh sửa"));
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

    private JPanel fieldBlock(String label, JTextField field,
                               JLabel errLabel, String hint) {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        b.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = lbl(label, 13, Font.BOLD, new Color(34, 52, 78));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        b.add(lbl);

        if (hint != null) {
            JLabel h = lbl("    " + hint, 11, Font.ITALIC, new Color(140, 150, 165));
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

    private JPanel dateBlock() {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        b.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl  = lbl("Ngày sinh", 13, Font.BOLD, new Color(34, 52, 78));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        JLabel hint = lbl("    Phải đủ 16 tuổi trở lên", 11, Font.ITALIC,
                          new Color(140, 150, 165));
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
    private JPanel buildFooter(KhachHang kh) {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        f.setBackground(C_BG);
        f.setBorder(new MatteBorder(1, 0, 0, 0, new Color(215, 220, 228)));

        JButton btnDong    = makeBtn("Đóng",              C_CANCEL,  new Color(50, 65, 90));
        JButton btnCapNhat = makeBtn("Lưu thay đổi", C_PRIMARY, Color.WHITE);

        btnDong   .addActionListener(e -> dispose());
        btnCapNhat.addActionListener(e -> handleUpdate(kh));
        getRootPane().setDefaultButton(btnCapNhat);

        f.add(btnDong);
        f.add(btnCapNhat);
        return f;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE PICKER
    // ════════════════════════════════════════════════════════════════════════
    private void initDatePicker(LocalDate init) {
        for (int i = 1; i <= 12; i++) cbThang.addItem("Tháng " + i);
        styleCombo(cbThang, 120);

        int maxYear = LocalDate.now().getYear() - 16;
        for (int y = maxYear; y >= 1920; y--) cbNam.addItem(y);
        styleCombo(cbNam, 90);
        styleCombo(cbNgay, 70);

        cbThang.setSelectedIndex(init.getMonthValue() - 1);
        cbNam.setSelectedItem(Math.min(init.getYear(), maxYear));
        refreshDays(init.getDayOfMonth());

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
    //  VALIDATE & UPDATE
    // ════════════════════════════════════════════════════════════════════════
    private void handleUpdate(KhachHang kh) {
        clearErrors();
        boolean ok = true;

        String ten  = txtTenKH.getText().trim();
        String cccd = txtCCCD .getText().trim();
        String sdt  = txtSDT  .getText().trim();

        if (ten.isEmpty())             { showErr(errTen,  "Tên khách hàng không được để trống"); ok = false; }
        if (cccd.isEmpty())            { showErr(errCCCD, "Số CCCD không được để trống"); ok = false; }
        else if (!cccd.matches("\\d{12}")) { showErr(errCCCD, "CCCD phải gồm đúng 12 chữ số"); ok = false; }
        if (sdt.isEmpty())             { showErr(errSDT,  "Số điện thoại không được để trống"); ok = false; }
        else if (!sdt.matches("\\d{10}"))  { showErr(errSDT,  "Số điện thoại phải gồm đúng 10 chữ số"); ok = false; }

        LocalDate ns = getPickedDate();
        if (LocalDate.now().minusYears(16).isBefore(ns)) {
            showErr(errNgaySinh, "Khách hàng phải đủ 16 tuổi trở lên");
            ok = false;
        }
        if (!ok) return;

        kh.setHoTen(toTitleCase(ten));
        kh.setCCCD(cccd);
        kh.setSoDienThoai(sdt);
        kh.setNgaySinh(ns);

        if (dao.update(kh)) {
            JOptionPane.showMessageDialog(this,
                    "✅  Cập nhật thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            if (panel != null) panel.initData();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌  Cập nhật thất bại – kiểm tra lại.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FACTORY / UTILITY
    // ════════════════════════════════════════════════════════════════════════
    private JTextField makeField(String value) {
        JTextField f = new JTextField(value);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(new Color(30, 40, 55));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 205, 215), 1, true),
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
                        new LineBorder(new Color(200, 205, 215), 1, true),
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
        cb.setBorder(new LineBorder(new Color(200, 205, 215), 1, true));
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
        l.setForeground(new Color(130, 140, 155));
        return l;
    }

    private void showErr(JLabel lbl, String msg) {
        if (lbl != null) lbl.setText("⚠  " + msg);
    }

    private void clearErrors() {
        for (JLabel l : new JLabel[]{errTen, errNgaySinh, errCCCD, errSDT})
            if (l != null) l.setText(" ");
    }

    private String nvl(String s) { return s != null ? s : ""; }
    
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