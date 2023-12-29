package com.xupt.opengaugepro;

import com.xupt.opengaugepro.imageprocessing.ImageProcessingService;
import com.xupt.opengaugepro.util.ImageConverter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;

public class DialImageProcessingApp extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private ImageView imageView;
    private ImageProcessingService imageProcessingService;
    private ImageConverter imageConverter;

    @Override
    public void start(Stage primaryStage) {
        imageProcessingService = new ImageProcessingService();
        imageConverter = new ImageConverter();

        imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        Button btnUpload = new Button("上传图像");
        btnUpload.setOnAction(event -> uploadImage(primaryStage));

        Button btnExtract = new Button("提取表盘图像");
        Button btnEnhance = new Button("增强处理表盘图像");
        btnExtract.setOnAction(event -> processImage("extract"));
        btnEnhance.setOnAction(event -> processImage("enhance"));

        Label statusLabel = new Label("状态：待处理");

        HBox controlPanel = new HBox(10, btnUpload, btnExtract, btnEnhance);
        controlPanel.setPadding(new javafx.geometry.Insets(10));

        VBox statusBar = new VBox(statusLabel);
        statusBar.setPadding(new javafx.geometry.Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(controlPanel);
        root.setCenter(imageView);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("压力表盘图像信息处理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void uploadImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图像文件");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图像文件", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image("file:" + file.getAbsolutePath());
            imageView.setImage(image);
        }
    }

    private void processImage(String processType) {
        // Assuming image is already loaded and filepath is known
        String filePath = "path/to/your/image.jpg"; // replace with actual file path
        Mat image = imageProcessingService.loadImage(filePath);

        if ("extract".equals(processType)) {
            image = imageProcessingService.extractDial(image);
        } else if ("enhance".equals(processType)) {
            image = imageProcessingService.enhanceImage(image);
        }

        Image fxImage = imageConverter.convertMatToJavaFXImage(image);
        imageView.setImage(fxImage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
