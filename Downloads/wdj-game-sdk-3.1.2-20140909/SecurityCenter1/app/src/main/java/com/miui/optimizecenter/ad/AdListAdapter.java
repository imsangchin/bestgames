
package com.miui.optimizecenter.ad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

import com.miui.securitycenter.R;

public class AdListAdapter extends ExpandableListAdapter {

    private EventHandler mEventHandler;

    private List<AdModel> mData = new ArrayList<AdModel>();

    public AdListAdapter(EventHandler handler) {
        mEventHandler = handler;
    }

    public void updateData(List<AdModel> data) {
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
            convertView = inflater.inflate(R.layout.op_ad_list_item_main_view, null);
        }

        AdListItemMainView itemView = (AdListItemMainView) convertView;
        itemView.setTag(R.id.position, position);
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(position));
        return convertView;
    }

    @Override
    public View getSubView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.op_ad_list_item_sub_view, null);
        }

        AdListItemSubView itemView = (AdListItemSubView) convertView;
        itemView.setTag(R.id.position, position);
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(position));
        return convertView;
    }
}
