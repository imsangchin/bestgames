package cn.shidian;

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
import cn.shidian.PullToRefreshListView.OnRefreshListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Prog_Hot extends Activity implements OnGestureListener {
    public static final int UPDATE_LIST_EVENT = 0;
    public static final int DOWNLOAD_HOT_PROG_EVENT = 1;
    public static final int REFRESH_LIST_EVENT = 2;

    public static final int PROGTYPE_YINGSHI = 0;
    public static final int PROGTYPE_XINWEN = 1;
    public static final int PROGTYPE_SHAOER = 2;
    public static final int PROGTYPE_SHEHUI = 3;
    public static final int PROGTYPE_CAIJING = 4;
    public static final int PROGTYPE_TIYU = 5;
    public static final int PROGTYPE_HOT = 6;
    public static final int PROGTYPE_ZONGYI = 7;
    public static final int PROGTYPE_ALL = 8;
    
    public static final String TITLE_STRING = "日热点 ▼";
    public static final int TODAY = 0;
    public static final int TOMORROW = 1;
    public static final int DAYAFTERTOMORROW = 2;

    private int dateNumber = TODAY;
    private int lastDateNumber = -1;
    private int progtype = PROGTYPE_ALL;
    private int lastProgtype = -1;
    private Gallery gallery_bar;
    private TextAdapter textAdapter;
    private PullToRefreshListView list_ProgHot;
    private Button btn_Setting;
    private ImageView image_user;
    private String user_image_url; 
    private TextView text_title;
    private TextView text_today_hot;
    private TextView text_tomorrow_hot;
    private TextView text_day_after_tomorrow_hot;
    private PopupWindow m_popupWindow;
    private LayoutInflater layoutInflater;
    private View popupWindowView;
    private boolean isPopupWindowShow = false;
    private Calendar calendar = null;
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<Map<String, Object>> mProgList = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> mTempList = new ArrayList<Map<String, Object>>();
    private String todayString = "";
    private String tomorrowString = "";
    private String dayAfterTomorrowString = "";
    private String gotoDate = "";
    private ProgressDialog progressDialog;
    private ListItemAdapter listItemAdapter;
    private GestureDetector detector;  
    private boolean isFlapping = false;

    private Timer timer = new Timer();
    private int localPosition = 0;
    private String resultString[] = {"", "'"};
    private String todayResultString[] = {"", ""};
    private String tomorrowResultString[] = {"", ""};
    private String dayAfterTomorrowResultString[] = {"", ""};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prog_hot);

        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        user_image_url = preferences.getString("user_image_url", "");
        
        detector = new GestureDetector(this);

        progressDialog = new ProgressDialog(Prog_Hot.this);
        
        calendar = Calendar.getInstance();
        todayString = sf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        tomorrowString = sf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        dayAfterTomorrowString = sf.format(calendar.getTime());

        layoutInflater = getLayoutInflater();
        popupWindowView = layoutInflater.inflate(R.layout.prog_hot_popup_menu, null);
        m_popupWindow = new PopupWindow(popupWindowView, 150, 150);

        todayResultString[0] = "";
        todayResultString[1] = "";
        tomorrowResultString[0] = "";
        tomorrowResultString[1] = "";
        dayAfterTomorrowResultString[0] = "";
        dayAfterTomorrowResultString[1] = "";
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (progtype != lastProgtype || dateNumber != lastDateNumber) {
                    lastProgtype = progtype;
                    lastDateNumber = dateNumber;
                    sendMsg(DOWNLOAD_HOT_PROG_EVENT);
                }
            }
        }, 800, 800);

        //取回每个控件
        gallery_bar = (Gallery)findViewById(R.id.prog_hot_gallery_bar);
        list_ProgHot = (PullToRefreshListView)findViewById(R.id.list_prog_hot);
        text_title = (TextView)findViewById(R.id.text_title_prog_hot);
        text_today_hot = (TextView)popupWindowView.findViewById(R.id.text_today_prog_hot);
        text_tomorrow_hot = (TextView)popupWindowView.findViewById(R.id.text_tomorrow_prog_hot);
        text_day_after_tomorrow_hot = (TextView)popupWindowView.findViewById(R.id.text_day_after_tomorrow_prog_hot);
        btn_Setting = (Button)findViewById(R.id.prog_hot_btn_setting);
        image_user = (ImageView)findViewById(R.id.image_prog_hot_user);
        
        text_title.setText("今" + TITLE_STRING);

        image_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Prog_Hot.this, User_Login_After.class);
                intent.putExtra("from_user_image", "yes");
                intent.putExtra("from_where", "prog_hot");
                startActivity(intent);
                finish();
            }
        });
        
        btn_Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Prog_Hot.this, Software_Setting.class));
            }
        });

        if (user_image_url.equals("")) {
            image_user.setBackgroundResource(R.drawable.unknown_user);
        } else {
            try {
                BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user);
                bitmapTask.execute();
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        
        //设置转轮
        textAdapter = new TextAdapter(this);
        gallery_bar.setAdapter(textAdapter);
        gallery_bar.setSelection(4);
        gallery_bar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                for (int i = 0; i < textAdapter.mTextView.length; i++) {
                    textAdapter.mTextView[i].setTextColor(Color.WHITE);
                }
                ((TextView)arg1).setTextColor(Color.rgb(244, 194, 21));
                String viewText = ((TextView)arg1).getText().toString();
                if (viewText.equals("影视")) {
                    progtype = PROGTYPE_YINGSHI;
                } else if (viewText.equals("新闻")) {
                    progtype = PROGTYPE_XINWEN;
                } else if (viewText.equals("少儿")) {
                    progtype = PROGTYPE_SHAOER;
                } else if (viewText.equals("社会")) {
                    progtype = PROGTYPE_SHEHUI;
                } else if (viewText.equals("综艺")) {
                    progtype = PROGTYPE_ZONGYI;
                } else if (viewText.equals("财经")) {
                    progtype = PROGTYPE_CAIJING;
                } else if (viewText.equals("体育")) {
                    progtype = PROGTYPE_TIYU;
                } else if (viewText.equals("全部")) {
                    progtype = PROGTYPE_ALL;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}}
        );

        listItemAdapter = new ListItemAdapter(Prog_Hot.this);
        list_ProgHot.setAdapter(listItemAdapter);
        
        text_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopupWindowShow == false) {
                    m_popupWindow.showAsDropDown(text_title);
                    isPopupWindowShow = true;
                } else {
                    m_popupWindow.dismiss();
                    isPopupWindowShow = false;
                }
            }
        });

        text_today_hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("今" + TITLE_STRING);
                dateNumber = TODAY;
            }
        });

        text_tomorrow_hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("明" + TITLE_STRING);
                dateNumber = TOMORROW;
            }
        });

        text_day_after_tomorrow_hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("后" + TITLE_STRING);
                dateNumber = DAYAFTERTOMORROW;
            }
        });

        list_ProgHot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int myPosition = position - 2;
                
                final String station_name = (String) mProgList.get(myPosition).get("station_name");
                final String subStation_name = (String) mProgList.get(myPosition).get("subStation_name");
                final String prog_name = (String) mProgList.get(myPosition).get("prog_name");
                final String start_time = (String) mProgList.get(myPosition).get("start_time");

                Calendar calendar = Calendar.getInstance();
                long current_time = calendar.getTimeInMillis();

                SharedPreferences preferences = getSharedPreferences("Shidian", 0);
                String login_username = preferences.getString("login_username", "");
                String login_password = preferences.getString("login_password", "");
                String access_token = preferences.getString("access_token", "");
                long login_time = preferences.getLong("login_time", 0);

                if (login_username.equals("")
                        || login_password.equals("")
                        || access_token.equals("")
                        || login_time == 0) {
                    AlertDialog.Builder builder = new Builder(Prog_Hot.this);
                    builder.setMessage("此操作需要使用新浪微博登录，是否登录？");
                    builder.setTitle("提示");

                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Prog_Hot.this, User_Login_Before.class);
                            intent.putExtra("station_name", station_name);
                            intent.putExtra("subStation_name", subStation_name);
                            intent.putExtra("prog_name", prog_name);
                            intent.putExtra("start_time", start_time);
                            intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                            intent.putExtra("activity", Utils.INTENT_PROG_TABHOST);
                            startActivity(intent);
                        }
                    });
                    
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    
                    builder.create().show();
                } else if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
                    Utils.DoLogin(Prog_Hot.this, login_username, login_password);
                    Intent intent = new Intent(Prog_Hot.this, Prog_TabHost.class);
                    intent.putExtra("station_name", station_name);
                    intent.putExtra("subStation_name", subStation_name);
                    intent.putExtra("prog_name", prog_name);
                    intent.putExtra("start_time", start_time);
                    intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Prog_Hot.this, Prog_TabHost.class);
                    intent.putExtra("station_name", station_name);
                    intent.putExtra("subStation_name", subStation_name);
                    intent.putExtra("prog_name", prog_name);
                    intent.putExtra("start_time", start_time);
                    intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                    startActivity(intent);
                }
            }
        });

        list_ProgHot.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                todayResultString[0] = "";
                todayResultString[1] = "";
                tomorrowResultString[0] = "";
                tomorrowResultString[1] = "";
                dayAfterTomorrowResultString[0] = "";
                dayAfterTomorrowResultString[1] = "";
                DownloadHotProg();
                sendMsg(REFRESH_LIST_EVENT);
            }
        });
    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        user_image_url = preferences.getString("user_image_url", "");
        
        if (user_image_url.equals("")) {
            image_user.setBackgroundResource(R.drawable.unknown_user);
        } else {
            try {
                BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user);
                bitmapTask.execute();
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isPopupWindowShow == true) {
            m_popupWindow.dismiss();
            isPopupWindowShow = false;
        }
        super.onPause();
    }

    void DownloadHotProg() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.setMessage("正在获取节目信息...");  
        progressDialog.setIndeterminate(false);  
        progressDialog.setCancelable(false);  
        progressDialog.show();  
        
        new Thread() {
            public void run() {
                GetHotProg();
                sendMsg(UPDATE_LIST_EVENT);
            }
        }.start();
    }
    
    class TextAdapter extends BaseAdapter {
        private Context mContext;
        private String class_string[] = {
                "体育", "财经", "社会", "综艺",
                "全部", "影视", "新闻", "少儿"};
        public TextView[] mTextView = new TextView[class_string.length];

        public TextAdapter(Context context) {
            this.mContext = context;
            for (int i = 0; i < class_string.length; i++) {
                mTextView[i] = new TextView(mContext);
                mTextView[i].setText(class_string[i].toString());
                mTextView[i].setTextSize(20);
                mTextView[i].setLayoutParams(new Gallery.LayoutParams(100, 40));
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

    void GetHotProg() {
        mTempList.clear();
        
        switch (dateNumber) {
        case TODAY:
            gotoDate = todayString;
            resultString = todayResultString;
            break;
        case TOMORROW:
            gotoDate = tomorrowString;
            resultString = tomorrowResultString;
            break;
        case DAYAFTERTOMORROW:
            gotoDate = dayAfterTomorrowString;
            resultString = dayAfterTomorrowResultString;
            break;
        default:
            gotoDate = todayString;
            resultString = todayResultString;
            break;
        }
        
        String beginString = gotoDate + "%2000:00:00";
        String endString = gotoDate + "%2023:59:59";
        
        String urlString[] = {
            "http://shenkantv.sinaapp.com/epginter/remarkedEpg.php",
            "http://shenkantv.sinaapp.com/epginter/epgSelf.php?begin=" + beginString + "&end=" + endString
        };
        
        for (int j = 0; j < urlString.length; j++) {
            try {
                if (resultString[j].equals("")) {
                    HttpGet request = new HttpGet(urlString[j]);
                    HttpResponse response = new DefaultHttpClient().execute(request);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        resultString[j] = EntityUtils.toString(response.getEntity(), "UTF-8");
                    }
                }
    
                String station_name = "";
                String subStation_name = "";
                String prog_name = "";
                String start_time = "";
                String url = "";
                String tag = "";
                boolean isHotProg = false;
                
                JSONArray jsonArray = new JSONArray(resultString[j]);
                for (int i = 0; i < jsonArray.length(); i++) {
                    int tagNumber = 0;
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    station_name = jsonObject.getString("station");
                    subStation_name = jsonObject.getString("subStation");
                    prog_name = jsonObject.getString("program");
                    start_time = jsonObject.getString("playTime");
                    if (jsonObject.has("url")) {
                        url = jsonObject.getString("url");
                    }
                    tag = jsonObject.getString("tag");
                    if (!tag.equals("")) {
                        tagNumber = Integer.parseInt(tag);
                    }
                    
                    if (((tagNumber >> PROGTYPE_HOT) & 0x01) == 1) {
                        isHotProg = true;
                    } else {
                        isHotProg = false;
                    }
                    
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("station_name", station_name);
                    item.put("subStation_name", subStation_name);
                    item.put("prog_name", prog_name);
                    item.put("start_time", start_time);
                    item.put("url", url);
                    item.put("tag", tag);
                    item.put("hot", isHotProg);
    
                    if (start_time.substring(0, 10).equals(gotoDate) && (!prog_name.equals(""))) {
                        if (progtype == PROGTYPE_ALL || ((tagNumber >> progtype) & 0x01) == 1) {
                            mTempList.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ListItemAdapter extends BaseAdapter {
        private Context mContext = null;
        ListItemAdapter(Context context) {
            mContext = context;
        }
        @Override
        public int getCount() {
            return mProgList.size() + 1;
        }
        @Override
        public Object getItem(int position) {return null;}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image_station_ico = null;
            TextView text_station_name = null;
            TextView text_start_time = null;
            TextView text_prog_name = null;
            TextView text_url = null;
            ImageView image_hot_prog = null;
            String station_name = "";
            String subStation_name = "";
            String start_time = "";
            String url = "";
            boolean isHotProg = false;

            if (position == 0) {
                View myview = new View(Prog_Hot.this);
                return myview;
            }
            localPosition = position - 1;

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.prog_list_item_full, null);

            image_station_ico = (ImageView)view.findViewById(R.id.image_prog_tvstation_ico);
            text_station_name = (TextView)view.findViewById(R.id.text_prog_tvstation_name);
            text_start_time = (TextView)view.findViewById(R.id.text_prog_start_time);
            text_prog_name = (TextView)view.findViewById(R.id.text_prog_prog_name);
            text_url = (TextView)view.findViewById(R.id.text_prog_url);
            image_hot_prog = (ImageView)view.findViewById(R.id.image_prog_hot);

            text_station_name.setText(mProgList.get(localPosition).get("subStation_name").toString());
            text_prog_name.setText(mProgList.get(localPosition).get("prog_name").toString());

            start_time = mProgList.get(localPosition).get("start_time").toString();
            if (!start_time.equals("")) {
                start_time = start_time.substring(11, 16);
            }
            text_start_time.setText(start_time);

            station_name = mProgList.get(localPosition).get("station_name").toString();
            subStation_name = mProgList.get(localPosition).get("subStation_name").toString();
            url = mProgList.get(localPosition).get("url").toString();

            image_station_ico.setImageResource(Utils.GetStationIcoFromStationName(station_name, subStation_name));

            isHotProg = (Boolean)mProgList.get(localPosition).get("hot");
            if (isHotProg == true) {
                image_hot_prog.setVisibility(View.VISIBLE);
            } else {
                image_hot_prog.setVisibility(View.INVISIBLE);
            }

            if (!url.equals("")) {
                text_url.setText(Utils.ProcessStringWithURL(url));
                text_url.setTextSize(15);
                text_url.setMovementMethod(LinkMovementMethod.getInstance());
            }
            
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
                    mProgList = (ArrayList<Map<String, Object>>) mTempList.clone();
                    listItemAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    if (list_ProgHot.getFirstVisiblePosition() == 0) {
                        list_ProgHot.setSelection(1);
                    }
                    break;
                case DOWNLOAD_HOT_PROG_EVENT:
                    DownloadHotProg();
                    break;
                case REFRESH_LIST_EVENT:
                    list_ProgHot.onRefreshComplete();
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };

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