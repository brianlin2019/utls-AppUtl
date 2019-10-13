package brnln.utils.appUtils.taskCond;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskCondition implements TaskSignalReceiver {

    private final ReentrantLock reentrantLock;
    private final Condition myCond;
    //
    private boolean taskStarting = false;
    private boolean waitTaskDone = false;

    public TaskCondition(ReentrantLock reentrantLock) {
        this.reentrantLock = reentrantLock;
        this.myCond = this.reentrantLock.newCondition();
    }

    public void markTaskStarted() throws TaskConditionException {
        try {
            reentrantLock.lock();
            if (taskStarting && waitTaskDone) {
                throw new TaskConditionException(TaskConditionException.DUPLICATED_STARTING, "Task had started twice!");
            } else {
                taskStarting = true;
                waitTaskDone = true;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    public void waitTaskDone(long mSec) throws TaskConditionException {
        if (mSec <= 0) {
            mSec = 1000L * 60L;//60 SEC
        }
        try {
            reentrantLock.lock();
            if (!taskStarting) {
                throw new TaskConditionException(TaskConditionException.NOT_START_YET, "Task should be started first!");
            }
            boolean isSignaled = !waitTaskDone;
            if (!isSignaled) {
                try {
                    isSignaled = myCond.await(mSec, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    throw new TaskConditionException(TaskConditionException.WAIT_BUT_INTERRUPTED, "Someone interrupted task waiting!");
                }
            }
            if (isSignaled) {
                taskStarting = false;
            } else {
                taskStarting = false;
                throw new TaskConditionException(TaskConditionException.TIME_OUT, "The task waiting is timeout!");
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void signalTaskDone() throws TaskConditionException {
        try {
            reentrantLock.lock();
            //
            if (!waitTaskDone) {
                throw new TaskConditionException(TaskConditionException.NOT_START_YET, "Task should be started first!");
            } else {
                waitTaskDone = false;
            }
            this.myCond.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    public static TaskCondition buildTaskCond(ReentrantLock reentrantLock) throws TaskConditionException {
        if (reentrantLock == null) {
            throw new TaskConditionException(TaskConditionException.ReentrantLock_NULL, "ReentrantLock can not be null!");
        }
        try {
            reentrantLock.lock();
            return new TaskCondition(reentrantLock);
        } finally {
            reentrantLock.unlock();
        }
    }
}
