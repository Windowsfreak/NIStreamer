package de.windowsfreak.testjni.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class CharLSCodec extends AbstractCodec {
    public CharLSCodec() {}
    public CharLSCodec(int x, int y) {
        super(x, y);
    }

    private native void hello();
    private native int encode(ByteBuffer compressedData, int compressedLength, ByteBuffer uncompressedData, int uncompressedLength, int x, int y, int allowedlossyerror, int MAXVAL, int T1, int T2, int T3, int RESET);
    private native int decode(ByteBuffer compressedData, int compressedLength, ByteBuffer uncompressedData, int uncompressedLength, int x, int y, int allowedlossyerror, int MAXVAL, int T1, int T2, int T3, int RESET);

    static {
        System.loadLibrary("libCharLS");
    }

    @Override
    public int encode(final ByteBuffer compressedData, final ByteBuffer uncompressedData, final int uncompressedLength) throws IOException {
        hello();
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                //uncompressedData.rewind();
                //compressedData.clear();
                //System.out.println("encoding " + uncompressedLength + " bytes into " + compressedData.capacity() + " bytes storage, " + x + "x" + y + " pixels");
                int compressedLength = encode(compressedData, compressedData.capacity(), uncompressedData, uncompressedLength, x, y, 0, 0, 0, 0, 0, 0);
                if (compressedLength < 0) {
                    System.out.println("Encode: CharLS error no. " + -compressedLength);
                }
                compressedData.flip();
                compressedData.limit(compressedLength);
                //System.out.println("Compressed length: " + compressedLength);
                return compressedLength;
            }
        }
    }

    @Override
    public void decode(final ByteBuffer uncompressedData, int uncompressedLength, final ByteBuffer compressedData, final int compressedLength) throws IOException {
        hello();
        synchronized(compressedData) {
            synchronized (uncompressedData) {
                //compressedData.rewind();
                //uncompressedData.clear();
                //System.out.println("Compressed length: " + compressedLength);
                uncompressedLength = decode(compressedData, compressedLength, uncompressedData, uncompressedLength, x, y, 0, 0, 0, 0, 0, 0);
                if (uncompressedLength < 0) {
                    System.out.println("Decode: CharLS error no. " + -uncompressedLength);
                }
                uncompressedData.flip();
                uncompressedData.limit(uncompressedLength);
            }
        }
    }
}
