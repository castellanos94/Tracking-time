package com.castellanos94.tracking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import lombok.extern.slf4j.Slf4j;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * JavaFX App
 */
@Slf4j
public class App extends Application {

    private static Scene scene;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> showErrorDialog(e));

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("widget.fxml"));
        Parent root = fxmlLoader.load();

        // Transparent scene for widget feel
        scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Remove window decorations (title bar, etc.)
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        // Dragging logic
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void showErrorDialog(Throwable e) {
        log.error("Uncaught exception: ", e);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error inesperado en Tracking Time");
            alert.setHeaderText("Ha ocurrido un error inesperado");
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            log.error("Exception: {}", rootCause.getMessage());
            alert.setContentText(
                    rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.getClass().getSimpleName());

            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            rootCause.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);

            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
