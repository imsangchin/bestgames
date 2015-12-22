package cn.shidian;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Prog_List extends Activity implements OnGestureListener {
    public static final int UPDATE_LIST_EVENT = 0;
    public static final int DOWNLOAD_PROG_LIST_EVENT = 1;
    
    public static final int DAYOFWEEK_SUNDAY = 0;
    public static final int DAYOFWEEK_MONDAY = 1;
    public static final int DAYOFWEEK_TUESDAY = 2;
    public static final int DAYOFWEEK_WEDNESDAY = 3;
    public static final int DAYOFWEEK_THURSDAY = 4;
    public static final int DAYOFWEEK_FRIDAY = 5;
    public static final int DAYOFWEEK_SATURSDAY = 6;
    public static final String URL_PREFIX = "http://shenkantv.sinaapp.com/epgData.php";
    public static final String TITLE_STRING = " ▼";

    private int current_dayofweek = 0;
    private int current_Monday = 0;
    private int current_Tuesday = 0;
    private int current_Wednesday = 0;
    private int current_Thursday = 0;
    private int current_Friday = 0;
    private int current_Satursday = 0;
    private int current_Sunday = 0;
    private Date date = new Date();
    private Calendar calendar = null;

    private Gallery gallery_bar;
    private TextAdapter textAdapter;
    private ListView list_ProgList;
    private TextView text_Monday_1;
    private TextView text_Monday_2;
    private TextView text_Tuesday_1;
    private TextView text_Tuesday_2;
    private TextView text_Wednesday_1;
    private TextView text_Wednesday_2;
    private TextView text_Thursday_1;
    private TextView text_Thursday_2;
    private TextView text_Friday_1;
    private TextView text_Friday_2;
    private TextView text_Satursday_1;
    private TextView text_Satursday_2;
    private TextView text_Sunday_1;
    private TextView text_Sunday_2;

    private ArrayList<Map<String, Object>> mProgList = new ArrayList<Map<String, Object>>();
    private ArrayList<Map<String, Object>> mTempList = new ArrayList<Map<String, Object>>();

    private String epgString = "";
    private String today = "";

    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

    private String gotoDate = "";
    private String lastGotoDate = "";
    private Timer timer = new Timer();

    private PopupWindow m_popupWindow;
    private LayoutInflater layoutInflater;
    private View popupWindowView;
    private boolean isPopupWindowShow = false;
    private TextView text_title;
    private TextView text_local_station;
    private TextView text_cctv_station;
    private TextView text_satellite_station;
    private TextView text_hd_station;
    private TextView text_digital_station;

    private String location;
    private String[][] local_station;
    private String station = "";
    private String lastStation = "";
    private int current_station_type = Utils.STATION_TYPE_LOCAL;

    private GestureDetector detector;  
    private boolean isFlapping = false;
    
    private ProgressDialog progressDialog;

    private ListItemAdapter listItemAdapter = new ListItemAdapter(Prog_List.this);
    
    private Button btn_Setting;
    private ImageView image_user;
    private String user_image_url; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prog_list);

        detector = new GestureDetector(this);
        progressDialog = new ProgressDialog(Prog_List.this);

        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        location = preferences.getString("location", "北京");
        local_station = GetProvinceArray(location);
        user_image_url = preferences.getString("user_image_url", "");

        layoutInflater = getLayoutInflater();
        popupWindowView = layoutInflater.inflate(R.layout.prog_list_popup_menu, null);
        m_popupWindow = new PopupWindow(popupWindowView, 100, 220);

        today = sf.format(date);
        gotoDate = today;
        station = GetStationCode(current_station_type, 0);
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!lastGotoDate.equals(gotoDate) || !lastStation.equals(station)) {
                    lastGotoDate = gotoDate;
                    lastStation = station;
                    sendMsg(DOWNLOAD_PROG_LIST_EVENT);
                }
            }
        }, 800, 800);

        //获取星期
        current_dayofweek = date.getDay();

        //如果当前为星期日，特殊处理
        calendar = Calendar.getInstance();
        if (0 == current_dayofweek) {
            calendar.add(Calendar.DAY_OF_MONTH, -7);
        }

        //获取一个星期的每一天
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        current_Monday = calendar.getTime().getDate();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        current_Tuesday = calendar.getTime().getDate();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        current_Wednesday = calendar.getTime().getDate();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        current_Thursday = calendar.getTime().getDate();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        current_Friday = calendar.getTime().getDate();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        current_Satursday = calendar.getTime().getDate();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        current_Sunday = calendar.getTime().getDate();

        calendar.add(Calendar.DAY_OF_WEEK, -6);

        //取回每个控件
        gallery_bar = (Gallery)findViewById(R.id.prog_list_gallery_bar);
        list_ProgList = (ListView)findViewById(R.id.list_prog_list);
        text_Monday_1 = (TextView)findViewById(R.id.text_prog_list_Monday_1);
        text_Monday_2 = (TextView)findViewById(R.id.text_prog_list_Monday_2);
        text_Tuesday_1 = (TextView)findViewById(R.id.text_prog_list_Tuesday_1);
        text_Tuesday_2 = (TextView)findViewById(R.id.text_prog_list_Tuesday_2);
        text_Wednesday_1 = (TextView)findViewById(R.id.text_prog_list_Wendesday_1);
        text_Wednesday_2 = (TextView)findViewById(R.id.text_prog_list_Wendesday_2);
        text_Thursday_1 = (TextView)findViewById(R.id.text_prog_list_Thursday_1);
        text_Thursday_2 = (TextView)findViewById(R.id.text_prog_list_Thursday_2);
        text_Friday_1 = (TextView)findViewById(R.id.text_prog_list_Friday_1);
        text_Friday_2 = (TextView)findViewById(R.id.text_prog_list_Friday_2);
        text_Satursday_1 = (TextView)findViewById(R.id.text_prog_list_Satursday_1);
        text_Satursday_2 = (TextView)findViewById(R.id.text_prog_list_Satursday_2);
        text_Sunday_1 = (TextView)findViewById(R.id.text_prog_list_Sunday_1);
        text_Sunday_2 = (TextView)findViewById(R.id.text_prog_list_Sunday_2);

        text_title = (TextView)findViewById(R.id.text_title_prog_list);
        text_local_station = (TextView)popupWindowView.findViewById(R.id.text_local_station_prog_list);
        text_cctv_station = (TextView)popupWindowView.findViewById(R.id.text_cctv_station_prog_list);
        text_satellite_station = (TextView)popupWindowView.findViewById(R.id.text_satellite_station_prog_list);
        text_hd_station = (TextView)popupWindowView.findViewById(R.id.text_hd_station_prog_list);
        text_digital_station = (TextView)popupWindowView.findViewById(R.id.text_digital_station_prog_list);

        btn_Setting = (Button)findViewById(R.id.prog_list_btn_setting);
        image_user = (ImageView)findViewById(R.id.image_prog_list_user);
        
        image_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Prog_List.this, User_Login_After.class);
                intent.putExtra("from_user_image", "yes");
                intent.putExtra("from_where", "prog_list");
                startActivity(intent);
                finish();
            }
        });
        
        btn_Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Prog_List.this, Software_Setting.class));
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

        text_local_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("本地台" + TITLE_STRING);
                SetStation(local_station);
                current_station_type = Utils.STATION_TYPE_LOCAL;
            }
        });

        text_cctv_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("央视台" + TITLE_STRING);
                SetStation(Utils.CCTV_STATION);
                current_station_type = Utils.STATION_TYPE_CCTV;
            }
        });

        text_satellite_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("省卫视" + TITLE_STRING);
                SetStation(Utils.SATELLITE_STATION);
                current_station_type = Utils.STATION_TYPE_SATELLATE;
            }
        });

        text_hd_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("高清台" + TITLE_STRING);
                SetStation(Utils.HD_STATION);
                current_station_type = Utils.STATION_TYPE_HD;
            }
        });

        text_digital_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupWindow.dismiss();
                isPopupWindowShow = false;
                text_title.setText("数字台" + TITLE_STRING);
                SetStation(Utils.DIGITAL_STATION);
                current_station_type = Utils.STATION_TYPE_DIGITAL;
            }
        });

        //设置每天的日期
        resetAllDate();
        setActiveDate(current_dayofweek);

        //设置轮子
        SetStation(local_station);
        gallery_bar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                for (int i = 0; i < textAdapter.mTextView.length; i++) {
                    textAdapter.mTextView[i].setTextColor(Color.WHITE);
                }
                ((TextView)view).setTextColor(Color.rgb(244, 194, 21));
                station = GetStationCode(current_station_type, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}}
        );

        list_ProgList.setAdapter(listItemAdapter);
        
        //为每天设置点击事件
        text_Monday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickMonday();
            }
        });

        text_Monday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickMonday();
            }
        });

        text_Tuesday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickTuesday();
            }
        });

        text_Tuesday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickTuesday();
            }
        });

        text_Wednesday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickWednesday();
            }
        });

        text_Wednesday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickWednesday();
            }
        });

        text_Thursday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickThursday();
            }
        });

        text_Thursday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickThursday();
            }
        });

        text_Friday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickFriday();
            }
        });

        text_Friday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickFriday();
            }
        });

        text_Satursday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickSatursday();
            }
        });

        text_Satursday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickSatursday();
            }
        });

        text_Sunday_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickSunday();
            }
        });

        text_Sunday_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickSunday();
            }
        });

        list_ProgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String prog_name = (String) mProgList.get(position).get("prog_name");
                final String station_name = (String) mProgList.get(position).get("station_name");
                final String subStation_name = (String) mProgList.get(position).get("subStation_name");
                final String start_time = (String) mProgList.get(position).get("real_start_time");
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
                    AlertDialog.Builder builder = new Builder(Prog_List.this);
                    builder.setMessage("此操作需要使用新浪微博登录，是否登录？");
                    builder.setTitle("提示");

                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Prog_List.this, User_Login_Before.class);
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
                    Utils.DoLogin(Prog_List.this, login_username, login_password);
                    Intent intent = new Intent(Prog_List.this, Prog_TabHost.class);
                    intent.putExtra("station_name", station_name);
                    intent.putExtra("subStation_name", subStation_name);
                    intent.putExtra("prog_name", prog_name);
                    intent.putExtra("start_time", start_time);
                    intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Prog_List.this, Prog_TabHost.class);
                    intent.putExtra("station_name", station_name);
                    intent.putExtra("subStation_name", subStation_name);
                    intent.putExtra("prog_name", prog_name);
                    intent.putExtra("start_time", start_time);
                    intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        user_image_url = preferences.getString("user_image_url", "");
        location = preferences.getString("location", "北京");
        local_station = GetProvinceArray(location);

        if (text_title.getText().toString().equals("本地台 ▼")) {
            SetStation(local_station);
        }

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

    void DownloadProgList() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.setMessage("正在获取节目信息...");  
        progressDialog.setIndeterminate(false);  
        progressDialog.setCancelable(false);  
        progressDialog.show();  
        
        new Thread() {
            public void run() {
                GetProgList();
                sendMsg(UPDATE_LIST_EVENT);
            }
        }.start();
    }
    
    void SetStation(String[][] station) {
        textAdapter = new TextAdapter(this, station);
        gallery_bar.setAdapter(textAdapter);
    }

    void ClickMonday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        gotoDate = sf.format(calendar.getTime());
    }

    void ClickTuesday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_TUESDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        gotoDate = sf.format(calendar.getTime());
    }

    void ClickWednesday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_WEDNESDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        gotoDate = sf.format(calendar.getTime());
    }

    void ClickThursday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_THURSDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        gotoDate = sf.format(calendar.getTime());
    }

    void ClickFriday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_FRIDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        gotoDate = sf.format(calendar.getTime());
    }

    void ClickSatursday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_SATURSDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        gotoDate = sf.format(calendar.getTime());
    }

    void ClickSunday() {
        resetAllDate();
        setActiveDate(DAYOFWEEK_SUNDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.add(Calendar.DAY_OF_WEEK, 7);
        gotoDate = sf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_WEEK, -7);
    }

    class TextAdapter extends BaseAdapter {
        private Context mContext;
        private String[][] mStation;
        public TextView[] mTextView;

        public TextAdapter(Context context, String[][] station) {
            this.mContext = context;
            mStation = station;
            mTextView = new TextView[mStation.length];
            for (int i = 0; i < mStation.length; i++) {
                mTextView[i] = new TextView(mContext);
                mTextView[i].setText(mStation[i][Utils.STATION_PARAM_NAME].toString());
                mTextView[i].setTextSize(20);
                mTextView[i].setLayoutParams(new Gallery.LayoutParams(400, 40));
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

    //为今天设置日期
    void setActiveDate(int dayofweek) {
        resetAllDate();
        switch (dayofweek) {
        case DAYOFWEEK_SUNDAY:
            text_Sunday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Sunday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Sunday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Sunday_2.setTextColor(Color.BLACK);
            break;
        case DAYOFWEEK_MONDAY:
            text_Monday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Monday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Monday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Monday_2.setTextColor(Color.BLACK);
            break;
        case DAYOFWEEK_TUESDAY:
            text_Tuesday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Tuesday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Tuesday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Tuesday_2.setTextColor(Color.BLACK);
            break;
        case DAYOFWEEK_WEDNESDAY:
            text_Wednesday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Wednesday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Wednesday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Wednesday_2.setTextColor(Color.BLACK);
            break;
        case DAYOFWEEK_THURSDAY:
            text_Thursday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Thursday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Thursday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Thursday_2.setTextColor(Color.BLACK);
            break;
        case DAYOFWEEK_FRIDAY:
            text_Friday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Friday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Friday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Friday_2.setTextColor(Color.BLACK);
            break;
        case DAYOFWEEK_SATURSDAY:
            text_Satursday_1.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Satursday_2.setBackgroundColor(Color.rgb(246, 246, 246));
            text_Satursday_1.setTextColor(Color.rgb(152, 152, 152));
            text_Satursday_2.setTextColor(Color.BLACK);
            break;
        default:
            break;
        }
    }

    //给每天设置日期
    void resetAllDate() {
        text_Monday_1.setText(String.valueOf(current_Monday) + "日");
        text_Monday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Monday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Monday_2.setText("周\n一");
        text_Monday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Monday_2.setTextColor(Color.WHITE);
        text_Tuesday_1.setText(String.valueOf(current_Tuesday) + "日");
        text_Tuesday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Tuesday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Tuesday_2.setText("周\n二");
        text_Tuesday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Tuesday_2.setTextColor(Color.WHITE);
        text_Wednesday_1.setText(String.valueOf(current_Wednesday) + "日");
        text_Wednesday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Wednesday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Wednesday_2.setText("周\n三");
        text_Wednesday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Wednesday_2.setTextColor(Color.WHITE);
        text_Thursday_1.setText(String.valueOf(current_Thursday) + "日");
        text_Thursday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Thursday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Thursday_2.setText("周\n四");
        text_Thursday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Thursday_2.setTextColor(Color.WHITE);
        text_Friday_1.setText(String.valueOf(current_Friday) + "日");
        text_Friday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Friday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Friday_2.setText("周\n五");
        text_Friday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Friday_2.setTextColor(Color.WHITE);
        text_Satursday_1.setText(String.valueOf(current_Satursday) + "日");
        text_Satursday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Satursday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Satursday_2.setText("周\n六");
        text_Satursday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Satursday_2.setTextColor(Color.WHITE);
        text_Sunday_1.setText(String.valueOf(current_Sunday) + "日");
        text_Sunday_1.setBackgroundColor(Color.TRANSPARENT);
        text_Sunday_1.setTextColor(Color.rgb(214, 214, 214));
        text_Sunday_2.setText("周\n日");
        text_Sunday_2.setBackgroundColor(Color.TRANSPARENT);
        text_Sunday_2.setTextColor(Color.WHITE);

        switch (current_dayofweek) {
        case DAYOFWEEK_SUNDAY:
            text_Sunday_2.setText("今\n天");
            break;
        case DAYOFWEEK_MONDAY:
            text_Monday_2.setText("今\n天");
            break;
        case DAYOFWEEK_TUESDAY:
            text_Tuesday_2.setText("今\n天");
            break;
        case DAYOFWEEK_WEDNESDAY:
            text_Wednesday_2.setText("今\n天");
            break;
        case DAYOFWEEK_THURSDAY:
            text_Thursday_2.setText("今\n天");
            break;
        case DAYOFWEEK_FRIDAY:
            text_Friday_2.setText("今\n天");
            break;
        case DAYOFWEEK_SATURSDAY:
            text_Satursday_2.setText("今\n天");
            break;
        default:
            break;
        }
    }

    void GetProgList() {
        mTempList.clear();

        File folder1 = new File(Utils.store_path);
        if (!folder1.exists()) {
            folder1.mkdir();
        }
        
        File folder2 = new File(Utils.store_path_EPG);
        if (!folder2.exists()) {
            folder2.mkdir();
        }

        try {
            String filePath = Utils.store_path_EPG + gotoDate + "-" + station;
            File epgFile = new File(filePath);
            if (epgFile.exists()) {
                FileInputStream inStream = new FileInputStream(filePath);
                byte[] buffer = new byte[inStream.available()];
                inStream.read(buffer);
                epgString = new String(buffer);
            } else {
                String urlString = URL_PREFIX + "?begin=" + gotoDate + "%2000:00:00"
                                              + "&end=" + gotoDate + "%2023:59:59"
                                              + "&subStation=" + station;
    
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = new DefaultHttpClient().execute(request);
    
                if (response.getStatusLine().getStatusCode() == 200) {
                    epgString = EntityUtils.toString(response.getEntity(), "UTF-8");
                    if (epgString.length() > 30) {
                        FileOutputStream outStream = new FileOutputStream(filePath);
                        outStream.write(epgString.getBytes());
                        outStream.close();
                    }
                }
            }
    
            String station_name = "";
            String subStation_name = "";
            String prog_name = "";
            String start_time = "";
            int time_start = 0;
            JSONArray jsonArray = new JSONArray(epgString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                station_name = jsonObject.getString("station");
                subStation_name = jsonObject.getString("subStation");
                prog_name = jsonObject.getString("program");
                start_time = jsonObject.getString("playTime");
                time_start = start_time.indexOf(" ");
                start_time = start_time.substring(time_start + 1, time_start + 6);
    
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("station_name", station_name);
                item.put("subStation_name", subStation_name);
                item.put("start_time", start_time);
                item.put("playing", "播完");
                item.put("prog_name", prog_name);
                item.put("real_start_time", jsonObject.getString("playTime"));
    
                mTempList.add(item);
            }
    
            if (mTempList.size() > 1) {
                Date date = new Date();
                SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
                String nowTimeString = sf.format(date);
    
                if (gotoDate.equals(today)) {
                    for (int i = 0; i < mTempList.size() - 1; i++) {
                        String curProgTimeString = (String) mTempList.get(i).get("start_time"); 
                        String nextProgTimeString = (String) mTempList.get(i + 1).get("start_time"); 
                        if (nowTimeString.compareToIgnoreCase(nextProgTimeString) >= 0) {
                            mTempList.get(i).put("playing", "播完");
                        } else if (nowTimeString.compareToIgnoreCase(curProgTimeString) >= 0
                                && nowTimeString.compareToIgnoreCase(nextProgTimeString) < 0) {
                            mTempList.get(i).put("playing", "正播");
                        } else if (nowTimeString.compareToIgnoreCase(curProgTimeString) < 0) {
                            mTempList.get(i).put("playing", "未播");
                        }
                    }
    
                    String lastProgTimeString = (String) mTempList.get(mTempList.size() - 1).get("start_time"); 
                    if (nowTimeString.compareToIgnoreCase(lastProgTimeString) > 0) {
                        mTempList.get(mTempList.size() - 1).put("playing", "正播");
                    } else if (nowTimeString.compareToIgnoreCase(lastProgTimeString) < 0) {
                        mTempList.get(mTempList.size() - 1).put("playing", "未播");
                    }
                } else if (gotoDate.compareTo(today) < 0) {
                    for (int i = 0; i < mTempList.size(); i++) {
                        mTempList.get(i).put("playing", "播完");
                    }
                } else if (gotoDate.compareTo(today) > 0) {
                    for (int i = 0; i < mTempList.size(); i++) {
                        mTempList.get(i).put("playing", "未播");
                    }
                }
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
                    mProgList = (ArrayList<Map<String, Object>>) mTempList.clone();
                    listItemAdapter.notifyDataSetChanged();
                    list_ProgList.setSelection(0);
                    for (int i = 0; i < listItemAdapter.getCount(); i++) {
                        View view = listItemAdapter.getView(i, null, null);
                        TextView textView = (TextView)view.findViewById(R.id.text_prog_list_playing);
                        if (textView.getText().toString() == "正播") {
                            list_ProgList.setSelection(i);
                            break;
                        }
                    }
                    progressDialog.dismiss();
                    break;
                case DOWNLOAD_PROG_LIST_EVENT:
                    DownloadProgList();
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };

    class ListItemAdapter extends BaseAdapter {
        private Context mContext = null;

        public ListItemAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mProgList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView start_time = null;
            TextView playing = null;
            TextView prog_name = null;

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.prog_list_item, null);

            start_time = (TextView)view.findViewById(R.id.text_prog_list_start_time);
            playing = (TextView)view.findViewById(R.id.text_prog_list_playing);
            prog_name = (TextView)view.findViewById(R.id.text_prog_list_prog_name);

            start_time.setText(mProgList.get(position).get("start_time").toString());
            playing.setText(mProgList.get(position).get("playing").toString());
            prog_name.setText(mProgList.get(position).get("prog_name").toString());

            if (playing.getText().equals("正播")) {
                start_time.setTextColor(Color.RED);
                playing.setTextColor(Color.RED);
            }

            return view;
        }
    }

    String[][] GetProvinceArray(String location) {
        String[][] result = Utils.BEIJING_STATION;
        if (location == "北京") {
            result = Utils.BEIJING_STATION;
        } else if (location == "上海") {
            result = Utils.SHANGHAI_STATION;
        } else if (location == "天津") {
            result = Utils.TIANJIN_STATION;
        } else if (location == "重庆") {
            result = Utils.CHONGQING_STATION;
        } else if (location == "广东") {
            result = Utils.GUANGDONG_STATION;
        } else if (location == "福建") {
            result = Utils.FUJIAN_STATION;
        } else if (location == "广西") {
            result = Utils.GUANGXI_STATION;
        } else if (location == "海南") {
            result = Utils.HAINAN_STATION;
        } else if (location == "浙江") {
            result = Utils.ZHEJIANG_STATION;
        } else if (location == "江苏") {
            result = Utils.JIANGSU_STATION;
        } else if (location == "安徽") {
            result = Utils.ANHUI_STATION;
        } else if (location == "四川") {
            result = Utils.SICHUAN_STATION;
        } else if (location == "云南") {
            result = Utils.YUNNAN_STATION;
        } else if (location == "贵州") {
            result = Utils.GUIZHOU_STATION;
        } else if (location == "湖北") {
            result = Utils.HUBEI_STATION;
        } else if (location == "湖南") {
            result = Utils.HUNAN_STATION;
        } else if (location == "河南") {
            result = Utils.HENAN_STATION;
        } else if (location == "江西") {
            result = Utils.JIANGXI_STATION;
        } else if (location == "河北") {
            result = Utils.HEBEI_STATION;
        } else if (location == "山东") {
            result = Utils.SHANDONG_STATION;
        } else if (location == "山西") {
            result = Utils.SHANXI_STATION;
        } else if (location == "内蒙古") {
            result = Utils.NEIMENGGU_STATION;
        } else if (location == "新疆") {
            result = Utils.XINJIANG_STATION;
        } else if (location == "西藏") {
            result = Utils.XIZANG_STATION;
        } else if (location == "青海") {
            result = Utils.QINGHAI_STATION;
        } else if (location == "甘肃") {
            result = Utils.GANSU_STATION;
        } else if (location == "宁夏") {
            result = Utils.NINGXIA_STATION;
        } else if (location == "陕西") {
            result = Utils.SHAANXI_STATION;
        } else if (location == "黑龙江") {
            result = Utils.HEILONGJIAN_STATION;
        } else if (location == "吉林") {
            result = Utils.JILIN_STATION;
        } else if (location == "辽宁") {
            result = Utils.LIAONING_STATION;
        }

        return result;
    }

    String GetStationCode(int stationType, int position) {
        String result = "";
        switch (stationType) {
        case Utils.STATION_TYPE_LOCAL:
            result = local_station[position][Utils.STATION_PARAM_CODE];
            break;
        case Utils.STATION_TYPE_CCTV:
            result = Utils.CCTV_STATION[position][Utils.STATION_PARAM_CODE];
            break;
        case Utils.STATION_TYPE_SATELLATE:
            result = Utils.SATELLITE_STATION[position][Utils.STATION_PARAM_CODE];
            break;
        case Utils.STATION_TYPE_HD:
            result = Utils.HD_STATION[position][Utils.STATION_PARAM_CODE];
            break;
        case Utils.STATION_TYPE_DIGITAL:
            result = Utils.DIGITAL_STATION[position][Utils.STATION_PARAM_CODE];
            break;
        default:
            break;
        }
        return result;
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