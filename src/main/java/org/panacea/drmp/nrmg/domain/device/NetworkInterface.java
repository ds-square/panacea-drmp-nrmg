
package org.panacea.drmp.nrmg.domain.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.panacea.drmp.nrmg.domain.reachability.ReachedDevice;

import java.util.List;


@Data
@SuppressWarnings("unused")
public class NetworkInterface {

    @JsonProperty("ipAddress")
    private String iPAddress;
    private String macAddress;
    private String mask;
    private String name;
    private List<Port> ports;
    private String version;
    private List<ReachedDevice> reachedDevices;


    public NetworkInterface(String name, String macAddress, String iPAddress, String version, String mask) {
        this.iPAddress = iPAddress;
        this.macAddress = macAddress;
        this.name = name;
        this.version = version;
        if (mask != null) {
            if (!mask.equals("0.0.0.0") && !mask.equals("")) {
                this.mask = mask;
            } else {
                this.mask = "255.255.255.0";
            }
        } else {
            this.mask = "255.255.255.0";
        }
    }

}
