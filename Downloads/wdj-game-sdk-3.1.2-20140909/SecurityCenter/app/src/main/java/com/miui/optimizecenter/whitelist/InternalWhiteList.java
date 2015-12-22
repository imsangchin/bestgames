
package com.miui.optimizecenter.whitelist;

import android.os.Environment;

import com.miui.common.ImmutableSetMultimap;

import java.util.Set;

public class InternalWhiteList {

    private static final String TAG = InternalWhiteList.class.getSimpleName();

    private static final String EXT_VOLUME_PATH = Environment.getExternalStorageDirectory()
            .getPath();

    private static final ImmutableSetMultimap<WhiteListType, String> WHITE_LIST =
            new ImmutableSetMultimap.Builder<WhiteListType, String>()
                    .put(WhiteListType.CACHE, EXT_VOLUME_PATH + "/MIUI/theme/.data")
                    .put(WhiteListType.CACHE, EXT_VOLUME_PATH + "/MIUI/contactphoto/.data")
                    .put(WhiteListType.CACHE, EXT_VOLUME_PATH + "/MIUI/wallpaper")
                    .put(WhiteListType.CACHE, EXT_VOLUME_PATH + "/MIUI/ringtone")
                    .put(WhiteListType.CACHE, EXT_VOLUME_PATH + "/MIUI/Gallery/cloud")
                    .build();

    public static boolean inInternalCacheWhiteList(String dirPath) {
        return inInternalWhiteList(WhiteListType.CACHE, dirPath);
    }

    public static boolean inInternalWhiteList(WhiteListType type, String dirPath) {
        Set<String> paths = WHITE_LIST.get(type);
        if (paths == null) {
            return false;
        }
        for (String path : paths) {
            if (dirPath.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    public static void printWhiteList() {
        Set<String> paths = WHITE_LIST.get(WhiteListType.CACHE);
        if (paths != null) {
            for (String path : paths) {
                android.util.Log.d(TAG, path);
            }
        }
    }

}
