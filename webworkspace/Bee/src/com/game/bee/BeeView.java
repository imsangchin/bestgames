package com.game.bee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class BeeView extends View{
	
	private Bitmap[] mBeeArray;

	public BeeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for(int i = 0; i < mBeeArray.length; i ++){
			
		}
	}
	
	public void setBeeNum(int num) {
		mBeeArray = new Bitmap[num];
	}

}
