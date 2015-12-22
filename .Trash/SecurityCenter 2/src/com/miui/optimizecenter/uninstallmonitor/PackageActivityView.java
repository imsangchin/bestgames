
package com.miui.optimizecenter.uninstallmonitor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.securitycenter.R;

public class PackageActivityView extends RelativeLayout {
    private ImageView mAppLauncherView;
    private TextView mAppNameView;
    private Button mCancelButton;
    private Button mCleanButton;
    private TextView mContentView;
    private View mContentFrame;

    public PackageActivityView(Context context) {
        this(context, null);
    }

    public PackageActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PackageActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppLauncherView = (ImageView) findViewById(R.id.app_launcher);
        mAppNameView = (TextView) findViewById(R.id.app_name);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCleanButton = (Button) findViewById(R.id.clean_button);
        mContentView = (TextView) findViewById(R.id.content);
        mContentFrame = findViewById(R.id.content_frame);
    }

    public void setPackageModel(PackageModel model) {
        mAppLauncherView.setImageDrawable(model.getLauncher());
        mAppNameView.setText(model.getApplicationLabel());
    }

    public void setContent(CharSequence content) {
        mContentView.setText(content);
    }

    public void setOnClickListener(OnClickListener listener) {
        mCancelButton.setOnClickListener(listener);
        mCleanButton.setOnClickListener(listener);
        mContentFrame.setOnClickListener(listener);
    }
}
