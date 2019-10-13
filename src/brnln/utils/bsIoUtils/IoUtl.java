package brnln.utils.bsIoUtils;

import java.nio.charset.Charset;

public class IoUtl {

    public static final byte[] BIT_MASKS = new byte[8];

    static {
        BIT_MASKS[0] = (byte) 0x80;
        BIT_MASKS[1] = (byte) 0x40;
        BIT_MASKS[2] = (byte) 0x20;
        BIT_MASKS[3] = (byte) 0x10;
        BIT_MASKS[4] = (byte) 0x08;
        BIT_MASKS[5] = (byte) 0x04;
        BIT_MASKS[6] = (byte) 0x02;
        BIT_MASKS[7] = (byte) 0x01;
    }

    public static class BPos {

        public int pos = 0;

        public BPos(int pos) {
            this.pos = pos;
        }

        @Override
        public String toString() {
            return String.format("BPos(%s)", pos);
        }
    }

    private static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    //  ----  data write to bytes method (start) ----
    public static int writeLong(byte[] bs, int pos, long val) {
        bs[pos + 0] = (byte) ((val >>> 56) & 0xFF);
        bs[pos + 1] = (byte) ((val >>> 48) & 0xFF);
        bs[pos + 2] = (byte) ((val >>> 40) & 0xFF);
        bs[pos + 3] = (byte) ((val >>> 32) & 0xFF);
        bs[pos + 4] = (byte) ((val >>> 24) & 0xFF);
        bs[pos + 5] = (byte) ((val >>> 16) & 0xFF);
        bs[pos + 6] = (byte) ((val >>> 8) & 0xFF);
        bs[pos + 7] = (byte) ((val) & 0xFF);
        return pos + 8;
    }

    public static byte[] convToBs(long val) {
        byte[] bs = new byte[8];
        writeLong(bs, 0, val);
        return bs;
    }

    public static int writeInt(byte[] bs, int pos, int val) {
        bs[pos + 0] = (byte) ((val >>> 24) & 0xFF);
        bs[pos + 1] = (byte) ((val >>> 16) & 0xFF);
        bs[pos + 2] = (byte) ((val >>> 8) & 0xFF);
        bs[pos + 3] = (byte) ((val) & 0xFF);
        return pos + 4;
    }

    public static byte[] convToBs(int val) {
        byte[] bs = new byte[4];
        writeInt(bs, 0, val);
        return bs;
    }

    public static int writeShort(byte[] bs, int pos, short val) {
        bs[pos + 0] = (byte) ((val >>> 8) & 0xFF);
        bs[pos + 1] = (byte) ((val) & 0xFF);
        return pos + 2;
    }

    public static byte[] convToBs(short val) {
        byte[] bs = new byte[2];
        writeShort(bs, 0, val);
        return bs;
    }

    public static int writeChar(byte[] bs, int pos, char val) {
        bs[pos + 0] = (byte) ((val >>> 8) & 0xFF);
        bs[pos + 1] = (byte) ((val) & 0xFF);
        return pos + 2;
    }

    public static byte[] convToBs(char val) {
        byte[] bs = new byte[2];
        writeChar(bs, 0, val);
        return bs;
    }

    public static int writeByte(byte[] bs, int pos, byte val) {
        bs[pos] = (byte) ((val) & 0xFF);
        return pos + 1;
    }

    public static int writeBytes(byte[] bs, int pos, byte[] val) {
//        pos = writeShort(bs, pos, (short) val.length);
        pos = writeInt(bs, pos, val.length);
        if (val.length > 0) {
            System.arraycopy(val, 0, bs, pos, val.length);
        }
        return pos + val.length;
    }

    public static int writeBytes(byte[] dstBs, int dstPos, byte[] frmBs, int frmPos, int len) {
        if (len < 0) {
            len = 0;
        }
        int wrtLen = Math.min(frmBs.length - frmPos, len);
//        dstPos = writeShort(bs, dstPos, (short) wrtLen);
        dstPos = writeInt(dstBs, dstPos, (short) wrtLen);
        if (wrtLen > 0) {
            System.arraycopy(frmBs, frmPos, dstBs, dstPos, wrtLen);
        } else {
            wrtLen = 0;
        }
        return dstPos + wrtLen;
    }

