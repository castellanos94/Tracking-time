module com.castellanos94.tracking {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.castellanos94.tracking to javafx.fxml;
    opens com.castellanos94.tracking.model to com.fasterxml.jackson.databind;

    exports com.castellanos94.tracking;
}
