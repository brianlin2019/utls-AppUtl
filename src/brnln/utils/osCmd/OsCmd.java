package brnln.utils.osCmd;

import brnln.utils.appUtils.DoneCondition;
import brnln.utils.appUtils.LmUtl;
import brnln.utils.appUtils.LmUtl.MyExRcvr;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class OsCmd {

    @FunctionalInterface
    public static interface RsltRdr {

        public void rdLine(String ln) throws IOException;
    }

    private static class ThdFactry implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public ThdFactry() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "OsCmd-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread tmpThd = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (tmpThd.isDaemon()) {
                tmpThd.setDaemon(false);
            }
            if (tmpThd.getPriority() != Thread.NORM_PRIORITY) {
                tmpThd.setPriority(Thread.NORM_PRIORITY);
            }
            return tmpThd;
        }

    }

    private static OsCmd inst;
    private final ThreadPoolExecutor thdPoolExtr = new ThreadPoolExecutor(10, 20, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThdFactry());
    private final JvmInfo aJvmInfo = JvmInfo.inst();
    private final ReentrantLock rLck = new ReentrantLock();

    private OsCmd() {
    }

    //
    public void execOsCmd(String cmd, String charset, RsltRdr std, RsltRdr err, int timeout) throws IOException {
        this.execOsCmd(new ArrayList(Arrays.asList(cmd)), charset, std, err, timeout);
    }

    public void execOsCmd(ArrayList<String> cmdAL, String charset, RsltRdr std, RsltRdr err, int timeout) throws IOException {
        if (aJvmInfo.isWindows()) {
            cmdAL.add(0, "cmd.exe");
            cmdAL.add(1, "/c");
        } else if (aJvmInfo.isLinux()) {
            cmdAL.add(0, "bash");
            cmdAL.add(1, "-c");
        } else {
            throw new IOException(String.format("This feature does not support this OS. (os:%s, cmd:%s)", aJvmInfo.osName(), cmdAL));
        }
        ProcessBuilder aPrcBldr = new ProcessBuilder(cmdAL.toArray(new String[cmdAL.size()]));
        Process proc = aPrcBldr.start();
        MyExRcvr exRr1 = LmUtl.nwExRcvr();
        DoneCondition dnCnd01 = lnkInStrm(proc.getInputStream(), std, charset, rLck, exRr1);
        MyExRcvr exRr2 = LmUtl.nwExRcvr();
        DoneCondition dnCnd02 = lnkInStrm(proc.getErrorStream(), err, charset, rLck, exRr2);
        try {
            if (timeout > 0) {
                if (!proc.waitFor(timeout, TimeUnit.SECONDS)) {
                    proc.destroyForcibly();
                }
            } else {
                proc.waitFor();
            }
        } catch (InterruptedException ex) {
            if (proc.isAlive()) {
                proc.destroyForcibly();
            }
        }
        dnCnd01.waitCond(timeout * 1000L);
        dnCnd02.waitCond(timeout * 1000L);
        exRr1.thrEx(IOException.class);
        exRr2.thrEx(IOException.class);
    }

    public void release() {
        thdPoolExtr.shutdownNow();
    }

    //  ------------------------------------------------------------------------
    private DoneCondition lnkInStrm(InputStream in, RsltRdr inRdr, String charset, ReentrantLock aRLck, MyExRcvr exRr) {
        DoneCondition dnCond = DoneCondition.buildCond(aRLck, true);
        thdPoolExtr.submit(() -> LmUtl.run(() -> {
            try {
                BufferedReader bufRdr = new BufferedReader(new InputStreamReader(in, charset));
                String ln = bufRdr.readLine();
                while (ln != null) {
                    try {
                        if (inRdr != null) {
                            inRdr.rdLine(ln);
                        }
                    } finally {
                        ln = bufRdr.readLine();
                    }
                }
            } finally {
                dnCond.signalAll();
            }
        }, exRr));
        return dnCond;
    }

    //  ------------------------------------------------------------------------
    public static ArrayList<String> cmd(String... cmds) {
        return new ArrayList<>(Arrays.asList(cmds));
    }

    public synchronized static OsCmd inst() {
        if (inst == null) {
            inst = new OsCmd();
        }
        return inst;
    }
}
