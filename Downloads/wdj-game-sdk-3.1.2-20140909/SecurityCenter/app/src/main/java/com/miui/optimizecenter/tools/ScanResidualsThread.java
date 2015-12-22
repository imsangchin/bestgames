
package com.miui.optimizecenter.tools;

import android.content.Context;
import android.os.RemoteException;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.IKSCleaner;
import com.cleanmaster.sdk.IResidualCallback;

import java.util.Locale;

public class ScanResidualsThread extends Thread {
    private Context mContext;
    private IKSCleaner mCleaner;
    private IResidualCallback mObserver;

    public ScanResidualsThread(Context context, IKSCleaner cleaner, IResidualCallback observer) {
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
                mCleaner.scanResidual(CMCleanConst.MASK_SCAN_COMMON
                        | CMCleanConst.MASK_SCAN_ADVANCED, mObserver);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
