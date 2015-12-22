
package com.miui.optimizecenter.deepclean;

import miui.app.ActionBar;
import miui.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cleanmaster.sdk.IKSCleaner;
import com.miui.analytics.AnalyticsUtil;
import com.miui.backup.proxy.IBackupProxy;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.securitycenter.AidlProxyHelper;

import miui.app.ActionBar.FragmentViewPagerChangeListener;

import com.miui.securitycenter.R;

public class DeepCleanActivity extends Activity implements FragmentViewPagerChangeListener {

    private ServiceConnection mCleanerConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIKSCleaner = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIKSCleaner = IKSCleaner.Stub.asInterface(service);
            AidlProxyHelper.getInstance().setIKSCleaner(mIKSCleaner);
            AidlProxyHelper.getInstance().bindFileProxy(DeepCleanActivity.this, mFileConnection);
        }
    };

    private ServiceConnection mFileConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIFileProxy = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIFileProxy = IFileProxy.Stub.asInterface(service);
            AidlProxyHelper.getInstance().setIFileProxy(mIFileProxy);
            checkServiceConnection();
        }
    };

    private ServiceConnection mBackupConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIBackupProxy = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIBackupProxy = IBackupProxy.Stub.asInterface(service);
            AidlProxyHelper.getInstance().setIBackupProxy(mIBackupProxy);

            InstalledAppsFragment installedApps = (InstalledAppsFragment) getActionBar()
                    .getFragmentAt(TAB_INSTALLED_APPS);
            installedApps.setIBackupProxy(mIBackupProxy);
        }
    };

    public static final int TAB_INSTALLED_APPS = 0;
    public static final int TAB_LARGE_FILE = 1;
    public static final int TAB_CACHE = 2;

    public static final int MENU_APPS_SORT_TYPE_ITEM_TIME = Menu.FIRST;
    public static final int MENU_APPS_SORT_TYPE_ITEM_SIZE = Menu.FIRST + 1;

    public static final int MENU_LARGE_FILE_SORT_TYPE_ITEM_SIZE = Menu.FIRST;
    public static final int MENU_LARGE_FILE_SORT_TYPE_ITEM_NAME = Menu.FIRST + 1;

    public static final int MENU_CACHE_SORT_TYPE_ITEM_SIZE = Menu.FIRST;
    public static final int MENU_CACHE_SORT_TYPE_ITEM_NAME = Menu.FIRST + 1;

    private IKSCleaner mIKSCleaner;
    private IFileProxy mIFileProxy;
    private IBackupProxy mIBackupProxy;
    private int position = 0;
    private BaseDeepCleanFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_TRASH);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);

        ActionBar bar = getActionBar();
        bar.setFragmentViewPagerMode(this, getFragmentManager());
        bar.addOnFragmentViewPagerChangeListener(this);

        bar.addFragmentTab(InstalledAppsFragment.TAG,
                bar.newTab().setText(R.string.activity_title_uninstall_apps),
                InstalledAppsFragment.class, null, false);

        bar.addFragmentTab(LargeFilesFragment.TAG,
                bar.newTab().setText(R.string.activity_title_large_file),
                LargeFilesFragment.class, null, false);

        bar.addFragmentTab(CacheFragment.TAG,
                bar.newTab().setText(R.string.activity_title_cache_data),
                CacheFragment.class, null, false);

        onPageSelected(TAB_INSTALLED_APPS);
        position = 0;

        AidlProxyHelper.getInstance().bindCleanProxy(this, mCleanerConnection);
        AidlProxyHelper.getInstance().bindBackupProxy(this, mBackupConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AidlProxyHelper.getInstance().unbindProxy(this, mCleanerConnection);
        AidlProxyHelper.getInstance().unbindProxy(this, mFileConnection);
        AidlProxyHelper.getInstance().unbindProxy(this, mBackupConnection);
    }

    private void checkServiceConnection() {
        if (mIKSCleaner != null && mIFileProxy != null) {
            ActionBar bar = getActionBar();

            BaseDeepCleanFragment installedApps = (BaseDeepCleanFragment) bar
                    .getFragmentAt(TAB_INSTALLED_APPS);
            installedApps.onServiceBinded(DeepCleanActivity.this, mIKSCleaner, mIFileProxy);
            BaseDeepCleanFragment largeFiles = (BaseDeepCleanFragment) bar
                    .getFragmentAt(TAB_LARGE_FILE);
            largeFiles.onServiceBinded(DeepCleanActivity.this, mIKSCleaner, mIFileProxy);
            BaseDeepCleanFragment cache = (BaseDeepCleanFragment) bar
                    .getFragmentAt(TAB_CACHE);
            cache.onServiceBinded(DeepCleanActivity.this, mIKSCleaner, mIFileProxy);
        }
    }

    @Override
    public void onPageScrolled(int position, float ratio, boolean fromHasActionMenu,
            boolean toHasActionMenu) {
        // ignore
    }

    @Override
    public void onPageSelected(int position) {
        ActionBar bar = getActionBar();
        this.position = position;
        fragment = (BaseDeepCleanFragment) bar.getFragmentAt(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // ignore
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.apps_sort_type_item_time);
        menu.add(R.string.apps_sort_type_item_size);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        switch (position) {
            case TAB_INSTALLED_APPS:
                menu.add(0, MENU_APPS_SORT_TYPE_ITEM_TIME, 0, R.string.apps_sort_type_item_time);
                menu.add(0, MENU_APPS_SORT_TYPE_ITEM_SIZE, 1, R.string.apps_sort_type_item_size);
                break;
            case TAB_LARGE_FILE:
                menu.add(0, MENU_LARGE_FILE_SORT_TYPE_ITEM_SIZE, 0,
                        R.string.large_file_sort_type_size);
                menu.add(0, MENU_LARGE_FILE_SORT_TYPE_ITEM_NAME, 1,
                        R.string.large_file_sort_type_name);
                break;
            case TAB_CACHE:
                menu.add(0, MENU_CACHE_SORT_TYPE_ITEM_SIZE, 0, R.string.cache_sort_type_item_size);
                menu.add(0, MENU_CACHE_SORT_TYPE_ITEM_NAME, 1, R.string.cache_sort_type_item_name);
                break;
            default:
                return super.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        fragment.onFragmentSortTypeSelected(DeepCleanActivity.this, item.getItemId());
        return super.onOptionsItemSelected(item);
    }
}
