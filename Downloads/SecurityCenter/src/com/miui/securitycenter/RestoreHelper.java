package com.miui.securitycenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.miui.common.PreferenceStore;

public class RestoreHelper {
    public static final String TAG = "SecApp";
    private static final String sKeyClean = "restore_v5_data_clean";
    private static final String sKeyPower = "restore_v5_data_power";
    private static final String sKeyPre = "restore_v5_data_preference";
    private static final String sKeyAnti = "restore_v5_data_antivirus";
    private static final String sKeyGarbg = "restore_v5_data_garba";
    private static final String sKeyOptimize = "restore_v5_data_optimize";
    private static final String sKeyApp = "restore_v5_data_app";

    private static final String sSourceClean = "/data/data/com.android.settings/databases/clean_master";
    private static final String sSourcePowerCenter = "/data/data/com.android.settings/databases/com_miui_powercenter.db";
    private static final String sSourcePre = "/data/data/com.android.settings/shared_prefs/power_data_settings.xml";
    private static final String sSourceAntiVirus = "/data/data/com.android.settings/databases/anti_virus.db";
    private static final String sSourceGarbage = "/data/data/com.android.settings/databases/garbage_clean.db";
    private static final String sSourceOptimize = "/data/data/com.android.settings/databases/Optimize_Center";
    private static final String sSourceApp = "/data/data/com.android.settings/shared_prefs/com.android.settings_preferences.xml";

    private static final String sTargetFolder = "/data/data/com.miui.securitycenter";
    private static final String sTargetFolderDB = "/data/data/com.miui.securitycenter/databases";
    private static final String sTargetFolderPre = "/data/data/com.miui.securitycenter/shared_prefs";

    private static final String sCopyClean = "cp /data/data/com.android.settings/databases/clean_master /data/data/com.miui.securitycenter/databases/clean_master";
    private static final String sCopyPower = "cp /data/data/com.android.settings/databases/com_miui_powercenter.db /data/data/com.miui.securitycenter/databases/com_miui_powercenter.db";
    private static final String sCopyPref = "cp /data/data/com.android.settings/shared_prefs/power_data_settings.xml /data/data/com.miui.securitycenter/shared_prefs/power_data_settings.xml";
    private static final String sCopyAnti = "cp /data/data/com.android.settings/databases/anti_virus.db /data/data/com.miui.securitycenter/databases/anti_virus.db";
    private static final String sCopyGarbage = "cp /data/data/com.android.settings/databases/garbage_clean.db /data/data/com.miui.securitycenter/databases/garbage_clean.db";
    private static final String sCopyOptimize = "cp /data/data/com.android.settings/databases/Optimize_Center /data/data/com.miui.securitycenter/databases/Optimize_Center";
    private static final String sCopyApp = "cp /data/data/com.android.settings/shared_prefs/com.android.settings_preferences.xml /data/data/com.miui.securitycenter/shared_prefs/com.miui.securitycenter_preferences.xml";

    private static final String sDelClean =  "rm /data/data/com.android.settings/databases/clean_master";
    private static final String sDelPower = "rm /data/data/com.android.settings/databases/com_miui_powercenter.db";
    private static final String sDelPre = "rm /data/data/com.android.settings/shared_prefs/power_data_settings.xml";
    private static final String sDelAnti = "rm /data/data/com.android.settings/databases/anti_virus.db";
    private static final String sDelGarbage  = "rm /data/data/com.android.settings/databases/garbage_clean.db";
    private static final String sDelOptimize = "rm /data/data/com.android.settings/databases/Optimize_Center";
    private static final String sDelApp = "rm /data/data/com.android.settings/shared_prefs/com.android.settings_preferences.xml";

    private static final int ACTION_TYPE_CLEAN = 1;
    private static final int ACTION_TYPE_POWER = 2;
    private static final int ACTION_TYPE_PREFE = 3;
    private static final int ACTION_TYPE_ANTI  = 4;
    private static final int ACTION_TYPE_GARBA = 5;
    private static final int ACTION_TYPE_OPTI  = 6;
    private static final int ACTION_TYPE_APP   = 7;


