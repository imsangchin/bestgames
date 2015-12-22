
package com.miui.powercenter;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.miui.securitycenter.R;

/**
 * 这个类，虽然继承了 Preference，但是在xml 里面， 使用了自己的layout 来替代Preference 原先的layout
 *
 */
public class OptionPreference extends Preference {
    private static final String TAG = "OptionPreference";
    private String mLabel;
    private boolean mClickable;

    public OptionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //定义在 pc_power_center_attrs.xml 里面，作为这个类的可以在xml 里面配置的属性
        //这里是获得这些配置属性的值
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomAttributes);
        mLabel = a.getString(R.styleable.CustomAttributes_label);
        mClickable = a.getBoolean(R.styleable.CustomAttributes_clickable, false);
        a.recycle();
    }

    public OptionPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        TextView labelView = (TextView) view.findViewById(R.id.label);
        if (labelView != null) {
            labelView.setText(mLabel);
            if (!mClickable) {
                view.findViewById(R.id.arrow_view).setVisibility(View.GONE);
                //如果不能进行点击，那么就把text 的上下左右的图片去掉
                labelView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }else{
                view.findViewById(R.id.arrow_view).setVisibility(View.VISIBLE);
            }
        }

        super.onBindView(view);
    }

    public void setMiuiLabel(String label) {
        if (!TextUtils.equals(mLabel, label)) {
            mLabel = label;
            notifyChanged();
        }
    }

    public void setMiuiLabel(Context context, int resId) {
        setMiuiLabel(context.getString(resId));
    }

    public void setMiuiClickable(boolean clickable) {
        if (mClickable != clickable) {
            mClickable = clickable;
            notifyChanged();
        }
    }
}
