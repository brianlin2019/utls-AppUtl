package brnln.utils.ndUtl._myExamples;

import brnln.utils.ndUtl.MyNode;
import brnln.utils.ndUtl.NdUtl;
import brnln.utils.ndUtl.RtNode;
import java.io.IOException;

public class C002_search {

    public static void main(String[] args) throws IOException {
        RtNode aRtNode = NdUtl.parseRsrc(C001_visit.class, "t01.txt");
        //
        String ptrnStr = "/dir01/d1s2";
        MyNode aMyNode = aRtNode.search(ptrnStr, null).orElse(null);
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        System.out.println("-----------------------------------------------------");
        ptrnStr = "/dir02/d2s2";
        aMyNode = aMyNode.search(ptrnStr, null).orElse(null);
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        System.out.println("-----------------------------------------------------");
        ptrnStr = "/.*/d2s2";
        aMyNode = aMyNode.search(ptrnStr, null).orElse(null);
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        System.out.println("-----------------------------------------------------");
        ptrnStr = "d3s1/.*/d3s1s1s1";
        aMyNode = aMyNode.search(ptrnStr, null).orElse(null);
        System.out.println(String.format("\t\t ptrnStr:%s --> aMyNode:%s", ptrnStr, aMyNode));
        System.out.println("-----------------------------------------------------");
    }
}
