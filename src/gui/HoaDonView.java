package gui;

import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import model.entities.HoaDon;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private final ChiTietHoaDonDAO cthdDAO = new ChiTietHoaDonDAO();
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
                createStatCard("📄", "TỔNG SỐ HÓA ĐƠN", lblSoHoaDon, C_NAVY));

        // Thanh tìm kiếm & Lọc
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm kiếm theo mã hóa đơn, tên khách hoặc mã đặt phòng...");
        txtSearch.setPrefHeight(44);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER
                + "; -fx-padding: 0 16;");
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
        colNgay.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNgayTaoHD() != null
                ? p.getValue().getNgayTaoHD().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—"));
        colNgay.setMinWidth(140);
        colNgay.setStyle("-fx-alignment: CENTER;");

        TableColumn<HoaDon, String> colDat = new TableColumn<>("Mã đặt phòng");
        colDat.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDatPhong().getMaDat()));
        colDat.setMinWidth(120);
        colDat.setStyle("-fx-alignment: CENTER;");

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getDatPhong() != null &&
                p.getValue().getDatPhong().getKhachHang() != null
                ? p.getValue().getDatPhong().getKhachHang().getTenKH() : "—"));
        colKhach.setMinWidth(140);

        TableColumn<HoaDon, String> colNV = new TableColumn<>("Nhân viên lập");
        colNV.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getNhanVien() != null ? p.getValue().getNhanVien().getHoTen() : "—"));

        TableColumn<HoaDon, String> colTong = new TableColumn<>("Tổng thanh toán");
        colTong.setCellValueFactory(
                p -> new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getTongTien())));
        colTong.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTong.setMinWidth(160);

        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái TT");
        colTrangThai.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTenTrangThaiThanhToan()));
        colTrangThai.setStyle("-fx-alignment: CENTER;");

        TableColumn<HoaDon, String> colTienPhong = new TableColumn<>("Tiền phòng");
        colTienPhong.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getTienPhong())));
        colTienPhong.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<HoaDon, String> colTienCoc = new TableColumn<>("Tiền cọc");
        colTienCoc.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getTienCoc())));
        colTienCoc.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTienCoc.setMinWidth(120);

        TableColumn<HoaDon, String> colNgayThanhToan = new TableColumn<>("Ngày thanh toán");
        colNgayThanhToan.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNgayThanhToan() != null
                ? p.getValue().getNgayThanhToan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—"));
        colNgayThanhToan.setStyle("-fx-alignment: CENTER;");
        colNgayThanhToan.setMinWidth(140);

        table.getColumns().add(colMa);
        table.getColumns().add(colKhach);
        table.getColumns().add(colNgay);
        table.getColumns().add(colNgayThanhToan);
        table.getColumns().add(colTienPhong);
        table.getColumns().add(colTienCoc);
        table.getColumns().add(colTong);
        table.getColumns().add(colTrangThai);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setRowFactory(tv -> {
            TableRow<HoaDon> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showHoaDonDetail(row.getItem());
                }
            });
            return row;
        });

        // Add hint
        Label lblHint = new Label(
                "💡 Mẹo: Nháy đúp chuột vào một hóa đơn bất kỳ để xem chi tiết tiền phòng, dịch vụ...");
        lblHint.setTextFill(Color.web(C_TEXT_GRAY));
        lblHint.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 13));
        lblHint.setPadding(new Insets(10, 0, 0, 0));

        card.getChildren().addAll(table, lblHint);
        return card;
    }

    private void showHoaDonDetail(HoaDon hd) {
        // Lấy danh sách phòng đã trả từ DB
        java.util.List<Object[]> dsPhong = cthdDAO.getDanhSachPhongDaTra(hd.getMaHD());

        Stage detailStage = new Stage();
        detailStage.setTitle("Chi tiết hóa đơn: " + hd.getMaHD());
        detailStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: white;");
        root.setMinWidth(520);

        // Tiêu đề
        Label lblTitle = new Label("💳 Hóa đơn: " + hd.getMaHD());
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web(C_NAVY));

        // Thông tin khách hàng
        VBox khachBox = new VBox(8);
        khachBox.setPadding(new Insets(12));
        khachBox.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 8;");
        String tenKH = hd.getDatPhong() != null && hd.getDatPhong().getKhachHang() != null
                ? hd.getDatPhong().getKhachHang().getTenKH() : "—";
        String soDT = hd.getDatPhong() != null && hd.getDatPhong().getKhachHang() != null
                ? (hd.getDatPhong().getKhachHang().getSoDT() != null ? hd.getDatPhong().getKhachHang().getSoDT() : "—") : "—";
        String cccd = hd.getDatPhong() != null && hd.getDatPhong().getKhachHang() != null
                ? (hd.getDatPhong().getKhachHang().getSoCCCD() != null ? hd.getDatPhong().getKhachHang().getSoCCCD() : "—") : "—";
        String maDat = hd.getDatPhong() != null ? hd.getDatPhong().getMaDat() : "—";
        khachBox.getChildren().addAll(
            makeBillInfoRow("👤 Khách hàng:", tenKH),
            makeBillInfoRow("📞 Số điện thoại:", soDT),
            makeBillInfoRow("🆔 Số CCCD:", cccd),
            makeBillInfoRow("🏠 Mã đặt phòng:", maDat)
        );

        Label lblPhongHeader = new Label("🛏  Phòng đã trả");
        lblPhongHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblPhongHeader.setTextFill(Color.web(C_TEXT_DARK));

        VBox phongBox = new VBox(8);
        double tongTienPhong = 0;
        double tongCoc = 0;

        if (dsPhong.isEmpty()) {
            phongBox.getChildren().add(new Label("— Chưa có dữ liệu phòng"));
        } else {
            for (Object[] row : dsPhong) {
                String maPhong    = (String) row[0];
                String tenPhong   = (String) row[1];
                String loaiPhong  = (String) row[2];
                double sodem      = (double)  row[3];
                double thanhTien  = (double)  row[4];
                double giaCoc     = (double)  row[5];
                tongTienPhong += thanhTien;
                tongCoc       += giaCoc;

                HBox r = new HBox();
                r.setPadding(new Insets(8, 12, 8, 12));
                r.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 6;");
                Label lblP = new Label("🛏 " + maPhong + " - " + tenPhong + " (" + loaiPhong + ")"
                        + "  •  " + (int)sodem + " đêm");
                lblP.setFont(Font.font("Segoe UI", 13));
                HBox.setHgrow(lblP, Priority.ALWAYS);
                Label lblAmt = new Label(String.format("%,.0f đ", thanhTien));
                lblAmt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                lblAmt.setTextFill(Color.web(C_BLUE));
                r.getChildren().addAll(lblP, lblAmt);
                phongBox.getChildren().add(r);
            }
        }

        // Tóm tắt tiền
        Separator sep = new Separator();
        VBox sumBox = new VBox(10);
        sumBox.setPadding(new Insets(8, 0, 0, 0));

        double tienDV   = hd.getTienDV();
        double tienCoc  = tongCoc > 0 ? tongCoc : hd.getTienCoc();
        double tongTT   = hd.getTongTien() > 0 ? hd.getTongTien() : Math.max(0, tongTienPhong + tienDV - tienCoc);

        sumBox.getChildren().addAll(
            makeSumRow("Tiền phòng:",         String.format("%,.0f đ", tongTienPhong),  Color.web(C_TEXT_DARK)),
            makeSumRow("Tiền dịch vụ:",       String.format("%,.0f đ", tienDV),         Color.web(C_TEXT_DARK)),
            makeSumRow("Tiền cọc (đã khấu trừ):", String.format("- %,.0f đ", tienCoc),   Color.web(C_GREEN))
        );

        HBox totalRow = new HBox();
        Label lblTotalText = new Label("⭐ TỔNG THANH TOÁN:");
        lblTotalText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        HBox.setHgrow(lblTotalText, Priority.ALWAYS);
        Label lblTotalAmt = new Label(String.format("%,.0f đ", tongTT));
        lblTotalAmt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTotalAmt.setTextFill(Color.web(C_BLUE));
        totalRow.getChildren().addAll(lblTotalText, lblTotalAmt);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        sumBox.getChildren().addAll(sep, totalRow);

        // Nút đóng
        Button btnClose = new Button("✕  Đóng");
        btnClose.setPrefHeight(38);
        btnClose.setStyle("-fx-background-color: " + C_NAVY + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 20;");
        btnClose.setOnAction(e -> detailStage.close());
        HBox btnRow = new HBox(btnClose);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitle, khachBox, lblPhongHeader, phongBox, sumBox, btnRow);

        Scene scene = new Scene(root);
        detailStage.setScene(scene);
        detailStage.sizeToScene();
        detailStage.showAndWait();
    }

    private HBox makeBillInfoRow(String label, String value) {
        HBox hb = new HBox(8); hb.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label); l.setMinWidth(160); l.setTextFill(Color.web(C_TEXT_GRAY)); l.setFont(Font.font("Segoe UI", 13));
        Label v = new Label(value); v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13)); v.setTextFill(Color.web(C_TEXT_DARK));
        hb.getChildren().addAll(l, v); return hb;
    }

    private HBox makeSumRow(String label, String value, Color valColor) {
        HBox hb = new HBox(); hb.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label); l.setTextFill(Color.web(C_TEXT_GRAY)); l.setFont(Font.font("Segoe UI", 14)); HBox.setHgrow(l, Priority.ALWAYS);
        Label v = new Label(value); v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14)); v.setTextFill(valColor);
        hb.getChildren().addAll(l, v); return hb;
    }

    private void loadData() {
        List<HoaDon> list = dao.getAllWithKhachHang(); // JOIN tên khách
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
            // Tìm theo tên khách
            if (hd.getDatPhong().getKhachHang() != null
                    && hd.getDatPhong().getKhachHang().getTenKH() != null
                    && hd.getDatPhong().getKhachHang().getTenKH().toLowerCase().contains(filter)) return true;
            return false;
        });
    }

    private VBox createStatCard(String icon, String title, Label valueLbl, String accentHex) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setMinWidth(240);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER
                + "; -fx-background-radius: 10; -fx-border-radius: 10;");
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000008")));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane();
        Rectangle badgeBg = new Rectangle(40, 40);
        badgeBg.setArcWidth(8);
        badgeBg.setArcHeight(8);
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
