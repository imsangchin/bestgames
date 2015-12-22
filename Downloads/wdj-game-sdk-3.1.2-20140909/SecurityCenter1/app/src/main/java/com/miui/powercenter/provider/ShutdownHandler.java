package com.miui.powercenter.provider;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class ShutdownHandler {

  private static ShutdownHandler instance;
  private final Handler mHandler;
  public static final int SHUT_DOWN_MESSAGE = 100;
  private final Context mContext;

  private ShutdownHandler(Context context) {
    mContext = context;
    mHandler = new Handler(ShutdownAlarmThreadFactory.SHUTDOWN_THREAD.getLooper()) {
      @Override
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case SHUT_DOWN_MESSAGE:
            shutdownPhone();
            break;
          default:
            super.handleMessage(msg);
        }
      }
    };
  }

  private void shutdownPhone() {
    Intent shutdownIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
    shutdownIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
    shutdownIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mContext.startActivity(shutdownIntent);
  }

  private static class ShutdownAlarmThreadFactory {

    private static final HandlerThread SHUTDOWN_THREAD;
    private static final String THREAD_NAME = "ShutdownHandlerThread";

    static {
      SHUTDOWN_THREAD = new HandlerThread(THREAD_NAME);
      SHUTDOWN_THREAD.start();
    }
  }

  public static synchronized ShutdownHandler getInstance(Context context) {
    if (instance == null) {
      instance = new ShutdownHandler(context);
    }
    return instance;
  }

  public Handler getHandler() {
    return mHandler;
  }
}
