package de.windowsfreak.testjni.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public interface Codec {
    public int encode(final ByteBuffer compressedData, final ByteBuffer uncompressedData, final int uncompressedLength) throws IOException;
    public void decode(final ByteBuffer uncompressedData, int uncompressedLength, final ByteBuffer compressedData, final int compressedLength) throws IOException;
}
