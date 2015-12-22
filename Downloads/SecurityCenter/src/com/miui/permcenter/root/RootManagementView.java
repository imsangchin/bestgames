
package com.miui.permcenter.root;

import miui.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.miui.securitycenter.R;
import com.miui.common.PinnedHeaderListView;

public class RootManagementView extends RelativeLayout {

    private PinnedHeaderListView mAutoStartList;

    private ProgressDialog mLoadingDialog;

    public RootManagementView(Context context) {
        this(context, null);
    }

    public RootManagementView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RootManagementView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAutoStartList = (PinnedHeaderListView) findViewById(R.id.root_list);
    }

    public void setRootListAdapter(RootListAdapter adapter) {
        mAutoStartList.setAdapter(adapter);
    }

    public void setLoadingShown(boolean shown) {
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

        if (!shown && mAutoStartList.getEmptyView() == null) {
            mAutoStartList.setEmptyView(findViewById(R.id.empty_view));
        }
    }
}
