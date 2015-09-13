package de.windowsfreak.testjni.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedInputStream extends InputStream {

    private ByteBuffer buf;
    public ByteBufferBackedInputStream( ByteBuffer buf){
        this.buf = buf;
    }
    public synchronized int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get();
    }
    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len > 0 ? len : -1;
    }
    public int available() throws IOException {
        return buf.remaining();
    }
}
