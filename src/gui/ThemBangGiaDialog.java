package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import dao.BangGiaDichVuDAO;
import dao.DichVuDAO;
import model.entities.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThemBangGiaDialog extends JDialog {
    private JTextField txtMaBG, txtTenBG;
    private JSpinner spNgayAD, spNgayHet;
    private JTable tblDichVu;
    private DefaultTableModel model;
    private JButton btnLuu, btnHuy;
    
    // Định nghĩa bảng màu Luxury
    private final Color PRIMARY_BROWN = new Color(74, 45, 42); 
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);

    public ThemBangGiaDialog(Frame parent) {
        super(parent, "Thiết lập bảng giá dịch vụ mới", true);
        setSize(1000, 700); // Tăng kích thước để thoáng hơn
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- 1. PHẦN TOP: Thông tin bảng giá ---
        JPanel pnlInfo = new JPanel(new GridLayout(2, 4, 20, 15));
        pnlInfo.setBackground(Color.WHITE);
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), "THÔNG TIN CHUNG"
        );
        titledBorder.setTitleFont(FONT_BOLD);
        titledBorder.setTitleColor(PRIMARY_BROWN);
        
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 20, 20, 20), titledBorder
        ));

        // Thêm các thành phần nhập liệu (Sử dụng hàm helper để tái sử dụng style)
        pnlInfo.add(createStyledLabel("Mã bảng giá:"));
        txtMaBG = createStyledTextField("BG" + System.currentTimeMillis() % 10000);
        txtMaBG.setEditable(false); // Thường mã không nên cho sửa tay
        pnlInfo.add(txtMaBG);

        pnlInfo.add(createStyledLabel("Tên bảng giá:"));
        txtTenBG = createStyledTextField("");
        pnlInfo.add(txtTenBG);

        pnlInfo.add(createStyledLabel("Ngày áp dụng:"));
        spNgayAD = createStyledDateSpinner();
        pnlInfo.add(spNgayAD);

        pnlInfo.add(createStyledLabel("Ngày hết hạn:"));
        spNgayHet = createStyledDateSpinner();
        pnlInfo.add(spNgayHet);

        // --- 2. PHẦN CENTER: Bảng danh sách ---
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(new EmptyBorder(0, 20, 10, 20));

        String[] columns = {"Mã DV", "Tên Dịch Vụ", "Loại", "Giá Gốc (VNĐ)", "Giá Áp Dụng (Nhập)"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        tblDichVu = new JTable(model);
        tblDichVu.getTableHeader().setReorderingAllowed(true);
        applyTableStyle(tblDichVu);
        loadServices();

        JScrollPane scrollPane = new JScrollPane(tblDichVu);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        pnlTable.add(scrollPane, BorderLayout.CENTER);

        // --- 3. PHẦN BOTTOM: Nút bấm ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(LIGHT_GRAY);
        
        btnHuy = new JButton("Hủy bỏ");
        applyButtonStyle(btnHuy, false);
        btnHuy.addActionListener(e -> dispose());

        btnLuu = new JButton("Lưu bảng giá");
        applyButtonStyle(btnLuu, true);
        btnLuu.addActionListener(e -> xuLyLuu());

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);

        add(pnlInfo, BorderLayout.NORTH);
        add(pnlTable, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // --- CÁC HÀM HELPER STYLE ---

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        return lbl;
    }

    private JTextField createStyledTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(FONT_PLAIN);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    private JSpinner createStyledDateSpinner() {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setFont(FONT_PLAIN);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "dd/MM/yyyy");
        sp.setEditor(editor);
        editor.getTextField().setBorder(new EmptyBorder(5, 5, 5, 5));
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return sp;
    }

    private void applyTableStyle(JTable table) {
        table.setRowHeight(35);
        table.setFont(FONT_PLAIN);
        table.setSelectionBackground(new Color(240, 235, 230));
        table.setSelectionForeground(PRIMARY_BROWN);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setBackground(PRIMARY_BROWN);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BOLD);

        // Căn giữa các cột số liệu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    }

    private void applyButtonStyle(JButton btn, boolean isPrimary) {
        btn.setFont(FONT_BOLD);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setFocusPainted(false);
        if (isPrimary) {
            btn.setBackground(PRIMARY_BROWN);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder());
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.GRAY);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }

    // Giữ nguyên loadServices() và xuLyLuu() của bạn...
    private void loadServices() {
        DichVuDAO dvDao = new DichVuDAO();
        List<DichVu> ds = dvDao.getAll();
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        for (DichVu dv : ds) {
            model.addRow(new Object[]{
                dv.getMaDV(), 
                dv.getTenDV(), 
                dv.getLoaiDV(), 
                df.format(dv.getGia()), 
                "" 
            });
        }
    }

    private void xuLyLuu() {
        if(txtTenBG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bảng giá!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BangGiaDichVu tt = new BangGiaDichVu();
        tt.setMaBangGia(txtMaBG.getText());
        tt.setTenBangGia(txtTenBG.getText());
        // Fix lỗi Date cast nếu cần thiết tùy thuộc vào Model của bạn
        java.util.Date utNgayAD = (java.util.Date) spNgayAD.getValue();
        java.util.Date utNgayHet = (java.util.Date) spNgayHet.getValue();

        // Chuyển sang java.sql.Date để set vào Entity
        tt.setNgayApDung(new java.sql.Date(utNgayAD.getTime()));
        tt.setNgayHetHieuLuc(new java.sql.Date(utNgayHet.getTime()));
        tt.setTrangThai(0); 

        List<BangGiaDichVu_ChiTiet> dsChiTiet = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 4);
            if (val != null && !val.toString().trim().isEmpty()) {
                try {
                    double giaApDung = Double.parseDouble(val.toString().replace(",", ""));
                    BangGiaDichVu_ChiTiet ct = new BangGiaDichVu_ChiTiet();
                    
                    DichVu dv = new DichVu();
                    dv.setMaDV(model.getValueAt(i, 0).toString());
                    ct.setMaDichVu(dv);
                    
                    ct.setGiaDichVu(giaApDung);
                    ct.setDonViTinh("Cái"); 
                    dsChiTiet.add(ct);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Giá tại dòng " + (i+1) + " không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (dsChiTiet.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải nhập giá cho ít nhất 1 dịch vụ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BangGiaDichVuDAO bgDao = new BangGiaDichVuDAO();
        if (bgDao.insertFullBangGia(tt, dsChiTiet)) {
            JOptionPane.showMessageDialog(this, "Đã thêm bảng giá mới thành công!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi lưu bảng giá!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}