
package com.miui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miui.common.MultiExpandableListView.MultiExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMultiExpandableListAdapter<GD, CD, G extends BindableGroupView<GD>, M extends BindableView<CD>, S extends BindableView<CD>>
        extends
        MultiExpandableListAdapter {
    private EventHandler mEventHandler;

    private Map<GD, List<CD>> mChildData = new HashMap<GD, List<CD>>();

    private List<GD> mGroupData = new ArrayList<GD>();

    public BaseMultiExpandableListAdapter(EventHandler handler) {
        mEventHandler = handler;
    }

    public void updateData(List<GD> groupData, Map<GD, List<CD>> childrenData) {
        mGroupData.clear();
        mChildData.clear();
        mGroupData.addAll(groupData);
        mChildData.putAll(childrenData);
    }

    @Override
    public int getGroupCount() {
        return mGroupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildData.get(mGroupData.get(groupPosition)).size();
    }

    protected abstract int getGroupLayout();

    @Override
    public View getGroupView(LayoutInflater inflater, int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(getGroupLayout(), parent, false);
        }
        // view.setTag(R.id.position, position);
        BindableGroupView v = (BindableGroupView) view;
        v.setEventHandler(mEventHandler);
        v.setExpanded(isExpanded);
        v.fillData(mGroupData.get(groupPosition), groupPosition);
        return view;
    }

    protected abstract int getChildMainLayout();

    @Override
    public View getChildMainView(LayoutInflater inflater, int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(getChildMainLayout(), parent, false);
        }
        // view.setTag(R.id.position, position);
        BindableView v = (BindableView) view;
        v.setEventHandler(mEventHandler);
        GD group = mGroupData.get(groupPosition);
        v.fillData(mChildData.get(group).get(childPosition));
        return view;

    }

    protected abstract int getChildSubLayout();

    @Override
    public View getChildSubView(LayoutInflater inflater, int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(getChildSubLayout(), parent, false);
        }
        // view.setTag(R.id.position, position);
        BindableView v = (BindableView) view;
        v.setEventHandler(mEventHandler);
        GD group = mGroupData.get(groupPosition);
        v.fillData(mChildData.get(group).get(childPosition));
        return view;
    }

}
