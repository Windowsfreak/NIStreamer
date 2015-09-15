package de.windowsfreak.testjni.reader;

import de.windowsfreak.testjni.Config;

import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public interface Source {
    /**
     * Source offers the method readImage that inserts new data into the ByteBuffer. Additionally, it may update fields
     * on the Config, such as the frameId. It can also change the condition flag to gracefully shut down the application
     * if the pipe to the device or remote system is broken.
     *
     * @param config Config object from the Main class
     * @param out ByteBuffer containing the (processed) frame
     * @return if a normal execution was possible, similar to a call to condition.
     */
    boolean readImage(final Config config, final ByteBuffer out);
}
