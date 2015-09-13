package de.windowsfreak.testjni.reader;

import org.openni.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class OniStreamReader implements ImageReader {
    private Device device;
    private VideoStream videoStream;
    SensorType type = SensorType.DEPTH;
    private ArrayList<VideoMode> supportedModes = new ArrayList<VideoMode>();

    public void initialize() {
        System.loadLibrary("OpenNI2");
        System.loadLibrary("OpenNI2.jni");
        OpenNI.initialize();

        String uri;

        List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
        if (devicesInfo.isEmpty()) {
            new Exception("Error! There is no connected device.").printStackTrace();
            return;
        }
        uri = devicesInfo.get(0).getUri();

        device = Device.open(uri);
    }

    public void start(int x, int y) {
        videoStream = VideoStream.create(device, type);
        List<VideoMode> modes = videoStream.getSensorInfo().getSupportedVideoModes();
        supportedModes = new ArrayList<VideoMode>();

        // now only keep the ones that our application supports
        for (VideoMode mode : modes) {
            if (mode.getResolutionX() != x || mode.getResolutionY() != y) continue;
            if (mode.getFps() != 30) continue;
            switch (mode.getPixelFormat()) {
                case DEPTH_1_MM:
                case DEPTH_100_UM:
                case GRAY16:
                    supportedModes.add(mode);
                    break;
            }
        }

        //VideoMode mode = supportedModes.get(0); // 1mm
        VideoMode mode = supportedModes.get(supportedModes.size() - 1); // 100um
        videoStream.setVideoMode(mode);
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
}
