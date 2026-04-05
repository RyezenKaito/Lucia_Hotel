package gui;

import dao.KhachHangDAO;
import model.entities.KhachHang;
import model.utils.ValidationUtils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.scene.input.KeyCode; // Thư viện để bắt phím Enter

import java.time.LocalDate;

/**
 * ThemSuaKhachHangDialog – JavaFX
 */
public class ThemSuaKhachHangDialog {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_RED = "#dc2626";

    /* ── State ──────────────────────────────────────────────────────── */
    private final Window owner;
    private final KhachHang khachHang;
    private final KhachHangDAO dao;
    private final Runnable onSuccess;
    private final boolean isEdit;

    /* ── Form controls ──────────────────────────────────────────────── */
    private TextField txtMaKH, txtTen, txtCCCD, txtSDT;
    private DatePicker dpNgaySinh;
    private Label errTen, errNS, errCCCD, errSDT;

    /*
     * ══════════════════════════════════════════════════════════════════
     * CONSTRUCTOR
     * ══════════════════════════════════════════════════════════════════
     */
    public ThemSuaKhachHangDialog(Window owner, KhachHang kh,
            KhachHangDAO dao, Runnable onSuccess) {
        this.owner = owner;
        this.khachHang = kh;
        this.dao = dao;
        this.onSuccess = onSuccess;
        this.isEdit = (kh != null);
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * HIỂN THỊ DIALOG
     * ══════════════════════════════════════════════════════════════════
     */
    public void show() {
        Region overlay = DimOverlay.show(owner);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Cập nhật thông tin khách hàng" : "Thêm khách hàng mới");
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null)
            dialog.initOwner(owner);
        dialog.setResizable(false);

        dialog.getDialogPane().setContent(buildContent());
        dialog.getDialogPane().setStyle("-fx-padding: 0;");

        ButtonType btnSubmit = new ButtonType(
                isEdit ? "💾 Cập nhật" : "Thêm khách hàng",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, btnCancel);

        styleDialogButtons(dialog, btnSubmit, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSubmit())
                event.consume();
        });

        // BẮT SỰ KIỆN THAY ĐỔI ĐỂ ENABLE NÚT CẬP NHẬT (CHỈ ÁP DỤNG KHI SỬA)
        if (isEdit) {
            okBtn.setDisable(true);
            javafx.beans.value.ChangeListener<Object> changeListener = (obs, oldVal, newVal) -> {
                boolean changed = false;
                if (!java.util.Objects.equals(txtTen.getText().trim(), nvl(khachHang.getTenKH()).trim()))
                    changed = true;
                else if (!java.util.Objects.equals(txtCCCD.getText().trim(), nvl(khachHang.getSoCCCD()).trim()))
                    changed = true;
                else if (!java.util.Objects.equals(txtSDT.getText().trim(), nvl(khachHang.getSoDT()).trim()))
                    changed = true;
                else if (!java.util.Objects.equals(dpNgaySinh.getValue(), khachHang.getNgaySinh()))
                    changed = true;

                okBtn.setDisable(!changed);
            };
            txtTen.textProperty().addListener(changeListener);
            txtCCCD.textProperty().addListener(changeListener);
            txtSDT.textProperty().addListener(changeListener);
            dpNgaySinh.valueProperty().addListener(changeListener);
        }

        // BẮT SỰ KIỆN PHÍM ENTER ĐỂ KÍCH HOẠT NÚT SUBMIT
        dialog.getDialogPane().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                okBtn.fire(); // Giả lập hành động click chuột vào nút
                e.consume();
            }
        });

        dialog.showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * XÂY DỰNG NỘI DUNG FORM
     * ══════════════════════════════════════════════════════════════════
     */
    private VBox buildContent() {
        VBox content = new VBox(0);
        content.setPrefWidth(540);

        /* ── Header ───────────────────────────────────────────────── */
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(24, 28, 18, 28));
        headerBox.setStyle("-fx-background-color: " + C_NAVY + ";");

        Label dlgTitle = new Label(isEdit
                ? "Cập nhật thông tin khách hàng"
                : "Thêm khách hàng mới");
        dlgTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        dlgTitle.setTextFill(Color.WHITE);

        Label dlgSub = new Label(isEdit
                ? "Mã khách hàng: " + khachHang.getMaKH()
                : "Vui lòng điền đầy đủ thông tin bên dưới");
        dlgSub.setFont(Font.font("Segoe UI", 13));
        dlgSub.setTextFill(Color.web("#93c5fd"));

        Region sepLine = new Region();
        sepLine.setPrefHeight(2);
        sepLine.setStyle("-fx-background-color: #3b82f6;");

        headerBox.getChildren().addAll(dlgTitle, dlgSub, sepLine);

        /* ── Form fields ──────────────────────────────────────────── */
        VBox formBox = new VBox(16);
        formBox.setPadding(new Insets(22, 28, 10, 28));
        formBox.setStyle("-fx-background-color: " + C_BG + ";");

        txtMaKH = new TextField(isEdit ? khachHang.getMaKH() : dao.getNextMaKH());
        txtMaKH.setEditable(false);
        txtMaKH.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280;");

        txtTen = makeField(isEdit ? nvl(khachHang.getTenKH()) : "", "Nhập họ và tên");
        errTen = errLabel();

        dpNgaySinh = new DatePicker();
        dpNgaySinh.setPromptText("Chọn ngày sinh");
        dpNgaySinh.setPrefHeight(42);
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinh.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;" +
                        "-fx-background-radius: 8; -fx-border-radius: 8;" +
                        "-fx-border-color: " + C_BORDER + ";");
        if (isEdit && khachHang.getNgaySinh() != null)
            dpNgaySinh.setValue(khachHang.getNgaySinh());
        errNS = errLabel();

        txtCCCD = makeField(isEdit ? nvl(khachHang.getSoCCCD()) : "", "Nhập CCCD");
        ValidationUtils.applyNumericOnlyFilter(txtCCCD, 12);
        errCCCD = errLabel();

        txtSDT = makeField(isEdit ? nvl(khachHang.getSoDT()) : "", "Nhập số điện thoại");
        ValidationUtils.applyNumericOnlyFilter(txtSDT, 11);
        errSDT = errLabel();

        formBox.getChildren().addAll(
                fieldBlock("Mã khách hàng", txtMaKH, null,
                        isEdit ? "Không thể chỉnh sửa" : "Tự động tạo mã"),
                fieldBlockWithErr("Họ và tên *", txtTen, errTen, null),
                fieldBlockWithErr("Ngày sinh *", dpNgaySinh, errNS, "Phải đủ 16 tuổi trở lên"),
                fieldBlockWithErr("Số CCCD *", txtCCCD, errCCCD, "Mã số định danh 12 số"),
                fieldBlockWithErr("Số điện thoại *", txtSDT, errSDT, "Gồm 10 số, theo chuẩn nhà mạng"));

        content.getChildren().addAll(headerBox, formBox);
        setupValidation();
        return content;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * VALIDATE & SUBMIT
     * ══════════════════════════════════════════════════════════════════
     */
    private boolean validateTen() {
        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        if (ten.isEmpty()) {
            showErrorField(txtTen, errTen, "⚠ Không được để trống.");
            return false;
        }
        if (!ten.matches(ValidationUtils.REGEX_NAME)) {
            showErrorField(txtTen, errTen, "⚠ Chỉ được chứa chữ cái và khoảng trắng.");
            return false;
        }
        if (ten.matches(ValidationUtils.REGEX_SPAM_CHAR)) {
            showErrorField(txtTen, errTen, "⚠ Tên có chứa ký tự lặp lại bất thường.");
            return false;
        }
        if (!ValidationUtils.isValidNameLength(ten)) {
            showErrorField(txtTen, errTen, "⚠ Họ phải chứa ít 1 ký tự, Tên chứa ít nhất 2 ký tự.");
            return false;
        }
        clearErrorField(txtTen, errTen);
        return true;
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

    private boolean validateSDT() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            showErrorField(txtSDT, errSDT, "⚠ Không được để trống.");
            return false;
        }
        if (!sdt.matches(ValidationUtils.REGEX_PHONE_VN)) {
            showErrorField(txtSDT, errSDT, "⚠ Sai đầu số nhà mạng Việt Nam (03x, 05x, 07x, 08x, 09x).");
            return false;
        }
        clearErrorField(txtSDT, errSDT);
        return true;
    }

    private boolean validateCCCD() {
        String cccd = txtCCCD.getText().trim();
        LocalDate ns = dpNgaySinh.getValue();
        if (cccd.isEmpty()) {
            showErrorField(txtCCCD, errCCCD, "⚠ Không được để trống.");
            return false;
        }
        if (!cccd.matches(ValidationUtils.REGEX_CCCD_FORMAT)) {
            showErrorField(txtCCCD, errCCCD, "⚠ Phải gồm đúng 12 chữ số.");
            return false;
        }
        if (!ValidationUtils.isValidProvinceCode(cccd)) {
            showErrorField(txtCCCD, errCCCD, "⚠ Mã tỉnh/thành phố không hợp lệ.");
            return false;
        }
        if (ns != null) {
            int namSinh = ns.getYear();
            if (!ValidationUtils.isValidCCCDCenturyAndGender(cccd, namSinh)) {
                showErrorField(txtCCCD, errCCCD,
                        "⚠ số thứ 4 không khớp (dưới năm 2000 0:nam 1:nữ, từ năm 2000 2:nam, 3:nữ ).");
                return false;
            }
            if (!ValidationUtils.isValidCCCDBirthYear(cccd, namSinh)) {
                showErrorField(txtCCCD, errCCCD, "⚠ 2 số năm sinh trên CCCD bị sai (số 5,6).");
                return false;
            }
        }
        clearErrorField(txtCCCD, errCCCD);
        return true;
    }

    private void setupValidation() {
        txtTen.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateTen();
            else
                clearErrorField(txtTen, errTen);
        });
        txtSDT.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateSDT();
            else
                clearErrorField(txtSDT, errSDT);
        });
        txtCCCD.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateCCCD();
            else
                clearErrorField(txtCCCD, errCCCD);
        });
        dpNgaySinh.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv)
                validateNS();
            else
                clearErrorField(dpNgaySinh, errNS);
        });
    }

    private boolean validateAndSubmit() {
        boolean ok = true;
        if (!validateTen())
            ok = false;
        if (!validateNS())
            ok = false;
        if (!validateSDT())
            ok = false;
        if (!validateCCCD())
            ok = false;
        if (!ok)
            return false;

        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        String cccd = txtCCCD.getText().trim();
        String sdt = txtSDT.getText().trim();
        LocalDate ns = dpNgaySinh.getValue();

        // Nếu mọi thứ hợp lệ -> Chuẩn hóa tên viết hoa chữ cái đầu trước khi lưu vào DB
        String tenChuanHoa = ValidationUtils.toTitleCase(ten);
        String maKH = txtMaKH.getText().trim();

        if (isEdit) {
            khachHang.setTenKH(toTitleCase(ten));
            khachHang.setSoCCCD(cccd);
            khachHang.setSoDT(sdt);
            khachHang.setNgaySinh(ns);
            if (dao.update(khachHang)) {
                showInfo("Cập nhật thành công!", "Thông tin khách hàng " + maKH + " đã được cập nhật.");
                if (onSuccess != null)
                    onSuccess.run();
                return true;
            }
            showError("Cập nhật thất bại", "Kiểm tra lại dữ liệu hoặc kết nối CSDL.");
            return false;
        } else {
            KhachHang newKH = new KhachHang(maKH, toTitleCase(ten), cccd, sdt, ns);
            if (dao.insert(newKH)) {
                showInfo("Thêm thành công!", "Khách hàng " + maKH + " đã được thêm vào hệ thống.");
                if (onSuccess != null)
                    onSuccess.run();
                return true;
            }
            showError("Thêm thất bại", "Kiểm tra lại dữ liệu hoặc kết nối CSDL.");
            return false;
        }
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * FACTORY / UTILITY
     * ══════════════════════════════════════════════════════════════════
     */
    private VBox fieldBlock(String label, Control field, Label errLbl, String hint) {
        VBox b = new VBox(4);
        HBox lblBox = new HBox(4);
        if (label.endsWith("*")) {
            Label lblText = new Label(label.substring(0, label.length() - 1).trim());
            lblText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblText.setTextFill(Color.web(C_TEXT_DARK));
            Label lblStar = new Label("*");
            lblStar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblStar.setTextFill(Color.web(C_RED));
            lblBox.getChildren().addAll(lblText, lblStar);
        } else {
            Label lblText = new Label(label);
            lblText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblText.setTextFill(Color.web(C_TEXT_DARK));
            lblBox.getChildren().add(lblText);
        }
        b.getChildren().add(lblBox);
        if (hint != null) {
            Label h = new Label("   " + hint);
            h.setFont(Font.font("Segoe UI", 11));
            h.setTextFill(Color.web(C_TEXT_GRAY));
            b.getChildren().add(h);
        }
        field.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().add(field);
        if (errLbl != null) {
            errLbl.setMaxWidth(Double.MAX_VALUE);
            b.getChildren().add(errLbl);
        }
        return b;
    }

    private VBox fieldBlockWithErr(String label, Control field, Label errLbl, String hint) {
        return fieldBlock(label, field, errLbl, hint);
    }

    private String fieldStyle() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 42;" +
                "-fx-background-radius: 8; -fx-border-radius: 8;" +
                "-fx-border-color: " + C_BORDER + "; -fx-padding: 0 14;";
    }

    private Label errLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(C_RED));
        l.setWrapText(true);
        return l;
    }

    private TextField makeField(String value, String prompt) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private void showErrorField(Control tf, Label errLabel, String msg) {
        if (errLabel != null)
            errLabel.setText(msg);
        tf.setStyle(fieldStyle() + "-fx-border-color: " + C_RED + "; -fx-background-color: #fef2f2;");
    }

    private void clearErrorField(Control tf, Label errLabel) {
        if (errLabel != null)
            errLabel.setText("");
        tf.setStyle(fieldStyle());
    }

    private void styleDialogButtons(Dialog<?> dialog, ButtonType submit, ButtonType cancel) {
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(submit);
        okBtn.setStyle(
                "-fx-background-color: " + C_BLUE + "; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
        okBtn.setOnMouseEntered(e -> okBtn.setStyle(okBtn.getStyle().replace(C_BLUE, C_BLUE_HOVER)));
        okBtn.setOnMouseExited(e -> okBtn.setStyle(okBtn.getStyle().replace(C_BLUE_HOVER, C_BLUE)));

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancel);
        cancelBtn.setStyle(
                "-fx-background-color: #e5e7eb;" +
                        "-fx-text-fill: " + C_TEXT_DARK + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 24; -fx-cursor: hand;");
    }

    private void showInfo(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty())
            return input;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char ch : input.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                nextUpper = true;
                sb.append(ch);
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(ch));
                nextUpper = false;
            } else {
                sb.append(Character.toLowerCase(ch));
            }
        }
        return sb.toString();
    }
}