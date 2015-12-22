
package com.miui.antivirus;

import miui.text.ExtraTextUtils;
import miui.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.miui.securitycenter.R;
import com.miui.antivirus.VirusCheckManager.ScanItemType;
import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;

import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.securitycenter.AidlProxyHelper;

public class VirusDetailActivity extends Activity implements OnClickListener {

    public static final String EXTRA_DATA = "extra_data";

    private IFileProxy mIFileProxy;
    private VirusModel mData;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSummaryView;
    private TextView mVirusNameView;
    private TextView mVirusDescxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.v_activity_virus_detail);

        mData = (VirusModel) getIntent().getSerializableExtra(EXTRA_DATA);
        if (mData == null) {
            finish();
            return;
        }
        mIFileProxy = AidlProxyHelper.getInstance().getIFileProxy();

        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);
        mVirusNameView = (TextView) findViewById(R.id.virus_name);
        mVirusDescxView = (TextView) findViewById(R.id.virus_descx);

        ProxyFileInfo fileInfo = null;
        try {
            fileInfo = mIFileProxy.getFileInfo(mData.getSourceDir());
        } catch (RemoteException e) {
            // ignore
        }

        if (mData.getScanItemType() == ScanItemType.INSTALLED_APP) {
            ApkIconHelper.getInstance(this).loadInstalledAppLauncher(mIconView, mData.getPkgName());
            if (fileInfo != null) {
                mSummaryView.setText(getString(R.string.hints_virus_app_summary,
                        ExtraTextUtils.formatFileSize(this, fileInfo.getSize())));
            }
        } else {
            ApkIconHelper.getInstance(this).loadFileIcon(mIconView, mData.getSourceDir());
            if (fileInfo != null) {
                mSummaryView.setText(getString(R.string.hints_virus_apk_summary,
                        ExtraTextUtils.formatFileSize(this, fileInfo.getSize())));
            }
        }

        mTitleView.setText(mData.getAppLabel());
        mVirusNameView.setText(mData.getVirusName());
        mVirusDescxView.setText(mData.getVirusDescx());

        findViewById(R.id.clear).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.clear) {
            VirusCheckManager.getInstance(getApplicationContext()).removeVirus(mData);
            new ClearTask().execute();
            finish();
        }
    }

    private class ClearTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            VirusCheckManager.getInstance(getApplicationContext()).cleanupVirus(mIFileProxy, mData);
            return null;
        }

    }
}
