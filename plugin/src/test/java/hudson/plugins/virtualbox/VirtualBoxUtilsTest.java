package hudson.plugins.virtualbox;

import static org.junit.jupiter.api.Assertions.*;

import hudson.util.Secret;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class VirtualBoxUtilsTest {

    private static final Logger LOG = Logger.getLogger(VirtualBoxUtils.class.getName());

    @Test
    void getMachines() {
        String url = "http://localhost:18083";
        String username = "godin";
        Secret password = Secret.fromString("12345");
        VirtualBoxCloud cloud = new VirtualBoxCloud("TestConnection", url, username, password);
        VirtualBoxSystemLog log = new VirtualBoxSystemLog(LOG, "[VirtualBox] ");

        List<VirtualBoxMachine> machines = VirtualBoxUtils.getMachines(cloud, log);

        assertEquals(6, machines.size());
    }
}
