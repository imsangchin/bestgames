
package com.miui.permcenter;

import miui.app.Activity;
import android.content.Intent;
import android.content.MiuiIntent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.miui.analytics.AnalyticsUtil;
import com.miui.common.ApkIconHelper;
import com.miui.permcenter.autostart.AutoStartManagementActivity;
import com.miui.permcenter.permissions.AppPermissionsTabActivity;
import com.miui.permcenter.root.RootManagementActivity;

import com.miui.securitycenter.R;

public class MainAcitivty extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm_activity_main);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_PERMISSION);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);

        findViewById(R.id.handle_item_auto_start).setOnClickListener(this);
        findViewById(R.id.handle_item_permissions).setOnClickListener(this);
        findViewById(R.id.handle_item_root).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        View settingView = findViewById(R.id.settings);
        settingView.setOnClickListener(this);

        // 如果不是一个海外版本， 权限管理的设置项需要隐藏。 实际需要隐藏的是全局的权限开关,现在只有这一个设置项。
        if (!miui.os.Build.IS_INTERNATIONAL_BUILD && !miui.os.Build.IS_CTS_BUILD) {
            settingView.setVisibility(View.GONE);
        }

        if (miui.os.Build.IS_STABLE_VERSION) {
            findViewById(R.id.handle_item_root).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.handle_item_auto_start:
                startActivity(new Intent(this, AutoStartManagementActivity.class));
                break;
            case R.id.handle_item_permissions:
                startActivity(new Intent(this, AppPermissionsTabActivity.class));
                break;
            case R.id.handle_item_root:
                startActivity(new Intent(this, RootManagementActivity.class));
                break;
            case R.id.back:
                finish();
                break;
            case R.id.settings:
                Intent permissionSettingIntent = new Intent(MiuiIntent.ACTION_PERMISSION_SETTINGS);
                permissionSettingIntent.putExtra(MiuiIntent.EXTRA_SETTINGS_TITLE,
                        getString(R.string.settings_title_permission));
                startActivity(permissionSettingIntent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApkIconHelper.getInstance(this).clearCacheLaunchers();
    }
}
