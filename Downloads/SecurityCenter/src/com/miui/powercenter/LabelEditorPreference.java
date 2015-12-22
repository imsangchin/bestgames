
package com.miui.powercenter;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.miui.securitycenter.R;

public class LabelEditorPreference extends Preference {
    private static final String TAG = "TitlePreference";
    private String mTitle;
    private String mLabel;

    public LabelEditorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomAttributes);
        mTitle = a.getString(R.styleable.CustomAttributes_title);
        mLabel = a.getString(R.styleable.CustomAttributes_label);
        a.recycle();

        setLayoutResource(R.layout.pc_label_editor);
    }

    public LabelEditorPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView titleVeiw = (TextView)view.findViewById(R.id.title);
        if(titleVeiw != null) {
            titleVeiw.setText(mTitle);
        }

        EditText edit = (EditText)view.findViewById(R.id.label);
        if(edit != null) {
            edit.setText(mLabel);
        }
    }

    public void setMiuiTitle(String title) {
       if(!TextUtils.equals(mTitle, title)) {
           mTitle = title;
           notifyChanged();
       }
    }

    public void setMiuiLabel(String label) {
        if(!TextUtils.equals(mLabel, label)) {
            mLabel = label;
            notifyChanged();
        }
    }
}