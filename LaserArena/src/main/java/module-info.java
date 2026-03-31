module com.laserarena {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.laserarena.Controller to javafx.fxml;
    exports com.laserarena.Model;

}