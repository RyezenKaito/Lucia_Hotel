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
import javafx.beans.binding.Bindings;
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

        Button btnAddService = new Button("＋ Thêm dịch vụ");
        btnAddService.setStyle("-fx-background-color: " + C_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        btnAddService
                .setOnMouseEntered(e -> btnAddService.setStyle(btnAddService.getStyle().replace(C_BLUE, C_BLUE_HOVER)));
        btnAddService
                .setOnMouseExited(e -> btnAddService.setStyle(btnAddService.getStyle().replace(C_BLUE_HOVER, C_BLUE)));
        btnAddService.setOnAction(e -> showServicePicker());

        tableToolbar.getChildren().addAll(lblTableTitle, txtSearch);
        if (isEdit) {
            tableToolbar.getChildren().add(btnAddService);
        }

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

                // Chỉ cho phép nhập số và dấu phẩy (dấu phẩy do formatter tự thêm)
                tf.setTextFormatter(new TextFormatter<>(change -> {
                    if (change.getControlNewText().matches("[\\d,]*")) {
                        return change;
                    }
                    return null;
                }));

                tf.textProperty().addListener((obs, oldV, newV) -> {
                    if (updating || newV == null || newV.isEmpty())
                        return;

                    // Loại bỏ dấu phẩy cũ để lấy số thuần túy
                    String digits = newV.replaceAll("[^\\d]", "");
                    if (digits.isEmpty()) {
                        updating = true;
                        tf.setText("");
                        updating = false;
                        return;
                    }

                    try {
                        long val = Long.parseLong(digits);
                        String formatted = String.format("%,d", val);

                        if (!formatted.equals(newV)) {
                            updating = true;
                            tf.setText(formatted);
                            tf.positionCaret(tf.getText().length());
                            updating = false;
                        }
                    } catch (NumberFormatException e) {
                        // Trường hợp số quá lớn (vượt quá Long)
                        updating = true;
                        tf.setText(oldV);
                        updating = false;
                    }
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

        // Thiết lập ContextMenu xóa dịch vụ khi ở chế độ Sửa
        table.setRowFactory(tv -> {
            TableRow<Object[]> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Xóa dịch vụ khỏi bảng giá này");
            deleteItem.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            deleteItem.setOnAction(event -> {
                Object[] rowData = row.getItem();
                if (rowData != null) {
                    handleDeleteServiceFromList(rowData);
                }
            });
            contextMenu.getItems().add(deleteItem);

            if (isEdit) {
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu));
            }
            return row;
        });

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
        tableData.clear();
        DichVuDAO dvDAO = new DichVuDAO();
        List<DichVu> allDV = dvDAO.getAll();

        if (isEdit && dsChiTietGoc != null) {
            // Nạp full thông tin dịch vụ để tránh bị 0 (giá gốc) hoặc null (tên/loại)
            Map<String, DichVu> dvMap = new HashMap<>();
            for (DichVu d : allDV) {
                dvMap.put(d.getMaDV(), d);
            }

            for (BangGiaDichVu_ChiTiet ct : dsChiTietGoc) {
                DichVu dvFromCT = ct.getMaDichVu();
                DichVu dvFull = dvMap.get(dvFromCT.getMaDV());
                if (dvFull == null)
                    dvFull = dvFromCT;

                tableData.add(new Object[] {
                        dvFull.getMaDV(),
                        dvFull.getTenDV(),
                        dvFull.getLoaiDV(),
                        String.format("%,.0f", dvFull.getGia()),
                        String.format("%,.0f", ct.getGiaDichVu())
                });
            }
        } else if (!isEdit) {
            // Chế độ THÊM MỚI: Load tất cả dịch vụ, giá áp dụng mặc định ĐỂ TRỐNG
            for (DichVu dv : allDV) {
                tableData.add(new Object[] {
                        dv.getMaDV(),
                        dv.getTenDV(),
                        dv.getLoaiDV(),
                        String.format("%,.0f", dv.getGia()),
                        "" // Để trống giá áp dụng
                });
            }
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

        // Parse chi tiết giá (Chỉ thêm các dịch vụ có giá thay đổi so với giá gốc)
        List<BangGiaDichVu_ChiTiet> dsGia = new ArrayList<>();
        for (Object[] row : tableData) {
            String rawGia = row[4] != null ? row[4].toString().trim() : "";
            if (!rawGia.isEmpty()) {
                try {
                    // Loại bỏ tất cả ký tự không phải số để parse an toàn
                    String digitsGia = rawGia.replaceAll("[^\\d]", "");
                    if (digitsGia.isEmpty())
                        continue;
                    double gia = Double.parseDouble(digitsGia);

                    // Lấy giá gốc để so sánh
                    String rawGiaGoc = row[3] != null ? row[3].toString().trim() : "0";
                    String digitsGiaGoc = rawGiaGoc.replaceAll("[^\\d]", "");
                    double giaGoc = digitsGiaGoc.isEmpty() ? 0 : Double.parseDouble(digitsGiaGoc);

                    // Chỉ thêm vào danh sách nếu giá áp dụng khác giá gốc
                    if (gia != giaGoc) {
                        BangGiaDichVu_ChiTiet ct = new BangGiaDichVu_ChiTiet();
                        ct.setMaDichVu(new DichVu(row[0].toString()));
                        ct.setGiaDichVu(gia);
                        ct.setDonViTinh("Cái");
                        dsGia.add(ct);
                    }
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
        String currentMa = txtMaBG.getText().trim();

        BangGiaDichVuDAO bgDAO = new BangGiaDichVuDAO();

        // Kiểm tra tránh trùng mã (vòng lặp để đảm bảo mã duy nhất tuyệt đối)
        int safety = 0;
        while (!isEdit && bgDAO.exists(currentMa) && safety < 10) {
            currentMa = bgDAO.generateNextMaBangGia();
            safety++;
        }
        txtMaBG.setText(currentMa); // Cập nhật UI để người dùng thấy mã thực tế được lưu

        bg.setMaBangGia(currentMa);
        bg.setTenBangGia(txtTenBG.getText().trim());
        bg.setNgayApDung(Date.valueOf(dpNgayAD.getValue()));
        bg.setNgayHetHieuLuc(Date.valueOf(dpNgayHH.getValue()));

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

    private void showServicePicker() {
        Dialog<List<DichVu>> picker = new Dialog<>();
        picker.setTitle("Chọn dịch vụ thêm vào bảng giá");
        picker.initOwner(owner);
        picker.initModality(Modality.WINDOW_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(600);

        TextField search = new TextField();
        search.setPromptText("Tìm dịch vụ...");
        search.setStyle("-fx-background-radius: 15; -fx-padding: 8 15;");

        DichVuDAO dvDAO = new DichVuDAO();
        List<DichVu> all = dvDAO.getAll();
        // Lọc bỏ những gì đã có trong bảng
        List<String> existingIds = new ArrayList<>();
        for (Object[] row : tableData) {
            existingIds.add(row[0].toString());
        }
        List<DichVu> available = new ArrayList<>();
        for (DichVu d : all) {
            if (!existingIds.contains(d.getMaDV())) {
                available.add(d);
            }
        }

        ObservableList<DichVuSelection> pickerData = FXCollections.observableArrayList();
        for (DichVu d : available) {
            pickerData.add(new DichVuSelection(d));
        }

        FilteredList<DichVuSelection> filteredPicker = new FilteredList<>(pickerData, p -> true);
        search.textProperty().addListener((obs, oldV, newV) -> {
            filteredPicker.setPredicate(item -> {
                if (newV == null || newV.trim().isEmpty())
                    return true;
                String lower = newV.toLowerCase();
                return item.dv.getMaDV().toLowerCase().contains(lower) ||
                        item.dv.getTenDV().toLowerCase().contains(lower);
            });
        });

        TableView<DichVuSelection> pickerTable = new TableView<>(filteredPicker);
        pickerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pickerTable.setPrefHeight(400);

        TableColumn<DichVuSelection, Boolean> cCheck = new TableColumn<>("Chọn");
        cCheck.setCellValueFactory(p -> p.getValue().selected);
        cCheck.setCellFactory(tc -> new javafx.scene.control.cell.CheckBoxTableCell<>());
        cCheck.setEditable(true);
        cCheck.setMaxWidth(60);

        TableColumn<DichVuSelection, String> cId = new TableColumn<>("Mã");
        cId.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().dv.getMaDV()));
        TableColumn<DichVuSelection, String> cName = new TableColumn<>("Tên dịch vụ");
        cName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().dv.getTenDV()));
        TableColumn<DichVuSelection, String> cType = new TableColumn<>("Loại");
        cType.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().dv.getLoaiDV()));

        pickerTable.getColumns().addAll(cCheck, cId, cName, cType);
        pickerTable.setEditable(true);

        root.getChildren().addAll(new Label("Danh sách dịch vụ chưa áp dụng:"), search, pickerTable);
        picker.getDialogPane().setContent(root);

        ButtonType btnNap = new ButtonType("Nạp dịch vụ", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        picker.getDialogPane().getButtonTypes().addAll(btnNap, btnHuy);

        picker.setResultConverter(bt -> {
            if (bt == btnNap) {
                List<DichVu> selected = new ArrayList<>();
                for (DichVuSelection s : pickerData) {
                    if (s.selected.get())
                        selected.add(s.dv);
                }
                return selected;
            }
            return null;
        });

        picker.showAndWait().ifPresent(selected -> {
            for (DichVu dv : selected) {
                tableData.add(new Object[] {
                        dv.getMaDV(),
                        dv.getTenDV(),
                        dv.getLoaiDV(),
                        String.format("%,.0f", dv.getGia()),
                        String.format("%,.0f", dv.getGia()) // Mặc định bảng giá mới = giá gốc
                });
            }
        });
    }

    private static class DichVuSelection {
        final DichVu dv;
        final javafx.beans.property.BooleanProperty selected = new javafx.beans.property.SimpleBooleanProperty(false);

        DichVuSelection(DichVu dv) {
            this.dv = dv;
        }
    }

    private void handleDeleteServiceFromList(Object[] rowData) {
        String tenDV = (String) rowData[1];
        String tenBG = txtTenBG.getText();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa dịch vụ khỏi bảng giá");
        confirm.setContentText("Bạn có chắc muốn xóa dịch vụ [" + tenDV + "] ra khỏi bảng giá [" + tenBG + "] không?");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                tableData.remove(rowData);
            }
        });
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }
}
