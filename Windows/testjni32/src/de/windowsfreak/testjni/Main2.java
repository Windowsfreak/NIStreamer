package de.windowsfreak.testjni;

import de.windowsfreak.testjni.codec.AbstractCodec;
import de.windowsfreak.testjni.codec.WindowCodec;
import de.windowsfreak.testjni.writer.TCPWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Main2 {

    public static void main(String[] args) throws IOException {
        final ImageReaderFactory sif = ImageReaderFactory.getFactory("Oni640");

        if (sif == null) return;
        final int x = sif.getX();
        final int y = sif.getY();

        final ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        sif.start(640, 480);

        sif.factor(sourceBuffer);

        final ByteBuffer compressedBuffer = ByteBuffer.allocateDirect((x * y) << 1);
        CodecFactory codecFactory = CodecFactory.getFactory("Deflate9");
        if (codecFactory == null) return;
        final AbstractCodec pc = codecFactory.getCodec(x, y);
        if (pc == null) return;

        int compressedLength = pc.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());

        new Thread() {
            public void run() {
                TCPWriter writer = new TCPWriter();
                try {
                    writer.initialize();
                    while (writer.instances.size() < 1) {
                        System.out.println("Waiting for client!");
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                writer.writeImage(x, y, compressedBuffer);

                try {
                    while (!this.isInterrupted() && writer.instances.size() > 0) {
                        sourceBuffer.clear();
                        sif.factor(sourceBuffer);
                        int compressedLength = pc.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                        writer.writeImage(x, y, compressedBuffer);
                    }
                    System.out.println("Nothing more to do!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sif.stop();
                try {
                    writer.shutDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.exit(0);
            }
        }.start();
    }
}

