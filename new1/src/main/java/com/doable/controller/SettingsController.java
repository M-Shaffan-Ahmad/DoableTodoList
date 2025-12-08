package com.doable.controller;

import com.doable.dao.TaskDao;
import com.doable.dao.CategoryDao;
import com.doable.model.Task;
import com.doable.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

public class SettingsController {
    @FXML private ComboBox<String> ringtoneCombo;
    @FXML private Label exportStatus;
    @FXML private Label importStatus;
    @FXML private Label customRingtoneLabel;
    @FXML private Label customRingtoneStatus;
    @FXML private ListView<Category> categoriesList;
    @FXML private Label categoriesStatus;

    private final TaskDao taskDao = new TaskDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
    private Stage stage;
    private String customRingtoneFile = null;

    public void initialize() {
        // Initialize ringtone options
        ringtoneCombo.setItems(FXCollections.observableArrayList(
            "None", "Beep", "Chime", "Ding", "Ping", "Custom"
        ));
        
        // Load saved ringtone preference
        String savedRingtone = prefs.get("ringtone", "Beep");
        ringtoneCombo.setValue(savedRingtone);
        
        // Load custom ringtone file path if exists
        customRingtoneFile = prefs.get("customRingtoneFile", null);
        if (customRingtoneFile != null && !customRingtoneFile.isEmpty()) {
            File f = new File(customRingtoneFile);
            if (f.exists()) {
                customRingtoneLabel.setText(f.getName());
                customRingtoneStatus.setText("Custom ringtone loaded: " + customRingtoneFile);
            } else {
                customRingtoneFile = null;
                customRingtoneLabel.setText("No custom ringtone selected");
                customRingtoneStatus.setText("(Previous file not found)");
            }
        }
        
        // Load categories
        loadCategories();
    }
    
