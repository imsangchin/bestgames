package cn.shidian;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;

class Utils
{
	//视点应用的新浪微博AppKey和AppSecret
    public static final String SINAWEIBO_APPKEY = "752444993";
    public static final String SINAWEIBO_APPSECRET = "07e2ed9c707aac9d2c8e53dd735acc29";
    //视点应用的回调地址
    public static final String SINAWEIBO_REDIRECT_URI = "http://";
    //视点应用的新浪微博登录地址
    public static final String SINAWEIBO_LOGIN_URL = "https://api.weibo.com/oauth2/access_token";

    //微博自动重登录时间（50分钟）
    public static final long WEIBO_RELOGIN_TIME = 50 * 60 * 1000;
    
    //节目单界面里电视台类型，分别为本地台、央视台、省卫视、高清台、数字台
    public static final int STATION_TYPE_LOCAL = 0;
    public static final int STATION_TYPE_CCTV = 1;
    public static final int STATION_TYPE_SATELLATE = 2;
    public static final int STATION_TYPE_HD = 3;
    public static final int STATION_TYPE_DIGITAL = 4;

    //电视台参数类型，分别为电视台名称、电视台代码
    public static final int STATION_PARAM_NAME = 0;
    public static final int STATION_PARAM_CODE = 1;

    //第一界面各TAB所代表的VIEW，分别为在播、热点、猜你想看、节目单
    public static final int MAIN_TABHOST_PLAYING = 0;
    public static final int MAIN_TABHOST_HOT = 1;
    public static final int MAIN_TABHOST_GUESS = 2;
    public static final int MAIN_TABHOST_LIST = 3;
    
    //第二界面各TAB所代表的VIEW，分别为简介、评论、PK台（暂时废弃）、邀请、提醒
    public static final int PROG_TABHOST_BRIEF = 0;
    public static final int PROG_TABHOST_COMMENT = 1;
    public static final int PROG_TABHOST_PK = 2;
    public static final int PROG_TABHOST_INVITE = 3;
    public static final int PROG_TABHOST_REMIND = 4;

    //各主界面INTENT类型，分别为第一界面、第二界面、新评论、转发评论、回复评论
    public static final int INTENT_MAIN_TABHOST = 0;
    public static final int INTENT_PROG_TABHOST = 1;
    public static final int INTENT_NEW_COMMENT = 2;
    public static final int INTENT_REPOST_COMMENT = 3;
    public static final int INTENT_REPLY_COMMENT = 4;

    //微博登录结果类型，分别为成功、密码错误、网络错误、过程处理错误
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_PASSWORD_ERROR = 1;
    public static final int LOGIN_NETWORK_ERROR = 2;
    public static final int LOGIN_PROCESS_ERROR = 3;

    //简介及评论最大缓存数
    public static final int MAX_CACHE_BRIEF_COUNT = 24;
    public static final int MAX_CACHE_COMMENT_COUNT = 24;

    //通过INTENT类型获取INTENT
    public static Intent getIntentFromType(int intentType, Context context) {
        Intent intent = null;
        switch (intentType) {
        case INTENT_MAIN_TABHOST:
            intent = new Intent(context, Main_TabHost.class);
            break;
        case INTENT_PROG_TABHOST:
            intent = new Intent(context, Prog_TabHost.class);
            break;
        case INTENT_NEW_COMMENT:
            intent = new Intent(context, New_Comment.class);
            break;
        case INTENT_REPOST_COMMENT:
            intent = new Intent(context, Repost_Comment.class);
            break;
        case INTENT_REPLY_COMMENT:
            intent = new Intent(context, Reply_Comment.class);
            break;
        }
        
        return intent;
    }

    //登录操作
    public static int DoLogin(Context context, String username, String password) {
        int result = Utils.LOGIN_SUCCESS;

        //使用POST方式进行HTTPS通信
        Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
        Protocol.registerProtocol("https", myhttps);
        String dataReturn1 = "";
        HttpClient client1 = new HttpClient();
        PostMethod postMethod = new PostMethod(Utils.SINAWEIBO_LOGIN_URL);
        postMethod.addParameter("client_id", Utils.SINAWEIBO_APPKEY);
        postMethod.addParameter("client_secret", Utils.SINAWEIBO_APPSECRET);
        postMethod.addParameter("grant_type", "password");
        postMethod.addParameter("redirect_uri", Utils.SINAWEIBO_REDIRECT_URI);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        try {
            client1.executeMethod(postMethod);
            dataReturn1 = postMethod.getResponseBodyAsString();
            JSONObject jsonObject1 = new JSONObject(dataReturn1);
            //获取access_token
            String access_token = jsonObject1.getString("access_token");

            //记录下本次登录时间，以便计算自动重登录的时间
            Calendar calendar = Calendar.getInstance();
            long login_time = calendar.getTimeInMillis();
            
            //将登录相关参数存入SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
            preferences.edit().putString("access_token", access_token).commit();
            preferences.edit().putString("login_username", username).commit();
            preferences.edit().putString("login_password", password).commit();
            preferences.edit().putLong("login_time", login_time).commit();
            
            //获取用户的基本信息
            HttpClient client2 = new HttpClient();
            String urlString1 = "https://api.weibo.com/2/account/profile/basic.json?access_token=" + access_token;
            GetMethod getMethod1 = new GetMethod(urlString1);

            client2.executeMethod(getMethod1);
            String dataReturn2 = getMethod1.getResponseBodyAsString();
            JSONObject jsonObject2 = new JSONObject(dataReturn2);

            String profile_image_url = jsonObject2.getString("profile_image_url");
            String screen_name = jsonObject2.getString("screen_name");
            String user_id = jsonObject2.getString("id");
            String location = jsonObject2.getString("location");
            preferences.edit().putString("user_image_url", profile_image_url).commit();
            preferences.edit().putString("screen_name", screen_name).commit();
            preferences.edit().putString("user_id", user_id).commit();
            preferences.edit().putString("user_location", location).commit();

            //获取用户的附加信息
            HttpClient client3 = new HttpClient();
            String urlString2 = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + user_id;
            GetMethod getMethod2 = new GetMethod(urlString2);

            client3.executeMethod(getMethod2);
            String dataReturn3 = getMethod2.getResponseBodyAsString();
            JSONObject jsonObject3 = new JSONObject(dataReturn3);

            int user_care_count = jsonObject3.getInt("friends_count");
            int user_fans_count = jsonObject3.getInt("followers_count");
            int user_weibo_count = jsonObject3.getInt("statuses_count");
            preferences.edit().putInt("user_care_count", user_care_count).commit();
            preferences.edit().putInt("user_fans_count", user_fans_count).commit();
            preferences.edit().putInt("user_weibo_count", user_weibo_count).commit();
        } catch (JSONException e) {
            result = Utils.LOGIN_PASSWORD_ERROR;
        } catch (HttpException e) {
            result = Utils.LOGIN_NETWORK_ERROR;
        } catch (IOException e) {
            result = Utils.LOGIN_PROCESS_ERROR;
        }

        return result;
    }
    
