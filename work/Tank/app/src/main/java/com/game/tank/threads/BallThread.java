package com.game.tank.threads;

import com.game.tank.models.Ball;
import com.game.tank.models.Obstruction;
import com.game.tank.views.GameView;

public class BallThread extends Thread {
    public boolean flag;   //标记线程是否开启
    public Ball ball;  //小球
    public double currentTime;  //当前时间
                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
    public BallThread(Ball ball) {
        flag = true;
        this.ball = ball;
    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
    @Override
    public void run() {
        while(flag) {
            //调试：碰撞检测开始时间
            long startTime = System.currentTimeMillis();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
             //计算出小球移动的时间片：将每次刷新分成若干时间小片段，用于计算每次时间小片段小球移动的距离
            currentTime = System.nanoTime();
            double timeSpanX = (currentTime - ball.startTimeX) /1000 /1000 /1000;
            double timeSpanY = (currentTime - ball.startTimeY) /1000 /1000 /1000;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
            int xBackup = ball.x; //保存小球的碰撞前的位置
            int yBackup = ball.y;
            ball.x = (int) (ball.startX + ball.vX * timeSpanX);//小球移动的距离
            ball.y = (int) (ball.startY + ball.vY * timeSpanY);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
            //边界碰撞检测
            if((ball.vX > 0 && (ball.x + ball.r) >= 479) || (ball.vX < 0 && (ball.x - ball.r) <= 0)) {
                ball.x = xBackup;
                ball.vX = 0 - ball.vX;  //速度反向
                ball.startTimeX = System.nanoTime();  //重新记录开始时间
                ball.startX = ball.x;  //重新记录开始位置
            }
            if((ball.vY > 0 && (ball.y + ball.r) >= 799) || (ball.vY < 0 && (ball.y - ball.r) <= 0)) {
                ball.y = yBackup;
                ball.vY = 0 - ball.vY;   //速度反向
                ball.startTimeY = System.nanoTime();   //重新记录开始时间
                ball.startY = ball.y;   //重新记录开始位置
            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
            //障碍物碰撞检测
            for(int i = 0; i < GameView.obstructList.size(); i++) {
                Obstruction o = GameView.obstructList.get(i);
                if(Math.abs(ball.x - o.x) < (ball.r + o.hWeight) && Math.abs(ball.y - o.y) < (ball.r + o.hWeight)){
                    if(Math.abs(xBackup - o.x) >= (ball.r + o.hWeight)) {
                        ball.x = xBackup;
                        ball.vX =  0 - ball.vX;
                        ball.startTimeX = System.nanoTime();
                        ball.startX = ball.x;
                    }
                    if(Math.abs(yBackup - o.y) >= (ball.r + o.hWeight)) {
                        ball.y = yBackup;
                        ball.vY = 0 - ball.vY;
                        ball.startTimeY = System.nanoTime();
                        ball.startY = ball.y;
                    }
                    break; //跳出循环
                }
            }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
            //调试：碰撞检测结束时间     实验证明碰撞加测基本不耗时间
            long endTime = System.currentTimeMillis();
            System.out.println(endTime + "----" + startTime + "= " +(endTime - startTime));
                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}