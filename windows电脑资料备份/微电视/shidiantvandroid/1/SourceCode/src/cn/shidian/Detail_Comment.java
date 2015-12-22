package cn.shidian;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Detail_Comment extends Activity {
    private Button btn_return = null;
    private ImageView image_repost = null;
    private ImageView image_reply = null;
    private ImageView image_user_image = null;
    private TextView text_user_name = null;
    private ListView list_detail_comment = null;
    private String login_username = "";
    private String login_password = "";
    private String access_token = "";
    private long login_time = 0;
    private String comment_id = "";
    private String user_image_url = "";
    private String user_name = "";
    private String user_id = "";
    private String original_comment_id = "";
    private String created_at = "";
    private String comment_text = "";
    private String source = "";
    private String client_name = "";
    private String repost_count = "";
    private String reply_count = "";
    private String original_text = "";
    private String original_user_name = "";
    private String original_repost_count = "";
    private String original_reply_count = "";
    private String image_small = "";
    private String image_middle = "";
    private String image_small_original = "";
    private String image_middle_original = "";
    private int start = 0;
    private int end = 0;
    private long current_time = Calendar.getInstance().getTimeInMillis();

    private ArrayList<Map<String, Object>> mDetailList = new ArrayList<Map<String, Object>>();
    private boolean isForOriginal = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.detail_comment);

        comment_id = getIntent().getStringExtra("comment_id");
        user_image_url = getIntent().getStringExtra("user_image_url");
        user_name = getIntent().getStringExtra("user_name");
        user_id = getIntent().getStringExtra("user_id");
        original_comment_id = getIntent().getStringExtra("original_comment_id");
        comment_text = getIntent().getStringExtra("comment_text");
        created_at = getIntent().getStringExtra("created_at");
        source = getIntent().getStringExtra("source");
        repost_count = getIntent().getStringExtra("repost_count");
        reply_count = getIntent().getStringExtra("reply_count");
        original_text = getIntent().getStringExtra("original_text");
        original_user_name = getIntent().getStringExtra("original_user_name");
        original_repost_count = getIntent().getStringExtra("original_repost_count");
        original_reply_count = getIntent().getStringExtra("original_reply_count");
        image_small = getIntent().getStringExtra("image_small");
        image_middle = getIntent().getStringExtra("image_middle");
        image_small_original = getIntent().getStringExtra("image_small_original"); 
        image_middle_original = getIntent().getStringExtra("image_middle_original"); 
        
        SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        access_token = preferences.getString("access_token", "");
        login_username = preferences.getString("login_username", "");
        login_password = preferences.getString("login_password", "");
        login_time = preferences.getLong("login_time", 0);

        btn_return = (Button)findViewById(R.id.btn_new_comment_return);
        image_repost = (ImageView)findViewById(R.id.image_detail_comment_repost);
        image_reply = (ImageView)findViewById(R.id.image_detail_comment_reply);
        image_user_image = (ImageView)findViewById(R.id.image_detail_comment_user_image);
        text_user_name = (TextView)findViewById(R.id.text_detail_comment_user_name);
        list_detail_comment = (ListView)findViewById(R.id.list_detail_comment_list);
        
        if (current_time - login_time > Utils.WEIBO_RELOGIN_TIME) {
            Utils.DoLogin(Detail_Comment.this, login_username, login_password);
            access_token = preferences.getString("access_token", "");
        }
        
        try {
            BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(user_image_url, image_user_image);
            bitmapTask.execute();
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }

        start = source.indexOf(">");
        end = source.indexOf("</a>");
        client_name = source.substring(start + 1, end);

        text_user_name.setText(user_name);
        
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_repost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isForOriginal = false;
                Intent intent = new Intent(Detail_Comment.this, Repost_Comment.class);
                intent.putExtra("comment_id", comment_id);
                intent.putExtra("user_name", user_name);
                intent.putExtra("comment_text", comment_text);
                intent.putExtra("original_comment_id", original_comment_id);
                intent.putExtra("original_user_name", original_user_name);
                intent.putExtra("original_text", original_text);
                intent.putExtra("isForOriginal", isForOriginal);
                startActivity(intent);
            }
        });

        image_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isForOriginal = false;
                Intent intent = new Intent(Detail_Comment.this, Reply_Comment.class);
                intent.putExtra("comment_id", comment_id);
                intent.putExtra("user_name", user_name);
                intent.putExtra("comment_text", comment_text);
                intent.putExtra("original_comment_id", original_comment_id);
                intent.putExtra("original_user_name", original_user_name);
                intent.putExtra("original_text", original_text);
                intent.putExtra("isForOriginal", isForOriginal);
                startActivity(intent);
            }
        });

        image_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Detail_Comment.this, User_Detail.class);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });
        
        GetDetailCommentData();
    }

    void GetDetailCommentData() {
        try {
            String result = "";
            Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
            Protocol.registerProtocol("https", myhttps);
            HttpClient client = new HttpClient();
            GetMethod getMethod = new GetMethod("https://api.weibo.com/2/comments/show.json?access_token=" + access_token + "&id=" + comment_id);
            client.executeMethod(getMethod);
            result = getMethod.getResponseBodyAsString();

            String user_name = "";
            String created_at = "";
            String comment_text = "";
            JSONObject jsonObjectTotal = new JSONObject(result);
            JSONArray jsonArray = jsonObjectTotal.getJSONArray("comments");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                user_name = jsonObject.getJSONObject("user").getString("screen_name");
                created_at = jsonObject.getString("created_at");
                comment_text = jsonObject.getString("text");

                Map<String, Object> item = new HashMap<String, Object>();
                item.put("user_name", user_name);
                item.put("created_at", created_at);
                item.put("comment_text", comment_text);

                mDetailList.add(item);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        
        list_detail_comment.setAdapter(new ListItemAdapter(Detail_Comment.this));
    }

    class ListItemAdapter extends BaseAdapter {
        private Context mContext = null;

        public ListItemAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mDetailList.size() + 1;
        }

        @Override
        public Object getItem(int position) {return null;}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            LayoutInflater inflater = LayoutInflater.from(mContext);

            if (position == 0) {
                TextView text_comment_text = null;
                TextView text_time = null;
                TextView text_client_name = null;
                TextView text_count = null;
                ImageView image_small_image = null;
                ImageView image_small_image_original = null;
                LinearLayout layout_original_text = null;
                TextView text_original_text = null;
                TextView text_original_count = null;
                ImageView image_original_repost = null;
                ImageView image_original_reply = null;

                view = inflater.inflate(R.layout.detail_comment_original_item, null);

                text_comment_text = (TextView)view.findViewById(R.id.text_detail_comment_comment_text);
                text_time = (TextView)view.findViewById(R.id.text_detail_comment_time);
                text_client_name = (TextView)view.findViewById(R.id.text_detail_comment_client_name);
                text_count = (TextView)view.findViewById(R.id.text_detail_comment_count);
                image_small_image = (ImageView)view.findViewById(R.id.image_detail_comment_image_small);
                image_small_image_original = (ImageView)view.findViewById(R.id.image_detail_comment_original_image_small);
                layout_original_text = (LinearLayout)view.findViewById(R.id.linear_layout_detail_comment_original_text);
                text_original_text = (TextView)view.findViewById(R.id.text_detail_comment_original_text);
                text_original_count = (TextView)view.findViewById(R.id.text_detail_comment_original_count);
                image_original_repost = (ImageView)view.findViewById(R.id.image_detail_comment_original_repost);
                image_original_reply = (ImageView)view.findViewById(R.id.image_detail_comment_original_reply);

                text_comment_text.setText(Utils.ProcessStringWithURL(comment_text));
                text_time.setText(Utils.GetFormattedStringFromCreateAtString(created_at));
                text_client_name.setText(client_name);
                text_count.setText("转发:" + repost_count + " | 评论:" + reply_count);
                
                text_comment_text.setMovementMethod(LinkMovementMethod.getInstance());
                
                if (original_text.equals("")) {
                    text_original_text.setTextSize(1);
                    text_original_count.setTextSize(1);
                    layout_original_text.setVisibility(View.INVISIBLE);
                } else {
                    layout_original_text.setBackgroundResource(R.drawable.original_text_border);
                    text_original_text.setText(Utils.GetOriginalTextString(original_user_name, original_text));
                    text_original_count.setText("转发:" + original_repost_count + " | 评论:" + original_reply_count);
                }
                
                image_small_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(Detail_Comment.this);
                        ImageView view = new ImageView(Detail_Comment.this);
                        try {
                            BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(image_middle, view);
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
                        final Dialog dialog = new Dialog(Detail_Comment.this);
                        ImageView view = new ImageView(Detail_Comment.this);
                        try {
                            BitmapDownloaderTask bitmapTask = new BitmapDownloaderTask(image_middle_original, view);
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
                
                image_original_repost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isForOriginal = true;
                        Intent intent = new Intent(Detail_Comment.this, Repost_Comment.class);
                        intent.putExtra("comment_id", comment_id);
                        intent.putExtra("user_name", user_name);
                        intent.putExtra("comment_text", comment_text);
                        intent.putExtra("original_comment_id", original_comment_id);
                        intent.putExtra("original_user_name", original_user_name);
                        intent.putExtra("original_text", original_text);
                        intent.putExtra("isForOriginal", isForOriginal);
                        startActivity(intent);
                    }
                });

                image_original_reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isForOriginal = true;
                        Intent intent = new Intent(Detail_Comment.this, Reply_Comment.class);
                        intent.putExtra("comment_id", comment_id);
                        intent.putExtra("user_name", user_name);
                        intent.putExtra("comment_text", comment_text);
                        intent.putExtra("original_comment_id", original_comment_id);
                        intent.putExtra("original_user_name", original_user_name);
                        intent.putExtra("original_text", original_text);
                        intent.putExtra("isForOriginal", isForOriginal);
                        startActivity(intent);
                    }
                });

            } else {
                int localPosition = position - 1;
                TextView text_user_name = null;
                TextView text_time = null;
                TextView text_comment_text = null;
                String created_at = "";

                view = inflater.inflate(R.layout.detail_comment_item, null);

                text_user_name = (TextView)view.findViewById(R.id.text_detail_comment_item_user_name);
                text_time = (TextView)view.findViewById(R.id.text_detail_comment_item_time);
                text_comment_text = (TextView)view.findViewById(R.id.text_detail_comment_item_text);

                text_user_name.setText(mDetailList.get(localPosition).get("user_name").toString());
                created_at = mDetailList.get(localPosition).get("created_at").toString();
                text_time.setText(Utils.GetFormattedStringFromCreateAtString(created_at));
                text_comment_text.setText(Utils.ProcessStringWithURL(mDetailList.get(localPosition).get("comment_text").toString()));
                text_comment_text.setMovementMethod(LinkMovementMethod.getInstance());
            }

            return view;
        }
    }
}