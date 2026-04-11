package gui;

import dao.DatPhongDAO;
import dao.PhongDAO;
import dao.ChiTietDatPhongDAO;
import dao.HoaDonDAO;
import dao.LoaiPhongDAO;
import model.entities.*;
import model.enums.TrangThaiPhong;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CheckInView – JavaFX
 * Cung cấp giao diện làm thủ tục nhận phòng cho khách đã đặt trước.
 */
public class CheckInView extends BorderPane {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_GREEN = "#16a34a";

    /* ── DAO ────────────────────────────────────────────────────────── */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();

    /* ── Controls ───────────────────────────────────────────────────── */
    private TextField txtSearch;
    private FlowPane quickSearchFlow;
    private VBox detailSection;
    private FlowPane roomFlow;
    private ComboBox<String> cbRoomType;
    private Button btnConfirm;

    // Data hiện tại
    private DatPhong currentDatPhong;
    private Set<Phong> selectedRooms = new HashSet<>();

    public CheckInView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildMainContent());

        resetView();
    }

    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 24, 0));

        // Tiêu đề
        VBox titleBox = new VBox(4);
        Label lblTitle = new Label("Thủ tục nhận phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSubtitle = new Label("Tìm khách hàng và gán phòng để hoàn tất check-in");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        // Thanh tìm kiếm
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Nhập mã đặt phòng hoặc số điện thoại...");
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

        // Gợi ý nhanh
        VBox quickBox = new VBox(8);
        Label lblQuick = new Label("Gợi ý đơn hôm nay:");
        lblQuick.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblQuick.setTextFill(Color.web(C_TEXT_GRAY));

        quickSearchFlow = new FlowPane(10, 10);
        loadQuickSuggestions();

        quickBox.getChildren().addAll(lblQuick, quickSearchFlow);

        header.getChildren().addAll(titleBox, searchRow, quickBox);
        return header;
    }

    private HBox buildMainContent() {
        HBox main = new HBox(24);
        main.setAlignment(Pos.TOP_LEFT);

        // CỘT TRÁI: Thông tin đơn đặt
        VBox leftCol = new VBox(16);
        leftCol.setMinWidth(400);
        leftCol.setPrefWidth(450);

        detailSection = new VBox(16);
        detailSection.setPadding(new Insets(24));
        detailSection.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER
                + "; -fx-border-radius: 12;");
        detailSection.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        detailSection.setAlignment(Pos.CENTER);
        Label lblNoData = new Label("Chưa có thông tin đơn đặt phòng");
        lblNoData.setTextFill(Color.web(C_TEXT_GRAY));
        detailSection.getChildren().add(lblNoData);

        leftCol.getChildren().add(detailSection);

        // CỘT PHẢI: Chọn phòng
        VBox rightCol = new VBox(16);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox roomContainer = new VBox(16);
        roomContainer.setPadding(new Insets(24));
        roomContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + C_BORDER
                + "; -fx-border-radius: 12;");
        roomContainer.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));
        VBox.setVgrow(roomContainer, Priority.ALWAYS);

        Label lblRoomTitle = new Label("Lựa chọn phòng trống");
        lblRoomTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblRoomTitle.setTextFill(Color.web(C_TEXT_DARK));

        // Thanh công cụ chọn phòng
        HBox roomToolbar = new HBox(12);
        roomToolbar.setAlignment(Pos.CENTER_LEFT);

        Label lblFilter = new Label("Lọc theo loại:");
        lblFilter.setFont(Font.font("Segoe UI", 13));
        lblFilter.setTextFill(Color.web(C_TEXT_GRAY));

        cbRoomType = new ComboBox<>();
        cbRoomType.getItems().add("Tất cả");
        cbRoomType.getItems().addAll(loaiPhongDAO.fetchAllRoomTypeNames());
        cbRoomType.setValue("Tất cả");
        cbRoomType.setPrefHeight(36);
        cbRoomType.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        cbRoomType.setOnAction(e -> applyFilter());

        roomToolbar.getChildren().addAll(lblFilter, cbRoomType);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        roomFlow = new FlowPane(20, 20);
        roomFlow.setPadding(new Insets(10));
        scroll.setContent(roomFlow);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Thanh hành động
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

        btnConfirm = new Button("XÁC NHẬN NHẬN PHÒNG");
        btnConfirm.setPrefHeight(44);
        btnConfirm.setMinWidth(220);
        btnConfirm.setCursor(Cursor.HAND);
        styleButton(btnConfirm, C_GREEN, "white", "#15803d");
        btnConfirm.setDisable(true);
        btnConfirm.setOnAction(e -> handleConfirm());

        actionRow.getChildren().addAll(btnCancel, btnConfirm);

        roomContainer.getChildren().addAll(lblRoomTitle, roomToolbar, scroll, actionRow);
        rightCol.getChildren().add(roomContainer);

        main.getChildren().addAll(leftCol, rightCol);
        return main;
    }

    private void loadQuickSuggestions() {
        quickSearchFlow.getChildren().clear();
        List<String> suggestions = datPhongDAO.getMaDatPhongCheckInHomNay();
        if (suggestions.isEmpty()) {
            Label lbl = new Label("Không có đơn chờ hôm nay");
            lbl.setFont(Font.font("Segoe UI", 13));
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            quickSearchFlow.getChildren().add(lbl);
        } else {
            for (String s : suggestions) {
                Button b = new Button(s);
                b.setCursor(Cursor.HAND);
                b.setStyle("-fx-background-color: white; -fx-border-color: " + C_BLUE + "; -fx-text-fill: " + C_BLUE
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
            updateDetailUI(dp);
            loadAvailableRooms();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Không tìm thấy đơn đặt phòng");
            alert.setContentText("Vùi lòng kiểm tra lại mã đặt hoặc số điện thoại.");
            alert.showAndWait();
        }
    }

    private void updateDetailUI(DatPhong dp) {
        detailSection.getChildren().clear();
        detailSection.setAlignment(Pos.TOP_LEFT);

        Label lblHeader = new Label("Chi tiết đơn đặt");
        lblHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblHeader.setTextFill(Color.web(C_NAVY));
        lblHeader.setPadding(new Insets(0, 0, 8, 0));

        VBox infoBox = new VBox(12);
        infoBox.getChildren().addAll(
                lblHeader,
                createDetailItem("🏠 Mã đơn", dp.getMaDat()),
                createDetailItem("👤 Khách hàng", dp.getKhachHang().getTenKH()),
                createDetailItem("📞 Số điện thoại", dp.getKhachHang().getSoDT()),
                createDetailItem("🆔 Số CCCD", dp.getKhachHang().getSoCCCD()));

        Separator sep = new Separator();
        sep.setPadding(new Insets(8, 0, 8, 0));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        VBox scheduleBox = new VBox(12);
        scheduleBox.getChildren().addAll(
                createDetailItem("📅 Ngày nhận", dp.getNgayCheckIn() != null ? dp.getNgayCheckIn().format(dtf) : "—"),
                createDetailItem("📅 Ngày trả dự kiến",
                        dp.getNgayCheckOut() != null ? dp.getNgayCheckOut().format(dtf) : "—"));

        detailSection.getChildren().addAll(infoBox, sep, scheduleBox);
    }

    private HBox createDetailItem(String label, String value) {
        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setMinWidth(120);
        lbl.setTextFill(Color.web(C_TEXT_GRAY));
        lbl.setFont(Font.font("Segoe UI", 14));

        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        val.setTextFill(Color.web(C_TEXT_DARK));

        hb.getChildren().addAll(lbl, val);
        return hb;
    }

    private void applyFilter() {
        loadAvailableRooms();
    }

    private void loadAvailableRooms() {
        roomFlow.getChildren().clear();
        String filterType = cbRoomType.getValue();
        if (filterType == null) filterType = "Tất cả";

        String finalFilterType = filterType;
        List<Phong> rooms = phongDAO.getAll().stream()
                .filter(p -> p.getTinhTrang() == TrangThaiPhong.CONTRONG)
                .filter(p -> "Tất cả".equals(finalFilterType)
                        || p.getLoaiPhong().toString().equalsIgnoreCase(finalFilterType))
                .collect(Collectors.toList());

        if (rooms.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label lbl = new Label("Không tìm thấy phòng phù hợp!");
            lbl.setTextFill(Color.web(C_TEXT_GRAY));
            empty.getChildren().add(lbl);
            roomFlow.getChildren().add(empty);
        } else {
            for (Phong p : rooms) {
                roomFlow.getChildren().add(createRoomCard(p));
            }
        }
    }

    private VBox createRoomCard(Phong p) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(160, 120);
        card.setPadding(new Insets(12));
        
        // Trạng thái ban đầu dựa trên selection
        if (selectedRooms.contains(p)) {
            card.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: " + C_BLUE
                    + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-width: 2;");
        } else {
            card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER
                    + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        }
        
        card.setCursor(Cursor.HAND);
        card.setEffect(new DropShadow(5, 0, 2, Color.web("#00000005")));

        Label lblId = new Label(p.getMaPhong());
        lblId.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblId.setTextFill(Color.web(C_NAVY));

        Label lblType = new Label(p.getLoaiPhong().getMaLoaiPhong());
        lblType.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblType.setTextFill(Color.web(C_BLUE));
        lblType.setStyle("-fx-background-color: #eff6ff; -fx-padding: 2 8; -fx-background-radius: 4;");

        Label lblPrice = new Label(String.format("%,.0f VNĐ", p.getLoaiPhong().getGia()));
        lblPrice.setFont(Font.font("Segoe UI", 12));
        lblPrice.setTextFill(Color.web(C_TEXT_GRAY));

        card.getChildren().addAll(lblId, lblType, lblPrice);

        card.setOnMouseClicked(e -> {
            if (selectedRooms.contains(p)) {
                selectedRooms.remove(p);
                card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER
                        + "; -fx-border-radius: 12; -fx-background-radius: 12;");
            } else {
                selectedRooms.add(p);
                card.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: " + C_BLUE
                        + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-width: 2;");
            }
            btnConfirm.setDisable(selectedRooms.isEmpty());
        });

        card.setOnMouseEntered(e -> {
            if (!selectedRooms.contains(p)) {
                card.setStyle(
                        "-fx-background-color: #f9fafb; -fx-border-color: " + C_BLUE + "; -fx-border-radius: 12; -fx-background-radius: 12;");
                card.setScaleX(1.02);
                card.setScaleY(1.02);
            }
        });
        card.setOnMouseExited(e -> {
            if (!selectedRooms.contains(p)) {
                card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER
                        + "; -fx-border-radius: 12; -fx-background-radius: 12;");
                card.setScaleX(1.0);
                card.setScaleY(1.0);
            }
        });

        return card;
    }

    private void handleConfirm() {
        if (currentDatPhong == null || selectedRooms.isEmpty())
            return;

        int count = selectedRooms.size();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận Check-in Nhóm");
        confirm.setHeaderText("Xác nhận cho khách " + currentDatPhong.getKhachHang().getTenKH() + " nhận " + count + " phòng?");
        confirm.setContentText("Hệ thống sẽ cập nhật trạng thái các phòng, tạo chi tiết đặt phòng và khởi tạo hóa đơn (nếu chưa có).");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    // Check if HoaDon already exists for this reservation
                    HoaDon hd = hoaDonDAO.getByMaDat(currentDatPhong.getMaDat());
                    if (hd == null) {
                        hd = new HoaDon();
                        hd.setMaHD(hoaDonDAO.generateMaHD());
                        hd.setDatPhong(currentDatPhong);
                        hd.setNhanVien(new NhanVien("LUCIA001")); // Mặc định nhân viên hệ thống
                        hd.setNgayTaoHD(java.time.LocalDateTime.now());
                        hd.setTienPhong(0.0);
                        hd.setTienDV(0.0);
                        hd.setTienCoc(0.0);
                        hd.setThueVAT(0.1);
                        hd.setTongTien(0.0);
                        hoaDonDAO.insert(hd);
                    }

                    int successCount = 0;
                    List<String> roomIds = new ArrayList<>();

                    for (Phong p : selectedRooms) {
                        // 1. Cập nhật trạng thái phòng
                        p.setTinhTrang(TrangThaiPhong.DACOKHACH);
                        boolean pUpdate = phongDAO.update(p);

                        // 2. Tạo ChiTietDatPhong
                        ChiTietDatPhong ctdp = new ChiTietDatPhong();
                        ctdp.setMaCTDP(chiTietDatPhongDAO.generateMaCTDP());
                        ctdp.setPhong(p);
                        ctdp.setDatPhong(currentDatPhong);
                        ctdp.setGiaCoc(0.0);
                        ctdp.setSoNguoi(p.getLoaiPhong().getSucChua());
                        ctdp.setGhiChu("Check-in nhóm tại quầy");
                        boolean cUpdate = chiTietDatPhongDAO.insert(ctdp);

                        if (pUpdate && cUpdate) {
                            successCount++;
                            roomIds.add(p.getMaPhong());
                        }
                    }

                    if (successCount == count) {
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Thành công");
                        success.setHeaderText("Check-in hoàn tất!");
                        success.setContentText("Các phòng [" + String.join(", ", roomIds) + "] đã được bàn giao cho khách "
                                + currentDatPhong.getKhachHang().getTenKH());
                        success.showAndWait();
                        resetView();
                        loadQuickSuggestions();
                    } else if (successCount > 0) {
                        Alert partial = new Alert(Alert.AlertType.WARNING);
                        partial.setTitle("Hoàn tất một phần");
                        partial.setHeaderText("Có lỗi xảy ra trong quá trình xử lý");
                        partial.setContentText("Đã check-in thành công " + successCount + "/" + count + " phòng: [" + String.join(", ", roomIds) + "]");
                        partial.showAndWait();
                        resetView();
                    } else {
                        throw new Exception("Không thể thực hiện check-in cho bất kỳ phòng nào.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Lỗi hệ thống");
                    error.setHeaderText("Không thể hoàn tất Check-in");
                    error.setContentText("Chi tiết lỗi: " + ex.getMessage());
                    error.showAndWait();
                }
            }
        });
    }

    private void resetView() {
        txtSearch.clear();
        currentDatPhong = null;
        selectedRooms.clear();
        btnConfirm.setDisable(true);

        detailSection.getChildren().clear();
        detailSection.setAlignment(Pos.CENTER);
        Label lblNoData = new Label("Chưa có thông tin đơn đặt phòng");
        lblNoData.setTextFill(Color.web(C_TEXT_GRAY));
        detailSection.getChildren().add(lblNoData);

        roomFlow.getChildren().clear();
        loadAvailableRooms();
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
