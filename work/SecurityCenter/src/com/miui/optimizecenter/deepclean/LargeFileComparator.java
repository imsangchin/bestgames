
package com.miui.optimizecenter.deepclean;

import com.miui.optimizecenter.enums.LargeFileSortType;

import java.util.Comparator;

public class LargeFileComparator implements Comparator<LargeFileModel> {

    private LargeFileSortType mSortType = LargeFileSortType.SIZE;

    public LargeFileComparator() {
        mSortType = LargeFileSortType.SIZE;
    }

    public LargeFileComparator(LargeFileSortType sortType) {
        mSortType = sortType;
    }

    @Override
    public int compare(LargeFileModel lhs, LargeFileModel rhs) {
        if (mSortType == LargeFileSortType.NAME) {
            try {
                return lhs.getName().compareTo(rhs.getName());
            } catch (Exception e) {
                return 0;
            }
        } else {
            if (lhs.getFileSize() > rhs.getFileSize()) {
                return -1;
            } else if (lhs.getFileSize() < rhs.getFileSize()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
