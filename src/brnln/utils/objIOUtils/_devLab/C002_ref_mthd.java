package brnln.utils.objIOUtils._devLab;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class C002_ref_mthd {

    public static class Sub01 {

        public static void SameNmFunc() {
            System.out.println("\t\t exec Sub01.SameNmFunc");
        }
    }

    public static void SameNmFunc() {
        System.out.println("\t\t exec C002.SameNmFunc");
        try {
            exec_sttc_mthd(Sub01.class, "SameNmFunc");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void exec_sttc_mthd(Class clz, String mthdNm) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        System.out.println(String.format("\t ----  exec %s -> %s (start) ----", clz.getSimpleName(), mthdNm));
        Method mthd = clz.getMethod(mthdNm, new Class[]{});
        mthd.invoke(null);
        System.out.println(String.format("\t ----  exec %s -> %s ( end ) ----", clz.getSimpleName(), mthdNm));

    }

    public static void main(String[] args) {
        try {
            System.out.println("-----------------------------------------------");
            exec_sttc_mthd(C002_ref_mthd.class, "SameNmFunc");
            System.out.println("-----------------------------------------------");
            exec_sttc_mthd(Sub01.class, "SameNmFunc");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

}
