package gui;

import dao.DichVuDAO;
import model.entities.DichVu;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.scene.input.KeyCode;

/**
 * ThemSuaDichVuDialog – JavaFX
 * Thay thế ThemDichVuDialog (Swing).
 */
public class ThemSuaDichVuDialog {

    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_RED = "#dc2626";

    private final Window owner;
    private final DichVu dichVu;   // null = thêm mới
    private final Runnable onSuccess;
    private final boolean isEdit;

    private TextField txtMaDV, txtTenDV, txtGia, txtDonVi;
    private ComboBox<String> cbLoai;

    public ThemSuaDichVuDialog(Window owner, DichVu dichVu, Runnable onSuccess) {
        this.owner = owner;
        this.dichVu = dichVu;
        this.onSuccess = onSuccess;
        this.isEdit = (dichVu != null);
    }

    public void show() {
        Region overlay = DimOverlay.show(owner);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Cập nhật dịch vụ" : "Thêm dịch vụ mới");
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) dialog.initOwner(owner);
        dialog.setResizable(false);

        dialog.getDialogPane().setContent(buildContent());
        dialog.getDialogPane().setStyle("-fx-padding: 0;");

        ButtonType btnSubmit = new ButtonType(isEdit ? "💾 Cập nhật" : "Thêm dịch vụ", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, btnCancel);
        styleDialogButtons(dialog, btnSubmit, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSubmit()) event.consume();
        });

        dialog.getDialogPane().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { okBtn.fire(); e.consume(); }
        });

        dialog.showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    private VBox buildContent() {
        VBox content = new VBox(0);
        content.setPrefWidth(480);

        // Header
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(24, 28, 18, 28));
        headerBox.setStyle("-fx-background-color: " + C_NAVY + ";");

        Label dlgTitle = new Label(isEdit ? "Cập nhật thông tin dịch vụ" : "Thêm dịch vụ mới");
        dlgTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        dlgTitle.setTextFill(Color.WHITE);

        Label dlgSub = new Label(isEdit ? "Mã DV: " + dichVu.getMaDV() : "Vui lòng điền đầy đủ thông tin");
        dlgSub.setFont(Font.font("Segoe UI", 13));
        dlgSub.setTextFill(Color.web("#93c5fd"));

        Region sep = new Region(); sep.setPrefHeight(2);
        sep.setStyle("-fx-background-color: #3b82f6;");
        headerBox.getChildren().addAll(dlgTitle, dlgSub, sep);

        // Form
        VBox formBox = new VBox(16);
        formBox.setPadding(new Insets(22, 28, 10, 28));
        formBox.setStyle("-fx-background-color: " + C_BG + ";");

        // Mã dịch vụ
        txtMaDV = new TextField(isEdit ? dichVu.getMaDV() : "");
        txtMaDV.setPromptText("Ví dụ: DV001");
        if (isEdit) {
            txtMaDV.setEditable(false);
            txtMaDV.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280;");
        } else {
            txtMaDV.setStyle(fieldStyle());
        }

        // Tên dịch vụ
        txtTenDV = new TextField(isEdit ? nvl(dichVu.getTenDV()) : "");
        txtTenDV.setPromptText("Nhập tên dịch vụ");
        txtTenDV.setStyle(fieldStyle());

        // Loại dịch vụ
        cbLoai = new ComboBox<>(FXCollections.observableArrayList("Ẩm thực", "Giải trí", "Sức khỏe", "Tiện ích"));
        cbLoai.setMaxWidth(Double.MAX_VALUE);
        cbLoai.setStyle(fieldStyle());
        if (isEdit && dichVu.getLoaiDV() != null) cbLoai.setValue(dichVu.getLoaiDV());
        else cbLoai.setValue("Ẩm thực");

        // Đơn giá
        txtGia = new TextField(isEdit ? String.valueOf((long) dichVu.getGia()) : "");
        txtGia.setPromptText("Nhập đơn giá (VNĐ)");
        txtGia.setStyle(fieldStyle());

        // Đơn vị tính
        txtDonVi = new TextField(isEdit ? nvl(dichVu.getDonVi()) : "");
        txtDonVi.setPromptText("Ví dụ: Cái, Lần, Chai...");
        txtDonVi.setStyle(fieldStyle());

        formBox.getChildren().addAll(
                fieldBlock("Mã dịch vụ *", txtMaDV, isEdit ? "Không thể chỉnh sửa mã" : "Nhập mã duy nhất trong hệ thống"),
                fieldBlock("Tên dịch vụ *", txtTenDV, null),
                fieldBlock("Loại dịch vụ *", cbLoai, null),
                fieldBlock("Đơn giá (VNĐ) *", txtGia, "Chỉ nhập số, không dấu phẩy"),
                fieldBlock("Đơn vị tính", txtDonVi, null)
        );

        content.getChildren().addAll(headerBox, formBox);
        return content;
    }

    private boolean validateAndSubmit() {
        if (txtMaDV.getText().trim().isEmpty()) { showError("Lỗi", "Mã dịch vụ không được để trống!"); return false; }
        if (txtTenDV.getText().trim().isEmpty()) { showError("Lỗi", "Tên dịch vụ không được để trống!"); return false; }
        double gia;
        try {
            gia = Double.parseDouble(txtGia.getText().trim().replace(",", ""));
            if (gia < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Lỗi", "Đơn giá phải là số không âm!"); return false;
        }

        DichVu dv = new DichVu();
        dv.setMaDV(txtMaDV.getText().trim());
        dv.setTenDV(txtTenDV.getText().trim());
        dv.setLoaiDV(cbLoai.getValue());
        dv.setGia(gia);
        dv.setDonVi(txtDonVi.getText().trim());

        DichVuDAO dvDAO = new DichVuDAO();
        boolean ok;
        if (isEdit) {
            ok = dvDAO.update(dv);
            if (ok) showInfo("Thành công!", "Dịch vụ " + dv.getMaDV() + " đã được cập nhật.");
            else { showError("Thất bại", "Không thể cập nhật. Kiểm tra kết nối CSDL."); return false; }
        } else {
            ok = dvDAO.insert(dv);
            if (ok) showInfo("Thành công!", "Đã thêm dịch vụ " + dv.getMaDV() + " vào hệ thống.");
            else { showError("Thất bại", "Có thể trùng mã dịch vụ trong CSDL!"); return false; }
        }
        if (onSuccess != null) onSuccess.run();
        return true;
    }

    // Helpers
    private VBox fieldBlock(String label, javafx.scene.control.Control field, String hint) {
        VBox b = new VBox(4);
        HBox lblBox = new HBox(4);
        if (label.endsWith("*")) {
            Label t = new Label(label.substring(0, label.length() - 1).trim());
            t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13)); t.setTextFill(Color.web(C_TEXT_DARK));
            Label star = new Label("*"); star.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13)); star.setTextFill(Color.web(C_RED));
            lblBox.getChildren().addAll(t, star);
        } else {
            Label t = new Label(label); t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13)); t.setTextFill(Color.web(C_TEXT_DARK));
            lblBox.getChildren().add(t);
        }
        b.getChildren().add(lblBox);
        if (hint != null) {
            Label h = new Label("   " + hint); h.setFont(Font.font("Segoe UI", 11)); h.setTextFill(Color.web(C_TEXT_GRAY));
            b.getChildren().add(h);
        }
        field.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().add(field);
        return b;
    }

    private String fieldStyle() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 42;" +
               "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER + ";";
    }

    private void styleDialogButtons(Dialog<?> dialog, ButtonType submit, ButtonType cancel) {
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(submit);
        okBtn.setStyle("-fx-background-color: " + C_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
        okBtn.setOnMouseEntered(e -> okBtn.setStyle(okBtn.getStyle().replace(C_BLUE, C_BLUE_HOVER)));
        okBtn.setOnMouseExited(e -> okBtn.setStyle(okBtn.getStyle().replace(C_BLUE_HOVER, C_BLUE)));

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancel);
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: " + C_TEXT_DARK + ";" +
                "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
    }

    private void showInfo(String h, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Thông báo"); a.setHeaderText(h); a.setContentText(m); a.showAndWait(); }
    private void showError(String h, String m) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Lỗi"); a.setHeaderText(h); a.setContentText(m); a.showAndWait(); }
    private static String nvl(String s) { return s != null ? s : ""; }
}
