package gui;

import dao.DatPhongDAO;
import dao.ChiTietDatPhongDAO;
import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.PhongDAO;
import model.entities.*;
import connectDatabase.ConnectDatabase;
import java.sql.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.util.StringConverter;

/**
 * CheckInView – Thu tuc nhan phong.
 * Luong moi: tim don (ma dat / SDT / CCCD) -> hien thi phong da assign trong
 * CTDP -> xac nhan.
 * Khi xac nhan: cap nhat phong DANGSUDUNG + don DA_CHECKIN + tao 1 HoaDon cho
 * don.
 */
public class CheckInView extends BorderPane {

    /* -- Bang mau ---- */
    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_GREEN = "#16a34a";

    /* -- DAO -- */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final ChiTietDatPhongDAO ctdpDAO = new ChiTietDatPhongDAO();
    private final ChiTietHoaDonDAO cthdDAO = new ChiTietHoaDonDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    /* -- Controls -- */
    private TextField txtSearch;
    private TableView<Object[]> tableOrders;
    private ObservableList<Object[]> orderData;
    private FilteredList<Object[]> filteredData;
    private VBox detailSection;
    private VBox roomListSection;
    private Button btnConfirm;

    /* -- State -- */
    private DatPhong currentDatPhong;
    private List<Object[]> currentRoomDetails; // {maPhong, tenLoaiPhong}
    private NhanVien staff;
    private boolean isTableSelecting = false;

    public CheckInView() {
        this(null);
    }

