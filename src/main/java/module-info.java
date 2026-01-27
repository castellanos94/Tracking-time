module com.castellanos94.tracking {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static lombok;
    requires org.slf4j;
    requires javafx.graphics;
    requires org.apache.derby.engine;
    requires org.apache.derby.commons;
    requires org.apache.derby.tools;
    requires java.sql;

    opens com.castellanos94.tracking to javafx.fxml;
    opens com.castellanos94.tracking.model to com.fasterxml.jackson.databind, javafx.base;

    exports com.castellanos94.tracking;

}
