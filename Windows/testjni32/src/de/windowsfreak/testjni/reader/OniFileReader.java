package de.windowsfreak.testjni.reader;

import org.openni.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class OniFileReader implements ImageReader {
    private String fileName;

    private Device device;
    private VideoStream videoStream;
    private PlaybackControl playbackControl;
    SensorType type = SensorType.DEPTH;
    private ArrayList<VideoMode> supportedModes = new ArrayList<VideoMode>();

    public void initialize() {
        System.loadLibrary("OpenNI2");
        System.loadLibrary("OpenNI2.jni");
        OpenNI.initialize();

        device = Device.open(fileName);
        playbackControl = device.getPlaybackControl();
        playbackControl.setSpeed(-1);
        playbackControl.setRepeatEnabled(true);
    }

    public void seek(int frame) {
        playbackControl.seek(videoStream, frame);
    }

    public void start(int x, int y) {
        videoStream = VideoStream.create(device, type);
        List<VideoMode> modes = videoStream.getSensorInfo().getSupportedVideoModes();
        supportedModes = new ArrayList<VideoMode>();

        // now only keep the ones that our application supports
        for (VideoMode mode : modes) {
            if (mode.getResolutionX() != x || mode.getResolutionY() != y) continue;
            switch (mode.getPixelFormat()) {
                case DEPTH_1_MM:
                case DEPTH_100_UM:
                case GRAY16:
                    supportedModes.add(mode);
                    break;
            }
        }

        VideoMode mode = supportedModes.get(0);
        //videoStream.setVideoMode(mode);
        videoStream.start();

    }

    public void stop() {
        if (videoStream != null) {
            videoStream.stop();
            videoStream.destroy();
            videoStream = null;
        }
    }

    public void shutDown() {
        device.close();
        OpenNI.shutdown();
    }

    @Override
    public boolean readImage(int x, int y, ByteBuffer out) {
        if (videoStream == null) return false;
        VideoFrameRef lastFrame = videoStream.readFrame();
        synchronized(out) {
            out.clear();
            out.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(lastFrame.getData().order(ByteOrder.BIG_ENDIAN).asShortBuffer());
            out.limit(lastFrame.getData().limit());
        }
        lastFrame.release();
        return true;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
