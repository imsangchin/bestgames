
package com.miui.securitycenter.system;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.miui.securitycenter.ContentView;
import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.miui.antivirus.Preferences;
import com.miui.antivirus.VirusCheckManager;
import com.miui.antivirus.VirusCheckManager.ScanItemType;
import com.miui.antivirus.VirusCheckManager.ScanResultType;
import com.miui.antivirus.VirusCheckManager.VirusScanCallback;
import com.miui.securitycenter.ScoreConstants;

import android.content.BroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class SystemCheckManager {

    public interface SystemScanCallback {
        void onStartScan(int totalCount);

        /**
         * @param descx
         * @return true : force stop
         */
        boolean onScanItem(String descx);

        void onFinishScan();
    }

    public interface SystemCleanupCallback {
        void onStartCleanup();

        /**
         * @param descx
         * @return true : force stop
         */
        boolean onCleanupItem(String descx);

        void onFinishCleanup();
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        @Override
        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            Log.d("SecurityCenter", "System protection delete virus package : " + packageName);
        }
    }

    private final class SystemScore {
        int permission_score;
        int usb_score;
        int virus_lib_score;
        int mms_score;
        int install_score;
        int dev_score;

        boolean has_virus = false;

        SystemScore() {
            reset();
        }

        void reset() {
            has_virus = false;
            permission_score = ScoreConstants.SYSTEM_ITEM_PERMISSION_SCORE;
            usb_score = ScoreConstants.SYSTEM_ITEM_USB_DEBUG_SCORE;
            virus_lib_score = ScoreConstants.SYSTEM_ITEM_VIRUS_AUTO_UPDATE_SCORE;
            mms_score = ScoreConstants.SYSTEM_ITEM_MMS_SCORE;
            install_score = ScoreConstants.SYSTEM_ITEM_INSTALL_MONITOR_SCORE;
            dev_score = ScoreConstants.SYSTEM_ITEM_DEV_OFF_SCORE;
        }

        int getScore() {
            int score = permission_score + usb_score + virus_lib_score + mms_score + install_score
                    + dev_score;
            if (has_virus) {
                if (score >= 20) {
                    score = score - 20;
                } else {
                    score = 0;
                }
            }
            return score;
        }
    }

    private class Virus {
        public String pkgName;
        public String appLabel;
    }

    private Context mContext;
    private static SystemCheckManager INST;

    private SystemScore mSystemScore;
    private SystemScanCallback mSystemScanCallback;
    private SystemCleanupCallback mCleanupCallback;

    private VirusCheckManager mVirusCheckManager;

    private List<Virus> mVirusList = new ArrayList<Virus>();

    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final int TOTAL_INTERNAL_SYSTEM_ITEM = 7;

    private SystemCheckManager(Context context) {
        mContext = context;
        mSystemScore = new SystemScore();
        mVirusCheckManager = VirusCheckManager.getInstance(context);
    }

    public static SystemCheckManager getInstance(Context context) {
        if (INST == null) {
            INST = new SystemCheckManager(context.getApplicationContext());
        }
        return INST;
    }

    public boolean hasVirus() {
        return mSystemScore.has_virus;
    }

    public String getVirusAppName() {
        if (!mVirusList.isEmpty()) {
            return mVirusList.get(0).appLabel;
        }
        return null;
    }

    public int getVirusAppCount() {
        return mVirusList.size();
    }

    public void setHasVirus(boolean hasVirus) {
        mSystemScore.has_virus = hasVirus;
    }

    public void setPermissonScore(int score) {
        mSystemScore.permission_score = score;
    }

    public void setMmsScore(int score) {
        mSystemScore.mms_score = score;
    }

    public void setUsbScore(int score) {
        mSystemScore.usb_score = score;
    }

    public void setVirusLibScore(int score) {
        mSystemScore.virus_lib_score = score;
    }

    public void setDevScore(int score) {
        mSystemScore.dev_score = score;
    }

    public void setInstallScore(int score) {
        mSystemScore.install_score = score;
    }

    public int getScore() {
        return mSystemScore.getScore();
    }

    public void resetScore() {
        mSystemScore.reset();
    }

    public CharSequence getCheckResult() {
        int dangers = 0;
        if (mSystemScore.has_virus) {
            dangers++;
        }
        if (mSystemScore.permission_score == 0) {
            dangers++;
        }
        if (mSystemScore.mms_score == 0) {
            dangers++;
        }
        if (mSystemScore.dev_score == 0) {
            dangers++;
            if (mSystemScore.usb_score == 0) {
                dangers++;
            }
        }
        if (mSystemScore.virus_lib_score == 0) {
            dangers++;
        }
        if (mSystemScore.install_score == 0) {
            dangers++;
        }

        return mContext.getString(R.string.system_check_content, dangers);
    }

    public void startScan(SystemScanCallback callback) {

        mSystemScanCallback = callback;
        mSystemScore.reset();

        if (mSystemScanCallback != null) {
            mSystemScanCallback.onStartScan(mVirusCheckManager.getScanPackagesCount() + TOTAL_INTERNAL_SYSTEM_ITEM);
        }
        // virus
        mVirusCheckManager.scanInstalledApps(mVirusScanCallback);
        mSystemScore.has_virus = !mVirusList.isEmpty();
        if (updateScanProgress(mContext.getString(R.string.item_virus))) {
            return;
        }

        // dev mode & debug mode
        if (isDevModeOn()) {
            mSystemScore.dev_score = 0;
            if (isUsbDebugOn()) {
                mSystemScore.usb_score = 0;
            }
            if (updateScanProgress(mContext.getString(R.string.item_usb_debug))) {
                return;
            }
        }
        if (updateScanProgress(mContext.getString(R.string.item_dev_mode))) {
            return;
        }

        // app permission
        if (!isPermissionMonitorOn()) {
            mSystemScore.permission_score = 0;
        }
        if (updateScanProgress(mContext.getString(R.string.item_app_permission))) {
            return;
        }

        // mms
        if (!isSMSFilterOn()) {
            mSystemScore.mms_score = 0;
        }
        if (updateScanProgress(mContext.getString(R.string.item_mms))) {
            return;
        }

        // install monitor
        if (!isInstallMotinorOn()) {
            mSystemScore.install_score = 0;
        }
        if (updateScanProgress(mContext.getString(R.string.item_install_monitor))) {
            return;
        }

        // virus lib update
        if (!isVirusLibAutoUpdateOn()) {
            mSystemScore.virus_lib_score = 0;
        }
        if (updateScanProgress(mContext.getString(R.string.item_virus_lib))) {
            return;
        }

        if (mSystemScanCallback != null) {
            mSystemScanCallback.onFinishScan();
        }
    }

    private boolean updateScanProgress(String descx) {
        if (mSystemScanCallback != null) {
            return mSystemScanCallback.onScanItem(descx);
        }
        return false;
    }

    private VirusScanCallback mVirusScanCallback = new VirusScanCallback() {

        @Override
        public void onStartScan() {
            mVirusList.clear();
        }

        @Override
        public boolean onScanItem(String descx) {
            updateScanProgress(descx);
            return false;
        }

        @Override
        public void onFinishScan() {
            // ignore
        }

        @Override
        public void onFinishCloudScan() {
            // ignore
        }

        @Override
        public void onFindItem(ScanResultType resultType, ScanItemType itemType, String pkgName,
                String appLabel, String sourceDir, String virusDescx, String virusName) {
            Virus virus = new Virus();
            virus.pkgName = pkgName;
            virus.appLabel = appLabel;
            mVirusList.add(virus);
            if (resultType == ScanResultType.VIRUS) {
                AnalyticsUtil.trackVirusScanVirusDetail(mContext, pkgName, appLabel, virusName);
            } else if (resultType == ScanResultType.RISK) {
                AnalyticsUtil.trackVirusScanRiskDetail(mContext, pkgName, appLabel);
            }
        }

        @Override
        public void onCloudScanStart() {
            // ignore
        }

        @Override
        public void onCancelScan() {
            // ignore
        }
    };

    public void startCleanup(SystemCleanupCallback callback) {
        mCleanupCallback = callback;

        if (mCleanupCallback != null) {
            mCleanupCallback.onStartCleanup();
        }

        // virus
        deleteVirusApps(false);
        mSystemScore.has_virus = false;
        if (updateCleanupProgress(mContext.getString(R.string.item_virus))) {
            return;
        }

        // dev mode & usb debug
        closeDevMode();
        mSystemScore.dev_score = ScoreConstants.SYSTEM_ITEM_DEV_OFF_SCORE;
        mSystemScore.usb_score = ScoreConstants.SYSTEM_ITEM_USB_DEBUG_SCORE;
        if (updateCleanupProgress(mContext.getString(R.string.item_dev_mode))) {
            return;
        }
        if (updateCleanupProgress(mContext.getString(R.string.item_usb_debug))) {
            return;
        }

        // app permission
        openPermissionMonitor();
        mSystemScore.permission_score = ScoreConstants.SYSTEM_ITEM_PERMISSION_SCORE;
        if (updateCleanupProgress(mContext.getString(R.string.item_app_permission))) {
            return;
        }

        // mms
        openSMSFilter();
        mSystemScore.mms_score = ScoreConstants.SYSTEM_ITEM_MMS_SCORE;
        if (updateCleanupProgress(mContext.getString(R.string.item_mms))) {
            return;
        }

        // install monitor
        openInstallMotinor();
        mSystemScore.install_score = ScoreConstants.SYSTEM_ITEM_INSTALL_MONITOR_SCORE;
        if (updateCleanupProgress(mContext.getString(R.string.item_install_monitor))) {
            return;
        }

        // virus lib update
        openVirusLibAutoUpdate();
        mSystemScore.virus_lib_score = ScoreConstants.SYSTEM_ITEM_VIRUS_AUTO_UPDATE_SCORE;
        if (updateCleanupProgress(mContext.getString(R.string.item_virus_lib))) {
            return;
        }

        if (mCleanupCallback != null) {
            mCleanupCallback.onFinishCleanup();
        }
    }

    private boolean updateCleanupProgress(String descx) {
        if (mCleanupCallback != null) {
            return mCleanupCallback.onCleanupItem(descx);
        }
        return false;
    }

    public void deleteVirusApps(boolean background) {
        final List<Virus> virusList = new ArrayList<Virus>(mVirusList);
        mVirusList.clear();
        mSystemScore.has_virus = false;
        if (background) {
            new Thread() {
                public void run() {
                    doDeleteVirusApps(virusList);
                };
            }.start();
        } else {
            doDeleteVirusApps(virusList);
        }
    }

    private void doDeleteVirusApps(List<Virus> virusList) {
        PackageManager pm = mContext.getPackageManager();
        for (Virus virus : virusList) {
            try {
                pm.deletePackage(virus.pkgName, new PackageDeleteObserver(), 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isUsbDebugOn() {
        ContentResolver cr = mContext.getContentResolver();
        return Settings.Secure.getInt(cr, Settings.Secure.ADB_ENABLED, 0) == 1;
    }

    public boolean closeUsbDebug() {
        ContentResolver cr = mContext.getContentResolver();
        return Settings.Secure.putInt(cr, Settings.Secure.ADB_ENABLED, 0);
    }

    public boolean isPermissionMonitorOn() {
        return true;
    }

    public void openPermissionMonitor() {
        // TODO
    }

    public boolean isSMSFilterOn() {
        return true;
    }

    public void openSMSFilter() {
        // TODO
    }

    public boolean isInstallMotinorOn() {
        return MiuiSettings.AntiVirus.isInstallMonitorEnabled(mContext);
    }

    public void openInstallMotinor() {
        MiuiSettings.AntiVirus.setInstallMonitorEnabled(mContext, true);
    }

    public boolean isVirusLibAutoUpdateOn() {
        return Preferences.isVirusLibAutoUpdateEnabled();
    }

    public void openVirusLibAutoUpdate() {
        Preferences.setVirusLibAutoUpdateEnabled(true);
    }

    public boolean isDevModeOn() {
        Context mSettingsContext = null;
        try {
            mSettingsContext = mContext.createPackageContext(SETTINGS_PACKAGE_NAME,
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (mSettingsContext != null) {
            return mSettingsContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                    .getBoolean(PREF_SHOW, false);
        }
        return false;
    }

    public void closeDevMode() {
        closeUsbDebug();
        Context mSettingsContext = null;
        try {
            mSettingsContext = mContext.createPackageContext(SETTINGS_PACKAGE_NAME,
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (mSettingsContext != null) {
            mSettingsContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
                    .putBoolean(PREF_SHOW, false).commit();
            Intent intent = new Intent();
            intent.setAction("com.android.settings.action.DEV_CLOSE");
            intent.putExtra(PREF_SHOW, false);
            mContext.sendBroadcast(intent);
        }
    }

    private static final String PREF_SHOW = "show";
    private static final String PREF_FILE = "development";

}
