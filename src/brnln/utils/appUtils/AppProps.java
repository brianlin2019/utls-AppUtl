package brnln.utils.appUtils;

import brnln.utils.appUtils.LmUtl.ExRcvr;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
//  test2
public class AppProps {

    private final Properties myProps;
    private final File file;

    private AppProps(Properties myProps, File file) {
        this.myProps = myProps;
        this.file = file;
    }

    public void putBooleanValue(String name, boolean value) {
        myProps.put(name, value + "");
    }

    public Optional<Boolean> getBooleanOpt(String name) {
        String strVal = myProps.getProperty(name);
        if (strVal != null) {
            strVal = strVal.trim();
        }
        if (strVal != null) {
            return Optional.ofNullable(Boolean.parseBoolean(strVal));
        } else {
            return Optional.empty();
        }
    }

    public void putLongValue(String name, long value) {
        myProps.put(name, value + "");
    }

    public Optional<Long> getLongOpt(String name) {
        String strVal = myProps.getProperty(name);
        if (strVal != null) {
            strVal = strVal.trim();
        }
        try {
            return Optional.of(Long.parseLong(strVal));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public void putIntValue(String name, int value) {
        myProps.put(name, value + "");
    }

    public Optional<Integer> getIntOpt(String name) {
        String strVal = myProps.getProperty(name);
        if (strVal != null) {
            strVal = strVal.trim();
        }
        try {
            return Optional.of(Integer.parseInt(strVal));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public void putStringValue(String name, String value) {
        myProps.put(name, value);
    }

    public Optional<String> getStrOpt(String name) {
        String strVal = myProps.getProperty(name);
        strVal = strVal != null ? strVal.trim() : strVal;
        return Optional.ofNullable(strVal);
    }

    public List<String> qryPropNameLst(Pattern ptrn) {
        return myProps.keySet().stream().map((k) -> (String) k).filter((k) -> ptrn.matcher(k).find()).collect(Collectors.toList());
    }

    public boolean reloadFromFile() {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
            this.myProps.load(fileIn);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public boolean saveToFile() {
        FileOutputStream fileOutStrm = null;
        try {
            fileOutStrm = new FileOutputStream(file);
            this.myProps.put("SAVE_TIME", AppUtils.getCurrDateTimeStrV2());
            this.myProps.store(fileOutStrm, null);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (fileOutStrm != null) {
                try {
                    fileOutStrm.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public Optional<File> getFile() {
        return Optional.ofNullable(this.file);
    }

    //  --------------------------------------------------------------------
    private static void sendEx(Exception ex, ExRcvr aExRcvr) {
        try {
            if (aExRcvr != null) {
                aExRcvr.rcve(ex);
            } else {
                ex.printStackTrace(System.err);
            }
        } catch (Exception ex2) {
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static Optional<AppProps> loadPropData(String cnfNm, String filePath, ExRcvr aExRcvr) {
        try {
            //  取得設定檔相關路徑資訊
            File cnfFile = Optional.ofNullable(
                    Optional.ofNullable(cnfNm == null ? null : System.getProperty(cnfNm)) //  先取用 cnfNm 的設定
                    .orElseGet(() -> filePath) /*  若不存在的話則取用 filePath 值 */)
                    .map((s) -> new File(s)) // 將路徑轉成檔案
                    .orElseThrow( // 取出檔案, 若不存在則抛出例外
                            () -> new Exception(String.format("The settings file could not be found. (cnfNm:%s, filePath:%s, )", cnfNm, filePath)));
            //  若設定檔案不存在, 則抛出例外
            Optional.of(cnfFile).map((f) -> f.exists() ? Boolean.TRUE : null).orElseThrow(
                    () -> new IOException(String.format("The settings file does not exist. (path:%s)", cnfFile.getPath())));
            //  若檔案存在, 則將其載入
            try (FileInputStream fileIn = new FileInputStream(cnfFile)) {
                Properties myProps = new Properties();
                myProps.load(fileIn);
                return Optional.of(new AppProps(myProps, cnfFile));
            }
        } catch (Exception ex) {
            sendEx(ex, aExRcvr);
        }
        return Optional.empty();
    }

}
