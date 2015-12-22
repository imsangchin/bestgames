
package com.miui.permcenter.permissions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.miui.securitycenter.R;

import miui.app.ProgressDialog;

public class PermissionAppsEditorView extends RelativeLayout {

    private ListView mAppListView;

    private ProgressDialog mLoadingDialog;

    public PermissionAppsEditorView(Context context) {
        this(context, null);
    }

    public PermissionAppsEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PermissionAppsEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppListView = (ListView) findViewById(R.id.app_list);
    }

    public void setAppsEditorListAdapter(PermissionAppsEditorListAdapter adapter) {
        mAppListView.setAdapter(adapter);
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

        if (!shown && mAppListView.getEmptyView() == null) {
            mAppListView.setEmptyView(findViewById(R.id.empty_view));
        }
    }
}
