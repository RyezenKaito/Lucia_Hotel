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

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.util.Optional;

import connectDatabase.ConnectDatabase;
import model.utils.DimOverlay; // Thêm import này

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

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat DF = new DecimalFormat("#,###");

    /* ── State ────────────────────────────────────────────────────── */
    private TableView<Object[]> table;
    private ObservableList<Object[]> masterData = FXCollections.observableArrayList();
    private FilteredList<Object[]> filteredData;

    private Label lblTotal, lblDaDat, lblDangO, lblDaTra;
    private TextField txtSearch;

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
        lblDangO = new Label("0");
        lblDaTra = new Label("0");

        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.getChildren().addAll(
                buildStatCard("Tổng", lblTotal, "#1f2937"),
                buildStatCard("Đã đặt", lblDaDat, C_ACTIVE),
                buildStatCard("Đang ở", lblDangO, "#16a34a"),
                buildStatCard("Đã trả", lblDaTra, C_TEXT_MUTED));

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
            MenuItem miEdit = new MenuItem("✏ Sửa đơn đặt phòng");
            miEdit.setOnAction(e -> {
                Object[] r = row.getItem();
                if (r != null)
                    openEditDialog(r);
            });

            MenuItem miDelete = new MenuItem("🗑 Xóa đơn đặt phòng");
            miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
            miDelete.setOnAction(e -> {
                Object[] r = row.getItem();
                if (r != null)
                    confirmDelete(r);
            });

            ctx.getItems().addAll(miEdit, new SeparatorMenuItem(), miDelete);
            row.setContextMenu(ctx);

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
        TableColumn<Object[], String> colIn = col("Ngày nhận", 4, 105);
        colIn.setStyle("-fx-alignment: CENTER;");
        TableColumn<Object[], String> colOut = col("Ngày trả", 5, 105);
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
                    case "DANG_O" -> {
                        bg = "#d1fae5";
                        fg = "#065f46";
                    }
                    case "DA_TRA" -> {
                        bg = "#f3f4f6";
                        fg = "#6b7280";
                    }
                    case "DA_HUY" -> {
                        bg = "#fee2e2";
                        fg = "#b91c1c";
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
        long cntDaDat = 0, cntDangO = 0, cntDaTra = 0;

        String sql = """
                SELECT dp.maDat, kh.tenKH, kh.soDT,
                       ctdp.maPhong, dp.ngayCheckIn, dp.ngayCheckOut,
                       ctdp.soNguoi, ctdp.giaCoc,
                       CASE
                           WHEN p.tinhTrang = N'DANGSUDUNG' THEN 'DANG_O'
                           WHEN dp.ngayCheckOut < GETDATE() THEN 'DA_TRA'
                           ELSE 'DA_DAT'
                       END AS trangThai
                FROM DatPhong dp
                JOIN KH kh ON dp.maKH = kh.maKH
                LEFT JOIN ChiTietDatPhong ctdp ON dp.maDat = ctdp.maDat
                LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong
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
                String checkIn = rs.getDate("ngayCheckIn") != null ? rs.getDate("ngayCheckIn").toLocalDate().format(FMT)
                        : "—";
                String checkOut = rs.getDate("ngayCheckOut") != null
                        ? rs.getDate("ngayCheckOut").toLocalDate().format(FMT)
                        : "—";
                String soNguoi = rs.getObject("soNguoi") != null ? String.valueOf(rs.getInt("soNguoi")) : "—";
                String giaCoc = rs.getObject("giaCoc") != null ? DF.format(rs.getDouble("giaCoc")) + " đ" : "0 đ";
                String tt = rs.getString("trangThai");

                switch (tt) {
                    case "DANG_O" -> cntDangO++;
                    case "DA_TRA" -> cntDaTra++;
                    default -> cntDaDat++;
                }

                masterData.add(new Object[] { maDat, tenKH, soDT, maPhong, checkIn, checkOut, soNguoi, giaCoc, tt });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        lblTotal.setText(String.valueOf(cntDaDat + cntDangO + cntDaTra));
        lblDaDat.setText(String.valueOf(cntDaDat));
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
        new ThemSuaDatPhongDialog(owner, null, this::loadData).showDialog();
    }

    // Gọi form Cập nhật
    private void openEditDialog(Object[] row) {
        Window owner = getScene().getWindow();
        new ThemSuaDatPhongDialog(owner, row, this::loadData).showDialog();
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
                            "DELETE dvsd FROM DichVuSuDung dvsd JOIN ChiTietHoaDon cthd ON dvsd.maCTHD = cthd.maCTHD JOIN HoaDon hd ON cthd.maHD = hd.maHD WHERE hd.maDat = ?",
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
        return switch (key) {
            case "DA_DAT" -> "Đã đặt";
            case "DANG_O" -> "Đang ở";
            case "DA_TRA" -> "Đã trả";
            case "DA_HUY" -> "Đã hủy";
            default -> key;
        };
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