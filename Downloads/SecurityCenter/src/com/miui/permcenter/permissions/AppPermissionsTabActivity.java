
package com.miui.permcenter.permissions;

import miui.app.Activity;
import miui.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import miui.app.ActionBar;

import com.miui.permcenter.Preferences;
import com.miui.securitycenter.R;

public class AppPermissionsTabActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();
        bar.setFragmentViewPagerMode(this, getFragmentManager());

        bar.addFragmentTab(AppPermissionsFragment.TAG,
                bar.newTab().setText(R.string.activity_title_apps_manager),
                AppPermissionsFragment.class, null, false);

        bar.addFragmentTab(PermissionsFragment.TAG,
                bar.newTab().setText(R.string.activity_title_permissions_manager),
                PermissionsFragment.class, null, false);

    }

}
