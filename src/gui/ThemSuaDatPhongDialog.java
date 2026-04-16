package gui;

import connectDatabase.ConnectDatabase;
import dao.ChiTietDatPhongDAO;
import dao.DatPhongDAO;
import dao.KhachHangDAO;
import dao.LoaiPhongDAO;
import dao.PhongDAO;
import model.entities.LoaiPhong;
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
import javafx.scene.text.FontPosture;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ThemSuaDatPhongDialog – Dialog thêm đơn đặt phòng.
 * - Chọn NHIỀU loại phòng (CheckBox list)
 * - Chọn phòng cụ thể (không random)
 * - Khóa tiền cọc (tự tính từ giá phòng)
 * - Bắt buộc thanh toán cọc trước khi lưu
 * - Validate CCCD khớp năm sinh
 * - Kiểm tra sức chứa phòng >= số người
 * - Tự động tạo Hóa Đơn khi đặt phòng thành công
 */
public class ThemSuaDatPhongDialog extends Stage {

    /* ── Bảng màu ─────────────────────────────────────────────────────── */
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_ERROR = "#dc2626";
    private static final String C_GREEN = "#16a34a";

    private static final DecimalFormat DF = new DecimalFormat("#,###");

    /* ── State ────────────────────────────────────────────────────────── */
    private double xOffset = 0, yOffset = 0;
    private final Window owner;
    private final Runnable onSuccess;
    private boolean isReadOnly = false;
    private String maDatExisting = null;

    /* ── Form fields ──────────────────────────────────────────────────── */
    private TextField txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtGhiChu;
    private TextField txtTienCoc; // readonly – tự tính
    private DatePicker dpCheckIn, dpCheckOut, dpNgaySinh;
    private Label errTen, errSDT, errCCCD, errNgayIn, errNgayOut, errSoNguoi, errNS;
    private Label lblGopY; // Gợi ý số phòng
    private CheckBox cbDaThanhToanCoc; // Checkbox xác nhận đã nhận cọc

    // Multi-select loại phòng
    private VBox loaiPhongCheckBoxArea;
    private Map<String, CheckBox> loaiPhongCheckBoxes = new LinkedHashMap<>();

    // Chọn phòng cụ thể
    private FlowPane phongSelectFlow;
    private Map<String, CheckBox> phongCheckBoxes = new LinkedHashMap<>(); // maPhong → CheckBox
    private Map<String, Phong> phongMap = new LinkedHashMap<>(); // maPhong → Phong object

    // Tính tiền
    private Label lblTongTienPhong;
    private Label lblCanThanhToan;
    private Label lblCapacityWarning; // Cảnh báo sức chứa real-time

    private Button btnSave;
    private Region overlay;
    private model.entities.KhachHang khFoundBySDT = null; // KH tìm được qua SĐT (dùng để check CCCD khớp)

    /* ── DAO ──────────────────────────────────────────────────────────── */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final ChiTietDatPhongDAO ctdpDAO = new ChiTietDatPhongDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    /* ── Constructor ──────────────────────────────────────────────────── */
    public ThemSuaDatPhongDialog(Window owner, Runnable onSuccess) {
        this(owner, null, onSuccess);
    }

    public ThemSuaDatPhongDialog(Window owner, String maDat, Runnable onSuccess) {
        this.owner = owner;
        this.onSuccess = onSuccess;
        this.maDatExisting = maDat;
        this.isReadOnly = (maDat != null);

        if (owner != null)
            initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(buildRoot(), 660, 780);
        scene.setFill(Color.WHITE);
        setScene(scene);
        centerOnScreen();
        initEvents();

        if (isReadOnly && maDatExisting != null) {
            populateData(maDatExisting);
            disableInputs();
        }
    }

    public void showDialog() {
        if (owner != null)
            this.overlay = DimOverlay.show(owner);
        showAndWait();
        if (owner != null && this.overlay != null)
            DimOverlay.hide(owner, this.overlay);
    }

    private void initEvents() {
        try {
            if (txtHoTen != null && txtSoDT != null) {
                EventUtils.setupEnterToSave(() -> {
                    if (btnSave != null && !btnSave.isDisabled())
                        handleSave();
                }, txtHoTen, txtSoDT, txtCCCD, txtSoNguoi, txtGhiChu, dpNgaySinh, dpCheckIn, dpCheckOut);
            }
        } catch (Exception e) {
            System.err.println("Bỏ qua lỗi EventUtils: " + e.getMessage());
        }
    }

