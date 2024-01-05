package com.xupt.opengaugepro;

import com.xupt.opengaugepro.entity.Status;
import com.xupt.opengaugepro.factory.ButtonFactory;
import com.xupt.opengaugepro.imageprocessing.ImageProcessingService;
import com.xupt.opengaugepro.util.ImageConverter;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.io.File;

public class DialImageProcessingApp extends Application {
    public static Status status;
    public static ButtonFactory buttonFactory;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        status = new Status();
        buttonFactory = new ButtonFactory();
    }

    public static File currentImageFile;
    public static Mat processedImage;
    public static ImageView originalImageView;
    public static ImageView processedImageView;
    public static ImageProcessingService imageProcessingService;
    public static ImageConverter imageConverter;
    public static Label statusLabel;


    @Override
    public void start(Stage primaryStage) {
        imageProcessingService = new ImageProcessingService();
        imageConverter = new ImageConverter();
        originalImageView = new ImageView();
        processedImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        processedImageView.setPreserveRatio(true);

        Button[] buttons = buttonFactory.createButton(primaryStage);
        FlowPane controlPanel = new FlowPane(10, 10, buttons);
        controlPanel.setPadding(new javafx.geometry.Insets(10));
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setMinWidth(200); // 设置FlowPane的最小宽度

        statusLabel = new Label(status.getStatus());

        VBox statusBar = new VBox(statusLabel);
        statusBar.setPadding(new javafx.geometry.Insets(10));
        Label originalImageLabel = new Label("原图");
        Label processedImageLabel = new Label("处理图");
        GridPane imagePanel = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        imagePanel.getColumnConstraints().addAll(column1, column2);

        imagePanel.add(originalImageView, 0, 0);
        imagePanel.add(processedImageView, 1, 0);
        imagePanel.setMinSize(400, 400); // 设置imagePanel的最小尺寸

        // 监听窗口宽度变化并动态调整 ImageView 的宽度
        imagePanel.widthProperty().addListener((obs, oldVal, newVal) -> {
            double panelWidth = newVal.doubleValue();
            originalImageView.setFitWidth(panelWidth / 2);
            processedImageView.setFitWidth(panelWidth / 2);
        });
        imagePanel.add(originalImageLabel, 0, 1);
        imagePanel.add(processedImageLabel, 1, 1);
        GridPane.setHalignment(originalImageLabel, HPos.CENTER);
        GridPane.setHalignment(processedImageLabel, HPos.CENTER);
        BorderPane root = new BorderPane();
        root.setTop(controlPanel);
        root.setCenter(imagePanel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 800, 450);
        primaryStage.setHeight(673);
        primaryStage.setTitle("压力表盘图像信息处理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

