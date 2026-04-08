package gui;

import dao.BangGiaDichVuDAO;
import dao.DatPhongDAO;
import dao.DichVuDAO;
import dao.PhongDAO;
import model.entities.DichVu;
import model.entities.Phong;
import model.enums.TrangThaiPhong;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DichVuView – JavaFX
 * Thay thế ThemDichVuPanel (Swing).
 */
public class DichVuView extends BorderPane {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    /* ── Bảng màu chuẩn hệ thống ────────────────────────────────────── */
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_RED = "#d5ccccff";

    /* ── DAO ─────────────────────────────────────────────────────────── */
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final BangGiaDichVuDAO bangGiaDAO = new BangGiaDichVuDAO();

    /* ── State ───────────────────────────────────────────────────────── */
    private String selectedMaPhong = "";
    private String currentCategory = "Thực phẩm";
    private final Map<DichVu, Integer> cart = new HashMap<>();

    /* ── UI Controls ─────────────────────────────────────────────────── */
    private FlowPane roomPane;
    private FlowPane servicePane;
    private VBox billContainer;
    private Label lblTotal;
    private Label lblRoomTitle;
    private HBox tabBar;

    public DichVuView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeaderBlock());
        setCenter(buildBody());

        refreshRooms();
        refreshServices("Thực phẩm");
    }

    /* ══════════════════ HEADER TITLE ══════════════════ */
    private VBox buildHeaderBlock() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 20, 0));
        Label title = new Label("Sử dụng dịch vụ");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(C_TEXT_DARK));
        Label subtitle = new Label("Thêm dịch vụ vào phòng đang có khách");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web(C_TEXT_GRAY));
        box.getChildren().addAll(title, subtitle);
        return box;
    }

    /* ══════════════════ BODY = LEFT + RIGHT ══════════════════ */
    private HBox buildBody() {
        HBox body = new HBox(24);
        body.setAlignment(Pos.TOP_LEFT);

        // Left chiếm 65%
        VBox leftPane = buildLeftPane();
        leftPane.prefWidthProperty().bind(body.widthProperty().multiply(0.65));
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        // Right chiếm 35%
        VBox rightPane = buildRightPane();
        rightPane.prefWidthProperty().bind(body.widthProperty().multiply(0.35));
        rightPane.setMinWidth(320);

        body.getChildren().addAll(leftPane, rightPane);
        return body;
    }

    /* ══════════════════ LEFT PANE ══════════════════ */
    private VBox buildLeftPane() {
        VBox pane = new VBox(16);

        // --- Hộp chọn phòng ---
        VBox roomBox = new VBox(10);
        roomBox.setPadding(new Insets(10, 15, 10, 15));
        roomBox.setStyle(
                "-fx-background-color: " + C_CARD + ";" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 12; -fx-background-radius: 12;");
        roomBox.setEffect(new DropShadow(8, 0, 2, Color.web("#00000008")));
        roomBox.setPrefHeight(150);
        roomBox.setMinHeight(150);
        roomBox.setMaxHeight(150);

        Label lblRoom = new Label("Phòng đang có khách ( nhấp để chọn )");
        lblRoom.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblRoom.setTextFill(Color.web(C_TEXT_DARK));

        roomPane = new FlowPane(12, 8);
        ScrollPane roomScroll = new ScrollPane(roomPane);
        roomScroll.setFitToWidth(true);
        roomScroll.setPrefHeight(90);
        roomScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        roomBox.getChildren().addAll(lblRoom, roomScroll);

        // --- Hộp dịch vụ ---
        VBox serviceBox = new VBox(12);
        serviceBox.setPadding(new Insets(0));
        serviceBox.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(serviceBox, Priority.ALWAYS);

        // Tab bar
        tabBar = new HBox(8);
        String[] cats = { "Thực phẩm", "Giải trí", "Sức khỏe", "Tiện ích" };
        for (String c : cats)
            tabBar.getChildren().add(buildTabButton(c));

        servicePane = new FlowPane(12, 12);
        ScrollPane serviceScroll = new ScrollPane(servicePane);
        serviceScroll.setFitToWidth(true);
        serviceScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(serviceScroll, Priority.ALWAYS);

        serviceBox.getChildren().addAll(tabBar, serviceScroll);
        VBox.setVgrow(serviceBox, Priority.ALWAYS);

        pane.getChildren().addAll(roomBox, serviceBox);
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    private Button buildTabButton(String cat) {
        boolean isActive = cat.equals(currentCategory);
        Button btn = new Button(cat);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setCursor(Cursor.HAND);
        btn.setPrefHeight(36);
        btn.setPadding(new Insets(0, 16, 0, 16));
        if (isActive) {
            btn.setStyle("-fx-background-color: " + C_SIDEBAR + "; -fx-text-fill: white; -fx-background-radius: 8;");
        } else {
            btn.setStyle("-fx-background-color: " + C_CARD + "; -fx-text-fill: " + C_TEXT_GRAY + ";" +
                    "-fx-border-color: " + C_BORDER + "; -fx-background-radius: 8; -fx-border-radius: 8;");
        }
        btn.setOnAction(e -> {
            currentCategory = cat;
            refreshTabBar();
            refreshServices(cat);
        });
        return btn;
    }

    private void refreshTabBar() {
        tabBar.getChildren().clear();
        for (String c : new String[] { "Thực phẩm", "Giải trí", "Sức khỏe", "Tiện ích" }) {
            tabBar.getChildren().add(buildTabButton(c));
        }
    }

    /* ══════════════════ RIGHT PANE (Hóa đơn) ══════════════════ */
    private VBox buildRightPane() {
        VBox pane = new VBox(0);
        pane.setStyle(
                "-fx-background-color: " + C_CARD + ";" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 12; -fx-background-radius: 12;");
        pane.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        // Header của bill - Phối Navy cho đồng bộ sidebar
        VBox billHeader = new VBox(4);
        billHeader.setPadding(new Insets(18, 20, 18, 20));
        billHeader.setStyle("-fx-background-color: " + C_SIDEBAR + "; -fx-background-radius: 12 12 0 0;");

        lblRoomTitle = new Label("Hóa đơn: ---");
        lblRoomTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblRoomTitle.setTextFill(Color.WHITE);

        billHeader.getChildren().setAll(lblRoomTitle);

        // Danh sách dịch vụ đã chọn
        billContainer = new VBox(0);
        billContainer.setStyle("-fx-background-color: white;");
        ScrollPane billScroll = new ScrollPane(billContainer);
        billScroll.setFitToWidth(true);
        billScroll.setPrefHeight(300);
        billScroll.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");
        VBox.setVgrow(billScroll, Priority.ALWAYS);

        // Footer tổng tiền + nút
        VBox footer = new VBox(15);
        footer.setPadding(new Insets(16, 20, 20, 20));
        footer.setStyle(
                "-fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        lblTotal = new Label("Tổng: 0 đ");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTotal.setTextFill(Color.web(C_SIDEBAR));

        HBox actionPanel = new HBox(12);
        actionPanel.setAlignment(Pos.CENTER);

        Button btnClear = new Button("✕ HỦY BỎ");
        btnClear.setPrefWidth(110);
        btnClear.setPrefHeight(40);
        btnClear.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnClear.setStyle("-fx-background-color: white; -fx-text-fill: " + C_RED
                + "; -fx-background-radius: 8; -fx-cursor: hand;");
        btnClear.setOnAction(e -> {
            cart.clear();
            updateBillUI();
            refreshServices(currentCategory);
        });

        Button btnConfirm = new Button("✔ XÁC NHẬN");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setPrefHeight(40);
        btnConfirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnConfirm.setStyle("-fx-background-color: " + C_ACTIVE
                + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        HBox.setHgrow(btnConfirm, Priority.ALWAYS);
        btnConfirm.setOnAction(e -> handleConfirm());

        actionPanel.getChildren().addAll(btnClear, btnConfirm);
        footer.getChildren().addAll(lblTotal, actionPanel);

        pane.getChildren().addAll(billHeader, billScroll, footer);
        return pane;
    }

    /* ══════════════════ CARD PHÒNG ══════════════════ */
    private VBox buildRoomCard(Phong p) {
        boolean isSelected = selectedMaPhong.equals(p.getMaPhong());
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(90, 70);
        card.setCursor(Cursor.HAND);
        card.setStyle("-fx-background-color: " + (isSelected ? "#eff6ff" : C_CARD) + ";" +
                "-fx-border-color: " + (isSelected ? C_SIDEBAR : C_BORDER) + ";" +
                "-fx-border-width: " + (isSelected ? "2" : "1") + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        Label lblMa = new Label(p.getMaPhong());
        lblMa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblMa.setTextFill(Color.web(isSelected ? C_SIDEBAR : C_TEXT_DARK));

        String guest = datPhongDAO.getTenKhachHienTai(p.getMaPhong());
        Label lblGuest = new Label(guest != null ? guest : "...");
        lblGuest.setFont(Font.font("Segoe UI", 11));
        lblGuest.setTextFill(Color.web(C_TEXT_GRAY));
        lblGuest.setAlignment(Pos.CENTER);
        lblGuest.setMaxWidth(80);

        card.getChildren().addAll(lblMa, lblGuest);
        card.setOnMouseClicked(e -> {
            selectedMaPhong = p.getMaPhong();
            lblRoomTitle.setText("Hóa đơn: P." + selectedMaPhong);
            refreshRooms();
        });
        return card;
    }

    /* ══════════════════ CARD DỊCH VỤ ══════════════════ */
    private HBox buildServiceCard(DichVu dv) {
        HBox card = new HBox(12);
        card.setPrefSize(230, 85);
        card.setMaxWidth(230);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER
                + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(6, 0, 2, Color.web("#00000005")));

        VBox info = new VBox(4);
        info.setAlignment(Pos.CENTER_LEFT);
        Label lblName = new Label(dv.getTenDV());
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblName.setTextFill(Color.web(C_TEXT_DARK));
        Label lblPrice = new Label(String.format("%,.0f đ", dv.getGia()));
        lblPrice.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPrice.setTextFill(Color.web(C_ACTIVE));
        info.getChildren().addAll(lblName, lblPrice);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox qtyBox = new HBox(4);
        qtyBox.setAlignment(Pos.CENTER_RIGHT);

        int currentQty = cart.getOrDefault(dv, 0);

        Button btnMinus = new Button("-");
        styleQtyBtn(btnMinus, "white", C_TEXT_GRAY);
        btnMinus.setOnAction(e -> {
            int q = cart.getOrDefault(dv, 0);
            if (q > 0) {
                if (q == 1)
                    cart.remove(dv);
                else
                    cart.put(dv, q - 1);
                updateBillUI();
                refreshServices(currentCategory);
            }
        });

        Label lblQty = new Label(String.valueOf(currentQty));
        lblQty.setPrefWidth(25);
        lblQty.setAlignment(Pos.CENTER);
        lblQty.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblQty.setTextFill(Color.web(C_SIDEBAR));

        Button btnPlus = new Button("+");
        styleQtyBtn(btnPlus, C_ACTIVE, "white");
        btnPlus.setOnAction(e -> {
            cart.put(dv, cart.getOrDefault(dv, 0) + 1);
            updateBillUI();
            refreshServices(currentCategory);
        });

        qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);
        card.getChildren().addAll(info, qtyBox);
        return card;
    }

    private void styleQtyBtn(Button b, String bg, String fg) {
        b.setPrefSize(28, 28);
        b.setCursor(Cursor.HAND);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-border-color: #f0f0f0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 13;");
    }

    /* ══════════════════ REFRESH / UPDATE ══════════════════ */
    private void refreshRooms() {
        roomPane.getChildren().clear();
        PhongDAO phongDAO = new PhongDAO();
        for (Phong p : phongDAO.getAll()) {
            if (p.getTinhTrang() == TrangThaiPhong.DACOKHACH) {
                roomPane.getChildren().add(buildRoomCard(p));
            }
        }
    }

    private void refreshServices(String cat) {
        servicePane.getChildren().clear();
        DichVuDAO dichVuDAO = new DichVuDAO();
        List<DichVu> list = dichVuDAO.getByType(cat);

        // Lấy bản đồ giá đang áp dụng (Trạng thái = 0 và trong thời gian hiệu lực)
        Map<String, Double> activePrices = bangGiaDAO.getActivePriceMap();

        // 1. Cập nhật giá cho danh sách dịch vụ hiển thị
        for (DichVu dv : list) {
            if (activePrices.containsKey(dv.getMaDV())) {
                dv.setGia(activePrices.get(dv.getMaDV()));
            }
            servicePane.getChildren().add(buildServiceCard(dv));
        }

        // 2. Cập nhật giá cho các dịch vụ đang có trong giỏ hàng
        if (!cart.isEmpty()) {
            for (DichVu dvInCart : cart.keySet()) {
                if (activePrices.containsKey(dvInCart.getMaDV())) {
                    dvInCart.setGia(activePrices.get(dvInCart.getMaDV()));
                } else {
                    // Nếu không có trong bảng giá active, lấy lại giá gốc từ DB
                    DichVu base = dichVuDAO.getServiceByID(dvInCart.getMaDV());
                    if (base != null) {
                        dvInCart.setGia(base.getGia());
                    }
                }
            }
            updateBillUI(); // Cập nhật lại giao diện hóa đơn và tổng tiền
        }
    }

    private void updateBillUI() {
        billContainer.getChildren().clear();
        double total = 0;
        for (Map.Entry<DichVu, Integer> entry : cart.entrySet()) {
            DichVu dv = entry.getKey();
            int qty = entry.getValue();
            double sub = dv.getGia() * qty;
            total += sub;

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 15, 12, 15));
            row.setStyle("-fx-border-color: transparent transparent #f3f4f6 transparent; -fx-border-width: 0 0 1 0;");

            Label lblQty = new Label(qty + " x");
            lblQty.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblQty.setTextFill(Color.web(C_ACTIVE));
            lblQty.setMinWidth(40);

            Label lblName = new Label(dv.getTenDV());
            lblName.setFont(Font.font("Segoe UI", 13));
            lblName.setTextFill(Color.web(C_TEXT_DARK));

            // Dòng dấu chấm căn lề (Leader dots) - Căn giữa dòng cho đẹp
            Region dots = new Region();
            dots.setStyle("-fx-border-style: dotted; -fx-border-width: 0 0 1 0; -fx-border-color: #d1d5db;");
            dots.setPrefHeight(1);
            dots.setMaxHeight(1);
            HBox.setHgrow(dots, Priority.ALWAYS);
            // Căn chỉnh để dấu chấm nằm giữa chiều cao chữ
            HBox.setMargin(dots, new Insets(8, 0, 0, 0));

            Label lblSub = new Label(String.format("%,.0f đ", sub));
            lblSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblSub.setTextFill(Color.web(C_SIDEBAR));
            lblSub.setMinWidth(80);
            lblSub.setAlignment(Pos.CENTER_RIGHT);

            Button btnRemove = new Button();
            try {
                ImageView icon = new ImageView(new Image("file:src/icon/exit.png"));
                icon.setFitWidth(14);
                icon.setFitHeight(14);
                btnRemove.setGraphic(icon);
            } catch (Exception ex) {
                btnRemove.setText("✕");
            }
            btnRemove.setPrefSize(28, 28);
            btnRemove.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-opacity: 0.5;");
            btnRemove.setOnMouseEntered(e -> {
                btnRemove.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 5; -fx-opacity: 1;");
            });
            btnRemove.setOnMouseExited(e -> {
                btnRemove.setStyle("-fx-background-color: transparent; -fx-opacity: 0.5;");
            });
            btnRemove.setOnAction(e -> {
                cart.remove(dv);
                updateBillUI();
                refreshServices(currentCategory);
            });

            row.getChildren().addAll(lblQty, lblName, dots, lblSub, btnRemove);
            billContainer.getChildren().add(row);
        }
        lblTotal.setText(String.format("Tổng cộng: %,.0f đ", total));
    }

    private void handleConfirm() {
        if (selectedMaPhong.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn phòng", "Vui lòng chọn phòng ở bên trái trước!");
            return;
        }
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn dịch vụ", "Vui lòng thêm ít nhất một dịch vụ vào đơn!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận thêm " + cart.size() + " dịch vụ vào phòng " + selectedMaPhong + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                if (datPhongDAO.saveServiceOrder(selectedMaPhong, cart)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công!",
                            "Đã lưu dịch vụ vào phòng " + selectedMaPhong);
                    cart.clear();
                    updateBillUI();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi lưu dữ liệu", "Vui lòng kiểm tra kết nối CSDL!");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String header, String msg) {
        Alert a = new Alert(type);
        a.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }
}