    /* ── Build UI ────────────── */
    private VBox buildRoot() {
        VBox root = new VBox();
        root.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_SIDEBAR + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");
        root.getChildren().addAll(buildHeader(), buildFormBody(), buildFooterVBox());
        return root;
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: " + C_SIDEBAR + ";");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label(isReadOnly ? "CHI TIẾT ĐẶT PHÒNG" : "THÊM ĐƠN ĐẶT PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setTextFill(Color.WHITE);
        Label lblSub = new Label(isReadOnly ? "Thông tin chi tiết đơn đặt phòng" : "Điền đầy đủ thông tin bên dưới");
        lblSub.setFont(Font.font("Segoe UI", 12));
        lblSub.setTextFill(Color.web("#93c5fd"));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-background-radius: 4;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 4 10 4 10;"));
        btnClose.setOnAction(e -> close());

        header.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        header.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });
        header.getChildren().addAll(titleBox, spacer, btnClose);
        return header;
    }

    private ScrollPane buildFormBody() {
        VBox form = new VBox(6);
        form.setPadding(new Insets(22, 32, 10, 32));
        form.setStyle("-fx-background-color: white;");

        int curYear = LocalDate.now().getYear();

        // ── CCCD + Tên ──────────────────────────────────────────────────
        txtCCCD = makeField("", "Nhập CCCD (12 số)");
        try {
            ValidationUtils.applyNumericOnlyFilter(txtCCCD, 12);
        } catch (Exception ignored) {
        }
        errCCCD = errLabel();
        txtCCCD.focusedProperty().addListener((obs, o, n) -> {
            if (!n)
                validateCCCD();
        });
        txtCCCD.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() == 12) {
                model.entities.KhachHang kh = khachHangDAO.findByCCCD(newVal);
                if (kh != null) {
                    if (txtHoTen != null) {
                        txtHoTen.setText(kh.getTenKH());
                        txtHoTen.setEditable(false);
                        txtHoTen.setStyle(fieldStyle() + " -fx-background-color: #f3f4f6;");
                    }
                    if (txtSoDT != null) {
                        txtSoDT.setText(kh.getSoDT());
                    }
                    if (dpNgaySinh != null && kh.getNgaySinh() != null) {
                        dpNgaySinh.setValue(kh.getNgaySinh());
                        dpNgaySinh.setDisable(true);
                        dpNgaySinh.setStyle("-fx-opacity: 0.8;");
                    }
                    clearErrorField(txtCCCD, errCCCD);
                } else {
                    if (txtHoTen != null) {
                        txtHoTen.setEditable(true);
                        txtHoTen.setStyle(fieldStyle());
                    }
                    if (dpNgaySinh != null) {
                        dpNgaySinh.setDisable(false);
                        dpNgaySinh.setStyle("");
                    }
                }
                validateSDT(); // Re-validate phone in case it was flagged as mismatch
            } else if (newVal != null && newVal.length() < 12) {
                if (txtHoTen != null && !txtHoTen.isEditable()) {
                    txtHoTen.setEditable(true);
                    txtHoTen.setStyle(fieldStyle());
                }
                if (dpNgaySinh != null && dpNgaySinh.isDisabled()) {
                    dpNgaySinh.setDisable(false);
                    dpNgaySinh.setStyle("");
                }
            }
        });

        txtHoTen = makeField("", "Nhập họ và tên khách hàng");
        errTen = errLabel();

        HBox rowCCCD_Ten = new HBox(16);
        VBox colCCCD = fieldBlock("Số CCCD (Nhập trước) *", txtCCCD, errCCCD, null);
        VBox colTen = fieldBlock("Họ và tên *", txtHoTen, errTen, null);
        HBox.setHgrow(colCCCD, Priority.ALWAYS);
        HBox.setHgrow(colTen, Priority.ALWAYS);
        rowCCCD_Ten.getChildren().addAll(colCCCD, colTen);
        form.getChildren().add(rowCCCD_Ten);

        // ── Ngày sinh + SĐT ─────────────────────────────────────────────
        dpNgaySinh = new DatePicker(curYear - 100, curYear + 25);
        dpNgaySinh.setPromptText("Chọn ngày sinh khách hàng");
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        errNS = errLabel();

        txtSoDT = makeField("", "Nhập số điện thoại");
        try {
            ValidationUtils.applyNumericOnlyFilter(txtSoDT, 10);
        } catch (Exception ignored) {
        }
        errSDT = errLabel();

        // Kiểm tra SĐT trùng với khách hàng khác
        txtSoDT.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() == 10) {
                model.entities.KhachHang kh = khachHangDAO.findBySDT(newVal);
                if (kh != null) {
                    khFoundBySDT = kh;
                    validateSDT(); // Trigger validation to show error if CCCD doesn't match
                } else {
                    khFoundBySDT = null;
                    errSDT.setText("");
                }
            } else {
                khFoundBySDT = null;
                if (newVal != null && newVal.length() < 10) {
                    errSDT.setText("");
                }
            }
        });

        HBox rowNS_SDT = new HBox(16);
        VBox colNS = fieldBlock("Ngày sinh *", dpNgaySinh, errNS, "Vui lòng chọn ngày sinh (từ đủ 16 tuổi)");
        VBox colSDT = fieldBlock("Số điện thoại *", txtSoDT, errSDT, null);
        HBox.setHgrow(colNS, Priority.ALWAYS);
        HBox.setHgrow(colSDT, Priority.ALWAYS);
        rowNS_SDT.getChildren().addAll(colNS, colSDT);
        form.getChildren().add(rowNS_SDT);

        // ── Ngày check-in / out ──────────────────────────────────────────
        dpCheckIn = new DatePicker(curYear, curYear + 2);
        dpCheckIn.setPromptText("Chọn ngày nhận phòng");
        dpCheckIn.setValue(LocalDate.now());
        try {
            dpCheckIn.setMinDate(LocalDate.now());
        } catch (Exception ignored) {
        }
        dpCheckIn.setMaxWidth(Double.MAX_VALUE);

        dpCheckOut = new DatePicker(curYear, curYear + 2);
        dpCheckOut.setPromptText("Chọn ngày trả phòng");
        dpCheckOut.setValue(LocalDate.now().plusDays(1));
        try {
            dpCheckOut.setMinDate(LocalDate.now().plusDays(1));
        } catch (Exception ignored) {
        }
        dpCheckOut.setMaxWidth(Double.MAX_VALUE);

        dpCheckIn.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                try {
                    dpCheckOut.setMinDate(n.plusDays(1));
                } catch (Exception ignored) {
                }
                if (dpCheckOut.getValue() != null && !dpCheckOut.getValue().isAfter(n))
                    dpCheckOut.setValue(n.plusDays(1));
            }
            reloadPhongTrong();
            updateTongTien();
        });
        dpCheckOut.valueProperty().addListener((obs, o, n) -> {
            reloadPhongTrong();
            updateTongTien();
        });

        errNgayIn = errLabel();
        errNgayOut = errLabel();

        HBox rowDate = new HBox(16);
        VBox colIn = fieldBlock("Ngày nhận phòng *", dpCheckIn, errNgayIn, null);
        VBox colOut = fieldBlock("Ngày trả phòng *", dpCheckOut, errNgayOut, null);
        HBox.setHgrow(colIn, Priority.ALWAYS);
        HBox.setHgrow(colOut, Priority.ALWAYS);
        rowDate.getChildren().addAll(colIn, colOut);
        form.getChildren().add(rowDate);

        // ── Số người ────────────────────────────────────────────────────
        txtSoNguoi = makeField("1", "Nhập số người");
        try {
            ValidationUtils.applyNumericOnlyFilter(txtSoNguoi, 2);
        } catch (Exception ignored) {
        }
        errSoNguoi = errLabel();
        txtSoNguoi.textProperty().addListener((obs, o, n) -> {
            updateGopY();
            updateCapacityWarning();
        });

        lblGopY = new Label("");
        lblGopY.setFont(Font.font("Segoe UI", 12));
        lblGopY.setTextFill(Color.web(C_ACTIVE));
        lblGopY.setWrapText(true);

        VBox colNguoi = fieldBlock("Số người *", txtSoNguoi, errSoNguoi, null);
        colNguoi.getChildren().add(lblGopY);
        form.getChildren().add(colNguoi);

        // ── Chọn loại phòng (multi CheckBox) ────────────────────────────
        Label lblLoaiPhongHeader = new Label("Loại phòng *  (có thể chọn nhiều)");
        lblLoaiPhongHeader.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblLoaiPhongHeader.setTextFill(Color.web("#374151"));

        loaiPhongCheckBoxArea = new VBox(6);
        loaiPhongCheckBoxArea.setPadding(new Insets(8, 12, 8, 12));
        loaiPhongCheckBoxArea.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;"
                + " -fx-border-color: " + C_BORDER + "; -fx-border-radius: 8;");

        List<LoaiPhong> allLoaiPhong = loaiPhongDAO.getAll();
        for (LoaiPhong lp : allLoaiPhong) {
            String displayName = lp.toString() + " — " + DF.format(lp.getGia()) + " đ/đêm"
                    + " (Sức chứa: " + lp.getSucChua() + " người)";
            CheckBox cb = new CheckBox(displayName);
            cb.setFont(Font.font("Segoe UI", 13));
            cb.setTextFill(Color.web("#374151"));
            cb.selectedProperty().addListener((obs, o, n) -> {
                reloadPhongTrong();
                updateGopY();
                updateTongTien();
            });
            loaiPhongCheckBoxes.put(lp.getMaLoaiPhong(), cb);
            loaiPhongCheckBoxArea.getChildren().add(cb);
        }

        form.getChildren().addAll(lblLoaiPhongHeader, loaiPhongCheckBoxArea);

        // ── Chọn phòng cụ thể ───────────────────────────────────────────
        Label lblPhongHeader = new Label("Chọn phòng cụ thể *");
        lblPhongHeader.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblPhongHeader.setTextFill(Color.web("#374151"));

        Label lblPhongHint = new Label("Hệ thống sẽ tự tải danh sách phòng trống theo loại và ngày đã chọn");
        lblPhongHint.setFont(Font.font("Segoe UI", 11));
        lblPhongHint.setTextFill(Color.web(C_TEXT_GRAY));

        phongSelectFlow = new FlowPane(10, 8);
        phongSelectFlow.setPadding(new Insets(10, 12, 10, 12));
        phongSelectFlow.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;"
                + " -fx-border-color: " + C_BORDER + "; -fx-border-radius: 8;");
        phongSelectFlow.setMinHeight(60);

        // Label cảnh báo sức chứa (real-time)
        lblCapacityWarning = new Label("");
        lblCapacityWarning.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblCapacityWarning.setTextFill(Color.web(C_ERROR));
        lblCapacityWarning.setWrapText(true);

        form.getChildren().addAll(lblPhongHeader, lblPhongHint, phongSelectFlow, lblCapacityWarning);

        // ── Ghi chú ─────────────────────────────────────────────────────
        txtGhiChu = makeField("", "Ghi chú (không bắt buộc)");
        form.getChildren().add(fieldBlock("Ghi chú", txtGhiChu, null, null));

        // ── Tiền cọc (readonly) + Tính tiền ─────────────────────────────
        txtTienCoc = makeField("0", "Tiền đặt cọc");
        txtTienCoc.setEditable(false);
        txtTienCoc.setStyle(fieldStyle() + "-fx-background-color: #f3f4f6;");

        VBox colCoc = fieldBlock("Tiền đặt cọc (tự tính – 1 đêm đầu)", txtTienCoc, null, null);
        form.getChildren().add(colCoc);

        // ── Tổng tiền ────────────────────────────────────────────────────
        lblTongTienPhong = new Label("0 đ");
        lblTongTienPhong.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTongTienPhong.setTextFill(Color.web("#4b5563"));

        lblCanThanhToan = new Label("0 đ");
        lblCanThanhToan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblCanThanhToan.setTextFill(Color.web(C_ACTIVE));

        VBox totalBox = new VBox(6);
        totalBox.setPadding(new Insets(10, 0, 0, 0));
        totalBox.setStyle(
                "-fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        HBox rowTong = new HBox(8);
        rowTong.setAlignment(Pos.CENTER_LEFT);
        Label lbl1 = new Label("Tổng tiền phòng:");
        lbl1.setFont(Font.font("Segoe UI", 14));
        lbl1.setTextFill(Color.web(C_TEXT_GRAY));
        rowTong.getChildren().addAll(lbl1, lblTongTienPhong);

        HBox rowCan = new HBox(8);
        rowCan.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label("Cần thanh toán (Đã trừ cọc):");
        lbl2.setFont(Font.font("Segoe UI", 14));
        lbl2.setTextFill(Color.web(C_TEXT_GRAY));
        rowCan.getChildren().addAll(lbl2, lblCanThanhToan);

        totalBox.getChildren().addAll(rowTong, rowCan);
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

    private VBox buildFooterVBox() {
        if (isReadOnly) {
            VBox footerWrap = new VBox(10);
            footerWrap.setPadding(new Insets(12, 32, 20, 32));
            footerWrap.setStyle("-fx-background-color: white;");
            footerWrap.setAlignment(Pos.CENTER);
            Button btnClose = makeFooterBtn("ĐÓNG", "white", C_TEXT_GRAY, C_BORDER, "#f3f4f6");
            btnClose.setOnAction(e -> close());
            footerWrap.getChildren().add(btnClose);
            return footerWrap;
        }

        VBox footerWrap = new VBox(10);
        footerWrap.setPadding(new Insets(12, 32, 20, 32));
        footerWrap.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER
                + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        // Checkbox xác nhận cọc
        cbDaThanhToanCoc = new CheckBox("Xác nhận: Khách đã thanh toán tiền cọc");
        cbDaThanhToanCoc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        cbDaThanhToanCoc.setTextFill(Color.web(C_GREEN));
        cbDaThanhToanCoc.setTooltip(
                new Tooltip("Đánh dấu nếu đã thu tiền cọc.\nTrạng thái đơn sẽ được đặt là 'Đã xác nhận'."));

        Label lblCocHint = new Label("Trống -> Chờ xác nhận | Đã chọn -> Đã xác nhận");
        lblCocHint.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 11));
        lblCocHint.setTextFill(Color.web(C_TEXT_GRAY));

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = makeFooterBtn("Hủy", "white", "#374151", C_BORDER, "#f3f4f6");
        btnCancel.setOnAction(e -> close());

        btnSave = makeFooterBtn("💾 Thêm mới", C_SIDEBAR, "white", "transparent", C_ACTIVE);
        btnSave.setOnAction(e -> handleSave());

        btnRow.getChildren().addAll(btnCancel, btnSave);
        footerWrap.getChildren().addAll(cbDaThanhToanCoc, lblCocHint, btnRow);
        return footerWrap;
    }

    /* ── Validation ─────────────────────────────────────────────────── */
    private void setupValidation() {
        txtHoTen.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateTen();
        });
        txtSoDT.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateSDT();
        });
        txtCCCD.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateCCCD();
        });
        dpNgaySinh.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateNS();
        });
    }

    private boolean validateNS() {
        LocalDate ns = dpNgaySinh.getValue();
        if (ns == null) {
            showErrorField(dpNgaySinh, errNS, "⚠ Vui lòng chọn ngày sinh.");
            return false;
        }
        if (LocalDate.now().minusYears(16).isBefore(ns)) {
            showErrorField(dpNgaySinh, errNS, "⚠ Khách hàng phải từ đủ 16 tuổi.");
            return false;
        }
        clearErrorField(dpNgaySinh, errNS);
        return true;
    }

    private boolean validateTen() {
        String ten = txtHoTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) {
            showErrorField(txtHoTen, errTen, "⚠ Vui lòng nhập họ và tên.");
            return false;
        }
        if (!ten.matches(ValidationUtils.REGEX_NAME)) {
            showErrorField(txtHoTen, errTen,
                    "⚠ Chỉ được chứa chữ cái, khoảng trắng và dấu . - '");
            return false;
        }
        // [MỚI] Không cho 2 ký tự đặc biệt liên tiếp: --, .., .-, -., '., ...
        if (!ValidationUtils.isValidNameStructure(ten)) {
            showErrorField(txtHoTen, errTen,
                    "⚠ Không được có 2 ký tự đặc biệt liên tiếp (VD: --, .., .-)");
            return false;
        }
        clearErrorField(txtHoTen, errTen);
        return true;
    }

    private boolean validateSDT() {
        String sdt = txtSoDT.getText().trim();
        String cccd = txtCCCD.getText().trim();
        if (sdt.isEmpty()) {
            showErrorField(txtSoDT, errSDT, "⚠ Vui lòng nhập số điện thoại.");
            return false;
        }
        if (!sdt.matches(ValidationUtils.REGEX_PHONE_VN)) {
            showErrorField(txtSoDT, errSDT, "⚠ Sai đầu số nhà mạng Việt Nam.");
            return false;
        }

        // Kiểm tra xem SĐT này có thuộc về khách hàng khác không
        if (khFoundBySDT != null && !cccd.equals(khFoundBySDT.getSoCCCD())) {
            showErrorField(txtSoDT, errSDT, "⚠ SĐT này thuộc về KH khác (" + khFoundBySDT.getTenKH() + ").");
            return false;
        }

        clearErrorField(txtSoDT, errSDT);
        return true;
    }

    private boolean validateCCCD() {
        String cccd = txtCCCD.getText().trim();
        LocalDate ns = dpNgaySinh.getValue();
        if (cccd.isEmpty()) {
            errCCCD.setText("⚠ Vui lòng nhập số CCCD.");
            return false;
        }
        if (!cccd.matches(ValidationUtils.REGEX_CCCD_FORMAT)) {
            errCCCD.setText("⚠ Phải gồm đúng 12 chữ số.");
            return false;
        }
        if (!ValidationUtils.isValidProvinceCode(cccd)) {
            errCCCD.setText("⚠ Mã tỉnh/thành phố không hợp lệ.");
            return false;
        }

        // Kiểm tra CCCD khớp với SĐT đã tồn tại trong hệ thống (như yêu cầu)
        if (khFoundBySDT != null && khFoundBySDT.getSoCCCD() != null
                && !cccd.equals(khFoundBySDT.getSoCCCD())) {
            errCCCD.setText("⚠ CCCD sai! SĐT này là của KH có CCCD: " + khFoundBySDT.getSoCCCD());
            return false;
        }

        if (ns != null) {
            int namSinh = ns.getYear();
            if (!ValidationUtils.isValidCCCDCenturyAndGender(cccd, namSinh)) {
                errCCCD.setText("⚠ 1 số không khớp (dưới 2000 0:nam 1:nữ, từ 2000 2:nam 3:nữ).");
                return false;
            }
            if (!ValidationUtils.isValidCCCDBirthYear(cccd, namSinh)) {
                errCCCD.setText("⚠ 2 số năm sinh trên CCCD bị sai.");
                return false;
            }
        }
        errCCCD.setText("");
        return true;
    }

    /* ── Room Loading ────────────────────────────────────────────────── */
    /** Lấy danh sách loại đang được chọn */
    private List<String> getSelectedLoaiPhong() {
        return loaiPhongCheckBoxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Reload danh sách phòng trống theo loại đã chọn + khoảng ngày */
    private void reloadPhongTrong() {
        if (isReadOnly) 
            return;

        if (phongSelectFlow == null)
            return;

        List<String> oldSelected = phongCheckBoxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());

        phongSelectFlow.getChildren().clear();
        phongCheckBoxes.clear();
        phongMap.clear();

        List<String> selectedLoai = getSelectedLoaiPhong();
        if (selectedLoai.isEmpty()) {
            Label lbl = new Label("← Hãy chọn ít nhất 1 loại phòng để xem phòng trống");
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            phongSelectFlow.getChildren().add(lbl);
            return;
        }

        LocalDate dIn = dpCheckIn != null ? dpCheckIn.getValue() : LocalDate.now();
        LocalDate dOut = dpCheckOut != null ? dpCheckOut.getValue() : LocalDate.now().plusDays(1);
        if (dIn == null)
            dIn = LocalDate.now();
        if (dOut == null || !dOut.isAfter(dIn))
            dOut = dIn.plusDays(1);

        List<Phong> phongs = phongDAO.getPhongTrongByMultiLoai(selectedLoai, dIn, dOut);

        if (phongs.isEmpty()) {
            Label lbl = new Label("⚠ Không có phòng trống cho khoảng thời gian này");
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(C_ERROR));
            phongSelectFlow.getChildren().add(lbl);
            return;
        }

        for (Phong p : phongs) {
            VBox card = new VBox(4);
            card.setPadding(new Insets(8, 12, 8, 12));
            card.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;"
                    + " -fx-border-color: " + C_BORDER + "; -fx-border-radius: 8;");

            CheckBox cb = new CheckBox(p.getMaPhong());
            cb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            cb.setTextFill(Color.web("#1e3a8a"));
            cb.selectedProperty().addListener((obs, o, n) -> {
                card.setStyle(n
                        ? "-fx-background-color: #eff6ff; -fx-background-radius: 8; -fx-border-color: " + C_ACTIVE
                                + "; -fx-border-radius: 8; -fx-border-width: 1.5;"
                        : "-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-border-color: " + C_BORDER
                                + "; -fx-border-radius: 8;");
                updateTongTien();
                updateCapacityWarning();
                if (btnSave != null)
                    btnSave.setDisable(false);
            });
            if (oldSelected.contains(p.getMaPhong())) {
                cb.setSelected(true);
            }

            Label lblLoai = new Label(p.getLoaiPhong().toString());
            lblLoai.setFont(Font.font("Segoe UI", 11));
            lblLoai.setTextFill(Color.web(C_ACTIVE));
            lblLoai.setStyle("-fx-background-color: #eff6ff; -fx-padding: 1 6; -fx-background-radius: 4;");

            Label lblGia = new Label(DF.format(p.getLoaiPhong().getGia()) + " đ/đêm");
            lblGia.setFont(Font.font("Segoe UI", 11));
            lblGia.setTextFill(Color.web(C_TEXT_GRAY));

            card.getChildren().addAll(cb, lblLoai, lblGia);
            phongCheckBoxes.put(p.getMaPhong(), cb);
            phongMap.put(p.getMaPhong(), p);
            phongSelectFlow.getChildren().add(card);
        }
        updateTongTien();
        updateCapacityWarning();
    }

    private void updateCapacityWarning() {
        if (isReadOnly) 
            return;

        if (lblCapacityWarning == null)
            return;
        List<Phong> selectedPhongs = getSelectedPhongs();
        if (selectedPhongs.isEmpty()) {
            lblCapacityWarning.setText("");
            return;
        }

        int totalCapacity = selectedPhongs.stream().mapToInt(p -> p.getLoaiPhong().getSucChua()).sum();
        String soNguoiStr = txtSoNguoi.getText().trim();
        int soNguoi = soNguoiStr.isEmpty() ? 0 : Integer.parseInt(soNguoiStr);

        if (totalCapacity < soNguoi) {
            lblCapacityWarning.setText(String.format(
                    "⚠ Không đủ sức chứa! Bạn đang chọn %d người nhưng tổng sức chứa chỉ là %d. Vui lòng chọn thêm phòng.",
                    soNguoi, totalCapacity));
        } else {
            lblCapacityWarning.setText("");
        }
    }

    /** Gợi ý số phòng dựa trên số người và loại phòng đã chọn */
    private void updateGopY() {
        if (txtSoNguoi == null || lblGopY == null)
            return;
        try {
            int soNguoi = Integer.parseInt(txtSoNguoi.getText().trim());
            if (soNguoi <= 0) {
                lblGopY.setText("");
                return;
            }

            List<String> selectedLoai = getSelectedLoaiPhong();
            if (selectedLoai.isEmpty()) {
                lblGopY.setText("💡 Hãy chọn loại phòng để xem gợi ý.");
                return;
            }

            // Tính gợi ý đơn giản dựa trên loại đầu tiên được chọn
            int maxSucChua = loaiPhongDAO.getAll().stream()
                    .filter(lp -> selectedLoai.contains(lp.getMaLoaiPhong()))
                    .mapToInt(LoaiPhong::getSucChua).max().orElse(2);

            int soPhongGoiY = (int) Math.ceil((double) soNguoi / maxSucChua);
            lblGopY.setText("💡 Gợi ý: cần ít nhất " + soPhongGoiY + " phòng cho " + soNguoi + " người.");
        } catch (Exception ignored) {
            lblGopY.setText("");
        }
    }

    /* ── Tính tiền tự động ──────────────────────────────────────────── */
    private void updateTongTien() {
        if (isReadOnly) 
            return;

        if (lblTongTienPhong == null || txtTienCoc == null)
            return;
        try {
            LocalDate dIn = dpCheckIn.getValue();
            LocalDate dOut = dpCheckOut.getValue();
            if (dIn == null || dOut == null) {
                lblTongTienPhong.setText("0 d");
                lblCanThanhToan.setText("0 d");
                txtTienCoc.setText("0");
                return;
            }
            long rawDays = ChronoUnit.DAYS.between(dIn, dOut);
            final long days = rawDays <= 0 ? 1 : rawDays;
            double tongPhong, coc;
            // ADD MODE: dung phong dang duoc chon trong FlowPane
            List<Phong> selectedPhongs = getSelectedPhongs();
            tongPhong = selectedPhongs.stream().mapToDouble(p -> p.getLoaiPhong().getGia() * days).sum();
            coc = selectedPhongs.stream().mapToDouble(p -> p.getLoaiPhong().getGia()).sum();
            txtTienCoc.setText(DF.format(coc));
            lblTongTienPhong.setText(DF.format(tongPhong) + " d");
            lblCanThanhToan.setText(DF.format(Math.max(0, tongPhong - coc)) + " d");
        } catch (Exception ignored) {
            lblTongTienPhong.setText("0 d");
            lblCanThanhToan.setText("0 d");
        }
    }

    private List<Phong> getSelectedPhongs() {
        return phongCheckBoxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(e -> phongMap.get(e.getKey()))
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    private double parseTienCoc() {
        String raw = txtTienCoc.getText().replaceAll("[^\\d]", "");
        return raw.isEmpty() ? 0 : Double.parseDouble(raw);
    }

    /* ── SAVE ───────────────────────────────────────────────────────── */
    private void handleSave() {
        boolean ok = true;
        if (!validateTen())
            ok = false;
        if (!validateSDT())
            ok = false;
        if (!validateNS())
            ok = false;
        if (!validateCCCD())
            ok = false;

        if (dpCheckIn.getValue() == null) {
            errNgayIn.setText("⚠ Chọn ngày nhận phòng");
            ok = false;
        } else
            errNgayIn.setText("");
        if (dpCheckOut.getValue() == null) {
            errNgayOut.setText("⚠ Chọn ngày trả phòng");
            ok = false;
        } else if (dpCheckIn.getValue() != null && !dpCheckOut.getValue().isAfter(dpCheckIn.getValue())) {
            errNgayOut.setText("⚠ Ngày trả phải sau ngày nhận");
            ok = false;
        } else
            errNgayOut.setText("");

        String soNguoiStr = txtSoNguoi.getText().trim();
        if (soNguoiStr.isEmpty() || Integer.parseInt(soNguoiStr) < 1) {
            errSoNguoi.setText("⚠ Tối thiểu 1 người");
            ok = false;
        } else
            errSoNguoi.setText("");

        List<Phong> selectedPhongs = getSelectedPhongs();
        if (selectedPhongs.isEmpty()) {
            ok = false;
        }
        // Capacity check đã hiện real-time qua lblCapacityWarning
        if (lblCapacityWarning != null && !lblCapacityWarning.getText().isEmpty()) {
            ok = false;
        }

        final boolean daThanhToanCoc = cbDaThanhToanCoc != null && cbDaThanhToanCoc.isSelected();
        if (!ok || !daThanhToanCoc) {
            showError("⚠ Vui lòng điền đầy đủ thông tin và thanh toán cọc để xác nhận đặt phòng.");
            return;
        }

        // Trích xuất toàn bộ dữ liệu trên FX Thread
        final String hoTen = ValidationUtils.toTitleCase(txtHoTen.getText().trim().replaceAll("\\s+", " "));
        final String soDT = txtSoDT.getText().trim();
        final String cccd = txtCCCD.getText().trim();
        final LocalDate ngaySinh = dpNgaySinh.getValue();
        final LocalDate checkIn = dpCheckIn.getValue();
        final LocalDate checkOut = dpCheckOut.getValue();
        final int soNguoi = Integer.parseInt(txtSoNguoi.getText().trim());
        final double tienCoc = parseTienCoc();
        final String ghiChu = txtGhiChu.getText().trim();

        String tempMa = khachHangDAO.getNextMaKH();
        if (tempMa == null)
            tempMa = "KH001"; // Phòng hờ nếu database lỗi trả về null
        final String preGenMaKH = tempMa;
        final String preGenMaDat = datPhongDAO.generateMaDat();
        final String baseMaCTDP = ctdpDAO.generateMaCTDP();
        final String preGenMaHD = new dao.HoaDonDAO().generateMaHD();
        final List<Phong> finalPhongs = new ArrayList<>(selectedPhongs);

        // UI loading state
        String oldBtnText = btnSave.getText();
        btnSave.setDisable(true);
        btnSave.setText("⏳ Đang lưu...");

        new Thread(() -> {
            boolean success = false;
            String errorMsg = "";

            try (Connection con = ConnectDatabase.getInstance().getConnection()) {
                con.setAutoCommit(false);
                try {
                    String maKH = khachHangDAO.findOrCreate(con, hoTen, soDT, cccd, ngaySinh, preGenMaKH);
                    String trangThaiMoi = "DA_XACNHAN"; // Always DA_XACNHAN because we force deposit
                    datPhongDAO.insertWithConnection(con, preGenMaDat, maKH, checkIn, checkOut, trangThaiMoi);

                    // Parse base number cho maCTDP
                    int baseNum = 0;
                    if (baseMaCTDP != null && baseMaCTDP.length() > 4) {
                        try {
                            baseNum = Integer.parseInt(baseMaCTDP.substring(4));
                        } catch (Exception ignored) {
                        }
                    }

                    // --- ĐOẠN MỚI THAY THẾ ---
                    int nguoiConLai = soNguoi;

                    for (int i = 0; i < finalPhongs.size(); i++) {
                        Phong p = finalPhongs.get(i);
                        String maCTDP = String.format("CTDP%04d", baseNum + i);
                        double cdpCoc = p.getLoaiPhong().getGia();

                        // Tính số người cho từng phòng: dồn vào cho đến khi hết người
                        int currentCdpNguoi = 0;
                        if (nguoiConLai > 0) {
                            int sucChua = p.getLoaiPhong().getSucChua();
                            // Lấy số người nhỏ hơn giữa "người còn dư" và "sức chứa của phòng"
                            currentCdpNguoi = Math.min(nguoiConLai, sucChua);
                            nguoiConLai -= currentCdpNguoi;
                        }

                        ctdpDAO.insertWithConnection(con, maCTDP, p.getMaPhong(), preGenMaDat, cdpCoc, currentCdpNguoi,
                                ghiChu);
                    }
                    // --- HẾT ĐOẠN THAY THẾ ---

                    // Tự động sinh Hóa Đơn ngay khi tạo phiếu đặt phòng thành công
                    dao.HoaDonDAO hdDAO = new dao.HoaDonDAO();
                    model.entities.HoaDon hd = new model.entities.HoaDon();
                    hd.setMaHD(preGenMaHD);
                    hd.setDatPhong(new model.entities.DatPhong(preGenMaDat));
                    hd.setNhanVien(new model.entities.NhanVien("ADMIN"));
                    hd.setNgayTaoHD(LocalDateTime.now());
                    hd.setTienPhong(0);
                    hd.setTienDV(0);
                    hd.setTienCoc(tienCoc);
                    hd.setTongTien(0);
                    hd.setLoaiHD("HOA_DON_PHONG");
                    hd.setTrangThaiThanhToan("DA_THANH_TOAN_COC");
                    hdDAO.insertWithConnection(con, hd);

                    con.commit();
                    success = true;
                } catch (Exception ex) {
                    try {
                        con.rollback();
                    } catch (Exception ignored) {
                    }
                    errorMsg = ex.getMessage();
                    ex.printStackTrace();
                } finally {
                    try {
                        con.setAutoCommit(true);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                errorMsg = "Lỗi kết nối Server: " + ex.getMessage();
            }

            final boolean finalSuccess = success;
            final String finalError = errorMsg;
            Platform.runLater(() -> {
                if (finalSuccess) {
                    showInfo("Thành công!", "Đã thêm đơn đặt phòng.");
                    if (onSuccess != null)
                        onSuccess.run();
                    close();
                } else {
                    showError("Lỗi CSDL: " + finalError);
                    btnSave.setDisable(false);
                    btnSave.setText(oldBtnText);
                }
            });
        }).start();
    }

    /* ── UI Factory ──────────────────────────────────────────────────── */
    private String fieldStyle() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-pref-height: 40;"
                + " -fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;"
                + " -fx-border-color: " + C_BORDER + "; -fx-padding: 8 12 8 12;";
    }

    private TextField makeField(String value, String prompt) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        return tf;
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
        if (field instanceof Region r)
            r.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().add(field);
        if (errLbl != null) {
            errLbl.setMaxWidth(Double.MAX_VALUE);
            b.getChildren().add(errLbl);
        }
        if (hint != null) {
            Label h = new Label(hint);
            h.setFont(Font.font("Segoe UI", 11));
            h.setTextFill(Color.web(C_TEXT_GRAY));
            b.getChildren().add(h);
        }
        return b;
    }

    private void showErrorField(javafx.scene.Node tf, Label errLabel, String msg) {
        if (errLabel != null)
            errLabel.setText(msg);
        tf.setStyle(fieldStyle() + "-fx-border-color: " + C_ERROR + "; -fx-background-color: #fef2f2;");
    }

    private void clearErrorField(javafx.scene.Node tf, Label errLabel) {
        if (errLabel != null)
            errLabel.setText("");
        tf.setStyle(fieldStyle());
    }

    private Button makeFooterBtn(String text, String bg, String fg, String border, String bgHover) {
        Button btn = new Button(text);
        btn.setPrefHeight(40);
        btn.setPrefWidth(text.contains("Thêm") || text.contains("Cập") ? 140 : 100);
        String baseStyle = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
                + (border.equals("transparent") ? "" : "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        String hoverStyle = "-fx-background-color: " + bgHover + "; -fx-text-fill: " + fg + ";"
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
                + (border.equals("transparent") ? "" : "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

private void populateData(String maDat) {
        Object[] data = datPhongDAO.findEditDetail(maDat);
        if (data == null)
            return;

        txtHoTen.setText(nvl((String) data[0]));
        txtSoDT.setText(nvl((String) data[1]));
        txtCCCD.setText(nvl((String) data[2]));
        
        LocalDate dIn = (LocalDate) data[4];
        LocalDate dOut = (LocalDate) data[5];

        if (data[3] instanceof LocalDate)
            dpNgaySinh.setValue((LocalDate) data[3]);
        if (dIn != null)
            dpCheckIn.setValue(dIn); // Nhờ có "if (isReadOnly) return;" nên lệnh này không làm mất list phòng nữa
        if (dOut != null)
            dpCheckOut.setValue(dOut);

        txtSoNguoi.setText(String.valueOf(data[7]));
        txtGhiChu.setText(nvl((String) data[9]));

        double cocValue = (double) data[8];
        txtTienCoc.setText(DF.format(cocValue)); 

        java.util.List<Phong> dsPhongCuaDon = loadPhongsOfDatPhong(maDat);

        // [MỚI] Hiển thị tổng tiền và cần thanh toán từ dữ liệu quá khứ
        long rawDays = (dIn != null && dOut != null) ? ChronoUnit.DAYS.between(dIn, dOut) : 1;
        final long days = rawDays <= 0 ? 1 : rawDays;
        double tongPhong = 0;
        for (Phong p : dsPhongCuaDon) {
            tongPhong += p.getLoaiPhong().getGia() * days;
        }
        lblTongTienPhong.setText(DF.format(tongPhong) + " đ");
        lblCanThanhToan.setText(DF.format(Math.max(0, tongPhong - cocValue)) + " đ");

        // Tick sẵn tất cả các loại phòng xuất hiện trong đơn
        java.util.Set<String> loaiPhongDaChon = dsPhongCuaDon.stream()
                .map(p -> p.getLoaiPhong().getMaLoaiPhong())
                .collect(Collectors.toSet());
        for (Map.Entry<String, CheckBox> entry : loaiPhongCheckBoxes.entrySet()) {
            if (loaiPhongDaChon.contains(entry.getKey())) {
                entry.getValue().setSelected(true);
            }
        }

        // Render từng phòng cụ thể trong FlowPane
        phongSelectFlow.getChildren().clear();
        phongCheckBoxes.clear();
        phongMap.clear();

        if (dsPhongCuaDon.isEmpty()) {
            Label lbl = new Label("— Không có dữ liệu phòng");
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            phongSelectFlow.getChildren().add(lbl);
        } else {
            for (Phong p : dsPhongCuaDon) {
                VBox card = new VBox(4);
                card.setPadding(new Insets(8, 12, 8, 12));
                card.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8;"
                        + " -fx-border-color: " + C_ACTIVE + "; -fx-border-radius: 8; -fx-border-width: 1.5;");

                CheckBox cb = new CheckBox(p.getMaPhong());
                cb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                cb.setTextFill(Color.web("#1e3a8a"));
                cb.setSelected(true);

                Label lblLoai = new Label(p.getLoaiPhong() != null && p.getLoaiPhong().toString() != null
                        ? p.getLoaiPhong().toString()
                        : p.getLoaiPhong().getMaLoaiPhong());
                lblLoai.setFont(Font.font("Segoe UI", 11));
                lblLoai.setTextFill(Color.web(C_ACTIVE));
                lblLoai.setStyle("-fx-background-color: white; -fx-padding: 1 6; -fx-background-radius: 4;");

                Label lblGia = new Label(DF.format(p.getLoaiPhong().getGia()) + " đ/đêm");
                lblGia.setFont(Font.font("Segoe UI", 11));
                lblGia.setTextFill(Color.web(C_TEXT_GRAY));

                card.getChildren().addAll(cb, lblLoai, lblGia);
                phongCheckBoxes.put(p.getMaPhong(), cb);
                phongMap.put(p.getMaPhong(), p);
                phongSelectFlow.getChildren().add(card);
            }
        }
    }

    /**
     * [MỚI] Lấy danh sách phòng cụ thể của 1 đơn đặt phòng
     * (kèm loaiPhong thực — không chỉ loại đại diện).
     */
    private java.util.List<Phong> loadPhongsOfDatPhong(String maDat) {
        java.util.List<Phong> ds = new ArrayList<>();
        String sql = "SELECT p.maPhong, p.tenPhong, p.loaiPhong, p.soPhong, p.soTang, " +
                     "       lp.gia, lp.sucChua, lp.tenLoaiPhong " +
                     "FROM ChiTietDatPhong ctdp " +
                     "JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                     "JOIN LoaiPhong lp ON p.loaiPhong = lp.maLoaiPhong " +
                     "WHERE ctdp.maDat = ?";
        try (Connection con = ConnectDatabase.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDat);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LoaiPhong lp = new LoaiPhong();
                lp.setMaLoaiPhong(rs.getString("loaiPhong"));
                lp.setGia(rs.getDouble("gia"));
                lp.setSucChua(rs.getInt("sucChua"));

                Phong p = new Phong();
                p.setMaPhong(rs.getString("maPhong"));
                p.setTenPhong(rs.getString("tenPhong"));
                p.setLoaiPhong(lp);
                p.setSoPhong(rs.getInt("soPhong"));
                p.setSoTang(rs.getInt("soTang"));
                ds.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    private void disableInputs() {
        txtHoTen.setEditable(false);
        txtSoDT.setEditable(false);
        txtCCCD.setEditable(false);
        txtSoNguoi.setEditable(false);
        txtGhiChu.setEditable(false);
        dpNgaySinh.setDisable(true);
        dpCheckIn.setDisable(true);
        dpCheckOut.setDisable(true);

        if (loaiPhongCheckBoxArea != null)
            loaiPhongCheckBoxArea.setDisable(true);
        if (phongSelectFlow != null)
            phongSelectFlow.setDisable(true);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}