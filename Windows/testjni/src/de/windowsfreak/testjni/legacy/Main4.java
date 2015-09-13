package de.windowsfreak.testjni.legacy;

import de.windowsfreak.testjni.Config;

import java.io.IOException;

public class Main4 {
    public static void main(final Config config) throws IOException {
        /*
        final TCPSink sink;
        if (config.sinkControlled) {
            sink = new TCPSink();
            sink.initialize();
            ByteBuffer header = null;
            while (header == null) {
                header = sink.receiveCommandHeader();
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
            sink = new TCPSink();
            sink.initialize();
        } else {
            sink = null;
        }
        final SourceFactory sif = SourceFactory.getFactory(config.source);

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
        final AbstractCodec codec;
        if (config.compression != null) {
            compressedBuffer = ByteBuffer.allocateDirect((int)x * (int)y << 1);
            CodecFactory codecFactory = CodecFactory.getFactory(config.compression);
            if (codecFactory == null) return;
            codec = codecFactory.getCodec(x, y);
            if (codec == null) return;
        } else {
            compressedBuffer = null;
            codec = null;
        }

        if (config.sink.equals("window")) {
            WindowSink wc = new WindowSink();
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

                if (codec != null) {
                    if (config.compress) {
                        codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                    }
                    if (config.decompress) {
                        codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
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
            if (sink == null) {
                System.out.println("Sink not initialized :-(");
                return;
            }

            new Thread() {
                public void run() {
                    try {
                        while (sink.instances.size() < 1) {
                            System.out.println("Waiting for first client!");
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        while (!this.isInterrupted() && sink.instances.size() > 0) {

                            if (config.decompress && !config.compress) {
                                sif.factor(compressedBuffer);
                            } else {
                                sif.factor(sourceBuffer);
                            }

                            if (codec != null) {
                                if (config.compress) {
                                    codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                                }
                                if (config.decompress) {
                                    codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
                                }
                            }

                            if (config.compress && !config.decompress) {
                                sink.writeImage(x, y, compressedBuffer);
                            } else {
                                sink.writeImage(x, y, sourceBuffer);
                            }
                        }
                        System.out.println("No more clients connected!");
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
        }
        */
    }
}

