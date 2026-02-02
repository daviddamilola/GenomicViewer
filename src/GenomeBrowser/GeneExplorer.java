/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GenomeBrowser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;

/**
 *
 * @author davidoluwasusi
 */
public class GeneExplorer extends javax.swing.JPanel {

    private String fastaFilePath;
    private String gffFilePath;
    private Map<String, String> scaffoldSequences;
    private ContigsHistogramPanel histogramPanel;

    /**
     * Creates new form GeneExplorer
     */
    public GeneExplorer() {
        initComponents();
        scaffoldSequences = new HashMap<>();
    }

    private List<int[]> parseGffExons(String filePath) {
        List<int[]> exonList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split("\t");
                if (fields.length < 9) {
                    continue;
                }

                if (fields[2].equalsIgnoreCase("exon")) {
                    int start = Integer.parseInt(fields[3]);
                    int end = Integer.parseInt(fields[4]);
                    exonList.add(new int[]{start, end});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exonList;
    }

    private void setGenesAsOptions(String path) {
        // Use SwingWorker to perform the operation in the background
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                // Perform the long-running task of getting gene names
                return getGeneNamesFromFasta(path);
            }

            @Override
            protected void done() {
                try {
                    // Update the UI on the Event Dispatch Thread
                    List<String> options = get();

                    // Remove all existing items
                    geneSelector.removeAllItems();

                    // Add a default option
                    geneSelector.addItem("Select gene to view");

                    // Add each gene name from the list to the JComboBox
                    for (String option : options) {
                        geneSelector.addItem(option);
                    }
                } catch (Exception e) {
                    // Handle exceptions (e.g., file not found, access issues)
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error loading gene names: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Execute the SwingWorker
        worker.execute();
    }

    public static ArrayList<String> getGeneNamesFromFasta(String fastaFilePath) {
        ArrayList<String> geneNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fastaFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    // Extract the gene name from the header line
                    String geneName = getGeneIdFromLine(line);
                    geneNames.add(geneName);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading FASTA file: " + e.getMessage());
        }

        return geneNames;
    }

    private static String getGeneIdFromLine(String line) {
        String[] parts = line.split("\\|");
        return parts[0].substring(1);
    }

    public static Map<String, Integer> getContigLengthsFromFasta(String fastaFilePath) {
        Map<String, Integer> contigLengths = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fastaFilePath))) {
            String line;
            String currentContig = null;
            int currentLength = 0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    // Save previous contig length
                    if (currentContig != null) {
                        contigLengths.put(currentContig, currentLength);
                    }
                    // Start new contig
                    currentContig = line.split("\\s+")[0].substring(1);
                    currentLength = 0;
                } else {
                    currentLength += line.trim().length();
                }
            }

            // Save the last contig
            if (currentContig != null) {
                contigLengths.put(currentContig, currentLength);
            }

        } catch (IOException e) {
            System.err.println("Error reading FASTA file: " + e.getMessage());
        }

        return contigLengths;
    }

    public void setExplorerFastaFilePath(String filePath) {
        fastaFilePath = filePath;
        setGenesAsOptions(filePath);
        loadScaffoldSequences(filePath);

        // Extract contig lengths and update histogram
        Map<String, Integer> contigLengths = getContigLengthsFromFasta(filePath);
        histogramPanel = new ContigsHistogramPanel(contigLengths);
        contigsHistoPane.removeAll();
        contigsHistoPane.setLayout(new BorderLayout());
        contigsHistoPane.setPreferredSize(new java.awt.Dimension(1110, 300));
        contigsHistoPane.add(histogramPanel, BorderLayout.CENTER);
        contigsHistoPane.revalidate();
        contigsHistoPane.repaint();
    }

    public void setExplorerGffFilePath(String filePath) {
        gffFilePath = filePath;
        loadGeneData(filePath);
    }

    private void loadScaffoldSequences(String fastaFilePath) {
        scaffoldSequences.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(fastaFilePath))) {
            String line;
            String currentScaffold = null;
            StringBuilder sequenceBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (currentScaffold != null) {
                        scaffoldSequences.put(currentScaffold, sequenceBuilder.toString());
                    }
                    currentScaffold = line.split("\\s+")[0].substring(1);
                    sequenceBuilder = new StringBuilder();
                } else {
                    sequenceBuilder.append(line.trim());
                }
            }
            if (currentScaffold != null) {
                scaffoldSequences.put(currentScaffold, sequenceBuilder.toString());
            }
        } catch (IOException e) {
            System.err.println("Error loading sequences: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ExplorePanel = new javax.swing.JPanel();
        geneSelectorPrompt = new javax.swing.JLabel();
        geneSelector = new javax.swing.JComboBox<>();
        chromosomeLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        scaffoldLengthPane = new javax.swing.JTextPane();
        startPositionLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        gcContentPane = new javax.swing.JTextPane();
        geneSequenceLabel = new javax.swing.JLabel();
        visualRepresentationLabel = new javax.swing.JLabel();
        contigsHistoPane = new javax.swing.JPanel();
        geneVisualizationPanel = new java.awt.Panel();
        jScrollPane1 = new javax.swing.JScrollPane();
        scaffoldSequencePanel = new javax.swing.JTextPane();

        geneSelectorPrompt.setText("Select gene to view:");

        geneSelector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        geneSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneSelectorActionPerformed(evt);
            }
        });

        chromosomeLabel.setText("Scafold Length");

        jScrollPane2.setViewportView(scaffoldLengthPane);

        startPositionLabel.setText("GC content");

        jScrollPane3.setViewportView(gcContentPane);

        geneSequenceLabel.setText("Gene sequence");

        visualRepresentationLabel.setText("Visual Representation of selected gene");

        contigsHistoPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Contigs Histogram"));

        javax.swing.GroupLayout contigsHistoPaneLayout = new javax.swing.GroupLayout(contigsHistoPane);
        contigsHistoPane.setLayout(contigsHistoPaneLayout);
        contigsHistoPaneLayout.setHorizontalGroup(
            contigsHistoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1096, Short.MAX_VALUE)
        );
        contigsHistoPaneLayout.setVerticalGroup(
            contigsHistoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout geneVisualizationPanelLayout = new javax.swing.GroupLayout(geneVisualizationPanel);
        geneVisualizationPanel.setLayout(geneVisualizationPanelLayout);
        geneVisualizationPanelLayout.setHorizontalGroup(
            geneVisualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 474, Short.MAX_VALUE)
        );
        geneVisualizationPanelLayout.setVerticalGroup(
            geneVisualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 162, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(scaffoldSequencePanel);

        javax.swing.GroupLayout ExplorePanelLayout = new javax.swing.GroupLayout(ExplorePanel);
        ExplorePanel.setLayout(ExplorePanelLayout);
        ExplorePanelLayout.setHorizontalGroup(
            ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ExplorePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ExplorePanelLayout.createSequentialGroup()
                        .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(visualRepresentationLabel)
                            .addGroup(ExplorePanelLayout.createSequentialGroup()
                                .addComponent(geneSelectorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(geneSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(geneVisualizationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(ExplorePanelLayout.createSequentialGroup()
                                .addComponent(chromosomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(startPositionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(geneSequenceLabel)
                            .addComponent(jScrollPane1))
                        .addGap(0, 10, Short.MAX_VALUE))
                    .addGroup(ExplorePanelLayout.createSequentialGroup()
                        .addComponent(contigsHistoPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))))
        );
        ExplorePanelLayout.setVerticalGroup(
            ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ExplorePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contigsHistoPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(geneSelectorPrompt)
                        .addComponent(geneSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chromosomeLabel))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startPositionLabel)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(geneSequenceLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(visualRepresentationLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ExplorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(geneVisualizationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(108, 108, 108))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ExplorePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(616, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ExplorePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showScaffoldDetails(String scaffoldName) {
        String sequence = scaffoldSequences.get(scaffoldName);
        if (sequence == null) {
            return;
        }

        int length = sequence.length();
        if (length > 1000) {
            sequence = sequence.substring(0, 1000) + "..."; // Append "..." to indicate truncation
        }

        double gcContent = calculateGCContent(sequence);

        scaffoldLengthPane.setText(String.valueOf(length));
        gcContentPane.setText(String.format("%.2f%%", gcContent * 100));
        scaffoldSequencePanel.setContentType("text/html");
        scaffoldSequencePanel.setText("<html><body><p style=\"width:500px;\">" + sequence + "</p></body></html>");

        Map<String, List<ScaffoldGeneVisualizer.Gene>> geneData = loadGeneData(gffFilePath);
        ScaffoldGeneVisualizer geneVisualizer = new ScaffoldGeneVisualizer(scaffoldName, geneData, length);

        // Display in a panel
        geneVisualizationPanel.removeAll();
        geneVisualizationPanel.setLayout(new BorderLayout());
        geneVisualizationPanel.setPreferredSize(new java.awt.Dimension(350, 200));
        geneVisualizationPanel.add(geneVisualizer, BorderLayout.CENTER);
        geneVisualizationPanel.revalidate();
        geneVisualizationPanel.repaint();
    }

    private double calculateGCContent(String sequence) {
        int gcCount = 0;
        for (char c : sequence.toCharArray()) {
            if (c == 'G' || c == 'C') {
                gcCount++;
            }
        }
        return (double) gcCount / sequence.length();
    }

    public Map<String, List<ScaffoldGeneVisualizer.Gene>> loadGeneData(String filePath) {
        Map<String, List<ScaffoldGeneVisualizer.Gene>> geneData = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // Skip comments
                }
                String[] fields = line.split("\t");
                if (fields.length < 9) {
                    continue;
                }

                String seqid = fields[0];  // Scaffold/contig name
                String type = fields[2];   // Feature type
                int start = Integer.parseInt(fields[3]);
                int end = Integer.parseInt(fields[4]);
                String strand = fields[6];

                if (type.equals("gene")) {
                    // Extract gene ID from attributes
                    String attributes = fields[8];
                    String geneName = attributes.split(";")[0].replace("ID=", "").replace("\"", "");

                    geneData.computeIfAbsent(seqid, k -> new ArrayList<>()).add(
                            new ScaffoldGeneVisualizer.Gene(geneName, start, end, strand)
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geneData;
    }


    private void geneSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneSelectorActionPerformed
        // TODO add your handling code here:
        if (evt.getSource() == geneSelector) {
            JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
            var selectedGene = (String) comboBox.getSelectedItem();

            if (selectedGene == null) {
                return;
            }

            if (!selectedGene.isEmpty() || !selectedGene.isBlank()) {
                showScaffoldDetails(selectedGene);
            }
        }
    }//GEN-LAST:event_geneSelectorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ExplorePanel;
    private javax.swing.JLabel chromosomeLabel;
    private javax.swing.JPanel contigsHistoPane;
    private javax.swing.JTextPane gcContentPane;
    private javax.swing.JComboBox<String> geneSelector;
    private javax.swing.JLabel geneSelectorPrompt;
    private javax.swing.JLabel geneSequenceLabel;
    private java.awt.Panel geneVisualizationPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane scaffoldLengthPane;
    private javax.swing.JTextPane scaffoldSequencePanel;
    private javax.swing.JLabel startPositionLabel;
    private javax.swing.JLabel visualRepresentationLabel;
    // End of variables declaration//GEN-END:variables
}
