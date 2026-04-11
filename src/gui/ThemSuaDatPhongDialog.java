package gui;

import connectDatabase.ConnectDatabase;
import dao.LoaiPhongDAO;
import dao.PhongDAO;
import model.entities.Phong;
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

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.DecimalFormat;

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

    /* ── Form fields ──────────────────────────────────────────────── */
    private TextField txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtTienCoc, txtGhiChu;
    private ComboBox<String> cbLoaiPhong, cbPhong;
    private DatePicker dpCheckIn, dpCheckOut;
    private Label errTen, errSDT, errCCCD, errNgayIn, errNgayOut, errSoNguoi, errPhong;
    private Label lblTongTien;
    private Button btnSave;

    private Region overlay;

    /* ── Constructor ──────────────────────────────────────────────── */
    public ThemSuaDatPhongDialog(Window owner, Object[] editRow, Runnable onSuccess) {
        this.owner = owner;
        this.editRow = editRow;
        this.onSuccess = onSuccess;

        if (editRow != null) this.editMaDat = (String) editRow[0];

        // KHẮC PHỤC LỖI TÀNG HÌNH: Dùng UNDECORATED thay vì TRANSPARENT
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED); 

        // Khởi tạo Scene với nền trắng an toàn
        Scene scene = new Scene(buildRoot(), 600, 680);
        scene.setFill(Color.WHITE);
        setScene(scene);
        centerOnScreen();

        initEvents();

        Platform.runLater(() -> {
            if (txtHoTen != null) {
                txtHoTen.requestFocus();
                txtHoTen.positionCaret(txtHoTen.getText().length());
            }
        });

        // Load dữ liệu khi sửa
        if (editRow != null) {
            Platform.runLater(this::populateEditData);
        }
    }

    public void showDialog() {
        if (owner != null) {
            this.overlay = DimOverlay.show(owner);
        }
        showAndWait(); // Lệnh này sẽ block màn hình chính cho tới khi form đóng
        if (owner != null && this.overlay != null) {
            DimOverlay.hide(owner, this.overlay);
        }
    }

    /* ════════════════════════════════════════════════════════════════
       SỰ KIỆN LOGIC (EventUtils)
    ════════════════════════════════════════════════════════════════ */
    private void initEvents() {
        // KHẮC PHỤC LỖI NGẦM: Bọc Try-Catch để đảm bảo form vẫn mở nếu tiện ích lỗi
        try {
            if (txtHoTen != null && txtSoDT != null) {
                EventUtils.setupEnterNavigation(this::handleSave, txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtTienCoc, txtGhiChu);
                if (editRow != null && btnSave != null) {
                    EventUtils.setupDirtyTracking(btnSave, txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtTienCoc, txtGhiChu, dpCheckIn, dpCheckOut, cbLoaiPhong, cbPhong);
                }
            }
        } catch (Exception e) {
            System.err.println("Bỏ qua lỗi EventUtils: " + e.getMessage());
        }
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * ROOT
     * ════════════════════════════════════════════════════════════════
     */
    private VBox buildRoot() {
        VBox root = new VBox();
        root.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + C_SIDEBAR + ";" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");
        
        // Không dùng Try-Catch bao bọc ngoài cùng ở đây để tránh tạo ra form rỗng
        root.getChildren().addAll(
                buildHeader(),
                buildFormBody(),
                buildFooter());
                
        return root;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * HEADER
     * ════════════════════════════════════════════════════════════════
     */
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

    /*
     * ════════════════════════════════════════════════════════════════
     * FORM BODY
     * ════════════════════════════════════════════════════════════════
     */
    private ScrollPane buildFormBody() {
        VBox form = new VBox(6);
        form.setPadding(new Insets(22, 32, 10, 32));
        form.setStyle("-fx-background-color: white;");

        txtHoTen = makeField("", "Nhập họ và tên khách hàng");
        errTen = errLabel();
        form.getChildren().add(fieldBlock("Họ và tên *", txtHoTen, errTen, null));

        txtSoDT = makeField("", "Nhập số điện thoại");
        try { ValidationUtils.applyNumericOnlyFilter(txtSoDT, 10); } catch (Exception ignored) {}
        errSDT = errLabel();

        txtCCCD = makeField("", "Nhập CCCD (12 số)");
        try { ValidationUtils.applyNumericOnlyFilter(txtCCCD, 12); } catch (Exception ignored) {}
        errCCCD = errLabel();

        HBox rowSDT = new HBox(16);
        VBox colSDT = fieldBlock("Số điện thoại *", txtSoDT, errSDT, null);
        VBox colCCCD = fieldBlock("Số CCCD *", txtCCCD, errCCCD, null);
        HBox.setHgrow(colSDT, Priority.ALWAYS); HBox.setHgrow(colCCCD, Priority.ALWAYS);
        rowSDT.getChildren().addAll(colSDT, colCCCD);
        form.getChildren().add(rowSDT);

        int curYear = LocalDate.now().getYear();
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

        cbLoaiPhong = new ComboBox<>(); styleCombo(cbLoaiPhong);
        cbLoaiPhong.setPromptText("Chọn loại phòng");
        loadLoaiPhong();
        cbLoaiPhong.setOnAction(e -> reloadPhongTrong());

        cbPhong = new ComboBox<>(); styleCombo(cbPhong);
        cbPhong.setPromptText("Chọn phòng trống");
        errPhong = errLabel();

        HBox rowRoom = new HBox(16);
        VBox colType = fieldBlock("Loại phòng *", cbLoaiPhong, null, null);
        VBox colRoom = fieldBlock("Phòng trống *", cbPhong, errPhong, null);
        HBox.setHgrow(colType, Priority.ALWAYS); HBox.setHgrow(colRoom, Priority.ALWAYS);
        rowRoom.getChildren().addAll(colType, colRoom);
        form.getChildren().add(rowRoom);

        txtSoNguoi = makeField("1", "Số người");
        try { ValidationUtils.applyNumericOnlyFilter(txtSoNguoi, 2); } catch(Exception ignored){}
        errSoNguoi = errLabel();

        txtTienCoc = makeField("0", "Tiền đặt cọc");
        txtTienCoc.textProperty().addListener((obs, o, n) -> updateTongTien());

        HBox rowExtra = new HBox(16);
        VBox colNguoi = fieldBlock("Số người *", txtSoNguoi, errSoNguoi, null);
        VBox colCoc = fieldBlock("Tiền đặt cọc (VND)", txtTienCoc, null, null);
        HBox.setHgrow(colNguoi, Priority.ALWAYS); HBox.setHgrow(colCoc, Priority.ALWAYS);
        rowExtra.getChildren().addAll(colNguoi, colCoc);
        form.getChildren().add(rowExtra);

        txtGhiChu = makeField("", "Ghi chú (không bắt buộc)");
        form.getChildren().add(fieldBlock("Ghi chú", txtGhiChu, null, null));

        lblTongTien = new Label("0 đ");
        lblTongTien.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTongTien.setTextFill(Color.web(C_ACTIVE));

        HBox totalRow = new HBox(8);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(10, 0, 0, 0));
        totalRow.setStyle("-fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        Label lbl = new Label("Tạm tính:"); lbl.setFont(Font.font("Segoe UI", 14)); lbl.setTextFill(Color.web(C_TEXT_GRAY));
        totalRow.getChildren().addAll(lbl, lblTongTien);
        form.getChildren().add(totalRow);

        setupValidation();
        Platform.runLater(this::reloadPhongTrong);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * FOOTER
     * ════════════════════════════════════════════════════════════════
     */
    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 32, 20, 32));
        footer.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Button btnCancel = makeFooterBtn("Hủy", "white", "#374151", C_BORDER, "#f3f4f6");
        btnCancel.setOnAction(e -> close());

        btnSave = makeFooterBtn(editRow == null ? "💾  Thêm mới" : "💾  Cập nhật", C_SIDEBAR, "white", "transparent", C_ACTIVE);
        btnSave.setOnAction(e -> handleSave());

        footer.getChildren().addAll(btnCancel, btnSave);
        return footer;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * VALIDATION
     * ════════════════════════════════════════════════════════════════
     */
    private void setupValidation() {
        txtHoTen.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateTen(); });
        txtSoDT.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateSDT(); });
        txtCCCD.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateCCCD(); });
    }

    private boolean validateTen() {
        String ten = txtHoTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) { showErrorField(txtHoTen, errTen, "⚠ Vui lòng nhập họ và tên."); return false; }
        clearErrorField(txtHoTen, errTen); return true;
    }

    private boolean validateSDT() {
        String sdt = txtSoDT.getText().trim();
        if (sdt.isEmpty()) { showErrorField(txtSoDT, errSDT, "⚠ Vui lòng nhập số điện thoại."); return false; }
        clearErrorField(txtSoDT, errSDT); return true;
    }

    private boolean validateCCCD() {
        String cccd = txtCCCD.getText().trim();
        if (cccd.isEmpty()) { showErrorField(txtCCCD, errCCCD, "⚠ Vui lòng nhập số CCCD."); return false; }
        clearErrorField(txtCCCD, errCCCD); return true;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * SAVE
     * ════════════════════════════════════════════════════════════════
     */
    private void handleSave() {
        boolean ok = true;
        if (!validateTen()) ok = false;
        if (!validateSDT()) ok = false;
        if (!validateCCCD()) ok = false;

        if (dpCheckIn.getValue() == null) { errNgayIn.setText("⚠ Chọn ngày nhận phòng"); ok = false; } else errNgayIn.setText("");
        if (dpCheckOut.getValue() == null) { errNgayOut.setText("⚠ Chọn ngày trả phòng"); ok = false; }
        else if (dpCheckIn.getValue() != null && !dpCheckOut.getValue().isAfter(dpCheckIn.getValue())) { errNgayOut.setText("⚠ Ngày trả phải sau ngày nhận"); ok = false; }
        else errNgayOut.setText("");

        String soNguoiStr = txtSoNguoi.getText().trim();
        if (soNguoiStr.isEmpty() || Integer.parseInt(soNguoiStr) < 1) { errSoNguoi.setText("⚠ Tối thiểu 1 người"); ok = false; } else errSoNguoi.setText("");
        
        if (cbPhong.getValue() == null || cbPhong.getValue().isEmpty()) { errPhong.setText("⚠ Vui lòng chọn phòng"); ok = false; } else errPhong.setText("");

        if (!ok) return;

        try (Connection con = ConnectDatabase.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                String hoTen = txtHoTen.getText().trim();
                String soDT = txtSoDT.getText().trim();
                String cccd = txtCCCD.getText().trim();
                LocalDate checkIn = dpCheckIn.getValue();
                LocalDate checkOut = dpCheckOut.getValue();
                int soNguoi = Integer.parseInt(txtSoNguoi.getText().trim());
                double tienCoc = parseTienCoc();
                String ghiChu = txtGhiChu.getText().trim();
                
                String maPhong = cbPhong.getValue().split(" ")[0];

                if (editRow != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE KH SET tenKH=?, soDT=?, soCCCD=? WHERE maKH=(SELECT maKH FROM DatPhong WHERE maDat=?)")) {
                        ps.setString(1, hoTen); ps.setString(2, soDT); ps.setString(3, cccd); ps.setString(4, editMaDat);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE DatPhong SET ngayCheckIn=?, ngayCheckOut=? WHERE maDat=?")) {
                        ps.setDate(1, Date.valueOf(checkIn)); ps.setDate(2, Date.valueOf(checkOut)); ps.setString(3, editMaDat);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE ChiTietDatPhong SET maPhong=?, giaCoc=?, soNguoi=?, ghiChu=? WHERE maDat=?")) {
                        ps.setString(1, maPhong); ps.setDouble(2, tienCoc); ps.setInt(3, soNguoi); ps.setString(4, ghiChu); ps.setString(5, editMaDat);
                        ps.executeUpdate();
                    }
                } else {
                    String maKH = findOrCreateKH(con, hoTen, soDT, cccd);
                    String maDat = generateId("DP");

                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO DatPhong(maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut) VALUES(?,?,?,?,?)")) {
                        ps.setString(1, maDat); ps.setDate(2, Date.valueOf(LocalDate.now()));
                        ps.setString(3, maKH); ps.setDate(4, Date.valueOf(checkIn)); ps.setDate(5, Date.valueOf(checkOut));
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO ChiTietDatPhong(maCTDP, maPhong, maDat, giaCoc, soNguoi, ghiChu) VALUES(?,?,?,?,?,?)")) {
                        ps.setString(1, generateId("CTDP")); ps.setString(2, maPhong);
                        ps.setString(3, maDat); ps.setDouble(4, tienCoc); ps.setInt(5, soNguoi); ps.setString(6, ghiChu);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                showInfo("Thành công!", editRow == null ? "Đã thêm đơn đặt phòng." : "Đã cập nhật đơn đặt phòng.");
                if (onSuccess != null) onSuccess.run();
                close();

            } catch (Exception ex) {
                con.rollback(); ex.printStackTrace();
                showError("Lỗi CSDL: " + ex.getMessage());
            } finally { con.setAutoCommit(true); }
        } catch (Exception ex) { ex.printStackTrace(); showError("Lỗi kết nối Server!"); }
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * HELPERS
     * ════════════════════════════════════════════════════════════════
     */
    private void loadLoaiPhong() {
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT maLoaiPhong FROM LoaiPhong")) {
            while (rs.next()) cbLoaiPhong.getItems().add(rs.getString("maLoaiPhong"));
            if (!cbLoaiPhong.getItems().isEmpty()) cbLoaiPhong.getSelectionModel().selectFirst();
        } catch (Exception e) { System.err.println("Lỗi load loại phòng: " + e.getMessage()); }
    }

    private void reloadPhongTrong() {
        if (cbPhong == null || cbLoaiPhong == null) return;
        cbPhong.getItems().clear();
        String loai = cbLoaiPhong.getValue();
        LocalDate dIn = dpCheckIn.getValue(), dOut = dpCheckOut.getValue();
        if (loai == null || dIn == null || dOut == null) return;

        String sql = "SELECT maPhong, tenPhong FROM Phong WHERE loaiPhong = ? AND tinhTrang = N'CONTRONG'";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loai);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cbPhong.getItems().add(rs.getString("maPhong") + " - " + rs.getString("tenPhong"));
            if (!cbPhong.getItems().isEmpty()) cbPhong.getSelectionModel().selectFirst();
        } catch (Exception e) {}
    }

    private void populateEditData() {
        if (editRow == null) return;
        try (Connection con = ConnectDatabase.getInstance().getConnection()) {
            String sql = """
                SELECT dp.maDat, kh.tenKH, kh.soDT, kh.soCCCD,
                       dp.ngayCheckIn, dp.ngayCheckOut,
                       ctdp.maPhong, ctdp.soNguoi, ctdp.giaCoc, ctdp.ghiChu,
                       p.loaiPhong
                FROM DatPhong dp JOIN KH kh ON dp.maKH = kh.maKH
                LEFT JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat
                LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong
                WHERE dp.maDat = ?
                """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, editMaDat);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtHoTen.setText(rs.getString("tenKH") != null ? rs.getString("tenKH") : "");
                    txtSoDT.setText(rs.getString("soDT") != null ? rs.getString("soDT") : "");
                    txtCCCD.setText(rs.getString("soCCCD") != null ? rs.getString("soCCCD") : "");
                    if (rs.getDate("ngayCheckIn") != null) dpCheckIn.setValue(rs.getDate("ngayCheckIn").toLocalDate());
                    if (rs.getDate("ngayCheckOut") != null) dpCheckOut.setValue(rs.getDate("ngayCheckOut").toLocalDate());
                    txtSoNguoi.setText(rs.getObject("soNguoi") != null ? String.valueOf(rs.getInt("soNguoi")) : "1");
                    txtTienCoc.setText(rs.getObject("giaCoc") != null ? DF.format(rs.getDouble("giaCoc")) : "0");
                    txtGhiChu.setText(rs.getString("ghiChu") != null ? rs.getString("ghiChu") : "");

                    String loai = rs.getString("loaiPhong");
                    if (loai != null) cbLoaiPhong.setValue(loai);
                    reloadPhongTrong();

                    String maPhong = rs.getString("maPhong");
                    if (maPhong != null) {
                        for (String item : cbPhong.getItems()) {
                            if (item.startsWith(maPhong)) { cbPhong.setValue(item); break; }
                        }
                    }
                    updateTongTien();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateTongTien() {
        if (lblTongTien == null || cbLoaiPhong == null || txtTienCoc == null) return;
        try {
            LocalDate dIn = dpCheckIn.getValue(), dOut = dpCheckOut.getValue();
            if (dIn == null || dOut == null) { lblTongTien.setText("0 đ"); return; }
            long days = ChronoUnit.DAYS.between(dIn, dOut);
            if (days <= 0) days = 1;

            double giaPhong = 0;
            String loai = cbLoaiPhong.getValue();
            if (loai != null) {
                try (Connection con = ConnectDatabase.getInstance().getConnection();
                     PreparedStatement ps = con.prepareStatement("SELECT gia FROM LoaiPhong WHERE maLoaiPhong = ?")) {
                    ps.setString(1, loai);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) giaPhong = rs.getDouble("gia");
                } catch (Exception ignored) {}
            }
            double total = (giaPhong * days) - parseTienCoc();
            lblTongTien.setText(DF.format(Math.max(total, 0)) + " đ");
        } catch (Exception e) { lblTongTien.setText("0 đ"); }
    }

    private double parseTienCoc() {
        String raw = txtTienCoc.getText().replaceAll("[^\\d]", "");
        return raw.isEmpty() ? 0 : Double.parseDouble(raw);
    }

    private String findOrCreateKH(Connection con, String ten, String sdt, String cccd) throws SQLException {
        for (String col : new String[]{"soCCCD", "soDT"}) {
            try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KH WHERE " + col + " = ?")) {
                ps.setString(1, col.equals("soCCCD") ? cccd : sdt);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("maKH");
            }
        }
        String maKH = generateId("KH");
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO KH(maKH, tenKH, soDT, soCCCD) VALUES(?,?,?,?)")) {
            ps.setString(1, maKH); ps.setString(2, ten); ps.setString(3, sdt); ps.setString(4, cccd);
            ps.executeUpdate();
        }
        return maKH;
    }

    private String generateId(String prefix) {
        return prefix + String.format("%03d", (int)(Math.random() * 900) + 100);
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