
package com.miui.optimizecenter.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class FileHelper {
    private static final String EXTRA_SHOW_FILE_CLEANUP = "show_file_cleanup";

    public static void openFile(Context context, String path) {
        Intent intent = new Intent();
        intent.setClassName("com.android.fileexplorer",
                "com.android.fileexplorer.FileExplorerTabActivity");
        intent.setData(Uri.parse("file://" + path));
        intent.putExtra(EXTRA_SHOW_FILE_CLEANUP, false);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