    public static int writeFloat(byte[] bs, int pos, float val) {
        return writeInt(bs, pos, Float.floatToIntBits(val));
    }

    public static byte[] convToBs(float val) {
        byte[] bs = new byte[4];
        writeFloat(bs, 0, val);
        return bs;
    }

    public static int writeDouble(byte[] bs, int pos, double val) {
        return writeLong(bs, pos, Double.doubleToLongBits(val));
    }

    public static byte[] convToBs(double val) {
        byte[] bs = new byte[8];
        writeDouble(bs, 0, val);
        return bs;
    }

    public static int writeString(byte[] bs, int pos, String val) {
        if (val == null) {
            val = "";
        }
        byte[] strBs = val.getBytes(UTF_16BE);
        return writeBytes(bs, pos, strBs);
    }

    public static byte[] convToBs(String val) {
        if (val != null) {
            return val.getBytes(UTF_16BE);
        } else {
            return new byte[0];
        }
    }

    //  ----  data write to bytes method ( end ) ----
    //
    //  ----  data read from bytes method ( end ) ----
    public static long readLong(byte[] bs, int p) {
        return 0
                + ((long) (bs[p + 0] & 0xFF) << 56)
                + ((long) (bs[p + 1] & 0xFF) << 48)
                + ((long) (bs[p + 2] & 0xFF) << 40)
                + ((long) (bs[p + 3] & 0xFF) << 32)
                + ((long) (bs[p + 4] & 0xFF) << 24)
                + ((bs[p + 5] & 0xFF) << 16)
                + ((bs[p + 6] & 0xFF) << 8)
                + ((bs[p + 7] & 0xFF));
    }

    public static long readLong(byte[] bs, BPos pos) {
        int p = pos.pos;
        pos.pos += 8;
        return 0
                + ((long) (bs[p + 0] & 0xFF) << 56)
                + ((long) (bs[p + 1] & 0xFF) << 48)
                + ((long) (bs[p + 2] & 0xFF) << 40)
                + ((long) (bs[p + 3] & 0xFF) << 32)
                + ((long) (bs[p + 4] & 0xFF) << 24)
                + ((bs[p + 5] & 0xFF) << 16)
                + ((bs[p + 6] & 0xFF) << 8)
                + ((bs[p + 7] & 0xFF));
    }

    public static long convToLong(byte[] val) {
        if (val != null && val.length >= 8) {
            return readLong(val, 0);
        }
        return Long.MIN_VALUE;
    }

    public static int readInt(byte[] bs, int p) {
        return 0
                + ((bs[p + 0] & 0xFF) << 24)
                + ((bs[p + 1] & 0xFF) << 16)
                + ((bs[p + 2] & 0xFF) << 8)
                + ((bs[p + 3] & 0xFF));
    }

    public static int readInt(byte[] bs, BPos pos) {
        int p = pos.pos;
        pos.pos += 4;
        return 0
                + ((bs[p + 0] & 0xFF) << 24)
                + ((bs[p + 1] & 0xFF) << 16)
                + ((bs[p + 2] & 0xFF) << 8)
                + ((bs[p + 3] & 0xFF));
    }

    public static int convToInt(byte[] val) {
        if (val != null && val.length >= 4) {
            return readInt(val, 0);
        }
        return Integer.MIN_VALUE;
    }

    public static short readShort(byte[] bs, int p) {
        if (bs.length >= p + 2) {
            return (short) (0
                    + ((bs[p + 0] & 0xFF) << 8)
                    + ((bs[p + 1] & 0xFF)));
        } else {
            return Short.MIN_VALUE;
        }
    }

    public static short readShort(byte[] bs, BPos pos) {
        int p = pos.pos;
        pos.pos += 2;
        return (short) (0
                + ((bs[p + 0] & 0xFF) << 8)
                + ((bs[p + 1] & 0xFF)));
    }

    public static short convToShort(byte[] val) {
        if (val != null && val.length >= 2) {
            return readShort(val, 0);
        }
        return Short.MIN_VALUE;
    }

