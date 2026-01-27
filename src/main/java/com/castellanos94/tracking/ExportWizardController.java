package com.castellanos94.tracking;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import com.castellanos94.tracking.model.FormatExportEnum;
import com.castellanos94.tracking.service.TimeReportExportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportWizardController {

    @FXML
    private TextField pathField;

    @FXML
    private RadioButton jsonRadio;
    @FXML
    private RadioButton xlsxRadio;
    @FXML
    private RadioButton csvRadio;

    @FXML
    private RadioButton allHistoryRadio;
    @FXML
    private RadioButton customRangeRadio;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    private Stage stage;
    private TimeReportExportService timeReportExportService;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.timeReportExportService = new TimeReportExportService();
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Export File");

        String ext = FormatExportEnum.JSON.getExtension();
        if (xlsxRadio.isSelected())
            ext = FormatExportEnum.XLSX.getExtension();
        else if (csvRadio.isSelected())
            ext = FormatExportEnum.CSV.getExtension();

        fileChooser.setInitialFileName("tracking_time_export" + ext);
        if (pathField.getText() != null && !pathField.getText().isEmpty()) {
            File current = new File(pathField.getText());
            if (current.getParentFile() != null && current.getParentFile().exists()) {
                fileChooser.setInitialDirectory(current.getParentFile());
            }
        }

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            pathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleRangeToggle() {
        boolean custom = customRangeRadio.isSelected();
        startDatePicker.setDisable(!custom);
        endDatePicker.setDisable(!custom);
    }

    @FXML
    private void handleExport() {
        if (pathField.getText() == null || pathField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please select an export destination.");
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }

        FormatExportEnum format = FormatExportEnum.JSON;
        if (xlsxRadio.isSelected())
            format = FormatExportEnum.XLSX;
        else if (csvRadio.isSelected())
            format = FormatExportEnum.CSV;

        boolean isCustomRange = customRangeRadio.isSelected();
        String rangeInfo = "All History";
        if (isCustomRange) {
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Please select both start and end dates.");
                alert.initOwner(stage);
                alert.showAndWait();
                return;
            }
            if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Start date must be before end date.");
                alert.initOwner(stage);
                alert.showAndWait();
                return;
            }
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("End date must be after start date.");
                alert.initOwner(stage);
                alert.showAndWait();
                return;
            }
            rangeInfo = String.format("From %s to %s", startDatePicker.getValue(), endDatePicker.getValue());
        }

        log.info("Exporting to: " + pathField.getText());
        log.info("Format: " + format);
        log.info("Range: " + rangeInfo);

        try {
            if (isCustomRange) {
                timeReportExportService.export(startDatePicker.getValue(), endDatePicker.getValue(), format,
                        pathField.getText());
            } else {
                timeReportExportService.export(null, null, format, pathField.getText());
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Export completed successfully.");
            alert.initOwner(stage);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Export failed: " + e.getMessage());
            alert.initOwner(stage);
            alert.showAndWait();
        }

        stage.close();
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }
}
