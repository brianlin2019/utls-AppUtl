package brnln.utils.osCmd._devLab;

import brnln.utils.osCmd.OsCmd;
import java.io.IOException;

public class C005a_win {

    public static void main(String[] args) {
        try {
//            OsCmd.inst(null).execOsCmd("netstat -na|findstr LISTENING", "Big5", (s) -> System.out.println(String.format("std>%s", s)), (s) -> System.out.println(String.format("err>%s", s)), 10);
//            OsCmd.inst(null).execOsCmd("wmic cpu get loadpercentage", "Big5", (s) -> System.out.println(String.format("std>%s", s)), (s) -> System.out.println(String.format("err>%s", s)), 10);
            OsCmd.inst().execOsCmd("wmic cpu list full", "Big5", (s) -> System.out.println(String.format("std>%s", s)), (s) -> System.out.println(String.format("err>%s", s)), 10);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            OsCmd.inst().release();
        }
    }

}
