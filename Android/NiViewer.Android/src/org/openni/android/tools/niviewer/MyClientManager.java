package org.openni.android.tools.niviewer;

import android.util.Log;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * Created by lazer_000 on 06.05.2015.
 */
public class MyClientManager extends Thread {
    private MyNetworkManager nm;
    private BufferedReader in;
    private OutputStream out;
    private Socket socket;
    private WritableByteChannel channel;
    private int lastFrameIndex = Integer.MIN_VALUE;
    public MyClientManager(MyNetworkManager nm, Socket socket) {
        this.nm = nm;
        this.socket = socket;
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();
            channel = Channels.newChannel(out);
        } catch (IOException e) {
            Log.d("NETWORK", "Could not create socket stream: " + e.getMessage());
            return;
        }
        start();
    }
    public void run() {
        while (!this.isInterrupted()) {
            try {
                while (this.sendFrame()) {
                    Log.d("NETWORK", "Frame sent: " + lastFrameIndex);
                }
            } catch (IOException e) {
                Log.d("NETWORK", "Sending frame failed: " + e.getMessage());
                return;
            }
            try {
                synchronized(nm.waitLock) {
                    nm.waitLock.wait();
                }
            } catch (InterruptedException e) {
                Log.d("NETWORK", "Thread interrupted: " + e.getMessage());
                return;
            }
        }
    }
    private int writeBuffer(ByteBuffer buffer, OutputStream stream) throws IOException {
        return channel.write(buffer);
    }
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    private ByteBuffer frameHeader = ByteBuffer.allocateDirect(20);
    public boolean sendFrame() throws IOException {
        VideoFrameRef frame = nm.frame;
        if (frame == null) return false;
        int newFrameIndex = frame.getFrameIndex();
        if (newFrameIndex <= lastFrameIndex) return false;
        lastFrameIndex = newFrameIndex;
        sendMetadata(frame);
        frame.getData().rewind();
        frameHeader.rewind();
        frameHeader
                .putInt(1) /* reserved */
                .putInt(frame.getFrameIndex())
                .putLong(frame.getTimestamp())
                .putInt(frame.getData().remaining());
        byte[] p = new byte[20];
        frameHeader.flip();
        frameHeader.get(p);
        Log.d("NETWORK", "Bytes sent: 0x" + bytesToHex(p));
        frameHeader.flip();
        writeBuffer(frameHeader, out);
        writeBuffer(frame.getData(), out);
        return true;
    }
    private VideoMode mode;
    private ByteBuffer modeHeader = ByteBuffer.allocateDirect(32);
    public boolean sendMetadata(VideoFrameRef frame) throws IOException {
        if (frame == null || frame.getVideoMode().equals(mode)) return false;
        mode = frame.getVideoMode();
        modeHeader.rewind();
        modeHeader
                .putInt(2) /* reserved */
                .putInt(frame.getStrideInBytes())
                .putInt(frame.getWidth())
                .putInt(frame.getHeight())
                .putInt(mode.getResolutionX())
                .putInt(mode.getResolutionY())
                .putInt(mode.getFps())
                .putInt(mode.getPixelFormat().toNative());
        modeHeader.flip();
        byte[] p = new byte[32];
        modeHeader.get(p);
        Log.d("NETWORK", "Bytes sent: 0x" + bytesToHex(p));
        modeHeader.flip();
        writeBuffer(modeHeader, out);
        return true;
    }
}
