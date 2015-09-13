package de.windowsfreak.testjni;

import javax.swing.*;
import java.nio.ByteBuffer;

/**
 * Created by lazer_000 on 24.07.2015.
 */
public abstract class ShortBufferPanel extends JPanel {
    abstract void setImage(ByteBuffer in, int black, int white, Config config);
}
