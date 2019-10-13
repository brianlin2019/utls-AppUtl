package brnln.utils.appUtils._devLab;

import brnln.utils.appUtils.AppUtils;
import brnln.utils.appUtils.LmUtl;
import brnln.utils.appUtils.LmUtl.MyExRcvr;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class C003 {

    public static void main(String[] args) {
        System.out.println(String.format("2d: %s ms", AppUtils.convTimeDescToMs("2d")));
        System.out.println(String.format("1d: %s ms", AppUtils.convTimeDescToMs("1d")));
        System.out.println(String.format("1h: %s ms", AppUtils.convTimeDescToMs("1h")));
        System.out.println(String.format("1m: %s ms", AppUtils.convTimeDescToMs("1m")));
        System.out.println(String.format("1s: %s ms", AppUtils.convTimeDescToMs("1s")));

    }

    public static void main3(String[] args) {
        MyExRcvr aMyExRcvr = LmUtl.nwExRcvr();
        RuntimeException aRnEx = new RuntimeException("RuntimeException");
        aMyExRcvr.rcve(aRnEx);
        try {
            aMyExRcvr.thrEx(IOException.class);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

    }

    public static void main2(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        RuntimeException aRnEx = new RuntimeException("RuntimeException");
        Class<? extends Exception> clz = IOException.class;
        //
        Constructor cnstr = clz.getConstructor(Throwable.class);
        Object obj = cnstr.newInstance(aRnEx);
        System.out.println("obj:" + obj);

    }

}
