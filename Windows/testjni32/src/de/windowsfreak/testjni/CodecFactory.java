package de.windowsfreak.testjni;

import de.windowsfreak.testjni.codec.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public abstract class CodecFactory {
    public static List<CodecFactory> cf = new LinkedList<CodecFactory>();
    static {
        cf.add(new CodecFactory("PNG") {
            @Override
            public AbstractCodec getCodec() {
                return new PNGCodec();
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new PNGCodec(x, y);
            }
        });
        cf.add(new CodecFactory("JpegLS") {
            @Override
            public AbstractCodec getCodec() {
                return new JpegLSCodec();
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new JpegLSCodec(x, y);
            }
        });
        cf.add(new CodecFactory("CharLS") {
            @Override
            public AbstractCodec getCodec() {
                return new CharLSCodec();
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new CharLSCodec(x, y);
            }
        });
        cf.add(new CodecFactory("Deflate9") {
            @Override
            public AbstractCodec getCodec() {
                return new DeflateCodec(9);
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new DeflateCodec(x, y, 9);
            }
        });
        cf.add(new CodecFactory("Deflate5") {
            @Override
            public AbstractCodec getCodec() {
                return new DeflateCodec(5);
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new DeflateCodec(x, y, 5);
            }
        });
        cf.add(new CodecFactory("Deflate2") {
            @Override
            public AbstractCodec getCodec() {
                return new DeflateCodec(2);
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new DeflateCodec(x, y, 2);
            }
        });
        cf.add(new CodecFactory("Bz29") {
            @Override
            public AbstractCodec getCodec() {
                return new Bz2Codec(9);
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new Bz2Codec(x, y, 9);
            }
        });
        cf.add(new CodecFactory("Bz21") {
            @Override
            public AbstractCodec getCodec() {
                return new Bz2Codec(1);
            }
            @Override
            public AbstractCodec getCodec(int x, int y) {
                return new Bz2Codec(x, y, 1);
            }
        });
    }

    public static CodecFactory getFactory(String factoryName) {
        for (CodecFactory f : cf) {
            if (f.s.equals(factoryName)) {
                return f;
            }
        }
        new Exception("Warning! This factory was not found.").printStackTrace();
        return null;
    }

    protected CodecFactory(String s) {
        this.s = s;
    }

    protected final String s;

    public String getName() { return s; }

    public abstract AbstractCodec getCodec();
    public abstract AbstractCodec getCodec(int x, int y);
}
