package gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture; // FIX: import trực tiếp, bỏ inner class
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import model.entities.NhanVien;
import model.enums.ChucVu;
import model.utils.DimOverlay;

/**
 * MainFrameView – JavaFX
 * Sidebar xanh navy + content area.
 *
 * Lưu ý: Các panel chưa chuyển sang JavaFX (KhachHangPanel, NhanVienPanel …)
 * được nhúng tạm qua SwingNode. Khi từng panel được viết lại bằng JavaFX,
 * chỉ cần thay dòng showSwing(...) bằng showFX(...) tương ứng.
 */
public class MainFrameView {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_SIDEBAR_DARK = "#172554";
    private static final String C_HOVER = "#1e40af";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_TEXT_LIGHT = "#bfdbfe";
    private static final String C_TEXT_MUTED = "#93c5fd";
    private static final String C_GOLD = "#d4af37";
    private static final String C_CONTENT_BG = "#f8f9fa";
    private static final String C_BORDER = "#e9ecef";

    /* ── State ──────────────────────────────────────────────────────── */
    private final Stage primaryStage;
    private final NhanVien staff;
    private final boolean isAdmin;
    private final Stage loginStage;

    private StackPane contentArea;
    private Button activeBtn = null;
    private boolean serviceExpanded = false;
    private VBox serviceSubMenu;

    /* ── Constructors ───────────────────────────────────────────────── */
    public MainFrameView(Stage stage, NhanVien staff) {
        this(stage, staff, null);
    }

    public MainFrameView(Stage stage, NhanVien staff, Stage loginStage) {
        this.primaryStage = stage;
        this.staff = staff;
        this.loginStage = loginStage;
        this.isAdmin = (staff != null &&
                (staff.getRole() == ChucVu.QUAN_LY || staff.getRole() == ChucVu.ADMIN));

        build();
    }

