package se.osbe.jetski;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// --- Dataklasser för YAML-konfigurationen ---
class AppConfig {
    public List<ScriptConfig> scripts;
}

class ScriptConfig {
    public String name;
    public String path;
    public String parameters;
    public String tooltip;
}

public class JetskiRunnerApp extends JFrame {

    public static final String APP_NAME = "Jetski Async Script Runner";
    private static final String CONFIG_FILE_NAME = "settings.yml";

    private JTextArea outputArea;
    private AppConfig config;
    private JPanel tablePanel;

    private JButton clearButton;
    private int activeScriptsCount = 0;

    public JetskiRunnerApp() {
        setTitle(APP_NAME);
        setSize(850, 500);

        // Set windows size boundaries - begin
        setMinimumSize(new Dimension(600, 400));
        Rectangle usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setMaximumSize(new Dimension(usableBounds.width, usableBounds.height));
        // Set windows size boundaries - end

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createMenuBar();
        loadConfiguration();

        // --- BYTT: Skapa tabell-panelen med GridBagLayout istället för GridLayout ---
        tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildTable();

        // --- Skapa textytan för utskrifter ---
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // --- Skapa en bottenpanel med "Rensa logg"-knappen ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> outputArea.setText(""));
        bottomPanel.add(clearButton);

        // --- Skapa en rubrik för loggen ---
        JLabel outputLabel = new JLabel("Script output:");
        outputLabel.setFont(new Font("Arial", Font.BOLD, 12));
        outputLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));

        // --- Montera ihop logg-panelen ---
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        logPanel.add(outputLabel, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        logPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(new JScrollPane(tablePanel), BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Arkiv-meny
        JMenu fileMenu = new JMenu("File");
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.setToolTipText("Edit " + CONFIG_FILE_NAME);
        settingsItem.addActionListener(e -> openSettingsDialog());
        fileMenu.add(settingsItem);

        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Hjälp-meny med About
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> openAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void openAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "About", true);
        aboutDialog.setSize(400, 250);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Jetski Async Script Runner");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descArea = new JTextArea(
            "This application executes bash scripts asynchronously, " +
                "ensuring the graphical user interface remains responsive.\n\n" +
                "It is fully configurable via a YAML file (.yml), allowing you to " +
                "dynamically add, manage, and run multiple scripts."
        );
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(descArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> aboutDialog.dispose());
        buttonPanel.add(closeButton);

        aboutDialog.add(contentPanel, BorderLayout.CENTER);
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);

        aboutDialog.setVisible(true);
    }

    private void openSettingsDialog() {
        JDialog dialog = new JDialog(this, "Edit settings (" + CONFIG_FILE_NAME + ")", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea yamlEditor = new JTextArea();
        yamlEditor.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Path yamlPath = Path.of(CONFIG_FILE_NAME);
        try {
            if (Files.exists(yamlPath)) {
                yamlEditor.setText(Files.readString(yamlPath));
            } else {
                yamlEditor.setText("scripts:\n  - name: \"New script\"\n    path: \"./myscript.sh\"\n    parameters: \"\"\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not read the file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                Files.writeString(yamlPath, yamlEditor.getText());
                dialog.dispose();

                // --- NYTT: Spara fönstrets nuvarande position och storlek ---
                Rectangle currentBounds = JetskiRunnerApp.this.getBounds();

                JetskiRunnerApp.this.dispose();
                SwingUtilities.invokeLater(() -> {
                    JetskiRunnerApp newApp = new JetskiRunnerApp();

                    // Åsidosätt standardcentreringen med de sparade värdena
                    newApp.setBounds(currentBounds);

                    newApp.setVisible(true);
                });

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not save the file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(new JScrollPane(yamlEditor), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void loadConfiguration() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            config = mapper.readValue(new File(CONFIG_FILE_NAME), AppConfig.class);
        } catch (Exception e) {
            config = new AppConfig();
        }
    }

    private void buildTable() {
        tablePanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Marginaler mellan celler
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // Vänsterställ innehållet

        // --- Skapa tabellrubrikerna ---
        gbc.gridy = 0; // Rad 0

        gbc.gridx = 0; gbc.weightx = 0.0; // Kolumn 1: Ta exakt så mycket plats texten behöver
        tablePanel.add(new JLabel("<html><b>Script</b></html>"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; // Kolumn 2: Ta ALL resterande plats
        tablePanel.add(new JLabel("<html><b>Params</b></html>"), gbc);

        gbc.gridx = 2; gbc.weightx = 0.0; // Kolumn 3: Ta exakt så mycket plats knapparna behöver
        tablePanel.add(new JLabel("<html><b>Action</b></html>"), gbc);

        int row = 1;
        if (config != null && config.scripts != null) {
            for (ScriptConfig script : config.scripts) {
                gbc.gridy = row++; // Öka radnumret för varje skript

                // --- Skriptnamn ---
                gbc.gridx = 0; gbc.weightx = 0.0;
                JLabel label = new JLabel(script.name != null ? script.name : "Unknown");
                label.setToolTipText(script.tooltip);
                tablePanel.add(label, gbc);

                // --- Parametrar ---
                gbc.gridx = 1; gbc.weightx = 1.0;
                String params = script.parameters != null ? script.parameters : "";
                JTextField paramField = new JTextField(params);
                paramField.setEditable(true);
                paramField.setEnabled(true);
                tablePanel.add(paramField, gbc);

                // --- Knappar ---
                gbc.gridx = 2; gbc.weightx = 0.0;
                JPanel actionPanel = new JPanel(new GridLayout(1, 2, 5, 0));
                JButton runButton = new JButton("Start");
                JButton stopButton = new JButton("Cancel");
                stopButton.setEnabled(false);

                actionPanel.add(runButton);
                actionPanel.add(stopButton);
                tablePanel.add(actionPanel, gbc);

                final Process[] activeProcess = new Process[1];

                runButton.addActionListener(e -> {
                    executeScript(script.path, paramField.getText(), runButton, stopButton, activeProcess);
                });

                stopButton.addActionListener(e -> {
                    Process p = activeProcess[0];
                    if (p != null && p.isAlive()) {
                        p.destroyForcibly();
                        outputArea.append("\n[!] Sending a kill signal to the script...\n");
                    }
                });
            }
        }

        // --- Filler-panel för att trycka upp raderna ---
        // Det förhindrar att raderna "sprider ut sig" vertikalt om man drar ut fönstret stort
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        tablePanel.add(new JLabel(""), gbc);

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void executeScript(String scriptPath, String parameters, JButton sourceButton, JButton stopButton, Process[] activeProcess) {
        if (scriptPath == null || scriptPath.isEmpty()) {
            outputArea.append("\nOgiltig sökväg för skriptet.\n");
            return;
        }

        sourceButton.setEnabled(false);
        stopButton.setEnabled(true);

        activeScriptsCount++;
        clearButton.setEnabled(false);

        outputArea.append("\n--- Startar: " + scriptPath + " " + parameters + " ---\n");

        Thread scriptThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", scriptPath + " " + parameters);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                activeProcess[0] = process;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String outputLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append(outputLine + "\n");
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        });
                    }
                }

                int exitCode = process.waitFor();

                SwingUtilities.invokeLater(() -> {
                    if (exitCode == 137 || exitCode == 143) {
                        outputArea.append("Skriptet avbröts manuellt.\n");
                    } else {
                        outputArea.append("Avslutades med kod: " + exitCode + "\n");
                    }

                    sourceButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    activeProcess[0] = null;

                    activeScriptsCount--;
                    if (activeScriptsCount == 0) {
                        clearButton.setEnabled(true);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Fel vid körning: " + ex.getMessage() + "\n");
                    sourceButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    activeProcess[0] = null;

                    activeScriptsCount--;
                    if (activeScriptsCount == 0) {
                        clearButton.setEnabled(true);
                    }
                });
            }
        });

        scriptThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JetskiRunnerApp().setVisible(true);
        });
    }
}