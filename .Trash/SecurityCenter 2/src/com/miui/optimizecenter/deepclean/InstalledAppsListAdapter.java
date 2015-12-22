
package com.miui.optimizecenter.deepclean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView.ExpandableListAdapter;
import com.miui.optimizecenter.ad.AdListItemMainView;
import com.miui.optimizecenter.ad.AdListItemSubView;
import com.miui.optimizecenter.apk.ApkModel;

import java.util.ArrayList;
import java.util.List;

import com.miui.securitycenter.R;

public class InstalledAppsListAdapter extends ExpandableListAdapter {

    public EventHandler mEventHandler;

    private List<InstalledAppModel> mData = new ArrayList<InstalledAppModel>();

    public InstalledAppsListAdapter(EventHandler handler) {
        mEventHandler = handler;
    }

    public void updateData(List<InstalledAppModel> data) {
        mData.clear();
        mData.addAll(data);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public View getMainView(LayoutInflater inflater, int position, View convertView,
            ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.op_installed_apps_list_item_main_view, null);
        }

        InstalledAppsListItemMainView itemView = (InstalledAppsListItemMainView) convertView;
        itemView.setTag(R.id.position, position);
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(position));
        return convertView;
    }

    @Override
    public View getSubView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.op_installed_apps_list_item_sub_view, null);
        }

        InstalledAppsListItemSubView itemView = (InstalledAppsListItemSubView) convertView;
        itemView.setTag(R.id.position, position);
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(position));
        return convertView;
    }

}
