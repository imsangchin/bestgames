
package com.miui.optimizecenter.cache;

import android.text.TextUtils;

import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;
import com.miui.common.BaseMultiExpandableListAdapter;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.cache.StateButton.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class CacheExpandableListAdater
        extends
        BaseMultiExpandableListAdapter<CacheGroupModel, CacheModel,
        CacheExpandableListItemGroupView,
        CacheExpandableListItemChildView,
        CacheExpandableListItemSubView> {

    public CacheExpandableListAdater(EventHandler handler) {
        super(handler);
    }

    public void updateData(Map<String, List<CacheModel>> data, CacheGroupComparator comparator) {
        List<CacheGroupModel> groupList = new ArrayList<CacheGroupModel>();
        Map<CacheGroupModel, List<CacheModel>> childMap = new HashMap<CacheGroupModel, List<CacheModel>>();

        Set<String> pkgNames = data.keySet();
        for (String pkgName : pkgNames) {
            List<CacheModel> cacheList = data.get(pkgName);

            CacheGroupModel group = new CacheGroupModel();
            group.setPackageName(pkgName);

            long totalSize = 0;
            int adviseDeleteCount = 0;

            for (CacheModel cache : cacheList) {
                totalSize += cache.getFileSize();
                if (cache.adviseDelete()) {
                    ++adviseDeleteCount;
                }
            }

            group.setTotalSize(totalSize);
            if (adviseDeleteCount == 0) {
                group.setState(State.UNCHECK);
            } else if (adviseDeleteCount == cacheList.size()) {
                group.setState(State.CHECKED);
            } else {
                group.setState(State.MIDDLE);
            }
            if (TextUtils.equals(pkgName, ApkIconHelper.PKG_SYSTEM_CACHE)) {
                group.setAppName(getContext().getString(R.string.cache_title_system_cache));
            } else if (TextUtils.equals(pkgName, ApkIconHelper.PKG_EMPTY_FOLDER)) {
                group.setAppName(getContext().getString(R.string.cache_title_empty_folder2));
            } else {
                group.setAppName(AndroidUtils.getAppName(getContext(), pkgName).toString());
            }

            groupList.add(group);
            childMap.put(group, cacheList);
        }
        if (groupList.size() >= 2) {
            Collections.sort(groupList, comparator);
        }
        updateData(groupList, childMap);
    }

    @Override
    protected int getGroupLayout() {
        return R.layout.op_cache_expandable_list_item_group_view;
    }

    @Override
    protected int getChildMainLayout() {
        return R.layout.op_cache_expandable_list_item_child_view;
    }

    @Override
    protected int getChildSubLayout() {
        return R.layout.op_cache_expandable_list_item_sub_view;
    }

}
