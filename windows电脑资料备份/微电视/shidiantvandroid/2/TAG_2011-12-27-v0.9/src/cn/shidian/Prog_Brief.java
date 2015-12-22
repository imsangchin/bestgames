package cn.shidian;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import cn.shidian.PullToRefreshListView.OnRefreshListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Prog_Brief extends Activity {
    public static final int GET_DATA_MODE_NETWORK = 0;
    public static final int GET_DATA_MODE_STORE = 1;    
    public static final int UPDATE_LIST_EVENT = 0;
    public static final int DOWNLOAD_BRIEF_EVENT = 1;
    public static final int REFRESH_LIST_EVENT = 2;
    public static final String POSTFIX_TYPE = "_Brief";
    private String login_username = "";
    private String login_password = "";
    private String access_token = "";
    private long login_time = 0;
    private String prog_name = "";
    private String briefString = "";
    private long current_time = Calendar.getInstance().getTimeInMillis();
    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    private TextView text_title;
    private PullToRefreshListView list_comment;
    private Button btn_return;

    private ListItemAdapter listItemAdapter;

    private int getDataMode = GET_DATA_MODE_NETWORK;
    private ArrayList<Map<String, Object>> mBriefList = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> mTempList = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> mStoreList = null;
    private ProgressDialog progressDialog;
    
    private int localPosition = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prog_brief);

        progressDialog = new ProgressDialog(Prog_Brief.this);

        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        access_token = preferences.getString("access_token", "");
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        login_time = preferences.getLong("login_time", 0);
        
        prog_name = getIntent().getStringExtra("prog_name");

        text_title = (TextView)findViewById(R.id.text_prog_brief_title);
        list_comment = (PullToRefreshListView)findViewById(R.id.list_prog_brief);
        btn_return = (Button)findViewById(R.id.btn_prog_brief_return);

        text_title.setText(prog_name);

        if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
            Utils.DoLogin(Prog_Brief.this, login_username, login_password);
            access_token = preferences.getString("access_token", "");
        }

        listItemAdapter = new ListItemAdapter(this);
        list_comment.setAdapter(listItemAdapter);
        
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        list_comment.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread() {
                    public void run() {
                        String indexString = "";
                        String filename = "";
                        mStoreList = Utils.GetBriefListFromStored(Prog_Brief.this);
                        if (mStoreList != null) {
                            for (int i = 0; i < mStoreList.size(); i++) {
                                if (mStoreList.get(i).get("keyword").toString().equals(prog_name)) {
                                    indexString = mStoreList.get(i).get("index").toString();
                                    filename = Utils.store_path_Brief + indexString + POSTFIX_TYPE;
                                    File file = new File(filename);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    break;
                                }
                            }
						}
                        GetBriefData();
                        sendMsg(REFRESH_LIST_EVENT);
                    }
                }.start();
            }
        });
                       
        sendMsg(DOWNLOAD_BRIEF_EVENT);
    }

    void DownloadBrief() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.setMessage("正在获取简介信息...");  
        progressDialog.setIndeterminate(false);  
        progressDialog.setCancelable(false);  
        progressDialog.show();  
        
        new Thread() {
            public void run() {
                GetBriefData();
                sendMsg(UPDATE_LIST_EVENT);
            }
        }.start();
    }
    
    void GetBriefData() {
        mTempList.clear();
        JSONArray jsonArray = null;
        String urlString = "";
        boolean isStoreListFull = false;
        String indexString = "";
        String filename = "";
        Calendar calendar = Calendar.getInstance();
        String indexStringToStore = sf.format(calendar.getTime());
        mStoreList = Utils.GetBriefListFromStored(Prog_Brief.this);
        
        try {
            getDataMode = GET_DATA_MODE_NETWORK;
            
            if (mStoreList != null) {
                if (mStoreList.size() == Utils.MAX_CACHE_BRIEF_COUNT) {
                    isStoreListFull = true;
                }
                
                for (int i = 0; i < mStoreList.size(); i++) {
                    if (mStoreList.get(i).get("keyword").toString().equals(prog_name)) {
                        indexString = mStoreList.get(i).get("index").toString();
                        filename = Utils.store_path_Brief + indexString + POSTFIX_TYPE;
                        File file = new File(filename);
                        if (file.exists()) {
                            getDataMode = GET_DATA_MODE_STORE;
                        }
                        break;
                    }
                }
            } else {
                mStoreList = new ArrayList<Map<String, Object>>();
            }
        
            if (getDataMode == GET_DATA_MODE_NETWORK) {
                String keyword = URLEncoder.encode(prog_name, "UTF-8");
                urlString = "http://shenkantv.sinaapp.com/weibo_phpsdk/getHotBlog.php?token=" + access_token + "&key=" + keyword;
            } else if (getDataMode == GET_DATA_MODE_STORE) {
                urlString = filename;
            }

            File folder1 = new File(Utils.store_path);
            if (!folder1.exists()) {
                folder1.mkdir();
            }
            
            File folder2 = new File(Utils.store_path_Brief);
            if (!folder2.exists()) {
                folder2.mkdir();
            }
            
            String storeFilename = "";
            if (getDataMode == GET_DATA_MODE_NETWORK) {
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = new DefaultHttpClient().execute(request);
        
                if (response.getStatusLine().getStatusCode() == 200) {
                    briefString = EntityUtils.toString(response.getEntity(), "UTF-8");
                }
                
                if (briefString.length() > 30) {
                    storeFilename = Utils.store_path_Brief + indexStringToStore + POSTFIX_TYPE;
                    FileOutputStream outStream = new FileOutputStream(storeFilename);
                    outStream.write(briefString.getBytes());
                    outStream.close();
                } else {
                    Toast.makeText(Prog_Brief.this, "抱歉，没有匹配的微博!", Toast.LENGTH_LONG).show();
                }
            } else if (getDataMode == GET_DATA_MODE_STORE) {
                FileInputStream inStream = new FileInputStream(urlString);
                byte[] buffer = new byte[inStream.available()];
                inStream.read(buffer);
                briefString = new String(buffer);
            }
    
            String text = "";
            String user_image_url = "";
            String user_name = "";
            String user_id = "";
            jsonArray = new JSONArray(briefString);
            for (int j = 0; j < jsonArray.length(); j++) {
                String original_text = "";
                String original_user_name = "";
                String image_small = "";
                String image_middle = "";
                String image_small_original = "";
                String image_middle_original = "";
                JSONObject jsonObjectOriginal = null;
            	
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                text = jsonObject.getString("text");
    
                JSONObject jsonObjectUser = jsonObject.getJSONObject("user");
                user_image_url = jsonObjectUser.getString("profile_image_url");
                user_name = jsonObjectUser.getString("screen_name");
                user_id = jsonObjectUser.getString("id");
                
                if (jsonObject.has("thumbnail_pic") == true) {
                    image_small = jsonObject.getString("thumbnail_pic");
                    image_middle = jsonObject.getString("bmiddle_pic");
                }
                
                if (jsonObject.has("retweeted_status") == true) {
                    jsonObjectOriginal = jsonObject.getJSONObject("retweeted_status");
                    original_text = jsonObjectOriginal.getString("text");
                    if (jsonObjectOriginal.has("thumbnail_pic") == true) {
                        image_small_original = jsonObjectOriginal.getString("thumbnail_pic");
                        image_middle_original = jsonObjectOriginal.getString("bmiddle_pic");
                    }
                    JSONObject jsonObjectOriginalUser = jsonObjectOriginal.getJSONObject("user");
                    original_user_name = jsonObjectOriginalUser.getString("screen_name");
                }
                
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("text", text);
                item.put("user_image_url", user_image_url);
                item.put("user_name", user_name);
                item.put("user_id", user_id);
                item.put("original_text", original_text);
                item.put("original_user_name", original_user_name);
                item.put("image_small", image_small);
                item.put("image_middle", image_middle);
                item.put("image_small_original", image_small_original);
                item.put("image_middle_original", image_middle_original);
                
                mTempList.add(item);
            }
            
            if (getDataMode == GET_DATA_MODE_NETWORK) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("keyword", prog_name);
                item.put("index", indexStringToStore);
                mStoreList.add(item);
                
                if (isStoreListFull == true) {
                    indexString = mStoreList.get(0).get("index").toString();
                    mStoreList.remove(0);
                    filename = Utils.store_path_Brief + indexString + POSTFIX_TYPE;
                    File file = new File(filename);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                
                Utils.SaveBriefListToStored(Prog_Brief.this, mStoreList);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

    class ListItemAdapter extends BaseAdapter {
        private Context mContext;

        public ListItemAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mBriefList.size() + 1;
        }

        @Override
        public Object getItem(int position) {return null;}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView image_user_image = null;
            TextView text_comment_text = null;
            TextView text_comment_user_name = null;
            LinearLayout layout_original_text = null;
            TextView text_original_text = null;
            ImageView image_small_image = null;
            ImageView image_small_image_original = null;
            String user_image_url = "";
            String original_text = "";
            String original_user_name = "";
            String image_small = "";
            String image_small_original = "";

            if (position == 0) {
                View myview = new View(Prog_Brief.this);
                return myview;
            }
            localPosition = position - 1;
            
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.prog_brief_item, null);

            image_user_image = (ImageView)view.findViewById(R.id.image_prog_brief_item_user_image);
            text_comment_text = (TextView)view.findViewById(R.id.text_prog_brief_item_text);
            text_comment_user_name = (TextView)view.findViewById(R.id.text_prog_brief_item_user_name);
            layout_original_text = (LinearLayout)view.findViewById(R.id.linear_layout_prog_brief_original_text);
            text_original_text = (TextView)view.findViewById(R.id.text_prog_brief_original_text);
            image_small_image = (ImageView)view.findViewById(R.id.image_prog_brief_item_image_small);
            image_small_image_original = (ImageView)view.findViewById(R.id.image_prog_brief_item_original_image_small);

            user_image_url = mBriefList.get(localPosition).get("user_image_url").toString();

            text_comment_text.setText(Utils.ProcessStringWithURL(mBriefList.get(localPosition).get("text").toString()));
            text_comment_user_name.setText(mBriefList.get(localPosition).get("user_name").toString());

            text_comment_text.setMovementMethod(LinkMovementMethod.getInstance());
            image_user_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int myPosition = position - 1;
                    Intent intent = new Intent(Prog_Brief.this, User_Detail.class);
                    intent.putExtra("user_name", (String)mBriefList.get(myPosition).get("user_name"));
                    intent.putExtra("user_id", (String)mBriefList.get(myPosition).get("user_id"));
                    startActivity(intent);
                }
            });

            try {
                BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user_image);
                bitmapTask.execute();
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }

            original_text = (String)mBriefList.get(localPosition).get("original_text");
            original_user_name = (String)mBriefList.get(localPosition).get("original_user_name");
            if (original_text.equals("")) {
                text_original_text.setTextSize(1);
                layout_original_text.setVisibility(View.INVISIBLE);
            } else {
                layout_original_text.setBackgroundResource(R.drawable.original_text_border);
                text_original_text.setText(Utils.GetOriginalTextString(original_user_name, original_text));
            }

            image_small = (String)mBriefList.get(localPosition).get("image_small");
            if (!image_small.equals("")) {
                try {
                    BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(image_small, image_small_image);
                    bitmapTask.execute();
                } catch (Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            }
            
            image_small_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int myPosition = position - 1;
                    final Dialog dialog = new Dialog(Prog_Brief.this);
                    ImageView view = new ImageView(Prog_Brief.this);
                    try {
                        BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(mBriefList.get(myPosition).get("image_middle").toString(), view);
                        bitmapTask.execute();
                    } catch (Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(view);
                    dialog.setCancelable(true);
                    dialog.setTitle("查看图片...");
                    dialog.show();
                }
            });
            
            image_small_original = (String)mBriefList.get(localPosition).get("image_small_original");
            if (!image_small_original.equals("")) {
                try {
                    BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(image_small_original, image_small_image_original);
                    bitmapTask.execute();
                } catch (Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            }
            
            image_small_image_original.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int myPosition = position - 1;
                    final Dialog dialog = new Dialog(Prog_Brief.this);
                    ImageView view = new ImageView(Prog_Brief.this);
                    try {
                        BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(mBriefList.get(myPosition).get("image_middle_original").toString(), view);
                        bitmapTask.execute();
                    } catch (Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                    
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            
                        }
                    });
                    
                    dialog.setContentView(view);
                    dialog.setCancelable(true);
                    dialog.setTitle("查看图片...");
                    dialog.show();
                }
            });
            
            return view;
        }
    }
    
    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what)
                {
                case UPDATE_LIST_EVENT:
                    mBriefList = (ArrayList<Map<String, Object>>) mTempList.clone();
                    listItemAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    if (list_comment.getFirstVisiblePosition() == 0) {
                        list_comment.setSelection(1);
                    }
                    break;
                case DOWNLOAD_BRIEF_EVENT:
                    DownloadBrief();
                    break;
                case REFRESH_LIST_EVENT:
                    listItemAdapter.notifyDataSetChanged();
                    list_comment.onRefreshComplete();
                	break;
                }
            }
            super.handleMessage(msg);
        }
    };
}