package cn.shidian;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class Prog_TabHost extends TabActivity {
    public static final String BRIEF = "简介";
    public static final String COMMENT = "评论";
    //public static final String PK = "PK台";
    public static final String INVITE = "邀请";
    public static final String REMIND = "提醒";

    private String station_name = "";
    private String subStation_name = "";
    private String prog_name = "";
    private String start_time = "";
    private int sub_activity = 0;
    
    private TabHost tabHost = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.prog_tabhost);

        station_name = getIntent().getStringExtra("station_name");
        subStation_name = getIntent().getStringExtra("subStation_name");
        prog_name = getIntent().getStringExtra("prog_name");
        start_time = getIntent().getStringExtra("start_time");
        sub_activity = getIntent().getIntExtra("sub_activity", -1);
        
        tabHost = this.getTabHost();

        Intent intent1 = new Intent(this, Prog_Brief.class);
        intent1.putExtra("prog_name", prog_name);
        View view1 = View.inflate(Prog_TabHost.this, R.layout.prog_tabview, null);
        ((ImageView)view1.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_brief_selector);
        ((TextView)view1.findViewById(R.id.tab_textview_title)).setText(BRIEF);
        TabHost.TabSpec spec1 = tabHost.newTabSpec(BRIEF).setIndicator(view1).setContent(intent1);
        tabHost.addTab(spec1);

        Intent intent2 = new Intent(this, Prog_Comment.class);
        intent2.putExtra("prog_name", prog_name);
        View view2 = View.inflate(Prog_TabHost.this, R.layout.prog_tabview, null);
        ((ImageView)view2.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_comment_selector);
        ((TextView)view2.findViewById(R.id.tab_textview_title)).setText(COMMENT);
        TabHost.TabSpec spec2 = tabHost.newTabSpec(COMMENT).setIndicator(view2).setContent(intent2);
        tabHost.addTab(spec2);
/*
        Intent intent3 = new Intent(this, Not_Finish.class);
        intent3.putExtra("prog_name", prog_name);
        intent3.putExtra("station_name", station_name);
        View view3 = View.inflate(Prog_TabHost.this, R.layout.prog_tabview, null);
        ((ImageView)view3.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_pk_selector);
        ((TextView)view3.findViewById(R.id.tab_textview_title)).setText(PK);
        TabHost.TabSpec spec3 = tabHost.newTabSpec(PK).setIndicator(view3).setContent(intent3);
        tabHost.addTab(spec3);
*/
        Intent intent4 = new Intent(this, Invite.class);
        intent4.putExtra("prog_name", prog_name);
        intent4.putExtra("subStation_name", subStation_name);
        View view4 = View.inflate(Prog_TabHost.this, R.layout.prog_tabview, null);
        ((ImageView)view4.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_invite_selector);
        ((TextView)view4.findViewById(R.id.tab_textview_title)).setText(INVITE);
        TabHost.TabSpec spec4 = tabHost.newTabSpec(INVITE).setIndicator(view4).setContent(intent4);
        tabHost.addTab(spec4);

        Intent intent5 = new Intent(this, Reminder.class);
        intent5.putExtra("station_name", station_name);
        intent5.putExtra("subStation_name", subStation_name);
        intent5.putExtra("prog_name", prog_name);
        intent5.putExtra("start_time", start_time);
        View view5 = View.inflate(Prog_TabHost.this, R.layout.prog_tabview, null);
        ((ImageView)view5.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_remind_selector);
        ((TextView)view5.findViewById(R.id.tab_textview_title)).setText(REMIND);
        TabHost.TabSpec spec5 = tabHost.newTabSpec(REMIND).setIndicator(view5).setContent(intent5);
        tabHost.addTab(spec5);

        switch (sub_activity) {
        case Utils.PROG_TABHOST_BRIEF:
            tabHost.setCurrentTabByTag(BRIEF);
            break;
        case Utils.PROG_TABHOST_COMMENT:
            tabHost.setCurrentTabByTag(COMMENT);
            break;
/*
        case Utils.PROG_TABHOST_PK:
            tabHost.setCurrentTabByTag(PK);
            break;
*/
        case Utils.PROG_TABHOST_INVITE:
            tabHost.setCurrentTabByTag(INVITE);
            break;
        case Utils.PROG_TABHOST_REMIND:
            tabHost.setCurrentTabByTag(REMIND);
            break;
        default:
            tabHost.setCurrentTabByTag(BRIEF);
            break;
        }
    }
}