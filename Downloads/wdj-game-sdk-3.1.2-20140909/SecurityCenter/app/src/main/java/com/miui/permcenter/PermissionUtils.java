
package com.miui.permcenter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.lbe.security.bean.AppPermissionConfig;
import com.lbe.security.bean.AppPermissionConfigHelper;
import com.lbe.security.service.provider.Active;
import com.lbe.security.service.provider.Groups;
import com.lbe.security.service.provider.PermissionManager;
import com.lbe.security.service.provider.Permissions;
import com.miui.common.AndroidUtils;
import com.miui.common.IOUtils;
import com.miui.common.ImmutableSetMultimap;
import com.miui.permcenter.permissions.GroupModel;
import com.miui.permcenter.permissions.PermissionModel;
import com.miui.permcenter.permissions.PermissionSwitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.miui.securitycenter.R;

public class PermissionUtils {

    //联网开关的通知
    public static final Uri NOTIF_CONNECT_SWITCH_CHANGE_URI = Uri.withAppendedPath(PermissionManager.CONTENT_URI,
            "connectswitch");

    // 资费相关（默认不记住）
    public static final long GROUP_CHARGES_RELATED = 0x001;

    // 隐私相关（默认记住）
    public static final long GROUP_PRIVACY_RELATED = 0x002;

    // 多媒体相关（默认记住）
    public static final long GROUP_MEDIA_RELATED = 0x004;

    // 设置相关（默认记住）
    public static final long GROUP_SETTINGS_RELATED = 0x008;

    // 完全不敏感权限（不显示）和 单独权限
    public static final long GROUP_OTHERS = 0x010;

    private static final Map<Long, Long> GROUPS_PERMISSIONS = new HashMap<Long, Long>();

