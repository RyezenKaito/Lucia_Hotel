package gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class ThongKeDoanhThuPanel extends JPanel {

    // Bảng màu chuẩn Premium
    private final Color COLOR_BG = new Color(250, 247, 240);
    private final Color COLOR_BROWN = new Color(75, 45, 40); 
    private final Color COLOR_BROWN_HOVER = new Color(110, 80, 70); // Màu khi hover nút
    private final Color COLOR_GOLD = new Color(200, 165, 115);
    private final Color COLOR_TEXT = new Color(60, 45, 35);
    private final Color COLOR_GRID = new Color(235, 230, 220);

    public ThongKeDoanhThuPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // 1. Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Container chứa các biểu đồ
        JPanel chartsContainer = new JPanel();
        chartsContainer.setLayout(new BoxLayout(chartsContainer, BoxLayout.Y_AXIS));
        chartsContainer.setOpaque(false);
        
        chartsContainer.add(createBarChartPanel());
        chartsContainer.add(Box.createVerticalStrut(40));
        chartsContainer.add(createLineChartPanel());

        JScrollPane scrollPane = new JScrollPane(chartsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Monthly Revenue Dashboard");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(COLOR_BROWN);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        filterPanel.setOpaque(false);

        // Style cho nhãn "Xem theo"
        JLabel lblFilter = new JLabel("Xem theo: ");
        lblFilter.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblFilter.setForeground(COLOR_TEXT);

        // Style cho ComboBox
        String[] options = {"Tuần này", "Tháng này", "Năm nay"};
        JComboBox<String> cbFilter = new JComboBox<>(options);
        cbFilter.setPreferredSize(new Dimension(130, 35));
        cbFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cbFilter.setBackground(Color.WHITE);

        // Style cho Nút Xuất Báo Cáo (Đã sửa lại đẹp)
        JButton btnExport = new JButton("Xuất Báo Cáo");
        btnExport.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnExport.setBackground(new Color(34, 139, 34));
        btnExport.setForeground(new Color(46, 184, 46));
        btnExport.setPreferredSize(new Dimension(140, 35));
        btnExport.setFocusPainted(false);
        btnExport.setBorder(new LineBorder(new Color(100, 80, 60), 1, true)); 
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));

        filterPanel.add(lblFilter);
        filterPanel.add(cbFilter);
        filterPanel.add(btnExport);

        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] months = {"Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};
        double[] room = {85, 92, 78, 115, 108, 90, 125};
        double[] service = {20, 22, 18, 30, 25, 20, 32};

        for (int i = 0; i < months.length; i++) {
            dataset.addValue(room[i], "Room", months[i]);
            dataset.addValue(service[i], "Service", months[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart("Room & Service Breakdown", "", "", dataset, 
                PlotOrientation.VERTICAL, true, true, false);

        styleChart(chart, false);
        return createConstrainedChartPanel(chart, 350);
    }

    private JPanel createLineChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(105, "Total", "Sep");
        dataset.addValue(115, "Total", "Oct");
        dataset.addValue(95, "Total", "Nov");
        dataset.addValue(145, "Total", "Dec");
        dataset.addValue(135, "Total", "Jan");
        dataset.addValue(110, "Total", "Feb");
        dataset.addValue(160, "Total", "Mar");

        JFreeChart chart = ChartFactory.createLineChart("Total Revenue Trend", "", "", dataset, 
                PlotOrientation.VERTICAL, false, true, false);

        styleChart(chart, true);
        
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, COLOR_BROWN);
        renderer.setSeriesStroke(0, new BasicStroke(3.5f)); 
        renderer.setSeriesShapesVisible(0, true);           
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10)); 
        
        plot.setRenderer(renderer);
        return createConstrainedChartPanel(chart, 350);
    }

    private ChartPanel createConstrainedChartPanel(JFreeChart chart, int height) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, height));
        panel.setBackground(COLOR_BG);
        panel.setMouseWheelEnabled(false);      
        panel.setDomainZoomable(false);         
        panel.setRangeZoomable(false);          
        panel.setPopupMenu(null);               
        return panel;
    }

    private void styleChart(JFreeChart chart, boolean isLineChart) {
        chart.setBackgroundPaint(COLOR_BG);
        chart.getTitle().setFont(new Font("Serif", Font.BOLD, 22));
        chart.getTitle().setPaint(COLOR_BROWN);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(COLOR_GRID);
        plot.setOutlineVisible(false);

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        xAxis.setAxisLineVisible(true);
        // Chỉnh khoảng cách giữa các nhóm tháng cho khít lại
        xAxis.setCategoryMargin(0.15); 

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        yAxis.setNumberFormatOverride(new DecimalFormat("0M")); 
        yAxis.setAxisLineVisible(false);
        yAxis.setUpperBound(180); 

        if (!isLineChart) {
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, COLOR_BROWN);
            renderer.setSeriesPaint(1, COLOR_GOLD);
            renderer.setShadowVisible(false);
            
            // CHỈNH CỘT BỰ VÀ KHÍT
            renderer.setMaximumBarWidth(0.35); // Cột bự ra
            renderer.setItemMargin(-0.02);     // Hai cột trong cùng một tháng khít sát nhau
            
            chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
            chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hotel Management System - Statistics");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new ThongKeDoanhThuPanel());
            frame.setSize(1200, 850);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}