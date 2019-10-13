package brnln.utils.appUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class AppUtilsV2 {

    @FunctionalInterface
    public static interface AP_Lgr {

        public static enum Level {
            TRACE, DEBUG, INFO, WARN
        }

        public void msg(Level level, String msg, Throwable t);
    }

    @FunctionalInterface
    public static interface FilePrc {

        public void fileOpnCls(boolean open, File trgtFile, int lineNum) throws IOException;
    }

    @FunctionalInterface
    public static interface LinePrc {

        public boolean prcLine(int lnNum, String line) throws IOException;
    }

    private static final Pattern ARG_LN_PTRN = Pattern.compile("\\s*((\"[^\"]+\")|([^\\s]+))");

    public static String[] parseArgsLine(String argStr) {
        ArrayList<String> ansAL = new ArrayList<>();
        if (argStr == null) {
            argStr = "";
        }
        Matcher mtchr = ARG_LN_PTRN.matcher(argStr);
        while (mtchr.find()) {
            String g2 = mtchr.group(2);
            String g3 = mtchr.group(3);
            ansAL.add(g2 != null ? g2.substring(1, g2.length() - 1) : g3);
        }
        return ansAL.toArray(new String[ansAL.size()]);
    }

    //  -----------------------------------------------------------------------
    public static byte[] loadFileBytes(File f) throws IOException {
        ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        FileInputStream fIn = new FileInputStream(f);
        byte[] bs = new byte[1024];
        int rdN = fIn.read(bs);
        while (rdN > 0) {
            try {
                baOut.write(bs, 0, rdN);
            } finally {
                rdN = fIn.read(bs);
            }
        }
        return baOut.toByteArray();
    }

    //  -----------------------------------------------------------------------
    public static void procDir(File dir, Predicate<File> aDirPrc, FilePrc aFilePrc, LinePrc aLinePrc, AP_Lgr aAP_Lgr) {
        if (dir == null || dir.isFile()) {
            return;
        }
        //  取出該目錄下, 可接受的檔案或目錄
        File[] files = dir.listFiles((FileFilter) (File file) -> aDirPrc == null ? true : aDirPrc.test(file));
        Arrays.sort(files);
        //
        Arrays.asList(files).forEach((file) -> {
            if (file.isDirectory()) {
                procDir(file, aDirPrc, aFilePrc, aLinePrc, aAP_Lgr);
            } else {
                procFile(file, aFilePrc, aLinePrc, aAP_Lgr);
            }
        });

    }

    public static void procFile(File file, FilePrc aFilePrc, LinePrc aLinePrc, AP_Lgr aAP_Lgr) {
        AppUtilsV2.procFile(file, aFilePrc, aLinePrc, null, aAP_Lgr);
    }

    public static void procFile(File file, FilePrc aFilePrc, LinePrc aLinePrc, String charsetName, AP_Lgr aAP_Lgr) {
        if (file == null || file.isDirectory()) {
            return;
        }
        //  ----  proc before open file (start) ----
        try {
            if (aFilePrc != null) {
                aFilePrc.fileOpnCls(true, file, 0);
            }
        } catch (Throwable ex) {
            if (aAP_Lgr != null) {
                aAP_Lgr.msg(AP_Lgr.Level.WARN, String.format("FAIL OPEN FILE (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        }
        //  ----  proc before open file ( end ) ----
        //
        boolean isGzip = isGzipFile(file, aAP_Lgr);
        //
        FileInputStream fIn = null;
        GZIPInputStream gzipIn = null;
        InputStreamReader inRdr = null;
        BufferedReader bRdr = null;
        int lineNum = 1;
        try {
            //  ----  完成 bRdr 建置 (start) ----
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
            //  ----  完成 bRdr 建置 ( end ) ----
            //
            String lineData = bRdr.readLine();
            while (lineData != null) {
                try {
                    if (aLinePrc != null) {
                        boolean nextLine = aLinePrc.prcLine(lineNum, lineData);
                        if (!nextLine) {
                            break;
                        }
                    }
                } catch (Throwable ex) {
                    if (aAP_Lgr != null) {
                        aAP_Lgr.msg(AP_Lgr.Level.WARN, String.format("FAIL PROC LINE (file:%s, line:%s, rsn:%s)", file.getName(), lineNum, ex.getMessage()), ex);
                    } else {
                        ex.printStackTrace(System.err);
                    }
                } finally {
                    lineData = bRdr.readLine();
                    lineNum++;
                }
            }
        } catch (IOException ex) {
            if (aAP_Lgr != null) {
                aAP_Lgr.msg(AP_Lgr.Level.WARN, String.format("FAIL PROC FILE (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        } finally {
            close_ignore_err(bRdr);
            close_ignore_err(inRdr);
            close_ignore_err(gzipIn);
            close_ignore_err(fIn);
        }
        //  
        //  ----  proc after close file (start) ----
        try {
            if (aFilePrc != null) {
                aFilePrc.fileOpnCls(false, file, lineNum - 1);
            }
        } catch (Throwable ex) {
            if (aAP_Lgr != null) {
                aAP_Lgr.msg(AP_Lgr.Level.WARN, String.format("FAIL CLSE FILE (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        }
        //  ----  proc after close file ( end ) ----
    }

    public static boolean isGzipFile(File file, AP_Lgr aAP_Lgr) {
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
            if (aAP_Lgr != null) {
                aAP_Lgr.msg(AP_Lgr.Level.WARN, String.format("FAIL CHK_ GZIP (file:%s, rsn:%s)", file.getName(), ex.getMessage()), ex);
            } else {
                ex.printStackTrace(System.err);
            }
        } finally {
            close_ignore_err(fileIn);
        }
        //  ----  check is gzip file ( end ) ----
        //
        return isGzip;
    }

    //  Private
    private static void close_ignore_err(FileInputStream fIn) {
        if (fIn != null) {
            try {
                fIn.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void close_ignore_err(GZIPInputStream gzipIn) {
        if (gzipIn != null) {
            try {
                gzipIn.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void close_ignore_err(InputStreamReader inRdr) {
        if (inRdr != null) {
            try {
                inRdr.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void close_ignore_err(BufferedReader bRdr) {
        if (bRdr != null) {
            try {
                bRdr.close();
            } catch (IOException ex) {
            }
        }
    }
}
