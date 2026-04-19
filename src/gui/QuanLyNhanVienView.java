package gui; // Re-index 2026-04-18 05:58 AM

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import dao.NhanVienDAO;
import model.entities.NhanVien;
import model.enums.ChucVu;
import model.utils.BadgeUtils;
import model.utils.DimOverlay;

/**
 * NhanVienView – JavaFX (thay thế NhanVienPanel Swing).
 *
 * Quy tắc phân quyền:
 * ─ Staff (NHAN_VIEN): KHÔNG được truy cập view này.
 * → MainFrameView cần kiểm tra trước khi navigate.
 * ─ Manager (QUAN_LY): Xem danh sách KHÔNG bao gồm chính mình.
 * Chỉ thêm/sửa/xóa nhân viên (staff), không thao tác trên quản lý.
 *
 * Thiết kế đồng bộ với KhachHangView & QuanLyPhongView.
 */
public class QuanLyNhanVienView extends BorderPane {

    /* ── Bảng màu (đồng bộ KhachHangView & MainFrameView) ────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD_BG = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_RED = "#dc2626";
    private static final String C_GOLD = "#d97706";
    private static final String C_GREEN = "#16a34a";
    private static final String C_PURPLE = "#7c3aed";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── State ────────────────────────────────────────────────────── */
    private final NhanVienDAO dao = new NhanVienDAO();
    private final NhanVien currentUser;
    /** true khi currentUser là ADMIN – toàn quyền */
    private final boolean isCurrentUserAdmin;

    private TableView<NhanVien> table;
    private ObservableList<NhanVien> masterData = FXCollections.observableArrayList();
    private FilteredList<NhanVien> filteredData;

    private Label lblTotal, lblStaff, lblManager;
    private TextField txtSearch;

    /* ── Constructor ──────────────────────────────────────────────── */
    public QuanLyNhanVienView(NhanVien currentUser) {
        this.currentUser = currentUser;
        this.isCurrentUserAdmin = (currentUser != null && currentUser.getRole() == ChucVu.ADMIN);
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildTableCard());

