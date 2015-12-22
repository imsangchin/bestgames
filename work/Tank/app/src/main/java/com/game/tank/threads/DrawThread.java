package com.game.tank.threads;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import com.game.tank.views.GameView;

public class DrawThread extends Thread {
                                                                                                                                                                                                                                                                                              
    public boolean flag;  //标记线程是否开启
    public GameView gameView;
    public SurfaceHolder holder;
    public Canvas canvas;
                                                                                                                                                                                                                                                                                              
    public DrawThread(GameView gameView) {
        flag = true;
        this.gameView = gameView;
        holder = gameView.getHolder();  //获取画布锁
    }
    @Override
    public void run() {
        while(flag) {
            //获取当前绘画开始时间
            long startTime = System.currentTimeMillis();
            synchronized(holder) {
                canvas = holder.lockCanvas(); //获取当前被锁住的画布
                if(canvas != null) {
                    gameView.draw(canvas); //对画布进行操作
                    holder.unlockCanvasAndPost(canvas); //释放画布
                }
            }
            long endTime = System.currentTimeMillis();
            int diffTime = (int) (endTime - startTime);
            Log.d("DrawTime", diffTime+"");
                                                                                                                                                                                                                                                                                                      
            while(diffTime <= 2) {
                diffTime = (int) (System.currentTimeMillis() - startTime);
                Thread.yield(); //将线程的所有权交给另一个线程
            }
        }
    }
}