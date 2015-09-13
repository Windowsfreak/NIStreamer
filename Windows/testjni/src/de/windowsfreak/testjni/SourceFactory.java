package de.windowsfreak.testjni;

import de.windowsfreak.testjni.reader.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public abstract class SourceFactory {
    public static List<SourceFactory> sif = new LinkedList<SourceFactory>();
    static {
        sif.add(new SourceFactory("test") {
            @Override
            public boolean factor(final Config config, final ByteBuffer bb) {
                return new TestSource().readImage(config, bb);
            }
        });
        sif.add(new SourceFactory("raw") {
            RawSource rir = new RawSource();
            @Override
            public boolean factor(final Config config, final ByteBuffer bb) {
                return rir.readImage(config, bb);
            }
        });
        sif.add(new SourceFactory("png") {
            FileSource fir = new FileSource();
            @Override
            public boolean factor(final Config config, final ByteBuffer bb) {
                return fir.readImage(config, bb);
            }
        });
        sif.add(new SourceFactory("kinect") {
            OniStreamSource osr;
            @Override
            public void start(final Config config) {
                super.start(config);
                osr = new OniStreamSource();
                osr.initialize();
                osr.start(config);
            }
            @Override
            public void stop(final Config config) {
                osr.stop();
                osr.shutDown();
            }
            @Override
            public boolean factor(final Config config, final ByteBuffer bb) {
                boolean success = false;
                while (!success) {
                    success = osr.readImage(config, bb);
                    if (!success) System.out.println("Could not read a Kinect frame...");
                }
                return success;
            }
        });
        sif.add(new SourceFactory("oni") {
            OniFileSource ofr;
            @Override
            public void start(final Config config) {
                super.start(config);
                ofr = new OniFileSource();
                ofr.initialize(config);
                ofr.start(config);
                //ofr.seek(0);
            }
            @Override
            public void stop(final Config config) {
                ofr.stop();
                ofr.shutDown();
            }
            @Override
            public boolean factor(final Config config, final ByteBuffer bb) {
                boolean success = false;
                if (!success) {
                    success = ofr.readImage(config, bb);
                    if (!success) System.out.println("Could not read an ONI frame...");
                }
                return success;
            }
        });
        sif.add(new SourceFactory("tcp") {
            TCPSource tr;
            @Override
            public void start(final Config config) {
                super.start(config);
                tr = new TCPSource();
                try {
                    tr.initialize(config);
                } catch (IOException e) {
                    throw new UnsupportedOperationException("tcp initialization failed", e);
                }
                tr.start(config);
            }
            @Override
            public void stop(final Config config) {
                tr.stop();
                try {
                    tr.shutDown();
                } catch (IOException e) {
                    throw new UnsupportedOperationException("ex");
                }
            }
            @Override
            public boolean factor(final Config config, final ByteBuffer bb) {
                boolean success = false;
                if (!success) {
                    success = tr.readImage(config, bb);
                    if (!success) System.out.println("Could not read a TCP frame...");
                }
                return success;
            }
        });
    }
    public static SourceFactory getFactory(String factoryName) {
        for (SourceFactory f : sif) {
            if (f.s.equals(factoryName)) {
                return f;
            }
        }
        new Exception("Warning! This factory was not found.").printStackTrace();
        return null;
    }

    //protected short x, y;
    protected final String s;

    public String getName() { return s; }

    protected SourceFactory(String s) {
        this.s = s;
    }

    /*public short getX() {
        return x;
    }
    public short getY() {
        return y;
    }
    public void start(short x, short y) {
        this.x = x;
        this.y = y;
        start(null);
    }
    public void start(int x, int y) {
        this.x = (short) x;
        this.y = (short) y;
        start(null);
    }*/
    public void start(final Config config) {
        /*if (config != null) {
            this.x = config.x;
            this.y = config.y;
        }*/
    }
    public abstract boolean factor(final Config config, final ByteBuffer bb);
    public void stop(final Config config) {}
}
