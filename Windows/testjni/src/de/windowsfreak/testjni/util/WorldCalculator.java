package de.windowsfreak.testjni.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by lazer_000 on 25.06.2015.
 */
public class WorldCalculator {
    private final int resolutionX;
    private final int resolutionY;
    private final float horizontalFov;
    private final float verticalFov;
    private final int mode;

    private final float xzFactor;
    private final float yzFactor;
    private final float halfResX;
    private final float halfResY;
    private final float coeffX;
    private final float coeffY;
    private final float zFactor;

    public WorldCalculator(int resolutionX, int resolutionY, float horizontalFov, float verticalFov, int mode) {
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.horizontalFov = horizontalFov;
        this.verticalFov = verticalFov;
        this.mode = mode;

        xzFactor = (float) Math.tan(this.horizontalFov / 2) * 2;
        yzFactor = (float) Math.tan(this.verticalFov / 2) * 2;
        halfResX = resolutionX / 2;
        halfResY = resolutionY / 2;
        coeffX = resolutionX / xzFactor;
        coeffY = resolutionY / yzFactor;

        switch (mode)
        {
            case 1:
                zFactor = 1.f;
                break;
            case 2:
                zFactor = 0.1f;
                break;
            default:
                zFactor = 1.f; // not really an error
        }

    }

    public float[] convertDepthToWorldCoordinates(float depthX, float depthY, float depthZ)
    {
        float depthZmm = depthZ * zFactor;

        float normalizedX = depthX / resolutionX - .5f;
        float normalizedY = .5f - depthY / resolutionY;

        return new float[] {
                normalizedX * depthZmm * xzFactor,
                normalizedY * depthZmm * yzFactor,
                depthZ
        };
    }

    public float[] convertWorldToDepthCoordinates(float worldX, float worldY, float worldZ)
    {
        float worldZmm = worldZ * zFactor;

        return new float[]{
                coeffX * worldX / worldZmm + halfResX,
                halfResY - coeffY * worldY / worldZmm,
                worldZ // NOT worldZmm, as in http://forums.structure.io/t/openni-coordinate-conversion-not-correct/4358/2
        };
    }

    public void getPointCloud(ByteBuffer image, float[][] points) {
        image.rewind();
        ShortBuffer sb = image.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
        sb.rewind();
        int z, count = 0;
        for (int y = 0; y < this.resolutionY; y++) {
            for (int x = 0; x < this.resolutionX; x++) {
                z = (int) sb.get();
                if (z < 0) z = 65536 + z;
                points[count] = convertDepthToWorldCoordinates(x, y, z);
                count++;
            }
        }
    }

}
