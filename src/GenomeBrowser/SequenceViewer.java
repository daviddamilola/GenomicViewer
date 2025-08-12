/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GenomeBrowser;

import GenomeBrowser.utilities.TextPaneUtil;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author davidoluwasusi
 */
public class SequenceViewer extends javax.swing.JPanel {

    private String fastaPath = "";
    private Map<String, List<int[]>> parsedExonsFromGtf;
    private ArrayList geneList = new ArrayList();

    private JPanel getThisPanel() {
        return this;
    }

    /**
     * Creates new form SequenceViewer
     */
    public SequenceViewer() {
        initComponents();
    }

    public JTextPane getSequenceTextPanel() {
        return sequenceTextPanel;
    }

    public JScrollPane getSequenceScrollPane() {
        return sequenceScrollPane;
    }

    public void setParsedExons(Map<String, List<int[]>> parsedExons) {
        parsedExonsFromGtf = parsedExons;
    }

    private void appendFastaFileToTextPane(
            String filePath,
            JTextPane textPane,
            JTextPane statsTextPane,
            GenomeBrowser.GeneExplorer explorerTab
    ) {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            int totalLength = 0;
            int totalSequences = 0;
            int gcCount = 0;
            int currentSequenceLength = 0;
            int currentGCCount = 0;

            private Map<String, Object> fastaStats = new HashMap<>(); // To store the calculated statistics

            @Override
            protected Void doInBackground() {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    StringBuilder chunkBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();

                        if (line.startsWith(">")) {
                            // Finalize the current sequence
                            if (currentSequenceLength > 0) {
                                totalLength += currentSequenceLength;
                                gcCount += currentGCCount;
                                totalSequences++;
                                currentSequenceLength = 0;
                                currentGCCount = 0;
                            }

                            // Add header to the chunk
                            chunkBuilder.append("<br><b>").append(line).append("</b><br>");
                            geneList.add(getGeneIdFromLine(line));
                        } else {
                            // Add sequence data to chunk and process for statistics
                            chunkBuilder.append(line);
                            currentSequenceLength += line.length();
                            currentGCCount += countGC(line);
                        }

                        if (chunkBuilder.length() > 20000) {
                            // Publish the current chunk and reset
                            publish(chunkBuilder.toString());
                            chunkBuilder = new StringBuilder();
                        }
                    }

                    // Finalize the last sequence
                    if (currentSequenceLength > 0) {
                        totalLength += currentSequenceLength;
                        gcCount += currentGCCount;
                        totalSequences++;
                    }

                    // Publish any remaining HTML
                    if (chunkBuilder.length() > 0) {
                        publish(chunkBuilder.toString());
                    }
                } catch (Exception e) {
                    System.out.println("Error processing FASTA file: " + e.getMessage());
                }
                return null;
            }

            private void displayFastaStats(String namePath) {
                // Compute basic statistics for GFF
                BasicStatisticsCalculator calculator = new BasicStatisticsCalculator();
                AssemblyMetrics metrics = calculator.calculateAssemblyMetrics(namePath);

                // Format statistics
                String statsString = "FASTA Assembly Metrics:\n"
                        + "Total Contigs: " + metrics.totalContigs + "\n"
                        + "Total Length: " + metrics.totalLength + "\n"
                        + "Largest Contig: " + metrics.largestContig + "\n"
                        + "Average Contig Length: " + metrics.averageLength + "\n"
                        + "N50: " + metrics.n50 + "\n";

                // Update statistics panel
                var currentText = String.join("\n", statsTextPane.getText());
                statsTextPane.setText(currentText.concat("\n").concat(statsString.toString()));
            }

            private int countGC(String sequence) {
                return (int) sequence.chars().filter(ch -> ch == 'G' || ch == 'C').count();
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                javax.swing.text.Document doc = textPane.getDocument();
                appendHtmlToTextPane(textPane, "<html><body><p style=\"width:500px;\">");
                for (String chunk : chunks) {
                    appendHtmlToTextPane(textPane, chunk);
                }
                appendHtmlToTextPane(textPane, "</p></body></html>");

            }

            @Override
            protected void done() {
                // display the stats once processing is complete
                displayFastaStats(filePath);
                TextPaneUtil.toggleApplicationLoader(getParentFrame(getThisPanel()), false);
            }
        };

        worker.execute(); // Start the SwingWorker
    }

    public static JFrame getParentFrame(JPanel panel) {
        // Find and return the JFrame that the panel belongs to
        return (JFrame) SwingUtilities.getWindowAncestor(panel);
    }

    private void appendHtmlToTextPane(JTextPane textPane, String html) {
        HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();

        try {
            kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadupFastaFile(
            String filePath,
            JTextPane statsPane,
            GenomeBrowser.GeneExplorer explorerTab
    ) {

        // Configure the sequenceTextPanel
        sequenceTextPanel.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        sequenceTextPanel.setContentType("text/html");
        sequenceTextPanel.setEditable(false);

        fastaPath = filePath; // Set the global fastaPath

        // Create a SwingWorker for background processing
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Perform the actual FASTA file loading and processing in the background
                appendFastaFileToTextPane(filePath, sequenceTextPanel, statsPane, explorerTab);
                return null;
            }

            @Override
            protected void done() {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            sequenceTextPanel,
                            "An error occurred while loading the FASTA file: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String getGeneIdFromLine(String line) {
        String[] parts = line.split("\\|");
        return parts[0].substring(1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sequenceLabel = new javax.swing.JLabel();
        sequenceScrollPane = new javax.swing.JScrollPane();
        sequenceTextPanel = new javax.swing.JTextPane();

        sequenceLabel.setText("Fast Sequence");

        sequenceTextPanel.setEditable(false);
        sequenceTextPanel.setContentType("text/html"); // NOI18N
        sequenceScrollPane.setViewportView(sequenceTextPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sequenceLabel)
                        .addGap(0, 890, Short.MAX_VALUE))
                    .addComponent(sequenceScrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(sequenceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addGap(41, 41, 41))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel sequenceLabel;
    private javax.swing.JScrollPane sequenceScrollPane;
    private javax.swing.JTextPane sequenceTextPanel;
    // End of variables declaration//GEN-END:variables
}
