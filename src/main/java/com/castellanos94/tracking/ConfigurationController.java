package com.castellanos94.tracking;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
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
            timerService.getCategories().remove(selectedCategory);
            selectedCategory = null;
            categoriesTable.refresh();
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
}
