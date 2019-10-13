package brnln.utils.ndUtl;

import brnln.utils.appUtils.LmUtl;
import brnln.utils.appUtils.LmUtl.ExRcvr;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class MyNode {

    @FunctionalInterface
    public static interface NdVstr {

        public void visit(MyNode nd) throws Exception;
    }

    private MyNode parent;
    protected final String name;
    protected final String desc;
    protected final ArrayList<MyNode> children = new ArrayList<>();
    protected String pPathDiff = "";
    private final ArrayList<Object> entityAL = new ArrayList<>();

    public MyNode(MyNode parent, String pPathDiff, String name, String desc) {
        this.parent = parent;
        this.pPathDiff = pPathDiff;
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getPath() {
        String ansStr = "";
        if (this.parent != null) {
            ansStr += this.parent.getPath();
        }
        return ansStr += this.name.startsWith("/") ? this.name : "/" + this.name;
    }

    @Override
    public String toString() {
        return String.format("%s(%s,path:%s)", name, desc, this.getPath());
    }

    public RtNode getRootNd() throws IOException {
        if (this.parent != null) {
            return this.parent.getRootNd();
        } else if (this instanceof RtNode) {
            return (RtNode) this;
        } else {
            throw new IOException("Fail to find the RootNode");
        }
    }

    public MyNode getParent() {
        return this.parent;
    }

    public ArrayList<MyNode> getChildren() {
        return new ArrayList<>(this.children);
    }

    public MyNode pushEntry(Object entry) {
        if (entry == null) {
            return this;
        }
        entityAL.add(entry);
        return this;
    }

    public <T> ArrayList<T> getEntryAL(Class<T> usrClz) {
        ArrayList<T> ansAL = new ArrayList<>();
        entityAL.stream().filter((usrObj) -> usrObj != null && usrClz.isAssignableFrom(usrObj.getClass())).forEach((obj) -> ansAL.add((T) obj));
        return ansAL;
    }

    //  -----------------------------------------------------------------------
    public void visitNd(NdVstr aNdVstr, ExRcvr aExRcvr) {
        children.stream().forEach((nd) -> {
            LmUtl.run(() -> {
                aNdVstr.visit(nd);
                nd.visitNd(aNdVstr, aExRcvr);
            }, aExRcvr);
        });
    }

    public Optional<MyNode> search(String expr, ExRcvr aExRcvr) {
        return LmUtl.get(() -> this.getRootNd() == null ? null : this.getRootNd().search(expr, aExRcvr).orElse(null), aExRcvr);
    }

    public MyNode byPath(String ndPath) throws IOException {
        if (ndPath == null) {
            throw new IOException(String.format("ndPath can not be null (nd:%s)", this.getPath()));
        } else if (".".equals(ndPath)) {
            return this;
        } else if ("..".equals(ndPath)) {
            return this.parent != null ? this.parent : this.getRootNd();
        } else {
            return children.stream().filter((nd) -> ndPath.equals(nd.name)).findFirst().orElseThrow(() -> new IOException(String.format("fail to find child %s (nd:%s)", ndPath, this.getPath())));
        }
    }

    //  -----------------------------------------------------------------------
    void addChild(MyNode myNode) {
        children.add(myNode);
    }

    String getSpcOffset() {
        String ansStr = "";
        if (this.parent != null) {
            ansStr += this.parent.getSpcOffset();
        }
        return ansStr += this.pPathDiff;
    }

    String getPathOff(String chldSpcOffset) throws IOException {
        if (!is_prntNd_of(chldSpcOffset)) {
            throw new IOException(String.format("Check child node fail!(child:%s, curr:%s)", chldSpcOffset, this.getSpcOffset()));
        }
        String currSpcOffset = this.getSpcOffset();
        return chldSpcOffset.substring(currSpcOffset.length());
    }

    boolean is_prntNd_of(String chldSpcOffset) throws IOException {
        if (chldSpcOffset == null) {
            return false;
        }
        String selfSpcOffset = this.getSpcOffset();
        return chldSpcOffset.startsWith(selfSpcOffset) && !chldSpcOffset.equals(selfSpcOffset);
    }

    boolean isSibling(String chldSpcOffset) {
        if (chldSpcOffset == null) {
            return false;
        }
        String selfSpcOffset = this.getSpcOffset();
        return chldSpcOffset.equals(selfSpcOffset);
    }

    boolean is_chldNd_of(String prntSpcOffset) {
        if (parent != null && parent.isSibling(prntSpcOffset)) {
            return true;
        } else {
            return parent.is_chldNd_of(prntSpcOffset);
        }
    }

    MyNode get_prntNd_by(String prntSpcOffset) {
        if (parent != null && parent.isSibling(prntSpcOffset)) {
            return parent;
        } else {
            return parent.get_prntNd_by(prntSpcOffset);
        }
    }

    MyNode getPrntNd() {
        return this.parent;
    }

}
