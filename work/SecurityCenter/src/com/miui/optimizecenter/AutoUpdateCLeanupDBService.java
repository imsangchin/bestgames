
package com.miui.optimizecenter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.ICmSdkUpdateCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.miui.securitycenter.AidlProxyHelper;

public class AutoUpdateCLeanupDBService extends Service {

    private ServiceConnection mCleanerConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIKSCleaner = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIKSCleaner = IKSCleaner.Stub.asInterface(service);
            try {
                if (com.miui.securitycenter.Preferences.isConnectNetworkAlow()) {
                    updateCleanupDB();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    public void onCreate() {
        AidlProxyHelper.getInstance().bindCleanProxy(this, mCleanerConnection);
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        AidlProxyHelper.getInstance().unbindProxy(this, mCleanerConnection);
    }

    private IKSCleaner mIKSCleaner;

    private void updateCleanupDB() throws RemoteException {
        mIKSCleaner.StartUpdateCheck(new ICmSdkUpdateCallback.Stub() {

            @Override
            public void FinishUpdateCheck(int nErrorCode, long size, String strNewVersion) throws RemoteException {
                Log.d(AutoUpdateCLeanupDBService.class.getSimpleName(), "nErrorCode = "
                        + nErrorCode + " size = " + size + " strNewVersion = " + strNewVersion);
                if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_SUCCESS) {
                    try {
                        mIKSCleaner.StartUpdateData();
                        Preferences.setAutoUpdateCLeanupDBTime(System.currentTimeMillis());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                stopSelf();
            }

            @Override
            public void FinishUpdateData(final int nErrorCode) throws RemoteException {
                // TODO
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
