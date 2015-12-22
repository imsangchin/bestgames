
package com.miui.optimizecenter.uninstallmonitor;

import miui.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.miui.common.AndroidUtils;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.securitycenter.AidlProxyHelper;

import java.util.List;

import miui.text.ExtraTextUtils;
import com.miui.securitycenter.R;

public class PackageActivity extends Activity implements OnClickListener {

    public static final String EXTRA_PKG_NAME = "extra_pkg_name";
    public static final String EXTRA_RESIDUAL_SIZE = "extra_residual_size";
    public static final String EXTRA_RESIDUAL_PATHS = "extra_residual_paths";
    public static final String EXTRA_VIEW_PATH = "extra_view_path";

    private static final String TAG = PackageActivity.class.getSimpleName();

    private ServiceConnection mFileConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIFileProxy = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIFileProxy = IFileProxy.Stub.asInterface(service);
        }
    };

    protected PackageActivityView mPackageView;

    private String mPkgName;
    private long mResidualSize;
    private List<String> mResidualPaths;
    private String mViewPath;

    private PackageModel mPackageModel;

    private IFileProxy mIFileProxy;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.op_activity_package);

        AidlProxyHelper.getInstance().bindFileProxy(this, mFileConnection);

        mPackageView = (PackageActivityView) findViewById(R.id.package_view);
        mPackageView.setOnClickListener(this);

        Intent intent = getIntent();
        mPkgName = intent.getStringExtra(EXTRA_PKG_NAME);
        mResidualSize = Math.max(intent.getLongExtra(EXTRA_RESIDUAL_SIZE, 0), 1);
        mResidualPaths = intent.getStringArrayListExtra(EXTRA_RESIDUAL_PATHS);
        mViewPath = intent.getStringExtra(EXTRA_VIEW_PATH);

        Log.d(TAG, "pkg name = " + mPkgName);
        Log.d(TAG, "residual size = " + mResidualSize);
        Log.d(TAG, "residual paths = " + mResidualPaths);
        Log.d(TAG, "View path = " + mViewPath);

        if (TextUtils.isEmpty(mPkgName)) {
            finish();
            return;
        }

        mPackageModel = PackagesManager.getInstance(this).getPackageByName(mPkgName);
        if (mPackageModel == null) {
            finish();
            return;
        }
        mPackageView.setPackageModel(mPackageModel);

        String formatSize = ExtraTextUtils.formatFileSize(this, mResidualSize);
        String content = getString(R.string.hints_residual_tips, formatSize);
        int color = getResources().getColor(R.color.high_light_green);
        mPackageView.setContent(AndroidUtils.getHighLightString(content, color, formatSize));
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.cancel_button:
                    PackagesManager.getInstance(PackageActivity.this).deletePackageByName(mPkgName);
                    finish();
                    break;
                case R.id.clean_button:
                    new CleanTask(mResidualPaths).execute();
                    PackagesManager.getInstance(PackageActivity.this).deletePackageByName(mPkgName);
                    finish();
                    break;
                case R.id.content_frame:
                    if (!TextUtils.isEmpty(mViewPath)) {
                        ProxyFileInfo info = mIFileProxy.getFileInfo(mViewPath);
                        if (info.exists()) {
                            Intent intent = new Intent();
                            intent.setClassName("com.android.fileexplorer",
                                    "com.android.fileexplorer.FileExplorerTabActivity");
                            intent.setData(Uri.parse(info.getFileUri()));
                            startActivity(intent);
                        }
                    }
                    PackagesManager.getInstance(PackageActivity.this).deletePackageByName(mPkgName);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CleanTask extends AsyncTask<Void, Void, Void> {

        private List<String> mTargetPaths;

        public CleanTask(List<String> paths) {
            mTargetPaths = paths;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (mTargetPaths != null) {
                    for (String path : mTargetPaths) {
                        mIFileProxy.deleteFileByPath(path);
                    }
                }
            } catch (Exception e) {
                // ignore
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        AidlProxyHelper.getInstance().unbindProxy(this, mFileConnection);
        super.onDestroy();
    }
}
