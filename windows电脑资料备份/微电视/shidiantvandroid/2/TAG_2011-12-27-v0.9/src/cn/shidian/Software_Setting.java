package cn.shidian;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Software_Setting extends ActivityGroup {
	//省级名称列表
    public static final String[] PROVINCE_STRING = {
        "北京", "天津", "上海", "重庆",
        "河北", "河南", "湖北", "湖南",
        "山西", "山东", "江苏", "江西",
        "广东", "广西", "陕西", "甘肃",
        "浙江", "安徽", "福建", "四川",
        "青海", "宁夏", "云南", "贵州",
        "新疆", "西藏", "辽宁", "吉林",
        "黑龙江", "内蒙古"};

    //市级名称列表
    public static final String[][] CITY_STRING = {
        new String[] {"北京"},//北京
        new String[] {"天津"},//天津
        new String[] {"上海"},//上海
        new String[] {"重庆"},//重庆
        new String[] {"保定", "石家庄", "唐山", "廊坊", "邢台", "邯郸"},//河北
        new String[] {"洛阳", "驻马店", "开封", "焦作", "周口", "信阳"},//河南
        new String[] {"荆门", "武汉", "黄石", "宜昌", "襄樊", "黄冈"},//湖北
        new String[] {"长沙", "株洲", "湘潭", "衡阳", "常德", "张家界"},//湖南
        new String[] {"太原", "大同", "阳泉", "长治", "晋城", "朔州"},//山西
        new String[] {"菏泽", "菏泽", "潍坊", "济宁", "聊城", "济南"},//山东
        new String[] {"无锡", "徐州", "常州", "常熟", "常州", "南通"},//江苏
        new String[] {"井冈山", "南昌", "景德镇", "九江", "新余", "上饶"},//江西
        new String[] {"广州", "深圳", "珠海", "汕头", "韶关", "佛山"},//广东
        new String[] {"桂林", "南宁", "北海", "柳州", "玉林", "梧州"},//广西
        new String[] {"宝鸡", "西安", "汉中", "临潼", "淳化", "蓝田"},//陕西
        new String[] {"兰州", "白银", "武威", "天水", "酒泉", "嘉峪关"},//甘肃
        new String[] {"杭州", "宁波", "温州", "绍兴", "金华", "台州"},//浙江
        new String[] {"合肥", "阜阳", "蚌埠", "芜湖", "马鞍山"},//安徽
        new String[] {"福州", "厦门", "宁德", "莆田", "漳州", "三明"},//福建
        new String[] {"成都", "绵阳", "宜宾", "自贡", "乐山", "南充"},//四川
        new String[] {"西宁", "玉树", "平安", "乐都", "海晏", "祁连"},//青海
        new String[] {"银川", "永宁", "贺兰", "石嘴山", "平罗", "青铜峡"},//宁夏
        new String[] {"昆明", "曲靖", "丽江", "玉溪", "大理", "普洱"},//云南
        new String[] {"贵阳", "遵义", "兴义", "六盘水", "安顺", "凯里"},//贵州
        new String[] {"乌鲁木齐", "克拉玛依", "石河子", "哈密", "吐鲁番", "喀什"},//新疆
        new String[] {"拉萨", "昌都", "林芝", "山南", "日喀则", "阿里"},//西藏
        new String[] {"沈阳", "大连", "营口", "盘锦", "鞍山", "丹东"},//辽宁
        new String[] {"长春", "吉林", "松原", "四平", "通化", "延边"},//吉林
        new String[] {"哈尔滨", "齐齐哈尔", "牡丹江", "佳木斯", "大庆", "鸡西"},//黑龙江            
        new String[] {"呼和浩特", "包头", "赤峰", "乌兰浩特", "呼伦贝尔", "鄂尔多斯"}};//内蒙古

    //控件显示内容列表
    public static final String list_string[][] = {{"城市切换", "北京"}};

    private Button btn_finish;
    private Button btn_feedback;
    private WheelView province = null;
    private WheelView city = null;
    private TextView text_province = null;
    private String stored_location = "";
    private String selected_location = "";
    private int selected_index = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.software_setting);

        //从SharedPreferences获取相关参数
        final SharedPreferences preferences = getSharedPreferences("Shidian", 0);
        stored_location = preferences.getString("location", "");

        //取回每个控件
        btn_finish = (Button)findViewById(R.id.btn_finish);
        btn_feedback = (Button)findViewById(R.id.btn_feedback);
        province = (WheelView)findViewById(R.id.wheel_province);
        city = (WheelView)findViewById(R.id.wheel_city);
        text_province = (TextView)findViewById(R.id.text_setting_province);

        //设置省级转轮控件通用参数
        ArrayWheelAdapter<String> ProvinceAdapter = new ArrayWheelAdapter<String>(this, PROVINCE_STRING);
        province.setVisibleItems(3);
        province.setViewAdapter(ProvinceAdapter);

        //设置省级转轮控件内容
        if (stored_location.equals("")) {
            province.setCurrentItem(0);
            text_province.setText("北京");
        } else {
            text_province.setText(stored_location);
            for (int i = 0; i < PROVINCE_STRING.length; i++) {
                if (stored_location.equals(PROVINCE_STRING[i])) {
                    province.setCurrentItem(i);
                    selected_index = i;
                    break;
                }
            }
        }

        //设置市级转轮控件通用参数
        ArrayWheelAdapter<String> CityAdapter = new ArrayWheelAdapter<String>(this, CITY_STRING[selected_index]);
        city.setViewAdapter(CityAdapter);
        city.setCurrentItem(0);

        //为完成按钮设置点击事件
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//保存用户的位置信息到SharedPreferences中
                selected_location = (String)text_province.getText();
                preferences.edit().putString("location", selected_location).commit();
                finish();
            }
        });

        //为反馈按钮设置点击事件
        btn_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Software_Setting.this, Software_Description.class));
            }
        });

        //当省级转轮控件转动后，更新市级转轮控件内容
        province.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingFinished(WheelView wheel) {
                updateCities(city, CITY_STRING, province.getCurrentItem());
                updateTextDisplay(province.getCurrentItem());
            }

            @Override
            public void onScrollingStarted(WheelView wheel) {}
        });
        
        city.setVisibleItems(3);
    }

    //为市级转轮控件设置adapter
    void updateCities(WheelView city, String cities[][], int index) {
        ArrayWheelAdapter<String> CityAdapter = new ArrayWheelAdapter<String>(this, cities[index]);
        city.setViewAdapter(CityAdapter);
        city.setCurrentItem(0);
    }

    //设置市级转轮控件内容
    void updateTextDisplay(int index) {
        text_province.setText(PROVINCE_STRING[index]);
    }
}