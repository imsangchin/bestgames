
package com.miui.common;

public class FormatBytesUtil {

    public static final long GB = 1073741824L;
    public static final long MB = 1048576L;
    public static final long KB = 1024;

    private static final String UINT_GB = "GB";
    private static final String UINT_MB = "MB";
    private static final String UINT_KB = "KB";
    private static final String UINT_B = "B";

    public static String formatBytes(long bytes, int numOfPoint) {
        String uint = null;
        double f;
        if (bytes >= GB) {
            f = 1.00 * bytes / GB;
            uint = UINT_GB;
        } else if (bytes >= MB) {
            f = 1.00 * bytes / MB;
            uint = UINT_MB;
        } else if (bytes >= KB) {
            f = 1.00 * bytes / KB;
            uint = UINT_KB;
        } else {
            f = 1.00 * bytes;
            uint = UINT_B;
        }
        return textFormat(f, uint, numOfPoint);
    }

    public static String textFormat(double f, String uint, int numOfPoint) {
        String text;
        if (f > 999.5) {
            text = String.format("%d", (int) f);
        } else if (f > 99.5) {
            text = String.format("%.01f", f);
        } else {
            StringBuilder format = new StringBuilder(16);
            format.append("%.0");
            format.append(numOfPoint);
            format.append('f');
            text = String.format(format.toString(), f);
            format = null;
        }
        if (uint != null) {
            text += uint;
        }
        return text;
    }
}
