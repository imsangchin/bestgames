
package com.miui.optimizecenter;

import com.miui.securitycenter.R;
import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CustomPreference extends Preference {

    private CharSequence mTitle;
    private CharSequence mSummary;
    private CharSequence mContent;

    public CustomPreference(Context context) {
        this(context, null);
    }

    public CustomPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTitle = getTitle();
        mSummary = getSummary();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = View.inflate(getContext(), R.layout.op_preference_title_summary_content, null);

        return view;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
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
}
