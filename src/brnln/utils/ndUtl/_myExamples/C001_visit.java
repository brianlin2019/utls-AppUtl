package brnln.utils.ndUtl._myExamples;

import brnln.utils.ndUtl.NdUtl;
import brnln.utils.ndUtl.RtNode;
import java.io.IOException;

public class C001_visit {

    public static void main(String[] args) throws IOException {
        RtNode aRtNode = NdUtl.parseRsrc(C001_visit.class, "t01.txt");
        aRtNode.visitNd((nd) -> System.out.println(String.format("%s/", nd.getPath())), null);
    }
}
