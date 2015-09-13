package de.windowsfreak.testjni;

import de.windowsfreak.testjni.writer.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public abstract class SinkFactory {
    public static List<SinkFactory> cf = new LinkedList<SinkFactory>();
    static {
        cf.add(new SinkFactory("tcp") {
            @Override
            public Sink getSink(Config config) throws IOException {
                final Sink sink;
                if (config.sinkControlled) {
                    sink = new TCPSink();
                    sink.initialize(config);
                    ByteBuffer header = null;
                    while (header == null && config.condition) {
                        header = ((TCPSink)sink).receiveCommandHeader();
                        if (header == null) {
                            if (config.debug) System.out.println("Waiting for header!");
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println("Header received.");
                    if (header != null && header.get() == 1) {
                        config.depth = header.get();
                        config.fps = header.get();
                        config.mode = header.get();
                        config.x = header.getShort();
                        config.y = header.getShort();
                        System.out.println("Client requests " + config.x + "x" + config.y + "@" + config.fps + "fps.");
                    }
                } else {
                    sink = new TCPSink();
                    sink.initialize(config);
                }
                return sink;
            }
        });
        cf.add(new SinkFactory("window") {
            @Override
            public Sink getSink(Config config) throws IOException {
                final WindowSink sink;
                sink = new WindowSink();
                sink.initialize(config);
                sink.setTitle("Image viewer");
                return sink;
            }
        });
        cf.add(new SinkFactory("world") {
            @Override
            public Sink getSink(Config config) {
                return new WorldCalculatorSink(config);
            }
        });
    }

    public static SinkFactory getFactory(String factoryName) {
        for (SinkFactory f : cf) {
            if (f.s.equals(factoryName)) {
                return f;
            }
        }
        new Exception("Warning! This factory was not found.").printStackTrace();
        return null;
    }

    protected SinkFactory(String s) {
        this.s = s;
    }

    protected final String s;

    public String getName() { return s; }

    public abstract Sink getSink(Config config) throws IOException;
}