    //获取SD卡中的Shidian目录，作为视点应用的数据总路径
    public static String store_path = Environment.getExternalStorageDirectory().getAbsolutePath()
                                        + File.separator
                                        + "Shidian"
                                        + File.separator;

    //获取SD卡中的Shidian/EPG目录，作为视点应用的节目单数据存放路径
    public static String store_path_EPG = Environment.getExternalStorageDirectory().getAbsolutePath()
                                        + File.separator
                                        + "Shidian"
                                        + File.separator
                                        + "EPG"
                                        + File.separator;

    //获取SD卡中的Shidian/Brief目录，作为视点应用的简介数据存放路径
    public static String store_path_Brief = Environment.getExternalStorageDirectory().getAbsolutePath()
                                        + File.separator
                                        + "Shidian"
                                        + File.separator
                                        + "Brief"
                                        + File.separator;

    //获取SD卡中的Shidian/Comment目录，作为视点应用的评论数据存放路径
    public static String store_path_Comment = Environment.getExternalStorageDirectory().getAbsolutePath()
                                        + File.separator
                                        + "Shidian"
                                        + File.separator
                                        + "Comment"
                                        + File.separator;

    //各电视台名称与代码的映射关系
    //1. 本地台
    //北京
    public static final String[][] BEIJING_STATION = {
        {"北京卫视", "BTV1"}, {"北京电视台文艺频道", "BTV2"}, {"北京电视台科教频道", "BTV3"},
        {"北京电视台影视频道", "BTV4"}, {"北京电视台财经频道", "BTV5"}, {"北京电视台体育频道", "BTV6"},
        {"北京电视台生活频道", "BTV7"}, {"北京电视台青少频道", "BTV8"}, {"北京电视台公共频道", "BTV9"},
        {"北京电视台卡通频道", "BTV10"}, {"北京电视台国际频道", "BTV11"}
    };

    //上海
    public static final String[][] SHANGHAI_STATION = {
        {"东方卫视", "DONGFANG1"}, {"东方卫视海外版", "DONGFANG2"}, {"东方电影电视台", "DFMV1"},
        {"上海炫动电视台", "TOONMAX1"}, {"上海电视台新闻频道", "SHHAI1"}, {"上海电视台第一财经频道", "SHHAI2"},
        {"上海电视台生活时尚频道", "SHHAI3"}, {"上海电视台电视剧频道", "SHHAI4"}, {"上海电视台体育频道", "SHHAI5"},
        {"上海电视台纪实频道", "SHHAI6"}, {"上海电视台娱乐频道", "SHHAI7"}, {"上海电视台艺术人文频道", "SHHAI8"},
        {"上海电视台外语频道", "SHHAI9"}, {"上海电视台戏剧频道", "SHHAI10"}, {"上海电视台哈哈少儿频道", "SHHAI11"},
        {"上海教育电视台", "SHEDU1"}, {"SITV动漫秀场", "SITV1"}, {"SITV游戏风云", "SITV2"},
        {"SITV法治天地", "SITV3"}, {"SITV劲爆体育", "SITV4"}, {"SITV魅力音乐", "SITV5"},
        {"SITV金色频道", "SITV6"}, {"SITV欢笑剧场", "SITV7"}, {"SITV七彩戏剧", "SITV8"},
        {"SITV极速汽车", "SITV9"}, {"SITV生活时尚", "SITV10"}, {"SITV全纪实", "SITV11"},
        {"SITV都市剧场", "SITV12"}, {"SITV卫生健康", "SITV13"}, {"SITV东方财经", "SITV14"},
        {"SITV经典剧场", "SITV15"}, {"SITV海外影院", "SITV16"}, {"SITV白金剧场", "SITV17"},
        {"SITV首映剧场", "SITV18"}, {"SITV娱乐前线", "SITV19"}, {"SITV学习考试", "SITV20"},
        {"SITV生活时尚(上海)", "SITV21"}, {"SITV英语教室", "SITV22"}, {"SITV五星体育", "SITV23"},
        {"SITV劲爆足球", "SITV24"}, {"SITV资讯气象", "SITV25"}, {"SITV玩具益智", "SITV26"},
        {"SITV武术世界", "SITV27"}, {"SITV美食天府", "SITV28"}, {"SITV幸福彩", "SITV29"},
        {"SITV高清频道", "SITV30"}
    };

    //天津
    public static final String[][] TIANJIN_STATION = {
        {"天津卫视", "TJTV1"}, {"天津电视台滨海频道", "TJTV2"}, {"天津电视台文化娱乐频道", "TJTV3"},
        {"天津电视台影视频道", "TJTV4"}, {"天津电视台都市频道", "TJTV5"}, {"天津电视台体育频道", "TJTV6"},
        {"天津电视台科教频道", "TJTV7"}, {"天津电视台少儿频道", "TJTV8"}, {"天津电视台公共频道", "TJTV9"}
    };

