package de.windowsfreak.testjni.codec;

import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReaderSpi;
import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi;
import de.windowsfreak.testjni.util.ByteBufferBackedInputStream;
import de.windowsfreak.testjni.util.ByteBufferBackedOutputStream;

import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class CharLSCodec extends AbstractCodec {
    public CharLSCodec() {}
    public CharLSCodec(int x, int y) {
        super(x, y);
    }

    static {
        System.loadLibrary("libCharLS");
    }

    private native int encode(ByteBuffer compressedData, int compressedLength, ByteBuffer uncompressedData, int uncompressedLength, int x, int y, int allowedlossyerror, int MAXVAL, int T1, int T2, int T3, int RESET);
    private native int decode(ByteBuffer compressedData, int compressedLength, ByteBuffer uncompressedData, int uncompressedLength, int x, int y, int allowedlossyerror, int MAXVAL, int T1, int T2, int T3, int RESET);

    @Override
    public int encode(ByteBuffer compressedData, ByteBuffer uncompressedData, int uncompressedLength) throws IOException {
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                int compressedLength = encode(compressedData, compressedData.capacity(), uncompressedData, uncompressedLength, x, y, 0, 0, 0, 0, 0, 0);
                compressedData.flip();
                compressedData.limit(compressedLength);
                return compressedLength;
            }
        }
    }

    @Override
    public void decode(ByteBuffer uncompressedData, int uncompressedLength, ByteBuffer compressedData, int compressedLength) throws IOException {
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                uncompressedLength = decode(compressedData, compressedData.capacity(), uncompressedData, uncompressedLength, x, y, 0, 0, 0, 0, 0, 0);
                uncompressedData.flip();
                uncompressedData.limit(uncompressedLength);
            }
        }
    }
}
