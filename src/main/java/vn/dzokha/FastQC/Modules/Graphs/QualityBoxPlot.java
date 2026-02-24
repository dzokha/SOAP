package vn.dzokha.FastQC.Modules.Graphs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Phiên bản Web: Loại bỏ JPanel để hỗ trợ xuất báo cáo phía Server (Headless).
 * Vẽ biểu đồ Boxplot chất lượng trình tự (Phred Quality Scores).
 */
public class QualityBoxPlot {

    private double[] means;
    private double[] medians;
    private double[] lowest;
    private double[] highest;
    private double[] lowerQuartile;
    private double[] upperQuartile;
    private String[] xLabels;
    private String graphTitle;
    private double minY;
    private double maxY;
    private double yInterval;
    
    // Màu nền phân vùng chất lượng (Thang đo Phred)
    private static final Color GOOD = new Color(195, 230, 195);      // Xanh lá (Chất lượng tốt > 28)
    private static final Color BAD = new Color(230, 220, 195);       // Cam (Chất lượng trung bình 20-28)
    private static final Color UGLY = new Color(230, 195, 195);      // Đỏ (Chất lượng kém < 20)
    
    // Màu nền tối hơn để tạo hiệu ứng sọc (Zebra stripes)
    private static final Color GOOD_DARK = new Color(175, 230, 175);
    private static final Color BAD_DARK = new Color(230, 215, 175);
    private static final Color UGLY_DARK = new Color(230, 175, 175);

    public QualityBoxPlot(double[] means, double[] medians, double[] lowest, double[] highest, 
                          double[] lowerQuartile, double[] upperQuartile, double minY, double maxY, 
                          double yInterval, String[] xLabels, String graphTitle) {
        this.means = means;
        this.medians = medians;
        this.lowest = lowest;
        this.highest = highest;
        this.lowerQuartile = lowerQuartile;
        this.upperQuartile = upperQuartile;
        this.minY = minY;
        this.maxY = maxY;
        this.yInterval = yInterval;
        this.xLabels = xLabels;
        this.graphTitle = graphTitle;
    }

    /**
     * Phương thức vẽ chính lên một đối tượng Graphics (BufferedImage).
     */
    public void render(Graphics g, int width, int height) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // 1. Vẽ nền trắng
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 2. Tính toán lề trái (xOffset) dựa trên nhãn trục Y
        int xOffset = 0;
        double yStart = (minY % yInterval == 0) ? minY : yInterval * (((int) minY / yInterval) + 1);
        
        g.setColor(Color.BLACK);
        for (double i = yStart; i <= maxY; i += yInterval) {
            String label = String.valueOf((int)i);
            int labelWidth = g.getFontMetrics().stringWidth(label);
            if (labelWidth > xOffset) xOffset = labelWidth;
        }
        xOffset += 10; // Khoảng cách đệm từ nhãn đến trục

        // 3. Vẽ các vùng màu chất lượng và nhãn trục X
        int dataCount = means.length;
        int plotAreaWidth = width - xOffset - 15;
        int baseWidth = dataCount > 0 ? plotAreaWidth / dataCount : plotAreaWidth;
        if (baseWidth < 1) baseWidth = 1;

        int lastXLabelEnd = 0;
        for (int i = 0; i < dataCount; i++) {
            int xPos = xOffset + (baseWidth * i);
            
            // Vẽ các vùng phân loại chất lượng Phred (Background)
            // Vùng kém (< 20)
            g.setColor(i % 2 != 0 ? UGLY : UGLY_DARK);
            g.fillRect(xPos, getY(20, height), baseWidth, getY(minY, height) - getY(20, height));
            
            // Vùng trung bình (20 - 28)
            g.setColor(i % 2 != 0 ? BAD : BAD_DARK);
            g.fillRect(xPos, getY(28, height), baseWidth, getY(20, height) - getY(28, height));
            
            // Vùng tốt (> 28)
            g.setColor(i % 2 != 0 ? GOOD : GOOD_DARK);
            g.fillRect(xPos, getY(maxY, height), baseWidth, getY(28, height) - getY(maxY, height));

            // Vẽ nhãn trục X (bp)
            g.setColor(Color.BLACK);
            String label = xLabels[i];
            int labelW = g.getFontMetrics().stringWidth(label);
            int labelPos = (xPos + (baseWidth / 2)) - (labelW / 2);
            
            if (labelPos > lastXLabelEnd + 2) {
                g.drawString(label, labelPos, height - 25);
                lastXLabelEnd = labelPos + labelW;
            }
        }

