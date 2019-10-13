package brnln.utils.appUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RsrcLock {

    private final ReentrantLock reentrantLock;
    private final int lockNumber;// Lock 的編號
    private int lockMode = 0;
    private final Condition unlockCond;
    private Thread holdThread = null;

    private RsrcLock(ReentrantLock reentrantLock, int lockNumber) {
        if (reentrantLock == null) {
            throw new NullPointerException("reentrantLock is null");
        }
        this.reentrantLock = reentrantLock;
        this.lockNumber = lockNumber;
        unlockCond = reentrantLock.newCondition();
    }

    public boolean holdLock(Runnable runnable, int reqLockNumber, long waitTm) {
        this.reentrantLock.lock();
        try {
            Thread reqThd = Thread.currentThread();
            //  ----  若該 Lock 已被取走 (start) ----
            if (lockMode > 0 && !reqThd.equals(this.holdThread)) {
                //
                //  ----  若請求 Lock 的 Number 小於該 Lock 則直接放棄 (start) ----
                if (reqLockNumber > lockNumber) {
                    return false;
                }
                //  ----  若請求 Lock 的 Number 小於該 Lock 則直接放棄 ( end ) ----
                //
                //  ----  等待該 Lock 被釋放 (start) ----
                long nanos = TimeUnit.MILLISECONDS.toNanos(waitTm);
                while (nanos > 0 && lockMode > 0) {
                    try {
                        nanos = unlockCond.awaitNanos(nanos);
                    } catch (InterruptedException ex) {
                    }
                }
                //  ----  等待該 Lock 被釋放 ( end ) ----
                //
                //  ----  若該 Lock 仍被佔用則直接放棄 (start) ----
                if (lockMode > 0) {
                    return false;
                }
                //  ----  若該 Lock 仍被佔用則直接放棄 ( end ) ----
                //
            }
            //  ----  若該 Lock 已被取走 ( end ) ----
            //
            //  設定取用旗標
            this.lockMode = 1;
            this.holdThread = Thread.currentThread();
            //  ----  執行使用者在取得 Lock 後的指定操作 (start) ----
            if (runnable != null) {
                try {
                    runnable.run();
                } catch (Exception ex) {
                }
            }
            //  ----  執行使用者在取得 Lock 後的指定操作 ( end ) ----
            //
            //  回傳取用成功
            return true;
        } finally {
            reentrantLock.unlock();
        }
    }

    public void returnLock(Runnable runnable) {
        this.reentrantLock.lock();
        try {
            //  ----  僅有原 Thread 才能釋放 Lock (start) ----
            if (Thread.currentThread() != this.holdThread) {
                return;
            }
            //  ----  僅有原 Thread 才能釋放 Lock ( end ) ----
            //
            lockMode = 0;
            this.holdThread = null;
            if (runnable != null) {
                try {
                    runnable.run();
                } catch (Exception ex) {
                }
            }
            unlockCond.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    public static RsrcLock buildRcrcLock(ReentrantLock reentrantLock, int lockNumber) {
        return new RsrcLock(reentrantLock, lockNumber);
    }
}
