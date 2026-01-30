package com.castellanos94.tracking;

import com.castellanos94.tracking.model.TimeEntry;
import com.castellanos94.tracking.model.Project; // [NEW]
import com.castellanos94.tracking.service.CategoryService;
import com.castellanos94.tracking.service.ProjectService;
import com.castellanos94.tracking.service.TimeEntryService;
import com.castellanos94.tracking.service.TimerService; // [NEW]
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HistoryController {

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<TimeEntry> historyTable;
    @FXML
    private TableColumn<TimeEntry, String> dateColumn;
    @FXML
    private TableColumn<TimeEntry, String> startColumn;
    @FXML
    private TableColumn<TimeEntry, String> endColumn;
    @FXML
    private TableColumn<TimeEntry, String> durationColumn;
    @FXML
    private TableColumn<TimeEntry, String> categoryColumn;
    @FXML
    private TableColumn<TimeEntry, String> projectColumn;
    @FXML
    private TableColumn<TimeEntry, String> descriptionColumn;
    @FXML
    private TableColumn<TimeEntry, String> hourlyRateColumn;

    @FXML
    private Label totalHoursLabel;
    @FXML
    private Label totalEarningsLabel;

    private ObservableList<TimeEntry> historyData = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TimeEntryService timeEntryService;
    private CategoryService categoryService;
    private ProjectService projectService;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private void handleMousePressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(javafx.scene.input.MouseEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    public void initialize() {

        // Initial load
        timeEntryService = new TimeEntryService();
        categoryService = new CategoryService();
        projectService = new ProjectService();
        // Default to today
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        setupTableColumns();

        handleSearch();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime start = cellData.getValue().getStartTime();
            return new SimpleStringProperty(start != null ? start.format(DATE_FMT) : "");
        });

        startColumn.setCellValueFactory(cellData -> {
            LocalDateTime start = cellData.getValue().getStartTime();
            return new SimpleStringProperty(start != null ? start.format(TIME_FMT) : "");
        });

        endColumn.setCellValueFactory(cellData -> {
            LocalDateTime end = cellData.getValue().getEndTime();
            return new SimpleStringProperty(end != null ? end.format(TIME_FMT) : "Running...");
        });

        durationColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(formatDuration(cellData.getValue().getDurationSeconds()));
        });

        categoryColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getCategoryName());
        });

        projectColumn.setCellValueFactory(cellData -> {
            String projectName = cellData.getValue().getProjectName();
            return new SimpleStringProperty(projectName != null ? projectName : "");
        });

        // Create a list of project names for the ComboBox
        ObservableList<String> projectNames = FXCollections
                .observableArrayList(projectService.getProjects().stream().map(Project::getName).toList());

        projectColumn.setCellFactory(javafx.scene.control.cell.ComboBoxTableCell.forTableColumn(projectNames));

        projectColumn.setOnEditCommit(event -> {
            TimeEntry entry = event.getRowValue();
            String newProjectName = event.getNewValue();

            if (newProjectName == null || newProjectName.isEmpty()) {
                entry.setProjectId(null);
                entry.setProjectName(null);
            } else {
                Project selectedProject = TimerService.getInstance().getProjects().stream()
                        .filter(p -> p.getName().equals(newProjectName))
                        .findFirst()
                        .orElse(null);

                if (selectedProject != null) {
                    entry.setProjectId(selectedProject.getId());
                    entry.setProjectName(selectedProject.getName());
                } else {
                    // Should not happen if selecting from list, but handle anyway
                    log.warn("Project not found: " + newProjectName);
                    return;
                }
            }

            if (timeEntryService.update(entry)) {
                log.info("Updated project to: " + entry.getProjectName());
            } else {
                log.error("Error updating project");
                Dialogs.showExceptionDialog("Error updating project", "Error updating project",
                        this.historyTable.getScene().getWindow());
            }
            historyTable.refresh();
        });
        categoryColumn.setCellFactory(
                javafx.scene.control.cell.ComboBoxTableCell
                        .forTableColumn(FXCollections.observableList(categoryService.getAllCategoriesNames())));

        categoryColumn.setOnEditCommit(event -> {
            TimeEntry entry = event.getRowValue();
            entry.setCategoryName(event.getNewValue());
            entry.setCategoryId(categoryService.findCategoryByName(event.getNewValue()).getId());
            if (timeEntryService.update(entry)) {
                log.info("Updated entry category to: " + entry.getCategoryName());
            } else {
                log.error("Error updating entry category");
                Dialogs.showExceptionDialog("Error updating entry category", "Error updating entry category",
                        this.historyTable.getScene().getWindow());
            }
            historyTable.refresh();
        });
        descriptionColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        descriptionColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        descriptionColumn.setOnEditCommit(event -> {
            TimeEntry entry = event.getRowValue();
            entry.setDescription(event.getNewValue());
            if (timeEntryService.update(entry)) {
                log.info("Updated description to: " + entry.getDescription());
            } else {
                log.error("Error updating description");
                Dialogs.showExceptionDialog("Error updating description", "Error updating description",
                        this.historyTable.getScene().getWindow());
            }
            historyTable.refresh();
        });

        hourlyRateColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(String.valueOf(cellData.getValue().getHourlyRate()));
        });
        hourlyRateColumn.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
        hourlyRateColumn.setOnEditCommit(event -> {
            TimeEntry entry = event.getRowValue();
            if (isValidDouble(event.getNewValue())) {
                entry.setHourlyRate(Double.parseDouble(event.getNewValue()));
            } else {
                log.error("Error updating hourly rate");
                Dialogs.showExceptionDialog("Error updating hourly rate", "Invalid hourly rate",
                        this.historyTable.getScene().getWindow());
                return;
            }
            if (timeEntryService.update(entry)) {
                log.info("Updated hourly rate to: " + entry.getHourlyRate());
            } else {
                log.error("Error updating hourly rate");
                Dialogs.showExceptionDialog("Error updating hourly rate", "Error updating hourly rate",
                        this.historyTable.getScene().getWindow());
            }
            historyTable.refresh();
            calculateTotal();
        });
        historyTable.setItems(historyData);
        historyTable.setEditable(true);
    }

    private boolean isValidDouble(String newValue) {
        try {
            Double.parseDouble(newValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String formatDuration(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @FXML
    private void handleSearch() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start != null && end != null) {
            List<TimeEntry> entries = timeEntryService.getEntriesBetweenDates(start, end);
            historyData.setAll(entries);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        long totalSeconds = 0;
        double totalEarnings = 0.0;

        for (TimeEntry entry : historyData) {
            long duration = entry.getDurationSeconds();
            totalSeconds += duration;
            // Simple earnings (hours * rate)
            totalEarnings += (duration / 3600.0) * entry.getHourlyRate();
        }

        totalHoursLabel.setText(formatDuration(totalSeconds));
        totalEarningsLabel.setText(String.format("$%.2f", totalEarnings));
    }

    @FXML
    private void handleEdit() {
        // Redundant if inline editing is enabled, but can be kept for other properties
        TimeEntry selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Selected entry: " + selected.getDescription());
        }
    }

    @FXML
    private void handleDelete() {
        TimeEntry selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (Dialogs.showConfirmationDialog("Delete Entry", "Are you sure you want to delete this entry?",
                    this.historyTable.getScene().getWindow())) {
                historyData.remove(selected);
                if (timeEntryService.delete(selected)) {
                    log.info("Deleted entry: " + selected.getDescription());
                } else {
                    log.error("Error deleting entry");
                    Dialogs.showExceptionDialog("Error deleting entry", "Error deleting entry",
                            this.historyTable.getScene().getWindow());
                }
                historyTable.refresh();
                calculateTotal();
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) historyTable.getScene().getWindow();
        stage.close();
    }

}
