package vn.dzokha.FastQC.Modules.Graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Phiên bản Web thuần: Không kế thừa JPanel để chạy được trong môi trường Headless (Server)
 */
public class LineGraph {

    private String[] xTitles;
    private String xLabel;
    private String[] xCategories;
    private double[][] data;
    private String graphTitle;
    private double minY;
    private double maxY;
    private double yInterval;

    private static final Color[] COLOURS = new Color[] {
        new Color(136, 34, 85), new Color(51, 34, 136),
        new Color(17, 119, 51), new Color(221, 204, 119),
        new Color(68, 170, 153), new Color(170, 68, 153),
        new Color(204, 102, 119), new Color(136, 204, 238)
    };

    public LineGraph(double[][] data, double minY, double maxY, String xLabel, String[] xTitles, String[] xCategories, String graphTitle) {
        this.data = data;
        this.minY = minY;
        this.maxY = maxY;
        this.xTitles = xTitles;
        this.xLabel = xLabel;
        this.xCategories = xCategories;
        this.graphTitle = graphTitle;
        this.yInterval = findOptimalYInterval(maxY);
    }

    // Constructor phụ cho xCategories kiểu int
    public LineGraph(double[][] data, double minY, double maxY, String xLabel, String[] xTitles, int[] xCategories, String graphTitle) {
        this(data, minY, maxY, xLabel, xTitles, new String[xCategories.length], graphTitle);
        for (int i = 0; i < xCategories.length; i++) {
            this.xCategories[i] = "" + xCategories[i];
        }
    }

    /**
     * Phương thức quan trọng nhất: Vẽ biểu đồ vào một đối tượng Graphics bất kỳ (Web-friendly)
     */
    public void render(Graphics g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);

        double yStart = (minY % yInterval == 0) ? minY : yInterval * (((int) minY / yInterval) + 1);
        int xOffset = 0;

        // Tính toán xOffset dựa trên độ rộng của nhãn trục Y
        for (double i = yStart; i <= maxY; i += yInterval) {
            String label = String.valueOf(i).replaceAll("\\.0$", "");
            int labelWidth = g.getFontMetrics().stringWidth(label);
            if (labelWidth > xOffset) xOffset = labelWidth;
            g.drawString(label, 2, getY(i, height) + (g.getFontMetrics().getAscent() / 2));
        }

        xOffset += 5;

        // Vẽ tiêu đề biểu đồ
        int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
        g.drawString(graphTitle, (xOffset + ((width - (xOffset + 10)) / 2)) - (titleWidth / 2), 30);

        // Vẽ các trục
        g.drawLine(xOffset, height - 40, width - 10, height - 40);
        g.drawLine(xOffset, height - 40, xOffset, 40);

        // Nhãn trục X
        g.drawString(xLabel, (width / 2) - (g.getFontMetrics().stringWidth(xLabel) / 2), height - 5);

        // Xử lý dữ liệu và vẽ các điểm
        int dataCount = (data.length > 0) ? data[0].length : 0;
        int baseWidth = (width - (xOffset + 10)) / Math.max(dataCount, 1);
        if (baseWidth < 1) baseWidth = 1;

        int lastXLabelEnd = 0;
        for (int i = 0; i < dataCount; i++) {
            if (i % 2 != 0) {
                g.setColor(new Color(230, 230, 230));
                g.fillRect(xOffset + (baseWidth * i), 40, baseWidth, height - 80);
            }
            g.setColor(Color.BLACK);
            String baseNumber = xCategories[i];
            int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
            int baseNumberPosition = (baseWidth / 2) + xOffset + (baseWidth * i) - (baseNumberWidth / 2);

            if (baseNumberPosition > lastXLabelEnd) {
                g.drawString(baseNumber, baseNumberPosition, height - 25);
                lastXLabelEnd = baseNumberPosition + baseNumberWidth + 5;
            }
        }

        // Đường kẻ phụ ngang (Grid lines)
        g.setColor(new Color(180, 180, 180));
        for (double i = yStart; i <= maxY; i += yInterval) {
            g.drawLine(xOffset, getY(i, height), width - 10, getY(i, height));
        }

        // Vẽ các đường dữ liệu (Lines)
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        for (int d = 0; d < data.length; d++) {
            g.setColor(COLOURS[d % COLOURS.length]);
            if (data[d].length > 0) {
                int lastY = getY(data[d][0], height);
                for (int i = 1; i < data[d].length; i++) {
                    int thisY = getY(data[d][i], height);
                    g.drawLine((baseWidth / 2) + xOffset + (baseWidth * (i - 1)), lastY, (baseWidth / 2) + xOffset + (baseWidth * i), thisY);
                    lastY = thisY;
                }
            }
        }

        // Vẽ Chú thích (Legend)
        renderLegend(g, width, height);
    }

    private void renderLegend(Graphics g, int width, int height) {
        g.setFont(g.getFont().deriveFont(Font.BOLD));
        int widestLabel = 0;
        for (String title : xTitles) {
            int w = g.getFontMetrics().stringWidth(title);
            if (w > widestLabel) widestLabel = w;
        }
        widestLabel += 6;

        g.setColor(Color.WHITE);
        g.fillRect((width - 10) - widestLabel, 40, widestLabel, 3 + (20 * xTitles.length));
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect((width - 10) - widestLabel, 40, widestLabel, 3 + (20 * xTitles.length));

        for (int t = 0; t < xTitles.length; t++) {
            g.setColor(COLOURS[t % COLOURS.length]);
            g.drawString(xTitles[t], ((width - 10) - widestLabel) + 3, 35 + (20 * (t + 1)));
        }
        g.setFont(g.getFont().deriveFont(Font.PLAIN));
    }

    private int getY(double y, int height) {
        return (height - 40) - (int) (((height - 80) / (maxY - minY)) * y);
    }

    private double findOptimalYInterval(double max) {
        int base = 1;
        double[] divisions = new double[] {1, 2, 2.5, 5};
        while (true) {
            for (double d : divisions) {
                double tester = base * d;
                if (max / tester <= 10) return tester;
            }
            base *= 10;
        }
    }
}