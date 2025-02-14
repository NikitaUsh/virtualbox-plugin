package hudson.plugins.virtualbox;

import com.sun.xml.ws.commons.virtualbox_3_1.*;
import hudson.util.Secret;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public final class VirtualBoxControlV31 implements VirtualBoxControl {

    private final String hostUrl;
    private final String userName;
    private final String password;

    public VirtualBoxControlV31(String hostUrl, String userName, Secret password) {
        // verify connection
        this.hostUrl = hostUrl;
        this.userName = userName;
        this.password = password.getPlainText();

        ConnectionHolder holder = connect(hostUrl, userName, password.getPlainText());
        holder.disconnect();
    }

    private static class ConnectionHolder {
        IWebsessionManager manager;
        IVirtualBox vbox;

        public void disconnect() {
            manager.disconnect(vbox);
        }
    }

    private static ConnectionHolder connect(String hostUrl, String userName, String password) {
        IWebsessionManager manager = new IWebsessionManager(hostUrl);
        ConnectionHolder holder = new ConnectionHolder();
        holder.manager = manager;
        holder.vbox = manager.logon(userName, password);
        return holder;
    }

    @Override
    public synchronized void disconnect() {}

    @Override
    public boolean isConnected() {
        try {
            ConnectionHolder holder = connect(hostUrl, userName, password);
            holder.vbox.getVersion();
            holder.disconnect();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * @param host VirtualBox host
     * @return list of virtual machines installed on specified host
     */
    @Override
    public synchronized List<VirtualBoxMachine> getMachines(VirtualBoxCloud host, VirtualBoxLogger log) {
        List<VirtualBoxMachine> result = new ArrayList<VirtualBoxMachine>();
        ConnectionHolder holder = connect(hostUrl, userName, password);
        for (IMachine machine : holder.vbox.getMachines()) {
            result.add(new VirtualBoxMachine(host, machine.getName()));
        }
        holder.disconnect();
        return result;
    }

    /**
     * Starts specified VirtualBox virtual machine.
     *
     * @param vbMachine virtual machine to start
     * @param type      session type (can be headless, vrdp, gui, sdl)
     * @return result code
     */
    @Override
    public synchronized long startVm(VirtualBoxMachine vbMachine, String type, VirtualBoxLogger log) {
        ConnectionHolder holder = connect(hostUrl, userName, password);
        IMachine machine = holder.vbox.findMachine(vbMachine.getName());
        if (org.virtualbox_3_1.MachineState.RUNNING == machine.getState()) {
            holder.disconnect();
            return 0;
        }
        ISession session = holder.manager.getSessionObject(holder.vbox);
        // start the virtual machine in a separate process
        IProgress progress = holder.vbox.openRemoteSession(
                session,
                machine.getId(),
                type, // sessionType (headless, vrdp)
                "" // env
                );
        progress.waitForCompletion(-1);
        long result = progress.getResultCode();
        session.close(); // match openRemoteSession
        holder.disconnect();
        return result;
    }

    /**
     * Stops specified VirtualBox virtual machine.
     *
     * @param vbMachine virtual machine to stop
     * @return result code
     */
    @Override
    public synchronized long stopVm(VirtualBoxMachine vbMachine, String StopMode, VirtualBoxLogger log) {
        ConnectionHolder holder = connect(hostUrl, userName, password);
        IMachine machine = holder.vbox.findMachine(vbMachine.getName());
        if (org.virtualbox_3_1.MachineState.RUNNING != machine.getState()) {
            holder.disconnect();
            return 0;
        }
        ISession session = holder.manager.getSessionObject(holder.vbox);

        holder.vbox.openExistingSession(session, machine.getId());
        IProgress progress = session.getConsole().powerDown();
        progress.waitForCompletion(-1);
        long result = progress.getResultCode();
        session.close(); // match openExistingSession
        holder.disconnect();
        return result;
    }
}
