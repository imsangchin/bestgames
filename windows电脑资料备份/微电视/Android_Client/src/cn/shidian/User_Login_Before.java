package cn.shidian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class User_Login_Before extends Activity {
    private Button btn_login = null;
    private Button btn_return = null;
    private EditText text_username = null;
    private EditText text_password = null;
    private String username = "";
    private String password = "";
    private String prog_name = "";
    private String station_name = "";
    private String subStation_name = "";
    private String start_time = "";
    private String from_user_image = "";
    private String from_where = "";
    private int activity = 0;
    private int sub_activity = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_login_before);

        //获取由之前界面设置的参数
        prog_name = getIntent().getStringExtra("prog_name");
        station_name = getIntent().getStringExtra("station_name");
        subStation_name = getIntent().getStringExtra("subStation_name");
        start_time = getIntent().getStringExtra("start_time");
        activity = getIntent().getIntExtra("activity", -1);
        sub_activity = getIntent().getIntExtra("sub_activity", -1);
        from_user_image = getIntent().getStringExtra("from_user_image");
        from_where = getIntent().getStringExtra("from_where");

        //取回每个控件
        btn_login = (Button)findViewById(R.id.btn_user_login_before_login);
        btn_return = (Button)findViewById(R.id.btn_user_login_before_return);
        text_username = (EditText)findViewById(R.id.edit_user_login_username);
        text_password = (EditText)findViewById(R.id.edit_user_login_password);

        //为返回按钮设置点击事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//如果由点击用户头像的方式进入此界面，则点击返回按钮后进入第一界面，由from_where字段决定进入哪个VIEW
                if (from_user_image.equals("yes")) {
                    Intent intent = new Intent(User_Login_Before.this, Main_TabHost.class);
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
                }
                finish();
            }
        });

        //为登录按钮设置点击事件
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//获取用户输入的用户名及密码
                username = text_username.getText().toString();
                password = text_password.getText().toString();

                //若用户未输入用户名或密码，则提示用户输入
                if (username.equals("")	|| password.equals("")) {
                    Toast.makeText(User_Login_Before.this, "请输入用户名及密码", Toast.LENGTH_LONG).show();
                    return;
                }

                //执行登录过程
                int result = Utils.DoLogin(User_Login_Before.this, username, password);

                //根据登录过程的执行结果，执行后续过程
                switch (result) {
                //若登录成功，则进入相应界面
                case Utils.LOGIN_SUCCESS:
                    Toast.makeText(User_Login_Before.this, "登录成功", Toast.LENGTH_LONG).show();
                    Intent intent = Utils.getIntentFromType(activity, User_Login_Before.this);
                    if (intent != null) {
                        if (from_user_image == null) {
                        	//若由点击节目触发进入此界面，则进入 第二界面的相应VIEW
                            intent.putExtra("prog_name", prog_name);
                            intent.putExtra("station_name", station_name);
                            intent.putExtra("subStation_name", subStation_name);
                            intent.putExtra("start_time", start_time);
                            intent.putExtra("sub_activity", sub_activity);
                        } else if (from_user_image.equals("yes")) {
                        	//若由点击用户头像触发进入此界面，则进入第一界面的相应VIEW
                            if (from_where.equals("prog_playing")) {
                                intent.putExtra("sub_activity", Utils.MAIN_TABHOST_PLAYING);
                            } else if (from_where.equals("prog_hot")) {
                                intent.putExtra("sub_activity", Utils.MAIN_TABHOST_HOT);
                            } else if (from_where.equals("prog_guess")) {
                                intent.putExtra("sub_activity", Utils.MAIN_TABHOST_GUESS);
                            } else if (from_where.equals("prog_list")) {
                                intent.putExtra("sub_activity", Utils.MAIN_TABHOST_LIST);
                            }
                        }
                        startActivity(intent);
                        finish();
                    }
                    break;
                //三种登录错误，分别进行提示
                case Utils.LOGIN_PASSWORD_ERROR:
                    Toast.makeText(User_Login_Before.this, "用户名或密码错误", Toast.LENGTH_LONG).show();
                    break;
                case Utils.LOGIN_NETWORK_ERROR:
                    Toast.makeText(User_Login_Before.this, "网络连接失败", Toast.LENGTH_LONG).show();
                    break;
                case Utils.LOGIN_PROCESS_ERROR:
                    Toast.makeText(User_Login_Before.this, "登录过程处理错误", Toast.LENGTH_LONG).show();
                    break;
                }
            }
        });
    }
}