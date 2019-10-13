package brnln.utils.osCmd._devLab;

import brnln.utils.appUtils.AppUtils;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

public class C001_RuntimeMXBean {

    public static void main(String[] args) {
        RuntimeMXBean rtMbn = ManagementFactory.getRuntimeMXBean();
        System.out.println(String.format("\t bootPath:%s", rtMbn.getBootClassPath()));
        System.out.println(String.format("\t clz_Path:%s", rtMbn.getClassPath()));
        System.out.println(String.format("\t inputArg:%s", rtMbn.getInputArguments()));
        System.out.println(String.format("\t libPath :%s", rtMbn.getLibraryPath()));
        System.out.println(String.format("\t mngtSpc :%s", rtMbn.getManagementSpecVersion()));
        System.out.println(String.format("\t name    :%s", rtMbn.getName()));
        System.out.println(String.format("\t specName:%s", rtMbn.getSpecName()));
        System.out.println(String.format("\t specVndr:%s", rtMbn.getSpecVendor()));
        System.out.println(String.format("\t specVrsn:%s", rtMbn.getSpecVersion()));
        System.out.println(String.format("\t strtTime:%s", AppUtils.getDateTimeStrV2(new Date(rtMbn.getStartTime()))));
        System.out.println(String.format("\t uptime  :%s", rtMbn.getUptime()));
        System.out.println(String.format("\t VmName  :%s", rtMbn.getVmName()));
        System.out.println(String.format("\t VmVendor:%s", rtMbn.getVmVendor()));
        System.out.println(String.format("\t VmVrsn  :%s", rtMbn.getVmVersion()));
        System.out.println(String.format("\t bootClzSprt:%s", rtMbn.isBootClassPathSupported()));
        //
        System.out.println("-----------------------------------------------------");
        System.out.println(String.format("\t PID: %s ", rtMbn.getName().substring(0, rtMbn.getName().indexOf("@"))));

        if (true) /* show properties*/ {
            rtMbn.getSystemProperties().entrySet().stream().forEach((e) -> System.out.println(String.format("\t\t %s : %s", e.getKey(), e.getValue())));
        }

    }

}
