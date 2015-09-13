package com.sun.media.imageioimpl.plugins.jpeg;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

/**
 * This differs from the core JPEG ImageWriteParam in that:
 *
 * <ul>
 * <li>compression types are: "JPEG" (standard), "JPEG-LOSSLESS"
 * (lossless JPEG from 10918-1/ITU-T81), "JPEG-LS" (ISO 14495-1 lossless).</li>
 * <li>compression modes are: MODE_DEFAULT and MODE_EXPLICIT and the
 * other modes (MODE_DISABLED and MODE_COPY_FROM_METADATA) cause
 * an UnsupportedOperationException.</li>
 * <li>isCompressionLossless() will return true if type is NOT "JPEG".</li>
 * </ul>
 */
public final class CLibJPEGImageWriteParam extends ImageWriteParam {
    private static final float DEFAULT_COMPRESSION_QUALITY = 0.75F;

    static final String LOSSY_COMPRESSION_TYPE = "JPEG";
    static final String LOSSLESS_COMPRESSION_TYPE = "JPEG-LOSSLESS";
    static final String LS_COMPRESSION_TYPE = "JPEG-LS";

    private static final String[] compressionQualityDescriptions =
        new String[] {
            I18N.getString("CLibJPEGImageWriteParam0"),
            I18N.getString("CLibJPEGImageWriteParam1"),
            I18N.getString("CLibJPEGImageWriteParam2")
        };

    CLibJPEGImageWriteParam(Locale locale) {
        super(locale);

        canWriteCompressed = true;
        compressionMode = MODE_EXPLICIT;
        compressionQuality = DEFAULT_COMPRESSION_QUALITY;
        compressionType = LOSSY_COMPRESSION_TYPE;
        compressionTypes = new String[] {LOSSY_COMPRESSION_TYPE,
                                         LOSSLESS_COMPRESSION_TYPE,
                                         LS_COMPRESSION_TYPE};
    }

    public String[] getCompressionQualityDescriptions() {
        super.getCompressionQualityDescriptions(); // Performs checks.

        return compressionQualityDescriptions;
    }

    public float[] getCompressionQualityValues() {
        super.getCompressionQualityValues(); // Performs checks.

        return new float[] { 0.05F,   // "Minimum useful"
                             0.75F,   // "Visually lossless"
                             0.95F }; // "Maximum useful"
    }

    public boolean isCompressionLossless() {
        super.isCompressionLossless(); // Performs checks.

        return !compressionType.equalsIgnoreCase(LOSSY_COMPRESSION_TYPE);
    }

    public void setCompressionMode(int mode) {
        if(mode == MODE_DISABLED ||
           mode == MODE_COPY_FROM_METADATA) {
            throw new UnsupportedOperationException
                ("mode == MODE_DISABLED || mode == MODE_COPY_FROM_METADATA");
        }

        super.setCompressionMode(mode); // This sets the instance variable.
    }

    public void unsetCompression() {
        super.unsetCompression(); // Performs checks.

        compressionQuality = DEFAULT_COMPRESSION_QUALITY;
        compressionType = LOSSY_COMPRESSION_TYPE;
    }
}
