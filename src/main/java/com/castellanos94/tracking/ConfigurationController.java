package com.castellanos94.tracking;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import com.castellanos94.tracking.model.Project;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationController {

    @FXML
    private TableView<com.castellanos94.tracking.model.Category> categoriesTable;

    @FXML
    private TableColumn<com.castellanos94.tracking.model.Category, String> colName;

    @FXML
    private TableColumn<com.castellanos94.tracking.model.Category, Double> colRate;

    @FXML
    private TableColumn<com.castellanos94.tracking.model.Category, String> colColor;

    @FXML
    private TextField nameField;

    @FXML
    private TextField rateField;

    @FXML
    private ColorPicker colorPicker;

    // Project Tab FXML
    @FXML
    private TableView<Project> projectsTable;
    @FXML
    private TableColumn<Project, String> colProjectName;
    @FXML
    private TableColumn<Project, String> colProjectDesc;
    @FXML
    private TableColumn<Project, String> colProjectOwner;
    @FXML
    private TableColumn<Project, String> colProjectStatus;
    @FXML
    private TextField projectNameField;
    @FXML
    private TextField projectDescField;
    @FXML
    private TextField projectOwnerField;

    private Project selectedProject;

    private Stage stage;
    private com.castellanos94.tracking.service.TimerService timerService;
    private com.castellanos94.tracking.model.Category selectedCategory;

    @FXML
    public void initialize() {
        timerService = com.castellanos94.tracking.service.TimerService.getInstance();

        // Bind Table to ObservableList from Service
        categoriesTable.setItems(timerService.getCategories());

        // Setup Columns
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colRate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hourlyRate"));
        colColor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("color"));

        // Custom cell factory for Color column to show actual color
        colColor.setCellFactory(
                column -> new javafx.scene.control.TableCell<com.castellanos94.tracking.model.Category, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            setStyle("-fx-background-color: " + item + "; -fx-text-fill: black;"); // Simple
                                                                                                   // visualization
                        }
                    }
                });
        rateField.setTextFormatter(new javafx.scene.control.TextFormatter<>(new DoubleStringConverter()));
        // Listen for selection to populate fields for editing
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                com.castellanos94.tracking.model.Category c = (com.castellanos94.tracking.model.Category) newVal;
                nameField.setText(c.getName());
                rateField.setText(String.valueOf(c.getHourlyRate()));
                colorPicker.setValue(javafx.scene.paint.Color.web(safeColor(c.getColor())));
                selectedCategory = c;
            }
        });

        // --- Projects Setup ---
        projectsTable.setItems(timerService.getProjects());
        colProjectName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colProjectDesc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
        colProjectOwner.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("owner"));
        colProjectStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        projectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProject = newVal;
                projectNameField.setText(selectedProject.getName());
                projectDescField.setText(selectedProject.getDescription());
                projectOwnerField.setText(selectedProject.getOwner());
            }
        });
    }

    private String safeColor(String color) {
        return color.trim().isEmpty() ? "#FFFFFF" : color;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleClear() {
        // Logic to clear inputs or reset selection
        nameField.clear();
        rateField.clear();
        colorPicker.setValue(javafx.scene.paint.Color.WHITE);
        selectedCategory = null;
        categoriesTable.refresh();
    }

    @FXML
    private void handleDelete() {
        if (selectedCategory != null) {
            if (Dialogs.showConfirmationDialog("Delete Category", "Are you sure you want to delete this category?",
                    this.categoriesTable.getScene().getWindow())) {
                timerService.getCategories().remove(selectedCategory);
                selectedCategory = null;
                categoriesTable.refresh();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty() || rateField.getText().trim().isEmpty()) {
            rateField.requestFocus();
            rateField.setStyle("-fx-background-color: red;");
            return;
        }
        rateField.setStyle("-fx-background-color: white;");
        if (selectedCategory != null) {
            selectedCategory.setName(nameField.getText());
            selectedCategory.setHourlyRate(Double.parseDouble(rateField.getText()));
            selectedCategory.setColor(toRGBCode(colorPicker.getValue()));
            timerService.updateCategory(selectedCategory);
        } else {
            com.castellanos94.tracking.model.Category c = new com.castellanos94.tracking.model.Category();
            c.setName(nameField.getText());
            c.setHourlyRate(Double.parseDouble(rateField.getText()));
            c.setColor(toRGBCode(colorPicker.getValue()));
            timerService.addCategory(c);
            log.info("Category saved: " + c.getName());
        }
        handleClear();
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // --- Project Handlers ---

    @FXML
    private void handleProjectClear() {
        projectNameField.clear();
        projectDescField.clear();
        projectOwnerField.clear();
        selectedProject = null;
        projectsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleProjectDelete() {
        if (selectedProject != null) {
            if (Dialogs.showConfirmationDialog("Delete Project",
                    "Are you sure you want to delete project '" + selectedProject.getName() + "'?",
                    this.projectsTable.getScene().getWindow())) {
                timerService.deleteProject(selectedProject); // Soft delete likely
                handleProjectClear();
            }
        }
    }

    @FXML
    private void handleProjectSave() {
        if (projectNameField.getText().trim().isEmpty()) {
            projectNameField.requestFocus();
            projectNameField.setStyle("-fx-background-color: red;");
            return;
        }
        projectNameField.setStyle("-fx-background-color: white;");

        if (selectedProject != null) {
            selectedProject.setName(projectNameField.getText());
            selectedProject.setDescription(projectDescField.getText());
            selectedProject.setOwner(projectOwnerField.getText());
            timerService.updateProject(selectedProject);
            projectsTable.refresh();
        } else {
            Project p = new Project(projectNameField.getText());
            p.setDescription(projectDescField.getText());
            p.setOwner(projectOwnerField.getText());
            timerService.addProject(p);
        }
        handleProjectClear();
    }
}
