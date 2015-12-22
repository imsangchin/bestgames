
package com.miui.permcenter.autostart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miui.common.EventHandler;
import com.miui.common.PinnedBaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class AutoStartListAdapter extends PinnedBaseAdapter {

    private static final AutoStartHeaderComparator HEADER_COMPARATOR = new AutoStartHeaderComparator();
    private static final AutoStartComparator ITEM_COMPARATOR = new AutoStartComparator();

    private EventHandler mEventHandler;
    private LayoutInflater mInflater;

    private Map<AutoStartHeaderModel, List<AutoStartModel>> mData = new HashMap<AutoStartHeaderModel, List<AutoStartModel>>();

    private List<AutoStartHeaderModel> mHeaders = new ArrayList<AutoStartHeaderModel>();

    public AutoStartListAdapter(Context context, EventHandler handler) {
        mInflater = LayoutInflater.from(context);
        mEventHandler = handler;
    }

    public void updateData(Map<AutoStartHeaderModel, Map<String, AutoStartModel>> data) {
        mData.clear();
        mHeaders.clear();

        Set<AutoStartHeaderModel> headers = data.keySet();
        for (AutoStartHeaderModel header : headers) {
            mHeaders.add(header);

            Map<String, AutoStartModel> pkgMap = data.get(header);
            List<AutoStartModel> pkgList = new ArrayList<AutoStartModel>();

            Set<String> pkgs = pkgMap.keySet();
            for (String pkg : pkgs) {
                pkgList.add(pkgMap.get(pkg));
            }

            if (pkgList.size() >= 2) {
                Collections.sort(pkgList, ITEM_COMPARATOR);
            }

            mData.put(header, pkgList);
        }

        if (mHeaders.size() >= 2) {
            Collections.sort(mHeaders, HEADER_COMPARATOR);
        }
    }

    public void setHeaderTitle(AutoStartHeaderType headerType, String title) {
        for (AutoStartHeaderModel header : mHeaders) {
            if (header.getAutoStartHeaderType() == headerType) {
                header.setHeaderTitle(title);
            }
        }
    }

    @Override
    public Object getItem(int section, int position) {
        return position;
    }

    @Override
    public long getItemId(int section, int position) {
        return position;
    }

    @Override
    public int getSectionCount() {
        return mHeaders.size();
    }

    @Override
    public int getCountForSection(int section) {
        return mData.get(mHeaders.get(section)).size();
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pm_auto_start_list_item_view, null);
        }

        AutoStartListItemView itemView = (AutoStartListItemView) convertView;
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(mHeaders.get(section)).get(position));

        return convertView;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pm_auto_start_list_header_view, null);
        }

        AutoStartListHeaderView headerView = (AutoStartListHeaderView) convertView;
        headerView.setEventHandler(mEventHandler);
        headerView.fillData(mHeaders.get(section));

        return convertView;
    }

}
