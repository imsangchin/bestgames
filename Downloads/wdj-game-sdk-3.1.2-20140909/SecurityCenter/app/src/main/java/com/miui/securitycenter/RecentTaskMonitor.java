
package com.miui.securitycenter;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

public class RecentTaskMonitor {
    static final String TAG = RecentTaskMonitor.class.getSimpleName();

    public enum TopTask {
        SELF, OTHER, UNKNOWN
    }

    public interface RecentTaskListener {
        void onTopTaskChanged(TopTask topTask);
    }

    private static RecentTaskMonitor INST;

    private static final String SECURITYCENTER_PROCESS = "com.miui.securitycenter";

    private static final long MONITOR_DELAY = 1000;

    private RecentTaskListener mRecentTaskListener;
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private Looper mLooper;
    private Handler mHandler;

    private final Runnable mMonitorRunnable = new Runnable() {

        @Override
        public void run() {
            mRecentTaskListener.onTopTaskChanged(isSelfProcessTop() ? TopTask.SELF : TopTask.OTHER);
            if (mHandler != null) {
                mHandler.postDelayed(this, MONITOR_DELAY);
            }
        }
    };

    public boolean isSelfProcessTop() {
        List<RecentTaskInfo> tasks = mActivityManager.getRecentTasks(1,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);

        if (!tasks.isEmpty()) {
            RecentTaskInfo topTask = tasks.get(0);
            Intent intent = new Intent(topTask.baseIntent);
            if (topTask.origActivity != null) {
                intent.setComponent(topTask.origActivity);
            }
            intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
            if (resolveInfo != null && resolveInfo.activityInfo != null
                    && resolveInfo.activityInfo.packageName != null) {
                String processName = resolveInfo.activityInfo.processName;
                return TextUtils.equals(processName, SECURITYCENTER_PROCESS);
            }
        }
        return false;
    }

    private RecentTaskMonitor() {
        // ignore
    }

    private RecentTaskMonitor(Context context, RecentTaskListener listener) {
        mRecentTaskListener = listener;
        mPackageManager = context.getPackageManager();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public static RecentTaskMonitor getInstance() {
        if (INST == null) {
            INST = new RecentTaskMonitor();
        }
        return INST;
    }

    public static RecentTaskMonitor getInstance(Context context, RecentTaskListener listener) {
        if (INST == null) {
            INST = new RecentTaskMonitor(context.getApplicationContext(), listener);
        }
        return INST;
    }

    public synchronized void start() {
        if (mRecentTaskListener == null || mLooper != null) {
            return;
        }
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mLooper = thread.getLooper();
        mHandler = new Handler(mLooper);
        mHandler.post(mMonitorRunnable);
    }

    public synchronized void resume() {
        if (mRecentTaskListener == null || mLooper == null || mHandler == null) {
            return;
        }
        mHandler.post(mMonitorRunnable);
    }

    public synchronized void pause() {
        if (mRecentTaskListener == null || mLooper == null || mHandler == null) {
            return;
        }
        mHandler.removeCallbacks(mMonitorRunnable);
    }

    public synchronized void stop() {
        try {
            if (mLooper != null) {
                mLooper.quit();
                mLooper = null;
            }
            mHandler.removeCallbacks(mMonitorRunnable);
            mHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
