package hudson.plugins.virtualbox;

import hudson.Plugin;
import hudson.slaves.Cloud;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

/**
 * TODO see https://jax-ws.dev.java.net/issues/show_bug.cgi?id=554
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxPlugin extends Plugin {

    private static final Logger LOG = Logger.getLogger(VirtualBoxPlugin.class.getName());

    @Override
    public void start() throws Exception {
        LOG.log(Level.INFO, "Starting {0}", getClass().getSimpleName());
        super.start();
    }

    @Override
    public void stop() throws Exception {
        LOG.log(Level.INFO, "Stopping {0}", getClass().getSimpleName());
        super.stop();
        // close VirtualBox WEB sessions
        VirtualBoxUtils.disconnectAll();
    }

    /**
     * @return all registered {@link VirtualBoxCloud}
     */
    public static List<VirtualBoxCloud> getHosts() {
        List<VirtualBoxCloud> result = new ArrayList<VirtualBoxCloud>();
        Jenkins jenkins = Jenkins.get();
        for (Cloud cloud : jenkins.clouds) {
            if (cloud instanceof VirtualBoxCloud) {
                result.add((VirtualBoxCloud) cloud);
            }
        }
        return result;
    }

    /**
     * @param hostName host name
     * @return {@link VirtualBoxCloud} by specified name, null if not found
     */
    public static VirtualBoxCloud getHost(String hostName) {
        if (hostName == null) {
            return null;
        }
        for (VirtualBoxCloud host : getHosts()) {
            if (hostName.equals(host.getDisplayName())) {
                return host;
            }
        }
        return null;
    }

    /**
     * @param hostName host name
     * @return all registered {@link VirtualBoxMachine} from specified host, empty list if unknown host
     */
    public static List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
        VirtualBoxCloud host = getHost(hostName);
        if (host == null) {
            return Collections.emptyList();
        }
        return host.refreshVirtualMachinesList();
    }

    /**
     * @param hostName           host name
     * @param virtualMachineName virtual machine name
     * @return {@link VirtualBoxMachine} from specified host with specified name, null if not found
     */
    public static VirtualBoxMachine getVirtualBoxMachine(String hostName, String virtualMachineName) {
        if (virtualMachineName == null) {
            return null;
        }
        VirtualBoxCloud host = VirtualBoxPlugin.getHost(hostName);
        if (host == null) {
            return null;
        }
        return host.getVirtualMachine(virtualMachineName);
    }
}
