package brnln.utils.appUtils._devLab;

import brnln.utils.appUtils.AppUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class C002 {

    public static void main(String[] args) {
        String src = "0 S oracle   20268     1  0  76   0 - 341701 pipe_w 2014 ?        00:02:10 oraclexsms1 (DESCRIPTION=(LOCAL=YES)(ADDRESS=(PROTOCOL=beq)))";
        String dst01;
        //dst01 = AppUtils.replaceStr(src, "^.*oracle", "__@@@__");// greedy
        dst01 = AppUtils.replaceStr(src, "^.*?oracle", "<td>${matched_string}</td>");// not greedy
        //
        String dst02 = AppUtils.replaceStr(dst01, "\\s+", ".");
        System.out.println(src);
        System.out.println(dst01);
        System.out.println(dst02);
    }

    public static void main02(String[] args) {
        String str01 = "<td>${matched_string}</td>";
        String chkStr = "\\$\\{matched_string}";
        System.out.println(String.format("str01.matches(chkStr):%s", str01.matches(chkStr), 1));
        //
        Pattern p = Pattern.compile(chkStr);
        Matcher m = p.matcher(str01);
        System.out.println(":" + m.find());

    }

    public static void main01(String[] args) {
        String src = "0 S oracle   20268     1  0  76   0 - 341701 pipe_w 2014 ?        00:02:10 oraclexsms1 (DESCRIPTION=(LOCAL=YES)(ADDRESS=(PROTOCOL=beq)))";
        String dst01;
        //dst01 = AppUtils.replaceStr(src, "^.*oracle", "__@@@__");// greedy
        dst01 = AppUtils.replaceStr(src, "^.*?oracle", "__@@@__");// not greedy
        //
        String dst02 = AppUtils.replaceStr(dst01, "\\s+", ".");
        System.out.println(src);
        System.out.println(dst01);
        System.out.println(dst02);
    }

}
