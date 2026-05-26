package gui;

import dao.LoaiPhongDAO;
import dao.LoaiPhongTienNghiDAO;
import dao.TienNghiDAO;
import model.entities.LoaiPhong;
import model.entities.TienNghi;
import model.utils.BadgeUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuanLyTienNghiView extends BorderPane {

    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD_BG = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_BLUE_HOVER = "#1e40af";
    private static final String C_SIDEBAR = "#1e3a8a";
    private static final String C_ACTIVE = "#1d4ed8";

    private final TienNghiDAO tnDAO = new TienNghiDAO();
    private final LoaiPhongDAO lpDAO = new LoaiPhongDAO();
    private final LoaiPhongTienNghiDAO lptnDAO = new LoaiPhongTienNghiDAO();

    private ObservableList<TienNghi> masterData = FXCollections.observableArrayList();
    private ObservableList<TienNghiWrapper> allWrappers = FXCollections.observableArrayList();
    private FilteredList<TienNghiWrapper> filteredWrappers;
    private TableView<TienNghiWrapper> table;
    private ListView<LoaiPhong> listLoaiPhong;
    
    // Lưu trạng thái checkbox cho loại phòng đang chọn
    private Map<String, SimpleBooleanProperty> checkboxStates = new HashMap<>();
    private Map<String, SimpleStringProperty> soLuongStates = new HashMap<>();

    public QuanLyTienNghiView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        setTop(buildHeader());
        setCenter(buildMainContent());

        loadDataTienNghi();
        loadLoaiPhong();
    }

    private VBox buildHeader() {
        VBox header = new VBox(20);
        header.setPadding(new Insets(0, 0, 12, 0));

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label lblTitle = new Label("Quản lý Tiện nghi & Cấu hình");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(C_TEXT_DARK));
        Label lblSubtitle = new Label("Quản lý danh sách tiện nghi và gán tiện nghi cho từng loại phòng");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        Button btnAdd = new Button("＋  Thêm tiện nghi");
        btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnAdd.setPrefHeight(40);
        btnAdd.setCursor(Cursor.HAND);
        styleButton(btnAdd, C_BLUE, "white", C_BLUE_HOVER);
        btnAdd.setOnAction(e -> openDialog(null));
        
        Button btnSave = new Button("💾 Lưu cấu hình");
        btnSave.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnSave.setPrefHeight(40);
        btnSave.setCursor(Cursor.HAND);
        styleButton(btnSave, C_SIDEBAR, "white", C_ACTIVE);
        btnSave.setOnAction(e -> handleSaveConfig());

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm kiếm mã hoặc tên tiện nghi...");
        txtSearch.setPrefWidth(450);
        txtSearch.setPrefHeight(40);
        txtSearch.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: " + C_BORDER + "; -fx-border-radius: 8; -fx-padding: 8 12;");
        txtSearch.textProperty().addListener((obs, oldV, newV) -> {
            if (filteredWrappers != null) {
                filteredWrappers.setPredicate(wrapper -> {
                    if (newV == null || newV.isEmpty()) return true;
                    String lowerCaseFilter = newV.toLowerCase();
                    return wrapper.getTienNghi().getMaTienNghi().toLowerCase().contains(lowerCaseFilter)
                        || wrapper.getTienNghi().getTenTienNghi().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });

        titleRow.getChildren().addAll(titleBox, txtSearch, new Label("  "), btnSave, new Label("  "), btnAdd);

        header.getChildren().addAll(titleRow);
        return header;
    }

    private HBox buildMainContent() {
        HBox mainBox = new HBox(20);
        
        // --- Cột trái: Loại phòng ---
        VBox leftCard = new VBox(10);
        leftCard.setPrefWidth(250);
        leftCard.setStyle("-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        leftCard.setEffect(new DropShadow(8, 0, 2, Color.web("#00000010")));
        leftCard.setPadding(new Insets(16));
        
        Label lblLoaiPhong = new Label("Chọn Loại Phòng");
        lblLoaiPhong.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblLoaiPhong.setTextFill(Color.web(C_TEXT_DARK));
        
        listLoaiPhong = new ListView<>();
        listLoaiPhong.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        listLoaiPhong.setCellFactory(lv -> new ListCell<LoaiPhong>() {
            @Override
            protected void updateItem(LoaiPhong item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setFont(Font.font("Segoe UI", 14));
                    setPadding(new Insets(10));
                }
            }
        });
        listLoaiPhong.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                loadConfigForLoaiPhong(n.getMaLoaiPhong());
            }
        });
        VBox.setVgrow(listLoaiPhong, Priority.ALWAYS);
        
        leftCard.getChildren().addAll(lblLoaiPhong, listLoaiPhong);

        // --- Cột phải: Bảng tiện nghi ---
        VBox rightCard = new VBox(0);
        HBox.setHgrow(rightCard, Priority.ALWAYS);
        rightCard.setStyle("-fx-background-color: " + C_CARD_BG + ";" +
                "-fx-border-color: " + C_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        rightCard.setEffect(new DropShadow(8, 0, 2, Color.web("#00000010")));
        
        table = new TableView<>();
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-table-cell-border-color: " + C_BORDER + ";");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        table.setEditable(true); // Cho phép sửa cột checkbox

        // Cột Chọn (Checkbox)
        TableColumn<TienNghiWrapper, Boolean> colCheck = new TableColumn<>();
        CheckBox cbSelectAll = new CheckBox("Chọn");
        cbSelectAll.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold;");
        cbSelectAll.setOnAction(e -> {
            boolean selected = cbSelectAll.isSelected();
            for (TienNghiWrapper w : table.getItems()) {
                w.selectedProperty().set(selected);
            }
        });
        colCheck.setGraphic(cbSelectAll);
        colCheck.setMinWidth(80);
        colCheck.setMaxWidth(100);
        colCheck.setStyle("-fx-alignment: CENTER;");
        colCheck.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));
        colCheck.setEditable(true);

        // Cột Mã TN
        TableColumn<TienNghiWrapper, String> colMa = new TableColumn<>("Mã TN");
        colMa.setMinWidth(80);
        colMa.setMaxWidth(120);
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTienNghi().getMaTienNghi()));
        colMa.setEditable(false);

        // Cột Số lượng
        TableColumn<TienNghiWrapper, String> colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setMinWidth(80);
        colSoLuong.setMaxWidth(100);
        colSoLuong.setStyle("-fx-alignment: CENTER;");
        colSoLuong.setCellValueFactory(p -> p.getValue().soLuongProperty());
        colSoLuong.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        colSoLuong.setOnEditCommit(e -> {
            // Chỉ cho nhập số
            if (e.getNewValue().matches("\\d+")) {
                e.getRowValue().soLuongProperty().set(e.getNewValue());
            } else {
                e.getRowValue().soLuongProperty().set(e.getOldValue());
                table.refresh();
            }
        });
        colSoLuong.setEditable(true);

        // Cột Tên TN
        TableColumn<TienNghiWrapper, String> colTen = new TableColumn<>("Tên Tiện Nghi");
        colTen.setMinWidth(200);
        colTen.setStyle("-fx-alignment: CENTER_LEFT;");
        colTen.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTienNghi().getTenTienNghi()));
        colTen.setEditable(false);
        
        // Cột Mô tả
        TableColumn<TienNghiWrapper, String> colMoTa = new TableColumn<>("Mô tả");
        colMoTa.setMinWidth(250);
        colMoTa.setStyle("-fx-alignment: CENTER_LEFT;");
        colMoTa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTienNghi().getMoTa()));
        colMoTa.setEditable(false);

        // Cột Trạng thái
        TableColumn<TienNghiWrapper, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setMinWidth(120);
        colTrangThai.setMaxWidth(150);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTienNghi().isTrangThai() ? "Sử dụng" : "Ngưng"));
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                    setCursor(Cursor.DEFAULT);
                } else {
                    boolean active = "Sử dụng".equals(item);
                    String bg = active ? "#d1fae5" : "#fee2e2";
                    String text = active ? "#065f46" : "#991b1b";
                    setGraphic(BadgeUtils.createStatusBadge(item, bg, text, false));
                    setAlignment(Pos.CENTER);
                    setCursor(Cursor.HAND);
                    
                    setOnMouseClicked(e -> {
                        TienNghiWrapper tw = (TienNghiWrapper) getTableRow().getItem();
                        if (tw != null) {
                            TienNghi tn = tw.getTienNghi();
                            tn.setTrangThai(!tn.isTrangThai());
                            if (tnDAO.update(tn)) {
                                loadDataTienNghi();
                            }
                        }
                    });
                }
            }
        });
        colTrangThai.setEditable(false);
        
        table.getColumns().addAll(colCheck, colMa, colTen, colSoLuong, colMoTa, colTrangThai);
        
        // Menu ngữ cảnh & Double click để Sửa/Xóa gốc
        ContextMenu ctxMenu = new ContextMenu();
        MenuItem miEdit = new MenuItem("✏  Cập nhật tiện nghi");
        miEdit.setStyle("-fx-font-size: 13px;");
        miEdit.setOnAction(e -> {
            TienNghiWrapper tw = table.getSelectionModel().getSelectedItem();
            if (tw != null) openDialog(tw.getTienNghi());
        });

        MenuItem miDelete = new MenuItem("🗑  Xóa tiện nghi");
        miDelete.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
        miDelete.setOnAction(e -> {
            TienNghiWrapper tw = table.getSelectionModel().getSelectedItem();
            if (tw != null) handleDelete(tw.getTienNghi());
        });
        ctxMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDelete);

        table.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY && table.getSelectionModel().getSelectedItem() != null) {
                ctxMenu.show(table, e.getScreenX(), e.getScreenY());
            } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TienNghiWrapper tw = table.getSelectionModel().getSelectedItem();
                // Nếu click đúng cột (không phải cột checkbox) thì sửa
                if (tw != null && table.getSelectionModel().getSelectedCells().get(0).getColumn() != 0) {
                    openDialog(tw.getTienNghi());
                }
            } else {
                ctxMenu.hide();
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        rightCard.getChildren().add(table);
        
        mainBox.getChildren().addAll(leftCard, rightCard);
        return mainBox;
    }

    private void loadLoaiPhong() {
        List<LoaiPhong> lps = lpDAO.getAll();
        listLoaiPhong.setItems(FXCollections.observableArrayList(lps));
        if (!lps.isEmpty()) {
            listLoaiPhong.getSelectionModel().selectFirst();
        }
    }

    private void loadDataTienNghi() {
        masterData.setAll(tnDAO.getAll());
        rebuildTableWrappers();
    }
    
    private void rebuildTableWrappers() {
        allWrappers.clear();
        for (TienNghi tn : masterData) {
            SimpleBooleanProperty prop = checkboxStates.computeIfAbsent(tn.getMaTienNghi(), k -> new SimpleBooleanProperty(false));
            SimpleStringProperty slProp = soLuongStates.computeIfAbsent(tn.getMaTienNghi(), k -> new SimpleStringProperty("1"));
            allWrappers.add(new TienNghiWrapper(tn, prop, slProp));
        }
        if (filteredWrappers == null) {
            filteredWrappers = new FilteredList<>(allWrappers, p -> true);
            table.setItems(filteredWrappers);
        }
    }
    
    private void loadConfigForLoaiPhong(String maLoaiPhong) {
        // Reset toàn bộ
        for (SimpleBooleanProperty prop : checkboxStates.values()) {
            prop.set(false);
        }
        for (SimpleStringProperty prop : soLuongStates.values()) {
            prop.set("1");
        }
        // Load danh sách tiện nghi của loại phòng này
        Map<String, Integer> assigned = lptnDAO.getTienNghiMapByLoaiPhong(maLoaiPhong);
        for (Map.Entry<String, Integer> entry : assigned.entrySet()) {
            SimpleBooleanProperty prop = checkboxStates.get(entry.getKey());
            SimpleStringProperty slProp = soLuongStates.get(entry.getKey());
            if (prop != null) prop.set(true);
            if (slProp != null) slProp.set(String.valueOf(entry.getValue()));
        }
    }
    
    private void handleSaveConfig() {
        LoaiPhong lp = listLoaiPhong.getSelectionModel().getSelectedItem();
        if (lp == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn loại phòng", "Vui lòng chọn loại phòng bên trái để cấu hình.");
            return;
        }
        
        Map<String, Integer> selectedMaTN = new HashMap<>();
        for (TienNghi tn : masterData) {
            SimpleBooleanProperty prop = checkboxStates.get(tn.getMaTienNghi());
            if (prop != null && prop.get()) {
                int sl = 1;
                try { sl = Integer.parseInt(soLuongStates.get(tn.getMaTienNghi()).get()); } catch (Exception ignored) {}
                selectedMaTN.put(tn.getMaTienNghi(), sl);
            }
        }
        
        if (lptnDAO.updateTienNghiForLoaiPhong(lp.getMaLoaiPhong(), selectedMaTN)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu cấu hình tiện nghi cho loại phòng " + lp.toString() + ".");
        } else {
            showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi lưu cấu hình.");
        }
    }

    private void openDialog(TienNghi tn) {
        Window owner = getScene().getWindow();
        new ThemSuaTienNghiDialog(owner, tn, tnDAO, this::loadDataTienNghi).showDialog();
    }

    private void handleDelete(TienNghi tn) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa tiện nghi [" + tn.getTenTienNghi() + "]?");
        confirm.setContentText("Hành động này sẽ chuyển trạng thái thành Ngưng sử dụng.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (tnDAO.delete(tn.getMaTienNghi())) {
                showAlert(Alert.AlertType.INFORMATION, "Đã xóa", "Tiện nghi đã chuyển sang trạng thái ngưng sử dụng.");
                loadDataTienNghi();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể vô hiệu hóa tiện nghi này.");
            }
        }
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
    
    // Lớp bọc TienNghi để hỗ trợ CheckBox trong TableView
    public static class TienNghiWrapper {
        private final TienNghi tienNghi;
        private final SimpleBooleanProperty selected;
        private final SimpleStringProperty soLuong;
        
        public TienNghiWrapper(TienNghi tn, SimpleBooleanProperty prop, SimpleStringProperty slProp) {
            this.tienNghi = tn;
            this.selected = prop;
            this.soLuong = slProp;
        }
        
        public TienNghi getTienNghi() { return tienNghi; }
        public SimpleBooleanProperty selectedProperty() { return selected; }
        public SimpleStringProperty soLuongProperty() { return soLuong; }
    }
}
