package com.miui.powercenter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.miui.securitycenter.R;

import java.util.ArrayList;
import java.util.List;

public class PowerRankAdapter extends BaseAdapter {

    List<BatterySipper> mList = new ArrayList<BatterySipper>();

    static class ViewHolder {
        final TextView mTitle;
        final ImageView mIcon;
        final ProgressBar mProgressBar;
        final TextView mProgressText;

        public ViewHolder(View view) {
            mTitle = (TextView) view.findViewById(android.R.id.title);
            mIcon = (ImageView) view.findViewById(android.R.id.icon);
            mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            mProgressText = (TextView) view.findViewById(android.R.id.text1);
        }
    }

    public void add(BatterySipper item) {
        mList.add(item);
    }

    public void removeAll() {
        mList.clear();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.pc_power_percentage_item, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        BatterySipper item = (BatterySipper) getItem(position);
        int percent = (int) Math.round(item.percent);
        final ViewHolder vh = (ViewHolder) convertView.getTag();
        vh.mTitle.setText(item.name);
        vh.mProgressBar.setMax(100);
        vh.mProgressBar.setProgress(percent);
        vh.mProgressText.setText(String.format("%d%%", percent));
        if (item.icon != null) {
            vh.mIcon.setBackground(item.icon);
        }

        return convertView;
    }
}
