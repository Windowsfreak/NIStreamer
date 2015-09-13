package de.windowsfreak.testjni;

import de.windowsfreak.testjni.reader.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public abstract class ImageReaderFactory {
    public static List<ImageReaderFactory> sif = new LinkedList<ImageReaderFactory>();
    static {
        sif.add(new ImageReaderFactory("test") {
            @Override
            public void start(Config config) {
                super.start(config);
            }
            @Override
            public boolean factor(ByteBuffer bb) {
                return new TestImageReader().readImage(x, y, bb);
            }
        });
        sif.add(new ImageReaderFactory("raw") {
            @Override
            public void start(Config config) {
                super.start(config);
                this.x = 800;
                this.y = 600;
            }
            @Override
            public boolean factor(ByteBuffer bb) {
                RawImageReader rir = new RawImageReader();
                rir.setFileName("C:\\Software\\adt\\workspace\\testjni\\res\\Lena16_800x600.raw");
                return rir.readImage(x, y, bb);
            }
        });
        sif.add(new ImageReaderFactory("png") {
            @Override
            public void start(Config config) {
                super.start(config);
                this.x = 640;
                this.y = 480;
            }
            @Override
            public boolean factor(ByteBuffer bb) {
                FileImageReader fir = new FileImageReader();
                fir.setFileName("C:\\Software\\adt\\workspace\\testjni\\res\\DeltaE_16bit_gamma2.2.png");
                return fir.readImage(x, y, bb);
            }
        });
        sif.add(new ImageReaderFactory("kinect") {
            OniStreamReader osr;
            @Override
            public void start(Config config) {
                super.start(config);
                osr = new OniStreamReader();
                osr.initialize();
                osr.start(x, y);
            }
            @Override
            public void stop() {
                osr.stop();
                osr.shutDown();
            }
            @Override
            public boolean factor(ByteBuffer bb) {
                boolean success = false;
                while (!success) {
                    success = osr.readImage(x, y, bb);
                    if (!success) System.out.println("Could not read a Kinect frame...");
                }
                return success;
            }
        });
        sif.add(new ImageReaderFactory("oni") {
            OniFileReader ofr;
            @Override
            public void start(Config config) {
                super.start(config);
                this.x = 640;
                this.y = 480;
                ofr = new OniFileReader();
                ofr.setFileName("C:\\Users\\lazer_000\\SkyDrive\\Documents\\sample.oni");
                ofr.initialize();
                ofr.start(x, y);
                //ofr.seek(0);
            }
            @Override
            public void stop() {
                ofr.stop();
                ofr.shutDown();
            }
            @Override
            public boolean factor(ByteBuffer bb) {
                boolean success = false;
                if (!success) {
                    success = ofr.readImage(x, y, bb);
                    if (!success) System.out.println("Could not read an ONI frame...");
                }
                return success;
            }
        });
        sif.add(new ImageReaderFactory("tcp") {
            TCPReader tr;
            @Override
            public void start(Config config) {
                super.start(config);
                tr = new TCPReader();
                try {
                    tr.initialize();
                } catch (IOException e) {
                    throw new UnsupportedOperationException("ex");
                }
                tr.start(x, y);
            }
            @Override
            public void stop() {
                tr.stop();
                try {
                    tr.shutDown();
                } catch (IOException e) {
                    throw new UnsupportedOperationException("ex");
                }
            }
            @Override
            public boolean factor(ByteBuffer bb) {
                boolean success = false;
                if (!success) {
                    success = tr.readImage(x, y, bb);
                    if (!success) System.out.println("Could not read a TCP frame...");
                }
                return success;
            }
        });
    }
    public static ImageReaderFactory getFactory(String factoryName) {
        for (ImageReaderFactory f : sif) {
            if (f.s.equals(factoryName)) {
                return f;
            }
        }
        new Exception("Warning! This factory was not found.").printStackTrace();
        return null;
    }

    protected short x, y;
    protected final String s;

    public String getName() { return s; }

    protected ImageReaderFactory(String s) {
        this.s = s;
    }

    public short getX() {
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
    }
    public void start(Config config) {
        if (config != null) {
            this.x = config.x;
            this.y = config.y;
        }
    }
    public abstract boolean factor(ByteBuffer bb);
    public void stop() {}
}
