package com.xupt.opengaugepro;

import com.xupt.opengaugepro.entity.Status;
import com.xupt.opengaugepro.factory.ButtonBinding;
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

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        status = new Status();
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

        // 初始化ImageView并设置保持图片宽高比
        originalImageView = new ImageView();
        processedImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        processedImageView.setPreserveRatio(true);

        // 初始化状态标签
        statusLabel = new Label("状态: 未开始");  // 确保statusLabel在这里初始化，避免再次声明局部变量

        // 创建ButtonBinding实例，传入必要的参数
        ButtonBinding buttonBinding = new ButtonBinding(originalImageView, processedImageView, statusLabel);

        // 假设ButtonFactory已经修改为接受ButtonBinding实例
        ButtonFactory buttonFactory = new ButtonFactory(buttonBinding,primaryStage);
        Button[] buttons = buttonFactory.createButton(primaryStage);

        // 设置控制面板
        FlowPane controlPanel = new FlowPane(10, 10, buttons);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER);

        // 设置状态栏
        VBox statusBar = new VBox(statusLabel);
        statusBar.setPadding(new Insets(10));

        // 创建包含ImageView的ScrollPane，这里不需要设置fitWidth和fitHeight，因为已经绑定
        originalImageScrollPane = new ScrollPane(originalImageView);
        processedImageScrollPane = new ScrollPane(processedImageView);

        // 设置ScrollPane政策
        originalImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        originalImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        processedImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        processedImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 动态调整ImageView大小以适应ScrollPane
        bindImageViewSizeToScrollPane(originalImageView, originalImageScrollPane);
        bindImageViewSizeToScrollPane(processedImageView, processedImageScrollPane);

        // 设置图像面板
        GridPane imagePanel = setupImagePanel();

        // 构建根面板
        BorderPane root = new BorderPane();
        root.setTop(controlPanel);
        root.setCenter(imagePanel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 800, 450);
        primaryStage.setTitle("压力表盘图像信息处理系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane setupImagePanel() {
        GridPane imagePanel = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        imagePanel.getColumnConstraints().addAll(column1, column2);

        Label originalImageLabel = new Label("原图");
        Label processedImageLabel = new Label("处理图");

        imagePanel.add(originalImageScrollPane, 0, 0);
        imagePanel.add(processedImageScrollPane, 1, 0);
        imagePanel.add(originalImageLabel, 0, 1);
        imagePanel.add(processedImageLabel, 1, 1);
        GridPane.setHalignment(originalImageLabel, HPos.CENTER);
        GridPane.setHalignment(processedImageLabel, HPos.CENTER);

        return imagePanel;
    }

    private void bindImageViewSizeToScrollPane(ImageView imageView, ScrollPane scrollPane) {
        imageView.fitWidthProperty().bind(scrollPane.widthProperty());
        // 如果您也希望根据ScrollPane的高度动态调整图片的高度，则可以取消下面这行代码的注释
        // imageView.fitHeightProperty().bind(scrollPane.heightProperty());
    }




    public static void main(String[] args) {
        launch(args);
    }
}

