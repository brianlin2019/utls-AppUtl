package brnln.utils.ndUtl;

import brnln.utils.appUtils.LmUtl;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtNode extends MyNode {

    public RtNode() {
        super(null, "", "", null);
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public Optional<MyNode> search(String expr, LmUtl.ExRcvr aExRcvr) {
        try {
            Pattern ptrn = Pattern.compile(expr);
            LmUtl.UObjHM uHm = new LmUtl.UObjHM();
            this.visitNd((nd) -> {
                Matcher mtchr = ptrn.matcher(nd.getPath());
                if (mtchr.find()) {
                    String currPtrn = mtchr.group(0);
                    String lastPtrn = uHm.get(String.class, "ptrnStr").orElse(null);
                    if (lastPtrn == null || currPtrn.length() > lastPtrn.length()) {
                        uHm.put("ptrnStr", currPtrn);
                        uHm.put("myNode", nd);
                    }
                }
            }, aExRcvr);
            return uHm.get(MyNode.class, "myNode");
        } catch (Exception ex) {
            if (aExRcvr != null) {
                try {
                    aExRcvr.rcve(ex);
                } catch (Exception ex2) {
                    /* do nothing */
                }
            }
        }
        return null;
    }

    //  ------------------------------------------------------------------------
    @Override
    String getSpcOffset() {
        return "";
    }

    @Override
    boolean isSibling(String chldSpcOffset) {
        //  根節點不為其他節點的同儕
        return false;
    }

    @Override
    boolean is_prntNd_of(String chldSpcOffset) throws IOException {
        if (this.children.isEmpty()) {
            return true;
        } else if (this.children.get(0).isSibling(chldSpcOffset)) {
            return true;
        } else if (this.children.get(0).is_prntNd_of(chldSpcOffset)) {
            return true;
        } else {
            throw new IOException(String.format("Check child node fail!(child:%s, curr:root)", chldSpcOffset));
        }
    }

    @Override
    boolean is_chldNd_of(String prntSpcOffset) {
        return false;
    }

}
