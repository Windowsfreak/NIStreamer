package de.windowsfreak.testjni.reader;

import de.windowsfreak.testjni.Config;
import org.openni.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class OniFileSource implements Source {
    private Device device;
    private VideoStream videoStream;
    private PlaybackControl playbackControl;
    SensorType type = SensorType.DEPTH;
    private ArrayList<VideoMode> supportedModes = new ArrayList<VideoMode>();

    public void initialize(final Config config) {
        System.loadLibrary("OpenNI2");
        System.loadLibrary("OpenNI2.jni");
        OpenNI.initialize();

        device = Device.open(config.sourceUrl);
        playbackControl = device.getPlaybackControl();
        playbackControl.setSpeed(-1);
        playbackControl.setRepeatEnabled(true);
    }

    public void seek(final int frame) {
        playbackControl.seek(videoStream, frame);
    }

    public void start(final Config config) {
        switch (config.depth) {
            case 1: type = SensorType.DEPTH; break;
            case 2: type = SensorType.COLOR; break;
            case 3: type = SensorType.IR; break;
        }
        videoStream = VideoStream.create(device, type);
        List<VideoMode> modes = videoStream.getSensorInfo().getSupportedVideoModes();
        supportedModes = new ArrayList<VideoMode>();

        // now only keep the ones that our application supports
        for (VideoMode mode : modes) {
            if (mode.getResolutionX() != config.x || mode.getResolutionY() != config.y) continue;
            switch (mode.getPixelFormat()) {
                case DEPTH_1_MM:
                    if (config.mode == 1) supportedModes.add(mode);
                    break;
                case DEPTH_100_UM:
                    if (config.mode == 2) supportedModes.add(mode);
                    break;
                case GRAY16:
                    if (config.mode == 3) supportedModes.add(mode);
                    break;
            }
        }

        VideoMode mode = supportedModes.get(0);
        if (mode == null) System.out.println("Hey, I could not find an appropriate stream!");
        //videoStream.setVideoMode(mode);
        videoStream.start();

        config.fovX = videoStream.getHorizontalFieldOfView();
        config.fovY = videoStream.getVerticalFieldOfView();
        config.x = (short)videoStream.getVideoMode().getResolutionX();
        config.y = (short)videoStream.getVideoMode().getResolutionY();
        config.fps = (byte)videoStream.getVideoMode().getFps();
        System.out.println("Hey, I am reading a " + config.x + "x" + config.y + "@" + config.fps + " recording with " + config.fovX + "x" + config.fovY + " viewing range!");
    }

    public void stop() {
        if (videoStream != null) {
            videoStream.stop();
            videoStream.destroy();
            videoStream = null;
        }
    }

    public void shutDown() {
        try {
            device.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        OpenNI.shutdown();
    }

    @Override
    public boolean readImage(final Config config, final ByteBuffer out) {
        if (videoStream == null) return false;
        VideoFrameRef lastFrame = videoStream.readFrame();
        config.frameId = lastFrame.getFrameIndex();
        synchronized(out) {
            out.clear();
            out.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(lastFrame.getData().order(ByteOrder.BIG_ENDIAN).asShortBuffer());
            out.limit(lastFrame.getData().limit());
        }
        lastFrame.release();
        return true;
    }
}
