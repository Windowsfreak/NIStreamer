package de.windowsfreak.testjni.legacy;

import java.io.IOException;

public class Main3 {

    public static void main(String[] args) throws IOException {
        /*
        SourceFactory sif = SourceFactory.getFactory("TCP640");

        if (sif == null) return;
        int x = sif.getX();
        int y = sif.getY();

        ByteBuffer compressedBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        sif.start(640, 480);

        sif.factor(compressedBuffer);

        ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        CodecFactory codecFactory = CodecFactory.getFactory("Deflate9");
        if (codecFactory == null) return;
        AbstractCodec codec = codecFactory.getCodec(x, y);
        if (codec == null) return;

        codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());

        WindowSink wc = new WindowSink();
        wc.setDimensions(x, y);
        wc.encode(null, sourceBuffer, 0);
        wc.setTitle("Image viewer");

        long u = System.currentTimeMillis();
        int i = 0;
        while (wc.window.isVisible()) {

            sif.factor(compressedBuffer);

            codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());

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
        */
    }
}

