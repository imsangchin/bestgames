
package com.miui.powercenter;

import java.util.Arrays;
import java.util.List;

import miui.content.res.IconCustomizer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import miui.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miui.securitycenter.R;
import android.content.pm.IPackageDeleteObserver;
import com.miui.internal.os.BatteryStatsHelper;
import com.miui.powercenter.view.PowerUsageDetailsItemPreference;
import com.miui.powercenter.view.PowerUsageDetailsTitlePreference;
import com.miui.securitycenter.utils.Utils;

public class PowerDetailActivity extends PreferenceActivity {

    public enum DrainType {
        IDLE,
        CELL,
        PHONE,
        WIFI,
        BLUETOOTH,
        SCREEN,
        APP,
        OTHER
    }

    // Note: Must match the sequence of the DrainType
    private static int[] sDrainTypeDesciptions = new int[] {
            R.string.battery_desc_standby,
            R.string.battery_desc_radio,
            R.string.battery_desc_voice,
            R.string.battery_desc_wifi,
            R.string.battery_desc_bluetooth,
            R.string.battery_desc_display,
            R.string.battery_desc_apps,
            R.string.battery_desc_other_apps
    };

    public static final int ACTION_DISPLAY_SETTINGS = 1;
    public static final int ACTION_WIFI_SETTINGS = 2;
    public static final int ACTION_BLUETOOTH_SETTINGS = 3;
    public static final int ACTION_WIRELESS_SETTINGS = 4;
    public static final int ACTION_APP_DETAILS = 5;
    public static final int ACTION_LOCATION_SETTINGS = 6;
    public static final int ACTION_FORCE_STOP = 7;
    public static final int ACTION_REPORT = 8;

    public static final int USAGE_SINCE_UNPLUGGED = 1;
    public static final int USAGE_SINCE_RESET = 2;

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_PERCENT = "percent";
    public static final String EXTRA_GAUGE = "gauge";
    public static final String EXTRA_UID = "uid";
    public static final String EXTRA_USAGE_SINCE = "since";
    public static final String EXTRA_USAGE_DURATION = "duration";
    public static final String EXTRA_REPORT_DETAILS = "report_details";
    public static final String EXTRA_REPORT_CHECKIN_DETAILS = "report_checkin_details";
    // Array of usage types (cpu, gps, etc)
    public static final String EXTRA_DETAIL_TYPES = "types";
    // Array of doubles
    public static final String EXTRA_DETAIL_VALUES = "values";
    public static final String EXTRA_DRAIN_TYPE = "drainType"; // DrainType
    public static final String EXTRA_ICON_PACKAGE = "iconPackage"; // String
    public static final String EXTRA_NO_COVERAGE = "noCoverage";
    public static final String EXTRA_ICON_ID = "iconId"; // Int

