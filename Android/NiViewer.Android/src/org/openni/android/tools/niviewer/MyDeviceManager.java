package org.openni.android.tools.niviewer;

import java.nio.ByteBuffer;
import java.util.List;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.Toast;

public class MyDeviceManager {
	/*
	Installation installation = new Installation();
	Device device;
	VideoStream stream;
	Tee tee;
	Thread teeRunner;
	float[] mHistogram;
	int[] colors;
	private void toast(String message, Context applicationContext) {
    	Toast toast = Toast.makeText(applicationContext,
				message, Toast.LENGTH_SHORT);
		toast.show();
	}
	public void initialize(Context applicationContext) {
		if (null != device) return;
		//if (Installation.canRunRootCommands()) {
		if (installation.execute()) {
        	Log.d("bjoern", "ROOT commands executed.");
		} else {
        	toast("Not running as ROOT.", applicationContext);
        	Log.d("bjoern", "Not running as ROOT.");
		}
		//}
		
		OpenNI.initialize();

        List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
        if (devicesInfo.isEmpty()) {
        	toast("No device is connected!", applicationContext);
        	Log.d("bjoern", "no device is connected");
        } else {
            Log.d("bjoern", "device info: " + devicesInfo.get(0).getUri());
            try {
                device = Device.open(/*devicesInfo.get(0).getUri()*/ /*);
            	toast("Connection to " + device.getDeviceInfo().getName() + " successful!", applicationContext);
                Log.d("bjoern", "device info: " + device.getDeviceInfo().getUri());
            } catch (RuntimeException re) {
            	toast("USB connection failed. +rw missing on /dev/usb/* ?", applicationContext);
            	Log.d("bjoern", "USB error: " + re.getMessage());
            }
            //device.getDeviceInfo();
            
        }        
	}
	public void openStream() {
		if (null != stream) return;
		if (null == device) return;
		try {
	        stream = VideoStream.create(device, SensorType.DEPTH);
	        List<VideoMode> supportedModes = stream.getSensorInfo().getSupportedVideoModes();
	        VideoMode selectedMode = null;
	        for (VideoMode mode : supportedModes) {
	        	Log.d("bjoern", "Video mode: " + mode.getResolutionX() + "x" + mode.getResolutionY() + "x" + mode.getPixelFormat().name() + "@" + mode.getFps() + "fps");
	        	if (mode.getResolutionX() == 320 && mode.getResolutionY() == 240 && mode.getFps() == 30) {
	        		selectedMode = mode;
	        		Log.d("bjoern", "chosen!");
	        	}
	        }
	        stream.setVideoMode(selectedMode);
	        stream.start();
	        /*tee = new Tee(stream);
	        teeRunner = new Thread(new Runnable() {
				@Override
				public void run() {
					while (!teeRunner.isInterrupted()) {
						Log.v("bjoern", "Reading next frame...");
						tee.readNextFrame();
					}
				}
			});
	        teeRunner.start();*//*
		} catch (Exception e) {
			stream = null;
			throw new RuntimeException(e);
		}
	}
	public void closeStream() {
		if (null == stream) return;
        stream.stop();
        VideoStream temp = stream;
        stream = null;
        temp.destroy();
	}
	public Bitmap getImage() {
		if (null == tee) return null;
		VideoFrameRef frame = stream.readFrame();//tee.obtainFrame();
		frame.release();
		frame = stream.readFrame();//tee.obtainFrame();
		frame.release();
		frame = stream.readFrame();//tee.obtainFrame();
		frame.release();
		frame = stream.readFrame();//tee.obtainFrame();
		frame.release();
		frame = stream.readFrame();//tee.obtainFrame();
		if (null == frame) return null;

		//Create a new image bitmap and attach a brand new canvas to it
		Bitmap tempBitmap = Bitmap.createBitmap(frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(tempBitmap);

        // make sure we have enough room
		if (colors == null || colors.length < frame.getWidth() * frame.getHeight()) {
			colors = new int[frame.getWidth() * frame.getHeight()];
		}
		ByteBuffer data = frame.getData();

        switch (frame.getVideoMode().getPixelFormat())
        {
            case DEPTH_1_MM:
            case DEPTH_100_UM:
            case SHIFT_9_2:
            case SHIFT_9_3:
                calcHist(data);
                data.rewind();
                int pos = 0;
                while(data.remaining() > 0) {
                    int depth = (int)data.getShort() & 0xFFFF;
                    short pixel = (short)mHistogram[depth];
                    colors[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8);
                    pos++;
                }
                break;
            case RGB888:
                pos = 0;
                while (data.remaining() > 0) {
                    int red = (int)data.get() & 0xFF;
                    int green = (int)data.get() & 0xFF;
                    int blue = (int)data.get() & 0xFF;
                    colors[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
                    pos++;
                }
                break;
            default:
                // don't know how to draw
        }
		
		//Draw the image bitmap into the canvas
		tempCanvas.drawBitmap(colors, 0, frame.getWidth(), 0, 0,frame.getWidth(), frame.getHeight(), false, null);

		frame.release();
		//tee.releaseFrame(frame);
		
		return tempBitmap;
	}

    private void calcHist(ByteBuffer depthBuffer) {
        // make sure we have enough room
        if (mHistogram == null || mHistogram.length < stream.getMaxPixelValue()) {
            mHistogram = new float[stream.getMaxPixelValue()];
        }
        
        // reset
        for (int i = 0; i < mHistogram.length; ++i)
            mHistogram[i] = 0;

        int points = 0;
        while (depthBuffer.remaining() > 0) {
            int depth = depthBuffer.getShort() & 0xFFFF;
            if (depth != 0) {
                mHistogram[depth]++;
                points++;
            }
        }

        for (int i = 1; i < mHistogram.length; i++) {
            mHistogram[i] += mHistogram[i - 1];
        }

        if (points > 0) {
            for (int i = 1; i < mHistogram.length; i++) {
                mHistogram[i] = (int) (256 * (1.0f - (mHistogram[i] / (float) points)));
            }
        }
    }
		*/
}
