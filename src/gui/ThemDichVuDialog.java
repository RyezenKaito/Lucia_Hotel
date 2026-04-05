package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.DichVuDAO;
import model.entities.DichVu;
import java.awt.*;

public class ThemDichVuDialog extends JDialog {
    private JTextField txtMaDV, txtTenDV, txtGiaGoc, txtDonVi;
    private JComboBox<String> cbLoaiDV;
    private JButton btnLuu, btnHuy;

    private final Color PRIMARY_BROWN = new Color(74, 45, 42);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    public ThemDichVuDialog(Frame parent) {
        super(parent, "Thêm dịch vụ mới vào hệ thống", true);
        setSize(450, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel pnlTitle = new JPanel();
        pnlTitle.setBackground(PRIMARY_BROWN);
        JLabel lblTitle = new JLabel("THÔNG TIN DỊCH VỤ MỚI");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlTitle.add(lblTitle);
        add(pnlTitle, BorderLayout.NORTH);

        // --- Form ---
        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 20));
        pnlForm.setBorder(new EmptyBorder(30, 40, 30, 40));
        pnlForm.setBackground(Color.WHITE);

        pnlForm.add(new JLabel("Mã Dịch Vụ:"));
        txtMaDV = createStyledTextField();
        pnlForm.add(txtMaDV);

        pnlForm.add(new JLabel("Tên Dịch Vụ:"));
        txtTenDV = createStyledTextField();
        pnlForm.add(txtTenDV);

        pnlForm.add(new JLabel("Loại Dịch Vụ:"));
        cbLoaiDV = new JComboBox<>(new String[]{"Ẩm thực", "Giải trí", "Sức khỏe", "Tiện ích"});
        cbLoaiDV.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlForm.add(cbLoaiDV);

        pnlForm.add(new JLabel("Đơn Giá Gốc:"));
        txtGiaGoc = createStyledTextField();
        pnlForm.add(txtGiaGoc);

        pnlForm.add(new JLabel("Đơn Vị Tính:"));
        txtDonVi = createStyledTextField();
        pnlForm.add(txtDonVi);

        add(pnlForm, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(new Color(245, 245, 245));

        btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());
        
        btnLuu = new JButton("Lưu Dịch Vụ");
        btnLuu.setBackground(PRIMARY_BROWN);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFocusPainted(false);
        btnLuu.setFont(FONT_BOLD);
        btnLuu.setPreferredSize(new Dimension(120, 35));
        
        btnLuu.addActionListener(e -> xuLyLuu());

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return txt;
    }

    private void xuLyLuu() {
        // 1. Kiểm tra dữ liệu trống
        if(txtMaDV.getText().isEmpty() || txtTenDV.getText().isEmpty() || txtGiaGoc.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Tạo đối tượng Entity
        try {
            DichVu dv = new DichVu();
            dv.setMaDV(txtMaDV.getText().trim());
            dv.setTenDV(txtTenDV.getText().trim());
            dv.setLoaiDV(cbLoaiDV.getSelectedItem().toString());
            dv.setGia(Double.parseDouble(txtGiaGoc.getText().trim()));
            dv.setDonVi(txtDonVi.getText().trim()); 

            // 3. Gọi DAO để lưu
            DichVuDAO dao = new DichVuDAO();
            if (dao.insert(dv)) { // Giả sử hàm addService đã viết trong DAO
                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: Mã dịch vụ có thể đã tồn tại!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá tiền phải là con số!");
        }
    }
}