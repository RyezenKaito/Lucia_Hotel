package gui;

import dao.BangGiaDichVuDAO;
import model.entities.BangGiaDichVu;
import model.entities.BangGiaDichVu_ChiTiet;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * BangGiaDichVuView – JavaFX
 * Thay thế BangGiaDichVuPanel (Swing).
 */
public class BangGiaDichVuView extends BorderPane {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD_BG = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_GREEN = "#16a34a";

    /* ── DAO & dữ liệu ─────────────────────────────────────────────── */
    private final BangGiaDichVuDAO dao = new BangGiaDichVuDAO();
    private ObservableList<BangGiaDichVu> masterData = FXCollections.observableArrayList();
    private FilteredList<BangGiaDichVu> filteredData;

    /* ── Controls ───────────────────────────────────────────────────── */
    private TableView<BangGiaDichVu> table;
    private TextField txtSearch;
    private Label lblTong, lblDangAp, lblHetHan;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public BangGiaDichVuView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildTableCard());

        loadData();
    }

    /* ── HEADER ─────────────────────────────────────────────────────── */
    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 12, 0));

        // Dòng 1: Tiêu đề + nút thêm
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label lblTitle = new Label("Bảng giá dịch vụ");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSub = new Label("Quản lý bảng giá và chi tiết giá dịch vụ của khách sạn");
        lblSub.setFont(Font.font("Segoe UI", 14));
        lblSub.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAddDV = new Button("＋  Thêm dịch vụ");
        btnAddDV.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnAddDV.setPrefHeight(40);
        btnAddDV.setCursor(Cursor.HAND);
        styleButton(btnAddDV, "#374151", "white", "#1f2937");
        btnAddDV.setOnAction(e -> openThemDichVuDialog());

        Button btnAddBG = new Button("＋  Thêm bảng giá");
        btnAddBG.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnAddBG.setPrefHeight(40);
        btnAddBG.setCursor(Cursor.HAND);
        styleButton(btnAddBG, C_BLUE, "white", C_BLUE_HOVER);
        btnAddBG.setOnAction(e -> openThemBangGiaDialog());

        btnBox.getChildren().addAll(btnAddDV, btnAddBG);
        titleRow.getChildren().addAll(titleBox, btnBox);

        // Dòng 2: Thẻ thống kê
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        lblTong = new Label("0");
        lblDangAp = new Label("0");
        lblHetHan = new Label("0");

        VBox c1 = createStatCard("📋", "TỔNG BẢNG GIÁ", lblTong, C_NAVY);
        VBox c2 = createStatCard("✅", "ĐANG ÁP DỤNG", lblDangAp, C_GREEN);
        VBox c3 = createStatCard("⏸", "NGƯNG ÁP DỤNG", lblHetHan, C_TEXT_GRAY);

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        statsRow.getChildren().addAll(c1, c2, c3);

        // Dòng 3: Tìm kiếm
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm kiếm theo mã hoặc tên bảng giá...");
        txtSearch.setFont(Font.font("Segoe UI", 14));
        txtSearch.setPrefHeight(42);
        txtSearch.setStyle(
                "-fx-background-color: white; -fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 16;");
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n));

        header.getChildren().addAll(titleRow, statsRow, txtSearch);
        return header;
    }

    /* ── TABLE CARD ─────────────────────────────────────────────────── */
    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000010")));

        table = new TableView<>();
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;" +
                "-fx-table-cell-border-color: " + C_BORDER + ";");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Không có dữ liệu bảng giá"));

        // STT
        TableColumn<BangGiaDichVu, String> colSTT = new TableColumn<>("STT");
        colSTT.setMinWidth(50); colSTT.setMaxWidth(60);
        colSTT.setStyle("-fx-alignment: CENTER;");
        colSTT.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(table.getItems().indexOf(p.getValue()) + 1)));

        // Mã BG
        TableColumn<BangGiaDichVu, String> colMa = new TableColumn<>("Mã bảng giá");
        colMa.setMinWidth(120);
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(nvl(p.getValue().getMaBangGia())));

        // Tên BG
        TableColumn<BangGiaDichVu, String> colTen = new TableColumn<>("Tên bảng giá");
        colTen.setMinWidth(200);
        colTen.setCellValueFactory(p -> new SimpleStringProperty(nvl(p.getValue().getTenBangGia())));

        // Ngày áp dụng
        TableColumn<BangGiaDichVu, String> colNgayAD = new TableColumn<>("Ngày áp dụng");
        colNgayAD.setMinWidth(130);
        colNgayAD.setStyle("-fx-alignment: CENTER;");
        colNgayAD.setCellValueFactory(p -> {
            java.sql.Date d = p.getValue().getNgayApDung();
            return new SimpleStringProperty(d != null ? SDF.format(d) : "");
        });

        // Ngày hết hạn
        TableColumn<BangGiaDichVu, String> colNgayHH = new TableColumn<>("Ngày hết hạn");
        colNgayHH.setMinWidth(130);
        colNgayHH.setStyle("-fx-alignment: CENTER;");
        colNgayHH.setCellValueFactory(p -> {
            java.sql.Date d = p.getValue().getNgayHetHieuLuc();
            return new SimpleStringProperty(d != null ? SDF.format(d) : "");
        });

        // Trạng thái
        TableColumn<BangGiaDichVu, String> colTT = new TableColumn<>("Trạng thái");
        colTT.setMinWidth(140);
        colTT.setStyle("-fx-alignment: CENTER;");
        colTT.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTrangThai() == 1 ? "Đang áp dụng" : "Ngưng áp dụng"));
        colTT.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else if (item.equals("Đang áp dụng")) {
                    setText(item);
                    setStyle("-fx-text-fill: " + C_GREEN + "; -fx-alignment: CENTER; -fx-font-weight: bold;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + C_TEXT_GRAY + "; -fx-alignment: CENTER;");
                }
            }
        });

        for (TableColumn<BangGiaDichVu, ?> c : List.of(colSTT, colMa, colTen, colNgayAD, colNgayHH, colTT)) {
            c.setReorderable(false);
            c.setSortable(false);
        }
        table.getColumns().addAll(colSTT, colMa, colTen, colNgayAD, colNgayHH, colTT);

        // Context menu
        ContextMenu ctxMenu = new ContextMenu();
        MenuItem miEdit = new MenuItem("✏  Chỉnh sửa bảng giá");
        miEdit.setStyle("-fx-font-size: 13px;");
        miEdit.setOnAction(e -> {
            BangGiaDichVu bg = table.getSelectionModel().getSelectedItem();
            if (bg != null) openSuaBangGiaDialog(bg);
        });
        MenuItem miDelete = new MenuItem("🗑  Xóa bảng giá");
        miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
        miDelete.setOnAction(e -> {
            BangGiaDichVu bg = table.getSelectionModel().getSelectedItem();
            if (bg != null) handleDelete(bg);
        });
        ctxMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDelete);

        table.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY && table.getSelectionModel().getSelectedItem() != null)
                ctxMenu.show(table, e.getScreenX(), e.getScreenY());
            else ctxMenu.hide();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        card.getChildren().add(table);
        return card;
    }

    /* ── DATA ───────────────────────────────────────────────────────── */
    public void loadData() {
        try {
            List<BangGiaDichVu> list = dao.getAllBangGia();
            masterData.setAll(list);
            filteredData = new FilteredList<>(masterData, p -> true);
            table.setItems(filteredData);

            long dangAp = list.stream().filter(b -> b.getTrangThai() == 1).count();
            lblTong.setText(String.valueOf(list.size()));
            lblDangAp.setText(String.valueOf(dangAp));
            lblHetHan.setText(String.valueOf(list.size() - dangAp));
        } catch (Exception ex) {
            lblTong.setText("—");
            lblDangAp.setText("—");
            lblHetHan.setText("—");
        }
    }

    private void applyFilter(String keyword) {
        if (filteredData == null) return;
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredData.setPredicate(bg -> {
            if (kw.isEmpty()) return true;
            return nvl(bg.getMaBangGia()).toLowerCase().contains(kw)
                    || nvl(bg.getTenBangGia()).toLowerCase().contains(kw);
        });
    }

    /* ── ACTIONS ─────────────────────────────────────────────────────── */
    private void openThemDichVuDialog() {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        new ThemSuaDichVuDialog(owner, null, this::loadData).show();
    }

    private void openThemBangGiaDialog() {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        new ThemSuaBangGiaDialog(owner, null, null, this::loadData).show();
    }

    private void openSuaBangGiaDialog(BangGiaDichVu bg) {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        List<BangGiaDichVu_ChiTiet> dsChiTiet = dao.getChiTietByMa(bg.getMaBangGia());
        new ThemSuaBangGiaDialog(owner, bg, dsChiTiet, this::loadData).show();
    }

    private void handleDelete(BangGiaDichVu bg) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa bảng giá [" + bg.getMaBangGia() + "]?");
        confirm.setContentText("Hành động này sẽ xóa toàn bộ chi tiết giá trong bảng.\nKhông thể hoàn tác!");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showAlert(Alert.AlertType.INFORMATION, "Chú ý", "Chức năng xóa bảng giá đang được bảo trì để tránh ảnh hưởng dữ liệu lịch sử.");
        }
    }

    /* ── UTIL ───────────────────────────────────────────────────────── */
    private VBox createStatCard(String icon, String title, Label valueLbl, String accentHex) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + C_CARD_BG + "; -fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000018")));

        HBox topRow = new HBox(); topRow.setAlignment(Pos.CENTER_LEFT);
        VBox textBox = new VBox(4); HBox.setHgrow(textBox, Priority.ALWAYS);
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTitle.setTextFill(Color.web(C_TEXT_GRAY));
        valueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLbl.setTextFill(Color.web(accentHex));
        textBox.getChildren().addAll(lblTitle, valueLbl);

        StackPane badge = new StackPane(); badge.setMinSize(46, 46); badge.setPrefSize(46, 46);
        Rectangle bg = new Rectangle(46, 46); bg.setArcWidth(10); bg.setArcHeight(10);
        Color ac = Color.web(accentHex);
        bg.setFill(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 0.12));
        Label iconLbl = new Label(icon); iconLbl.setFont(Font.font("Segoe UI Emoji", 22));
        badge.getChildren().addAll(bg, iconLbl);
        topRow.getChildren().addAll(textBox, badge);

        card.getChildren().add(topRow);
        return card;
    }

    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        String base = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;";
        String hover = base.replace("-fx-background-color: " + bg, "-fx-background-color: " + hoverBg);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void showAlert(Alert.AlertType type, String header, String msg) {
        Alert a = new Alert(type);
        a.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String nvl(String s) { return s != null ? s : ""; }
}
