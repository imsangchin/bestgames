package com.game.tank.views;

import java.util.ArrayList;
import java.util.Random;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.game.tank.models.Ball;
import com.game.tank.models.Obstruction;
import com.game.tank.threads.DrawThread;

/**
 * @author huwei.nwu@gmail.com
 * 
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
  public SurfaceHolder holder;
  public DrawThread drawThread; // 绘画线程

  public Ball[] ballArray = new Ball[5]; // 装小球的数组
  public int ballPointer = 0; // 当前指向数组中第几个球

  public static ArrayList<Obstruction> obstructList = new ArrayList<Obstruction>(); // 装障碍物的集合

  int xDown, yDown; // 记录手指按下时的坐标

  public GameView(Context context) {
    super(context);
    holder = getHolder(); // 获取画布锁
    holder.addCallback(this); // 添加回调

    // 初始化障碍物
    Random random = new Random();
    for (int i = 0; i < 3; i++) {
      Obstruction o = new Obstruction(random.nextInt(380) + 50, random.nextInt(700) + 50, 50);
      obstructList.add(o);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    drawThread = new DrawThread(this);
    drawThread.start(); // 开启绘画线程
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    // 画布发生变化，eg:转屏操作,处理画布操作
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    // 销毁画布操作
    drawThread.flag = false; // 停掉线程
    drawThread = null; // GC会及时发现并处理掉该对象
  }

  public void draw(Canvas canvas) {
    canvas.drawColor(Color.BLACK); // 背景颜色
    Paint paint = new Paint();
    paint.setTextSize(25);
    paint.setColor(Color.WHITE); // 文字颜色
    canvas.drawText("小球碰撞检测", 50, 20, paint);

    // 画出小球
    for (int i = 0; i < 5; i++) {
      if (ballArray[i] != null) {
        ballArray[i].drawSelf(canvas); // 当前小球绘画出自己
      }
    }
    // 画出障碍物
    for (int i = 0; i < obstructList.size(); i++) {
      obstructList.get(i).drawSelf(canvas);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int x = (int) event.getX();
    int y = (int) event.getY();

    if (event.getAction() == 0) { // 按下
      // 记录按下时X，Y的坐标
      xDown = x;
      yDown = y;
      // 生成第一个球
      Ball ball = new Ball(x, y, 0, 0, 20);
      if (ballArray[ballPointer] != null) {
        ballArray[ballPointer].ballThread.flag = false; // 关闭小球移动线程
        ballArray[ballPointer].ballThread = null;
      }
      ballArray[ballPointer] = ball;

    } else if (event.getAction() == 1) { // 抬起
      int xOffset = x - xDown;
      int yOffset = y - yDown;
      double sin = yOffset / Math.sqrt(xOffset * xOffset + yOffset * yOffset);
      double cos = xOffset / Math.sqrt(xOffset * xOffset + yOffset * yOffset);

      ballArray[ballPointer].startTimeX = System.nanoTime(); // 当前小球开始时间
      ballArray[ballPointer].startTimeY = System.nanoTime();
      ballArray[ballPointer].vX = (float) (500 * cos); // 当前小球的速度
      ballArray[ballPointer].vY = (float) (500 * sin);
      ballArray[ballPointer].ballThread.start(); // 开启小球移动线程

      ballPointer++; // 下一个小球
      if (ballPointer >= 5) {
        ballPointer = 0;
      }
    }
    return true;
  }
}
