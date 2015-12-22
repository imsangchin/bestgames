
package com.miui.securitycenter.settings;

import com.miui.securitycenter.R;
import miui.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.miui.securitycenter.settings.ShortcutHelper.Shortcut;

import java.util.ArrayList;
import java.util.List;

public class ShortcutActivity extends Activity {

    private List<Shortcut> mShortcutList = new ArrayList<Shortcut>();
    private ListView mListView;
    private ShortcutAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_activity_shortcut);
        mShortcutList.add(Shortcut.QUICk_CLEANUP);
        mShortcutList.add(Shortcut.OPTIMIZE_CENTER);
        mShortcutList.add(Shortcut.NETWORK_ASSISTANT);
        mShortcutList.add(Shortcut.ANTISPAM);
        mShortcutList.add(Shortcut.POWER_CENTER);
        mShortcutList.add(Shortcut.VIRUS_CENTER);
        mShortcutList.add(Shortcut.PERM_CENTER);

        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new ShortcutAdapter();
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private class ShortcutAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mShortcutList.size();
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
        public View getView(int position, View convertView, ViewGroup arg2) {
            if (convertView == null) {
                convertView = View.inflate(ShortcutActivity.this,
                        R.layout.op_shortcut_list_item_view, null);
            }

            ShortcutListItemView itemView = (ShortcutListItemView) convertView;
            itemView.fillData(mShortcutList.get(position));
            return convertView;
        }

    }
}
