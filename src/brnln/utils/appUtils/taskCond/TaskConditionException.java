package brnln.utils.appUtils.taskCond;

public class TaskConditionException extends Exception {

    public static final int DUPLICATED_STARTING = 0x1;
    public static final int NOT_START_YET = 0x2;
    public static final int WAIT_BUT_INTERRUPTED = 0x3;
    public static final int TIME_OUT = 0x4;
    public static final int ReentrantLock_NULL = 0x5;

    public final int ERROR_CODE;
    private final String msg;

    public TaskConditionException(int ERROR_CODE, String msg) {
        this.ERROR_CODE = ERROR_CODE;
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return this.msg;
    }
}
