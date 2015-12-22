
package com.miui.optimizecenter.cache;

import android.text.TextUtils;

import com.miui.common.ApkIconHelper;

import java.util.Comparator;

import com.miui.optimizecenter.enums.CacheGroupSortType;

import android.util.Log;

import java.text.Collator;


public class CacheGroupComparator implements Comparator<CacheGroupModel> {

    private CacheGroupSortType mSortType = CacheGroupSortType.SIZE;
    
    private final Collator mCollator = Collator.getInstance();
    
    public CacheGroupComparator() {
        mSortType = CacheGroupSortType.SIZE;
    }

    public CacheGroupComparator(CacheGroupSortType sortType) {
        mSortType = sortType;
    }

    @Override
    public int compare(CacheGroupModel lhs, CacheGroupModel rhs){
        if (mSortType == CacheGroupSortType.NAME){
            return mCollator.compare(lhs.getAppName(), rhs.getAppName());
        }
        else{
            if(lhs.getTotalSize() > rhs.getTotalSize()){
                return -1;
            }
            else if(lhs.getTotalSize() < rhs.getTotalSize()){
                return 1;
            }
            else{
                return 0;
            }
        }
    }
}
