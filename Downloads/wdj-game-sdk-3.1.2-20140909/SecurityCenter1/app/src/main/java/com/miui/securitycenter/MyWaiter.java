package com.miui.securitycenter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

public class MyWaiter {

    private final Lock mLock = new ReentrantLock();
    private final Condition mCondition = mLock.newCondition();
    private int mLockCount = 0;

    public void add() {
        mLock.lock();
        mLockCount ++;
        mLock.unlock();
    }

    public void remove() {
        mLock.lock();
        mLockCount --;
        if (mLockCount == 0) {
            mCondition.signalAll();
        }
        mLock.unlock();

    }

    public void waitIt() {
        mLock.lock();
        while (mLockCount > 0) {
            try {
                mCondition.await();
            } catch (InterruptedException e) {
                Log.e(RestoreHelper.TAG,
                        "Interrupted while waiting on isChallenged");
            }
        }
        mLock.unlock();
    }
}