    /**
     * 权限及其对应的权限组
     */
    static {
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_SENDSMS, GROUP_CHARGES_RELATED);
        // 暂时把PERM_ID_SENDSMS和PERM_ID_SENDMMS
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_SENDMMS, GROUP_CHARGES_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_CALLPHONE, GROUP_CHARGES_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_SMSDB, GROUP_PRIVACY_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_CONTACT, GROUP_PRIVACY_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_CALLLOG, GROUP_PRIVACY_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_CALENDAR, GROUP_PRIVACY_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_LOCATION, GROUP_PRIVACY_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_VIDEO_RECORDER, GROUP_MEDIA_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_AUDIO_RECORDER, GROUP_MEDIA_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_WIFI_CONNECTIVITY, GROUP_SETTINGS_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_MOBILE_CONNECTIVITY, GROUP_SETTINGS_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_BT_CONNECTIVITY, GROUP_SETTINGS_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_SYSTEMALERT, GROUP_SETTINGS_RELATED);
        // GROUPS_PERMISSIONS.put(Permissions.PERM_ID_WAKELOCK,
        // GROUP_SETTINGS_RELATED);
        GROUPS_PERMISSIONS.put(Permissions.PERM_ID_SETTINGS, GROUP_SETTINGS_RELATED);
        // PERM_ID_AUTOSTART已经单独处理了
        // GROUPS_PERMISSIONS.put(Permissions.PERM_ID_AUTOSTART, GROUP_OTHERS);

        // PERM_ID_NOTIFICATION暂时不显示
        // GROUPS_PERMISSIONS.put(Permissions.PERM_ID_NOTIFICATION,
        // GROUP_OTHERS);
        // GROUPS_PERMISSIONS.put(0L, GROUP_OTHERS);
    }

    private static final Map<Long, Integer> GROUPS_NAMES = new HashMap<Long, Integer>();

    /**
     * 权限组及其对应的name
     */
    static {
        GROUPS_NAMES.put(GROUP_CHARGES_RELATED, R.string.permission_group_title_charges_related);
        GROUPS_NAMES.put(GROUP_PRIVACY_RELATED, R.string.permission_group_title_privacy_related);
        GROUPS_NAMES.put(GROUP_MEDIA_RELATED, R.string.permission_group_title_media_related);
        GROUPS_NAMES.put(GROUP_SETTINGS_RELATED, R.string.permission_group_title_settings_related);
        GROUPS_NAMES.put(GROUP_OTHERS, R.string.permission_group_title_others);
    }

    /**
     * 获取所有已安装的应用的权限信息 Map<PackageName, AppPermissionConfig>
     * 
     * @param context
     * @return
     */
    public static Map<String, AppPermissionConfig> loadInstalledAppPermissionConfigs(Context context) {
        Map<String, AppPermissionConfig> cfgMap = new HashMap<String, AppPermissionConfig>();

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Active.CONTENT_URI, null,
                    Active.PRESENT + "!= 0", null, null);
            List<AppPermissionConfig> cfgList = AppPermissionConfigHelper
                    .createFromActiveTable(cursor);

            PackageManager pm = context.getPackageManager();

            for (AppPermissionConfig config : cfgList) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(config.getPackageName(), 0);
                    if (AndroidUtils.isThirdPartApp(appInfo)) {
                        long[] permissionIds = config.getRequestedPermissionList();
                        for (long permissionId : permissionIds) {
                            if (GROUPS_PERMISSIONS.containsKey(permissionId)) {
                                cfgMap.put(config.getPackageName(), config);
                                break;
                            }
                        }
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return cfgMap;
    }

    /**
     * 找到在permissionIds 里面，我们的有效的permission 的数目
     * 
     * @param permissionIds
     * @return
     */
    public static int getEffectivePermissionCount(long[] permissionIds) {
        int count = 0;
        for (long permissionId : permissionIds) {
            if (GROUPS_PERMISSIONS.containsKey(permissionId)) {
                count++;
            }
        }

        return count;
    }

    /**
     * 判断某一个权限是否在MIUI需要的权限列表当中
     * 
     * @param permissionId
     * @return
     */
    public static final boolean inPermissionGroups(long permissionId) {
        return GROUPS_PERMISSIONS.containsKey(permissionId);
    }

    /**
     * 根据某一权限获取所有使用该权限的应用
     * 
     * @param context
     * @param permissionId
     * @return
     */
    public static Map<String, AppPermissionConfig> loadPermissionApps(Context context,
            long permissionId) {

        Map<String, AppPermissionConfig> AppConfigs = new HashMap<String, AppPermissionConfig>();

        PackageManager pm = context.getPackageManager();
        ContentResolver cr = context.getContentResolver();

        Cursor cursor = null;
        try {
            cursor = cr.query(Active.CONTENT_URI, null, Active.PERMISSION_MASK + "& ? != 0",
                    new String[] {
                        Long.toString(permissionId)
                    }, null);

            List<AppPermissionConfig> cfgList = AppPermissionConfigHelper
                    .createFromActiveTable(cursor);

            for (AppPermissionConfig config : cfgList) {
                try {
                    if (AndroidUtils.SHELL_PKG_NAME.equals(config.getPackageName())) {
                        continue;
                    }
                    ApplicationInfo appInfo = pm.getApplicationInfo(config.getPackageName(), 0);
                    if (AndroidUtils.isThirdPartApp(appInfo)) {
                        AppConfigs.put(config.getPackageName(), config);
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return AppConfigs;
    }

    public static int loadPermissionAppsCount(Context context, long permissionId) {

        PackageManager pm = context.getPackageManager();
        ContentResolver cr = context.getContentResolver();

        int count = 0;

        Cursor cursor = null;
        try {
            cursor = cr.query(Active.CONTENT_URI, null, Active.PERMISSION_MASK + "& ? != 0",
                    new String[] {
                        Long.toString(permissionId)
                    }, null);

            List<AppPermissionConfig> cfgList = AppPermissionConfigHelper
                    .createFromActiveTable(cursor);

            for (AppPermissionConfig config : cfgList) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(config.getPackageName(), 0);
                    if (AndroidUtils.isThirdPartApp(appInfo)) {
                        ++count;
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return count;
    }

    /**
     * 获取某一应用的权限信息
     * 
     * @param context
     * @param pkgName
     * @return
     */
    public static AppPermissionConfig loadAppPermissionConfig(Context context, String pkgName) {

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(Active.CONTENT_URI, null, Active.PACKAGE_NAME + " =? ",
                    new String[] {
                        pkgName
                    }, null);
            return AppPermissionConfigHelper.createFirstFromActiveTable(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return null;
    }

    /**
     * Map<GroupId, List<PermissonModel>>
     * 
     * @param context
     * @return 已分组的所有权限
     */
    public static Map<Long, List<PermissionModel>> loadGroupedAllPermissions(Context context) {
        Map<Long, List<PermissionModel>> permissionGroups = new HashMap<Long, List<PermissionModel>>();

        // Map<Long, PermissionModel> allPermissions =
        // loadAllPermissions(context);

        Map<Long, String> permissionNames = AppPermissionConfig.getPermissionNames();

        Set<Long> permissionIds = permissionNames.keySet();
        for (long permissionId : permissionIds) {

            if (GROUPS_PERMISSIONS.containsKey(permissionId)) {
                PermissionModel model = loadPermissionById(context, permissionId);
                if (model != null) {
                    long groupId = GROUPS_PERMISSIONS.get(permissionId);
                    List<PermissionModel> permissionList = permissionGroups.get(groupId);
                    if (permissionList == null) {
                        permissionList = new ArrayList<PermissionModel>();
                        permissionGroups.put(groupId, permissionList);
                    }
                    permissionList.add(model);
                }
            }

        }

        return permissionGroups;
    }

    /**
     * Map<id, PermissonModel>
     * 
     * @param context
     * @return 所有权限列表
     */
    public static Map<Long, PermissionModel> loadAllPermissions(Context context) {
        Map<Long, PermissionModel> permissions = new HashMap<Long, PermissionModel>();

        ContentResolver cr = context.getContentResolver();

        Cursor cursor = null;
        try {
            String[] projection = {
                    Permissions._ID, Permissions.DEFAULT_ACTION, Permissions.DESC, Permissions.NAME
            };
            cursor = cr.query(Permissions.CONTENT_URI, projection, null, null, null);

            if (cursor == null) {
                return permissions;
            }

            if (!cursor.moveToFirst()) {
                return permissions;
            }

            do {
                int defAction = cursor.getInt(cursor.getColumnIndex(Permissions.DEFAULT_ACTION));
                String descx = cursor.getString(cursor.getColumnIndex(Permissions.DESC));
                String name = cursor.getString(cursor.getColumnIndex(Permissions.NAME));
                long id = cursor.getLong(cursor.getColumnIndex(Permissions._ID));

//                if (id == Permissions.PERM_ID_SENDSMS) {
//                    descx = context.getResources().getString(R.string.permission_send_sms_descx);
//                }

                PermissionModel model = new PermissionModel();
                model.setDefaultAction(defAction);
                model.setDescx(descx);
                model.setName(name);
                model.setId(id);
                model.setUsedAppsCount(loadPermissionAppsCount(context, id));

                permissions.put(id, model);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return permissions;
    }

    public static PermissionModel loadPermissionById(Context context, long permissionId) {
        ContentResolver cr = context.getContentResolver();

        Cursor cursor = null;
        try {
            String[] projection = {
                    Permissions.DEFAULT_ACTION, Permissions.DESC, Permissions.NAME
            };
            String selection = Permissions._ID + " = ? ";
            String[] selectionArgs = {
                    String.valueOf(permissionId)
            };
            cursor = cr.query(Permissions.CONTENT_URI, projection, selection, selectionArgs, null);

            if (cursor == null) {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            int defAction = cursor.getInt(cursor.getColumnIndex(Permissions.DEFAULT_ACTION));
            String descx = cursor.getString(cursor.getColumnIndex(Permissions.DESC));
            String name = cursor.getString(cursor.getColumnIndex(Permissions.NAME));

//            if (permissionId == Permissions.PERM_ID_SENDSMS) {
//                descx = context.getResources().getString(R.string.permission_send_sms_descx);
//            }

            PermissionModel model = new PermissionModel();
            model.setDefaultAction(defAction);
            model.setDescx(descx);
            model.setName(name);
            model.setId(permissionId);
            model.setUsedAppsCount(loadPermissionAppsCount(context, permissionId));

            return model;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return null;
    }

    /**
     * Map<GroupId, List<PermissonModel>>
     * 
     * @param context
     * @param config
     * @return 某一个应用的所有已分组的权限列表
     */
    public static Map<Long, List<PermissionModel>> loadGroupedAppPermissions(Context context,
            AppPermissionConfig config) {

        // Map<Long, PermissionModel> allPermissions =
        // loadAllPermissions(context);
        Map<Long, List<PermissionModel>> targetPermissions = new HashMap<Long, List<PermissionModel>>();

        long[] permIdArray = config.getRequestedPermissionList();
        for (long permId : permIdArray) {
            if (GROUPS_PERMISSIONS.containsKey(permId)) {
                PermissionModel model = loadPermissionById(context, permId);
                if (model != null) {
                    long groupId = GROUPS_PERMISSIONS.get(permId);

                    List<PermissionModel> permList = targetPermissions.get(groupId);
                    if (permList == null) {
                        permList = new ArrayList<PermissionModel>();
                        targetPermissions.put(groupId, permList);
                    }
                    permList.add(model);
                }
            }
        }

        return targetPermissions;
    }

    /**
     * @param context
     * @param groupId
     * @return 根据groupId获取分组信息
     */
    public static GroupModel getGroupById(Context context, long groupId) {
        GroupModel model = new GroupModel();
        model.setId(groupId);
        model.setName(context.getResources().getString(GROUPS_NAMES.get(groupId)));
        return model;
    }

    /**
     * @param config
     * @param permissionId
     * @return 获取应用的权限action（允许、询问、拒绝）
     */
    public static int getPermissionAction(AppPermissionConfig config, long permissionId) {
        return config.getEffectivePermissionConfig(permissionId);
    }

    /**
     * 设置应用的权限action（允许、询问、拒绝）
     * 
     * @param context
     * @param config
     * @param permissionId
     * @param action
     */
    public static void setPermissionAction(Context context, AppPermissionConfig config,
            long permissionId, int action) {

        ContentResolver cr = context.getContentResolver();

//        if (permissionId == Permissions.PERM_ID_SENDSMS) {
//            config.setPermissionAction(Permissions.PERM_ID_SENDMMS, action);
//        }
        config.setPermissionAction(permissionId, action);
        cr.update(Uri.withAppendedPath(Active.CONTENT_URI, config.getPackageName()),
                config.toContentValues(), null, null);
    }

    public static void setAppPermissionControlOpen(Context context, boolean isOpen) {

        ContentResolver cr = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put("permission_switch", isOpen ? 1 : 0);
        cr.update(PermissionSwitch.CONTENT_URI,contentValues, null, null);
    }

    public static boolean isAppPermissionControlOpen(Context context) {

        ContentResolver cr = context.getContentResolver();

        Cursor cursor = null;
        try {
            cursor = cr.query(PermissionSwitch.CONTENT_URI, null, null, null, null);

            if (cursor == null) {
                return false;
            }

            if (!cursor.moveToFirst()) {
                return false;
            }

            int defAction = cursor.getInt(cursor.getColumnIndex("isOpen"));
            
            if (defAction == 0) {
                return false;
            }else {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return false;
    }

    public static void notifAuthManagerConnectSwitchChanged(Context context) {
        if(context != null) {
            context.getContentResolver().update(NOTIF_CONNECT_SWITCH_CHANGE_URI, new ContentValues(), null, null);
        }
    }
}
