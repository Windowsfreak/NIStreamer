package de.windowsfreak.testjni.codec;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public interface Codec {
    public int encode(ByteBuffer compressedData, ByteBuffer uncompressedData, int uncompressedLength) throws IOException;
    public void decode(ByteBuffer uncompressedData, int uncompressedLength, ByteBuffer compressedData, int compressedLength) throws IOException;
}
