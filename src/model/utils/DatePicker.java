package model.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Popup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * SmartDatePicker – DatePicker custom với ComboBox chọn nhanh tháng/năm.
 *
 * Trông như một ô nhập ngày duy nhất (giống DatePicker gốc).
 * Khi click → mở popup:
 * ┌─────────────────────────────────────┐
 * │ [< ] [Tháng ▾] [Năm ▾] [ >] │
 * ├─────────────────────────────────────┤
 * │ T2 T3 T4 T5 T6 T7 CN │
 * │ 1 2 3 4 5 6 7 │
 * │ 8 9 10 ... │
 * └─────────────────────────────────────┘
 *
 * Sử dụng:
 * SmartDatePicker dp = new SmartDatePicker();
 * dp.setValue(LocalDate.of(2000, 5, 15));
 * LocalDate date = dp.getValue();
 */
public class DatePicker extends HBox {

    /* ── Bảng màu ─────────────────────────────────────────────────── */
    private static final String C_BORDER = "#e9ecef";
    private static final String C_BLUE = "#1d4ed8";
    private static final String C_NAVY = "#1e3a8a";
    private static final String C_TODAY_BG = "#dbeafe";
    private static final String C_SELECT_BG = "#1d4ed8";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] WEEK_DAYS = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
    private static final String[] MONTH_NAMES = {
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    };

    /* ── State ────────────────────────────────────────────────────── */
    private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(this, "value");
    private YearMonth viewMonth; // tháng đang hiển thị trên popup
    private final TextField txtDisplay;
    private Popup popup;
    private GridPane calendarGrid;
    private ComboBox<String> cbMonth;
    private ComboBox<Integer> cbYear;

    private final int minYear;
    private final int maxYear;
    private LocalDate minDate;

    /*
     * ══════════════════════════════════════════════════════════════════
     * CONSTRUCTOR
     * ══════════════════════════════════════════════════════════════════
     */
    public DatePicker() {
        this(LocalDate.now().getYear() - 100, LocalDate.now().getYear());
    }

    /**
     * @param minYear năm nhỏ nhất trong danh sách
     * @param maxYear năm lớn nhất trong danh sách
     */
    public DatePicker(int minYear, int maxYear) {
        super(0);
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.viewMonth = YearMonth.now();
        this.minDate = null;

        setAlignment(Pos.CENTER_LEFT);
        setCursor(Cursor.HAND);

        /* ── Text field hiển thị ─────────────────────────────────── */
        txtDisplay = new TextField();
        txtDisplay.setPromptText("Chọn ngày sinh");
        txtDisplay.setEditable(false);
        txtDisplay.setCursor(Cursor.HAND);
        txtDisplay.setPrefHeight(40);
        HBox.setHgrow(txtDisplay, Priority.ALWAYS);
        txtDisplay.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 8 0 0 8;" +
                        "-fx-background-radius: 8 0 0 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 12 8 12;");
        txtDisplay.focusedProperty().addListener((obs, o, focused) -> {
            if (focused) {
                txtDisplay.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + C_BLUE + ";" +
                                "-fx-border-radius: 8 0 0 8;" +
                                "-fx-background-radius: 8 0 0 8;" +
                                "-fx-font-size: 13px;" +
                                "-fx-padding: 8 12 8 12;" +
                                "-fx-border-width: 2;");
            } else {
                txtDisplay.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + C_BORDER + ";" +
                                "-fx-border-radius: 8 0 0 8;" +
                                "-fx-background-radius: 8 0 0 8;" +
                                "-fx-font-size: 13px;" +
                                "-fx-padding: 8 12 8 12;");
            }
        });

        /* ── Nút mở popup ────────────────────────────────────────── */
        Button btnOpen = new Button("📅");
        btnOpen.setPrefSize(42, 40);
        btnOpen.setMinWidth(42);
        btnOpen.setCursor(Cursor.HAND);
        btnOpen.setStyle(
                "-fx-background-color: " + C_NAVY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 0 8 8 0;" +
                        "-fx-border-radius: 0 8 8 0;" +
                        "-fx-cursor: hand;");
        btnOpen.setOnMouseEntered(e -> btnOpen.setStyle(
                "-fx-background-color: " + C_BLUE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 0 8 8 0;" +
                        "-fx-border-radius: 0 8 8 0;" +
                        "-fx-cursor: hand;"));
        btnOpen.setOnMouseExited(e -> btnOpen.setStyle(
                "-fx-background-color: " + C_NAVY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 0 8 8 0;" +
                        "-fx-border-radius: 0 8 8 0;" +
                        "-fx-cursor: hand;"));

        getChildren().addAll(txtDisplay, btnOpen);

        /* ── Click → toggle popup ────────────────────────────────── */
        txtDisplay.setOnMouseClicked(e -> togglePopup());
        btnOpen.setOnAction(e -> togglePopup());
    }

    /**
     * Constructor mới nhận vào ngày mặc định ban đầu.
     */
    public DatePicker(LocalDate initialDate) {
        this();
        setValue(initialDate);
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * GET / SET VALUE
     * ══════════════════════════════════════════════════════════════════
     */
    public LocalDate getValue() {
        return value.get();
    }

    public void setValue(LocalDate date) {
        this.value.set(date);
        if (date != null) {
            txtDisplay.setText(date.format(FMT));
            viewMonth = YearMonth.from(date);
        } else {
            txtDisplay.setText("");
        }
    }

    public ObjectProperty<LocalDate> valueProperty() {
        return value;
    }

    public void setMinDate(LocalDate minDate) {
        this.minDate = minDate;
        refreshCalendar();
    }

    public void setPromptText(String prompt) {
        txtDisplay.setPromptText(prompt);
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * POPUP
     * ══════════════════════════════════════════════════════════════════
     */
    private void togglePopup() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
            return;
        }
        showPopup();
    }

    private void showPopup() {
        if (popup == null)
            popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().clear();
        popup.getContent().add(buildPopupContent());

        // Vị trí ngay dưới text field
        var bounds = localToScreen(getBoundsInLocal());
        if (bounds != null) {
            popup.show(getScene().getWindow(),
                    bounds.getMinX(), bounds.getMaxY() + 4);
        }
    }

    private VBox buildPopupContent() {
        VBox box = new VBox(0);
        box.setPrefWidth(320);
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);");

        box.getChildren().addAll(buildNavBar(), buildWeekHeader(), buildCalendarGrid());
        refreshCalendar();
        return box;
    }

    /* ── Nav bar: [<] [Tháng ▾] [Năm ▾] [>] ──────────────────────── */
    private HBox buildNavBar() {
        HBox nav = new HBox(8);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 14, 10, 14));
        nav.setStyle("-fx-background-color: " + C_NAVY + ";" +
                "-fx-background-radius: 12 12 0 0;");

        Button btnPrev = navArrow("◂");
        btnPrev.setOnAction(e -> {
            viewMonth = viewMonth.minusMonths(1);
            syncCombosFromView();
            refreshCalendar();
        });

        // ── ComboBox Tháng ──────────────────────────────────────
        cbMonth = new ComboBox<>();
        cbMonth.getItems().addAll(MONTH_NAMES);
        cbMonth.getSelectionModel().select(viewMonth.getMonthValue() - 1);
        cbMonth.setPrefHeight(32);
        cbMonth.setPrefWidth(105);
        cbMonth.setStyle(
                "-fx-font-size: 12px; -fx-font-family: 'Segoe UI';" +
                        "-fx-background-radius: 6; -fx-border-radius: 6;" +
                        "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-text-fill: white; -fx-prompt-text-fill: white;" +
                        "-fx-border-color: rgba(255,255,255,0.3);");
        // FIX: Đảm bảo text hiển thị bên trong ComboBox cũng màu trắng
        cbMonth.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Segoe UI", 12));
                }
            }
        });
        cbMonth.setOnAction(e -> {
            int m = cbMonth.getSelectionModel().getSelectedIndex() + 1;
            if (m > 0) {
                viewMonth = viewMonth.withMonth(m);
                refreshCalendar();
            }
        });

        // ── ComboBox Năm ────────────────────────────────────────
        cbYear = new ComboBox<>();
        for (int y = maxYear; y >= minYear; y--)
            cbYear.getItems().add(y);
        cbYear.setValue(viewMonth.getYear());
        cbYear.setPrefHeight(32);
        cbYear.setPrefWidth(85);
        cbYear.setStyle(
                "-fx-font-size: 12px; -fx-font-family: 'Segoe UI';" +
                        "-fx-background-radius: 6; -fx-border-radius: 6;" +
                        "-fx-background-color: rgba(255,255,255,0.15);" +
                        "-fx-text-fill: white; -fx-prompt-text-fill: white;" +
                        "-fx-border-color: rgba(255,255,255,0.3);");
        // FIX: Đảm bảo text hiển thị bên trong ComboBox cũng màu trắng
        cbYear.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Segoe UI", 12));
                }
            }
        });
        cbYear.setOnAction(e -> {
            Integer y = cbYear.getValue();
            if (y != null) {
                viewMonth = viewMonth.withYear(y);
                refreshCalendar();
            }
        });

        Button btnNext = navArrow("▸");
        btnNext.setOnAction(e -> {
            viewMonth = viewMonth.plusMonths(1);
            syncCombosFromView();
            refreshCalendar();
        });

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        nav.getChildren().addAll(btnPrev, sp1, cbMonth, cbYear, sp2, btnNext);
        return nav;
    }

    /* ── Header thứ: T2 T3 T4 T5 T6 T7 CN ─────────────────── */
    private GridPane buildWeekHeader() {
        GridPane g = new GridPane();
        g.setPadding(new Insets(8, 14, 4, 14));
        g.setHgap(0);
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(WEEK_DAYS[i]);
            lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            lbl.setTextFill(Color.web(i >= 5 ? "#dc2626" : "#6b7280"));
            lbl.setAlignment(Pos.CENTER);
            lbl.setPrefSize(320.0 / 7 - 4, 24);
            lbl.setMaxWidth(Double.MAX_VALUE);
            g.add(lbl, i, 0);
            GridPane.setHgrow(lbl, Priority.ALWAYS);
        }
        return g;
    }

    /* ── Lưới ngày 7×6 ───────────────────────────────────────────── */
    private GridPane buildCalendarGrid() {
        calendarGrid = new GridPane();
        calendarGrid.setPadding(new Insets(2, 14, 14, 14));
        calendarGrid.setHgap(0);
        calendarGrid.setVgap(2);
        return calendarGrid;
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * REFRESH CALENDAR GRID
     * ══════════════════════════════════════════════════════════════════
     */
    private void refreshCalendar() {
        if (calendarGrid == null)
            return;
        calendarGrid.getChildren().clear();

        LocalDate first = viewMonth.atDay(1);
        int dow = first.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        int offset = dow - 1; // số ô trống đầu
        int daysInMonth = viewMonth.lengthOfMonth();

        LocalDate today = LocalDate.now();
        LocalDate val = getValue();

        int row = 0;
        for (int i = 0; i < offset; i++) {
            calendarGrid.add(emptyCell(), i, row);
        }

        int col = offset;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = viewMonth.atDay(day);
            Button btn = dayButton(day, date, today, val);
            calendarGrid.add(btn, col, row);
            GridPane.setHgrow(btn, Priority.ALWAYS);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
        // Ô trống cuối
        while (col > 0 && col < 7) {
            calendarGrid.add(emptyCell(), col, row);
            col++;
        }
    }

    /*
     * ══════════════════════════════════════════════════════════════════
     * HELPERS
     * ══════════════════════════════════════════════════════════════════
     */
    private void syncCombosFromView() {
        cbMonth.getSelectionModel().select(viewMonth.getMonthValue() - 1);
        cbYear.setValue(viewMonth.getYear());
    }

    private Button dayButton(int day, LocalDate date, LocalDate today, LocalDate val) {
        Button btn = new Button(String.valueOf(day));
        btn.setFont(Font.font("Segoe UI", 12));
        btn.setPrefSize(320.0 / 7 - 4, 34);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(Cursor.HAND);

        boolean isToday = date.equals(today);
        boolean isSelected = date.equals(val);
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean isDisabled = minDate != null && date.isBefore(minDate);

        String bg = "transparent";
        String fg = isWeekend ? "#dc2626" : "#1f2937";
        String radius = "50";

        if (isSelected) {
            bg = C_SELECT_BG;
            fg = "white";
        } else if (isToday) {
            bg = C_TODAY_BG;
            fg = C_BLUE;
        }

        if (isDisabled) {
            fg = "#d1d5db"; // màu xám nhạt
            btn.setDisable(true);
        }

        String style = "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-background-radius: " + radius + ";" +
                "-fx-border-radius: " + radius + ";" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: " + (isDisabled ? "default" : "hand") + ";";
        btn.setStyle(style);

        if (!isDisabled) {
            String hoverStyle = style.replace(
                    "-fx-background-color: " + bg,
                    "-fx-background-color: " + (isSelected ? C_SELECT_BG : "#e0e7ff"));

            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(style));

            btn.setOnAction(e -> {
                setValue(date);
                if (popup != null)
                    popup.hide();
            });
        }

        return btn;
    }

    private Region emptyCell() {
        Region r = new Region();
        r.setPrefSize(320.0 / 7 - 4, 34);
        return r;
    }

    private Button navArrow(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btn.setPrefSize(32, 32);
        btn.setCursor(Cursor.HAND);
        String normal = "-fx-background-color: rgba(255,255,255,0.1);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 50;" +
                "-fx-cursor: hand;";
        String hover = "-fx-background-color: rgba(255,255,255,0.25);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 50;" +
                "-fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }
}