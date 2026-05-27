package gui.interfaces;

public interface IRefreshable {
    /**
     * Tự động làm mới dữ liệu từ Database và cố gắng giữ nguyên dòng đang chọn (nếu có).
     */
    void autoRefresh();
}
