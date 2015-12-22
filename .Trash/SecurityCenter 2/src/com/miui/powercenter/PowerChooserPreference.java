
package com.miui.powercenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.PowerData.PowerMode;

import miui.preference.RadioButtonPreference;

public class PowerChooserPreference extends RadioButtonPreference {
    private static final String TAG = "PowerChooserPreference";

    private int mModeId;

    public PowerChooserPreference(Context context, AttributeSet attrs, int modeId) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.pc_preference_widget_detail);
        mModeId = modeId;
    }

    public PowerChooserPreference(Context context, int modeId) {
        this(context, null, modeId);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView detailArrow = (ImageView) view.findViewById(R.id.detail_arrow);
        detailArrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.miui.powercenter.PowerModeCustomizer");
                Bundle b = new Bundle();
                b.putInt(PowerMode.KEY_POWER_MODE_ID, mModeId);
                intent.putExtras(b);
                getContext().startActivity(intent);
            }
        });
    }
}
