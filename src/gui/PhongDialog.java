package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import model.entities.LoaiPhong;
import model.entities.Phong;
import model.enums.TenLoaiPhong;
import model.enums.TrangThaiPhong;
import dao.PhongDAO;
import dao.LoaiPhongDAO;

public class PhongDialog extends JDialog {
    private JTextField txtMaPhong, txtSucChua;
    private JComboBox<TenLoaiPhong> cbLoaiPhong;
    private JComboBox<String> cbTrangThai;
    private JComboBox<Integer> cbSoTang;
    private JButton btnSave, btnCancel;
    private JLabel lblTitle; 
    
    private PhongDAO phongDAO = new PhongDAO();
    private QuanLyPhongPanel parentPanel;
    
    private boolean isEditMode = false;
    private int editSoTang = 1; 
    private int pX, pY; 

    public PhongDialog(QuanLyPhongPanel parentPanel) {
        this.parentPanel = parentPanel;
        initUI();
        lblTitle.setText("Thêm phòng mới");
        btnSave.setText("Thêm phòng");
        
        updateSucChua();
        generateMaPhong();
    }

    public PhongDialog(QuanLyPhongPanel parentPanel, String maPhong, String loaiPhong, String trangThai, int soTang) {
        this.parentPanel = parentPanel;
        this.isEditMode = true; 
        this.editSoTang = soTang; 
        
        initUI(); 
        
        lblTitle.setText("Cập nhật phòng");
        btnSave.setText("Cập nhật");
        
        txtMaPhong.setText(maPhong.trim());
        
        try {
            cbLoaiPhong.setSelectedItem(TenLoaiPhong.valueOf(loaiPhong.trim()));
            updateSucChua();
        } catch (Exception e) {
            System.err.println("Không thể parse loại phòng: " + loaiPhong);
        }
        
        cbTrangThai.setSelectedItem(trangThai.trim());
    }

    private void initUI() {
        setUndecorated(true);
        setSize(420, 620); 
        setLocationRelativeTo(null); 
        setModal(true); 
        setLayout(new BorderLayout());
        getRootPane().setBorder(new LineBorder(Color.GRAY, 1)); 
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(62, 39, 35)); 
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlTitle.setOpaque(false);
        
        lblTitle = new JLabel("Thêm phòng mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSubTitle = new JLabel("Vui lòng điền đầy đủ thông tin phòng");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(new Color(243, 156, 18)); 
        
        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubTitle);

