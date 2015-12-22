package cn.shidian;

import java.util.Calendar;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONObject;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class User_Detail extends Activity {
    private String login_username = "";
    private String login_password = "";
    private String access_token = "";
    private long login_time = 0;
    private String user_name = "";
    private String user_id = "";
    private TextView text_title;
    private Button btn_return;
    private ImageView image_user_image;
    private TextView text_user_name;
    private TextView text_user_gender;
    private TextView text_location;
    private TextView text_blog_url;
    private TextView text_description;
    private long current_time = Calendar.getInstance().getTimeInMillis();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_detail);

        //获取由之前界面设置的参数
        user_name = getIntent().getStringExtra("user_name");
        user_id = getIntent().getStringExtra("user_id");

        //从SharedPreferences获取相关参数
        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        access_token = preferences.getString("access_token", "");
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        login_time = preferences.getLong("login_time", 0);

        //取回每个控件
        text_title = (TextView)findViewById(R.id.text_user_detail_title);
        btn_return = (Button)findViewById(R.id.btn_user_detail_return);
        image_user_image = (ImageView)findViewById(R.id.image_user_detail_user_image);
        text_user_name = (TextView)findViewById(R.id.text_user_detail_user_name);
        text_user_gender = (TextView)findViewById(R.id.text_user_detail_user_gender);
        text_location = (TextView)findViewById(R.id.text_user_detail_location);
        text_blog_url = (TextView)findViewById(R.id.text_user_detail_blog_url);
        text_description = (TextView)findViewById(R.id.text_user_detail_description);

        //设置界面标题为用户昵称
        text_title.setText(user_name);

        //若当前时间与上次登录时间相差大于设置值，则自动进行重登录，并取回access_token
        if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
            Utils.DoLogin(User_Detail.this, login_username, login_password);
            access_token = preferences.getString("access_token", "");
        }

        //为返回按钮设置点击事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        String result = "";
        String user_image_url = "";
        String user_gender = "";
        String location = "";
        String blog_url = "";
        String description = "";
        try {
        	//用GET方式进行HTTPS通信，获取用户相关信息
            Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
            Protocol.registerProtocol("https", myhttps);
            HttpClient client = new HttpClient();
            GetMethod getMethod = new GetMethod("https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + user_id);
            client.executeMethod(getMethod);
            result = getMethod.getResponseBodyAsString();

            JSONObject jsonObject = new JSONObject(result);
            user_image_url = jsonObject.getString("profile_image_url");
            user_gender = jsonObject.getString("gender");
            location = jsonObject.getString("location");
            blog_url = jsonObject.getString("url");
            description = jsonObject.getString("description");

            //下载用户头像，并设置给ImageView
            try {
                BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user_image);
                bitmapTask.execute();
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }

            //设置用户昵称及地理位置
            text_user_name.setText(user_name);
            text_location.setText(location);

            //设置用户博客地址
            if (blog_url.equals("")) {
                text_blog_url.setText("无");
            } else {
                text_blog_url.setText(Utils.ProcessStringWithURL(blog_url));
                text_blog_url.setMovementMethod(LinkMovementMethod.getInstance());
            }

            //设置用户描述信息
            if (description.equals("")) {
                text_description.setText("无");
            } else {
                text_description.setText(Utils.ProcessStringWithURL(description));
                text_description.setMovementMethod(LinkMovementMethod.getInstance());
            }

            //设置用户性别
            if (user_gender.equals("m")) {
                text_user_gender.setText("性别： 男");
            } else if (user_gender.equals("f")) {
                text_user_gender.setText("性别： 女");
            } else if (user_gender.equals("n")) {
                text_user_gender.setText("性别： 未知");
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }
}