package model.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * EventUtils – Tiện ích sự kiện dùng chung cho các Dialog (JavaFX).
 */
public final class EventUtils {

    private EventUtils() {} // không tạo instance

    /* ══════════════════════════════════════════════════════════════════
       1. ENTER → NEXT FIELD (field cuối cùng → gọi onSubmit)
    ══════════════════════════════════════════════════════════════════ */

    /**
     * Gắn sự kiện Enter cho danh sách control theo thứ tự tab.
     * Khi nhấn Enter ở field bất kỳ → focus chuyển sang field tiếp theo.
     * Khi nhấn Enter ở field cuối cùng → gọi {@code onSubmit}.
     */
    public static void setupEnterNavigation(Runnable onSubmit, Control... fields) {
        for (int i = 0; i < fields.length; i++) {
            final int next = i + 1;
            Control current = fields[i];

            current.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    e.consume();
                    if (next < fields.length) {
                        // Focus field tiếp theo
                        Control nextField = fields[next];
                        Platform.runLater(nextField::requestFocus);
                    } else {
                        // Field cuối → submit
                        if (onSubmit != null) onSubmit.run();
                    }
                }
            });
        }
    }

    /* ══════════════════════════════════════════════════════════════════
       2. FOCUS VÀO Ô LỖI ĐẦU TIÊN (CAO NHẤT)
    ══════════════════════════════════════════════════════════════════ */

    public static boolean focusFirstError(Control[] fields, Label[] errorLabels) {
        if (fields == null || errorLabels == null) return false;

        int len = Math.min(fields.length, errorLabels.length);
        for (int i = 0; i < len; i++) {
            String errText = errorLabels[i].getText();
            if (errText != null && !errText.isBlank()) {
                final Control target = fields[i];
                Platform.runLater(target::requestFocus);
                return true;
            }
        }
        return false;
    }

    public static boolean focusFirstError(Node[] fields, Label[] errorLabels) {
        if (fields == null || errorLabels == null) return false;

        int len = Math.min(fields.length, errorLabels.length);
        for (int i = 0; i < len; i++) {
            String errText = errorLabels[i].getText();
            if (errText != null && !errText.isBlank()) {
                final Node target = fields[i];
                Platform.runLater(target::requestFocus);
                return true;
            }
        }
        return false;
    }

    /* ══════════════════════════════════════════════════════════════════
    3. DIRTY TRACKING THÔNG MINH (Tự động Disable nút nếu chưa đổi)
 ══════════════════════════════════════════════════════════════════ */

    /**
     * Tự động theo dõi thay đổi và Disable nút nếu dữ liệu giống hệt lúc mới mở form.
     */
    public static void setupDirtyTracking(Button btn, Node... nodes) {
        if (btn == null || nodes == null) return;

        // 1. Chụp snapshot giá trị ngay khi mở form
        Object[] initialValues = new Object[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            initialValues[i] = getValue(nodes[i]);
        }

        // 2. Khóa nút ngay lập tức
        btn.setDisable(true);

        // 3. Hàm kiểm tra sự khác biệt
        Runnable check = () -> {
            boolean changed = false;
            for (int i = 0; i < nodes.length; i++) {
                if (!Objects.equals(initialValues[i], getValue(nodes[i]))) {
                    changed = true;
                    break;
                }
            }
            btn.setDisable(!changed);
        };

        // 4. Gắn listener cho mọi loại component
        for (Node n : nodes) {
            if (n instanceof TextField) {
                ((TextField) n).textProperty().addListener((o, oldV, newV) -> check.run());
            } else if (n instanceof ComboBox) {
                ((ComboBox<?>) n).valueProperty().addListener((o, oldV, newV) -> check.run());
            } else if (n instanceof javafx.scene.control.DatePicker) {
                ((javafx.scene.control.DatePicker) n).valueProperty().addListener((o, oldV, newV) -> check.run());
            } else if (n instanceof CheckBox) {
                ((CheckBox) n).selectedProperty().addListener((o, oldV, newV) -> check.run());
            } else {
                // Hỗ trợ DatePicker custom của bạn thông qua Focus và Click
                n.focusedProperty().addListener((o, oldV, focused) -> { if (!focused) check.run(); });
                n.setOnMouseClicked(e -> Platform.runLater(check));
            }
        }
    }

 /**
  * Helper: Trích xuất giá trị an toàn từ mọi loại giao diện.
  */
 private static Object getValue(Node n) {
     if (n instanceof TextField) return ((TextField) n).getText() != null ? ((TextField) n).getText().trim() : "";
     if (n instanceof ComboBox) return ((ComboBox<?>) n).getValue();
     if (n instanceof javafx.scene.control.DatePicker) return ((javafx.scene.control.DatePicker) n).getValue();
     if (n instanceof CheckBox) return ((CheckBox) n).isSelected();
     
     // Dùng Reflection: Nếu component tự chế có viết sẵn hàm getValue(), tự động gọi nó ra!
     try {
         return n.getClass().getMethod("getValue").invoke(n);
     } catch (Exception e) {
         return null; // Bỏ qua nếu không lấy được giá trị
     }
 }

    /* ══════════════════════════════════════════════════════════════════
       4. TIỆN ÍCH TẠO SNAPSHOT GIÁ TRỊ BAN ĐẦU
    ══════════════════════════════════════════════════════════════════ */

    public static String[] snapshot(TextField... fields) {
        String[] snap = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            snap[i] = fields[i].getText() != null ? fields[i].getText() : "";
        }
        return snap;
    }

    public static boolean hasTextChanged(TextField[] fields, String[] originals) {
        if (fields.length != originals.length) return true;
        for (int i = 0; i < fields.length; i++) {
            String current = fields[i].getText() != null ? fields[i].getText() : "";
            if (!current.equals(originals[i])) return true;
        }
        return false;
    }
}