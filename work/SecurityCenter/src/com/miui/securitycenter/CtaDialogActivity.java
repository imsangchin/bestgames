
package com.miui.securitycenter;


import com.cleanmaster.sdk.IKSCleaner;
import com.lbe.security.service.provider.PermissionManager;
import com.miui.permcenter.PermissionUtils;
import com.miui.securitycenter.R;

import miui.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CtaDialogActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showConnectNetworkAlert();
    }

    public void showConnectNetworkAlert() {
        final String titleText = getString(R.string.cta_dialog_title);
        final String possitiveText = getString(android.R.string.ok);

        final View message = View.inflate(CtaDialogActivity.this, R.layout.impunity_declaration,
                null);
        TextView declaration = (TextView) message.findViewById(R.id.impunity_declaration);
        declaration.setMovementMethod(LinkMovementMethod.getInstance());

        final CheckBox checkbox = (CheckBox) message
                .findViewById(R.id.impunity_declaration_checkbox);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(titleText)
                .setView(message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Preferences.setCtaCheckboxChecked(false);
                        Preferences.setConnectNetworkAlow(false);
                        miui.provider.ExtraGuard.setTmsAutoConnectNetworkEnabled(
                                CtaDialogActivity.this, false);
                        setCleanerNetworkAccess(CtaDialogActivity.this, false);
                        PermissionUtils.notifAuthManagerConnectSwitchChanged(CtaDialogActivity.this);
                    }
                })
                .setPositiveButton(possitiveText, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Preferences.setCtaCheckboxChecked(checkbox.isChecked());
                        Preferences.setConnectNetworkAlow(true);
                        miui.provider.ExtraGuard.setTmsAutoConnectNetworkEnabled(
                                CtaDialogActivity.this, true);
                        setCleanerNetworkAccess(CtaDialogActivity.this, true);
                        PermissionUtils.notifAuthManagerConnectSwitchChanged(CtaDialogActivity.this);
                    }
                })
                .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    private void setCleanerNetworkAccess(final Context context, final boolean enabled) {
        AidlProxyHelper.getInstance().bindCleanProxy(context, new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // ignore
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                IKSCleaner cleaner = IKSCleaner.Stub.asInterface(service);
                if (cleaner != null) {
                    try {
                        cleaner.SetEnableNetworkAccess(enabled);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    AidlProxyHelper.getInstance().unbindProxy(context, this);
                }
            }
        });
    }
}
