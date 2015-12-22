
package com.miui.powercenter.view;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerData.PowerMode;
import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PowerUsageDetailsItemPreference extends Preference {

    private CharSequence mTitle;
    private CharSequence mContent;

    public PowerUsageDetailsItemPreference(Context context) {
        this(context, null);
    }

    public PowerUsageDetailsItemPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowerUsageDetailsItemPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTitle = getTitle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return View.inflate(getContext(), R.layout.pc_power_usage_details_item_pref, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView mTitleView = (TextView) view.findViewById(R.id.title);
        TextView mContentView = (TextView) view.findViewById(R.id.content);

        if (TextUtils.isEmpty(mTitle)) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(mContent)) {
            mContentView.setVisibility(View.GONE);
        } else {
            mContentView.setVisibility(View.VISIBLE);
        }

        mTitleView.setText(mTitle);
        mContentView.setText(mContent);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (title == null && mTitle != null || title != null && !title.equals(mTitle)) {
            mTitle = title;
            notifyChanged();
        }
    }

    @Override
    public void setTitle(int titleResId) {
        setTitle(getContext().getResources().getString(titleResId));
    }

    public void setContent(CharSequence content) {
        if (content == null && mContent != null || content != null && !content.equals(mContent)) {
            mContent = content;
            notifyChanged();
        }
    }

    public void setContent(int contentResId) {
        setContent(getContext().getResources().getString(contentResId));
    }
}
