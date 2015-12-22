
package com.miui.optimizecenter.settings;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.MiuiIntent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.ICmSdkUpdateCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.miui.common.AndroidUtils;
import com.miui.optimizecenter.CustomPreference;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.enums.GarbageCleanupSize;
import com.miui.optimizecenter.enums.GarbageCleanupTimes;
import com.miui.optimizecenter.whitelist.WhiteListActivity;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.R;

import miui.app.AlertDialog;
import miui.app.ProgressDialog;
import miui.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
        OnPreferenceChangeListener, OnPreferenceClickListener {

    private ServiceConnection mCleanerConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIKSCleaner = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIKSCleaner = IKSCleaner.Stub.asInterface(service);
            updateDBSettings();
        }
    };

    private CheckBoxPreference mExpandPreference;

    private CheckBoxPreference mAutoUpdatePreference;
    private Preference mManualUpdatePreference;

    private CustomPreference mGarbageTimePreference;
    private CustomPreference mGarbageSizePreference;

    private CustomPreference mWhiteListPreference;

    private IKSCleaner mIKSCleaner;

    private ProgressDialog mUpdateDialog;
    private boolean mIsAttachedToWindow = false;
    private boolean mHasUpdateCleanerDb = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_settings);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(MiuiIntent.EXTRA_SETTINGS_TITLE);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }

        mExpandPreference = (CheckBoxPreference) findPreference(getString(R.string.preference_key_default_expand_cache_group));
        mExpandPreference.setOnPreferenceChangeListener(this);
        mExpandPreference.setChecked(Preferences.isDefaultExpandCacheGroups());

        mWhiteListPreference = (CustomPreference) findPreference(getString(R.string.preference_key_cleanup_white_list));
        mWhiteListPreference.setOnPreferenceClickListener(this);

        mAutoUpdatePreference = (CheckBoxPreference) findPreference(getString(R.string.preference_key_auto_update_cleanup_db));
        mAutoUpdatePreference.setOnPreferenceChangeListener(this);

        mManualUpdatePreference = (Preference) findPreference(getString(R.string.preference_key_manual_update_cleanup_db));
        mManualUpdatePreference.setOnPreferenceClickListener(this);

        mGarbageTimePreference = (CustomPreference) findPreference(getString(R.string.preference_key_garbage_cleanup_auto_check_time));
        mGarbageTimePreference.setOnPreferenceClickListener(this);

        mGarbageSizePreference = (CustomPreference) findPreference(getString(R.string.preference_key_garbage_cleanup_auto_check_size));
        mGarbageSizePreference.setOnPreferenceClickListener(this);

        AidlProxyHelper.getInstance().bindCleanProxy(this, mCleanerConnection);
    }

    private void updateDBSettings() {
        if (mIKSCleaner != null) {
            try {
                mAutoUpdatePreference.setChecked(Preferences.isAutoUpdateCLeanupDBEnabled());
                long time = Preferences.getAutoUpdateCLeanupDBTime();
                if (time == 0) {
                    mAutoUpdatePreference.setSummary(getString(
                            R.string.msg_cleanup_db_update_time_unknown));
                } else {
                    mAutoUpdatePreference.setSummary(getString(
                            R.string.pref_summary_auto_update_cleanup_db,
                            DateFormat.format("yyyy-MM-dd", time)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAlarmSettings() {
        GarbageCleanupTimes time = Preferences.getGarbageCleanupTime();
        switch (time) {
            case THREE_DAYS:
                mGarbageTimePreference.setContent(R.string.item_garbage_time_3d);
                break;
            case SEVEN_DAYS:
                mGarbageTimePreference.setContent(R.string.item_garbage_time_7d);
                break;
            case NEVER:
                mGarbageTimePreference.setContent(R.string.item_garbage_time_never);
                break;
            default:
                mGarbageTimePreference.setContent(R.string.item_garbage_time_daily);
                break;
        }

        GarbageCleanupSize size = Preferences.getGarbageCleanupSize();
        switch (size) {
            case M300:
                mGarbageSizePreference.setContent(R.string.item_garbage_size_300m);
                break;
            case M500:
                mGarbageSizePreference.setContent(R.string.item_garbage_size_500m);
                break;
            case M1000:
                mGarbageSizePreference.setContent(R.string.item_garbage_size_1000m);
                break;
            default:
                mGarbageSizePreference.setContent(R.string.item_garbage_size_100m);
                break;
        }
    }

    private void updateWhiteListSettings() {
        new LoadWhiteListSizeTask().execute();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttachedToWindow = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttachedToWindow = false;
    }

    private void manualUpdate() throws Exception {
        if (mHasUpdateCleanerDb) {
            showToastAndDismissDialog(R.string.msg_update_error_code_no_newer_db,
                    mUpdateDialog);
            return;
        }
        mHasUpdateCleanerDb = true;

        mUpdateDialog = ProgressDialog.show(this, null,
                getString(R.string.msg_cleanup_db_update_checking), true, true);

        mIKSCleaner.StartUpdateCheck(new ICmSdkUpdateCallback.Stub() {

            @Override
            public void FinishUpdateCheck(final int nErrorCode, long size, String strNewVersion)
                    throws RemoteException {

                if (!mIsAttachedToWindow) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_NO_NEWER_DB) {
                            if (Preferences.getAutoUpdateCLeanupDBTime() == 0) {
                                Preferences.setAutoUpdateCLeanupDBTime(System.currentTimeMillis());
                            }
                            showToastAndDismissDialog(R.string.msg_update_error_code_no_newer_db,
                                    mUpdateDialog);
                        } else if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_ALREADY_RUNNING) {
                            showToastAndDismissDialog(
                                    R.string.msg_update_error_code_already_running, mUpdateDialog);
                        } else if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_NETWORK_ERROR) {
                            showToastAndDismissDialog(R.string.msg_update_error_code_network_error,
                                    mUpdateDialog);
                        }
                        /*
                         * else if (nErrorCode ==
                         * CMCleanConst.UPDATE_ERROR_CODE_UNKNOWN_ERROR) {
                         * showToastAndDismissDialog
                         * (R.string.msg_update_error_code_unknown_error,
                         * mUpdateDialog); } else if(nErrorCode ==
                         * CMCleanConst.UPDATE_ERROR_CODE_SUCCESS)
                         */
                        else {
                            showToastAndDismissDialog(R.string.msg_update_error_code_success,
                                    mUpdateDialog);
                            try {
                                mIKSCleaner.StartUpdateData();
                                Preferences.setAutoUpdateCLeanupDBTime(System.currentTimeMillis());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        updateDBSettings();
                    }
                });

            }

            @Override
            public void FinishUpdateData(final int nErrorCode) throws RemoteException {
                // TODO
            }
        });
    }

    private void showToastAndDismissDialog(int resId, ProgressDialog dialog) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDBSettings();
        updateAlarmSettings();
        updateWhiteListSettings();
    }

    @Override
    public void onDestroy() {
        AidlProxyHelper.getInstance().unbindProxy(this, mCleanerConnection);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(getString(R.string.preference_key_auto_update_cleanup_db))) {
            Preferences.setAutoUpdateCLeanupDBEnabled((Boolean) newValue);
            return true;
        } else if (preference.getKey().equals(
                getString(R.string.preference_key_default_expand_cache_group))) {
            Preferences.setDefaultExpandCacheGroups((Boolean) newValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.preference_key_manual_update_cleanup_db))) {
            try {
                if (com.miui.securitycenter.Preferences.isConnectNetworkAlow()) {
                    if (AndroidUtils.isNetConnected(SettingsActivity.this)) {
                        manualUpdate();
                        return true;
                    } else {
                        Toast.makeText(SettingsActivity.this, R.string.toast_network_eror,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (preference.getKey().equals(
                getString(R.string.preference_key_garbage_cleanup_auto_check_time))) {
            showAlarmTimeDialog();
            return true;
        } else if (preference.getKey().equals(
                getString(R.string.preference_key_garbage_cleanup_auto_check_size))) {
            showAlarmSizeDialog();
            return true;
        } else if (preference.getKey().equals(
                getString(R.string.preference_key_cleanup_white_list))) {
            startActivity(new Intent(SettingsActivity.this, WhiteListActivity.class));
            return true;
        }
        return false;
    }

    private void showAlarmTimeDialog() {
        final String[] items = {
                getString(R.string.item_garbage_time_daily),
                getString(R.string.item_garbage_time_3d),
                getString(R.string.item_garbage_time_7d),
                getString(R.string.item_garbage_time_never)
        };

        int checkedItem = -1;
        GarbageCleanupTimes time = Preferences.getGarbageCleanupTime();
        if (time == GarbageCleanupTimes.DAILY) {
            checkedItem = 0;
        } else if (time == GarbageCleanupTimes.THREE_DAYS) {
            checkedItem = 1;
        } else if (time == GarbageCleanupTimes.SEVEN_DAYS) {
            checkedItem = 2;
        } else if (time == GarbageCleanupTimes.NEVER) {
            checkedItem = 3;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.item_title_garbage_time)
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Preferences.setGarbageCleanupTime(GarbageCleanupTimes.DAILY);
                            mGarbageTimePreference.setContent(items[0]);
                        } else if (which == 1) {
                            Preferences.setGarbageCleanupTime(GarbageCleanupTimes.THREE_DAYS);
                            mGarbageTimePreference.setContent(items[1]);
                        } else if (which == 2) {
                            Preferences.setGarbageCleanupTime(GarbageCleanupTimes.SEVEN_DAYS);
                            mGarbageTimePreference.setContent(items[2]);
                        } else if (which == 3) {
                            Preferences.setGarbageCleanupTime(GarbageCleanupTimes.NEVER);
                            mGarbageTimePreference.setContent(items[3]);
                        }
                        updateAlarmSettings();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAlarmSizeDialog() {
        final String[] items = {
                getString(R.string.item_garbage_size_100m),
                getString(R.string.item_garbage_size_300m),
                getString(R.string.item_garbage_size_500m),
                getString(R.string.item_garbage_size_1000m)
        };

        int checkedItem = -1;
        GarbageCleanupSize size = Preferences.getGarbageCleanupSize();
        if (size == GarbageCleanupSize.M100) {
            checkedItem = 0;
        } else if (size == GarbageCleanupSize.M300) {
            checkedItem = 1;
        } else if (size == GarbageCleanupSize.M500) {
            checkedItem = 2;
        } else if (size == GarbageCleanupSize.M1000) {
            checkedItem = 3;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.item_title_garbage_size)
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Preferences.setGarbageCleanupSize(GarbageCleanupSize.M100);
                            mGarbageSizePreference.setContent(items[0]);
                        } else if (which == 1) {
                            Preferences.setGarbageCleanupSize(GarbageCleanupSize.M300);
                            mGarbageSizePreference.setContent(items[1]);
                        } else if (which == 2) {
                            Preferences.setGarbageCleanupSize(GarbageCleanupSize.M500);
                            mGarbageSizePreference.setContent(items[2]);
                        } else if (which == 3) {
                            Preferences.setGarbageCleanupSize(GarbageCleanupSize.M1000);
                            mGarbageSizePreference.setContent(items[3]);
                        }
                        updateAlarmSettings();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private class LoadWhiteListSizeTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            WhiteListManager whiteListManager = WhiteListManager.getInstance(SettingsActivity.this);
            return whiteListManager.getCacheWhiteList().size()
                    + whiteListManager.getAdWhiteList().size()
                    + whiteListManager.getApkWhiteList().size()
                    + whiteListManager.getResidualWhiteList().size()
                    + whiteListManager.getLargeFileWhiteList().size();
        }

        @Override
        protected void onPostExecute(Integer result) {
            mWhiteListPreference.setContent(String.valueOf(result));
        }
    }
}
