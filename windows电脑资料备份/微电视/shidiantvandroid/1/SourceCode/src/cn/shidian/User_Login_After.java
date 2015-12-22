package cn.shidian;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class User_Login_After extends Activity {
    private String user_image_url = "";
    private String login_username = "";
    private String login_password = "";
    private String screen_name = "";
    private String from_user_image = "";
    private String from_where = "";
    
    private Button btn_return = null;
    private EditText edit_username = null;
    private EditText edit_password = null;
    private TextView text_screenname = null;
    private ImageView image_profile_image = null;
    private TextView text_exit = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_login_after);

        //获取由之前界面设置的参数
        from_user_image = getIntent().getStringExtra("from_user_image");
        from_where = getIntent().getStringExtra("from_where");

        //从SharedPreferences获取登录相关参数
        final SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        user_image_url = preferences.getString("user_image_url", "");
        screen_name = preferences.getString("screen_name", "");

        //若其中任何一个参数为空，即认为需要重新登录，进入“登录前”界面
        if (login_username.equals("")
                || login_password.equals("")
                || user_image_url.equals("")
                || screen_name.equals("")) {
            Intent intent = new Intent(User_Login_After.this, User_Login_Before.class);
            intent.putExtra("sub_activity", Utils.MAIN_TABHOST_PLAYING);
            intent.putExtra("activity", Utils.INTENT_MAIN_TABHOST);
            if (from_user_image.equals("yes")) {
                intent.putExtra("from_user_image", "yes");
                intent.putExtra("from_where", from_where);
            }
            startActivity(intent);
            finish();
        }

        //取回每个控件
        btn_return = (Button)findViewById(R.id.btn_user_login_after_return);
        edit_username = (EditText)findViewById(R.id.edit_user_login_after_username);
        edit_password = (EditText)findViewById(R.id.edit_user_login_after_password);
        text_screenname = (TextView)findViewById(R.id.text_user_login_after_user_screenname);
        image_profile_image = (ImageView)findViewById(R.id.image_user_login_image);
        text_exit = (TextView)findViewById(R.id.text_user_login_after_exit);

        //设置用户名及密码字段
        edit_username.setText(login_username);
        edit_password.setText(login_password);

        //为返回按钮设置点击事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//如果由点击用户头像的方式进入此界面，则点击返回按钮后进入第一界面，由from_where字段决定进入哪个VIEW
                Intent intent = new Intent(User_Login_After.this, Main_TabHost.class);
                if (from_where.equals("prog_playing")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_PLAYING);
                } else if (from_where.equals("prog_hot")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_HOT);
                } else if (from_where.equals("prog_guess")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_GUESS);
                } else if (from_where.equals("prog_list")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_LIST);
                }
                startActivity(intent);
                finish();
            }
        });

        //为退出按钮设置点击事件
        text_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//删除SharedPreferences中登录相关参数
                preferences.edit().remove("access_token").commit();
                preferences.edit().remove("login_username").commit();
                preferences.edit().remove("login_password").commit();
                preferences.edit().remove("user_image_url").commit();
                preferences.edit().remove("screen_name").commit();
                
                //返回前一界面
                Intent intent = new Intent(User_Login_After.this, Main_TabHost.class);
                if (from_where.equals("prog_playing")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_PLAYING);
                } else if (from_where.equals("prog_hot")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_HOT);
                } else if (from_where.equals("prog_guess")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_GUESS);
                } else if (from_where.equals("prog_list")) {
                    intent.putExtra("sub_activity", Utils.MAIN_TABHOST_LIST);
                }
                startActivity(intent);
                finish();
            }
        });

        //设置用户昵称
        text_screenname.setText(screen_name);

        //下载用户头像，并设置给ImageView
        try {
            BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_profile_image);
            bitmapTask.execute();
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }
}