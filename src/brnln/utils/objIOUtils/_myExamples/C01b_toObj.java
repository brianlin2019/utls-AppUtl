package brnln.utils.objIOUtils._myExamples;

import brnln.utils.appUtils.AppUtils;
import brnln.utils.objIOUtils.ObjIOUtils;
import brnln.utils.objIOUtils.ObjIOUtils.RsltB;

public class C01b_toObj {

    public static void main(String[] args) {
        String data = "ACED00057372003363632E63752E627269616E6C696E2E7574696C732E6F626A494F5574696C732E5F6D794578616D706C65732E4D79436C7A3031C617D51F38DBE6140200024900066E756D6265724C00046E616D657400124C6A6176612F6C616E672F537472696E673B78700000000A740003613031";
        byte[] bs = AppUtils.convertToBytes(data);
        RsltB<MyClz01> rsltB = ObjIOUtils.convertToObject(MyClz01.class, bs);
        if (rsltB.succ) {
            System.out.println(String.format("myObj01.a:%s", rsltB.obj));
        } else {
            System.out.println(String.format("FAIL: desc:%s, ex:%s", rsltB.desc, rsltB.ex));
        }
    }
}
