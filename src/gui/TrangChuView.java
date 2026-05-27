package gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Window;
import javafx.util.Duration;

import dao.PhongDAO;
import model.entities.Phong;
import model.enums.TrangThaiPhong;
import model.entities.LoaiPhong;

import java.io.InputStream;
import java.util.*;
import gui.interfaces.IRefreshable;

/**
 * TrangChuView – JavaFX Dashboard
 * Chứa Banner ảnh phòng (hỗ trợ kéo thả/nút bấm) và sơ đồ phòng chia theo tầng.
 */
public class TrangChuView extends BorderPane implements IRefreshable {

    /* ── Màu giao diện ───────────────────────────────────────────────── */
    private static final String C_BG = "#f8f9fa";
    private static final String C_CARD_BG = "white";
    private static final String C_BORDER = "#e9ecef";
    private static final String C_TEXT_DARK = "#111827";
    private static final String C_TEXT_GRAY = "#6b7280";

    /*
     * Màu phòng khớp với label thực trong enum TrangThaiPhong:
     */
    private static final Color COLOR_CONTRONG = Color.web("#22c55e");
    private static final Color COLOR_DACOKHACH = Color.web("#f59e0b");
    private static final Color COLOR_BAN = Color.web("#ef4444");
    private static final Color COLOR_DEFAULT = Color.web("#9ca3af");

    private final PhongDAO phongDAO = new PhongDAO();

    /* ── Banner carousel ─────────────────────────────────────────────── */
    private final List<BannerItem> bannerItems = new ArrayList<>();
    private ImageView bannerImageView;
    private Label bannerTitle;
    private Label bannerSubtitle;
    private HBox bannerDots;
    private Timeline bannerTimeline;
    private int currentBannerIndex = 0;
    
    // Biến lưu vị trí chuột để vuốt (swipe) và click vùng trái/phải
    private static final double SIDE_CLICK_ZONE = 130;
    private static final double SWIPE_THRESHOLD = 50;
    private double dragStartX;
    private double dragStartLocalX;
    
    private ScrollPane scrollRoomGrid;

    /* ── Constructor ─────────────────────────────────────────────────── */
    public TrangChuView() {
        setStyle("-fx-background-color: " + C_BG + ";");
        setPadding(new Insets(26, 32, 32, 32));

        // Banner trên cùng
        StackPane heroBanner = buildHeroBanner();
        setTop(heroBanner);
        BorderPane.setMargin(heroBanner, new Insets(0, 0, 24, 0));

        // Card sơ đồ phòng 
        VBox center = new VBox(0);
        center.setStyle(
                "-fx-background-color: " + C_CARD_BG + ";" +
                        "-fx-border-color: " + C_BORDER + ";" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;");
        center.setEffect(new DropShadow(10, 0, 3, Color.web("#00000012")));

        center.getChildren().add(buildSectionHeader());

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: " + C_BORDER + ";");
        center.getChildren().add(divider);

        scrollRoomGrid = new ScrollPane(buildRoomGrid());
        scrollRoomGrid.setBorder(Border.EMPTY);
        scrollRoomGrid.setFitToWidth(true);
        scrollRoomGrid.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollRoomGrid.getContent().setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollRoomGrid, Priority.ALWAYS);
        center.getChildren().add(scrollRoomGrid);

        setCenter(center);
        startBannerAutoPlay();
    }

    @Override
    public void autoRefresh() {
        if (scrollRoomGrid != null) {
            double vvalue = scrollRoomGrid.getVvalue();
            scrollRoomGrid.setContent(buildRoomGrid());
            scrollRoomGrid.setVvalue(vvalue); // Cố gắng giữ nguyên cuộn
        }
    }

