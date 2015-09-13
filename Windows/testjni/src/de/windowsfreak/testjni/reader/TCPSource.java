package de.windowsfreak.testjni.reader;

import de.windowsfreak.testjni.Config;
import de.windowsfreak.testjni.Loader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class TCPSource implements Source {
    SocketChannel socketChannel;
    public void initialize(final Config config) throws IOException {
        if (socketChannel != null) {
            try {
                shutDown();
            } catch (Exception e) {
                new Exception("Could not close previous SocketChannel", e).printStackTrace();
            }
        }
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(config.sourceUrl, config.sourcePort));
    }

    public void start(final Config config) {
        // Sockets are bidirectional. Let's send some messages!
        synchronized(header) {
            header.clear();
            header.put((byte) 1); // open stream
            header.put(config.depth); // depth
            header.put(config.fps); // fps
            header.put(config.mode); // mode (1 = mm, 2 = 100um)
            header.putShort(config.x); // width
            header.putShort(config.y); // height
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

    /*
    public boolean writeConfig(final Config config) {
        boolean errors = false;
        synchronized(header) {
            header.clear();
            header.putInt(0); // magic value for config
            header.putShort(config.x);
            header.putShort(config.y);
            header.flip();
            errors |= writeHeader(header);
            header.clear();
            header.putFloat(config.fovX);
            header.putFloat(config.fovY);
            header.flip();
            errors |= writeHeader(header);
            header.clear();
            header.put(config.fps);
            header.put(config.mode);
            header.put(config.depth);
            header.put((byte) 0); // unused
            header.putInt(0); // unused
            header.flip();
            errors |= writeHeader(header);
        }
        return !errors;
    }
    */
    private boolean readHeader(ByteBuffer header) throws IOException {
        int headerBytes = 0, bytesRead;
        header.clear();
        while (headerBytes < headerSize) {
            bytesRead = socketChannel.read(header);
            headerBytes += bytesRead;
            if (bytesRead < 1) return false;
        }
        header.flip();
        return true;
    }

    final int headerSize = 8;
    final ByteBuffer header = ByteBuffer.allocateDirect(headerSize);
    @Override
    public boolean readImage(final Config config, final ByteBuffer out) {
        if (socketChannel == null) return false;
        int headerBytes = 0, imageSize = 0, imageBytes = 0, bytesRead = 0;
        try {
            // We read in chunks. Limit the ByteBuffer to not read too much, but also retry reading until the ByteBuffer is full.
            // If we read less than 1 byte, we assume that the stream is closed.
            synchronized(header) {
                readHeader(header);
                imageSize = header.getInt();
                while (imageSize == 0) {
                    System.out.println("Reading header information");
                    config.x = header.getShort();
                    config.y = header.getShort();
                    readHeader(header);
                    config.fovX = header.getFloat();
                    config.fovY = header.getFloat();
                    readHeader(header);
                    config.fps = header.get();
                    config.mode = header.get();
                    config.depth = header.get();
                    readHeader(header);
                    imageSize = header.getInt();
                    System.out.println("Hey, I am connected to a " + config.x + "x" + config.y + "@" + config.fps + " stream with " + config.fovX + "x" + config.fovY + " viewing range!");
                }
                if (imageSize == 1) {
                    System.out.println("I assume the remote device is using a different format :(");
                    return false;
                }
                config.frameId = header.getInt();
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
            Loader.stop();
            return false;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("(Potentially compressed) image size " + imageSize + " did not fit into output buffer " + out.capacity() + "!", e);
        }
        return true;
    }
}
