package brnln.utils.osCmd._devLab;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class C003_OperatingSystemMXBean {

    public static void main(String[] args) {
        OperatingSystemMXBean osMbn = ManagementFactory.getOperatingSystemMXBean();
        System.out.println(String.format("\t arch    : %s", osMbn.getArch()));
        System.out.println(String.format("\t avlbProc: %s", osMbn.getAvailableProcessors()));
        System.out.println(String.format("\t getName : %s", osMbn.getName()));
        System.out.println(String.format("\t version : %s", osMbn.getVersion()));
        System.out.println(String.format("\t getSystemLoadAverage : %s", osMbn.getSystemLoadAverage()));
    }

}
