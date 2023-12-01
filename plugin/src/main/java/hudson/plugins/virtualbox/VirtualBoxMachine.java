package hudson.plugins.virtualbox;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxMachine implements Comparable<VirtualBoxMachine> {

    private final VirtualBoxCloud host;
    private final String name;

    @DataBoundConstructor
    public VirtualBoxMachine(VirtualBoxCloud host, String name) {
        this.host = host;
        this.name = name;
    }

    public VirtualBoxCloud getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VirtualBoxMachine that = (VirtualBoxMachine) o;

        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull VirtualBoxMachine obj) {
        // TODO Godin compare host ? check on null?
        return name.compareTo(obj.getName());
    }

    @Override
    public String toString() {
        return "VirtualBoxMachine{" + "host=" + host + ", name='" + name + '\'' + '}';
    }
}
