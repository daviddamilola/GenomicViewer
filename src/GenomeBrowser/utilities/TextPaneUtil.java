package GenomeBrowser.utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Utility class for handling JTextPane-related actions, including downloading
 * content and showing a loading spinner overlay.
 *
 * @author davidoluwasusi
 */
public class TextPaneUtil {

    private static JComponent glassPaneLoader;

    /**
     * Shows or hides a loader overlay across the entire application window.
     *
     * @param frame The main application frame.
     * @param show  If true, shows the loader; if false, hides it.
     */
    public static void toggleApplicationLoader(JFrame frame, boolean show) {
        if (show) {
            // Initialize the glass pane loader if not already created
            if (glassPaneLoader == null) {
                glassPaneLoader = new JComponent() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                glassPaneLoader.setLayout(new GridBagLayout()); // Center components
                glassPaneLoader.setOpaque(false);

                // Add spinner and label
                JPanel loaderPanel = new JPanel(new BorderLayout());
                loaderPanel.setOpaque(false);

                JLabel loadingLabel = new JLabel("Loading...");
                loadingLabel.setFont(new Font("Arial", Font.BOLD, 18));
                loadingLabel.setForeground(Color.WHITE);
                loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JProgressBar spinner = new JProgressBar();
                spinner.setIndeterminate(true);
                spinner.setPreferredSize(new Dimension(150, 20));

                loaderPanel.add(spinner, BorderLayout.CENTER);
                loaderPanel.add(loadingLabel, BorderLayout.SOUTH);

                glassPaneLoader.add(loaderPanel, new GridBagConstraints());
            }

            // Set the glass pane for the frame
            frame.setGlassPane(glassPaneLoader);
            glassPaneLoader.setVisible(true);
        } else {
            if (glassPaneLoader != null) {
                glassPaneLoader.setVisible(false);
            }
        }
    }

    /**
     * Downloads the contents of a JTextPane to a file chosen by the user.
     *
     * @param textPane The JTextPane containing the content to save.
     * @param parent   The parent component for the JFileChooser dialog.
     */
    public static void downloadTextPaneContent(JTextPane textPane, Component parent) {
        // Check if the textPane has any content
        String content = textPane.getText();
        if (content == null || content.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "The text pane is empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use JFileChooser to let the user pick a save location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");
        int userSelection = fileChooser.showSaveDialog(parent);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // Write the content to the selected file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(content);
                JOptionPane.showMessageDialog(parent, "File saved successfully to " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void appendHtmlToTextPane(JTextPane textPane, String html) {
        HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();

        try {
            kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
