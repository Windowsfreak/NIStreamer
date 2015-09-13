package de.windowsfreak.testjni;

/**
 * Created by lazer_000 on 16.07.2015.
 */
public class Config {
    public String source;
    public String sink;
    public String compression;
    public boolean compress = false, decompress = false, sinkControlled = false;
    public String sourceUrl;
    public String sinkUrl;
    public int sourcePort = 9999;
    public int sinkPort = 9999;
    public short x;
    public short y;
    public byte depth, mode, fps; // 1 = depth
    public int threads = 1;
}
