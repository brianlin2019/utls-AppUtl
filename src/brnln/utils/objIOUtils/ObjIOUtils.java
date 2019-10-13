package brnln.utils.objIOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ObjIOUtils {

    @FunctionalInterface
    public static interface DiffChk<T> {

        public boolean isDiff(T a, T b);
    }

    public static class RsltA {

        public final boolean succ;
        public final String desc;
        public final Exception ex;
        //
        public final byte[] bs;

        public RsltA(String desc, Exception ex) {
            this.succ = false;
            this.desc = desc;
            this.ex = ex;
            this.bs = null;
        }

        public RsltA(byte[] bs) {
            this.succ = true;
            this.desc = "Success";
            this.ex = null;
            this.bs = bs;
        }

    }

    public static class RsltB<T> {

        public final boolean succ;
        public final String desc;
        public final Exception ex;
        public final T obj;

        public RsltB(String desc, Exception ex) {
            this.succ = false;
            this.desc = desc;
            this.ex = ex;
            this.obj = null;

        }

        public RsltB(T obj) {
            this.succ = true;
            this.desc = "Success";
            this.ex = null;
            this.obj = obj;
        }

    }

    private ObjIOUtils() {
    }

    /* 透過 Serializable 界面將 Obj 轉成 byte[] */
    public static RsltA convertToBytes(Serializable serObj) {
        if (serObj == null) {
            String msg = "Object is null";
            return new RsltA(msg, new NullPointerException(msg));
        }
        ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(baOut);
        } catch (IOException ex) {
            return new RsltA("Fail to build ObjectOutputStream", ex);
        }
        try {
            out.writeObject(serObj);
        } catch (IOException ex) {
            return new RsltA("Fail to write ObjectOutputStream", ex);
        }
        try {
            out.close();
        } catch (IOException ex) {
            return new RsltA("Fail to close ObjectOutputStream", ex);
        }
        try {
            baOut.close();
        } catch (IOException ex) {
            return new RsltA("Fail to close ByteArrayOutputStream", ex);
        }
        return new RsltA(baOut.toByteArray());
    }

    /* 將之前 Serializable 轉成的 bs 再轉成 Obj 並檢查其型態是否為 usrClz */
    public static <U> RsltB<U> convertToObject(Class<U> usrClz, byte[] bs) {
        if (bs == null) {
            String msg = "Byte array is null";
            return new RsltB<>(msg, new NullPointerException(msg));
        }
        ByteArrayInputStream baIn = new ByteArrayInputStream(bs);
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(baIn);
        } catch (IOException ex) {
            return new RsltB<>("Fail to build ObjectInputStream", ex);
        }
        Object usrObj;
        try {
            usrObj = in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return new RsltB<>("Fail to read ObjectInputStream", ex);
        }
        try {
            in.close();
        } catch (IOException ex) {
            return new RsltB<>("Fail to close ObjectInputStream", ex);
        }
        try {
            baIn.close();
        } catch (IOException ex) {
            return new RsltB<>("Fail to close ByteArrayInputStream", ex);
        }
        if (!usrClz.isAssignableFrom(usrObj.getClass())) {
            return new RsltB<>("Fail to convert to type:" + usrClz.getName(), new RuntimeException(""));
        }
        return new RsltB((U) usrObj);
    }

    //  -----------------------------------------------------------------------
    /*  --  檢查二個物件 相同/不同/不確定  --
    檢查 Obj 是否一定相同、不同或不確定 (若全為 null 則視為相同，若僅有一個 null 則視為不同)
    若二者皆不存在 視為相同 回傳 0
    若二者一個存在一個不存在 視為不相同 回傳 1
    若二者皆存在 視為不確定 回傳 -1
     */
    public static int diffStts(Object aObj, Object bObj) {
        if (aObj == null && bObj == null) {
            return 0;
        } else if (aObj != null && bObj == null) {
            return 1;
        } else if (aObj == null && bObj != null) {
            return 1;
        }
        return -1;
    }

    //  若 a/b Lst 的元素皆為 clz, 且 clz.isDiff(aObj,bObj) 存在, 則比對二個 List 是否不同
    public static boolean isDiff(List aLst, List bLst, Class clz) {
        //  若傳入 clz 為 null 則直接回傳不同，因為無法比對
        if (clz == null) {
            return true;
        }
        //  ----  檢查物件是否皆存在 (start) ----
        int diffVal = ObjIOUtils.diffStts(aLst, bLst);
        if (diffVal >= 0) {
            return diffVal > 0;
        }
        assert aLst != null && bLst != null;
        //  ----  檢查物件是否皆存在 ( end ) ----
        //
        //  ----  檢查長度是否相同 (start) ----
        if (aLst.size() != bLst.size()) {
            return true;
        }
        //  ----  檢查長度是否相同 ( end ) ----
        try {
            Method mthd = clz.getMethod("isDiff", new Class[]{clz, clz});
            for (int idx = 0; idx < aLst.size(); idx++) {
                Object aObj = aLst.get(idx);
                Object bObj = bLst.get(idx);
                //  若 a/b Obj 有一個不為 clz 則直接回應不同, 因為無法比對
                if (!clz.isInstance(aObj) || !clz.isInstance(bObj)) {
                    return true;
                }
                //  呼叫 clz.isDiff(a,b) 
                Object rtrnObj = mthd.invoke(null, aObj, bObj);
                if (rtrnObj instanceof Boolean) {
                    //  若有一個比對結果是不同，則直接回應不同
                    if ((Boolean) rtrnObj) {
                        return true;
                    }
                } else {
                    //  在非預期的回應下，直接回應不同, 因為無法比對
                    return true;
                }
            }
            return false;
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            //  預期的 isDiff 方法不存在, 回傳 true 代表 a/b Lst 不同(因為無比對的method)
            return true;
        }
    }

    public static <T> boolean isDiff(T a, T b, DiffChk<T> dc) {
        //  chk null or notNull
        int diffVal = ObjIOUtils.diffStts(a, b);
        if (diffVal >= 0) {
            return diffVal > 0;
        }
        assert a != null && b != null;
        if (dc == null) /*  預期的 DiffChk 方法不存在, 回傳 true 代表 a/b 不同(因為無比對的方式) */ {
            return true;
        }
        return dc.isDiff(a, b);
    }

    public static <T> boolean isDiffLst(List<T> aL, List<T> bL, DiffChk<T> dc) {
        if (dc == null) /*  預期的 DiffChk 方法不存在, 回傳 true 代表 a/b 不同(因為無比對的方式) */ {
            return true;
        }
        //  ----  檢查物件是否皆存在 (start) ----
        int diffVal = ObjIOUtils.diffStts(aL, bL);
        if (diffVal >= 0) {
            return diffVal > 0;
        }
        assert aL != null && bL != null;
        //  ----  檢查物件是否皆存在 ( end ) ----
        //
        if (aL.size() != bL.size()) /* 若長度不同，則視為不同 */ {
            return true;
        }
        for (int idx = 0; idx < aL.size(); idx++) {
            T a = aL.get(idx);
            T b = bL.get(idx);
            if (dc.isDiff(a, b)) /* 若有任一個元素不同，則視為不同 */ {
                return true;
            }
        }
        return false;
    }

    public static boolean isDiff(byte[] aBs, byte[] bBs) {
        //  ----  檢查物件是否皆存在 (start) ----
        int diffVal = ObjIOUtils.diffStts(aBs, bBs);
        if (diffVal >= 0) {
            return diffVal > 0;
        }
        assert aBs != null && bBs != null;
        //  ----  檢查物件是否皆存在 ( end ) ----
        //
        if (aBs.length != bBs.length) /* 若長度不同，則視為不同 */ {
            return true;
        }
        return !Arrays.equals(aBs, bBs);
    }
}
