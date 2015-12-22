
package com.miui.optimizecenter.deepclean;

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

public class LargeFilesListAdapter extends ExpandableListAdapter {

    public EventHandler mEventHandler;

    private List<LargeFileModel> mData = new ArrayList<LargeFileModel>();

    public LargeFilesListAdapter(EventHandler handler) {
        mEventHandler = handler;
    }

    public void updateData(List<LargeFileModel> data) {
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
            convertView = inflater.inflate(R.layout.op_large_files_list_item_main_view, null);
        }

        LargeFilesListItemMainView itemView = (LargeFilesListItemMainView) convertView;
        itemView.setTag(R.id.position, position);
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(position));
        return convertView;
    }

    @Override
    public View getSubView(LayoutInflater inflater, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.op_large_files_list_item_sub_view, null);
        }

        LargeFilesListItemSubView itemView = (LargeFilesListItemSubView) convertView;
        itemView.setTag(R.id.position, position);
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mData.get(position));
        return convertView;
    }

}
