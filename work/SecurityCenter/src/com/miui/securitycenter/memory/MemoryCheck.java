
package com.miui.securitycenter.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import miui.os.Environment;
import miui.text.ExtraTextUtils;

import com.miui.common.AndroidUtils;
import com.miui.securitycenter.R;
import com.miui.securitycenter.ScoreConstants;

public class MemoryCheck extends IMemoryCheck.Stub {

    public static final int CRITERION_MEMORY_BASE = 716800;
    public static final int CRITERION_MEMORY_ONE = 20;
    public static final int CRITERION_MEMORY_TWO = 35;
    public static final int CRITERION_MEMORY_THREE = 65;
    public static final int CRITERION_MEMORY_FOUR = 90;

    private static final int MAX_TASKS = 1001;

    private static final String PREFS_MEMORY_DATA = "memory_check";
    private static final String LOCKED_APPS_PREFERENCE_KEY = "pref_locked_pkgs";

    private class TaskInfo {

        public TaskInfo(String pkgName, int taskId, int lockState) {
            this.pkgName = pkgName;
            this.taskId = taskId;
            this.lockState = lockState;
        }

        String pkgName;
        int taskId;
        int lockState;
    }

    private class RunningAppInfo {
        public RunningAppInfo(String pkgName, long memeoryUsed, int lockState) {
            this.pkgName = pkgName;
            this.memeoryUsed = memeoryUsed;
            this.lockState = lockState;
        }

        String pkgName;
        long memeoryUsed;
        int lockState;
    }

    public static MemoryCheck INST;
    private Context mContext;
    private int mMemoryScore = ScoreConstants.SCORE_MEMORY_ONE;

    // pkg name
    private List<String> mWhiteList = new ArrayList<String>();

    // locked apps
    private Set<String> mLockedAppsSet = new HashSet<String>();

    private Map<String, RunningAppInfo> mRunningAppsMap = new HashMap<String, RunningAppInfo>();
    private Map<String, TaskInfo> mRecentTasksMap = new HashMap<String, TaskInfo>();

    private long mUsedMemory;
    private long mTotalMemory;

    private MemoryCheck(Context context) {
        mContext = context;
        mTotalMemory = Environment.getTotalPhysicalMemory() / 1024 - CRITERION_MEMORY_BASE;
        reloadLockedApps();
    }

    public static MemoryCheck getInstance(Context context) {
        if (INST == null) {
            INST = new MemoryCheck(context.getApplicationContext());
        }
        return INST;
    }

    public int getScore() {
        return mMemoryScore;
    }

    public void resetScore() {
        mMemoryScore = ScoreConstants.SCORE_MEMORY_ONE;
    }

    public CharSequence getCheckResult() {
        int count = 0;
        Set<String> pkgs = mRunningAppsMap.keySet();
        for (String pkg : pkgs) {
            RunningAppInfo appInfo = mRunningAppsMap.get(pkg);
            if (appInfo != null && appInfo.lockState != ScoreConstants.LOCK_STATE_LOCK) {
                ++count;
            }
        }

        if (count == 0) {
            return mContext.getString(R.string.memeory_check_content, count, "");
        }
        return mContext.getString(R.string.memeory_check_content, count,
                ExtraTextUtils.formatFileSize(mContext, mUsedMemory));
    }

