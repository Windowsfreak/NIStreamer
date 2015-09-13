package de.windowsfreak.testjni.util;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class Calculator {
    private final int x;
    private final int y;
    private final int bytesPerPixel = 2;

    public Calculator(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getBitsPerPixel() {
        return bytesPerPixel << 4;
    }

    public int getBytesPerPixel() {
        return bytesPerPixel;
    }

    public int getWidth() {
        return x;
    }

    public int getHeight() {
        return y;
    }

    public int getBytesPerLine() {
        return x * bytesPerPixel;
    }
}
