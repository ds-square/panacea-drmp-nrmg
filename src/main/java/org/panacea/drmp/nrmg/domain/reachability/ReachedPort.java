
package org.panacea.drmp.nrmg.domain.reachability;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@SuppressWarnings("unused")
public class ReachedPort implements Comparable {

    private int port;
    private String protocol;
    private List<String> service;

    public ReachedPort() {
    }

    public ReachedPort(int port, List<String> service) {
        this.port = port;
        if (service == null) {
            this.service = new ArrayList<>();
        } else {
            this.service = service;
        }
    }

    public ReachedPort(int port, String protocol, List<String> service) {
        this.port = port;
        this.protocol = protocol;
        this.service = service;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReachedPort that = (ReachedPort) o;
        return port == that.port &&
                Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, protocol);
    }

    @Override
    public int compareTo(Object o) {
        int to_compare = 0;
        if (o.getClass() == ReachedPort.class) {
            to_compare = ((ReachedPort) o).port;
        }
        if (this.port < to_compare)
            return -1;
        if (this.port == to_compare) {
            return 0;
        }
        return 1;
    }
}
