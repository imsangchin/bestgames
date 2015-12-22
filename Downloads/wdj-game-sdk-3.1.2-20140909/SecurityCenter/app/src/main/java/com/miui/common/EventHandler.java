
package com.miui.common;

import android.os.Handler;
import android.os.Message;

public class EventHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        // ignore
    }

    /**
     * @param eventType
     * @param event
     */
    public void sendEventMessage(int eventType, Object event) {
        Message msg = new Message();
        msg.what = eventType;
        msg.obj = event;
        sendMessage(msg);
    }
}
