package brnln.utils.appUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class LmUtl {

    @FunctionalInterface
    public static interface ExRcvr {

        public void rcve(Exception ex) throws Exception;
    }

    @FunctionalInterface
    public static interface MyRunnable {

        public void run() throws Exception;
    }

    @FunctionalInterface
    public static interface MySupplier<T> {

        public T get() throws Exception;
    }

    public static class MyTmrTsk extends TimerTask {

        private final Runnable rnbl;

        public MyTmrTsk(Runnable rnbl) {
            this.rnbl = rnbl;
        }

        @Override
        public void run() {
            if (rnbl != null) {
                rnbl.run();
            }
        }
    }

    public static class MyExRcvr implements ExRcvr {

        private final ArrayList<Exception> aExAL = new ArrayList<>();

        @Override
        public void rcve(Exception ex) {
            aExAL.add(ex);
        }

        public Optional<Exception> getEx() {
            return Optional.ofNullable(aExAL.isEmpty() ? null : aExAL.get(0));
        }

        public Exception aEx() {
            return aExAL.isEmpty() ? null : aExAL.get(0);
        }

        public boolean noEx() {
            return aExAL.isEmpty();
        }

        public void cln() {
            aExAL.clear();
        }

        public <T extends Exception> void thrEx(Class<T> exClz) throws T {
            T ex = LmUtl.wrapEx(exClz, aEx());
            if (ex != null) {
                throw ex;
            }
        }

        public <T extends Exception> ArrayList<T> exLst(Class<T> exClz) {
            ArrayList<T> ansAL = new ArrayList<>();
            aExAL.stream().forEachOrdered((ex) -> ansAL.add(LmUtl.wrapEx(exClz, ex)));
            return ansAL;
        }
    }

    public static class UObjHM {

        private final HashMap<String, Object> USR_OBJ_HM = new HashMap<>();

        public UObjHM put(String usrKey, Object usrObj) {
            synchronized (USR_OBJ_HM) {
                USR_OBJ_HM.put(usrKey, usrObj);
            }
            return this;
        }

        public <T> Optional<T> get(Class<T> usrClz, String usrKey) {
            synchronized (USR_OBJ_HM) {
                Object usrObj = USR_OBJ_HM.get(usrKey);
                if (usrObj != null && usrClz.isAssignableFrom(usrObj.getClass())) {
                    return Optional.of((T) usrObj);
                } else {
                    return Optional.empty();
                }
            }
        }

        public UObjHM cln() {
            USR_OBJ_HM.clear();
            return this;
        }
    }

    public static class UObj<T> {

        private T val;

        public UObj() {
        }

        public UObj(T val) {
            this.val = val;
        }

        public UObj<T> set(T obj) {
            this.val = obj;
            return this;
        }

        public Optional<T> opt() {
            return Optional.ofNullable(val);
        }

        public boolean hasVal() {
            return this.val != null;
        }

        public T get() {
            return this.val;
        }
    }

    //  ------------------------------------------------------------------------
    public static MyExRcvr nwExRcvr() {
        return new MyExRcvr();
    }

    public static MyExRcvr nwExRcvr(ExRcvr aExRcvr) {
        return new MyExRcvr() {
            @Override
            public void rcve(Exception ex) {
                super.rcve(ex);
                LmUtl.run(() -> aExRcvr.rcve(ex), null);
            }
        };
    }

    public static UObjHM nwUObjHM() {
        return new UObjHM();
    }

    public static <T> UObj<T> nwUObj() {
        return new UObj<>();
    }

    public static <T> UObj<T> nwUObj(Class<T> usrClz) {
        return new UObj<>();
    }

    public static boolean run(MyRunnable rnnbl, ExRcvr aExRcvr) {
        return run(rnnbl, null, aExRcvr);
    }

    public static boolean run(MyRunnable rnnbl, MyRunnable succRnnbl, ExRcvr aExRcvr) {
        try {
            if (rnnbl != null) {
                rnnbl.run();
            }
            if (succRnnbl != null) {
                try {
                    succRnnbl.run();
                } catch (Exception ex) {
                    /* do nothing*/
                }
            }
            return true;
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
            return false;
        }
    }

    public static boolean runfnl(MyRunnable rnnbl, MyRunnable fnlRnnbl, ExRcvr aExRcvr) {
        try {
            if (rnnbl != null) {
                rnnbl.run();
            }
            return true;
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
            return false;
        } finally {
            if (fnlRnnbl != null) {
                try {
                    fnlRnnbl.run();
                } catch (Exception ex) {
                    /* do nothing*/
                }
            }
        }
    }

    public static <T> Optional<T> get(MySupplier<T> splr, ExRcvr aExRcvr) {
        try {
            if (splr != null) {
                return Optional.ofNullable(splr.get());
            }
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getfnl(MySupplier<T> splr, MyRunnable fnlRnnbl, ExRcvr aExRcvr) {
        try {
            if (splr != null) {
                return Optional.ofNullable(splr.get());
            }
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
        } finally {
            if (fnlRnnbl != null) {
                try {
                    fnlRnnbl.run();
                } catch (Exception ex) {
                    /* do nothing*/
                }
            }
        }
        return Optional.empty();
    }

    public static boolean xRun(ReadLock lck, MyRunnable rnnbl, ExRcvr aExRcvr) /* lockRun*/ {
        try {
            if (lck != null) {
                lck.lock();
            }
            if (rnnbl != null) {
                rnnbl.run();
            }
            return true;
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
            return false;
        } finally {
            if (lck != null) {
                lck.unlock();
            }
        }
    }

    public static boolean xRun(WriteLock lck, MyRunnable rnnbl, ExRcvr aExRcvr) /* lockRun*/ {
        try {
            if (lck != null) {
                lck.lock();
            }
            if (rnnbl != null) {
                rnnbl.run();
            }
            return true;
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
            return false;
        } finally {
            if (lck != null) {
                lck.unlock();
            }
        }
    }

    public static <T> Optional<T> xGet(ReadLock lck, MySupplier<T> splr, ExRcvr aExRcvr) /* lockGet */ {
        try {
            if (lck != null) {
                lck.lock();
            }
            if (splr != null) {
                return Optional.ofNullable(splr.get());
            }
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
        } finally {
            if (lck != null) {
                lck.unlock();
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> xGet(WriteLock lck, MySupplier<T> splr, ExRcvr aExRcvr) /* lockGet */ {
        try {
            if (lck != null) {
                lck.lock();
            }
            if (splr != null) {
                return Optional.ofNullable(splr.get());
            }
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex1) {
                }
            }
        } finally {
            if (lck != null) {
                lck.unlock();
            }
        }
        return Optional.empty();
    }

    public static void apndRun(MyExRcvr exRr, MyRunnable succ, MyRunnable fail) {
        if (exRr == null) {
            return;
        }
        if (exRr.aEx() == null && succ != null) {
            LmUtl.run(succ, exRr);
        } else if (exRr.aEx() != null && fail != null) {
            LmUtl.run(fail, exRr);
        }
    }

    public static void doIf(boolean cond, MyRunnable tRun, MyRunnable fRun) throws Exception {
        if (cond && tRun != null) {
            tRun.run();
        } else if (!cond && fRun != null) {
            fRun.run();
        }
    }

    public static <T extends Exception> void doIf(boolean cond, MyRunnable tRun, MyRunnable fRun, Class<T> exClz) throws T {
        MyExRcvr exRr = LmUtl.nwExRcvr();
        LmUtl.run(() -> LmUtl.doIf(cond, tRun, fRun), exRr);
        exRr.thrEx(exClz);
    }

    //  ------------------------------------------------------------------------
    private static <T extends Exception> T wrapEx(Class<T> exClz, Exception aEx) {
        if (aEx != null && exClz.isAssignableFrom(aEx.getClass())) {
            return (T) aEx;
        } else if (aEx != null) {
            T nwEx = LmUtl.get(() -> exClz.getConstructor(Throwable.class).newInstance(aEx), null).orElse(null);
            if (nwEx != null) {
                return nwEx;
            }
        }
        return null;
    }
}
