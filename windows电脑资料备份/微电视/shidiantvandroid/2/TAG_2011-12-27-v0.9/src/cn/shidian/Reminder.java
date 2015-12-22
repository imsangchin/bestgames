package cn.shidian;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Reminder extends Activity {
    public static final int UPDATE_LIST_EVENT = 0;
    public static final int POP_WINDOW = 1;
    
    public static final int AHEAD_TIME_5MIN = 0;
    public static final int AHEAD_TIME_15MIN = 1;
    public static final int AHEAD_TIME_30MIN = 2;

    private Button btn_return = null;
    private Button btn_edit = null;
	private ImageView image_user_image = null;
	private TextView text_user_name = null;
	private TextView text_user_care_count = null;
	private TextView text_user_fans_count = null;
	private TextView text_user_location = null;
	private TextView text_weibo_count = null;
	private ListView list_reminder = null;

	private String user_image_url = "";
	private String screen_name = "";
	private String user_location = "";
	private int user_care_count = 0;
	private int user_fans_count = 0;
	private int user_weibo_count = 0;

    private ArrayList<Map<String, Object>> mRemindList = new ArrayList<Map<String, Object>>();
    private ListItemAdapter listItemAdapter;

    private String station_name = "";
    private String subStation_name = "";
    private String prog_name = "";
    private String start_time = "";
    
    private Calendar calendar;

    private LayoutInflater layoutInflater;
    private View dialogView;
    private Dialog dialog;
    private Button btn_set5minReminder = null;
    private Button btn_set15minReminder = null;
    private Button btn_set30minReminder = null;
    private Button btn_cancelSetReminder = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reminder);

        //从SharedPreferences获取相关参数
        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        user_image_url = preferences.getString("user_image_url", "");
        screen_name = preferences.getString("screen_name", "");
        user_location = preferences.getString("user_location", "");
        user_care_count = preferences.getInt("user_care_count", 0);
        user_fans_count = preferences.getInt("user_fans_count", 0);
        user_weibo_count = preferences.getInt("user_weibo_count", 0);

        //获取由之前界面设置的参数
        station_name = getIntent().getStringExtra("station_name");
        subStation_name = getIntent().getStringExtra("subStation_name");
        prog_name = getIntent().getStringExtra("prog_name");
        start_time = getIntent().getStringExtra("start_time");
        calendar = Utils.GetCalendarFromString(start_time);

        //取回每个控件
        btn_return = (Button)findViewById(R.id.btn_reminder_return);
        btn_edit = (Button)findViewById(R.id.btn_reminder_edit);
        image_user_image = (ImageView)findViewById(R.id.image_reminder_user_image);
        text_user_name = (TextView)findViewById(R.id.text_reminder_user_name);
        text_user_care_count = (TextView)findViewById(R.id.text_reminder_user_care_count);
        text_user_fans_count = (TextView)findViewById(R.id.text_reminder_user_fans_count);
        text_user_location = (TextView)findViewById(R.id.text_reminder_user_location);
        text_weibo_count = (TextView)findViewById(R.id.text_reminder_weibo_count);
        list_reminder = (ListView)findViewById(R.id.list_reminder_list);

        //下载用户头像，并设置给ImageView
        try {
            BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user_image);
            bitmapTask.execute();
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }

        //设置用户信息
        text_user_name.setText(screen_name);
        text_user_location.setText(user_location);
        text_user_care_count.setText(String.valueOf(user_care_count));
        text_user_fans_count.setText(String.valueOf(user_fans_count));
        text_weibo_count.setText(String.valueOf(user_weibo_count));

        //由SharedPreferences获取提醒列表
        mRemindList = Utils.GetReminderListFromStored(Reminder.this);

        //给ListView设置Adapter
        listItemAdapter = new ListItemAdapter(Reminder.this);
        list_reminder.setAdapter(listItemAdapter);

        //为返回按钮设置点击事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//将提醒列表存入SharedPreferences中
                Utils.SaveReminderListToStored(Reminder.this, mRemindList);
                finish();
            }
        });

        //为编辑按钮设置点击事件
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn_edit = (Button)v;
                if (btn_edit.getText().equals("编辑")) {
                	//设置编辑按钮的文本
                    btn_edit.setText("完成");
                    for (int i = 0; i < mRemindList.size(); i++) {
                        //设置每个列表项是否显示CheckBox控件
                        mRemindList.get(i).put("checkbox_display", true);
                    }
                } else {
                	//设置编辑按钮的文本
                    btn_edit.setText("编辑");
                    for (int i = 0; i < mRemindList.size(); i++) {
                    	//设置每个列表项是否显示CheckBox控件
                        mRemindList.get(i).put("checkbox_display", false);
                    }
                }
                //通知ListView内容变化，使界面更新
                sendMsg(UPDATE_LIST_EVENT);
            }
        });
        //通知弹出设置提醒时间的窗口
        sendMsg(POP_WINDOW);
    }
    
    class ListItemAdapter extends BaseAdapter {
        private Context mContext;

        public ListItemAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mRemindList.size();
        }

        @Override
        public Object getItem(int position) {return null;}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView image_station_ico = null;
            TextView text_prog_name = null;
            TextView text_station_name = null;
            TextView text_start_time = null;
            TextView text_ahead_time = null;
            String station_name = "";
            String subStation_name = "";
            String start_time = "";
            int ahead_type = -1;
            Button btn_delete = null;
            CheckBox checkbox_delete = null;

            //获取每个列表项是否显示CheckBox及其值
            boolean checkbox_display = (Boolean)mRemindList.get(position).get("checkbox_display");
            boolean checkbox_checked = (Boolean)mRemindList.get(position).get("checkbox_checked");

            //指定每个列表项的布局
            LayoutInflater inflater = LayoutInflater.from(mContext);
            final View view = inflater.inflate(R.layout.reminder_item, null);

            //取回每个控件
            image_station_ico = (ImageView)view.findViewById(R.id.image_reminder_item_station_ico);
            btn_delete = (Button)view.findViewById(R.id.btn_reminder_item_delete);
            text_prog_name = (TextView)view.findViewById(R.id.text_reminder_item_prog_name);
            text_station_name = (TextView)view.findViewById(R.id.text_reminder_item_station_name);
            text_start_time = (TextView)view.findViewById(R.id.text_reminder_item_start_time);
            text_ahead_time = (TextView)view.findViewById(R.id.text_reminder_item_ahead_time);
            checkbox_delete = (CheckBox)view.findViewById(R.id.checkbox_reminder_item_delete);

            //从数据列表中获取每个数据元素
            station_name = mRemindList.get(position).get("station_name").toString();
            subStation_name = mRemindList.get(position).get("subStation_name").toString();
            image_station_ico.setImageResource(Utils.GetStationIcoFromStationName(station_name, subStation_name));
            text_prog_name.setText(mRemindList.get(position).get("prog_name").toString());
            text_station_name.setText(subStation_name);
            start_time = mRemindList.get(position).get("start_time").toString();
            start_time = Utils.GetShortStringFromDateTimeString(start_time);
            text_start_time.setText(start_time);
            ahead_type = (Integer)mRemindList.get(position).get("ahead_type");

            //由提醒类型设置提醒提前时间
            switch (ahead_type) {
            case AHEAD_TIME_5MIN:
                text_ahead_time.setText("5min");
                break;
            case AHEAD_TIME_15MIN:
                text_ahead_time.setText("15min");
                break;
            case AHEAD_TIME_30MIN:
                text_ahead_time.setText("30min");
                break;
            }

            //设置CheckBox的显示及状态和删除按钮的显示
            if (checkbox_display == true) {
                checkbox_delete.setVisibility(View.VISIBLE);
                if (checkbox_checked == true) {
                    checkbox_delete.setChecked(true);
                    btn_delete.setVisibility(View.VISIBLE);
                }
            } else {
                checkbox_delete.setVisibility(View.INVISIBLE);
                if (checkbox_checked == true) {
                    checkbox_delete.setChecked(true);
                    btn_delete.setVisibility(View.INVISIBLE);
                }
            }

            //为删除按钮设置点击事件
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	//从数据列表里删除当前列表项，并通知ListView内容变化，使界面更新
                    mRemindList.remove(position);
                    sendMsg(UPDATE_LIST_EVENT);
                }
            });

            //设置删除按钮的显示
            checkbox_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean)(mRemindList.get(position).get("checkbox_checked")) == false) {
                        mRemindList.get(position).put("checkbox_checked", true);
                    } else {
                        mRemindList.get(position).put("checkbox_checked", false);
                    }
                    //通知ListView内容变化，使界面更新
                    sendMsg(UPDATE_LIST_EVENT);
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
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what)
                {
                case UPDATE_LIST_EVENT:
                	//更新ListView界面
                    listItemAdapter.notifyDataSetChanged();
                    break;
                case POP_WINDOW:
                	//弹出设置提醒时间的窗口
                    layoutInflater = getLayoutInflater();
                    dialogView = layoutInflater.inflate(R.layout.reminder_popup_window, null);
                    btn_set5minReminder = (Button)dialogView.findViewById(R.id.btn_reminder_popup_window_5minutes);
                    btn_set15minReminder = (Button)dialogView.findViewById(R.id.btn_reminder_popup_window_15minutes);
                    btn_set30minReminder = (Button)dialogView.findViewById(R.id.btn_reminder_popup_window_30minutes);
                    btn_cancelSetReminder = (Button)dialogView.findViewById(R.id.btn_reminder_popup_window_cancel);

                    dialog = new Dialog(Reminder.this);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.setTitle("设置提醒");
                    dialog.show();

                    //为“提前5分钟”按钮设置点击事件
                    btn_set5minReminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String remind_time = "";
                            calendar.add(Calendar.MINUTE, -5);
                            remind_time = Utils.GetStringFromCalendarWithoutSecond(calendar);

                            Map<String, Object> item = new HashMap<String, Object>();
                            item.put("station_name", station_name);
                            item.put("subStation_name", subStation_name);
                            item.put("prog_name", prog_name);
                            item.put("start_time", start_time);
                            item.put("remind_time", remind_time);
                            item.put("ahead_type", AHEAD_TIME_5MIN);
                            item.put("checkbox_display", false);
                            item.put("checkbox_checked", false);
                            if (Utils.CheckItemExistentInReminderList(item, mRemindList) == false) {
                                mRemindList.add(item);
                            }
                            listItemAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });

                    //为“提前15分钟”按钮设置点击事件
                    btn_set15minReminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String remind_time = "";
                            calendar.add(Calendar.MINUTE, -15);
                            remind_time = Utils.GetStringFromCalendarWithoutSecond(calendar);

                            Map<String, Object> item = new HashMap<String, Object>();
                            item.put("station_name", station_name);
                            item.put("subStation_name", subStation_name);
                            item.put("prog_name", prog_name);
                            item.put("start_time", start_time);
                            item.put("remind_time", remind_time);
                            item.put("ahead_type", AHEAD_TIME_15MIN);
                            item.put("checkbox_display", false);
                            item.put("checkbox_checked", false);
                            if (Utils.CheckItemExistentInReminderList(item, mRemindList) == false) {
                                mRemindList.add(item);
                            }
                            list_reminder.setAdapter(listItemAdapter);
                            dialog.dismiss();
                        }
                    });

                    //为“提前30分钟”按钮设置点击事件
                    btn_set30minReminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String remind_time = "";
                            calendar.add(Calendar.MINUTE, -30);
                            remind_time = Utils.GetStringFromCalendarWithoutSecond(calendar);

                            Map<String, Object> item = new HashMap<String, Object>();
                            item.put("station_name", station_name);
                            item.put("subStation_name", subStation_name);
                            item.put("prog_name", prog_name);
                            item.put("start_time", start_time);
                            item.put("remind_time", remind_time);
                            item.put("ahead_type", AHEAD_TIME_30MIN);
                            item.put("checkbox_display", false);
                            item.put("checkbox_checked", false);
                            if (Utils.CheckItemExistentInReminderList(item, mRemindList) == false) {
                                mRemindList.add(item);
                            }

                            list_reminder.setAdapter(listItemAdapter);
                            dialog.dismiss();
                        }
                    });

                    //为取消按钮设置点击事件，直接关闭窗口
                    btn_cancelSetReminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };
}