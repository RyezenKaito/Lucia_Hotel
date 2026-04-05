package gui;

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

import java.util.List;
import java.util.Objects;

import dao.NhanVienDAO;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.enums.trinhDo;
import model.enums.TrangThaiNV;

/**
 * ThemSuaNhanVienDialog – JavaFX.
 *
 * Quy tắc phân quyền:
 * ─ ADMIN : Có thể thêm cả Quản lý lẫn Nhân viên. Sửa/xóa tất cả.
 * ─ QUAN_LY : Khi THÊM → chức vụ gán cứng "Nhân viên" (không ComboBox).
 * Khi SỬA → chức vụ readonly.
 * ─ Khi chọn QUAN_LY làm chức vụ mới: trình độ phải là DAIHOC hoặc TREN_DAIHOC.
 */
public class ThemSuaNhanVienDialog extends Stage {

    /* ── Bảng màu ─────────────────────────────────────────────────── */
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_CONTENT_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_GOLD = "#d4af37";
    private static final String C_ERROR = "#dc2626";

    /* ── State ────────────────────────────────────────────────────── */
    private final NhanVienDAO dao = new NhanVienDAO();
    private final NhanVien nvEdit; // null = thêm mới
    private final NhanVien currentUser; // người đang đăng nhập
    private final NhanVienView parentView; // để reload

    /** Có phải ADMIN không (toàn quyền) */
    private final boolean isAdmin;

    /* ── Form fields ──────────────────────────────────────────────── */
    private TextField txtTen, txtSDT, txtCCCD, txtDiaChi;
    private ComboBox<String> cbChucVu;
    private ComboBox<trinhDo> cbTrinhDo;
    private ComboBox<TrangThaiNV> cbTrangThai;
    private ComboBox<NhanVien> cbNguoiQuanLy;
    private Button btnSave;

    private DatePicker dpNgaySinh;
    private Label lblErrTen, lblErrSDT, lblErrCCCD, lblErrDiaChi, lblErrNS;
    private VBox nguoiQuanLyBox;

