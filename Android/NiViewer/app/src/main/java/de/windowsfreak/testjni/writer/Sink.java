package de.windowsfreak.testjni.writer;

import de.windowsfreak.testjni.Config;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 24.07.2015.
 */
public interface Sink {
    void initialize(Config config) throws IOException;
    boolean writeImage(Config config, ByteBuffer in, int frameId);
    boolean condition(Config config) throws InterruptedException;
    void shutDown() throws IOException;
}