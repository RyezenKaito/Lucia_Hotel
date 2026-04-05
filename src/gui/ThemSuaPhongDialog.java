package gui;

import dao.LoaiPhongDAO;
import dao.PhongDAO;
import model.entities.LoaiPhong;
import model.entities.Phong;
import model.enums.TrangThaiPhong;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.scene.input.KeyCode;
import javafx.collections.FXCollections;

import java.util.List;

/**
 * ThemSuaPhongDialog – JavaFX
 */
public class ThemSuaPhongDialog {

    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_RED = "#dc2626";

    private final Window owner;
    private final Phong phong;
    private final PhongDAO dao;
    private final Runnable onSuccess;
    private final boolean isEdit;

    private TextField txtMaPhong;
    private ComboBox<Integer> cbSoTang;
    private TextField txtSoTangEdit; // Dùng khi isEdit = true
    private ComboBox<String> cbLoaiPhong;
    private TextField txtSucChua;
    private ComboBox<String> cbTrangThai;

    private final LoaiPhongDAO lpDAO = new LoaiPhongDAO();
    private List<LoaiPhong> listLoaiPhong;

    public ThemSuaPhongDialog(Window owner, Phong phong, PhongDAO dao, Runnable onSuccess) {
        this.owner = owner;
        this.phong = phong;
        this.dao = dao;
        this.onSuccess = onSuccess;
        this.isEdit = (phong != null);
    }

    public void show() {
        Region overlay = DimOverlay.show(owner);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Cập nhật phòng" : "Thêm phòng mới");
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) dialog.initOwner(owner);
        dialog.setResizable(false);

        dialog.getDialogPane().setContent(buildContent());
        dialog.getDialogPane().setStyle("-fx-padding: 0;");

        ButtonType btnSubmit = new ButtonType(isEdit ? "💾 Cập nhật" : "Thêm phòng", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, btnCancel);