    //重庆
    public static final String[][] CHONGQING_STATION = {
        {"重庆卫视", "CCQTV1"}, {"重庆电视台影视频道", "CCQTV2"}, {"重庆电视台新闻频道", "CCQTV3"},
        {"重庆电视台科教频道", "CCQTV4"}, {"重庆电视台都市频道", "CCQTV5"}, {"重庆电视台娱乐频道", "CCQTV6"},
        {"重庆电视台生活频道", "CCQTV7"}, {"重庆电视台时尚频道", "CCQTV8"}, {"重庆电视台公共频道", "CCQTV9"},
        {"重庆电视台青少频道", "CCQTV10"}, {"重庆电视台国际频道", "CCQTV11"}
    };

    //广东
    public static final String[][] GUANGDONG_STATION = {
        {"广东卫视", "GDTV1"}, {"广东电视台珠江频道", "GDTV2"}, {"广东电视台体育频道", "GDTV3"},
        {"广东电视台公共频道", "GDTV4"}, {"广东电视台珠江频道海外版", "GDTV5"}, {"广东电视台新闻频道", "GDTV6"},
        {"嘉佳卡通频道", "GDTV7"}, {"广东电视台国际频道", "GDTV8"}
    };

    //福建
    public static final String[][] FUJIAN_STATION = {
        {"福建电视台综合频道", "FJTV1"},  {"东南卫视", "FJTV2"}, {"福建电视台公共频道", "FJTV3"},
        {"福建电视台新闻频道", "FJTV4"},  {"福建电视台电视剧频道", "FJTV5"}, {"福建电视台都市时尚频道", "FJTV6"},
        {"福建电视台经济生活频道", "FJTV7"},  {"福建电视台体育频道", "FJTV8"}, {"福建电视台少儿频道", "FJTV9"}
    };

    //广西
    public static final String[][] GUANGXI_STATION = {
        {"广西卫视", "GUANXI1"},  {"广西电视台文体频道", "GUANXI2"}, {"广西电视台生活频道", "GUANXI3"},
        {"广西电视台公共频道", "GUANXI4"},  {"广西电视台影视频道", "GUANXI5"}, {"广西电视台体育频道", "GUANXI6"},
        {"广西电视台经济频道", "GUANXI7"}
    };

    //旅游（海南）
    public static final String[][] HAINAN_STATION = {
        {"旅游卫视", "TCTC1"}
    };

    //浙江
    public static final String[][] ZHEJIANG_STATION = {
        {"浙江卫视", "ZJTV1"},  {"浙江电视台钱江都市频道", "ZJTV2"}, {"浙江电视台经济生活频道", "ZJTV3"},
        {"浙江电视台教育科技频道", "ZJTV4"},  {"浙江电视台影视娱乐频道", "ZJTV5"}, {"浙江电视台民生休闲频道", "ZJTV6"},
        {"浙江电视台公共新农村频道", "ZJTV7"},  {"浙江电视台少儿频道", "ZJTV8"}, {"浙江电视台国际频道", "ZJTV9"}
    };

    //江苏
    public static final String[][] JIANGSU_STATION = {
        {"江苏卫视", "JSTV1"},  {"江苏电视台综艺频道", "JSTV2"}, {"江苏电视台城市频道", "JSTV3"},
        {"江苏电视台影视频道", "JSTV4"},  {"江苏电视台靓妆频道", "JSTV5"}, {"江苏电视台体育频道", "JSTV6"},
        {"江苏电视台优漫卡通频道", "JSTV7"},  {"江苏电视台公共频道", "JSTV8"}, {"江苏教育电视台", "JSTV9"},
        {"江苏电视台国际频道", "JSTV10"}
    };

    //安徽
    public static final String[][] ANHUI_STATION = {
        {"安徽卫视", "AHTV1"},  {"安徽电视台经济生活频道", "AHTV2"}, {"安徽电视台影视频道", "AHTV3"},
        {"安徽电视台综艺频道", "AHTV4"},  {"安徽电视台公共频道", "AHTV5"}, {"安徽电视台科教频道", "AHTV6"},
        {"安徽电视台人物频道", "AHTV7"},  {"安徽电视台国际频道", "AHTV8"}
    };

    //四川
    public static final String[][] SICHUAN_STATION = {
        {"四川卫视", "SCTV1"},  {"四川电视台文化旅游频道", "SCTV2"}, {"四川电视台经济频道", "SCTV3"},
        {"四川电视台新闻资讯频道", "SCTV4"},  {"四川电视台影视文艺频道", "SCTV5"}, {"四川电视台星空购物频道", "SCTV6"},
        {"四川电视台妇女儿童频道", "SCTV7"},  {"四川电视台峨嵋电影频道", "SCTV8"}, {"四川电视台公共频道", "SCTV9"}
    };

    //云南
    public static final String[][] YUNNAN_STATION = {
        {"云南卫视", "YNTV1"},  {"云南电视台经济生活频道", "YNTV2"}, {"云南电视台旅游民族频道", "YNTV3"},
        {"云南电视台体育娱乐频道", "YNTV4"},  {"云南电视台影视剧频道", "YNTV5"}, {"云南电视台公共频道", "YNTV6"},
        {"云南电视台少儿频道", "YNTV7"}
    };

    //贵州
    public static final String[][] GUIZHOU_STATION = {
        {"贵州卫视", "GUIZOUTV1"},  {"贵州电视台二频道", "GUIZOUTV2"}, {"贵州电视台三频道", "GUIZOUTV3"},
        {"贵州电视台四频道", "GUIZOUTV4"},  {"贵州电视台五频道", "GUIZOUTV5"}, {"贵州电视台六频道", "GUIZOUTV6"},
        {"贵州电视台摄影频道", "GUIZOUTV7"},  {"贵州电视台天元围棋频道", "GUIZOUTV8"}
    };

    //湖北
    public static final String[][] HUBEI_STATION = {
        {"湖北卫视", "HUBEI1"},  {"湖北电视台综合频道", "HUBEI2"}, {"湖北电视台影视频道", "HUBEI3"},
        {"湖北电视台教育频道", "HUBEI4"},  {"湖北电视台体育频道", "HUBEI5"}, {"湖北电视台都市频道", "HUBEI6"},
        {"湖北电视台公共频道", "HUBEI7"},  {"湖北电视台经济频道", "HUBEI8"}
    };

