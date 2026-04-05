package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KhachHangFrame extends JFrame {

    private JTextField txtMaKH;
    private JTextField txtTenKH;
    private JTextField txtSDT;
    private JTextField txtCCCD;

    private JButton btnThem;
    private JButton btnHuy;

    public KhachHangFrame() {

        setTitle("Thêm Khách Hàng");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5,2,10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel lblMaKH = new JLabel("Mã khách hàng");
        JLabel lblTenKH = new JLabel("Tên khách hàng");
        JLabel lblSDT = new JLabel("Số điện thoại");
        JLabel lblCCCD = new JLabel("CCCD");

        txtMaKH = new JTextField();
        txtTenKH = new JTextField();
        txtSDT = new JTextField();
        txtCCCD = new JTextField();

        btnThem = new JButton("Thêm");
        btnHuy = new JButton("Hủy");

        panel.add(lblMaKH);
        panel.add(txtMaKH);

        panel.add(lblTenKH);
        panel.add(txtTenKH);

        panel.add(lblSDT);
        panel.add(txtSDT);

        panel.add(lblCCCD);
        panel.add(txtCCCD);

        panel.add(btnThem);
        panel.add(btnHuy);

        add(panel);

        // sự kiện nút thêm
        btnThem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String maKH = txtMaKH.getText();
                String tenKH = txtTenKH.getText();
                String sdt = txtSDT.getText();
                String cccd = txtCCCD.getText();

                System.out.println("Mã KH: " + maKH);
                System.out.println("Tên KH: " + tenKH);
                System.out.println("SDT: " + sdt);
                System.out.println("CCCD: " + cccd);

                JOptionPane.showMessageDialog(null,"Thêm khách hàng thành công");
            }
        });

        btnHuy.addActionListener(e -> dispose());
    }
}
