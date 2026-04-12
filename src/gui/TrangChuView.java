package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Window;

import dao.PhongDAO;
import model.entities.Phong;
import model.enums.TrangThaiPhong; // FIX: import enum để so sánh đúng kiểu
import model.entities.LoaiPhong;

import java.util.*;

/**
 * TrangChuView – JavaFX Dashboard
 * 4 thẻ thống kê + sơ đồ phòng chia theo tầng.
 */
public class TrangChuView extends BorderPane {

    /* ── Màu giao diện ───────────────────────────────────────────────── */
    private static final String C_BG        = "#f8f9fa";
    private static final String C_CARD_BG   = "white";
    private static final String C_BORDER    = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";
    private static final String C_NAVY      = "#1e3a8a";
    private static final String C_RED       = "#dc2626";
    private static final String C_GREEN     = "#16a34a";
    private static final String C_GOLD      = "#d97706";

    /*
     * FIX: màu phòng khớp với label thực trong enum TrangThaiPhong:
     *   CONTRONG("Trống")  → xanh lá
     *   DACOKHACH("Đang ở") → vàng cam
     *   BAN("Bận")          → đỏ
     */
    private static final Color COLOR_CONTRONG  = Color.web("#22c55e");
    private static final Color COLOR_DACOKHACH = Color.web("#f59e0b");
    private static final Color COLOR_BAN       = Color.web("#ef4444");
    private static final Color COLOR_DEFAULT   = Color.web("#9ca3af");

    private final PhongDAO phongDAO = new PhongDAO();
    private Label lblTotal, lblOccupied, lblAvailable, lblRevenue;

