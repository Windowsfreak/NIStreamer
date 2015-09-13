package de.windowsfreak.testjni;

import de.windowsfreak.testjni.codec.AbstractCodec;
import de.windowsfreak.testjni.codec.WindowCodec;
import de.windowsfreak.testjni.writer.TCPWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Main4 {
    public static void main(final Config config) throws IOException {
        final TCPWriter writer;
        if (config.sinkControlled) {
            writer = new TCPWriter();
            writer.initialize();
            ByteBuffer header = null;
            while (header == null) {
                header = writer.receiveCommandHeader();
                if (header == null) {
                    System.out.println("Waiting for header!");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (header.get() == 1) {
                config.depth = header.get();
                config.fps = header.get();
                config.mode = header.get();
                config.x = header.getShort();
                config.y = header.getShort();
            }
        } else if (config.sink.equals("tcp")) {
            writer = new TCPWriter();
            writer.initialize();
        } else {
            writer = null;
        }
        final ImageReaderFactory sif = ImageReaderFactory.getFactory(config.source);

        if (sif == null) return;
        sif.start(config);

        if (!config.sinkControlled) {
            config.x = sif.getX();
            config.y = sif.getY();
        }
        final short x = config.x;
        final short y = config.y;

        final ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((int)x * (int)y << 1);

        final ByteBuffer compressedBuffer;
        final AbstractCodec pc;
        if (config.compression != null) {
            compressedBuffer = ByteBuffer.allocateDirect((int)x * (int)y << 1);
            CodecFactory codecFactory = CodecFactory.getFactory(config.compression);
            if (codecFactory == null) return;
            pc = codecFactory.getCodec(x, y);
            if (pc == null) return;
        } else {
            compressedBuffer = null;
            pc = null;
        }

        if (config.sink.equals("window")) {
            WindowCodec wc = new WindowCodec();
            wc.setDimensions(x, y);
            wc.encode(null, null, 0);
            wc.setTitle("Image viewer");

            long u = System.currentTimeMillis();
            int i = 0, compressedLength;
            long c = 0, z = 0;
            while (wc.window.isVisible()) {

                if (config.decompress && !config.compress) {
                    sif.factor(compressedBuffer);
                } else {
                    sif.factor(sourceBuffer);
                }

                if (pc != null) {
                    if (config.compress) {
                        pc.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                    }
                    if (config.decompress) {
                        pc.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
                    }
                }

                if (config.compress && !config.decompress) {
                    wc.window.setSourceImage(compressedBuffer, x, y);
                } else {
                    wc.window.setSourceImage(sourceBuffer, x, y);
                }
                if (compressedBuffer != null) c += compressedBuffer.limit();
                z += sourceBuffer.limit();
                long v = System.currentTimeMillis();
                i++;
                if (i == 10) {
                    wc.window.setTitle("Video stream: " + (i * 1000 / (v - u)) + "fps, " + (c / i) + " bytes (" + (c * 100 / z) + "%), " + ((i * 1000 / (v - u)) * (c / i)) + "bps");
                    u = v;
                    i = 0;
                    c = 0;
                    z = 0;
                }
            }
            sif.stop();
        } else if (config.sink.equals("tcp")) {
            if (writer == null) {
                System.out.println("Writer not initialized :-(");
                return;
            }

            new Thread() {
                public void run() {
                    try {
                        while (writer.instances.size() < 1) {
                            System.out.println("Waiting for first client!");
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        while (!this.isInterrupted() && writer.instances.size() > 0) {

                            if (config.decompress && !config.compress) {
                                sif.factor(compressedBuffer);
                            } else {
                                sif.factor(sourceBuffer);
                            }

                            if (pc != null) {
                                if (config.compress) {
                                    pc.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                                }
                                if (config.decompress) {
                                    pc.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
                                }
                            }

                            if (config.compress && !config.decompress) {
                                writer.writeImage(x, y, compressedBuffer);
                            } else {
                                writer.writeImage(x, y, sourceBuffer);
                            }
                        }
                        System.out.println("No more clients connected!");
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
}

