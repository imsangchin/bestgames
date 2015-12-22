
package com.miui.optimizecenter.tools;

import android.content.Context;
import android.os.RemoteException;

import com.cleanmaster.sdk.IAdDirCallback;
import com.cleanmaster.sdk.IKSCleaner;

import java.util.Locale;

public class ScanADsThread extends Thread {
    private Context mContext;
    private IKSCleaner mCleaner;
    private IAdDirCallback mObserver;

    public ScanADsThread(Context context, IKSCleaner cleaner, IAdDirCallback observer) {
        mContext = context;
        mCleaner = cleaner;
        mObserver = observer;
    }

    @Override
    public void run() {
        try {
            if (mCleaner != null) {
                Locale locale = mContext.getResources().getConfiguration().locale;
                mCleaner.init(locale.getLanguage(), locale.getCountry());
                mCleaner.scanAdDir(mObserver);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
