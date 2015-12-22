
package com.miui.permcenter.permissions;

import miui.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.miui.securitycenter.R;

public class AppsFragmentView extends RelativeLayout {

    private ProgressDialog mLoadingDialog;

    private ListView mAppsListView;

    public AppsFragmentView(Context context) {
        this(context, null);
    }

    public AppsFragmentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppsFragmentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppsListView = (ListView) findViewById(R.id.apps_list);
    }

    public void setAppsListAdapter(AppsListAdapter adapter) {
        mAppsListView.setAdapter(adapter);
    }

    public void setLoadingViewShown(boolean shown) {
        if (shown) {
            if (mLoadingDialog == null) {
                mLoadingDialog = ProgressDialog.show(getContext(), null,
                        getResources().getString(R.string.hints_loading_text), true, false);
            }
        } else {
            if (mLoadingDialog != null) {
                mLoadingDialog.cancel();
                mLoadingDialog = null;
            }
        }

        if (!shown && mAppsListView.getEmptyView() == null) {
            mAppsListView.setEmptyView(findViewById(R.id.empty_view));
        }
    }
}
