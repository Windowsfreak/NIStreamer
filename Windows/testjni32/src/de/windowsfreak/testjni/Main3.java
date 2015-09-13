package de.windowsfreak.testjni;

import de.windowsfreak.testjni.codec.AbstractCodec;
import de.windowsfreak.testjni.codec.WindowCodec;
import de.windowsfreak.testjni.writer.TCPWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Main3 {

    public static void main(String[] args) throws IOException {
        ImageReaderFactory sif = ImageReaderFactory.getFactory("TCP640");

        if (sif == null) return;
        int x = sif.getX();
        int y = sif.getY();

        ByteBuffer compressedBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        sif.start(640, 480);

        sif.factor(compressedBuffer);

        ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        CodecFactory codecFactory = CodecFactory.getFactory("Deflate9");
        if (codecFactory == null) return;
        AbstractCodec pc = codecFactory.getCodec(x, y);
        if (pc == null) return;

        pc.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());

        WindowCodec wc = new WindowCodec();
        wc.setDimensions(x, y);
        wc.encode(null, sourceBuffer, 0);
        wc.setTitle("Image viewer");

        long u = System.currentTimeMillis();
        int i = 0;
        while (wc.window.isVisible()) {

            sif.factor(compressedBuffer);

            pc.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());

            wc.window.setSourceImage(sourceBuffer, x, y);
            long v = System.currentTimeMillis();
            i++;
            if (i == 10) {
                wc.window.setTitle("Video stream: " + (i * 1000 / (v - u)) + "fps");
                u = v;
                i = 0;
            }
        }
        sif.stop();
    }
}

