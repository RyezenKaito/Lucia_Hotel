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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import dao.NhanVienDAO;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.utils.DimOverlay;

/**
 * 
 * NhanVienView – JavaFX (thay thế NhanVienPanel Swing).
 *
 * Quy tắc phân quyền:
 * ─ Staff (NHAN_VIEN): KHÔNG được truy cập view này.
 * → MainFrameView cần kiểm tra trước khi navigate.
 * ─ Manager (QUAN_LY): Xem danh sách KHÔNG bao gồm chính mình.
 * Chỉ thêm/sửa/xóa nhân viên (staff), không thao tác trên quản lý.
 */
public class NhanVienView extends BorderPane {

    /* ── Bảng màu (từ MainFrameView) ─────────────────────────────── */
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_CONTENT_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_GOLD = "#d4af37";
    private static final String C_TEXT_MUTED = "#6b7280";
    private static final String C_BLUE = "#2563eb";
    private static final String C_BLUE_HOVER = "#1d4ed8";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── State ────────────────────────────────────────────────────── */
    private final NhanVienDAO dao = new NhanVienDAO();
    private final NhanVien currentUser;
    /** true khi currentUser là ADMIN – toàn quyền */
    private final boolean isCurrentUserAdmin;

    private TableView<NhanVien> table;
    private ObservableList<NhanVien> masterData = FXCollections.observableArrayList();
    private FilteredList<NhanVien> filteredData;

    private Label lblTotal, lblStaff, lblManager, lblAdmin;
    private TextField txtSearch;

