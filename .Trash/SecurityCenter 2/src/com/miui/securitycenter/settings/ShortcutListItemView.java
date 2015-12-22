
package com.miui.securitycenter.settings;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.permcenter.event.EnableAppGetRootEvent;
import com.miui.permcenter.event.EventType;

import miui.widget.SlidingButton;
import miui.content.res.IconCustomizer;

import com.miui.securitycenter.R;
import com.miui.securitycenter.settings.ShortcutHelper.Shortcut;

public class ShortcutListItemView extends LinearLayout implements OnCheckedChangeListener {

    private EventHandler mEventHandler;
    private Shortcut mShortcut;

    private ImageView mIconView;
    private TextView mTitleView;

    private SlidingButton mSlidingButton;

    private ShortcutHelper mShortcutHelper;

    public ShortcutListItemView(Context context) {
        this(context, null);
    }

    public ShortcutListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortcutListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mShortcutHelper = ShortcutHelper.getInstance(getContext());
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mSlidingButton = (SlidingButton) findViewById(R.id.sliding_button);
    }

    public void fillData(Shortcut data) {
        mShortcut = data;
        mSlidingButton.setOnCheckedChangeListener(null);

        Resources res = getResources();
        mSlidingButton.setChecked(mShortcutHelper.queryShortcut(data));
        switch (data) {
            case QUICk_CLEANUP:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_quick_clean)));
                mTitleView.setText(R.string.btn_text_quick_cleanup);
                break;
            case OPTIMIZE_CENTER:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_rubbish_clean)));
                mTitleView.setText(R.string.activity_title_garbage_cleanup);
                break;
            case NETWORK_ASSISTANT:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_network_assistant)));
                mTitleView.setText(R.string.activity_title_networkassistants);
                break;
            case ANTISPAM:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_anti_spam)));
                mTitleView.setText(R.string.activity_title_antispam);
                break;
            case POWER_CENTER:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_power_optimize)));
                mTitleView.setText(R.string.activity_title_power_manager);
                break;
            case VIRUS_CENTER:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_virus_scan)));
                mTitleView.setText(R.string.activity_title_antivirus);
                break;
            case PERM_CENTER:
                mIconView.setImageDrawable(IconCustomizer.generateIconStyleDrawable(
                        res.getDrawable(R.drawable.ic_launcher_license_manage)));
                mTitleView.setText(R.string.activity_title_license_manager);
                break;

            default:
                break;
        }
        mSlidingButton.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mShortcutHelper.createShortcut(mShortcut);
        } else {
            mShortcutHelper.removeShortcut(mShortcut);
        }
    }
}