    public void startScan(IMemoryScanCallback callback) {
        try {
            if (callback == null) {
                return;
            }

            ActivityManager am = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            PackageManager pm = mContext.getPackageManager();

            reloadLockedApps();
            reloadWhiteList();
            mRecentTasksMap.clear();
            mMemoryScore = ScoreConstants.SCORE_MEMORY_ONE;

            // get recent tasks
            List<RecentTaskInfo> tasks = am.getRecentTasks(MAX_TASKS,
                    ActivityManager.RECENT_IGNORE_UNAVAILABLE);
            for (int i = 0; i < tasks.size(); ++i) {
                RecentTaskInfo recentInfo = tasks.get(i);
                Intent intent = new Intent(recentInfo.baseIntent);
                if (recentInfo.origActivity != null) {
                    intent.setComponent(recentInfo.origActivity);
                }
                intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);

                if (resolveInfo != null && resolveInfo.activityInfo != null
                        && resolveInfo.activityInfo.packageName != null) {
                    String pkgName = resolveInfo.activityInfo.packageName;

                    if (i == 0) {
                        mWhiteList.add(pkgName);
                    }

                    if (!mWhiteList.contains(pkgName)) {
                        int lockState = mLockedAppsSet.contains(pkgName) ? ScoreConstants.LOCK_STATE_LOCK
                                : ScoreConstants.LOCK_STATE_UNLOCK;
                        TaskInfo info = new TaskInfo(pkgName, recentInfo.persistentId, lockState);
                        mRecentTasksMap.put(pkgName, info);
                    }
                }
            }

            // get running processes
            mUsedMemory = 0;
            mRunningAppsMap.clear();

            List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            for (RunningAppProcessInfo proc : processes) {
                String pkgName = proc.pkgList != null ? proc.pkgList[0] : null;
                int[] pids = new int[1];
                pids[0] = proc.pid;
                try {
                    long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
                    if (pkgName != null) {
                        ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                        if (!mWhiteList.contains(pkgName)
                                && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            RunningAppInfo runningInfo = mRunningAppsMap.get(pkgName);

                            long memory = pss[0] * 1024;

                            if (runningInfo == null) {
                                runningInfo = new RunningAppInfo(pkgName, 0,
                                        ScoreConstants.LOCK_STATE_UNLOCK);
                                mRunningAppsMap.put(pkgName, runningInfo);
                            }

                            if (mLockedAppsSet.contains(pkgName)) {
                                runningInfo.lockState = ScoreConstants.LOCK_STATE_LOCK;
                            } else {
                                runningInfo.lockState = ScoreConstants.LOCK_STATE_UNLOCK;
                            }

                            runningInfo.memeoryUsed = memory;

                            if (runningInfo.lockState != ScoreConstants.LOCK_STATE_LOCK) {
                                mUsedMemory += runningInfo.memeoryUsed;
                            }
                            calculateScore();

                        }
                    }
                } catch (RemoteException e) {
                    // ignore
                } catch (NameNotFoundException e) {
                    // ignore
                }
            }

            callback.onStartScan(mRunningAppsMap.keySet().size());
            for (String pkgName : mRunningAppsMap.keySet()){
                if (callback.onScanItem(AndroidUtils.getAppName(mContext, pkgName)
                        .toString())) {
                    return;
                }
            }
            callback.onFinishScan();
        } catch (RemoteException e) {
            // ignore
        }
    }

    public void startCleanup(IMemoryCleanupCallback callback) {
        try {
            if (callback == null) {
                return;
            }

            callback.onStartCleanup();

            ActivityManager am = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            PackageManager pm = mContext.getPackageManager();

            // clear recent tasks
            Map<String, TaskInfo> targetRecentMap = new HashMap<String, TaskInfo>(mRecentTasksMap);
            Set<String> recentPkgs = targetRecentMap.keySet();
            for (String pkg : recentPkgs) {
                TaskInfo taskInfo = targetRecentMap.get(pkg);
                if (taskInfo.lockState != ScoreConstants.LOCK_STATE_LOCK) {
                    am.removeTask(taskInfo.taskId, 0);
                    mRecentTasksMap.remove(pkg);
                }
            }

            // clear running processes
            Map<String, RunningAppInfo> targetRunningMap = new HashMap<String, RunningAppInfo>(
                    mRunningAppsMap);
            Set<String> runningPkgs = targetRunningMap.keySet();
            for (String pkg : runningPkgs) {
                RunningAppInfo runningInfo = targetRunningMap.get(pkg);
                if (runningInfo.lockState != ScoreConstants.LOCK_STATE_LOCK) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(runningInfo.pkgName, 0);
                        if (appInfo != null
                                && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            am.forceStopPackage(runningInfo.pkgName);
                        } else {
                            am.killBackgroundProcesses(runningInfo.pkgName);
                        }

                        mRunningAppsMap.remove(pkg);
                        mUsedMemory -= runningInfo.memeoryUsed;
                        calculateScore();

                        callback.onCleanupItem(AndroidUtils.getAppName(mContext, pkg).toString());
                    } catch (NameNotFoundException e) {
                        // ignore
                    }
                }
            }

            mUsedMemory = 0;
            callback.onFinishCleanup();
        } catch (RemoteException e) {
            // ignore
        }
    }

    public List<String> getRunningApps() {
        List<String> pkgs = new ArrayList<String>();
        Map<String, RunningAppInfo> targteMap = new HashMap<String, RunningAppInfo>(mRunningAppsMap);
        Set<String> keys = targteMap.keySet();
        for (String pkg : keys) {
            pkgs.add(pkg);
        }
        return pkgs;
    }

    public long getMemoryUsed(String pkgName) {
        RunningAppInfo runningInfo = mRunningAppsMap.get(pkgName);
        if (runningInfo == null) {
            return 0;
        }

        return runningInfo.memeoryUsed;
    }

    @Override
    public void setAppLockState(String pkgName, int state) {
        if (state != ScoreConstants.LOCK_STATE_LOCK && state != ScoreConstants.LOCK_STATE_UNLOCK) {
            return;
        }

        RunningAppInfo runningInfo = mRunningAppsMap.get(pkgName);
        if (runningInfo != null && runningInfo.lockState != state) {
            runningInfo.lockState = state;
            if (state == ScoreConstants.LOCK_STATE_LOCK) {
                mUsedMemory -= runningInfo.memeoryUsed;
            } else if (state == ScoreConstants.LOCK_STATE_UNLOCK) {
                mUsedMemory += runningInfo.memeoryUsed;
            }
            calculateScore();
        }

        TaskInfo taskInfo = mRecentTasksMap.get(pkgName);
        if (taskInfo != null) {
            taskInfo.lockState = state;
        }

        if (state == ScoreConstants.LOCK_STATE_LOCK) {
            if (!mLockedAppsSet.contains(pkgName)) {
                mLockedAppsSet.add(pkgName);
            }
        } else if (state == ScoreConstants.LOCK_STATE_UNLOCK) {
            if (mLockedAppsSet.contains(pkgName)) {
                mLockedAppsSet.remove(pkgName);
            }
        }

        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_MEMORY_DATA,
                Context.MODE_PRIVATE);
        prefs.edit().putStringSet(LOCKED_APPS_PREFERENCE_KEY, mLockedAppsSet).apply();
        mLockedAppsSet = new HashSet<String>(mLockedAppsSet);
    }

    public List<String> getLockedComponents() {
        if (mLockedAppsSet.isEmpty()) {
            reloadLockedApps();
        }

        List<String> lockedComponents = new ArrayList<String>();
        for (String lockedApp : mLockedAppsSet) {
            lockedComponents.add(lockedApp);
        }
        return lockedComponents;
    }

    @Override
    public int getAppLockState(String pkgName) {
        if (pkgName == null) {
            return ScoreConstants.LOCK_STATE_ERROR;
        }
        if (mLockedAppsSet.contains(pkgName)) {
            return ScoreConstants.LOCK_STATE_LOCK;
        } else {
            return ScoreConstants.LOCK_STATE_UNLOCK;
        }
    }

    public int getMemoryScanCount() {
        int scan_cnt = 0;
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = mContext.getPackageManager();

        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        for (RunningAppProcessInfo proc : processes) {
            String pkgName = proc.pkgList != null ? proc.pkgList[0] : null;
            int[] pids = new int[1];
            pids[0] = proc.pid;
            try {
                long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
                if (pkgName != null) {
                    ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                    if (!mWhiteList.contains(pkgName)
                            && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        scan_cnt++;
                    }
                }
            } catch (RemoteException e) {
                // ignore
            } catch (NameNotFoundException e) {
                // ignore
            }
        }
        return scan_cnt;
    }

    private void calculateScore() {
        int proportion = (int) (mUsedMemory * 100 / mTotalMemory);

        if (proportion >= CRITERION_MEMORY_FOUR) {
            mMemoryScore = ScoreConstants.SCORE_MEMORY_FIVE;
        } else if (proportion >= CRITERION_MEMORY_THREE) {
            mMemoryScore = ScoreConstants.SCORE_MEMORY_FOUR;
        } else if (proportion >= CRITERION_MEMORY_TWO) {
            mMemoryScore = ScoreConstants.SCORE_MEMORY_THREE;
        } else if (proportion >= CRITERION_MEMORY_ONE) {
            mMemoryScore = ScoreConstants.SCORE_MEMORY_TWO;
        } else {
            mMemoryScore = ScoreConstants.SCORE_MEMORY_ONE;
        }
    }

    private void reloadLockedApps() {
        // locked apps
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_MEMORY_DATA,
                Context.MODE_PRIVATE);
        Set<String> lockedApps = prefs.getStringSet(LOCKED_APPS_PREFERENCE_KEY,
                new HashSet<String>());
        mLockedAppsSet = new HashSet<String>(lockedApps);
    }

    private boolean isLockedApp(String pkgName) {
        return mLockedAppsSet.contains(pkgName);
    }

    private void reloadWhiteList() {
        mWhiteList.clear();

        // android system
        mWhiteList.add("android");
        // media provider
        mWhiteList.add("com.android.providers.media");
        // desk clock widget
        mWhiteList.add("com.android.deskclock");

        // Default home
        ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .resolveActivityInfo(mContext.getPackageManager(), 0);
        if (homeInfo != null) {
            mWhiteList.add(homeInfo.packageName);
        }
        // Default input method
        String inputMethodId = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        if (!TextUtils.isEmpty(inputMethodId)) {
            mWhiteList.add(inputMethodId.substring(0, inputMethodId.indexOf('/')));
        }
        // Current live wallpaper
        WallpaperInfo wInfo = WallpaperManager.getInstance(mContext).getWallpaperInfo();
        if (wInfo != null) {
            mWhiteList.add(wInfo.getPackageName());
        }
    }

    private boolean inWhiteList(String pkgName) {
        return mWhiteList.contains(pkgName);
    }
}
