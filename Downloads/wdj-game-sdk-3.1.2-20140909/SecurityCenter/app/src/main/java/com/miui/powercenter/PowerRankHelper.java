
package com.miui.powercenter;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.os.BatteryStats.Uid;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.SignalStrength;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.miui.securitycenter.R;
import com.miui.common.AndroidUtils;
import com.miui.internal.os.BatteryStatsHelper;
import com.miui.powercenter.PowerDetailActivity.DrainType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PowerRankHelper implements Runnable {

    public static final String TAG = PowerRankFragment.class.getName();
    protected static final boolean DEBUG = false;

    private long mStatsPeriod = 0;
    protected double mMaxPower = 1;
    protected double mTotalPower;
    protected double mAppPower;
    protected double mMiscPower;
    private double mWifiPower;
    private double mBluetoothPower;

    // How much the apps together have left WIFI running.
    private long mAppWifiRunning;

    IBatteryStats mBatteryInfo;
    protected PowerProfile mPowerProfile;
    protected int mStatsType = BatteryStats.STATS_SINCE_CHARGED;
    BatteryStatsImpl mStats;
    private BatteryStatsHelper mStatsHelper;

    private final List<BatterySipper> mUsageList = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mAppUsageList = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mMiscUsageList = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mWifiSippers = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mBluetoothSippers = new ArrayList<BatterySipper>();

    /** Queue for fetching name and icon for an application */
    private ArrayList<BatterySipper> mRequestQueue = new ArrayList<BatterySipper>();
    private Thread mRequestThread;
    private boolean mAbort = false;
    private Handler ParentHandler;

    public PowerRankHelper(Context context , Handler handler) {
        ParentHandler = handler;
        mPowerProfile = new PowerProfile(context);
        mStatsHelper = new BatteryStatsHelper();
        mBatteryInfo = mStatsHelper.getBatteryStats();
    }

    public void onResume(Context context) {
        mAbort = false;
        load();
        refreshStats(context);
    }

    public void onPause() {
        synchronized (mRequestQueue) {
            mAbort = true;
        }
        mHandler.removeMessages(MSG_UPDATE_NAME_ICON);
    }

    private void load() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);
            mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }

    private void refreshStats(Context context) {
        mMaxPower = 0;
        mTotalPower = 0;
        mAppPower = 0;
        mMiscPower = 0;
        mWifiPower = 0;
        mBluetoothPower = 0;
        mAppWifiRunning = 0;

        mUsageList.clear();
        mAppUsageList.clear();
        mMiscUsageList.clear();
        mWifiSippers.clear();
        mBluetoothSippers.clear();

        processAppUsage(context);
        processMiscUsage(context);

        Collections.sort(mUsageList);
        Collections.sort(mAppUsageList);
        Collections.sort(mMiscUsageList);
        synchronized (mRequestQueue) {
            if (!mRequestQueue.isEmpty()) {
                if (mRequestThread == null) {
                    mRequestThread = new Thread(this, "BatteryUsage Icon Loader");
                    mRequestThread.setPriority(Thread.MIN_PRIORITY);
                    mRequestThread.start();
                }
                mRequestQueue.notify();
            }
        }
    }

    private BatterySipper addEntry(Context context, String label, DrainType drainType, long time,
            int iconId, double power) {
        if (power > mMaxPower)
            mMaxPower = power;
        mTotalPower += power;
        mMiscPower += power;
        BatterySipper bs = new BatterySipper(context, mRequestQueue, mHandler,
                label, drainType, iconId, null, new double[] {
                        power
                });
        bs.usageTime = time;
        bs.iconId = iconId;
        mMiscUsageList.add(bs);
        mUsageList.add(bs);
        return bs;
    }

    private double getAverageDataCost() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
                                       // system
        final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from
                                        // system
        final double WIFI_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE)
                / 3600;
        final double MOBILE_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
                / 3600;
        final long mobileData = (long) mStatsHelper.getMobilePowerPerByte(mStats, mPowerProfile,
                mStatsType);
        final long wifiData = (long) mStatsHelper.getWifiPowerPerByte(mStats, mPowerProfile,
                mStatsType);
        final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
        final long mobileBps = radioDataUptimeMs != 0
                ? mobileData * 8 * 1000 / radioDataUptimeMs
                : MOBILE_BPS;

        double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
        double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);
        if (wifiData + mobileData != 0) {
            return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
                    / (mobileData + wifiData);
        } else {
            return 0;
        }
    }

    private void processAppUsage(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(
                Context.SENSOR_SERVICE);
        final int which = mStatsType;
        final int speedSteps = mPowerProfile.getNumSpeedSteps();
        final double[] powerCpuNormal = new double[speedSteps];
        final long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p = 0; p < speedSteps; p++) {
            powerCpuNormal[p] = mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);
        }
        final double averageCostPerByte = getAverageDataCost();
        long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, which);
        long appWakelockTime = 0;
        BatterySipper osApp = null;
        mStatsPeriod = uSecTime;
        SparseArray<? extends Uid> uidStats = mStats.getUidStats();
        final int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            double power = 0;
            double highestDrain = 0;
            String packageWithHighestDrain = null;
            // mUsageList.add(new AppUsage(u.getUid(), new double[] {power}));
            Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
            long cpuTime = 0;
            long cpuFgTime = 0;
            long wakelockTime = 0;
            long gpsTime = 0;
            if (processStats.size() > 0) {
                // Process CPU time
                for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent : processStats
                        .entrySet()) {
                    if (DEBUG)
                        Log.i(TAG, "Process name = " + ent.getKey());
                    Uid.Proc ps = ent.getValue();
                    final long userTime = ps.getUserTime(which);
                    final long systemTime = ps.getSystemTime(which);
                    final long foregroundTime = ps.getForegroundTime(which);
                    cpuFgTime += foregroundTime * 10; // convert to millis
                    final long tmpCpuTime = (userTime + systemTime) * 10; // convert
                                                                          // to
                                                                          // millis
                    int totalTimeAtSpeeds = 0;
                    // Get the total first
                    for (int step = 0; step < speedSteps; step++) {
                        cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, which);
                        totalTimeAtSpeeds += cpuSpeedStepTimes[step];
                    }
                    if (totalTimeAtSpeeds == 0)
                        totalTimeAtSpeeds = 1;
                    // Then compute the ratio of time spent at each speed
                    double processPower = 0;
                    for (int step = 0; step < speedSteps; step++) {
                        double ratio = (double) cpuSpeedStepTimes[step] / totalTimeAtSpeeds;
                        processPower += ratio * tmpCpuTime * powerCpuNormal[step];
                    }
                    cpuTime += tmpCpuTime;
                    power += processPower;
                    if (packageWithHighestDrain == null
                            || packageWithHighestDrain.startsWith("*")) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    } else if (highestDrain < processPower
                            && !ent.getKey().startsWith("*")) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    }
                }
                if (DEBUG)
                    Log.i(TAG, "Max drain of " + highestDrain
                            + " by " + packageWithHighestDrain);
            }
            if (cpuFgTime > cpuTime) {
                if (DEBUG && cpuFgTime > cpuTime + 10000) {
                    Log.i(TAG, "WARNING! Cputime is more than 10 seconds behind Foreground time");
                }
                cpuTime = cpuFgTime; // Statistics may not have been gathered
                                     // yet.
            }
            power /= 1000;

            // Process wake lock usage
            Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u.getWakelockStats();
            for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry : wakelockStats
                    .entrySet()) {
                Uid.Wakelock wakelock = wakelockEntry.getValue();
                // Only care about partial wake locks since full wake locks
                // are canceled when the user turns the screen off.
                BatteryStats.Timer timer = wakelock.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
                if (timer != null) {
                    wakelockTime += timer.getTotalTimeLocked(uSecTime, which);
                }
            }
            wakelockTime /= 1000; // convert to millis
            appWakelockTime += wakelockTime;

            // Add cost of holding a wake lock
            power += (wakelockTime
                    * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;

            // Add cost of data traffic
            long tcpBytesReceived = mStatsHelper.getTcpBytesReceived(u, mStatsType);
            long tcpBytesSent = mStatsHelper.getTcpBytesSent(u, mStatsType);
            power += (tcpBytesReceived + tcpBytesSent) * averageCostPerByte;

            // Add cost of keeping WIFI running.
            long wifiRunningTimeMs = u.getWifiRunningTime(uSecTime, which) / 1000;
            mAppWifiRunning += wifiRunningTimeMs;
            power += (wifiRunningTimeMs
                    * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;

            // Process Sensor usage
            Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u.getSensorStats();
            for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry : sensorStats
                    .entrySet()) {
                Uid.Sensor sensor = sensorEntry.getValue();
                int sensorType = sensor.getHandle();
                BatteryStats.Timer timer = sensor.getSensorTime();
                long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
                double multiplier = 0;
                switch (sensorType) {
                    case Uid.Sensor.GPS:
                        multiplier = mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
                        gpsTime = sensorTime;
                        break;
                    default:
                        android.hardware.Sensor sensorData =
                                sensorManager.getDefaultSensor(sensorType);
                        if (sensorData != null) {
                            multiplier = sensorData.getPower();
                            if (DEBUG) {
                                Log.i(TAG, "Got sensor " + sensorData.getName() + " with power = "
                                        + multiplier);
                            }
                        }
                }
                power += (multiplier * sensorTime) / 1000;
            }

            if (DEBUG)
                Log.i(TAG, "UID " + u.getUid() + ": power=" + power);

            // Add the app to the list if it is consuming power
            if (power != 0 || u.getUid() == 0) {
                BatterySipper app = new BatterySipper(context, mRequestQueue, mHandler,
                        packageWithHighestDrain, DrainType.APP, 0, u,
                        new double[] {
                                power
                        });
                app.cpuTime = cpuTime;
                app.gpsTime = gpsTime;
                app.wifiRunningTime = wifiRunningTimeMs;
                app.cpuFgTime = cpuFgTime;
                app.wakeLockTime = wakelockTime;
                app.tcpBytesReceived = tcpBytesReceived;
                app.tcpBytesSent = tcpBytesSent;
                if (u.getUid() == Process.WIFI_UID) {
                    mWifiSippers.add(app);
                } else if (u.getUid() == Process.BLUETOOTH_UID) {
                    mBluetoothSippers.add(app);
                } else {
                    mAppUsageList.add(app);
                    mUsageList.add(app);
                }
                if (u.getUid() == 0) {
                    osApp = app;
                }
            }
            if (u.getUid() == Process.WIFI_UID) {
                mWifiPower += power;
            } else if (u.getUid() == Process.BLUETOOTH_UID) {
                mBluetoothPower += power;
            } else {
                if (power > mMaxPower)
                    mMaxPower = power;
                mTotalPower += power;
                mAppPower += power;
            }
            if (DEBUG)
                Log.i(TAG, "Added power = " + power);
        }

        // The device has probably been awake for longer than the screen on
        // time and application wake lock time would account for. Assign
        // this remainder to the OS, if possible.
        if (osApp != null) {
            long wakeTimeMillis = mStats.computeBatteryUptime(
                    SystemClock.uptimeMillis() * 1000, which) / 1000;
            wakeTimeMillis -= appWakelockTime + (mStats.getScreenOnTime(
                    SystemClock.elapsedRealtime(), which) / 1000);
            if (wakeTimeMillis > 0) {
                double power = (wakeTimeMillis
                        * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
                if (DEBUG)
                    Log.i(TAG, "OS wakeLockTime " + wakeTimeMillis + " power " + power);
                osApp.wakeLockTime += wakeTimeMillis;
                osApp.value += power;
                osApp.values[0] += power;
                if (osApp.value > mMaxPower)
                    mMaxPower = osApp.value;
                mTotalPower += power;
                mAppPower += power;
            }
        }
    }

    private void processMiscUsage(Context context) {
        final int which = mStatsType;
        long uSecTime = SystemClock.elapsedRealtime() * 1000;
        final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
        final long timeSinceUnplugged = uSecNow;
        if (DEBUG) {
            Log.i(TAG, "Uptime since last unplugged = " + (timeSinceUnplugged / 1000));
        }

        addPhoneUsage(context, uSecNow);
        addScreenUsage(context, uSecNow);
        addWiFiUsage(context, uSecNow);
        addBluetoothUsage(context, uSecNow);
        addIdleUsage(context, uSecNow); // Not including cellular idle power
        // Don't compute radio usage if it's a wifi-only device
        if (!AndroidUtils.isWifiOnly(context)) {
            addRadioUsage(context, uSecNow);
        }
    }

    private void addPhoneUsage(Context context, long uSecNow) {
        long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
        double phoneOnPower = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
                * phoneOnTimeMs / 1000;
        addEntry(context, context.getString(R.string.power_phone), DrainType.PHONE, phoneOnTimeMs,
                R.drawable.power_consume_call, phoneOnPower);
    }

    private void addScreenUsage(Context context, long uSecNow) {
        double power = 0;
        long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
        power += screenOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
        final double screenFullPower =
                mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            double screenBinPower = screenFullPower * (i + 0.5f)
                    / BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
            long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow, mStatsType) / 1000;
            power += screenBinPower * brightnessTime;
            if (DEBUG) {
                Log.i(TAG, "Screen bin power = " + (int) screenBinPower + ", time = "
                        + brightnessTime);
            }
        }
        power /= 1000; // To seconds
        addEntry(context, context.getString(R.string.power_screen), DrainType.SCREEN,
                screenOnTimeMs,
                R.drawable.power_consume_screen, power);
    }

    private void addWiFiUsage(Context context, long uSecNow) {
        long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
        long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow, mStatsType) / 1000;
        if (DEBUG)
            Log.i(TAG, "WIFI runningTime=" + runningTimeMs
                    + " app runningTime=" + mAppWifiRunning);
        runningTimeMs -= mAppWifiRunning;
        if (runningTimeMs < 0)
            runningTimeMs = 0;
        double wifiPower = (onTimeMs * 0 /* TODO */
                * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)
                + runningTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
        if (DEBUG)
            Log.i(TAG, "WIFI power=" + wifiPower + " from procs=" + mWifiPower);
        BatterySipper bs = addEntry(context, context.getString(R.string.power_wifi),
                DrainType.WIFI,
                runningTimeMs, R.drawable.power_consume_wifi, wifiPower + mWifiPower);
        aggregateSippers(bs, mWifiSippers, "WIFI");
    }

    private void addBluetoothUsage(Context context, long uSecNow) {
        long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
        double btPower = btOnTimeMs
                * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
                / 1000;
        int btPingCount = mStats.getBluetoothPingCount();
        btPower += (btPingCount
                * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;
        BatterySipper bs = addEntry(context, context.getString(R.string.power_bluetooth),
                DrainType.BLUETOOTH, btOnTimeMs, R.drawable.power_consume_bluetooth,
                btPower + mBluetoothPower);
        aggregateSippers(bs, mBluetoothSippers, "Bluetooth");
    }

    private void addIdleUsage(Context context, long uSecNow) {
        long idleTimeMs = (uSecNow - mStats.getScreenOnTime(uSecNow, mStatsType)) / 1000;
        double idlePower = (idleTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))
                / 1000;
        addEntry(context, context.getString(R.string.power_idle), DrainType.IDLE, idleTimeMs,
                R.drawable.power_consume_idle, idlePower);
    }

    private void addRadioUsage(Context context, long uSecNow) {
        double power = 0;
        final int BINS = SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
        long signalTimeMs = 0;
        for (int i = 0; i < BINS; i++) {
            long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow, mStatsType) / 1000;
            power += strengthTimeMs / 1000
                    * mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i);
            signalTimeMs += strengthTimeMs;
        }
        long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow, mStatsType) / 1000;
        power += scanningTimeMs / 1000 * mPowerProfile.getAveragePower(
                PowerProfile.POWER_RADIO_SCANNING);
        BatterySipper bs =
                addEntry(context, context.getString(R.string.power_cell), DrainType.CELL,
                        signalTimeMs, R.drawable.power_consume_signal, power);
        if (signalTimeMs != 0) {
            bs.noCoveragePercent = mStats.getPhoneSignalStrengthTime(0, uSecNow, mStatsType)
                    / 1000 * 100.0 / signalTimeMs;
        }
    }

    private void aggregateSippers(BatterySipper bs, List<BatterySipper> from, String tag) {
        for (int i = 0; i < from.size(); i++) {
            BatterySipper wbs = from.get(i);
            if (DEBUG)
                Log.i(TAG, tag + " adding sipper " + wbs + ": cpu=" + wbs.cpuTime);
            bs.cpuTime += wbs.cpuTime;
            bs.gpsTime += wbs.gpsTime;
            bs.wifiRunningTime += wbs.wifiRunningTime;
            bs.cpuFgTime += wbs.cpuFgTime;
            bs.wakeLockTime += wbs.wakeLockTime;
            bs.tcpBytesReceived += wbs.tcpBytesReceived;
            bs.tcpBytesSent += wbs.tcpBytesSent;
        }
    }

    public void startPowerDetail(Context context, BatterySipper sipper) {
        Bundle args = new Bundle();
        args.putString(PowerDetailActivity.EXTRA_TITLE, sipper.name);
        args.putInt(PowerDetailActivity.EXTRA_PERCENT, (int) Math.round(sipper.percent));
        args.putInt(PowerDetailActivity.EXTRA_GAUGE, (int)
                Math.ceil(sipper.getSortValue() * 100 / mMaxPower));
        args.putLong(PowerDetailActivity.EXTRA_USAGE_DURATION, mStatsPeriod);
        args.putString(PowerDetailActivity.EXTRA_ICON_PACKAGE, sipper.defaultPackageName);
        args.putInt(PowerDetailActivity.EXTRA_ICON_ID, sipper.iconId);
        args.putDouble(PowerDetailActivity.EXTRA_NO_COVERAGE, sipper.noCoveragePercent);
        if (sipper.uidObj != null) {
            args.putInt(PowerDetailActivity.EXTRA_UID, sipper.uidObj.getUid());
        }
        args.putSerializable(PowerDetailActivity.EXTRA_DRAIN_TYPE, sipper.drainType);

        int[] types;
        double[] values;
        switch (sipper.drainType) {
            case APP: {
                Uid uid = sipper.uidObj;
                types = new int[] {
                        R.string.usage_type_cpu,
                        R.string.usage_type_cpu_foreground,
                        R.string.usage_type_wake_lock,
                        R.string.usage_type_gps,
                        R.string.usage_type_wifi_running,
                        R.string.usage_type_data_send,
                        R.string.usage_type_data_recv,
                        R.string.usage_type_audio,
                        R.string.usage_type_video,
                };
                values = new double[] {
                        sipper.cpuTime,
                        sipper.cpuFgTime,
                        sipper.wakeLockTime,
                        sipper.gpsTime,
                        sipper.wifiRunningTime,
                        sipper.tcpBytesSent,
                        sipper.tcpBytesReceived,
                        0,
                        0
                };

                Writer result = new StringWriter();
                PrintWriter printWriter = new PrintWriter(result);
                mStats.dumpLocked(printWriter, "", mStatsType, uid.getUid());
                args.putString(PowerDetailActivity.EXTRA_REPORT_DETAILS, result.toString());

                result = new StringWriter();
                printWriter = new PrintWriter(result);
                mStats.dumpCheckinLocked(printWriter, mStatsType, uid.getUid());
                args.putString(PowerDetailActivity.EXTRA_REPORT_CHECKIN_DETAILS, result.toString());
            }
                break;
            case CELL: {
                types = new int[] {
                        R.string.usage_type_on_time,
                        R.string.usage_type_no_coverage
                };
                values = new double[] {
                        sipper.usageTime,
                        sipper.noCoveragePercent
                };
            }
                break;
            case WIFI: {
                types = new int[] {
                        R.string.usage_type_wifi_running,
                        R.string.usage_type_cpu,
                        R.string.usage_type_cpu_foreground,
                        R.string.usage_type_wake_lock,
                        R.string.usage_type_data_send,
                        R.string.usage_type_data_recv,
                };
                values = new double[] {
                        sipper.usageTime,
                        sipper.cpuTime,
                        sipper.cpuFgTime,
                        sipper.wakeLockTime,
                        sipper.tcpBytesSent,
                        sipper.tcpBytesReceived,
                };
            }
                break;
            case BLUETOOTH: {
                types = new int[] {
                        R.string.usage_type_on_time,
                        R.string.usage_type_cpu,
                        R.string.usage_type_cpu_foreground,
                        R.string.usage_type_wake_lock,
                        R.string.usage_type_data_send,
                        R.string.usage_type_data_recv,
                };
                values = new double[] {
                        sipper.usageTime,
                        sipper.cpuTime,
                        sipper.cpuFgTime,
                        sipper.wakeLockTime,
                        sipper.tcpBytesSent,
                        sipper.tcpBytesReceived,
                };
            }
                break;
            default: {
                types = new int[] {
                        R.string.usage_type_on_time
                };
                values = new double[] {
                        sipper.usageTime
                };
            }
        }
        args.putIntArray(PowerDetailActivity.EXTRA_DETAIL_TYPES, types);
        args.putDoubleArray(PowerDetailActivity.EXTRA_DETAIL_VALUES, values);
        Intent intent = new Intent(context, PowerDetailActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    static final int MSG_UPDATE_NAME_ICON = 1;
    static final int MSG_REPORT_FULLY_DRAWN = 2;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_NAME_ICON:
                    BatterySipper bs = (BatterySipper) msg.obj;
                    for (BatterySipperListener listener : mListeners) {
                        listener.OnUpdate();
                    }
                case MSG_REPORT_FULLY_DRAWN:
                    ParentHandler.sendEmptyMessage(MSG_REPORT_FULLY_DRAWN);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    static interface BatterySipperListener {
        void OnUpdate();
    }

    List<BatterySipperListener> mListeners = new ArrayList<BatterySipperListener>();

    public void addBatterySipperListener(BatterySipperListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void run() {
        while (true) {
            BatterySipper bs;
            synchronized (mRequestQueue) {
                if (mRequestQueue.isEmpty() || mAbort) {
                    mRequestThread = null;
                    mHandler.sendEmptyMessage(MSG_REPORT_FULLY_DRAWN);
                    return;
                }
                bs = mRequestQueue.remove(0);
            }
            bs.getNameIcon();
        }
    }

    public List<BatterySipper> getAppUsageList() {
        return mAppUsageList;
    }

    public List<BatterySipper> getMiscUsageList() {
        return mMiscUsageList;
    }

    public List<BatterySipper> getUsageList() {
        return mUsageList;
    }

    public double getAppUsageTotle() {
        return mAppPower;
    }

    public double getMiscUsageTotle() {
        return mMiscPower;
    }

    public double getUsageTotle() {
        return mTotalPower;
    }
}
