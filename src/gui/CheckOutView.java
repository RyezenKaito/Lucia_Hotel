package gui;

import dao.DatPhongDAO;
import dao.DichVuSuDungDAO;
import dao.HoaDonDAO;
import dao.PhongDAO;
import model.entities.DatPhong;
import model.entities.DichVuSuDung;
import model.entities.HoaDon;
import model.entities.NhanVien;

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
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    /* ── Controls ───────────────────────────────────────────────────── */
    private TextField txtSearch;
    private FlowPane quickCheckOutFlow;
    private VBox guestInfoSection;
    private TableView<DichVuSuDung> serviceTable;
    private VBox billingSection;

    // Data hiện tại
    private DatPhong currentDatPhong;
    private String currentMaHD;
    private String currentMaPhong;
    private double currentGiaPhong = 0;
    private double currentTienPhong = 0;
    private double currentTienDV = 0;
    private double currentTienCoc = 0;
    private double currentLateFee = 0;
    private double currentTongTien = 0;
    private long currentSoDem = 0;

    /* ── Nhân viên đang đăng nhập ──────────────────────────────────── */
    private NhanVien staff;

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
        Label lblQuick = new Label("Phòng đang có khách:");
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

    // ── Quick Suggestions: Load phòng đang sử dụng từ DB ──
    private void loadQuickSuggestions() {
        quickCheckOutFlow.getChildren().clear();
        List<String> dsPhong = datPhongDAO.getPhongDangSuDung();
        if (dsPhong.isEmpty()) {
            Label lblEmpty = new Label("Không có phòng nào đang sử dụng");
            lblEmpty.setFont(Font.font("Segoe UI", 12));
            lblEmpty.setTextFill(Color.web(C_TEXT_GRAY));
            quickCheckOutFlow.getChildren().add(lblEmpty);
            return;
        }
        for (String s : dsPhong) {
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

    // ── Tìm phòng: lấy thông tin checkout từ DB ──
    private void handleSearch() {
        String maPhong = txtSearch.getText().trim().toUpperCase();
        if (maPhong.isEmpty())
            return;

        // Query tổng hợp từ DB (DatPhong + KH + ChiTietDatPhong + LoaiPhong)
        Object[] info = datPhongDAO.findCheckOutInfoByMaPhong(maPhong);
        if (info != null) {
            currentDatPhong = (DatPhong) info[0];
            currentTienCoc = (double) info[1];
            currentGiaPhong = (double) info[2];
            currentMaPhong = (String) info[3];

            // Lấy mã hóa đơn (nếu đã tạo từ trước)
            currentMaHD = datPhongDAO.getMaHDByMaPhong(maPhong);

            updateDetailUI();
            loadServices();
            calculateBilling();
            updateBillingUI();
        } else {
            // Kiểm tra xem phòng có đang ở trạng thái DANGSUDUNG nhưng mất dữ liệu đặt phòng không
            boolean isStuckRoom = checkStuckRoom(maPhong);
            if (isStuckRoom) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Phòng bị kẹt trạng thái");
                alert.setHeaderText("Phòng \"" + maPhong + "\" đang ở trạng thái 'Đang sử dụng' nhưng không tìm thấy đơn đặt phòng.");
                alert.setContentText("Có thể đơn đặt phòng đã bị xóa. Bạn có muốn giải phóng phòng này về 'Còn trống' không?");
                alert.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.OK) {
                        if (phongDAO.updateTrangThai(maPhong, "CONTRONG")) {
                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Thành công");
                            success.setHeaderText("Đã giải phóng phòng " + maPhong);
                            success.setContentText("Trạng thái phòng đã được cập nhật thành 'Còn trống'.");
                            success.showAndWait();
                            resetView();
                            loadQuickSuggestions();
                        } else {
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setHeaderText("Không thể cập nhật trạng thái phòng.");
                            error.showAndWait();
                        }
                    }
                });
            } else {
                resetView();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText("Không tìm thấy khách đang ở tại phòng này");
                alert.setContentText("Phòng \"" + maPhong + "\" không đang được sử dụng hoặc không tồn tại.");
                alert.showAndWait();
            }
        }
    }

    /**
     * Kiểm tra phòng có đang ở trạng thái DANGSUDUNG nhưng không có dữ liệu đặt phòng hợp lệ
     */
    private boolean checkStuckRoom(String maPhong) {
        try (java.sql.Connection con = connectDatabase.ConnectDatabase.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM Phong WHERE maPhong = ? AND tinhTrang = N'DANGSUDUNG'")) {
            ps.setString(1, maPhong);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Hiển thị thông tin khách + đặt phòng ──
    private void updateDetailUI() {
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
        grid.add(createValue(currentMaPhong + " — " + String.format("%,.0f đ/đêm", currentGiaPhong)), 3, 0);

        grid.add(createLabel("Khách hàng:"), 0, 1);
        grid.add(createValue(currentDatPhong.getKhachHang().getTenKH()), 1, 1);
        grid.add(createLabel("Số điện thoại:"), 2, 1);
        grid.add(createValue(currentDatPhong.getKhachHang().getSoDT() != null
                ? currentDatPhong.getKhachHang().getSoDT() : "—"), 3, 1);

        grid.add(createLabel("Ngày nhận phòng:"), 0, 2);
        grid.add(createValue(currentDatPhong.getNgayCheckIn() != null
                ? currentDatPhong.getNgayCheckIn().format(fmt) : "—"), 1, 2);
        grid.add(createLabel("Ngày trả dự kiến:"), 2, 2);
        grid.add(createValue(currentDatPhong.getNgayCheckOut() != null
                ? currentDatPhong.getNgayCheckOut().format(fmt) : "—"), 3, 2);

        grid.add(createLabel("Tiền cọc:"), 0, 3);
        Label lblCoc = createValue(String.format("%,.0f đ", currentTienCoc));
        lblCoc.setTextFill(Color.web(C_GREEN));
        grid.add(lblCoc, 1, 3);

        guestInfoSection.getChildren().addAll(lblT, grid);
    }

    // ── Load dịch vụ đã sử dụng ──
    private void loadServices() {
        if (currentMaHD != null) {
            List<DichVuSuDung> list = dvsdDAO.findByMaHD(currentMaHD);
            serviceTable.setItems(FXCollections.observableArrayList(list));
            currentTienDV = list.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();
        } else {
            serviceTable.setItems(FXCollections.observableArrayList());
            currentTienDV = 0;
        }
    }

    // ── Tính toán hóa đơn thực tế ──
    private void calculateBilling() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkIn = currentDatPhong.getNgayCheckIn();

        // Tính số đêm (ít nhất 1 đêm)
        if (checkIn != null) {
            Duration duration = Duration.between(checkIn, now);
            currentSoDem = Math.max(1, duration.toDays());
        } else {
            currentSoDem = 1;
        }

        // Tiền phòng = số đêm × giá phòng/đêm (từ LoaiPhong)
        currentTienPhong = currentSoDem * currentGiaPhong;

        // Phụ phí trả muộn (so với ngày checkout dự kiến)
        currentLateFee = 0;
        if (currentDatPhong.getNgayCheckOut() != null && now.isAfter(currentDatPhong.getNgayCheckOut())) {
            int hour = now.getHour();
            if (hour >= 18)
                currentLateFee = currentGiaPhong;       // 100% giá 1 đêm
            else if (hour >= 15)
                currentLateFee = currentGiaPhong * 0.5;  // 50%
            else if (hour >= 12)
                currentLateFee = currentGiaPhong * 0.3;  // 30%
        }

        // Tổng = tiền phòng + dịch vụ + phụ phí - cọc
        currentTongTien = currentTienPhong + currentTienDV + currentLateFee - currentTienCoc;
        if (currentTongTien < 0)
            currentTongTien = 0;
    }

    // ── Hiển thị hóa đơn thanh toán ──
    private void updateBillingUI() {
        billingSection.getChildren().clear();
        Label lblT = new Label("Chi tiết hóa đơn");
        lblT.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblT.setTextFill(Color.web(C_TEXT_DARK));

        VBox rows = new VBox(12);
        rows.getChildren().addAll(
                createBillRow("Tiền phòng (" + currentSoDem + " đêm × " + String.format("%,.0f", currentGiaPhong)
                        + " đ)", currentTienPhong, Color.web(C_TEXT_DARK)),
                createBillRow("Tiền dịch vụ", currentTienDV, Color.web(C_TEXT_DARK)),
                createBillRow("Phụ phí trả muộn", currentLateFee,
                        currentLateFee > 0 ? Color.web(C_RED) : Color.web(C_TEXT_GRAY)),
                createBillRow("Tiền đã cọc (trừ)", -currentTienCoc, Color.web(C_GREEN)));

        Separator sep = new Separator();

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotal = new Label("TỔNG THANH TOÁN");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        HBox.setHgrow(lblTotal, Priority.ALWAYS);
        Label valTotal = new Label(String.format("%,.0f đ", currentTongTien));
        valTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valTotal.setTextFill(Color.web(C_BLUE));
        totalRow.getChildren().addAll(lblTotal, valTotal);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnPay = new Button("💳  XÁC NHẬN THANH TOÁN VÀ TRẢ PHÒNG");
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setPrefHeight(50);
        btnPay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        styleButton(btnPay, C_NAVY, "white", "#1e3a8aEE");
        btnPay.setOnAction(e -> handleCheckOut());

        billingSection.getChildren().addAll(lblT, new Separator(), rows, sep, totalRow, spacer, btnPay);
    }

    // ── Thanh toán + Trả phòng (Business Logic) ──
    private void handleCheckOut() {
        if (currentDatPhong == null || currentMaPhong == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Hoàn tất trả phòng " + currentMaPhong + " cho khách "
                + currentDatPhong.getKhachHang().getTenKH() + "?");
        confirm.setContentText("Tổng thanh toán: " + String.format("%,.0f đ", currentTongTien)
                + "\nHóa đơn sẽ được lưu và trạng thái phòng sẽ chuyển sang 'Còn trống'.");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                boolean success = performCheckOut();
                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thành công");
                    successAlert.setHeaderText("✅ Đã thanh toán và trả phòng thành công!");
                    successAlert.setContentText(
                            "Phòng: " + currentMaPhong + "\n" +
                                    "Khách: " + currentDatPhong.getKhachHang().getTenKH() + "\n" +
                                    "Tổng tiền: " + String.format("%,.0f đ", currentTongTien) + "\n\n" +
                                    "Trạng thái phòng đã được cập nhật thành 'Còn trống'.");
                    successAlert.showAndWait();
                    resetView();
                    loadQuickSuggestions();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Lỗi");
                    errorAlert.setHeaderText("Thanh toán thất bại");
                    errorAlert.setContentText("Đã xảy ra lỗi khi lưu dữ liệu. Vui lòng thử lại.");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    /**
     * Thực hiện nghiệp vụ checkout thực tế:
     * 1. Nếu HoaDon đã tồn tại → cập nhật tổng tiền
     * 2. Nếu chưa có HoaDon → tạo mới
     * 3. Cập nhật trạng thái phòng → CONTRONG
     */
    private boolean performCheckOut() {
        try {
            // ── Bước 1: Xử lý HoaDon ──
            if (currentMaHD != null) {
                HoaDon hd = hoaDonDAO.getById(currentMaHD);
                if (hd != null) {
                    hd.setTienPhong(currentTienPhong);
                    hd.setTienDV(currentTienDV);
                    hd.setTienCoc(currentTienCoc);
                    hd.setThueVAT(0); // Không tính VAT theo yêu cầu
                    hd.setTongTien(currentTongTien);
                    hd.setNgayTaoHD(LocalDateTime.now());
                    if (!hoaDonDAO.updateTongTien(hd)) {
                        System.err.println("Lỗi cập nhật hóa đơn: " + currentMaHD);
                        return false;
                    }
                }
            } else {
                HoaDon hd = new HoaDon();
                hd.setMaHD(hoaDonDAO.generateMaHD());
                hd.setDatPhong(currentDatPhong);
                hd.setNhanVien(staff != null ? staff : new NhanVien("ADMIN"));
                hd.setNgayTaoHD(LocalDateTime.now());
                hd.setTienPhong(currentTienPhong);
                hd.setTienDV(currentTienDV);
                hd.setTienCoc(currentTienCoc);
                hd.setThueVAT(0);
                hd.setTongTien(currentTongTien);
                if (!hoaDonDAO.insert(hd)) {
                    System.err.println("Lỗi tạo hóa đơn mới");
                    return false;
                }
            }

            // ── Bước 2: Cập nhật trạng thái phòng → CONTRONG ──
            if (!phongDAO.updateTrangThai(currentMaPhong, "CONTRONG")) {
                System.err.println("Lỗi cập nhật trạng thái phòng: " + currentMaPhong);
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void resetView() {
        txtSearch.clear();
        currentDatPhong = null;
        currentMaHD = null;
        currentMaPhong = null;
        currentGiaPhong = 0;
        currentTienPhong = 0;
        currentTienDV = 0;
        currentTienCoc = 0;
        currentLateFee = 0;
        currentTongTien = 0;
        currentSoDem = 0;

        guestInfoSection.getChildren().clear();
        Label lblEmpty = new Label("🛏  Vui lòng chọn phòng để bắt đầu thủ tục trả phòng");
        lblEmpty.setFont(Font.font("Segoe UI", 14));
        lblEmpty.setTextFill(Color.web(C_TEXT_GRAY));
        guestInfoSection.getChildren().add(lblEmpty);

        serviceTable.setItems(FXCollections.observableArrayList());

        billingSection.getChildren().clear();
        VBox emptyBilling = new VBox(12);
        emptyBilling.setAlignment(Pos.CENTER);
        Label lblIcon = new Label("💰");
        lblIcon.setFont(Font.font(36));
        Label lblMsg = new Label("Chọn phòng để xem hóa đơn");
        lblMsg.setFont(Font.font("Segoe UI", 14));
        lblMsg.setTextFill(Color.web(C_TEXT_GRAY));
        emptyBilling.getChildren().addAll(lblIcon, lblMsg);
        billingSection.getChildren().add(emptyBilling);
    }

    /* ── UI Helpers ──────────────────────────────────────────────────── */

    private HBox createBillRow(String label, double value, Color valColor) {
        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setTextFill(Color.web(C_TEXT_GRAY));
        lbl.setFont(Font.font("Segoe UI", 13));
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