    /* ── Constructor ─────────────────────────────────────────────────── */
    public TrangChuView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(32));

        // Thống kê trên cùng
        HBox statsRow = buildStatsRow();
        setTop(statsRow);
        BorderPane.setMargin(statsRow, new Insets(0, 0, 24, 0));

        // Card sơ đồ phòng
        VBox center = new VBox(0);
        center.setStyle(
            "-fx-background-color: " + C_CARD_BG + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );

        center.getChildren().add(buildSectionHeader());

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: " + C_BORDER + ";");
        center.getChildren().add(divider);

        ScrollPane scroll = new ScrollPane(buildRoomGrid());
        scroll.setBorder(Border.EMPTY);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.getContent().setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        center.getChildren().add(scroll);

        setCenter(center);
        loadStats();
    }

    /* ════════════════════════════════════════════════════════════════════
       THỐNG KÊ
    ════════════════════════════════════════════════════════════════════ */
    private HBox buildStatsRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        String[][] defs = {
            { "🛏", "TỔNG PHÒNG",     "--", C_NAVY  },
            { "👥", "ĐÃ CÓ KHÁCH",    "--", C_RED   },
            { "🏠", "PHÒNG TRỐNG",    "--", C_GREEN },
            { "💰", "DOANH THU (VND)", "--", C_GOLD  },
        };

        Label[] vals = new Label[4];
        for (int i = 0; i < 4; i++) {
            Object[] result = createStatCard(defs[i][0], defs[i][1], defs[i][2], defs[i][3]);
            VBox card = (VBox) result[0];
            vals[i]   = (Label) result[1];
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }

        lblTotal     = vals[0];
        lblOccupied  = vals[1];
        lblAvailable = vals[2];
        lblRevenue   = vals[3]; // doanh thu cần HoaDonDAO, giữ "--"

        return row;
    }

    /** @return Object[]{ VBox card, Label valueLbl } */
    private Object[] createStatCard(String icon, String title, String value, String accentHex) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(22));
        card.setStyle(
            "-fx-background-color: " + C_CARD_BG + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
        card.setEffect(new DropShadow(8, 0, 2, Color.web("#00000018")));

        /* Hàng trên: text + badge icon */
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTitle.setTextFill(Color.web(C_TEXT_GRAY));
        lblTitle.setTextOverrun(OverrunStyle.CLIP);

        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblVal.setTextFill(Color.web(C_TEXT_DARK));

        textBox.getChildren().addAll(lblTitle, lblVal);

        StackPane badge = new StackPane();
        badge.setMinSize(46, 46);
        badge.setPrefSize(46, 46);
        Rectangle badgeBg = new Rectangle(46, 46);
        badgeBg.setArcWidth(10); badgeBg.setArcHeight(10);
        Color accent = Color.web(accentHex);
        badgeBg.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.12));
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("Segoe UI Emoji", 22));
        badge.getChildren().addAll(badgeBg, iconLbl);

        topRow.getChildren().addAll(textBox, badge);
        card.getChildren().add(topRow);

        /* Accent bar */
        Region bar = new Region();
        bar.setPrefHeight(3);
        bar.setStyle("-fx-background-color: " + accentHex + "; -fx-background-radius: 2;");
        card.getChildren().add(bar);

        return new Object[]{ card, lblVal };
    }

    private void loadStats() {
        try {
            List<Phong> all = phongDAO.getAll();
            long total = all.size();

            /*
             * FIX: so sánh trực tiếp với giá trị enum TrangThaiPhong,
             * không dùng .equals(String) vì getTrangThai() trả về enum.
             */
            long occupied  = all.stream()
                .filter(p -> p.getTrangThai() == TrangThaiPhong.DACOKHACH).count();
            long available = all.stream()
                .filter(p -> p.getTrangThai() == TrangThaiPhong.CONTRONG).count();

            lblTotal.setText(String.valueOf(total));
            lblOccupied.setText(String.valueOf(occupied));
            lblAvailable.setText(String.valueOf(available));
            lblRevenue.setText("--"); // cần HoaDonDAO để tính
        } catch (Exception ignored) {}
    }

    /* ════════════════════════════════════════════════════════════════════
       SƠ ĐỒ PHÒNG
    ════════════════════════════════════════════════════════════════════ */
    private HBox buildSectionHeader() {
        HBox h = new HBox();
        h.setPadding(new Insets(18, 22, 14, 22));
        h.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Sơ đồ phòng");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(C_TEXT_DARK));
        HBox.setHgrow(title, Priority.ALWAYS);

        /* FIX: chú thích dùng label thực của enum ("Bận", không phải "Bẩn") */
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER_RIGHT);
        legend.getChildren().addAll(
            legendDot(COLOR_CONTRONG,  TrangThaiPhong.CONTRONG.getLabel()),   // "Trống"
            legendDot(COLOR_DACOKHACH, TrangThaiPhong.DACOKHACH.getLabel()),  // "Đang ở"
            legendDot(COLOR_BAN,       TrangThaiPhong.BAN.getLabel())         // "Bận"
        );

        h.getChildren().addAll(title, legend);
        return h;
    }

    private HBox legendDot(Color color, String label) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER);
        Rectangle dot = new Rectangle(12, 12);
        dot.setArcWidth(4); dot.setArcHeight(4);
        dot.setFill(color);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web(C_TEXT_GRAY));
        item.getChildren().addAll(dot, lbl);
        return item;
    }

    private VBox buildRoomGrid() {
        VBox grid = new VBox(0);
        grid.setStyle("-fx-background-color: " + C_CARD_BG + ";");

        try {
            List<Phong> all = phongDAO.getAll();
            Map<Integer, List<Phong>> byFloor = new TreeMap<>();
            for (Phong p : all) {
                int floor = 0;
                try { floor = p.getSoTang(); } catch (Exception e) {}
                byFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<Integer, List<Phong>> entry : byFloor.entrySet()) {
                grid.getChildren().add(buildFloorSection(entry.getKey(), entry.getValue()));
                Region sep = new Region();
                sep.setPrefHeight(1);
                sep.setStyle("-fx-background-color: " + C_BORDER + ";");
                grid.getChildren().add(sep);
            }
        } catch (Exception ignored) {
            Label err = new Label("Không thể tải dữ liệu phòng.");
            err.setPadding(new Insets(20));
            err.setTextFill(Color.web(C_TEXT_GRAY));
            grid.getChildren().add(err);
        }

        return grid;
    }

    private VBox buildFloorSection(int floorNum, List<Phong> rooms) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(16, 22, 16, 22));

        Label floorLbl = new Label("TẦNG " + floorNum);
        floorLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        floorLbl.setTextFill(Color.web(C_TEXT_GRAY));

        FlowPane flow = new FlowPane(14, 14);
        flow.setPrefWrapLength(Double.MAX_VALUE);
        for (Phong p : rooms)
            flow.getChildren().add(buildRoomCard(p));

        section.getChildren().addAll(floorLbl, flow);
        return section;
    }

    private StackPane buildRoomCard(Phong phong) {
        /*
         * FIX 1: getTrangThai() trả về TrangThaiPhong enum, không phải String.
         *         Gọi .toString() để lấy label hiển thị ("Trống", "Đang ở", "Bận").
         */
        TrangThaiPhong trangThai = phong.getTrangThai();
        String status = (trangThai != null) ? trangThai.toString() : "Không rõ";
        String maPhong = phong.getMaPhong();
        LoaiPhong loaiPhong = phong.getLoaiPhong();
        
        String loaiStr = (loaiPhong != null && loaiPhong.getTenLoai() != null)
        		? loaiPhong.getTenLoai() // "DOUBLE", "SINGLE"
        		: "--";
        String priceStr = (loaiPhong != null && loaiPhong.getGiaPerNgay() > 0)
        		? String.format("%,.0f đ", loaiPhong.getGiaPerNgay())
        		: "";

        /* FIX 2: switch trên enum thay vì String để tránh nhầm label */
        Color colorTop, colorBottom;
        if (trangThai == TrangThaiPhong.CONTRONG) {
            colorTop = Color.web("#22c55e"); colorBottom = Color.web("#16a34a");
        } else if (trangThai == TrangThaiPhong.DACOKHACH) {
            colorTop = Color.web("#f59e0b"); colorBottom = Color.web("#d97706");
        } else if (trangThai == TrangThaiPhong.BAN) {
            colorTop = Color.web("#ef4444"); colorBottom = Color.web("#dc2626");
        } else {
            colorTop = Color.web("#9ca3af"); colorBottom = Color.web("#6b7280");
        }

        StackPane card = new StackPane();
        card.setPrefSize(150, 96);
        card.setMinSize(150, 96);
        card.setMaxSize(150, 96);
        card.setCursor(Cursor.HAND);

        Region bg = new Region();
        bg.setPrefSize(147, 93);
        bg.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s);" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 6, 0, 2, 2);",
            toHex(colorTop), toHex(colorBottom)
        ));

        /* Nội dung card */
        VBox content = new VBox(3);
        content.setPadding(new Insets(10, 14, 10, 14));
        content.setAlignment(Pos.TOP_LEFT);
        content.setPickOnBounds(false);
        // Mã phòng
        Label lblName = new Label(maPhong);
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblName.setTextFill(Color.WHITE);
        // Loại phòng
        Label lblType = new Label(loaiStr);
        lblType.setFont(Font.font("Segoe UI", 10));
        lblType.setTextFill(Color.rgb(255, 255, 255, 0.80));
        // Trạng thái
        Label lblStatus = new Label(status);
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblStatus.setTextFill(Color.WHITE);
        // Giá phòng
        Label lblPrice = new Label(priceStr);
        lblPrice.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblPrice.setTextFill(Color.WHITE);
        
        content.getChildren().addAll(lblName, lblType,lblPrice, lblStatus);
        card.getChildren().addAll(bg, content);
        
        /* Hiệu ứng hover: scale nhẹ toàn card */
        card.setOnMouseEntered(e -> card.setScaleX(1.04));
        card.setOnMouseExited(e  -> card.setScaleX(1.0));

        /*
         * FIX 5: ChiTietPhongDialog (JavaFX) thay thế ChiTietPhongFrame (Swing).
         *         Truyền loaiPhong dạng String để Dialog không phụ thuộc enum.
         */
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Window owner = getScene() != null ? getScene().getWindow() : null;
                new ChiTietPhongDialog(
                    owner,
                    maPhong,
                    loaiStr,
                    priceStr,
                    "Tầng " + phong.getSoTang(),
                    status
                ).show();
            }
        });

        return card;
    }

    /* ── Utility ─────────────────────────────────────────────────────── */
    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
            (int)(c.getRed()   * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue()  * 255));
    }
}