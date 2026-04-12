package gui;

import dao.BangGiaDichVuDAO;
import dao.DichVuDAO;
import model.entities.BangGiaDichVu;
import model.entities.BangGiaDichVu_ChiTiet;
import model.entities.DichVu;
import model.utils.DimOverlay;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ThemSuaBangGiaDialog – JavaFX
 * Thay thế ThemBangGiaDialog và SuaBangGiaDialog (Swing).
 * 
 * FIX:
 * - Thêm CheckBox trạng thái (bật/tắt áp dụng)
 * - Sinh mã bảng giá tuần tự (BG001, BG002...)
 * - Kiểm tra xung đột thời gian trước khi lưu
 * - Validate ngày áp dụng (không cho chọn quá khứ khi tạo mới)
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
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Window owner;
    private final BangGiaDichVu bangGia; // null = thêm mới
    private final List<BangGiaDichVu_ChiTiet> dsChiTietGoc;
    private final Runnable onSuccess;
    private final boolean isEdit;

    // Controls
    private TextField txtMaBG, txtTenBG;
    private DatePicker dpNgayAD, dpNgayHH;
    private TableView<Object[]> table; // [maDV, tenDV, loai, giaGoc, giaApDung]
    private ObservableList<Object[]> tableData;
    private FilteredList<Object[]> filteredData;
    private TextField txtSearch;

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
        if (owner != null)
            dialog.initOwner(owner);
        dialog.setResizable(true);

        // FIX: Tăng PrefWidth lên 1100 để các cột không bị hiện "..."
        dialog.getDialogPane().setContent(buildContent());
        dialog.getDialogPane().setStyle("-fx-padding: 0;");
        dialog.getDialogPane().setPrefWidth(1000);
        dialog.getDialogPane().setMinWidth(1000);
        dialog.getDialogPane().setPrefHeight(700);

        ButtonType btnSubmit = new ButtonType(isEdit ? "💾 Cập nhật" : "💾 Thêm mới", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, btnCancel);
        styleDialogButtons(dialog, btnSubmit, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSubmit);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSubmit())
                event.consume();
        });

        dialog.showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    @SuppressWarnings("unchecked")
    private VBox buildContent() {
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: white;");
        content.setMaxWidth(Double.MAX_VALUE);

        // ── 1. Header ───────────────────────────────────────────────
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(22, 28, 16, 28));
        headerBox.setStyle("-fx-background-color: " + C_NAVY + ";");
        Label dlgTitle = new Label(isEdit ? "Chỉnh sửa bảng giá dịch vụ" : "Tạo bảng giá dịch vụ mới");
        dlgTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        dlgTitle.setTextFill(Color.WHITE);
        Label dlgSub = new Label(
                isEdit ? "Mã bảng giá: " + bangGia.getMaBangGia() : "Thiết lập đợt giá mới cho các dịch vụ");
        dlgSub.setFont(Font.font("Segoe UI", 14));
        dlgSub.setTextFill(Color.web("#93c5fd"));
        Region sep = new Region();
        sep.setPrefHeight(2);
        sep.setStyle("-fx-background-color: #3b82f6;");
        headerBox.getChildren().addAll(dlgTitle, dlgSub, sep);

        // ── 2. Form thông tin chung ─────────────────────────────────
        BangGiaDichVuDAO bgDAO = new BangGiaDichVuDAO();
        String autoMa = isEdit ? bangGia.getMaBangGia() : bgDAO.generateNextMaBangGia();

        GridPane infoGrid = new GridPane();
        infoGrid.setPadding(new Insets(20, 28, 10, 28));
        infoGrid.setHgap(30);
        infoGrid.setVgap(10);
        infoGrid.setStyle("-fx-background-color: " + C_BG + ";");

        txtMaBG = new TextField(autoMa);
        txtMaBG.setEditable(false);
        txtMaBG.setStyle(
                fieldStyle() + "-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; -fx-font-weight: bold;");

        txtTenBG = new TextField(isEdit ? nvl(bangGia.getTenBangGia()) : "");
        txtTenBG.setPromptText("Nhập tên bảng giá...");
        txtTenBG.setStyle(fieldStyle());
        txtTenBG.setMaxWidth(Double.MAX_VALUE);

        dpNgayAD = new DatePicker();
        dpNgayAD.setPromptText("dd/MM/yyyy");
        dpNgayAD.setMaxWidth(Double.MAX_VALUE);
        dpNgayAD.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 40;");

        dpNgayHH = new DatePicker();
        dpNgayHH.setPromptText("dd/MM/yyyy");
        dpNgayHH.setMaxWidth(Double.MAX_VALUE);
        dpNgayHH.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-pref-height: 40;");

        // Force dd/MM/yyyy format for selected values
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? DTF.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isEmpty())
                    return null;
                try {
                    return LocalDate.parse(string, DTF);
                } catch (Exception e) {
                    return null;
                }
            }
        };
        dpNgayAD.setConverter(converter);
        dpNgayHH.setConverter(converter);

        if (isEdit && bangGia.getNgayApDung() != null)
            dpNgayAD.setValue(bangGia.getNgayApDung().toLocalDate());
        else
            dpNgayAD.setValue(LocalDate.now());

        if (isEdit && bangGia.getNgayHetHieuLuc() != null)
            dpNgayHH.setValue(bangGia.getNgayHetHieuLuc().toLocalDate());
        else
            dpNgayHH.setValue(LocalDate.now());

        // Logic ngày giữ nguyên
        if (isEdit && bangGia.getNgayApDung() != null
                && !bangGia.getNgayApDung().toLocalDate().isAfter(LocalDate.now())) {
            dpNgayAD.setDisable(true);
            dpNgayAD.setStyle(dpNgayAD.getStyle() + "-fx-opacity: 0.7;");
        }

        // Khống chế ngày áp dụng >= hôm nay
        dpNgayAD.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        dpNgayAD.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && dpNgayHH.getValue() != null && dpNgayHH.getValue().isBefore(newV)) {
                dpNgayHH.setValue(newV.plusDays(1));
            }
            refreshDateHHConstraint();
        });
        refreshDateHHConstraint(); // Khởi tạo ban đầu

        infoGrid.add(makeLbl("Mã bảng giá"), 0, 0);
        infoGrid.add(txtMaBG, 1, 0);
        infoGrid.add(makeLbl("Tên bảng giá *"), 2, 0);
        infoGrid.add(txtTenBG, 3, 0);
        infoGrid.add(makeLbl("Ngày áp dụng *"), 0, 1);
        infoGrid.add(dpNgayAD, 1, 1);
        infoGrid.add(makeLbl("Ngày hết hạn *"), 2, 1);
        infoGrid.add(dpNgayHH, 3, 1);

        ColumnConstraints ccLabel = new ColumnConstraints(120);
        ColumnConstraints ccField = new ColumnConstraints();
        ccField.setHgrow(Priority.ALWAYS);
        infoGrid.getColumnConstraints().addAll(ccLabel, ccField, ccLabel, ccField);

        // ── 3. Bảng chi tiết giá (TĂNG CHIỀU CAO SEARCH, GIẢM FOOTER) ──────
        VBox tableSection = new VBox(10);
        // GIẢM PADDING DƯỚI (chỉ để 5 thay vì 20)
        tableSection.setPadding(new Insets(10, 28, 3, 28));
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        HBox tableToolbar = new HBox(15);
        tableToolbar.setAlignment(Pos.CENTER_LEFT);

        Label lblTableTitle = new Label("DANH SÁCH DỊCH VỤ");
        lblTableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblTableTitle.setTextFill(Color.web(C_NAVY));

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm nhanh theo mã, tên, loại dịch vụ...");
        // TĂNG CHIỀU CAO LÊN 50, FONT SIZE 16px
        txtSearch.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-pref-height: 60; " +
                "-fx-background-radius: 25; -fx-border-radius: 25; -fx-border-color: " + C_BORDER + "; " +
                "-fx-padding: 0 25; -fx-background-color: white;");
        txtSearch.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilter(newV));

        tableToolbar.getChildren().addAll(lblTableTitle, txtSearch);

        tableData = FXCollections.observableArrayList();
        loadAllServices();
        filteredData = new FilteredList<>(tableData, p -> true);
        table = new TableView<>(filteredData);
        table.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER + "; -fx-border-radius: 8;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Định nghĩa cột (Giữ nguyên tỷ lệ % đã fix)
        TableColumn<Object[], String> cMa = col("Mã DV", 0, false, "-fx-alignment: CENTER; -fx-font-weight: bold;");
        TableColumn<Object[], String> cTen = col("Tên dịch vụ", 1, false, null);
        TableColumn<Object[], String> cLoai = col("Loại", 2, false, "-fx-alignment: CENTER;");
        TableColumn<Object[], String> cGocBase = col("Giá gốc (đ)", 3, false, "-fx-alignment: CENTER-RIGHT;");
        TableColumn<Object[], String> cGiaAD = new TableColumn<>("Giá áp dụng (đ)");

        cMa.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        cTen.setMaxWidth(1f * Integer.MAX_VALUE * 40);
        cLoai.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        cGocBase.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        cGiaAD.setMaxWidth(1f * Integer.MAX_VALUE * 20);

        // Logic CellFactory giữ nguyên
        cGiaAD.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: " + C_BLUE + ";");
        cGiaAD.setCellValueFactory(
                p -> new SimpleStringProperty(p.getValue()[4] != null ? p.getValue()[4].toString() : ""));
        cGiaAD.setCellFactory(tc -> new TableCell<>() {
            private final TextField tf = new TextField();
            private boolean updating = false;
            {
                tf.setStyle("-fx-background-color: transparent; -fx-alignment: CENTER-RIGHT;");
                tf.textProperty().addListener((obs, oldV, newV) -> {
                    if (updating || newV == null || newV.isEmpty())
                        return;
                    String digits = newV.replaceAll("[^\\d]", "");
                    if (digits.isEmpty())
                        return;
                    updating = true;
                    tf.setText(String.format("%,d", Long.parseLong(digits)));
                    tf.positionCaret(tf.getText().length());
                    updating = false;
                });
            }

            @Override
            public void startEdit() {
                super.startEdit();
                setGraphic(tf);
                setText(null);
                String raw = getItem() != null ? getItem().replaceAll("[^\\d]", "") : "";
                tf.setText(!raw.isEmpty() ? String.format("%,d", Long.parseLong(raw)) : "");
                tf.requestFocus();
                tf.selectAll();
                tf.setOnAction(e -> commitEdit(tf.getText()));
                tf.focusedProperty().addListener((o, ov, nv) -> {
                    if (!nv)
                        commitEdit(tf.getText());
                });
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(null);
                setText(getItem());
            }

            @Override
            public void commitEdit(String newVal) {
                String digits = newVal != null ? newVal.replaceAll("[^\\d]", "") : "";
                String display = digits.isEmpty() ? "" : String.format("%,d", Long.parseLong(digits));
                super.commitEdit(display);
                getTableRow().getItem()[4] = display;
                setGraphic(null);
                setText(display);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!isEditing()) {
                    setGraphic(null);
                    setText(empty ? null : item);
                }
            }
        });

        table.getColumns().setAll(cMa, cTen, cLoai, cGocBase, cGiaAD);
        table.setEditable(true);

        Label lblHint = new Label("💡 Gợi ý: Click đúp vào ô ở cột \"Giá áp dụng\" để thay đổi giá.");
        lblHint.setFont(Font.font("Segoe UI", 12));
        lblHint.setTextFill(Color.web(C_TEXT_GRAY));

        tableSection.getChildren().addAll(tableToolbar, table, lblHint);
        content.getChildren().addAll(headerBox, infoGrid, tableSection);

        return content;
    }

    private void applyFilter(String kw) {
        if (kw == null || kw.trim().isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = kw.trim().toLowerCase();
            filteredData.setPredicate(row -> {
                String ma = str(row[0]).toLowerCase();
                String ten = str(row[1]).toLowerCase();
                String loai = str(row[2]).toLowerCase();
                return ma.contains(lower) || ten.contains(lower) || loai.contains(lower);
            });
        }
    }

    private static String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private void refreshDateHHConstraint() {
        dpNgayHH.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate minFromAD = dpNgayAD.getValue() != null ? dpNgayAD.getValue() : LocalDate.now();
                // Ngày hết hạn phải >= max(ngày áp dụng, hôm nay)
                LocalDate minDate = minFromAD.isBefore(LocalDate.now()) ? LocalDate.now() : minFromAD;
                setDisable(empty || date.isBefore(minDate));
            }
        });
    }

    /**
     * Load tất cả dịch vụ vào bảng.
     * - Nếu sửa: giữ lại giá áp dụng cũ
     * - Nếu thêm mới: lấy giá từ bảng giá đang Active, nếu không có thì lấy giá gốc
     */
    private void loadAllServices() {
        DichVuDAO dvDAO = new DichVuDAO();
        List<DichVu> tatCa = dvDAO.getAll();

        // Map mã DV -> giá áp dụng
        Map<String, String> giaApDungMap = new HashMap<>();
        if (isEdit && dsChiTietGoc != null) {
            // Sửa: giữ lại giá áp dụng cũ
            for (BangGiaDichVu_ChiTiet ct : dsChiTietGoc) {
                giaApDungMap.put(ct.getMaDichVu().getMaDV(), String.valueOf((long) ct.getGiaDichVu()));
            }
        } else {
            // Thêm mới: lấy giá từ bảng giá đang Active
            BangGiaDichVuDAO bgDAO = new BangGiaDichVuDAO();
            Map<String, Double> activePrices = bgDAO.getActivePriceMap();
            for (Map.Entry<String, Double> entry : activePrices.entrySet()) {
                giaApDungMap.put(entry.getKey(), String.valueOf((long) entry.getValue().doubleValue()));
            }
        }

        tableData.clear();
        for (DichVu dv : tatCa) {
            // Nếu có giá từ bảng giá active/cũ thì dùng, không thì lấy giá gốc
            String rawGia = giaApDungMap.getOrDefault(dv.getMaDV(), String.valueOf((long) dv.getGia()));
            String formatted;
            try {
                long num = Long.parseLong(rawGia.replaceAll("[^\\d]", ""));
                formatted = String.format("%,d", num);
            } catch (NumberFormatException e) {
                formatted = rawGia;
            }
            tableData.add(new Object[] {
                    dv.getMaDV(), dv.getTenDV(), dv.getTenLoaiDV(),
                    String.format("%,.0f", dv.getGia()),
                    formatted
            });
        }
    }

    private boolean validateAndSubmit() {
        // Validate tên bảng giá
        if (txtTenBG.getText().trim().isEmpty()) {
            showError("Lỗi", "Tên bảng giá không được để trống!");
            return false;
        }
        // Validate ngày
        if (dpNgayAD.getValue() == null || dpNgayHH.getValue() == null) {
            showError("Lỗi", "Vui lòng chọn đủ ngày áp dụng và hết hạn!");
            return false;
        }
        if (dpNgayHH.getValue().isBefore(dpNgayAD.getValue())) {
            showError("Lỗi", "Ngày hết hạn phải sau ngày áp dụng!");
            return false;
        }

        // Parse chi tiết giá
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
                    showError("Lỗi giá", "Giá tại dịch vụ \"" + row[1] + "\" không hợp lệ!");
                    return false;
                }
            }
        }
        if (dsGia.isEmpty()) {
            showError("Thiếu thông tin", "Phải nhập giá cho ít nhất 1 dịch vụ!");
            return false;
        }

        // Build entity
        BangGiaDichVu bg = new BangGiaDichVu();
        bg.setMaBangGia(txtMaBG.getText().trim());
        bg.setTenBangGia(txtTenBG.getText().trim());
        bg.setNgayApDung(Date.valueOf(dpNgayAD.getValue()));
        bg.setNgayHetHieuLuc(Date.valueOf(dpNgayHH.getValue()));

        BangGiaDichVuDAO bgDAO = new BangGiaDichVuDAO();

        // === KIỂM TRA XUNG ĐỘT NGHIÊM NGẶT ===
        // Quét toàn bộ bảng giá có trangThai != -1 (chưa xóa mềm)
        String excludeMa = isEdit ? bg.getMaBangGia() : null;
        List<BangGiaDichVu> conflicts = bgDAO.findStrictConflicts(
                bg.getNgayApDung(), bg.getNgayHetHieuLuc(), excludeMa);
        if (!conflicts.isEmpty()) {
            // Chặn cứng: Thông báo lỗi đích danh, không cho tiếp tục
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            StringBuilder sb = new StringBuilder();
            sb.append("Khoảng thời gian bạn chọn bị trùng với:\n\n");
            for (BangGiaDichVu bgConflict : conflicts) {
                sb.append("• Bảng giá [").append(bgConflict.getTenBangGia()).append("]\n");
                sb.append("   Từ ngày ").append(sdf.format(bgConflict.getNgayApDung()));
                sb.append(" đến ngày ").append(sdf.format(bgConflict.getNgayHetHieuLuc()));
                sb.append("\n\n");
            }
            sb.append("Vui lòng chỉnh sửa ngày áp dụng/hết hạn sao cho không chồng lấn.");
            showError("Xung đột thời gian", sb.toString());
            return false;
        }

        // Lưu/Cập nhật
        boolean ok;
        if (isEdit) {
            ok = bgDAO.updateFullBangGia(bg, dsGia);
            if (ok)
                showInfo("Cập nhật thành công!", "Bảng giá " + bg.getMaBangGia() + " đã được cập nhật.");
            else {
                showError("Thất bại", "Không thể cập nhật. Kiểm tra kết nối CSDL.");
                return false;
            }
        } else {
            ok = bgDAO.insertFullBangGia(bg, dsGia);
            if (ok)
                showInfo("Thêm thành công!", "Bảng giá " + bg.getMaBangGia() + " đã được tạo.");
            else {
                showError("Thất bại", "Lỗi hệ thống khi lưu bảng giá!");
                return false;
            }
        }
        if (onSuccess != null)
            onSuccess.run();
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
        if (style != null)
            c.setStyle(style);
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

    private void showInfo(String h, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo");
        a.setHeaderText(h);
        a.setContentText(m);
        a.showAndWait();
    }

    private void showError(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi");
        a.setHeaderText(h);
        a.setContentText(m);
        a.showAndWait();
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }
}
