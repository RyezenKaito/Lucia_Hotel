package gui;

import dao.BangGiaDichVuDAO;
import dao.DichVuDAO;
import model.entities.BangGiaDichVu;
import model.entities.BangGiaDichVu_ChiTiet;
import model.entities.DichVu;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ThemSuaBangGiaDialog – JavaFX
 * Thay thế ThemBangGiaDialog và SuaBangGiaDialog (Swing).
 */
public class ThemSuaBangGiaDialog {

    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_RED = "#dc2626";
    private static final String C_GREEN = "#16a34a";

    private final Window owner;
    private final BangGiaDichVu bangGia;                        // null = thêm mới
    private final List<BangGiaDichVu_ChiTiet> dsChiTietGoc;
    private final Runnable onSuccess;
    private final boolean isEdit;

    // Controls
    private TextField txtMaBG, txtTenBG;
    private DatePicker dpNgayAD, dpNgayHH;
    private TableView<Object[]> table;         // [maDV, tenDV, loai, giaGoc, giaApDung]
    private ObservableList<Object[]> tableData;

    public ThemSuaBangGiaDialog(Window owner, BangGiaDichVu bangGia,
                                 List<BangGiaDichVu_ChiTiet> dsChiTiet, Runnable onSuccess) {
        this.owner = owner;
        this.bangGia = bangGia;
        this.dsChiTietGoc = dsChiTiet;
        this.onSuccess = onSuccess;
        this.isEdit = (bangGia != null);
    }

    public void show() {
        Region overlay = DimOverlay.show(owner);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Chỉnh sửa bảng giá" : "Tạo bảng giá mới");
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) dialog.initOwner(owner);
        dialog.setResizable(true);

        dialog.getDialogPane().setContent(buildContent());
        dialog.getDialogPane().setStyle("-fx-padding: 0;");
        dialog.getDialogPane().setPrefSize(860, 620);

