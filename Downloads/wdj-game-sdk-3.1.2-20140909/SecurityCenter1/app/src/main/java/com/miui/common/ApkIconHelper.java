
package com.miui.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.miui.securitycenter.R;

public class ApkIconHelper {

    private static ApkIconHelper INST;
    private Context mContext;

    private FileIconLoader mFileIconLoader;
    private ApkIconLoader mApkIconLoader;

    private ApkIconHelper(Context context) {
        mContext = context;
        mFileIconLoader = new FileIconLoader(context);
        mApkIconLoader = new ApkIconLoader(context);
    }

    public static ApkIconHelper getInstance(Context context) {
        if (INST == null) {
            INST = new ApkIconHelper(context.getApplicationContext());
        }
        return INST;
    }

    public void clearCacheLaunchers() {
        INST = null;
        mFileIconLoader.clear();
        mFileIconLoader = null;
        mApkIconLoader.clear();
        mApkIconLoader = null;
    }

    Drawable loadInstalledAppLauncher(String pkgName) {
        return loadInstalledAppLauncher(mContext, pkgName);
    }

    public static final String PKG_EMPTY_FOLDER = "pkg_empty_folder";
    public static final String PKG_SYSTEM_CACHE = "pkg_system_cache";
    public static final String PKG_UPDATER = "com.android.updater";
    public static final String PKG_FILEEXPLORER = "com.android.fileexplorer";

    Drawable loadInstalledAppLauncher(Context context, String pkgName) {
        if (TextUtils.equals(PKG_SYSTEM_CACHE, pkgName)) {
            return context.getResources().getDrawable(R.drawable.icon_system_cache);
        }
        PackageManager pm = context.getPackageManager();
        try {
            if (TextUtils.equals(PKG_EMPTY_FOLDER, pkgName)) {
                return pm.getApplicationIcon(PKG_FILEEXPLORER);
            }
            return pm.getApplicationIcon(pkgName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return FileIconHelper.getDefaultApkIcon(context);
    }

    public void loadInstalledAppLauncher(ImageView imageView, String pkgName) {
        if (TextUtils.equals(PKG_SYSTEM_CACHE, pkgName)) {
            imageView.setImageResource(R.drawable.icon_system_cache);
        } else if (TextUtils.equals(PKG_EMPTY_FOLDER, pkgName)) {
            loadFolderIcon(imageView);
        } else {
            mApkIconLoader.loadIcon(imageView, pkgName);
        }
    }

    public void loadFileIcon(ImageView imageView, String fileFullPath) {
        mFileIconLoader.loadIcon(imageView, fileFullPath);
    }

    public void loadFileIcon(ImageView imageView, String fileFullPath, boolean custom) {
        mFileIconLoader.loadIcon(imageView, fileFullPath, custom);
    }

    public static final String ICON_FOLDER = "ICON_FOLDER";

    public void loadFolderIcon(ImageView imageView) {
        mFileIconLoader.loadFolderIcon(imageView);
    }
}
