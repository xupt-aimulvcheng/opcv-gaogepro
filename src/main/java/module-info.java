module com.xupt.opengaugepro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires opencv;
    requires javafx.swing;


    opens com.xupt.opengaugepro to javafx.fxml;
    exports com.xupt.opengaugepro;
}