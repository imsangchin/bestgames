
package com.miui.optimizecenter.tools;

import android.content.Context;
import android.os.RemoteException;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.ICacheCallback;
import com.cleanmaster.sdk.IKSCleaner;

import java.util.Locale;

public class ScanCachesThread extends Thread {

    private Context mContext;
    private IKSCleaner mCleaner;
    private ICacheCallback mObserver;

    private int mScanMask = CMCleanConst.MASK_SCAN_COMMON;

    public ScanCachesThread(Context context, IKSCleaner cleaner, ICacheCallback observer) {
        this(context, cleaner, CMCleanConst.MASK_SCAN_COMMON, observer);
    }

    public ScanCachesThread(Context context, IKSCleaner cleaner, int scanMask,
            ICacheCallback observer) {
        mContext = context;
        mCleaner = cleaner;
        mScanMask = scanMask;
        mObserver = observer;
    }

    @Override
    public void run() {
        try {
            if (mCleaner != null) {
                Locale locale = mContext.getResources().getConfiguration().locale;
                mCleaner.init(locale.getLanguage(), locale.getCountry());
                mCleaner.scanCache(mScanMask, mObserver);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
