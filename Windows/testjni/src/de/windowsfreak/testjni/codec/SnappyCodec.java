package de.windowsfreak.testjni.codec;

import de.windowsfreak.testjni.util.ByteBufferBackedInputStream;
import de.windowsfreak.testjni.util.ByteBufferBackedOutputStream;
import org.iq80.snappy.Snappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class SnappyCodec extends AbstractCodec {
    private final SnappyStore store;
    public void toggleDebug(boolean debugEnabled) {
        store.toggleDebug(debugEnabled);
    }
    public SnappyCodec() { store = new SnappyStore(640 * 480 * 2);}
    public SnappyCodec(int x, int y) {
        super(x, y);
        store = new SnappyStore(x * y << 1);
    }

    @Override
    public int encode(final ByteBuffer compressedData, final ByteBuffer uncompressedData, final int uncompressedLength) throws IOException {
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
                int b1c = store.obtain();
                int b2c = store.obtain();
                byte[] b1 = store.acquire(b1c);
                byte[] b2 = store.acquire(b2c);
                uncompressedData.get(b1);
                int compressedLength = Snappy.compress(
                        b1,
                        0,
                        uncompressedLength,
                        b2,
                        0);
                //System.out.println("Compressed length: " + compressedLength + " / " + uncompressedLength);
                compressedData.put(b2, 0, compressedLength);
                store.release(b1c);
                store.release(b2c);
                compressedData.flip();
                return compressedData.limit();
            }
        }
    }

    @Override
    public void decode(final ByteBuffer uncompressedData, final int uncompressedLength, final ByteBuffer compressedData, final int compressedLength) throws IOException {
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                compressedData.rewind();
                uncompressedData.clear();

                int b1c = store.obtain();
                int b2c = store.obtain();
                byte[] b1 = store.acquire(b1c);
                byte[] b2 = store.acquire(b2c);
                compressedData.get(b1, 0, compressedLength);
                int length = Snappy.uncompress(
                        b1,
                        0,
                        compressedLength,
                        b2,
                        0);
                uncompressedData.put(b2, 0, length);
                store.release(b1c);
                store.release(b2c);
                uncompressedData.flip();

                if (uncompressedData.limit() != x * y << 1) {
                    new Exception("Warning! The byte buffer size does not match the raw image dimension! It has " + uncompressedData.limit() + " bytes, where it should has " + (x * y << 1) + " bytes instead.").printStackTrace();
                }
                if (uncompressedData.limit() != uncompressedLength) {
                    new Exception("Warning! The byte buffer size does not match the suggested length! It has " + uncompressedData.limit() + " bytes, where its length suggests " + (x * y << 1) + " bytes instead.").printStackTrace();
                }
            }
        }
    }

}
