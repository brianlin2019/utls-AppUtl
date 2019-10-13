package brnln.utils.appUtils._devLab;

public class C001<T> {

    private T value;

    public C001(Class<T> clz) {
    }

    public void setT(T v) {
        this.value = v;
    }

    public T getT() {
        return value;
    }

    public static void main(String[] args) {
        C001<B01> c01 = new C001<B01>(B01.class);
        B01 b01 = new B01();
        c01.setT(b01);
        B01 aa1 = c01.getT();
        System.out.println("b01:" + b01);
        System.out.println("aa1:" + aa1);
    }
}