    //湖南
    public static final String[][] HUNAN_STATION = {
        {"湖南卫视", "HUNANTV1"},  {"湖南电视台金鹰卡通频道", "HUNANTV2"}, {"湖南电视台娱乐频道", "HUNANTV3"},
        {"湖南电视台电视剧频道", "HUNANTV4"},  {"湖南电视台公共频道", "HUNANTV5"}, {"湖南电视台潇湘电影频道", "HUNANTV6"},
        {"湖南电视台国际频道", "HUNANTV7"}
    };

    //河南
    public static final String[][] HENAN_STATION = {
        {"河南卫视", "HNTV1"},  {"河南电视台都市频道", "HNTV2"}, {"河南电视台民生频道", "HNTV3"},
        {"河南电视台法制频道", "HNTV4"},  {"河南电视台电视剧频道", "HNTV5"}, {"河南电视台新闻频道", "HNTV6"},
        {"河南电视台商务信息频道", "HNTV7"},  {"河南电视台公共频道", "HNTV8"}, {"河南电视台新农村频道", "HNTV9"}
    };    

    //江西
    public static final String[][] JIANGXI_STATION = {
        {"江西卫视", "JXTV1"},  {"江西电视台都市频道", "JXTV2"}, {"江西电视台经济生活频道", "JXTV3"},
        {"江西电视台影视频道", "JXTV4"},  {"江西电视台公共频道", "JXTV5"}, {"江西电视台少儿家庭频道", "JXTV6"}
    };

    //河北
    public static final String[][] HEBEI_STATION = {
        {"河北卫视", "HEBEI1"},  {"河北电视台经济生活频道", "HEBEI2"}, {"河北电视台都市频道", "HEBEI3"},
        {"河北电视台影视频道", "HEBEI4"},  {"河北电视台少儿科教频道", "HEBEI5"}, {"河北电视台公共频道", "HEBEI6"},
        {"河北电视台农民频道", "HEBEI7"}
    };

    //山东
    public static final String[][] SHANDONG_STATION = {
        {"山东卫视", "SDTV1"},  {"山东电视台齐鲁频道", "SDTV2"}, {"山东电视台体育频道", "SDTV3"},
        {"山东电视台农科频道", "SDTV4"},  {"山东电视台公共频道", "SDTV5"}, {"山东电视台少儿频道", "SDTV6"},
        {"山东电视台影视频道", "SDTV7"},  {"山东电视台综艺频道", "SDTV8"}, {"山东电视台生活频道", "SDTV9"}
    };

    //山西
    public static final String[][] SHANXI_STATION = {
        {"山西卫视", "SXTV1"},  {"山西电视台经济资讯频道", "SXTV2"}, {"山西电视台影视频道", "SXTV3"},
        {"山西电视台科教频道", "SXTV4"},  {"山西电视台公共频道", "SXTV5"}
    };

    //内蒙古
    public static final String[][] NEIMENGGU_STATION = {
        {"内蒙古卫视", "NMGTV1"},  {"内蒙古电视台新闻综合频道", "NMGTV2"}, {"内蒙古电视台经济生活频道", "NMGTV3"},
        {"内蒙古电视台文体娱乐频道", "NMGTV4"},  {"内蒙古电视台影视剧频道", "NMGTV5"}, {"内蒙古电视台少儿频道", "NMGTV6"},
        {"内蒙古电视台蒙语卫视频道", "NMGTV7"}
    };

    //新疆
    public static final String[][] XINJIANG_STATION = {
        {"新疆卫视", "XJTV1"},  {"新疆电视台维语新闻综合频道", "XJTV2"}, {"新疆电视台哈萨克语新闻综合频道", "XJTV3"},
        {"新疆电视台汉语综艺频道", "XJTV4"},  {"新疆电视台维语综艺频道", "XJTV5"}, {"新疆电视台汉语影视频道", "XJTV6"},
        {"新疆电视台汉语经济生活频道", "XJTV7"},  {"新疆电视台哈语综艺频道", "XJTV8"}, {"新疆电视台维语经济生活频道", "XJTV9"},
        {"新疆电视台体育健康频道", "XJTV10"},  {"新疆电视台法制信息频道", "XJTV11"}, {"新疆电视台少儿频道", "XJTV12"}
    };

    //西藏
    public static final String[][] XIZANG_STATION = {
        {"西藏电视台藏语卫视", "XIZANGTV1"},  {"西藏电视台汉语卫视", "XIZANGTV2"}, {"西藏电视台影视文化频道", "XIZANGTV3"}
    };

    //青海
    public static final String[][] QINGHAI_STATION = {
        {"青海卫视", "QHTV1"},  {"青海电视台经济生活频道", "QHTV2"}, {"青海电视台综合频道", "QHTV3"},
        {"青海电视台影视综艺频道", "QHTV4"}
    };

    //甘肃
    public static final String[][] GANSU_STATION = {
        {"甘肃卫视", "GSTV1"},  {"甘肃电视台经济频道", "GSTV2"}, {"甘肃电视台少儿频道", "GSTV3"},
        {"甘肃电视台公共频道", "GSTV4"},  {"甘肃电视台都市频道", "GSTV5"}, {"甘肃电视台影视频道", "GSTV6"}
    };

    //宁夏
    public static final String[][] NINGXIA_STATION = {
        {"宁夏电视台公共频道", "NXTV1"},  {"宁夏卫视", "NXTV2"}, {"宁夏电视台经济频道", "NXTV3"},
        {"宁夏电视台影视频道", "NXTV4"},  {"宁夏电视台少儿频道", "NXTV5"}
    };

    //陕西
    public static final String[][] SHAANXI_STATION = {
        {"陕西卫视", "SHXITV1"},  {"陕西电视台新闻综合频道", "SHXITV2"}, {"陕西电视台都市青春频道", "SHXITV3"},
        {"陕西电视台家庭生活频道", "SHXITV4"},  {"陕西电视台影视娱乐频道", "SHXITV5"}, {"陕西电视台公共政法频道", "SHXITV6"},
        {"陕西电视台乐家电视购物频道", "SHXITV7"},  {"陕西电视台体育健康频道", "SHXITV8"}, {"陕西电视台农林科技卫视", "SHXITV9"}
    };

