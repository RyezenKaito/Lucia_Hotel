package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import dao.BangGiaPhongDAO;
import dao.LoaiPhongDAO;
import dao.PhongDAO;
import model.entities.BangGiaPhong;
import model.entities.Phong;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("serial")
public class TaoDonDatPhongPanel extends JPanel implements ActionListener, FocusListener, DocumentListener{
	private JTextField txtFullName, txtPhone, txtID;
	private JComboBox<String> cbRoomType;
	private LoaiPhongDAO lpDAO = new LoaiPhongDAO();
	private BangGiaPhongDAO bgDAO = new BangGiaPhongDAO();
	private JDateChooser dcCheckIn, dcCheckOut;
	private JButton btnBooking;
	private JTextField txtGuests;
	private JPanel pnlRoomDetailsContainer;
	private JTextField txtTienCoc;
	private JLabel lblSumName;
	private JLabel lblSumPhone;
	private JLabel lblSumRoomType;
	private JLabel lblSumGuests;
	private JLabel lblSumCheckIn;
	private JLabel lblSumCheckOut;
	private JLabel lblSumDays;
	private Container pnlCalculation;
	private JLabel lblSumRoomPrice;
	private JLabel lblSumDeposit;
	private JLabel lblSumTotal;
	private JButton btnCancel;
	private String nameStaff;
	private final DecimalFormat df = new DecimalFormat("#,###");
	private JPanel roomSelection;
	private PhongDAO phongDAO = new PhongDAO();
	private JButton btnOpenRoomSelect;
	private JLabel lblSumRoomSelected;
	private String selectedRooms= "";
	