        ButtonType btnSubmit = new ButtonType(isEdit ? "💾 Cập nhật" : "Lưu bảng giá", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, btnCancel);
        styleDialogButtons(dialog, btnSubmit, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSubmit()) event.consume();
        });

        dialog.showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    @SuppressWarnings("unchecked")
    private VBox buildContent() {
        VBox content = new VBox(0);

        // ── Header ──────────────────────────────────────────────────
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(22, 28, 16, 28));
        headerBox.setStyle("-fx-background-color: " + C_NAVY + ";");

        Label dlgTitle = new Label(isEdit ? "Chỉnh sửa bảng giá dịch vụ" : "Tạo bảng giá dịch vụ mới");
        dlgTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        dlgTitle.setTextFill(Color.WHITE);

        Label dlgSub = new Label(isEdit ? "Mã bảng giá: " + bangGia.getMaBangGia() : "Thiết lập đợt giá mới cho các dịch vụ");
        dlgSub.setFont(Font.font("Segoe UI", 13));
        dlgSub.setTextFill(Color.web("#93c5fd"));

        Region sep = new Region(); sep.setPrefHeight(2);
        sep.setStyle("-fx-background-color: #3b82f6;");
        headerBox.getChildren().addAll(dlgTitle, dlgSub, sep);

        // ── Form thông tin chung ─────────────────────────────────────
        String autoMa = "BG" + (System.currentTimeMillis() % 10000);
        GridPane infoGrid = new GridPane();
        infoGrid.setPadding(new Insets(20, 28, 12, 28));
        infoGrid.setHgap(20); infoGrid.setVgap(12);
        infoGrid.setStyle("-fx-background-color: " + C_BG + ";");

        txtMaBG = new TextField(isEdit ? bangGia.getMaBangGia() : autoMa);
        txtMaBG.setEditable(false);
        txtMaBG.setStyle(fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; -fx-font-weight: bold;");

        txtTenBG = new TextField(isEdit ? nvl(bangGia.getTenBangGia()) : "");
        txtTenBG.setPromptText("Nhập tên bảng giá...");
        txtTenBG.setStyle(fieldStyle());

        dpNgayAD = new DatePicker();
        dpNgayAD.setMaxWidth(Double.MAX_VALUE);
        dpNgayAD.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 40;");
        if (isEdit && bangGia.getNgayApDung() != null)
            dpNgayAD.setValue(bangGia.getNgayApDung().toLocalDate());
        else dpNgayAD.setValue(LocalDate.now());

        dpNgayHH = new DatePicker();
        dpNgayHH.setMaxWidth(Double.MAX_VALUE);
        dpNgayHH.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 40;");
        if (isEdit && bangGia.getNgayHetHieuLuc() != null)
            dpNgayHH.setValue(bangGia.getNgayHetHieuLuc().toLocalDate());
        else dpNgayHH.setValue(LocalDate.now().plusMonths(1));

        infoGrid.add(makeLbl("Mã bảng giá"), 0, 0);  infoGrid.add(txtMaBG, 1, 0);
        infoGrid.add(makeLbl("Tên bảng giá *"), 2, 0); infoGrid.add(txtTenBG, 3, 0);
        infoGrid.add(makeLbl("Ngày áp dụng *"), 0, 1); infoGrid.add(dpNgayAD, 1, 1);
        infoGrid.add(makeLbl("Ngày hết hạn *"), 2, 1); infoGrid.add(dpNgayHH, 3, 1);

        ColumnConstraints cc = new ColumnConstraints(); cc.setMinWidth(100); cc.setMaxWidth(140);
        ColumnConstraints cf = new ColumnConstraints(); cf.setHgrow(Priority.ALWAYS); cf.setFillWidth(true);
        infoGrid.getColumnConstraints().addAll(cc, cf, cc, cf);

        // ── Bảng chi tiết giá ────────────────────────────────────────
        VBox tableSection = new VBox(10);
        tableSection.setPadding(new Insets(4, 28, 16, 28));
        tableSection.setStyle("-fx-background-color: " + C_BG + ";");
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Toolbar của bảng
        HBox tableToolbar = new HBox(10);
        tableToolbar.setAlignment(Pos.CENTER_LEFT);

        Label lblTableTitle = new Label("Danh sách dịch vụ áp dụng");
        lblTableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTableTitle.setTextFill(Color.web(C_TEXT_DARK));
        HBox.setHgrow(lblTableTitle, Priority.ALWAYS);

        Button btnAdd = new Button("＋  Thêm dịch vụ");
        btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnAdd.setCursor(Cursor.HAND);
        btnAdd.setStyle("-fx-background-color: " + C_GREEN + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 7 14;");
        btnAdd.setOnAction(e -> openChonDichVuDialog());

        Button btnRemove = new Button("✕  Xóa dòng chọn");
        btnRemove.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btnRemove.setCursor(Cursor.HAND);
        btnRemove.setStyle("-fx-background-color: " + C_RED + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 7 14;");
        btnRemove.setOnAction(e -> {
            Object[] sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) tableData.remove(sel);
        });

        tableToolbar.getChildren().addAll(lblTableTitle, btnAdd, btnRemove);

        // TableView
        tableData = FXCollections.observableArrayList();
        table = new TableView<>(tableData);
        table.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER + "; -fx-border-radius: 8;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Object[], String> cMa = col("Mã DV", 0, false, "-fx-alignment: CENTER; -fx-font-weight: bold;");
        cMa.setMaxWidth(100);
        TableColumn<Object[], String> cTen = col("Tên dịch vụ", 1, false, null);
        TableColumn<Object[], String> cLoai = col("Loại", 2, false, "-fx-alignment: CENTER;");
        cLoai.setMaxWidth(110);
        TableColumn<Object[], String> cGocBase = col("Giá gốc (đ)", 3, false, "-fx-alignment: CENTER;");
        cGocBase.setMaxWidth(120);

        // Cột Giá áp dụng – editable
        TableColumn<Object[], String> cGiaAD = new TableColumn<>("Giá áp dụng (đ)");
        cGiaAD.setStyle("-fx-alignment: CENTER;");
        cGiaAD.setMinWidth(140);
        cGiaAD.setCellValueFactory(p -> {
            Object v = p.getValue()[4];
            return new SimpleStringProperty(v != null ? v.toString() : "");
        });
        cGiaAD.setCellFactory(tc -> new TableCell<>() {
            private final TextField tf = new TextField();
            { tf.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"); }
            @Override public void startEdit() {
                super.startEdit(); setGraphic(tf); setText(null);
                tf.setText(getItem()); tf.requestFocus();
                tf.setOnAction(e -> commitEdit(tf.getText()));
                tf.focusedProperty().addListener((o, ov, nv) -> { if (!nv) commitEdit(tf.getText()); });
            }
            @Override public void cancelEdit() { super.cancelEdit(); setGraphic(null); setText(getItem()); }
            @Override public void commitEdit(String newVal) {
                super.commitEdit(newVal);
                getTableRow().getItem()[4] = newVal;
                setGraphic(null); setText(newVal);
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!isEditing()) { setGraphic(null); setText(empty ? null : item); }
            }
        });
        cGiaAD.setEditable(true);
        table.setEditable(true);

        table.getColumns().addAll(cMa, cTen, cLoai, cGocBase, cGiaAD);

        // Load data nếu sửa
        if (isEdit && dsChiTietGoc != null) {
            DichVuDAO dvDAO = new DichVuDAO();
            for (BangGiaDichVu_ChiTiet ct : dsChiTietGoc) {
                DichVu dv = dvDAO.getServiceByID(ct.getMaDichVu().getMaDV());
                if (dv != null) {
                    tableData.add(new Object[]{
                        dv.getMaDV(), dv.getTenDV(), dv.getLoaiDV(),
                        String.format("%,.0f", dv.getGia()),
                        String.valueOf((long) ct.getGiaDichVu())
                    });
                }
            }
        }

        // Gợi ý click vào cột giá áp dụng
        Label lblHint = new Label("💡 Click vào cột \"Giá áp dụng\" để nhập giá cho từng dịch vụ");
        lblHint.setFont(Font.font("Segoe UI", 12));
        lblHint.setTextFill(Color.web(C_TEXT_GRAY));

        tableSection.getChildren().addAll(tableToolbar, table, lblHint);

        content.getChildren().addAll(headerBox, infoGrid, tableSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        return content;
    }

    private void openChonDichVuDialog() {
        Dialog<ButtonType> chonDialog = new Dialog<>();
        chonDialog.setTitle("Chọn dịch vụ thêm vào bảng giá");
        chonDialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) chonDialog.initOwner(owner);

        DichVuDAO dvDAO = new DichVuDAO();
        List<DichVu> tatCa = dvDAO.getAll();

        ObservableList<DichVu> chonData = FXCollections.observableArrayList();
        for (DichVu dv : tatCa) {
            boolean exists = tableData.stream().anyMatch(r -> r[0].equals(dv.getMaDV()));
            if (!exists) chonData.add(dv);
        }

        TableView<DichVu> chonTable = new TableView<>(chonData);
        chonTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        chonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        chonTable.setPrefSize(500, 350);

        TableColumn<DichVu, String> cMa = new TableColumn<>("Mã DV");
        cMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getMaDV()));
        cMa.setMaxWidth(100);
        TableColumn<DichVu, String> cTen = new TableColumn<>("Tên dịch vụ");
        cTen.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTenDV()));
        TableColumn<DichVu, String> cGia = new TableColumn<>("Giá gốc (đ)");
        cGia.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f", p.getValue().getGia())));
        cGia.setMaxWidth(130);
        cGia.setStyle("-fx-alignment: CENTER;");
        chonTable.getColumns().add(cMa);
        chonTable.getColumns().add(cTen);
        chonTable.getColumns().add(cGia);

        chonDialog.getDialogPane().setContent(new VBox(chonTable));
        chonDialog.getDialogPane().setStyle("-fx-padding: 16;");
        ButtonType ok = new ButtonType("✔  Thêm vào bảng", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        chonDialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        chonDialog.showAndWait().ifPresent(btn -> {
            if (btn == ok) {
                for (DichVu dv : chonTable.getSelectionModel().getSelectedItems()) {
                    tableData.add(new Object[]{
                        dv.getMaDV(), dv.getTenDV(), dv.getLoaiDV(),
                        String.format("%,.0f", dv.getGia()),
                        String.valueOf((long) dv.getGia())  // default = giá gốc
                    });
                }
            }
        });
    }

    private boolean validateAndSubmit() {
        if (txtTenBG.getText().trim().isEmpty()) { showError("Lỗi", "Tên bảng giá không được để trống!"); return false; }
        if (dpNgayAD.getValue() == null || dpNgayHH.getValue() == null) { showError("Lỗi", "Vui lòng chọn đủ ngày áp dụng và hết hạn!"); return false; }
        if (dpNgayHH.getValue().isBefore(dpNgayAD.getValue())) { showError("Lỗi", "Ngày hết hạn phải sau ngày áp dụng!"); return false; }

        List<BangGiaDichVu_ChiTiet> dsGia = new ArrayList<>();
        for (Object[] row : tableData) {
            String rawGia = row[4] != null ? row[4].toString().trim() : "";
            if (!rawGia.isEmpty()) {
                try {
                    double gia = Double.parseDouble(rawGia.replace(",", ""));
                    BangGiaDichVu_ChiTiet ct = new BangGiaDichVu_ChiTiet();
                    ct.setMaDichVu(new DichVu(row[0].toString()));
                    ct.setGiaDichVu(gia);
                    ct.setDonViTinh("Cái");
                    dsGia.add(ct);
                } catch (NumberFormatException e) {
                    showError("Lỗi giá", "Giá tại dịch vụ \"" + row[1] + "\" không hợp lệ!"); return false;
                }
            }
        }
        if (dsGia.isEmpty()) { showError("Thiếu thông tin", "Phải nhập giá cho ít nhất 1 dịch vụ!"); return false; }

        BangGiaDichVu bg = new BangGiaDichVu();
        bg.setMaBangGia(txtMaBG.getText().trim());
        bg.setTenBangGia(txtTenBG.getText().trim());
        bg.setNgayApDung(Date.valueOf(dpNgayAD.getValue()));
        bg.setNgayHetHieuLuc(Date.valueOf(dpNgayHH.getValue()));
        bg.setTrangThai(0);

        BangGiaDichVuDAO bgDAO = new BangGiaDichVuDAO();
        boolean ok;
        if (isEdit) {
            ok = bgDAO.updateFullBangGia(bg, dsGia);
            if (ok) showInfo("Cập nhật thành công!", "Bảng giá " + bg.getMaBangGia() + " đã được cập nhật.");
            else { showError("Thất bại", "Không thể cập nhật. Kiểm tra kết nối CSDL."); return false; }
        } else {
            ok = bgDAO.insertFullBangGia(bg, dsGia);
            if (ok) showInfo("Thêm thành công!", "Bảng giá " + bg.getMaBangGia() + " đã được tạo.");
            else { showError("Thất bại", "Lỗi hệ thống khi lưu bảng giá!"); return false; }
        }
        if (onSuccess != null) onSuccess.run();
        return true;
    }

    // Utilities
    private TableColumn<Object[], String> col(String header, int idx, boolean editable, String style) {
        TableColumn<Object[], String> c = new TableColumn<>(header);
        c.setCellValueFactory(p -> {
            Object v = p.getValue()[idx];
            return new SimpleStringProperty(v != null ? v.toString() : "");
        });
        c.setEditable(editable);
        if (style != null) c.setStyle(style);
        c.setReorderable(false);
        c.setSortable(false);
        return c;
    }

    private Label makeLbl(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        l.setTextFill(Color.web(C_TEXT_DARK));
        return l;
    }

    private String fieldStyle() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 40;" +
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
