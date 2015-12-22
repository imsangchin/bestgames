package com.miui.mdb;

import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.miui.Shell;

public class MDBUpdateUtil {
    public static final String LOG_TAG = "MDBUpdate";

    private static final int MDB_VERSION_CODE = 1;
    private static final String MDB_DATA_NAME = "mdb_pub.key";
    private static final String MDB_VERSION_NAME = "mdbversion";
    private static final String DATA_SYSTEM_PATH = "/data/system/";

    public static final String MDB_DATA_PATH = DATA_SYSTEM_PATH + MDB_DATA_NAME;
    public static final String MDB_VERSION_PATH = DATA_SYSTEM_PATH + MDB_VERSION_NAME;

    public static boolean updateDataFile() {
        String dataName = MDB_DATA_NAME;
        String dataUrl = DataUpdateUtils.MDB_URL;

        if (null != dataName && null != dataUrl) {
            InputStream in = null;
            OutputStream out = null;
            File tmpFile = new File(Environment.getDownloadCacheDirectory(), dataName);
            try {
                URL url = new URL(dataUrl);
                in = ((HttpURLConnection) url.openConnection()).getInputStream();
                out = new FileOutputStream(tmpFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "url exception", e);
                return false;
            } catch (IOException e) {
                Log.e(LOG_TAG, "io exception", e);
                return false;
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "io exception", e);
                    }
                }
                if (null != out) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "io exception", e);
                    }
                }
            }
            Shell.move(tmpFile.getAbsolutePath(), MDB_DATA_PATH);
            return true;
        }
        return false;
    }

    public static void setCurrentVersionCode(int version) {
        String versionName = MDB_VERSION_NAME;

        if (null != versionName) {
            FileWriter writer = null;
            File tmpFile = new File(Environment.getDownloadCacheDirectory(), versionName);
            try {
                writer = new FileWriter(tmpFile);
                writer.write(String.valueOf(version) + "\n");
            } catch (IOException e) {
                Log.e(LOG_TAG, "write holiday version error", e);
                return;
            } finally {
                if (null != writer) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "write holiday version error", e);
                    }
                }
            }
            Shell.move(tmpFile.getAbsolutePath(), MDB_VERSION_PATH);
        }
    }

    public static int getCurrentVersionCode() {
        String versionName = MDB_VERSION_NAME;
        int version = MDB_VERSION_CODE;

        if (null != versionName) {
            String versionFilePath = DATA_SYSTEM_PATH + versionName;
            if (new File(versionFilePath).exists()) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(versionFilePath));
                    String versionStr = reader.readLine();
                    if (!TextUtils.isEmpty(versionStr)) {
                        version = Integer.parseInt(versionStr);
                    }
                } catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, "read version error", e);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "read version error", e);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "read version error", e);
                } finally {
                    if (null != reader) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "read version error", e);
                        }
                    }
                }
            }
        }
        return version;
    }

    /*
     * Set owner and group for mdb_data and mdb_version file.
     */
    public static void setFileOwnerAndPermission() {
        // mdb data: read/write for owner and group
        Shell.chmod(MDB_DATA_PATH, 0660);
        Shell.chown(MDB_DATA_PATH, Process.SYSTEM_UID, Process.SYSTEM_UID);

        // mdb version: read/write for owner and group, read for other
        Shell.chmod(MDB_VERSION_PATH, 0664);
        Shell.chown(MDB_VERSION_PATH, Process.SYSTEM_UID, Process.SYSTEM_UID);
    }
}