    //黑龙江
    public static final String[][] HEILONGJIAN_STATION = {
        {"黑龙江卫视", "HLJTV1"},  {"黑龙江电视台影视频道", "HLJTV2"}, {"黑龙江电视台文艺频道", "HLJTV3"},
        {"黑龙江电视台都市频道", "HLJTV4"},  {"黑龙江电视台法制频道", "HLJTV5"}, {"黑龙江电视台公共频道", "HLJTV6"},
        {"黑龙江电视台少儿频道", "HLJTV7"}
    };

    //吉林
    public static final String[][] JILIN_STATION = {
        {"吉林卫视", "JILIN1"},  {"吉林电视台都市频道", "JILIN2"}, {"吉林电视台生活频道", "JILIN3"},
        {"吉林电视台影视频道", "JILIN4"},  {"吉林电视台乡村频道", "JILIN5"}, {"吉林电视台公共频道", "JILIN6"},
        {"吉林电视台法制频道", "JILIN7"}
    };

    //辽宁
    public static final String[][] LIAONING_STATION = {
        {"辽宁卫视", "LNTV1"},  {"辽宁电视台都市频道", "LNTV2"}, {"辽宁电视台影视娱乐频道", "LNTV3"},
        {"辽宁电视台宜佳购物频道", "LNTV4"},  {"辽宁电视台青少频道", "LNTV5"}, {"辽宁电视台生活频道", "LNTV6"},
        {"辽宁电视台公共频道", "LNTV7"},  {"辽宁电视台北方频道", "LNTV8"}
    };

    //2. 央视台
    public static final String[][] CCTV_STATION = {
        {"中央电视台综合频道", "CCTV1"}, {"中央电视台经济频道", "CCTV2"}, {"中央电视台综艺频道", "CCTV3"},
        {"中央电视台中文国际频道", "CCTV4"}, {"中央电视台体育频道", "CCTV5"}, {"中央电视台电影频道", "CCTV6"},
        {"中央电视台军事农业频道", "CCTV7"}, {"中央电视台电视剧频道", "CCTV8"}, {"中央电视台纪录频道", "CCTV9"},
        {"中央电视台科教频道", "CCTV10"}, {"中央电视台戏曲频道", "CCTV11"}, {"中央电视台法制频道", "CCTV12"},
        {"中央电视台新闻频道", "CCTV13"}, {"中央电视台少儿频道", "CCTV15"}, {"中央电视台音乐频道", "CCTV16"},
        {"中央电视台西语频道", "CCTV17"}, {"CCTV-纪录频道（英文）", "CCTV18"}, {"CCTV-NEWS", "CCTV19"},
        {"CCTV-4欧洲频道", "CCTV68"}, {"CCTV-4美洲频道", "CCTV69"}, {"中央电视台法语频道", "CCTV95"}
    };

    //3. 省卫视
    public static final String[][] SATELLITE_STATION = {
        {"北京卫视", "BTV1"}, {"东方卫视", "DONGFANG1"}, {"天津卫视", "TJTV1"},
        {"重庆卫视", "CCQTV1"}, {"广东卫视", "GDTV1"}, {"东南卫视", "FJTV2"},
        {"广西卫视", "GUANXI1"}, {"旅游卫视", "TCTC1"}, {"浙江卫视", "ZJTV1"},
        {"江苏卫视", "JSTV1"}, {"安徽卫视", "AHTV1"}, {"四川卫视", "SCTV1"},
        {"云南卫视", "YNTV1"}, {"贵州卫视", "GUIZOUTV1"}, {"湖北卫视", "HUBEI1"},
        {"湖南卫视", "HUNANTV1"}, {"河南卫视", "HNTV1"}, {"江西卫视", "JXTV1"},
        {"河北卫视", "HEBEI1"}, {"山东卫视", "SDTV1"}, {"山西卫视", "SXTV1"},
        {"内蒙古卫视", "NMGTV1"}, {"新疆卫视", "XJTV1"}, {"西藏汉语卫视", "XIZANGTV2"},
        {"青海卫视", "QHTV1"}, {"甘肃卫视", "GSTV1"}, {"宁夏卫视", "NXTV2"},
        {"陕西卫视", "SHXITV1"}, {"黑龙江卫视", "HLJTV1"}, {"吉林卫视", "JILIN1"},
        {"辽宁卫视", "LNTV1"}
    };

    //4. 高清台
    public static final String[][] HD_STATION = {
        {"央视高清影视频道", "CCTVHD1"}
    };

    //5. 数字台
    public static final String[][] DIGITAL_STATION = {
        {"风云足球", "CCTVPAYFEE1"}, {"风云音乐", "CCTVPAYFEE2"}, {"第一剧场", "CCTVPAYFEE3"},
        {"风云剧场", "CCTVPAYFEE4"}, {"世界地理", "CCTVPAYFEE5"}, {"电视指南", "CCTVPAYFEE6"},
        {"怀旧剧场", "CCTVPAYFEE7"}, {"国防军事", "CCTVPAYFEE8"}, {"女性时尚", "CCTVPAYFEE9"},
        {"CCTV娱乐", "CCTVPAYFEE10"}, {"CCTV戏曲", "CCTVPAYFEE11"}, {"CCTV电影", "CCTVPAYFEE12"},
        {"高尔夫网球", "CCTVPAYFEE13"}, {"央视精品", "CCTVPAYFEE14"}, {"中视购物", "CCTVPAYFEE15"},
        {"彩民在线", "CCTVPAYFEE16"}, {"法律服务", "CCTVPAYFEE17"}, {"高尔夫", "CCTVPAYFEE18"},
        {"CCTV靓妆", "CCTVPAYFEE19"}, {"CCTV梨园", "CCTVPAYFEE20"}, {"CCTV气象", "CCTVPAYFEE21"},
        {"CCTV汽摩", "CCTVPAYFEE22"}, {"CCTV老年福", "CCTVPAYFEE23"}, {"留学世界", "CCTVPAYFEE24"},
        {"青年学苑", "CCTVPAYFEE25"}, {"摄影频道", "CCTVPAYFEE26"}, {"天元围棋", "CCTVPAYFEE27"},
        {"先锋纪录", "CCTVPAYFEE28"}, {"现代女性", "CCTVPAYFEE29"}, {"英语辅导", "CCTVPAYFEE30"},
        {"游戏竞技", "CCTVPAYFEE31"}, {"孕育指南", "CCTVPAYFEE32"}, {"早期教育", "CCTVPAYFEE33"},
        {"CCTV证券资讯", "CCTVPAYFEE34"}, {"中学生频道", "CCTVPAYFEE35"}, {"央视台球", "CCTVPAYFEE36"},
        {"中国教育电视台一套", "CETV1"}, {"中国教育电视台二套", "CETV2"}, {"中国教育电视台三套", "CETV3"},
        {"中国教育电视台四套", "CETV4"}, {"CHC家庭影院", "CHC1"}, {"CHC动作电影", "CHC2"},
        {"CHC高清电影", "CHC3"}
    };

