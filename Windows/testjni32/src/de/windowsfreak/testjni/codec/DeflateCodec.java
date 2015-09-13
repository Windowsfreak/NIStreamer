package de.windowsfreak.testjni.codec;

import de.windowsfreak.testjni.util.ByteBufferBackedInputStream;
import de.windowsfreak.testjni.util.ByteBufferBackedOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.*;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class DeflateCodec extends AbstractCodec {
    private int level = 0;
    public DeflateCodec() {}
    public DeflateCodec(int x, int y) {
        super(x, y);
    }
    public DeflateCodec(int compressionLevel) { level = compressionLevel; }
    public DeflateCodec(int x, int y, int compressionLevel) {
        super(x, y);
        level = compressionLevel;
    }

    @Override
    public int encode(ByteBuffer compressedData, ByteBuffer uncompressedData, int uncompressedLength) throws IOException {
        uncompressedData.rewind();
        compressedData.clear();
        if (uncompressedData.limit() != x * y << 1) {
            new Exception("Warning! The byte buffer size does not match the raw image dimension! It has " + uncompressedData.limit() + " bytes, where it should has " + (x * y << 1) + " bytes instead.").printStackTrace();
        }
        if (uncompressedData.limit() != uncompressedLength) {
            new Exception("Warning! The byte buffer size does not match the suggested length! It has " + uncompressedData.limit() + " bytes, where its length suggests " + (x * y << 1) + " bytes instead.").printStackTrace();
        }

        OutputStream baos = new ByteBufferBackedOutputStream(compressedData);
        Deflater deflater = new Deflater();
        deflater.setLevel(level);
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);

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

    @Override
    public void decode(ByteBuffer uncompressedData, int uncompressedLength, ByteBuffer compressedData, int compressedLength) throws IOException {
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                compressedData.rewind();
                uncompressedData.clear();

                InputStream bais = new ByteBufferBackedInputStream(compressedData);

                InflaterInputStream iis = new InflaterInputStream(bais, new Inflater());

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
