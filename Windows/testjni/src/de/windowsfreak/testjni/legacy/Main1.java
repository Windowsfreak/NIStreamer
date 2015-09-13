package de.windowsfreak.testjni.legacy;

import java.io.IOException;

public class Main1 {

    public static void main(String[] args) throws IOException {
/*
        */
/*
                        OniStreamSource osr = new OniStreamSource();
                for (int i = 5; i > 0; i--) {
                    System.out.println("Capturing frame in... " + i + " seconds");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Frame captured.");

         *//*

        SourceFactory sif = SourceFactory.getFactory("Kinect640");

        if (sif == null) return;
        int x = sif.getX();
        int y = sif.getY();

        ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        sif.start(640, 480);
        */
/*try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*//*

        sif.factor(sourceBuffer);

        ByteBuffer compressedBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        CodecFactory codecFactory = CodecFactory.getFactory("Deflate9");
        if (codecFactory == null) return;
        AbstractCodec codec = codecFactory.getCodec(x, y);
        if (codec == null) return;

        int compressedLength = codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
        // Arrays.fill(sourceBuffer.array(), (byte) 0);

        codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedLength);

        WindowSink wc = new WindowSink();
        wc.setDimensions(x, y);
        wc.encode(null, sourceBuffer, 0);
        wc.setTitle("Image viewer");

        long u = System.currentTimeMillis();
        int i = 0;
        while (wc.window.isVisible()) {
            sif.factor(sourceBuffer);
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

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ByteBuffer destBuffer = ByteBuffer.allocateDirect((x * y) << 1);

        for (CodecFactory cf : CodecFactory.cf) {
            System.out.println("Performance testing " + cf.getName());
            AbstractCodec c = cf.getCodec(x, y);
            u = System.currentTimeMillis();
            for (i = 0; i < 10; i++) {
                compressedLength = c.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
            }
            long v = System.currentTimeMillis();
            System.out.println("Execution time in ms, encoding 10 rounds: " + (v - u));
            for (i = 0; i < 10; i++) {
                c.decode(destBuffer, destBuffer.capacity(), compressedBuffer, compressedLength);
            }
            u = System.currentTimeMillis();
            System.out.println("Execution time in ms, decoding 10 rounds: " + (u - v));
            System.out.println("Compression: " + compressedLength + " / " + sourceBuffer.capacity() + " (" + ((compressedLength * 100 / (sourceBuffer.capacity() == 0 ? 1 : sourceBuffer.capacity()))) + "%)");
        }
*/
    }
}

