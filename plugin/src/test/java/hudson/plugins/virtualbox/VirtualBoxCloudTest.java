package hudson.plugins.virtualbox;

import static org.junit.Assert.*;

import hudson.util.FormValidation;
import hudson.util.Secret;
import org.junit.Test;

public class VirtualBoxCloudTest {

    @Test
    public void connect() throws Throwable {
        String url = "http://localhost:18083";
        String username = "godin";
        Secret password = Secret.fromString("12345");
        VirtualBoxCloud.DescriptorImpl impl = new VirtualBoxCloud.DescriptorImpl();

        FormValidation res = impl.doTestConnection(url, username, password);

        assertEquals(FormValidation.ok().kind, res.kind);
    }
}
