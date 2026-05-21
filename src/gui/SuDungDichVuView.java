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

public class SuDungDichVuView extends BorderPane {

    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_ACTIVE = "#1d4ed8";
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";

    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final BangGiaDichVuDAO bangGiaDAO = new BangGiaDichVuDAO();
    private final dao.LoaiDichVuDAO loaiDVDAO = new dao.LoaiDichVuDAO();
    private final dao.DichVuSuDungDAO dvsdDAO = new dao.DichVuSuDungDAO();
    private String selectedMaPhong = "";
    private model.entities.LoaiDichVu currentCategory;
    private final Map<DichVu, Integer> cart = new HashMap<>();

    // ← THÊM MỚI: trạng thái thu gọn/mở rộng của "Dịch vụ đã dùng"
    private boolean usedServicesExpanded = false;

    private FlowPane roomPane;
    private FlowPane servicePane;
    private VBox billContainer;
    private Label lblTotal;
    private Label lblRoomTitle;
    private HBox tabBar;

    public SuDungDichVuView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));
        setTop(buildHeaderBlock());
        setCenter(buildBody());

        refreshRooms();

        List<model.entities.LoaiDichVu> cats = loaiDVDAO.getAll();
        if (!cats.isEmpty()) {
            currentCategory = cats.get(0);
            refreshTabBar();
            refreshServices(currentCategory);
        }
    }

    /* ══ HEADER ══ */
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

    /* ══ BODY ══ */
    private HBox buildBody() {
        HBox body = new HBox(24);
        body.setAlignment(Pos.TOP_LEFT);

        VBox leftPane = buildLeftPane();
        leftPane.prefWidthProperty().bind(body.widthProperty().multiply(0.65));
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        VBox rightPane = buildRightPane();
        rightPane.prefWidthProperty().bind(body.widthProperty().multiply(0.35));
        rightPane.setMinWidth(320);

        body.getChildren().addAll(leftPane, rightPane);
        return body;
    }

    /* ══ LEFT PANE ══ */
    private VBox buildLeftPane() {
        VBox pane = new VBox(16);

        VBox roomBox = new VBox(10);
        roomBox.setPadding(new Insets(10, 15, 10, 15));
        roomBox.setStyle("-fx-background-color: " + C_CARD + ";" +
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

        VBox serviceBox = new VBox(12);
        serviceBox.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(serviceBox, Priority.ALWAYS);

        tabBar = new HBox(8);
        // tabBar sẽ được populate qua refreshTabBar() gọi từ constructor hoặc sau khi
        // load data

        servicePane = new FlowPane(12, 12);
        ScrollPane serviceScroll = new ScrollPane(servicePane);
        serviceScroll.setFitToWidth(true);
        servicePane.prefWrapLengthProperty().bind(serviceScroll.widthProperty().subtract(20));
        serviceScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(serviceScroll, Priority.ALWAYS);

        serviceBox.getChildren().addAll(tabBar, serviceScroll);
        VBox.setVgrow(serviceBox, Priority.ALWAYS);

        pane.getChildren().addAll(roomBox, serviceBox);
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    private Button buildTabButton(model.entities.LoaiDichVu cat) {
        boolean isActive = currentCategory != null && cat.getMaLoaiDV().equals(currentCategory.getMaLoaiDV());
        Button btn = new Button(cat.getTenLoaiDV());
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setCursor(Cursor.HAND);
        btn.setPrefHeight(36);
        btn.setPadding(new Insets(0, 16, 0, 16));
        btn.setStyle(isActive
                ? "-fx-background-color: " + C_SIDEBAR + "; -fx-text-fill: white; -fx-background-radius: 8;"
                : "-fx-background-color: " + C_CARD + "; -fx-text-fill: " + C_TEXT_GRAY + ";" +
                        "-fx-border-color: " + C_BORDER + "; -fx-background-radius: 8; -fx-border-radius: 8;");
        btn.setOnAction(e -> {
            currentCategory = cat;
            refreshTabBar();
            refreshServices(cat);
        });
        return btn;
    }

    private void refreshTabBar() {
        tabBar.getChildren().clear();
        List<model.entities.LoaiDichVu> list = loaiDVDAO.getAll();
        for (model.entities.LoaiDichVu c : list)
            tabBar.getChildren().add(buildTabButton(c));
    }

    /* ══ RIGHT PANE ══ */
    private VBox buildRightPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: " + C_CARD + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 12; -fx-background-radius: 12;");
        pane.setEffect(new DropShadow(10, 0, 4, Color.web("#00000008")));

        VBox billHeader = new VBox(4);
        billHeader.setPadding(new Insets(20, 25, 20, 25));
        billHeader.setStyle("-fx-background-color: " + C_SIDEBAR + "; -fx-background-radius: 12 12 0 0;");

        lblRoomTitle = new Label("Hóa đơn: ---");
        lblRoomTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblRoomTitle.setTextFill(Color.WHITE);
        billHeader.getChildren().setAll(lblRoomTitle);

        billContainer = new VBox(0);
        billContainer.setStyle("-fx-background-color: white;");
        ScrollPane billScroll = new ScrollPane(billContainer);
        billScroll.setFitToWidth(true);
        billScroll.setPrefHeight(300);
        billScroll.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");
        VBox.setVgrow(billScroll, Priority.ALWAYS);

        VBox footer = new VBox(15);
        footer.setPadding(new Insets(16, 20, 20, 20));
        footer.setStyle(
                "-fx-border-color: " + C_BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        lblTotal = new Label("Tổng: 0 đ");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTotal.setTextFill(Color.web(C_SIDEBAR));

        HBox actionPanel = new HBox(12);
        actionPanel.setAlignment(Pos.CENTER);

        Button btnClear = new Button("Hủy");
        btnClear.setPrefWidth(110);
        btnClear.setPrefHeight(45);
        btnClear.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        btnClear.setStyle("-fx-background-color: white; -fx-text-fill: " + C_SIDEBAR +
                "; -fx-border-color: #d1d5db; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;");
        btnClear.setOnAction(e -> {
            cart.clear();
            updateBillUI();
            refreshServices(currentCategory);
        });

        Button btnConfirm = new Button("✔ XÁC NHẬN");
        btnConfirm.setPrefWidth(200);
        btnConfirm.setPrefHeight(45);
        btnConfirm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        btnConfirm.setStyle("-fx-background-color: " + C_ACTIVE + "; -fx-text-fill: white;" +
                "-fx-background-radius: 15; -fx-cursor: hand;");
        HBox.setHgrow(btnConfirm, Priority.ALWAYS);
        btnConfirm.setOnAction(e -> handleConfirm());

        actionPanel.getChildren().addAll(btnClear, btnConfirm);
        footer.getChildren().addAll(lblTotal, actionPanel);
        pane.getChildren().addAll(billHeader, billScroll, footer);
        return pane;
    }

    /* ══ ROOM CARD ══ */
    private VBox buildRoomCard(Phong p) {
        boolean isSelected = selectedMaPhong.equals(p.getMaPhong());
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(90, 70);
        card.setCursor(Cursor.HAND);
        card.setStyle("-fx-background-color: " + (isSelected ? "#eff6ff" : C_CARD) + ";" +
                "-fx-border-color: " + (isSelected ? C_SIDEBAR : C_BORDER) + ";" +
                "-fx-border-width: " + (isSelected ? "2" : "1") + ";" +
                "-fx-border-radius: 12; -fx-background-radius: 12;");

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
            if (!selectedMaPhong.equals(p.getMaPhong())) {
                selectedMaPhong = p.getMaPhong();
                usedServicesExpanded = false; // ← reset về thu gọn khi đổi phòng
                lblRoomTitle.setText("Hóa đơn: P." + selectedMaPhong);
                refreshRooms();
                updateBillUI();
            }
        });
        return card;
    }

    /* ══ SERVICE CARD ══ */
    private HBox buildServiceCard(DichVu dv) {
        HBox card = new HBox(12);
        card.setPrefSize(230, 85);
        card.setMaxWidth(230);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + C_BORDER +
                "; -fx-border-radius: 12; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(6, 0, 2, Color.web("#00000005")));

        VBox info = new VBox(4);
        info.setAlignment(Pos.CENTER_LEFT);
        Label lblName = new Label(dv.getTenDV());
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblName.setTextFill(Color.web(C_TEXT_DARK));
        Label lblPrice = new Label(dv.getGia() != null ? String.format("%,.0f đ", dv.getGia()) : "---");
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
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-border-color: #f0f0f0; -fx-border-radius: 5; -fx-background-radius: 5;" +
                " -fx-font-weight: bold; -fx-font-size: 13;");
    }

    /* ══ REFRESH ══ */
    private void refreshRooms() {
        roomPane.getChildren().clear();
        PhongDAO phongDAO = new PhongDAO();
        for (Phong p : phongDAO.getPhongByTrangThai(TrangThaiPhong.DACOKHACH))
            roomPane.getChildren().add(buildRoomCard(p));
    }

    private void refreshServices(model.entities.LoaiDichVu cat) {
        if (cat == null)
            return;
        servicePane.getChildren().clear();
        DichVuDAO dichVuDAO = new DichVuDAO();
        List<DichVu> list = dichVuDAO.getByType(cat.getMaLoaiDV());
        Map<String, Double> activePrices = bangGiaDAO.getActivePriceMap();

        for (DichVu dv : list) {
            Double price = activePrices.get(dv.getMaDV());
            if (price != null) {
                dv.setGia(price);
                servicePane.getChildren().add(buildServiceCard(dv));
            }
        }

        if (!cart.isEmpty()) {
            for (DichVu dvInCart : cart.keySet()) {
                if (activePrices.containsKey(dvInCart.getMaDV())) {
                    dvInCart.setGia(activePrices.get(dvInCart.getMaDV()));
                } else {
                    DichVu base = dichVuDAO.getServiceByID(dvInCart.getMaDV());
                    if (base != null && base.getGia() != null)
                        dvInCart.setGia(base.getGia());
                }
            }
        }
    }

    /* ══ BILL UI ══ */
    private void updateBillUI() {
        billContainer.getChildren().clear();
        double total = 0;

        // ════ 1. DỊCH VỤ ĐÃ DÙNG (collapsible) ════
        if (selectedMaPhong != null && !selectedMaPhong.isEmpty()) {
            String maCTDP = datPhongDAO.getMaCTDPDangSuDungByMaPhong(selectedMaPhong);
            if (maCTDP != null) {
                List<model.entities.DichVuSuDung> usedList = dvsdDAO.findByMaCTDP(maCTDP);
                if (!usedList.isEmpty()) {

                    // Tính tổng tiền dịch vụ đã dùng (dù đang thu gọn hay không)
                    double subTotalUsed = 0;
                    for (model.entities.DichVuSuDung sd : usedList)
                        subTotalUsed += sd.getGiaDV() * sd.getSoLuong();
                    total += subTotalUsed;

                    // ── Header row: nhấp để toggle ──
                    HBox usedHeaderRow = new HBox(6);
                    usedHeaderRow.setAlignment(Pos.CENTER_LEFT);
                    usedHeaderRow.setPadding(new Insets(14, 20, 10, 20));
                    usedHeaderRow.setCursor(Cursor.HAND);
                    usedHeaderRow.setStyle("-fx-background-color: #f0f4ff;" +
                            "-fx-border-color: transparent transparent " + C_BORDER + " transparent;" +
                            "-fx-border-width: 0 0 1 0;");

                    Label lblArrow = new Label(usedServicesExpanded ? "▾" : "▸");
                    lblArrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                    lblArrow.setTextFill(Color.web(C_SIDEBAR));

                    Label lblUsedTitle = new Label("Dịch vụ đã dùng  (" + usedList.size() + " món)");
                    lblUsedTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    lblUsedTitle.setTextFill(Color.web(C_SIDEBAR));
                    HBox.setHgrow(lblUsedTitle, Priority.ALWAYS);

                    Label lblUsedSum = new Label(String.format("%,.0f đ", subTotalUsed));
                    lblUsedSum.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    lblUsedSum.setTextFill(Color.web(C_TEXT_GRAY));

                    usedHeaderRow.getChildren().addAll(lblArrow, lblUsedTitle, lblUsedSum);

                    // Toggle khi nhấp
                    usedHeaderRow.setOnMouseClicked(e -> {
                        usedServicesExpanded = !usedServicesExpanded;
                        updateBillUI();
                    });

                    billContainer.getChildren().add(usedHeaderRow);

                    // ── Nội dung chi tiết (chỉ hiện khi expanded) ──
                    if (usedServicesExpanded) {
                        billContainer.getChildren().add(buildBillHeader(false));
                        for (model.entities.DichVuSuDung sd : usedList) {
                            double sub = sd.getGiaDV() * sd.getSoLuong();
                            billContainer.getChildren().add(
                                    buildBillRow(sd.getDichVu().getTenDV(), sd.getSoLuong(), sub, false, null));
                        }
                        Label lblSub = new Label(String.format("Tạm tính: %,.0f đ", subTotalUsed));
                        lblSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                        lblSub.setPadding(new Insets(5, 15, 12, 15));
                        lblSub.setMaxWidth(Double.MAX_VALUE);
                        lblSub.setAlignment(Pos.CENTER_RIGHT);
                        billContainer.getChildren().add(lblSub);
                    }
                }
            }
        }

        // ════ 2. DỊCH VỤ THÊM MỚI (luôn hiện, không thu gọn) ════
        if (!cart.isEmpty()) {
            HBox newHeaderRow = new HBox(6);
            newHeaderRow.setAlignment(Pos.CENTER_LEFT);
            newHeaderRow.setPadding(new Insets(14, 20, 10, 20));
            newHeaderRow.setStyle("-fx-background-color: #f0fdf4;" +
                    "-fx-border-color: transparent transparent " + C_BORDER + " transparent;" +
                    "-fx-border-width: 0 0 1 0;");

            Label lblNewTitle = new Label("Dịch vụ thêm mới  (" + cart.size() + " món)");
            lblNewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblNewTitle.setTextFill(Color.web("#166534")); // xanh lá đậm
            HBox.setHgrow(lblNewTitle, Priority.ALWAYS);
            newHeaderRow.getChildren().add(lblNewTitle);
            billContainer.getChildren().add(newHeaderRow);

            billContainer.getChildren().add(buildBillHeader(true));

            double subTotalNew = 0;
            for (Map.Entry<DichVu, Integer> entry : cart.entrySet()) {
                DichVu dv = entry.getKey();
                int qty = entry.getValue();
                double price = dv.getGia() != null ? dv.getGia() : 0;
                double sub = price * qty;
                subTotalNew += sub;
                billContainer.getChildren().add(buildBillRow(dv.getTenDV(), qty, sub, true, dv));
            }
            total += subTotalNew;

            Label lblSub = new Label(String.format("Tạm tính: %,.0f đ", subTotalNew));
            lblSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            lblSub.setPadding(new Insets(5, 15, 15, 15));
            lblSub.setMaxWidth(Double.MAX_VALUE);
            lblSub.setAlignment(Pos.CENTER_RIGHT);
            billContainer.getChildren().add(lblSub);
        }

        lblTotal.setText(String.format("Tổng cộng: %,.0f đ", total));
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
    }

    /* ══ BILL HEADER ══ */
    private HBox buildBillHeader(boolean showDelete) {
        HBox header = new HBox(0);
        header.setPadding(new Insets(5, 20, 5, 20));
        header.setStyle("-fx-border-color: transparent transparent #f3f4f6 transparent; -fx-border-width: 0 0 1 0;");

        Label lblName = new Label("Dịch vụ");
        lblName.setPrefWidth(160);

        Label lblQty = new Label("SL");
        lblQty.setPrefWidth(40);
        lblQty.setAlignment(Pos.CENTER);

        Label lblPrice = new Label("Tiền");
        lblPrice.setPrefWidth(110);
        lblPrice.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(lblName, lblQty, lblPrice);

        if (showDelete) {
            Label lblDel = new Label("Xóa");
            lblDel.setPrefWidth(50);
            lblDel.setPadding(new Insets(0, 0, 0, 30));
            lblDel.setAlignment(Pos.CENTER_LEFT);
            header.getChildren().add(lblDel);
        } else {
            Label placeholder = new Label("");
            placeholder.setPrefWidth(50);
            header.getChildren().add(placeholder);
        }

        for (javafx.scene.Node n : header.getChildren()) {
            if (n instanceof Label l) {
                l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                l.setTextFill(Color.web(C_TEXT_GRAY));
            }
        }
        return header;
    }

    /* ══ BILL ROW ══ */
    private HBox buildBillRow(String name, int qty, double sub, boolean showDelete, DichVu dv) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-border-color: transparent transparent #f9fafb transparent; -fx-border-width: 0 0 1 0;");

        Label lblName = new Label(name);
        lblName.setPrefWidth(160);
        lblName.setFont(Font.font("Segoe UI", 13));
        lblName.setTextFill(Color.web(C_TEXT_DARK));

        Label lblQty = new Label(String.valueOf(qty));
        lblQty.setPrefWidth(40);
        lblQty.setAlignment(Pos.CENTER);
        lblQty.setFont(Font.font("Segoe UI", 13));

        Label lblSub = new Label(String.format("%,.0f", sub));
        lblSub.setPrefWidth(110);
        lblSub.setAlignment(Pos.CENTER_RIGHT);
        lblSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblSub.setTextFill(Color.web(C_SIDEBAR));

        row.getChildren().addAll(lblName, lblQty, lblSub);

        if (showDelete) {
            StackPane delPane = new StackPane();
            delPane.setPrefWidth(50);
            delPane.setPadding(new Insets(0, 0, 0, 36));
            delPane.setAlignment(Pos.CENTER_LEFT);

            Button btn = new Button();
            try {
                ImageView iv = new ImageView(new Image("file:src/icon/xoa.png"));
                iv.setFitWidth(14);
                iv.setFitHeight(14);
                btn.setGraphic(iv);
            } catch (Exception e) {
                btn.setText("✕");
            }
            btn.setPrefSize(25, 25);
            btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;" +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                cart.remove(dv);
                updateBillUI();
                refreshServices(currentCategory);
            });
            delPane.getChildren().add(btn);
            row.getChildren().add(delPane);
        } else {
            StackPane placeholder = new StackPane();
            placeholder.setPrefWidth(50);
            row.getChildren().add(placeholder);
        }

        return row;
    }

    /* ══ CONFIRM ══ */
    private void handleConfirm() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn dịch vụ",
                    "Vui lòng thêm ít nhất một dịch vụ vào đơn!");
            return;
        }
        if (selectedMaPhong.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn phòng",
                    "Bạn đã chọn " + cart.size() + " dịch vụ. Vui lòng chọn phòng ở bên trái để xác nhận!");
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
                    showAlert(Alert.AlertType.ERROR, "Lỗi lưu dữ liệu",
                            "Vui lòng kiểm tra kết nối CSDL!");
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