        JLabel lblClose = new JLabel("X");
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblClose.setForeground(Color.WHITE);
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { dispose(); }
        });

        pnlHeader.add(pnlTitle, BorderLayout.CENTER);
        pnlHeader.add(lblClose, BorderLayout.EAST);

        pnlHeader.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) { pX = me.getX(); pY = me.getY(); }
        });
        pnlHeader.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent me) {
                setLocation(getLocation().x + me.getX() - pX, getLocation().y + me.getY() - pY);
            }
        });

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 30, 10, 30));

        JComponent componentSoTang;
        if (isEditMode) {
            JTextField txtTang = createReadOnlyTextField();
            txtTang.setText(String.valueOf(editSoTang));
            componentSoTang = txtTang;
        } else {
            Integer[] floors = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            cbSoTang = new JComboBox<>(floors);
            cbSoTang.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
            cbSoTang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            cbSoTang.setBackground(Color.WHITE);
            cbSoTang.addActionListener(e -> generateMaPhong());
            componentSoTang = cbSoTang;
        }

        txtMaPhong = createReadOnlyTextField();

        cbLoaiPhong = new JComboBox<>(TenLoaiPhong.values());
        cbLoaiPhong.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        cbLoaiPhong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cbLoaiPhong.setBackground(Color.WHITE);
        cbLoaiPhong.addActionListener(e -> updateSucChua());

        txtSucChua = createReadOnlyTextField();

        cbTrangThai = new JComboBox<>(new String[]{"Còn trống", "Đã có khách", "Đang bảo trì"});
        cbTrangThai.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        cbTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cbTrangThai.setBackground(Color.WHITE);

        addFieldGroup(pnlBody, "Số tầng", componentSoTang, isEditMode ? "Không thể thay đổi tầng khi cập nhật" : "Chọn tầng từ 1 đến 10");
        addFieldGroup(pnlBody, "Mã phòng", txtMaPhong, "Tự động phát sinh theo tầng");
        addFieldGroup(pnlBody, "Loại phòng", cbLoaiPhong, null);
        addFieldGroup(pnlBody, "Sức chứa", txtSucChua, "Tự động lấy theo loại phòng từ DB");
        addFieldGroup(pnlBody, "Trạng thái", cbTrangThai, null);

        add(pnlBody, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(0, 20, 15, 20));

        btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(Color.BLACK);
        btnCancel.setFocusPainted(false);

        btnSave = new JButton(isEditMode ? "Cập nhật" : "Thêm phòng");
        btnSave.setPreferredSize(new Dimension(130, 38));
        btnSave.setBackground(new Color(62, 39, 35)); 
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        pnlBottom.add(btnCancel);
        pnlBottom.add(btnSave);
        add(pnlBottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> savePhong());
    }

    // LOAD SỨC CHỨA TỪ DB
    private void updateSucChua() {
        TenLoaiPhong loaiDuocChon = (TenLoaiPhong) cbLoaiPhong.getSelectedItem();
        if (loaiDuocChon != null) {
            LoaiPhongDAO lpDAO = new LoaiPhongDAO();
            List<model.entities.LoaiPhong> listLP = lpDAO.getAll();
            for (model.entities.LoaiPhong lp : listLP) {
                if (lp.getTenLoaiPhong() == loaiDuocChon) {
                    txtSucChua.setText(lp.getSucChua() + " người");
                    txtSucChua.setForeground(Color.BLACK);
                    break;
                }
            }
        }
    }

    private void generateMaPhong() {
        if (isEditMode) return;
        int floor = (Integer) cbSoTang.getSelectedItem();
        List<Phong> danhSachHienTai = phongDAO.getAll(); 
        String maMoi = Phong.phatSinhMaPhong(floor, danhSachHienTai);
        
        if ("FULL".equals(maMoi)) {
            txtMaPhong.setText("Tầng " + floor + " đã tối đa 10 phòng!");
            txtMaPhong.setForeground(Color.RED); 
            btnSave.setEnabled(false);
        } else {
            txtMaPhong.setText(maMoi);
            txtMaPhong.setForeground(Color.BLACK); 
            btnSave.setEnabled(true);
        }
    }

    private void savePhong() {
        String ma = txtMaPhong.getText().trim();
        int tang = isEditMode ? editSoTang : (int) cbSoTang.getSelectedItem();

        if (ma.isEmpty() || ma.contains("tối đa")) {
            JOptionPane.showMessageDialog(this, "Mã phòng không hợp lệ!");
            return;
        }

        TenLoaiPhong loai = (TenLoaiPhong) cbLoaiPhong.getSelectedItem();
        String ttStr = cbTrangThai.getSelectedItem().toString().trim();
        TrangThaiPhong tt = ttStr.equals("Còn trống") ? TrangThaiPhong.CONTRONG :
                            (ttStr.equals("Đã có khách") ? TrangThaiPhong.DACOKHACH : TrangThaiPhong.BAN);

        Phong p = new Phong(ma, new LoaiPhong(loai), tt, tang);
        
        if (isEditMode) {
            if (phongDAO.update(p)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                parentPanel.initData(); 
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Vui lòng kiểm tra lại!");
            }
        } else {
            if (phongDAO.insert(p)) {
                JOptionPane.showMessageDialog(this, "Thêm phòng thành công!");
                parentPanel.initData(); 
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại. Có thể trùng mã phòng!");
            }
        }
    }

    private JTextField createReadOnlyTextField() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 210, 210)), 
            new EmptyBorder(5, 10, 5, 10)
        ));
        txt.setEditable(false);
        txt.setFocusable(false); 
        txt.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
        txt.setBackground(new Color(245, 245, 245)); 
        txt.setForeground(Color.BLACK); 
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return txt;
    }

    private void addFieldGroup(JPanel parent, String labelText, JComponent input, String subLabel) {
        JLabel lbl = new JLabel("<html><font color='red'>*</font> " + labelText + "</html>");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createRigidArea(new Dimension(0, 5))); 
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(input);

        if (subLabel != null) {
            parent.add(Box.createRigidArea(new Dimension(0, 3)));
            JLabel lblSub = new JLabel(subLabel);
            lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblSub.setForeground(new Color(211, 84, 0));
            lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(lblSub);
            parent.add(Box.createRigidArea(new Dimension(0, 15)));
        } else {
            parent.add(Box.createRigidArea(new Dimension(0, 20))); 
        }
    }
}