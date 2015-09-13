package de.windowsfreak.testjni.codec;

import de.windowsfreak.testjni.Window;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class WindowCodec extends AbstractCodec {
    public WindowCodec() {}
    public WindowCodec(int x, int y) {
        super(x, y);
    }

    public Window window;
    @Override
    public int encode(ByteBuffer compressedData, ByteBuffer uncompressedData, int uncompressedLength) throws IOException {
        window = Window.makeWindow("Image");
        window.setSourceImage(uncompressedData, x, y);
        window.pack();
        window.setVisible(true);

        return 0;
    }

    @Override
    public void decode(ByteBuffer uncompressedData, int uncompressedLength, ByteBuffer compressedData, int compressedLength) throws IOException {
        new Exception("Warning! This method is not implemented.").printStackTrace();
    }

    public void setTitle(String title) {
        window.setTitle(title);
    }
}