    public static char readChar(byte[] bs, int p) {
        if (bs.length >= p + 2) {
            return (char) (0
                    + ((bs[p + 0] & 0xFF) << 8)
                    + ((bs[p + 1] & 0xFF)));
        } else {
            return Character.MIN_VALUE;
        }
    }

    public static char readChar(byte[] bs, BPos pos) {
        int p = pos.pos;
        pos.pos += 2;
        return (char) (0
                + ((bs[p + 0] & 0xFF) << 8)
                + ((bs[p + 1] & 0xFF)));
    }

    public static char convToChar(byte[] val) {
        if (val != null && val.length >= 2) {
            return readChar(val, 0);
        }
        return Character.MIN_VALUE;
    }

    public static byte readByte(byte[] bs, int p) {
        return (byte) (bs[p + 0] & 0xFF);
    }

    public static byte readByte(byte[] bs, BPos pos) {
        int p = pos.pos;
        pos.pos += 1;
        return (byte) (bs[p + 0] & 0xFF);
    }

    public static byte[] readBytes(byte[] bs, int p) {
//        short bsLen = readShort(bs, p);
        int bsLen = readInt(bs, p);
        p = p + 4;
        byte[] dstBs = new byte[bsLen];
        if (bsLen > 0) {
            System.arraycopy(bs, p, dstBs, 0, bsLen);
        }
        return dstBs;
    }

    public static byte[] readBytes(byte[] bs, BPos pos) {
//        short bsLen = readShort(bs, pos);
        int bsLen = readInt(bs, pos);
        byte[] dstBs = new byte[bsLen];
        if (bsLen > 0) {
            System.arraycopy(bs, pos.pos, dstBs, 0, bsLen);
            pos.pos += bsLen;
        }
        return dstBs;
    }

    public static float readFloat(byte[] bs, int p) {
        return Float.intBitsToFloat(readInt(bs, p));
    }

    public static float readFloat(byte[] bs, BPos pos) {
        return Float.intBitsToFloat(readInt(bs, pos));
    }

    public static float convToFloat(byte[] val) {
        if (val != null && val.length >= 4) {
            return readFloat(val, 0);
        } else {
            return Float.MAX_VALUE;
        }
    }

    public static double readDouble(byte[] bs, int p) {
        return Double.longBitsToDouble(readLong(bs, p));
    }

    public static double readDouble(byte[] bs, BPos pos) {
        return Double.longBitsToDouble(readLong(bs, pos));
    }

    public static double convToDouble(byte[] val) {
        if (val != null && val.length >= 4) {
            return readDouble(val, 0);
        } else {
            return Float.MAX_VALUE;
        }
    }

    public static String readString(byte[] bs, BPos pos) {
        byte[] strBs = readBytes(bs, pos);
        return new String(strBs, UTF_16BE);
    }

    public static String convToStr(byte[] bs) {
        if (bs != null) {
            return new String(bs, UTF_16BE);
        } else {
            return "";
        }
    }

    //  ----  data read from bytes method ( end ) ----
    //
    public static String getIPV4String(byte[] ipBs) {
        if (ipBs == null || ipBs.length != 4) {
            return null;
        }
        String ipStr = "";
        for (int idx = 0; idx < 4; idx++) {
            if (idx != 0) {
                ipStr += ".";
            }
            int tmpInt = ipBs[idx];
            if (tmpInt < 0) {
                tmpInt += 256;
            }
            ipStr += tmpInt;

        }

        return ipStr;
    }

    public static boolean getBitFlag(byte flagByte, int bitIdx) {
        bitIdx = bitIdx % 8;
        return (flagByte & IoUtl.BIT_MASKS[bitIdx]) != 0;
    }

    public static byte setBitFlag(byte flagByte, int bitIdx, boolean setIt) {
        boolean bitOn = (flagByte & IoUtl.BIT_MASKS[bitIdx]) != 0;
        if (bitOn != setIt) {
            flagByte = (byte) (flagByte ^ IoUtl.BIT_MASKS[bitIdx]);
        }
        return flagByte;
    }
}
