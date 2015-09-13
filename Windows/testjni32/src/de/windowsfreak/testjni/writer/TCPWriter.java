package de.windowsfreak.testjni.writer;

import de.windowsfreak.testjni.reader.ImageReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class TCPWriter {
    ServerSocketChannel serverSocketChannel;
    boolean running = false;
    Thread thread;
    public LinkedList<TCPWriterInstance> instances = new LinkedList<TCPWriterInstance>();
    public Selector selector;
    Object waitRead = new Object();
    class TCPWriterInstance {
        SocketChannel socketChannel;
        public Selector selector;
        TCPWriterInstance(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.selector = Selector.open();
        }
    }
    public void initialize() throws IOException {
        selector = Selector.open();
        if (serverSocketChannel != null) {
            try {
                shutDown();
            } catch (Exception e) {
                new Exception("Could not close previous ServerSocketChannel", e).printStackTrace();
            }
        }
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9999));
        new Thread() {
            public void run() {
                while(!(this.isInterrupted()))
                {
                    try {
                        SocketChannel sc = TCPWriter.this.serverSocketChannel.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                        TCPWriterInstance instance = new TCPWriterInstance(sc);
                        sc.register(instance.selector, SelectionKey.OP_WRITE);
                        instances.add(instance);
                    } catch (IOException e) {
                        System.out.println("TCP Server stopped!"); //e.printStackTrace();
                        //e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
    }
    public ByteBuffer receiveCommandHeader() {
        for (TCPWriterInstance instance : instances) {
            try {
                selector.select();
                Set readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        synchronized(header) {
                            header.clear();
                            int bytesRead = 0, headerBytes = 0;
                            while (headerBytes < headerSize) {
                                bytesRead = client.read(header);
                                headerBytes += bytesRead;
                                if (bytesRead < 1) break;
                            }
                            if (headerBytes < 8) continue;
                            header.flip();
                            return header;
                        }
                    }
                }
            } catch (Exception e) {
                new Exception("Could not receive command headers", e).printStackTrace();
            }
        }
        return null;
    }

    public void shutDown() throws IOException {
        for (TCPWriterInstance instance : instances) {
            try {
                instance.socketChannel.close();
            } catch (Exception e) {
                new Exception("Could not close SocketChannel", e).printStackTrace();
            }
        }
        instances.clear();
        try {
            serverSocketChannel.close();
        } finally {
            serverSocketChannel = null;
        }
    }

    final int headerSize = 8;
    ByteBuffer header = ByteBuffer.allocateDirect(headerSize);
    public boolean writeImage(int x, int y, ByteBuffer in) {
        boolean errors = false;
        TCPWriterInstance erroneous = null;
        synchronized(in) {
            in.rewind();
            int size = in.remaining();
            synchronized(header) {
                header.clear();
                header.putInt(size);
                header.putShort((short) x);
                header.putShort((short) y);
                header.flip();
                int totalBytes = header.remaining() + size;
                // not thread safe !!!
                for (TCPWriterInstance instance : instances) {
                    try {
                        SocketChannel channel = instance.socketChannel;
                        header.rewind();
                        in.rewind();
                        int totalWritten = 0;
                        while (totalWritten < totalBytes) {
                            instance.selector.select();
                            Set readyKeys = instance.selector.selectedKeys();
                            Iterator iterator = readyKeys.iterator();
                            while (iterator.hasNext()) {
                                SelectionKey key = (SelectionKey) iterator.next();
                                iterator.remove();
                                if (key.isWritable()) {
                                    SocketChannel client = (SocketChannel) key.channel();
                                    int bytesWritten = (int) client.write(new ByteBuffer[]{header, in});
                                    totalWritten += bytesWritten;
                                    if (bytesWritten < 0) throw new Exception("Could not complete writing!");
                                }
                            }
                        }
                    } catch (Exception e) {
                        new Exception("Could not write to SocketChannel", e).printStackTrace();
                        erroneous = instance;
                        errors = true;
                    }
                }
            }
        }
        if (errors) {
            instances.remove(erroneous);
            instances.removeIf(new Predicate<TCPWriterInstance>() {
                @Override
                public boolean test(TCPWriterInstance instance) {
                    return !instance.socketChannel.isOpen();
                }
            });
        }
        return !errors;
    }
}
