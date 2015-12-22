package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.zgy.piechartview.OnPieChartItemSelectedLinstener;
import com.zgy.piechartview.PieChartView;

public class MainActivity extends Activity {

	private String[] colors = { "#000000", "#ff0000", "#ff6666", "#ff80FF", "#ffFF00", "#ffE685" };
	private float[] items = { (float) 21.0, (float) 20.0, (float) 10.0, (float) 10.0, (float) 10.0, (float) 10.0, (float) 10.0, (float) 10.0, (float) 10.0, (float) 10.0 };
	// private float[] items = { (float) 20.0, (float) 20.0, (float) 10.0 };
	private int total = 150;
	private int radius = 200;
	private int strokeWidth = 1;
	private String strokeColor = "#000000";
	private float animSpeed = 2;

	private PieChartView pieChart;

	private TextView textInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textInfo = (TextView) findViewById(R.id.text_item_info);
		pieChart = (PieChartView) findViewById(R.id.parbar_view);
		// pb.setShowItem(5, true);//������ʾ�Ŀ�
		// pb.setAnimEnabled(false);//�Ƿ�������
		pieChart.setItemsSizes(items);// ���ø������ֵ
		// pieChart.setTotal(total);//���������ֵ, Ĭ��Ϊ��
		// pieChart.setItemsColors(colors);//���ø��������ɫ
		pieChart.setAnimSpeed(animSpeed);// ������ת�ٶ�
		pieChart.setRaduis(radius);// ���ñ�״ͼ�뾶����������Ե��Բ��
		pieChart.setStrokeWidth(strokeWidth);// ���ñ�Ե��Բ���ֶ�
		pieChart.setStrokeColor(strokeColor);// ���ñ�Ե��Բ����ɫ
		// pieChart.setRotateWhere(PieChartView.TO_RIGHT);//����ѡ�е�itemͣ����λ�ã�Ĭ�����Ҳ�
		pieChart.setSeparateDistence(15);// ������ת��item����ľ���
		//Ҳ���Բ�ʹ��xml���֣�����ϸ���뿴DOC

		pieChart.setOnItemSelectedListener(new OnPieChartItemSelectedLinstener() {

			@Override
			public void onPieChartItemSelected(PieChartView view, int position, String colorRgb, float size, float rate, boolean isFreePart, float rotateTime) {
				// TODO Auto-generated method stub
				Log.e("Main", "onClicked item : " + position);
				if (isFreePart) {
					textInfo.setText("����Ĳ���" + position + "\r\nitem size: " + size + "\r\nitem color: " + colorRgb + "\r\nitem rate: " + rate + "\r\nrotateTime : " + rotateTime);
				} else {
					textInfo.setText("item position: " + position + "\r\nitem size: " + size + "\r\nitem color: " + colorRgb + "\r\nitem rate: " + rate + "\r\nrotateTime : " + rotateTime);
				}
				textInfo.setVisibility(View.VISIBLE);
				Animation myAnimation_Alpha = new AlphaAnimation(0.1f, 1.0f);
				myAnimation_Alpha.setDuration((int) (3 * rotateTime));
				textInfo.startAnimation(myAnimation_Alpha);
			}

		});
	}
}
