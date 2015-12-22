package com.game.tank.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.tank.threads.BallThread;

public class Ball {
  public int x, y; // 小球的实时位置
  public int startX, startY; // 小球的初始位置
  public float vX, vY; // 小球的速度
  public int r; // 小球的半径

  public double startTimeX; // 开始时间
  public double startTimeY; // 开始时间

  public BallThread ballThread; // 小球移动线程
  public Paint paint = new Paint(); // 画笔

  public Ball(int x, int y, float vX, float vY, int r) {
    this.x = x;
    this.y = y;
    this.startX = x;
    this.startY = y;
    this.vX = vX;
    this.vY = vY;
    this.r = r;

    // 为每个小球实例化一个独立的线程，在抬手时开启线程
    ballThread = new BallThread(this);

    paint.setColor(Color.RED); // 小球为红色实心
  }

  /** 绘画方法 **/
  public void drawSelf(Canvas canvas) {
    canvas.drawCircle(x, y, r, paint);
  }
}
