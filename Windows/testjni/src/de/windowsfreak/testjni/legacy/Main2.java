package de.windowsfreak.testjni.legacy;

import java.io.IOException;

public class Main2 {

    public static void main(String[] args) throws IOException {
        /*
        final SourceFactory sif = SourceFactory.getFactory("Oni640");

        if (sif == null) return;
        final int x = sif.getX();
        final int y = sif.getY();

        final ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        sif.start(640, 480);

        sif.factor(sourceBuffer);

        final ByteBuffer compressedBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        CodecFactory codecFactory = CodecFactory.getFactory("Deflate9");
        if (codecFactory == null) return;
        final AbstractCodec codec = codecFactory.getCodec(x, y);
        if (codec == null) return;

        int compressedLength = codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());

        new Thread() {
            public void run() {
                TCPSink sink = new TCPSink();
                try {
                    sink.initialize();
                    while (sink.instances.size() < 1) {
                        System.out.println("Waiting for client!");
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                sink.writeImage(x, y, compressedBuffer);

                try {
                    while (!this.isInterrupted() && sink.instances.size() > 0) {
                        sourceBuffer.clear();
                        sif.factor(sourceBuffer);
                        int compressedLength = codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                        sink.writeImage(x, y, compressedBuffer);
                    }
                    System.out.println("Nothing more to do!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sif.stop();
                try {
                    sink.shutDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.exit(0);
            }
        }.start();
        */
    }
}

