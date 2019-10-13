package brnln.utils.osCmd._devLab;

import brnln.utils.osCmd.OsCmd;
import java.io.IOException;

public class C005b_lnx {

    public static void main(String[] args) {
        try {
            OsCmd.inst().execOsCmd("netstat -na|grep LISTENING", "UTF-8", (s) -> System.out.println(String.format("std>%s", s)), (s) -> System.out.println(String.format("err>%s", s)), 10);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            OsCmd.inst().release();
        }
    }

}