    private void loadCategories() {
        try {
            categoriesList.setItems(FXCollections.observableArrayList(categoryDao.findAll()));
            categoriesList.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(null);
                    } else {
                        setText(item.getName());
                        setStyle("-fx-text-fill: black;");
                    }
                }
            });
            categoriesStatus.setText("Categories loaded: " + categoriesList.getItems().size());
        } catch (SQLException e) {
            e.printStackTrace();
            categoriesStatus.setText("Error loading categories: " + e.getMessage());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onTestRingtone() {
        String selected = ringtoneCombo.getValue();
        if (selected == null || "None".equals(selected)) {
            return;
        }
        playRingtone(selected);
    }
    
    @FXML
    private void onNewCategorySettings() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Category");
        dialog.setHeaderText("Enter category name:");
        dialog.setContentText("Category name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String categoryName = result.get().trim();
            try {
                // Check if category already exists
                for (Category cat : categoriesList.getItems()) {
                    if (cat.getName().equalsIgnoreCase(categoryName)) {
                        Alert a = new Alert(Alert.AlertType.WARNING, "Category already exists!");
                        a.showAndWait();
                        return;
                    }
                }
                
                // Create new category
                Category newCat = new Category(categoryName);
                categoryDao.save(newCat);
                loadCategories();
                
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Category '" + categoryName + "' created successfully!");
                a.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error creating category: " + e.getMessage());
                a.showAndWait();
            }
        }
    }
    
    @FXML
    private void onEditCategory() {
        Category selected = categoriesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a category to edit");
            a.showAndWait();
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit category name:");
        dialog.setContentText("Category name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();
            try {
                // Check if new name already exists (but allow keeping same name)
                for (Category cat : categoriesList.getItems()) {
                    if (cat.getId() != selected.getId() && cat.getName().equalsIgnoreCase(newName)) {
                        Alert a = new Alert(Alert.AlertType.WARNING, "Category name already exists!");
                        a.showAndWait();
                        return;
                    }
                }
                
                selected.setName(newName);
                categoryDao.save(selected);
                loadCategories();
                
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Category updated successfully!");
                a.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error updating category: " + e.getMessage());
                a.showAndWait();
            }
        }
    }
    
    @FXML
    private void onDeleteCategory() {
        Category selected = categoriesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a category to delete");
            a.showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Delete category '" + selected.getName() + "'? Tasks will not be deleted but will be unassigned.");
        confirm.setTitle("Confirm Delete");
        Optional<javafx.scene.control.ButtonType> response = confirm.showAndWait();
        
        if (response.isPresent() && response.get() == javafx.scene.control.ButtonType.OK) {
            try {
                categoryDao.delete(selected.getId());
                loadCategories();
                
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Category deleted successfully!");
                a.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error deleting category: " + e.getMessage());
                a.showAndWait();
            }
        }
    }
    
    @FXML
    private void onChooseCustomRingtone() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Custom Ringtone File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files (*.wav, *.mp3)", "*.wav", "*.mp3"),
            new FileChooser.ExtensionFilter("WAV Files (*.wav)", "*.wav"),
            new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            customRingtoneFile = selectedFile.getAbsolutePath();
            customRingtoneLabel.setText(selectedFile.getName());
            customRingtoneStatus.setText("Path: " + customRingtoneFile);
            
            // Automatically switch to Custom ringtone
            ringtoneCombo.setValue("Custom");
        }
    }

    @FXML
    private void onExportTasks() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Tasks to JavaScript File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JavaScript Files (*.js)", "*.js"));
            fileChooser.setInitialFileName("tasks.js");

            File file = fileChooser.showSaveDialog(stage);
            if (file == null) return;

            List<Task> tasks = taskDao.findAll();
            String jsContent = generateJavaScriptExport(tasks);

            Files.writeString(file.toPath(), jsContent);
            exportStatus.setText("✓ Exported " + tasks.size() + " tasks to " + file.getName());
        } catch (SQLException | IOException e) {
            exportStatus.setText("✗ Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onImportTasks() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Tasks from JavaScript File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JavaScript Files (*.js)", "*.js"));

            File file = fileChooser.showOpenDialog(stage);
            if (file == null) return;

            String content = Files.readString(file.toPath());
            int imported = importJavaScriptTasks(content);

            if (imported > 0) {
                importStatus.setText("✓ Imported " + imported + " tasks from " + file.getName());
                showInfo("Import Successful", "Successfully imported " + imported + " tasks.");
            } else {
                importStatus.setText("✗ No valid tasks found in file");
            }
        } catch (IOException | SQLException e) {
            importStatus.setText("✗ Import failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSave() {
        String ringtone = ringtoneCombo.getValue();
        System.out.println("Saving ringtone preference: " + ringtone);
        
        if (ringtone != null) {
            prefs.put("ringtone", ringtone);
        }
        
        // Save custom ringtone file path if using custom ringtone
        if ("Custom".equals(ringtone) && customRingtoneFile != null) {
            System.out.println("Saving custom ringtone file: " + customRingtoneFile);
            prefs.put("customRingtoneFile", customRingtoneFile);
        } else if (!"Custom".equals(ringtone)) {
            // Clear custom ringtone file if switching away from Custom
            System.out.println("Clearing custom ringtone file");
            prefs.remove("customRingtoneFile");
        }
        
        showInfo("Saved", "Settings saved successfully.");
        
        // Close the settings window
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void onClose() {
        if (stage != null) {
            stage.close();
        }
    }

    private void playRingtone(String ringtone) {
        // Play system sound based on selection on a separate thread
        new Thread(() -> {
            try {
                if ("Beep".equals(ringtone)) {
                    playSystemSound("Beep");
                } else if ("Chime".equals(ringtone)) {
                    playSystemSound("Chime");
                } else if ("Ding".equals(ringtone)) {
                    playSystemSound("Ding");
                } else if ("Ping".equals(ringtone)) {
                    playSystemSound("Ping");
                } else if ("Custom".equals(ringtone)) {
                    if (customRingtoneFile != null) {
                        playAudioFile(new java.io.File(customRingtoneFile));
                    } else {
                        showInfo("No Custom Ringtone", "Please select a custom ringtone file first.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void playSystemSound(String soundName) {
        try {
            // Use Windows native sounds
            String soundPath;
            if ("Beep".equals(soundName)) {
                // Play using Toolkit beep - most reliable
                java.awt.Toolkit.getDefaultToolkit().beep();
                Thread.sleep(100);
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            } else if ("Chime".equals(soundName)) {
                soundPath = "C:\\Windows\\Media\\chime.wav";
            } else if ("Ding".equals(soundName)) {
                soundPath = "C:\\Windows\\Media\\ding.wav";
            } else if ("Ping".equals(soundName)) {
                soundPath = "C:\\Windows\\Media\\notify.wav";
            } else {
                return;
            }
            
            // Try to play the Windows system sound file
            java.io.File audioFile = new java.io.File(soundPath);
            if (audioFile.exists()) {
                playAudioFile(audioFile);
            } else {
                // Fallback to beep
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            // Fallback to beep if anything fails
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void playAudioFile(java.io.File audioFile) {
        try {
            String filename = audioFile.getName().toLowerCase();
            
            if (filename.endsWith(".mp3")) {
                playMP3File(audioFile);
            } else if (filename.endsWith(".wav")) {
                playWAVFile(audioFile);
            } else {
                // Try as WAV first, then fallback
                try {
                    playWAVFile(audioFile);
                } catch (Exception e) {
                    playMP3File(audioFile);
                }
            }
        } catch (Exception e) {
            // Fallback to beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    
    private void playWAVFile(java.io.File audioFile) {
        try {
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(javax.sound.sampled.AudioSystem.getAudioInputStream(audioFile));
            clip.start();
            // Wait for sound to finish playing
            Thread.sleep(clip.getMicrosecondLength() / 1000 + 100);
            clip.close();
        } catch (Exception e) {
            e.printStackTrace();
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    
    private void playMP3File(java.io.File audioFile) {
        try {
            // Try to use jLayer library for MP3 playback
            javazoom.jl.player.Player player = new javazoom.jl.player.Player(new java.io.FileInputStream(audioFile));
            player.play();
        } catch (NoClassDefFoundError e) {
            // jLayer not available, try WAV format or fallback
            try {
                playWAVFile(audioFile);
            } catch (Exception ex) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private String generateJavaScriptExport(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("// Doable Tasks Export\n");
        sb.append("// Generated: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("const tasks = [\n");

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  {\n");
            sb.append("    title: \"").append(escapeJsString(t.getTitle())).append("\",\n");
            sb.append("    description: \"").append(escapeJsString(t.getDescription())).append("\",\n");
            sb.append("    dueDate: \"").append(t.getDueDate() != null ? t.getDueDate().format(DateTimeFormatter.ISO_DATE_TIME) : "").append("\",\n");
            sb.append("    completed: ").append(t.isCompleted()).append(",\n");
            sb.append("    repeatRule: \"").append(t.getRepeatRule() != null ? t.getRepeatRule() : "NONE").append("\",\n");
            sb.append("    categoryName: \"").append(escapeJsString(t.getCategoryName() != null ? t.getCategoryName() : "")).append("\"\n");
            sb.append("  }");
            if (i < tasks.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("];\n\n");
        sb.append("// You can import these tasks by opening the Settings and using Import function\n");
        sb.append("// Each task object should have: title, description, dueDate, completed, repeatRule, categoryName\n");

        return sb.toString();
    }

    private int importJavaScriptTasks(String jsContent) throws SQLException {
        int imported = 0;

        // Parse JavaScript array format: { title: "...", description: "...", ... }
        // Simple regex-based parser for the expected format
        String[] taskBlocks = jsContent.split("\\{");

        for (String block : taskBlocks) {
            if (!block.contains("title:")) continue;

            try {
                Task task = new Task();

                // Extract title
                String title = extractJsValue(block, "title:");
                if (title.isEmpty()) continue;
                
                // Check if title exists and auto-rename if needed
                if (taskDao.isTitleExists(title)) {
                    String originalTitle = title;
                    int copyCount = 1;
                    while (taskDao.isTitleExists(title)) {
                        title = originalTitle + " (copy " + copyCount + ")";
                        copyCount++;
                    }
                    System.out.println("Renamed duplicate import: '" + originalTitle + "' -> '" + title + "'");
                }
                
                task.setTitle(title);

                // Extract description
                String description = extractJsValue(block, "description:");
                task.setDescription(description);

                // Extract dueDate
                String dueDateStr = extractJsValue(block, "dueDate:");
                if (!dueDateStr.isEmpty()) {
                    try {
                        task.setDueDate(LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME));
                    } catch (Exception e) {
                        // Invalid date format, skip
                    }
                }

                // Extract completed
                String completedStr = extractJsValue(block, "completed:");
                task.setCompleted("true".equalsIgnoreCase(completedStr));

                // Extract repeatRule
                String repeatRule = extractJsValue(block, "repeatRule:");
                task.setRepeatRule(repeatRule.isEmpty() ? "NONE" : repeatRule);

                // Extract categoryName
                String categoryName = extractJsValue(block, "categoryName:");
                task.setCategoryName(categoryName.isEmpty() ? null : categoryName);

                // Save to database
                taskDao.save(task);
                imported++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return imported;
    }

    private String extractJsValue(String block, String key) {
        int startIndex = block.indexOf(key);
        if (startIndex == -1) return "";

        // Skip the key and find the opening quote or value
        int quoteStart = block.indexOf('"', startIndex);
        if (quoteStart == -1) {
            // It's a boolean or null, not a string
            int colonIndex = startIndex + key.length();
            int commaIndex = block.indexOf(',', colonIndex);
            int closeIndex = block.indexOf('}', colonIndex);
            int endIndex = Math.min(commaIndex == -1 ? closeIndex : commaIndex, closeIndex);
            return block.substring(colonIndex, endIndex).trim();
        }

        quoteStart++; // Move past the opening quote
        int quoteEnd = block.indexOf('"', quoteStart);
        if (quoteEnd == -1) return "";

        return unescapeJsString(block.substring(quoteStart, quoteEnd));
    }

    private String escapeJsString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String unescapeJsString(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
