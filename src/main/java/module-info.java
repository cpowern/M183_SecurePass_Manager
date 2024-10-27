module ims {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ims to javafx.fxml;
    exports ims;
}
