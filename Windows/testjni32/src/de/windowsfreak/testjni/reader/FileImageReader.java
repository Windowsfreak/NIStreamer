package de.windowsfreak.testjni.reader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class FileImageReader implements ImageReader {
    private String fileName;
    @Override
    public boolean readImage(int max_x, int max_y, ByteBuffer out) {
        File aFile = new File(fileName);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(aFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        out.clear();
        if (out.limit() != max_x * max_y << 1) {
            new Exception("Warning! The byte buffer size does not match the raw image dimension! It stores " + out.limit() + " bytes, where it should store " + (max_x * max_y << 1) + " bytes instead.").printStackTrace();
        }
        if (bi.getWidth() != max_x || bi.getHeight() != max_y) {
            new Exception("Warning! The source image dimensions don't match the ByteBuffer's dimensions! Source Image: [" + bi.getWidth() + " x " + bi.getHeight() + "], ByteBuffer: [" + max_x + " x " + max_y + "]").printStackTrace();
        }
        if (bi.getColorModel().getPixelSize() != 16) {
            new Exception("Warning! The source image is not a 16-bit grayscale image (TYPE_USHORT_GRAY). It has " + bi.getColorModel().getPixelSize() + " bits per pixel, where it should have 16 instead.").printStackTrace();
        }
        synchronized(out) {
            out.order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(((DataBufferUShort) bi.getRaster().getDataBuffer()).getData());
            out.rewind();
        }
        return true;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
