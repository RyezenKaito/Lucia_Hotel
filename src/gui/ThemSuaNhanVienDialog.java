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

/**
 * ThemSuaNhanVienDialog – JavaFX (thay thế NhanVienForm Swing).
 *
 * Quy tắc phân quyền:
 * ─ Khi THÊM: Manager chỉ được chọn chức vụ "Nhân viên", option "Quản lý" bị disable.
 * ─ Khi SỬA:  Chỉ cho sửa nhân viên (staff). Chức vụ là readonly.
 */
public class ThemSuaNhanVienDialog extends Stage {

    /* ── Bảng màu ─────────────────────────────────────────────────── */
    private static final String C_SIDEBAR      = "#1e3a8a";
    private static final String C_ACTIVE       = "#1d4ed8";
    private static final String C_CONTENT_BG   = "#f8f9fa";
    private static final String C_BORDER       = "#e9ecef";
    private static final String C_GOLD         = "#d4af37";
    private static final String C_ERROR        = "#dc2626";

    /* ── State ────────────────────────────────────────────────────── */
    private final NhanVienDAO dao = new NhanVienDAO();
    private final NhanVien nvEdit;         // null = thêm mới
    private final NhanVien currentUser;    // người đang đăng nhập
    private final NhanVienView parentView; // để reload

    /* ── Form fields ──────────────────────────────────────────────── */
    private TextField txtTen, txtSDT, txtHeSo, txtDiaChi;
    private ComboBox<String> cbChucVu;
    private ComboBox<trinhDo> cbTrinhDo;
    private ComboBox<NhanVien> cbNguoiQuanLy;

    private Label lblErrTen, lblErrSDT, lblErrHeSo, lblErrDiaChi;
    private VBox nguoiQuanLyBox;

    /* ── Constructor ──────────────────────────────────────────────── */
    public ThemSuaNhanVienDialog(Window owner, NhanVien nvEdit,
                                  NhanVien currentUser, NhanVienView parentView) {
        this.nvEdit      = nvEdit;
        this.currentUser = currentUser;
        this.parentView  = parentView;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(buildRoot(), 560, 620);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        setScene(scene);
        centerOnScreen();
    }

