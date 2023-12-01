package hudson.plugins.virtualbox;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * {@link Cloud} implementation for VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxCloud extends Cloud {
    private static final Logger LOG = Logger.getLogger(VirtualBoxCloud.class.getName());

    private final String url;
    private final String username;
    private final Secret password;

    /**
     * Lazily computed list of virtual machines from this host.
     */
    private transient List<VirtualBoxMachine> virtualBoxMachines = null;

    @DataBoundConstructor
    public VirtualBoxCloud(String displayName, String url, String username, Secret password) {
        super(displayName);
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<NodeProvisioner.PlannedNode> provision(CloudState state, int excessWorkload) {
        return Collections.emptyList();
    }

    @Override
    public boolean canProvision(CloudState state) {
        return false;
    }

    public synchronized List<VirtualBoxMachine> refreshVirtualMachinesList() {
        virtualBoxMachines = VirtualBoxUtils.getMachines(this, new VirtualBoxSystemLog(LOG, "[VirtualBox] "));
        return virtualBoxMachines;
    }

    public synchronized VirtualBoxMachine getVirtualMachine(String virtualMachineName) {
        if (null == virtualBoxMachines) {
            refreshVirtualMachinesList();
        }
        for (VirtualBoxMachine machine : virtualBoxMachines) {
            if (virtualMachineName.equals(machine.getName())) {
                return machine;
            }
        }
        return null;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.VirtualBoxHost_displayName();
        }

        /**
         * For UI.
         */
        @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
        public FormValidation doTestConnection(
                @QueryParameter String url, @QueryParameter String username, @QueryParameter Secret password) {
            LOG.log(Level.INFO, "Testing connection to {0} with username {1}", new Object[] {url, username});
            try {
                VirtualBoxUtils.getMachines(
                        new VirtualBoxCloud("testConnection", url, username, password),
                        new VirtualBoxSystemLog(LOG, "[VirtualBox] "));
                return FormValidation.ok(Messages.VirtualBoxHost_success());
            } catch (Throwable e) {
                return FormValidation.error(e.getMessage());
            }
        }
    }

    @NonNull
    @Override
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public Secret getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "VirtualBoxCloud{" + "url='"
                + url + '\'' + ", username='"
                + username + '\'' + ", name='"
                + name + '\'' + '}';
    }
}
