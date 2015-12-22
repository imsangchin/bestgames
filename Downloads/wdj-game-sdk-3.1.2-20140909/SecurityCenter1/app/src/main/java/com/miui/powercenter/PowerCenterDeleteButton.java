package com.miui.powercenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.miui.securitycenter.R;

public class PowerCenterDeleteButton extends LinearLayout{
    private LayoutInflater mInflater;
    private Button mDeleteButton;

    public PowerCenterDeleteButton(Context context) {
        super(context);
    }

    public PowerCenterDeleteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDeleteButton = (Button)findViewById(R.id.button_delete);
        mDeleteButton.setText(R.string.delete);
    }

    public void setButtonListener(OnClickListener listener) {
        if (null != listener) {
            mDeleteButton.setOnClickListener(listener);
        }
    }
}
