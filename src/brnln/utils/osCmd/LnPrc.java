package brnln.utils.osCmd;

import brnln.utils.osCmd.OsCmd.RsltRdr;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LnPrc implements RsltRdr {

    public static interface FltLn {

        public String flt(String ln) throws IOException;
    }

    private static class RegFltLn implements FltLn {

        private final boolean include;
        private final boolean andOp;
        private final ArrayList<Pattern> aPatternAL = new ArrayList<>();

        public RegFltLn(boolean include, boolean andOp, String[] pttrns) throws IOException {
            this.include = include;
            this.andOp = andOp;
            for (String pttrn : pttrns) {
                try {
                    aPatternAL.add(Pattern.compile(pttrn, Pattern.CASE_INSENSITIVE));
                } catch (PatternSyntaxException ex) {
                    throw new IOException(ex);
                }
            }
        }

        public RegFltLn(boolean include, boolean andOp, Pattern[] pttrns) {
            this.include = include;
            this.andOp = andOp;
            for (Pattern pttrn : pttrns) {
                aPatternAL.add(pttrn);
            }
        }

        @Override
        public String flt(String ln) throws IOException {
            if (mtchLn(ln)) {
                return ln;
            } else {
                return null;
            }
        }

        private boolean mtchLn(String ln) {
            int mtchCnt = 0;
            for (Pattern ptrn : aPatternAL) {
                boolean mtch = ptrn.matcher(ln).find();
                mtchCnt += mtch ? 1 : 0;
                if (include && andOp && !mtch) /* 在 '包含' 且為 '與' 運算下，有一個不符合，整行文字放棄 */ {
                    return false /* 此行不再處理 */;
                } else if (include && !andOp && mtch) /* 在 '包含' 且為 '或' 運算下，有一個符合，整行文字納入 */ {
                    return true /* 整行文字納入 */;
                } else if (!include && !andOp && mtch) /* 在 '不包含' 且為 '或' 運算下，有一個符合，整行文字放棄 */ {
                    return false /* 此行不再處理 */;
                }
            }
            if (include && andOp && mtchCnt == aPatternAL.size()) /* 在 '包含' 且為 '與' 運算下，全部符合，整行文字納入 */ {
                return true /* 整行文字納入 */;
            } else if (!include && andOp && mtchCnt < aPatternAL.size()) /* 在 '不包含' 且為 '與' 運算下，沒有全部符合，整行文字納入 */ {
                return true /* 整行文字納入 */;
            } else if (!include && !andOp && mtchCnt == 0) /* 在 '不包含' 且為 '或' 運算下，全部不符合，整行文字納入 */ {
                return true /* 整行文字納入 */;
            }
            return false /* 此行不再處理 */;
        }

    }

    private static class PrcElm {

        final PrcElm prev;
        final FltLn aFltLn;
        PrcElm next;

        public PrcElm(PrcElm prev, FltLn aFltLn) {
            this.prev = prev;
            this.aFltLn = aFltLn;
        }

        void prcLn(String ln) throws IOException {
            if (aFltLn != null) {
                String nxtLn = aFltLn.flt(ln);
                if (nxtLn != null && next != null) {
                    next.prcLn(ln);
                }
            }
        }

    }

    private PrcElm frstPrcElm;
    private PrcElm lastPrcElm;
    private final Object lck = new Object();

    @Override
    public void rdLine(String ln) throws IOException {
        synchronized (lck) {
            if (frstPrcElm != null && frstPrcElm.aFltLn != null) {
                frstPrcElm.prcLn(ln);
            }
        }
    }

    public LnPrc map(FltLn aFltLn) throws IOException {
        synchronized (lck) {
            apndPrcElm(new PrcElm(lastPrcElm, (ln) -> aFltLn.flt(ln)));
        }
        return this;
    }

    public LnPrc rd(RsltRdr aRsltRdr) {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, (ln) -> {
                aRsltRdr.rdLine(ln);
                return ln;
            });
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc inPtrn(String ptrn) throws IOException {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(true, true, new String[]{ptrn}));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc inPtrn(Pattern ptrn) {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(true, true, new Pattern[]{ptrn}));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc noPtrn(String ptrn) throws IOException {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(false, true, new String[]{ptrn}));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc noPtrn(Pattern ptrn) {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(false, true, new Pattern[]{ptrn}));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc inPtrnAnd(String... ptrns) throws IOException {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(true, true, ptrns));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc inPtrnAnd(Pattern... ptrns) {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(true, true, ptrns));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc inPtrnOr(String... ptrns) throws IOException {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(true, false, ptrns));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc inPtrnOr(Pattern... ptrns) {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(true, false, ptrns));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc noPtrnOr(String... ptrns) throws IOException {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(false, false, ptrns));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    public LnPrc noPtrnOr(Pattern... ptrns) {
        synchronized (lck) {
            PrcElm aPrcElm = new PrcElm(lastPrcElm, new RegFltLn(false, false, ptrns));
            apndPrcElm(aPrcElm);
        }
        return this;
    }

    //  ------------------------------------------------------------------------
    private void apndPrcElm(PrcElm aPrcElm) {
        if (frstPrcElm == null && lastPrcElm == null) {
            frstPrcElm = aPrcElm;
            lastPrcElm = aPrcElm;
        } else if (frstPrcElm != null && lastPrcElm != null) {
            lastPrcElm.next = aPrcElm;
            lastPrcElm = aPrcElm;
        }
    }
}