    private static MyWaiter sWaiter = new MyWaiter();
    public static void restoreData() {
        boolean flag = false;

        try {
            File folder = new File(sTargetFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            folder = new File(sTargetFolderDB);
            if (!folder.exists()) {
                folder.mkdir();
            }

            folder = new File(sTargetFolderPre);
            if (!folder.exists()) {
                folder.mkdir();
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG,"create folder failed");
            return;
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyClean, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_CLEAN);
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyPower, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_POWER);
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyPre, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_PREFE);
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyAnti, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_ANTI);
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyGarbg, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_GARBA);
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyOptimize, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_OPTI);
        }

        flag = PreferenceStore.getPreferenceBoolean(sKeyApp, false);
        if (!flag) {
            restoreData_core(ACTION_TYPE_APP);
        }

        sWaiter.waitIt();
    }

    private static void restoreData_core(int flag) {
        File source = null;
        File targetFolder = null;
        String command = "";

        try {
            switch(flag) {
            case ACTION_TYPE_CLEAN:
                source = new File(sSourceClean);
                targetFolder = new File(sTargetFolderDB);
                command = sCopyClean;
                break;

            case ACTION_TYPE_POWER:
                source = new File(sSourcePowerCenter);
                targetFolder = new File(sTargetFolderDB);
                command = sCopyPower;
                break;

            case ACTION_TYPE_PREFE:
                source = new File(sSourcePre);
                targetFolder = new File(sTargetFolderPre);
                command = sCopyPref;
                Log.d(TAG, "copy the preference: " + sCopyPref);
                break;

            case ACTION_TYPE_ANTI:
                source = new File(sSourceAntiVirus);
                targetFolder = new File(sTargetFolderDB);
                command = sCopyAnti;
                break;

            case ACTION_TYPE_GARBA:
                source = new File(sSourceGarbage);
                targetFolder = new File(sTargetFolderDB);
                command = sCopyGarbage;
                break;

            case ACTION_TYPE_OPTI:
                source = new File(sSourceOptimize);
                targetFolder = new File(sTargetFolderDB);
                command = sCopyOptimize;
                break;

            case ACTION_TYPE_APP:
                source = new File(sSourceApp);
                targetFolder = new File(sTargetFolderPre);
                command = sCopyApp;
                break;
            }

            if (!source.exists() || !targetFolder.exists()) {
                Log.d(TAG, "target folder does not exist: " + command + " " + source.exists() + " " + targetFolder.exists());
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, "exception " + command);
            return;
        }

        if (TextUtils.isEmpty(command)) return;

        sWaiter.add();
        BackgroundCopyTask task = new BackgroundCopyTask(flag);
        task.execute();
    }


    private static class BackgroundCopyTask extends AsyncTask<Void, Void, Integer> {
        private int mFlag;

        public BackgroundCopyTask(int flag) {
            mFlag = flag;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Integer exitValue = null;
            try {
                Runtime rt = Runtime.getRuntime();
                exitValue = copyFile();
            } catch(Exception e) {
                Log.d(TAG, "exception");
                e.printStackTrace();
            }

            sWaiter.remove();
            return exitValue;
        }

        @Override
        protected void onPostExecute(Integer result) {

            String key = "";

            switch(mFlag) {
            case ACTION_TYPE_CLEAN:
                key = sKeyClean;
                break;
            case ACTION_TYPE_POWER:
                key = sKeyPower;
                break;
            case ACTION_TYPE_PREFE:
                key = sKeyPre;
                break;
            case ACTION_TYPE_ANTI:
                key = sKeyAnti;
                break;
            case ACTION_TYPE_GARBA:
                key = sKeyGarbg;
                break;
            case ACTION_TYPE_OPTI:
                key = sKeyOptimize;
                break;
            case ACTION_TYPE_APP:
                key = sKeyApp;
                break;
            }

            if (result != null && result.intValue() == 0) {
                Log.d(TAG, "copy success1 " + key);
                PreferenceStore.setPreferenceBoolean(key, true);
            }
        }

        private int copyFile() {
            int ret = -1;
            String command = "";
            String delCommand = "";

            switch(mFlag) {
            case ACTION_TYPE_CLEAN:
                command = sCopyClean;
                delCommand = sDelClean;
                break;
            case ACTION_TYPE_POWER:
                command = sCopyPower;
                delCommand = sDelPower;
                break;
            case ACTION_TYPE_PREFE:
                command = sCopyPref;
                delCommand = sDelPre;
                Log.d(TAG, "start to copy pref");
                break;
            case ACTION_TYPE_ANTI:
                command = sCopyAnti;
                delCommand = sDelAnti;
                break;
            case ACTION_TYPE_GARBA:
                command = sCopyGarbage;
                delCommand = sDelGarbage;
                break;
            case ACTION_TYPE_OPTI:
                command = sCopyOptimize;
                delCommand = sDelOptimize;
                break;
            case ACTION_TYPE_APP:
                command = sCopyApp;
                delCommand = sDelApp;
                break;
            }

            try {
                ret = doProcessCommand(command);

                Log.d(TAG, "the copy result is: " + ret + "  " + delCommand);
                if (ret == 0 && !TextUtils.isEmpty(delCommand)) {
                    int innerret = doProcessCommand(delCommand);
                    Log.d(TAG, "del result: " +  innerret);
                }
            } catch(Exception e) {
                e.printStackTrace();
                ret = -1;
            }

            if (ret != 0) {
                Log.d(TAG, "data transfer failed: " + command);
            }
            return ret;
        }

        private int doProcessCommand(String command) {
            int exitValue = -1;
            StringBuilder builder  = new StringBuilder();
            try {
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(command);
                InputStream stderr = proc.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);

                String line = null;
                while ( (line = br.readLine()) != null) {
                    builder.append(line);
                }

                Log.d(TAG, "other process result: " + builder.toString());
                exitValue = proc.waitFor();
            } catch(Exception e) {
                exitValue = -1;
            }

            return exitValue;
        }
    }
}
