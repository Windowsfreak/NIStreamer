package de.windowsfreak.testjni.writer;

import de.windowsfreak.testjni.Config;
import de.windowsfreak.testjni.util.WorldCalculator;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class WorldCalculatorSink extends WorldCalculator implements Sink {
    public final float[][] image;

    public WorldCalculatorSink(int resolutionX, int resolutionY, float horizontalFov, float verticalFov, int mode) {
        super(resolutionX, resolutionY, horizontalFov, verticalFov, mode);
        image = new float[resolutionX * resolutionY][];
    }

    public WorldCalculatorSink(Config config) {
        this(config.x, config.y, config.fovX, config.fovY, config.mode);
    }

    public void initialize(Config config) {
    }
    public boolean writeImage(final Config config, final ByteBuffer in, final int frameId) {
        //if (image == null) image = new float[config.x * config.y][];
        getPointCloud(in, image);
        return true;
    }

    @Override
    public boolean condition(Config config) throws InterruptedException {
        return true;
    }

    @Override
    public void shutDown() throws IOException {

    }
}
