
package com.miui.powercenter;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import miui.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerData;
import com.miui.powercenter.provider.PowerModeStateTransfer;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.powercenter.provider.PowerModeChangedReceiver;
import com.miui.powercenter.provider.PowerModeChangedReceiver.UIPowerModeChangeListener;
import com.miui.powercenter.provider.PowerUtils;
import com.miui.powercenter.provider.SqlUtils;
import com.miui.powercenter.view.PowerNewChooserPreference;

import miui.preference.RadioButtonPreferenceCategory;

import java.util.ArrayList;

public class PowerModeChooser extends PreferenceActivity {
    private static final String TAG = "PowerModeChooser";
    private PowerModeStateTransfer mTransition;

    //这个是系统自带的各种模式
    private final static int MENU_ADD_NEW                                       = Menu.FIRST;

    private DataManager mDataManager;
    private MenuItem mAddMenuItem;
    private RadioButtonPreferenceCategory mDefinedCategory;
    private int mSize;
    private boolean mFirstblood;

    private ContentObserver mDBObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            fillModePreference();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pc_power_mode_chooser);

        Log.d(TAG, "PDEBUG--test1");
        getContentResolver().registerContentObserver(PowerMode.EXTERNAL_URI, false, mDBObserver);
        mDefinedCategory = (RadioButtonPreferenceCategory) findPreference("key_st_powercenter_mode_defined");

        mTransition = PowerModeStateTransfer.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(mDBObserver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mAddMenuItem = menu.add(0, MENU_ADD_NEW, 0, R.string.power_chooser_user_define_title);
        mAddMenuItem.setIcon(miui.R.drawable.action_button_new_light);
        mAddMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        switch (item.getItemId()) {
            case MENU_ADD_NEW:
            Intent intent = new Intent(
                    "com.miui.powercenter.PowerModeCustomizer");
            Bundle b = new Bundle();
            b.putInt(PowerMode.KEY_POWER_MODE_ID, -1);
            intent.putExtras(b);
            PowerModeChooser.this.startActivity(intent);
            ret = true;
            break;
            case android.R.id.home:
                finish();
                ret = true;
                break;
            default:
                break;
        }

        return ret;
    }

    protected void onResume() {
        super.onResume();
        mFirstblood = true;
        fillModePreference();
    }

    private void fillModePreference() {
        clearPreference();
        fillDefaultModePreference();
        fillCustomModePreference();
        refreshUI();
    }

    private void clearPreference() {
        mDefinedCategory.removeAll();
    }

    private void fillDefaultModePreference() {

        PowerMode[] modeArray = PowerData.getDefaultModeArray(this);
        if (modeArray == null || modeArray.length == 0) {
            return;
        }

        mSize = modeArray.length;
        for (int i = 0; i < mSize; ++i) {
            PowerMode mode = modeArray[i];
            int modeId = Integer.parseInt(String.valueOf(mode.mDBValue[0]));
            addPreference(mode, modeId,i);
        }
    }

    //获得我们定制的mode
    private void fillCustomModePreference() {
        ArrayList<PowerMode> modeList = SqlUtils.getModeList(this);
        if (modeList == null || modeList.isEmpty()) {
            return;
        }

        for (int i = 0; i < modeList.size(); ++i) {
            PowerMode mode = modeList.get(i);
            int modeId = Integer.parseInt(String.valueOf(mode.mDBValue[0]));
            modeId += PowerData.getDefaultModeCount() - 1;
            addPreference(mode, modeId,i+mSize);
        }

    }

    private void addPreference(PowerMode mode, final int modeId , int position) {
        PowerNewChooserPreference pref = new PowerNewChooserPreference(PowerModeChooser.this, modeId);
        mDefinedCategory.addPreference(pref);
        pref.setKey(String.valueOf(modeId));
        pref.setTitle(String.valueOf(mode.mDBValue[1]));
        int order = modeId;
        pref.setOrder(order);
        pref.setPersistent(true);
        if (modeId == DataManager.getInstance(this).getInt(DataManager.KEY_POWER_MODE_APPLIED, -1)) {
            mDefinedCategory.unCheckAllPreference();
            mDefinedCategory.setCheckedPosition(position);
            if (mFirstblood && (mode.any_diffice_from_state(this)>0)) {
                pref.setSummary(R.string.power_mode_choose_summary_change);
            } else {
                pref.setSummary(String.valueOf(mode.mDBValue[2]));
            }
        } else {
            pref.setSummary(String.valueOf(mode.mDBValue[2]));
        }
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Context mContext = PowerModeChooser.this;
                boolean firstApplyManual = DataManager.getInstance(mContext).getBoolean(DataManager.KEY_FIRST_APPLY_MANUAL_MODE,
                        true);
                if (firstApplyManual) {
                    Log.d(TAG, "电源中心--PowerNewChooserPreference  第一次apply mode");
                    PowerMode mode = new PowerMode();
                    mode.retrieve(mContext);
                    PowerMode applyMode = mTransition.enterManualMode(modeId);
                    mode.mDBValue[PowerMode.INDEX_TITLE] = mContext.getResources().getString(R.string.power_mode_my_mode);
                    mode.mDBValue[PowerMode.INDEX_NAME] = mContext.getResources().getString(R.string.power_mode_my_mode);
                    mode.mDBValue[PowerMode.INDEX_SUMMARY] = mContext.getResources().getString(R.string.power_mode_my_mode_summary);
                    SqlUtils.insertMode(mContext, mode);
                    DataManager.getInstance(mContext).putBoolean(DataManager.KEY_FIRST_APPLY_MANUAL_MODE,
                            false);

                    String name = (String)applyMode.mDBValue[PowerMode.INDEX_NAME];
                    String toast = String.format(mContext.getResources().getString(R.string.power_apply_toast), name);
                } else {
                    PowerMode mode = mTransition.enterManualMode(modeId);
//                    mTransition.enterManualMode(modeId);
                }
                mFirstblood = false;
                fillModePreference();
                return false;
            }
        });
    }

    private void refreshUI() {
        mDataManager = DataManager.getInstance(this);
    }
}
