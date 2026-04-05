package gui;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * DimOverlay – Tiện ích làm tối nền khi mở dialog.
 *
 * Cách dùng:
 *   Region overlay = DimOverlay.show(ownerWindow);
 *   dialog.showAndWait();
 *   DimOverlay.hide(ownerWindow, overlay);
 */
public final class DimOverlay {

    private static final String WRAPPER_ID = "__dim_overlay_wrapper__";

    private DimOverlay() {} // không cho tạo instance

    /**
     * Thêm lớp phủ tối lên cửa sổ chủ.
     * @return Region overlay (giữ lại để truyền vào {@link #hide})
     */
    public static Region show(Window owner) {
        if (owner == null || owner.getScene() == null) return null;

        Parent root = owner.getScene().getRoot();
        StackPane wrapper = ensureWrapper(owner, root);

        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        overlay.setMouseTransparent(false);
        wrapper.getChildren().add(overlay);
        
        // Hiệu ứng chuyển mượt
        FadeTransition ft = new FadeTransition(Duration.millis(250), overlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        
        return overlay;
    }

    /**
     * Gỡ lớp phủ tối khỏi cửa sổ chủ.
     */
    public static void hide(Window owner, Region overlay) {
        if (owner == null || overlay == null || owner.getScene() == null) return;
        Parent root = owner.getScene().getRoot();

        if (root instanceof StackPane wrapper && WRAPPER_ID.equals(wrapper.getId())) {
            FadeTransition ft = new FadeTransition(Duration.millis(150), overlay);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
            	wrapper.getChildren().remove(overlay);
        	
            // Nếu chỉ còn root gốc → bỏ wrapper, trả root gốc về scene
            if (wrapper.getChildren().size() == 1) {
                Parent original = (Parent) wrapper.getChildren().get(0);
                wrapper.getChildren().clear();
                owner.getScene().setRoot(original);
            }
        });
            ft.play();
      }
    }

    /* ── Đảm bảo scene root được bọc trong StackPane ────────────── */
    private static StackPane ensureWrapper(Window owner, Parent root) {
        // Đã bọc rồi → dùng lại
        if (root instanceof StackPane sp && WRAPPER_ID.equals(sp.getId())) {
            return sp;
        }

        // Bọc root gốc vào StackPane
        StackPane wrapper = new StackPane();
        wrapper.setId(WRAPPER_ID);
        owner.getScene().setRoot(wrapper);
        wrapper.getChildren().add(root);
        return wrapper;
    }
}