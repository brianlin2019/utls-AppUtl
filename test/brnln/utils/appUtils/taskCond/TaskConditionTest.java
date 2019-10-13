package brnln.utils.appUtils.taskCond;

import brnln.utils.appUtils.AppUtils;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author brianlin
 */
public class TaskConditionTest {

    public TaskConditionTest() {
    }

    @Test
    public void test_normal_usage() throws Exception {
        //  build TaskCondition
        ReentrantLock reentrantLock = new ReentrantLock();
        final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
        final TaskSignalReceiver taskSignalReceiver = (TaskSignalReceiver) taskCond;
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(100L);
                    taskSignalReceiver.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  wait task done
        taskCond.waitTaskDone(1000L);
    }

    @Test
    public void check_mark_task_start_twice() throws Exception {
        //  build TaskCondition
        ReentrantLock reentrantLock = new ReentrantLock();
        final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
        final TaskSignalReceiver taskSignalReceiver = (TaskSignalReceiver) taskCond;
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(100L);
                    taskCond.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  ----  [CHECK_POINT] mark task start (2) (start) ----
        try {
            taskCond.markTaskStarted();
            Assert.fail();
        } catch (TaskConditionException ex) {
            Assert.assertEquals("Task had started twice!", ex.getMessage());
        }
        //  ----  [CHECK_POINT] mark task start (2) ( end ) ----
        //
        //  wait task done
        taskCond.waitTaskDone(1000L);
    }

    @Test
    public void check_wait_task_done_before_start() throws Exception {
        //  build TaskCondition
        ReentrantLock reentrantLock = new ReentrantLock();
        final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
        final TaskSignalReceiver taskSignalReceiver = (TaskSignalReceiver) taskCond;
        //
        //  ----  [CHECK_POINT] wait task done <before start> (start) ----
        //  wait task done
        try {
            taskCond.waitTaskDone(10L);
            Assert.fail();
        } catch (TaskConditionException ex) {
            Assert.assertEquals("Task should be started first!", ex.getMessage());
        }
        //  ----  [CHECK_POINT] wait task done <before start> ( end ) ----
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(100L);
                    taskSignalReceiver.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  wait task done
        taskCond.waitTaskDone(1000L);
    }

    @Test
    public void check_wait_task_interrupted() throws Exception {
        //  build TaskCondition
        ReentrantLock reentrantLock = new ReentrantLock();
        final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
        final TaskSignalReceiver taskSignalReceiver = (TaskSignalReceiver) taskCond;
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(1000L);
                    taskSignalReceiver.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  ----  [CHECK_POINT] wait task done <interrupt wait thread> (start) ----
        final Thread currThd = Thread.currentThread();
        (new Thread() {
            @Override
            public void run() {
                AppUtils.mySleep(100L);
                currThd.interrupt();
            }
        }).start();
        try {
            //  wait task done
            taskCond.waitTaskDone(1000L);
            Assert.fail();
        } catch (TaskConditionException ex) {
            Assert.assertEquals("Someone interrupted task waiting!", ex.getMessage());
        }
        //  ----  [CHECK_POINT] wait task done <interrupt wait thread> ( end ) ----
        //
    }

    @Test
    public void check_wait_task_done_timeout() throws Exception {
        //  build TaskCondition
        ReentrantLock reentrantLock = new ReentrantLock();
        final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
        final TaskSignalReceiver taskSignalReceiver = (TaskSignalReceiver) taskCond;
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(100L);
                    taskSignalReceiver.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  ----  [CHECK_POINT] wait task done <timeout> (start) ----
        //  wait task done
        try {
            taskCond.waitTaskDone(10L);
            Assert.fail();
        } catch (TaskConditionException ex) {
            Assert.assertEquals("The task waiting is timeout!", ex.getMessage());
        }
        //  ----  [CHECK_POINT] wait task done <timeout> ( end ) ----
    }

    @Test
    public void check_signal_done_before_start() throws Exception {
        //  build TaskCondition
        ReentrantLock reentrantLock = new ReentrantLock();
        final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
        final TaskSignalReceiver taskSignalReceiver = (TaskSignalReceiver) taskCond;
        //
        //  ----  [CHECK_POINT] signal down <before start> (start) ----
        //  signal down
        try {
            taskSignalReceiver.signalTaskDone();
            Assert.fail();
        } catch (TaskConditionException ex) {
            Assert.assertEquals("Task should be started first!", ex.getMessage());
        }
        //  ----  [CHECK_POINT] signal down <before start> ( end ) ----
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(100L);
                    taskCond.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  wait task done
        taskCond.waitTaskDone(1000L);
    }

    @Test
    @SuppressWarnings("null")
    public void check_build_cond_null_lock() throws Exception {
        //
        //  ----  [CHECK_POINT] build TaskCondition <null lock> (start) ----
        //  build TaskCondition
        @SuppressWarnings("UnusedAssignment")
        TaskCondition tmpTaskCond = null;
        try {
            //  build TaskCondition
            tmpTaskCond = TaskCondition.buildTaskCond(null);
            //
            Assert.fail();
        } catch (TaskConditionException ex) {
            Assert.assertEquals("ReentrantLock can not be null!", ex.getMessage());
            return; // no need to forward
        }
        final TaskCondition taskCond = tmpTaskCond;
        //  ----  [CHECK_POINT] build TaskCondition <null lock> ( end ) ----
        //
        //  mark task start
        taskCond.markTaskStarted();
        //
        //  ----  signal task done (start) ----
        (new Thread() {
            @Override
            public void run() {
                try {
                    AppUtils.mySleep(100L);
                    taskCond.signalTaskDone();
                } catch (TaskConditionException ex) {
                }
            }
        }).start();
        //  ----  signal task done ( end ) ----
        //
        //  wait task done
        taskCond.waitTaskDone(1000L);
    }

}
