package brnln.utils.ndUtl._myExamples;

import brnln.utils.ndUtl.MyNode;
import brnln.utils.ndUtl.NdUtl;
import brnln.utils.ndUtl.RtNode;
import java.io.IOException;

public class C003_entity {

    public static void main(String[] args) throws IOException {
        RtNode aRtNode = NdUtl.parseRsrc(C001_visit.class, "t01.txt");
        //
        String ptrnStr = "/dir01/d1s2";
        MyNode aMyNode = aRtNode.search(ptrnStr, null).orElse(null);
        aMyNode.pushEntry("Test 01").pushEntry("Test 02");
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        System.out.println("-----------------------------------------------------");
        ptrnStr = "/.*/d1s1";
        aMyNode = aMyNode.search(ptrnStr, null).orElse(null);
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        aMyNode.getEntryAL(String.class).stream().forEach((o) -> System.out.println(String.format("\t\t o:%s", o)));
        System.out.println("-----------------------------------------------------");
        ptrnStr = "/.*/d1s2";
        aMyNode = aMyNode.search(ptrnStr, null).orElse(null);
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        aMyNode.getEntryAL(String.class).stream().forEach((o) -> System.out.println(String.format("\t\t o:%s", o)));
        System.out.println("-----------------------------------------------------");
    }
}
