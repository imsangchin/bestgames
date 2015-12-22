
package com.miui.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.HashMap;

import com.miui.securitycenter.R;

public class FileIconHelper {
    public static final String TYPE_APK = "apk";
    private static final String LOG_TAG = "FileIconHelper";
    private static HashMap<String, Integer> sFileExtToIcons = new HashMap<String, Integer>();

    static {
        addItem(new String[] {
                "mp3"
        }, miui.R.drawable.file_icon_mp3);
        addItem(new String[] {
                "wma"
        }, miui.R.drawable.file_icon_wma);
        addItem(new String[] {
                "wav"
        }, miui.R.drawable.file_icon_wav);
        addItem(new String[] {
                "mid"
        }, miui.R.drawable.file_icon_mid);
        addItem(new String[] {
                "mp4", "wmv", "mpeg", "m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf",
                "flv", "mkv", "vob", "ts", "f4v", "rm", "mov", "rmvb"
        }, miui.R.drawable.file_icon_video);
        addItem(new String[] {
                "jpg", "jpeg", "gif", "png", "bmp", "wbmp"
        }, miui.R.drawable.file_icon_picture);
        addItem(new String[] {
                "txt", "log", "xml", "ini", "lrc"
        }, miui.R.drawable.file_icon_txt);
        addItem(new String[] {
                "doc", "docx"
        }, miui.R.drawable.file_icon_doc);
        addItem(new String[] {
                "ppt", "pptx"
        }, miui.R.drawable.file_icon_ppt);
        addItem(new String[] {
                "xls", "xlsx"
        }, miui.R.drawable.file_icon_xls);
        addItem(new String[] {
                "wps"
        }, miui.R.drawable.file_icon_wps);
        addItem(new String[] {
                "pps"
        }, miui.R.drawable.file_icon_pps);
        addItem(new String[] {
                "et"
        }, miui.R.drawable.file_icon_et);
        addItem(new String[] {
                "wpt"
        }, miui.R.drawable.file_icon_wpt);
        addItem(new String[] {
                "ett"
        }, miui.R.drawable.file_icon_ett);
        addItem(new String[] {
                "dps"
        }, miui.R.drawable.file_icon_dps);
        addItem(new String[] {
                "dpt"
        }, miui.R.drawable.file_icon_dpt);
        addItem(new String[] {
                "pdf"
        }, miui.R.drawable.file_icon_pdf);
        addItem(new String[] {
                "zip"
        }, miui.R.drawable.file_icon_zip);
        addItem(new String[] {
                "mtz"
        }, miui.R.drawable.file_icon_theme);
        addItem(new String[] {
                "rar"
        }, miui.R.drawable.file_icon_rar);
        addItem(new String[] {
                "apk"
        }, miui.R.drawable.file_icon_apk);
        addItem(new String[] {
                "amr"
        }, miui.R.drawable.file_icon_amr);
        addItem(new String[] {
                "vcf"
        }, miui.R.drawable.file_icon_vcf);
    }

    private static void addItem(String[] exts, int resId) {
        if (exts != null) {
            for (String ext : exts) {
                sFileExtToIcons.put(ext.toLowerCase(), resId);
            }
        }
    }

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    private static Drawable getApkIcon(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            if (appInfo != null) {
                try {
                    appInfo.publicSourceDir = path;
                    return pm.getApplicationIcon(appInfo);
                } catch (OutOfMemoryError e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }

        return context.getResources().getDrawable(miui.R.drawable.file_icon_default);
    }

    public static int getFileIcon(String ext) {
        Integer i = sFileExtToIcons.get(ext.toLowerCase());
        int resId = miui.R.drawable.file_icon_default;
        if (i != null) {
            resId = i.intValue();
        }
        return resId;
    }

    public static Drawable getFileIcon(Context context, String fileFullPath) {
        String ext = getExtFromFilename(fileFullPath);
        Drawable icon = null;
        if (ext.equals(TYPE_APK)) {
            icon = getApkIcon(context, fileFullPath);
        } else {
            icon = context.getResources().getDrawable(getFileIcon(ext));
        }

        return icon;
    }

    public static int getFolderIcon() {
        return R.drawable.file_icon_folder;
    }

    public static Drawable getFolderIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.file_icon_folder);
    }

    public static Drawable getDefaultApkIcon(Context context) {
        return context.getResources().getDrawable(miui.R.drawable.file_icon_apk);
    }
}
