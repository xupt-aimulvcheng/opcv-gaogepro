package com.xupt.opengaugepro;

import com.xupt.opengaugepro.entity.Status;
import com.xupt.opengaugepro.factory.ButtonFactory;
import com.xupt.opengaugepro.imageprocessing.ImageProcessingService;
import com.xupt.opengaugepro.util.ImageConverter;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    public static ScrollPane originalImageScrollPane;
    public static ScrollPane processedImageScrollPane;

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
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setMinWidth(200); // 设置FlowPane的最小宽度

        statusLabel = new Label(status.getStatus());

        VBox statusBar = new VBox(statusLabel);
        statusBar.setPadding(new Insets(10));
        Label originalImageLabel = new Label("原图");
        Label processedImageLabel = new Label("处理图");

        // 创建包含ImageView的ScrollPane
        originalImageScrollPane = new ScrollPane(originalImageView);
        originalImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        originalImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        originalImageScrollPane.setFitToWidth(true); // 宽度适应

        processedImageScrollPane = new ScrollPane(processedImageView);
        processedImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        processedImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        processedImageScrollPane.setFitToWidth(true); // 宽度适应

        GridPane imagePanel = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        imagePanel.getColumnConstraints().addAll(column1, column2);

        imagePanel.add(originalImageScrollPane, 0, 0);
        imagePanel.add(processedImageScrollPane, 1, 0);
        imagePanel.setMinSize(400, 400); // 设置imagePanel的最小尺寸

        imagePanel.add(originalImageLabel, 0, 1);
        imagePanel.add(processedImageLabel, 1, 1);
        GridPane.setHalignment(originalImageLabel, HPos.CENTER);
        GridPane.setHalignment(processedImageLabel, HPos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(controlPanel);
        root.setCenter(imagePanel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 800, 450);
        // 将窗口设置为最大化
        primaryStage.setMaximized(true);
        primaryStage.setTitle("压力表盘图像信息处理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

