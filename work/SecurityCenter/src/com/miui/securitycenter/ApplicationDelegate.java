
package com.miui.securitycenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cleanmaster.sdk.IKSCleaner;
import com.lbe.security.bean.AppPermissionConfig;
import com.miui.common.MediaScannerUtil;
import com.miui.common.PreferenceStore;
import com.miui.optimizecenter.apk.ApkActivity;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;

public class ApplicationDelegate extends miui.external.ApplicationDelegate {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceStore.init(this);

        if (!android.provider.MiuiSettings.Secure.isCtaSupported(getContentResolver())) {
            Preferences.setConnectNetworkAlow(true);
        }
        if (Preferences.isConnectNetworkAlow()) {
            miui.provider.ExtraGuard
                    .setTmsAutoConnectNetworkEnabled(this, Preferences.isConnectNetworkAlow());
        } else {
            miui.provider.ExtraGuard.setTmsAutoConnectNetworkEnabled(this, false);
        }

        // 设置清理SDK的自动联网请求
        setCleanerNetworkAccess(this);

        // 初始化LBE权限管理
        new Thread() {
            public void run() {
                AppPermissionConfig.initialize(ApplicationDelegate.this);
            };
        }.start();

        // 初始化一些只能在com.miui.securitycenter进程中执行的操作
        startService(new Intent(this, SecurityCenterService.class));
    }

    private void setCleanerNetworkAccess(final Context context) {
        AidlProxyHelper.getInstance().bindCleanProxy(context, new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // ignore
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                IKSCleaner cleaner = IKSCleaner.Stub.asInterface(service);
                if (cleaner != null) {
                    try {
                        if (Preferences.isConnectNetworkAlow()) {
                            cleaner.SetEnableNetworkAccess(Preferences.isConnectNetworkAlow());
                        } else {
                            cleaner.SetEnableNetworkAccess(false);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    AidlProxyHelper.getInstance().unbindProxy(context, this);
                }
            }
        });
    }
}
