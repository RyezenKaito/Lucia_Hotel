package gui;

import dao.DatPhongDAO;
import dao.ChiTietDatPhongDAO;
import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.PhongDAO;
import model.entities.*;
import connectDatabase.ConnectDatabase;
import java.sql.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private FlowPane quickSearchFlow;
    private VBox detailSection;
    private VBox roomListSection;
    private Button btnConfirm;

    /* -- State -- */
    private DatPhong currentDatPhong;
    private List<String> currentMaPhongs;
    private NhanVien staff;

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
    }

    /* ===== HEADER ===== */
    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 24, 0));

        VBox titleBox = new VBox(4);
        Label lblTitle = new Label("Thủ tục nhận phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSub = new Label("Tìm đơn đặt phòng theo mã đặt, số điện thoại hoặc CCCD");
        lblSub.setFont(Font.font("Segoe UI", 14));
        lblSub.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Nhập mã đặt phòng, số điện thoại hoặc CCCD");
        txtSearch.setPrefHeight(48);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + C_BORDER
                + "; -fx-font-size: 14px; -fx-padding: 0 16;");
        txtSearch.setOnAction(e -> handleSearch());

        Button btnSearch = new Button("🔍  Tìm kiếm");
        btnSearch.setPrefHeight(48);
        btnSearch.setMinWidth(140);
        btnSearch.setCursor(Cursor.HAND);
        styleButton(btnSearch, C_BLUE, "white", C_BLUE_HOVER);
        btnSearch.setOnAction(e -> handleSearch());

        searchRow.getChildren().addAll(txtSearch, btnSearch);

        VBox quickBox = new VBox(8);
        Label lblQuick = new Label("Gợi ý đơn trong hôm nay (chờ nhận phòng):");
        lblQuick.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblQuick.setTextFill(Color.web(C_TEXT_GRAY));
        quickSearchFlow = new FlowPane(10, 10);
        loadQuickSuggestions();
        quickBox.getChildren().addAll(lblQuick, quickSearchFlow);

        header.getChildren().addAll(titleBox, searchRow, quickBox);
        return header;
    }

    /* ===== MAIN CONTENT ===== */
    private HBox buildMainContent() {
        HBox main = new HBox(24);
        main.setAlignment(Pos.TOP_LEFT);

        // COT TRAI: Thong tin don
        VBox leftCol = new VBox(16);
        leftCol.setMinWidth(380);
        leftCol.setPrefWidth(430);

        detailSection = new VBox(16);
        detailSection.setPadding(new Insets(24));
        detailSection.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: "
                + C_BORDER + "; -fx-border-radius: 12;");
        detailSection.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));
        detailSection.setAlignment(Pos.CENTER);
        Label lblNoData = new Label("Chưa có thông tin đơn đặt phòng");
        lblNoData.setTextFill(Color.web(C_TEXT_GRAY));
        detailSection.getChildren().add(lblNoData);
        leftCol.getChildren().add(detailSection);

        // COT PHAI: Danh sach phong + nut xac nhan
        VBox rightCol = new VBox(16);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox roomContainer = new VBox(16);
        roomContainer.setPadding(new Insets(24));
        roomContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: "
                + C_BORDER + "; -fx-border-radius: 12;");
        roomContainer.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));
        VBox.setVgrow(roomContainer, Priority.ALWAYS);

        Label lblRoomTitle = new Label("Phòng trong đơn đặt");
        lblRoomTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblRoomTitle.setTextFill(Color.web(C_TEXT_DARK));

        Label lblRoomHint = new Label("Các phòng dưới này sẽ được bàn giao cho khách khi xác nhận");
        lblRoomHint.setFont(Font.font("Segoe UI", 13));
        lblRoomHint.setTextFill(Color.web(C_TEXT_GRAY));
        lblRoomHint.setWrapText(true);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");
        roomListSection = new VBox(12);
        roomListSection.setPadding(new Insets(10, 0, 0, 0));
        scroll.setContent(roomListSection);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(12, 0, 0, 0));

        Button btnCancel = new Button("Hủy bỏ");
        btnCancel.setPrefHeight(44);
        btnCancel.setMinWidth(100);
        btnCancel.setCursor(Cursor.HAND);
        btnCancel.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-background-radius: 8; -fx-font-weight: bold;");
        btnCancel.setOnAction(e -> resetView());

        btnConfirm = new Button("Xác nhận phòng");
        btnConfirm.setPrefHeight(44);
        btnConfirm.setMinWidth(220);
        btnConfirm.setCursor(Cursor.HAND);
        styleButton(btnConfirm, "#1e3a8aEE", "white", "#1e3a8aEE");
        btnConfirm.setDisable(true);
        btnConfirm.setOnAction(e -> handleConfirm());

        actionRow.getChildren().addAll(btnCancel, btnConfirm);
        roomContainer.getChildren().addAll(lblRoomTitle, lblRoomHint, scroll, actionRow);
        rightCol.getChildren().add(roomContainer);

        main.getChildren().addAll(leftCol, rightCol);
        return main;
    }

    /* ===== LOGIC ===== */
    private void loadQuickSuggestions() {
        quickSearchFlow.getChildren().clear();
        List<String> suggestions = datPhongDAO.getMaDatPhongCheckInHomNay();
        if (suggestions.isEmpty()) {
            Label lbl = new Label("Không có đơn cho hôm nay");
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            quickSearchFlow.getChildren().add(lbl);
        } else {
            for (String s : suggestions) {
                Button b = new Button(s);
                b.setCursor(Cursor.HAND);
                b.setStyle("-fx-background-color: white; -fx-border-color: " + C_BLUE
                        + "; -fx-text-fill: " + C_BLUE
                        + "; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 4 12;");
                b.setOnAction(e -> {
                    txtSearch.setText(s);
                    handleSearch();
                });
                quickSearchFlow.getChildren().add(b);
            }
        }
    }

    private void handleSearch() {
        String key = txtSearch.getText().trim();
        if (key.isEmpty())
            return;

        DatPhong dp = datPhongDAO.findDatPhongDetail(key);
        if (dp != null) {
            currentDatPhong = dp;
            currentMaPhongs = ctdpDAO.getMaPhongByMaDat(dp.getMaDat());
            updateDetailUI(dp);
            updateRoomListUI();
            btnConfirm.setDisable(currentMaPhongs == null || currentMaPhongs.isEmpty());
        } else {
            new Alert(Alert.AlertType.WARNING, "Khong tim thay don dat phong.\nKiem tra lai ma dat / SDT / CCCD.",
                    ButtonType.OK).showAndWait();
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
        if (currentMaPhongs == null || currentMaPhongs.isEmpty()) {
            Label lbl = new Label("Đơn này chưa có phòng nào được phân công");
            lbl.setTextFill(Color.web("#d97706"));
            lbl.setWrapText(true);
            roomListSection.getChildren().add(lbl);
            return;
        }
        for (String maPhong : currentMaPhongs) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 10; "
                    + "-fx-border-color: " + C_BLUE + "; -fx-border-radius: 10; -fx-border-width: 1.5;");
            Label lblRoom = new Label("Phong  " + maPhong);
            lblRoom.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            lblRoom.setTextFill(Color.web(C_NAVY));
            Label lblStatus = new Label("San sang ban giao");
            lblStatus.setFont(Font.font("Segoe UI", 13));
            lblStatus.setTextFill(Color.web(C_GREEN));
            lblStatus.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 2 10; -fx-background-radius: 10;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(lblRoom, spacer, lblStatus);
            roomListSection.getChildren().add(row);
        }
    }

    /**
     * Xac nhan check-in:
     * 1. Update phong -> DANGSUDUNG (tung phong)
     * 2. Update don -> DA_CHECKIN (trong transaction)
     */
    /**
     * Xac nhan check-in:
     * 1. Tao Hoa Don (neu chua co)
     * 2. Update phong -> DANGSUDUNG (tung phong) & Tao ChiTietHoaDon
     * 3. Update don -> DA_CHECKIN (trong transaction)
     */
    private void handleConfirm() {
        if (currentDatPhong == null || currentMaPhongs == null || currentMaPhongs.isEmpty())
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bàn giao phòng: " + String.join(", ", currentMaPhongs)
                        + "\nHệ thống sẽ:\n - Cập nhật trạng thái phòng -> Đang sử dụng"
                        + "\n - Cập nhật đơn đặt phòng -> Đã nhận phòng\n - Tạo hóa đơn mới cho đơn đặt phòng",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Xác nhận Check-in");
        confirm.setHeaderText("Xác nhận cho khách " + currentDatPhong.getKhachHang().getTenKH()
                + " nhận " + currentMaPhongs.size() + " phòng?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;

            try (Connection con = ConnectDatabase.getInstance().getConnection()) {
                if (con == null)
                    return;
                con.setAutoCommit(false);
                try {
                    // ==========================================
                    // 1. TÌM HOẶC TẠO HÓA ĐƠN MỚI
                    // ==========================================
                    HoaDon hd = hoaDonDAO.getByMaDat(currentDatPhong.getMaDat());

                    if (hd == null) {
                        hd = new HoaDon();
                        hd.setMaHD(hoaDonDAO.generateMaHD()); // Tự sinh mã HD0xx
                        hd.setDatPhong(currentDatPhong);
                        hd.setNhanVien(staff); // staff lấy từ constructor CheckInView
                        hd.setTienPhong(0.0);
                        hd.setTienDV(0.0);
                        hd.setTienCoc(0.0);
                        hd.setThueVAT(0.0);
                        hd.setTongTien(0.0);
                        hd.setLoaiHD("HOA_DON_PHONG");
                        hd.setTrangThaiThanhToan("CHUA_THANH_TOAN");

                        // Insert hóa đơn vào DB
                        hoaDonDAO.insertWithConnection(con, hd);
                    }

                    // ==========================================
                    // 2. LẤY MÃ CHI TIẾT HÓA ĐƠN TIẾP THEO
                    // ==========================================
                    String baseMaCTHD = cthdDAO.generateMaCTHD();
                    int lastNum = 0;
                    if (baseMaCTHD != null && baseMaCTHD.length() > 4) {
                        try {
                            lastNum = Integer.parseInt(baseMaCTHD.substring(4));
                        } catch (Exception ignored) {
                        }
                    }

                    // ==========================================
                    // 3. CẬP NHẬT PHÒNG & TẠO CHI TIẾT HÓA ĐƠN
                    // ==========================================
                    for (String maPhong : currentMaPhongs) {
                        phongDAO.updateTrangThaiWithCon(con, maPhong, "DANGSUDUNG");
                        String maCTDP = datPhongDAO.findMaCTDPByMaPhong(currentDatPhong.getMaDat(), maPhong);

                        if (maCTDP != null && hd != null) {
                            String maCTHD = String.format("CTHD%03d", lastNum++);
                            // Tạo liên kết giữa Hóa Đơn và Chi tiết đặt phòng
                            cthdDAO.insertWithConnection(con, maCTHD, hd.getMaHD(), maCTDP, 0, 0);
                        }
                    }

                    // ==========================================
                    // 4. CẬP NHẬT ĐƠN ĐẶT PHÒNG
                    // ==========================================
                    datPhongDAO.updateTrangThaiWithCon(con, currentDatPhong.getMaDat(), "DA_CHECKIN");

                    // LƯU TOÀN BỘ VÀO DATABASE
                    con.commit();

                    Alert ok = new Alert(Alert.AlertType.INFORMATION,
                            "Các phòng [" + String.join(", ", currentMaPhongs) + "] đã bàn giao thành công.",
                            ButtonType.OK);
                    ok.setTitle("Check-in thành công");
                    ok.setHeaderText("Check-in hoàn tất!");
                    ok.showAndWait();

                    resetView();
                    loadQuickSuggestions();
                } catch (Exception ex) {
                    con.rollback(); // Nếu có lỗi thì hủy bỏ toàn bộ thao tác
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
        currentMaPhongs = null;
        if (btnConfirm != null)
            btnConfirm.setDisable(true);
        if (detailSection != null) {
            detailSection.getChildren().clear();
            detailSection.setAlignment(Pos.CENTER);
            Label lbl = new Label("Chưa có thông tin đơn đặt phòng");
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            detailSection.getChildren().add(lbl);
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
}
