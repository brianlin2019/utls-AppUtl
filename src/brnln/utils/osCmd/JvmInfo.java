package brnln.utils.osCmd;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;

public class JvmInfo {

    private static JvmInfo inst;
    private final RuntimeMXBean rtMbn;
    private final ThreadMXBean thdMbn;
    private final OperatingSystemMXBean osMbn;
    //
    public final long pid;

    private JvmInfo() {
        rtMbn = ManagementFactory.getRuntimeMXBean();
        thdMbn = ManagementFactory.getThreadMXBean();
        osMbn = ManagementFactory.getOperatingSystemMXBean();
        pid = Long.parseLong(rtMbn.getName().substring(0, rtMbn.getName().indexOf("@")));
    }

    public Date startTime() {
        return new Date(rtMbn.getStartTime());
    }

    public long upTime() {
        return rtMbn.getUptime();
    }

    public String osArch() {
        return osMbn.getArch();
    }

    public String osName() {
        return osMbn.getName();
    }

    public String osVer() {
        return osMbn.getVersion();
    }

    public String vmSpc() {
        return rtMbn.getSpecVersion();
    }

    public String vmName() {
        return rtMbn.getVmName();
    }

    public String vmVer() {
        return rtMbn.getVmVersion();
    }

    public String getOsInfo() {
        JvmInfo aJvmInfo = JvmInfo.inst();
        return String.format("%s (%s-%s) ", aJvmInfo.osName(), aJvmInfo.osVer(), aJvmInfo.osArch());
    }

    public String getVmInfo() {
        JvmInfo aJvmInfo = JvmInfo.inst();
        return String.format("Java %s (%s-%s)", aJvmInfo.vmSpc(), aJvmInfo.vmName(), aJvmInfo.vmVer());
    }

    //  ------------------------------------------------------------------------
    public boolean isWindows() {
        return JvmInfo.inst().osName().toLowerCase().contains("windows");
    }

    public boolean isLinux() {
        return JvmInfo.inst().osName().toLowerCase().contains("linux");
    }

    //  ------------------------------------------------------------------------
    public synchronized static JvmInfo inst() {
        if (inst == null) {
            inst = new JvmInfo();
        }

        return inst;
    }

}
