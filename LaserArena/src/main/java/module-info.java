module LaserArena {
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;


    opens com.laserarena.Controller to javafx.fxml;
    opens com.laserarena.Model     to javafx.base;
    opens com.laserarena to javafx.graphics;

    exports com.laserarena.Controller;
    exports com.laserarena.Model;
    exports com.laserarena.Repository;
}