    /* ── Constructor ──────────────────────────────────────────────── */
    public ThemSuaNhanVienDialog(Window owner, NhanVien nvEdit,
            NhanVien currentUser, NhanVienView parentView) {
        this.nvEdit = nvEdit;
        this.currentUser = currentUser;
        this.parentView = parentView;
        this.isAdmin = (currentUser != null && currentUser.getRole() == ChucVu.ADMIN);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(buildRoot(), 560, 640);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        setScene(scene);
        centerOnScreen();
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
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 6);");
        root.getChildren().addAll(
                buildDialogHeader(),
                buildFormBody(),
                buildFooter());
        return root;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * HEADER
     * ════════════════════════════════════════════════════════════════
     */
    private HBox buildDialogHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle(
                "-fx-background-color: " + C_SIDEBAR + ";" +
                        "-fx-background-radius: 16 16 0 0;");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label(nvEdit == null ? "THÊM NHÂN VIÊN MỚI" : "CẬP NHẬT NHÂN VIÊN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblTitle.setTextFill(Color.WHITE);

        Label lblSub = new Label(nvEdit == null
                ? "Điền đầy đủ thông tin bên dưới"
                : "Chỉnh sửa thông tin nhân viên " + nvEdit.getMaNV());
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
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10 4 10;" +
                        "-fx-background-radius: 6;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 4 10 4 10;"));
        btnClose.setOnAction(e -> close());

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

        /* ── Họ và tên ─────────────────────────────────────────────── */
        txtTen = makeField("Nhập họ và tên");
        lblErrTen = makeErrLabel();
        form.getChildren().add(fieldRow("Họ và tên *", txtTen, lblErrTen));

        /* ── Địa chỉ ────────────────────────────────────────────── */
        txtDiaChi = makeField("Nhập địa chỉ");
        lblErrDiaChi = makeErrLabel();
        form.getChildren().add(fieldRow("Địa chỉ", txtDiaChi, lblErrDiaChi));

        /* ── SĐT ────────────────────────────────────────────────── */
        txtSDT = makeField("VD: 0901234567");
        lblErrSDT = makeErrLabel();
        form.getChildren().add(fieldRow("Số điện thoại *", txtSDT, lblErrSDT));

        txtCCCD = makeField("VD: 001234567890 (Mã định danh 12 số)");
        model.utils.ValidationUtils.applyNumericOnlyFilter(txtCCCD, 12);
        lblErrCCCD = makeErrLabel();
        form.getChildren().add(fieldRow("Số CCCD *", txtCCCD, lblErrCCCD));

        /* ── Ngày sinh ──────────────────────────────────────────── */
        dpNgaySinh = new DatePicker();
        dpNgaySinh.setPromptText("Chọn ngày sinh");
        dpNgaySinh.setPrefHeight(40);
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinh.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;" +
                        "-fx-font-size: 13px;");
        lblErrNS = makeErrLabel();
        form.getChildren().add(fieldRow("Ngày sinh *", dpNgaySinh, lblErrNS));

        /* ── Trình độ ───────────────────────────────────────────── */
        cbTrinhDo = new ComboBox<>();
        cbTrinhDo.getItems().addAll(trinhDo.values());
        cbTrinhDo.getSelectionModel().selectFirst();
        styleCombo(cbTrinhDo);
        form.getChildren().add(comboRow("Trình độ *", cbTrinhDo));

        /* ── Chức vụ ────────────────────────────────────────────── */
        cbChucVu = new ComboBox<>();
        cbChucVu.getItems().addAll("Quản lý", "Nhân viên");
        styleCombo(cbChucVu);

        if (nvEdit != null) {
            if (isAdmin) {
                // ADMIN sửa: cho phép thay đổi chức vụ
                cbChucVu.getSelectionModel().select(getRoleLabel(nvEdit.getRole()));
                form.getChildren().add(comboRow("Chức vụ *", cbChucVu));
            } else {
                // SỬA: QUẢN LÝ chỉ xem chức vụ hiện tại, readonly
                String roleLabel = getRoleLabel(nvEdit.getRole());
                TextField txtCV = makeReadonlyField(roleLabel);
                form.getChildren().add(comboRow("Chức vụ", txtCV));
            }
        } else if (isAdmin) {
            // THÊM + ADMIN: chọn tự do Quản lý / Nhân viên
            cbChucVu.getSelectionModel().select("Nhân viên");
            form.getChildren().add(comboRow("Chức vụ *", cbChucVu));
        } else {
            // THÊM + QUAN_LY: gán cứng Nhân viên, không cho chọn
            TextField txtCV = makeReadonlyField("Nhân viên");
            form.getChildren().add(comboRow("Chức vụ", txtCV));
        }
        
        /* ── Trạng thái (Chỉ hiện khi Sửa) ──────────────────────── */
        if (nvEdit != null) {
            cbTrangThai = new ComboBox<>();
            cbTrangThai.getItems().addAll(TrangThaiNV.values());
            styleCombo(cbTrangThai);
            
            if (isAdmin) {
                cbTrangThai.getSelectionModel().select(nvEdit.getTrangThai());
                form.getChildren().add(comboRow("Trạng thái", cbTrangThai));
            } else {
                TextField txtTT = makeReadonlyField(nvEdit.getTrangThai() == TrangThaiNV.CON_LAM ? "Còn làm" : "Đã nghỉ");
                form.getChildren().add(comboRow("Trạng thái", txtTT));
            }
        }

        /* ── Người quản lý ──────────────────────────────────────── */
        cbNguoiQuanLy = new ComboBox<>();
        styleCombo(cbNguoiQuanLy);
        loadManagersToCombo();
        cbNguoiQuanLy.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NhanVien item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getHoTen() + " (" + item.getMaNV() + ")");
            }
        });
        cbNguoiQuanLy.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(NhanVien item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getHoTen() + " (" + item.getMaNV() + ")");
            }
        });

        nguoiQuanLyBox = new VBox(4);
        Label lblNQL = new Label("Người quản lý");
        lblNQL.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lblNQL.setTextFill(Color.web("#374151"));
        nguoiQuanLyBox.getChildren().addAll(lblNQL, cbNguoiQuanLy);

        // Mặc định chọn chính mình nếu đang là QUAN_LY
        if (!isAdmin && currentUser != null && currentUser.getRole() == ChucVu.QUAN_LY) {
            for (int i = 0; i < cbNguoiQuanLy.getItems().size(); i++) {
                if (Objects.equals(cbNguoiQuanLy.getItems().get(i).getMaNV(), currentUser.getMaNV())) {
                    cbNguoiQuanLy.getSelectionModel().select(i);
                    break;
                }
            }
            cbNguoiQuanLy.setDisable(true); // disable luôn để khỏi chuyển cho ông quản lý khác
        }

        updateNguoiQuanLyVisibility();
        form.getChildren().add(nguoiQuanLyBox);

        // Listener cập nhật visibility khi ADMIN đổi cbChucVu
        if (isAdmin) {
            cbChucVu.setOnAction(e -> updateNguoiQuanLyVisibility());
        }

        /* ── Load dữ liệu khi sửa ──────────────────────────────── */
        if (nvEdit != null) {
            txtTen.setText(nvEdit.getHoTen());
            txtDiaChi.setText(nvEdit.getDiaChi() != null ? nvEdit.getDiaChi() : "");
            txtSDT.setText(nvEdit.getSoDT());
            txtCCCD.setText(nvEdit.getCccd() != null ? nvEdit.getCccd() : "");

            if (nvEdit.getNgaySinh() != null)
                dpNgaySinh.setValue(nvEdit.getNgaySinh());

            if (nvEdit.getTrinhDo() != null)
                cbTrinhDo.getSelectionModel().select(nvEdit.getTrinhDo());

            if (nvEdit.getRole() == ChucVu.NHAN_VIEN && nvEdit.getMaQL() != null) {
                nguoiQuanLyBox.setVisible(true);
                nguoiQuanLyBox.setManaged(true);
                for (int i = 0; i < cbNguoiQuanLy.getItems().size(); i++) {
                    if (Objects.equals(cbNguoiQuanLy.getItems().get(i).getMaNV(), nvEdit.getMaQL())) {
                        cbNguoiQuanLy.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        }

        // Validation on focus lost
        setupValidation();
        setupChangeListeners();

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
        footer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + " transparent transparent transparent;" +
                        "-fx-border-width: 1 0 0 0;" +
                        "-fx-background-radius: 0 0 16 16;");

        Button btnCancel = makeFooterBtn("Hủy", "white", "#374151", C_BORDER, "#f3f4f6");
        btnCancel.setOnAction(e -> close());

        btnSave = makeFooterBtn(nvEdit == null ? "💾  Thêm mới" : "💾  Cập nhật",
                C_SIDEBAR, "white", "transparent", C_ACTIVE);
        btnSave.setOnAction(e -> handleSave());
        
        if (nvEdit != null) {
            btnSave.setDisable(true); // Mặc định khoá nút khi sửa
        }

        footer.getChildren().addAll(btnCancel, btnSave);
        return footer;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * VALIDATION
     * ════════════════════════════════════════════════════════════════
     */
    private void setupValidation() {
        txtTen.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateTen();
        });
        txtSDT.focusedProperty().addListener((o, ov, nv) -> {
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

    private boolean validateTen() {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) {
            showFieldError(lblErrTen, txtTen, "⚠ Không được để trống.");
            return false;
        }
        if (!ten.matches(model.utils.ValidationUtils.REGEX_NAME)) {
            showFieldError(lblErrTen, txtTen, "⚠ Chỉ được chứa chữ cái và khoảng trắng.");
            return false;
        }
        if (ten.matches(model.utils.ValidationUtils.REGEX_SPAM_CHAR)) {
            showFieldError(lblErrTen, txtTen, "⚠ Tên có chứa ký tự lặp lại bất thường.");
            return false;
        }
        if (!model.utils.ValidationUtils.isValidNameLength(ten)) {
            showFieldError(lblErrTen, txtTen, "⚠ Họ phải chứa ít nhất 1 ký tự, Tên chứa ít nhất 2 ký tự.");
            return false;
        }
        clearFieldError(lblErrTen, txtTen);
        return true;
    }

    private boolean validateNS() {
        java.time.LocalDate ns = dpNgaySinh.getValue();
        if (ns == null) {
            showFieldError(lblErrNS, dpNgaySinh, "⚠ Vui lòng chọn ngày sinh.");
            return false;
        }
        if (java.time.LocalDate.now().minusYears(16).isBefore(ns)) {
            showFieldError(lblErrNS, dpNgaySinh, "⚠ Khách hàng phải từ đủ 16 tuổi.");
            return false;
        }
        clearFieldError(lblErrNS, dpNgaySinh);
        return true;
    }

    private boolean validateSDT() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            showFieldError(lblErrSDT, txtSDT, "⚠ Không được để trống.");
            return false;
        }
        if (!sdt.matches(model.utils.ValidationUtils.REGEX_PHONE_VN)) {
            showFieldError(lblErrSDT, txtSDT, "⚠ Sai đầu số nhà mạng Việt Nam (03x, 05x, 07x, 08x, 09x).");
            return false;
        }
        clearFieldError(lblErrSDT, txtSDT);
        return true;
    }

    private boolean validateCCCD() {
        String cccd = txtCCCD.getText().trim();
        java.time.LocalDate ns = dpNgaySinh.getValue();
        if (cccd.isEmpty()) {
            showFieldError(lblErrCCCD, txtCCCD, "⚠ Không được để trống.");
            return false;
        }
        if (!cccd.matches(model.utils.ValidationUtils.REGEX_CCCD_FORMAT)) {
            showFieldError(lblErrCCCD, txtCCCD, "⚠ Phải gồm đúng 12 chữ số.");
            return false;
        }
        if (!model.utils.ValidationUtils.isValidProvinceCode(cccd)) {
            showFieldError(lblErrCCCD, txtCCCD, "⚠ Mã tỉnh/thành phố không hợp lệ.");
            return false;
        }
        if (ns != null) {
            int namSinh = ns.getYear();
            if (!model.utils.ValidationUtils.isValidCCCDCenturyAndGender(cccd, namSinh)) {
                showFieldError(lblErrCCCD, txtCCCD,
                        "⚠ số thứ 4 không khớp (dưới năm 2000 0:nam 1:nữ, từ năm 2000 2:nam, 3:nữ ).");
                return false;
            }
            if (!model.utils.ValidationUtils.isValidCCCDBirthYear(cccd, namSinh)) {
                showFieldError(lblErrCCCD, txtCCCD, "⚠ 2 số năm sinh trên CCCD bị sai (số 5,6).");
                return false;
            }
        }
        clearFieldError(lblErrCCCD, txtCCCD);
        return true;
    }

    private boolean validateAll() {
        boolean ok = true;
        if (!validateTen())
            ok = false;
        if (!validateSDT())
            ok = false;
        if (!validateCCCD())
            ok = false;
        if (!validateNS())
            ok = false;
        return ok;
    }

    private void setupChangeListeners() {
        javafx.beans.value.ChangeListener<Object> changeListener = (obs, oldVal, newVal) -> checkChanges();
        txtTen.textProperty().addListener(changeListener);
        txtSDT.textProperty().addListener(changeListener);
        txtCCCD.textProperty().addListener(changeListener);
        txtDiaChi.textProperty().addListener(changeListener);
        dpNgaySinh.valueProperty().addListener(changeListener);
        cbTrinhDo.valueProperty().addListener(changeListener);
        if (cbChucVu != null) cbChucVu.valueProperty().addListener(changeListener);
        if (cbTrangThai != null) cbTrangThai.valueProperty().addListener(changeListener);
        if (cbNguoiQuanLy != null) cbNguoiQuanLy.valueProperty().addListener(changeListener);
    }

    private void checkChanges() {
        if (nvEdit == null || btnSave == null) return;
        
        boolean changed = false;
        if (!Objects.equals(txtTen.getText().trim(), nvEdit.getHoTen() == null ? "" : nvEdit.getHoTen().trim())) changed = true;
        else if (!Objects.equals(txtSDT.getText().trim(), nvEdit.getSoDT() == null ? "" : nvEdit.getSoDT().trim())) changed = true;
        else if (!Objects.equals(txtCCCD.getText().trim(), nvEdit.getCccd() == null ? "" : nvEdit.getCccd().trim())) changed = true;
        else if (!Objects.equals(txtDiaChi.getText().trim(), nvEdit.getDiaChi() == null ? "" : nvEdit.getDiaChi().trim())) changed = true;
        else if (!Objects.equals(dpNgaySinh.getValue(), nvEdit.getNgaySinh())) changed = true;
        else if (!Objects.equals(cbTrinhDo.getValue(), nvEdit.getTrinhDo())) changed = true;
        
        if (!changed && isAdmin) {
             String oldRole = getRoleLabel(nvEdit.getRole());
             if (!Objects.equals(cbChucVu.getValue(), oldRole)) changed = true;
        }
        if (!changed && isAdmin && cbTrangThai != null) {
             if (!Objects.equals(cbTrangThai.getValue(), nvEdit.getTrangThai())) changed = true;
        }
        if (!changed && nguoiQuanLyBox != null && nguoiQuanLyBox.isVisible()) {
             String currentQL = cbNguoiQuanLy.getValue() != null ? cbNguoiQuanLy.getValue().getMaNV() : null;
             if (!Objects.equals(currentQL, nvEdit.getMaQL())) changed = true;
        }
        
        btnSave.setDisable(!changed);
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * SAVE
     * ════════════════════════════════════════════════════════════════
     */
    private void handleSave() {
        if (!validateAll())
            return;

        try {
            NhanVien n = (nvEdit != null) ? nvEdit : new NhanVien();

            // ── Xác định chức vụ target ──────────────────────────────
            ChucVu targetRole;
            if (isAdmin) {
                // ADMIN thêm/sửa: đọc từ ComboBox
                targetRole = "Quản lý".equals(cbChucVu.getValue()) ? ChucVu.QUAN_LY : ChucVu.NHAN_VIEN;
            } else if (nvEdit != null) {
                // QUAN_LY sửa: giữ nguyên role cũ
                targetRole = nvEdit.getRole();
            } else {
                // QUAN_LY thêm: gán cứng NV
                targetRole = ChucVu.NHAN_VIEN;
            }

            // ── Validate trình độ khi gán QUAN_LY ────────────────────
            trinhDo selectedTrinhDo = cbTrinhDo.getValue();
            if (targetRole == ChucVu.QUAN_LY) {
                if (selectedTrinhDo != trinhDo.DAIHOC && selectedTrinhDo != trinhDo.TREN_DAIHOC) {
                    showError("Quản lý phải có trình độ Đại học hoặc Trên đại học!");
                    return;
                }

                // Kiểm tra tối đa 3 quản lý
                if (nvEdit == null || nvEdit.getRole() != ChucVu.QUAN_LY) {
                    long managerCount = dao.getAll().stream().filter(nv -> nv.getRole() == ChucVu.QUAN_LY).count();
                    if (managerCount >= 3) {
                        showError("Hệ thống chỉ cho phép tối đa 3 Quản lý!");
                        return;
                    }
                }
            }

            // ── Tự động sinh mã khi thêm mới ───────────────────────
            if (nvEdit == null) {
                List<NhanVien> all = dao.getAll();
                int maxId = 0;
                for (NhanVien item : all) {
                    try {
                        int id = Integer.parseInt(item.getMaNV().replace("LUCIA", ""));
                        if (id > maxId)
                            maxId = id;
                    } catch (NumberFormatException ignored) {
                    }
                }
                n.setMaNV(String.format("LUCIA%03d", maxId + 1));
            }

            // ── Gán dữ liệu ────────────────────────────────────────
            n.setHoTen(model.utils.ValidationUtils.toTitleCase(txtTen.getText().trim().replaceAll("\\s+", " ")));
            n.setDiaChi(txtDiaChi.getText().trim());
            n.setSoDT(txtSDT.getText().trim());
            n.setCccd(txtCCCD.getText().trim());
            n.setNgaySinh(dpNgaySinh.getValue());
            n.setRole(targetRole);
            n.setTrinhDo(selectedTrinhDo);
            n.setTrangThai((nvEdit != null && isAdmin) ? cbTrangThai.getValue() : (nvEdit != null ? nvEdit.getTrangThai() : TrangThaiNV.CON_LAM));

            if (nvEdit != null) {
                n.setNgayVaoLamDate(nvEdit.getNgayVaoLamDate());
            } else {
                n.setNgayVaoLamDate(java.time.LocalDate.now());
            }

            if (n.getRole() == ChucVu.NHAN_VIEN && cbNguoiQuanLy.getValue() != null) {
                n.setMaQL(cbNguoiQuanLy.getValue().getMaNV());
            } else {
                n.setMaQL(null);
            }

            // ── Insert / Update ─────────────────────────────────────
            boolean ok = (nvEdit == null) ? dao.insert(n) : dao.update(n);

            if (ok) {
                showInfo("Thành công!", nvEdit == null
                        ? "Đã thêm nhân viên " + n.getMaNV() + " thành công."
                        : "Đã cập nhật thông tin nhân viên " + n.getMaNV() + ".");
                if (parentView != null)
                    parentView.loadData();
                close();
            } else {
                showError("Thao tác thất bại – kiểm tra lại dữ liệu hoặc cột cccd trong DB.");
            }

        } catch (Exception ex) {
            showError("Lỗi: " + ex.getMessage());
        }
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * HELPERS
     * ════════════════════════════════════════════════════════════════
     */

    /**
     * Xác định xem "Người quản lý" combobox có nên hiển thị không.
     * Ẩn khi: đang thêm và chức vụ = Quản lý, hoặc nvEdit là Quản lý/Admin.
     */
    private void updateNguoiQuanLyVisibility() {
        boolean showNQL;
        if (isAdmin) {
            showNQL = "Nhân viên".equals(cbChucVu.getValue());
        } else if (nvEdit != null) {
            showNQL = nvEdit.getRole() == ChucVu.NHAN_VIEN;
        } else {
            // QUAN_LY thêm → luôn hiển thị (vì target = NHAN_VIEN)
            showNQL = true;
        }
        nguoiQuanLyBox.setVisible(showNQL);
        nguoiQuanLyBox.setManaged(showNQL);
    }

    private void loadManagersToCombo() {
        cbNguoiQuanLy.getItems().clear();
        List<NhanVien> all = dao.getAll();
        if (all != null) {
            all.stream()
                    .filter(n -> n.getRole() == ChucVu.QUAN_LY)
                    .forEach(cbNguoiQuanLy.getItems()::add);
        }
    }

    private String getRoleLabel(ChucVu role) {
        if (role == null)
            return "Nhân viên";
        return switch (role) {
            case QUAN_LY -> "Quản lý";
            case ADMIN -> "Admin";
            default -> "Nhân viên";
        };
    }

    private VBox fieldRow(String labelText, Control field, Label errLabel) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 2, 0));

        HBox lblBox = new HBox(4);
        if (labelText.endsWith("*")) {
            Label lblText = new Label(labelText.substring(0, labelText.length() - 1).trim());
            lblText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblText.setTextFill(Color.web("#374151"));
            Label lblStar = new Label("*");
            lblStar.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblStar.setTextFill(Color.web(C_ERROR));
            lblBox.getChildren().addAll(lblText, lblStar);
        } else {
            Label lblText = new Label(labelText);
            lblText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblText.setTextFill(Color.web("#374151"));
            lblBox.getChildren().add(lblText);
        }

        box.getChildren().addAll(lblBox, field, errLabel);
        return box;
    }

    private VBox comboRow(String labelText, Control field) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 2, 0));

        HBox lblBox = new HBox(4);
        if (labelText.endsWith("*")) {
            Label lblText = new Label(labelText.substring(0, labelText.length() - 1).trim());
            lblText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblText.setTextFill(Color.web("#374151"));
            Label lblStar = new Label("*");
            lblStar.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblStar.setTextFill(Color.web(C_ERROR));
            lblBox.getChildren().addAll(lblText, lblStar);
        } else {
            Label lblText = new Label(labelText);
            lblText.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            lblText.setTextFill(Color.web("#374151"));
            lblBox.getChildren().add(lblText);
        }

        box.getChildren().addAll(lblBox, field);
        return box;
    }

    private TextField makeField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;");
        tf.focusedProperty().addListener((obs, o, focused) -> {
            if (focused) {
                tf.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + C_ACTIVE + ";" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 13px;" +
                                "-fx-padding: 8 12 8 12;" +
                                "-fx-border-width: 2;");
            } else {
                clearFieldError(null, tf); // restore normal on blur (validation will re-apply if needed)
            }
        });
        return tf;
    }

    private TextField makeReadonlyField(String value) {
        TextField tf = new TextField(value);
        tf.setEditable(false);
        tf.setPrefHeight(40);
        tf.setStyle(
                "-fx-background-color: #f3f4f6;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-text-fill: #6b7280;");
        return tf;
    }

    private Label makeErrLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(C_ERROR));
        l.setMinHeight(14);
        return l;
    }

    private void showFieldError(Label errLabel, Control tf, String msg) {
        if (errLabel != null)
            errLabel.setText(msg);
        tf.setStyle(
                "-fx-background-color: #fef2f2;" +
                        "-fx-border-color: " + C_ERROR + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;");
    }

    private void clearFieldError(Label errLabel, Control tf) {
        if (errLabel != null)
            errLabel.setText("");
        tf.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;");
    }

    private void styleCombo(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(40);
        cb.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;");
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
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Thông báo");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.initOwner(this);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.initOwner(this);
        a.showAndWait();
    }
}