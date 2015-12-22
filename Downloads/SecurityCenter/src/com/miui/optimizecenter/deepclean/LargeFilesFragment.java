
package com.miui.optimizecenter.deepclean;

import miui.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.view.Menu;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.MediaScannerUtil;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.IFileScanCallback;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.CleanDataManager;
import com.miui.optimizecenter.Preferences;
import com.miui.optimizecenter.ad.AdModel;
import com.miui.optimizecenter.enums.InstalledAppsSortType;
import com.miui.optimizecenter.enums.LargeFileSortType;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanLargeFileEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyButtonEnabledEvent;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.NotifyLoadingShownEvent;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.ViewApkDetailsEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.event.ViewLargeFileDetailsEvent;
import com.miui.optimizecenter.tools.FileHelper;
import com.miui.optimizecenter.tools.ScanFilesThread;
import com.miui.optimizecenter.whitelist.WhiteListManager;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.ExtraIntent;
import com.cleanmaster.sdk.IKSCleaner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.app.ActionBar;
import miui.text.ExtraTextUtils;

import com.miui.securitycenter.R;

public class LargeFilesFragment extends BaseDeepCleanFragment {

    public static final String TAG = LargeFilesFragment.class.getSimpleName();

    private IFileScanCallback.Stub mFileCallStub = new IFileScanCallback.Stub() {

        @Override
        public void onScanStart() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(true));
            mDataManager.clearLargeFilesMap();
        }

        // Stop scanning when return true
        @Override
        public boolean onFindFile(ProxyFileInfo info) {
            try {
                String dirPath = info.getAbsolutePath();
                if (!mWhiteListManager.inLargeFileWhiteList(dirPath)) {
                    LargeFileModel largeFile = new LargeFileModel();
                    largeFile.setFileSize(mIKSCleaner.pathCalcSize(dirPath));
                    largeFile.setName(info.getName());
                    largeFile.setPath(dirPath);
                    largeFile.setAdviseDelete(false);
                    mDataManager.addLargeFileModel(largeFile);

                    mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                            NotifyListUpdateEvent.create(false));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            return mForceStopped;
        }

        @Override
        public void onScanFinish() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        public void onError() {
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_BUTTON_ENABLED,
                    NotifyButtonEnabledEvent.create(true));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LOADING_SHOWN,
                    NotifyLoadingShownEvent.create(false));
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }
    };

    private class ClearLargeFilesTask extends AsyncTask<Void, Void, Void> {
        private List<String> mPathList = new ArrayList<String>();

        public ClearLargeFilesTask(List<String> paths) {
            mPathList.clear();
            mPathList.addAll(paths);
        }

        @Override
        protected void onPreExecute() {
            for (String path : mPathList) {
                mDataManager.removeLargeFileModel(path);
            }

            mLargeFilesView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String path : mPathList) {
                AidlProxyHelper.getInstance().deleteDirectory(path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.toast_garbage_cleanup_success,
                        Toast.LENGTH_SHORT).show();
                MediaScannerUtil.scanWholeExternalStorage(getActivity());
            }
        }
    }

    private EventHandler mEventHandler = new EventHandler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EventType.EVENT_NOTIFY_BUTTON_ENABLED:
                    notifyButtonEnabled((NotifyButtonEnabledEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LOADING_SHOWN:
                    notifyLoadingShown((NotifyLoadingShownEvent) msg.obj);
                    break;
                case EventType.EVENT_NOTIFY_LIST_UPDATE:
                    notifyListUpdate((NotifyListUpdateEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEANUP_LIST_ITEMS:
                    cleanupListItems((CleanupListItemsEvent) msg.obj);
                    break;
                case EventType.EVENT_ADD_TO_WHITE_LIST:
                    addToWhiteList((AddToWhiteListEvent) msg.obj);
                    break;
                case EventType.EVENT_VIEW_FILE:
                    viewFile((ViewFileEvent) msg.obj);
                    break;
                case EventType.EVENT_CLEAN_LARGE_FILE_ITEM:
                    cleanLargeFileItem((CleanLargeFileEvent) msg.obj);
                    break;
                case EventType.EVENT_PERFORM_ITEM_CLICK:
                    performItemClick((PerformItemClickEvent) msg.obj);
                    break;
                default:
                    break;
            }
        };

        public void performItemClick(PerformItemClickEvent event) {
            mLargeFilesView.performItemClick(event.getView(), event.getPosition(),
                    event.getPosition());
        }

        private void cleanLargeFileItem(CleanLargeFileEvent event) {
            List<String> pathList = new ArrayList<String>();
            pathList.add(event.getData().getPath());
            new ClearLargeFilesTask(pathList).execute();
        }

        private void viewFile(ViewFileEvent event) {
            try {
                String path = null;
                ProxyFileInfo info = mIFileProxy.getFileInfo(event.getPath());
                if (info.isDirectory()) {
                    path = info.getAbsolutePath();
                } else {
                    path = info.getParent();
                }
                FileHelper.openFile(getActivity(), path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addToWhiteList(AddToWhiteListEvent event) {
            LargeFileModel largeFile = (LargeFileModel) event.getData();
            mWhiteListManager.insertLargeFileToWhiteList(largeFile.getPath());
            mDataManager.removeLargeFileModel(largeFile.getPath());
            mLargeFilesView.collapseAllItems(false);
            mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                    NotifyListUpdateEvent.create(true));

            Toast.makeText(getActivity(), R.string.toast_add_white_list_success,
                    Toast.LENGTH_SHORT).show();
        }

        private void cleanupListItems(CleanupListItemsEvent event) {
            List<String> pathList = new ArrayList<String>();

            Map<String, LargeFileModel> largeFilesMap = new HashMap<String, LargeFileModel>(
                    mDataManager.getLargeFilesMap());
            Set<String> keys = largeFilesMap.keySet();
            for (String key : keys) {
                LargeFileModel largeFile = largeFilesMap.get(key);
                if (largeFile.adviseDelete()) {
                    pathList.add(largeFile.getPath());
                }
            }
            new ClearLargeFilesTask(pathList).execute();
        }

        private void notifyListUpdate(NotifyListUpdateEvent event) {
            try {
                List<LargeFileModel> largeFilesList = new ArrayList<LargeFileModel>();
                Map<String, LargeFileModel> largeFilesMap = new HashMap<String, LargeFileModel>(
                        mDataManager.getLargeFilesMap());
                Set<String> keys = largeFilesMap.keySet();
                for (String key : keys) {
                    LargeFileModel largeFile = largeFilesMap.get(key);
                    largeFilesList.add(largeFile);
                }

                if (event.isNeedSort() && largeFilesList.size() >= 2) {
                    Collections.sort(largeFilesList, mDefaultComparator);
                }

                mLargeFilesAdapter.updateData(largeFilesList);
                mLargeFilesAdapter.notifyDataSetChanged();
                mLargeFilesView.setCleanupButtonEnabled(!largeFilesList.isEmpty());

                String leftText = getString(R.string.hints_large_file_header_left);
                long totalSize = 0;
                for (LargeFileModel model : largeFilesList) {
                    totalSize += model.getFileSize();
                }

                int color = getResources().getColor(R.color.high_light_green);
                String leftContent = String.format(leftText, largeFilesList.size());
                String rightContent = ExtraTextUtils.formatFileSize(getActivity(), totalSize);
                mLargeFilesView.setHeaderLeftTitle(AndroidUtils.getHighLightString(leftContent,
                        color, String.valueOf(largeFilesList.size())));
                mLargeFilesView.setHeaderRightTitle(AndroidUtils.getHighLightString(
                        rightContent, color, rightContent));
                mLargeFilesView.setHeaderBarShown(!largeFilesList.isEmpty());
            } catch (Exception e) {
                // ignore
            }
        }

        private void notifyLoadingShown(NotifyLoadingShownEvent event) {
//            mLargeFilesView.setLoadingShown(event.isShown());
        }

        private void notifyButtonEnabled(NotifyButtonEnabledEvent event) {
            mLargeFilesView.setCleanupButtonEnabled(event.isEnabled());
        }
    };

    private LargeFileComparator mDefaultComparator = new LargeFileComparator();
    private LargeFileComparator SIZE_COMPARATOR = new LargeFileComparator(LargeFileSortType.SIZE);
    private LargeFileComparator NAME_COMPARATOR = new LargeFileComparator(LargeFileSortType.NAME);

    private boolean mForceStopped = false;

    private IFileProxy mIFileProxy;
    private IKSCleaner mIKSCleaner;

    private LargeFilesFragmentView mLargeFilesView;
    private LargeFilesListAdapter mLargeFilesAdapter;

    private WhiteListManager mWhiteListManager;
    private CleanDataManager mDataManager;

    private boolean mIsActivityCreated = false;
    private boolean mIsServiceBinded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForceStopped = false;

        if (Preferences.getLargeFileSortType() == LargeFileSortType.SIZE) {
            mDefaultComparator = SIZE_COMPARATOR;
        } else {
            mDefaultComparator = NAME_COMPARATOR;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.op_fragment_large_files, null);
        mLargeFilesView = (LargeFilesFragmentView) view;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWhiteListManager = WhiteListManager.getInstance(getActivity());
        mDataManager = CleanDataManager.getInstance(getActivity());

        mLargeFilesView.setEventHandler(mEventHandler);

        mLargeFilesAdapter = new LargeFilesListAdapter(mEventHandler);
        mLargeFilesView.setLargeFilesListAdapter(mLargeFilesAdapter);

        mIsActivityCreated = true;
        checkSatus();
    }

    @Override
    public void onServiceBinded(Context context, IKSCleaner cleaner, IFileProxy fileProxy) {
        mIFileProxy = fileProxy;
        mIKSCleaner = cleaner;

        mIsServiceBinded = true;
        checkSatus();
    };

    private void checkSatus() {
        new ScanFilesThread(mIFileProxy, mFileCallStub, ScanFilesThread.LargeFile.All).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mForceStopped = true;
    }

    @Override
    public void onFragmentSortTypeSelected(Context context, int sortTypeId) {
        if (sortTypeId == 1) {
            mDefaultComparator = SIZE_COMPARATOR;
            Preferences.setLargeFileSortType(LargeFileSortType.SIZE);
        } else if (sortTypeId == 2) {
            mDefaultComparator = NAME_COMPARATOR;
            Preferences.setLargeFileSortType(LargeFileSortType.NAME);
        }
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
    }
}
