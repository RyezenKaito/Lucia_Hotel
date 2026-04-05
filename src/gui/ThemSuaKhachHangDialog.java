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
                isEdit ? "Lưu thay đổi" : "Thêm khách hàng",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, btnCancel);

        styleDialogButtons(dialog, btnSubmit, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSubmit())
                event.consume();
        });

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

        txtTen = new TextField(isEdit ? nvl(khachHang.getTenKH()) : "");
        txtTen.setPromptText("Nhập họ và tên");
        txtTen.setStyle(fieldStyle());
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

        txtCCCD = new TextField(isEdit ? nvl(khachHang.getSoCCCD()) : "");
        txtCCCD.setPromptText("Nhập số căn cước công dân");
        txtCCCD.setStyle(fieldStyle());
        ValidationUtils.applyNumericOnlyFilter(txtCCCD, 12);
        errCCCD = errLabel();

        txtSDT = new TextField(isEdit ? nvl(khachHang.getSoDT()) : "");
        txtSDT.setPromptText("Nhập số điện thoại");
        txtSDT.setStyle(fieldStyle());
        ValidationUtils.applyNumericOnlyFilter(txtSDT, 11);
        errSDT = errLabel();

        formBox.getChildren().addAll(
                fieldBlock("Mã khách hàng", txtMaKH, null,
                        isEdit ? "Không thể chỉnh sửa" : "Tự động tạo mã"),
                fieldBlockWithErr("Họ và tên *", txtTen, errTen, null),
                fieldBlockWithErr("Ngày sinh *", dpNgaySinh, errNS, "Phải đủ 16 tuổi trở lên"),
                fieldBlockWithErr("Số căn cước công dân *", txtCCCD, errCCCD, "Mã số định danh 12 số"),
                fieldBlockWithErr("Số điện thoại *", txtSDT, errSDT, "Gồm 10 số, theo chuẩn nhà mạng"));

        content.getChildren().addAll(headerBox, formBox);
        return content;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * VALIDATE & SUBMIT
     * ══════════════════════════════════════════════════════════════════
     */
    private boolean validateAndSubmit() {
        clearErrors();
        boolean ok = true;

        String ten = txtTen.getText().trim().replaceAll("\\s+", " ");
        String cccd = txtCCCD.getText().trim();
        String sdt = txtSDT.getText().trim();
        LocalDate ns = dpNgaySinh.getValue();

        // 1. KIỂM TRA HỌ VÀ TÊN
        if (ten.isEmpty()) {
            errTen.setText("⚠ Không được để trống.");
            ok = false;
        } else if (!ten.matches(ValidationUtils.REGEX_NAME)) {
            errTen.setText("⚠ Chỉ được chứa chữ cái và khoảng trắng.");
            ok = false;
        } else if (ten.matches(ValidationUtils.REGEX_SPAM_CHAR)) {
            errTen.setText("⚠ Tên có chứa ký tự lặp lại bất thường.");
            ok = false;
        } else if (!ValidationUtils.isValidNameLength(ten)) {
            errTen.setText("⚠ Họ phải ≥ 1 ký tự, Tên ≥ 2 ký tự.");
            ok = false;
        }

        // 2. KIỂM TRA NGÀY SINH
        if (ns == null) {
            errNS.setText("⚠ Vui lòng chọn ngày sinh.");
            ok = false;
        } else if (LocalDate.now().minusYears(16).isBefore(ns)) {
            errNS.setText("⚠ Khách hàng phải ≥ 16 tuổi.");
            ok = false;
        }

        // 3. KIỂM TRA SỐ ĐIỆN THOẠI
        if (sdt.isEmpty()) {
            errSDT.setText("⚠ Không được để trống.");
            ok = false;
        } else if (!sdt.matches(ValidationUtils.REGEX_PHONE_VN)) {
            errSDT.setText("⚠ Sai đầu số nhà mạng Việt Nam.");
            ok = false;
        }

        // 4. KIỂM TRA CCCD
        if (cccd.isEmpty()) {
            errCCCD.setText("⚠ Không được để trống.");
            ok = false;
        } else if (!cccd.matches(ValidationUtils.REGEX_CCCD_FORMAT)) {
            errCCCD.setText("⚠ Phải gồm đúng 12 chữ số.");
            ok = false;
        } else if (ValidationUtils.isValidProvinceCode(cccd)) {
            errCCCD.setText("⚠ Mã tỉnh/thành phố không hợp lệ (từ 001-096).");
            ok = false;
        } else if (ns != null) {
            int namSinh = ns.getYear();

            // Gọi HÀM KIỂM TRA CHÉO THẾ KỶ & GIỚI TÍNH
            if (!ValidationUtils.isValidCCCDCenturyAndGender(cccd, namSinh)) {
                errCCCD.setText("⚠ Mã thế kỷ không khớp với năm sinh " + namSinh + ".");
                ok = false;
            }
            // Gọi HÀM KIỂM TRA 2 SỐ NĂM SINH
            else if (!ValidationUtils.isValidCCCDBirthYear(cccd, namSinh)) {
                errCCCD.setText("⚠ 2 số năm sinh trên CCCD bị sai.");
                ok = false;
            }

        }

        if (!ok)
            return false;

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
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(C_TEXT_DARK));
        b.getChildren().add(lbl);
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

    private void clearErrors() {
        for (Label l : new Label[] { errTen, errNS, errCCCD, errSDT })
            l.setText("");
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