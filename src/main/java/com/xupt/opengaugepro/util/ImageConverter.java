package com.xupt.opengaugepro.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class ImageConverter {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ImageConverter.class);

    /**
     * 将Mat转换为JavaFX Image
     * @param mat 待转化的Mat
     * @return JavaFX Image
     */
    public Image convertMatToJavaFXImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArray));
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            logger.error("Error converting Mat to JavaFX Image: {}", e.getMessage());
            return null;
        }
    }
}
