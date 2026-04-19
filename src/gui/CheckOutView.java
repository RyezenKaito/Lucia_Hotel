package gui;

import dao.DatPhongDAO;
import dao.DichVuSuDungDAO;
import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import dao.PhongDAO;
import dao.ChiTietDatPhongDAO;
import model.entities.DatPhong;
import model.entities.DichVuSuDung;
import model.entities.HoaDon;
import model.entities.NhanVien;
import model.enums.PhuongThucThanhToan;

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
 * CheckOutView – Thủ tục trả phòng.
 * Hỗ trợ 2 mode:
 * - Theo phòng: nhập mã phòng, checkout phòng đó, các phòng còn lại vẫn ở
 * - Theo đơn: nhập mã đặt, checkout tất cả / 1 số phòng trong đơn
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
    private static final double VAT_RATE = 0.1; // Default 10%
    private static final String C_RED = "#dc2626";
    private static final String C_GOLD = "#d97706";

    /* ── DAO ────────────────────────────────────────────────────────── */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final DichVuSuDungDAO dvsdDAO = new DichVuSuDungDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChiTietHoaDonDAO cthdDAO = new ChiTietHoaDonDAO();
    private final PhongDAO phongDAO = new PhongDAO();
    private final ChiTietDatPhongDAO ctdpDAO = new ChiTietDatPhongDAO();

    /* ── Controls ───────────────────────────────────────────────────── */
    private TextField txtSearch;
    private ToggleButton btnModePhong, btnModeDon;
    private FlowPane quickCheckOutFlow;
    private VBox guestInfoSection;
    private TableView<DichVuSuDung> serviceTable;
    private VBox billingSection;

    /* ── State hiện tại (search theo phòng) ────────────────────────── */
    private DatPhong currentDatPhong;
    private String currentMaPhong;
    private String currentMaCTDP;
    private double currentGiaPhong = 0;
    private double currentTienPhong = 0;
    private double currentTienDV = 0;
    private double currentTienCoc = 0;
    private double currentLateFee = 0;
    private double currentTongTien = 0;
    private long currentSoDem = 0;

    /* ── State (search theo đơn) ────────────────────────────────────── */
    private List<Object[]> currentRoomList; // [{maCTDP, maPhong, giaCoc, giaPhong}]

    /* ── Chế độ hiện tại ────────────────────────────────────────────── */
    private boolean modeByDon = false; // false=theo phòng, true=theo đơn

    private NhanVien staff;
    private Label lblQuickSuggestTitle;

    public CheckOutView() {
        this(null);
    }

    public CheckOutView(NhanVien staff) {
        this.staff = staff;
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildMainLayout());

        resetView();
    }

    /* ── HEADER ─────────────────────────────────────────────────────── */
    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 24, 0));

        VBox titleBox = new VBox(4);
        Label lblTitle = new Label("Thủ tục trả phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSub = new Label("Thanh toán và trả phòng cho khách – theo từng phòng hoặc toàn đơn");
        lblSub.setFont(Font.font("Segoe UI", 14));
        lblSub.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        // Toggle mode
        ToggleGroup modeGroup = new ToggleGroup();
        btnModePhong = new ToggleButton("🛏  Theo phòng");
        btnModeDon = new ToggleButton("📋  Theo đơn đặt");
        btnModePhong.setToggleGroup(modeGroup);
        btnModeDon.setToggleGroup(modeGroup);
        btnModePhong.setSelected(true);
        applyToggleStyle(btnModePhong, true);
        applyToggleStyle(btnModeDon, false);

        btnModePhong.setOnAction(e -> {
            if (btnModePhong.isSelected()) {
                modeByDon = false;
                applyToggleStyle(btnModePhong, true);
                applyToggleStyle(btnModeDon, false);
                txtSearch.setPromptText("Nhập mã phòng đang sử dụng...");
                resetView();
                loadQuickSuggestions();
            } else {
                btnModePhong.setSelected(true);
            }
        });
        btnModeDon.setOnAction(e -> {
            if (btnModeDon.isSelected()) {
                modeByDon = true;
                applyToggleStyle(btnModePhong, false);
                applyToggleStyle(btnModeDon, true);
                txtSearch.setPromptText("Nhập mã đặt phòng (VD: DP001)...");
                resetView();
                loadQuickSuggestions();
            } else {
                btnModeDon.setSelected(true);
            }
        });

        HBox modeRow = new HBox(8);
        modeRow.setAlignment(Pos.CENTER_LEFT);
        modeRow.getChildren().addAll(new Label("Chế độ:"), btnModePhong, btnModeDon);
        ((Label) modeRow.getChildren().get(0)).setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        ((Label) modeRow.getChildren().get(0)).setTextFill(Color.web(C_TEXT_GRAY));

        // Search
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        txtSearch = new TextField();
        txtSearch.setPromptText("Nhập mã phòng đang sử dụng...");
        txtSearch.setPrefHeight(48);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: "
                + C_BORDER + "; -fx-padding: 0 16;");
        txtSearch.setOnAction(e -> handleSearch());

        Button btnSearch = new Button("🔍  Tìm kiếm");
        btnSearch.setPrefHeight(48);
        btnSearch.setMinWidth(140);
        styleButton(btnSearch, C_BLUE, "white", C_BLUE_HOVER);
        btnSearch.setOnAction(e -> handleSearch());
        searchRow.getChildren().addAll(txtSearch, btnSearch);

        // Quick suggestions
        VBox quickBox = new VBox(8);
        lblQuickSuggestTitle = new Label("Phòng đang có khách:");
        lblQuickSuggestTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblQuickSuggestTitle.setTextFill(Color.web(C_TEXT_GRAY));
        quickCheckOutFlow = new FlowPane(10, 10);
        loadQuickSuggestions();
        quickBox.getChildren().addAll(lblQuickSuggestTitle, quickCheckOutFlow);

        header.getChildren().addAll(titleBox, modeRow, searchRow, quickBox);
        return header;
    }

    /* ── MAIN LAYOUT ────────────────────────────────────────────────── */
    private HBox buildMainLayout() {
        HBox main = new HBox(24);

        guestInfoSection = new VBox(16);
        guestInfoSection.setPadding(new Insets(24));
        guestInfoSection.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        guestInfoSection.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        VBox tableContainer = new VBox(12);
        tableContainer.setPadding(new Insets(24));
        tableContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        Label lblTable = new Label("Dịch vụ đã sử dụng");
        lblTable.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        serviceTable.setPlaceholder(new Label("Không có dữ liệu"));

        TableColumn<DichVuSuDung, String> colNgay = new TableColumn<>("Ngày");
        colNgay.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getNgaySuDung() != null ? p.getValue().getNgaySuDung().toString() : ""));
        TableColumn<DichVuSuDung, String> colTen = new TableColumn<>("Dịch vụ");
        colTen.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getDichVu() != null ? p.getValue().getDichVu().getTenDV() : ""));
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
        for (TableColumn<DichVuSuDung, ?> c : serviceTable.getColumns()) {
            c.setReorderable(false);
        }
        VBox.setVgrow(serviceTable, Priority.ALWAYS);
        tableContainer.getChildren().addAll(lblTable, serviceTable);

        VBox leftContent = new VBox(24);
        leftContent.getChildren().addAll(guestInfoSection, tableContainer);

        ScrollPane scrollLeft = new ScrollPane(leftContent);
        scrollLeft.setFitToWidth(true);
        scrollLeft.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollLeft.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        HBox.setHgrow(scrollLeft, Priority.ALWAYS);

        billingSection = new VBox(20);
        billingSection.setMinWidth(380);
        billingSection.setPadding(new Insets(24));
        billingSection.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER + ";");
        billingSection.setEffect(new DropShadow(15, 0, 5, Color.web("#0000000A")));

        main.getChildren().addAll(scrollLeft, billingSection);
        return main;
    }

    /* ── Quick Suggestions ──────────────────────────────────────────── */
    private void loadQuickSuggestions() {
        quickCheckOutFlow.getChildren().clear();
        List<String> list;

        if (modeByDon) {
            if (lblQuickSuggestTitle != null)
                lblQuickSuggestTitle.setText("Hóa đơn/Đơn đặt phòng đang có khách:");
            list = datPhongDAO.getDonDangSuDung();
            if (list.isEmpty()) {
                Label lbl = new Label("Không có đơn đặt phòng nào chưa thanh toán");
                lbl.setFont(Font.font("Segoe UI", 12));
                lbl.setTextFill(Color.web(C_TEXT_GRAY));
                quickCheckOutFlow.getChildren().add(lbl);
                return;
            }
        } else {
            if (lblQuickSuggestTitle != null)
                lblQuickSuggestTitle.setText("Phòng đang có khách:");
            list = datPhongDAO.getPhongDangSuDung();
            if (list.isEmpty()) {
                Label lbl = new Label("Không có phòng nào đang sử dụng");
                lbl.setFont(Font.font("Segoe UI", 12));
                lbl.setTextFill(Color.web(C_TEXT_GRAY));
                quickCheckOutFlow.getChildren().add(lbl);
                return;
            }
        }
        for (String s : list) {
            Button b = new Button(s);
            b.setCursor(Cursor.HAND);
            b.setStyle("-fx-background-color: white; -fx-border-color: " + C_GOLD + "; -fx-text-fill: " + C_GOLD
                    + "; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 4 12; -fx-font-weight: bold;");
            b.setOnAction(e -> {
                txtSearch.setText(s);
                handleSearch();
            });
            quickCheckOutFlow.getChildren().add(b);
        }
    }

    /* ── Search Handler ─────────────────────────────────────────────── */
    private void handleSearch() {
        String q = txtSearch.getText().trim().toUpperCase();
        if (q.isEmpty())
            return;

        if (modeByDon) {
            handleSearchByDon(q);
        } else {
            handleSearchByPhong(q);
        }
    }

    /** Tìm theo mã phòng (mode cũ) */
    private void handleSearchByPhong(String maPhong) {
        Object[] info = datPhongDAO.findCheckOutInfoByMaPhong(maPhong);
        if (info != null) {
            currentDatPhong = (DatPhong) info[0];
            currentTienCoc = (double) info[1];
            currentGiaPhong = (double) info[2];
            currentMaPhong = (String) info[3];
            currentMaCTDP = findMaCTDPByMaPhong(currentDatPhong.getMaDat(), maPhong);

            updateDetailUI_Phong();
            loadServices();
            calculateBilling();
            updateBillingUI(false);
        } else {
            boolean stuck = checkStuckRoom(maPhong);
            if (stuck) {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setHeaderText("Phòng \"" + maPhong + "\" đang ở trạng thái 'Đang sử dụng' nhưng không có đơn.");
                a.setContentText("Bạn có muốn giải phóng phòng này về 'Còn trống' không?");
                a.showAndWait().ifPresent(t -> {
                    if (t == ButtonType.OK) {
                        phongDAO.updateTrangThai(maPhong, "BAN");
                        resetView();
                        loadQuickSuggestions();
                    }
                });
            } else {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Không tìm thấy khách đang ở tại phòng " + maPhong);
                a.setContentText("Phòng không đang được sử dụng hoặc không tồn tại.");
                a.showAndWait();
            }
        }
    }

    /** Tìm theo mã đơn – hiển thị tất cả phòng đang DANGSUDUNG trong đơn */
    @SuppressWarnings("unchecked")
    private void handleSearchByDon(String maDat) {
        Object[] result = datPhongDAO.findCheckOutInfoByMaDat(maDat);
        if (result == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Không tìm thấy đơn đặt phòng: " + maDat);
            a.setContentText("Đơn không tồn tại hoặc đã được checkout hoàn toàn.");
            a.showAndWait();
            return;
        }
        currentDatPhong = (DatPhong) result[0];
        currentRoomList = (List<Object[]>) result[1];

        if (currentRoomList.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Đơn " + maDat + " không có phòng nào đang sử dụng.");
            a.showAndWait();
            return;
        }

        updateDetailUI_Don();
        updateBillingUI_Don();
    }

    /* ── Detail UI ──────────────────────────────────────────────────── */
    private void updateDetailUI_Phong() {
        guestInfoSection.getChildren().clear();
        Label lblT = new Label("Thông tin đặt phòng");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblT.setTextFill(Color.web(C_NAVY));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);

        grid.add(createLabel("Mã đặt phòng:"), 0, 0);
        grid.add(createValue(currentDatPhong.getMaDat()), 1, 0);
        grid.add(createLabel("Phòng:"), 2, 0);
        Label lblRoomVal = createValue(currentMaPhong + " — " + String.format("%,.0f đ/đêm", currentGiaPhong));
        lblRoomVal.setWrapText(true);
        grid.add(lblRoomVal, 3, 0);

        grid.add(createLabel("Khách hàng:"), 0, 1);
        Label lblKhVal = createValue(currentDatPhong.getKhachHang().getTenKH());
        lblKhVal.setWrapText(true);
        grid.add(lblKhVal, 1, 1);
        grid.add(createLabel("Số điện thoại:"), 2, 1);
        grid.add(createValue(currentDatPhong.getKhachHang().getSoDT() != null
                ? currentDatPhong.getKhachHang().getSoDT()
                : "—"), 3, 1);

        grid.add(createLabel("Ngày nhận:"), 0, 2);
        grid.add(createValue(currentDatPhong.getNgayCheckIn() != null
                ? currentDatPhong.getNgayCheckIn().format(fmt)
                : "—"), 1, 2);
        grid.add(createLabel("Ngày trả dự kiến:"), 2, 2);
        grid.add(createValue(currentDatPhong.getNgayCheckOut() != null
                ? currentDatPhong.getNgayCheckOut().format(fmt)
                : "—"), 3, 2);

        grid.add(createLabel("Tiền cọc (phòng):"), 0, 3);
        Label lblCoc = createValue(String.format("%,.0f đ", currentTienCoc));
        lblCoc.setTextFill(Color.web(C_GREEN));
        grid.add(lblCoc, 1, 3);

        guestInfoSection.getChildren().addAll(lblT, grid);
    }

    private void updateDetailUI_Don() {
        guestInfoSection.getChildren().clear();
        Label lblT = new Label("Thông tin đơn – Chọn phòng để checkout");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblT.setTextFill(Color.web(C_NAVY));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);
        grid.add(createLabel("Mã đặt phòng:"), 0, 0);
        grid.add(createValue(currentDatPhong.getMaDat()), 1, 0);
        grid.add(createLabel("Khách hàng:"), 0, 1);
        Label lblKhValDon = createValue(currentDatPhong.getKhachHang().getTenKH());
        lblKhValDon.setWrapText(true);
        grid.add(lblKhValDon, 1, 1);
        grid.add(createLabel("Ngày check-in:"), 0, 2);
        grid.add(createValue(currentDatPhong.getNgayCheckIn() != null
                ? currentDatPhong.getNgayCheckIn().format(fmt)
                : "—"), 1, 2);

        // Danh sách phòng
        VBox roomsBox = new VBox(10);
        roomsBox.setPadding(new Insets(12, 0, 0, 0));
        Label lblRooms = new Label("Các phòng được checkout (tất cả):");
        lblRooms.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblRooms.setTextFill(Color.web(C_TEXT_DARK));

        FlowPane flowRooms = new FlowPane(10, 10);
        for (Object[] room : currentRoomList) {
            String maPhong = (String) room[1];
            Label lblP = new Label(maPhong);
            lblP.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblP.setStyle(
                    "-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-padding: 6 12; -fx-background-radius: 6;");
            flowRooms.getChildren().add(lblP);
        }

        roomsBox.getChildren().addAll(lblRooms, flowRooms);

        guestInfoSection.getChildren().addAll(lblT, grid, new Separator(), roomsBox);
    }

    /* ── Service loading ────────────────────────────────────────────── */
    private void loadServices() {
        if (currentMaCTDP != null) {
            List<DichVuSuDung> list = dvsdDAO.findByMaCTDP(currentMaCTDP);
            serviceTable.setItems(FXCollections.observableArrayList(list));
            currentTienDV = list.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();
        } else {
            serviceTable.setItems(FXCollections.observableArrayList());
            currentTienDV = 0;
        }
    }

    /* ── Billing Calculation ────────────────────────────────────────── */
    private void calculateBilling() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkIn = currentDatPhong.getNgayCheckIn();
        if (checkIn != null) {
            Duration dur = Duration.between(checkIn, now);
            currentSoDem = Math.max(1, dur.toDays());
        } else {
            currentSoDem = 1;
        }

        currentTienPhong = currentSoDem * currentGiaPhong;

        currentLateFee = 0;
        if (currentDatPhong.getNgayCheckOut() != null && now.isAfter(currentDatPhong.getNgayCheckOut())) {
            int hour = now.getHour();
            if (hour >= 18)
                currentLateFee = currentGiaPhong;
            else if (hour >= 15)
                currentLateFee = currentGiaPhong * 0.5;
            else if (hour >= 12)
                currentLateFee = currentGiaPhong * 0.3;
        }

        double base = currentTienPhong + currentLateFee + currentTienDV;
        currentTongTien = base * (1 + VAT_RATE) - currentTienCoc;
    }

    private void calculateBilling_Don() {
        if (currentDatPhong == null || currentRoomList == null)
            return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkIn = currentDatPhong.getNgayCheckIn();
        long soDem = checkIn != null ? Math.max(1, Duration.between(checkIn, now).toDays()) : 1;
        currentSoDem = soDem;

        double lateFeeRate = 0;
        if (currentDatPhong.getNgayCheckOut() != null && now.isAfter(currentDatPhong.getNgayCheckOut())) {
            int hour = now.getHour();
            if (hour >= 18)
                lateFeeRate = 1;
            else if (hour >= 15)
                lateFeeRate = 0.5;
            else if (hour >= 12)
                lateFeeRate = 0.3;
        }

        currentTienPhong = 0;
        currentTienCoc = 0;
        currentLateFee = 0;

        List<DichVuSuDung> listDV = new java.util.ArrayList<>();

        for (Object[] room : currentRoomList) {
            String maCTDP = (String) room[0];
            double giaCoc = (double) room[2];
            double giaPhong = (double) room[3];

            currentTienPhong += giaPhong * soDem;
            currentTienCoc += giaCoc;
            currentLateFee += giaPhong * lateFeeRate;

            List<DichVuSuDung> dvRoom = dvsdDAO.findByMaCTDP(maCTDP);
            if (dvRoom != null) {
                listDV.addAll(dvRoom);
            }
        }

        serviceTable.setItems(FXCollections.observableArrayList(listDV));
        currentTienDV = listDV.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();

        double base = currentTienPhong + currentLateFee + currentTienDV;
        currentTongTien = base * (1 + VAT_RATE) - currentTienCoc;
    }

    /* ── Billing UI (mode theo phòng) ──────────────────────────────── */
    private void updateBillingUI(boolean isDon) {
        billingSection.getChildren().clear();
        Label lblT = new Label("Chi tiết hóa đơn");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblT.setTextFill(Color.web(C_TEXT_DARK));

        VBox rows = new VBox(12);
        rows.getChildren().addAll(
                createBillRow("Tiền phòng (" + currentSoDem + " đêm × " +
                        String.format("%,.0f", currentGiaPhong) + " đ)", currentTienPhong, Color.web(C_TEXT_DARK)),
                createBillRow("Tiền dịch vụ", currentTienDV, Color.web(C_TEXT_DARK)),
                createBillRow("Phụ phí trả muộn", currentLateFee,
                        currentLateFee > 0 ? Color.web(C_RED) : Color.web(C_TEXT_GRAY)),
                createBillRow("Tiền đã cọc (trừ)", -currentTienCoc, Color.web(C_GREEN)),
                createBillRow(String.format("Thuế VAT (%.0f%%)", VAT_RATE * 100),
                        (Math.max(0, currentTienPhong + currentLateFee - currentTienCoc) + currentTienDV) * VAT_RATE,
                        Color.web(C_TEXT_DARK)));

        HBox totalRow = new HBox();
        totalRow.setMaxWidth(Double.MAX_VALUE);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotal = new Label("TỔNG THANH TOÁN");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTotal.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTotal, Priority.ALWAYS);
        Label valTotal = new Label(String.format("%,.0f đ", currentTongTien));
        valTotal.setMinWidth(Region.USE_PREF_SIZE);
        valTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valTotal.setTextFill(Color.web(C_BLUE));
        totalRow.getChildren().addAll(lblTotal, valTotal);

        List<String> phongDaTra = getPhongDaTraByMaDat(currentDatPhong.getMaDat());
        VBox ptBox = new VBox(4);
        Label lblPt = new Label("Các phòng đã trả trước đó:");
        lblPt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPt.setTextFill(Color.web(C_TEXT_GRAY));
        Label valDaTra = new Label(phongDaTra.isEmpty() ? "Chưa có phòng nào" : String.join(", ", phongDaTra));
        valDaTra.setFont(Font.font("Segoe UI", 14));
        valDaTra.setTextFill(Color.web(phongDaTra.isEmpty() ? C_TEXT_GRAY : C_GREEN));
        ptBox.getChildren().addAll(lblPt, valDaTra);

        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(0, 10, 0, 0));
        scrollContent.getChildren().addAll(rows, new Separator(), totalRow, ptBox);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button btnPay = new Button("💳  XÁC NHẬN THANH TOÁN VÀ TRẢ PHÒNG");
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setPrefHeight(50);
        btnPay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        styleButton(btnPay, C_NAVY, "white", "#1e3a8aEE");
        btnPay.setOnAction(e -> handleCheckOut_Phong());

        billingSection.getChildren().addAll(lblT, new Separator(), scrollPane, btnPay);
    }

    /** Billing UI cho mode theo đơn: tổng hợp các phòng được tick */
    private void updateBillingUI_Don() {
        calculateBilling_Don();

        billingSection.getChildren().clear();
        Label lblT = new Label("Thanh toán theo đơn");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblT.setTextFill(Color.web(C_TEXT_DARK));
        Label lblHint = new Label("Kiểm tra hóa đơn và thanh toán toàn bộ đơn đặt phòng.");
        lblHint.setFont(Font.font("Segoe UI", 13));
        lblHint.setTextFill(Color.web(C_TEXT_GRAY));
        lblHint.setWrapText(true);

        VBox rows = new VBox(12);
        rows.getChildren().addAll(
                createBillRow("Tiền phòng (" + currentSoDem + " đêm)", currentTienPhong, Color.web(C_TEXT_DARK)),
                createBillRow("Tiền dịch vụ", currentTienDV, Color.web(C_TEXT_DARK)),
                createBillRow("Phụ phí trả muộn", currentLateFee,
                        currentLateFee > 0 ? Color.web(C_RED) : Color.web(C_TEXT_GRAY)),
                createBillRow("Tiền đã cọc (trừ)", -currentTienCoc, Color.web(C_GREEN)),
                createBillRow(String.format("Thuế VAT (%.0f%%)", VAT_RATE * 100),
                        (Math.max(0, currentTienPhong + currentLateFee - currentTienCoc) + currentTienDV) * VAT_RATE,
                        Color.web(C_TEXT_DARK)));

        HBox totalRow = new HBox();
        totalRow.setMaxWidth(Double.MAX_VALUE);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotal = new Label("TỔNG THANH TOÁN TOÀN ĐƠN");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTotal.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTotal, Priority.ALWAYS);
        Label valTotal = new Label(String.format("%,.0f đ", currentTongTien));
        valTotal.setMinWidth(Region.USE_PREF_SIZE);
        valTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valTotal.setTextFill(Color.web(C_BLUE));
        totalRow.getChildren().addAll(lblTotal, valTotal);

        List<String> phongDaTra = getPhongDaTraByMaDat(currentDatPhong.getMaDat());
        VBox ptBox = new VBox(4);
        Label lblPt = new Label("Các phòng đã trả trước đó:");
        lblPt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPt.setTextFill(Color.web(C_TEXT_GRAY));
        Label valDaTra = new Label(phongDaTra.isEmpty() ? "Chưa có phòng nào" : String.join(", ", phongDaTra));
        valDaTra.setFont(Font.font("Segoe UI", 14));
        valDaTra.setTextFill(Color.web(phongDaTra.isEmpty() ? C_TEXT_GRAY : C_GREEN));
        ptBox.getChildren().addAll(lblPt, valDaTra);

        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(0, 10, 0, 0));
        scrollContent.getChildren().addAll(rows, new Separator(), totalRow, ptBox);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button btnPay = new Button("💳  THANH TOÁN TOÀN BỘ ĐƠN");
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setPrefHeight(50);
        btnPay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        styleButton(btnPay, C_NAVY, "white", "#1e3a8aEE");
        btnPay.setOnAction(e -> handleCheckOut_Don());

        billingSection.getChildren().addAll(lblT, lblHint, new Separator(), scrollPane, btnPay);
    }

    /* ── Checkout Logic – Theo Phòng ────────────────────────────────── */
    private void handleCheckOut_Phong() {
        if (currentDatPhong == null || currentMaPhong == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText(
                "Trả phòng " + currentMaPhong + " cho khách " + currentDatPhong.getKhachHang().getTenKH() + "?");
        confirm.setContentText("Tổng: " + String.format("%,.0f đ", currentTongTien)
                + "\n\nLưu ý: Các phòng khác trong đơn (nếu có) vẫn tiếp tục ở.");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                boolean ok = performCheckOut_Phong();
                if (ok) {
                    Alert s = new Alert(Alert.AlertType.INFORMATION);
                    s.setTitle("Thành công");
                    s.setHeaderText("Đã thanh toán và trả phòng " + currentMaPhong);
                    s.setContentText(
                            "Phòng: " + currentMaPhong + "\nKhách: " + currentDatPhong.getKhachHang().getTenKH()
                                    + "\nTổng: " + String.format("%,.0f đ", currentTongTien));
                    s.showAndWait();
                    resetView();
                    loadQuickSuggestions();
                    serviceTable.getItems().clear();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Lưu dữ liệu thất bại. Vui lòng thử lại.").showAndWait();
                }
            }
        });
    }

    private boolean performCheckOut_Phong() {
        try {
            double finalTienPhong = currentTienPhong + currentLateFee;

            // 1. Lấy HoaDon của đơn
            HoaDon hd = hoaDonDAO.getByMaDat(currentDatPhong.getMaDat());
            if (hd == null) {
                // Đề phòng trường hợp chưa tạo (dữ liệu cũ), tạo mới
                hd = new HoaDon();
                hd.setMaHD(hoaDonDAO.generateMaHD());
                hd.setDatPhong(currentDatPhong);
                hd.setNhanVien(staff != null ? staff : new NhanVien("ADMIN"));
                hd.setNgayTaoHD(LocalDateTime.now());
                hd.setTienPhong(0);
                hd.setTienDV(0);
                hd.setTienCoc(currentTienCoc);
                hd.setThueVAT(VAT_RATE);
                hd.setTongTien(0);
                hd.setTrangThaiThanhToan("CHUA_THANH_TOAN");
                hoaDonDAO.insert(hd);
            }

            // 2. Update ChiTietHoaDon cho phòng này
            if (currentMaCTDP != null) {
                String maCTHD = cthdDAO.getMaCTHDByMaCTDP(currentMaCTDP);
                if (maCTHD != null) {
                    cthdDAO.updateLuuTruVaTien(maCTHD, currentSoDem, finalTienPhong);
                } else {
                    maCTHD = cthdDAO.generateMaCTHD();
                    cthdDAO.insert(maCTHD, hd.getMaHD(), currentMaCTDP, currentSoDem, finalTienPhong);
                }
            }

            // 3. Update trạng thái phòng -> BAN
            if (!phongDAO.updateTrangThai(currentMaPhong, "BAN"))
                return false;

            // 4. Tính toán lại tổng tiền của HoaDon
            double currentSumPhong = hoaDonDAO.getTongTienPhongCurrent(hd.getMaHD());

            List<model.entities.DichVuSuDung> listDV = dvsdDAO.findByMaHD(hd.getMaHD());
            double totalTienDV = listDV.stream().mapToDouble(model.entities.DichVuSuDung::getThanhTien).sum();

            hd.setTienPhong(currentSumPhong);
            hd.setTienDV(totalTienDV);
            hd.setNgayTaoHD(LocalDateTime.now());

            // Sử dụng logic trung tâm từ DAO
            hoaDonDAO.tinhTongTien(hd);
            hoaDonDAO.tinhDoanhThu(hd);

            // 5. Nếu hết phòng trong đơn -> hoàn tất đơn & hóa đơn
            if (datPhongDAO.isAllRoomsCheckedOut(currentDatPhong.getMaDat())) {
                datPhongDAO.updateTrangThai(currentDatPhong.getMaDat(), "DA_CHECKOUT");
                hd.setTrangThaiThanhToan("DA_THANH_TOAN");
            }

            return hoaDonDAO.updateTongTien(hd);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ── Checkout Logic – Theo Đơn ──────────────────────────────────── */
    private void handleCheckOut_Don() {
        if (currentDatPhong == null || currentRoomList == null)
            return;

        java.util.List<Object[]> selected = currentRoomList;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Checkout toàn bộ " + selected.size() + " phòng còn lại cho khách "
                + currentDatPhong.getKhachHang().getTenKH() + "?");
        confirm.setContentText("Tổng tiền thanh toán đợt này: " + String.format("%,.0f đ", currentTongTien));
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                boolean ok = performCheckOut_Don(selected);
                if (ok) {
                    Alert s = new Alert(Alert.AlertType.INFORMATION);
                    s.setTitle("Thành công");
                    s.setHeaderText("✅ Đã checkout " + selected.size() + " phòng.");
                    s.showAndWait();
                    resetView();
                    loadQuickSuggestions();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Lưu dữ liệu thất bại. Vui lòng thử lại.").showAndWait();
                }
            }
        });
    }

    private boolean performCheckOut_Don(java.util.List<Object[]> selected) {
        try {
            LocalDateTime checkIn = currentDatPhong.getNgayCheckIn();
            LocalDateTime now = LocalDateTime.now();
            long soDem = checkIn != null ? Math.max(1, Duration.between(checkIn, now).toDays()) : 1;

            // 1. Lấy HoaDon
            HoaDon hd = hoaDonDAO.getByMaDat(currentDatPhong.getMaDat());
            if (hd == null) {
                hd = new HoaDon();
                hd.setMaHD(hoaDonDAO.generateMaHD());
                hd.setDatPhong(currentDatPhong);
                hd.setNhanVien(staff != null ? staff : new NhanVien("LUCIA001")); // Use a default staff ID if null, not
                                                                                  // ADMIN
                hd.setNgayTaoHD(now);
                hd.setTienPhong(0);
                hd.setTienDV(0);
                hd.setTienCoc(ctdpDAO.getTongCocByMaDat(currentDatPhong.getMaDat()));
                hd.setThueVAT(VAT_RATE);
                hoaDonDAO.tinhTongTien(hd);
                hoaDonDAO.tinhDoanhThu(hd);
                hoaDonDAO.insert(hd);
            }

            for (Object[] room : selected) {
                String maCTDP = (String) room[0];
                String maPhong = (String) room[1];
                double giaPhong = (double) room[3];
                double tienPhong = soDem * giaPhong;

                // 2. Update ChiTietHoaDon cho phòng này
                String maCTHD = cthdDAO.getMaCTHDByMaCTDP(maCTDP);
                if (maCTHD != null) {
                    cthdDAO.updateLuuTruVaTien(maCTHD, soDem, tienPhong);
                } else {
                    maCTHD = cthdDAO.generateMaCTHD();
                    cthdDAO.insert(maCTHD, hd.getMaHD(), maCTDP, soDem, tienPhong);
                }

                // 3. Update trạng thái phòng
                phongDAO.updateTrangThai(maPhong, "BAN");
            }

            // 4. Update HD
            double currentSumPhong = hoaDonDAO.getTongTienPhongCurrent(hd.getMaHD());
            // Dịch vụ của đơn lấy từ DB (nếu có updateDV function, hiện tại chỉ dùng
            // currentTienDV lúc load)
            List<DichVuSuDung> listDV = dvsdDAO.findByMaHD(hd.getMaHD());
            double tienDV = listDV.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();

            double subtotal = Math.max(0, currentSumPhong - hd.getTienCoc()) + tienDV;
            double newTongTien = subtotal * (1 + hd.getThueVAT());
            hd.setTienPhong(currentSumPhong);
            hd.setTienDV(tienDV);
            hd.setTongTien(newTongTien);
            hd.setNgayTaoHD(now);

            // 5. Kiểm tra nếu hết phòng → update đơn & hóa đơn
            if (datPhongDAO.isAllRoomsCheckedOut(currentDatPhong.getMaDat())) {
                datPhongDAO.updateTrangThai(currentDatPhong.getMaDat(), "DA_CHECKOUT");
                hd.setTrangThaiThanhToan("DA_THANH_TOAN");
            }
            return hoaDonDAO.updateTongTien(hd);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ── Helpers ─────────────────────────────────────────────────────── */

    private List<String> getPhongDaTraByMaDat(String maDat) {
        List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT p.maPhong FROM ChiTietDatPhong ctdp join Phong p on ctdp.maPhong=p.maPhong WHERE ctdp.maDat=? AND p.tinhTrang='CONTRONG'";
        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDat);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res.add(rs.getString(1));
            }
        } catch (Exception e) {
        }
        return res;
    }

    private String findMaCTDPByMaPhong(String maDat, String maPhong) {
        String sql = "SELECT maCTDP FROM ChiTietDatPhong WHERE maDat = ? AND maPhong = ?";
        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDat);
            ps.setString(2, maPhong);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkStuckRoom(String maPhong) {
        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
                java.sql.PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM Phong WHERE maPhong = ? AND tinhTrang = N'DANGSUDUNG'")) {
            ps.setString(1, maPhong);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

    private void resetView() {
        if (txtSearch != null)
            txtSearch.clear();
        currentDatPhong = null;
        currentMaPhong = null;
        currentMaCTDP = null;
        currentRoomList = null;
        currentGiaPhong = 0;
        currentTienPhong = 0;
        currentTienDV = 0;
        currentTienCoc = 0;
        currentLateFee = 0;
        currentTongTien = 0;
        currentSoDem = 0;

        if (guestInfoSection != null) {
            guestInfoSection.getChildren().clear();
            Label lbl = new Label("🛏  Vui lòng tìm phòng hoặc đơn để bắt đầu thủ tục trả phòng");
            lbl.setFont(Font.font("Segoe UI", 14));
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            guestInfoSection.getChildren().add(lbl);
        }
        if (serviceTable != null)
            serviceTable.setItems(FXCollections.observableArrayList());
        if (billingSection != null) {
            billingSection.getChildren().clear();
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            Label ico = new Label("💰");
            ico.setFont(Font.font(36));
            Label msg = new Label("Chọn phòng / đơn để xem hóa đơn");
            msg.setFont(Font.font("Segoe UI", 14));
            msg.setTextFill(Color.web(C_TEXT_GRAY));
            empty.getChildren().addAll(ico, msg);
            billingSection.getChildren().add(empty);
        }
    }

    private HBox createBillRow(String label, double value, Color valColor) {
        HBox hb = new HBox();
        hb.setMaxWidth(Double.MAX_VALUE);
        hb.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setTextFill(Color.web(C_TEXT_GRAY));
        lbl.setFont(Font.font("Segoe UI", 13));
        lbl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lbl, Priority.ALWAYS);
        Label val = new Label(String.format("%,.0f đ", value));
        val.setMinWidth(Region.USE_PREF_SIZE);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        val.setTextFill(valColor);
        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    private Label createLabel(String t) {
        Label l = new Label(t);
        l.setTextFill(Color.web(C_TEXT_GRAY));
        l.setFont(Font.font("Segoe UI", 13));
        return l;
    }

    private Label createValue(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        l.setTextFill(Color.web(C_TEXT_DARK));
        return l;
    }

    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        String base = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-weight: bold;";
        String hover = "-fx-background-color: " + hoverBg + "; -fx-text-fill: " + fg
                + "; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-weight: bold;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void applyToggleStyle(ToggleButton btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: " + C_NAVY
                    + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            btn.setStyle(
                    "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        }
    }
}