package brnln.utils.objIOUtils._myExamples;

import brnln.utils.appUtils.AppUtils;
import brnln.utils.objIOUtils.ObjIOUtils;
import brnln.utils.objIOUtils.ObjIOUtils.RsltA;

public class C01a_toBytes {

    public static void main(String[] args) {
        MyClz01 myObj01 = new MyClz01("a01", 10);
        RsltA rsltA = ObjIOUtils.convertToBytes(myObj01);
        if (rsltA.succ) {
            System.out.println(String.format("SUCC: myObj01.b2:%s", AppUtils.convertToHexStr(rsltA.bs)));
        } else {
            System.out.println(String.format("FAIL: desc:%s, ex:%s", rsltA.desc, rsltA.ex));
        }
    }

}
