package de.windowsfreak.testjni.codec;

import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReaderSpi;
import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi;
import de.windowsfreak.testjni.util.ByteBufferBackedInputStream;
import de.windowsfreak.testjni.util.ByteBufferBackedOutputStream;

import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class JpegLSCodec extends AbstractCodec {
    public JpegLSCodec() {}
    public JpegLSCodec(int x, int y) {
        super(x, y);
    }

    static {
        System.loadLibrary("clib_jiio");
        System.loadLibrary("clib_jiio_sse2");
        System.loadLibrary("clib_jiio_util");
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

                //new CLibJPEGImageReaderSpi();
                //new CLibJPEGImageWriterSpi();
                //System.out.println(Arrays.toString(ImageIO.getReaderFormatNames()));
                ImageWriter writer = new CLibJPEGImageWriterSpi().createWriterInstance();
                //new com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriter();
                //ImageIO.getImageWritersByFormatName("JPEG-LS").next();
                ImageWriteParam iwparam = writer.getDefaultWriteParam();

                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                //iwparam.setCompressionType("HUFFMAN_ONLY");
                //iwparam.setCompressionType("JPEG-LOSSLESS");
                iwparam.setCompressionType("JPEG-LS");
                iwparam.setCompressionQuality(1.0f);

                //System.out.println("ImageWriteParam class name: " + iwparam.getClass());
                //System.out.println("Compression quality is: " + iwparam.getCompressionQuality());

                MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos);
                writer.setOutput(mcios);

                writer.write(null, new IIOImage(bi, null, null), iwparam);
                mcios.flush();
                if (compressedData.limit() <= mcios.getStreamPosition()) {
                    new Exception("Warning! The ByteBuffer to store the compressed image was too small... It has " + compressedData.limit() + " bytes, but " + mcios.getStreamPosition() + " bytes are required.").printStackTrace();
                }

                //ImageIO.write(bi, "png", baos);
                baos.flush();
                //byte[] imageInByte = baos.toByteArray();
                //System.out.println("Compression: " + imageInByte.length + " / " + uncompressedLength + " (" + ((imageInByte.length * 100 / (uncompressedLength == 0 ? 1 : uncompressedLength))) + "%)");
                //compressedData.put(imageInByte);
                compressedData.flip();
                baos.close();
                return compressedData.limit();
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

                ImageReader reader = new CLibJPEGImageReaderSpi().createReaderInstance();
                //ImageIO.getImageReadersByFormatName("JPEG-LS").next();
                ImageReadParam irparam = reader.getDefaultReadParam();

                //irparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                //iwparam.setCompressionType("HUFFMAN_ONLY");
                //iwparam.setCompressionType("JPEG-LOSSLESS");
                //irparam.setCompressionType("JPEG-LS");
                //irparam.setCompressionQuality(1.0f);

                reader.setInput(new MemoryCacheImageInputStream(bais)); // ImageIO.createImageInputStream(bais)

                BufferedImage bi = reader.read(0);
                //BufferedImage bi = ImageIO.read(bais);

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
