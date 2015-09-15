package de.windowsfreak.testjni.writer;

import de.windowsfreak.testjni.Config;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 24.07.2015.
 */
public interface Sink {
    void initialize(Config config) throws IOException;

    /**
     * The writeImage method receives an additional integer, which is the frameId of the frame that has been obtained by the CompressionWorker.
     * @param config Config object from the Main class
     * @param in ByteBuffer containing the (processed) frame
     * @param frameId frame Id
     * @return if a normal execution was possible, similar to a call to condition.
     */
    boolean writeImage(Config config, ByteBuffer in, int frameId);

    /**
     * Finally, the condition flag helps the CompressionWorker understand why no new depth frame can be obtained or sent.
     * If vital components fail, or the user wants to stop the processing, the flag is changed to let the workers interrupt naturally.
     *
     * Condition is allowed to block, e.g. to wait for a stream to initialize, and may even return an InterruptedException
     * @param config Config object from the Main class
     * @return true, if processing may continue. false , to shut down the application if the pipe to the device or remote system is broken.
     * @throws InterruptedException
     */
    boolean condition(Config config) throws InterruptedException;
    void shutDown() throws IOException;
}