    /* ════════════════════════════════════════════════════════════════
       ROOT
    ════════════════════════════════════════════════════════════════ */
    private VBox buildRoot() {
        VBox root = new VBox();
        root.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 6);"
        );

        root.getChildren().addAll(
            buildDialogHeader(),
            buildFormBody(),
            buildFooter()
        );
        return root;
    }

    /* ════════════════════════════════════════════════════════════════
       HEADER
    ════════════════════════════════════════════════════════════════ */
    private HBox buildDialogHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle(
            "-fx-background-color: " + C_SIDEBAR + ";" +
            "-fx-background-radius: 16 16 0 0;"
        );

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
            "-fx-padding: 4 10 4 10;"
        );
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 10 4 10;" +
            "-fx-background-radius: 6;"
        ));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 10 4 10;"
        ));
        btnClose.setOnAction(e -> close());

        header.getChildren().addAll(titleBox, spacer, btnClose);
        return header;
    }

    /* ════════════════════════════════════════════════════════════════
       FORM BODY
    ════════════════════════════════════════════════════════════════ */
    private ScrollPane buildFormBody() {
        VBox form = new VBox(6);
        form.setPadding(new Insets(22, 32, 10, 32));
        form.setStyle("-fx-background-color: white;");

        /* ── Họ tên ─────────────────────────────────────────────── */
        txtTen = makeField("Nhập họ tên nhân viên");
        lblErrTen = makeErrLabel();
        form.getChildren().addAll(fieldRow("Họ tên *", txtTen, lblErrTen));

        /* ── Địa chỉ ────────────────────────────────────────────── */
        txtDiaChi = makeField("Nhập địa chỉ");
        lblErrDiaChi = makeErrLabel();
        form.getChildren().addAll(fieldRow("Địa chỉ", txtDiaChi, lblErrDiaChi));

        /* ── SĐT ────────────────────────────────────────────────── */
        txtSDT = makeField("VD: 0901234567");
        lblErrSDT = makeErrLabel();
        form.getChildren().addAll(fieldRow("Số điện thoại *", txtSDT, lblErrSDT));

        /* ── Hệ số lương ────────────────────────────────────────── */
        txtHeSo = makeField("VD: 1.84");
        lblErrHeSo = makeErrLabel();
        form.getChildren().addAll(fieldRow("Hệ số lương *", txtHeSo, lblErrHeSo));

        /* ── Trình độ ───────────────────────────────────────────── */
        cbTrinhDo = new ComboBox<>();
        cbTrinhDo.getItems().addAll(trinhDo.values());
        cbTrinhDo.getSelectionModel().selectFirst();
        styleCombo(cbTrinhDo);
        form.getChildren().add(comboRow("Trình độ", cbTrinhDo));

        /* ── Chức vụ ────────────────────────────────────────────── */
        cbChucVu = new ComboBox<>();
        cbChucVu.getItems().addAll("Quản lý", "Nhân viên");
        styleCombo(cbChucVu);

        if (nvEdit != null) {
            // SỬA: chức vụ readonly (hiển thị TextField thay ComboBox)
            TextField txtCV = new TextField(
                nvEdit.getRole() == ChucVu.QUAN_LY ? "Quản lý" : "Nhân viên");
            txtCV.setEditable(false);
            txtCV.setStyle(
                "-fx-background-color: #f3f4f6;" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 12 8 12;" +
                "-fx-text-fill: #6b7280;"
            );
            txtCV.setPrefHeight(40);
            form.getChildren().add(comboRow("Chức vụ", txtCV));
        } else {
            // THÊM: Manager chỉ được thêm staff → disable option "Quản lý"
            cbChucVu.getSelectionModel().select("Nhân viên");

            // Disable "Quản lý" bằng cell factory
            cbChucVu.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setDisable(false);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("Quản lý".equals(item)) {
                            setDisable(true);
                            setStyle("-fx-opacity: 0.4;");
                        } else {
                            setDisable(false);
                            setStyle("");
                        }
                    }
                }
            });

            form.getChildren().add(comboRow("Chức vụ", cbChucVu));
        }

        /* ── Người quản lý (chỉ hiện khi chức vụ = Nhân viên) ──── */
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

        // Mặc định ẩn, chỉ hiện khi chọn Nhân viên
        updateNguoiQuanLyVisibility();
        form.getChildren().add(nguoiQuanLyBox);

        if (nvEdit == null) {
            cbChucVu.setOnAction(e -> updateNguoiQuanLyVisibility());
        }

        /* ── Load dữ liệu khi sửa ──────────────────────────────── */
        if (nvEdit != null) {
            txtTen.setText(nvEdit.getHoTen());
            txtDiaChi.setText(nvEdit.getDiaChi() != null ? nvEdit.getDiaChi() : "");
            txtSDT.setText(nvEdit.getSoDT());
            txtHeSo.setText(String.valueOf(nvEdit.getHeSoLuong()));

            if (nvEdit.getTrinhDo() != null) cbTrinhDo.getSelectionModel().select(nvEdit.getTrinhDo());

            // Nếu là nhân viên → chọn người quản lý
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

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    /* ════════════════════════════════════════════════════════════════
       FOOTER
    ════════════════════════════════════════════════════════════════ */
    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 32, 20, 32));
        footer.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + C_BORDER + " transparent transparent transparent;" +
            "-fx-border-width: 1 0 0 0;" +
            "-fx-background-radius: 0 0 16 16;"
        );

        Button btnCancel = new Button("Hủy");
        btnCancel.setPrefWidth(100);
        btnCancel.setPrefHeight(40);
        btnCancel.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #374151;" +
            "-fx-cursor: hand;"
        );
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(
            "-fx-background-color: #f3f4f6;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #374151;" +
            "-fx-cursor: hand;"
        ));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #374151;" +
            "-fx-cursor: hand;"
        ));
        btnCancel.setOnAction(e -> close());

        Button btnSave = new Button(nvEdit == null ? "💾  Thêm mới" : "💾  Cập nhật");
        btnSave.setPrefWidth(140);
        btnSave.setPrefHeight(40);
        btnSave.setStyle(
            "-fx-background-color: " + C_SIDEBAR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnSave.setOnMouseEntered(e -> btnSave.setStyle(
            "-fx-background-color: " + C_ACTIVE + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btnSave.setOnMouseExited(e -> btnSave.setStyle(
            "-fx-background-color: " + C_SIDEBAR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        btnSave.setOnAction(e -> handleSave());

        footer.getChildren().addAll(btnCancel, btnSave);
        return footer;
    }

    /* ════════════════════════════════════════════════════════════════
       VALIDATION
    ════════════════════════════════════════════════════════════════ */
    private void setupValidation() {
        txtTen.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateAll(); });
        txtSDT.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateAll(); });
        txtHeSo.focusedProperty().addListener((o, ov, nv) -> { if (!nv) validateAll(); });
    }

    private boolean validateAll() {
        boolean ok = true;

        // Tên
        if (txtTen.getText().trim().isEmpty()) {
            lblErrTen.setText("Họ tên không được để trống");
            highlightError(txtTen, true);
            ok = false;
        } else {
            lblErrTen.setText("");
            highlightError(txtTen, false);
        }

        // SĐT
        if (!txtSDT.getText().matches("0\\d{9}")) {
            lblErrSDT.setText("SĐT gồm 10 số, bắt đầu bằng 0");
            highlightError(txtSDT, true);
            ok = false;
        } else {
            lblErrSDT.setText("");
            highlightError(txtSDT, false);
        }

        // Hệ số
        try {
            float hs = Float.parseFloat(txtHeSo.getText().trim());
            if (hs < 1.0f || hs > 10.0f) throw new NumberFormatException();
            lblErrHeSo.setText("");
            highlightError(txtHeSo, false);
        } catch (NumberFormatException e) {
            lblErrHeSo.setText("Hệ số phải từ 1.0 đến 10.0");
            highlightError(txtHeSo, true);
            ok = false;
        }

        return ok;
    }

    private void highlightError(TextField tf, boolean error) {
        if (error) {
            tf.setStyle(
                "-fx-background-color: #fef2f2;" +
                "-fx-border-color: " + C_ERROR + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 12 8 12;"
            );
        } else {
            tf.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 12 8 12;"
            );
        }
    }

    /* ════════════════════════════════════════════════════════════════
       SAVE
    ════════════════════════════════════════════════════════════════ */
    private void handleSave() {
        if (!validateAll()) return;

        try {
            NhanVien n = (nvEdit != null) ? nvEdit : new NhanVien();

            // ── Xác định chức vụ ────────────────────────────────────
            boolean isManagerSelected;
            if (nvEdit != null) {
                isManagerSelected = nvEdit.getRole() == ChucVu.QUAN_LY;
            } else {
                isManagerSelected = "Quản lý".equals(cbChucVu.getValue());
            }

            // ── Chặn giới hạn quản lý ──────────────────────────────
            if (isManagerSelected) {
                List<NhanVien> all = dao.getAll();
                long countQL = all.stream().filter(x -> x.getRole() == ChucVu.QUAN_LY).count();

                if (nvEdit == null && countQL >= 2) {
                    showError("Chỉ được phép có tối đa 2 quản lý!");
                    return;
                }
                if (nvEdit != null && nvEdit.getRole() != ChucVu.QUAN_LY && countQL >= 2) {
                    showError("Đã đủ 2 quản lý!");
                    return;
                }
            }

            // ── Tự động sinh mã khi thêm mới ───────────────────────
            if (nvEdit == null) {
                List<NhanVien> all = dao.getAll();
                int maxId = 0;
                for (NhanVien item : all) {
                    try {
                        int id = Integer.parseInt(item.getMaNV().replace("LUCIA", ""));
                        if (id > maxId) maxId = id;
                    } catch (NumberFormatException ignored) {}
                }
                n.setMaNV(String.format("LUCIA%04d", maxId + 1));
            }

            // ── Gán dữ liệu ────────────────────────────────────────
            n.setHoTen(txtTen.getText().trim());
            n.setDiaChi(txtDiaChi.getText().trim());
            n.setSoDT(txtSDT.getText().trim());
            n.setHeSoLuong(Float.parseFloat(txtHeSo.getText().trim()));
            n.setRole(isManagerSelected ? ChucVu.QUAN_LY : ChucVu.NHAN_VIEN);
            n.setTrinhDo(cbTrinhDo.getValue());

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
                if (parentView != null) parentView.loadData();
                close();
            } else {
                showError("Thao tác thất bại – kiểm tra lại dữ liệu.");
            }

        } catch (Exception ex) {
            showError("Lỗi: " + ex.getMessage());
        }
    }

    /* ════════════════════════════════════════════════════════════════
       HELPERS
    ════════════════════════════════════════════════════════════════ */
    private void updateNguoiQuanLyVisibility() {
        boolean isStaff;
        if (nvEdit != null) {
            isStaff = nvEdit.getRole() == ChucVu.NHAN_VIEN;
        } else {
            isStaff = "Nhân viên".equals(cbChucVu.getValue());
        }
        nguoiQuanLyBox.setVisible(isStaff);
        nguoiQuanLyBox.setManaged(isStaff);
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

    /* ── Form helpers ─────────────────────────────────────────────── */
    private VBox fieldRow(String labelText, TextField field, Label errLabel) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 2, 0));

        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lbl.setTextFill(Color.web("#374151"));

        box.getChildren().addAll(lbl, field, errLabel);
        return box;
    }

    private VBox comboRow(String labelText, Control field) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 2, 0));

        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        lbl.setTextFill(Color.web("#374151"));

        box.getChildren().addAll(lbl, field);
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
            "-fx-padding: 8 12 8 12;"
        );
        tf.focusedProperty().addListener((obs, o, focused) -> {
            if (focused) {
                tf.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: " + C_ACTIVE + ";" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-font-size: 13px;" +
                    "-fx-padding: 8 12 8 12;" +
                    "-fx-border-width: 2;"
                );
            } else {
                // Will be re-styled by validation
            }
        });
        return tf;
    }

    private Label makeErrLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(C_ERROR));
        l.setMinHeight(14);
        return l;
    }

    private void styleCombo(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(40);
        cb.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 13px;"
        );
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi");
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