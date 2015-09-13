package de.windowsfreak.testjni.reader;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class TestImageReader implements ImageReader {
    public boolean readImage(int max_x, int max_y, ByteBuffer out) {
        synchronized(out) {
            out.clear();
            if (out.limit() != max_x * max_y << 1) {
                System.err.println("Warning! The byte buffer size does not match the raw image dimension! It stores " + out.limit() + " bytes, where it should store " + (max_x * max_y << 1) + " bytes instead.");
            }
            int i;
            int pixels = max_x * max_y;
            Random r = new Random(0);
            short[] lut = new short[max_x];
            for (i = 0; i < max_x; i++) {
                lut[i] = (short) (((i << 16) - i) / (max_x - 1));
            }
            i = 0;
            int x = 0;
            int y = 0;
            for (i = 0; i < pixels; i++) {
                int pat_x = x & 63;
                int pat_y = y & 63;

                if (pat_x < 32 && pat_y < 32) {
                    int c = x & 0xfffffff0 | (y & 0xfffffff0) << 16;
                    r.setSeed(c);
                    short s = (short) r.nextInt();
                    out.putShort(s);
                } else if (pat_y > 60) {
                    out.putShort(lut[max_x - 1 - x]);
                } else if (pat_y > 50) {
                    out.putShort((short) (x + (y >> 6) * max_x));
                } else {
                    out.putShort(lut[x]);
                }

                x++;
                if (x >= max_x) {
                    x = 0;
                    y++;
                }
            }
            out.flip();
        }
        return true;
    }
}
