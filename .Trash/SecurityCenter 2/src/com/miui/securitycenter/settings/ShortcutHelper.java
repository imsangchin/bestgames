
package com.miui.securitycenter.settings;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.MiuiIntent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;

import com.miui.securitycenter.R;

public class ShortcutHelper {
    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_UNINSTALL_SHORTCUT = "com.miui.home.launcher.action.UNINSTALL_SHORTCUT";
    public static final String CONTENT_URI_FAVORITES = "content://com.miui.home.launcher.settings/favorites";

    public static final String FAVORITES_ITEM_TYPE = "itemType";
    public static final int FAVORITES_ITEM_TYPE_GADGET = 5;
    public static final String FAVORITES_APPWIDGET_ID = "appWidgetId";
    public static final int FAVORITES_GADGET_ID_CLEAR_BUTTON = 12;

    public enum Shortcut {
        QUICk_CLEANUP, OPTIMIZE_CENTER, POWER_CENTER, VIRUS_CENTER, PERM_CENTER, NETWORK_ASSISTANT, ANTISPAM
    }

    private static ShortcutHelper INST;
    private Context mContext;

    private ShortcutHelper(Context context) {
        mContext = context;
    }

    public static ShortcutHelper getInstance(Context context) {
        if (INST == null) {
            INST = new ShortcutHelper(context.getApplicationContext());
        }
        return INST;
    }

    public void createShortcut(Shortcut shortcut) {
        Intent shortcutIntent = createShortcutIntent(shortcut, ACTION_INSTALL_SHORTCUT);
        mContext.sendBroadcast(shortcutIntent);
    }

    public boolean queryShortcut(Shortcut shortcut) {
        if (shortcut == Shortcut.QUICk_CLEANUP) {
            return queryQuickCleanShortcut();
        }
        Intent intent = createPendingIntent(shortcut);
        if (intent == null) {
            return false;
        }

        String intentString = intent.toUri(0);

        Uri uri = Uri.parse(CONTENT_URI_FAVORITES);
        String[] projection = new String[] {
                "_id"
        };
        String selection = " intent = ? ";
        String[] selectionArgs = new String[] {
                intentString
        };

        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        if (cursor == null) {
            return false;
        }

        if (cursor.getCount() != 0) {
            cursor.close();
            cursor = null;
            return true;
        }
        cursor.close();
        cursor = null;
        return false;
    }

    public boolean queryQuickCleanShortcut() {
        Uri uri = Uri.parse(CONTENT_URI_FAVORITES);
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(uri, null, FAVORITES_ITEM_TYPE + "=" + FAVORITES_ITEM_TYPE_GADGET
                + " AND " + FAVORITES_APPWIDGET_ID + "= " + FAVORITES_GADGET_ID_CLEAR_BUTTON, null,
                null);
        if (cursor == null) {
            return false;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            cursor = null;
            return false;
        }

        cursor.close();
        cursor = null;
        return true;
    }

    public void removeShortcut(Shortcut shortcut) {
        Intent shortcutIntent = createShortcutIntent(shortcut, ACTION_UNINSTALL_SHORTCUT);
        mContext.sendBroadcast(shortcutIntent);
    }

    public Intent createShortcutIntent(Shortcut shortcut, String action) {
        String name = null;
        int icon_res = -1;
        switch (shortcut) {
            case QUICk_CLEANUP:
                name = "com.miui.securitycenter:string/btn_text_quick_cleanup";
                icon_res = R.drawable.ic_launcher_quick_clean;
                break;
            case OPTIMIZE_CENTER:
                name = "com.miui.securitycenter:string/activity_title_garbage_cleanup";
                icon_res = R.drawable.ic_launcher_rubbish_clean;
                break;
            case NETWORK_ASSISTANT:
                name = "com.miui.securitycenter:string/activity_title_networkassistants";
                icon_res = R.drawable.ic_launcher_network_assistant;
                break;
            case ANTISPAM:
                name = "com.miui.securitycenter:string/activity_title_antispam";
                icon_res = R.drawable.ic_launcher_anti_spam;
                break;
            case POWER_CENTER:
                name = "com.miui.securitycenter:string/activity_title_power_manager";
                icon_res = R.drawable.ic_launcher_power_optimize;
                break;
            case VIRUS_CENTER:
                name = "com.miui.securitycenter:string/activity_title_antivirus";
                icon_res = R.drawable.ic_launcher_virus_scan;
                break;
            case PERM_CENTER:
                name = "com.miui.securitycenter:string/activity_title_license_manager";
                icon_res = R.drawable.ic_launcher_license_manage;
                break;
            default:
                break;
        }

        Intent intent = createPendingIntent(shortcut);
        if (intent == null) {
            return null;
        }

        Intent shortcutIntent = new Intent(action);
        shortcutIntent.putExtra("duplicate", false);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        final Parcelable icon = Intent.ShortcutIconResource.fromContext(mContext, icon_res);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        return shortcutIntent;
    }

    private Intent createPendingIntent(Shortcut shortcut) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        switch (shortcut) {
            case QUICk_CLEANUP:
                intent.setAction(MiuiIntent.ACTION_CREATE_QUICK_CLEANUP_SHORTCUT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                break;
            case OPTIMIZE_CENTER:
                intent.setAction(MiuiIntent.ACTION_GARBAGE_CLEANUP);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.optimizecenter.MainActivity"));
                break;
            case NETWORK_ASSISTANT:
                intent.setAction(MiuiIntent.ACTION_VIEW_DATA_USAGE_SUMMARY);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setComponent(new ComponentName("com.miui.networkassistant", "com.miui.networkassistant.ui.NetworkAssistantActivity"));
                break;
            case ANTISPAM:
                intent.setAction(MiuiIntent.ACTION_SET_FIREWALL);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setComponent(new ComponentName("com.miui.antispam", "com.miui.antispam.firewall.AntiSpamTab"));
                break;
            case POWER_CENTER:
                intent.setAction(MiuiIntent.ACTION_POWER_MANAGER);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerCenter"));
                break;
            case VIRUS_CENTER:
                intent.setAction(MiuiIntent.ACTION_ANTIVIRUS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.antivirus.MainActivity"));
                break;
            case PERM_CENTER:
                intent.setAction(MiuiIntent.ACTION_LICENSE_MANAGER);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.MainAcitivty"));
                break;
            default:
                break;
        }
        return intent;
    }
}
