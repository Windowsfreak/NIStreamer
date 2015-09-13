package de.windowsfreak.testjni;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class Surface extends JPanel {

    private ByteBuffer image;
    private int x = 640, y = 480, black = 0, white = 65535;
    private BufferedImage bi;
    short[] dst;

    public void setImage(ByteBuffer in, int x, int y, int black, int white) {
        synchronized(this) {
            this.x = x;
            this.y = y;
            this.black = black;
            this.white = white;
            this.image = in;
            this.bi = null;
        }
    }

    private BufferedImage prepareImage() {
        if (bi == null || bi.getWidth() != x || bi.getHeight() != y) {
            //bi = new BufferedImage(x, y, BufferedImage.TYPE_USHORT_GRAY);
            bi = new BufferedImage(x, y, BufferedImage.TYPE_BYTE_GRAY);
        }
        synchronized(image) {
            image.rewind();
            if (false && black == 0 && white == 65535) {
                ShortBuffer sb = image.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
                if (dst == null || dst.length != sb.capacity()) dst = new short[sb.capacity()];
                sb.get(dst);
                bi.getRaster().setDataElements(0, 0, x, y, dst);
            /*
            ShortBuffer sb = ShortBuffer.allocate(image.capacity());
            image.rewind();
            while (image.hasRemaining()) {
                sb.put(image.getShort());
            }
            bi.getRaster().setDataElements(0, 0, x, y, sb.array());*/
                //bi.setData(Raster.createBandedRaster(new DataBufferByte(image.array(), image.capacity()), x, y, x * 2, 2, new int[]{ 0, 1 }, new Point(0, 0)));
            } else {
                ShortBuffer sb = image.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
                WritableRaster r = bi.getRaster();
                int z;
                for (int y = 0; y < this.y; y++) {
                    for (int x = 0; x < this.x; x++) {
                        z = (int) sb.get();
                        if (z < 0) z = 65536 + z;
                        if (white - black == 0) {
                            z = z > black ? 255 : 0;
                        } else {
                            z = (z - black << 8) / (white - black);
                            if (z < 0) z = 0;
                            if (z > 255) z = 255;
                        }
                        //bi.setRGB(x, y, z);
                        r.setSample(x, y, 0, (byte) z);
                    }
                }
            }
        }
        return bi;
    }

    private void doDrawing(Graphics g) {
        synchronized(this) {
            if (image == null) {
                bi = null;
            } else {
                if (bi == null) bi = prepareImage();
            }
            if (bi != null) {
                //System.out.println("doDrawing " + bi.getWidth() + "," + bi.getHeight());
                g.drawImage(bi, 0, 0, null);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.drawLine(0, 0, getWidth() - 1, getHeight() - 1);
                g.drawLine(0, getHeight() - 1, getWidth() - 1, 0);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }
}
