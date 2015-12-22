
package com.miui.optimizecenter.deepclean;

import miui.app.ActionBar;
import android.app.Fragment;
import android.content.Context;

import com.cleanmaster.sdk.IKSCleaner;
import com.miui.guardprovider.service.IFileProxy;

public abstract class BaseDeepCleanFragment extends Fragment {
    public abstract void onServiceBinded(Context context, IKSCleaner cleaner, IFileProxy fileProxy);

    public abstract void onFragmentSortTypeSelected(Context context, int sortTypeId);
    
}