        styleDialogButtons(dialog, btnSubmit, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSubmit()) event.consume();
        });

        if (isEdit) {
            okBtn.setDisable(true);
            javafx.beans.value.ChangeListener<Object> changeListener = (obs, oldVal, newVal) -> {
                boolean changed = false;
                if (!java.util.Objects.equals(cbLoaiPhong.getValue(), phong.getLoaiPhong() != null ? phong.getLoaiPhong().getMaLoaiPhong() : "")) changed = true;
                
                String currentTT = "";
                if (phong.getTrangThai() == TrangThaiPhong.CONTRONG) currentTT = "Còn trống";
                else if (phong.getTrangThai() == TrangThaiPhong.DACOKHACH) currentTT = "Đã có khách";
                else if (phong.getTrangThai() == TrangThaiPhong.BAN) currentTT = "Đang bảo trì";

                if (!java.util.Objects.equals(cbTrangThai.getValue(), currentTT)) changed = true;

                okBtn.setDisable(!changed);
            };
            cbLoaiPhong.valueProperty().addListener(changeListener);
            cbTrangThai.valueProperty().addListener(changeListener);
        }

        dialog.getDialogPane().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                okBtn.fire();
                e.consume();
            }
        });

        dialog.showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    private VBox buildContent() {
        VBox content = new VBox(0);
        content.setPrefWidth(500);

        /* ── Header ───────────────────────────────────────────────── */
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(24, 28, 18, 28));
        headerBox.setStyle("-fx-background-color: " + C_NAVY + ";");

        Label dlgTitle = new Label(isEdit ? "Cập nhật thông tin phòng" : "Thêm phòng mới");
        dlgTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        dlgTitle.setTextFill(Color.WHITE);

        Label dlgSub = new Label(isEdit ? "Mã phòng: " + phong.getMaPhong() : "Vui lòng điền đầy đủ thông tin bên dưới");
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

        // Load Dữ liệu Loại Phòng
        listLoaiPhong = lpDAO.getAll();
        List<String> listTenLoai = java.util.Arrays.asList(lpDAO.fetchAllRoomTypeNames());

        txtMaPhong = new TextField(isEdit ? phong.getMaPhong() : "");
        txtMaPhong.setEditable(false);
        txtMaPhong.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; -fx-font-weight: bold;");

        // Số tầng
        Control tangControl;
        if (isEdit) {
            txtSoTangEdit = new TextField(String.valueOf(phong.getSoTang()));
            txtSoTangEdit.setEditable(false);
            txtSoTangEdit.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280;");
            tangControl = txtSoTangEdit;
        } else {
            cbSoTang = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
            cbSoTang.setValue(1);
            cbSoTang.setMaxWidth(Double.MAX_VALUE);
            cbSoTang.setStyle(fieldStyle());
            cbSoTang.setOnAction(e -> generateMaPhong());
            tangControl = cbSoTang;
        }

        // Loại Phòng
        cbLoaiPhong = new ComboBox<>(FXCollections.observableArrayList(listTenLoai));
        cbLoaiPhong.setMaxWidth(Double.MAX_VALUE);
        cbLoaiPhong.setStyle(fieldStyle());
        if (isEdit && phong.getLoaiPhong() != null) {
            cbLoaiPhong.setValue(phong.getLoaiPhong().getMaLoaiPhong());
        } else if (!listTenLoai.isEmpty()) {
            cbLoaiPhong.setValue(listTenLoai.get(0));
        }
        
        txtSucChua = new TextField("");
        txtSucChua.setEditable(false);
        txtSucChua.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #374151;");
        
        cbLoaiPhong.setOnAction(e -> updateSucChua());
        updateSucChua(); // Cập nhật lần đầu

        // Trạng Thái
        cbTrangThai = new ComboBox<>(FXCollections.observableArrayList("Còn trống", "Đã có khách", "Đang bảo trì"));
        cbTrangThai.setMaxWidth(Double.MAX_VALUE);
        cbTrangThai.setStyle(fieldStyle());
        if (isEdit) {
            if (phong.getTrangThai() == TrangThaiPhong.CONTRONG) cbTrangThai.setValue("Còn trống");
            else if (phong.getTrangThai() == TrangThaiPhong.DACOKHACH) cbTrangThai.setValue("Đã có khách");
            else cbTrangThai.setValue("Đang bảo trì");
        } else {
            cbTrangThai.setValue("Còn trống");
        }

        if (!isEdit) generateMaPhong();

        formBox.getChildren().addAll(
                fieldBlock("Số tầng *", tangControl, isEdit ? "Không thể thay đổi tầng khi cập nhật" : "Chọn tầng từ 1 đến 10"),
                fieldBlock("Mã phòng", txtMaPhong, "Tự động phát sinh theo tầng"),
                fieldBlock("Loại phòng *", cbLoaiPhong, null),
                fieldBlock("Sức chứa", txtSucChua, "Tự động lấy theo loại phòng từ DB"),
                fieldBlock("Trạng thái *", cbTrangThai, null)
        );

        content.getChildren().addAll(headerBox, formBox);
        return content;
    }

    private void updateSucChua() {
        String loaiDuocChon = cbLoaiPhong.getValue();
        if (loaiDuocChon != null && listLoaiPhong != null) {
            for (LoaiPhong lp : listLoaiPhong) {
                if (lp.getMaLoaiPhong().equals(loaiDuocChon)) {
                    txtSucChua.setText(lp.getSucChua() + " người");
                    break;
                }
            }
        }
    }

    private void generateMaPhong() {
        if (isEdit) return;
        Integer floor = cbSoTang.getValue();
        if (floor != null) {
            String maMoi = Phong.phatSinhMaPhong(floor, dao.getAll());
            if ("FULL".equals(maMoi)) {
                txtMaPhong.setText("Tầng " + floor + " đã tối đa số phòng");
                txtMaPhong.setStyle(fieldStyle() + "-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            } else {
                txtMaPhong.setText(maMoi);
                txtMaPhong.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; -fx-font-weight: bold;");
            }
        }
    }

    private boolean validateAndSubmit() {
        String ma = txtMaPhong.getText().trim();
        if (ma.isEmpty() || ma.contains("tối đa")) {
            showError("Lỗi", "Mã phòng không hợp lệ. Vui lòng chọn tầng khác!");
            return false;
        }

        int tang = isEdit ? phong.getSoTang() : cbSoTang.getValue();
        String loai = cbLoaiPhong.getValue();
        String ttStr = cbTrangThai.getValue();
        TrangThaiPhong tt = ttStr.equals("Còn trống") ? TrangThaiPhong.CONTRONG :
                            (ttStr.equals("Đã có khách") ? TrangThaiPhong.DACOKHACH : TrangThaiPhong.BAN);

        int kichThuoc = 0; // Kích thước này chỉ lưu số thứ tự phòng trong tầng
        if (!isEdit) {
            try { kichThuoc = Integer.parseInt(ma.substring(2)); } catch (Exception ignored) {}
        } else {
            kichThuoc = phong.getSoPhong();
        }

        Phong p = new Phong(ma, ma, new LoaiPhong(loai), tt, kichThuoc, tang);

        if (isEdit) {
            if (dao.update(p)) {
                showInfo("Cập nhật thành công!", "Phòng " + ma + " đã được cập nhật.");
                if (onSuccess != null) onSuccess.run();
                return true;
            }
            showError("Cập nhật thất bại", "Kiểm tra lại dữ liệu hoặc kết nối CSDL.");
            return false;
        } else {
            if (dao.insert(p)) {
                showInfo("Thêm thành công!", "Phòng " + ma + " đã được thêm vào hệ thống.");
                if (onSuccess != null) onSuccess.run();
                return true;
            }
            showError("Thêm thất bại", "Có thể trùng lặp mã phòng trong CSDL!");
            return false;
        }
    }

    private VBox fieldBlock(String label, Control field, String hint) {
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
        return b;
    }

    private String fieldStyle() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 42;" +
               "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER + ";";
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
                "-fx-background-color: #e5e7eb; -fx-text-fill: " + C_TEXT_DARK + ";" +
                        "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
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
}
