package de.windowsfreak.testjni.reader;

import de.windowsfreak.testjni.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class RawSource implements Source {
    @Override
    public boolean readImage(final Config config, final ByteBuffer out) {
        config.frameId++;
        final int x = config.x;
        final int y = config.y;
        File aFile = new File(config.sourceUrl);
        FileInputStream inFile = null;
        try {
            inFile = new FileInputStream(aFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        FileChannel inChannel = inFile.getChannel();
        int size = 0;
        synchronized(out) {
            out.clear();
            try {
                while (true) {
                    int chunk = inChannel.read(out);
                    if (chunk <= 0) break;
                    size += chunk;
                    System.out.println("Reading " + chunk + " bytes...");
                }
                if (inFile.available() > 0) {
                    new Exception("Warning! The source image is larger than the ByteBuffer supplied. " + size + " bytes have been read, at least " + inFile.available() + " bytes remain.").printStackTrace();
                }
                inFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            out.flip();
        }
        if (out.limit() != x * y << 1) {
            new Exception("Warning! The byte buffer size does not match the raw image dimension! It stores " + out.limit() + " bytes, where it should store " + (x * y << 1) + " bytes instead.").printStackTrace();
        }
        if (size != x * y << 1) {
            new Exception("Warning! The source image size does not match the ByteBuffer's dimensions! Source Image: " + size + " bytes, ByteBuffer: " + (x * y << 1) + " bytes.").printStackTrace();
        }
        return true;
    }
}
