package gui;

import connectDatabase.ConnectDatabase;
import dao.ChiTietDatPhongDAO;
import dao.DatPhongDAO;
import dao.KhachHangDAO;
import dao.LoaiPhongDAO;
import dao.PhongDAO;
import model.utils.DatePicker;
import model.utils.DimOverlay;
import model.utils.EventUtils;
import model.utils.ValidationUtils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * ThemSuaDatPhongDialog – Dialog thêm / sửa đơn đặt phòng.
 */
public class ThemSuaDatPhongDialog extends Stage {

    /* ── Bảng màu ─────────────────────────────────────────────────── */
    private static final String C_SIDEBAR   = "#1e3a8a";
    private static final String C_TEXT_GRAY  = "#6b7280";
    private static final String C_ACTIVE    = "#1d4ed8";
    private static final String C_BORDER    = "#e9ecef";
    private static final String C_ERROR     = "#dc2626";

    private static final DecimalFormat DF = new DecimalFormat("#,###");

    /* ── State ────────────────────────────────────────────────────── */
    private double xOffset = 0, yOffset = 0;

    private final Window owner;
    private final Object[] editRow;    // null = thêm mới
    private final Runnable onSuccess; 
    private String editMaDat;          // mã đặt khi sửa
    
    // Lưu vết phòng cũ để nạp lại vào ComboBox khi Sửa
    private String originalRoom = null; 
    private String originalRoomType = null;

    /* ── Form fields ──────────────────────────────────────────────── */
    private TextField txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtTienCoc, txtGhiChu, txtSoLuongPhong;
    private ComboBox<String> cbLoaiPhong, cbPhong;
    private DatePicker dpCheckIn, dpCheckOut, dpNgaySinh;
    private Label errTen, errSDT, errCCCD, errNgayIn, errNgayOut, errSoNguoi, errPhong, errNS, errSoLuongPhong;
    
    // Giao diện tiền bạc mới
    private Label lblTongTienPhong;
    private Label lblCanThanhToan;
    
    private Button btnSave;
    private Region overlay;

    /* ── DAO ──────────────────────────────────────────────────────── */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final ChiTietDatPhongDAO ctdpDAO = new ChiTietDatPhongDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    /* ── Constructor ──────────────────────────────────────────────── */
    public ThemSuaDatPhongDialog(Window owner, Object[] editRow, Runnable onSuccess) {
        this.owner = owner;
        this.editRow = editRow;
        this.onSuccess = onSuccess;

        if (editRow != null) this.editMaDat = (String) editRow[0];

        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED); 

        Scene scene = new Scene(buildRoot(), 600, 720); // Tăng height chút để chứa 2 dòng tính tiền
        scene.setFill(Color.WHITE);
        setScene(scene);
        centerOnScreen();

        initEvents();

