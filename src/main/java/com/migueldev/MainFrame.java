package com.migueldev;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {

    private static final String[] COLUMNS = {
        "Original Name",
        "New Name",
        "Status",
    };
    private static final String DEFAULT_PATTERN = "#";

    private final FileRenamer renamer = new FileRenamer();
    private final JTextField sourceField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextField patternField = new JTextField(DEFAULT_PATTERN);
    private final DefaultTableModel tableModel = new DefaultTableModel(
        COLUMNS,
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton refreshButton = new JButton("Refresh Preview");
    private final JButton renameButton = new JButton("Rename Files");
    private final JLabel statusLabel = new JLabel(" ");

    private String lastDefaultOutput = "";
    private boolean updatingFields = false;

    public MainFrame() {
        super("Java File Renamer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setupListeners();
        sourceField.requestFocusInWindow();

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Source folder:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(sourceField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(browseButton("source"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Output folder:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(outputField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(browseButton("output"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("New name pattern:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(patternField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        form.add(refreshButton, gbc);

        JLabel hint = new JLabel(
            "Use '#' for the file index (e.g. new_name_#), '#N' to start at N. Extensions are preserved automatically."
        );
        hint.setFont(hint.getFont().deriveFont(hint.getFont().getSize() - 1f));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        form.add(hint, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        renameButton.setEnabled(false);
        buttons.add(renameButton);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);

        return form;
    }

    private JButton browseButton(String target) {
        JButton button = new JButton("Browse...");
        button.addActionListener(e -> chooseDirectory(target));
        return button;
    }

    private JPanel buildTablePanel() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(700, 350));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                    1,
                    0,
                    0,
                    0,
                    java.awt.Color.GRAY
                ),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            )
        );
        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    private void setupListeners() {
        DocumentListener sourceListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onSourceChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSourceChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSourceChanged();
            }
        };
        sourceField.getDocument().addDocumentListener(sourceListener);

        refreshButton.addActionListener(e -> refreshPreview());
        renameButton.addActionListener(e -> renameFiles());
    }

    private void onSourceChanged() {
        if (updatingFields) {
            return;
        }
        updatingFields = true;
        try {
            String source = sourceField.getText().trim();
            if (!source.isEmpty()) {
                String defaultOutput = new File(
                    source,
                    "renamed"
                ).getAbsolutePath();
                if (
                    outputField.getText().trim().isEmpty() ||
                    outputField.getText().trim().equals(lastDefaultOutput)
                ) {
                    outputField.setText(defaultOutput);
                }
                lastDefaultOutput = defaultOutput;
            }
        } finally {
            updatingFields = false;
        }
    }

    private void chooseDirectory(String target) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            if (target.equals("source")) {
                sourceField.setText(dir.getAbsolutePath());
            } else {
                outputField.setText(dir.getAbsolutePath());
            }
        }
    }

    private void refreshPreview() {
        File sourceDir = validateSourceDir();
        if (sourceDir == null) {
            return;
        }
        clearTable();
        List<FileRenamer.RenameItem> items = renamer.preview(
            sourceDir,
            patternField.getText().trim()
        );
        for (FileRenamer.RenameItem item : items) {
            tableModel.addRow(new Object[] {
                item.originalName(),
                item.newFileName(),
                "",
            });
        }
        statusLabel.setText(items.size() + " file(s) ready to rename.");
        renameButton.setEnabled(!items.isEmpty());
    }

    private void renameFiles() {
        File sourceDir = validateSourceDir();
        if (sourceDir == null) {
            return;
        }
        File outputDir = validateOutputDir();
        if (outputDir == null) {
            return;
        }

        refreshPreview();
        if (tableModel.getRowCount() == 0) {
            return;
        }

        setRunning(true);
        final File src = sourceDir;
        final File dst = outputDir;
        final String pattern = patternField.getText().trim();

        new SwingWorker<FileRenamer.RenameResult, Void>() {
            @Override
            protected FileRenamer.RenameResult doInBackground() {
                return renamer.execute(src, dst, pattern);
            }

            @Override
            protected void done() {
                try {
                    FileRenamer.RenameResult result = get();
                    for (
                        int i = 0;
                        i < result.items().size() &&
                        i < tableModel.getRowCount();
                        i++
                    ) {
                        tableModel.setValueAt(
                            result.items().get(i).status(),
                            i,
                            2
                        );
                    }
                    statusLabel.setText(
                        "Summary: " +
                            result.successCount() +
                            " successfully renamed, " +
                            result.failCount() +
                            " failed."
                    );
                    if (result.failCount() > 0) {
                        String message = String.join("\n", result.errors());
                        JOptionPane.showMessageDialog(
                            MainFrame.this,
                            message,
                            "Some files failed",
                            JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "Successfully renamed " +
                                result.successCount() +
                                " file(s).",
                            "Done",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Operation failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                        MainFrame.this,
                        "An error occurred: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setRunning(false);
                }
            }
        }.execute();
    }

    private void setRunning(boolean running) {
        refreshButton.setEnabled(!running);
        renameButton.setEnabled(!running && tableModel.getRowCount() > 0);
        sourceField.setEnabled(!running);
        outputField.setEnabled(!running);
        patternField.setEnabled(!running);
    }

    private File validateSourceDir() {
        String path = sourceField.getText().trim();
        if (path.isEmpty()) {
            showError("Source folder path cannot be empty.");
            return null;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            showError("Directory does not exist: " + dir.getAbsolutePath());
            return null;
        }
        if (!dir.isDirectory()) {
            showError("Path is not a directory: " + dir.getAbsolutePath());
            return null;
        }
        return dir;
    }

    private File validateOutputDir() {
        String path = outputField.getText().trim();
        if (path.isEmpty()) {
            showError("Output folder path cannot be empty.");
            return null;
        }
        File dir = new File(path);
        if (dir.exists() && !dir.isDirectory()) {
            showError(
                "Target path exists but is not a directory: " +
                    dir.getAbsolutePath()
            );
            return null;
        }
        return dir;
    }

    private void showError(String message) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }
}