    /* ── Constructor ──────────────────────────────────────────────── */
    public NhanVienView(NhanVien currentUser) {
        this.currentUser = currentUser;
        this.isCurrentUserAdmin = (currentUser != null && currentUser.getRole() == ChucVu.ADMIN);
        setStyle("-fx-background-color: " + C_CONTENT_BG + ";");
        setPadding(new Insets(24, 32, 24, 32));

        setTop(buildHeader());
        setCenter(buildTable());

        loadData();
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * HEADER
     * ════════════════════════════════════════════════════════════════
     */
    private VBox buildHeader() {
        VBox header = new VBox(14);
        header.setPadding(new Insets(0, 0, 18, 0));

        /* ── Dòng 1: Tiêu đề + Nút thêm ──────────────────────────── */
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("👔  Quản lý Nhân viên");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#1f2937"));

        Label lblSub = new Label("Danh sách nhân viên trong hệ thống");
        lblSub.setFont(Font.font("Segoe UI", 13));
        lblSub.setTextFill(Color.web(C_TEXT_MUTED));
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Thêm nhân viên");
        btnAdd.setStyle(
                "-fx-background-color: " + C_SIDEBAR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 22 10 22;" +
                        "-fx-cursor: hand;");
        btnAdd.setOnMouseEntered(e -> btnAdd.setStyle(
                "-fx-background-color: " + C_ACTIVE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 22 10 22;" +
                        "-fx-cursor: hand;"));
        btnAdd.setOnMouseExited(e -> btnAdd.setStyle(
                "-fx-background-color: " + C_SIDEBAR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 22 10 22;" +
                        "-fx-cursor: hand;"));
        styleButton(btnAdd, C_BLUE, "white", C_BLUE_HOVER);
        btnAdd.setOnAction(e -> openAddDialog());

        row1.getChildren().addAll(titleBox, spacer, btnAdd);

        /* ── Dòng 2: Search + Stat Cards ──────────────────────────── */
        HBox row2 = new HBox(14);
        row2.setAlignment(Pos.CENTER_LEFT);

        // Search
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm theo mã hoặc tên...");
        txtSearch.setPrefWidth(320);
        txtSearch.setPrefHeight(40);
        txtSearch.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 14 8 14;");
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        lblTotal = new Label("0");
        lblStaff = new Label("0");
        lblManager = new Label("0");
        lblAdmin = new Label("0");

        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.getChildren().addAll(
                buildStatCard("Tổng", lblTotal, "#1f2937"),
                buildStatCard("Nhân viên", lblStaff, C_ACTIVE),
                buildStatCard("Quản lý", lblManager, C_GOLD),
                buildStatCard("Admin", lblAdmin, "#7c3aed"));

        row2.getChildren().addAll(txtSearch, spacer2, stats);

        header.getChildren().addAll(row1, row2);
        return header;
    }

    private VBox buildStatCard(String title, Label valueLbl, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 18, 10, 18));
        card.setMinWidth(110);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;");

        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web("#9ca3af"));

        valueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLbl.setTextFill(Color.web(color));

        card.getChildren().addAll(lbl, valueLbl);
        return card;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * TABLE
     * ════════════════════════════════════════════════════════════════
     */
    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;");
        table.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();
            row.setStyle("-fx-background-color: white;");
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty())
                    row.setStyle("-fx-background-color: #f0f4ff;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty())
                    row.setStyle("-fx-background-color: white;");
            });
            return row;
        });

        // Columns
        TableColumn<NhanVien, String> colSTT = new TableColumn<>("STT");
        colSTT.setCellValueFactory(c -> {
            int idx = table.getItems().indexOf(c.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(idx));
        });
        colSTT.setMinWidth(50);
        colSTT.setMaxWidth(60);
        colSTT.setStyle("-fx-alignment: CENTER;");

        TableColumn<NhanVien, String> colMa = new TableColumn<>("Mã NV");
        colMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaNV()));
        colMa.setMinWidth(90);
        colMa.setStyle("-fx-alignment: CENTER;");

        TableColumn<NhanVien, String> colTen = new TableColumn<>("Họ và tên");
        colTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHoTen()));
        colTen.setMinWidth(150);

        TableColumn<NhanVien, String> colCCCD = new TableColumn<>("Số CCCD");
        colCCCD.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCccd() != null ? c.getValue().getCccd() : ""));
        colCCCD.setMinWidth(100);
        colCCCD.setStyle("-fx-alignment: CENTER;");

        TableColumn<NhanVien, String> colSDT = new TableColumn<>("Số điện thoại");
        colSDT.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSoDT() != null ? c.getValue().getSoDT() : ""));
        colSDT.setMinWidth(100);
        colSDT.setStyle("-fx-alignment: CENTER;");

        TableColumn<NhanVien, String> colNS = new TableColumn<>("Ngày sinh");
        colNS.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgaySinh() != null
                        ? c.getValue().getNgaySinh().format(FMT)
                        : ""));
        colNS.setMinWidth(90);
        colNS.setStyle("-fx-alignment: CENTER;");

        TableColumn<NhanVien, String> colNgayVao = new TableColumn<>("Ngày vào làm");
        colNgayVao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgayVaoLamDate() != null
                        ? c.getValue().getNgayVaoLamDate().format(FMT)
                        : ""));
        colNgayVao.setMinWidth(100);
        colNgayVao.setStyle("-fx-alignment: CENTER;");

        TableColumn<NhanVien, String> colChucVu = new TableColumn<>("Chức vụ");
        colChucVu.setCellValueFactory(c -> {
            ChucVu role = c.getValue().getRole();
            if (role == ChucVu.ADMIN)
                return new SimpleStringProperty("ADMIN");
            if (role == ChucVu.QUAN_LY)
                return new SimpleStringProperty("QUẢN LÝ");
            return new SimpleStringProperty("NHÂN VIÊN");
        });
        colChucVu.setMinWidth(100);
        colChucVu.setStyle("-fx-alignment: CENTER;");
        colChucVu.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                switch (item) {
                    case "ADMIN" -> badge.setStyle(
                            "-fx-background-color: #ede9fe;" +
                                    "-fx-text-fill: #5b21b6;" +
                                    "-fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    case "QUẢN LÝ" -> badge.setStyle(
                            "-fx-background-color: #fef3c7;" +
                                    "-fx-text-fill: #92400e;" +
                                    "-fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    default -> badge.setStyle(
                            "-fx-background-color: #dbeafe;" +
                                    "-fx-text-fill: #1e40af;" +
                                    "-fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                }
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(c -> {
            model.enums.TrangThaiNV tt = c.getValue().getTrangThai();
            return new SimpleStringProperty(tt == model.enums.TrangThaiNV.CON_LAM ? "Còn làm" : "Đã nghỉ");
        });
        colTrangThai.setMinWidth(100);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                if ("Còn làm".equals(item)) {
                    badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                } else {
                    badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                }
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(colSTT, colMa, colTen, colCCCD, colSDT, colNS, colNgayVao, colChucVu, colTrangThai);

        // ── Context Menu (Click chuột phải) ──────────────────────── */
        table.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>() {
                @Override
                protected void updateItem(NhanVien item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("-fx-background-color: white;");
                    }
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

            // Context Menu
            ContextMenu ctx = new ContextMenu();

            MenuItem miEdit = new MenuItem("✏ Cập nhật thông tin");
            miEdit.setOnAction(e -> {
                NhanVien nv = row.getItem();
                if (nv != null)
                    openEditDialog(nv);
            });

            MenuItem miDelete = new MenuItem("🗑 Xóa nhân viên");
            miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
            miDelete.setOnAction(e -> {
                NhanVien nv = row.getItem();
                if (nv != null)
                    confirmDelete(nv);
            });

            MenuItem miView = new MenuItem("Xem chi tiết");
            miView.setOnAction(e -> {
                NhanVien nv = row.getItem();
                if (nv != null)
                    openViewDetail(nv);
            });

            ctx.getItems().addAll(miView, miEdit, new SeparatorMenuItem(), miDelete);

            row.setContextMenu(ctx);

            // Trước khi hiện context menu → kiểm tra quyền
            row.setOnContextMenuRequested(e -> {
                NhanVien nv = row.getItem();
                if (nv == null)
                    return;

                boolean isTargetManager = (nv.getRole() == ChucVu.QUAN_LY || nv.getRole() == ChucVu.ADMIN);

                // ADMIN: full quyền sửa/xóa tất cả
                // QUAN_LY: chỉ sửa/xóa nhân viên
                miEdit.setDisable(!isCurrentUserAdmin && isTargetManager);
                miDelete.setDisable(!isCurrentUserAdmin && isTargetManager);
            });

            return row;
        });

        // Placeholder khi bảng trống
        Label placeholder = new Label("Không có dữ liệu nhân viên");
        placeholder.setFont(Font.font("Segoe UI", 14));
        placeholder.setTextFill(Color.web("#9ca3af"));
        table.setPlaceholder(placeholder);

        VBox wrapper = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        wrapper.setPadding(new Insets(4, 0, 0, 0));
        return wrapper;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * DATA
     * ════════════════════════════════════════════════════════════════
     */
    public void loadData() {
        List<NhanVien> all = dao.getAll();
        masterData.clear();

        long totalStaff = 0, totalManager = 0, totalAdmin = 0;

        for (NhanVien nv : all) {
            // ── TÍNH THỐNG KÊ TOÀN BỘ ──
            if (nv.getRole() == ChucVu.ADMIN)
                totalAdmin++;
            else if (nv.getRole() == ChucVu.QUAN_LY)
                totalManager++;
            else
                totalStaff++;

            // Phân quyền: QUẢN LÝ chỉ được XEM role NHÂN VIÊN
            if (!isCurrentUserAdmin && nv.getRole() != ChucVu.NHAN_VIEN)
                continue;

            // Ẩn chính mình khỏi danh sách hiển thị
            if (currentUser != null && nv.getMaNV().equals(currentUser.getMaNV()))
                continue;

            masterData.add(nv);
        }

        filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        lblTotal.setText(String.valueOf(totalStaff + totalManager + totalAdmin));
        lblStaff.setText(String.valueOf(totalStaff));
        lblManager.setText(String.valueOf(totalManager));
        lblAdmin.setText(String.valueOf(totalAdmin));
    }

    private void applyFilter(String keyword) {
        if (filteredData == null)
            return;
        if (keyword == null || keyword.isBlank()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = keyword.toLowerCase();
            filteredData.setPredicate(nv -> nv.getMaNV().toLowerCase().contains(lower) ||
                    nv.getHoTen().toLowerCase().contains(lower) ||
                    (nv.getSoDT() != null && nv.getSoDT().contains(lower)));
        }
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * ACTIONS
     * ════════════════════════════════════════════════════════════════
     */
    private void openAddDialog() {
        Window owner = getScene().getWindow();
        Region overlay = DimOverlay.show(owner);
        new ThemSuaNhanVienDialog(owner, null, currentUser, this).showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    private void openEditDialog(NhanVien nv) {
        // ADMIN có thể sửa tất cả; QUAN_LY chỉ sửa NHAN_VIEN
        if (!isCurrentUserAdmin && nv.getRole() != ChucVu.NHAN_VIEN) {
            showAlert("Không thể sửa", "Bạn không có quyền chỉnh sửa quản lý cùng cấp hoặc cao hơn.");
            return;
        }
        Window owner = getScene().getWindow();
        Region overlay = DimOverlay.show(owner);
        new ThemSuaNhanVienDialog(owner, nv, currentUser, this).showAndWait();
        DimOverlay.hide(owner, overlay);
    }

    private void openViewDetail(NhanVien nv) {
        Window owner = getScene().getWindow();
        Region overlay = DimOverlay.show(owner);

        ThongTinCaNhanView detailView = new ThongTinCaNhanView(owner, nv);
        detailView.showAndWait();

        DimOverlay.hide(owner, overlay);
    }

    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        btn.setStyle("-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bg, hoverBg)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverBg, bg)));
    }

    private void confirmDelete(NhanVien nv) {
        // ADMIN có thể xóa tất cả; QUAN_LY chỉ xóa NHAN_VIEN
        if (!isCurrentUserAdmin && nv.getRole() != ChucVu.NHAN_VIEN) {
            showAlert("Không thể xóa", "Bạn không có quyền xóa quản lý cùng cấp hoặc cao hơn.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn xóa: " + nv.getHoTen() + " (" + nv.getRole() + ") ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.delete(nv.getMaNV());
            loadData();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}