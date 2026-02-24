package vn.dzokha.FastQC.Modules.Graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import vn.dzokha.FastQC.Modules.Shared.ModuleConfig;
import vn.dzokha.FastQC.Modules.Utilities.HotColdColourGradient;

public class TileGraph {

    private String[] xLabels;
    private int[] tiles;
    private double[][] tileBaseMeans;
    private HotColdColourGradient gradient = new HotColdColourGradient();

    public TileGraph(String[] xLabels, int[] tiles, double[][] tileBaseMeans) {
        this.xLabels = xLabels;
        this.tiles = tiles;
        this.tileBaseMeans = tileBaseMeans;
    }

    public void render(Graphics g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);

        int lastYLabelPos = 0;
        int xOffset = 0;

        // Tính xOffset từ nhãn Tile
        for (int tile : tiles) {
            int w = g.getFontMetrics().stringWidth(String.valueOf(tile));
            if (w > xOffset) xOffset = w;
        }
        xOffset += 5;

        // Vẽ nhãn trục Y (Tiles)
        for (int i = 0; i < tiles.length; i++) {
            String label = String.valueOf(tiles[i]);
            int thisY = getY(i, height);
            if (i > 0 && thisY + g.getFontMetrics().getAscent() > lastYLabelPos) continue;
            g.drawString(label, 2, thisY);
            lastYLabelPos = thisY;
        }

        // Tiêu đề và Trục
        String title = "Quality per tile";
        g.drawString(title, (xOffset + ((width - (xOffset + 10)) / 2)) - (g.getFontMetrics().stringWidth(title) / 2), 30);
        g.drawLine(xOffset, height - 40, width - 10, height - 40);
        g.drawLine(xOffset, height - 40, xOffset, 40);

        String xTitle = "Position in read (bp)";
        g.drawString(xTitle, (width / 2) - (g.getFontMetrics().stringWidth(xTitle) / 2), height - 5);

        // Vẽ dữ liệu (Heatmap)
        int baseWidth = (width - (xOffset + 10)) / Math.max(xLabels.length, 1);
        if (baseWidth < 1) baseWidth = 1;

        int lastXLabelEnd = 0;
        for (int base = 0; base < xLabels.length; base++) {
            int labelW = g.getFontMetrics().stringWidth(xLabels[base]);
            int labelPos = (baseWidth / 2) + xOffset + (baseWidth * base) - (labelW / 2);
            if (labelPos > lastXLabelEnd) {
                g.drawString(xLabels[base], labelPos, height - 25);
                lastXLabelEnd = labelPos + labelW + 5;
            }
        }

        for (int tile = 0; tile < tiles.length; tile++) {
            for (int base = 0; base < xLabels.length; base++) {
                g.setColor(gradient.getColor(0 - tileBaseMeans[tile][base], 0, ModuleConfig.getParam("tile", "error")));
                g.fillRect(xOffset + (baseWidth * base), getY(tile + 1, height), baseWidth, getY(tile, height) - getY(tile + 1, height));
            }
        }
    }

    private int getY(double y, int height) {
        return (height - 40) - (int) (((height - 80) / (double) (tiles.length)) * y);
    }
}