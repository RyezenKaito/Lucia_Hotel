package gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import javafx.scene.input.MouseButton;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.util.Optional;

import connectDatabase.ConnectDatabase;
import model.utils.DimOverlay;

/**
 * DatPhongView – Giao diện quản lý đặt phòng (JavaFX).
 * Cấu trúc đồng bộ với KhachHangView và NhanVienView.
 */
public class DatPhongView extends BorderPane {

    /* ── Bảng màu ────────────────────────────────────────────────── */
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_CONTENT_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_MUTED = "#6b7280";
    private static final String C_BLUE = "#2563eb";
    private static final String C_BLUE_HOVER = "#1d4ed8";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat DF = new DecimalFormat("#,###");

    /* ── State ────────────────────────────────────────────────────── */
    private TableView<Object[]> table;
    private ObservableList<Object[]> masterData = FXCollections.observableArrayList();
    private FilteredList<Object[]> filteredData;

    private Label lblTotal, lblDaDat, lblChoXacNhan, lblDangO, lblDaTra;
    private TextField txtSearch;

    private final dao.DatPhongDAO datPhongDAO = new dao.DatPhongDAO();

    /* ── Constructor ──────────────────────────────────────────────── */
    public DatPhongView() {
        setStyle("-fx-background-color: " + C_CONTENT_BG + ";");
        setPadding(new Insets(24, 32, 24, 32));

        setTop(buildHeader());
        setCenter(buildTable());

        loadData();
    }

    /* ── HEADER ────────────────────────────────────────────────────── */
    private VBox buildHeader() {
        VBox header = new VBox(14);
        header.setPadding(new Insets(0, 0, 18, 0));

        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("📅  Quản lý Đặt phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#1f2937"));

        Label lblSub = new Label("Danh sách đơn đặt phòng trong hệ thống");
        lblSub.setFont(Font.font("Segoe UI", 13));
        lblSub.setTextFill(Color.web(C_TEXT_MUTED));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Thêm đặt phòng");
        styleButton(btnAdd, C_BLUE, "white", C_BLUE_HOVER);
        btnAdd.setOnAction(e -> openAddDialog());

        row1.getChildren().addAll(titleBox, spacer, btnAdd);

