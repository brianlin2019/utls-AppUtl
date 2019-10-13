package brnln.utils.objIOUtils._myExamples;

import java.io.Serializable;

public class MyClz01 implements Serializable {

    public String name;
    public int number;

    public MyClz01(String name, int number) {
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return "MyClz01{" + "name=" + name + ", number=" + number + '}';
    }

}
