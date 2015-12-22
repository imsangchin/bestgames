
package com.miui.securitycenter;

import android.content.Context;
import android.content.Intent;
import android.content.MiuiIntent;
import android.content.ServiceConnection;

import com.cleanmaster.sdk.CMCleanConst;

import com.cleanmaster.sdk.IKSCleaner;
import com.miui.backup.proxy.IBackupProxy;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;

public class AidlProxyHelper {

    private static AidlProxyHelper INST;

    private IKSCleaner mIKSCleaner;
    private IFileProxy mIFileProxy;
    private IBackupProxy mIBackupProxy;

    private AidlProxyHelper() {
        // ignore
    }

    public static AidlProxyHelper getInstance() {
        if (INST == null) {
            INST = new AidlProxyHelper();
        }
        return INST;
    }

    public void bindFileProxy(Context context, ServiceConnection conn) {
        // 访问sdcard文件使用
        Intent fileIntent = new Intent(ExtraIntent.ACTION_FILE_PROXY_SERVICE);
        context.bindService(fileIntent, conn, Context.BIND_AUTO_CREATE);
    }

    public void bindCleanProxy(Context context, ServiceConnection conn) {
        // 扫描垃圾时使用
        Intent cleanerIntent = new Intent(CMCleanConst.ACTION_CLEAN_SERVICE);
        context.bindService(cleanerIntent, conn, Context.BIND_AUTO_CREATE);
    }

    public void bindBackupProxy(Context context, ServiceConnection conn) {
        // 备份
        Intent backupIntent = new Intent(MiuiIntent.ACTION_BACKUP_PROXY_SERVICE);
        context.bindService(backupIntent, conn, Context.BIND_AUTO_CREATE);
    }

    public void unbindProxy(Context context, ServiceConnection conn) {
        context.unbindService(conn);
    }

    public void setIKSCleaner(IKSCleaner cleaner) {
        mIKSCleaner = cleaner;
    }

    public IKSCleaner getIKSCleaner() {
        return mIKSCleaner;
    }

    public void setIFileProxy(IFileProxy fileProxy) {
        mIFileProxy = fileProxy;
    }

    public IFileProxy getIFileProxy() {
        return mIFileProxy;
    }

    public void setIBackupProxy(IBackupProxy proxy) {
        mIBackupProxy = proxy;
    }

    public IBackupProxy getIBackupProxy() {
        return mIBackupProxy;
    }

    public void deleteDirectory(String file) {
        try {
            mIFileProxy.deleteFileByPath(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProxyFileInfo getProxyFileInfo(String path) {
        try {
            return mIFileProxy.getFileInfo(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