        if (editRow != null) {
            Platform.runLater(this::populateEditData);
        }
    }

    public void showDialog() {
        if (owner != null) {
            this.overlay = DimOverlay.show(owner);
        }
        showAndWait();
        if (owner != null && this.overlay != null) {
            DimOverlay.hide(owner, this.overlay);
        }
    }

    private void initEvents() {
        try {
            if (txtHoTen != null && txtSoDT != null) {
                EventUtils.setupEnterToSave(() -> {
                    if (btnSave != null && !btnSave.isDisabled()) {
                        handleSave();
                    }
                }, txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtTienCoc, txtGhiChu, dpNgaySinh, dpCheckIn, dpCheckOut, cbLoaiPhong, cbPhong);
            }
        } catch (Exception e) {
            System.err.println("Bỏ qua lỗi EventUtils: " + e.getMessage());
        }
    }

    private VBox buildRoot() {
        VBox root = new VBox();
        root.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + C_SIDEBAR + ";" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");
        
        root.getChildren().addAll(
                buildHeader(),
                buildFormBody(),
                buildFooter());
                
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: " + C_SIDEBAR + ";");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label(editRow == null ? "THÊM ĐƠN ĐẶT PHÒNG" : "CẬP NHẬT ĐƠN ĐẶT PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setTextFill(Color.WHITE);

        Label lblSub = new Label(editRow == null
                ? "Điền đầy đủ thông tin bên dưới"
                : "Chỉnh sửa đơn đặt phòng " + editMaDat);
        lblSub.setFont(Font.font("Segoe UI", 12));
        lblSub.setTextFill(Color.web("#93c5fd"));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 10 4 10;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-background-radius: 4;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 4 10 4 10;"));
        btnClose.setOnAction(e -> close());

        header.setOnMousePressed(event -> { xOffset = event.getSceneX(); yOffset = event.getSceneY(); });
        header.setOnMouseDragged(event -> { setX(event.getScreenX() - xOffset); setY(event.getScreenY() - yOffset); });

        header.getChildren().addAll(titleBox, spacer, btnClose);
        return header;
    }

    private ScrollPane buildFormBody() {
        VBox form = new VBox(6);
        form.setPadding(new Insets(22, 32, 10, 32));
        form.setStyle("-fx-background-color: white;");

        int curYear = LocalDate.now().getYear();

        txtCCCD = makeField("", "Nhập CCCD (12 số)");
        try { ValidationUtils.applyNumericOnlyFilter(txtCCCD, 12); } catch (Exception ignored) {}
        errCCCD = errLabel();
        
        txtCCCD.focusedProperty().addListener((obs, o, n) -> {
            if (!n && txtCCCD.getText().length() == 12) {
                model.entities.KhachHang khExist = khachHangDAO.findByCCCD(txtCCCD.getText());
                if (khExist != null) {
                    if (txtHoTen != null) txtHoTen.setText(khExist.getTenKH());
                    if (txtSoDT != null) txtSoDT.setText(khExist.getSoDT());
                    if (dpNgaySinh != null && khExist.getNgaySinh() != null) dpNgaySinh.setValue(khExist.getNgaySinh());
                }
            }
        });

        txtHoTen = makeField("", "Nhập họ và tên khách hàng");
        errTen = errLabel();

        HBox rowCCCD_Ten = new HBox(16);
        VBox colCCCD = fieldBlock("Số CCCD (Nhập trước) *", txtCCCD, errCCCD, null);
        VBox colTen = fieldBlock("Họ và tên *", txtHoTen, errTen, null);
        HBox.setHgrow(colCCCD, Priority.ALWAYS); HBox.setHgrow(colTen, Priority.ALWAYS);
        rowCCCD_Ten.getChildren().addAll(colCCCD, colTen);
        form.getChildren().add(rowCCCD_Ten);

        dpNgaySinh = new DatePicker(curYear - 100, curYear + 25);
        dpNgaySinh.setPromptText("Chọn ngày sinh khách hàng");
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        errNS = errLabel();

        txtSoDT = makeField("", "Nhập số điện thoại");
        try { ValidationUtils.applyNumericOnlyFilter(txtSoDT, 10); } catch (Exception ignored) {}
        errSDT = errLabel();

        HBox rowNS_SDT = new HBox(16);
        VBox colNS = fieldBlock("Ngày sinh *", dpNgaySinh, errNS, "Vui lòng chọn ngày sinh (từ đủ 16 tuổi)");
        VBox colSDT = fieldBlock("Số điện thoại *", txtSoDT, errSDT, null);
        HBox.setHgrow(colNS, Priority.ALWAYS); HBox.setHgrow(colSDT, Priority.ALWAYS);
        rowNS_SDT.getChildren().addAll(colNS, colSDT);
        form.getChildren().add(rowNS_SDT);

        dpCheckIn = new DatePicker(curYear, curYear + 2);
        dpCheckIn.setPromptText("Chọn ngày nhận phòng");
        dpCheckIn.setValue(LocalDate.now());
        try { dpCheckIn.setMinDate(LocalDate.now()); } catch(Exception ignored){}
        dpCheckIn.setMaxWidth(Double.MAX_VALUE);

        dpCheckOut = new DatePicker(curYear, curYear + 2);
        dpCheckOut.setPromptText("Chọn ngày trả phòng");
        dpCheckOut.setValue(LocalDate.now().plusDays(1));
        try { dpCheckOut.setMinDate(LocalDate.now().plusDays(1)); } catch(Exception ignored){}
        dpCheckOut.setMaxWidth(Double.MAX_VALUE);

        dpCheckIn.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                try { dpCheckOut.setMinDate(n.plusDays(1)); } catch(Exception ignored){}
                if (dpCheckOut.getValue() != null && !dpCheckOut.getValue().isAfter(n))
                    dpCheckOut.setValue(n.plusDays(1));
            }
            updateTongTien(); reloadPhongTrong();
        });
        dpCheckOut.valueProperty().addListener((obs, o, n) -> { updateTongTien(); reloadPhongTrong(); });

        errNgayIn = errLabel(); errNgayOut = errLabel();

        HBox rowDate = new HBox(16);
        VBox colIn = fieldBlock("Ngày nhận phòng *", dpCheckIn, errNgayIn, null);
        VBox colOut = fieldBlock("Ngày trả phòng *", dpCheckOut, errNgayOut, null);
        HBox.setHgrow(colIn, Priority.ALWAYS); HBox.setHgrow(colOut, Priority.ALWAYS);
        rowDate.getChildren().addAll(colIn, colOut);
        form.getChildren().add(rowDate);

        // ──────────────────────────────────────────
        // Nhập Số Người, Số Lượng Phòng & Loại Phòng
        // ──────────────────────────────────────────
        txtSoNguoi = makeField("1", "Nhập số người");
        try { ValidationUtils.applyNumericOnlyFilter(txtSoNguoi, 2); } catch(Exception ignored){}
        errSoNguoi = errLabel();

        txtSoLuongPhong = makeField("1", "Nhập số phòng cần đặt");
        try { ValidationUtils.applyNumericOnlyFilter(txtSoLuongPhong, 2); } catch(Exception ignored){}
        errSoLuongPhong = errLabel();
        
        txtSoLuongPhong.textProperty().addListener((obs, o, n) -> updateTongTien());

        cbLoaiPhong = new ComboBox<>(); styleCombo(cbLoaiPhong);
        cbLoaiPhong.setPromptText("Chọn loại phòng");
        loadLoaiPhong();
        cbLoaiPhong.setOnAction(e -> { reloadPhongTrong(); updateTongTien(); });
        
        txtSoNguoi.textProperty().addListener((obs, o, n) -> suggestRoomCount());
        cbLoaiPhong.valueProperty().addListener((obs, o, n) -> suggestRoomCount());

        HBox rowSoNguoi = new HBox(16);
        VBox colNguoi = fieldBlock("Số người *", txtSoNguoi, errSoNguoi, null);
        VBox colLoai = fieldBlock("Loại phòng *", cbLoaiPhong, null, null);
        VBox colSL = fieldBlock("Số lượng phòng *", txtSoLuongPhong, errSoLuongPhong, null);
        HBox.setHgrow(colNguoi, Priority.ALWAYS); HBox.setHgrow(colLoai, Priority.ALWAYS); HBox.setHgrow(colSL, Priority.ALWAYS);
        rowSoNguoi.getChildren().addAll(colNguoi, colLoai, colSL);
        form.getChildren().add(rowSoNguoi);

        txtTienCoc = makeField("0", "Tiền đặt cọc");
        txtTienCoc.textProperty().addListener((obs, o, n) -> updateTongTien());

        txtGhiChu = makeField("", "Ghi chú (không bắt buộc)");

        HBox rowCoc_GhiChu = new HBox(16);
        VBox colCoc = fieldBlock("Tiền đặt cọc (VND)", txtTienCoc, null, null);
        VBox colGC = fieldBlock("Ghi chú", txtGhiChu, null, null);
        HBox.setHgrow(colCoc, Priority.ALWAYS); HBox.setHgrow(colGC, Priority.ALWAYS);
        rowCoc_GhiChu.getChildren().addAll(colCoc, colGC);
        form.getChildren().add(rowCoc_GhiChu);

        // ─────────────────────────────────────────────────────────────────
        // TÍNH TIỀN UX MỚI RÕ RÀNG HƠN
        // ─────────────────────────────────────────────────────────────────
        lblTongTienPhong = new Label("0 đ");
        lblTongTienPhong.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTongTienPhong.setTextFill(Color.web("#4b5563"));

        lblCanThanhToan = new Label("0 đ");
        lblCanThanhToan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblCanThanhToan.setTextFill(Color.web(C_ACTIVE));

        VBox totalBox = new VBox(6);
        totalBox.setPadding(new Insets(10, 0, 0, 0));
        totalBox.setStyle("-fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        HBox rowTongPhong = new HBox(8);
        rowTongPhong.setAlignment(Pos.CENTER_LEFT);
        Label lbl1 = new Label("Tổng tiền phòng:"); 
        lbl1.setFont(Font.font("Segoe UI", 14)); lbl1.setTextFill(Color.web(C_TEXT_GRAY));
        rowTongPhong.getChildren().addAll(lbl1, lblTongTienPhong);

        HBox rowThanhToan = new HBox(8);
        rowThanhToan.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label("Cần thanh toán (Đã trừ cọc):"); 
        lbl2.setFont(Font.font("Segoe UI", 14)); lbl2.setTextFill(Color.web(C_TEXT_GRAY));
        rowThanhToan.getChildren().addAll(lbl2, lblCanThanhToan);

        totalBox.getChildren().addAll(rowTongPhong, rowThanhToan);
        form.getChildren().add(totalBox);

        setupValidation();
        Platform.runLater(this::reloadPhongTrong);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 32, 20, 32));
        footer.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Button btnCancel = makeFooterBtn("Hủy", "white", "#374151", C_BORDER, "#f3f4f6");
        btnCancel.setOnAction(e -> close());

        btnSave = makeFooterBtn(editRow == null ? "💾  Thêm mới" : "💾  Cập nhật", C_SIDEBAR, "white", "transparent", C_ACTIVE);
        btnSave.setOnAction(e -> handleSave());
        if (editRow != null) btnSave.setDisable(true);

        footer.getChildren().addAll(btnCancel, btnSave);
        return footer;
    }

    private void setupValidation() {
        txtHoTen.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateTen(); });
        txtSoDT.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateSDT(); });
        txtCCCD.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateCCCD(); });
        dpNgaySinh.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateNS(); });
    }

    private boolean validateNS() {
        LocalDate ns = dpNgaySinh.getValue();
        if (ns == null) { showErrorField(dpNgaySinh, errNS, "⚠ Vui lòng chọn ngày sinh."); return false; }
        if (LocalDate.now().minusYears(16).isBefore(ns)) { showErrorField(dpNgaySinh, errNS, "⚠ Khách hàng phải từ đủ 16 tuổi."); return false; }
        clearErrorField(dpNgaySinh, errNS); return true;
    }

    private boolean validateTen() {
        String ten = txtHoTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) { showErrorField(txtHoTen, errTen, "⚠ Vui lòng nhập họ và tên."); return false; }
        if (!ten.matches(ValidationUtils.REGEX_NAME)) { showErrorField(txtHoTen, errTen, "⚠ Chỉ được chứa chữ cái và khoảng trắng."); return false; }
        if (ten.matches(ValidationUtils.REGEX_SPAM_CHAR)) { showErrorField(txtHoTen, errTen, "⚠ Tên có chứa ký tự lặp lại bất thường."); return false; }
        if (!ValidationUtils.isValidNameLength(ten)) { showErrorField(txtHoTen, errTen, "⚠ Họ và tên phải chứa ít nhất 1 ký tự"); return false; }
        clearErrorField(txtHoTen, errTen); return true;
    }

    private boolean validateSDT() {
        String sdt = txtSoDT.getText().trim();
        if (sdt.isEmpty()) { showErrorField(txtSoDT, errSDT, "⚠ Vui lòng nhập số điện thoại."); return false; }
        if (!sdt.matches(ValidationUtils.REGEX_PHONE_VN)) { showErrorField(txtSoDT, errSDT, "⚠ Sai đầu số nhà mạng Việt Nam."); return false; }
        clearErrorField(txtSoDT, errSDT); return true;
    }

    private boolean validateCCCD() {
        String cccd = txtCCCD.getText().trim();
        if (cccd.isEmpty()) { showErrorField(txtCCCD, errCCCD, "⚠ Vui lòng nhập số CCCD."); return false; }
        if (!cccd.matches(ValidationUtils.REGEX_CCCD_FORMAT)) { showErrorField(txtCCCD, errCCCD, "⚠ Phải gồm đúng 12 chữ số."); return false; }
        if (!ValidationUtils.isValidProvinceCode(cccd)) { showErrorField(txtCCCD, errCCCD, "⚠ Mã tỉnh/thành phố không hợp lệ."); return false; }
        if (dpNgaySinh != null && dpNgaySinh.getValue() != null) {
            int namSinh = dpNgaySinh.getValue().getYear();
            if (!ValidationUtils.isValidCCCDCenturyAndGender(cccd, namSinh)) {
                showErrorField(txtCCCD, errCCCD, "⚠ Số thứ 4 không khớp năm sinh/giới tính."); return false;
            }
            if (!ValidationUtils.isValidCCCDBirthYear(cccd, namSinh)) {
                showErrorField(txtCCCD, errCCCD, "⚠ 2 số năm sinh trên CCCD bị sai (số 5,6)."); return false;
            }
        }
        clearErrorField(txtCCCD, errCCCD); return true;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * LƯU DỮ LIỆU ĐA LUỒNG (TRÁNH ĐƠ GIAO DIỆN)
     * ════════════════════════════════════════════════════════════════
     */
    private void handleSave() {
        boolean ok = true;
        if (!validateTen()) ok = false;
        if (!validateSDT()) ok = false;
        if (!validateNS()) ok = false;
        if (!validateCCCD()) ok = false;

        if (dpCheckIn.getValue() == null) { errNgayIn.setText("⚠ Chọn ngày nhận phòng"); ok = false; } else errNgayIn.setText("");
        if (dpCheckOut.getValue() == null) { errNgayOut.setText("⚠ Chọn ngày trả phòng"); ok = false; }
        else if (dpCheckIn.getValue() != null && !dpCheckOut.getValue().isAfter(dpCheckIn.getValue())) { errNgayOut.setText("⚠ Ngày trả phải sau ngày nhận"); ok = false; }
        else errNgayOut.setText("");

        String soNguoiStr = txtSoNguoi.getText().trim();
        if (soNguoiStr.isEmpty() || Integer.parseInt(soNguoiStr) < 1) { errSoNguoi.setText("⚠ Tối thiểu 1 người"); ok = false; } else errSoNguoi.setText("");
        
        String slStr = txtSoLuongPhong.getText().trim();
        int soLuong = 1;
        try { soLuong = Integer.parseInt(slStr); } catch (Exception ignored) {}
        if (soLuong < 1) { errSoLuongPhong.setText("⚠ Tối thiểu 1 phòng"); ok = false; }

        List<String> emptyRooms = phongDAO.getPhongTrongByLoai(cbLoaiPhong.getValue());
        if (editRow == null && emptyRooms.size() < soLuong) {
            errSoLuongPhong.setText("⚠ Chỉ còn " + emptyRooms.size() + " phòng"); ok = false;
        }

        if (!ok) return;

        // 1. Trích xuất toàn bộ dữ liệu trên Main Thread (FX Thread)
        final String hoTen = ValidationUtils.toTitleCase(txtHoTen.getText().trim().replaceAll("\\s+", " "));
        final String soDT = txtSoDT.getText().trim();
        final String cccd = txtCCCD.getText().trim();
        final LocalDate ngaySinh = dpNgaySinh.getValue();
        final LocalDate checkIn = dpCheckIn.getValue();
        final LocalDate checkOut = dpCheckOut.getValue();
        final int soNguoi = Integer.parseInt(txtSoNguoi.getText().trim());
        final int fSoLuong = soLuong;
        final double tienCoc = parseTienCoc();
        final String ghiChu = txtGhiChu.getText().trim();

        final String preGenMaKH   = editRow == null ? khachHangDAO.getNextMaKH() : null;
        final String preGenMaDat  = editRow == null ? datPhongDAO.generateMaDat() : null;
        final String baseMaCTDP   = editRow == null ? ctdpDAO.generateMaCTDP() : null;

        // 2. Cập nhật UI sang trạng thái Đang lưu
        String oldBtnText = btnSave.getText();
        btnSave.setDisable(true);
        btnSave.setText("⏳ Đang lưu...");

        // 3. Đưa thao tác nặng xuống Background Thread
        new Thread(() -> {
            boolean success = false;
            String errorMsg = "";
            
            try (Connection con = ConnectDatabase.getInstance().getConnection()) {
                con.setAutoCommit(false);
                try {
                    if (editRow != null) {
                        khachHangDAO.updateByMaDat(con, editMaDat, hoTen, soDT, cccd, ngaySinh);
                        datPhongDAO.updateNgayCheckInOut(con, editMaDat, checkIn, checkOut);
                        // Chỉ cập nhật thông tin chung, KHÔNG đổi danh sách phòng đã gán
                        ctdpDAO.updateInfoByMaDat(con, editMaDat, tienCoc, soNguoi, ghiChu);
                    } else {
                        String maKH = khachHangDAO.findOrCreate(con, hoTen, soDT, cccd, ngaySinh, preGenMaKH);
                        datPhongDAO.insertWithConnection(con, preGenMaDat, maKH, checkIn, checkOut);
                        
                        double cdpCoc = tienCoc / fSoLuong;
                        int cdpNguoi = soNguoi / fSoLuong;
                        int leftoverNguoi = soNguoi % fSoLuong;
                        
                        // Parse baseMaCTDP (VD: CTDP015)
                        int baseNum = 0;
                        if (baseMaCTDP != null && baseMaCTDP.length() > 4) {
                            try { baseNum = Integer.parseInt(baseMaCTDP.substring(4)); } catch (Exception ignored) {}
                        }
                        
                        for (int i = 0; i < fSoLuong; i++) {
                            String maPhong = emptyRooms.get(i).split(" ")[0]; // Lấy phần đầu
                            String actualMaCTDP = String.format("CTDP%04d", baseNum + i);
                            ctdpDAO.insertWithConnection(con, actualMaCTDP, maPhong, preGenMaDat, cdpCoc, cdpNguoi + (i == 0 ? leftoverNguoi : 0), ghiChu);
                        }
                    }
                    con.commit();
                    success = true;
                } catch (Exception ex) {
                    try { con.rollback(); } catch (Exception ignored) {}
                    errorMsg = ex.getMessage();
                    ex.printStackTrace();
                } finally {
                    try { con.setAutoCommit(true); } catch (Exception ignored) {}
                }
            } catch (Exception ex) { 
                errorMsg = "Lỗi kết nối Server: " + ex.getMessage(); 
            }

            // 4. Trả kết quả về Main Thread
            final boolean finalSuccess = success;
            final String finalErrorMsg = errorMsg;
            Platform.runLater(() -> {
                if (finalSuccess) {
                    showInfo("Thành công!", editRow == null ? "Đã thêm đơn đặt phòng." : "Đã cập nhật đơn đặt phòng.");
                    if (onSuccess != null) onSuccess.run();
                    close();
                } else {
                    showError("Lỗi CSDL: " + finalErrorMsg);
                    btnSave.setDisable(false);
                    btnSave.setText(oldBtnText);
                }
            });
        }).start();
    }

    private void loadLoaiPhong() {
        try {
            cbLoaiPhong.getItems().addAll(loaiPhongDAO.getAllMaLoaiPhong());
            if (!cbLoaiPhong.getItems().isEmpty()) cbLoaiPhong.getSelectionModel().selectFirst();
        } catch (Exception e) { System.err.println("Lỗi load loại phòng: " + e.getMessage()); }
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * LOGIC GỢI Ý PHÒNG VÀ KIỂM TRA PHÒNG TRỐNG
     * ════════════════════════════════════════════════════════════════
     */
    private void suggestRoomCount() {
        if (txtSoNguoi == null || cbLoaiPhong == null || txtSoLuongPhong == null) return;
        try {
            int soNguoi = Integer.parseInt(txtSoNguoi.getText().trim());
            String loai = cbLoaiPhong.getValue();
            if (loai == null || soNguoi <= 0) return;
            
            int capacity = 2; // Default
            for (model.entities.LoaiPhong lp : loaiPhongDAO.getAll()) {
                if (lp.getMaLoaiPhong().equals(loai)) {
                    capacity = lp.getSucChua(); break;
                }
            }
            int soPhong = (int) Math.ceil((double) soNguoi / capacity);
            if (soPhong < 1) soPhong = 1;
            
            // Cập nhật UI
            if (txtSoLuongPhong.isFocused()) return; // Không ghi đè nếu người dùng đang tự gõ
            txtSoLuongPhong.setText(String.valueOf(soPhong));
            
            // Cập nhật lại tiền cọc tự động dựa trên số phòng (Mặc định bằng 1 đêm)
            double giaPhong = loaiPhongDAO.getGiaByMaLoai(loai);
            txtTienCoc.setText(DF.format(giaPhong * soPhong)); 
            
        } catch (Exception ignored) {}
    }

    private void reloadPhongTrong() {
        if (txtSoLuongPhong == null || cbLoaiPhong == null) return;
        String loai = cbLoaiPhong.getValue();
        if (loai == null) return;

        List<String> emptyRooms = new java.util.ArrayList<>(phongDAO.getPhongTrongByLoai(loai));
        try {
            int needed = Integer.parseInt(txtSoLuongPhong.getText().trim());
            // Nếu đang sửa đơn, và đang chọn đúng loại phòng cũ, ta cộng thêm những phòng hiện hữu của đơn đó
            if (editRow != null && loai.equals(originalRoomType) && originalRoom != null) {
                int existingCount = originalRoom.split(",").length;
                if (needed <= existingCount + emptyRooms.size()) {
                    errSoLuongPhong.setText("");
                } else {
                    errSoLuongPhong.setText("⚠ Chỉ còn " + emptyRooms.size() + " phòng trống kiểu này");
                }
            } else {
                if (emptyRooms.size() < needed) {
                    errSoLuongPhong.setText("⚠ Chỉ còn " + emptyRooms.size() + " phòng trống kiểu này");
                } else {
                    errSoLuongPhong.setText("");
                }
            }
        } catch (Exception ignored) {}
    }

    private void populateEditData() {
        if (editRow == null) return;
        Object[] data = datPhongDAO.findEditDetail(editMaDat);
        if (data == null) return;

        txtHoTen.setText(data[0] != null ? (String) data[0] : "");
        txtSoDT.setText(data[1] != null ? (String) data[1] : "");
        txtCCCD.setText(data[2] != null ? (String) data[2] : "");
        if (data[3] != null) dpNgaySinh.setValue((LocalDate) data[3]);
        if (data[4] != null) dpCheckIn.setValue((LocalDate) data[4]);
        if (data[5] != null) dpCheckOut.setValue((LocalDate) data[5]);
        
        // Lưu vết phòng gốc (có thể nhiều phòng, VD: "P101, P102")
        this.originalRoom = (String) data[6];
        this.originalRoomType = (String) data[10];

        txtSoNguoi.setText(String.valueOf((int) data[7]));
        txtTienCoc.setText(DF.format((double) data[8]));
        txtGhiChu.setText(data[9] != null ? (String) data[9] : "");

        if (originalRoomType != null) cbLoaiPhong.setValue(originalRoomType);
        if (originalRoom != null) txtSoLuongPhong.setText(String.valueOf(originalRoom.split(",\\s*").length));
        
        reloadPhongTrong();
        updateTongTien();

        Platform.runLater(() -> {
            if (btnSave != null) {
                EventUtils.setupDirtyTracking(btnSave, 
                    txtHoTen, txtSoDT, txtCCCD, dpNgaySinh, 
                    dpCheckIn, dpCheckOut, txtSoNguoi, txtTienCoc, txtGhiChu, 
                    cbLoaiPhong, txtSoLuongPhong
                );
            }
        });

        Platform.runLater(() -> {
            if (txtHoTen != null) {
                txtHoTen.requestFocus();
                txtHoTen.positionCaret(txtHoTen.getText().length());
            }
        });
    }

    private void updateTongTien() {
        if (lblTongTienPhong == null || cbLoaiPhong == null || txtTienCoc == null || txtSoLuongPhong == null) return;
        try {
            LocalDate dIn = dpCheckIn.getValue(), dOut = dpCheckOut.getValue();
            if (dIn == null || dOut == null) { 
                lblTongTienPhong.setText("0 đ"); 
                lblCanThanhToan.setText("0 đ");
                return; 
            }
            long days = ChronoUnit.DAYS.between(dIn, dOut);
            if (days <= 0) days = 1;

            double giaPhong = 0;
            String loai = cbLoaiPhong.getValue();
            if (loai != null) {
                giaPhong = loaiPhongDAO.getGiaByMaLoai(loai);
            }
            
            int soLuong = Integer.parseInt(txtSoLuongPhong.getText().trim());
            double tongPhong = giaPhong * soLuong * days;
            double canThanhToan = tongPhong - parseTienCoc();

            lblTongTienPhong.setText(DF.format(tongPhong) + " đ");
            lblCanThanhToan.setText(DF.format(Math.max(canThanhToan, 0)) + " đ");
        } catch (Exception e) { 
            lblTongTienPhong.setText("0 đ"); 
            lblCanThanhToan.setText("0 đ");
        }
    }

    private double parseTienCoc() {
        String raw = txtTienCoc.getText().replaceAll("[^\\d]", "");
        return raw.isEmpty() ? 0 : Double.parseDouble(raw);
    }

    /* ── UI Factory ──────────────────── */
    private String fieldStyle() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-pref-height: 40; -fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: "
                + C_BORDER + "; -fx-padding: 8 12 8 12;";
    }

    private TextField makeField(String value, String prompt) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private void styleCombo(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(40);
        cb.setStyle(fieldStyle());
    }

    private Label errLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(C_ERROR));
        l.setWrapText(true);
        l.setMinHeight(14);
        return l;
    }

    private VBox fieldBlock(String label, javafx.scene.Node field, Label errLbl, String hint) {
        VBox b = new VBox(4);
        b.setPadding(new Insets(0, 0, 2, 0));
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefWidth(400);
        HBox lblBox = new HBox(4);

        if (label.endsWith("*")) {
            Label lblText = new Label(label.substring(0, label.length() - 1).trim());
            lblText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblText.setTextFill(Color.web("#374151"));
            Label lblStar = new Label("*");
            lblStar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblStar.setTextFill(Color.web(C_ERROR));
            lblBox.getChildren().addAll(lblText, lblStar);
        } else {
            Label lblText = new Label(label);
            lblText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblText.setTextFill(Color.web("#374151"));
            lblBox.getChildren().add(lblText);
        }
        b.getChildren().add(lblBox);

        if (field instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().add(field);

        if (errLbl != null) { errLbl.setMaxWidth(Double.MAX_VALUE); b.getChildren().add(errLbl); }
        if (hint != null) {
            Label h = new Label(hint);
            h.setFont(Font.font("Segoe UI", 11));
            h.setTextFill(Color.web(C_TEXT_GRAY));
            b.getChildren().add(h);
        }
        return b;
    }

    private void showErrorField(javafx.scene.Node tf, Label errLabel, String msg) {
        if (errLabel != null) errLabel.setText(msg);
        tf.setStyle(fieldStyle() + "-fx-border-color: " + C_ERROR + "; -fx-background-color: #fef2f2;");
    }

    private void clearErrorField(javafx.scene.Node tf, Label errLabel) {
        if (errLabel != null) errLabel.setText("");
        tf.setStyle(fieldStyle());
    }

    private Button makeFooterBtn(String text, String bg, String fg, String border, String bgHover) {
        Button btn = new Button(text);
        btn.setPrefHeight(40);
        btn.setPrefWidth(text.contains("Thêm") || text.contains("Cập") ? 140 : 100);
        String baseStyle = "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-cursor: hand;" +
                (border.equals("transparent") ? "" : "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        String hoverStyle = "-fx-background-color: " + bgHover + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-cursor: hand;" +
                (border.equals("transparent") ? "" : "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}