
package com.miui.permcenter.permissions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lbe.security.bean.AppPermissionConfig;
import com.miui.common.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class PermissionAppsEditorListAdapter extends BaseAdapter {

    private EventHandler mEventHandler;
    private LayoutInflater mInflater;
    private long mPermissionId;

    private boolean isItemEnable;

    private PermissionAppsComparator mComparator;

    private List<AppPermissionConfig> mData = new ArrayList<AppPermissionConfig>();

    public PermissionAppsEditorListAdapter(Context context, EventHandler handler, long permissionId) {
        mEventHandler = handler;
        mInflater = LayoutInflater.from(context);
        mPermissionId = permissionId;
        mComparator = new PermissionAppsComparator(context, permissionId);
    }

    public void updateData(Map<String, AppPermissionConfig> data) {
        mData.clear();
        Set<String> pkgNames = data.keySet();
        for (String pkgName : pkgNames) {
            mData.add(data.get(pkgName));
        }
        if (mData.size() >= 2) {
            Collections.sort(mData, mComparator);
        }
    }

    public boolean isItemEnable() {
        return isItemEnable;
    }

    public void setItemEnable(boolean isItemEnable) {
        this.isItemEnable = isItemEnable;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pm_permission_apps_list_item_view, null);
        }

        PermissionAppsListItemView itemView = (PermissionAppsListItemView) convertView;
        itemView.setEventHandler(mEventHandler);
        itemView.fillData(mPermissionId, mData.get(position),isItemEnable);

        return convertView;
    }

}
