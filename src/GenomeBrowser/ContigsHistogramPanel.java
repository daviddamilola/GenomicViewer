/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GenomeBrowser;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContigsHistogramPanel extends JPanel {

    private Map<String, Integer> contigLengths;

    public ContigsHistogramPanel(Map<String, Integer> contigLengths) {
        this.contigLengths = contigLengths;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (contigLengths == null || contigLengths.isEmpty()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 60;
        int maxBarWidth = width - 250 - 2 * padding; // Space for legend
        int maxBarHeight = height - 2 * padding - 40;

        // Sort contigs in descending order based on length
        List<Map.Entry<String, Integer>> sortedContigs = new ArrayList<>(contigLengths.entrySet());
        sortedContigs.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // Descending order

        // Get max contig length for scaling
        int maxContigLength = sortedContigs.get(0).getValue();

        // Bar settings with better spacing
        int numBars = sortedContigs.size();
        int barWidth = Math.max(3, maxBarWidth / (numBars * 2)); // Reduce bar width for better spacing
        int barSpacing = barWidth / 2; // Extra spacing between bars
        int x = padding + 40; // Start position after Y-axis

        // Identify N50
        int n50Value = calculateN50(sortedContigs);

        // Create a legend mapping (A, B, C, ...) -> Contig Names
        Map<String, String> legend = new LinkedHashMap<>();
        char label = 'A';

        for (Map.Entry<String, Integer> entry : sortedContigs) {
            legend.put(String.valueOf(label), entry.getKey());
            label++;
        }

        // Draw Title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Contig Lengths Histogram", width / 3, padding - 20);

        // Set the font for Y-Axis labels and Legend (same size)
        Font axisLegendFont = new Font("Arial", Font.PLAIN, 12);
        g2d.setFont(axisLegendFont);

        // Draw Y-Axis
        g2d.drawLine(padding + 40, height - padding, padding + 40, padding);
        g2d.drawString("Length", padding - 30, padding - 10);

        // Draw Y-Axis Labels
        for (int i = 0; i <= 5; i++) {
            int y = height - padding - (i * maxBarHeight / 5);
            int labelValue = (maxContigLength * i) / 5;
            g2d.drawString(String.valueOf(labelValue), padding - 30, y);
            g2d.drawLine(padding + 35, y, padding + 40, y); // Small tick
        }

        // Draw bars
        label = 'A';
        for (Map.Entry<String, Integer> entry : sortedContigs) {
            int barHeight = (int) (((double) entry.getValue() / maxContigLength) * maxBarHeight);
            int barX = x;
            int barY = height - barHeight - padding;

            // Color the N50 bar red, others blue
            if (entry.getValue() == n50Value) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(new Color(70, 130, 180)); // Steel Blue
            }

            g2d.fillRect(barX, barY, barWidth, barHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(barX, barY, barWidth, barHeight);

            // Draw "N50" on top of the N50 bar
            if (entry.getValue() == n50Value) {
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(Color.RED);
                g2d.drawString("N50", barX + barWidth / 4, barY - 5); // Positioned above the bar
            }

            // Draw Short Labels Under Each Bar
            g2d.setFont(axisLegendFont);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(label), barX + barWidth / 3, height - padding + 20);

            x += barWidth + barSpacing; // Space between bars
            label++;
        }

        // Draw Legend
        int legendX = width - 220;
        int legendY = padding;

        g2d.drawString("Legend:", legendX, legendY - 10);

        // Add Contig Labels to Legend
        for (Map.Entry<String, String> entry : legend.entrySet()) {
            g2d.drawString(entry.getKey() + " = " + entry.getValue(), legendX, legendY);
            legendY += 15;
        }
    }

// Method to calculate N50
    private int calculateN50(List<Map.Entry<String, Integer>> sortedContigs) {
        int totalLength = sortedContigs.stream().mapToInt(Map.Entry::getValue).sum();
        int halfLength = totalLength / 2;
        int cumulativeLength = 0;

        for (Map.Entry<String, Integer> entry : sortedContigs) {
            cumulativeLength += entry.getValue();
            if (cumulativeLength >= halfLength) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public void updateData(Map<String, Integer> newData) {
        this.contigLengths = newData;
        repaint();
    }
}
