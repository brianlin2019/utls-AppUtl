package brnln.utils.appUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DoneCondition {

    private final ReentrantLock reentrantLock;
    private final Condition myCond;
    boolean taskStarted = false;
    boolean isDone = false;
    Exception exception;

    private DoneCondition(ReentrantLock reentrantLock) {
        this.reentrantLock = reentrantLock;
        this.myCond = this.reentrantLock.newCondition();
    }

    public void setTaskStarted() {
        try {
            reentrantLock.lock();
            //
            this.taskStarted = true;
        } finally {
            reentrantLock.unlock();
        }

    }

    public void signalAll() {
        try {
            reentrantLock.lock();
            //
            this.isDone = true;
            this.myCond.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    public boolean isDone() {
        try {
            reentrantLock.lock();
            //
            return this.isDone;
        } finally {
            reentrantLock.unlock();
        }
    }

    public boolean waitCond() {
        return this.waitCond(-1);
    }

    public boolean waitCond(long mSec) {
        try {
            reentrantLock.lock();
            //
            if (this.taskStarted && !this.isDone) {//僅有 task 已啟動後,才需等待結束
                if (mSec <= 0) {
                    this.myCond.awaitUninterruptibly();
                    return true;
                } else {
                    try {
                        return this.myCond.await(mSec, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        return false;
                    }
                }
            } else if (this.taskStarted && this.isDone) {//若該 task 已完成
                return true;
            }
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    //  fix
    public static DoneCondition buildCond(ReentrantLock reentrantLock) {
        return buildCond(reentrantLock, true);
    }

    public static DoneCondition buildCond(ReentrantLock reentrantLock, boolean taskStarted) {
        try {
            reentrantLock.lock();
            DoneCondition doneCondition = new DoneCondition(reentrantLock);
            doneCondition.taskStarted = taskStarted;
            return doneCondition;
        } finally {
            reentrantLock.unlock();
        }
    }

}
