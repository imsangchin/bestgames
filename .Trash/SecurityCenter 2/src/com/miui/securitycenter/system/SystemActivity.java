
package com.miui.securitycenter.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.PinnedBaseAdapter;
import com.miui.common.PinnedHeaderListView;
import com.miui.securitycenter.NotificationService;
import com.miui.securitycenter.Preferences;
import com.miui.securitycenter.RecentTaskMonitor;
import com.miui.securitycenter.ScoreConstants;
import com.miui.securitycenter.event.CleanupListEvent;
import com.miui.securitycenter.event.EventType;

import com.miui.securitycenter.R;

public class SystemActivity extends Activity implements View.OnClickListener {

    private class LoadProtectionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mProtectionData.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SystemItemModel virus = new SystemItemModel();
            virus.setItem(SystemItem.VIRUS);
            if (mSystemCheckManager.hasVirus()) {
                virus.setSystemType(SystemType.DANGEROUS);
                virus.setTitle(getString(R.string.title_virus_no));
                virus.setSummary(getString(R.string.summary_virus,
                        mSystemCheckManager.getVirusAppName(),
                        mSystemCheckManager.getVirusAppCount()));
                addProtectionItem(SystemType.DANGEROUS, virus);
                mSystemCheckManager.setHasVirus(true);
            } else {
                virus.setSystemType(SystemType.SECURITY);
                virus.setTitle(getString(R.string.title_virus_yes));
                addProtectionItem(SystemType.SECURITY, virus);
                mSystemCheckManager.setHasVirus(false);
            }

            SystemItemModel permission = new SystemItemModel();
            permission.setItem(SystemItem.PERMISSION);
            if (mSystemCheckManager.isPermissionMonitorOn()) {
                permission.setSystemType(SystemType.SECURITY);
                permission.setTitle(getString(R.string.title_permission_yes));
                permission.setSummary(getString(R.string.summary_permission));
                addProtectionItem(SystemType.SECURITY, permission);
                mSystemCheckManager.setPermissonScore(ScoreConstants.SYSTEM_ITEM_PERMISSION_SCORE);
            } else {
                permission.setSystemType(SystemType.DANGEROUS);
                permission.setTitle(getString(R.string.title_permission_no));
                permission.setSummary(getString(R.string.summary_permission));
                addProtectionItem(SystemType.DANGEROUS, permission);
                mSystemCheckManager.setPermissonScore(0);
            }

            SystemItemModel mms = new SystemItemModel();
            mms.setItem(SystemItem.MMS);
            if (mSystemCheckManager.isSMSFilterOn()) {
                mms.setSystemType(SystemType.SECURITY);
                mms.setTitle(getString(R.string.title_mms_yes));
                mms.setSummary(getString(R.string.summary_mms));
                addProtectionItem(SystemType.SECURITY, mms);
                mSystemCheckManager.setMmsScore(ScoreConstants.SYSTEM_ITEM_MMS_SCORE);
            } else {
                mms.setSystemType(SystemType.DANGEROUS);
                mms.setTitle(getString(R.string.title_mms_no));
                mms.setSummary(getString(R.string.summary_mms));
                addProtectionItem(SystemType.DANGEROUS, mms);
                mSystemCheckManager.setMmsScore(0);
            }

            SystemItemModel dev = new SystemItemModel();
            dev.setItem(SystemItem.DEV_MODE);
            SystemItemModel usb = new SystemItemModel();
            usb.setItem(SystemItem.USB);
            if (mSystemCheckManager.isDevModeOn()) {
                dev.setSystemType(SystemType.DANGEROUS);
                dev.setTitle(getString(R.string.title_dev_no));
                dev.setSummary(getString(R.string.summary_dev));
                addProtectionItem(SystemType.DANGEROUS, dev);
                mSystemCheckManager.setDevScore(0);
                if (mSystemCheckManager.isUsbDebugOn()) {
                    usb.setSystemType(SystemType.DANGEROUS);
                    usb.setTitle(getString(R.string.title_usb_no));
                    usb.setSummary(getString(R.string.summary_usb));
                    addProtectionItem(SystemType.DANGEROUS, usb);
                    mSystemCheckManager.setUsbScore(0);
                } else {
                    usb.setSystemType(SystemType.SECURITY);
                    usb.setTitle(getString(R.string.title_usb_yes));
                    usb.setSummary(getString(R.string.summary_usb));
                    addProtectionItem(SystemType.SECURITY, usb);
                    mSystemCheckManager.setUsbScore(ScoreConstants.SYSTEM_ITEM_USB_DEBUG_SCORE);
                }
            }
            else{
                dev.setSystemType(SystemType.SECURITY);
                dev.setTitle(getString(R.string.title_dev_yes));
                dev.setSummary(getString(R.string.summary_dev));
                addProtectionItem(SystemType.SECURITY, dev);
                mSystemCheckManager.setDevScore(ScoreConstants.SYSTEM_ITEM_DEV_OFF_SCORE);
                usb.setSystemType(SystemType.SECURITY);
                usb.setTitle(getString(R.string.title_usb_yes));
                usb.setSummary(getString(R.string.summary_usb));
                addProtectionItem(SystemType.SECURITY, usb);
                mSystemCheckManager.setUsbScore(ScoreConstants.SYSTEM_ITEM_USB_DEBUG_SCORE);
            }

