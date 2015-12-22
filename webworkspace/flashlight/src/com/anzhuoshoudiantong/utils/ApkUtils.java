package com.anzhuoshoudiantong.utils;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ApkUtils {
  public static void install(Context context, String path) {
    File file = new File(path);
    if (file != null) {
      Intent intent = new Intent();
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction(android.content.Intent.ACTION_VIEW);
      intent.setDataAndType(Uri.fromFile(file),
          "application/vnd.android.package-archive");
      context.startActivity(intent);
    }
  }
}
