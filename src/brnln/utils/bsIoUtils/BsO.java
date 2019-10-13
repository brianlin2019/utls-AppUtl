package brnln.utils.bsIoUtils;

import brnln.utils.bsIoUtils.IoUtl.BPos;
import java.io.IOException;

public class BsO {

    private final boolean writeMode;
    private byte[] bs;
    private int pos = 0;
    private final BPos B_POS = new BPos(0);
    //
    private boolean fixMode = false;

    public BsO() {
        writeMode = true;
        this.bs = new byte[1024];
    }

    public BsO(byte[] bs) {
        writeMode = false;
        this.bs = bs;
    }

    public synchronized BsO wLong(long val) throws IOException {
        chkBsLen_for_write(8);
        this.pos = IoUtl.writeLong(bs, pos, val);
        return this;
    }

    public synchronized BsO wInt(int val) throws IOException {
        chkBsLen_for_write(4);
        this.pos = IoUtl.writeInt(bs, pos, val);
        return this;
    }

    public synchronized BsO wShort(short val) throws IOException {
        chkBsLen_for_write(2);
        this.pos = IoUtl.writeShort(bs, pos, val);
        return this;
    }

    public synchronized BsO wChar(char val) throws IOException {
        chkBsLen_for_write(2);
        this.pos = IoUtl.writeChar(bs, pos, val);
        return this;
    }

    public synchronized BsO wByte(byte val) throws IOException {
        chkBsLen_for_write(1);
        this.pos = IoUtl.writeByte(bs, pos, val);
        return this;
    }

    public synchronized BsO wBytes(byte[] val) throws IOException {
        if (val == null) {
            val = new byte[0];
        }
        chkBsLen_for_write(val.length + 4);
        this.pos = IoUtl.writeBytes(bs, pos, val);
        return this;
    }

    public synchronized BsO wFloat(float val) throws IOException {
        chkBsLen_for_write(4);
        this.pos = IoUtl.writeFloat(bs, pos, val);
        return this;
    }

    public synchronized BsO wDouble(double val) throws IOException {
        chkBsLen_for_write(8);
        this.pos = IoUtl.writeDouble(bs, pos, val);
        return this;
    }

    public synchronized BsO wStr(String val) throws IOException {
        if (val == null) {
            val = "";
        }
        chkBsLen_for_write(val.length() * 2 + 4);
        this.pos = IoUtl.writeString(bs, pos, val);
        return this;
    }

    //  ------------------------------------------------------------------------
    public synchronized byte[] getBytes() {
        fixMode = true;
        byte[] dstBs = new byte[pos];
        System.arraycopy(bs, 0, dstBs, 0, pos);
        return dstBs;
    }

    //  ------------------------------------------------------------------------
    public synchronized long rLong() throws IOException {
        chkBsLen_for_read(8);
        return IoUtl.readLong(bs, B_POS);
    }

    public synchronized int rInt() throws IOException {
        chkBsLen_for_read(4);
        return IoUtl.readInt(bs, B_POS);
    }

    public synchronized short rShort() throws IOException {
        chkBsLen_for_read(2);
        return IoUtl.readShort(bs, B_POS);
    }

    public synchronized char rChar() throws IOException {
        chkBsLen_for_read(2);
        return IoUtl.readChar(bs, B_POS);
    }

    public synchronized byte rByte() throws IOException {
        chkBsLen_for_read(1);
        return IoUtl.readByte(bs, B_POS);
    }

    public synchronized byte[] rBytes() throws IOException {
        try {
            return IoUtl.readBytes(bs, B_POS);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public synchronized float rFloat() throws IOException {
        chkBsLen_for_read(4);
        return IoUtl.readFloat(bs, B_POS);
    }

    public synchronized double rDouble() throws IOException {
        chkBsLen_for_read(8);
        return IoUtl.readDouble(bs, B_POS);
    }

    public synchronized String rStr() throws IOException {
        try {
            return IoUtl.readString(bs, B_POS);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    //  ------------------------------------------------------------------------
    private void chkBsLen_for_write(int reqLen) throws IOException {
        if (!writeMode) {
            throw new IOException("This BsO is not write mode!");
        }
        if (fixMode) {
            throw new IOException("This BsO is fixed!");
        }
        while (this.pos + reqLen > this.bs.length) {
            int maxExtLen = 1024 * 10;
            int nwLen = bs.length + (bs.length < maxExtLen ? bs.length : maxExtLen);
            byte[] nwBs = new byte[nwLen];
            System.arraycopy(bs, 0, nwBs, 0, this.pos);
            this.bs = nwBs;
        }
    }

    private void chkBsLen_for_read(int reqLen) throws IOException {
        if (B_POS.pos + reqLen > this.bs.length) {
            throw new IOException("This BsO has no space for read!");
        }

    }
}
