package brnln.utils.appUtils.taskCond._devLab;

import brnln.utils.appUtils.AppUtils;
import brnln.utils.appUtils.taskCond.TaskCondition;
import brnln.utils.appUtils.taskCond.TaskConditionException;
import java.util.concurrent.locks.ReentrantLock;

public class C001 {

    public static void main(String[] args) {
        try {
            ReentrantLock reentrantLock = new ReentrantLock();
            //
            final TaskCondition taskCond = TaskCondition.buildTaskCond(reentrantLock);
            //
            long startTm = System.currentTimeMillis();
            System.out.println("[TASK] start to mark task");
            taskCond.markTaskStarted();
            //
            (new Thread() {
                @Override
                public void run() {
                    try {
                        AppUtils.mySleep(1000L * 2L);
                        System.out.println("[OTHR] signal task done!");
                        taskCond.signalTaskDone();
                    } catch (TaskConditionException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }).start();
            //
            System.out.println("[TASK] wait task done!");
            taskCond.waitTaskDone(1000L * 10L);
            System.out.println("[TASK] task is done!");
            long takeTm = System.currentTimeMillis() - startTm;
            System.out.println(String.format("take %s ms", takeTm));
        } catch (TaskConditionException ex) {
            ex.printStackTrace(System.err);
        }
    }

}
