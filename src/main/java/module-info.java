module com.xupt.opengaugepro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires opencv;
    requires javafx.swing;
    requires org.slf4j;

    opens com.xupt.opengaugepro to javafx.fxml;
    exports com.xupt.opengaugepro;
}