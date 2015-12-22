
package com.miui.securitycenter;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.common.AnimationListenerAdapter;
import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;
import com.miui.common.PinnedBaseAdapter;
import com.miui.securitycenter.MenuBar.MenuItem;
import com.miui.securitycenter.handlebar.WeightConstants;
import com.miui.securitycenter.handlebar.HandleItem;
import com.miui.securitycenter.handlebar.HandleHeaderType;
import com.miui.securitycenter.handlebar.HandleListBaseItemView;
import com.miui.securitycenter.handlebar.HandleListManualItemView;
import com.miui.securitycenter.handlebar.HandleListAutoItemView;
import com.miui.securitycenter.handlebar.HandleListHeaderView;
import com.miui.securitycenter.cache.CacheActivity;
import com.miui.securitycenter.cache.CacheCheckManager;
import com.miui.securitycenter.cache.CacheCheckManager.CacheCleanupCallback;
import com.miui.securitycenter.cache.CacheCheckManager.CacheScanCallback;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.OnActionButtonClickEvent;
import com.miui.securitycenter.event.OnCancelOptimizeButtonClickEvent;
import com.miui.securitycenter.event.OnCleanupItemEvent;
import com.miui.securitycenter.event.OnFinishCleanupEvent;
import com.miui.securitycenter.event.OnFinishScanEvent;
import com.miui.securitycenter.event.OnHandleItemClickEvent;
import com.miui.securitycenter.event.OnManualItemClickEvent;
import com.miui.securitycenter.event.OnManualItemLongClickEvent;
import com.miui.securitycenter.event.OnMenuItemClickEvent;
import com.miui.securitycenter.event.OnMenuItemLongClickEvent;
import com.miui.securitycenter.event.OnScanItemEvent;
import com.miui.securitycenter.event.OnStartCleanupEvent;
import com.miui.securitycenter.event.OnStartScanEvent;
import com.miui.securitycenter.event.RefreshCheckingBarEvent;
import com.miui.securitycenter.event.RefreshHandleAutoItemEvent;
import com.miui.securitycenter.event.RefreshManualItemsEvent;
import com.miui.securitycenter.event.RefreshScoreEvent;
import com.miui.securitycenter.event.StartQuickScanEvent;
import com.miui.securitycenter.event.ViewSettingsEvent;
import com.miui.securitycenter.handlebar.HandleItemModel;
import com.miui.securitycenter.manualitem.ItemListManager;
import com.miui.securitycenter.manualitem.ManualItemCheckManager;
import com.miui.securitycenter.manualitem.ManualItemCheckManager.ManualItemScanCallback;
import com.miui.securitycenter.manualitem.WhiteListManager;
import com.miui.securitycenter.memory.IMemoryCheck;
import com.miui.securitycenter.memory.IMemoryCleanupCallback;
import com.miui.securitycenter.memory.IMemoryScanCallback;
import com.miui.securitycenter.memory.MemoryActivity;
import com.miui.securitycenter.memory.MemoryCheckManager;
import com.miui.securitycenter.memory.MemoryCheckManager.OnServiceConnectedListener;
import com.miui.securitycenter.settings.SettingsActivity;
import com.miui.securitycenter.settings.ShortcutHelper;
import com.miui.securitycenter.settings.ShortcutHelper.Shortcut;
import com.miui.securitycenter.system.SystemActivity;
import com.miui.securitycenter.system.SystemCheckManager;
import com.miui.securitycenter.system.SystemCheckManager.SystemCleanupCallback;
import com.miui.securitycenter.system.SystemCheckManager.SystemScanCallback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import miui.app.AlertDialog;
import miui.os.Build;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.MiuiIntent;
import miui.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    public enum CleanupButtonStatus {
        NORMAL, SCANNING, SCANNED, CLEANNING, CLEANNED
    }

    // manual item scan
    private ManualItemScanCallback mManualItemScanCallback = new ManualItemScanCallback() {

        @Override
        public void onStartScan() {
            // TODO Auto-generated method stub
            mIsManualItemScanned = false;
        }

        @Override
        public boolean onScanItem(HandleItemModel model) {
            // TODO Auto-generated method stub
            if (!mWhiteListManager.inWhiteList(model.getItem())) {
                updateManualModelMap(model);
            }
            return mForceStopped;
        }

        @Override
        public void onFinishScan() {
            // TODO Auto-generated method stub
            if (!mForceStopped) {
                mIsManualItemScanned = true;
                mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_CHECKINGBAR,
                        RefreshCheckingBarEvent.create());
            }
        }

    };

    // system scan
    private SystemScanCallback mSystemScanCallback = new SystemScanCallback() {

        private int mCount;
        private int mTotalSystemCount;

        @Override
        public void onStartScan(int totalCount) {
            mIsSystemScanned = false;
            mTotalSystemCount = totalCount;
            mCount = 1;
        }

        @Override
        public boolean onScanItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCAN_ITEM,
                    OnScanItemEvent.create(descx, mCount++, mTotalSystemCount));
            // TODO
            return mForceStopped;
        }

        @Override
        public void onFinishScan() {
            if (!mForceStopped) {
                mIsSystemScanned = true;
                mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN,
                        OnFinishScanEvent.create(HandleItem.SYSTEM));
            }
        }
    };

    // system clean up
    private SystemCleanupCallback mSystemCleanupCallback = new SystemCleanupCallback() {

        @Override
        public void onStartCleanup() {
            mIsSystemScanned = false;
        }

        @Override
        public boolean onCleanupItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_ITEM,
                    OnCleanupItemEvent.create(getString(R.string.descx_cleanuping_item, descx)));
            return mForceStopped;
        }

        @Override
        public void onFinishCleanup() {
            mIsSystemScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_CLEANUP,
                    OnFinishCleanupEvent.create(HandleItem.SYSTEM));
        }

    };

    // cache scan
    private CacheScanCallback mCacheScanCallback = new CacheScanCallback() {

        private int mCount;
        private int mTotalCacheCount;

        @Override
        public void onStartScan(int totalCount) {
            mIsCacheScanned = false;
            mCount = 1;
            mTotalCacheCount = totalCount;
        }

        @Override
        public boolean onScanItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCAN_ITEM,
                    OnScanItemEvent.create(descx, mCount++, mTotalCacheCount));
            // TODO
            return mForceStopped;
        }

        @Override
        public void onFinishScan() {
            if (!mForceStopped) {
                mIsCacheScanned = true;
                mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN,
                        OnFinishScanEvent.create(HandleItem.CACHE));
            }
        }
    };

    // cache clean up
    private CacheCleanupCallback mCacheCleanupCallback = new CacheCleanupCallback() {

        @Override
        public void onStartCleanup() {
            mIsCacheScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_CLEANUP,
                    OnStartCleanupEvent.create(HandleItem.CACHE));
        }

        @Override
        public boolean onCleanupItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_ITEM,
                    OnCleanupItemEvent.create(getString(R.string.descx_cleanuping_item, descx)));
            return mForceStopped;
        }

        @Override
        public void onFinishCleanup() {
            mIsCacheScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_CLEANUP,
                    OnFinishCleanupEvent.create(HandleItem.CACHE));
        }
    };

    // memory scan
    private IMemoryScanCallback.Stub mMemoryScanCallback = new IMemoryScanCallback.Stub() {

        private int mCount;
        private int mTotalMemoryCount;

        @Override
        public void onStartScan(int totalCount) {
            mIsMemoryScanned = false;
            mTotalMemoryCount = totalCount;
            mCount = 1;
        }

        @Override
        public boolean onScanItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_SCAN_ITEM,
                    OnScanItemEvent.create(descx, mCount++, mTotalMemoryCount));

            // TODO
            return mForceStopped;
        }

        @Override
        public void onFinishScan() {
            if (!mForceStopped) {
                mIsMemoryScanned = true;
                mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_SCAN,
                        OnFinishScanEvent.create(HandleItem.MEMORY));
            }
        }
    };

    // memory clean up
    private IMemoryCleanupCallback.Stub mMemoryCleanupCallback = new IMemoryCleanupCallback.Stub() {

        @Override
        public void onStartCleanup() {
            mIsMemoryScanned = false;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_START_CLEANUP,
                    OnStartCleanupEvent.create(HandleItem.MEMORY));
        }

        @Override
        public boolean onCleanupItem(String descx) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_CLEANUP_ITEM,
                    OnCleanupItemEvent.create(getString(R.string.descx_cleanuping_item, descx)));
            return mForceStopped;
        }

        @Override
        public void onFinishCleanup() {
            mIsMemoryScanned = true;
            mEventHandler.sendEventMessage(EventType.EVENT_ON_FINISH_CLEANUP,
                    OnFinishCleanupEvent.create(HandleItem.MEMORY));
        }
    };

    private EventHandler mEventHandler = new EventHandler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_ON_MENU_ITEM_CLICK:
                    onMenuItemClick((OnMenuItemClickEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_HANDLE_ITEM_CLICK:
                    onHandleItemClick((OnHandleItemClickEvent) msg.obj);
                    break;
                case EventType.EVENT_START_QUICK_SCAN:
                    startQuickScan((StartQuickScanEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_SCAN:
                    onStartScan((OnStartScanEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_SCAN_ITEM:
                    onScanItem((OnScanItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_SCAN:
                    onFinishScan((OnFinishScanEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_START_CLEANUP:
                    onStartCleanup((OnStartCleanupEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_CLEANUP_ITEM:
                    onCleanupItem((OnCleanupItemEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_FINISH_CLEANUP:
                    onFinishCleanup((OnFinishCleanupEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_SETTINGS:
                    viewSettings((ViewSettingsEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_HANDLE_AUTO_ITEMS:
                    refreshHandleAutoItem((RefreshHandleAutoItemEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_SCORE:
                    refreshScore((RefreshScoreEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_ACTION_BUTTON_CLICK:
                    onActionButtonClick((OnActionButtonClickEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_MENU_ITEM_LONG_CLICK:
                    onMenuItemLongClick((OnMenuItemLongClickEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_MANUAL_ITEM_CLICK:
                    onManualItemClick((OnManualItemClickEvent) msg.obj);
                    break;
                case EventType.EVENT_REFRESH_CHECKINGBAR:
                    refreshCheckingBar((RefreshCheckingBarEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_MANUAL_ITEM_LONG_CLICK:
                    onManualItemLongClick((OnManualItemLongClickEvent) msg.obj);
                    break;
                case EventType.EVENT_ON_CANCEL_OPTIMIZE_BUTTON_CLICK:
                    onCancelOptimizeButtonClick((OnCancelOptimizeButtonClickEvent) msg.obj);
                default:
                    break;
            }
        }

        private void onCancelOptimizeButtonClick(OnCancelOptimizeButtonClickEvent event) {
            mMainView.resetListAdapter();
            mEventHandler.removeCallbacks(mScanCacheRunnable);
            mEventHandler.removeCallbacks(mScanSystemRunnable);
            mEventHandler.removeCallbacks(mScanMemoryRunnable);
            mForceStopped = true;
            mCurrentStatus = CleanupButtonStatus.NORMAL;
            mMainView.stopScanningAnimation();
            AnalyticsUtil.track(MainActivity.this,
                    AnalyticsUtil.TRACK_ID_CENTER_CHECK_CANCEL);
        }

        private void onManualItemLongClick(OnManualItemLongClickEvent event) {
            // TODO Auto-generated method stub
            final String[] items = {
                    getString(R.string.button_text_ignore)
            };
            final HandleItemModel model = event.getModel();
            int checkedItem = -1;
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(model.getTitle())
                    .setSingleChoiceItems(items, checkedItem,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        showAlertInfoDialog(model);
                                    }
                                    dialog.dismiss();
                                }
                            })
                    .show();
        }

        private void showAlertInfoDialog(final HandleItemModel model) {
            // TODO Auto-generated method stub
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.button_text_ignore_alert_title))
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mWhiteListManager.insertToWhiteList(model.getItem(),
                                            model.getTitle(), model.getSummary(), model.getWeight());
                                    dialog.dismiss();
                                    new ManualScanTask().execute();
                                }
                            })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private void onManualItemClick(OnManualItemClickEvent event) {
            HandleItemModel model = event.getModel();
            switch (model.getItem()) {
                case MIUI_UPDATE:
                    mManualCheckManager.viewUpdater();
                    break;
                case CLOUD_ACCOUNT:
                    mManualCheckManager.viewCloudAccount(MainActivity.this);
                    break;
                case CLOUD_SERVICE:
                    mManualCheckManager.viewCloudService();
                    break;
                case POWER_OPTIMIZER:
                    mManualCheckManager.viewActionActivity(ActionConstant.ACTION_POWER_MANAGER,
                            false);
                    break;
                case DATA_FLOW:
                    mManualCheckManager
                            .viewActionActivity(ActionConstant.ACTION_MONTH_PACKAGE_SETTING, false);
                    break;
                case TELECOM_OPERAOTR:
                    mManualCheckManager
                            .viewActionActivity(
                                    ActionConstant.ACTION_AUTO_TRAFFIC_CORRECTION_SETTING, false);
                    break;
                case FLOW_VERIFY:
                    mManualCheckManager.viewActionActivity(ActionConstant.ACTION_ENTRANCE, false);
                    break;
                case SAVING_SWITCH:
                    mManualCheckManager.viewActionActivity(ActionConstant.ACTION_TRAFFIC_SAVING,
                            false);
                    break;
                case FLOW_NOTIFICATION:
                    mManualCheckManager
                            .viewActionActivity(ActionConstant.ACTION_STATUS_BAR_SETTING, false);
                    break;
                case FLOW_PURCHASE:
                    mManualCheckManager.viewActionActivity(ActionConstant.ACTION_TRAFFIC_PURCHASE,
                            false);
                    break;
                case PERMANENT_NOTIFICATIONBAR:
                    mManualCheckManager
                            .viewActionActivity(ActionConstant.ACTION_SECURITYCENTER_SETTINGS,
                                    false);
                    break;
                case GARBAGE_LIB:
                    mManualCheckManager
                            .viewActionActivity(ActionConstant.ACTION_GARBAGE_CLEANUP_SETTINGS,
                                    true);
                    break;
                case PERMISSION_ROOT:
                    mManualCheckManager.viewActionActivity(ActionConstant.ACTION_ROOT_MANAGER,
                            false);
                    break;
                case APP_UPDATE:
                    mManualCheckManager.viewMiuiMarket();
                    break;
                default:
                    break;
            }
            new ManualScanTask().execute();
        }

        private void onMenuItemLongClick(OnMenuItemLongClickEvent event) {
            Shortcut shortcut = null;
            int titleResId = -1;
            switch (event.getMenuItem()) {
                case GARBAGE_CLEANUP:
                    shortcut = Shortcut.OPTIMIZE_CENTER;
                    titleResId = R.string.menu_text_garbage_cleanup;
                    break;
                case NETWORK_ASSISTANTS:
                    shortcut = Shortcut.NETWORK_ASSISTANT;
                    titleResId = R.string.menu_text_networkassistants;
                    break;
                case ANTISPAM:
                    shortcut = Shortcut.ANTISPAM;
                    titleResId = R.string.menu_text_antispam;
                    break;
                case POWER_MANAGER:
                    shortcut = Shortcut.POWER_CENTER;
                    titleResId = R.string.menu_text_power_manager;
                    break;
                case ANTIVIRUS:
                    shortcut = Shortcut.VIRUS_CENTER;
                    titleResId = R.string.menu_text_antivirus;
                    break;
                case LICENSE_MANAGER:
                    shortcut = Shortcut.PERM_CENTER;
                    titleResId = R.string.menu_text_license_manager;
                    break;
                default:
                    break;
            }

            if (shortcut == null) {
                return;
            }

            ShortcutHelper mShortcutHelper = ShortcutHelper.getInstance(MainActivity.this);
            showShortcutDialog(shortcut, titleResId, mShortcutHelper.queryShortcut(shortcut));
        }

        private void showShortcutDialog(final Shortcut shortcut, int titleId,
                final boolean installed) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(titleId)
                    .setPositiveButton(installed ? R.string.button_text_uninstall_shortcut
                            : R.string.button_text_create_shortcut,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ShortcutHelper mShortcutHelper = ShortcutHelper
                                            .getInstance(MainActivity.this);

                                    if (installed) {
                                        mShortcutHelper.removeShortcut(shortcut);
                                        Toast.makeText(MainActivity.this,
                                                R.string.toast_shortcut_uninstall,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        mShortcutHelper.createShortcut(shortcut);
                                        Toast.makeText(MainActivity.this,
                                                R.string.toast_shortcut_install,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .show();
        }

        private void onActionButtonClick(OnActionButtonClickEvent event) {
            switch (mCurrentStatus) {
                case SCANNED:
                    mForceStopped = false;
                    mCurrentStatus = CleanupButtonStatus.CLEANNING;
                    mMainView.setActionButtonText(getString(R.string.button_text_cancel_clean));
                    new CleanupTask().execute();
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CENTER_CHECK_ONE_OPT_CLICKED);
                    break;
                case CLEANNING:
                    mForceStopped = true;
                    mCurrentStatus = CleanupButtonStatus.NORMAL;
                    mMainView.setActionButtonText(getString(R.string.button_text_cancel_optimize));
                    mMainView.stopCheckingAnimation();
                    break;
                case CLEANNED:
                    mForceStopped = false;
                    mCurrentStatus = CleanupButtonStatus.NORMAL;
                    mMainView.setActionButtonText(getString(R.string.button_text_cancel_optimize));
                    mMainView.stopCheckingAnimation();
                    break;
                default:
                    break;
            }
        }

        private void refreshScore(RefreshScoreEvent event) {
            mMainView.setScore(getScore(), null);
        }

        public void refreshHandleAutoItem(RefreshHandleAutoItemEvent event) {
            mModelMap.remove(HandleHeaderType.Auto);

            updateAutoModelMap(HandleItem.SYSTEM,
                    getString(R.string.handle_text_system),
                    mSystemCheckManager.getCheckResult(), WeightConstants.SYSTEM);
            try {
                updateAutoModelMap(HandleItem.MEMORY,
                        getString(R.string.handle_text_memory),
                        mMemoryCheck.getCheckResult(), WeightConstants.MEMORY);
            } catch (RemoteException e) {
                // ignore
            }
            updateAutoModelMap(HandleItem.CACHE,
                    getString(R.string.handle_text_cache),
                    mCacheCheckManager.getCheckResult(), WeightConstants.CACHE);
            mMainView.updateAdapterData(mModelMap);
        }

        private void viewSettings(ViewSettingsEvent event) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            AnalyticsUtil.track(MainActivity.this,
                    AnalyticsUtil.TRACK_ID_ENTER_SECURITYCENTER_SETTINGS);
        }

        private void onFinishCleanup(OnFinishCleanupEvent event) {
            refreshHandleAutoItem(RefreshHandleAutoItemEvent.create());
            refreshCheckingBar(RefreshCheckingBarEvent.create());

            if (mIsSystemScanned && mIsMemoryScanned && mIsCacheScanned) {

                mCurrentStatus = CleanupButtonStatus.CLEANNED;
                mMainView.setListViewSelection(LATTER_LISTVIEW_SELECTION);
                mMainView.setActionButtonText(getString(R.string.btn_text_done));
                refreshStatusInfo();
            }
        }

        private void onCleanupItem(OnCleanupItemEvent event) {
            mMainView.setCheckingText(event.getDescx());

            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_SCORE,
                    RefreshScoreEvent.create());
        }

        private void onStartCleanup(OnStartCleanupEvent event) {
            refreshHandleAutoItem(RefreshHandleAutoItemEvent.create());
            refreshCheckingBar(RefreshCheckingBarEvent.create());
        }

        private void onFinishScan(OnFinishScanEvent event) {
            switch (event.getHandleItem()) {
                case SYSTEM:
                    mMainView.setScanningTitle(getString(R.string.scanning_bar_system_title));
                    mEventHandler.removeCallbacks(mScanCacheRunnable);
                    mEventHandler.postDelayed(mScanCacheRunnable, 0);
                    break;
                case MEMORY:
                    mMainView.setScanningTitle(getString(R.string.scanning_bar_memory_title));
                    mEventHandler.removeCallbacks(mScanMemoryRunnable);
                    mEventHandler.postDelayed(mScanSystemRunnable, 500);
                    break;
                case CACHE:
                    new ManualScanTask().execute();
                    refreshScore(RefreshScoreEvent.create());
                    mMainView.setScanningTitle(getString(R.string.scanning_bar_cache_title));
                    Preferences.setLatestOptimizeDate(System.currentTimeMillis());
                    refreshCheckingBar(RefreshCheckingBarEvent.create());
                    refreshHandleAutoItem(RefreshHandleAutoItemEvent.create());
                    mMainView.setListViewSelection(INITIAL_LISTVIEW_SELECTION);
                    mMainView.finishScanningAnimation();
                    break;
                default:
                    break;
            }
        }

        private Runnable finishRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

            }
        };

        private void refreshCheckingBar(RefreshCheckingBarEvent event) {
            if (mIsSystemScanned && mIsMemoryScanned && mIsCacheScanned) {
                int score = getScore();

                if (score == 100) {
                    mCurrentStatus = CleanupButtonStatus.CLEANNED;
                    mMainView.setActionButtonText(getString(R.string.btn_text_done));
                } else {
                    mCurrentStatus = CleanupButtonStatus.SCANNED;
                    mMainView.setActionButtonText(getString(R.string.button_text_quick_clean));
                }

                int recommend = 0;
                if (mModelMap.get(HandleHeaderType.Manual) != null) {
                    recommend = mModelMap.get(HandleHeaderType.Manual).size();
                }

                // 显示体检结果
                if (mSystemCheckManager.hasVirus()) {
                    mMainView.setCheckingText(getString(R.string.examination_score_has_virus));
                    mMainView.setStatusText(getString(R.string.examination_score_has_virus));
                } else if (score == 100) {
                    if (recommend > 0) {
                        mMainView.setCheckingText(getString(
                                R.string.examination_score_100_with_recommend, recommend));
                        mMainView.setStatusText(getString(
                                R.string.examination_score_100_with_recommend, recommend));
                    }
                    else {
                        mMainView.setCheckingText(getString(R.string.examination_score_100));
                        mMainView.setStatusText(getString(R.string.examination_score_100));
                    }
                } else if (score >= 80) {
                    if (recommend > 0) {
                        mMainView.setCheckingText(getString(
                                R.string.examination_score_80_100_with_recommend, recommend));
                        mMainView.setStatusText(getString(
                                R.string.examination_score_80_100_with_recommend, recommend));
                    }
                    else {
                        mMainView
                                .setCheckingText(getString(R.string.examination_score_80_100));
                        mMainView
                                .setStatusText(getString(R.string.examination_score_80_100));
                    }
                } else if (score >= 60) {
                    mMainView.setCheckingText(getString(R.string.examination_score_80_60));
                    mMainView.setStatusText(getString(R.string.examination_score_80_60));
                } else {
                    mMainView.setCheckingText(getString(R.string.examination_score_60_0));
                    mMainView.setStatusText(getString(R.string.examination_score_60_0));
                }
                AnalyticsUtil.trackMainCheckScore(MainActivity.this, score);
            }
        }

        private void onScanItem(OnScanItemEvent event) {
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_SCORE,
                    RefreshScoreEvent.create());
            mMainView.setScanningText(event.getScanText());
            mMainView.setScanningNumber(getString(R.string.scanning_bar_number, event.getCount()
                    * 100 / event.getTotalCount()));
        }

        private void onStartScan(OnStartScanEvent event) {

        }

        private void onMenuItemClick(OnMenuItemClickEvent event) {
            switch (event.getMenuItem()) {
                case GARBAGE_CLEANUP:
                    startActivity(new Intent(MiuiIntent.ACTION_GARBAGE_CLEANUP));
                    AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                            AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_LJ);
                    break;
                case NETWORK_ASSISTANTS:
                    startActivity(new Intent(MiuiIntent.ACTION_NETWORK_ASSISTANT));
                    AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                            AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_WL);
                    break;
                case ANTISPAM:
                    startActivity(new Intent(MiuiIntent.ACTION_SET_FIREWALL));
                    AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                            AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_SR);
                    break;
                case POWER_MANAGER:
                    startActivity(new Intent(MiuiIntent.ACTION_POWER_MANAGER));
                    AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                            AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_SD);
                    break;
                case ANTIVIRUS:
                    startActivity(new Intent(MiuiIntent.ACTION_ANTIVIRUS));
                    AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                            AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_BD);
                    break;
                case LICENSE_MANAGER:
                    startActivity(new Intent(MiuiIntent.ACTION_LICENSE_MANAGER));
                    AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                            AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_SQ);
                    break;

                default:
                    break;
            }
        }

        private void onHandleItemClick(OnHandleItemClickEvent event) {
            switch (event.getHandleItem()) {
                case SYSTEM:
                    startActivity(new Intent(MainActivity.this, SystemActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CENTER_ENTER_SYSTEM);
                    break;
                case MEMORY:
                    startActivity(new Intent(MainActivity.this, MemoryActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CENTER_ENTER_MEMORY);
                    break;
                case CACHE:
                    startActivity(new Intent(MainActivity.this, CacheActivity.class));
                    AnalyticsUtil.track(MainActivity.this,
                            AnalyticsUtil.TRACK_ID_CENTER_ENTER_CACHE);
                    break;
                default:
                    break;
            }
        }

        private void startQuickScan(StartQuickScanEvent event) {
            Preferences.setLastCheckCanceled(false);
            mMainView.resetListAdapter();
            mEventHandler.removeCallbacks(mScanCacheRunnable);
            mEventHandler.removeCallbacks(mScanSystemRunnable);
            mEventHandler.removeCallbacks(mScanMemoryRunnable);
            mCurrentStatus = CleanupButtonStatus.SCANNING;
            mMainView.setScanningHeaderVisibility(View.INVISIBLE);
            mMainView.setScanningNumber(getString(R.string.scanning_bar_number, 0));
            mSystemCheckManager.resetScore();
            mCacheCheckManager.resetScore();
            try {
                mMemoryCheck.resetScore();
            } catch (Exception e) {
                // ignore
            }
            mMainView.startCheckingAnimation();
            mMainView.setScore(100, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    mForceStopped = false;
                    mEventHandler.postDelayed(mScanMemoryRunnable, 0);

                };
            });

            AnalyticsUtil.trackMainMenuClick(MainActivity.this,
                    AnalyticsUtil.TRACK_VALUE_MAIN_MENU_CLICK_TJ);
        }
    };

    private Runnable mScanSystemRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Animation anim = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.show_from_right_to_left);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    mMainView.resetListAdapter();
                    mMainView.setScanningTitle(getString(R.string.scanning_bar_system_title));
                    mMainView.setScanningNumber(getString(R.string.scanning_bar_number, 0));
                    mMainView.setScanningHeaderVisibility(View.VISIBLE);
                    new SystemScanTask().execute();
                }
            });
            mMainView.setScanningBarAnimation(anim);
        }
    };

    private Runnable mScanCacheRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Animation anim = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.show_from_right_to_left);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    mMainView.resetListAdapter();
                    mMainView.setScanningTitle(getString(R.string.scanning_bar_cache_title));
                    mMainView.setScanningNumber(getString(R.string.scanning_bar_number, 0));
                    mMainView.setScanningHeaderVisibility(View.VISIBLE);
                    new CacheScanTask().execute();
                }
            });
            mMainView.setScanningBarAnimation(anim);
        }
    };

    private Runnable mScanMemoryRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Animation anim = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.show_from_right_to_left);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    mMainView.resetListAdapter();
                    mMainView.setScanningTitle(getString(R.string.scanning_bar_memory_title));
                    mMainView.setScanningNumber(getString(R.string.scanning_bar_number, 0));
                    mMainView.setScanningHeaderVisibility(View.VISIBLE);
                    new MemoryScanTask().execute();
                }
            });
            mMainView.setScanningBarAnimation(anim);
        }
    };

    private class ManualScanTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            mModelMap.remove(HandleHeaderType.Manual);
            mWhiteListManager.loadWhiteList();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            mManualCheckManager.startScanManulItem(mManualItemScanCallback);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            mMainView.updateAdapterData(mModelMap);
        }
    }

    private class MemoryScanTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mMainView.setCheckingText(getString(R.string.hints_scanning_text));
            mMainView.resetListAdapter();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mMemoryCheck.startScan(mMemoryScanCallback);
            } catch (RemoteException e) {
                // ignore
            }
            return null;
        }
    }

    private class CacheScanTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mMainView.setCheckingText(getString(R.string.hints_scanning_text));
            mMainView.resetListAdapter();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCacheCheckManager.startScan(mCacheScanCallback);
            return null;
        }
    }

    private class SystemScanTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mMainView.setCheckingText(getString(R.string.hints_scanning_text));
            mMainView.resetListAdapter();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mSystemCheckManager.startScan(mSystemScanCallback);
            return null;
        }
    }

    private class CleanupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mSystemCheckManager.startCleanup(mSystemCleanupCallback);
            mCacheCheckManager.startCleanup(mCacheCleanupCallback);
            try {
                mMemoryCheck.startCleanup(mMemoryCleanupCallback);
            } catch (RemoteException e) {
                // ignore
            }
            return null;
        }
    }

    private Runnable mFlashAnimationRunnable = new Runnable() {

        @Override
        public void run() {
            mMainView.startFlashAnimation();
        }
    };

    private OnServiceConnectedListener mOnServiceConnectedListener = new OnServiceConnectedListener() {

        @Override
        public void onServiceConnected(IMemoryCheck memoryCheck) {
            mMemoryCheck = memoryCheck;
        }
    };

    private boolean mIsManualItemScanned = false;
    private boolean mIsSystemScanned = false;
    private boolean mIsMemoryScanned = false;
    private boolean mIsCacheScanned = false;

    private SystemCheckManager mSystemCheckManager;
    private IMemoryCheck mMemoryCheck;
    private CacheCheckManager mCacheCheckManager;
    private ItemListManager mItemListManager;

    private MainActivityView mMainView;

    private boolean mForceStopped = false;

    private CleanupButtonStatus mCurrentStatus = CleanupButtonStatus.NORMAL;

    private Map<HandleHeaderType, List<HandleItemModel>> mModelMap = new HashMap<HandleHeaderType, List<HandleItemModel>>();
    private WhiteListManager mWhiteListManager;
    private ManualItemCheckManager mManualCheckManager;

    private HandleItemComparator mHandleItemComparator = new HandleItemComparator();

    private final static int INITIAL_LISTVIEW_SELECTION = 0;
    private final static int LATTER_LISTVIEW_SELECTION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m_activity_main);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);
        // 初始化V5升V6的数据迁移
        RestoreHelper.restoreData();

        mMemoryCheck = MemoryCheckManager.getInstance(this).getMemoryCheck();
        if (mMemoryCheck == null) {
            MemoryCheckManager.getInstance(this)
                    .bindMemoryCheckService(mOnServiceConnectedListener);
        }
        mSystemCheckManager = SystemCheckManager.getInstance(this);
        mCacheCheckManager = CacheCheckManager.getInstance(this);
        mWhiteListManager = WhiteListManager.getInstance(this);
        mManualCheckManager = ManualItemCheckManager.getInstance(this);
        mItemListManager = ItemListManager.getInstance(this);

        mMainView = (MainActivityView) findViewById(R.id.main_view);
        mMainView.setEventHandler(mEventHandler);
        mMainView.setHandleListAdapter(this, mEventHandler);

        refreshStatusInfo();

        mEventHandler.postDelayed(mFlashAnimationRunnable, 300);

        if (mItemListManager.getItemList().isEmpty()) {
            mItemListManager.initialDataBaseIfNoData();
        }

        if (android.provider.MiuiSettings.Secure.isCtaSupported(getContentResolver())
                && !Preferences.isCtaCheckboxChecked()) {
            startActivity(new Intent(this, CtaDialogActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentStatus != CleanupButtonStatus.NORMAL) {
            new ManualScanTask().execute();
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_HANDLE_AUTO_ITEMS,
                    RefreshHandleAutoItemEvent.create());
            mEventHandler.sendEventMessage(EventType.EVENT_REFRESH_SCORE,
                    RefreshScoreEvent.create());
        }

        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_SECURITY_CENTER_ENTER);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentStatus == CleanupButtonStatus.SCANNING) {
            mMainView.resetListAdapter();
            mEventHandler.removeCallbacks(mScanCacheRunnable);
            mEventHandler.removeCallbacks(mScanSystemRunnable);
            mEventHandler.removeCallbacks(mScanMemoryRunnable);
            mForceStopped = true;
            mCurrentStatus = CleanupButtonStatus.NORMAL;
            mMainView.stopScanningAnimation();
        }
        else if (mCurrentStatus != CleanupButtonStatus.NORMAL) {
            mForceStopped = true;
            mCurrentStatus = CleanupButtonStatus.NORMAL;
            mMainView.stopCheckingAnimation();

            AnalyticsUtil.track(MainActivity.this, AnalyticsUtil.TRACK_ID_CENTER_CHECK_CANCEL);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApkIconHelper.getInstance(this).clearCacheLaunchers();
    }

    private void refreshStatusInfo() {
        long defValue = 0;
        long targetTime = Preferences.getLatestOptimizeDate(defValue);
        if (targetTime == defValue || targetTime > System.currentTimeMillis()) {
            mMainView.setStatusText(getString(R.string.last_check_never_checked));
            return;
        }
        if (Preferences.isLastCheckCanceled()) {
            mMainView.setStatusText(getString(R.string.last_check_canceled));
        } else {
            mMainView.setStatusText(DateTimeUtils.formatCheckedTime(this, targetTime));
        }
    }

    private int getScore() {
        try {
            int systemScore = mSystemCheckManager.getScore();
            int memoryScore = mMemoryCheck.getScore();
            int cacheScore = mCacheCheckManager.getScore();

            int score = systemScore + memoryScore + cacheScore + ScoreConstants.BASE_SCORE;
            if (mSystemCheckManager.hasVirus() && score >= 60) {
                score = 59;
            }

            return score;
        } catch (RemoteException e) {
            // ignore
        }
        return 0;
    }

    private void updateManualModelMap(HandleItemModel model) {
        List<HandleItemModel> items = mModelMap.get(HandleHeaderType.Manual);
        if (items == null) {
            items = new ArrayList<HandleItemModel>();
            mModelMap.put(HandleHeaderType.Manual, items);
        }
        model.setType(HandleHeaderType.Manual);
        items.add(model);
        if (items.size() >= 2) {
            Collections.sort(items, mHandleItemComparator);
        }
    }

    private void updateAutoModelMap(HandleItem item, CharSequence title, CharSequence summary,
            int weight) {
        HandleItemModel model = new HandleItemModel();
        model.setType(HandleHeaderType.Auto);
        model.setItem(item);
        model.setTitle(title);
        model.setSummary(summary);
        model.setWeight(weight);
        List<HandleItemModel> items = mModelMap.get(HandleHeaderType.Auto);
        if (items == null) {
            items = new ArrayList<HandleItemModel>();
            mModelMap.put(HandleHeaderType.Auto, items);
        }
        items.add(model);
        if (items.size() >= 2) {
            Collections.sort(items, mHandleItemComparator);
        }
    }

    private class HandleItemComparator implements Comparator<HandleItemModel> {

        @Override
        public int compare(HandleItemModel lhs, HandleItemModel rhs) {
            // TODO Auto-generated method stub
            if (lhs.getWeight() < rhs.getWeight()) {
                return -1;
            }
            else if (lhs.getWeight() > rhs.getWeight()) {
                return 1;
            }
            return 0;
        }
    }
}
