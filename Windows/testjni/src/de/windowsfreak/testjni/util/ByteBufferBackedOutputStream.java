package de.windowsfreak.testjni.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedOutputStream extends OutputStream {
    private ByteBuffer buf;
    public ByteBufferBackedOutputStream( ByteBuffer buf){
        this.buf = buf;
    }
    public synchronized void write(int b) throws IOException {
        buf.put((byte) b);
    }

    public synchronized void write(byte[] bytes, int off, int len) throws IOException {
        buf.put(bytes, off, len);
    }

}
