package brnln.utils.ndUtl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NdUtl {

    private static class ProcData {

        MyNode currPrntNd = null;
        MyNode lastCurrNd = null;
        int lnCnt = 0;
    }

    private static final Pattern PTRN_LN_CMMT = Pattern.compile("//.*$");
    private static final Pattern PTRN_CMMT = Pattern.compile("/\\*.*\\*/");
    private static final Pattern PTRN_SP_PRFX = Pattern.compile("^( )+");
    private static final Pattern PTRN_NM_DESC = Pattern.compile("(\\w+)(\\((.+)\\))?");
    //
    private static final Pattern PATH_PTRN = Pattern.compile("/?([\\w\\. ]+)");

    public static RtNode parseRsrc(Class clz, String rPath) throws IOException {
        return parseRsrc(clz, rPath, null);
    }

    public static RtNode parseRsrc(Class clz, String rPath, String charsetName) throws IOException {
        if (clz == null || rPath == null) {
            return null;
        }
        try (InputStream in = clz.getResourceAsStream(rPath)) {
            if (in == null) {
                throw new IOException(String.format("Fail to find resource (rPath:%s, clz:%s)", rPath, clz.getName()));
            }
            try (InputStreamReader inRdr = charsetName == null ? new InputStreamReader(in) : new InputStreamReader(in, charsetName);
                    BufferedReader bufRdr = new BufferedReader(inRdr)) {
                RtNode rtNode = new RtNode();
                ProcData aProcData = new ProcData();
                aProcData.currPrntNd = rtNode;
                aProcData.lastCurrNd = null;
                String ln = bufRdr.readLine();
                while (ln != null) {
                    try {
                        //  ----  載入有效的資料行 (start) ----
                        aProcData.lnCnt++;
                        ln = proc_cmmt(ln);
                        ln = proc_lnCmmt(ln);
                        if (ln == null || ln.trim().length() == 0) {
                            continue;
                        }
                        String spPrfx = get_spPrefix(ln);
                        String name = get_name(ln);
                        String desc = get_desc(ln);
                        //  ----  載入有效的資料行 ( end ) ----
                        //
                        NdUtl._proc_currNode_subNode(aProcData, spPrfx, name, desc);
                    } finally {
                        ln = bufRdr.readLine();
                    }
                }
                return rtNode;
            }
        }
    }

    public static ArrayList<String> parsePath(String path) {
        ArrayList<String> ansAL = new ArrayList<>();
        Matcher mtchr = PATH_PTRN.matcher(path);
        while (mtchr.find()) {
            ansAL.add(mtchr.group(1));
        }
        return ansAL;
    }

    private static void _proc_currNode_subNode(ProcData aProcData, String spPrfx, String name, String desc) throws IOException {
        if (aProcData.currPrntNd.is_prntNd_of(spPrfx) && aProcData.lastCurrNd == null) /* 節點為當下的子節點 */ {
            //  currPrnt/
            //         /currNd
        } else if (aProcData.currPrntNd.is_prntNd_of(spPrfx) && aProcData.lastCurrNd != null && aProcData.lastCurrNd.isSibling(spPrfx)) /* 節點為當下的子節點 */ {
            //  currPrnt/siblingNd
            //         /currNd
        } else if (aProcData.currPrntNd.is_prntNd_of(spPrfx) && aProcData.lastCurrNd != null && !aProcData.lastCurrNd.isSibling(spPrfx) && aProcData.lastCurrNd.is_prntNd_of(spPrfx)) /* 節點為當下的次子節點 */ {
            //  currPrnt/siblingNd/
            //                  /currNd
            aProcData.currPrntNd = aProcData.lastCurrNd;
        } else if (aProcData.currPrntNd.isSibling(spPrfx))/* 節點為父節點的 同儕 */ {
            //  currPrnt/siblingNd/
            //  currNd
            aProcData.lastCurrNd = aProcData.currPrntNd;
            aProcData.currPrntNd = aProcData.lastCurrNd.getPrntNd();
        } else if (aProcData.currPrntNd.is_chldNd_of(spPrfx))/* 節點為父節點的 上層 */ {
            //  /.../......../currPrnt/
            //     /currNd
            aProcData.lastCurrNd = aProcData.currPrntNd.get_prntNd_by(spPrfx);
            aProcData.currPrntNd = aProcData.lastCurrNd.getPrntNd();
            //
        } else {
            throw new IOException(String.format("Fail parse node! (line:%s, name:%s, desc:%s, spc:%s)", aProcData.lnCnt, name, desc, spPrfx));
        }
        String pPathDiff = aProcData.currPrntNd.getPathOff(spPrfx);
        MyNode aMyNode = new MyNode(aProcData.currPrntNd, pPathDiff, name, desc);
        aProcData.currPrntNd.addChild(aMyNode);
        aProcData.lastCurrNd = aMyNode;
    }

    //  ------------------------------------------------------------------------
    private static String proc_lnCmmt(String ln) {
        Matcher mtchr = PTRN_LN_CMMT.matcher(ln);
        if (mtchr.find()) {
            ln = mtchr.replaceAll("");
        }
        return ln;
    }

    private static String proc_cmmt(String ln) {
        Matcher mtchr = PTRN_CMMT.matcher(ln);
        if (mtchr.find()) {
            ln = mtchr.replaceAll("");
        }
        return ln;
    }

    private static String get_spPrefix(String ln) {
        Matcher mtchr = PTRN_SP_PRFX.matcher(ln);
        if (mtchr.find()) {
            return mtchr.group(0);
        }
        return "";
    }

    private static String get_name(String ln) {
        ln = ln.trim();
        Matcher mtchr = PTRN_NM_DESC.matcher(ln);
        if (mtchr.find() && mtchr.groupCount() >= 1) {
            return mtchr.group(1);
        }
        return "";
    }

    private static String get_desc(String ln) {
        ln = ln.trim();
        Matcher mtchr = PTRN_NM_DESC.matcher(ln);
        if (mtchr.find() && mtchr.groupCount() >= 3) {
            return mtchr.group(3);
        }
        return "";
    }

    //  ------------------------------------------------------------------------
}
