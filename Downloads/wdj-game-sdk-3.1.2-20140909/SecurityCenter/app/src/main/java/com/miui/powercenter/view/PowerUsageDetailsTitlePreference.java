
package com.miui.powercenter.view;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerData.PowerMode;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PowerUsageDetailsTitlePreference extends Preference {

    private CharSequence mTitle;
    private CharSequence mSummary;
    private CharSequence mContent;
    private int mProgress;
    private Drawable mIcon;

    public PowerUsageDetailsTitlePreference(Context context) {
        this(context, null);
    }

    public PowerUsageDetailsTitlePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowerUsageDetailsTitlePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTitle = getTitle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return View.inflate(getContext(), R.layout.pc_power_usage_details_title_pref, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        iconView.setImageDrawable(mIcon);

        TextView mTitleView = (TextView) view.findViewById(R.id.title);
        TextView mSummaryView = (TextView) view.findViewById(R.id.summary);
        TextView mContentView = (TextView) view.findViewById(R.id.content);

        if (TextUtils.isEmpty(mTitle)) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(mSummary)) {
            mSummaryView.setVisibility(View.GONE);
        } else {
            mSummaryView.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(mContent)) {
            mContentView.setVisibility(View.GONE);
        } else {
            mContentView.setVisibility(View.VISIBLE);
        }

        mTitleView.setText(mTitle);
        mSummaryView.setText(mSummary);
        mContentView.setText(mContent);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBar.setProgress(mProgress);
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

    @Override
    public void setSummary(CharSequence summary) {
        if (summary == null && mSummary != null || summary != null && !summary.equals(mSummary)) {
            mSummary = summary;
            notifyChanged();
        }
    }

    @Override
    public void setSummary(int summaryResId) {
        setSummary(getContext().getResources().getString(summaryResId));
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

    public void setProgress(int progress) {
        if (mProgress != progress) {
            mProgress = progress;
            notifyChanged();
        }
    }

    public void setIcon(Drawable icon) {
        if (mIcon != icon) {
            mIcon = icon;
            notifyChanged();
        }
    }
}
