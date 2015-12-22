
package com.miui.antivirus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;

import miui.provider.CloudAppControll;
import miui.provider.ExtraGuard;
import miui.provider.ExtraGuardVirusInfoEntity;

import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.util.Log;

import com.miui.common.AndroidUtils;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyPackageInfo;

import com.miui.securitycenter.R;

public class VirusCheckManager {

    public enum ScanResultType {
        SAFE, RISK, VIRUS
    }

    public enum ScanItemType {
        INSTALLED_APP, UNINSTALLED_APK
    }

    public interface VirusScanCallback {
        void onStartScan();

        /**
         * @param descx
         * @return true:force stop
         */
        boolean onScanItem(String descx);

        void onFindItem(ScanResultType resultType, ScanItemType itemType, String pkgName,
                String appLabel, String sourceDir, String virusDescx, String virusName);

        void onCloudScanStart();

        void onFinishCloudScan();

        void onFinishScan();

        void onCancelScan();
    }

    public interface VirusCleanupCallback {
        void onStartCleanup();

        boolean onCleanupItem(String descx);

        void onFinishCleanup();

        void onCancelCleanup();
    }

    private static VirusCheckManager INST;
    private Context mContext;

    private VirusCheckManager(Context context) {
        mContext = context;
    }

    public static VirusCheckManager getInstance(Context context) {
        if (INST == null) {
            INST = new VirusCheckManager(context.getApplicationContext());
        }
        return INST;
    }

