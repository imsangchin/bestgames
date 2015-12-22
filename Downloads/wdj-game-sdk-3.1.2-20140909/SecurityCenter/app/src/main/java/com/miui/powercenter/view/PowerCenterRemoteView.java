package com.miui.powercenter.view;

import android.app.PendingIntent;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import com.miui.securitycenter.R;

public class PowerCenterRemoteView extends RemoteViews{

    protected Context mContext;
    private static final String sPcBaseLayout             = "pc_notification_remoteview";
    private static final String sSubTitle                 = "sub_title";
    public static final String  sTitle                    = "title";
    public static final String  sActionButton             = "action_button";

    public PowerCenterRemoteView(Context context) {
        super(context.getPackageName(), context.getResources().getIdentifier(sPcBaseLayout, "layout", context.getPackageName()));
        mContext = context;
    }

    public PowerCenterRemoteView(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public void setIcon(int srcId) {
        setImageViewResource(R.id.custom_icon, srcId);
    }

    public void setIcon(android.graphics.Bitmap bitmap) {
        setImageViewBitmap(R.id.custom_icon, bitmap);
    }

    public void setTitles(String title, String subTitle) {

        if (title != null) title = title.trim();
        if (subTitle != null) subTitle = subTitle.trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(subTitle)) return;

        //如果title 是空，那么使用 subTitle 作为替代
        if (TextUtils.isEmpty(title)) {
            title = subTitle;
            subTitle = "";
        }

        //如果subTitle 是空，那么使用单行格式，否则使用双行格式
        int titleLayoutId    = 0;
        int subTitleLayoutId = 0;

        if (!TextUtils.isEmpty(subTitle)) {
            subTitleLayoutId = mContext.getResources().getIdentifier(sSubTitle, "id", mContext.getPackageName());
            setTextViewText(subTitleLayoutId, subTitle);
            setViewVisibility(subTitleLayoutId, View.VISIBLE);
        }

        titleLayoutId = mContext.getResources().getIdentifier(sTitle, "id", mContext.getPackageName());
        setTextViewText(titleLayoutId, title);
    }

    public void setActionButton(String text, PendingIntent pendingIntent) {

        if (text != null) text = text.trim();

        int buttonLayoutId = mContext.getResources().getIdentifier(sActionButton, "id", mContext.getPackageName());
        if (!TextUtils.isEmpty(text)) {
            setTextViewText(buttonLayoutId, text);
            if (null != pendingIntent)
                setOnClickPendingIntent(buttonLayoutId, pendingIntent);

            setViewVisibility(buttonLayoutId, View.VISIBLE);
        }
        else {
            setViewVisibility(buttonLayoutId, View.GONE);
        }
    }
}

