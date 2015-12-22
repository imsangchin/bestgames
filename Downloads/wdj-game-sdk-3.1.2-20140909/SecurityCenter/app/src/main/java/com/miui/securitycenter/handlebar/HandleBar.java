
package com.miui.securitycenter.handlebar;

import com.miui.common.EventHandler;
import com.miui.common.PinnedBaseAdapter;
import com.miui.common.PinnedHeaderListView;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.OnActionButtonClickEvent;
import com.miui.securitycenter.event.OnHandleItemClickEvent;
import com.miui.securitycenter.event.OnManualItemLongClickEvent;
import com.miui.securitycenter.system.SystemCheckManager;
import com.miui.securitycenter.system.SystemItem;
import com.miui.securitycenter.system.SystemItemModel;
import com.miui.securitycenter.system.SystemActivity.SystemAdapter;
import com.miui.securitycenter.system.SystemListItemView;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.miui.securitycenter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HandleBar extends LinearLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private Button mQuickCleanupButton;
    private PinnedHandleListAdapter mPinnedHandleListAdapter;

    private ListView mHandleItemListView;

    public HandleBar(Context context) {
        this(context, null);
    }

    public HandleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandleItemListView = (ListView) findViewById(R.id.handle_item_list_view);

        mQuickCleanupButton = (Button) findViewById(R.id.btn_quick_cleanup);
        mQuickCleanupButton.setOnClickListener(this);

    }

    public void setListViewSelection(int position) {
        mHandleItemListView.setSelection(position);
    }
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setHandleListAdapter(Context context, EventHandler handler) {
        mPinnedHandleListAdapter = new PinnedHandleListAdapter(context, mEventHandler);
        mHandleItemListView.setAdapter(mPinnedHandleListAdapter);
    }

    public void resetContents() {
    }

    public void setActionButtonText(CharSequence text) {
        mQuickCleanupButton.setText(text);
    }

    public void updateAdapterData(Map<HandleHeaderType, List<HandleItemModel>> modelMap) {
        mPinnedHandleListAdapter.updateData(modelMap);
        mPinnedHandleListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_quick_cleanup) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_ACTION_BUTTON_CLICK,
                    OnActionButtonClickEvent.create());
            return;
        }

    }

    private class PinnedHandleListAdapter extends PinnedBaseAdapter {

        private HeaderTypeComparator mHeaderTypeComparator = new HeaderTypeComparator();
        private EventHandler mHandler;
        private LayoutInflater mInflater;

        private Map<HandleHeaderType, List<HandleItemModel>> mData = new HashMap<HandleHeaderType, List<HandleItemModel>>();

        private List<HandleHeaderType> mHeaders = new ArrayList<HandleHeaderType>();

        public PinnedHandleListAdapter(Context context, EventHandler handler) {
            mHandler = handler;
            mInflater = LayoutInflater.from(context);
        }

        public void updateData(Map<HandleHeaderType, List<HandleItemModel>> data) {
            mData.clear();
            mData.putAll(data);

            mHeaders.clear();
            Set<HandleHeaderType> keys = mData.keySet();
            for (HandleHeaderType header : keys) {
                mHeaders.add(header);
            }
            if (mHeaders.size() >= 2) {
                Collections.sort(mHeaders, mHeaderTypeComparator);
            }
        }

        @Override
        public Object getItem(int section, int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int section, int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getSectionCount() {
            // TODO Auto-generated method stub
            return mHeaders.size();
        }

        @Override
        public int getCountForSection(int section) {
            // TODO Auto-generated method stub
            return mData.get(mHeaders.get(section)).size();
        }

        @Override
        public View getItemView(int section, int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final HandleItemModel model = mData.get(mHeaders.get(section)).get(position);
            HandleHeaderType type = model.getType();
            HandleListBaseItemView itemView;

            if (convertView != null) {
                itemView = (HandleListBaseItemView) convertView;
            }
            switch (type) {
                case Auto:
                    itemView = (HandleListAutoItemView) mInflater
                            .inflate(R.layout.m_activity_handle_auto_list_view, parent, false);
                    break;
                case Manual:
                    itemView = (HandleListManualItemView) mInflater
                            .inflate(R.layout.m_activity_handle_manual_list_view, parent, false);
                    break;
                default:
                    itemView = (HandleListBaseItemView) convertView;
                    break;
            }
            itemView.setEventHandler(mHandler);
            itemView.fillData(model);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (model.getType() == HandleHeaderType.Auto) {
                        mEventHandler.sendEventMessage(EventType.EVENT_ON_HANDLE_ITEM_CLICK,
                                OnHandleItemClickEvent.create(model.getItem()));
                    }
                }
            });
            itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
                    if (model.getType() == HandleHeaderType.Manual) {
                        mEventHandler.sendEventMessage(EventType.EVENT_ON_MANUAL_ITEM_LONG_CLICK,
                                OnManualItemLongClickEvent.create(model));
                        return true;
                    }
                    return false;
                }
            });
            return itemView;
        }

        @Override
        public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            HandleHeaderType type = mHeaders.get(section);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.m_activity_handle_header_view, parent, false);
            }
            HandleListHeaderView headerView = (HandleListHeaderView) convertView;
            headerView.fillData(type);
            headerView.setEnabled(false);
            headerView.setOnClickListener(null);
            return headerView;
        }

    }

    private class HeaderTypeComparator implements Comparator<HandleHeaderType> {

        @Override
        public int compare(HandleHeaderType lhs, HandleHeaderType rhs) {
            // TODO Auto-generated method stub
            if (lhs.ordinal() < rhs.ordinal()) {
                return -1;
            }
            else if (lhs.ordinal() > rhs.ordinal()) {
                return 1;
            }
            return 0;
        }
    }
}
