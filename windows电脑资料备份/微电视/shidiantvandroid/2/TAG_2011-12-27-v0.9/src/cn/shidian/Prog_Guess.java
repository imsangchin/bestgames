package cn.shidian;

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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Prog_Guess extends Activity {
    public static final int UPDATE_LIST_EVENT = 0;
    private ListView list_ProgGuess;
    private ArrayList<Map<String, Object>> mProgList;
    private ProgressDialog progressDialog;
    private Button btn_Setting;
    private ImageView image_user;
    private String user_image_url; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prog_guess);

        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        user_image_url = preferences.getString("user_image_url", "");
        
        mProgList = new ArrayList<Map<String, Object>>();
        progressDialog = new ProgressDialog(Prog_Guess.this);
        
        //取回每个控件
        list_ProgGuess = (ListView)findViewById(R.id.list_prog_guess);
        btn_Setting = (Button)findViewById(R.id.prog_guess_btn_setting);
        image_user = (ImageView)findViewById(R.id.image_prog_guess_user);

        image_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Prog_Guess.this, User_Login_After.class);
                intent.putExtra("from_user_image", "yes");
                intent.putExtra("from_where", "prog_guess");
                startActivity(intent);
                finish();
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
        
        btn_Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Prog_Guess.this, Software_Setting.class));
            }
        });
        
        list_ProgGuess.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String station_name = (String) mProgList.get(position).get("station_name");
                final String subStation_name = (String) mProgList.get(position).get("subStation_name");
                final String prog_name = (String) mProgList.get(position).get("prog_name");
                final String start_time = (String) mProgList.get(position).get("start_time");
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
                    AlertDialog.Builder builder = new Builder(Prog_Guess.this);
                    builder.setMessage("此操作需要使用新浪微博登录，是否登录？");
                    builder.setTitle("提示");

                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Prog_Guess.this, User_Login_Before.class);
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
                    Utils.DoLogin(Prog_Guess.this, login_username, login_password);
                    Intent intent = new Intent(Prog_Guess.this, Prog_TabHost.class);
                    intent.putExtra("station_name", station_name);
                    intent.putExtra("subStation_name", subStation_name);
                    intent.putExtra("prog_name", prog_name);
                    intent.putExtra("start_time", start_time);
                    intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Prog_Guess.this, Prog_TabHost.class);
                    intent.putExtra("station_name", station_name);
                    intent.putExtra("subStation_name", subStation_name);
                    intent.putExtra("prog_name", prog_name);
                    intent.putExtra("start_time", start_time);
                    intent.putExtra("sub_activity", Utils.PROG_TABHOST_BRIEF);
                    startActivity(intent);
                }
            }
        });

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        progressDialog.setMessage("正在获取节目信息...");  
        progressDialog.setIndeterminate(false);  
        progressDialog.setCancelable(false);  
        progressDialog.show();  
        
        new Thread() {
            public void run() {
                GetGuessProg();
                sendMsg(UPDATE_LIST_EVENT);
            }
        }.start();
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

    void GetGuessProg() {
        String urlString = "http://shenkantv.sinaapp.com/epginter/guessTaste.php";
        String result = "";

        try {
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = new DefaultHttpClient().execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
            }

            String station_name = "";
            String subStation_name = "";
            String prog_name = "";
            String start_time = "";
            
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                station_name = jsonObject.getString("station");
                subStation_name = jsonObject.getString("subStation");
                prog_name = jsonObject.getString("program");
                start_time = jsonObject.getString("playTime");
                
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("station_name", station_name);
                item.put("subStation_name", subStation_name);
                item.put("prog_name", prog_name);
                item.put("start_time", start_time);
                
                if (!prog_name.equals("")) {
                    mProgList.add(item);
                }
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
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
            return mProgList.size();
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
            String station_name = "";
            String subStation_name = "";
            String start_time = "";

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.prog_list_item_full, null);

            image_station_ico = (ImageView)view.findViewById(R.id.image_prog_tvstation_ico);
            text_station_name = (TextView)view.findViewById(R.id.text_prog_tvstation_name);
            text_start_time = (TextView)view.findViewById(R.id.text_prog_start_time);
            text_prog_name = (TextView)view.findViewById(R.id.text_prog_prog_name);

            text_station_name.setText(mProgList.get(position).get("subStation_name").toString());
            text_prog_name.setText(mProgList.get(position).get("prog_name").toString());

            start_time = mProgList.get(position).get("start_time").toString();
            if (!start_time.equals("")) {
                start_time = Utils.GetShortStringFromDateTimeString(start_time);
            }
            text_start_time.setText(start_time);

            station_name = mProgList.get(position).get("station_name").toString();
            subStation_name = mProgList.get(position).get("subStation_name").toString();

            image_station_ico.setImageResource(Utils.GetStationIcoFromStationName(station_name, subStation_name));

            return view;
        }
    }
    
    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what)
                {
                case UPDATE_LIST_EVENT:
                    list_ProgGuess.setAdapter(new ListItemAdapter(Prog_Guess.this));
                    progressDialog.cancel();
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };
}