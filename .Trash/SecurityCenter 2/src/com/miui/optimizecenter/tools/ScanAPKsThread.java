
package com.miui.optimizecenter.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;

import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.IFileScanCallback;

import java.io.File;

public class ScanAPKsThread extends Thread {

    private IFileProxy mIFileProxy;
    private IFileScanCallback mCallback;

    public ScanAPKsThread(IFileProxy proxy, IFileScanCallback callback) {
        mIFileProxy = proxy;
        mCallback = callback;
    }

    @Override
    public void run() {
        try {
            if (mIFileProxy != null) {
                String volumeName = "external";
                String[] columns = new String[] {
                        FileColumns.DATA
                };
                String selection = FileColumns.DATA + " LIKE '%.apk'";

                mIFileProxy.scanFilesByUri(mCallback, Files.getContentUri(volumeName), columns,
                        selection, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