    public TaoDonDatPhongPanel(String nameStaff) {
    	this.nameStaff = nameStaff;
    	init();    
    }
    public TaoDonDatPhongPanel() {
    	init();  
    }
    private void init() {
        setLayout(new BorderLayout(25,25));
        setBackground(new Color(245,241,234));
        setBorder(new EmptyBorder(25,25,25,25));
        
        lblSumRoomPrice = new JLabel("0 VND");
        lblSumDeposit = new JLabel("0 VND");
        lblSumTotal = new JLabel("0 VND");
        lblSumName = new JLabel("—");
        lblSumPhone = new JLabel("—");
        lblSumRoomType = new JLabel("—");
        lblSumGuests = new JLabel("1");
        lblSumCheckIn = new JLabel("—");
        lblSumCheckOut = new JLabel("—");
        lblSumDays = new JLabel("—");
        lblSumRoomSelected = new JLabel("Chưa chọn");

        // Tạo left panel (nội dung cần cuộn)
        JPanel leftPanel = createLeftPanel();
        
        // Tạo JScrollPane với thanh cuộn mỏng, đẹp
        JScrollPane leftScrollPane = new JScrollPane(leftPanel);
        leftScrollPane.setBorder(null);
        leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Tùy chỉnh thanh cuộn mỏng, không nút lên/xuống
        JScrollBar verticalBar = leftScrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(6, 0));
        verticalBar.setBackground(new Color(230, 230, 230));
        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(160, 160, 160);
                this.trackColor = new Color(245, 245, 245);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 6, 6);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
        
        // Thêm vào giao diện
        add(leftScrollPane, BorderLayout.CENTER);
        add(createSummaryPanel(), BorderLayout.EAST);
        java.beans.PropertyChangeListener dateChange = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                updateSummary();
                updateCalculation();
                triggerLoadRoom();
            }
        };
        dcCheckIn.addPropertyChangeListener(dateChange);
        dcCheckOut.addPropertyChangeListener(dateChange);
        
        updateRoomDetails();
        updateCalculation();
        triggerLoadRoom();
    }
    

    private JPanel createLeftPanel(){

        JPanel pnlLeftBox = new JPanel();
        pnlLeftBox.setOpaque(false);
        pnlLeftBox.setLayout(new BoxLayout(pnlLeftBox,BoxLayout.Y_AXIS));

        pnlLeftBox.add(createGuestPanel());
        pnlLeftBox.add(Box.createVerticalStrut(25));
        pnlLeftBox.add(createStayPanel());
        pnlLeftBox.add(Box.createVerticalStrut(25));
        
        pnlRoomDetailsContainer = new JPanel(new BorderLayout());
        pnlRoomDetailsContainer.setOpaque(false);
        
        updateRoomDetails(); 
        
        pnlLeftBox.add(pnlRoomDetailsContainer);
        pnlLeftBox.add(Box.createVerticalStrut(25));
        pnlLeftBox.add(createStepChoiceRoomPanel());

        return pnlLeftBox;
    }
    private JPanel createRoomTypeDetails(BangGiaPhong bgp){

        JPanel pnlBox= new JPanel(new BorderLayout());
        pnlBox.setBackground(Color.WHITE);
        pnlBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,220,220)),
                new EmptyBorder(25,25,25,25)));

        JLabel lblTitle = new JLabel("Loại phòng");
        lblTitle.setFont(new Font("Serif",Font.BOLD,20));
        lblTitle.setForeground(new Color(150,100,60));

        JPanel pnlInfoRoomType = new JPanel(new GridLayout(1,3,20,0));
        pnlInfoRoomType.setOpaque(false);

        pnlInfoRoomType.add(createBox("TÊN LOẠI PHÒNG", bgp.getLoaiPhong().getTenLoai().toString()));
        pnlInfoRoomType.add(createBox("GIÁ CƠ BẢN",bgp.getDonGia() +"đ/Đêm"));
        lblSumRoomPrice.setText(bgp.getDonGia()+"");
        pnlInfoRoomType.add(createBox("SỐ KHÁCH TỐI ĐA", bgp.getLoaiPhong().getSucChua() +" người"));

        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop,BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);

        pnlTop.add(lblTitle);
        pnlTop.add(Box.createVerticalStrut(5));
        

        pnlBox.add(pnlTop,BorderLayout.NORTH);
        pnlBox.add(pnlInfoRoomType,BorderLayout.CENTER);

        return pnlBox;
    }
    private JPanel createBox(String lblTitle,String value){

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(230,225,215));
        box.setBorder(new LineBorder(new Color(210,200,190)));

        JLabel t = new JLabel(lblTitle,SwingConstants.CENTER);
        t.setForeground(new Color(150,100,60));

        JLabel v = new JLabel(value,SwingConstants.CENTER);
        v.setFont(new Font("Serif",Font.BOLD,18));

        box.add(t,BorderLayout.NORTH);
        box.add(v,BorderLayout.CENTER);

        return box;
    }

    private JTextField createField(){
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(200,38));
        txt.setFont(new Font("Segoe UI",Font.PLAIN,18));
        return txt;
    }

    private JPanel createGuestPanel(){

        JPanel pnlBox= new JPanel(new GridBagLayout());
        pnlBox.setBackground(Color.WHITE);
        pnlBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,220,220)),
                new EmptyBorder(25,25,25,25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblTitle = new JLabel("Thông tin khách hàng");
        lblTitle.setFont(new Font("Serif",Font.BOLD,22));
        lblTitle.setForeground(new Color(150,100,60));

        txtFullName = createField(); txtFullName.addFocusListener(this);txtFullName.getDocument().addDocumentListener(this);
        txtPhone = createField(); txtPhone.addFocusListener(this);
        txtPhone.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtPhone.getText();
                // Chỉ giữ lại số và giới hạn 10 ký tự
                String digits = text.replaceAll("[^\\d]", "");
                
                if (digits.length() > 10) {
                    digits = digits.substring(0, 10);
                }

                if (!text.equals(digits)) {
                    String finalDigits = digits;
                    SwingUtilities.invokeLater(() -> {
                        txtPhone.setText(finalDigits);
                    });
                }
                
                updateSummary(); // Cập nhật bảng tóm tắt bên phải
            }
        });
        ((AbstractDocument) txtPhone.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (string.matches("\\d+") && (currentText.length() + string.length() <= 10)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int futureLength = currentText.length() - length + text.length();
                if (text.matches("\\d*") && futureLength <= 10) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        txtID = createField(); txtID.addFocusListener(this);txtID.getDocument().addDocumentListener(this);
        ((AbstractDocument) txtID.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (string.matches("\\d+") && (currentText.length() + string.length() <= 12)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int futureLength = currentText.length() - length + text.length();
                if (text.matches("\\d*") && futureLength <= 12) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        pnlBox.add(lblTitle,gbc);

        gbc.gridy++;
        pnlBox.add(new JLabel("Họ và tên(*): "),gbc);

        gbc.gridy++;
        pnlBox.add(txtFullName,gbc);

        gbc.gridy++; gbc.gridwidth=1;
        gbc.gridx=0;
        pnlBox.add(new JLabel("Số điện thoại(*):"),gbc);

        gbc.gridx=1;
        pnlBox.add(new JLabel("CCCD (*):"),gbc);

        gbc.gridy++;
        gbc.gridx=0;
        pnlBox.add(txtPhone,gbc);

        gbc.gridx=1;
        pnlBox.add(txtID,gbc);

        return pnlBox;
    }

    private JPanel createStayPanel(){

        JPanel pnlBox= new JPanel(new GridBagLayout());
        pnlBox.setBackground(Color.WHITE);
        pnlBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,220,220)),
                new EmptyBorder(25,25,25,25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx=1;

        JLabel lblTitle = new JLabel("Chi tiết lưu trú");
        lblTitle.setFont(new Font("Serif",Font.BOLD,22));
        lblTitle.setForeground(new Color(150,100,60));

        dcCheckIn = new JDateChooser();
        dcCheckIn.setDateFormatString("dd/MM/yyyy");
        dcCheckIn.setPreferredSize(new Dimension(200, 38));
        dcCheckIn.getDateEditor().getUiComponent().setFont(new Font("Segoe UI", Font.PLAIN, 18));
        dcCheckIn.setDate(new java.util.Date()); 
        dcCheckIn.getJCalendar().setMinSelectableDate(new java.util.Date());
        
        dcCheckOut = new JDateChooser();
        dcCheckOut.setDateFormatString("dd/MM/yyyy");
        dcCheckOut.setPreferredSize(new Dimension(200, 38));
        dcCheckOut.getDateEditor().getUiComponent().setFont(new Font("Segoe UI", Font.PLAIN, 18));
        dcCheckOut.setDate(new java.util.Date());
        dcCheckOut.getJCalendar().setMinSelectableDate(dcCheckIn.getDate());
        ((JTextFieldDateEditor) dcCheckOut.getDateEditor()).addFocusListener(this);
        
        
        txtTienCoc = createField();
        txtTienCoc.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Tránh xử lý các phím điều hướng (mũi tên, home, end...)
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) return;

                String text = txtTienCoc.getText();
                if (!text.isEmpty()) {
                    try {
                        // Xóa bỏ tất cả ký tự không phải số (bao gồm cả dấu phẩy cũ)
                        String cleanString = text.replaceAll("[^\\d]", "");
                        if (!cleanString.isEmpty()) {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = df.format(parsed);
                            
                            // Chỉ set lại nếu nội dung hiển thị thực sự thay đổi (tránh lặp vô tận)
                            if (!text.equals(formatted)) {
                                SwingUtilities.invokeLater(() -> {
                                    txtTienCoc.setText(formatted);
                                });
                            }
                        }
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
                updateCalculation(); 
                updateSummary();
            }
        });

        ((AbstractDocument) txtTienCoc.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                    throws BadLocationException {
                if (string == null) return;
                // Cho phép số và dấu phẩy (vì khi setText nó sẽ đi qua filter này)
                if (string.matches("[\\d,]+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (text == null) return;
                if (text.matches("[\\d,]+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
//        JTextField note = createField();

        cbRoomType = new JComboBox<>(lpDAO.fetchAllRoomTypeNames());
        cbRoomType.addActionListener(this);

        txtGuests = createField();
        txtGuests.setText("1");
        txtGuests.getDocument().addDocumentListener(this);
        

        cbRoomType.setPreferredSize(new Dimension(200,38));
        txtGuests.setPreferredSize(new Dimension(200,38));
        
        

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        pnlBox.add(lblTitle,gbc);

        gbc.gridy++; gbc.gridwidth=1;
        gbc.gridx=0;
        pnlBox.add(new JLabel("Ngày nhận phòng(*):"),gbc);

        gbc.gridx=1;
        pnlBox.add(new JLabel("Ngày trả phòng(*):"),gbc);

        gbc.gridy++;
        gbc.gridx=0;
        pnlBox.add(dcCheckIn,gbc);

        gbc.gridx=1;
        pnlBox.add(dcCheckOut,gbc);

        gbc.gridy++;
        gbc.gridx=0;
        pnlBox.add(new JLabel("Loại phòng: "),gbc);

        gbc.gridx=1;
        pnlBox.add(new JLabel("Số người *"),gbc);

        gbc.gridy++;
        gbc.gridx=0;
        pnlBox.add(cbRoomType,gbc);

        gbc.gridx=1;
        pnlBox.add(txtGuests,gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        pnlBox.add(new JLabel("Số tiền đặt cọc (VND)"), gbc);

        gbc.gridy++;
        pnlBox.add(txtTienCoc, gbc);

        return pnlBox;
    }

    private JPanel createSummaryPanel(){

        JPanel pnlBox= new JPanel();
        pnlBox.setPreferredSize(new Dimension(320,500));
        pnlBox.setLayout(new BoxLayout(pnlBox,BoxLayout.Y_AXIS));
        pnlBox.setBackground(Color.WHITE);
        pnlBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,220,220)),
                new EmptyBorder(25,25,25,25)));

        Box row= Box.createHorizontalBox();
        JLabel lblTitle = new JLabel("Thông tin đặt phòng");
        row.add(lblTitle);
        pnlBox.add(row);
        lblTitle.setFont(new Font("Serif",Font.BOLD,22));
        pnlBox.add(Box.createVerticalStrut(25));
        

        pnlBox.add(createRow("Họ tên khách", lblSumName));
        pnlBox.add(createRow("Số điện thoại", lblSumPhone));
        pnlBox.add(createRow("Loại phòng", lblSumRoomType));
        pnlBox.add(createRow("Phòng đã chọn", lblSumRoomSelected));
        pnlBox.add(createRow("Số người", lblSumGuests));
        pnlBox.add(createRow("Nhận phòng", lblSumCheckIn));
        pnlBox.add(createRow("Trả phòng", lblSumCheckOut));
        pnlBox.add(createRow("Số ngày", lblSumDays));
        

        
        pnlBox.add(createCalculationPanel());
        pnlBox.add(Box.createVerticalStrut(25));

        btnBooking = new JButton("Tạo Đặt Phòng");
        btnBooking.setBackground(new Color(110,60,45));
        btnBooking.setForeground(Color.WHITE);
        btnBooking.setFocusPainted(false);
        btnBooking.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        btnBooking.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBooking.addActionListener(this);

        btnCancel = new JButton("Hủy");
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCancel.addActionListener(this);

        pnlBox.add(btnBooking);
        pnlBox.add(Box.createVerticalStrut(10));
        pnlBox.add(btnCancel);

        return pnlBox;
    }
    private void updateSummary() {
        lblSumName.setText(txtFullName.getText().isEmpty() ? "—" : txtFullName.getText());
        lblSumPhone.setText(txtPhone.getText().isEmpty() ? "—" : txtPhone.getText());
        lblSumRoomType.setText(cbRoomType.getSelectedItem().toString());
        lblSumGuests.setText(txtGuests.getText());
        lblSumGuests.setText(txtGuests.getText());
        lblSumDeposit.setText(txtTienCoc.getText());


        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (dcCheckIn.getDate() != null) {
            lblSumCheckIn.setText(dcCheckIn.getDate().toInstant().atZone(ZoneId.systemDefault()).format(fmt));
        }
        
        if (dcCheckOut.getDate() != null) {
            LocalDateTime in = dcCheckIn.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime out = dcCheckOut.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            lblSumCheckOut.setText(out.format(fmt));

            long days = java.time.temporal.ChronoUnit.DAYS.between(in.toLocalDate(), out.toLocalDate());
            lblSumDays.setText((days <= 0 ? 1 : days) + " ngày");
        }
    }

    private JPanel createRow(String name, JLabel valueLabel) {
        JPanel pnlRow = new JPanel(new BorderLayout());
        pnlRow.setOpaque(false);
        pnlRow.setBorder(new MatteBorder(0,0,1,0,new Color(230,230,230)));
        pnlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));

        pnlRow.add(new JLabel(name), BorderLayout.WEST);
        pnlRow.add(valueLabel, BorderLayout.EAST);

        return pnlRow;
    }
    
    /****
     *	Hàm kiểm tra các nhập dữ liệu vào JTextField có đúng định dạng yêu cầu  không
     * @param txtField :  JTextField
     * @param regex: String biểu thức chính qui
     * @param text1: String, thông báo khi txtField bị trống
     * @param text2: String, thông báo khi txtField khác biểu thức chính qui
     * @return true, txtField thõa điều kiện của biểu thức chính qui, ngược lại trả về false
     */
    private boolean checkInput(JTextField txtField, String regex, String text1, String text2) {
    	String txt = txtField.getText().trim();
    	if (txt.isEmpty()) {
    		JOptionPane.showMessageDialog(this, text1);
    		txtField.requestFocus();
    		return false;
    	}
    	
    	if(!txt.matches(regex)) {
    		JOptionPane.showMessageDialog(this, text2);
    		txtField.requestFocus();
    		return false;
    	}
    	return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(btnBooking)) {
            new ChiTietDatPhongFrame(lblSumName.getText(), lblSumPhone.getText(), txtID.getText(), 
                    lblSumRoomType.getText(), lblSumCheckIn.getText(), lblSumCheckOut.getText(), 
                    Integer.parseInt(lblSumGuests.getText().replace(",", "").replace(" VNĐ", "")), 
                    Double.parseDouble(lblSumDeposit.getText().replace(",", "").replace(" VNĐ", "")), 
                    Double.parseDouble(lblSumTotal.getText().replace(",", "").replace(" VND", ""))).setVisible(true);
                    
        } else if (source.equals(cbRoomType)) {
            updateRoomDetails();
            updateSummary();
            updateCalculation();
            triggerLoadRoom(); // Load lại phòng khi đổi loại phòng
            
        } else if (source.equals(btnCancel)) {
            // RESET TOÀN BỘ FORM
            txtFullName.setText("");
            txtPhone.setText("");
            txtID.setText("");
            txtTienCoc.setText("0");
            txtGuests.setText("1");
            
            lblSumName.setText("—");
            lblSumPhone.setText("—");
            lblSumRoomType.setText("—");
            lblSumGuests.setText("1");
            lblSumCheckIn.setText("—");
            lblSumCheckOut.setText("—");
            lblSumDays.setText("—");
            lblSumRoomPrice.setText("0 VND");
            lblSumDeposit.setText("0 VND");
            lblSumTotal.setText("0 VND");
            
            // Reset ngày về mặc định
            dcCheckIn.setDate(new java.util.Date());
            dcCheckOut.setDate(new java.util.Date());
            
            // Xóa danh sách phòng trống đang hiển thị
            if (roomSelection != null) {
                roomSelection.removeAll();
                roomSelection.revalidate();
                roomSelection.repaint();
            }
            
            triggerLoadRoom(); // Tải lại danh sách theo mặc định mới
        }else if (source.equals(btnOpenRoomSelect)) {
            if (dcCheckIn.getDate() == null || dcCheckOut.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày trước khi chọn phòng!");
                return;
            }

            String loaiPhong = cbRoomType.getSelectedItem().toString();
            
            // Khởi tạo Dialog
            ChonPhongFrame diag = new ChonPhongFrame(loaiPhong, dcCheckIn.getDate(), dcCheckOut.getDate());
            diag.setVisible(true); // Màn hình sẽ dừng tại đây cho đến khi Dialog đóng

            // Lấy kết quả sau khi Dialog đóng
            String result = diag.getSelectedRoomList();
            if (!result.isEmpty()) {
                selectedRooms = result;
                lblSumRoomSelected.setText(result); // Hiển thị mã phòng lên bảng tóm tắt
                btnOpenRoomSelect.setText("ĐÃ CHỌN: " + result);
                btnOpenRoomSelect.setBackground(new Color(220, 255, 220));
            }
        }
    }
    
    private void triggerLoadRoom() {
        // Kiểm tra xem đã có đủ ngày và loại phòng chưa mới gọi DAO
        if (dcCheckIn != null && dcCheckOut != null && dcCheckIn.getDate() != null && dcCheckOut.getDate() != null) {
            String loaiPhongSelected = cbRoomType.getSelectedItem().toString();
            loadPhongEmpty(loaiPhongSelected, dcCheckIn.getDate(), dcCheckOut.getDate());
        }
    }
	
	/***
	 * Tính toán tổng tiền phòng dựa trên đơn giá, số ngày ở và trừ đi tiền cọc.
	 */
	private void updateCalculation() {
	    try {
	        String roomType = cbRoomType.getSelectedItem().toString();
	        BangGiaPhong bgp = bgDAO.getPriceByNameRoomType(roomType);
	        double donGiaMotDem = (bgp != null) ? bgp.getDonGia() : 0;

	        long days = 1;
	        if (dcCheckIn.getDate() != null && dcCheckOut.getDate() != null) {
	            LocalDate d1 = dcCheckIn.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            LocalDate d2 = dcCheckOut.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	            days = java.time.temporal.ChronoUnit.DAYS.between(d1, d2);
	            if (days <= 0) days = 1; 
	        }

	        // LẤY TIỀN CỌC: Xóa tất cả ký tự không phải số trước khi parse
	        double tienCoc = 0;
	        String strCoc = txtTienCoc.getText().replaceAll("[^\\d]", ""); 
	        if (!strCoc.isEmpty()) {
	            tienCoc = Double.parseDouble(strCoc);
	        }

	        double totalRoomPrice = donGiaMotDem * days;
	        double remainingTotal = totalRoomPrice - tienCoc;

	        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
	        
	        lblSumRoomPrice.setText(df.format(totalRoomPrice) + " VND");
	        lblSumDeposit.setText("-" + df.format(tienCoc) + " VND");
	        lblSumTotal.setText(df.format(remainingTotal) + " VND");

	        pnlCalculation.setVisible(totalRoomPrice > 0);
	        pnlCalculation.revalidate();
	        pnlCalculation.repaint();

	    } catch (Exception e) {
	         e.printStackTrace();
	    }
	}
	
	private JPanel createCalculationPanel() {
	    pnlCalculation = new JPanel();
	    pnlCalculation.setLayout(new BoxLayout(pnlCalculation, BoxLayout.Y_AXIS));
	    ((JComponent) pnlCalculation).setOpaque(false);
	    ((JComponent) pnlCalculation).setBorder(new EmptyBorder(10, 0, 10, 0));
	    ((JComponent) pnlCalculation).setBackground(new Color(240,235,228));

	    lblSumTotal.setFont(new Font("Serif", Font.BOLD, 18));

	    pnlCalculation.add(createRow("Giá phòng", lblSumRoomPrice));
	    pnlCalculation.add(createRow("Tiền cọc", lblSumDeposit));
	    
	    JSeparator sep = new JSeparator();
	    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
	    pnlCalculation.add(Box.createVerticalStrut(10));
	    pnlCalculation.add(sep);
	    pnlCalculation.add(Box.createVerticalStrut(10));
	    
	    pnlCalculation.add(createRow("Tiền chưa thanh toán", lblSumTotal));

	    pnlCalculation.setVisible(false);
	    return (JPanel) pnlCalculation;
	}
	private void updateRoomDetails() {
	    String roomtype = cbRoomType.getSelectedItem().toString();
	    BangGiaPhong bgp = bgDAO.getPriceByNameRoomType(roomtype);
	    
	    if (bgp != null) {
	        pnlRoomDetailsContainer.removeAll();
	        pnlRoomDetailsContainer.add(createRoomTypeDetails(bgp));
	        
	        pnlRoomDetailsContainer.revalidate();
	        pnlRoomDetailsContainer.repaint();
	    }
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}
	private boolean checkDate() {
	    if (dcCheckIn.getDate() == null) {
	        JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày nhận phòng!");
	        return false;
	    }
	    if (dcCheckOut.getDate() == null) {
	        JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày trả phòng!");
	        return false;
	    }
	    if (dcCheckOut.getDate().before(dcCheckIn.getDate())) {
	        JOptionPane.showMessageDialog(this, "Ngày trả phòng phải sau ngày nhận phòng!");
	        return false;
	    }
	    return true;
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		Object source =e.getSource();
		if(source.equals(txtFullName)) {
			String vniRegex = "^([A-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯ][a-zàáâãèéêìíòóôõùúăđĩũơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹ]*)(\\s[A-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯ][a-zàáâãèéêìíòóôõùúăđĩũơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹ]*)+$";

			if(checkInput(txtFullName, vniRegex, "Họ tên không được bỏ trống!", "Họ tên phải viết hoa chữ cái đầu!")) {
			    txtFullName.setBorder(BorderFactory.createLineBorder(Color.GREEN));
			    
			} else {
			    txtFullName.setBorder(BorderFactory.createLineBorder(Color.RED));
			    return;
			}
		}else if(source.equals(txtPhone)) {
			String regex = "^(09|07|03|08|02|05)\\d{8}$";
			if(checkInput(txtPhone, regex, "Số điện thoại không được bỏ trống!", "Số điện thoại có 10 số.")) {
			    txtPhone.setBorder(BorderFactory.createLineBorder(Color.GREEN));return;
			} else {
			    txtPhone.setBorder(BorderFactory.createLineBorder(Color.RED));return;
			}
		}else if(source.equals(txtID)) {
			String regex = "^\\d{12}$";
			if(checkInput(txtID, regex, "CCCD không được bỏ trống!", "CCCD có 13 số.")) {
			    txtID.setBorder(BorderFactory.createLineBorder(Color.GREEN));return;
			} else {
			    txtID.setBorder(BorderFactory.createLineBorder(Color.RED));return;
			}
		}else if(source == dcCheckOut.getDateEditor().getUiComponent()) {
			if(checkDate()) {
				dcCheckOut.setBorder(BorderFactory.createLineBorder(Color.GREEN));
				return;
			}else {
				dcCheckOut.setBorder(BorderFactory.createLineBorder(Color.RED));
				return;
			}
		}
		
	}


	@Override public void insertUpdate(DocumentEvent e) { handleDocumentChange(e); }
    @Override public void removeUpdate(DocumentEvent e) { handleDocumentChange(e); }
    @Override public void changedUpdate(DocumentEvent e) { handleDocumentChange(e); }
	private void handleDocumentChange(DocumentEvent e) {
		Object doc = e.getDocument();

	    if (doc == txtTienCoc.getDocument()) {
	            updateCalculation();
	            updateSummary();
	    } 
	    else {
	        updateSummary();
	        if (doc == txtGuests.getDocument()) {
	            updateCalculation();
	        }
	    }
	}
	
	private JPanel createStepChoiceRoomPanel() {
	    JPanel step3 = new JPanel(new GridBagLayout());
	    step3.setBackground(Color.WHITE);
	    step3.setBorder(new CompoundBorder(
	            new LineBorder(new Color(220, 220, 220)),
	            new EmptyBorder(20	, 20, 20, 20)));

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(10, 10, 10, 10);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1;

	    JLabel lblTitle = new JLabel("Chọn phòng lưu trú");
	    lblTitle.setFont(new Font("Serif", Font.BOLD, 22));
	    lblTitle.setForeground(new Color(150, 100, 60));

	    btnOpenRoomSelect = new JButton("BẤM ĐỂ CHỌN PHÒNG TRỐNG");
	    btnOpenRoomSelect.setFont(new Font("Segoe UI", Font.BOLD, 15));
	    btnOpenRoomSelect.setBackground(new Color(245, 240, 230));
	    btnOpenRoomSelect.setForeground(new Color(110, 60, 45));
	    btnOpenRoomSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    btnOpenRoomSelect.setPreferredSize(new Dimension(0, 40));
	    btnOpenRoomSelect.addActionListener(this);

	    gbc.gridx = 0; gbc.gridy = 0;
	    step3.add(lblTitle, gbc);

	    gbc.gridy++;
	    step3.add(new JLabel("Hệ thống sẽ lọc các phòng trống theo loại phòng và thời gian đã chọn."), gbc);

	    gbc.gridy++;
	    step3.add(btnOpenRoomSelect, gbc);

	    return step3;
	}
	
	// Cập nhật lại createRoomCard để trông chuyên nghiệp hơn
	private JPanel createRoomCard(String roomID, String type) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setPreferredSize(new Dimension(120, 80));
        card.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80)));
        card.setBackground(new Color(232, 245, 233));

        JLabel lblID = new JLabel(roomID, SwingConstants.CENTER);
        lblID.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel lblType = new JLabel(type, SwingConstants.CENTER);
        
        card.add(lblID);
        card.add(lblType);
        return card;
    }
	
	public void loadPhongEmpty(String loaiPhong, java.util.Date dIn, java.util.Date dOut) {
		if (dIn == null || dOut == null || roomSelection == null) {
	        return; 
	    }

	    java.sql.Date sqlIn = new java.sql.Date(dIn.getTime());
	    java.sql.Date sqlOut = new java.sql.Date(dOut.getTime());

	    roomSelection.removeAll(); // Sẽ không còn lỗi ở đây nữa

        // Lấy dữ liệu mới
        List<Phong> ds = phongDAO .getDanhSachPhongTrong(loaiPhong, sqlIn, sqlOut);

        for (Phong p : ds) {
            String soPhongHienThi = p.getMaPhong().substring(1);
            roomSelection.add(createRoomCard(soPhongHienThi, p.getLoaiPhong().getTenLoai().toString()));
        }

        roomSelection.revalidate();
        roomSelection.repaint();
    }
}