        loadData();
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * HEADER: Tiêu đề + Nút thêm + Thẻ thống kê + Thanh tìm kiếm
     * ══════════════════════════════════════════════════════════════════
     */
    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 12, 0));

        /* ── Dòng 1: Tiêu đề + Nút thêm ─────────────────────────── */
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label lblTitle = new Label("Quản lý nhân viên");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSubtitle = new Label("Quản lý thông tin và hồ sơ nhân viên khách sạn");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        Button btnAdd = new Button("＋  Thêm nhân viên");
        btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnAdd.setPrefHeight(40);
        btnAdd.setCursor(Cursor.HAND);
        styleButton(btnAdd, C_BLUE, "white", C_BLUE_HOVER);
        btnAdd.setOnAction(e -> openAddDialog());

        titleRow.getChildren().addAll(titleBox, btnAdd);

        /* ── Dòng 2: Thẻ thống kê ────────────────────────────────── */
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        lblTotal = new Label("0");
        lblStaff = new Label("0");
        lblManager = new Label("0");

        VBox c1 = createStatCard("👥", "TỔNG NHÂN VIÊN", lblTotal, C_NAVY);
        VBox c2 = createStatCard("👔", "NHÂN VIÊN", lblStaff, C_BLUE);
        VBox c3 = createStatCard("⭐", "QUẢN LÝ", lblManager, C_GOLD);

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        statsRow.getChildren().addAll(c1, c2, c3);

        /* ── Dòng 3: Thanh tìm kiếm ──────────────────────────────── */
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm kiếm theo mã, tên, SĐT nhân viên...");
        txtSearch.setFont(Font.font("Segoe UI", 14));
        txtSearch.setPrefHeight(42);
        txtSearch.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 16 0 16;");
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n));

        header.getChildren().addAll(titleRow, statsRow, txtSearch);
        return header;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * BẢNG DỮ LIỆU (đồng bộ KhachHangView)
     * ══════════════════════════════════════════════════════════════════
     */
    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;");
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000010")));

        table = new TableView<>();
        table.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-table-cell-border-color: " + C_BORDER + ";");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));

        // ── Định nghĩa các cột ──────────────────────────────────────

        TableColumn<NhanVien, Void> colSTT = new TableColumn<>("STT");
        colSTT.setMinWidth(60);
        colSTT.setMaxWidth(60);
        colSTT.setResizable(false);
        colSTT.setStyle("-fx-alignment: CENTER;");
        colSTT.setCellFactory(col -> new TableCell<>() {
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<NhanVien, String> colMa = new TableColumn<>("Mã NV");
        colMa.setMinWidth(100);
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        colMa.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getMaNV())));

        TableColumn<NhanVien, String> colTen = new TableColumn<>("Họ và tên");
        colTen.setMinWidth(200);
        colTen.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 20;");
        colTen.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getHoTen())));

        TableColumn<NhanVien, String> colCCCD = new TableColumn<>("Số CCCD");
        colCCCD.setMinWidth(130);
        colCCCD.setStyle("-fx-alignment: CENTER;");
        colCCCD.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getCccd())));

        TableColumn<NhanVien, String> colSDT = new TableColumn<>("Số điện thoại");
        colSDT.setMinWidth(120);
        colSDT.setStyle("-fx-alignment: CENTER;");
        colSDT.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getSoDT())));

        TableColumn<NhanVien, String> colNS = new TableColumn<>("Ngày sinh");
        colNS.setMinWidth(100);
        colNS.setStyle("-fx-alignment: CENTER;");
        colNS.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgaySinh() != null ? c.getValue().getNgaySinh().format(FMT) : ""));

        TableColumn<NhanVien, String> colNgayVao = new TableColumn<>("Ngày vào làm");
        colNgayVao.setMinWidth(110);
        colNgayVao.setStyle("-fx-alignment: CENTER;");
        colNgayVao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgayVaoLamDate() != null ? c.getValue().getNgayVaoLamDate().format(FMT) : ""));

        TableColumn<NhanVien, String> colChucVu = new TableColumn<>("Chức vụ");
        colChucVu.setMinWidth(110);
        colChucVu.setStyle("-fx-alignment: CENTER;");
        colChucVu.setCellValueFactory(c -> {
            ChucVu role = c.getValue().getRole();
            if (role == ChucVu.QUAN_LY)
                return new SimpleStringProperty("QUẢN LÝ");
            return new SimpleStringProperty("NHÂN VIÊN");
        });
        colChucVu.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String bg, text;
                switch (item) {
                    case "QUẢN LÝ" -> {
                        bg = "#fef3c7";
                        text = "#92400e";
                    }
                    default -> {
                        bg = "#dbeafe";
                        text = "#1e40af";
                    }
                }

                HBox badge = model.utils.BadgeUtils.createStatusBadge(item, bg, text, false);
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setMinWidth(120);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(c -> {
            model.enums.TrangThaiNV tt = c.getValue().getTrangThai();
            return new SimpleStringProperty(tt == model.enums.TrangThaiNV.CON_LAM ? "Còn làm" : "Đã nghỉ");
        });

        // ── Custom Header Labels (Bold & Aligned) ──────────────────
        for (TableColumn<NhanVien, ?> c : List.of(colSTT, colMa, colTen, colCCCD, colSDT, colNS, colNgayVao, colChucVu, colTrangThai)) {
            Label lblHeader = new Label(c.getText());
            lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: " + C_TEXT_DARK + ";");
            if (c == colTen) {
                lblHeader.setPadding(new Insets(0, 0, 0, 15));
                c.setGraphic(new StackPane(lblHeader));
                ((StackPane) c.getGraphic()).setAlignment(Pos.CENTER_LEFT);
            } else {
                c.setGraphic(new StackPane(lblHeader));
                ((StackPane) c.getGraphic()).setAlignment(Pos.CENTER);
            }
            c.setText(""); // Xóa text gốc để hiển thị Graphic
        }
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String bg = "Còn làm".equals(item) ? "#d1fae5" : "#fee2e2";
                String text = "Còn làm".equals(item) ? "#065f46" : "#b91c1c";

                HBox badge = model.utils.BadgeUtils.createStatusBadge(item, bg, text, true);

                // Click để đổi trạng thái
                ContextMenu statusMenu = new ContextMenu();
                MenuItem mConLam = new MenuItem("✅  Còn làm");
                mConLam.setOnAction(ev -> {
                    NhanVien nv = getTableRow().getItem();
                    if (nv != null)
                        updateTrangThai(nv, model.enums.TrangThaiNV.CON_LAM);
                });
                MenuItem mDaNghi = new MenuItem("🚪  Đã nghỉ việc");
                mDaNghi.setOnAction(ev -> {
                    NhanVien nv = getTableRow().getItem();
                    if (nv != null)
                        updateTrangThai(nv, model.enums.TrangThaiNV.DA_NGHI);
                });
                statusMenu.getItems().addAll(mConLam, mDaNghi);

                badge.setOnMouseClicked(ev -> {
                    if (ev.getButton() == MouseButton.PRIMARY) {
                        statusMenu.show(badge, ev.getScreenX(), ev.getScreenY());
                    }
                });

                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        // Khóa cứng tất cả cột – không cho kéo thả, không cho sort
        for (TableColumn<NhanVien, ?> c : List.of(colSTT, colMa, colTen, colCCCD, colSDT, colNS, colNgayVao, colChucVu,
                colTrangThai)) {
            c.setReorderable(false);
            c.setSortable(false);
        }

        table.getColumns().addAll(colSTT, colMa, colTen, colCCCD, colSDT, colNS, colNgayVao, colChucVu, colTrangThai);

        // ── Context Menu (chuột phải / double click) ────────────────
        ContextMenu ctx = new ContextMenu();

        MenuItem miView = new MenuItem("👁  Xem chi tiết");
        miView.setStyle("-fx-font-size: 13px;");
        miView.setOnAction(e -> {
            NhanVien nv = table.getSelectionModel().getSelectedItem();
            if (nv != null)
                openViewDetail(nv);
        });

        MenuItem miEdit = new MenuItem("✏  Cập nhật thông tin");
        miEdit.setStyle("-fx-font-size: 13px;");
        miEdit.setOnAction(e -> {
            NhanVien nv = table.getSelectionModel().getSelectedItem();
            if (nv != null)
                openEditDialog(nv);
        });

        MenuItem miDelete = new MenuItem("🗑  Xóa nhân viên");
        miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
        miDelete.setOnAction(e -> {
            NhanVien nv = table.getSelectionModel().getSelectedItem();
            if (nv != null)
                confirmDelete(nv);
        });

        ctx.getItems().addAll(miView, miEdit, new SeparatorMenuItem(), miDelete);

        table.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    // Phân quyền trước khi show menu
                    NhanVien nv = row.getItem();
                    if (nv != null) {
                        boolean isTargetManager = (nv.getRole() == ChucVu.QUAN_LY || nv.getRole() == ChucVu.ADMIN);
                        miEdit.setDisable(!isCurrentUserAdmin && isTargetManager);
                        miDelete.setDisable(!isCurrentUserAdmin && isTargetManager);
                    }
                    ctx.show(row, event.getScreenX(), event.getScreenY());
                } else if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openEditDialog(row.getItem());
                } else {
                    ctx.hide();
                }
            });

            return row;
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        card.getChildren().add(table);
        return card;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * THẺ THỐNG KÊ (đồng bộ KhachHangView)
     * ══════════════════════════════════════════════════════════════════
     */
    private VBox createStatCard(String icon, String title, Label valueLbl, String accentHex) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;");
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000018")));

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTitle.setTextFill(Color.web(C_TEXT_GRAY));

        valueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLbl.setTextFill(Color.web(C_TEXT_DARK));

        textBox.getChildren().addAll(lblTitle, valueLbl);

        StackPane badge = new StackPane();
        badge.setMinSize(46, 46);
        badge.setPrefSize(46, 46);
        Rectangle badgeBg = new Rectangle(46, 46);
        badgeBg.setArcWidth(10);
        badgeBg.setArcHeight(10);
        Color accent = Color.web(accentHex);
        badgeBg.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.12));
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("Segoe UI Emoji", 22));
        badge.getChildren().addAll(badgeBg, iconLbl);

        topRow.getChildren().addAll(textBox, badge);

        Region bar = new Region();
        bar.setPrefHeight(3);
        bar.setStyle("-fx-background-color: " + accentHex + "; -fx-background-radius: 2;");

        card.getChildren().addAll(topRow, bar);
        return card;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * DATA
     * ══════════════════════════════════════════════════════════════════
     */
    public void loadData() {
        List<NhanVien> all = dao.getAll();
        masterData.clear();

        long totalStaff = 0, totalManager = 0;

        for (NhanVien nv : all) {
            if (nv.getRole() == ChucVu.ADMIN)
                continue;

            // ── TÍNH THỐNG KÊ TOÀN BỘ ──
            if (nv.getRole() == ChucVu.QUAN_LY)
                totalManager++;
            else
                totalStaff++;

            // Phân quyền: QUẢN LÝ chỉ được XEM role NHÂN VIÊN
            if (!isCurrentUserAdmin && nv.getRole() != ChucVu.NHAN_VIEN)
                continue;

            // Thêm vào danh sách hiển thị
            masterData.add(nv);
        }

        filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        lblTotal.setText(String.valueOf(totalStaff + totalManager));
        lblStaff.setText(String.valueOf(totalStaff));
        lblManager.setText(String.valueOf(totalManager));
    }

    private void applyFilter(String keyword) {
        if (filteredData == null)
            return;
        if (keyword == null || keyword.isBlank()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = keyword.toLowerCase();
            filteredData.setPredicate(nv -> nvl(nv.getMaNV()).toLowerCase().contains(lower) ||
                    nvl(nv.getHoTen()).toLowerCase().contains(lower) ||
                    nvl(nv.getSoDT()).contains(lower));
        }
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * ACTIONS
     * ══════════════════════════════════════════════════════════════════
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

    private void updateTrangThai(NhanVien nv, model.enums.TrangThaiNV status) {
        if (nv.getTrangThai() == status)
            return;

        // Ràng buộc ít nhất 1 Quản lý đang làm việc
        if (nv.getRole() == ChucVu.QUAN_LY && nv.getTrangThai() == model.enums.TrangThaiNV.CON_LAM
                && status == model.enums.TrangThaiNV.DA_NGHI) {
            long activeManagerCount = dao.getAll().stream()
                    .filter(n -> n.getRole() == ChucVu.QUAN_LY && n.getTrangThai() == model.enums.TrangThaiNV.CON_LAM)
                    .count();
            if (activeManagerCount <= 1) {
                showAlert("Không thể cập nhật", "Hệ thống phải có ít nhất 1 Quản lý đang làm việc!");
                return;
            }

            // Kiểm tra số lượng cấp dưới
            int subCount = dao.countSubordinates(nv.getMaNV());

            if (subCount > 0) {
                java.util.List<NhanVien> otherManagers = dao.getAll().stream()
                        .filter(n -> n.getRole() == ChucVu.QUAN_LY
                                && n.getTrangThai() == model.enums.TrangThaiNV.CON_LAM
                                && !n.getMaNV().equals(nv.getMaNV()))
                        .toList();

                if (otherManagers.isEmpty()) {
                    showAlert("Không thể cập nhật", "Nhân sự này đang quản lý " + subCount
                            + " cá nhân khác!\nKhông tìm thấy quản lý thay thế khả dụng nào để bàn giao.");
                    return;
                }

                java.util.Optional<NhanVien> result = showHandoverDialog(nv, otherManagers, subCount);

                if (result.isPresent()) {
                    dao.updateManagerForSubordinates(nv.getMaNV(), result.get().getMaNV());
                } else {
                    return; // Người dùng nhấn Hủy
                }
            }
        }

        nv.setTrangThai(status);
        if (dao.update(nv)) {
            loadData(); // Refresh table và thống kê
        } else {
            showAlert("Lỗi", "Không thể cập nhật trạng thái nhân viên.");
        }
    }

    private void confirmDelete(NhanVien nv) {
        // ADMIN có thể xóa tất cả; QUAN_LY chỉ xóa NHAN_VIEN
        if (!isCurrentUserAdmin && nv.getRole() != ChucVu.NHAN_VIEN) {
            showAlert("Không thể xóa", "Bạn không có quyền xóa quản lý cùng cấp hoặc cao hơn.");
            return;
        }

        // 1. Kiểm tra ràng buộc nhân sự (Quản lý)
        int subordinatesCount = dao.countSubordinates(nv.getMaNV());
        String replacementMaNV = null;

        if (subordinatesCount > 0) {
            List<NhanVien> otherManagers = dao.getAll().stream()
                    .filter(n -> n.getRole() == ChucVu.QUAN_LY
                            && !n.getMaNV().equals(nv.getMaNV())
                            && n.getTrangThai() == model.enums.TrangThaiNV.CON_LAM)
                    .toList();

            if (otherManagers.isEmpty()) {
                showAlert("Không thể xóa", "Nhân sự này đang quản lý " + subordinatesCount
                        + " nhân viên khác.\nKhông tìm thấy Quản lý thay thế khả dụng để bàn giao công việc!");
                return;
            }

            Optional<NhanVien> result = showHandoverDialog(nv, otherManagers, subordinatesCount);

            if (result.isPresent()) {
                replacementMaNV = result.get().getMaNV();
            } else {
                return; // Hủy xóa
            }
        }

        // 2. Xác nhận xóa cuối cùng
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa vĩnh viễn: " + nv.getHoTen() + " ?");

        if (replacementMaNV != null) {
            alert.setContentText(
                    alert.getContentText() + "\n(Mọi cấp dưới sẽ được chuyển giao cho " + replacementMaNV + ")");
        }

        Optional<ButtonType> confirmResult = alert.showAndWait();
        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
            // Nếu có bàn giao, thực hiện update trước
            if (replacementMaNV != null) {
                dao.updateManagerForSubordinates(nv.getMaNV(), replacementMaNV);
            }

            if (dao.delete(nv.getMaNV())) {
                loadData();
            } else {
                showAlert("Lỗi hệ thống",
                        "Không thể xóa nhân sự này.\nCó thể do ràng buộc dữ liệu khác (như Hóa đơn, Đơn đặt phòng).");
            }
        }
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * UTILITY
     * ══════════════════════════════════════════════════════════════════
     */
    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        String base = "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 20; -fx-cursor: hand;";
        String hover = base.replace(
                "-fx-background-color: " + bg,
                "-fx-background-color: " + hoverBg);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private Optional<NhanVien> showHandoverDialog(NhanVien nv, List<NhanVien> managers, int subCount) {
        Dialog<NhanVien> dialog = new Dialog<>();
        dialog.setTitle("Bàn giao quyền quản lý");
        Window owner = getScene().getWindow();
        dialog.initOwner(owner);

        // Header Section
        VBox header = new VBox(8);
        header.setPadding(new Insets(24));
        header.setStyle("-fx-background-color: " + C_NAVY + ";");
        Label title = new Label("Bàn giao nhân sự");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label(
                "Quản lý [" + nv.getHoTen() + "] đang trực tiếp quản lý " + subCount + " nhân viên.");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web("#bfdbfe"));
        header.getChildren().addAll(title, subtitle);

        // Content Section
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        Label prompt = new Label("Vui lòng chọn Quản lý thay thế để tiếp nhận bàn giao công việc:");
        prompt.setFont(Font.font("Segoe UI", 14));
        prompt.setTextFill(Color.web(C_TEXT_DARK));

        ComboBox<NhanVien> cb = new ComboBox<>(FXCollections.observableArrayList(managers));
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(42);
        cb.setPromptText("Chọn quản lý thay thế...");
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NhanVien item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getHoTen() + " (" + item.getMaNV() + ")");
            }
        });
        cb.setButtonCell(cb.getCellFactory().call(null));
        if (!managers.isEmpty())
            cb.getSelectionModel().select(0);

        content.getChildren().addAll(prompt, cb);

        VBox layout = new VBox(0, header, content);
        layout.setPrefWidth(480);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style buttons
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText("Xác nhận bàn giao");

        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Hủy bỏ");

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? cb.getValue() : null);

        Region dim = model.utils.DimOverlay.show(owner);
        Optional<NhanVien> result = dialog.showAndWait();
        DimOverlay.hide(owner, dim);

        return result;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }
}