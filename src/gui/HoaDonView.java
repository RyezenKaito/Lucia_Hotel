package gui;

import dao.HoaDonDAO;
import model.entities.HoaDon;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HoaDonView – JavaFX
 * Thay thế HoaDonPanel (Swing).
 */
public class HoaDonView extends BorderPane {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_GREEN = "#16a34a";

    /* ── DAO & Dữ liệu ────────────────────────────────────────────── */
    private final HoaDonDAO dao = new HoaDonDAO();
    private ObservableList<HoaDon> masterData = FXCollections.observableArrayList();
    private FilteredList<HoaDon> filteredData;

    /* ── Controls ───────────────────────────────────────────────────── */
    private TableView<HoaDon> table;
    private TextField txtSearch;
    private Label lblTongDoanhThu, lblSoHoaDon;

    public HoaDonView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildTableCard());

        loadData();
    }

    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 24, 0));

        // Tiêu đề
        VBox titleBox = new VBox(4);
        Label lblTitle = new Label("Lịch sử hóa đơn");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSubtitle = new Label("Quản lý và tra cứu toàn bộ danh sách hóa đơn đã lập");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        // Thẻ thống kê nhanh
        HBox statsRow = new HBox(20);
        lblTongDoanhThu = new Label("0 đ");
        lblSoHoaDon = new Label("0");
        
        statsRow.getChildren().addAll(
            createStatCard("💰", "TỔNG DOANH THU", lblTongDoanhThu, C_GREEN),
            createStatCard("📄", "TỔNG SỐ HÓA ĐƠN", lblSoHoaDon, C_NAVY)
        );

        // Thanh tìm kiếm & Lọc
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm kiếm theo mã hóa đơn, tên khách hoặc mã đặt phòng...");
        txtSearch.setPrefHeight(44);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER + "; -fx-padding: 0 16;");
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n));

        header.getChildren().addAll(titleBox, statsRow, filterRow);
        return header;
    }

    private VBox buildTableCard() {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        card.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã hóa đơn");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getMaHD()));
        colMa.setMinWidth(120);
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNgayTaoHD() != null ? p.getValue().getNgayTaoHD().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—"));
        colNgay.setMinWidth(140);
        colNgay.setStyle("-fx-alignment: CENTER;");

        TableColumn<HoaDon, String> colDat = new TableColumn<>("Mã đặt phòng");
        colDat.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDatPhong().getMaDat()));
        colDat.setMinWidth(120);
        colDat.setStyle("-fx-alignment: CENTER;");

        TableColumn<HoaDon, String> colNV = new TableColumn<>("Nhân viên lập");
        colNV.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNhanVien() != null ? p.getValue().getNhanVien().getHoTen() : "—"));

        TableColumn<HoaDon, String> colPhong = new TableColumn<>("Tiền phòng");
        colPhong.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f", p.getValue().getTienPhong())));
        colPhong.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<HoaDon, String> colDV = new TableColumn<>("Tiền dịch vụ");
        colDV.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f", p.getValue().getTienDV())));
        colDV.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<HoaDon, String> colTong = new TableColumn<>("Tổng tiền");
        colTong.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getTongTien())));
        colTong.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: " + C_BLUE + ";");

        table.getColumns().add(colMa);
        table.getColumns().add(colNgay);
        table.getColumns().add(colDat);
        table.getColumns().add(colNV);
        table.getColumns().add(colPhong);
        table.getColumns().add(colDV);
        table.getColumns().add(colTong);
        VBox.setVgrow(table, Priority.ALWAYS);

        card.getChildren().add(table);
        return card;
    }

    private void loadData() {
        List<HoaDon> list = dao.getAll();
        masterData.setAll(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        double tongDoanhThu = list.stream().mapToDouble(HoaDon::getTongTien).sum();
        lblTongDoanhThu.setText(String.format("%,.0f đ", tongDoanhThu));
        lblSoHoaDon.setText(String.valueOf(list.size()));
    }

    private void applyFilter(String kw) {
        String filter = kw == null ? "" : kw.toLowerCase().trim();
        filteredData.setPredicate(hd -> {
            if (filter.isEmpty()) return true;
            if (hd.getMaHD().toLowerCase().contains(filter)) return true;
            if (hd.getDatPhong().getMaDat().toLowerCase().contains(filter)) return true;
            if (hd.getNhanVien() != null && hd.getNhanVien().getHoTen().toLowerCase().contains(filter)) return true;
            return false;
        });
    }

    private VBox createStatCard(String icon, String title, Label valueLbl, String accentHex) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setMinWidth(240);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER + "; -fx-background-radius: 10; -fx-border-radius: 10;");
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000008")));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane();
        Rectangle badgeBg = new Rectangle(40, 40);
        badgeBg.setArcWidth(8); badgeBg.setArcHeight(8);
        Color accent = Color.web(accentHex);
        badgeBg.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.1));
        badge.getChildren().addAll(badgeBg, new Label(icon));
        
        VBox text = new VBox(2);
        Label lblT = new Label(title);
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblT.setTextFill(Color.web(C_TEXT_GRAY));
        
        valueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLbl.setTextFill(accent);
        
        text.getChildren().addAll(lblT, valueLbl);
        topRow.getChildren().addAll(badge, text);
        card.getChildren().add(topRow);
        return card;
    }
}