    /*
     * ════════════════════════════════════════════════════════════════════
     * BANNER ẢNH PHÒNG
     * ════════════════════════════════════════════════════════════════════
     */
    private StackPane buildHeroBanner() {
        loadBannerItems();

        StackPane banner = new StackPane();
        banner.setMinHeight(380);
        banner.setPrefHeight(380);
        banner.setStyle(
                "-fx-background-color: linear-gradient(to right, #111827, #1e3a8a);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;");
        banner.setEffect(new DropShadow(12, 0, 4, Color.web("#00000020")));

        Rectangle clip = new Rectangle();
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        clip.widthProperty().bind(banner.widthProperty());
        clip.heightProperty().bind(banner.heightProperty());
        banner.setClip(clip);

        bannerImageView = new ImageView();
        bannerImageView.setPreserveRatio(false);
        bannerImageView.setSmooth(true);
        bannerImageView.setMouseTransparent(true);
        bannerImageView.fitWidthProperty().bind(banner.widthProperty());
        bannerImageView.fitHeightProperty().bind(banner.heightProperty());

        Region overlay = new Region();
        overlay.setStyle(
                "-fx-background-color: linear-gradient(to right, " +
                        "rgba(15, 23, 42, 0.88), " +
                        "rgba(30, 58, 138, 0.48), " +
                        "rgba(15, 23, 42, 0.10));");
        overlay.setMouseTransparent(true);
        overlay.prefWidthProperty().bind(banner.widthProperty());
        overlay.prefHeightProperty().bind(banner.heightProperty());

        VBox textBox = new VBox(10);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setMaxWidth(680);
        textBox.setMouseTransparent(true);

        Label pill = new Label("LUCIA HOTEL · ROOM GALLERY");
        pill.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        pill.setTextFill(Color.WHITE);
        pill.setPadding(new Insets(6, 12, 6, 12));
        pill.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.18);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.26);" +
                        "-fx-border-radius: 999;");

        bannerTitle = new Label("Lucia Hotel");
        bannerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 31));
        bannerTitle.setTextFill(Color.WHITE);

        bannerSubtitle = new Label("Không gian nghỉ dưỡng hiện đại, sạch sẽ và phù hợp cho từng nhu cầu đặt phòng.");
        bannerSubtitle.setFont(Font.font("Segoe UI", 14));
        bannerSubtitle.setTextFill(Color.rgb(255, 255, 255, 0.90));
        bannerSubtitle.setWrapText(true);

        textBox.getChildren().addAll(pill, bannerTitle, bannerSubtitle);

        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(34, 80, 34, 80));
        content.setMouseTransparent(true);
        content.getChildren().add(textBox);

        // Nút trái/phải chỉ để hiển thị.
        // Sự kiện click được xử lý trực tiếp trên banner theo tọa độ X,
        // nên không còn phụ thuộc vào việc JavaFX có pick được node nút hay không.
        StackPane btnPrev = createBannerClickZone("‹");
        StackPane btnNext = createBannerClickZone("›");

        StackPane.setAlignment(btnPrev, Pos.CENTER_LEFT);
        StackPane.setMargin(btnPrev, new Insets(0, 0, 0, 18));
        
        StackPane.setAlignment(btnNext, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnNext, new Insets(0, 18, 0, 0));

        bannerDots = new HBox(8);
        bannerDots.setAlignment(Pos.CENTER);
        StackPane.setAlignment(bannerDots, Pos.BOTTOM_CENTER);
        StackPane.setMargin(bannerDots, new Insets(0, 0, 18, 0));

        banner.getChildren().addAll(bannerImageView, overlay, content, btnPrev, btnNext, bannerDots);
        
        btnPrev.toFront();
        btnNext.toFront();
        bannerDots.toFront();

        banner.setOnMouseEntered(e -> {
            if (bannerTimeline != null) bannerTimeline.pause();
        });
        banner.setOnMouseExited(e -> {
            if (bannerTimeline != null) bannerTimeline.play();
        });

        // LOGIC CHẮC CHẮN CHẠY:
        // 1) Kéo qua trái/phải: đổi banner.
        // 2) Click vùng 130px bên trái/phải của banner: đổi banner.
        // Vì bắt sự kiện trực tiếp trên banner cha, click vào icon tròn 2 bên cũng sẽ chạy.
        banner.setOnMouseMoved(e -> {
            double x = e.getX();
            if (x <= SIDE_CLICK_ZONE || x >= banner.getWidth() - SIDE_CLICK_ZONE) {
                banner.setCursor(Cursor.HAND);
            } else {
                banner.setCursor(Cursor.DEFAULT);
            }
        });

        banner.setOnMousePressed(e -> {
            dragStartX = e.getSceneX();
            dragStartLocalX = e.getX();
            banner.setCursor(Cursor.CLOSED_HAND);
        });

        banner.setOnMouseReleased(e -> {
            double deltaX = e.getSceneX() - dragStartX;
            double releaseX = e.getX();

            if (Math.abs(deltaX) >= SWIPE_THRESHOLD) {
                if (deltaX > 0) {
                    showPreviousBanner(true);
                } else {
                    showNextBanner(true);
                }
                restartBannerAutoPlay();
            } else {
                // Không kéo đủ xa thì coi là click.
                // Nếu click vào vùng trái/phải, chuyển banner luôn.
                double clickX = (dragStartLocalX + releaseX) / 2.0;
                if (clickX <= SIDE_CLICK_ZONE) {
                    showPreviousBanner(true);
                    restartBannerAutoPlay();
                } else if (clickX >= banner.getWidth() - SIDE_CLICK_ZONE) {
                    showNextBanner(true);
                    restartBannerAutoPlay();
                }
            }

            if (releaseX <= SIDE_CLICK_ZONE || releaseX >= banner.getWidth() - SIDE_CLICK_ZONE) {
                banner.setCursor(Cursor.HAND);
            } else {
                banner.setCursor(Cursor.DEFAULT);
            }
        });

        showBanner(0, false);
        return banner;
    }

    private StackPane createBannerClickZone(String text) {
        StackPane zone = new StackPane();
        zone.setMaxSize(44, 44);
        zone.setPrefSize(44, 44);

        // Quan trọng: để mouseTransparent = true.
        // Như vậy node nút không tự bắt chuột nữa, toàn bộ click sẽ đi về banner cha.
        // Banner cha sẽ dựa vào tọa độ X để biết người dùng bấm nút trái hay nút phải.
        zone.setMouseTransparent(true);

        Circle background = new Circle(22);
        background.setFill(Color.rgb(255, 255, 255, 0.24));
        background.setStroke(Color.rgb(255, 255, 255, 0.48));
        background.setStrokeWidth(1.1);
        background.setMouseTransparent(true);

        Label icon = new Label(text);
        icon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        icon.setTextFill(Color.WHITE);
        icon.setTranslateY(-2);
        icon.setMouseTransparent(true);

        zone.getChildren().addAll(background, icon);
        return zone;
    }

    private void loadBannerItems() {
        if (!bannerItems.isEmpty()) return;

        addBannerItem(
                "Single Room",
                "Phòng đơn gọn gàng, đầy đủ tiện nghi cho khách đi công tác hoặc nghỉ ngắn ngày.",
                "/icon/Single_room.jpg");
        addBannerItem(
                "Double Room",
                "Không gian rộng rãi, hiện đại, phù hợp cho cặp đôi hoặc khách muốn nghỉ dưỡng thoải mái.",
                "/icon/Double_room.jpg");
        addBannerItem(
                "Double Bed Room",
                "Thiết kế hai giường linh hoạt, phù hợp cho bạn bè, đồng nghiệp hoặc nhóm khách nhỏ.",
                "/icon/Double_bed_room.jpg");
        addBannerItem(
                "Family Room",
                "Không gian lớn với khu sinh hoạt riêng, phù hợp cho gia đình và nhóm khách dài ngày.",
                "/icon/Family_room.jpg");
        addBannerItem(
                "Triple Room",
                "Phòng ba giường thoải mái, tối ưu cho nhóm khách cần nhiều chỗ nghỉ.",
                "/icon/Tripple_room.jpg");
    }

    private void addBannerItem(String title, String subtitle, String imagePath) {
        Image image = loadImage(imagePath);
        if (image != null && !image.isError()) {
            bannerItems.add(new BannerItem(title, subtitle, image));
        }
    }

    private Image loadImage(String path) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) return null;
            return new Image(stream);
        } catch (Exception e) {
            return null;
        }
    }

    private void showPreviousBanner(boolean animate) {
        if (bannerItems.isEmpty()) return;
        int nextIndex = (currentBannerIndex - 1 + bannerItems.size()) % bannerItems.size();
        showBanner(nextIndex, animate);
    }

    private void showNextBanner(boolean animate) {
        if (bannerItems.isEmpty()) return;
        int nextIndex = (currentBannerIndex + 1) % bannerItems.size();
        showBanner(nextIndex, animate);
    }

    private void showBanner(int index, boolean animate) {
        if (bannerItems.isEmpty()) {
            bannerTitle.setText("Lucia Hotel");
            bannerSubtitle.setText("Chưa tìm thấy ảnh trong thư mục /icon. Hãy kiểm tra lại tên file ảnh phòng.");
            return;
        }

        if (index < 0 || index >= bannerItems.size()) index = 0;

        currentBannerIndex = index;
        BannerItem item = bannerItems.get(currentBannerIndex);

        if (!animate) {
            bannerImageView.setImage(item.getImage());
            bannerTitle.setText(item.getTitle());
            bannerSubtitle.setText(item.getSubtitle());
            updateBannerDots();
            return;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), bannerImageView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.35);
        fadeOut.setOnFinished(e -> {
            bannerImageView.setImage(item.getImage());
            bannerTitle.setText(item.getTitle());
            bannerSubtitle.setText(item.getSubtitle());
            updateBannerDots();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(260), bannerImageView);
            fadeIn.setFromValue(0.35);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void updateBannerDots() {
        if (bannerDots == null) return;

        bannerDots.getChildren().clear();
        for (int i = 0; i < bannerItems.size(); i++) {
            final int targetIndex = i;
            Circle dot = new Circle(i == currentBannerIndex ? 5 : 4);
            dot.setCursor(Cursor.HAND);
            dot.setFill(i == currentBannerIndex ? Color.WHITE : Color.rgb(255, 255, 255, 0.42));
            dot.setStroke(Color.rgb(255, 255, 255, 0.65));
            dot.setStrokeWidth(i == currentBannerIndex ? 1.2 : 0);
            dot.setOnMouseClicked(e -> {
                e.consume();
                showBanner(targetIndex, true);
                restartBannerAutoPlay();
            });
            bannerDots.getChildren().add(dot);
        }
    }

    private void startBannerAutoPlay() {
        if (bannerItems.size() <= 1) return;
        bannerTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> showNextBanner(true)));
        bannerTimeline.setCycleCount(Animation.INDEFINITE);
        bannerTimeline.play();
    }

    private void restartBannerAutoPlay() {
        if (bannerTimeline != null) {
            bannerTimeline.stop();
            bannerTimeline.playFromStart();
        }
    }

    private static class BannerItem {
        private final String title;
        private final String subtitle;
        private final Image image;

        BannerItem(String title, String subtitle, Image image) {
            this.title = title;
            this.subtitle = subtitle;
            this.image = image;
        }

        String getTitle() { return title; }
        String getSubtitle() { return subtitle; }
        Image getImage() { return image; }
    }

    /*
     * ════════════════════════════════════════════════════════════════════
     * SƠ ĐỒ PHÒNG
     * ════════════════════════════════════════════════════════════════════
     */
    private HBox buildSectionHeader() {
        HBox h = new HBox();
        h.setPadding(new Insets(18, 22, 14, 22));
        h.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Sơ đồ phòng");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(C_TEXT_DARK));
        
        // TẠO LÒ XO (SPACER) ĐỂ ĐẨY CHÚ THÍCH SANG PHẢI, TÁCH RỜI KHỎI TITLE
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox legend = new HBox(36); 
        legend.setAlignment(Pos.CENTER_RIGHT);
        legend.getChildren().addAll(
                legendDot(COLOR_CONTRONG, TrangThaiPhong.CONTRONG.getLabel()),
                legendDot(COLOR_DACOKHACH, TrangThaiPhong.DACOKHACH.getLabel()),
                legendDot(COLOR_BAN, TrangThaiPhong.BAN.getLabel())
        );

        h.getChildren().addAll(title, spacer, legend);
        return h;
    }

    private HBox legendDot(Color color, String label) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER);
        
        Rectangle dot = new Rectangle(14, 14); 
        dot.setArcWidth(4);
        dot.setArcHeight(4);
        dot.setFill(color);
        
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 13));
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
                try {
                    floor = p.getSoTang();
                } catch (Exception e) {}
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
        for (Phong p : rooms) {
            flow.getChildren().add(buildRoomCard(p));
        }

        section.getChildren().addAll(floorLbl, flow);
        return section;
    }

    private StackPane buildRoomCard(Phong phong) {
        TrangThaiPhong trangThai = phong.getTrangThai();
        String status = (trangThai != null) ? trangThai.toString() : "Không rõ";
        String maPhong = phong.getMaPhong();
        LoaiPhong loaiPhong = phong.getLoaiPhong();

        String loaiStr = (loaiPhong != null && loaiPhong.getTenLoai() != null)
                ? loaiPhong.getTenLoai() : "--";
        String priceStr = (loaiPhong != null && loaiPhong.getGiaPerNgay() > 0)
                ? String.format("%,.0f đ", loaiPhong.getGiaPerNgay()) : "";

        Color colorTop, colorBottom;
        if (trangThai == TrangThaiPhong.CONTRONG) {
            colorTop = Color.web("#22c55e");
            colorBottom = Color.web("#16a34a");
        } else if (trangThai == TrangThaiPhong.DACOKHACH) {
            colorTop = Color.web("#f59e0b");
            colorBottom = Color.web("#d97706");
        } else if (trangThai == TrangThaiPhong.BAN) {
            colorTop = Color.web("#ef4444");
            colorBottom = Color.web("#dc2626");
        } else {
            colorTop = COLOR_DEFAULT;
            colorBottom = Color.web("#6b7280");
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
                toHex(colorTop), toHex(colorBottom)));

        VBox content = new VBox(3);
        content.setPadding(new Insets(10, 14, 10, 14));
        content.setAlignment(Pos.TOP_LEFT);
        content.setPickOnBounds(false);

        Label lblName = new Label(maPhong);
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblName.setTextFill(Color.WHITE);

        Label lblType = new Label(loaiStr);
        lblType.setFont(Font.font("Segoe UI", 10));
        lblType.setTextFill(Color.rgb(255, 255, 255, 0.80));

        Label lblStatus = new Label(status);
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblStatus.setTextFill(Color.WHITE);

        Label lblPrice = new Label(priceStr);
        lblPrice.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblPrice.setTextFill(Color.WHITE);

        content.getChildren().addAll(lblName, lblType, lblPrice, lblStatus);
        card.getChildren().addAll(bg, content);

        card.setOnMouseEntered(e -> {
            card.setScaleX(1.04);
            card.setScaleY(1.04);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Window owner = getScene() != null ? getScene().getWindow() : null;
                new ChiTietPhongDialog(
                        owner, maPhong, loaiStr, priceStr, "Tầng " + phong.getSoTang(), status).show();
            }
        });

        return card;
    }

    /* ── Utility ─────────────────────────────────────────────────────── */
    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }
}