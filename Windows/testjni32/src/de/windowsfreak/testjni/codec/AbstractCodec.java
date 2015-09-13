package de.windowsfreak.testjni.codec;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public abstract class AbstractCodec implements Codec {
    protected int x = 640;
    protected int y = 480;

    public AbstractCodec() {}
    public AbstractCodec(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setDimensions(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
