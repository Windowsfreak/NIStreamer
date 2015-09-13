package de.windowsfreak.testjni.codec;

import de.windowsfreak.testjni.util.ByteBufferBackedInputStream;
import de.windowsfreak.testjni.util.ByteBufferBackedOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class PNGCodec extends AbstractCodec {
    public PNGCodec() {}
    public PNGCodec(int x, int y) {
        super(x, y);
    }

    @Override
    public int encode(ByteBuffer compressedData, ByteBuffer uncompressedData, int uncompressedLength) throws IOException {
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                uncompressedData.rewind();
                compressedData.clear();
                if (uncompressedData.limit() != x * y << 1) {
                    new Exception("Warning! The byte buffer size does not match the raw image dimension! It has " + uncompressedData.limit() + " bytes, where it should has " + (x * y << 1) + " bytes instead.").printStackTrace();
                }
                if (uncompressedData.limit() != uncompressedLength) {
                    new Exception("Warning! The byte buffer size does not match the suggested length! It has " + uncompressedData.limit() + " bytes, where its length suggests " + (x * y << 1) + " bytes instead.").printStackTrace();
                }
                BufferedImage bi;
                bi = new BufferedImage(x, y, BufferedImage.TYPE_USHORT_GRAY);
                ShortBuffer sb = uncompressedData.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
                short[] dst = new short[sb.capacity()];
                sb.get(dst);
                bi.getRaster().setDataElements(0, 0, x, y, dst);
                OutputStream baos = new ByteBufferBackedOutputStream(compressedData);
                ImageIO.write(bi, "png", baos);
                baos.flush();
        /*if (compressedData.limit() <= baos.getStreamPosition()) {
            new Exception("Warning! The ByteBuffer to store the compressed image was too small... It has " + compressedData.limit() + " bytes, but " + mcios.getStreamPosition() + " bytes are required.").printStackTrace();
        }*/
                //byte[] imageInByte = baos.toByteArray();
                //System.out.println("Compression: " + imageInByte.length + " / " + uncompressedLength + " (" + ((imageInByte.length * 100 / (uncompressedLength == 0 ? 1 : uncompressedLength))) + "%)");
        /*if (compressedData.limit() < imageInByte.length) {
            new Exception("Warning! The ByteBuffer to store the compressed image was too small... It has " + compressedData.limit() + " bytes, but " + imageInByte.length + " bytes are required.").printStackTrace();
        }*/
                //compressedData.put(imageInByte);
                compressedData.flip();
                baos.close();
                return compressedData.limit();//imageInByte.length;
            }
        }
    }

    @Override
    public void decode(ByteBuffer uncompressedData, int uncompressedLength, ByteBuffer compressedData, int compressedLength) throws IOException {
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                compressedData.rewind();
                uncompressedData.clear();

                InputStream bais = new ByteBufferBackedInputStream(compressedData);
                BufferedImage bi = ImageIO.read(bais);
                if (bi.getWidth() != x || bi.getHeight() != y) {
                    new Exception("Warning! The source image dimensions don't match the ByteBuffer's dimensions! Source Image: [" + bi.getWidth() + " x " + bi.getHeight() + "], ByteBuffer: [" + x + " x " + y + "]").printStackTrace();
                }
                if (bi.getColorModel().getPixelSize() != 16) {
                    new Exception("Warning! The source image is not a 16-bit grayscale image (TYPE_USHORT_GRAY). It has " + bi.getColorModel().getPixelSize() + " bits per pixel, where it should have 16 instead.").printStackTrace();
                }
                if (uncompressedData.limit() != x * y << 1) {
                    new Exception("Warning! The byte buffer size does not match the raw image dimension! It has " + uncompressedData.limit() + " bytes, where it should has " + (x * y << 1) + " bytes instead.").printStackTrace();
                }
                if (uncompressedData.limit() != uncompressedLength) {
                    new Exception("Warning! The byte buffer size does not match the suggested length! It has " + uncompressedData.limit() + " bytes, where its length suggests " + (x * y << 1) + " bytes instead.").printStackTrace();
                }

                ShortBuffer sb = uncompressedData.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
                sb.put(((DataBufferUShort) bi.getRaster().getDataBuffer()).getData());

                uncompressedData.limit(sb.position() << 1);
            }
        }
    }

}
