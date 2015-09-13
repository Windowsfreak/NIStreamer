package de.windowsfreak.testjni.reader;

import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public interface ImageReader {
    boolean readImage(int x, int y, ByteBuffer out);
}
