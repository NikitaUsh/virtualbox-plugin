package hudson.plugins.virtualbox;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * {@link Slave} running on VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxSlave extends Slave {
    private static final Logger LOG = Logger.getLogger(VirtualBoxSlave.class.getName());

    private final String hostName;
    private final String virtualMachineName;
    private final String virtualMachineType;
    private final String virtualMachineStopMode;

    @DataBoundConstructor
    public VirtualBoxSlave(
            @NonNull String name,
            String remoteFS,
            ComputerLauncher delegateLauncher,
            String hostName,
            String virtualMachineName,
            String virtualMachineType,
            String virtualMachineStopMode)
            throws Descriptor.FormException, IOException {
        super(
                name,
                remoteFS,
                new VirtualBoxComputerLauncher(
                        delegateLauncher, hostName, virtualMachineName, virtualMachineType, virtualMachineStopMode));
        this.hostName = hostName;
        this.virtualMachineName = virtualMachineName;
        this.virtualMachineType = virtualMachineType;
        this.virtualMachineStopMode = virtualMachineStopMode;
    }

    @Override
    public Computer createComputer() {
        return new VirtualBoxComputer(this);
    }

    /**
     * @return host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @return virtual machine name
     */
    public String getVirtualMachineName() {
        return virtualMachineName;
    }

    /**
     * @return type of virtual machine, can be headless, vrdp, gui, or sdl
     */
    public String getVirtualMachineType() {
        return virtualMachineType;
    }

    /**
     * @return type of stop mode for virtual machine, can be powerdown or pause
     */
    public String getVirtualMachineStopMode() {
        return virtualMachineStopMode;
    }

    @Override
    public VirtualBoxComputerLauncher getLauncher() {
        return (VirtualBoxComputerLauncher) super.getLauncher();
    }

    /**
     * For UI.
     *
     * @return original launcher
     */
    public ComputerLauncher getDelegateLauncher() {
        return getLauncher().getCore();
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.VirtualBoxSlave_displayName();
        }

        /**
         * For UI.
         *
         * @see VirtualBoxPlugin#getHosts()
         * @return A list of defined VB cloud hosts
         */
        public ListBoxModel doFillHostNameItems() {
            ListBoxModel m = new ListBoxModel();
            m.add(Messages.VirtualBoxSlave_defaultHost());
            for (VirtualBoxCloud vc : VirtualBoxPlugin.getHosts()) {
                m.add(vc.getDisplayName());
            }
            return m;
        }

        /**
         * For UI.
         * @param hostName The name of the host to be validated.
         * @return FormValidation for error or ok
         */
        public FormValidation doCheckHostName(@QueryParameter String hostName) {
            LOG.log(Level.INFO, "Check host name as " + hostName);
            if (hostName == null
                    || hostName.isEmpty()
                    || Messages.VirtualBoxSlave_defaultHost().equals(hostName)) {
                LOG.log(Level.INFO, "mandatory host name as " + hostName);
                return FormValidation.error("VirtualBox Host is mandatory!");
            } else {
                return FormValidation.ok();
            }
        }

        /**
         * For UI.
         *
         * @see VirtualBoxPlugin#getDefinedVirtualMachines(String)
         * @param hostName VirtualBox cloud name
         * @return A list of defined VirtualBoxMachines
         */
        public ListBoxModel doFillVirtualMachineNameItems(@QueryParameter String hostName) {
            if (hostName == null || Messages.VirtualBoxSlave_defaultHost().equals(hostName)) {
                LOG.log(Level.INFO, "Default host name selected - returning null virtual machine list");
                return null;
            }
            LOG.log(Level.INFO, "Host name set as " + hostName);
            ListBoxModel m = new ListBoxModel();
            for (VirtualBoxMachine vm : VirtualBoxPlugin.getDefinedVirtualMachines(hostName)) {
                m.add(vm.getName());
            }
            return m;
        }

        /**
         * For UI.
         * @param virtualMachineName The name of the virtual machine to be validated.
         * @return FormValidation for error or ok
         */
        public FormValidation doCheckVirtualMachineName(@QueryParameter String virtualMachineName) {
            LOG.log(Level.INFO, "Check VM name as " + virtualMachineName);
            if (Util.fixEmptyAndTrim(virtualMachineName) == null) {
                return FormValidation.error("Virtual Machine Name is mandatory!");
            } else {
                return FormValidation.ok();
            }
        }
    }
}
