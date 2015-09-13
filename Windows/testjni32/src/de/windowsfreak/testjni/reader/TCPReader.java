package de.windowsfreak.testjni.reader;

import org.openni.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class TCPReader implements ImageReader {
    SocketChannel socketChannel;
    public void initialize() throws IOException {
        if (socketChannel != null) {
            try {
                shutDown();
            } catch (Exception e) {
                new Exception("Could not close previous SocketChannel", e).printStackTrace();
            }
        }
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
    }

    public void start(int x, int y) {
        // Sockets are bidirectional. Let's send some messages!
        synchronized(header) {
            header.clear();
            header.put((byte) 1); // open stream
            header.put((byte) 1); // depth
            header.put((byte) 30); // fps
            header.put((byte) 1); // mode (1 = mm, 2 = 100um)
            header.putShort((short) x); // width
            header.putShort((short) y); // height
            header.flip();
            try {
                socketChannel.write(header);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        // Sockets are bidirectional. Let's send some messages!
        synchronized(header) {
            header.clear();
            header.putLong(0L); // close stream, and send a total of 8 bytes!
            header.flip();
            try {
                socketChannel.write(header);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutDown() throws IOException {
        try {
            socketChannel.close();
        } finally {
            // we want to know that we have to set up a new connection.
            socketChannel = null;
        }
    }

    final int headerSize = 8;
    short x, y;
    ByteBuffer header = ByteBuffer.allocateDirect(headerSize);
    @Override
    public boolean readImage(int x, int y, ByteBuffer out) {
        if (socketChannel == null) return false;
        int headerBytes = 0, imageSize = 0, imageBytes = 0, bytesRead = 0;
        try {
            // We read in chunks. Limit the ByteBuffer to not read too much, but also retry reading until the ByteBuffer is full.
            // If we read less than 1 byte, we assume that the stream is closed.
            synchronized(header) {
                header.clear();
                while (headerBytes < headerSize) {
                    bytesRead = socketChannel.read(header);
                    headerBytes += bytesRead;
                    if (bytesRead < 1) return false;
                }
                header.flip();
                imageSize = header.getInt();
                if (imageSize == 1) {
                    System.out.println("I assume the remote device is using a different format :(");
                    return false;
                }
                this.x = header.getShort();
                this.y = header.getShort();
            }
            synchronized(out) {
                out.clear();
                out.limit(imageSize);
                while (imageBytes < imageSize) {
                    bytesRead = socketChannel.read(out);
                    imageBytes += bytesRead;
                    if (bytesRead < 1) return false;
                }
                out.flip();
            }
            //out.limit(imageSize);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
