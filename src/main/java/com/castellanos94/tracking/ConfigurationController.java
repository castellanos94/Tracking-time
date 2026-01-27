package com.castellanos94.tracking;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    private Stage stage;
    private com.castellanos94.tracking.service.TimerService timerService;

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

        // Listen for selection to populate fields for editing
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                com.castellanos94.tracking.model.Category c = (com.castellanos94.tracking.model.Category) newVal;
                nameField.setText(c.getName());
                rateField.setText(String.valueOf(c.getHourlyRate()));
                colorPicker.setValue(javafx.scene.paint.Color.web(c.getColor()));
            }
        });
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
    }

    @FXML
    private void handleDelete() {
        // Logic to delete selected category
    }

    @FXML
    private void handleSave() {
        // Logic to save (create or update) category
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}
