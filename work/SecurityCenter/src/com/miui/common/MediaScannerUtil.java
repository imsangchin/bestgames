
package com.miui.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class MediaScannerUtil {

    private static final String MEDIA_SCANNER_PACKAGE = "com.android.providers.media";
    private static final String MEDIA_SCANNER_CLASS = "com.android.providers.media.MediaScannerReceiver";

    public static void requestMediaScannerToScan(Context context, String path) {
        if (!TextUtils.isEmpty(path)) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setClassName(MEDIA_SCANNER_PACKAGE, MEDIA_SCANNER_CLASS);
            intent.setData(Uri.fromFile(new File(path)));
            context.sendBroadcast(intent);
        }
    }

    public static void scanWholeExternalStorage(Context context) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
        intent.setClassName(MEDIA_SCANNER_PACKAGE, MEDIA_SCANNER_CLASS);
        intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
        context.sendBroadcast(intent);
    }
}
