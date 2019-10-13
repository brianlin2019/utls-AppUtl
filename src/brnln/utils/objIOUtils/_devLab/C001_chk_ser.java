package brnln.utils.objIOUtils._devLab;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class C001_chk_ser {

    public static class MyS1 implements Serializable {

        String vStr;

        public MyS1(String vStr) {
            this.vStr = vStr;
        }

    }

    public static class MyN1 {

        String vStr;

        public MyN1(String vStr) {
            this.vStr = vStr;
        }

    }

    public static void main(String[] args) {
        try {
            ByteArrayOutputStream baOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baOut);
            MyS1 aMyS1 = new MyS1("abc");
            out.writeObject(aMyS1);
            System.out.println("MyS1 可成功寫入 (正確)");
            MyN1 aMyN1 = new MyN1("abc");
            try {
                out.writeObject(aMyN1);
            } catch (Exception ex) {
                System.out.println("MyN1 寫入會失敗 (正確)");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

}
