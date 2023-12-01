package hudson.plugins.virtualbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mihai Serban
 */
public final class VirtualBoxUtils {

    // public methods
    public static long startVm(VirtualBoxMachine machine, String virtualMachineType, VirtualBoxLogger log) {
        long result = 1;
        Thread current_thread = Thread.currentThread();
        ClassLoader cl_orig = current_thread.getContextClassLoader();
        current_thread.setContextClassLoader(VirtualBoxUtils.class.getClassLoader());
        try {
            result = getVboxControl(machine.getHost(), log).startVm(machine, virtualMachineType, log);
        } finally {
            current_thread.setContextClassLoader(cl_orig);
        }
        return result;
    }

    public static long stopVm(VirtualBoxMachine machine, String virtualMachineStopMode, VirtualBoxLogger log) {
        long result = 1;
        Thread current_thread = Thread.currentThread();
        ClassLoader cl_orig = current_thread.getContextClassLoader();
        current_thread.setContextClassLoader(VirtualBoxUtils.class.getClassLoader());
        try {
            result = getVboxControl(machine.getHost(), log).stopVm(machine, virtualMachineStopMode, log);
        } finally {
            current_thread.setContextClassLoader(cl_orig);
        }
        return result;
    }

    public static List<VirtualBoxMachine> getMachines(VirtualBoxCloud host, VirtualBoxLogger log) {
        List<VirtualBoxMachine> machines = null;
        Thread current_thread = Thread.currentThread();
        ClassLoader cl_orig = current_thread.getContextClassLoader();
        current_thread.setContextClassLoader(VirtualBoxUtils.class.getClassLoader());
        try {
            machines = getVboxControl(host, log).getMachines(host, log);
        } finally {
            current_thread.setContextClassLoader(cl_orig);
        }
        return machines;
    }

    public static void disconnectAll() {
        Thread current_thread = Thread.currentThread();
        ClassLoader cl_orig = current_thread.getContextClassLoader();
        current_thread.setContextClassLoader(VirtualBoxUtils.class.getClassLoader());
        try {
            for (Map.Entry<String, VirtualBoxControl> entry : vboxControls.entrySet()) {
                entry.getValue().disconnect();
            }
            vboxControls.clear();
        } finally {
            current_thread.setContextClassLoader(cl_orig);
        }
    }

    // private methods
    private VirtualBoxUtils() {}

    /**
     * Cache connections to VirtualBox hosts
     * TODO: keep the connections alive with a noop
     */
    private static HashMap<String, VirtualBoxControl> vboxControls = new HashMap<String, VirtualBoxControl>();

    private static synchronized VirtualBoxControl getVboxControl(VirtualBoxCloud host, VirtualBoxLogger log) {
        VirtualBoxControl vboxControl = vboxControls.get(host.toString());
        if (null != vboxControl) {
            if (vboxControl.isConnected()) {
                return vboxControl;
            }
            log.logInfo("Lost connection to " + host.getUrl() + ", reconnecting");
            vboxControls.remove(host.toString()); // force a reconnect
        }
        vboxControl = createVboxControl(host, log);

        vboxControls.put(host.toString(), vboxControl);
        return vboxControl;
    }

    private static VirtualBoxControl createVboxControl(VirtualBoxCloud host, VirtualBoxLogger log) {
        VirtualBoxControl vboxControl = null;

        String version = GetVboxVersion(host, log);

        if (version.startsWith("7.0")) {
            vboxControl = new VirtualBoxControlV70(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("6.1")) {
            vboxControl = new VirtualBoxControlV61(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("6.0")) {
            vboxControl = new VirtualBoxControlV60(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("5.2")) {
            vboxControl = new VirtualBoxControlV52(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("5.1")) {
            vboxControl = new VirtualBoxControlV51(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("5.0")) {
            vboxControl = new VirtualBoxControlV50(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("4.3")) {
            vboxControl = new VirtualBoxControlV43(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("4.2")) {
            vboxControl = new VirtualBoxControlV42(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("4.1")) {
            vboxControl = new VirtualBoxControlV41(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("4.0")) {
            vboxControl = new VirtualBoxControlV40(host.getUrl(), host.getUsername(), host.getPassword());
        } else if (version.startsWith("3.")) {
            vboxControl = new VirtualBoxControlV31(host.getUrl(), host.getUsername(), host.getPassword());
        } else {
            log.logError("VirtualBox version " + version + " not supported.");
            throw new UnsupportedOperationException("VirtualBox version " + version + " not supported.");
        }

        log.logInfo("Connected to VirtualBox version " + version + " on host " + host.getUrl());
        return vboxControl;
    }

    private static String GetVboxVersion(VirtualBoxCloud host, VirtualBoxLogger log) {
        log.logInfo("Trying to connect to " + host.getUrl() + ", user " + host.getUsername());
        String version = "";
        try {
            org.virtualbox_7_0.VirtualBoxManager manager = org.virtualbox_7_0.VirtualBoxManager.createInstance(null);
            manager.connect(
                    host.getUrl(), host.getUsername(), host.getPassword().getPlainText());
            version = manager.getVBox().getVersion();
            manager.disconnect();
        } catch (Exception e) {
            try {
                // fallback to old method
                com.sun.xml.ws.commons.virtualbox_3_1.IWebsessionManager manager =
                        new com.sun.xml.ws.commons.virtualbox_3_1.IWebsessionManager(host.getUrl());
                com.sun.xml.ws.commons.virtualbox_3_1.IVirtualBox vbox =
                        manager.logon(host.getUsername(), host.getPassword().getPlainText());
                version = vbox.getVersion();
                manager.disconnect(vbox);
            } catch (Exception e1) {
                log.logError("Not connection : " + e1.getMessage());
                version = "0.0";
            }
        }
        return version;
    }
}
