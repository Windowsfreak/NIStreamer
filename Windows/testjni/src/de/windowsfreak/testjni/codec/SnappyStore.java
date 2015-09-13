package de.windowsfreak.testjni.codec;

/**
 * Created by lazer_000 on 26.07.2015.
 */
public class SnappyStore {
    // Multi-threading allows up to arraysize / 2 threads.
    private final int size;
    private byte[][] bytes = new byte[16][];
    private byte[] flags = new byte[16];
    private boolean debug = false;

    public void toggleDebug(boolean debugEnabled) {
        this.debug = debugEnabled;
    }

    public SnappyStore(int size) {
        this.size = size;
    }

    public int obtain() {
        synchronized(flags) {
            int counter = 0;
            for (byte flag : flags) {
                if (flag == 0) {
                    flags[counter] = 1;
                    return counter;
                }
                counter++;
            }
        }
        return -1;
    }
    public void release(int counter) {
        if (debug) System.out.println("released " + counter);
        synchronized(flags) {
            flags[counter] = 0;
        }
    }
    public byte[] acquire(int counter) {
        if (debug) System.out.println("obtained " + counter);
        if (bytes[counter] == null) {
            bytes[counter] = new byte[size];
        }
        return bytes[counter];
    }
}
