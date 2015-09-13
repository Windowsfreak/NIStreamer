package de.windowsfreak.testjni.reader;

import de.windowsfreak.testjni.Config;

import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public interface Source {
    boolean readImage(final Config config, final ByteBuffer out);
}
