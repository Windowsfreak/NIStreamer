package de.windowsfreak.testjni.writer;

import de.windowsfreak.testjni.Config;
import de.windowsfreak.testjni.Window;

import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class WindowSink implements Sink {
    public Window window;
    public void initialize(Config config) {
        window = Window.makeWindow("Image");
        window.setSourceImage(null, config);
        window.pack();
        window.setVisible(true);
    }
    public boolean writeImage(final Config config, final ByteBuffer in, final int frameId) {
        window.setSourceImage(in, config);
        return true;
    }
    public boolean condition(Config config) {
        return window.isVisible();
    }
    public void shutDown() {}

    public void setTitle(String title) {
        window.setTitle(title);
    }
}
