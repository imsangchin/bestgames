package cn.shidian;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class Main_TabHost extends TabActivity {
    public static final String PLAYING_PROG = "当前";
    public static final String HOT_PROG = "热点";
    public static final String GUESS_YOU_WANT = "猜你想看";
    public static final String PROG_LIST = "节目单";

    public static final String VERSION_URL = "http://shenkantv.sinaapp.com/epgVersion.php";

    public static final int GPS_OK = 0;
    public static final int GPS_ERROR = 1;

    private String versionString = "";
    private String versionStore = "";
    private TabHost tabHost = null;
    private Dialog dialog = null;
    private Location current_location = null;
    private String addr;
    private String location = "";
    
    private int sub_activity = 0;

    private MyReceiver myReceiver;
    
    @Override
    protected void onStart() {
        super.onStart();
        
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_tabhost);
        
        sub_activity = getIntent().getIntExtra("sub_activity", -1);
        SharedPreferences preferences1 = getSharedPreferences("Shidian", 0);
        location = preferences1.getString("location", "");
        
        tabHost = this.getTabHost();

        View view1 = View.inflate(Main_TabHost.this, R.layout.main_tabview, null);
        ((ImageView)view1.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_playing_selector);
        ((TextView)view1.findViewById(R.id.tab_textview_title)).setText(PLAYING_PROG);
        TabHost.TabSpec spec1 = tabHost.newTabSpec(PLAYING_PROG).setIndicator(view1).setContent(new Intent(this, Prog_Playing.class));
        tabHost.addTab(spec1);

        View view2 = View.inflate(Main_TabHost.this, R.layout.main_tabview, null);
        ((ImageView)view2.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_hot_selector);
        ((TextView)view2.findViewById(R.id.tab_textview_title)).setText(HOT_PROG);
        TabHost.TabSpec spec2 = tabHost.newTabSpec(HOT_PROG).setIndicator(view2).setContent(new Intent(this, Prog_Hot.class));
        tabHost.addTab(spec2);

        View view3 = View.inflate(Main_TabHost.this, R.layout.main_tabview, null);
        ((ImageView)view3.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_guess_selector);
        ((TextView)view3.findViewById(R.id.tab_textview_title)).setText(GUESS_YOU_WANT);
        TabHost.TabSpec spec3 = tabHost.newTabSpec(GUESS_YOU_WANT).setIndicator(view3).setContent(new Intent(this, Prog_Guess.class));
        tabHost.addTab(spec3);

        View view4 = View.inflate(Main_TabHost.this, R.layout.main_tabview, null);
        ((ImageView)view4.findViewById(R.id.tab_imageview_icon)).setImageResource(R.drawable.bottom_ico_proglist_selector);
        ((TextView)view4.findViewById(R.id.tab_textview_title)).setText(PROG_LIST);
        TabHost.TabSpec spec4 = tabHost.newTabSpec(PROG_LIST).setIndicator(view4).setContent(new Intent(this, Prog_List.class));
        tabHost.addTab(spec4);

        switch (sub_activity) {
        case Utils.MAIN_TABHOST_PLAYING:
            tabHost.setCurrentTabByTag(PLAYING_PROG);
            break;
        case Utils.MAIN_TABHOST_HOT:
            tabHost.setCurrentTabByTag(HOT_PROG);
            break;
        case Utils.MAIN_TABHOST_GUESS:
            tabHost.setCurrentTabByTag(GUESS_YOU_WANT);
            break;
        case Utils.MAIN_TABHOST_LIST:
            tabHost.setCurrentTabByTag(PROG_LIST);
            break;
        default:
            tabHost.setCurrentTabByTag(PLAYING_PROG);
            break;
        }
        
        if (location.equals("")) {
            //openGPSSettings();    //为了加快开启速度，临时注释掉。
            preferences1.edit().putString("location", "北京").commit();    //关闭GPS后，默认设置为北京。
        }

        HttpGet request = new HttpGet(VERSION_URL);
        HttpResponse response;
        try {
            response = new DefaultHttpClient().execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                versionString = EntityUtils.toString(response.getEntity(), "UTF-8");
                SharedPreferences preferences2 = getSharedPreferences("Shidian", 0);
                versionStore = preferences2.getString("epgVersion", "");
                if (!versionString.equals(versionStore)) {
                    File path = new File(Utils.store_path_EPG);
                    if (path.exists()) {
                        File[] tmp = path.listFiles();
                        for (int i = 0; i < tmp.length; i++) {
                            tmp[i].delete();
                        }
                    }
                    preferences2.edit().putString("epgVersion", versionString).commit();
                }
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

    private void openGPSSettings() {
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            dialog = ProgressDialog.show(Main_TabHost.this, "请稍候...", "正在获取GPS数据...", true);
            new Thread() {
                public void run() {
                    try {
                        Looper.prepare();
                        getLocation();
                        Thread.sleep(30000);
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    } catch (Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            return;
        }

        Toast.makeText(this, "请开启GPS！", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent, 1);
    }

    private void getLocation() {
        final LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        try {
            List<String> provide = locationManager.getAllProviders();

            for (int i = 0; i < provide.size(); i++) {
                current_location = locationManager.getLastKnownLocation(provide.get(i));
                if (current_location != null)
                    break;
            }

            for (int i = 0; i < provide.size(); i++) {
                locationManager.requestLocationUpdates(provide.get(i), 1000, 0, location_listener);
            }

            Runnable showWaitDialog = new Runnable() {
                @Override
                public void run() {
                    while (current_location == null) {}
                    if (current_location == null) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }

                    locationManager.removeUpdates(location_listener);
                    if (current_location == null) {
                        Message message = mHandler.obtainMessage(GPS_ERROR);
                        mHandler.sendMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage(GPS_OK);
                        mHandler.sendMessage(message);
                    }
                }
            };

            Thread t = new Thread(showWaitDialog);
            t.start();
        } catch (Exception e) {
            current_location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    private LocationListener location_listener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onLocationChanged(Location location) {
            current_location = location;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GPS_OK:
                try {
                    if (current_location != null) {
                        Toast.makeText(Main_TabHost.this,
                                "经度: " + current_location.getLongitude()
                                    + "\n纬度: " + current_location.getLatitude(),
                                Toast.LENGTH_SHORT).show();
                    }
                    addr = geocodeAddr(current_location.getLatitude() + "",
                            current_location.getLongitude() + "");
                    if (dialog != null) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    if (addr.equals(null)) {
                        Toast.makeText(Main_TabHost.this, "GPS定位失败", Toast.LENGTH_LONG).show();
                        return;
                    } else
                        Toast.makeText(Main_TabHost.this, addr, Toast.LENGTH_LONG).show();

                    SharedPreferences preferences = getSharedPreferences("Shidian", 0);
                    int index = 0;
                    if (addr.indexOf("黑龙江") != -1) {
                        preferences.edit().putString("location", "黑龙江").commit();
                    } else if (addr.indexOf("内蒙古") != -1) {
                        preferences.edit().putString("location", "内蒙古").commit();
                    } else if (addr.indexOf("宁夏") != -1) {
                        preferences.edit().putString("location", "宁夏").commit();
                    } else if (addr.indexOf("广西") != -1) {
                        preferences.edit().putString("location", "广西").commit();
                    } else if (addr.indexOf("新疆") != -1) {
                        preferences.edit().putString("location", "新疆").commit();
                    } else if (addr.indexOf("西藏") != -1) {
                        preferences.edit().putString("location", "西藏").commit();
                    } else if ((index = addr.indexOf("省")) != -1) {
                        preferences.edit().putString("location", addr.substring(2, index)).commit();
                    } else if ((index = addr.indexOf("市")) != -1) {
                        preferences.edit().putString("location", addr.substring(2, index)).commit();
                    }
                } catch (Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
                break;
            case GPS_ERROR:
                Toast.makeText(Main_TabHost.this, "GPS定位失败", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }
        }
    };

    public String geocodeAddr(String latitude, String longitude) {
        String addr = "";
        String url = String.format("http://ditu.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s", latitude, longitude);
        URL myURL = null;
        URLConnection httpsConn = null;
        try {
            myURL = new URL(url);
        } catch (Exception e) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (e != null) {
                e.printStackTrace();    
            }

            return null;
        }
        try {
            httpsConn = (URLConnection) myURL.openConnection();
            if (httpsConn != null) {
                InputStreamReader insr = new InputStreamReader(
                        httpsConn.getInputStream(), "UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if ((data = br.readLine()) != null) {
                    System.out.println(data);
                    String[] retList = data.split(",");
                    if (retList.length > 2 && ("200".equals(retList[0]))) {
                        addr = retList[2];
                        addr = addr.replace("\"", "");
                    } else {
                        addr = "";
                    }
                }
                insr.close();
            }
        } catch (Exception e) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (e != null) {
                e.printStackTrace();
            }

            return null;
        }
        return addr;
    }
}