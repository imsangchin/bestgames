
package com.miui.permcenter.root;

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

public class RootListAdapter extends PinnedBaseAdapter {

    private static final RootHeaderComparator HEADER_COMPARATOR = new RootHeaderComparator();
    private static final RootComparator ITEM_COMPARATOR = new RootComparator();

    private EventHandler mEventHandler;
    private LayoutInflater mInflater;

    private Map<RootHeaderModel, List<RootModel>> mData = new HashMap<RootHeaderModel, List<RootModel>>();

    private List<RootHeaderModel> mHeaders = new ArrayList<RootHeaderModel>();

    public RootListAdapter(Context context, EventHandler handler) {
        mInflater = LayoutInflater.from(context);
        mEventHandler = handler;
    }

    public void updateData(Map<RootHeaderModel, Map<String, RootModel>> data) {
        mData.clear();
        mHeaders.clear();

        Set<RootHeaderModel> headers = data.keySet();
        for (RootHeaderModel header : headers) {
            mHeaders.add(header);

            Map<String, RootModel> pkgMap = data.get(header);
            List<RootModel> pkgList = new ArrayList<RootModel>();

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
            convertView = mInflater.inflate(R.layout.pm_root_list_item_view, null);
        }

        RootListItemView itemView = (RootListItemView) convertView;
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(mHeaders.get(section)).get(position));

        return convertView;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pm_root_list_header_view, null);
        }

        RootListHeaderView headerView = (RootListHeaderView) convertView;
        headerView.setEventHandler(mEventHandler);
        headerView.fillData(mHeaders.get(section));

        return convertView;
    }

}
