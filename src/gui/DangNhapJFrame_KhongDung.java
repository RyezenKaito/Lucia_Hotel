package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import dao.NhanVienDAO;
import model.entities.NhanVien;
import java.awt.*;
import java.awt.event.*;

public class DangNhapJFrame extends JFrame implements ActionListener {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private NhanVienDAO nvDAO = new NhanVienDAO();
    private JCheckBox chkShowPassword;

    private final Color BG_COLOR = new Color(245, 241, 234);
    private final Color TEXT_DARK = new Color(50, 30, 20);
    private final Color BTN_GOLD_DEFAULT = new Color(190, 150, 80);
    private final Color BTN_GOLD_HOVER = new Color(210, 170, 100);
    private final Color BTN_TEXT_COLOR = new Color(70, 45, 10);

    public DangNhapJFrame() {
        setTitle("Lucia Star - Đăng nhập");
        setSize(850, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel pnlMain = new JPanel(new GridLayout(1, 2));

        // --- LEFT PANEL: BACKGROUND ---
        JPanel pnlLeft = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgIcon = new ImageIcon("src/background/LUCIA_STAR.jpg");
                if (bgIcon.getImage() != null) {
                    g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // --- RIGHT PANEL ---
        JPanel pnlRight = new JPanel();
        pnlRight.setBackground(BG_COLOR);
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel lblLoginTitle = new JLabel("ĐĂNG NHẬP");
        lblLoginTitle.setFont(new Font("Serif", Font.BOLD, 32));
        lblLoginTitle.setForeground(TEXT_DARK);
        lblLoginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLoginTitle.setBorder(new MatteBorder(0, 0, 3, 0, BTN_GOLD_DEFAULT));

        txtUsername = new JTextField("LUCIA");
        txtPassword = new JPasswordField();

        JPanel pnlUserField = createCustomInput("Mã nhân viên:", txtUsername);
        JPanel pnlPassField = createCustomInput("Mật khẩu:", txtPassword);

        // --- CHECKBOX ---
        chkShowPassword = new JCheckBox("Hiện mật khẩu");
        chkShowPassword.setBackground(BG_COLOR);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkShowPassword.setForeground(TEXT_DARK);
        chkShowPassword.addActionListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•');
        });

        JPanel pnlChk = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlChk.setBackground(BG_COLOR);
        pnlChk.add(chkShowPassword);

        // --- BUTTON ---
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setBackground(BTN_GOLD_DEFAULT);
        btnLogin.setForeground(BTN_TEXT_COLOR);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(BTN_GOLD_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(BTN_GOLD_DEFAULT);
            }
        });

        JPanel pnlBtnWrapper = new JPanel(new BorderLayout());
        pnlBtnWrapper.setOpaque(false);
        pnlBtnWrapper.setBorder(new EmptyBorder(10, 30, 10, 30));
        pnlBtnWrapper.add(btnLogin, BorderLayout.CENTER);
        pnlBtnWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        // =========================
        // ✅ FIX ENTER BEHAVIOR
        // =========================

        // Enter ở username → xuống password
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        // Enter ở password → login
        txtPassword.addActionListener(this);

        // Button login
        btnLogin.addActionListener(this);

        // =========================

        // Layout
        pnlRight.add(Box.createVerticalGlue());
        pnlRight.add(lblLoginTitle);
        pnlRight.add(Box.createVerticalStrut(30));
        pnlRight.add(pnlUserField);
        pnlRight.add(Box.createVerticalStrut(15));
        pnlRight.add(pnlPassField);
        pnlRight.add(pnlChk);
        pnlRight.add(Box.createVerticalStrut(20));
        pnlRight.add(pnlBtnWrapper);
        pnlRight.add(Box.createVerticalGlue());

        pnlMain.add(pnlLeft);
        pnlMain.add(pnlRight);
        add(pnlMain);
    }

    private JPanel createCustomInput(String labelText, JTextField textField) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textField.setOpaque(false);
        textField.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 160, 150)));

        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(textField, BorderLayout.CENTER);
        return pnl;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập đủ thông tin đi ông!");
            return;
        }

        if (nvDAO.authenticate(user, pass)) {
            NhanVien staff = nvDAO.getById(user);
            JOptionPane.showMessageDialog(this, "Chào mừng " + staff.getHoTen());
            new MainFrame(staff).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Sai mã NV hoặc mật khẩu!");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}