        // 4. Vẽ các đường lưới và nhãn trục Y
        g.setColor(new Color(200, 200, 200)); // Màu xám nhạt cho lưới
        for (double i = yStart; i <= maxY; i += yInterval) {
            int yPos = getY(i, height);
            g.drawLine(xOffset, yPos, width - 15, yPos);
            
            g.setColor(Color.BLACK);
            String label = String.valueOf((int)i);
            g.drawString(label, xOffset - g.getFontMetrics().stringWidth(label) - 5, yPos + (g.getFontMetrics().getAscent() / 2));
            g.setColor(new Color(200, 200, 200));
        }

        // 5. Vẽ tiêu đề biểu đồ và trục
        g.setColor(Color.BLACK);
        int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
        g.drawString(graphTitle, (xOffset + (plotAreaWidth / 2)) - (titleWidth / 2), 25);

        g.drawLine(xOffset, height - 40, width - 15, height - 40); // Trục X
        g.drawLine(xOffset, height - 40, xOffset, 40);             // Trục Y
        
        String xTitle = "Position in read (bp)";
        g.drawString(xTitle, (xOffset + (plotAreaWidth / 2)) - (g.getFontMetrics().stringWidth(xTitle) / 2), height - 5);

        // 

        // 6. Vẽ các Boxplots (Râu, Hộp, Trung vị)
        for (int i = 0; i < dataCount; i++) {
            int xCenter = xOffset + (baseWidth * i) + (baseWidth / 2);
            int boxLeft = xOffset + (baseWidth * i) + 2;
            int boxW = Math.max(baseWidth - 4, 1);

            int boxBottomY = getY(lowerQuartile[i], height);
            int boxTopY = getY(upperQuartile[i], height);
            int lowWhiskerY = getY(lowest[i], height);
            int upWhiskerY = getY(highest[i], height);
            int medianY = getY(medians[i], height);

            // Vẽ thân hộp (Yellow Box)
            g.setColor(new Color(240, 240, 0));
            g.fillRect(boxLeft, boxTopY, boxW, boxBottomY - boxTopY);
            g.setColor(Color.BLACK);
            g.drawRect(boxLeft, boxTopY, boxW, boxBottomY - boxTopY);

            // Vẽ râu (Whiskers)
            g.drawLine(xCenter, upWhiskerY, xCenter, boxTopY);     // Râu trên
            g.drawLine(boxLeft, upWhiskerY, boxLeft + boxW, upWhiskerY);
            g.drawLine(xCenter, lowWhiskerY, xCenter, boxBottomY);  // Râu dưới
            g.drawLine(boxLeft, lowWhiskerY, boxLeft + boxW, lowWhiskerY);

            // Vẽ đường trung vị (Red Median line)
            g.setColor(new Color(200, 0, 0));
            g.drawLine(boxLeft, medianY, boxLeft + boxW, medianY);
        }

        // 7. Vẽ đường nối giá trị trung bình (Blue Mean line)
        if (dataCount > 1) {
            g.setColor(new Color(0, 0, 200));
            int prevX = xOffset + (baseWidth / 2);
            int prevY = getY(means[0], height);
            for (int i = 1; i < dataCount; i++) {
                int currX = xOffset + (baseWidth * i) + (baseWidth / 2);
                int currY = getY(means[i], height);
                g.drawLine(prevX, prevY, currX, currY);
                prevX = currX;
                prevY = currY;
            }
        }
    }

    /**
     * Chuyển đổi giá trị Phred Quality Score sang tọa độ Pixel Y.
     */
    private int getY(double y, int height) {
        double range = maxY - minY;
        if (range <= 0) range = 1; // Tránh chia cho 0
        int drawableHeight = height - 80;
        return (height - 40) - (int) ((drawableHeight / range) * (y - minY));
    }
}