            SystemItemModel Virus_lib = new SystemItemModel();
            Virus_lib.setItem(SystemItem.VIRUS_LIB);
            Virus_lib.setSummary(getString(R.string.summary_virus_update));
            if (mSystemCheckManager.isVirusLibAutoUpdateOn()) {
                Virus_lib.setSystemType(SystemType.SECURITY);
                Virus_lib.setTitle(getString(R.string.title_virus_update_yes));
                addProtectionItem(SystemType.SECURITY, Virus_lib);
                mSystemCheckManager
                        .setVirusLibScore(ScoreConstants.SYSTEM_ITEM_VIRUS_AUTO_UPDATE_SCORE);
            } else {
                Virus_lib.setSystemType(SystemType.DANGEROUS);
                Virus_lib.setTitle(getString(R.string.title_virus_update_no));
                addProtectionItem(SystemType.DANGEROUS, Virus_lib);
                mSystemCheckManager.setVirusLibScore(0);
            }

            SystemItemModel install_monitor = new SystemItemModel();
            install_monitor.setItem(SystemItem.INSTALL_MONITOR);
            install_monitor.setSummary(getString(R.string.summary_install_virus));
            if (mSystemCheckManager.isInstallMotinorOn()) {
                install_monitor.setSystemType(SystemType.SECURITY);
                install_monitor.setTitle(getString(R.string.title_install_virus_yes));
                addProtectionItem(SystemType.SECURITY, install_monitor);
                mSystemCheckManager
                        .setInstallScore(ScoreConstants.SYSTEM_ITEM_INSTALL_MONITOR_SCORE);
            } else {
                install_monitor.setSystemType(SystemType.DANGEROUS);
                install_monitor.setTitle(getString(R.string.title_install_virus_no));
                addProtectionItem(SystemType.DANGEROUS, install_monitor);
                mSystemCheckManager.setInstallScore(0);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mSystemAdapter.updateData(mProtectionData);
            mSystemAdapter.notifyDataSetChanged();
        }

    }

    private void addProtectionItem(SystemType type, SystemItemModel itemModel) {
        List<SystemItemModel> items = mProtectionData.get(type);
        if (items == null) {
            items = new ArrayList<SystemItemModel>();
            mProtectionData.put(type, items);
        }
        items.add(itemModel);
        if (items.size() >= 2) {
            Collections.sort(items, mItemsComparator);
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventType.EVENT_ON_SYSTEM_ITEM_CHECKED:
                    onProtectionItemClick((OnSystemItemClickEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEANUP_LIST_EVENT:
                    optimizeProtection((CleanupListEvent) msg.obj);
                    break;
                default:
                    break;
            }
        }

        public void optimizeProtection(CleanupListEvent event) {
            mSystemCheckManager.deleteVirusApps(true);
            mSystemCheckManager.openPermissionMonitor();
            mSystemCheckManager.openSMSFilter();
            mSystemCheckManager.closeDevMode();
            mSystemCheckManager.openVirusLibAutoUpdate();
            mSystemCheckManager.openInstallMotinor();
            new LoadProtectionTask().execute();
        }

        public void onProtectionItemClick(OnSystemItemClickEvent event) {
            SystemItemModel model = event.getProtectionModel();
            switch (model.getItem()) {
                case VIRUS:
                    mSystemCheckManager.deleteVirusApps(true);
                    break;
                case PERMISSION:
                    mSystemCheckManager.openPermissionMonitor();
                    break;
                case MMS:
                    mSystemCheckManager.openSMSFilter();
                    break;
                case USB:
                    mSystemCheckManager.closeUsbDebug();
                    break;
                case VIRUS_LIB:
                    mSystemCheckManager.openVirusLibAutoUpdate();
                    break;
                case INSTALL_MONITOR:
                    mSystemCheckManager.openInstallMotinor();
                    break;
                case DEV_MODE:
                    mSystemCheckManager.closeDevMode();
                    break;
                default:
                    break;
            }
            new LoadProtectionTask().execute();
        }
    };

    private SystemItemComparator mItemsComparator = new SystemItemComparator();

    private Map<SystemType, List<SystemItemModel>> mProtectionData = new HashMap<SystemType, List<SystemItemModel>>();

    private PinnedHeaderListView mSystemListView;
    private Button mOptimize;

    private SystemAdapter mSystemAdapter;

    private SystemCheckManager mSystemCheckManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_activity_system);

        mSystemCheckManager = SystemCheckManager.getInstance(this);

        mSystemListView = (PinnedHeaderListView) findViewById(R.id.protection_list);
        mOptimize = (Button) findViewById(R.id.optimize);
        mOptimize.setOnClickListener(this);

        mSystemAdapter = new SystemAdapter(mEventHandler, this);
        mSystemListView.setAdapter(mSystemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new LoadProtectionTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        mEventHandler.sendEventMessage(EventType.EVENT_CLEANUP_LIST_EVENT,
                CleanupListEvent.create());
    }

    public class SystemAdapter extends PinnedBaseAdapter {

        private SystemHeaderComparator mHeadersComparator = new SystemHeaderComparator();

        private Map<SystemType, List<SystemItemModel>> mData = new HashMap<SystemType, List<SystemItemModel>>();
        private List<SystemHeaderModel> mHeaders = new ArrayList<SystemHeaderModel>();

        private EventHandler mEventHandler;
        private LayoutInflater mInflater;
        private Context mContext;

        public SystemAdapter(EventHandler handler, Context context) {
            mEventHandler = handler;
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public void updateData(Map<SystemType, List<SystemItemModel>> data) {
            mData.clear();
            mData.putAll(data);

            mHeaders.clear();
            Set<SystemType> keys = mData.keySet();
            for (SystemType type : keys) {
                SystemHeaderModel header = new SystemHeaderModel();
                header.setSystemType(type);

                switch (type) {
                    case SECURITY:
                        String security = getString(R.string.protection_category_security_title);
                        header.setHeader(String.format(security, mData.get(SystemType.SECURITY)
                                .size()));
                        break;
                    case DANGEROUS:
                        String dangerous = getString(R.string.protection_category_dangerous_title);
                        header.setHeader(String.format(dangerous,
                                mData.get(SystemType.DANGEROUS).size()));
                        break;
                    case RECOMMEND:
                        String recommend = getString(R.string.protection_category_recommend_title);
                        header.setHeader(String.format(recommend,
                                mData.get(SystemType.RECOMMEND).size()));
                        break;
                    default:
                        break;
                }
                mHeaders.add(header);
            }
            Collections.sort(mHeaders, mHeadersComparator);
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
            return mHeaders.size();
        }

        @Override
        public int getCountForSection(int section) {
            return mData.get(mHeaders.get(section).getProtectionType()).size();
        }

        @Override
        public View getItemView(int section, int position, View convertView, ViewGroup parent) {
            SystemItemModel itemModel = mData.get(mHeaders.get(section).getProtectionType())
                    .get(position);

            SystemListItemView itemView = null;
            if (convertView != null) {
                itemView = (SystemListItemView) convertView;
            } else {
                itemView = (SystemListItemView) mInflater.inflate(
                        R.layout.m_system_list_item_view, null);
            }
            itemView.setEventHandler(mEventHandler);
            itemView.fillData(itemModel);
            return itemView;
        }

        @Override
        public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.m_system_header_view, null);
            }

            SystemHeaderModel headerModel = mHeaders.get(section);

            TextView header = (TextView) convertView.findViewById(R.id.header_view);
            header.setText(headerModel.getHeader());
            if (headerModel.getProtectionType() == SystemType.DANGEROUS) {
                header.setTextColor(getResources().getColor(R.color.system_header_text_color));
            } else {
                header.setTextAppearance(mContext, miui.R.style.TextAppearance_Small);
            }

            return convertView;
        }
    }

    private class SystemItemComparator implements Comparator<SystemItemModel> {

        @Override
        public int compare(SystemItemModel lhs, SystemItemModel rhs) {
            if (lhs.getItem().ordinal() > rhs.getItem().ordinal()) {
                return 1;
            } else if (lhs.getItem().ordinal() < rhs.getItem().ordinal()) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    private class SystemHeaderComparator implements Comparator<SystemHeaderModel> {

        @Override
        public int compare(SystemHeaderModel lhs, SystemHeaderModel rhs) {
            if (lhs.getProtectionType().ordinal() > rhs.getProtectionType().ordinal()) {
                return 1;
            } else if (lhs.getProtectionType().ordinal() < rhs.getProtectionType().ordinal()) {
                return -1;
            } else {
                return 0;
            }
        }

    }
}
