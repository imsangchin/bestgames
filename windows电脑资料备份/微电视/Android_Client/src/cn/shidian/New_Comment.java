package cn.shidian;

import java.util.Calendar;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.EncodingUtil;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class New_Comment extends Activity {
    private Button btn_return = null;
    private Button btn_submit = null;
    private EditText edit_content = null;
    private TextView text_remain_count = null;

    private int remain_count = 140;
    private String contentString = "";
    private String login_username = "";
    private String login_password = "";
    private String access_token = "";
    private long login_time = 0;
    private long current_time = Calendar.getInstance().getTimeInMillis();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.new_comment);

        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        access_token = preferences.getString("access_token", "");
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        login_time = preferences.getLong("login_time", 0);

        btn_return = (Button)findViewById(R.id.btn_new_comment_return);
        btn_submit = (Button)findViewById(R.id.btn_new_comment_submit);
        edit_content = (EditText)findViewById(R.id.edit_new_comment_content);
        text_remain_count = (TextView)findViewById(R.id.text_new_comment_remain_count);

        if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
            Utils.DoLogin(New_Comment.this, login_username, login_password);
            access_token = preferences.getString("access_token", "");
        }
        
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (edit_content.getText().toString().equals("")) {
                        Toast.makeText(New_Comment.this, "请输入内容", Toast.LENGTH_LONG).show();
                        return;
                    }

                    NameValuePair[] nameValuePairs = {new NameValuePair("status", edit_content.getText().toString())};
                    contentString = EncodingUtil.formUrlEncode(nameValuePairs, "UTF-8");
                    ByteArrayRequestEntity entity = new ByteArrayRequestEntity(
                            EncodingUtil.getAsciiBytes(contentString),
                            PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
                    Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
                    Protocol.registerProtocol("https", myhttps);
                    HttpClient client = new HttpClient();
                    PostMethod postMethod = new PostMethod("https://api.weibo.com/2/statuses/update.json?access_token=" + access_token);
                    postMethod.setRequestEntity(entity);
                    client.executeMethod(postMethod);
                    Toast.makeText(New_Comment.this, "发布成功", Toast.LENGTH_LONG).show();
                    finish();
                } catch (Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    Toast.makeText(New_Comment.this, "发布失败", Toast.LENGTH_LONG).show();
                }
            }
        });

        edit_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                remain_count = 140 - edit_content.getText().length();
                text_remain_count.setText(String.valueOf(remain_count));
                if (remain_count < 0) {
                    edit_content.setText(edit_content.getText().toString().substring(0, 140).toString());
                    edit_content.setSelection(140);
                }
            }
        });
    }
}