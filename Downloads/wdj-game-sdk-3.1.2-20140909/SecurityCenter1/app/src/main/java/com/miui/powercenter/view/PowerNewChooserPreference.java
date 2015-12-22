package com.miui.powercenter.view;

import com.miui.securitycenter.R;
import com.miui.powercenter.PowerModeChooser;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerModeStateTransfer;
import com.miui.powercenter.provider.SqlUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import miui.preference.RadioButtonPreference;

public class PowerNewChooserPreference extends RadioButtonPreference{

    private ImageView mApply;
    private int mModeId;
    private PowerModeStateTransfer mTransition;
    private Context mContext;
    private static final String TAG = "PowerNewChooserPreference";

    public PowerNewChooserPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.pc_power_choose_item);
    }

    public PowerNewChooserPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowerNewChooserPreference(Context context, int modeId) {
        this(context, null);
        this.mModeId = modeId;
        mTransition = PowerModeStateTransfer.getInstance(context);
        mContext = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mApply = (ImageView)view.findViewById(R.id.right_arrow);
        mApply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.miui.powercenter.PowerModeCustomizer");
                Bundle b = new Bundle();
                int uising_id = DataManager.getInstance(mContext).getInt(DataManager.KEY_POWER_MODE_APPLIED , -1);
                b.putInt(PowerMode.KEY_POWER_MODE_ID, Integer.parseInt(String.valueOf(mModeId)));
                b.putInt(PowerMode.KEY_POWER_MODE_ID_USING, Integer.parseInt(String.valueOf(uising_id)));
                intent.putExtras(b);
                getContext().startActivity(intent);
                // TODO Auto-generated method stub
            }
        });
    }

    public int getModeID() {
        return this.mModeId;
    }
}
