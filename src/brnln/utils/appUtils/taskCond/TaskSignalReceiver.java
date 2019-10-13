package brnln.utils.appUtils.taskCond;

public interface TaskSignalReceiver {

    public void signalTaskDone() throws TaskConditionException;
}