    public CheckInView(NhanVien staff) {
        this.staff = staff;
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));
        setTop(buildHeader());
        setCenter(buildMainContent());
        resetView();
        refreshTable();
    }

    /* ===== HEADER ===== */
    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 24, 0));

        VBox titleBox = new VBox(4);
        Label lblTitle = new Label("Thủ tục nhận phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSub = new Label("Tìm đơn đặt phòng theo mã đặt, họ tên ,số điện thoại hoặc CCCD");
        lblSub.setFont(Font.font("Segoe UI", 14));
        lblSub.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Nhập mã đặt phòng, họ tên,số điện thoại hoặc CCCD");
        txtSearch.setPrefHeight(40);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER
                + "; -fx-font-size: 14px; -fx-padding: 0 16;");

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredData != null) {
                filteredData.setPredicate(order -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String filter = removeAccents(newValue.toLowerCase());
                    String maDat = removeAccents(order[0].toString().toLowerCase());
                    String tenKH = removeAccents(order[1].toString().toLowerCase());
                    String soDT = order.length > 4 ? removeAccents(order[4].toString().toLowerCase()) : "";
                    String soCCCD = order.length > 5 ? removeAccents(order[5].toString().toLowerCase()) : "";

                    return maDat.contains(filter)
                            || tenKH.contains(filter)
                            || soDT.contains(filter)
                            || soCCCD.contains(filter);
                });

                if (filteredData.isEmpty()) {
                    resetDetailView();
                } else if (filteredData.size() == 1) {
                    isTableSelecting = true;
                    tableOrders.getSelectionModel().select(0);
                    isTableSelecting = false;
                    loadOrderDetail((String) filteredData.get(0)[0]);
                } else {
                    resetDetailView();
                }
            }
        });

        Button btnSearch = new Button("\uD83D\uDD0D  Tìm kiếm");
        btnSearch.setPrefHeight(35);
        btnSearch.setMinWidth(140);
        btnSearch.setCursor(Cursor.HAND);
        styleButton(btnSearch, C_BLUE, "white", C_BLUE_HOVER);

        btnSearch.setOnAction(e -> txtSearch.requestFocus());
        txtSearch.setOnAction(e -> {
            if (filteredData != null && filteredData.size() == 1) {
                tableOrders.getSelectionModel().select(0);
                loadOrderDetail((String) filteredData.get(0)[0]);
            }
        });

        searchRow.getChildren().addAll(txtSearch, btnSearch);

        header.getChildren().addAll(titleBox, searchRow);
        return header;
    }

    /* ===== MAIN CONTENT ===== */
    private HBox buildMainContent() {
        HBox main = new HBox(24);
        main.setAlignment(Pos.TOP_LEFT);

        // COT TRAI: Thong tin don (Combined Scroll)
        VBox leftCol = new VBox(16);
        leftCol.setMinWidth(400);
        leftCol.setPrefWidth(430);

        VBox combinedInfoWrapper = new VBox(16);
        combinedInfoWrapper.setPadding(new Insets(24));
        combinedInfoWrapper.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: "
                + C_BORDER + "; -fx-border-radius: 12;");
        combinedInfoWrapper.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        detailSection = new VBox(16);
        detailSection.setAlignment(Pos.CENTER);
        Label lblNoData = new Label("Chưa có thông tin đơn đặt phòng");
        lblNoData.setTextFill(Color.web(C_TEXT_GRAY));
        detailSection.getChildren().add(lblNoData);

        Separator cardSep = new Separator();
        cardSep.setPadding(new Insets(8, 0, 8, 0));

        // Room list section
        Label lblRoomTitle = new Label("Phòng trong đơn");
        lblRoomTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblRoomTitle.setTextFill(Color.web(C_TEXT_DARK));

        roomListSection = new VBox(8);
        Label lblNoRoom = new Label("Chọn đơn để xem danh sách phòng");
        lblNoRoom.setTextFill(Color.web(C_TEXT_GRAY));
        roomListSection.getChildren().add(lblNoRoom);

        ScrollPane scrollRooms = new ScrollPane(roomListSection);
        scrollRooms.setFitToWidth(true);
        scrollRooms.setPrefHeight(200); // Gioi han chieu cao danh sach phong
        scrollRooms.setStyle(
                "-fx-background: white; -fx-background-color: white; -fx-border-color: #f3f4f6; -fx-border-radius: 8;");

        // Action row with confirm button
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(12, 0, 0, 0));

        Button btnCancel = new Button("Hủy bỏ");
        btnCancel.setPrefHeight(40);
        btnCancel.setMinWidth(90);
        btnCancel.setCursor(Cursor.HAND);
        btnCancel.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-background-radius: 8; -fx-font-weight: bold;");
        btnCancel.setOnAction(e -> {
            resetView();
            refreshTable();
        });

        btnConfirm = new Button("✓  Xác nhận nhận phòng");
        btnConfirm.setPrefHeight(40);
        btnConfirm.setMinWidth(200);
        btnConfirm.setCursor(Cursor.HAND);
        styleButton(btnConfirm, "#1e3a8aEE", "white", "#1e3a8aEE");
        btnConfirm.setVisible(false);
        btnConfirm.setManaged(false);
        btnConfirm.setOnAction(e -> handleConfirm());

        actionRow.getChildren().addAll(btnCancel, btnConfirm);

        combinedInfoWrapper.getChildren().addAll(detailSection, cardSep, lblRoomTitle, scrollRooms, actionRow);

        ScrollPane leftScroll = new ScrollPane(combinedInfoWrapper);
        leftScroll.setFitToWidth(true);
        leftScroll.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(leftScroll, Priority.ALWAYS);

        leftCol.getChildren().add(leftScroll);

        // COT PHAI: Danh sach don nhan phong
        VBox rightCol = new VBox(16);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox orderListContainer = new VBox(16);
        orderListContainer.setPadding(new Insets(24));
        orderListContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: "
                + C_BORDER + "; -fx-border-radius: 12;");
        orderListContainer.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));
        VBox.setVgrow(orderListContainer, Priority.ALWAYS);

        Label lblOrderTitle = new Label("Danh sách đơn nhận phòng");
        lblOrderTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblOrderTitle.setTextFill(Color.web(C_TEXT_DARK));

        // Table
        orderData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(orderData, p -> true);
        SortedList<Object[]> sortedData = new SortedList<>(filteredData);

        tableOrders = new TableView<>(sortedData);
        sortedData.comparatorProperty().bind(tableOrders.comparatorProperty());
        tableOrders.setPlaceholder(new Label("Không có đơn đặt phòng nào"));
        tableOrders.setStyle("-fx-font-size: 13px;");
        VBox.setVgrow(tableOrders, Priority.ALWAYS);

        TableColumn<Object[], String> colMaDat = new TableColumn<>("Mã đặt");
        colMaDat.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[0]));
        colMaDat.setPrefWidth(100);
        colMaDat.setReorderable(false);

        TableColumn<Object[], String> colTenKH = new TableColumn<>("Tên khách hàng");
        colTenKH.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[1]));
        colTenKH.setPrefWidth(180);
        colTenKH.setReorderable(false);

        TableColumn<Object[], String> colPhong = new TableColumn<>("Danh sách phòng");
        colPhong.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[2]));
        colPhong.setPrefWidth(160);
        colPhong.setReorderable(false);

        TableColumn<Object[], String> colSoNguoi = new TableColumn<>("Số người");
        colSoNguoi.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue()[3])));
        colSoNguoi.setPrefWidth(90);
        colSoNguoi.setStyle("-fx-alignment: CENTER;");
        colSoNguoi.setReorderable(false);

        tableOrders.getColumns().addAll(colMaDat, colTenKH, colPhong, colSoNguoi);
        tableOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // When selecting a row in table, load detail
        tableOrders.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isTableSelecting) {
                String maDat = (String) newVal[0];
                loadOrderDetail(maDat);
            }
        });

        orderListContainer.getChildren().addAll(lblOrderTitle, tableOrders);
        rightCol.getChildren().add(orderListContainer);

        main.getChildren().addAll(leftCol, rightCol);
        return main;
    }

    /* ===== LOGIC ===== */
    private void refreshTable() {
        orderData.clear();
        List<Object[]> list = datPhongDAO.getDonCheckInByDate(LocalDate.now(), false);
        orderData.addAll(list);
    }

    private void loadOrderDetail(String key) {
        if (key == null || key.isEmpty())
            return;

        DatPhong dp = datPhongDAO.findDatPhongAllStatus(key);
        if (dp == null)
            return;

        // Nếu là DA_XACNHAN, tiến hành load dữ liệu
        currentDatPhong = dp;
        currentRoomDetails = ctdpDAO.getPhongDetailsByMaDat(dp.getMaDat());
        updateDetailUI(dp);
        updateRoomListUI();

        boolean hasRooms = currentRoomDetails != null && !currentRoomDetails.isEmpty();
        btnConfirm.setVisible(hasRooms);
        btnConfirm.setManaged(hasRooms);
    }

    private void resetDetailView() {
        currentDatPhong = null;
        currentRoomDetails = null;
        if (btnConfirm != null) {
            btnConfirm.setVisible(false);
            btnConfirm.setManaged(false);
        }
        if (detailSection != null) {
            detailSection.getChildren().clear();
            detailSection.setAlignment(Pos.CENTER);
            Label lbl = new Label("Chưa có thông tin đơn đặt phòng");
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            detailSection.getChildren().add(lbl);
        }
        if (roomListSection != null) {
            roomListSection.getChildren().clear();
            Label lbl = new Label("Chọn đơn để xem danh sách phòng");
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            roomListSection.getChildren().add(lbl);
        }
    }

    private void updateDetailUI(DatPhong dp) {
        detailSection.getChildren().clear();
        detailSection.setAlignment(Pos.TOP_LEFT);

        Label lblHeader = new Label("Chi tiết đơn đặt");
        lblHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblHeader.setTextFill(Color.web(C_NAVY));
        lblHeader.setPadding(new Insets(0, 0, 8, 0));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        VBox infoBox = new VBox(12);
        infoBox.getChildren().addAll(
                lblHeader,
                createDetailItem("Mã đơn", dp.getMaDat()),
                createDetailItem("Khách hàng", dp.getKhachHang().getTenKH()),
                createDetailItem("Số điện thoại", safe(dp.getKhachHang().getSoDT())),
                createDetailItem("Số CCCD", safe(dp.getKhachHang().getSoCCCD())));

        Separator sep = new Separator();
        sep.setPadding(new Insets(8, 0, 8, 0));

        String trangThaiStr = safe(dp.getTrangThai());
        try {
            trangThaiStr = model.enums.TrangThaiDatPhong.valueOf(dp.getTrangThai()).getThongTinTrangThai();
        } catch (Exception ignored) {
        }

        VBox scheduleBox = new VBox(12);
        scheduleBox.getChildren().addAll(
                createDetailItem("Ngày nhận", dp.getNgayCheckIn() != null ? dp.getNgayCheckIn().format(dtf) : "---"),
                createDetailItem("Ngày trả", dp.getNgayCheckOut() != null ? dp.getNgayCheckOut().format(dtf) : "---"),
                createDetailItem("Trạng thái đơn", trangThaiStr));

        detailSection.getChildren().addAll(infoBox, sep, scheduleBox);
    }

    private void updateRoomListUI() {
        roomListSection.getChildren().clear();
        if (currentRoomDetails == null || currentRoomDetails.isEmpty()) {
            Label lbl = new Label("Đơn này chưa có phòng nào được phân công");
            lbl.setTextFill(Color.web("#d97706"));
            lbl.setWrapText(true);
            roomListSection.getChildren().add(lbl);
            return;
        }

        // Lấy trạng thái thực tế của các phòng
        java.util.List<String> listMa = new java.util.ArrayList<>();
        for (Object[] r : currentRoomDetails)
            listMa.add((String) r[0]);
        java.util.Map<String, String> mapStatus = phongDAO.getTrangThaiMapByMaPhongs(listMa);

        for (Object[] rowData : currentRoomDetails) {
            String maPhong = (String) rowData[0];
            String maLoai = (String) rowData[1];
            String status = mapStatus.getOrDefault(maPhong, "CONTRONG");
            boolean isOccupied = "DANGSUDUNG".equals(status);
            boolean isDirty = "BAN".equals(status);
            boolean isMaintenance = "BAOTRI".equals(status);
            boolean isReady = !isOccupied && !isDirty && !isMaintenance;

            String tenLoai = maLoai;
            try {
                tenLoai = model.enums.TenLoaiPhong.valueOf(maLoai.toUpperCase()).getDisplayName();
            } catch (Exception ignored) {
            }

            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));

            // Đổi màu nền nếu chưa sẵn sàng
            String bg = isReady ? "#f0f9ff" : (isOccupied ? "#fef2f2" : "#fff7ed");
            String border = isReady ? C_BLUE : (isOccupied ? "#ef4444" : "#fb923c");

            row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; "
                    + "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: 1.5;");

            VBox roomInfo = new VBox(2);
            Label lblRoom = new Label("Phòng  " + maPhong);
            lblRoom.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblRoom.setTextFill(Color.web(isReady ? C_NAVY : (isOccupied ? "#b91c1c" : "#9a3412")));

            Label lblType = new Label(tenLoai);
            lblType.setFont(Font.font("Segoe UI", 12));
            lblType.setTextFill(Color.web(C_TEXT_GRAY));
            roomInfo.getChildren().addAll(lblRoom, lblType);

            String statusText = "Sẵn sàng bàn giao";
            if (isOccupied)
                statusText = "Đang có khách";
            else if (isDirty)
                statusText = "Chưa sẵn sàng (cần dọn)";
            else if (isMaintenance)
                statusText = "Chưa sẵn sàng (bảo trì)";

            Label lblStatus = new Label(statusText);
            lblStatus.setFont(Font.font("Segoe UI", 12));
            lblStatus.setTextFill(Color.web(isReady ? C_GREEN : (isOccupied ? "#ef4444" : "#ea580c")));
            lblStatus.setStyle("-fx-background-color: " + (isReady ? "#f0fdf4" : "#fff2f2")
                    + "; -fx-padding: 2 10; -fx-background-radius: 10;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(roomInfo, spacer, lblStatus);
            roomListSection.getChildren().add(row);
        }
    }

    /**
     * Xac nhan check-in:
     * 1. Tao Hoa Don (neu chua co)
     * 2. Update phong -> DANGSUDUNG (tung phong) & Tao ChiTietHoaDon
     * 3. Update don -> DA_CHECKIN (trong transaction)
     */
    private void handleConfirm() {
        if (currentDatPhong == null || currentRoomDetails == null || currentRoomDetails.isEmpty())
            return;

        List<String> listMaPhong = new java.util.ArrayList<>();
        for (Object[] r : currentRoomDetails)
            listMaPhong.add((String) r[0]);

        // --- KIỂM TRA TRẠNG THÁI PHÒNG (BẨN / BẢO TRÌ) ---
        java.util.Map<String, String> mapStatus = phongDAO.getTrangThaiMapByMaPhongs(listMaPhong);
        java.util.List<String> dirtyRooms = new java.util.ArrayList<>();
        java.util.List<String> maintenanceRooms = new java.util.ArrayList<>();
        java.util.List<String> occupiedRooms = new java.util.ArrayList<>();

        for (String mp : listMaPhong) {
            String status = mapStatus.getOrDefault(mp, "CONTRONG");
            if ("BAN".equals(status))
                dirtyRooms.add(mp);
            else if ("BAOTRI".equals(status))
                maintenanceRooms.add(mp);
            else if ("DANGSUDUNG".equals(status))
                occupiedRooms.add(mp);
        }

        // --- TRƯỜNG HỢP CÓ PHÒNG CHƯA SẴN SÀNG: HIỆN CẢNH BÁO VÀ CHẶN NHẬN ---
        if (!dirtyRooms.isEmpty() || !maintenanceRooms.isEmpty() || !occupiedRooms.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Không thể nhận phòng do có phòng chưa sẵn sàng:\n");
            if (!occupiedRooms.isEmpty()) {
                errorMsg.append("\n⛔ Phòng [").append(String.join(", ", occupiedRooms))
                        .append("] hiện đang CÓ KHÁCH ĐANG Ở.");
            }
            if (!dirtyRooms.isEmpty()) {
                errorMsg.append("\n❌ Phòng [").append(String.join(", ", dirtyRooms))
                        .append("] đang BẨN.");
            }
            if (!maintenanceRooms.isEmpty()) {
                errorMsg.append("\n❌ Phòng [").append(String.join(", ", maintenanceRooms))
                        .append("] đang BẢO TRÌ.");
            }
            errorMsg.append("\n\nVui lòng yêu cầu dọn dẹp hoặc đổi phòng trước khi tiếp tục.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi Check-in");
            alert.setHeaderText("Phòng chưa sẵn sàng!");
            alert.setContentText(errorMsg.toString());
            alert.showAndWait();
            return; // Chặn không cho thực hiện tiếp
        }

        // --- TRƯỜNG HỢP TẤT CẢ PHÒNG ĐỀU SẴN SÀNG: HIỆN XÁC NHẬN BÌNH THƯỜNG ---
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Check-in");
        confirm.setHeaderText("Xác nhận cho khách " + currentDatPhong.getKhachHang().getTenKH()
                + " nhận " + listMaPhong.size() + " phòng?");

        String content = "Bàn giao phòng: " + String.join(", ", listMaPhong)
                + "\nHệ thống sẽ:\n - Cập nhật trạng thái phòng -> Đang sử dụng"
                + "\n - Cập nhật đơn đặt phòng -> Đã nhận phòng\n - Tạo hóa đơn mới cho đơn đặt phòng";

        confirm.setContentText(content);
        confirm.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;

            try (Connection con = ConnectDatabase.getInstance().getConnection()) {
                if (con == null)
                    return;
                con.setAutoCommit(false);
                try {
                    // 1. TÌM HOẶC TẠO HÓA ĐƠN MỚI
                    HoaDon hd = hoaDonDAO.getByMaDat(currentDatPhong.getMaDat());

                    if (hd == null) {
                        hd = new HoaDon();
                        hd.setMaHD(hoaDonDAO.generateMaHD());
                        hd.setDatPhong(currentDatPhong);
                        hd.setNhanVien(staff);
                        hd.setTienPhong(0.0);
                        hd.setTienDV(0.0);
                        hd.setTienCoc(0.0);
                        hd.setThueVAT(0.1);
                        hd.setTongTien(0.0);
                        hd.setLoaiHD("HOA_DON_PHONG");
                        hd.setTrangThaiThanhToan("CHUA_THANH_TOAN");
                        hoaDonDAO.insertWithConnection(con, hd);
                    }

                    // 2. LẤY MÃ CHI TIẾT HÓA ĐƠN TIẾP THEO
                    String baseMaCTHD = cthdDAO.generateMaCTHD();
                    int lastNum = 0;
                    if (baseMaCTHD != null && baseMaCTHD.length() > 4) {
                        try {
                            lastNum = Integer.parseInt(baseMaCTHD.substring(4));
                        } catch (Exception ignored) {
                        }
                    }

                    // 3. CẬP NHẬT PHÒNG & TẠO CHI TIẾT HÓA ĐƠN
                    java.util.Map<String, String> mapMaCTDP = ctdpDAO.getMaCTDPMapByMaDat(currentDatPhong.getMaDat());

                    for (String maPhong : listMaPhong) {
                        phongDAO.updateTrangThaiWithCon(con, maPhong, "DANGSUDUNG");
                        String maCTDP = mapMaCTDP.get(maPhong);

                        if (maCTDP != null && hd != null) {
                            String maCTHD = String.format("CTHD%03d", lastNum++);
                            cthdDAO.insertWithConnection(con, maCTHD, hd.getMaHD(), maCTDP, 0, 0);
                        }
                    }

                    // 4. CẬP NHẬT ĐƠN ĐẶT PHÒNG
                    datPhongDAO.updateTrangThaiWithCon(con, currentDatPhong.getMaDat(), "DA_CHECKIN");

                    con.commit();

                    Alert ok = new Alert(Alert.AlertType.INFORMATION,
                            "Các phòng [" + String.join(", ", listMaPhong) + "] đã bàn giao thành công.",
                            ButtonType.OK);
                    ok.setTitle("Check-in thành công");
                    ok.setHeaderText("Check-in hoàn tất!");
                    ok.showAndWait();

                    resetView();
                    refreshTable();
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Lỗi Check-in: " + ex.getMessage(), ButtonType.OK).showAndWait();
            }
        });
    }

    private void resetView() {
        if (txtSearch != null)
            txtSearch.clear();
        currentDatPhong = null;
        currentRoomDetails = null;
        if (btnConfirm != null) {
            btnConfirm.setVisible(false);
            btnConfirm.setManaged(false);
        }
        if (detailSection != null) {
            detailSection.getChildren().clear();
            detailSection.setAlignment(Pos.CENTER);
            Label lbl = new Label("Chưa có thông tin đơn đặt phòng");
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            detailSection.getChildren().add(lbl);
        }
        if (roomListSection != null) {
            roomListSection.getChildren().clear();
            Label lbl = new Label("Chọn đơn để xem danh sách phòng");
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            roomListSection.getChildren().add(lbl);
        }
        if (tableOrders != null) {
            tableOrders.getSelectionModel().clearSelection();
        }
    }

    /* ===== Helpers ===== */
    private String safe(String s) {
        return s != null ? s : "---";
    }

    private HBox createDetailItem(String label, String value) {
        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setMinWidth(160);
        lbl.setTextFill(Color.web(C_TEXT_GRAY));
        lbl.setFont(Font.font("Segoe UI", 14));
        Label val = new Label(value != null ? value : "---");
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        val.setTextFill(Color.web(C_TEXT_DARK));
        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        String base = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-weight: bold;";
        String hover = base.replace("-fx-background-color: " + bg, "-fx-background-color: " + hoverBg);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private String removeAccents(String s) {
        if (s == null)
            return "";
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('Đ', 'D').replace('đ', 'd');
    }
}
