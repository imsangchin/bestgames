package com.anzhuoshoudiantong;

import com.anzhuoshoudiantong.utils.ApkUtils;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

public class DownloadController {
  private static DownloadController instance;
  private DownloadManager downloadManager;
  private Context mContext;
  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
        long downloadId = intent.getLongExtra(
            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        Query query = new Query();
        query.setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
          int columnIndex = c
              .getColumnIndex(DownloadManager.COLUMN_STATUS);
          if (DownloadManager.STATUS_SUCCESSFUL == c
              .getInt(columnIndex)) {
            String uriString = c
                .getString(c
                    .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            ApkUtils.install(mContext, uriString);
          }
        }
      }
    }
  };


  private DownloadController() {}

  public DownloadController setContext(Context context) {
    mContext = context;
    downloadManager =
        (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    return instance;
  }

  public static synchronized DownloadController getInstance() {
    if (instance == null) {
      instance = new DownloadController();
    }
    return instance;
  }

  public long download(String url, String title) {
    Request request = new Request(Uri.parse(url));
    request.setTitle(title);
    Toast.makeText(mContext, R.string.start_download, Toast.LENGTH_SHORT).show();
    return downloadManager.enqueue(request);
  }

  public BroadcastReceiver getDownloadReceiver() {
    return receiver;
  }

}
