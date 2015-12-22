package cn.shidian;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import cn.shidian.PullToRefreshListView;
import cn.shidian.PullToRefreshListView.OnRefreshListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Prog_Comment extends Activity implements OnGestureListener {
    public static final int GET_DATA_MODE_NETWORK = 0;
    public static final int GET_DATA_MODE_STORE = 1;    
    public static final int UPDATE_LIST_EVENT = 0;
    public static final int REFRESH_LIST_EVENT = 1;
    public static final int ADD_FOOTER_EVENT = 2;
    public static final int REMOVE_FOOTER_EVENT = 3;
    public static final int DOWNLOAD_COMMENT_EVENT = 4;
    public static final int COMMENT_COUNT_PER_PAGE = 20;
    public static final int TOTALTYPE_ALL = 0;
    public static final int TOTALTYPE_FRIEND = 1;
    public static final int TOTALTYPE_CARE = 2;
    public static final String POSTFIX_TYPE1 = "_All";
    public static final String POSTFIX_TYPE2 = "_Friend";
    public static final String POSTFIX_TYPE3 = "_Care";
    
    public static final String EMPTY_RESPONSE_STRING = "{\"statuses\":[]";
    
    private String login_username = "";
    private String login_password = "";
    private String access_token = "";
    private String user_id = "";
    private long login_time = 0;
    private String prog_name = "";
    private int comment_page = 1;

    private String dataString = "";
    private long current_time = Calendar.getInstance().getTimeInMillis();
    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    private Gallery gallery_bar;
    private TextAdapter textAdapter;
    private TextView text_title;
    private PullToRefreshListView list_comment;
    private Button btn_return;
    private Button btn_new_comment;
    private TextView text_more_comment;

    private ListItemAdapter listItemAdapter;
    private View footerView = null;
    private boolean isAddedFooterView = false;
    
    private ArrayList<Map<String, Object>> mCommentList = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> mTempList = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> mAllStoreList = null;
    private ArrayList<Map<String, Object>> mOtherStoreList = null;
    private Timer timer = new Timer();
    
    private int totalType = TOTALTYPE_ALL;
    private int lastTotalType = -1;
    private ProgressDialog progressDialog;
    
    private GestureDetector detector;  
    private boolean isFlapping = false;
    
    private int getDataMode = GET_DATA_MODE_NETWORK;
    private String firstItemId = "";
    private int localPosition = 0;
    
    private boolean isFromPullList = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prog_comment);

        detector = new GestureDetector(this);

        progressDialog = new ProgressDialog(Prog_Comment.this);
        footerView = View.inflate(Prog_Comment.this, R.layout.listview_loading, null);

        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        access_token = preferences.getString("access_token", "");
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        login_time = preferences.getLong("login_time", 0);
        user_id = preferences.getString("user_id", "");
        
        prog_name = getIntent().getStringExtra("prog_name");

        text_title = (TextView)findViewById(R.id.text_prog_comment_title);
        gallery_bar = (Gallery)findViewById(R.id.prog_comment_gallery_bar);
        list_comment = (PullToRefreshListView)findViewById(R.id.list_prog_comment);
        btn_return = (Button)findViewById(R.id.btn_prog_comment_return);
        btn_new_comment = (Button)findViewById(R.id.btn_prog_comment_new_comment);
        text_more_comment = (TextView)footerView.findViewById(R.id.text_loading_more);

        text_title.setText(prog_name);

        if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
            Utils.DoLogin(Prog_Comment.this, login_username, login_password);
            access_token = preferences.getString("access_token", "");
        }

        textAdapter = new TextAdapter(this);
        gallery_bar.setAdapter(textAdapter);
        gallery_bar.setSelection(1);
        gallery_bar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                for (int i = 0; i < textAdapter.mTextView.length; i++) {
                    textAdapter.mTextView[i].setTextColor(Color.WHITE);
                }
                ((TextView)view).setTextColor(Color.rgb(244, 194, 21));
                String viewText = ((TextView)view).getText().toString();
                if (viewText.equals("全部")) {
                    totalType = TOTALTYPE_ALL;
                } else if (viewText.equals("好友")) {
                    totalType = TOTALTYPE_FRIEND;
                } else if (viewText.equals("所关注")) {
                    totalType = TOTALTYPE_CARE;
                }
                
                if (totalType != TOTALTYPE_ALL) {
                    comment_page = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}}
        );

        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_new_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Prog_Comment.this, New_Comment.class));
            }
        });

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (totalType != lastTotalType) {
                    lastTotalType = totalType;
                    mTempList.clear();
                    sendMsg(DOWNLOAD_COMMENT_EVENT);
                }
            }
        }, 800, 800);

        listItemAdapter = new ListItemAdapter(this);
        list_comment.setAdapter(listItemAdapter);

        text_more_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment_page++;
                sendMsg(DOWNLOAD_COMMENT_EVENT);
            }
        });

        list_comment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int myPosition = position - 2;

                String user_image_url = mCommentList.get(myPosition).get("user_image_url").toString();
                String user_name = mCommentList.get(myPosition).get("user_name").toString();
                String user_id = mCommentList.get(myPosition).get("user_id").toString();
                String comment_text = mCommentList.get(myPosition).get("text").toString();
                String created_at = mCommentList.get(myPosition).get("created_at").toString();
                String source = mCommentList.get(myPosition).get("source").toString();
                String repost_count = mCommentList.get(myPosition).get("reposts_count").toString();
                String reply_count = mCommentList.get(myPosition).get("comments_count").toString();
                String comment_id = mCommentList.get(myPosition).get("comment_id").toString();
                String original_comment_id = mCommentList.get(myPosition).get("original_comment_id").toString();
                String original_text = mCommentList.get(myPosition).get("original_text").toString();
                String original_user_name = mCommentList.get(myPosition).get("original_user_name").toString();
                String original_repost_count = mCommentList.get(myPosition).get("original_reposts_count").toString();
                String original_reply_count = mCommentList.get(myPosition).get("original_comments_count").toString();
                String image_small = mCommentList.get(myPosition).get("image_small").toString();
                String image_middle = mCommentList.get(myPosition).get("image_middle").toString();
                String image_small_original = mCommentList.get(myPosition).get("image_small_original").toString();
                String image_middle_original = mCommentList.get(myPosition).get("image_middle_original").toString();
                Intent intent = new Intent(Prog_Comment.this, Detail_Comment.class);
                intent.putExtra("user_image_url", user_image_url);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_id", user_id);
                intent.putExtra("comment_text", comment_text);
                intent.putExtra("created_at", created_at);
                intent.putExtra("source", source);
                intent.putExtra("repost_count", repost_count);
                intent.putExtra("reply_count", reply_count);
                intent.putExtra("comment_id", comment_id);
                intent.putExtra("original_comment_id", original_comment_id);
                intent.putExtra("original_text", original_text);
                intent.putExtra("original_user_name", original_user_name);
                intent.putExtra("original_repost_count", original_repost_count);
                intent.putExtra("original_reply_count", original_reply_count);
                intent.putExtra("image_small", image_small);
                intent.putExtra("image_middle", image_middle);
                intent.putExtra("image_small_original", image_small_original);
                intent.putExtra("image_middle_original", image_middle_original);
                startActivity(intent);
            }
        });

        list_comment.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                String filename = "";
                ArrayList<Map<String, Object>> storeList = null;
                if (totalType == TOTALTYPE_ALL) {
                    storeList = Utils.GetCommentAllListFromStored(Prog_Comment.this);
                } else if (totalType == TOTALTYPE_FRIEND || totalType == TOTALTYPE_CARE) {
                    storeList = Utils.GetCommentOtherListFromStored(Prog_Comment.this);
                }

                for (int i = 0; i < storeList.size(); i++) {
                    if (storeList.get(i).get("keyword").toString().equals(prog_name)) {
                        String indexString = storeList.get(i).get("index").toString();
                        if (totalType == TOTALTYPE_ALL) {
                            filename = Utils.store_path_Comment + indexString + POSTFIX_TYPE1;
                        } else if (totalType == TOTALTYPE_FRIEND) {
                            filename = Utils.store_path_Comment + indexString + POSTFIX_TYPE2;
                        } else if (totalType == TOTALTYPE_CARE) {
                            filename = Utils.store_path_Comment + indexString + POSTFIX_TYPE3;
                        }
                        File file = new File(filename);
                        if (file.exists()) {
                            file.delete();
                        }
                        break;
                    }
                }
                
                comment_page = 1;
                isFromPullList = true;
                DownloadComment();
            }
        });
    }

    void DownloadComment() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.setMessage("正在获取评论信息...");  
        progressDialog.setIndeterminate(false);  
        progressDialog.setCancelable(false);  
        progressDialog.show();  
        
        new Thread() {
            public void run() {
                if (totalType == TOTALTYPE_ALL) {
                    GetAllCommentData();
                } else if (totalType == TOTALTYPE_FRIEND || totalType == TOTALTYPE_CARE) {
                    GetOtherCommentData();
                }
                sendMsg(UPDATE_LIST_EVENT);
                
                if (isFromPullList == true) {
                    isFromPullList = false;
                    sendMsg(REFRESH_LIST_EVENT);
                }
            }
        }.start();
    }

    class TextAdapter extends BaseAdapter {
        private Context mContext;
        private String class_string[] = {"好友", "全部", "所关注"};
        public TextView[] mTextView = new TextView[class_string.length];

        public TextAdapter(Context context) {
            this.mContext = context;
            for (int i = 0; i < class_string.length; i++) {
                mTextView[i] = new TextView(mContext);
                mTextView[i].setText(class_string[i].toString());
                mTextView[i].setTextSize(20);
                mTextView[i].setLayoutParams(new Gallery.LayoutParams(150, 40));
                mTextView[i].setGravity(Gravity.CENTER);
            }
        }

        @Override
        public int getCount() {
            return mTextView.length;
        }

        @Override
        public Object getItem(int position) {return null;}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mTextView[position];
        }
    }

    void GetAllCommentData() {
        boolean isStoreListFull = false;
        String indexString = "";
        String filename = "";
        Calendar calendar = Calendar.getInstance();
        String indexStringToStore = sf.format(calendar.getTime());
        mAllStoreList = Utils.GetCommentAllListFromStored(Prog_Comment.this);
        String savedPage = "1";
        int savedPageInt = 1;
        
        if (isFromPullList == true) {
            mTempList.clear();
        }
        
        try {
            getDataMode = GET_DATA_MODE_NETWORK;
            if (mAllStoreList != null) {
                if (mAllStoreList.size() == Utils.MAX_CACHE_COMMENT_COUNT) {
                    isStoreListFull = true;
                }
                for (int i = 0; i < mAllStoreList.size(); i++) {
                    if (mAllStoreList.get(i).get("keyword").toString().equals(prog_name)) {
                        indexString = mAllStoreList.get(i).get("index").toString();
                        savedPage = mAllStoreList.get(i).get("page").toString();
                        savedPageInt = Integer.parseInt(savedPage);
                        if (savedPageInt >= comment_page) {
                            if (isFromPullList == false) {
                                comment_page = Integer.parseInt(savedPage);
                            }
                            filename = Utils.store_path_Comment + indexString + POSTFIX_TYPE1;
                            File file = new File(filename);
                            if (file.exists()) {
                                getDataMode = GET_DATA_MODE_STORE;
                            }
                        }
                        break;
                    }
                }
            } else {
                mAllStoreList = new ArrayList<Map<String, Object>>();
            }

            File folder1 = new File(Utils.store_path);
            if (!folder1.exists()) {
                folder1.mkdir();
            }

            File folder2 = new File(Utils.store_path_Comment);
            if (!folder2.exists()) {
                folder2.mkdir();
            }
            
            if (getDataMode == GET_DATA_MODE_NETWORK) {
                String keyword = URLEncoder.encode(prog_name, "UTF-8");
                String urlString = "http://shenkantv.sinaapp.com/weibo_phpsdk/getProgram.php?token=" + access_token + "&key=" + keyword + "&page=" + String.valueOf(comment_page);
        
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = new DefaultHttpClient().execute(request);
        
                if (response.getStatusLine().getStatusCode() == 200) {
                    dataString = EntityUtils.toString(response.getEntity(), "UTF-8");
                }
            } else if (getDataMode == GET_DATA_MODE_STORE) {
                FileInputStream inStream = new FileInputStream(filename);
                byte[] buffer = new byte[inStream.available()];
                inStream.read(buffer);
                dataString = new String(buffer);
            }
            
            String text = "";
            String created_at = "";
            String comment_id = "";
            int reposts_count = 0;
            int comments_count = 0;
            String user_image_url = "";
            String user_name = "";
            String source = "";
            String user_id = "";
            JSONObject jsonTotalObject = new JSONObject(dataString);

            if (!dataString.substring(0, EMPTY_RESPONSE_STRING.length()).equals(EMPTY_RESPONSE_STRING)) {
                sendMsg(ADD_FOOTER_EVENT);
            } else {
                sendMsg(REMOVE_FOOTER_EVENT);
            }

            JSONArray jsonArray = jsonTotalObject.getJSONArray("statuses");
            for (int j = 0; j < jsonArray.length(); j++) {
                String original_text = "";
                String original_user_name = "";
                int original_reposts_count = 0;
                int original_comments_count = 0;
                String image_small = "";
                String image_middle = "";
                String image_small_original = "";
                String image_middle_original = "";
                String original_comment_id = "";
                JSONObject jsonObjectOriginal = null;
                
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                text = jsonObject.getString("text");
                created_at = jsonObject.getString("created_at");
                comment_id = jsonObject.getString("id");
                reposts_count = jsonObject.getInt("reposts_count");
                comments_count = jsonObject.getInt("comments_count");
                source = jsonObject.getString("source");

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
                    original_comment_id = jsonObjectOriginal.getString("id");
                    original_reposts_count = jsonObjectOriginal.getInt("reposts_count");
                    original_comments_count = jsonObjectOriginal.getInt("comments_count");
                    if (jsonObjectOriginal.has("thumbnail_pic") == true) {
                        image_small_original = jsonObjectOriginal.getString("thumbnail_pic");
                        image_middle_original = jsonObjectOriginal.getString("bmiddle_pic");
                    }
                    JSONObject jsonObjectOriginalUser = jsonObjectOriginal.getJSONObject("user");
                    original_user_name = jsonObjectOriginalUser.getString("screen_name");
                }

                Map<String, Object> item = new HashMap<String, Object>();
                item.put("text", text);
                item.put("created_at", created_at);
                item.put("comment_id", comment_id);
                item.put("reposts_count", reposts_count);
                item.put("comments_count", comments_count);
                item.put("user_image_url", user_image_url);
                item.put("user_name", user_name);
                item.put("source", source);
                item.put("user_id", user_id);
                item.put("original_text", original_text);
                item.put("original_user_name", original_user_name);
                item.put("image_small", image_small);
                item.put("image_middle", image_middle);
                item.put("image_small_original", image_small_original);
                item.put("image_middle_original", image_middle_original);
                item.put("original_comment_id", original_comment_id);
                item.put("original_reposts_count", original_reposts_count);
                item.put("original_comments_count", original_comments_count);

                mTempList.add(item);

                if (firstItemId.equals("")) {
                    firstItemId = comment_id;
                }
            }
            
            if (getDataMode == GET_DATA_MODE_NETWORK) {
                boolean isShouldAddNew = true;
                String storeFilename;

                for (int i = 0; i < mAllStoreList.size(); i++) {
                    if (mAllStoreList.get(i).get("keyword").toString().equals(prog_name)) {
                        mAllStoreList.get(i).put("page", String.valueOf(comment_page));
                        isShouldAddNew = false;
                        break;
                    }
                }

                if (isShouldAddNew == true) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("keyword", prog_name);
                    item.put("index", indexStringToStore);
                    item.put("page", String.valueOf(comment_page));
                    mAllStoreList.add(item);
                }

                if (isShouldAddNew == true) {
                    storeFilename = Utils.store_path_Comment + indexStringToStore + POSTFIX_TYPE1;
                } else {
                    storeFilename = Utils.store_path_Comment + indexString + POSTFIX_TYPE1;
                }
                Utils.SaveCommentConentListToJsonFile(mTempList, storeFilename);

                if (isStoreListFull == true) {
                    indexString = mAllStoreList.get(0).get("index").toString();
                    if (isShouldAddNew == false) {
                        mAllStoreList.remove(0);
                    }
                    String filenameDelete = Utils.store_path_Comment + indexString + POSTFIX_TYPE1;
                    File fileDelete = new File(filenameDelete);
                    if (fileDelete.exists()) {
                        fileDelete.delete();
                    }
                }

                Utils.SaveCommentAllListToStored(Prog_Comment.this, mAllStoreList);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

    void GetOtherCommentData() {
        boolean isStoreListFull = false;
        String indexString = "";
        String filename = "";
        Calendar calendar = Calendar.getInstance();
        String indexStringToStore = sf.format(calendar.getTime());
        mOtherStoreList = Utils.GetCommentOtherListFromStored(Prog_Comment.this);
        boolean isExistentInList = false;

        if (isFromPullList == true) {
            mTempList.clear();
        }
        
        try {
            getDataMode = GET_DATA_MODE_NETWORK;
            if (mOtherStoreList != null) {
                if (mOtherStoreList.size() == Utils.MAX_CACHE_COMMENT_COUNT) {
                    isStoreListFull = true;
                }
                for (int i = 0; i < mOtherStoreList.size(); i++) {
                    if (mOtherStoreList.get(i).get("keyword").toString().equals(prog_name)) {
                        indexString = mOtherStoreList.get(i).get("index").toString();
                        if (totalType == TOTALTYPE_FRIEND) {
                            filename = Utils.store_path_Comment + indexString + POSTFIX_TYPE2;
                        } else if (totalType == TOTALTYPE_CARE) {
                            filename = Utils.store_path_Comment + indexString + POSTFIX_TYPE3;
                        }
                        isExistentInList = true;
                        File file = new File(filename);
                        if (file.exists()) {
                            getDataMode = GET_DATA_MODE_STORE;
                        }
                        break;
                    }
                }
            } else {
                mOtherStoreList = new ArrayList<Map<String, Object>>();
            }

            File folder1 = new File(Utils.store_path);
            if (!folder1.exists()) {
                folder1.mkdir();
            }

            File folder2 = new File(Utils.store_path_Comment);
            if (!folder2.exists()) {
                folder2.mkdir();
            }

            String keyword = URLEncoder.encode(prog_name, "UTF-8");
            String urlString = "";
            
            sendMsg(REMOVE_FOOTER_EVENT);

            if (getDataMode == GET_DATA_MODE_NETWORK) {
                if (totalType == TOTALTYPE_FRIEND) {
                    urlString = "http://shenkantv.sinaapp.com/weibo_phpsdk/getFriends.php?token=" + access_token + "&uid=" + user_id + "&key=" + keyword;
                } else if (totalType == TOTALTYPE_CARE) {
                    urlString = "http://shenkantv.sinaapp.com/weibo_phpsdk/getCares.php?token=" + access_token + "&uid=" + user_id + "&key=" + keyword;
                }
        
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = new DefaultHttpClient().execute(request);
        
                if (response.getStatusLine().getStatusCode() == 200) {
                    dataString = EntityUtils.toString(response.getEntity(), "UTF-8");
                }
            } else if (getDataMode == GET_DATA_MODE_STORE) {
                FileInputStream inStream = new FileInputStream(filename);
                byte[] buffer = new byte[inStream.available()];
                inStream.read(buffer);
                dataString = new String(buffer);
            }

            if (dataString.length() < 30) {
                Toast.makeText(Prog_Comment.this, "抱歉，没有匹配的微博!", Toast.LENGTH_LONG).show();
            }

            String text = "";
            String created_at = "";
            String comment_id = "";
            int reposts_count = 0;
            int comments_count = 0;
            String user_image_url = "";
            String user_name = "";
            String source = "";
            String user_id = "";
            JSONArray jsonArray = new JSONArray(dataString);
            for (int j = 0; j < jsonArray.length(); j++) {
                String original_text = "";
                String original_user_name = "";
                int original_reposts_count = 0;
                int original_comments_count = 0;
                String image_small = "";
                String image_middle = "";
                String image_small_original = "";
                String image_middle_original = "";
                String original_comment_id = "";
                JSONObject jsonObjectOriginal = null;

                JSONObject jsonObject = jsonArray.getJSONObject(j);
                text = jsonObject.getString("text");
                created_at = jsonObject.getString("created_at");
                comment_id = jsonObject.getString("id");
                reposts_count = jsonObject.getInt("reposts_count");
                comments_count = jsonObject.getInt("comments_count");
                source = jsonObject.getString("source");
    
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
                    original_comment_id = jsonObjectOriginal.getString("id");
                    original_reposts_count = jsonObjectOriginal.getInt("reposts_count");
                    original_comments_count = jsonObjectOriginal.getInt("comments_count");
                    if (jsonObjectOriginal.has("thumbnail_pic") == true) {
                        image_small_original = jsonObjectOriginal.getString("thumbnail_pic");
                        image_middle_original = jsonObjectOriginal.getString("bmiddle_pic");
                    }
                    JSONObject jsonObjectOriginalUser = jsonObjectOriginal.getJSONObject("user");
                    original_user_name = jsonObjectOriginalUser.getString("screen_name");
                }

                Map<String, Object> item = new HashMap<String, Object>();
                item.put("text", text);
                item.put("created_at", created_at);
                item.put("comment_id", comment_id);
                item.put("reposts_count", reposts_count);
                item.put("comments_count", comments_count);
                item.put("user_image_url", user_image_url);
                item.put("user_name", user_name);
                item.put("source", source);
                item.put("user_id", user_id);
                item.put("original_text", original_text);
                item.put("original_user_name", original_user_name);
                item.put("image_small", image_small);
                item.put("image_middle", image_middle);
                item.put("image_small_original", image_small_original);
                item.put("image_middle_original", image_middle_original);
                item.put("original_comment_id", original_comment_id);
                item.put("original_reposts_count", original_reposts_count);
                item.put("original_comments_count", original_comments_count);

                mTempList.add(item);
            }
            
            if (getDataMode == GET_DATA_MODE_NETWORK) {
                if (isExistentInList == false) {
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("keyword", prog_name);
                    item.put("index", indexStringToStore);
                    mOtherStoreList.add(item);
                    Utils.SaveCommentOtherListToStored(Prog_Comment.this, mOtherStoreList);
                }

                if (isStoreListFull == true) {
                    indexString = mOtherStoreList.get(0).get("index").toString();
                    mOtherStoreList.remove(0);
                    String filenameDelete = Utils.store_path_Comment + indexString + POSTFIX_TYPE1;
                    File fileDelete = new File(filenameDelete);
                    if (fileDelete.exists()) {
                        fileDelete.delete();
                    }
                }

                String storeFilename = "";
                if (totalType == TOTALTYPE_FRIEND) {
                    storeFilename = Utils.store_path_Comment + indexStringToStore + POSTFIX_TYPE2;
                } else if (totalType == TOTALTYPE_CARE) {
                    storeFilename = Utils.store_path_Comment + indexStringToStore + POSTFIX_TYPE3;
                }

                Utils.SaveCommentConentListToJsonFile(mTempList, storeFilename);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
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
                    mCommentList = (ArrayList<Map<String, Object>>) mTempList.clone();
                    listItemAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    if (list_comment.getFirstVisiblePosition() == 0) {
                        list_comment.setSelection(1);
                    }
                    break;
                case REFRESH_LIST_EVENT:
                    list_comment.onRefreshComplete();
                    break;
                case ADD_FOOTER_EVENT:
                    if (isAddedFooterView == false) {
                        list_comment.addFooterView(footerView);
                        list_comment.setAdapter(listItemAdapter);
                        isAddedFooterView = true;
                    }
                    break;
                case REMOVE_FOOTER_EVENT:
                    if (isAddedFooterView == true) {
                        list_comment.removeFooterView(footerView);
                        isAddedFooterView = false;
                    }
                    break;
                case DOWNLOAD_COMMENT_EVENT:
                    DownloadComment();
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };
    
    class ListItemAdapter extends BaseAdapter {
        private Context mContext;

        public ListItemAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mCommentList.size() + 1;
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
            TextView text_comment_created_at = null;
            TextView text_comment_count = null;
            TextView text_original_count = null;
            LinearLayout layout_original_text = null;
            TextView text_original_text = null;
            ImageView image_small_image = null;
            ImageView image_small_image_original = null;
            String user_image_url = "";
            String reposts_count = "";
            String comments_count = "";
            String created_at = "";
            String original_text = "";
            String original_user_name = "";
            String original_reposts_count = "";
            String original_comments_count = "";
            String image_small = "";
            String image_small_original = "";

            if (position == 0) {
                View myview = new View(Prog_Comment.this);
                return myview;
            }

            localPosition = position - 1;

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.prog_comment_item, null);

            image_user_image = (ImageView)view.findViewById(R.id.image_prog_comment_item_user_image);
            text_comment_text = (TextView)view.findViewById(R.id.text_prog_comment_item_text);
            text_comment_user_name = (TextView)view.findViewById(R.id.text_prog_comment_item_user_name);
            text_comment_created_at = (TextView)view.findViewById(R.id.text_prog_comment_created_at);
            text_comment_count = (TextView)view.findViewById(R.id.text_prog_comment_count);
            layout_original_text = (LinearLayout)view.findViewById(R.id.linear_layout_prog_comment_original_text);
            text_original_text = (TextView)view.findViewById(R.id.text_prog_comment_original_text);
            image_small_image = (ImageView)view.findViewById(R.id.image_prog_comment_item_image_small);
            image_small_image_original = (ImageView)view.findViewById(R.id.image_prog_comment_item_original_image_small);
            text_original_count = (TextView)view.findViewById(R.id.text_prog_comment_original_count);
            
            user_image_url = mCommentList.get(localPosition).get("user_image_url").toString();
            reposts_count = mCommentList.get(localPosition).get("reposts_count").toString();
            comments_count = mCommentList.get(localPosition).get("comments_count").toString();
            created_at = mCommentList.get(localPosition).get("created_at").toString();

            text_comment_text.setText(Utils.ProcessStringWithURL(mCommentList.get(localPosition).get("text").toString()));
            text_comment_user_name.setText(mCommentList.get(localPosition).get("user_name").toString());
            text_comment_count.setText("转发:" + reposts_count + " | 评论:" + comments_count);
            text_comment_created_at.setText(Utils.GetFormattedStringFromCreateAtString(created_at));

            try {
                BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user_image);
                bitmapTask.execute();
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }

            image_user_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int myPosition = position - 1;

                    Intent intent = new Intent(Prog_Comment.this, User_Detail.class);
                    intent.putExtra("user_name", (String)mCommentList.get(myPosition).get("user_name"));
                    intent.putExtra("user_id", (String)mCommentList.get(myPosition).get("user_id"));
                    startActivity(intent);
                }
            });
            
            original_text = mCommentList.get(localPosition).get("original_text").toString();
            original_user_name = mCommentList.get(localPosition).get("original_user_name").toString();
            original_reposts_count = mCommentList.get(localPosition).get("original_reposts_count").toString();
            original_comments_count = mCommentList.get(localPosition).get("original_comments_count").toString();
            if (original_text.equals("")) {
                text_original_text.setTextSize(1);
                text_original_count.setTextSize(1);
                layout_original_text.setVisibility(View.INVISIBLE);
            } else {
                layout_original_text.setBackgroundResource(R.drawable.original_text_border);
                text_original_text.setText(Utils.GetOriginalTextString(original_user_name, original_text));
                text_original_count.setText("转发:" + original_reposts_count + " | 评论:" + original_comments_count);
            }

            image_small = (String)mCommentList.get(localPosition).get("image_small");
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

                    final Dialog dialog = new Dialog(Prog_Comment.this);
                    ImageView view = new ImageView(Prog_Comment.this);
                    try {
                        BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(mCommentList.get(myPosition).get("image_middle").toString(), view);
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
            
            image_small_original = (String)mCommentList.get(localPosition).get("image_small_original");
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
                    final Dialog dialog = new Dialog(Prog_Comment.this);
                    ImageView view = new ImageView(Prog_Comment.this);
                    try {
                        BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(mCommentList.get(myPosition).get("image_middle_original").toString(), view);
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

    @Override 
    public boolean onTouchEvent(MotionEvent event) {
        return this.detector.onTouchEvent(event);
    }
    
    @Override
    public boolean onDown(MotionEvent e) {return false;}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > 200 && Math.abs(velocityX) > 200) {   
            if (gallery_bar.getSelectedItemPosition() != gallery_bar.getCount() - 1) {
                gallery_bar.setSelection(gallery_bar.getSelectedItemPosition() + 1);
                isFlapping = true;
            }
        } else if (e2.getX() - e1.getX() > 200 && Math.abs(velocityX) > 200) {   
            if (gallery_bar.getSelectedItemPosition() != 0) {
                gallery_bar.setSelection(gallery_bar.getSelectedItemPosition() - 1);
                isFlapping = true;
            }
        }   

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {return false;}
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        if (isFlapping == false) {
            super.dispatchTouchEvent(ev);
        }
        isFlapping = false;
        
        return true;
    }
}