package com.castellanos94.tracking;

import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.service.TimerService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.util.Duration;
import com.castellanos94.tracking.model.TimeEntry;
import java.time.format.DateTimeFormatter;

public class WidgetController {

    @FXML
    private Label timerLabel;

    @FXML
    private Label totalEarningsLabel;

    @FXML
    private TextField activityField;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private Button toggleButton;

    @FXML
    private VBox timerView;

    @FXML
    private VBox historyView;

    @FXML
    private ListView<TimeEntry> historyList;

    @FXML
    private Button historyButton;

    private final TimerService timerService = TimerService.getInstance();
    private Timeline timeline;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        // Setup Project Combo
        categoryComboBox.setItems(timerService.getCategories());
        categoryComboBox.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        // Select first if available
        if (!timerService.getCategories().isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }

        // Setup History List
        historyList.setItems(timerService.getHistory());
        historyList.setCellFactory(param -> new ListCell<TimeEntry>() {
            @Override
            protected void updateItem(TimeEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String projectName = "Unknown";
                    // Find project name (inefficient but works for small list)
                    for (Category p : timerService.getCategories()) {
                        if (p.getId().equals(item.getCategoryId())) {
                            projectName = p.getName();
                            break;
                        }
                    }
                    long duration = item.getDurationSeconds();
                    long mm = duration / 60;

                    String desc = item.getDescription();
                    if (desc == null || desc.isBlank()) {
                        setText(String.format("%s - %s (%d min)", projectName,
                                item.getStartTime().format(timeFormatter), mm));
                    } else {
                        setText(String.format("%s: %s - %s (%d min)", projectName, desc,
                                item.getStartTime().format(timeFormatter), mm));
                    }
                }
            }
        });

        // Listen for history changes to update total
        timerService.getHistory().addListener((javafx.collections.ListChangeListener.Change<? extends TimeEntry> c) -> {
            updateTotalEarnings();
        });
        updateTotalEarnings(); // Initial update

        // Setup Timer UI update
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimerLabel()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void updateTotalEarnings() {
        double total = 0.0;
        for (TimeEntry entry : timerService.getHistory()) {
            double rate = entry.getHourlyRate();
            // Fallback for legacy entries (if rate is 0, try to find current category rate)
            if (rate == 0.0) {
                for (Category c : timerService.getCategories()) {
                    if (c.getId().equals(entry.getCategoryId())) {
                        rate = c.getHourlyRate();
                        break;
                    }
                }
            }
            double hours = entry.getDurationSeconds() / 3600.0;
            total += hours * rate;
        }
        if (totalEarningsLabel != null) {
            totalEarningsLabel.setText(String.format("Total: $%.2f", total));
        }
    }

    private void updateTimerLabel() {
        if (timerService.isRunning() && timerService.getCurrentEntry() != null) {
            long seconds = timerService.getCurrentEntry().getDurationSeconds();
            long hh = seconds / 3600;
            long mm = (seconds % 3600) / 60;
            long ss = seconds % 60;
            timerLabel.setText(String.format("%02d:%02d:%02d", hh, mm, ss));
        }
    }

    @FXML
    private void handleToggleTimer() {
        if (timerService.isRunning()) {
            // Stop
            timerService.stopTimer();
            timeline.stop();
            toggleButton.setText("Start");
            toggleButton.getStyleClass().remove("stop-button");
            timerLabel.setText("00:00:00");
            activityField.setDisable(false);
            activityField.clear();
        } else {
            // Start
            Category selected = categoryComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String activity = activityField.getText();
                timerService.startTimer(selected, activity);
                timeline.play();
                toggleButton.setText("Stop");
                toggleButton.getStyleClass().add("stop-button");
                activityField.setDisable(true);
            }
        }
    }

    @FXML
    private void handleToggleHistory() {
        if (historyView.isVisible()) {
            // Hide History
            historyView.setVisible(false);
            historyView.setManaged(false);
            timerView.setVisible(true);
            timerView.setManaged(true);
            historyButton.setText("Show History");
        } else {
            // Show History
            historyView.setVisible(true);
            historyView.setManaged(true);
            timerView.setVisible(false);
            timerView.setManaged(false);
            historyButton.setText("Back to Timer");
        }
    }

    @FXML
    private void handleExport() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("export_wizard.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initStyle(javafx.stage.StageStyle.UTILITY);
            stage.setAlwaysOnTop(true);
            stage.setTitle("Export Data");
            stage.setScene(new javafx.scene.Scene(root));

            ExportWizardController controller = fxmlLoader.getController();
            controller.setStage(stage);

            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenSettings() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("configuration.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initStyle(javafx.stage.StageStyle.UTILITY);
            stage.setAlwaysOnTop(true);
            stage.setTitle("Configuration");
            stage.setScene(new javafx.scene.Scene(root));

            ConfigurationController controller = fxmlLoader.getController();
            controller.setStage(stage);

            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        if (timerService.isRunning()) {
            timerService.stopTimer();
        }
        timerService.saveData();
        Platform.exit();
        System.exit(0);
    }

}
