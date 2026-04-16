package gui;

import dao.PhongDAO;
import model.entities.Phong;
import model.enums.TrangThaiPhong;
import model.utils.BadgeUtils;

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

    /* ── DAO & dữ liệu ─────────────────────────────────────────────── */
    private final PhongDAO dao = new PhongDAO();
    private ObservableList<Phong> masterData = FXCollections.observableArrayList();
    private FilteredList<Phong> filteredData;

    /* ── Controls ───────────────────────────────────────────────────── */
    private TableView<Phong> table;
    private TextField txtSearch;

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
        Label lblTitle = new Label("Quản lý phòng sách phòng");
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

        header.getChildren().addAll(titleRow, txtSearch);
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
        colSTT.setCellValueFactory(
                p -> new SimpleStringProperty(String.valueOf(table.getItems().indexOf(p.getValue()) + 1)));

        // Cột Mã phòng
        TableColumn<Phong, String> colMa = new TableColumn<>("Mã phòng");
        colMa.setMinWidth(100);
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(nvl(p.getValue().getMaPhong())));

        // Cột Loại phòng
        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại phòng");
        colLoai.setMinWidth(150);
        colLoai.setStyle("-fx-alignment: CENTER;");
        colLoai.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getLoaiPhong() != null ? p.getValue().getLoaiPhong().toString() : ""));

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
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(p -> {
            TrangThaiPhong tt = p.getValue().getTrangThai();
            String val = (tt != null) ? tt.getLabel() : "Không xác định";
            return new SimpleStringProperty(val);
        });
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isOccupied = TrangThaiPhong.DACOKHACH.getLabel().equals(item);

                    // Style dựa trên trạng thái
                    String bg, text;
                    if (TrangThaiPhong.CONTRONG.getLabel().equals(item)) {
                        bg = "#d1fae5";
                        text = "#065f46"; // Emerald
                    } else if (isOccupied) {
                        bg = "#fef3c7";
                        text = "#92400e"; // Amber
                    } else {
                        bg = "#fee2e2";
                        text = "#991b1b"; // Rose
                    }

                    // Sử dụng Utility để tạo Badge đồng nhất
                    HBox badge = BadgeUtils.createStatusBadge(item, bg, text, !isOccupied);

                    // Menu chọn trạng thái: CHỈ cho phép Còn trống / Đang bảo trì
                    if (!isOccupied) {
                        ContextMenu statusMenu = new ContextMenu();
                        MenuItem m1 = new MenuItem("✅  " + TrangThaiPhong.CONTRONG.getLabel());
                        m1.setOnAction(ev -> updateStatus((Phong) getTableRow().getItem(), TrangThaiPhong.CONTRONG));
                        MenuItem m2 = new MenuItem("🗑   " + TrangThaiPhong.BAN.getLabel());
                        m2.setOnAction(ev -> updateStatus((Phong) getTableRow().getItem(), TrangThaiPhong.BAN));
                        MenuItem m3 = new MenuItem("🔧   " + TrangThaiPhong.BAOTRI.getLabel());
                        m3.setOnAction(ev -> updateStatus((Phong) getTableRow().getItem(), TrangThaiPhong.BAOTRI));
                        statusMenu.getItems().addAll(m1, m2, m3);

                        badge.setOnMouseClicked(e -> {
                            if (e.getButton() == MouseButton.PRIMARY) {
                                statusMenu.show(badge, e.getScreenX(), e.getScreenY());
                            }
                        });
                    }

                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
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
                if (p != null) {
                    if (p.getTrangThai() == TrangThaiPhong.DACOKHACH) {
                        showAlert(Alert.AlertType.WARNING, "Không thể chỉnh sửa",
                                "Phòng đang có khách. Vui lòng thực hiện trả phòng trước.");
                    } else {
                        openDialog(p);
                    }
                }
            });

            MenuItem miDelete = new MenuItem("🗑  Xóa phòng");
            miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
            miDelete.setOnAction(e -> {
                Phong p = table.getSelectionModel().getSelectedItem();
                if (p != null) {
                    if (p.getTrangThai() == TrangThaiPhong.DACOKHACH) {
                        showAlert(Alert.AlertType.WARNING, "Không thể xóa",
                                "Phòng đang có khách. Vui lòng thực hiện trả phòng trước.");
                    } else {
                        handleDelete(p);
                    }
                }
            });

            ctxMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDelete);

            table.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY && table.getSelectionModel().getSelectedItem() != null) {
                    ctxMenu.show(table, e.getScreenX(), e.getScreenY());
                } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    Phong p = table.getSelectionModel().getSelectedItem();
                    if (p != null) {
                        if (p.getTrangThai() == TrangThaiPhong.DACOKHACH) {
                            showAlert(Alert.AlertType.WARNING, "Không thể chỉnh sửa",
                                    "Phòng đang có khách. Vui lòng thực hiện trả phòng trước.");
                        } else {
                            openDialog(p);
                        }
                    }
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

        } catch (Exception ex) {
            // Không làm gì nếu lỗi db
        }
    }

    private void applyFilter(String keyword) {
        if (filteredData == null)
            return;
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        filteredData.setPredicate(p -> {
            if (kw.isEmpty())
                return true;
            String lp = p.getLoaiPhong() != null ? p.getLoaiPhong().toString().toLowerCase() : "";
            return nvl(p.getMaPhong()).toLowerCase().contains(kw) || lp.contains(kw);
        });
    }

    private void openDialog(Phong p) {
        Window owner = getScene().getWindow();
        new ThemSuaPhongDialog(owner, p, dao, this::loadData).showDialog();
    }

    private void updateStatus(Phong p, TrangThaiPhong newStatus) {
        if (p == null || p.getTrangThai() == newStatus)
            return;
        p.setTrangThai(newStatus);
        if (dao.update(p)) {
            loadData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Cập nhật thất bại", "Không thể cập nhật trạng thái phòng này.");
            loadData(); // reload to revert local change if db failed
        }
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
                showAlert(Alert.AlertType.ERROR, "Xóa thất bại",
                        "Kiểm tra lại xem phòng có đang dính dữ liệu hóa đơn không.");
            }
        }
    }

    /* ── Utilities ──────────────────────────────────────────────────── */

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
