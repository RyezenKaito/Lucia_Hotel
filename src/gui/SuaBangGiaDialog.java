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

public class SuaBangGiaDialog extends JDialog {
    private JTextField txtMaBG, txtTenBG;
    private JSpinner spNgayAD, spNgayHet;
    private JTable tblDichVu;
    private DefaultTableModel model;
    private JButton btnLuu, btnHuy;

    // Tông màu Luxury
    private final Color PRIMARY_BROWN = new Color(74, 45, 42);
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);

    public SuaBangGiaDialog(Frame parent, BangGiaDichVu bg, List<BangGiaDichVu_ChiTiet> dsChiTiet) {
        super(parent, "Chỉnh sửa bảng giá dịch vụ", true);
        setSize(1000, 700);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // --- 1. NORTH: Thông tin chung ---
        JPanel pnlInfo = createTopPanel(bg);

        // --- 2. CENTER: Bảng giá chi tiết ---
        JPanel pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.setBackground(Color.WHITE);
        pnlTableContainer.setBorder(new EmptyBorder(0, 20, 10, 20));

        // Nút thao tác trên bảng
        JPanel pnlTableActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTableActions.setBackground(Color.WHITE);

        JButton btnThemDV = new JButton(" + Thêm dịch vụ");
        styleActionButton(btnThemDV, new Color(40, 167, 69));
        
        JButton btnXoaDV = new JButton(" - Loại bỏ dòng chọn");
        styleActionButton(btnXoaDV, new Color(220, 53, 69));

        pnlTableActions.add(btnThemDV);
        pnlTableActions.add(btnXoaDV);

        // Khởi tạo Table
        String[] columns = {"Mã DV", "Tên Dịch Vụ", "Loại", "Giá Gốc", "Giá Áp Dụng"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 4; }
        };
        tblDichVu = new JTable(model);
        tblDichVu.getTableHeader().setReorderingAllowed(false);
        applyTableStyle(tblDichVu);

        // Đổ dữ liệu chi tiết vào bảng
        DichVuDAO dvDao = new DichVuDAO();
        for (BangGiaDichVu_ChiTiet ct : dsChiTiet) {
            DichVu dv = dvDao.getServiceByID(ct.getMaDichVu().getMaDV());
            if (dv != null) {
                model.addRow(new Object[]{
                    dv.getMaDV(), dv.getTenDV(), dv.getLoaiDV(), 
                    dv.getGia(), ct.getGiaDichVu()
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(tblDichVu);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        pnlTableContainer.add(pnlTableActions, BorderLayout.NORTH);
        pnlTableContainer.add(scrollPane, BorderLayout.CENTER);

        // --- 3. SOUTH: Nút bấm điều khiển ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(new Color(245, 245, 245));

        btnHuy = new JButton("Hủy bỏ");
        applyButtonStyle(btnHuy, false);
        btnLuu = new JButton("Cập nhật thay đổi");
        applyButtonStyle(btnLuu, true);

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnLuu);

        // --- Gắn Sự Kiện ---
        btnXoaDV.addActionListener(e -> {
            int row = tblDichVu.getSelectedRow();
            if (row != -1) model.removeRow(row);
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ để xóa!");
        });

        btnThemDV.addActionListener(e -> moDialogChonDichVu());
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> xuLyCapNhat());

        // --- Thêm vào Dialog ---
        add(pnlInfo, BorderLayout.NORTH);
        add(pnlTableContainer, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel(BangGiaDichVu bg) {
        JPanel pnlInfo = new JPanel(new GridLayout(2, 4, 20, 15));
        pnlInfo.setBackground(Color.WHITE);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "CẬP NHẬT THÔNG TIN"
        );
        titledBorder.setTitleFont(FONT_BOLD);
        titledBorder.setTitleColor(PRIMARY_BROWN);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(20, 20, 20, 20), titledBorder));

        pnlInfo.add(createStyledLabel("Mã bảng giá:"));
        txtMaBG = createStyledTextField(bg.getMaBangGia());
        txtMaBG.setEditable(false);
        txtMaBG.setBackground(new Color(245, 245, 245));
        pnlInfo.add(txtMaBG);

        pnlInfo.add(createStyledLabel("Tên bảng giá:"));
        txtTenBG = createStyledTextField(bg.getTenBangGia());
        pnlInfo.add(txtTenBG);

        pnlInfo.add(createStyledLabel("Ngày áp dụng:"));
        spNgayAD = createStyledDateSpinner(bg.getNgayApDung());
        pnlInfo.add(spNgayAD);

        pnlInfo.add(createStyledLabel("Ngày hết hạn:"));
        spNgayHet = createStyledDateSpinner(bg.getNgayHetHieuLuc());
        pnlInfo.add(spNgayHet);

        return pnlInfo;
    }

    private void moDialogChonDichVu() {
        JDialog dialog = new JDialog(this, "Chọn dịch vụ thêm vào bảng giá", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        DichVuDAO dvDao = new DichVuDAO();
        List<DichVu> dsTatCa = dvDao.getAll(); 
        
        String[] cols = {"Mã DV", "Tên Dịch Vụ", "Giá Gốc"};
        DefaultTableModel modelChon = new DefaultTableModel(cols, 0);
        for (DichVu dv : dsTatCa) {
            boolean exists = false;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).equals(dv.getMaDV())) {
                    exists = true; break;
                }
            }
            if (!exists) modelChon.addRow(new Object[]{dv.getMaDV(), dv.getTenDV(), dv.getGia()});
        }

        JTable tblChon = new JTable(modelChon);
        JButton btnXacNhan = new JButton("Xác nhận thêm");
        btnXacNhan.setFont(FONT_BOLD);
        btnXacNhan.setBackground(PRIMARY_BROWN);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(0, 45));

        btnXacNhan.addActionListener(e -> {
            int[] rows = tblChon.getSelectedRows();
            for (int row : rows) {
                model.addRow(new Object[]{
                    modelChon.getValueAt(row, 0), modelChon.getValueAt(row, 1), 
                    "Dịch vụ", modelChon.getValueAt(row, 2), modelChon.getValueAt(row, 2)
                });
            }
            dialog.dispose();
        });

        dialog.add(new JScrollPane(tblChon), BorderLayout.CENTER);
        dialog.add(btnXacNhan, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void xuLyCapNhat() {
        if(txtTenBG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên bảng giá không được để trống!");
            return;
        }

        BangGiaDichVu bg = new BangGiaDichVu();
        bg.setMaBangGia(txtMaBG.getText());
        bg.setTenBangGia(txtTenBG.getText());
        bg.setNgayApDung(new java.sql.Date(((Date) spNgayAD.getValue()).getTime()));
        bg.setNgayHetHieuLuc(new java.sql.Date(((Date) spNgayHet.getValue()).getTime()));

        List<BangGiaDichVu_ChiTiet> dsMoi = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 4);
            if (val != null) {
                BangGiaDichVu_ChiTiet ct = new BangGiaDichVu_ChiTiet();
                ct.setMaDichVu(new DichVu(model.getValueAt(i, 0).toString()));
                ct.setGiaDichVu(Double.parseDouble(val.toString()));
                ct.setDonViTinh("Cái");
                dsMoi.add(ct);
            }
        }

        if (new BangGiaDichVuDAO().updateFullBangGia(bg, dsMoi)) {
            JOptionPane.showMessageDialog(this, "Cập nhật bảng giá thành công!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật dữ liệu!");
        }
    }

    // --- Phụ trợ UI ---
    private void styleActionButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        return lbl;
    }

    private JTextField createStyledTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(FONT_PLAIN);
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)));
        return txt;
    }

    private JSpinner createStyledDateSpinner(Date value) {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setValue(value);
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return sp;
    }

    private void applyTableStyle(JTable table) {
        table.setRowHeight(35);
        table.setFont(FONT_PLAIN);
        table.setShowGrid(false);
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setBackground(PRIMARY_BROWN);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BOLD);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    private void applyButtonStyle(JButton btn, boolean isPrimary) {
        btn.setFont(FONT_BOLD);
        btn.setPreferredSize(new Dimension(180, 40));
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
}