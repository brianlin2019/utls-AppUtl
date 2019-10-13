package brnln.utils.bsIoUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AppArgs {

    final static int CLZ_ID = AppArgs.class.getName().hashCode();
    final static int VER_ID = 0x0001;

    private final Pattern PTRN = Pattern.compile("^-+");
    public final ArrayList<String> DFLT_ARG_AL = new ArrayList<>();
    public final HashMap<String, ArrayList<String>> SUB_CMD_ARG_HM = new HashMap<>();
    //  暫存目前所屬的 subCmd
    private transient String subCmd = null;

    public void resetValueFrom(AppArgs aMyArgs) {
        DFLT_ARG_AL.clear();
        DFLT_ARG_AL.addAll(aMyArgs.DFLT_ARG_AL);
        SUB_CMD_ARG_HM.clear();
        SUB_CMD_ARG_HM.putAll(aMyArgs.SUB_CMD_ARG_HM);
    }

    public void push(String arg) {
        Matcher mtchr = PTRN.matcher(arg);
        if (mtchr.find()) {
            subCmd = arg.substring(mtchr.group().length());
            if (!SUB_CMD_ARG_HM.containsKey(subCmd)) {
                SUB_CMD_ARG_HM.put(subCmd, new ArrayList<>());
            }
        } else if (subCmd == null) {
            DFLT_ARG_AL.add(arg);
        } else {
            SUB_CMD_ARG_HM.get(subCmd).add(arg);
        }
    }

    public String[] toArrs() {
        ArrayList<String> argAL = new ArrayList<>();
        argAL.addAll(DFLT_ARG_AL);
        for (Entry<String, ArrayList<String>> entry : SUB_CMD_ARG_HM.entrySet()) {
            argAL.add("-" + entry.getKey());
            argAL.addAll(entry.getValue());
        }
        return argAL.toArray(new String[argAL.size()]);
    }

    public void rmDfltArgs(int cnt) {
        int rmCnt = 0;
        while (rmCnt < cnt) {
            rmCnt++;
            DFLT_ARG_AL.remove(0);
        }
    }

    public boolean chkPrefixArgs(String... reg_expr_args) {
        return chkPrefixArgs(-1, reg_expr_args);
    }

    public boolean chkPrefixArgs(int maxCnt, String... reg_expr_args) {
        if (maxCnt >= 0 && reg_expr_args.length > maxCnt) {
            return false;
        }
        if (reg_expr_args != null && !DFLT_ARG_AL.isEmpty() && DFLT_ARG_AL.size() >= reg_expr_args.length) {
            for (int idx = 0; idx < reg_expr_args.length; idx++) {
                if (!Pattern.compile(reg_expr_args[idx]).matcher(DFLT_ARG_AL.get(idx)).find()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Optional<Boolean> chkDfltArgOrSubcmd(String name) {
        Pattern ptrn = Pattern.compile(name);
        Optional<String> strOpt = DFLT_ARG_AL.stream().filter((s) -> ptrn.matcher(s).find()).findFirst();
        if (!strOpt.isPresent()) {
            strOpt = this.SUB_CMD_ARG_HM.keySet().stream().filter((s) -> ptrn.matcher(s).find()).findFirst();
        }
        return Optional.of(strOpt.isPresent());
    }

    public Optional<Boolean> chkSubcmd(String name) {
        return Optional.of(this.SUB_CMD_ARG_HM.keySet().stream().filter((s) -> s.equals(name)).findFirst().isPresent());
    }

    public Optional<String> getSubcmdFrstArg(String cmdNm) {
        return this.SUB_CMD_ARG_HM.entrySet().stream().filter((e) -> e.getKey().equals(cmdNm))
                .findFirst().map((e) -> e.getValue()).map((aL) -> aL.size() > 0 ? aL.get(0) : null);
    }

    public String bldCmdLine() {
        StringBuilder strBldr = new StringBuilder();
        DFLT_ARG_AL.stream().forEach((s) -> strBldr.append(s).append(" "));
        SUB_CMD_ARG_HM.entrySet().stream().forEach((e1) -> {
            strBldr.append("-").append(e1.getKey()).append(" ");
            e1.getValue().stream().forEach((e2) -> strBldr.append(e2).append(" "));
        });
        return strBldr.toString();
    }

    //  ------------------------------------------------------------------------
    public static void wrtBytes(AppArgs aAppArgs, BsO bsO) throws IOException {
        bsO.wInt(CLZ_ID);
        bsO.wInt(VER_ID);
        if (aAppArgs == null) {
            bsO.wByte((byte) 0);
        } else {
            bsO.wByte((byte) 1);
            //  處理 DFLT_ARG_AL 參數
            bsO.wInt(aAppArgs.DFLT_ARG_AL.size());
            for (String arg : aAppArgs.DFLT_ARG_AL) {
                bsO.wStr(arg);
            }
            //  處理 SUB_CMD_ARG_HM 參數
            bsO.wInt(aAppArgs.SUB_CMD_ARG_HM.size());
            for (Entry<String, ArrayList<String>> entry : aAppArgs.SUB_CMD_ARG_HM.entrySet()) {
                bsO.wStr(entry.getKey());
                bsO.wInt(entry.getValue().size());
                for (String arrVal : entry.getValue()) {
                    bsO.wStr(arrVal);
                }
            }
        }
    }

    public static byte[] getBytes(AppArgs aAppArgs) throws IOException {
        BsO bsO = new BsO();
        AppArgs.wrtBytes(aAppArgs, bsO);
        return bsO.getBytes();
    }

    public static AppArgs loadFrom(BsO bsO) throws IOException {
        try {
            int clz_id = bsO.rInt();
            assert CLZ_ID == clz_id;
            int ver_id = bsO.rInt();
            assert VER_ID == ver_id;
            byte existVal = bsO.rByte();
            if (existVal == 0) {
                return null;
            }
            AppArgs aMyArgs = new AppArgs();
            //  ----  處理 DFLT_ARG_AL 參數 (start) ----
            int dfltArgCnt = bsO.rInt();
            for (int idx01 = 0; idx01 < dfltArgCnt; idx01++) {
                aMyArgs.DFLT_ARG_AL.add(bsO.rStr());
            }
            //  ----  處理 DFLT_ARG_AL 參數 ( end ) ----
            //
            //  ----  處理 SUB_CMD_ARG_HM 參數 (start) ----
            int sbCmdCnt = bsO.rInt();
            for (int idx01 = 0; idx01 < sbCmdCnt; idx01++) {
                String key = bsO.rStr();
                ArrayList<String> argAL = new ArrayList<>();
                int argCnt = bsO.rInt();
                for (int idx02 = 0; idx02 < argCnt; idx02++) {
                    argAL.add(bsO.rStr());
                }
                aMyArgs.SUB_CMD_ARG_HM.put(key, argAL);
            }
            //  ----  處理 SUB_CMD_ARG_HM 參數 ( end ) ----
            //
            return aMyArgs;
        } catch (IOException ex) {
            throw new IOException(ex);
        }
    }

    public static AppArgs loadFrom(byte[] bs) throws IOException {
        return AppArgs.loadFrom(new BsO(bs));
    }

    //  ------------------------------------------------------------------------
    public static AppArgs bld(String[] args) {
        AppArgs aMyArgs = new AppArgs();
        Stream.of(args).forEach((a) -> aMyArgs.push(a));
        return aMyArgs;
    }

}
