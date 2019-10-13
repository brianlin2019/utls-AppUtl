package brnln.utils.ndUtl._myExamples;

import brnln.utils.ndUtl.NdUtl;
import brnln.utils.ndUtl.RtNode;
import java.io.IOException;

public class C004_entity2 {

    public static void main(String[] args) throws IOException {
        RtNode aRtNode = NdUtl.parseRsrc(C001_visit.class, "t01.txt");
        aRtNode.search("/.*/d1s2", null).ifPresent((n) -> n.pushEntry("d1s2V1").pushEntry("d1s2V2"));
        aRtNode.search("/.*/d2s2", null).ifPresent((n) -> n.pushEntry("d2s2V1").pushEntry("d2s2V2"));
        //
        System.out.println("---------------------------------------------------");
        aRtNode.search("/.*/d1s2", null).ifPresent((n) -> n.getEntryAL(String.class).stream().forEach((s) -> System.out.println(String.format("\t%s", s))));
        System.out.println("---------------------------------------------------");
        aRtNode.search("/.*/d2s1", null).ifPresent((n) -> n.getEntryAL(String.class).stream().forEach((s) -> System.out.println(String.format("\t%s", s))));
        System.out.println("---------------------------------------------------");
        aRtNode.search("/.*/d2s2", null).ifPresent((n) -> n.getEntryAL(String.class).stream().forEach((s) -> System.out.println(String.format("\t%s", s))));
    }
}
