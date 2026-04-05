package gui;

import dao.DatPhongDAO;
import dao.DichVuSuDungDAO;
import model.entities.DatPhong;
import model.entities.DichVuSuDung;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CheckOutView – JavaFX
 * Thay thế CheckOutPanel (Swing).
 */
public class CheckOutView extends BorderPane {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_GREEN = "#16a34a";
    private static final String C_RED = "#dc2626";
    private static final String C_GOLD = "#d97706";

    /* ── DAO ────────────────────────────────────────────────────────── */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final DichVuSuDungDAO dvsdDAO = new DichVuSuDungDAO();

    /* ── Controls ───────────────────────────────────────────────────── */
    private TextField txtSearch;
    private FlowPane quickCheckOutFlow;
    private VBox guestInfoSection;
    private TableView<DichVuSuDung> serviceTable;
    private VBox billingSection;

    // Data hiện tại
    private DatPhong currentDatPhong;
    private String currentMaHD;
    private double currentTienPhong = 0;
    private double currentTienDV = 0;
    private double currentLateFee = 0;

    public CheckOutView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildMainLayout());

        resetView();
    }

    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 24, 0));

        VBox titleBox = new VBox(4);
        Label lblTitle = new Label("Thủ tục trả phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSubtitle = new Label("Thực hiện thủ tục thanh toán và trả phòng cho khách");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Nhập mã phòng đang sử dụng...");
        txtSearch.setPrefHeight(48);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER
                + "; -fx-padding: 0 16;");
        txtSearch.setOnAction(e -> handleSearch());

        Button btnSearch = new Button("🔍  Tìm kiếm");
        btnSearch.setPrefHeight(48);
        btnSearch.setMinWidth(140);
        styleButton(btnSearch, C_BLUE, "white", C_BLUE_HOVER);
        btnSearch.setOnAction(e -> handleSearch());

        searchRow.getChildren().addAll(txtSearch, btnSearch);

        VBox quickBox = new VBox(8);
        Label lblQuick = new Label("Phòng đang trả hôm nay:");
        lblQuick.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblQuick.setTextFill(Color.web(C_TEXT_GRAY));

        quickCheckOutFlow = new FlowPane(10, 10);
        loadQuickSuggestions();

        quickBox.getChildren().addAll(lblQuick, quickCheckOutFlow);

        header.getChildren().addAll(titleBox, searchRow, quickBox);
        return header;
    }

    private HBox buildMainLayout() {
        HBox main = new HBox(24);

        // CỘT TRÁI: Thông tin & Dịch vụ
        VBox leftCol = new VBox(24);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Thông tin khách
        guestInfoSection = new VBox(16);
        guestInfoSection.setPadding(new Insets(24));
        guestInfoSection.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        guestInfoSection.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        // Bảng dịch vụ
        VBox tableContainer = new VBox(12);
        tableContainer.setPadding(new Insets(24));
        tableContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        Label lblTable = new Label("Dịch vụ đã sử dụng");
        lblTable.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<DichVuSuDung, String> colNgay = new TableColumn<>("Ngày");
        colNgay.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNgaySuDung().toString()));

        TableColumn<DichVuSuDung, String> colTen = new TableColumn<>("Dịch vụ");
        colTen.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDichVu().getTenDV()));

        TableColumn<DichVuSuDung, String> colSl = new TableColumn<>("SL");
        colSl.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getSoLuong())));
        colSl.setMaxWidth(60);
        colSl.setStyle("-fx-alignment: CENTER;");

        TableColumn<DichVuSuDung, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(p -> new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getGiaDV())));
        colGia.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<DichVuSuDung, String> colTong = new TableColumn<>("Thành tiền");
        colTong.setCellValueFactory(
                p -> new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getThanhTien())));
        colTong.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        serviceTable.getColumns().add(colNgay);
        serviceTable.getColumns().add(colTen);
        serviceTable.getColumns().add(colSl);
        serviceTable.getColumns().add(colGia);
        serviceTable.getColumns().add(colTong);
        VBox.setVgrow(serviceTable, Priority.ALWAYS);

        tableContainer.getChildren().addAll(lblTable, serviceTable);
        leftCol.getChildren().addAll(guestInfoSection, tableContainer);

        // CỘT PHẢI: Thanh toán
        billingSection = new VBox(20);
        billingSection.setMinWidth(380);
        billingSection.setPadding(new Insets(24));
        billingSection.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        billingSection.setEffect(new DropShadow(15, 0, 5, Color.web("#0000000A")));

        main.getChildren().addAll(leftCol, billingSection);
        return main;
    }

    private void loadQuickSuggestions() {
        quickCheckOutFlow.getChildren().clear();
        // Giả lập danh sách phòng cần trả hôm nay (Trong thực tế query từ DB)
        String[] suggestions = { "P101", "P205", "P302" };
        for (String s : suggestions) {
            Button b = new Button(s);
            b.setCursor(Cursor.HAND);
            b.setStyle("-fx-background-color: white; -fx-border-color: " + C_GOLD + "; -fx-text-fill: " + C_GOLD
                    + "; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 4 12;");
            b.setOnAction(e -> {
                txtSearch.setText(s);
                handleSearch();
            });
            quickCheckOutFlow.getChildren().add(b);
        }
    }

    private void handleSearch() {
        String maPhong = txtSearch.getText().trim();
        if (maPhong.isEmpty())
            return;

        currentMaHD = datPhongDAO.getMaHDByMaPhong(maPhong);
        if (currentMaHD != null) {
            // Lấy thông tin đợt đặt
            currentDatPhong = datPhongDAO.findDatPhongDetail(maPhong); // Logic query theo mã phòng
            if (currentDatPhong != null) {
                updateDetailUI();
                loadServices();
                calculateBilling();
                updateBillingUI();
            }
        } else {
            // Thông thường là do phòng mang trạng thái Trống hoặc Bảo trì
            resetView();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Không tìm thấy khách đang ở tại phòng này");
            alert.showAndWait();
        }
    }

    private void updateDetailUI() {
        guestInfoSection.getChildren().clear();
        Label lblT = new Label("Thông tin đặt phòng");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblT.setTextFill(Color.web(C_NAVY));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);
        grid.add(createLabel("Khách hàng:"), 0, 0);
        grid.add(createValue(currentDatPhong.getKhachHang().getTenKH()), 1, 0);
        grid.add(createLabel("Số điện thoại:"), 0, 1);
        grid.add(createValue(currentDatPhong.getKhachHang().getSoDT()), 1, 1);
        grid.add(createLabel("Ngày nhận:"), 2, 0);
        grid.add(createValue(currentDatPhong.getNgayCheckIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                3, 0);
        grid.add(createLabel("Ngày trả dự kiến:"), 2, 1);
        grid.add(createValue(currentDatPhong.getNgayCheckOut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                3, 1);

        guestInfoSection.getChildren().addAll(lblT, grid);
    }

    private void loadServices() {
        List<DichVuSuDung> list = dvsdDAO.findByMaHD(currentMaHD);
        serviceTable.setItems(FXCollections.observableArrayList(list));
        currentTienDV = list.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();
    }

    private void calculateBilling() {
        // Giả sử lấy giá phòng từ booking hoặc query từ database.
        // Ở đây giả định một giá trị để demo.
        double donGiaP = 1200000;

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(currentDatPhong.getNgayCheckIn(), now);
        long nights = Math.max(1, duration.toDays());

        currentTienPhong = nights * donGiaP;

        // Phụ phí trả muộn (Logic demo)
        int hour = now.getHour();
        if (hour >= 18)
            currentLateFee = donGiaP; // 100%
        else if (hour >= 15)
            currentLateFee = donGiaP * 0.5; // 50%
        else if (hour >= 12)
            currentLateFee = donGiaP * 0.3; // 30%
        else
            currentLateFee = 0;
    }

    private void updateBillingUI() {
        billingSection.getChildren().clear();
        Label lblT = new Label("Chi tiết hóa đơn");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblT.setTextFill(Color.web(C_TEXT_DARK));

        VBox rows = new VBox(12);
        rows.getChildren().addAll(
                createBillRow("Tiền phòng (" + (long) (currentTienPhong / 1200000) + " đêm)", currentTienPhong,
                        Color.web(C_TEXT_DARK)),
                createBillRow("Tiền dịch vụ", currentTienDV, Color.web(C_TEXT_DARK)),
                createBillRow("Phụ phí trả muộn", currentLateFee, Color.web(C_RED)),
                createBillRow("Tiền đã cọc", -0.0, Color.web(C_GREEN)) // Giả định cọc
        );

        Separator sep = new Separator();
        double tong = currentTienPhong + currentTienDV + currentLateFee;

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotal = new Label("TỔNG THANH TOÁN");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        HBox.setHgrow(lblTotal, Priority.ALWAYS);
        Label valTotal = new Label(String.format("%,.0f đ", tong));
        valTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valTotal.setTextFill(Color.web(C_BLUE));
        totalRow.getChildren().addAll(lblTotal, valTotal);

        Button btnPay = new Button("💳  XÁC NHẬN THANH TOÁN");
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setPrefHeight(50);
        styleButton(btnPay, C_NAVY, "white", "#1e3a8aEE");
        btnPay.setOnAction(e -> handleCheckOut());

        billingSection.getChildren().addAll(lblT, new Separator(), rows, sep, totalRow, new Region(), btnPay);
    }

    private void handleCheckOut() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Hoàn tất trả phòng cho khách " + currentDatPhong.getKhachHang().getTenKH() + "?");
        confirm.setContentText("Hóa đơn sẽ được lưu và trạng thái phòng sẽ chuyển sang 'Trống'.");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                // Trong thực tế sẽ gọi HoaDonDAO.insert(...) và PhongDAO.update(...)
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Thành công");
                success.setHeaderText("Đã thanh toán và trả phòng!");
                success.showAndWait();
                resetView();
            }
        });
    }

    private void resetView() {
        txtSearch.clear();
        currentDatPhong = null;
        currentMaHD = null;
        guestInfoSection.getChildren().clear();
        guestInfoSection.getChildren().add(new Label("Chưa có thông tin phòng"));
        serviceTable.setItems(FXCollections.observableArrayList());
        billingSection.getChildren().clear();
        billingSection.getChildren().add(new Label("Vùi lòng chọn phòng để tính tiền"));
    }

    private HBox createBillRow(String label, double value, Color valColor) {
        HBox hb = new HBox();
        Label lbl = new Label(label);
        lbl.setTextFill(Color.web(C_TEXT_GRAY));
        HBox.setHgrow(lbl, Priority.ALWAYS);
        Label val = new Label(String.format("%,.0f đ", value));
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        val.setTextFill(valColor);
        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web(C_TEXT_GRAY));
        l.setFont(Font.font("Segoe UI", 13));
        return l;
    }

    private Label createValue(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        l.setTextFill(Color.web(C_TEXT_DARK));
        return l;
    }

    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        String base = "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 20; -fx-cursor: hand; -fx-font-weight: bold;";
        String hover = base.replace("-fx-background-color: " + bg, "-fx-background-color: " + hoverBg);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
}
