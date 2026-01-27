package com.castellanos94.tracking;

import com.castellanos94.tracking.model.Category;
import com.castellanos94.tracking.model.FormatExportEnum;
import com.castellanos94.tracking.service.TimerService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class ImportWizardController {

    @FXML
    private TextField pathField;

    @FXML
    private RadioButton jsonRadio;
    @FXML
    private RadioButton xlsxRadio;
    @FXML
    private RadioButton csvRadio;
    @FXML
    private RadioButton customRadio;

    @FXML
    private ComboBox<Category> categoryComboBox;

    private Stage stage;
    private final TimerService timerService = TimerService.getInstance();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
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
        categoryComboBox.setButtonCell(new ListCell<Category>() {
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
        if (!timerService.getCategories().isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Import File");

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            pathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleImport() {
        if (pathField.getText() == null || pathField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a file to import.");
            return;
        }

        FormatExportEnum format = FormatExportEnum.JSON;
        if (xlsxRadio.isSelected())
            format = FormatExportEnum.XLSX;
        else if (csvRadio.isSelected())
            format = FormatExportEnum.CSV;
        else if (customRadio.isSelected())
            format = FormatExportEnum.CUSTOM;

        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (format == FormatExportEnum.CUSTOM && selectedCategory == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Default Category is required for Custom import.");
            return;
        }

        log.info("Importing from: " + pathField.getText());
        log.info("Format: " + format);
        if (selectedCategory != null) {
            log.info("Default Category: " + selectedCategory.getName());
        }

        // TODO: Call Import Service here

        stage.close();
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}
