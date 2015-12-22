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
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Reply_Comment extends Activity {
    private Button btn_return = null;
    private Button btn_submit = null;
    private EditText edit_content = null;
    private TextView text_remain_count = null;
    private CheckBox checkbox_forward_weibo = null;
    private CheckBox checkbox_to_original_author = null;

    private int remain_count = 140;
    private String contentString = "";
    private String login_username = "";
    private String login_password = "";
    private String access_token = "";
    private long login_time = 0;
    private String comment_id = "";
    private String comment_text = "";
    private String original_comment_id = "";
    private String user_name = "";
    private String original_user_name = "";
    private String original_text = "";
    private boolean isForOriginal = false;
    private long current_time = Calendar.getInstance().getTimeInMillis();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reply_comment);

        //获取由之前界面设置的参数
        comment_id = getIntent().getStringExtra("comment_id");
        comment_text = getIntent().getStringExtra("comment_text");
        original_comment_id = getIntent().getStringExtra("original_comment_id");
        user_name = getIntent().getStringExtra("user_name");
        original_user_name = getIntent().getStringExtra("original_user_name");
        original_text = getIntent().getStringExtra("original_text");
        isForOriginal = getIntent().getBooleanExtra("isForOriginal", false);

        //从SharedPreferences获取相关参数
        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        access_token = preferences.getString("access_token", "");
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        login_time = preferences.getLong("login_time", 0);

        //取回每个控件
        btn_return = (Button)findViewById(R.id.btn_reply_comment_return);
        btn_submit = (Button)findViewById(R.id.btn_reply_comment_submit);
        edit_content = (EditText)findViewById(R.id.edit_reply_comment_content);
        text_remain_count = (TextView)findViewById(R.id.text_reply_comment_remain_count);
        checkbox_forward_weibo = (CheckBox)findViewById(R.id.checkbox_reply_comment_forward_weibo);
        checkbox_to_original_author = (CheckBox)findViewById(R.id.checkbox_reply_comment_to_original_author);

        if (isForOriginal == false) {
            checkbox_to_original_author.append(original_user_name);
        }
        
        //若当前时间与上次登录时间相差大于设置值，则自动进行重登录，并取回access_token
        if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
            Utils.DoLogin(Reply_Comment.this, login_username, login_password);
            access_token = preferences.getString("access_token", "");
        }

        //为返回按钮设置点击事件
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (original_comment_id.equals("") || isForOriginal == true) {
            checkbox_to_original_author.setEnabled(false);
            checkbox_to_original_author.setChecked(false);
            checkbox_to_original_author.setTextColor(Color.GRAY);
        }

        //为发布按钮设置点击事件
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                	//若用户未输入内容，则提示用户输入
                    if (edit_content.getText().toString().equals("")) {
                        Toast.makeText(Reply_Comment.this, "请输入内容", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String used_id = "";
                    if (isForOriginal == false) {
                        used_id = comment_id;
                    } else {
                        used_id = original_comment_id;
                    }

                    Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
                    Protocol.registerProtocol("https", myhttps);

                    //设置参数，包括用户输入内容及要回复的微博ID
                    NameValuePair[] nameValuePairs1 = {new NameValuePair("comment", edit_content.getText().toString()),
                        new NameValuePair("id", used_id)};
                    contentString = EncodingUtil.formUrlEncode(nameValuePairs1, "UTF-8");
                    ByteArrayRequestEntity entity1 = new ByteArrayRequestEntity(
                            EncodingUtil.getAsciiBytes(contentString),
                            PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
                    HttpClient client1 = new HttpClient();
                    PostMethod postMethod1 = new PostMethod("https://api.weibo.com/2/comments/create.json?access_token=" + access_token);
                    postMethod1.setRequestEntity(entity1);
                    client1.executeMethod(postMethod1);
                    
                    if (checkbox_forward_weibo.isChecked() == true) {
                        String content = "";
                        if (isForOriginal == true || original_text.equals("")) {
                            content = edit_content.getText().toString() + " ";
                        } else {
                            content = edit_content.getText().toString() + " //@" + user_name + ": " + comment_text;
                        }

                        if (content.length() > 140) {
                            content = content.substring(0, 140);
                        }
                        NameValuePair[] nameValuePairs2 = {new NameValuePair("status", content),
                                new NameValuePair("id", used_id)};
                        contentString = EncodingUtil.formUrlEncode(nameValuePairs2, "UTF-8");
                        ByteArrayRequestEntity entity2 = new ByteArrayRequestEntity(
                                EncodingUtil.getAsciiBytes(contentString),
                                PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);

                        HttpClient client2 = new HttpClient();
                        PostMethod postMethod2 = new PostMethod("https://api.weibo.com/2/statuses/repost.json?access_token=" + access_token);
                        postMethod2.setRequestEntity(entity2);
                        client2.executeMethod(postMethod2);
                    }

                    if (checkbox_to_original_author.isChecked() == true) {
                        NameValuePair[] nameValuePairs3 = {new NameValuePair("comment", " " + edit_content.getText().toString()),
                                new NameValuePair("id", original_comment_id)};
                            contentString = EncodingUtil.formUrlEncode(nameValuePairs3, "UTF-8");
                            ByteArrayRequestEntity entity3 = new ByteArrayRequestEntity(
                                    EncodingUtil.getAsciiBytes(contentString),
                                    PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
                            HttpClient client3 = new HttpClient();
                            PostMethod postMethod3 = new PostMethod("https://api.weibo.com/2/comments/create.json?access_token=" + access_token);
                            postMethod3.setRequestEntity(entity3);
                            client3.executeMethod(postMethod3);
                    }
                    
                    Toast.makeText(Reply_Comment.this, "发布成功", Toast.LENGTH_LONG).show();
                    finish();
                } catch (Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Reply_Comment.this, "发布失败", Toast.LENGTH_LONG).show();
                }
            }
        });

        remain_count = 140 - edit_content.getText().length();
        text_remain_count.setText(String.valueOf(remain_count));
        edit_content.setSelection(0);
        
        //用户输入内容有变化时，重新计算剩余字数，并更新到界面
        edit_content.addTextChangedListener(new TextWatcher() {
            String mTextBefore = "";
            int currentPos = 0;
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mTextBefore = edit_content.getText().toString();
                if (edit_content.getSelectionStart() == edit_content.getSelectionEnd()) {
                    currentPos = edit_content.getSelectionStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edit_content.getText().toString().length() > 140) {
                    edit_content.setText(mTextBefore);
                    if (currentPos != 0) {
                        edit_content.setSelection(currentPos - 1);
                    }
                }
                
                remain_count = 140 - edit_content.getText().length();
                text_remain_count.setText(String.valueOf(remain_count));
            }
        });
    }
}