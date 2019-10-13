package brnln.utils.appUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AppUtils {

    public static interface AP_Logger {

        public void trace(String msg);

        public void debug(String msg);

        public void info(String msg);

        public void warn(String msg);

        public void warn(String msg, Throwable t);
    }

    public static class BaseApLogger implements AP_Logger {

        @Override
        public void trace(String msg) {
        }

        @Override
        public void debug(String msg) {
        }

        @Override
        public void info(String msg) {
        }

        @Override
        public void warn(String msg) {
        }

        @Override
        public void warn(String msg, Throwable t) {
        }
    }

    public static interface LineProc {

        public void readLine(int lineNum, String line);
    }

    public static interface FileProc {

        public void beforeOpen(File file);

        public void afterClose(File file, int lineNum);

        public boolean procLine(int lineNum, String line);
    }

    public static interface DirProc {

        public boolean acceptFile(File file);

        public boolean acceptDir(File dir);
    }

    public static interface ChkTime {

        public boolean acceptTime(Date tm);
    }
    //

    public static class PerfLoopLog {

        @SuppressWarnings("Convert2Diamond")
        private static final HashMap<String, PerfLoopLog> PERF_LOOP_HM = new HashMap<String, PerfLoopLog>();
        private final String key;
        private long startTm = System.currentTimeMillis();
        private int doneCnt = 0;
        private int skipCnt = 0;

        public PerfLoopLog(String key) {
            this.key = key;
        }

        public synchronized void touch(boolean succ) {
            if (succ) {
                doneCnt++;
            } else {
                skipCnt++;
            }
        }

        public synchronized void touch(int currSuccCnt, int currSkipCnt) {
            doneCnt += currSuccCnt;
            skipCnt += currSkipCnt;
        }

        public synchronized String reset(int chkCnt) {
            int ttlCnt = doneCnt + skipCnt;
            if (ttlCnt < chkCnt) {
                return null;
            }
            //  ----  compute TPS values (start) ----
            long currTs = System.currentTimeMillis();
            long diffTs = currTs - startTm;
            int tps = 0;
            if (diffTs > 0) {
                tps = (int) (doneCnt * 1000 / diffTs);
            }
            //  ----  compute TPS values ( end ) ----
            //
            String msg = String.format("LK:%s, cnt:%s(d:%s, s:%s), take:%s ms, tps:%s, ", key, ttlCnt, doneCnt, skipCnt, diffTs, tps, 1);
            //
            //  ----  reset values (start) ----
            startTm = currTs;
            doneCnt = 0;
            skipCnt = 0;
            //  ----  reset values ( end ) ----
            //
            return msg;
        }

        //  --------------------------------------------------------------------
        public static synchronized PerfLoopLog getPerfLoopLog(String key) {
            if (!PERF_LOOP_HM.containsKey(key)) {
                PERF_LOOP_HM.put(key, new PerfLoopLog(key));
            }
            return PERF_LOOP_HM.get(key);
        }

        public static synchronized void removePerfLoopLog(String key) {
            if (PERF_LOOP_HM.containsKey(key)) {
                PERF_LOOP_HM.remove(key);
            }
        }

        public static synchronized PerfLoopLog[] getAllPerfLoopLogs() {
            return PERF_LOOP_HM.values().toArray(new PerfLoopLog[PERF_LOOP_HM.size()]);
        }

        public static synchronized PerfLoopLog[] getPerfLoopLogs(String keyPrefix) {
            @SuppressWarnings("Convert2Diamond")
            ArrayList<PerfLoopLog> ansAL = new ArrayList<PerfLoopLog>();
            PERF_LOOP_HM.values().stream().filter((perfLoopLog) -> (perfLoopLog.key.startsWith(keyPrefix))).forEachOrdered((perfLoopLog) -> ansAL.add(perfLoopLog));
            return ansAL.toArray(new PerfLoopLog[ansAL.size()]);
        }
    }

    public static class PerfActLog {

        @SuppressWarnings("Convert2Diamond")
        private static final HashMap<String, PerfActLog> MAIN_PERF_ACT_HM = new HashMap<String, PerfActLog>();
        @SuppressWarnings("Convert2Diamond")
        private static final HashMap<String, PerfActLog> THD_PERF_ACT_HM = new HashMap<String, PerfActLog>();
        public final String key;
        private long startTm = Long.MIN_VALUE;
        private long takeTs = 0;
        private long doneCnt = 0;
        private long skipCnt = 0;
        private boolean skipThis = false;

        public PerfActLog(String key) {
            this.key = key;
        }

        public synchronized void startTm() {
            if (startTm < 0) {
                startTm = System.currentTimeMillis();
                skipThis = false;
            }
        }

        public synchronized void skipThis() {
            skipThis = true;
        }

        public synchronized void storeTm() {
            if (startTm > 0) {
                takeTs += System.currentTimeMillis() - startTm;
                startTm = Long.MIN_VALUE;
                if (!skipThis) {
                    doneCnt++;
                } else {
                    skipCnt++;
                }
            }
        }

        public synchronized void storeTm(int currSuccCnt, int currSkipCnt) {
            if (startTm > 0) {
                takeTs += System.currentTimeMillis() - startTm;
                startTm = Long.MIN_VALUE;
                doneCnt += currSuccCnt;
                skipCnt += currSkipCnt;
            }
        }

        public synchronized String reset(int chkCnt) {
            long ttlCnt = doneCnt + skipCnt;
            if (ttlCnt < chkCnt) {
                return null;
            }
            //  ----  compute TPS values (start) ----
            int tps = 0;
            if (takeTs > 0) {
                tps = (int) (doneCnt * 1000 / takeTs);
            }
            //  ----  compute TPS values ( end ) ----
            //
            String msg = String.format("AK:%s, cnt:%s(d:%s, s:%s), take:%s ms, tps:%s, ", key, ttlCnt, doneCnt, skipCnt, takeTs, tps, 1);
            //
            //  ----  reset values (start) ----
            takeTs = 0;
            doneCnt = 0;
            skipCnt = 0;
            //  ----  reset values ( end ) ----
            //
            return msg;
        }

        //  --------------------------------------------------------------------
        public static PerfActLog getMainPerfActLog(String key) {
            if (!MAIN_PERF_ACT_HM.containsKey(key)) {
                MAIN_PERF_ACT_HM.put(key, new PerfActLog(key));
            }
            return MAIN_PERF_ACT_HM.get(key);
        }

        public static PerfActLog getThdPerfActLog(String key) {
            if (!THD_PERF_ACT_HM.containsKey(key)) {
                THD_PERF_ACT_HM.put(key, new PerfActLog(key));
            }
            return THD_PERF_ACT_HM.get(key);
        }

        public static synchronized void removeThdPerfActLog(String key) {
            if (THD_PERF_ACT_HM.containsKey(key)) {
                THD_PERF_ACT_HM.remove(key);
            }
        }

        public static synchronized PerfActLog[] getMainPerfActLogs() {
            return MAIN_PERF_ACT_HM.values().toArray(new PerfActLog[MAIN_PERF_ACT_HM.size()]);
        }

        public static synchronized PerfActLog[] getThdPerfActLogs(String keyPrefix) {
            @SuppressWarnings("Convert2Diamond")
            ArrayList<PerfActLog> ansAL = new ArrayList<PerfActLog>();
            THD_PERF_ACT_HM.values().stream().filter((perfActLog) -> (perfActLog.key.startsWith(keyPrefix))).forEachOrdered((perfActLog) -> ansAL.add(perfActLog));
            return ansAL.toArray(new PerfActLog[ansAL.size()]);
        }
    }
    //

    public static class TpsCtlr {

        @SuppressWarnings("Convert2Diamond")
        private static final HashMap<String, TpsCtlr> MAIN_TPS_CTLR_HM = new HashMap<String, TpsCtlr>();
        @SuppressWarnings("Convert2Diamond")
        private static final HashMap<String, TpsCtlr> THD_TPS_CTLR_HM = new HashMap<String, TpsCtlr>();
        //
        public static final TpsCtlr DFLT_TPS_CTLR = new TpsCtlr("DFLT_TPS_CTLR", 1000, 1000L * 10L);

        static {
            TpsCtlr.setMainTpsCtlr(DFLT_TPS_CTLR);
        }
        //
        public final String key;
        private final int tpsVal;
        private final long chkSegMs;
        //
        private long startTm = 0;
        private int doneCnt = 0;
        //
        //  performace
        private final int maxCnt = 10000;
        private long touchWaitTm = 0;
        private long touchStart = 0;
        private int touchCnt = 0;

        public TpsCtlr(String key, int tpsVal, long chkSegMs) {
            this.key = key;
            this.tpsVal = tpsVal;
            this.chkSegMs = chkSegMs;
        }

        @SuppressWarnings("UnnecessaryReturnStatement")
        public synchronized void touch() {
            //  ----  reset perf vars (start) ----
            if (touchStart <= 0) {
                touchStart = System.currentTimeMillis();
            }
            if (touchCnt >= maxCnt) {
                touchWaitTm = 0;
                touchStart = System.currentTimeMillis();
                touchCnt = 0;
            }
            //  ----  reset perf vars ( end ) ----
            //
            long touchStartTm = System.currentTimeMillis();
            try {
                //
                //  ----  check startTm (start) ----
                long currTs = System.currentTimeMillis();
                long diffTs = currTs - startTm;
                if (diffTs > chkSegMs) {
                    startTm = currTs;
                    doneCnt = 0;
                }
                //  ----  check startTm ( end ) ----
                //
                doneCnt++;
                if (doneCnt % tpsVal == 0) {//已達每整秒的量
                    long expcUsdMs = (doneCnt / tpsVal) * 1000L;
                    long realUsdMs = currTs - startTm;
                    if (realUsdMs >= (expcUsdMs - 10L)) {//TPS 落後(或在 10 ms 內), 故不需作延遲調控
                        return;
                    } else {
                        long fastDiff = expcUsdMs - realUsdMs;
                        AppUtils.mySleep(fastDiff - 10L);
                    }
                }
            } finally {
                touchCnt++;
                touchWaitTm += System.currentTimeMillis() - touchStartTm;
            }
        }

        public synchronized String reset(int chkCnt) {
            long ttlCnt = touchCnt;
            if (ttlCnt < chkCnt) {
                return null;
            }
            //  ----  compute TPS values (start) ----
            int tps = 0;
            long diffTm = System.currentTimeMillis() - touchStart;
            if (diffTm > 0) {
                tps = (int) (ttlCnt * 1000 / diffTm);
            }
            //  ----  compute TPS values ( end ) ----
            //
            String msg = String.format("TK:%s, cnt:%s, take:%s ms, tps:%s, wait:%s ms,", key, ttlCnt, diffTm, tps, touchWaitTm, 1);
            //
            //  ----  reset values (start) ----
            touchWaitTm = 0;
            touchStart = System.currentTimeMillis();
            touchCnt = 0;
            //  ----  reset values ( end ) ----
            //
            return msg;
        }

        public static synchronized TpsCtlr getMainTpsCtlr(String key) {
            if (MAIN_TPS_CTLR_HM.containsKey(key)) {
                return MAIN_TPS_CTLR_HM.get(key);
            }
            return DFLT_TPS_CTLR;
        }

        public static synchronized TpsCtlr getThdTpsCtlr(String key) {
            if (THD_TPS_CTLR_HM.containsKey(key)) {
                return THD_TPS_CTLR_HM.get(key);
            }
            return DFLT_TPS_CTLR;
        }

        public static synchronized void setMainTpsCtlr(TpsCtlr tpsCtlr) {
            if (!MAIN_TPS_CTLR_HM.containsKey(tpsCtlr.key)) {
                MAIN_TPS_CTLR_HM.put(tpsCtlr.key, tpsCtlr);
            }
        }

        public static synchronized void setThsTpsCtlr(TpsCtlr tpsCtlr) {
            if (!THD_TPS_CTLR_HM.containsKey(tpsCtlr.key)) {
                THD_TPS_CTLR_HM.put(tpsCtlr.key, tpsCtlr);
            }
        }

        public static synchronized void rmMainTpsCtlr(String key) {
            if (MAIN_TPS_CTLR_HM.containsKey(key)) {
                MAIN_TPS_CTLR_HM.remove(key);
            }
        }

        public static synchronized void rmThdTpsCtlr(String key) {
            if (THD_TPS_CTLR_HM.containsKey(key)) {
                THD_TPS_CTLR_HM.remove(key);
            }
        }

        public static synchronized TpsCtlr[] getMainTpsCtlrs() {
            return MAIN_TPS_CTLR_HM.values().toArray(new TpsCtlr[MAIN_TPS_CTLR_HM.size()]);
        }

        public static synchronized TpsCtlr[] getThdTpsCtlrs(String keyPrefix) {
            @SuppressWarnings("Convert2Diamond")
            ArrayList<TpsCtlr> ansAL = new ArrayList<TpsCtlr>();
            THD_TPS_CTLR_HM.values().stream().filter((tpsCtlr) -> (tpsCtlr.key.startsWith(keyPrefix))).forEachOrdered((tpsCtlr) -> ansAL.add(tpsCtlr));
            return ansAL.toArray(new TpsCtlr[ansAL.size()]);
        }
    }
    //
    private static Properties sysProps;
    private static String sysPropsFileName;
    private static final HashMap<String, Object> USR_OBJ_HM = new HashMap<>();
    //
    private static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final SimpleDateFormat STD_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat STD_SDF02 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat STD_SDF03 = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat STD_SDF04 = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat STD_SDF05 = new SimpleDateFormat("MM-dd HH:mm");
    public static final SimpleDateFormat STD_SDF06 = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat STD_SDF07 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    //
    //
    private static final DecimalFormat DAY_FMT = new DecimalFormat("#'d';-#'d'");
    private static final DecimalFormat HOUR_FMT = new DecimalFormat("#'h';-#'h'");
    private static final DecimalFormat MIN_FMT = new DecimalFormat("#'m';-#'m'");
    private static final DecimalFormat SEC_FMT = new DecimalFormat("#'s';-#'s'");
    //

    private static SecretKeySpec SKEY_SPEC = null;
    private static final SecretKeySpec DEFAULT_SKEY_SPEC = new SecretKeySpec("THIS IS DEFAULT KEY".getBytes(), "Blowfish");

    private static final Character.UnicodeBlock BASIC_BLOCK = Character.UnicodeBlock.BASIC_LATIN;
    //
    private static final ArrayList<String> EXEC_ONLY_ONE_AL = new ArrayList();
    public static final String EOO_LOG4J_CONF = "EOO_LOG4J_CONF";

    public synchronized static void setSecreyKey(byte[] key) {
        if (SKEY_SPEC == null) {
            SKEY_SPEC = new SecretKeySpec(key, "Blowfish");
        }
    }

    private synchronized static SecretKeySpec getSKeySpec() {
        if (SKEY_SPEC == null) {
            SKEY_SPEC = DEFAULT_SKEY_SPEC;
        }
        return SKEY_SPEC;
    }

    public static String getVersion() {
        return "AppUtils Version 0.0.6 (2016/12/15)";
    }

    //  ----  handle properties area (start) ----
    @SuppressWarnings({"null", "UseSpecificCatch"})
    public static synchronized void loadSystemProps(String confPropName, String local_fileName, AP_Logger log) {
        //  
        //  ----  檢查是否已載入 (start) ----
        if (sysProps != null) {
            if (log != null) {
                log.info(String.format("[%s] PROP HAD_ LOAD AND_ SKIP THIS TIME! (resource:%s)", Thread.currentThread().getName(), sysPropsFileName));
            }
            return;
        }
        //  ----  檢查是否已載入 ( end ) ----
        //
        //  ----  檢查設定檔是否存在 (start) ----
        Locale.setDefault(Locale.TAIWAN);
        String propPath = System.getProperty(confPropName);
        if (propPath == null) {
            propPath = local_fileName;
        }
        File tmpPropFile = null;
        if (propPath != null) {
            tmpPropFile = new File(propPath);
            if (!tmpPropFile.exists()) {
                tmpPropFile = null;
            }
        }
        //  ----  檢查設定檔是否存在 ( end ) ----
        //
        //  ----  處理 檔案不存在 的狀況 (start) ----
        if (tmpPropFile == null) {
            if (log != null) {
                log.warn(String.format("[%s] LOAD PROP FAIL! (resource:%s)", Thread.currentThread().getName(), local_fileName));
            }
            return;
        }
        //  ----  處理 檔案不存在 的狀況 ( end ) ----
        //
        InputStream in = null;
        try {
            boolean useExternal = true;
            if (propPath != null) {
                File propFile = new File(propPath);
                if (propFile.exists()) {
                    if (log != null) {
                        log.info(String.format("CHK_ CONF FILE (file:%s)", propFile.getAbsolutePath()));
                    }
                    in = new FileInputStream(propFile);
                }
            }
            if (in == null) {
                in = AppUtils.class.getClassLoader().getResourceAsStream(propPath);
                useExternal = false;
            }
            sysProps = new Properties();
            sysProps.load(in);
            sysPropsFileName = propPath;
            if (log != null) {
                log.debug(String.format("[APUT] LOAD %s SYS_ PROP (size:%s)", (useExternal ? "EXTL" : "INTL"), sysProps.size()));
            }
        } catch (Exception ex) {
            if (log != null) {
                log.warn(String.format("[%s] LOAD PROP FAIL! (resource:%s, message:%s)", Thread.currentThread().getName(), propPath, ex.getMessage()), ex);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public static synchronized int getIntProp(String name) {
        return getIntProp(name, Integer.MIN_VALUE);
    }

    public static synchronized int getIntProp(String name, int defaultVal) {
        if (sysProps == null || sysProps.getProperty(name) == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(sysProps.getProperty(name).trim());
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }

    public static synchronized long getLongProp(String name) {
        return getLongProp(name, Long.MIN_VALUE);
    }

    public static synchronized long getLongProp(String name, long defaultVal) {
        if (sysProps == null || sysProps.getProperty(name) == null) {
            return defaultVal;
        }
        try {
            return Long.parseLong(sysProps.getProperty(name).trim());
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }

    public static synchronized short getShortProp(String name) {
        return getShortProp(name, Short.MIN_VALUE);
    }

    public static synchronized short getShortProp(String name, short defaultVal) {
        if (sysProps == null || sysProps.getProperty(name) == null) {
            return defaultVal;
        }
        try {
            return Short.parseShort(sysProps.getProperty(name).trim());
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }

    public static String getStrProp(String name) {
        if (sysProps == null) {
            return null;
        }
        String val = sysProps.getProperty(name);
        if (val != null) {
            val = val.trim();
        }
        return val;
    }

    public static String getStrProp(String name, String dflt) {
        if (sysProps == null) {
            return dflt;
        }
        String val = sysProps.getProperty(name, dflt);
        if (val != null) {
            val = val.trim();
        }
        return val;
    }

    public static void setStrProp(String key, String val) {
        if (sysProps != null) {
            sysProps.setProperty(key, val);
        }
    }

    public static boolean getBooleanProp(String name) {
        return getBooleanProp(name, false);
    }

    public static boolean getBooleanProp(String name, boolean dflt) {
        if (sysProps == null) {
            return dflt;
        }
        String propVal = sysProps.getProperty(name);
        if (propVal != null) {
            return "true".equalsIgnoreCase(propVal)
                    || "yes".equalsIgnoreCase(propVal)
                    || "t".equalsIgnoreCase(propVal)
                    || "y".equalsIgnoreCase(propVal);
        } else {
            return dflt;
        }
    }

    public static String[] getPropNames(String expr) {
        if (sysProps == null) {
            return new String[]{};
        }
        Enumeration propEnum = sysProps.propertyNames();
        @SuppressWarnings("Convert2Diamond")
        ArrayList<String> propNameAL = new ArrayList<String>();
        while (propEnum.hasMoreElements()) {
            String propName = (String) propEnum.nextElement();
            if (Pattern.matches(expr, propName)) {
                propNameAL.add(propName);
            }
        }
        Collections.sort(propNameAL);
        return propNameAL.toArray(new String[propNameAL.size()]);
    }

    //  user object
    public static void putUsrObj(String usrKey, Object usrObj) {
        synchronized (USR_OBJ_HM) {
            USR_OBJ_HM.put(usrKey, usrObj);
        }
    }

    public static <T> T getUserObj(Class<T> usrClz, String usrKey) {
        synchronized (USR_OBJ_HM) {
            Object usrObj = USR_OBJ_HM.get(usrKey);
            if (usrObj != null && usrClz.isAssignableFrom(usrObj.getClass())) {
                return (T) usrObj;
            } else {
                return null;
            }
        }
    }
    //  ----  handle properties area ( end ) ----
    //

    //  ----  Date & Time method area (start) ----
    public static Date getDateTime(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        dateStr = dateStr.trim();
        try {
            return AppUtils.STD_SDF.parse(dateStr);
        } catch (ParseException ex) {
        }
        try {
            return AppUtils.STD_SDF02.parse(dateStr);
        } catch (ParseException ex) {
        }
        try {
            return AppUtils.STD_SDF03.parse(dateStr);
        } catch (ParseException ex) {
        }
        return null;
    }

    public static Date getDateTime(String dtFmtStr, String dtSrcStr) {
        SimpleDateFormat sdFmt = new SimpleDateFormat(dtFmtStr);
        try {
            return sdFmt.parse(dtSrcStr);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static String getCurrDateTimeStr() {
        return AppUtils.STD_SDF.format(new Date());
    }

    public static String getCurrDateTimeStrV2() {
        return AppUtils.STD_SDF07.format(new Date());
    }

    public static String getCurrDateStr() {
        return AppUtils.STD_SDF03.format(new Date());
    }

    public static String getDateTimeStr(Date date) {
        return AppUtils.STD_SDF.format(date);
    }

    public static String getDateTimeStrV2(Date date) {
        return AppUtils.STD_SDF07.format(date);
    }

    public static String getDateStr(Date date) {
        return AppUtils.STD_SDF03.format(date);
    }

    public static String getSegDateTimeStr(long segSizeMin) {
        return getSegDateTimeStr((String) null, segSizeMin);
    }

    public static String getSegDateTimeStr(String mdfyVal, long segSizeMin) {
        Date giveDt = getMdfyDateTime(mdfyVal);
        return AppUtils.getSegDateTimeStr(giveDt, segSizeMin);
    }

    public static String getSegDateTimeStr(Date giveDt, long segSizeMin) {
        long giveTs = giveDt.getTime();
        long segSize = 1000L * 60L * 5L;
        if (segSize >= 1) {
            segSize = 1000L * 60L * segSizeMin;
        }
        long segTs = ((giveTs / segSize) + 1) * (segSize);
        return AppUtils.STD_SDF02.format(new Date(segTs));
    }

    public static long getSegDateTime(long segSizeMin) {
        return getSegDateTime(null, segSizeMin);
    }

    public static long getSegDateTime(String mdfyVal, long segSizeMin) {
        if (segSizeMin <= 0) {
            segSizeMin = 5;
        }
        Date giveDt = getMdfyDateTime(mdfyVal);
        long giveTs = giveDt.getTime();
        long segSize = 1000L * 60L * segSizeMin;
        long segTs = ((giveTs / segSize) + 1) * (segSize);
        return segTs;
    }

    public static Date getMdfyDateTime(String mdfyVal) {
        if (mdfyVal != null) {
            mdfyVal = mdfyVal.toLowerCase();
            if (mdfyVal.startsWith("+")) {
                mdfyVal = mdfyVal.substring(1);
            }
            try {
                long offVal = DAY_FMT.parse(mdfyVal).longValue();
                return new Date(System.currentTimeMillis() + 1000L * 60L * 60L * 24L * offVal);
            } catch (ParseException | RuntimeException ex) {
            }
            try {
                long offVal = HOUR_FMT.parse(mdfyVal).longValue();
                return new Date(System.currentTimeMillis() + 1000L * 60L * 60L * offVal);
            } catch (ParseException | RuntimeException ex) {
            }
            try {
                long offVal = MIN_FMT.parse(mdfyVal).longValue();
                return new Date(System.currentTimeMillis() + 1000L * 60L * offVal);
            } catch (ParseException | RuntimeException ex) {
            }
            try {
                long offVal = SEC_FMT.parse(mdfyVal).longValue();
                return new Date(System.currentTimeMillis() + 1000L * offVal);
            } catch (ParseException | RuntimeException ex) {
            }
        }
        return new Date();
    }

    public static long convTimeDescToMs(String tmDsc) {
        if (tmDsc == null) {
            return -1L;
        }
        long sign = tmDsc.startsWith("-") ? -1L : 1L;
        try {
            return sign * 1000L * 60L * 60L * 24L * DAY_FMT.parse(tmDsc).longValue();
        } catch (ParseException | RuntimeException ex) {
        }
        try {
            return sign * 1000L * 60L * 60L * HOUR_FMT.parse(tmDsc).longValue();
        } catch (ParseException | RuntimeException ex) {
        }
        try {
            return sign * 1000L * 60L * MIN_FMT.parse(tmDsc).longValue();
        } catch (ParseException | RuntimeException ex) {
        }
        try {
            return sign * 1000L * SEC_FMT.parse(tmDsc).longValue();
        } catch (ParseException | RuntimeException ex) {
        }
        return -1L;
    }
    //  ----  Date & Time method area ( end ) ----
    //

    //  ----  proc file area (start) ----
    public static void procDir(File dir, final DirProc dirProc, FileProc fileProc, AP_Logger baseApLogger) {
        if (dir == null || dir.isFile()) {
            return;
        }
        //
        File[] files = dir.listFiles((File file) -> {
            if (file.isFile()) {
                return dirProc.acceptFile(file);
            } else if (file.isDirectory()) {
                return dirProc.acceptDir(file);
            } else {
                return false;
            }
        });
        Arrays.sort(files);
        //
        for (File file : files) {
            if (file.isFile()) {//FILE
                AppUtils.procFile(file, fileProc, baseApLogger);
            } else {//DIR
                AppUtils.procDir(file, dirProc, fileProc, baseApLogger);
            }
        }
    }

    public static void procFile(File file, FileProc fileProc, AP_Logger baseApLogger) {
        AppUtils.procFile(file, fileProc, baseApLogger, null);
    }

    public static void procFile(File file, FileProc fileProc, AP_Logger baseApLogger, String charsetName) {
        if (file == null || file.isDirectory()) {
            return;
        }
        //
        //  ----  proc before open file (start) ----
        try {
            if (fileProc != null) {
                fileProc.beforeOpen(file);
            }
        } catch (Throwable ex) {
            if (baseApLogger != null) {
                baseApLogger.warn(String.format("FAIL OPEN FILE (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        }
        //  ----  proc before open file ( end ) ----
        //
        boolean isGzip = isGzipFile(file, baseApLogger);
        //
        FileInputStream fIn = null;
        GZIPInputStream gzipIn = null;
        InputStreamReader inRdr = null;
        BufferedReader bRdr = null;
        int lineNum = 1;
        try {
            fIn = new FileInputStream(file);
            if (isGzip) {
                gzipIn = new GZIPInputStream(fIn);
                if (charsetName != null) {
                    inRdr = new InputStreamReader(gzipIn, charsetName);
                } else {
                    inRdr = new InputStreamReader(gzipIn);
                }
            } else if (charsetName != null) {
                inRdr = new InputStreamReader(fIn, charsetName);
            } else {
                inRdr = new InputStreamReader(fIn);
            }
            bRdr = new BufferedReader(inRdr);
            String lineData = bRdr.readLine();
            while (lineData != null) {
                try {
                    if (fileProc != null) {
                        boolean nextLine = fileProc.procLine(lineNum, lineData);
                        if (!nextLine) {
                            break;
                        }
                    }
                } catch (Throwable ex) {
                    if (baseApLogger != null) {
                        baseApLogger.warn(String.format("FAIL PROC LINE (file:%s, line:%s, rsn:%s)", file.getName(), lineNum, ex.getMessage()), ex);
                    } else {
                        ex.printStackTrace(System.err);
                    }
                } finally {
                    lineData = bRdr.readLine();
                    lineNum++;
                }
            }
        } catch (IOException ex) {
            if (baseApLogger != null) {
                baseApLogger.warn(String.format("FAIL PROC FILE (file:%s, rsn:%s)", file.getName(), ex.getMessage()));
            }
        } finally {
            AppUtils.close_ignore_err(bRdr);
            AppUtils.close_ignore_err(inRdr);
            AppUtils.close_ignore_err(gzipIn);
            AppUtils.close_ignore_err(fIn);
        }
        //  
        //  ----  proc after close file (start) ----
        try {
            if (fileProc != null) {
                fileProc.afterClose(file, lineNum - 1);
            }
        } catch (Throwable ex) {
            if (baseApLogger != null) {
                baseApLogger.warn(String.format("FAIL CLSE FILE (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        }
        //  ----  proc after close file ( end ) ----
        //  ----  proc after close file ( end ) ----
    }

    public static void readFile(File file, final LineProc lineProc) {
        AppUtils.readFile(file, lineProc, null);
    }

    public static void readFile(File file, final LineProc lineProc, String charsetName) {
        AppUtils.procFile(file, new FileProc() {

            @Override
            public void beforeOpen(File file) {
            }

            @Override
            public void afterClose(File file, int lineNum) {
            }

            @Override
            public boolean procLine(int lineNum, String line) {
                if (lineProc == null) {
                    return false;
                } else {
                    lineProc.readLine(lineNum, line);
                    return true;
                }
            }
        }, null, charsetName);
    }

    public static boolean isMatchTime(String reg, String fileName, ChkTime chkTime) {
        SimpleDateFormat sdFmt = new SimpleDateFormat(reg);
        try {
            Date d01 = sdFmt.parse(fileName);
            if (chkTime != null) {
                return chkTime.acceptTime(d01);
            } else {
                return false;
            }
        } catch (ParseException ex) {
            return false;
        }
    }

    public static void close_ignore_err(FileInputStream fIn) {
        if (fIn != null) {
            try {
                fIn.close();
            } catch (IOException ex) {
            }
        }
    }

    public static void close_ignore_err(GZIPInputStream gzipIn) {
        if (gzipIn != null) {
            try {
                gzipIn.close();
            } catch (IOException ex) {
            }
        }
    }

    public static void close_ignore_err(InputStreamReader inRdr) {
        if (inRdr != null) {
            try {
                inRdr.close();
            } catch (IOException ex) {
            }
        }
    }

    public static void close_ignore_err(InputStream inStr) {
        if (inStr != null) {
            try {
                inStr.close();
            } catch (IOException ex) {
            }
        }
    }

    public static void close_ignore_err(FileReader fRdr) {
        if (fRdr != null) {
            try {
                fRdr.close();
            } catch (IOException ex) {
            }
        }
    }

    public static void close_ignore_err(BufferedReader bRdr) {
        if (bRdr != null) {
            try {
                bRdr.close();
            } catch (IOException ex) {
            }
        }
    }
    //  Private

    private static boolean isGzipFile(File file, AP_Logger log) {
        //  ----  check is gzip file (start) ----
        boolean isGzip = false;
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
            byte[] tmpBs = new byte[2];
            int n = fileIn.read(tmpBs);
            if (n >= 2) {
                int head = ((int) tmpBs[0] & 0xff) | ((tmpBs[1] << 8) & 0xff00);
                if (GZIPInputStream.GZIP_MAGIC == head) {
                    isGzip = true;
                }
            }
        } catch (IOException ex) {
            if (log != null) {
                log.warn(String.format("FAIL CHK_ GZIP (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        } finally {
            AppUtils.close_ignore_err(fileIn);
        }
        //  ----  check is gzip file ( end ) ----
        //
        return isGzip;
    }
    //  ----  proc file area ( end ) ----
    //

    //  ----  convert type area (start) ----
    public static String convToUnicodeStr(String origStr) {
        if (origStr == null) {
            return null;
        }
        StringBuilder strBldr = new StringBuilder();
        for (char ch : origStr.toCharArray()) {
            strBldr.append("\\u");
            if (ch < 0x00FF) {
                strBldr.append("00");
            } else if (ch < 0x0FFF) {
                strBldr.append("0");
            }
            strBldr.append(Integer.toHexString(ch));
        }
        return strBldr.toString();
    }

    public static String convertToHexStr(byte[] bs) {
        if (bs == null) {
            return "x";
        }
        return convertToHexStr(bs, 0, bs.length);
    }

    public static String convertToHexStr(byte[] bs, int offset, int size) {
        if (bs == null) {
            return "x";
        }
        if (offset < 0) {
            offset = 0;
        }
        if (offset > bs.length) {
            offset = bs.length - 1;
        }
        if (offset + size > bs.length) {
            size = bs.length;
        }
        StringBuilder strBldr = new StringBuilder();
        for (int idx = offset; idx < offset + size; idx++) {
            byte b01 = bs[idx];
            byte lowB = (byte) (b01 & 0x0F);
            byte highB = (byte) ((b01 >>> 4) & 0x0F);
            strBldr.append(HEX_CHAR[highB]);
            strBldr.append(HEX_CHAR[lowB]);
        }
        return strBldr.toString();
    }

    public static byte[] convertToBytes(String hexStr) {
        if (hexStr == null || hexStr.length() < 2) {
            return new byte[]{};
        }
        byte[] bs = new byte[hexStr.length() / 2];
        for (int idx = 0; idx < bs.length * 2; idx += 2) {
            char ch1 = Character.toUpperCase(hexStr.charAt(idx));
            char ch2 = Character.toUpperCase(hexStr.charAt(idx + 1));
            int a01 = Arrays.binarySearch(HEX_CHAR, ch1);
            int a02 = Arrays.binarySearch(HEX_CHAR, ch2);
            byte b01 = (byte) ((a01 << 4) + (a02));
            bs[idx / 2] = b01;
        }
        return bs;
    }

    public static String converToDateTimeStr(long tm) {
        String tmStr = "x";
        if (tm > 0) {
            tmStr = STD_SDF.format(new Date(tm));
        }
        return tmStr;
    }

    public static String converToDateStr(long tm) {
        String tmStr = "x";
        if (tm > 0) {
            tmStr = STD_SDF03.format(new Date(tm));
        }
        return tmStr;
    }

    public static String converToDateStr_y4m2(long tm) {
        String tmStr = "x";
        if (tm > 0) {
            tmStr = STD_SDF04.format(new Date(tm));
        }
        return tmStr;
    }

    public static String converToDateStr_MDHM(long tm) {
        String tmStr = "x";
        if (tm > 0) {
            tmStr = STD_SDF05.format(new Date(tm));
        }
        return tmStr;
    }

    public static String converToTimeStr(long tm) {
        String tmStr = "x";
        if (tm > 0) {
            tmStr = STD_SDF06.format(new Date(tm));
        }
        return tmStr;
    }

    public static String convertToHexStr(long val) {
        String hexStr = Long.toHexString(val);
        while (hexStr.length() < 16) {
            hexStr = "0" + hexStr;
        }
        if (hexStr.length() > 16) {
            hexStr = hexStr.substring(hexStr.length() - 16);
        }
        return hexStr;
    }

    public static String convertToHexStr(int val) {
        String hexStr = Long.toHexString(val);
        while (hexStr.length() < 8) {
            hexStr = "0" + hexStr;
        }
        if (hexStr.length() > 8) {
            hexStr = hexStr.substring(hexStr.length() - 8);
        }
        return hexStr;
    }

    public static String convertToHexStr(short val) {
        String hexStr = Long.toHexString(val);
        while (hexStr.length() < 4) {
            hexStr = "0" + hexStr;
        }
        if (hexStr.length() > 4) {
            hexStr = hexStr.substring(hexStr.length() - 4);
        }
        return hexStr;
    }

    public static String convertToHexStr(byte val) {
        byte b01 = val;
        byte lowB = (byte) (b01 & 0x0F);
        byte highB = (byte) ((b01 >>> 4) & 0x0F);
        return "" + HEX_CHAR[highB] + HEX_CHAR[lowB];
//        String hexStr = Long.toHexString(val);
//        while (hexStr.length() < 2) {
//            hexStr = "0" + hexStr;
//        }
//        if (hexStr.length() > 2) {
//            hexStr = hexStr.substring(hexStr.length() - 2);
//        }
//        return hexStr; 
    }

    public static byte[] convertLongToBytes(long val) {
        byte[] bs = new byte[8];
        bs[0] = (byte) ((val >>> 56) & 0xFF);
        bs[1] = (byte) ((val >>> 48) & 0xFF);
        bs[2] = (byte) ((val >>> 40) & 0xFF);
        bs[3] = (byte) ((val >>> 32) & 0xFF);
        bs[4] = (byte) ((val >>> 24) & 0xFF);
        bs[5] = (byte) ((val >>> 16) & 0xFF);
        bs[6] = (byte) ((val >>> 8) & 0xFF);
        bs[7] = (byte) ((val) & 0xFF);
        return bs;
    }

    public static byte[] convertIntToBytes(int val) {
        byte[] bs = new byte[4];
        bs[0] = (byte) ((val >>> 24) & 0xFF);
        bs[1] = (byte) ((val >>> 16) & 0xFF);
        bs[2] = (byte) ((val >>> 8) & 0xFF);
        bs[3] = (byte) ((val) & 0xFF);
        return bs;
    }

    public static byte[] convertShortToBytes(short val) {
        byte[] bs = new byte[2];
        bs[0] = (byte) ((val >>> 8) & 0xFF);
        bs[1] = (byte) ((val) & 0xFF);
        return bs;
    }

    public static long convertBytesToLong(byte[] bs) {
        return convertBytesToLong(bs, 0);
    }

    public static long convertBytesToLong(byte[] bs, int pos) {
        if (bs.length >= pos + 8) {
            return 0
                    + ((long) (bs[pos + 0] & 0xFF) << 56)//
                    + ((long) (bs[pos + 1] & 0xFF) << 48)//
                    + ((long) (bs[pos + 2] & 0xFF) << 40)//
                    + ((long) (bs[pos + 3] & 0xFF) << 32)//
                    + ((long) (bs[pos + 4] & 0xFF) << 24)//
                    + ((bs[pos + 5] & 0xFF) << 16)//
                    + ((bs[pos + 6] & 0xFF) << 8)//
                    + ((bs[pos + 7] & 0xFF))//
                    ;
        } else {
            return Long.MIN_VALUE;
        }
    }

    public static int convertBytesToInt(byte[] bs) {
        return convertBytesToInt(bs, 0);
    }

    public static int convertBytesToInt(byte[] bs, int pos) {
        if (bs.length >= pos + 4) {
            return 0
                    + ((bs[pos + 0] & 0xFF) << 24)//
                    + ((bs[pos + 1] & 0xFF) << 16)//
                    + ((bs[pos + 2] & 0xFF) << 8)//
                    + ((bs[pos + 3] & 0xFF))//
                    ;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    public static short convertBytesToShort(byte[] bs) {
        return convertBytesToShort(bs, 0);
//        return (short) (((bs[0] & 0xFF) << 8) + ((bs[1] & 0xFF)));
    }

    public static short convertBytesToShort(byte[] bs, int pos) {
        if (bs.length >= pos + 2) {
            return (short) (((bs[pos + 0] & 0xFF) << 8) + ((bs[pos + 1] & 0xFF)));
        } else {
            return Short.MIN_VALUE;
        }
    }

    public static int convertToInt(String arg, int defaultVal) {
        if (arg != null) {
            try {
                return Integer.parseInt(arg.trim());
            } catch (NumberFormatException ex) {
                return defaultVal;
            }
        }
        return defaultVal;
    }

    public static long convertToLong(String arg, long defaultVal) {
        if (arg != null) {
            try {
                return Long.parseLong(arg);
            } catch (NumberFormatException ex) {
                return defaultVal;
            }
        }
        return defaultVal;
    }

    public static <T> T convertToClz(Class<T> usrClz, Object usrObj) {
        if (usrObj != null && usrClz.isAssignableFrom(usrObj.getClass())) {
            return (T) usrObj;
        } else {
            return null;
        }
    }

    public static byte[] convert_7bit_to_8bit(byte[] bit7Datas) {
        int byteCnt = (bit7Datas.length * 8) / 7;
        byte[] bit8Datas = new byte[byteCnt];
        for (int idx = 0; idx < bit8Datas.length; idx++) {
            int b7ttlBtOf = idx * 7;
            int b7Idx = b7ttlBtOf / 8;
            int b7startPos = b7ttlBtOf % 8;
            //
            byte tB01;
            byte tB02 = 0;
            switch (b7startPos) {
                case 0: {
                    tB01 = (byte) (bit7Datas[b7Idx] & 0x7F);
                    bit8Datas[idx] = tB01;
                    break;
                }
                case 7: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0x80) >> 7);
                    tB02 = (byte) ((bit7Datas[b7Idx + 1] & 0x3F) << 1);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
                case 6: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0xC0) >> 6);
                    tB02 = (byte) ((bit7Datas[b7Idx + 1] & 0x1F) << 2);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
                case 5: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0xE0) >> 5);
                    tB02 = (byte) ((bit7Datas[b7Idx + 1] & 0x0F) << 3);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
                case 4: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0xF0) >> 4);
                    tB02 = (byte) ((bit7Datas[b7Idx + 1] & 0x07) << 4);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
                case 3: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0xF8) >> 3);
                    tB02 = (byte) ((bit7Datas[b7Idx + 1] & 0x03) << 5);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
                case 2: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0xFC) >> 2);
                    tB02 = (byte) ((bit7Datas[b7Idx + 1] & 0x01) << 6);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
                case 1: {
                    tB01 = (byte) ((bit7Datas[b7Idx] & 0xFE) >> 1);
                    bit8Datas[idx] = (byte) (tB01 | tB02);
                    break;
                }
            }
        }
        return bit8Datas;
    }
    //  ----  convert type area ( end ) ----
    //

    //  ----  jdbc connection area (start) ----
    public static SQLException close_conn_return_err(Connection dbConn) {
        if (dbConn == null) {
            return null;
        }
        try {
            dbConn.close();
            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }

    public static SQLException close_pStmt_return_err(PreparedStatement pStmt) {
        if (pStmt != null) {
            try {
                pStmt.close();
                return null;
            } catch (SQLException ex) {
                return ex;
            }
        } else {
            return null;
        }
    }

    public static SQLException close_stmt_ignore_err(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
                return null;
            } catch (SQLException ex) {
                return ex;
            }
        } else {
            return null;
        }
    }

    public static SQLException close_rslt_ignore_err(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                return null;
            } catch (SQLException ex) {
                return ex;
            }
        } else {
            return null;
        }
    }

    public static void exec_update_sql_ignore_err(Statement stmt, String sqlStr) {
        if (stmt != null) {
            try {
                stmt.executeUpdate(sqlStr);
            } catch (SQLException ex) {
            }
        }
    }
    //  ----  jdbc connection area ( end ) ----
    //

    //  ----  convert bytes between primitive type (start) ----
    public static short readShort(byte[] bs, int p) {
        return (short) (0 + ((bs[p + 0] & 0xFF) << 8) + ((bs[p + 1] & 0xFF)));
    }
    //  ----  convert bytes between primitive type ( end ) ----
    //
    //  ----  password encrypt (start) ----
    private static final char[] BASE64_HEX_CHAR = new char[]{
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static String myBase64Encode(byte[] bs) {
        if (bs == null) {
            return "";
        }
        int remain = bs.length % 3;
        if (remain == 2) {
            byte[] tmpBs = new byte[bs.length + 1];
            System.arraycopy(bs, 0, tmpBs, 0, bs.length);
            tmpBs[bs.length] = (byte) 0;
            bs = tmpBs;
        } else if (remain == 1) {
            byte[] tmpBs = new byte[bs.length + 2];
            System.arraycopy(bs, 0, tmpBs, 0, bs.length);
            tmpBs[bs.length] = (byte) 0;
            tmpBs[bs.length + 1] = (byte) 0;
            bs = tmpBs;
        }
        //System.out.println("bs:" + AppUtils.convertToHexStr(bs) + "<");
        StringBuilder strBldr = new StringBuilder();
        for (int idx = 0; idx < bs.length; idx += 3) {
            byte b01 = bs[idx];
            byte b02 = bs[idx + 1];
            byte b03 = bs[idx + 2];
            //  for Char 01[0~6] =>B1[0~5]
            byte tmpB = (byte) ((b01 >> 2) & 0x3F);
            strBldr.append(BASE64_HEX_CHAR[tmpB]);
            //  for Char 02[0~6] => B1[6,7] B2[0~3]
            tmpB = (byte) (((b01 << 4) & 0x30) + ((b02 >> 4) & 0x0F));
            strBldr.append(BASE64_HEX_CHAR[tmpB]);
            //  for Char 03[0~6] => B2[4~7] B3[0,1]
            tmpB = (byte) (((b02 << 2) & 0x3C) + ((b03 >> 6) & 0x03));
            strBldr.append(BASE64_HEX_CHAR[tmpB]);
            //  for Char 04[0~6] => B3[2~7]
            tmpB = (byte) (b03 & 0x3F);
            strBldr.append(BASE64_HEX_CHAR[tmpB]);
        }
        if (remain == 2) {
            strBldr.deleteCharAt(strBldr.length() - 1);
            strBldr.append("=");
        }
        if (remain == 1) {
            strBldr.deleteCharAt(strBldr.length() - 2);
            strBldr.deleteCharAt(strBldr.length() - 1);
            strBldr.append("==");
        }
        return strBldr.toString();
    }

    private static byte _myB64decode_conv_ch_to_byte(char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return (byte) (ch - 'A');
        } else if (ch >= 'a' && ch <= 'z') {
            return (byte) (ch - 'a' + 26);
        } else if (ch >= '0' && ch <= '9') {
            return (byte) (ch - '0' + 52);
        } else if (ch == '+') {
            return 62;
        } else if (ch == '/') {
            return 63;
        }
        return 0;
    }

    public static byte[] myBase64Decode(String b64Str) {
        if (b64Str == null || b64Str.length() % 4 != 0 || b64Str.length() < 4) {
            return new byte[]{};
        }
        char[] chs = b64Str.toCharArray();
        int truncateCnt = 0;
        if (chs[chs.length - 1] == '=' && chs[chs.length - 2] == '=') {
            truncateCnt = 2;
        } else if (chs[chs.length - 1] == '=') {
            truncateCnt = 1;
        }

        byte[] ansBs = new byte[(chs.length / 4) * 3 - truncateCnt];
        //for (int idx = 0; idx < ansBs.length; idx += 3) {
        for (int chsIdx = 0, bsIdx = 0; chsIdx < chs.length; chsIdx += 4, bsIdx += 3) {
            byte chB1 = _myB64decode_conv_ch_to_byte(chs[chsIdx]);
            byte chB2 = _myB64decode_conv_ch_to_byte(chs[chsIdx + 1]);
            byte chB3 = _myB64decode_conv_ch_to_byte(chs[chsIdx + 2]);
            byte chB4 = _myB64decode_conv_ch_to_byte(chs[chsIdx + 3]);
            //
            //  B01 => chB1[2~7] chB2[2,3]
            ansBs[bsIdx] = (byte) ((chB1 << 2 & 0xFC) + (chB2 >> 4 & 0x3));
            //  B02 => chB2[4~7] chB3[2~5]
            if (bsIdx + 1 < ansBs.length) {
                ansBs[bsIdx + 1] = (byte) ((chB2 << 4 & 0xF0) + (chB3 >> 2 & 0x0F));
            }
            //  B03 => chB3[6,7] chB4[2~7]
            if (bsIdx + 2 < ansBs.length) {
                ansBs[bsIdx + 2] = (byte) ((chB3 << 6 & 0xC0) + (chB4 & 0x3F));
            }
        }
        return ansBs;
    }

    public static String pwdEncode(String clearPwd) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("Blowfish");
//        cipher.init(Cipher.ENCRYPT_MODE, SKEY_SPEC);
        cipher.init(Cipher.ENCRYPT_MODE, AppUtils.getSKeySpec());
        byte[] encPwd = cipher.doFinal(clearPwd.getBytes());
        return myBase64Encode(encPwd);
    }

    @SuppressWarnings("UseSpecificCatch")
    public static String pwdEncode_noError(String clearPwd) {
        try {
            return AppUtils.pwdEncode(clearPwd);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String pwdDecode(String endPwd) throws Exception {
        Cipher cipher = Cipher.getInstance("Blowfish");
//        cipher.init(Cipher.DECRYPT_MODE, SKEY_SPEC);
        cipher.init(Cipher.DECRYPT_MODE, AppUtils.getSKeySpec());
        return new String(cipher.doFinal(myBase64Decode(endPwd)));
    }

    @SuppressWarnings("UseSpecificCatch")
    public static String pwdDecode_noError(String endPwd) {
        try {
            Cipher cipher = Cipher.getInstance("Blowfish");
//            cipher.init(Cipher.DECRYPT_MODE, SKEY_SPEC);
            cipher.init(Cipher.DECRYPT_MODE, AppUtils.getSKeySpec());
            return new String(cipher.doFinal(myBase64Decode(endPwd)));
        } catch (Exception ex) {
            return null;
        }
    }
    //  ----  password encrypt ( end ) ----
    //

    //  ----  search method (start) ----
    public static int searchInBytes(byte[] expectedBs, byte[] dataBs) {
        return searchInBytes(expectedBs, dataBs, 0, dataBs.length);
    }

    public static int searchInBytes(byte[] expectedBs, byte[] dataBs, int fromIdx, int toIdx) {
        if (false
                || expectedBs == null || dataBs == null
                || expectedBs.length == 0 || dataBs.length == 0 || expectedBs.length > dataBs.length
                || fromIdx < 0 || toIdx < 0 || fromIdx >= toIdx || toIdx - fromIdx < expectedBs.length
                || fromIdx + expectedBs.length > dataBs.length
                || toIdx > dataBs.length
                || false) {
            return -1;
        }
        for (int idxA = 0; idxA < dataBs.length - expectedBs.length; idxA++) {
            boolean matched = true;
            for (int idxB = 0; idxB < expectedBs.length; idxB++) {
                if (dataBs[idxA + idxB] != expectedBs[idxB]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return idxA;
            }
        }
        return -1;
    }
    //  ----  search method ( end ) ----
    //

    //  ----  String Utils (start) ----
    public static boolean isBasicLatin(String msg) {
        if (msg == null) {
            return true;
        }
        for (char ch : msg.toCharArray()) {
            if (BASIC_BLOCK != Character.UnicodeBlock.of(ch)) {
                return false;
            }
        }
        return true;
    }

    public static String[] splitMsg(String msg, int segSize) {
        int setCnt = (msg.length() / segSize) + ((msg.length() % segSize) > 0 ? 1 : 0);
        String[] msgs = new String[setCnt];
        for (int idx = 0; idx < msgs.length; idx++) {
            int startPos = idx * segSize;
            int stopPos = (idx + 1) * segSize;
            if (stopPos > msg.length()) {
                stopPos = msg.length();
            }
            msgs[idx] = msg.substring(startPos, stopPos);
        }
        return msgs;
    }

    public static String getField(String lineData, int idx) {
        return getField(lineData, " ", idx);
    }

    public static String getField(String lineData, String separator, int idx) {
        if (lineData == null || separator == null || idx < 0) {
            return "";
        }
        if (idx > 0) {
            lineData = trimFileds(lineData, separator, idx);
        }
        String[] flds = lineData.split(separator, 2);
        if (flds.length < 1) {
            return "";
        } else {
            return flds[0].trim();
        }
    }

    public static String trimFileds(String lineData, int fldCnt) {
        return trimFileds(lineData, " ", fldCnt);
    }

    public static String trimFileds(String lineData, String separator, int fldCnt) {
        if (lineData == null || separator == null || fldCnt <= 0) {
            return "";
        }
        String[] flds = lineData.split(separator, fldCnt + 1);
        if (flds.length < fldCnt + 1) {
            return "";
        } else {
            return flds[fldCnt];
        }
    }

    public static String replaceStr(String sourceStr, String regx, String replace) {
        //  ----  檢查傳入參數 (start) ----
        if (sourceStr == null) {
            return null;
        }
        if (regx == null || regx.equals("")) {
            return sourceStr;
        }
        if (replace == null) {
            replace = "";
        }
        //  ----  檢查傳入參數 ( end ) ----
        //
        StringBuilder dstBldr = new StringBuilder(sourceStr);
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(sourceStr);
        @SuppressWarnings("Convert2Diamond")
        ArrayList<int[]> findAL = new ArrayList<int[]>();
        @SuppressWarnings("Convert2Diamond")
        ArrayList<String> origStrAL = new ArrayList<String>();
        while (m.find()) {
            findAL.add(0, new int[]{m.start(), m.end()});
            origStrAL.add(0, m.group());
        }
        while (!findAL.isEmpty()) {
            int[] grpPos = findAL.remove(0);
            String origStr = origStrAL.remove(0);
            if (replace.contains("${matched_string}")) {
                String nwReplace = _replace_all_by_pattern(replace, "\\$\\{matched_string}", origStr);
                dstBldr.replace(grpPos[0], grpPos[1], nwReplace);
            } else {
                dstBldr.replace(grpPos[0], grpPos[1], replace);
            }
        }
        return dstBldr.toString();
    }

    private static String _replace_all_by_pattern(String sourceStr, String regx, String replace) {
        //  ----  檢查傳入參數 (start) ----
        if (sourceStr == null) {
            return null;
        }
        if (regx == null || regx.equals("")) {
            return sourceStr;
        }
        if (replace == null) {
            replace = "";
        }
        //  ----  檢查傳入參數 ( end ) ----
        //
        StringBuilder dstBldr = new StringBuilder(sourceStr);
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(sourceStr);
        @SuppressWarnings("Convert2Diamond")
        ArrayList<int[]> findAL = new ArrayList<int[]>();
        while (m.find()) {
            findAL.add(0, new int[]{m.start(), m.end()});
        }
        while (!findAL.isEmpty()) {
            int[] grpPos = findAL.remove(0);
            dstBldr.replace(grpPos[0], grpPos[1], replace);
        }
        return dstBldr.toString();
    }

    public static String getMatchFieldValue(String sourceStr, String regx, String fieldName) {
        if (sourceStr == null || regx == null || fieldName == null || !regx.contains(fieldName)) {
            return null;
        }
        //  ----  將 regx 拆成二段 (start) ----
        int idx01 = regx.indexOf(fieldName);
        String regx01 = regx.substring(0, idx01);
        String regx02 = regx.substring(idx01 + fieldName.length());
        String nwRegx = regx01 + ".*" + regx02;
        //  ----  將 regx 拆成二段 ( end ) ----
        //
        //  ----  取得新的 sourceStr (start) ----
        String nwSourceStr;
        try {
            Pattern p = Pattern.compile(nwRegx);
            Matcher m = p.matcher(sourceStr);
            if (m.find()) {
                nwSourceStr = m.group();
            } else {
                return null;
            }
        } catch (PatternSyntaxException ex) {
            return null;
        }
        //  ----  取得新的 sourceStr ( end ) ----
        //
        //  ----  取得符合 regx01 的字串 (start) ----
        int pos01;
        if (regx01.length() > 0) {
            try {
                Pattern p = Pattern.compile(regx01);
                Matcher m = p.matcher(nwSourceStr);
                if (m.find()) {
                    pos01 = m.end();
                } else {
                    return null;
                }
            } catch (PatternSyntaxException ex) {
                return null;
            }
        } else {
            pos01 = 0;
        }
        //  ----  取得符合 regx01 的字串 ( end ) ----
        //
        //  ----  取得符合 regx02 的字串 (start) ----
        int pos02 = -1;
        if (regx02.length() > 0) {
            try {
                Pattern p = Pattern.compile(regx02);
                Matcher m = p.matcher(nwSourceStr);
                while (m.find()) {
                    pos02 = m.start();
                }
                if (pos02 < 0) {
                    return null;
                }
            } catch (PatternSyntaxException ex) {
                return null;
            }
        } else {
            pos02 = nwSourceStr.length();
        }
        //  ----  取得符合 regx01 的字串 ( end ) ----
        //
        //  ----  取出符合的 Field Value (start) ----
        return nwSourceStr.substring(pos01, pos02);
        //  ----  取出符合的 Field Value ( end ) ----
        //
    }
    //  ----  String Utils ( end ) ----

    //
    //  ----  Application method area (start) ----
    public static void mySleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
        }
    }

    public static int getJVM_PID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (name != null && name.indexOf("@") > 0) {
            String pidStr = name.substring(0, name.indexOf("@"));
            try {
                return Integer.parseInt(pidStr);
            } catch (NumberFormatException ex) {
            }
        }
        return -1;
    }

    public static void show_all_active_threads(String... skipPrefixs) {
        String str = get_all_active_thrd(skipPrefixs);
        System.err.println(str);
    }

    public static String get_all_active_thrd(String... skipPrefixs) {
        StringBuilder strBldr = new StringBuilder();
        HashMap<Thread, StackTraceElement[]> ansHM = get_all_active_threads(skipPrefixs);
        ansHM.entrySet().stream().map((entry) -> {
            Thread thd = entry.getKey();
            ThreadGroup thdGrp = thd.getThreadGroup();
            String grpName = thdGrp.getName();
            String thdName = thd.getName();
            strBldr.append(String.format("  thd:%s/:%s\n", grpName, thdName));
            StackTraceElement[] stks = entry.getValue();
            return stks;
        }).forEachOrdered((stks) -> {
            for (StackTraceElement stk : stks) {
                strBldr.append(String.format("    %s\n", stk));
            }
        });
        return strBldr.toString();
    }

    public static HashMap<Thread, StackTraceElement[]> get_all_active_threads(String... skipPrefixs) {
        @SuppressWarnings("Convert2Diamond")
        HashMap<Thread, StackTraceElement[]> ansHM = new HashMap<Thread, StackTraceElement[]>();
        Map<Thread, StackTraceElement[]> thdStkHM = Thread.getAllStackTraces();
        MAIN_LOOP:
        for (Map.Entry<Thread, StackTraceElement[]> entry : thdStkHM.entrySet()) {
            Thread thd = entry.getKey();
            ThreadGroup thdGrp = thd.getThreadGroup();
            if (thdGrp == null) {
                continue;
            }
            //
            String grpName = thdGrp.getName();
            String thdName = thd.getName();
            if ("system".equalsIgnoreCase(grpName)) {
                continue;
            }
//            System.err.println(String.format("\tthd:%s/:%s", grpName, thdName));
            if (thdName.startsWith("DestroyJavaVM") || thdName.startsWith("Common-Cleaner")) {
                continue;
            }
            for (String skipPrefix : skipPrefixs) {
                if (thdName.startsWith(skipPrefix)) {
                    continue MAIN_LOOP;
                }
            }
            StackTraceElement[] stks = entry.getValue();
            ansHM.put(thd, stks);
        }
        return ansHM;
    }

    public static String[] getStackTraceStrArr() {
        StackTraceElement[] stackElmts = Thread.currentThread().getStackTrace();
        String[] stackStrs = new String[]{};
        if (stackElmts.length > 2) {
            stackStrs = new String[stackElmts.length - 2];
            for (int idx = 2; idx < stackElmts.length; idx++) {
                StackTraceElement stackElmt = stackElmts[idx];
                stackStrs[idx - 2] = String.format("%s.%s(%s:%s)", stackElmt.getClassName(), stackElmt.getMethodName(), stackElmt.getFileName(), stackElmt.getLineNumber());
            }
        }
        return stackStrs;
    }

    public static String[] removeHeader(String[] args, int cnt) {
        if (args == null) {
            args = new String[]{};
        }
        if (cnt >= args.length) {
            return new String[]{};
        }
        String[] ans = new String[args.length - cnt];
        System.arraycopy(args, cnt, ans, 0, args.length - cnt);
        return ans;
    }

    public static String execLocalCmd(String cmdStr) throws IOException, InterruptedException {
        return execLocalCmd(cmdStr, null);
    }

    public static String execLocalCmd(String cmdStr, StringBuilder errStrBldr) throws IOException, InterruptedException {
        //
        InputStream stdInStr = null;
        InputStreamReader stdInRdr = null;
        BufferedReader stdBufRdr = null;
        //
        InputStream errInStr = null;
        InputStreamReader errInRdr = null;
        BufferedReader errBufRdr = null;
        //
        StringBuilder stdStrBldr = new StringBuilder();
        try {
            Process proc = Runtime.getRuntime().exec(cmdStr);
            stdInStr = proc.getInputStream();
            stdInRdr = new InputStreamReader(stdInStr);
            stdBufRdr = new BufferedReader(stdInRdr);
            //
            //  ----  get stdout data (start) ----
            String line = stdBufRdr.readLine();
            boolean appendNwLn = false;
            while (line != null) {
                try {
                    stdStrBldr.append(line);
                    stdStrBldr.append("\n");
                    appendNwLn = true;
                } finally {
                    line = stdBufRdr.readLine();
                }
            }
            if (appendNwLn) {
                stdStrBldr.deleteCharAt(stdStrBldr.length() - 1);
            }
            //  ----  get stdout data ( end ) ----
            //
            errInStr = proc.getErrorStream();
            errInRdr = new InputStreamReader(errInStr);
            errBufRdr = new BufferedReader(errInRdr);
            //
            //  ----  get errout data (start) ----
            line = errBufRdr.readLine();
            appendNwLn = false;
            while (line != null) {
                try {
                    if (errStrBldr != null) {
                        errStrBldr.append(line);
                        errStrBldr.append("\n");
                    }
                    appendNwLn = true;
                } finally {
                    line = errBufRdr.readLine();
                }
            }
            if (errStrBldr != null && appendNwLn) {
                errStrBldr.deleteCharAt(errStrBldr.length() - 1);
            }
            //  ----  get errout data ( end) ----
            //
            proc.waitFor();
            //
        } finally {
            AppUtils.close_ignore_err(stdBufRdr);
            AppUtils.close_ignore_err(stdInRdr);
            AppUtils.close_ignore_err(stdInStr);
            //
            AppUtils.close_ignore_err(errBufRdr);
            AppUtils.close_ignore_err(errInRdr);
            AppUtils.close_ignore_err(errInStr);
        }
        return stdStrBldr.toString();
    }

    public synchronized static boolean execOnlyOne(String key, Callable<Boolean> callable) {
        if (callable == null || key == null || EXEC_ONLY_ONE_AL.contains(key)) {
            return false;
        } else {
            try {
                if (callable.call()) {
                    EXEC_ONLY_ONE_AL.add(key);
                }
            } catch (Exception ex) {
            }
            return true;
        }
    }

    public static void execRunSuspendError(Runnable run) {
        if (run == null) {
            return;
        }
        try {
            run.run();
        } catch (Throwable ex) {
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static String execOSCommand(int wait_ms, String... cmdStrs) {
        StringBuilder strBldr = new StringBuilder();
        InputStream in = null;
        InputStreamReader inRdr = null;
        try {
            Process proc = Runtime.getRuntime().exec(cmdStrs);
            proc.waitFor(wait_ms, TimeUnit.MILLISECONDS);
            in = proc.getInputStream();
            inRdr = new InputStreamReader(in);
            char[] chs = new char[1024];
            int rdN = inRdr.read(chs);
            while (rdN > 0) {
                strBldr.append(chs, 0, rdN);
                rdN = inRdr.read(chs);
            }
            return strBldr.toString().trim();
        } catch (Exception ex) {
            return null;
        } finally {
            if (inRdr != null) {
                try {
                    inRdr.close();
                } catch (IOException ex) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    //  ----  Application method area ( end ) ----
}