    private DrainType mDrainType;
    private int mUid;
    private String[] mPackages;
    private DevicePolicyManager mDpm;

    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mForceStopMenu.setEnabled(getResultCode() != Activity.RESULT_CANCELED);
        }
    };

    private PowerUsageDetailsTitlePreference mTitlePreference;
    private PreferenceCategory mDetailsCategory;
    private PreferenceCategory mPackagesCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pc_power_usage_details);

        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        mTitlePreference = (PowerUsageDetailsTitlePreference) findPreference(getString(R.string.preference_key_power_usage_details_title));
        mDetailsCategory = (PreferenceCategory) findPreference(getString(R.string.category_key_power_usage_details));
        mPackagesCategory = (PreferenceCategory) findPreference(getString(R.string.category_key_power_usage_packages));

        Intent intent = getIntent();
        // title
        mTitlePreference.setTitle(intent.getStringExtra(EXTRA_TITLE));
        final int percentage = intent.getIntExtra(EXTRA_PERCENT, 1);
        mTitlePreference.setProgress(percentage);
        mTitlePreference.setContent(getString(R.string.percentage, percentage));
        mDrainType = (DrainType) intent.getSerializableExtra(EXTRA_DRAIN_TYPE);
        mTitlePreference.setSummary(getDescriptionForDrainType());
        String iconPackage = intent.getStringExtra(EXTRA_ICON_PACKAGE);
        int iconId = intent.getIntExtra(EXTRA_ICON_ID, 0);
        Drawable mAppIcon = null;
        if (!TextUtils.isEmpty(iconPackage)) {
            try {
                final PackageManager pm = getPackageManager();
                ApplicationInfo ai = pm.getPackageInfo(iconPackage, 0).applicationInfo;
                if (ai != null) {
                    mAppIcon = ai.loadIcon(pm);
                }
            } catch (NameNotFoundException nnfe) {
                // Use default icon
            }
        } else if (iconId != 0) {
            mAppIcon = getResources().getDrawable(iconId);
        }
        if (mAppIcon == null) {
            mAppIcon = getPackageManager().getDefaultActivityIcon();
        } else {
            mAppIcon = IconCustomizer.generateIconStyleDrawable(mAppIcon);
        }
        mTitlePreference.setIcon(mAppIcon);

        // details
        int[] mTypes = intent.getIntArrayExtra(EXTRA_DETAIL_TYPES);
        double[] mValues = intent.getDoubleArrayExtra(EXTRA_DETAIL_VALUES);

        if (mTypes != null && mValues != null) {
            for (int i = 0; i < mTypes.length; i++) {
                // Only add an item if the time is greater than zero
                if (mValues[i] <= 0) {
                    continue;
                }
                final String label = getString(mTypes[i]);
                String value = null;
                boolean mUsesGps = false;
                switch (mTypes[i]) {
                    case R.string.usage_type_data_recv:
                    case R.string.usage_type_data_send:
                        value = Utils.formatBytes(this, mValues[i]);
                        break;
                    case R.string.usage_type_no_coverage:
                        value = String.format("%d%%", (int) Math.floor(mValues[i]));
                        break;
                    case R.string.usage_type_gps:
                        mUsesGps = true;
                    default:
                        value = Utils.formatElapsedTime(this, mValues[i]);
                        break;
                }

                PowerUsageDetailsItemPreference detailsItem = new PowerUsageDetailsItemPreference(
                        this);
                detailsItem.setTitle(label);
                detailsItem.setContent(value);
                mDetailsCategory.addPreference(detailsItem);
            }
        }
        if (mDetailsCategory.getPreferenceCount() == 0) {
            mDetailsCategory.setTitle(null);
            getPreferenceScreen().removePreference(mDetailsCategory);
        }

        mUid = intent.getIntExtra(EXTRA_UID, 0);
        if (mUid >= 1) {
            PackageManager pm = getPackageManager();
            mPackages = pm.getPackagesForUid(mUid);
            if (mPackages != null && mPackages.length >= 2) {
                // Convert package names to user-facing labels where possible
                for (int i = 0; i < mPackages.length; i++) {
                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(mPackages[i], 0);
                        CharSequence label = ai.loadLabel(pm);
                        if (label != null) {
                            mPackages[i] = label.toString();
                        }
                        PowerUsageDetailsItemPreference packagesItem = new PowerUsageDetailsItemPreference(
                                this);
                        packagesItem.setTitle(label);
                        mPackagesCategory.addPreference(packagesItem);
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        }
        if (mPackagesCategory.getPreferenceCount() == 0) {
            mPackagesCategory.setTitle(null);
            getPreferenceScreen().removePreference(mPackagesCategory);
        }
    }

    private String getDescriptionForDrainType() {
        return getResources().getString(sDrainTypeDesciptions[mDrainType.ordinal()]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMenus();

        if (mDrainType == DrainType.APP) {
            checkForceStop();
        }
    }

    private void checkMenus() {
        if (mUninstallMenu == null || mForceStopMenu == null || mAppDetailsMenu == null) {
            return;
        }

        PackageManager pm = getPackageManager();
        String[] packages = pm.getPackagesForUid(mUid);
        PackageInfo pi = null;
        try {
            pi = packages != null ? pm.getPackageInfo(packages[0], 0) : null;
        } catch (NameNotFoundException nnfe) { /* Nothing */
        }
        ApplicationInfo ai = pi != null ? pi.applicationInfo : null;
        boolean isSystem = ai != null ? (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 : false;

        mUninstallMenu.setEnabled(!isSystem);
        if (mDrainType == DrainType.APP && ai != null) {
            mAppDetailsMenu.setEnabled(true);
            mForceStopMenu.setEnabled(false);
            if (!isSystem) {
                mUninstallMenu.setEnabled(true);
            } else {
                mUninstallMenu.setEnabled(false);
            }

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
            for (RunningAppProcessInfo proc : processes) {
                String pkgName = proc.pkgList != null ? proc.pkgList[0] : null;
                if (TextUtils.equals(pkgName, ai.packageName)) {
                    mForceStopMenu.setEnabled(true);
                    break;
                }
            }
        } else {
            mAppDetailsMenu.setEnabled(false);
            mUninstallMenu.setEnabled(false);
            mForceStopMenu.setEnabled(false);
        }

        if (mDrainType == DrainType.APP && mPackages != null && !isSystem) {
            mAppDetailsMenu.setVisible(true);
            mUninstallMenu.setEnabled(true);
        } else {
            mAppDetailsMenu.setVisible(false);
            mUninstallMenu.setEnabled(false);
        }
    }

    private void checkForceStop() {
        if (mForceStopMenu == null) {
            return;
        }

        if (mPackages == null || mUid < Process.FIRST_APPLICATION_UID) {
            mForceStopMenu.setEnabled(false);
            return;
        }
        for (int i = 0; i < mPackages.length; i++) {
            if (mDpm.packageHasActiveAdmins(mPackages[i])) {
                mForceStopMenu.setEnabled(false);
                return;
            }
        }
        for (int i = 0; i < mPackages.length; i++) {
            try {
                ApplicationInfo info = getPackageManager().getApplicationInfo(mPackages[i], 0);
                if ((info.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
                    mForceStopMenu.setEnabled(true);
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        

        BatteryStatsHelper.sendQueryPackageIntent(this, mPackages, mUid, mCheckKillProcessesReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pc_power_usage_details_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private MenuItem mForceStopMenu;
    private MenuItem mUninstallMenu;
    private MenuItem mAppDetailsMenu;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mForceStopMenu = menu.findItem(R.id.item_force_stop);
        mForceStopMenu.setEnabled(false);

        mUninstallMenu = menu.findItem(R.id.item_uninstall);
        mUninstallMenu.setEnabled(false);

        mAppDetailsMenu = menu.findItem(R.id.item_details);

        checkMenus();

        if (mDrainType == DrainType.APP) {
            checkForceStop();
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_force_stop:
                killProcesses();
                return true;
            case R.id.item_uninstall:
                uninstallApp();
                return true;
            case R.id.item_details:
                startApplicationDetailsActivity();
                return true;
            default:
                break;
        }

        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }

    private void startApplicationDetailsActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME, mPackages[0], null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void killProcesses() {
        if (mPackages == null) {
            return;
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (int i = 0; i < mPackages.length; i++) {
            am.forceStopPackage(mPackages[i]);
        }
        checkForceStop();
    }

    private static final String SCHEME = "package";

    private void uninstallApp() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        Uri uri = Uri.fromParts(SCHEME, mPackages[0], null);
        intent.setData(uri);
        startActivity(intent);
    }
}
