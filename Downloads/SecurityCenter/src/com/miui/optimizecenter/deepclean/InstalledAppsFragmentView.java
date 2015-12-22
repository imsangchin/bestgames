
package com.miui.optimizecenter.deepclean;

import miui.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView;
import com.miui.optimizecenter.event.BackupUninstallAppsEvent;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.UninstallAppsEvent;

import com.miui.securitycenter.R;

public class InstalledAppsFragmentView extends LinearLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private TextView mHeaderLeftView;
    private TextView mHeaderRightView;

    private ExpandableListView mApkListView;

    private ProgressDialog mLoadingDialog;

    public InstalledAppsFragmentView(Context context) {
        this(context, null);
    }

    public InstalledAppsFragmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderLeftView = (TextView) findViewById(R.id.left_text);
        mHeaderRightView = (TextView) findViewById(R.id.right_text);

        mApkListView = (ExpandableListView) findViewById(R.id.installed_apps_list);

        findViewById(R.id.backup_uninstall).setOnClickListener(this);
        findViewById(R.id.uninstall).setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setInstalledAppsListAdapter(InstalledAppsListAdapter adapter) {
        mApkListView.setAdapter(adapter);
    }

    public void collapseAllItems(boolean anim) {
        mApkListView.collapseAllItem(anim);
    }

    public void performItemClick(View view, int position, int id) {
        mApkListView.performItemClick(view, position, id);
    }

    public void setHeaderLeftTitle(CharSequence text) {
        mHeaderLeftView.setText(text);
    }

    public void setHeaderRightTitle(CharSequence text) {
        mHeaderRightView.setText(text);
    }

    public void setHeaderBarShown(boolean shown) {
        findViewById(R.id.header_bar).setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
    }

    public void setUninstallButtonEnabled(boolean enabled) {
        findViewById(R.id.backup_uninstall).setEnabled(enabled);
        findViewById(R.id.uninstall).setEnabled(enabled);
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
        if (!shown && mApkListView.getEmptyView() == null) {
            mApkListView.setEmptyView(findViewById(R.id.empty_view));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backup_uninstall) {
            mEventHandler.sendEventMessage(EventType.EVENT_BACKUP_UNINSTALL_APPS,
                    BackupUninstallAppsEvent.create());
        } else if (v.getId() == R.id.uninstall) {
            mEventHandler.sendEventMessage(EventType.EVENT_UNINSTALL_APPS,
                    UninstallAppsEvent.create());
        }
    }
}
