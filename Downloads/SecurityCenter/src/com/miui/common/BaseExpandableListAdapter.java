
package com.miui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseExpandableListAdapter<GD, CD, G extends BindableGroupView<GD>, C extends BindableView<CD>>
        extends android.widget.BaseExpandableListAdapter {

    public static final int NO_POS = -100;

    protected EventHandler mEventHandler;
    protected Context mContext;
    protected LayoutInflater mInflater;

    private Map<GD, List<CD>> mChildData = new HashMap<GD, List<CD>>();

    private List<GD> mGroupData = new ArrayList<GD>();

    public BaseExpandableListAdapter(Context context, EventHandler handler) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mEventHandler = handler;
    }

    public void updateData(List<GD> groupData, Map<GD, List<CD>> childrenData) {
        mGroupData.clear();
        mChildData.clear();
        mGroupData.addAll(groupData);
        mChildData.putAll(childrenData);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groupPosition * 10000 + childPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 10000 + childPosition;
    }

    protected abstract int getChildLayout();

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(getChildLayout(), parent, false);
        }
        // view.setTag(R.id.position, position);
        BindableView v = (BindableView) view;
        v.setEventHandler(mEventHandler);
        GD group = mGroupData.get(groupPosition);
        v.fillData(mChildData.get(group).get(childPosition));
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildData.get(mGroupData.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getGroupCount() {
        return mGroupData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    protected abstract int getGroupLayout();

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(getGroupLayout(), parent, false);
        }
        // view.setTag(R.id.position, position);
        BindableGroupView v = (BindableGroupView) view;
        v.setEventHandler(mEventHandler);
        v.setExpanded(isExpanded);
        v.fillData(mGroupData.get(groupPosition), groupPosition);
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
