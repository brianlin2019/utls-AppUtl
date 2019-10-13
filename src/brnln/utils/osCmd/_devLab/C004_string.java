package brnln.utils.osCmd._devLab;

import brnln.utils.osCmd.JvmInfo;

public class C004_string {

    public static void main(String[] args) {
        System.out.println(String.format("osInfo: %s", JvmInfo.inst().getOsInfo()));
        System.out.println(String.format("vmInfo: %s", JvmInfo.inst().getVmInfo()));
    }

}
