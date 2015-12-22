package com.game.tank.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Obstruction {
  public int x, y;
  public int hWeight; // 宽度和高度一样
  public Paint paint = new Paint();

  public Obstruction(int x, int y, int hWeight) {
    this.x = x;
    this.y = y;
    this.hWeight = hWeight;

    paint.setColor(Color.GREEN); // 设置画笔颜色

  }

  public void drawSelf(Canvas canvas) {
    canvas.drawRect(x - hWeight, y - hWeight, x + hWeight, y + hWeight, paint);
  }
}