    //由月份的英文缩写获取数字代码
    public static String GetMonth(String monthText) {
        String result = "";

        if (monthText.equals("Jan")) {
            result = "01";
        } else if (monthText.equals("Feb")) {
            result = "02";
        } else if (monthText.equals("Mar")) {
            result = "03";
        } else if (monthText.equals("Apr")) {
            result = "04";
        } else if (monthText.equals("May")) {
            result = "05";
        } else if (monthText.equals("Jun")) {
            result = "06";
        } else if (monthText.equals("Jul")) {
            result = "07";
        } else if (monthText.equals("Aug")) {
            result = "08";
        } else if (monthText.equals("Sep")) {
            result = "09";
        } else if (monthText.equals("Oct")) {
            result = "10";
        } else if (monthText.equals("Nov")) {
            result = "11";
        } else if (monthText.equals("Dec")) {
            result = "12";
        }

        return result;
    }

    //由微博的"createAt"字段获取数字格式字符串
    public static String GetFormattedStringFromCreateAtString(String createAtString) {
        String result = "";
        String monthText = "";
        String month = "";
        String day = "";
        String hour = "";
        String minute = "";

        monthText = createAtString.substring(4, 4 + 3);
        month = GetMonth(monthText);
        day = createAtString.substring(8, 8 + 2);
        hour = createAtString.substring(11, 11 + 2);
        minute = createAtString.substring(14, 14 + 2);
        result = month + "-" + day + " " + hour + ":" + minute;

        return result;
    }

    //由电视台名称获取电视台图标
    public static int GetStationIcoFromStationName (String station_name, String subStation_name) {
        int result = R.drawable.icon;
        
        if (station_name.indexOf("北京") != -1) {
            if (subStation_name.indexOf("卫视") != -1) {
                result = R.drawable.station_ico_btv1;
            } else if (subStation_name.indexOf("文艺") != -1) {
                result = R.drawable.station_ico_btv2;
            } else if (subStation_name.indexOf("科教") != -1) {
                result = R.drawable.station_ico_btv3;
            } else if (subStation_name.indexOf("影视") != -1) {
                result = R.drawable.station_ico_btv4;
            } else if (subStation_name.indexOf("财经") != -1) {
                result = R.drawable.station_ico_btv5;
            } else if (subStation_name.indexOf("体育") != -1) {
                result = R.drawable.station_ico_btv6;
            } else if (subStation_name.indexOf("生活") != -1) {
                result = R.drawable.station_ico_btv7;
            } else if (subStation_name.indexOf("青少") != -1) {
                result = R.drawable.station_ico_btv8;
            } else if (subStation_name.indexOf("公共") != -1) {
                result = R.drawable.station_ico_btv9;
            } else if (subStation_name.indexOf("卡通") != -1) {
                result = R.drawable.station_ico_btv10;
            } else if (subStation_name.indexOf("国际") != -1) {
                result = R.drawable.station_ico_btv1;
            }
        } else if (station_name.indexOf("视点") != -1) {
            result = R.drawable.icon;
        } else if (station_name.indexOf("安徽") != -1) {
            result = R.drawable.station_ico_anhui;
        } else if (station_name.indexOf("重庆") != -1) {
            result = R.drawable.station_ico_chongqing;
        } else if (station_name.indexOf("东方") != -1) {
            result = R.drawable.station_ico_dongfang;
        } else if (station_name.indexOf("东南") != -1) {
            result = R.drawable.station_ico_dongnan;
        } else if (station_name.indexOf("甘肃") != -1) {
            result = R.drawable.station_ico_gansu;
        } else if (station_name.indexOf("广东") != -1) {
            result = R.drawable.station_ico_guangdong;
        } else if (station_name.indexOf("广西") != -1) {
            result = R.drawable.station_ico_guangxi;
        } else if (station_name.indexOf("贵州") != -1) {
            result = R.drawable.station_ico_guizhou;
        } else if (station_name.indexOf("河北") != -1) {
            result = R.drawable.station_ico_hebei;
        } else if (station_name.indexOf("黑龙江") != -1) {
            result = R.drawable.station_ico_heilongjiang;
        } else if (station_name.indexOf("河南") != -1) {
            result = R.drawable.station_ico_henan;
        } else if (station_name.indexOf("湖北") != -1) {
            result = R.drawable.station_ico_hubei;
        } else if (station_name.indexOf("湖南") != -1) {
            result = R.drawable.station_ico_hunan;
        } else if (station_name.indexOf("江苏") != -1) {
            result = R.drawable.station_ico_jiangsu;
        } else if (station_name.indexOf("江西") != -1) {
            result = R.drawable.station_ico_jiangxi;
        } else if (station_name.indexOf("吉林") != -1) {
            result = R.drawable.station_ico_jilin;
        } else if (station_name.indexOf("辽宁") != -1) {
            result = R.drawable.station_ico_liaoning;
        } else if (station_name.indexOf("旅游") != -1) {
            result = R.drawable.station_ico_lvyou;
        } else if (station_name.indexOf("内蒙古") != -1) {
            result = R.drawable.station_ico_neimenggu;
        } else if (station_name.indexOf("宁夏") != -1) {
            result = R.drawable.station_ico_ningxia;
        } else if (station_name.indexOf("青海") != -1) {
            result = R.drawable.station_ico_qinghai;
        } else if (station_name.indexOf("陕西") != -1) {
            result = R.drawable.station_ico_shaanxi;
        } else if (station_name.indexOf("山西") != -1) {
            result = R.drawable.station_ico_shanxi;
        } else if (station_name.indexOf("山东") != -1) {
            result = R.drawable.station_ico_shandong;
        } else if (station_name.indexOf("四川") != -1) {
            result = R.drawable.station_ico_sichuan;
        } else if (station_name.indexOf("天津") != -1) {
            result = R.drawable.station_ico_tianjing;
        } else if (station_name.indexOf("新疆") != -1) {
            result = R.drawable.station_ico_xinjiang;
        } else if (station_name.indexOf("西藏") != -1) {
            result = R.drawable.station_ico_xizang;
        } else if (station_name.indexOf("云南") != -1) {
            result = R.drawable.station_ico_yunnan;
        } else if (station_name.indexOf("浙江") != -1) {
            result = R.drawable.station_ico_zhejiang;
        } else if (station_name.indexOf("中央") != -1) {
            if (subStation_name.indexOf("CCTV-10") != -1) {
                result = R.drawable.station_ico_cctv10;
            } else if (subStation_name.indexOf("CCTV-11") != -1) {
                result = R.drawable.station_ico_cctv11;
            } else if (subStation_name.indexOf("CCTV-12") != -1) {
                result = R.drawable.station_ico_cctv12;
            } else if (subStation_name.indexOf("CCTV-1") != -1) {
                result = R.drawable.station_ico_cctv1;
            } else if (subStation_name.indexOf("CCTV-2") != -1) {
                result = R.drawable.station_ico_cctv2;
            } else if (subStation_name.indexOf("CCTV-3") != -1) {
                result = R.drawable.station_ico_cctv3;
            } else if (subStation_name.indexOf("CCTV-4") != -1) {
                result = R.drawable.station_ico_cctv4;
            } else if (subStation_name.indexOf("CCTV-5") != -1) {
                result = R.drawable.station_ico_cctv5;
            } else if (subStation_name.indexOf("CCTV-6") != -1) {
                result = R.drawable.station_ico_cctv6;
            } else if (subStation_name.indexOf("CCTV-7") != -1) {
                result = R.drawable.station_ico_cctv7;
            } else if (subStation_name.indexOf("CCTV-8") != -1) {
                result = R.drawable.station_ico_cctv8;
            } else if (subStation_name.indexOf("CCTV-9") != -1) {
                result = R.drawable.station_ico_cctv9;
            } else if (subStation_name.indexOf("新闻") != -1) {
                result = R.drawable.station_ico_cctv13;
            } else if (subStation_name.indexOf("少儿") != -1) {
                result = R.drawable.station_ico_cctv14;
            } else if (subStation_name.indexOf("音乐") != -1) {
                result = R.drawable.station_ico_cctv15;
            } else if (subStation_name.indexOf("纪录") != -1
                    && subStation_name.indexOf("英文") != -1) {
                result = R.drawable.station_ico_cctv9;
            } else if (subStation_name.indexOf("NEWS") != -1) {
                result = R.drawable.station_ico_cctv_news;
            } else {
                result = R.drawable.station_ico_cctv1;
            }
        }

        return result;
    }

