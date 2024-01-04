package com.xupt.opengaugepro.factory;

import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ButtonFactory {
    ButtonBinding buttonBinding = new ButtonBinding();
    public Button[] createButton(Stage primaryStage) {
        Button btnUpload = new Button("上传图像");
        Button btnDenoise = new Button("去噪");
        Button btnAdjustBrightness = new Button("调整亮度/对比度");
        Button btnToGrayScale = new Button("转换为灰度图");
        Button btnExtractDial = new Button("提取表盘图像");
        Button btnEnhance = new Button("增强处理表盘图像");
        Button btnZoomInOriginal = new Button("放大原图");
        Button btnZoomOutOriginal = new Button("缩小原图");
        Button btnZoomInProcessed = new Button("放大处理图");
        Button btnZoomOutProcessed = new Button("缩小处理图");

        btnUpload.setOnAction(event -> buttonBinding.uploadImage(primaryStage));
        btnDenoise.setOnAction(event -> buttonBinding.processImage("denoise"));
        btnAdjustBrightness.setOnAction(event -> buttonBinding.processImage("adjustBrightness"));
        btnToGrayScale.setOnAction(event -> buttonBinding.processImage("toGrayScale"));
        btnExtractDial.setOnAction(event -> buttonBinding.processImage("extractDial"));
        btnEnhance.setOnAction(event -> buttonBinding.processImage("enhance"));
        btnZoomInOriginal.setOnAction(event -> buttonBinding.zoomImage(true, true));
        btnZoomOutOriginal.setOnAction(event -> buttonBinding.zoomImage(false, true));
        btnZoomInProcessed.setOnAction(event -> buttonBinding.zoomImage(true, false));
        btnZoomOutProcessed.setOnAction(event -> buttonBinding.zoomImage(false, false));

        Button[] buttons = {btnUpload, btnDenoise, btnAdjustBrightness, btnToGrayScale, btnExtractDial, btnEnhance, btnZoomInOriginal, btnZoomOutOriginal, btnZoomInProcessed, btnZoomOutProcessed};
        return buttons;
    }
}
