
package com.miui.permcenter.permissions;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.miui.securitycenter.R;
import com.lbe.security.bean.AppPermissionConfig;

public class AppPermsEditorPreference extends Preference {
    private int permissonAction = AppPermissionConfig.ACTION_PROMPT;
    private boolean isIconEnable = true;

    public AppPermsEditorPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.pm_app_permission_preference);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ImageView actionView = (ImageView) view.findViewById(R.id.action);
        switch (permissonAction) {
            case AppPermissionConfig.ACTION_ACCEPT:
                if (isIconEnable) {
                    actionView.setImageResource(R.drawable.icon_action_accept);
                } else {
                    actionView.setImageResource(R.drawable.icon_action_accept_disable);
                }
                break;
            case AppPermissionConfig.ACTION_PROMPT:
                if (isIconEnable) {
                    actionView.setImageResource(R.drawable.icon_action_prompt);
                } else {
                    actionView.setImageResource(R.drawable.icon_action_prompt_disable);
                }
                break;
            case AppPermissionConfig.ACTION_REJECT:
                if (isIconEnable) {
                    actionView.setImageResource(R.drawable.icon_action_reject);
                } else {
                    actionView.setImageResource(R.drawable.icon_action_reject_disable);
                }
                break;
            default:
                actionView.setImageDrawable(null);
                break;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        isIconEnable = enabled;
        super.setEnabled(enabled);
    }

    public void setPermissionAction(int action) {
        permissonAction = action;
        notifyChanged();
    }

}
