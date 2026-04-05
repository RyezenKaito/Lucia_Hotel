package gui;

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
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD_BG = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_GOLD = "#d97706";

    /* ── DAO ─────────────────────────────────────────────────────────── */
    private final PhongDAO phongDAO = new PhongDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();

    /* ── State ───────────────────────────────────────────────────────── */
    private String selectedMaPhong = "";
    private String currentCategory = "Ẩm thực";
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
        refreshServices("Ẩm thực");
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
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        // Right chiếm 35%
        VBox rightPane = buildRightPane();
        rightPane.setMinWidth(300);
        rightPane.setMaxWidth(340);

        body.getChildren().addAll(leftPane, rightPane);
        return body;
    }

    /* ══════════════════ LEFT PANE ══════════════════ */
    private VBox buildLeftPane() {
        VBox pane = new VBox(16);

        // --- Hộp chọn phòng ---
        VBox roomBox = new VBox(10);
        roomBox.setPadding(new Insets(16));
        roomBox.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        roomBox.setEffect(new DropShadow(6, 0, 2, Color.web("#00000010")));
        roomBox.setMaxHeight(160);

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
        serviceBox.setPadding(new Insets(16));
        serviceBox.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        serviceBox.setEffect(new DropShadow(6, 0, 2, Color.web("#00000010")));
        VBox.setVgrow(serviceBox, Priority.ALWAYS);

        // Tab bar
        tabBar = new HBox(8);
        String[] cats = {"Ẩm thực", "Giải trí", "Sức khỏe", "Tiện ích"};
        for (String c : cats) tabBar.getChildren().add(buildTabButton(c));

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
            btn.setStyle("-fx-background-color: " + C_NAVY + "; -fx-text-fill: white; -fx-background-radius: 6;");
        } else {
            btn.setStyle("-fx-background-color: " + C_CARD_BG + "; -fx-text-fill: " + C_TEXT_GRAY + ";" +
                    "-fx-border-color: " + C_BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;");
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
        for (String c : new String[]{"Ẩm thực", "Giải trí", "Sức khỏe", "Tiện ích"}) {
            tabBar.getChildren().add(buildTabButton(c));
        }
    }

    /* ══════════════════ RIGHT PANE (Hóa đơn) ══════════════════ */
    private VBox buildRightPane() {
        VBox pane = new VBox(0);
        pane.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        pane.setEffect(new DropShadow(6, 0, 2, Color.web("#00000010")));

        // Header của bill
        VBox billHeader = new VBox(4);
        billHeader.setPadding(new Insets(18, 20, 14, 20));
        billHeader.setStyle("-fx-background-color: " + C_NAVY + "; -fx-background-radius: 10 10 0 0;");

        lblRoomTitle = new Label("Hóa đơn dịch vụ");
        lblRoomTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblRoomTitle.setTextFill(Color.WHITE);

        Label lblSub = new Label("Chọn phòng ở bên trái để xem");
        lblSub.setFont(Font.font("Segoe UI", 12));
        lblSub.setTextFill(Color.web("#93c5fd"));

        billHeader.getChildren().addAll(lblRoomTitle, lblSub);

        // Danh sách dịch vụ đã chọn
        billContainer = new VBox(0);
        billContainer.setPadding(new Insets(8, 0, 8, 0));
        ScrollPane billScroll = new ScrollPane(billContainer);
        billScroll.setFitToWidth(true);
        billScroll.setPrefHeight(300);
        billScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(billScroll, Priority.ALWAYS);

        // Footer tổng tiền + nút
        VBox footer = new VBox(12);
        footer.setPadding(new Insets(16, 20, 20, 20));
        footer.setStyle("-fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        lblTotal = new Label("Tổng cộng: 0 đ");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTotal.setTextFill(Color.web(C_NAVY));

        Button btnConfirm = new Button("✔  Xác nhận thêm dịch vụ");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnConfirm.setPrefHeight(44);
        btnConfirm.setCursor(Cursor.HAND);
        String baseBtnStyle = "-fx-background-color: " + C_BLUE + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;";
        String hoverBtnStyle = "-fx-background-color: " + C_BLUE_HOVER + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;";
        btnConfirm.setStyle(baseBtnStyle);
        btnConfirm.setOnMouseEntered(e -> btnConfirm.setStyle(hoverBtnStyle));
        btnConfirm.setOnMouseExited(e -> btnConfirm.setStyle(baseBtnStyle));
        btnConfirm.setOnAction(e -> handleConfirm());

        Button btnClear = new Button("✕  Xóa giỏ hàng");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setFont(Font.font("Segoe UI", 13));
        btnClear.setPrefHeight(36);
        btnClear.setCursor(Cursor.HAND);
        btnClear.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: " + C_TEXT_GRAY + "; -fx-background-radius: 8;");
        btnClear.setOnAction(e -> { cart.clear(); updateBillUI(); });

        footer.getChildren().addAll(lblTotal, btnConfirm, btnClear);

        pane.getChildren().addAll(billHeader, billScroll, footer);
        VBox.setVgrow(billScroll, Priority.ALWAYS);
        return pane;
    }

    /* ══════════════════ CARD PHÒNG ══════════════════ */
    private VBox buildRoomCard(Phong p) {
        boolean isSelected = selectedMaPhong.equals(p.getMaPhong());
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(90, 70);
        card.setCursor(Cursor.HAND);
        card.setPadding(new Insets(8));
        card.setStyle(
                "-fx-background-color: " + (isSelected ? "#eff6ff" : C_CARD_BG) + ";" +
                "-fx-border-color: " + (isSelected ? C_NAVY : C_BORDER) + ";" +
                "-fx-border-width: " + (isSelected ? "2" : "1") + ";" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label lblMa = new Label(p.getMaPhong());
        lblMa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblMa.setTextFill(Color.web(isSelected ? C_NAVY : C_TEXT_DARK));

        String guest = datPhongDAO.getTenKhachHienTai(p.getMaPhong());
        Label lblGuest = new Label(guest != null ? guest : "...");
        lblGuest.setFont(Font.font("Segoe UI", 10));
        lblGuest.setTextFill(Color.web(C_TEXT_GRAY));
        lblGuest.setMaxWidth(80);
        lblGuest.setWrapText(true);

        card.getChildren().addAll(lblMa, lblGuest);
        card.setOnMouseClicked(e -> {
            selectedMaPhong = p.getMaPhong();
            lblRoomTitle.setText("Phòng " + selectedMaPhong);
            refreshRooms();
        });
        return card;
    }

    /* ══════════════════ CARD DỊCH VỤ ══════════════════ */
    private VBox buildServiceCard(DichVu dv) {
        VBox card = new VBox(6);
        card.setPrefSize(180, 90);
        card.setMaxWidth(180);
        card.setPadding(new Insets(12));
        card.setCursor(Cursor.HAND);
        card.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        card.setEffect(new DropShadow(4, 0, 1, Color.web("#00000010")));

        Label lblName = new Label(dv.getTenDV());
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblName.setTextFill(Color.web(C_TEXT_DARK));
        lblName.setWrapText(true);

        Label lblPrice = new Label(String.format("%,.0f đ", dv.getGia()));
        lblPrice.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPrice.setTextFill(Color.web(C_GOLD));

        Button btnAdd = new Button("+ Thêm");
        btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        btnAdd.setPrefHeight(28);
        btnAdd.setCursor(Cursor.HAND);
        btnAdd.setStyle("-fx-background-color: " + C_NAVY + "; -fx-text-fill: white; -fx-background-radius: 5;");
        btnAdd.setOnMouseEntered(e -> btnAdd.setStyle("-fx-background-color: " + C_BLUE + "; -fx-text-fill: white; -fx-background-radius: 5;"));
        btnAdd.setOnMouseExited(e -> btnAdd.setStyle("-fx-background-color: " + C_NAVY + "; -fx-text-fill: white; -fx-background-radius: 5;"));
        btnAdd.setOnAction(e -> {
            cart.put(dv, cart.getOrDefault(dv, 0) + 1);
            updateBillUI();
        });

        card.getChildren().addAll(lblName, lblPrice, btnAdd);
        return card;
    }

    /* ══════════════════ REFRESH / UPDATE ══════════════════ */
    private void refreshRooms() {
        roomPane.getChildren().clear();
        for (Phong p : phongDAO.getAll()) {
            if (p.getTinhTrang() == TrangThaiPhong.DACOKHACH) {
                roomPane.getChildren().add(buildRoomCard(p));
            }
        }
    }

    private void refreshServices(String cat) {
        servicePane.getChildren().clear();
        List<DichVu> list = dichVuDAO.getByType(cat);
        for (DichVu dv : list) servicePane.getChildren().add(buildServiceCard(dv));
    }

    private void updateBillUI() {
        billContainer.getChildren().clear();
        double total = 0;
        for (Map.Entry<DichVu, Integer> entry : cart.entrySet()) {
            DichVu dv = entry.getKey();
            int qty = entry.getValue();
            double sub = dv.getGia() * qty;
            total += sub;

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 20, 10, 20));
            row.setStyle("-fx-border-color: transparent transparent " + C_BORDER + " transparent; -fx-border-width: 0 0 1 0;");

            Label lblItem = new Label(qty + " × " + dv.getTenDV());
            lblItem.setFont(Font.font("Segoe UI", 13));
            lblItem.setTextFill(Color.web(C_TEXT_DARK));
            HBox.setHgrow(lblItem, Priority.ALWAYS);

            Label lblSub = new Label(String.format("%,.0f đ", sub));
            lblSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblSub.setTextFill(Color.web(C_NAVY));

            row.getChildren().addAll(lblItem, lblSub);
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
                    showAlert(Alert.AlertType.INFORMATION, "Thành công!", "Đã lưu dịch vụ vào phòng " + selectedMaPhong);
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
