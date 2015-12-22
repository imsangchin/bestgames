
package com.miui.optimizecenter.whitelist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;

import com.miui.securitycenter.R;

public class WhiteListItemView extends LinearLayout implements OnClickListener,
        OnCheckedChangeListener {

    private EventHandler mEventHandler;

    private TextView mTitleView;
    private CheckBox mCheckBoxView;
    private ImageView mIconView;

    private WhiteListItemModel mData;

    public WhiteListItemView(Context context) {
        this(context, null);
    }

    public WhiteListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mCheckBoxView = (CheckBox) findViewById(R.id.checkbox);
        mCheckBoxView.setOnCheckedChangeListener(this);

        setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(WhiteListItemModel data) {
        mData = data;

        WhiteListType type = data.getWhiteListType();
        ApkIconHelper iconHelper = ApkIconHelper.getInstance(getContext());
        switch (type) {
            case AD:
                iconHelper.loadFolderIcon(mIconView);
                break;
            case APK:
                iconHelper.loadFileIcon(mIconView, data.getDirPath());
                break;
            case CACHE:
                iconHelper.loadInstalledAppLauncher(mIconView, data.getPkgName());
                break;
            case LARGE_FILE:
                iconHelper.loadFileIcon(mIconView, data.getDirPath());
                break;
            case RESIDUAL:
                iconHelper.loadFolderIcon(mIconView);
                break;
            default:
                break;
        }

        mTitleView.setText(data.getTitle());

        mCheckBoxView.setChecked(data.isChecked());
    }

    @Override
    public void onClick(View v) {
        mCheckBoxView.setChecked(!mCheckBoxView.isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mData.setChecked(isChecked);
    }
}
