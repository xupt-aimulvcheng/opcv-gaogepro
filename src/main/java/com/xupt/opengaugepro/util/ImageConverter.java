package com.xupt.opengaugepro.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class ImageConverter {

    public Image convertMatToJavaFXImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArray));
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
