package gui;

import dao.PhongDAO;
import model.entities.Phong;
import model.enums.TrangThaiPhong;

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

import java.util.List;
import java.util.Optional;

/**
 * QuanLyPhongView – JavaFX
 * Thay thế QuanLyPhongPanel (Swing).
 */
public class QuanLyPhongView extends BorderPane {

    /* ── Bảng màu ───────────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD_BG = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_RED = "#dc2626";
    private static final String C_GREEN = "#16a34a";
    private static final String C_GOLD = "#d97706";

    /* ── DAO & dữ liệu ─────────────────────────────────────────────── */
    private final PhongDAO dao = new PhongDAO();
    private ObservableList<Phong> masterData = FXCollections.observableArrayList();
    private FilteredList<Phong> filteredData;

    /* ── Controls ───────────────────────────────────────────────────── */
    private TableView<Phong> table;
    private TextField txtSearch;
    private Label lblTongPhong, lblPhongTrong, lblCoKhach, lblBaoTri;

    /* ── PHÂN QUYỀN ────────────────────────────────────────────── */
    private final boolean isAdmin;

    public QuanLyPhongView(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildTableCard());

        loadData();
    }

    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 12, 0));

        /* ── Dòng 1: Tiêu đề + Nút thêm ─────────────────────────── */
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label lblTitle = new Label("Danh sách phòng");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSubtitle = new Label("Quản lý và theo dõi trạng thái danh sách phòng");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        titleRow.getChildren().add(titleBox);

        if (isAdmin) {
            Button btnAdd = new Button("＋  Thêm phòng mới");
            btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btnAdd.setPrefHeight(40);
            btnAdd.setCursor(Cursor.HAND);
            styleButton(btnAdd, C_BLUE, "white", C_BLUE_HOVER);
            btnAdd.setOnAction(e -> openDialog(null));
            titleRow.getChildren().add(btnAdd);
        }

        /* ── Dòng 2: Thẻ thống kê ────────────────────────────────── */
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        lblTongPhong = new Label("0");
        lblPhongTrong = new Label("0");
        lblCoKhach = new Label("0");
        lblBaoTri = new Label("0");

        VBox c1 = createStatCard("🏢", "TỔNG SỐ PHÒNG", lblTongPhong, C_NAVY);
        VBox c2 = createStatCard("✅", "PHÒNG TRỐNG", lblPhongTrong, C_GREEN);
        VBox c3 = createStatCard("🛏", "ĐANG CÓ KHÁCH", lblCoKhach, C_GOLD);
        VBox c4 = createStatCard("🔧", "ĐANG BẢO TRÌ", lblBaoTri, C_RED);

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);
        statsRow.getChildren().addAll(c1, c2, c3, c4);

        /* ── Dòng 3: Thanh tìm kiếm ──────────────────────────────── */
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm kiếm theo mã phòng hoặc loại phòng...");
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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Không có dữ liệu phòng"));

        // Cột STT
        TableColumn<Phong, String> colSTT = new TableColumn<>("STT");
        colSTT.setMinWidth(50);
        colSTT.setMaxWidth(60);
        colSTT.setStyle("-fx-alignment: CENTER;");
        colSTT.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(table.getItems().indexOf(p.getValue()) + 1)));

        // Cột Mã phòng
        TableColumn<Phong, String> colMa = new TableColumn<>("Mã phòng");
        colMa.setMinWidth(100);
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(nvl(p.getValue().getMaPhong())));

        // Cột Loại phòng
        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại phòng");
        colLoai.setMinWidth(150);
        colLoai.setStyle("-fx-alignment: CENTER;");
        colLoai.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getLoaiPhong() != null ? nvl(p.getValue().getLoaiPhong().getMaLoaiPhong()) : ""));

        // Cột Đơn giá
        TableColumn<Phong, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setMinWidth(130);
        colGia.setStyle("-fx-alignment: CENTER;");
        colGia.setCellValueFactory(p -> {
            if (p.getValue().getLoaiPhong() != null) {
                return new SimpleStringProperty(String.format("%,.0f đ", p.getValue().getLoaiPhong().getGia()));
            }
            return new SimpleStringProperty("");
        });

        // Cột Sức chứa
        TableColumn<Phong, String> colSucChua = new TableColumn<>("Sức chứa");
        colSucChua.setMinWidth(100);
        colSucChua.setStyle("-fx-alignment: CENTER;");
        colSucChua.setCellValueFactory(p -> {
            if (p.getValue().getLoaiPhong() != null) {
                return new SimpleStringProperty(p.getValue().getLoaiPhong().getSucChua() + " người");
            }
            return new SimpleStringProperty("");
        });

        // Cột Trạng thái
        TableColumn<Phong, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setMinWidth(150);
        colTrangThai.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        colTrangThai.setCellValueFactory(p -> {
            TrangThaiPhong tt = p.getValue().getTrangThai();
            String val = "Không xác định";
            if (tt == TrangThaiPhong.CONTRONG) val = "Còn trống";
            else if (tt == TrangThaiPhong.DACOKHACH) val = "Đã có khách";
            else if (tt == TrangThaiPhong.BAN) val = "Đang bảo trì";
            return new SimpleStringProperty(val);
        });
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Còn trống")) setStyle("-fx-text-fill: " + C_GREEN + "; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    else if (item.equals("Đã có khách")) setStyle("-fx-text-fill: " + C_GOLD + "; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: " + C_RED + "; -fx-alignment: CENTER; -fx-font-weight: bold;");
                }
            }
        });

        // Cột Tầng
        TableColumn<Phong, String> colTang = new TableColumn<>("Tầng");
        colTang.setMinWidth(80);
        colTang.setStyle("-fx-alignment: CENTER;");
        colTang.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getSoTang())));

        table.getColumns().add(colSTT);
        table.getColumns().add(colMa);
        table.getColumns().add(colLoai);
        table.getColumns().add(colGia);
        table.getColumns().add(colSucChua);
        table.getColumns().add(colTrangThai);
        table.getColumns().add(colTang);

        for (TableColumn<Phong, ?> c : table.getColumns()) {
            c.setReorderable(false);
            c.setSortable(false);
        }

        // ContextMenu (Nhấn phải chuột)
        if (isAdmin) {
            ContextMenu ctxMenu = new ContextMenu();
            MenuItem miEdit = new MenuItem("✏  Chỉnh sửa phòng");
            miEdit.setStyle("-fx-font-size: 13px;");
            miEdit.setOnAction(e -> {
                Phong p = table.getSelectionModel().getSelectedItem();
                if (p != null) openDialog(p);
            });

            MenuItem miDelete = new MenuItem("🗑  Xóa phòng");
            miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
            miDelete.setOnAction(e -> {
                Phong p = table.getSelectionModel().getSelectedItem();
                if (p != null) handleDelete(p);
            });

            ctxMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDelete);

            table.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY && table.getSelectionModel().getSelectedItem() != null) {
                    ctxMenu.show(table, e.getScreenX(), e.getScreenY());
                } else {
                    ctxMenu.hide();
                }
            });
        }

        VBox.setVgrow(table, Priority.ALWAYS);
        card.getChildren().add(table);
        return card;
    }

    public void loadData() {
        try {
            List<Phong> ds = dao.getAll();
            masterData.setAll(ds);

            filteredData = new FilteredList<>(masterData, p -> true);
            table.setItems(filteredData);

            int tr = 0, ck = 0, bt = 0;
            for (Phong p : ds) {
                if (p.getTrangThai() == TrangThaiPhong.CONTRONG) tr++;
                else if (p.getTrangThai() == TrangThaiPhong.DACOKHACH) ck++;
                else bt++;
            }

            lblTongPhong.setText(String.valueOf(ds.size()));
            lblPhongTrong.setText(String.valueOf(tr));
            lblCoKhach.setText(String.valueOf(ck));
            lblBaoTri.setText(String.valueOf(bt));
        } catch (Exception ex) {
            lblTongPhong.setText("—");
            lblPhongTrong.setText("—");
            lblCoKhach.setText("—");
            lblBaoTri.setText("—");
        }
    }

    private void applyFilter(String keyword) {
        if (filteredData == null) return;
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredData.setPredicate(p -> {
            if (kw.isEmpty()) return true;
            String lp = p.getLoaiPhong() != null ? nvl(p.getLoaiPhong().getMaLoaiPhong()).toLowerCase() : "";
            return nvl(p.getMaPhong()).toLowerCase().contains(kw) || lp.contains(kw);
        });
    }

    private void openDialog(Phong p) {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        new ThemSuaPhongDialog(owner, p, dao, this::loadData).show();
    }

    private void handleDelete(Phong p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa phòng [" + p.getMaPhong() + "]?");
        confirm.setContentText("Hành động này không thể hoàn tác.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dao.delete(p.getMaPhong())) {
                showAlert(Alert.AlertType.INFORMATION, "Đã xóa", "Phòng " + p.getMaPhong() + " đã được xóa.");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Xóa thất bại", "Kiểm tra lại xem phòng có đang dính dữ liệu hóa đơn không.");
            }
        }
    }

    /* ── Utilities ──────────────────────────────────────────────────── */

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
        valueLbl.setTextFill(Color.web(accentHex));

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
        return card;
    }

    private void styleButton(Button btn, String bg, String fg, String hoverBg) {
        String base = "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 20; -fx-cursor: hand;";
        String hover = base.replace("-fx-background-color: " + bg, "-fx-background-color: " + hoverBg);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void showAlert(Alert.AlertType type, String header, String msg) {
        Alert a = new Alert(type);
        a.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }
}