    /* ── Root ───────────────────────────────────────────────────────── */
    private void build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + C_CONTENT_BG + ";");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + C_CONTENT_BG + ";");
        root.setCenter(contentArea);

        navigateTo("dashboard");

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setTitle("Khách sạn Lucia Star – " +
                (staff != null ? switch (staff.getRole()) {
                    case ADMIN -> "ADMIN";
                    case QUAN_LY -> "QUẢN LÝ";
                    default -> "NHÂN VIÊN";
                } : "HỆ THỐNG"));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            confirmExit();
        });
        primaryStage.show();
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * HEADER
     * ════════════════════════════════════════════════════════════════
     */
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPrefHeight(72);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;");

        VBox left = new VBox(3);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPadding(new Insets(0, 0, 0, 28));
        HBox.setHgrow(left, Priority.ALWAYS);

        Label title = new Label(
                staff != null && staff.getRole() == ChucVu.ADMIN ? "ADMIN" : isAdmin ? "QUẢN LÝ" : "NHÂN VIÊN");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(C_SIDEBAR));

        Label subtitle = new Label("Hệ thống quản lý khách sạn Lucia Star");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web("#6b7280"));

        left.getChildren().addAll(title, subtitle);

        HBox right = new HBox(16);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setPadding(new Insets(0, 28, 0, 0));

        String roleTag = (staff != null) ? switch (staff.getRole()) {
            case ADMIN -> "  [ADMIN]";
            case QUAN_LY -> "  [QUẢN LÝ]";
            default -> "  [NHÂN VIÊN]";
        } : "";
        String displayName = (staff != null ? staff.getHoTen() : "ADMIN") + roleTag;
        Label userLbl = new Label(displayName);
        userLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        boolean isAdminRole = staff != null && staff.getRole() == ChucVu.ADMIN;
        userLbl.setTextFill(Color.web(isAdminRole ? "#7c3aed" : isAdmin ? "#a07820" : C_SIDEBAR));

        Button btnLogout = outlinedBtn("Đăng xuất", "white", "#b03030", "#fde8e8");
        btnLogout.setOnAction(e -> handleLogout());
        Button btnProfile = outlinedBtn("Thông tin", "white", C_SIDEBAR, "#eff6ff");
        btnProfile.setOnAction(e -> navigateTo("profile"));

        right.getChildren().addAll(userLbl, btnProfile, btnLogout);
        header.getChildren().addAll(left, right);
        return header;
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * SIDEBAR
     * ════════════════════════════════════════════════════════════════
     */
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(256);
        sidebar.setStyle("-fx-background-color: " + C_SIDEBAR + ";");

        sidebar.getChildren().add(buildLogoBlock());
        sidebar.getChildren().add(hLine("#1e40af"));

        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 10, 12, 10));
        VBox.setVgrow(nav, Priority.ALWAYS);

        Button btnDashboard = navBtn("🏠", "Trang chủ", "dashboard");
        activateBtn(btnDashboard);
        nav.getChildren().add(btnDashboard);

        // ── Nhóm: NGHIỆP VỤ (Transaction Data) ──
        nav.getChildren().add(navHeader("- Nghiệp vụ"));
        nav.getChildren().addAll(
                navBtn("📅", "Đặt phòng", "booking"),
                navBtn("✅", "Nhận phòng", "checkin"),
                navBtn("🚪", " Trả phòng", "checkout"),
                buildServiceSection(),
                navBtn("🧾", " Hóa đơn", "invoices"));

        // ── Nhóm: QUẢN LÝ (Master Data) ──
        nav.getChildren().add(navHeader("- Quản lý"));
        nav.getChildren().addAll(
                navBtn("🛏", "Phòng", "rooms"),
                navBtn("👥", "Khách hàng", "customers"));

        if (isAdmin) {
            nav.getChildren().add(navBtn("👔", "Nhân viên", "staff"));
        }

        sidebar.getChildren().add(nav);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(spacer, hLine("#1e40af"));

        // FIX: dùng FontPosture từ import trực tiếp, bỏ inner class FontPosture
        String roleLabelText;
        String roleLabelColor;
        if (staff != null && staff.getRole() == ChucVu.ADMIN) {
            roleLabelText = "☆  Chế độ: ADMIN";
            roleLabelColor = "#c4b5fd";
        } else if (isAdmin) {
            roleLabelText = "⚙  Chế độ: QUẢN LÝ";
            roleLabelColor = C_GOLD;
        } else {
            roleLabelText = "👤  Chế độ: NHÂN VIÊN";
            roleLabelColor = C_TEXT_MUTED;
        }
        Label roleLbl = new Label(roleLabelText);
        roleLbl.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 13));
        roleLbl.setTextFill(Color.web(roleLabelColor));
        roleLbl.setPadding(new Insets(12, 0, 14, 20));
        sidebar.getChildren().add(roleLbl);

        return sidebar;
    }

    private HBox buildLogoBlock() {
        HBox block = new HBox(14);
        block.setAlignment(Pos.CENTER_LEFT);
        block.setPadding(new Insets(22, 20, 22, 20));

        StackPane iconBox = new StackPane();
        iconBox.setMinSize(44, 44);
        iconBox.setPrefSize(44, 44);
        Rectangle iconBg = new Rectangle(44, 44);
        iconBg.setArcWidth(10);
        iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(C_ACTIVE));
        Label iconLbl = new Label("🏨");
        iconLbl.setFont(Font.font(20));
        iconBox.getChildren().addAll(iconBg, iconLbl);

        VBox text = new VBox(2);
        Label appName = new Label("Lucia Hotel");
        appName.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        appName.setTextFill(Color.WHITE);
        Label staffName = new Label(staff != null ? staff.getHoTen() : "ADMIN");
        staffName.setFont(Font.font("Segoe UI", 12));
        staffName.setTextFill(Color.web(C_TEXT_MUTED));
        text.getChildren().addAll(appName, staffName);

        block.getChildren().addAll(iconBox, text);
        return block;
    }

    private VBox buildServiceSection() {
        VBox wrap = new VBox(0);

        Button header = new Button("🔧   Dịch vụ   ▾");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setPrefHeight(42);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setFont(Font.font("Segoe UI", 14));
        applyNormal(header);

        serviceSubMenu = new VBox(1);
        serviceSubMenu.setStyle("-fx-background-color: " + C_SIDEBAR_DARK + ";");
        serviceSubMenu.setPadding(new Insets(4, 0, 6, 0));
        serviceSubMenu.setVisible(false);
        serviceSubMenu.setManaged(false);

        serviceSubMenu.getChildren().add(subNavBtn("  ├  Sử dụng dịch vụ", "service"));
        if (isAdmin) {
            serviceSubMenu.getChildren().add(subNavBtn("  ├  Danh mục dịch vụ", "serviceManager"));
            serviceSubMenu.getChildren().add(subNavBtn("  └  Bảng giá dịch vụ", "servicePrice"));
        } else {
            serviceSubMenu.getChildren().add(subNavBtn("  └  Danh mục dịch vụ", "serviceManager"));
        }

        header.setOnAction(e -> {
            serviceExpanded = !serviceExpanded;
            serviceSubMenu.setVisible(serviceExpanded);
            serviceSubMenu.setManaged(serviceExpanded);
            header.setText("🔧   Dịch vụ   " + (serviceExpanded ? "▴" : "▾"));
        });
        setHover(header);

        wrap.getChildren().addAll(header, serviceSubMenu);
        return wrap;
    }

    /* ── Nav button helpers ─────────────────────────────────────────── */
    private Button navBtn(String icon, String label, String card) {
        Button btn = new Button(icon + "   " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", 14));
        applyNormal(btn);
        btn.setOnAction(e -> {
            if (activeBtn != null)
                applyNormal(activeBtn);
            activateBtn(btn);
            navigateTo(card);
        });
        setHover(btn);
        return btn;
    }

    private Label navHeader(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(C_TEXT_MUTED));
        lbl.setPadding(new Insets(12, 0, 5, 12));
        return lbl;
    }

    private Button subNavBtn(String label, String card) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", 13));
        applySubNormal(btn);
        btn.setOnAction(e -> {
            if (activeBtn != null)
                applyNormal(activeBtn);
            applySubActive(btn);
            activeBtn = btn;
            navigateTo(card);
        });
        btn.setOnMouseEntered(e -> {
            if (btn != activeBtn)
                btn.setStyle(subHoverStyle());
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeBtn)
                applySubNormal(btn);
        });
        return btn;
    }

    /* ── Style helpers ──────────────────────────────────────────────── */
    private void applyNormal(Button b) {
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + C_TEXT_LIGHT + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 0 12 0 12;" +
                        "-fx-cursor: hand;");
    }

    private void activateBtn(Button b) {
        b.setStyle(
                "-fx-background-color: " + C_ACTIVE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 0 12 0 12;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;");
        activeBtn = b;
    }

    private void setHover(Button b) {
        b.setOnMouseEntered(e -> {
            if (b != activeBtn)
                b.setStyle(hoverStyle());
        });
        b.setOnMouseExited(e -> {
            if (b != activeBtn)
                applyNormal(b);
        });
    }

    private String hoverStyle() {
        return "-fx-background-color: " + C_HOVER + ";" +
                "-fx-text-fill: white;" +
                "-fx-border-color: transparent;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 0 12 0 12;" +
                "-fx-cursor: hand;";
    }

    private void applySubNormal(Button b) {
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + C_TEXT_MUTED + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 0 12 0 24;" +
                        "-fx-cursor: hand;");
    }

    private void applySubActive(Button b) {
        b.setStyle(
                "-fx-background-color: " + C_ACTIVE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 0 12 0 24;" +
                        "-fx-cursor: hand;");
    }

    private String subHoverStyle() {
        return "-fx-background-color: " + C_HOVER + ";" +
                "-fx-text-fill: white;" +
                "-fx-border-color: transparent;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 0 12 0 24;" +
                "-fx-cursor: hand;";
    }

    /*
     * ════════════════════════════════════════════════════════════════
     * NAVIGATION
     * - TrangChuView đã là JavaFX → đặt trực tiếp vào contentArea.
     * - Các panel còn lại vẫn là Swing JPanel → nhúng qua SwingNode.
     * Khi nào panel đó được viết lại JavaFX thì đổi thành showFX().
     * ════════════════════════════════════════════════════════════════
     */
    private void navigateTo(String card) {
        switch (card) {
            // FIX: đổi TrangChuPanel (Swing) → TrangChuView (JavaFX)
            case "dashboard" -> showFX(new TrangChuView());
            case "customers" -> showFX(new KhachHangView(isAdmin));
            case "profile" -> {
                Region overlay = DimOverlay.show(primaryStage);
                new ThongTinCaNhanView(primaryStage, staff).showAndWait();
                DimOverlay.hide(primaryStage, overlay);
            }
            case "staff" -> {
                if (!isAdmin) {
                    showFX(buildAccessDenied());
                } else {
                    showFX(new NhanVienView(staff));
                }
            }
            case "booking" -> showFX(new DatPhongView());
            // Phase 3 - đã migrate sang JavaFX
            case "checkin" -> showFX(new CheckInView());
            case "checkout" -> showFX(new CheckOutView(staff));
            case "invoices" -> showFX(new HoaDonView());
            // Phase 2 - đã migrate sang JavaFX
            case "service" -> showFX(new DichVuView());
            case "serviceManager" -> showFX(new QuanLyDichVuView(isAdmin));
            case "servicePrice" -> showFX(new BangGiaDichVuView());
            case "rooms" -> showFX(new QuanLyPhongView(isAdmin));
            default -> showFX(buildPlaceholder(card));
        }
    }

    /** Chặn không cho nhân viên vào */
    private StackPane buildAccessDenied() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        Label icon = new Label("🔒");
        icon.setFont(Font.font(48));

        Label title = new Label("Truy cập bị từ chối");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#9ca3af"));

        Label sub = new Label("Bạn không có quyền truy cập chức năng này.");
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setTextFill(Color.web("#9ca3af"));

        box.getChildren().addAll(icon, title, sub);

        StackPane pane = new StackPane(box);
        pane.setStyle("-fx-background-color: " + C_CONTENT_BG + ";");
        return pane;
    }

    /** Hiển thị một JavaFX node trong content area */
    private void showFX(javafx.scene.Node node) {
        contentArea.getChildren().setAll(node);
    }

    private StackPane buildPlaceholder(String name) {
        Label lbl = new Label("[ " + name.toUpperCase() + " ]");
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lbl.setTextFill(Color.web("#9ca3af"));
        StackPane pane = new StackPane(lbl);
        pane.setStyle("-fx-background-color: " + C_CONTENT_BG + ";");
        return pane;
    }

    /* ── Utility ────────────────────────────────────────────────────── */
    private Region hLine(String color) {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + color + ";");
        return r;
    }

    /**
     * Nút outlined dùng trong header.
     * FIX: hover không append chuỗi CSS (gây duplicate property),
     * mà thay thế đúng giá trị background-color.
     */
    private Button outlinedBtn(String text, String bgNormal, String fg, String bgHover) {
        String base = "-fx-background-color: " + bgNormal + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-border-color: #e0c8c0;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 18 8 18;" +
                "-fx-cursor: hand;";
        String hover = base.replace(
                "-fx-background-color: " + bgNormal,
                "-fx-background-color: " + bgHover);
        Button btn = new Button(text);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    /* ── Logout / Exit ──────────────────────────────────────────────── */
    private void handleLogout() {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn đăng xuất?", ButtonType.YES, ButtonType.NO);
        dlg.setTitle("Xác nhận đăng xuất");
        dlg.setHeaderText(null);
        dlg.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                primaryStage.close();
                if (loginStage != null) {
                    Platform.runLater(() -> {
                        if (loginStage.getScene() != null &&
                                loginStage.getScene().getUserData() instanceof DangNhapView v)
                            v.resetForm();
                        loginStage.show();
                    });
                }
            }
        });
    }

    private void confirmExit() {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn thoát ứng dụng?", ButtonType.YES, ButtonType.NO);
        dlg.setTitle("Thoát");
        dlg.setHeaderText(null);
        dlg.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                Platform.exit();
                System.exit(0);
            }
        });
    }
}