    public int getScanPackagesCount() {
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> installedPkgs = pm.getInstalledPackages(0);
        int res_cnt = 0;
        for (PackageInfo pkgInfo : installedPkgs) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                res_cnt ++;
            }
        }
        return res_cnt;
    }

    public void scanInstalledApps(VirusScanCallback callback) {
        callback.onStartScan();

        PackageManager pm = mContext.getPackageManager();
        ExtraGuardHelper extraGuardHelper = ExtraGuardHelper.getInstance(mContext);
        Resources res = mContext.getResources();

        // 已安装
        List<PackageInfo> installedPkgs = pm.getInstalledPackages(0);
        for (PackageInfo pkgInfo : installedPkgs) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                CharSequence label = appInfo.loadLabel(pm);
                String pkgName = pkgInfo.packageName;

                ExtraGuardVirusInfoEntity entity = extraGuardHelper.incrementalCheckInstalledApp(
                        appInfo, false);

                if (callback.onScanItem(label.toString())) {
                    callback.onCancelScan();
                    return;
                }

                if (entity != null) {
                    ScanItemType itemType = ScanItemType.INSTALLED_APP;
                    ScanResultType resultType = ScanResultType.SAFE;
                    int type = entity.getType();
                    if (ExtraGuardHelper.VIRUS_BLACK == type || ExtraGuardHelper.VIRUS_GRAY == type) {
                        if (ExtraGuardHelper.VIRUS_BLACK == type) {
                            resultType = ScanResultType.VIRUS;
                        } else if (ExtraGuardHelper.VIRUS_GRAY == type) {
                            resultType = ScanResultType.RISK;
                        }

                        String descx = entity.getDescription();
                        if (descx == null) {
                            descx = res.getString(R.string.hints_virus_scan_result_descx);
                        }
                        String virusName = entity.getVirusName();
                        if (virusName == null) {
                            virusName = res.getString(R.string.hints_unknown_virus_name);
                        }

                        callback.onFindItem(resultType, itemType, pkgName, label.toString(),
                                appInfo.sourceDir, descx, virusName);
                    }
                }
            }
        }
        callback.onFinishScan();
    }

    private boolean mIsInLocalScanning = true;

    public void startScan(IFileProxy proxy, VirusScanCallback callback) {
        mVirusMap.clear();
        mRiskMap.clear();
        mIsInLocalScanning = true;

        PackageManager pm = mContext.getPackageManager();
        ExtraGuardHelper extraGuardHelper = ExtraGuardHelper.getInstance(mContext);
        Resources res = mContext.getResources();

        Map<String, PackageInfo> needCloudInstallPkgs = new HashMap<String, PackageInfo>();
        Map<String, PackageInfo> needCloudUninstalledApks = new HashMap<String, PackageInfo>();

        callback.onStartScan();

        // 已安装
        List<PackageInfo> installedPkgs = pm.getInstalledPackages(0);
        for (PackageInfo pkgInfo : installedPkgs) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                CharSequence label = appInfo.loadLabel(pm);
                String pkgName = pkgInfo.packageName;

                ExtraGuardVirusInfoEntity entity = extraGuardHelper.incrementalCheckInstalledApp(
                        appInfo, false);

                if (callback.onScanItem(label.toString())) {
                    callback.onCancelScan();
                    return;
                }

                if (entity != null) {
                    ScanItemType itemType = ScanItemType.INSTALLED_APP;
                    ScanResultType resultType = ScanResultType.SAFE;
                    int type = entity.getType();
                    if (ExtraGuardHelper.VIRUS_BLACK == type || ExtraGuardHelper.VIRUS_GRAY == type) {
                        if (ExtraGuardHelper.VIRUS_BLACK == type) {
                            resultType = ScanResultType.VIRUS;
                        } else if (ExtraGuardHelper.VIRUS_GRAY == type) {
                            resultType = ScanResultType.RISK;
                        }

                        String descx = entity.getDescription();
                        if (descx == null) {
                            descx = res.getString(R.string.hints_virus_scan_result_descx);
                        }
                        String virusName = entity.getVirusName();
                        if (virusName == null) {
                            virusName = res.getString(R.string.hints_unknown_virus_name);
                        }

                        callback.onFindItem(resultType, itemType, pkgName, label.toString(),
                                appInfo.sourceDir, descx, virusName);
                    } else {
                        needCloudInstallPkgs.put(appInfo.sourceDir, pkgInfo);
                    }
                }
            }
        }

        // 未安装
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            String volumeName = "external";
            String[] columes = new String[] {
                    FileColumns.DATA, FileColumns.DATE_MODIFIED
            };
            String selection = FileColumns.DATA + " LIKE '%.apk'";
            Uri uri = Files.getContentUri(volumeName);
            cursor = cr.query(uri, columes, selection, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String absolutePath = cursor.getString(0);
                    Log.d("miui", "==================absolutePath = " + absolutePath);
                    ProxyPackageInfo info = proxy.getPackageInfo(absolutePath);
                    if (info == null) {
                        continue;
                    }
                    String packageName = info.getPackageInfo().packageName;
                    CharSequence label = info.getLabel();
                    ExtraGuardVirusInfoEntity entity = extraGuardHelper
                            .incrementalCheckUninstalledApk(absolutePath, false);

                    if (callback.onScanItem(label.toString())) {
                        callback.onCancelScan();
                        return;
                    }

                    if (entity != null) {
                        ScanResultType resultType = ScanResultType.SAFE;
                        int type = entity.getType();
                        if (ExtraGuardHelper.VIRUS_BLACK == type
                                || ExtraGuardHelper.VIRUS_GRAY == type) {
                            if (ExtraGuardHelper.VIRUS_BLACK == type) {
                                resultType = ScanResultType.VIRUS;
                            } else if (ExtraGuardHelper.VIRUS_GRAY == type) {
                                resultType = ScanResultType.RISK;
                            }

                            String descx = entity.getDescription();
                            if (descx == null) {
                                descx = res.getString(R.string.hints_virus_scan_result_descx);
                            }
                            String virusName = entity.getVirusName();
                            if (virusName == null) {
                                virusName = res.getString(R.string.hints_unknown_virus_name);
                            }

                            callback.onFindItem(resultType, ScanItemType.UNINSTALLED_APK,
                                    packageName, label.toString(), absolutePath, descx, virusName);
                        } else {
                            needCloudUninstalledApks.put(absolutePath, info.getPackageInfo());
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        mIsInLocalScanning = false;

        if (!AndroidUtils.isNetConnected(mContext) || !Preferences.isVirusScanCloudEnabled()
                || !com.miui.securitycenter.Preferences.isConnectNetworkAlow()) {
            callback.onFinishScan();
            return;
        }

        callback.onCloudScanStart();

        try {
            // 云扫描已安装的
            String[] installedAppPaths = new String[needCloudInstallPkgs.size()];
            String[] types = new String[needCloudInstallPkgs.size()];
            ArrayList<ExtraGuardVirusInfoEntity> installedAppsResult = ExtraGuard
                    .checkSomeApkForVirusInfo(mContext, needCloudInstallPkgs.keySet()
                            .toArray(types));
            if (installedAppsResult != null) {
                for (ExtraGuardVirusInfoEntity entity : installedAppsResult) {
                    if (entity == null) {
                        continue;
                    }
                    int type = entity.getType();
                    if (ExtraGuardHelper.VIRUS_BLACK == type || ExtraGuardHelper.VIRUS_GRAY == type) {

                        ScanResultType resultType = ScanResultType.SAFE;
                        if (ExtraGuardHelper.VIRUS_BLACK == type) {
                            resultType = ScanResultType.VIRUS;
                        } else if (ExtraGuardHelper.VIRUS_GRAY == type) {
                            resultType = ScanResultType.RISK;
                        }

                        String descx = entity.getDescription();
                        if (descx == null) {
                            descx = res.getString(R.string.hints_virus_scan_result_descx);
                        }
                        String virusName = entity.getVirusName();
                        if (virusName == null) {
                            virusName = res.getString(R.string.hints_unknown_virus_name);
                        }

                        Set<String> keys = needCloudInstallPkgs.keySet();
                        for (String key : keys) {
                            PackageInfo pkgInfo = needCloudInstallPkgs.get(key);
                            if (TextUtils.equals(pkgInfo.packageName, entity.getPackageName())) {
                                if (!mIsInLocalScanning) {
                                    callback.onFindItem(resultType, ScanItemType.INSTALLED_APP,
                                            pkgInfo.packageName,
                                            pkgInfo.applicationInfo.loadLabel(pm).toString(),
                                            pkgInfo.applicationInfo.sourceDir, descx, virusName);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            // 云扫描未安装的
            String[] uninstalledAppPaths = new String[needCloudUninstalledApks.size()];
            String[] uninstalledTypes = new String[needCloudUninstalledApks.size()];
            ArrayList<ExtraGuardVirusInfoEntity> uninstalledAppsResult = ExtraGuard
                    .checkSomeApkForVirusInfo(mContext,
                            needCloudUninstalledApks.keySet().toArray(uninstalledTypes));
            if (uninstalledAppsResult != null) {
                for (ExtraGuardVirusInfoEntity entity : uninstalledAppsResult) {
                    if (entity == null) {
                        continue;
                    }
                    int type = entity.getType();
                    if (ExtraGuardHelper.VIRUS_BLACK == type || ExtraGuardHelper.VIRUS_GRAY == type) {

                        ScanResultType resultType = ScanResultType.SAFE;
                        if (ExtraGuardHelper.VIRUS_BLACK == type) {
                            resultType = ScanResultType.VIRUS;
                        } else if (ExtraGuardHelper.VIRUS_GRAY == type) {
                            resultType = ScanResultType.RISK;
                        }

                        String descx = entity.getDescription();
                        if (descx == null) {
                            descx = res.getString(R.string.hints_virus_scan_result_descx);
                        }
                        String virusName = entity.getVirusName();
                        if (virusName == null) {
                            virusName = res.getString(R.string.hints_unknown_virus_name);
                        }

                        Set<String> keys = needCloudUninstalledApks.keySet();
                        for (String key : keys) {
                            PackageInfo pkgInfo = needCloudUninstalledApks.get(key);
                            if (TextUtils.equals(pkgInfo.packageName, entity.getPackageName())) {
                                if (!mIsInLocalScanning) {
                                    callback.onFindItem(resultType, ScanItemType.UNINSTALLED_APK,
                                            pkgInfo.packageName,
                                            pkgInfo.applicationInfo.loadLabel(pm)
                                                    .toString(), key, descx, virusName);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (!mIsInLocalScanning) {
            callback.onFinishCloudScan();
            callback.onFinishScan();
        }
    }

    private final IPackageDeleteObserver.Stub mPackageDeleteObserver = new IPackageDeleteObserver.Stub() {
        @Override
        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
            // ignore
        }
    };

    public void startCleanup(IFileProxy proxy, VirusCleanupCallback callback) {
        PackageManager pm = mContext.getPackageManager();

        callback.onStartCleanup();

        Set<String> riskKeys = mRiskMap.keySet();
        for (String key : riskKeys) {
            VirusModel risk = mRiskMap.get(key);
            ScanItemType itemType = risk.getScanItemType();
            try {
                if (itemType == ScanItemType.INSTALLED_APP) {
                    pm.deletePackage(risk.getPkgName(), mPackageDeleteObserver, 0);
                } else {
                    proxy.deleteFileByPath(risk.getSourceDir());
                }
            } catch (Exception e) {
                // ignore
            }
            if (callback.onCleanupItem(risk.getAppLabel())) {
                callback.onCancelCleanup();
                return;
            }
        }
        mRiskMap.clear();

        Set<String> virusKeys = mVirusMap.keySet();
        for (String key : virusKeys) {
            VirusModel virus = mVirusMap.get(key);
            ScanItemType itemType = virus.getScanItemType();
            try {
                if (itemType == ScanItemType.INSTALLED_APP) {
                    pm.deletePackage(virus.getPkgName(), mPackageDeleteObserver, 0);
                } else {
                    proxy.deleteFileByPath(virus.getSourceDir());
                }
            } catch (Exception e) {
                // ignore
            }
            if (callback.onCleanupItem(virus.getAppLabel())) {
                callback.onCancelCleanup();
                return;
            }
        }
        mVirusMap.clear();

        callback.onFinishCleanup();
    }

    public void cleanupVirus(IFileProxy proxy, VirusModel model) {
        ScanItemType itemType = model.getScanItemType();
        try {
            if (itemType == ScanItemType.INSTALLED_APP) {
                mContext.getPackageManager().deletePackage(model.getPkgName(),
                        mPackageDeleteObserver, 0);
            } else {
                proxy.deleteFileByPath(model.getSourceDir());
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private Map<String, VirusModel> mVirusMap = new HashMap<String, VirusModel>();
    private Map<String, VirusModel> mRiskMap = new HashMap<String, VirusModel>();

    private boolean mNeedRefreshVirusInfo = false;

    public void clearRiskList() {
        mRiskMap.clear();
    }

    public void clearVirusList() {
        mVirusMap.clear();
    }

    public List<VirusModel> addRisk(VirusModel risk) {
        mRiskMap.put(risk.getSourceDir(), risk);
        return getRiskList();
    }

    public void removeRisk(VirusModel risk) {
        mRiskMap.remove(risk.getSourceDir());
    }

    public List<VirusModel> addVirus(VirusModel virus) {
        mVirusMap.put(virus.getSourceDir(), virus);
        return getVirusList();
    }

    public void removeVirus(VirusModel virus) {
        mVirusMap.remove(virus.getSourceDir());
    }

    public List<VirusModel> getRiskList() {
        List<VirusModel> riskList = new ArrayList<VirusModel>();
        Set<String> keys = mRiskMap.keySet();
        for (String key : keys) {
            riskList.add(mRiskMap.get(key));
        }
        return riskList;
    }

    public List<VirusModel> getVirusList() {
        List<VirusModel> virusList = new ArrayList<VirusModel>();
        Set<String> keys = mVirusMap.keySet();
        for (String key : keys) {
            virusList.add(mVirusMap.get(key));
        }
        return virusList;
    }

    public void setNeedRefreshVirusInfo(boolean needRefresh) {
        mNeedRefreshVirusInfo = needRefresh;
    }

    public boolean isNeedRefreshVirusInfo() {
        return mNeedRefreshVirusInfo;
    }
}
