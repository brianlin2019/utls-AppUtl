package brnln.utils.osCmd._devLab;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class C002_ThreadMXBean {

    public static void main(String[] args) {
        ThreadMXBean thdMbn = ManagementFactory.getThreadMXBean();
        if (true) {
            Arrays.asList(thdMbn.dumpAllThreads(true, true)).stream().forEach((thd) -> System.out.println(String.format("\t thdName:%s (%s)", thd.getThreadName(), thd.getThreadId())));
        }
        System.out.println(String.format("\t allThdIds : %s", Arrays.toString(thdMbn.getAllThreadIds())));
        System.out.println(String.format("\t crThdCpuTm: %s nanoseconds (10^-9 sec)", thdMbn.getCurrentThreadCpuTime()));
        System.out.println(String.format("\t crThdUsrTm: %s nanoseconds (10^-9 sec)", thdMbn.getCurrentThreadUserTime()));
        System.out.println(String.format("\t dmnThdCnt : %s ", thdMbn.getDaemonThreadCount()));
        System.out.println(String.format("\t peakThdCnt: %s ", thdMbn.getPeakThreadCount()));
        System.out.println(String.format("\t thdCnt:     %s ", thdMbn.getThreadCount()));
        System.out.println("\t getThreadCpuTime:");
        for (long id : thdMbn.getAllThreadIds()) {
            System.out.println(String.format("\t\t thd(%s):    cpu: %s, usr: %s ", id, thdMbn.getThreadCpuTime(id), thdMbn.getThreadUserTime(id)));
        }
        System.out.println(String.format("\t ttlThdCnt:  %s ", thdMbn.getTotalStartedThreadCount()));
//        thdMbn.getAllThreadIds() 

//        System.out.println(String.format("\t bootPath:%s", thdMbn.getBootClassPath()));
    }

}
