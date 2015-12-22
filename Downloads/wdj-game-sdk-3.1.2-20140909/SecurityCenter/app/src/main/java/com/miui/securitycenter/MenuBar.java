
package com.miui.securitycenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.MiuiSettings;

import com.miui.common.EventHandler;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.OnMenuItemClickEvent;
import com.miui.securitycenter.event.OnMenuItemLongClickEvent;

import com.miui.securitycenter.R;

public class MenuBar extends LinearLayout implements OnClickListener, OnLongClickListener {

    public enum MenuItem {
        GARBAGE_CLEANUP, NETWORK_ASSISTANTS, ANTISPAM, POWER_MANAGER, ANTIVIRUS, LICENSE_MANAGER,
    }

    public EventHandler mEventHandler;

    public MenuBar(Context context) {
        this(context, null);
    }

    public MenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.menu_item_garbage_cleanup).setOnClickListener(this);
        findViewById(R.id.menu_item_network_assistants).setOnClickListener(this);
        findViewById(R.id.menu_item_antispam).setOnClickListener(this);
        findViewById(R.id.menu_item_power_manger).setOnClickListener(this);
        findViewById(R.id.menu_item_antivirus).setOnClickListener(this);
        findViewById(R.id.menu_item_license_manager).setOnClickListener(this);

        findViewById(R.id.menu_item_garbage_cleanup).setOnLongClickListener(this);
        findViewById(R.id.menu_item_network_assistants).setOnLongClickListener(this);
        findViewById(R.id.menu_item_antispam).setOnLongClickListener(this);
        findViewById(R.id.menu_item_power_manger).setOnLongClickListener(this);
        findViewById(R.id.menu_item_antivirus).setOnLongClickListener(this);
        findViewById(R.id.menu_item_license_manager).setOnLongClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onClick(View v) {
        MenuItem item = null;
        switch (v.getId()) {
            case R.id.menu_item_garbage_cleanup:
                item = MenuItem.GARBAGE_CLEANUP;
                break;
            case R.id.menu_item_network_assistants:
                item = MenuItem.NETWORK_ASSISTANTS;
                break;
            case R.id.menu_item_antispam:
                item = MenuItem.ANTISPAM;
                break;
            case R.id.menu_item_power_manger:
                item = MenuItem.POWER_MANAGER;
                break;
            case R.id.menu_item_antivirus:
                item = MenuItem.ANTIVIRUS;
                break;
            case R.id.menu_item_license_manager:
                item = MenuItem.LICENSE_MANAGER;
                break;
            default:
                break;
        }
        if (item != null) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_MENU_ITEM_CLICK,
                    OnMenuItemClickEvent.create(item));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        MenuItem item = null;
        switch (v.getId()) {
            case R.id.menu_item_garbage_cleanup:
                item = MenuItem.GARBAGE_CLEANUP;
                break;
            case R.id.menu_item_network_assistants:
                item = MenuItem.NETWORK_ASSISTANTS;
                break;
            case R.id.menu_item_antispam:
                item = MenuItem.ANTISPAM;
                break;
            case R.id.menu_item_power_manger:
                item = MenuItem.POWER_MANAGER;
                break;
            case R.id.menu_item_antivirus:
                item = MenuItem.ANTIVIRUS;
                break;
            case R.id.menu_item_license_manager:
                item = MenuItem.LICENSE_MANAGER;
                break;
            default:
                return false;
        }
        if (item != null) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_MENU_ITEM_LONG_CLICK,
                    OnMenuItemLongClickEvent.create(item));
        }
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            // 垃圾清理
            updateGarbageIcon();

            // 骚扰拦截
            updateAntiSpamIcon();

            // 病毒扫描
            updateAntiVirusIcon();

            // 网络助手
            updateDataUsage();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 省电优化
        Intent intent = getContext().registerReceiver(mBatteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        updateBatteryStatus(intent);

        // 垃圾清理
        updateGarbageIcon();

        // 骚扰拦截
        updateAntiSpamIcon();

        // 病毒扫描
        updateAntiVirusIcon();

        // 网络助手
        registerDataUsageUpdateReceiver();
    }

    private void updateGarbageIcon() {
        boolean isGarbageInDanger = com.miui.optimizecenter.Preferences.isGarbageInDanger();
        TextView garbageView = (TextView) findViewById(R.id.menu_item_garbage_cleanup);
        if (isGarbageInDanger) {
            garbageView.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_icon_garbage_danger_selector, 0, 0);
        }
        else {
            garbageView.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_icon_garbage_selector, 0, 0);
        }
    }

    private void updateAntiSpamIcon() {
        TextView antispamView = (TextView) findViewById(R.id.menu_item_antispam);
        if (MiuiSettings.AntiSpam.hasNewAntispam(getContext())) {
            antispamView.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_icon_antispam_danger_selector, 0, 0);
        } else {
            antispamView.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_icon_antispam_selector, 0, 0);
        }
    }

    private void updateAntiVirusIcon() {
        int virusCount = com.miui.antivirus.Preferences.getLastVirusScanVirusCount();
        int riskCount = com.miui.antivirus.Preferences.getLastVirusScanRiskCount();
        TextView antiVirusView = (TextView) findViewById(R.id.menu_item_antivirus);
        if (virusCount + riskCount != 0) {
            antiVirusView.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_icon_virus_danger_selector, 0, 0);
        } else {
            antiVirusView.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.menu_icon_virus_save_selector, 0, 0);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mBatteryReceiver);
        unRegisterDataUsageUpdateReceiver();
        super.onDetachedFromWindow();
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryStatus(intent);
        }
    };

    private void updateBatteryStatus(Intent intent) {
        if (intent == null) {
            return;
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);

        if (scale != 0) {
            TextView view = (TextView) findViewById(R.id.menu_item_power_manger);
            int percent = (level * 100) / scale;
            view.setText(getResources().getString(R.string.menu_text_power_percent, percent + "%"));
            if (percent <= 10) {
                view.setCompoundDrawablesWithIntrinsicBounds(0,
                        R.drawable.menu_icon_power_danger_selector, 0, 0);
            } else {
                view.setCompoundDrawablesWithIntrinsicBounds(0,
                        R.drawable.menu_icon_power_save_selector, 0, 0);
            }
        }
    }

    /* Update NetworkAssistant menu status */

    private static final String ACTION_NETWORK_POLICY_UPDATE = "com.android.action.NETWORK_POLICY_UPDATE";

    private static final String URI_NETWORK_TRAFFIC_INFO = "content://com.miui.networkassistant.provider/datausage_status";
    private static final String COLUMN_NAME_TOTAL_LIMIT = "total_limit";
    private static final String COLUMN_NAME_MONTH_USED = "month_used";
    private static final String COLUMN_NAME_MONTH_WARNING = "month_warning";

    private static final long TB = 1073741824L * 1024;
    private static final long GB = 1073741824L;
    private static final long MB = 1048576L;
    private static final long KB = 1024;

    private static final String UINT_TB = "TB";
    private static final String UINT_GB = "GB";
    private static final String UINT_MB = "MB";
    private static final String UINT_KB = "KB";
    private static final String UINT_B = "B";

    private void registerDataUsageUpdateReceiver() {
        getContext().registerReceiver(mDataUsageUpdateReceiver,
                new IntentFilter(ACTION_NETWORK_POLICY_UPDATE));
    }

    private void unRegisterDataUsageUpdateReceiver() {
        getContext().unregisterReceiver(mDataUsageUpdateReceiver);
    }

    private BroadcastReceiver mDataUsageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDataUsage();
        }
    };

    private void updateDataUsage() {
        final Uri uri = Uri.parse(URI_NETWORK_TRAFFIC_INFO);
        final Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        TextView networkAssistantView = (TextView) findViewById(R.id.menu_item_network_assistants);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final long monthUsedTraffic = cursor.getLong(cursor
                        .getColumnIndex(COLUMN_NAME_MONTH_USED));
                final long totalLimitTraffic = cursor.getLong(cursor
                        .getColumnIndex(COLUMN_NAME_TOTAL_LIMIT));
                final long monthWarningTraffic = cursor.getLong(cursor
                        .getColumnIndex(COLUMN_NAME_MONTH_WARNING));

                if (-1 == monthUsedTraffic || 0 == totalLimitTraffic) {
                    networkAssistantView.setText(R.string.activity_title_networkassistants);
                    networkAssistantView.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.menu_icon_net_save_selector, 0, 0);
                } else {
                    // update menu icon
                    if (monthUsedTraffic < monthWarningTraffic) {
                        networkAssistantView.setCompoundDrawablesWithIntrinsicBounds(0,
                                R.drawable.menu_icon_net_save_selector, 0, 0);
                    } else {
                        networkAssistantView.setCompoundDrawablesWithIntrinsicBounds(0,
                                R.drawable.menu_icon_net_danger_selector, 0, 0);
                    }

                    // update menu text
                    long showValue = totalLimitTraffic - monthUsedTraffic;
                    int showStrRes = -1;
                    if (showValue >= 0) {
                        showStrRes = R.string.menu_text_networkassistants_remain;
                    } else {
                        showValue = Math.abs(showValue);
                        showStrRes = R.string.menu_text_networkassistants_danger;
                    }

                    networkAssistantView.setText(getResources().getString(showStrRes,
                            getDataUsageStr(showValue)));
                }
            }
            cursor.close();
        }
    }

    private String getDataUsageStr(long bytes) {
        String showUnit = null;
        double showValue;
        if (bytes >= TB - GB * 24) {
            showValue = 1.00 * bytes / TB;
            showUnit = UINT_TB;
        } else if (bytes >= GB - MB * 24) {
            showValue = 1.00 * bytes / GB;
            showUnit = UINT_GB;
        } else if (bytes >= MB - KB * 24) {
            showValue = 1.00 * bytes / MB;
            showUnit = UINT_MB;
        } else if (bytes >= KB - 24) {
            showValue = 1.00 * bytes / KB;
            showUnit = UINT_KB;
        } else {
            showValue = 1.00 * bytes;
            showUnit = UINT_B;
        }

        String result = null;
        if (showValue > 9.5) {
            result = String.format("%d%s", (int) showValue, showUnit);
        } else {
            result = String.format("%.01f%s", showValue, showUnit);
        }
        return result;
    }

}
