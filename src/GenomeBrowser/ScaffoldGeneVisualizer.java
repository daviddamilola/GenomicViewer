/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */




package GenomeBrowser;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author davidoluwasusi
 */

public class ScaffoldGeneVisualizer extends JPanel {

    private String scaffoldName;
    private List<Gene> genes; // List of genes on the scaffold
    private int scaffoldLength;

    public ScaffoldGeneVisualizer(String scaffoldName, Map<String, List<Gene>> geneData, int scaffoldLength) {
        this.scaffoldName = scaffoldName;
        this.genes = geneData.getOrDefault(scaffoldName, new ArrayList<>());
        this.scaffoldLength = scaffoldLength;
        setPreferredSize(new Dimension(800, 150));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int padding = 40;
        int usableWidth = getWidth() - 2 * padding;
        int trackHeight = 25;

        // Draw the scaffold line
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(padding, getHeight() / 2 - 5, usableWidth, 10);

        // Scale factor to fit genes in the panel
        double scale = (double) usableWidth / scaffoldLength;

        // Draw genes
        for (Gene gene : genes) {
            int startX = padding + (int) (gene.start * scale);
            int endX = padding + (int) (gene.end * scale);
            int width = Math.max(endX - startX, 5); // Ensure minimum width

            // Set color based on strand
            if (gene.strand.equals("+")) {
                g2d.setColor(Color.BLUE);
            } else {
                g2d.setColor(Color.RED);
            }

            // Draw gene
            g2d.fillRect(startX, getHeight() / 2 - trackHeight / 2, width, trackHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(startX, getHeight() / 2 - trackHeight / 2, width, trackHeight);

            // Label with gene name
            g2d.drawString(gene.strand, startX, getHeight() / 2 - trackHeight / 2 - 5);
        }

        // Labels
        g2d.setColor(Color.BLACK);
        g2d.drawString("Start", padding, getHeight() - 10);
        g2d.drawString("End", getWidth() - padding, getHeight() - 10);
    }

    // Gene class for storing information
    public static class Gene {
        String name;
        int start;
        int end;
        String strand;

        public Gene(String name, int start, int end, String strand) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.strand = strand;
        }
    }
}

