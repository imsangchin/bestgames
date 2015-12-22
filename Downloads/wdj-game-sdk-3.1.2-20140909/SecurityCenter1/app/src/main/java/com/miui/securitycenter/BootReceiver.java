
package com.miui.securitycenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lbe.security.bean.AppPermissionConfig;
import com.miui.common.PreferenceStore;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerModeStateTransfer;
import com.miui.powercenter.provider.PowerSaveService;

public class BootReceiver extends BroadcastReceiver {
    public static boolean isReboot = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        isReboot = true;
        //data transfer
        RestoreHelper.restoreData();

        // 初始化SharedPreferences
        PreferenceStore.init(context.getApplicationContext());

        // 启动安全中心service
        context.startService(new Intent(context, SecurityCenterService.class));

        // 省电动态图标
        NotificationService.sendBatteryConfigChangeBroadcast(context);

        // 启动省电service
        context.startService(new Intent(context, PowerSaveService.class));

        // 在开机的时候检查一下是否应该退出按时模式
        PowerModeStateTransfer mTransfer = PowerModeStateTransfer.getInstance(context
                .getApplicationContext());

        mTransfer.exitOnTimePage();

        isReboot = false;
    }
}