    //由日期时间字符串获取calendar
    public static Calendar GetCalendarFromString(String str) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = (Date)sf.parse(str);
            calendar.setTime(date);
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        
        return calendar;
    }

    //由calendar获取日期时间字符串
    public static String GetStringFromCalendarWithoutSecond(Calendar calendar) {
        String str = "";
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        str = sf.format(calendar.getTime()); 
        return str;
    }

    //由日期时间字符串获取某日时间字符串
    public static String GetShortStringFromDateTimeString(String str) {
        String newstr = "";
        Calendar calendar = GetCalendarFromString(str);
        Calendar current_calendar = Calendar.getInstance();
        SimpleDateFormat sf2;
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd");
        if (sf1.format(calendar.getTime()).equals(sf1.format(current_calendar.getTime()))) {
            sf2 = new SimpleDateFormat("今日 HH:mm");
        } else {
            sf2 = new SimpleDateFormat("d日 HH:mm");
        }
        newstr = sf2.format(calendar.getTime());
        
        return newstr;
    }

    //将提醒列表存入SharedPreferences中
    public static void SaveReminderListToStored(Context context, ArrayList<Map<String, Object>> list) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("station_name", list.get(i).get("station_name"));
                jsonObject.put("subStation_name", list.get(i).get("subStation_name"));
                jsonObject.put("prog_name", list.get(i).get("prog_name"));
                jsonObject.put("start_time", list.get(i).get("start_time"));
                jsonObject.put("remind_time", list.get(i).get("remind_time"));
                jsonObject.put("ahead_type", list.get(i).get("ahead_type"));
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        preferences.edit().putString("reminder", jsonArray.toString()).commit();
    }

    //从SharedPreferences获取提醒列表
    public static ArrayList<Map<String, Object>> GetReminderListFromStored(Context context) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        String reminder = preferences.getString("reminder", "");
        try {
            JSONArray jsonArray = new JSONArray(reminder);
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("station_name", jsonObject.getString("station_name"));
                item.put("subStation_name", jsonObject.getString("subStation_name"));
                item.put("prog_name", jsonObject.getString("prog_name"));
                item.put("start_time", jsonObject.getString("start_time"));
                item.put("remind_time", jsonObject.getString("remind_time"));
                item.put("ahead_type", jsonObject.getInt("ahead_type"));
                item.put("checkbox_display", false);
                item.put("checkbox_checked", false);
                list.add(item);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        return list;
    }

    //检查项目在提醒列表中是否已存在
    public static boolean CheckItemExistentInReminderList (Map<String, Object> item, ArrayList<Map<String, Object>> list) {
        boolean result = false;
        for (int i = 0; i < list.size(); i++) {
            if (item.equals(list.get(i))) {
                result = true;
            }
        }
        return result;
    }

    //将简介列表（关键字-索引映射表）存入SharedPreferences中
    public static void SaveBriefListToStored(Context context, ArrayList<Map<String, Object>> list) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("keyword", list.get(i).get("keyword"));
                jsonObject.put("index", list.get(i).get("index"));
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        preferences.edit().putString("brief", jsonArray.toString()).commit();
    }

    //从SharedPreferences获取简介列表（关键字-索引列表）
    public static ArrayList<Map<String, Object>> GetBriefListFromStored(Context context) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        String brief = preferences.getString("brief", "");
        
        if (brief.equals("")) {
            return null;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(brief);
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("keyword", jsonObject.getString("keyword"));
                item.put("index", jsonObject.getString("index"));
                list.add(item);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        return list;
    }

    //将评论列表（全部分类，关键字-索引列表）存入SharedPreferences中
    public static void SaveCommentAllListToStored(Context context, ArrayList<Map<String, Object>> list) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("keyword", list.get(i).get("keyword"));
                jsonObject.put("index", list.get(i).get("index"));
                jsonObject.put("page", list.get(i).get("page"));
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        preferences.edit().putString("comment_all", jsonArray.toString()).commit();
    }

    //将评论列表（好友及所关注分类，关键字-索引列表）存入SharedPreferences中
    public static void SaveCommentOtherListToStored(Context context, ArrayList<Map<String, Object>> list) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("keyword", list.get(i).get("keyword"));
                jsonObject.put("index", list.get(i).get("index"));
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        preferences.edit().putString("comment_other", jsonArray.toString()).commit();
    }

    //从SharedPreferences获取评论列表（全部分类，关键字-索引列表）
    public static ArrayList<Map<String, Object>> GetCommentAllListFromStored(Context context) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        String brief = preferences.getString("comment_all", "");
        
        if (brief.equals("")) {
            return null;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(brief);
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("keyword", jsonObject.getString("keyword"));
                item.put("index", jsonObject.getString("index"));
                item.put("page", jsonObject.getString("page"));
                list.add(item);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        return list;
    }
    
    //从SharedPreferences中获取评论（好友及所关注分类，关键字-索引列表）
    public static ArrayList<Map<String, Object>> GetCommentOtherListFromStored(Context context) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SharedPreferences preferences = context.getSharedPreferences("Shidian", 0);
        String brief = preferences.getString("comment_other", "");
        
        if (brief.equals("")) {
            return null;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(brief);
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("keyword", jsonObject.getString("keyword"));
                item.put("index", jsonObject.getString("index"));
                list.add(item);
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        return list;
    }
    
    //将评论内容列表存入JSON文件（存入SD卡的Shidian/Comment目录），以备下次使用
    public static void SaveCommentConentListToJsonFile(ArrayList<Map<String, Object>> list, String filename) {
        try {
            JSONObject jsonTotalObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObjectUser = new JSONObject();
                jsonObjectUser.put("profile_image_url", list.get(i).get("user_image_url"));
                jsonObjectUser.put("screen_name", list.get(i).get("user_name"));
                jsonObjectUser.put("id", list.get(i).get("user_id"));

                JSONObject jsonObjectOriginalUser = new JSONObject();
                jsonObjectOriginalUser.put("screen_name", list.get(i).get("original_user_name"));
                
                JSONObject jsonObjectOriginal = new JSONObject();
                jsonObjectOriginal.put("text", list.get(i).get("original_text"));
                jsonObjectOriginal.put("id", list.get(i).get("original_comment_id"));
                jsonObjectOriginal.put("thumbnail_pic", list.get(i).get("image_small_original"));
                jsonObjectOriginal.put("bmiddle_pic", list.get(i).get("image_middle_original"));
                jsonObjectOriginal.put("reposts_count", list.get(i).get("original_reposts_count"));
                jsonObjectOriginal.put("comments_count", list.get(i).get("original_comments_count"));
                jsonObjectOriginal.put("user", jsonObjectOriginalUser);
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user", jsonObjectUser);
                jsonObject.put("text", list.get(i).get("text"));
                jsonObject.put("created_at", list.get(i).get("created_at"));
                jsonObject.put("id", list.get(i).get("comment_id"));
                jsonObject.put("reposts_count", list.get(i).get("reposts_count"));
                jsonObject.put("comments_count", list.get(i).get("comments_count"));
                jsonObject.put("source", list.get(i).get("source"));
                jsonObject.put("thumbnail_pic", list.get(i).get("image_small"));
                jsonObject.put("bmiddle_pic", list.get(i).get("image_middle"));
                jsonObject.put("retweeted_status", jsonObjectOriginal);
                
                jsonArray.put(jsonObject);
            }
            jsonTotalObject.put("statuses", jsonArray);
            
            //太小表示可能没有内容，所以不进行存储
            if (jsonTotalObject.toString().length() > 30) {
                FileOutputStream outStream = new FileOutputStream(filename);
                outStream.write(jsonTotalObject.toString().getBytes());
                outStream.close();
            }
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
    }
    
    //获取超链接格式（以http://开始，以空格结束，其它内容保持原样）
    static public SpannableStringBuilder ProcessStringWithURL(String text) {
        SpannableStringBuilder result = new SpannableStringBuilder(text);
        URLSpan span = null;
        int finish = 0;

        while (text.length() > 0) {
            int start = text.indexOf("http://");
            if (start == -1) {
                return result;
            } else {
                text = text.substring(start);
                int end = text.indexOf(" ");

                String url = "";
                if (end == -1) {
                    url = text.substring(0);
                    text = "";
                } else {
                    url = text.substring(0, end);
                    text = text.substring(end + 1);
                }
                
                span = new URLSpan(url);
                result.setSpan(span, finish + start, finish + start + url.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                finish += start + url.length() + 1;
            }
        }
        
        return result;
    }
    
    //获取微博原贴格式（用户名设置为蓝色，其它内容保持原样）
    static public SpannableStringBuilder GetOriginalTextString(String user_name, String text)
    {
        SpannableStringBuilder spannableString = new SpannableStringBuilder("@" + user_name + ": " + text);
        ForegroundColorSpan span = new ForegroundColorSpan(Color.BLUE);
        spannableString.setSpan(span, 0, user_name.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return spannableString;
    }
}