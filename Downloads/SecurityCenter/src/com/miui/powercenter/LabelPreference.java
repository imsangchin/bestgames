package com.miui.powercenter;


import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.miui.securitycenter.R;

public class LabelPreference extends Preference{
    private static final String TAG = "LabelPreference";
    private String mLabel;

    public LabelPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLabel = null;
    }

    public LabelPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        TextView labelView = (TextView) view.findViewById(R.id.label);
        labelView.setText(mLabel);
        super.onBindView(view);
    }

    public void setLabel(String label) {
        if (!TextUtils.equals(mLabel, label)) {
            mLabel = label;
            notifyChanged();
        }
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(Context context, int resId) {
        setLabel(context.getString(resId));
    }

}
