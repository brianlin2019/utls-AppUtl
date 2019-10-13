package brnln.utils.appUtils;

public class PrpUtl {

    public static int getInt(Object val, int dfltVal) {
        if (val != null && val instanceof Integer) {
            return (Integer) val;
        }
        return dfltVal;
    }
}
