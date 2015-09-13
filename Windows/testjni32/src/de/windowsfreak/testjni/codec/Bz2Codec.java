package de.windowsfreak.testjni.codec;

import de.windowsfreak.testjni.util.ByteBufferBackedInputStream;
import de.windowsfreak.testjni.util.ByteBufferBackedOutputStream;
import org.itadaki.bzip2.BZip2InputStream;
import org.itadaki.bzip2.BZip2OutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class Bz2Codec extends AbstractCodec {
    private int level = 0;
    public Bz2Codec() {}
    public Bz2Codec(int x, int y) {
        super(x, y);
    }
    public Bz2Codec(int compressionLevel) { level = compressionLevel; }
    public Bz2Codec(int x, int y, int compressionLevel) {
        super(x, y);
        level = compressionLevel;
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

                OutputStream baos = new ByteBufferBackedOutputStream(compressedData);
                BZip2OutputStream dos = new BZip2OutputStream(baos, level);

                byte[] b = new byte[1024];
                while (true) {
                    int i = uncompressedData.remaining();
                    if (i <= 0) break;
                    if (i > b.length) i = b.length;
                    uncompressedData.get(b, 0, i);
                    dos.write(b, 0, i);
                }

                dos.finish();

                baos.flush();
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

                BZip2InputStream iis = new BZip2InputStream(bais, false);

                if (uncompressedData.limit() != x * y << 1) {
                    new Exception("Warning! The byte buffer size does not match the raw image dimension! It has " + uncompressedData.limit() + " bytes, where it should has " + (x * y << 1) + " bytes instead.").printStackTrace();
                }
                if (uncompressedData.limit() != uncompressedLength) {
                    new Exception("Warning! The byte buffer size does not match the suggested length! It has " + uncompressedData.limit() + " bytes, where its length suggests " + (x * y << 1) + " bytes instead.").printStackTrace();
                }

                byte[] b = new byte[1024];
                while (iis.available() > 0) {
                    int i = iis.read(b);
                    if (i <= 0) break;
                    uncompressedData.put(b, 0, i);
                }

                uncompressedData.flip();
            }
        }
    }

}