        HBox row2 = new HBox(14);
        row2.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm theo mã đặt, tên KH, SĐT...");
        txtSearch.setPrefWidth(320);
        txtSearch.setPrefHeight(40);
        txtSearch.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER +
                "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-padding: 8 14 8 14;");
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        lblTotal = new Label("0");
        lblDaDat = new Label("0");
        lblChoXacNhan = new Label("0");
        lblDangO = new Label("0");
        lblDaTra = new Label("0");

        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.getChildren().addAll(
                buildStatCard("Tổng", lblTotal, "#1f2937"),
                buildStatCard("Chờ xác nhận", lblChoXacNhan, "#d97706"),
                buildStatCard("Đã xác nhận", lblDaDat, C_ACTIVE),
                buildStatCard("Đã nhận phòng", lblDangO, "#16a34a"),
                buildStatCard("Đã trả phòng", lblDaTra, C_TEXT_MUTED));

        row2.getChildren().addAll(txtSearch, spacer2, stats);
        header.getChildren().addAll(row1, row2);
        return header;
    }

    private VBox buildStatCard(String title, Label valueLbl, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 18, 10, 18));
        card.setMinWidth(110);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER +
                "; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web("#9ca3af"));

        valueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLbl.setTextFill(Color.web(color));

        card.getChildren().addAll(lbl, valueLbl);
        return card;
    }

    /* ── TABLE ────────────────────────────────────────────────────── */
    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER +
                "; -fx-border-radius: 10; -fx-background-radius: 10;");

        table.setRowFactory(tv -> {
            TableRow<Object[]> row = new TableRow<>() {
                @Override
                protected void updateItem(Object[] item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null)
                        setStyle("-fx-background-color: white;");
                }
            };
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty())
                    row.setStyle("-fx-background-color: #f0f4ff;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty())
                    row.setStyle("-fx-background-color: white;");
            });

            ContextMenu ctx = new ContextMenu();
            MenuItem miDetail = new MenuItem("Xem chi tiết");
            miDetail.setStyle("-fx-font-size: 13px;");
            miDetail.setOnAction(e -> {
                Object[] r = row.getItem();
                if (r != null)
                    openDetailDialog((String) r[0]);
            });

            MenuItem miCancel = new MenuItem("Hủy đơn đặt phòng");
            miCancel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ea580c;");
            miCancel.setOnAction(e -> {
                Object[] r = row.getItem();
                if (r != null)
                    confirmCancel(r);
            });

            MenuItem miDelete = new MenuItem("Xóa đơn đặt phòng");
            miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
            miDelete.setOnAction(e -> {
                Object[] r = row.getItem();
                if (r != null)
                    confirmDelete(r);
            });

            // KHÓA CHỨC NĂNG HỦY/XÓA DỰA TRÊN TRẠNG THÁI
            row.itemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String status = (String) newVal[8];
                    
                    // Chỉ cho hủy khi CHƯA nhận phòng (CHO_XACNHAN hoặc DA_XACNHAN)
                    boolean canCancel = "CHO_XACNHAN".equals(status) || "DA_XACNHAN".equals(status);
                    miCancel.setDisable(!canCancel);

                    // Chỉ cho xóa nếu KHÔNG phải đang ở hoặc đã trả phòng
                    boolean canDelete = !"DA_CHECKIN".equals(status) && !"DA_CHECKOUT".equals(status);
                    miDelete.setDisable(!canDelete);
                }
            });

            ctx.getItems().addAll(miDetail, miCancel, new SeparatorMenuItem(), miDelete);
            row.setContextMenu(ctx);


            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openDetailDialog((String) row.getItem()[0]);
                }
            });

            return row;
        });

        // ── Columns ──────────────────────────────────────────────────
        TableColumn<Object[], String> colSTT = new TableColumn<>("STT");
        colSTT.setCellValueFactory(c -> {
            int idx = table.getItems().indexOf(c.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(idx));
        });
        colSTT.setMinWidth(50);
        colSTT.setMaxWidth(60);
        colSTT.setStyle("-fx-alignment: CENTER;");
        colSTT.setReorderable(false); // KHÓA CỘT STT

        TableColumn<Object[], String> colMaDat = col("Mã đặt", 0, 100);
        colMaDat.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colTenKH = col("Khách hàng", 1, 160);
        TableColumn<Object[], String> colSDT = col("SĐT", 2, 110);
        colSDT.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colPhong = col("Phòng", 3, 90);
        colPhong.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colIn = col("Ngày nhận", 4, 130);
        colIn.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colOut = col("Ngày trả", 5, 130);
        colOut.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colNguoi = col("Số người", 6, 75);
        colNguoi.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colCoc = col("Tiền cọc", 7, 110);
        colCoc.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Trạng thái Badge
        TableColumn<Object[], String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setReorderable(false); // KHÓA CỘT TRẠNG THÁI
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[8]));
        colStatus.setMinWidth(110);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(mapTrangThai(item));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                String bg, fg;
                switch (item) {
                    case "DA_CHECKIN" -> {
                        bg = "#d1fae5";
                        fg = "#065f46";
                    }
                    case "DA_CHECKOUT" -> {
                        bg = "#f3f4f6";
                        fg = "#6b7280";
                    }
                    case "DA_HUY" -> {
                        bg = "#fee2e2";
                        fg = "#b91c1c";
                    }
                    case "DA_XACNHAN" -> {
                        bg = "#ecfdf5";
                        fg = "#10b981";
                    }
                    case "CHO_XACNHAN" -> {
                        bg = "#fef3c7";
                        fg = "#d97706";
                    }
                    case"HUY_HOAN_COC" -> {
                        bg = "#e0e7ff";
                        fg = "#3730a3";
                    }
                    case "HUY_MAT_COC" -> {
                        bg = "#fce7f3"; 
                        fg = "#be185d";
                    }
                    default -> {
                        bg = "#dbeafe";
                        fg = "#1e40af";
                    }
                }
                badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                        "; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(colSTT, colMaDat, colTenKH, colSDT, colPhong, colIn, colOut, colNguoi, colCoc,
                colStatus);

        Label placeholder = new Label("Không có dữ liệu đặt phòng");
        placeholder.setFont(Font.font("Segoe UI", 14));
        placeholder.setTextFill(Color.web("#9ca3af"));
        table.setPlaceholder(placeholder);

        VBox wrapper = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        wrapper.setPadding(new Insets(4, 0, 0, 0));
        return wrapper;
    }

    private TableColumn<Object[], String> col(String title, int idx, double minW) {
        TableColumn<Object[], String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue()[idx] != null ? cd.getValue()[idx].toString() : ""));
        c.setMinWidth(minW);
        c.setReorderable(false); // KHÓA CÁC CỘT CÒN LẠI (KHÔNG CHO KÉO THẢ)
        return c;
    }

    /* ── DATA ────────────────────────────────────────────────────── */
    public void loadData() {
        masterData.clear();
        long cntDaDat = 0, cntDangO = 0, cntDaTra = 0, cntChoXacNhan = 0;

        String sql = """
                SELECT dp.maDat, kh.tenKH, kh.soDT,
                       STRING_AGG(ctdp.maPhong, ', ') as maPhong,
                       dp.ngayCheckIn, dp.ngayCheckOut,
                       SUM(ctdp.soNguoi) as soNguoi,
                       SUM(ctdp.giaCoc) as giaCoc,
                       
                       -- Dùng EXISTS để tìm hóa đơn mà không làm nhân đôi dòng
                       CASE 
                           WHEN dp.trangThai = 'DA_HUY' AND EXISTS (SELECT 1 FROM HoaDon WHERE maDat = dp.maDat AND loaiHD = 'HOA_DON_HOAN_TIEN') THEN 'HUY_HOAN_COC'
                           WHEN dp.trangThai = 'DA_HUY' AND EXISTS (SELECT 1 FROM HoaDon WHERE maDat = dp.maDat AND loaiHD = 'HOA_DON_PHONG') THEN 'HUY_MAT_COC'
                           ELSE dp.trangThai
                       END AS trangThai,
                       
                       dp.ngayDat
                FROM DatPhong dp
                JOIN KH kh ON dp.maKH = kh.maKH
                LEFT JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat
                LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong
                GROUP BY dp.maDat, kh.tenKH, kh.soDT, dp.ngayCheckIn, dp.ngayCheckOut, dp.ngayDat, dp.trangThai
                ORDER BY dp.ngayDat DESC
                """;

        try (Connection con = ConnectDatabase.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String maDat = rs.getString("maDat");
                String tenKH = rs.getString("tenKH");
                String soDT = rs.getString("soDT");
                String maPhong = rs.getString("maPhong") != null ? rs.getString("maPhong") : "—";
                String checkIn = rs.getTimestamp("ngayCheckIn") != null ? rs.getTimestamp("ngayCheckIn").toLocalDateTime().format(FMT)
                        : "—";
                String checkOut = rs.getTimestamp("ngayCheckOut") != null
                        ? rs.getTimestamp("ngayCheckOut").toLocalDateTime().format(FMT)
                        : "—";
                String soNguoi = rs.getObject("soNguoi") != null ? String.valueOf(rs.getInt("soNguoi")) : "—";
                String giaCoc = rs.getObject("giaCoc") != null ? DF.format(rs.getDouble("giaCoc")) + " đ" : "0 đ";
                String tt = rs.getString("trangThai");
                LocalDateTime ngayDat = rs.getTimestamp("ngayDat") != null ? rs.getTimestamp("ngayDat").toLocalDateTime() : null;

                switch (tt) {
                    case "DA_CHECKIN" -> cntDangO++;
                    case "DA_CHECKOUT" -> cntDaTra++;
                    case "DA_XACNHAN" -> cntDaDat++;
                    case "CHO_XACNHAN" -> cntChoXacNhan++;
                }

                masterData.add(new Object[] { maDat, tenKH, soDT, maPhong, checkIn, checkOut, soNguoi, giaCoc, tt, ngayDat });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        lblTotal.setText(String.valueOf(cntDaDat + cntDangO + cntDaTra + cntChoXacNhan));
        lblDaDat.setText(String.valueOf(cntDaDat));
        lblChoXacNhan.setText(String.valueOf(cntChoXacNhan));
        lblDangO.setText(String.valueOf(cntDangO));
        lblDaTra.setText(String.valueOf(cntDaTra));
    }

    private void applyFilter(String keyword) {
        if (filteredData == null)
            return;
        if (keyword == null || keyword.isBlank()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = keyword.toLowerCase();
            filteredData.setPredicate(row -> ((String) row[0]).toLowerCase().contains(lower) ||
                    ((String) row[1]).toLowerCase().contains(lower) ||
                    ((String) row[2]).toLowerCase().contains(lower) ||
                    ((String) row[3]).toLowerCase().contains(lower));
        }
    }

    /* ── ACTIONS ────────────────────────────────────────────────────── */

    // Gọi form Thêm
    private void openAddDialog() {
        Window owner = getScene().getWindow();
        new ThemSuaDatPhongDialog(owner, this::loadData).showDialog();
    }

    private void openDetailDialog(String maDat) {
        if (maDat == null) return;
        Window owner = getScene().getWindow();
        new ThemSuaDatPhongDialog(owner, maDat, this::loadData).showDialog();
    }



private void confirmCancel(Object[] row) {
        String maDat = (String) row[0];
        String tenKH = (String) row[1];
        
        // 1. LẤY NGÀY CHECK-IN ĐỂ TÍNH TOÁN THEO NGHIỆP VỤ KHÁCH SẠN
        String checkInStr = (String) row[4]; // Cột 4 trên bảng là Chuỗi ngày Check-in
        if (checkInStr == null || checkInStr.equals("—")) {
            showError("Đơn này chưa có ngày Check-in, không thể tính toán!");
            return;
        }

        LocalDateTime ngayCheckIn;
        try {
            // Ép chuỗi "16/04/2026 14:00" về kiểu Ngày giờ
            ngayCheckIn = LocalDateTime.parse(checkInStr, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            showError("Lỗi hệ thống khi đọc định dạng ngày Check-in.");
            return;
        }

        // 2. TÍNH SỐ NGÀY TỪ HÔM NAY ĐẾN LÚC CHECK-IN
        // Tính bằng LocalDate để bỏ qua giờ phút, chỉ đếm ngày chênh lệch
        long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), ngayCheckIn.toLocalDate());
        
        // Hủy trước 3 ngày trở lên -> Hoàn. Dưới 3 ngày -> Mất.
        boolean duocHoanCoc = (daysUntilCheckIn >= 3);

        String msg = String.format(
                "Xác nhận HỦY đơn %s của khách %s?\n\n" +
                "📅 Ngày Check-in: %s\n" +
                "⏳ Khách hủy trước: %d ngày\n" +
                "💰 Chế độ: %s",
                maDat, tenKH, 
                checkInStr,
                daysUntilCheckIn,
                duocHoanCoc ? "Hủy sớm (>= 3 ngày) -> ĐƯỢC HOÀN CỌC ✅" : "Hủy sát ngày/Trễ hạn -> BỊ MẤT CỌC ❌"
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hủy đơn đặt phòng");
        alert.setContentText(msg);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = ConnectDatabase.getInstance().getConnection()) {
                con.setAutoCommit(false); 
                try {
                    // Chuyển đơn thành DA_HUY
                    try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = 'DA_HUY' WHERE maDat = ?")) {
                        ps.setString(1, maDat);
                        ps.executeUpdate();
                    }

                    // Trả lại phòng trống
                    try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET tinhTrang = N'CONTRONG' WHERE maPhong IN (SELECT maPhong FROM ChiTietDatPhong WHERE maDat = ?)")) {
                        ps.setString(1, maDat);
                        ps.executeUpdate();
                    }

                    // Lưu Hóa Đơn bằng chứng
                    // 3. CẬP NHẬT LẠI HÓA ĐƠN ĐÃ CÓ SẴN (Chuyển trạng thái từ Cọc -> Đã thanh toán)
                    String loaiHD = duocHoanCoc ? "HOA_DON_HOAN_TIEN" : "HOA_DON_PHONG";
                    String ghiChu = duocHoanCoc ? "Hoàn cọc (Hủy trước " + daysUntilCheckIn + " ngày)" : "Thu phạt (Hủy sát ngày/Trễ hạn)";
                    
                    // Dùng lệnh UPDATE để sửa chính hóa đơn gốc, không đẻ thêm hóa đơn mới
                    String sqlUpdateHD = "UPDATE HoaDon SET loaiHD = ?, trangThaiThanhToan = 'DA_THANH_TOAN', ghiChuThanhToan = ?, ngayThanhToan = GETDATE() WHERE maDat = ?";
                    
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateHD)) {
                        ps.setString(1, loaiHD);
                        ps.setString(2, ghiChu);
                        ps.setString(3, maDat); 
                        ps.executeUpdate();
                    }

                    con.commit();
                    loadData();
                    showInfo("Thành công", "Đã hủy đơn " + maDat + ". Hóa đơn tự động: " + (duocHoanCoc ? "HOÀN CỌC" : "THU PHẠT") + ".");
                } catch (Exception ex) {
                    con.rollback();
                    showError("Lỗi CSDL: " + ex.getMessage());
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void confirmDelete(Object[] row) {
        String maDat = (String) row[0];
        String tenKH = (String) row[1];

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xóa đơn " + maDat + " của " + tenKH + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = ConnectDatabase.getInstance().getConnection()) {
                con.setAutoCommit(false);
                try {
                    // Trước khi xóa, cập nhật trạng thái phòng về CONTRONG
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE Phong SET tinhTrang = N'CONTRONG' WHERE maPhong IN (SELECT maPhong FROM ChiTietDatPhong WHERE maDat = ?) AND tinhTrang = N'DANGSUDUNG'")) {
                        ps.setString(1, maDat);
                        ps.executeUpdate();
                    }

                    String[] sqls = {
                            "DELETE dvsd FROM DichVuSuDung dvsd JOIN ChiTietDatPhong ctdp ON dvsd.maCTDP = ctdp.maCTDP WHERE ctdp.maDat = ?",
                            "DELETE cthd FROM ChiTietHoaDon cthd JOIN HoaDon hd ON cthd.maHD = hd.maHD WHERE hd.maDat = ?",
                            "DELETE FROM HoaDon WHERE maDat = ?",
                            "DELETE FROM ChiTietDatPhong WHERE maDat = ?",
                            "DELETE FROM DatPhong WHERE maDat = ?"
                    };
                    for (String s : sqls) {
                        try (PreparedStatement ps = con.prepareStatement(s)) {
                            ps.setString(1, maDat);
                            ps.executeUpdate();
                        }
                    }
                    con.commit();
                    loadData();
                    showInfo("Thành công!", "Đã xóa đơn đặt phòng " + maDat + ".");
                } catch (Exception ex) {
                    con.rollback();
                    showError("Lỗi khi xóa: " + ex.getMessage());
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /* ── Helpers ────────────────────────────────────────────────────── */
    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bg, hoverBg)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverBg, bg)));
    }

    private String mapTrangThai(String key) {
        try {
            return model.enums.TrangThaiDatPhong.valueOf(key).getThongTinTrangThai();
        } catch (Exception e) {
            return key;
        }
    }

    private void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    private void showError(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}