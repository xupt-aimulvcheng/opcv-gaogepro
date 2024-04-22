module com.xupt.opengaugepro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires opencv;  // 仅保留对 OpenCV 的引用
    requires javafx.swing;
    requires org.slf4j;
    requires org.controlsfx.controls;

    opens com.xupt.opengaugepro to javafx.fxml;
    exports com.xupt.opengaugepro;
}
