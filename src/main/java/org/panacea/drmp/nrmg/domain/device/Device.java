
package org.panacea.drmp.nrmg.domain.device;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@SuppressWarnings("unused")
public class Device {

    private String hostName;
    private String id;
    private List<NetworkInterface> networkInterfaces;
    private OperatingSystem operatingSystem;
    private String type;
    private List<String> vulnerabilities;

    public Device(String hostName, String id, List<NetworkInterface> networkInterfaces, OperatingSystem operatingSystem, String type, List<String> vulnerabilities) {
        this.hostName = hostName;
        this.id = id;
        this.networkInterfaces = networkInterfaces;
        this.operatingSystem = operatingSystem;
        this.type = type;
        this.vulnerabilities = vulnerabilities;
    }

    public Device() {
        this.vulnerabilities = new ArrayList<>();
    }

    public void addSingleVulnerability(String vulnId) {
        this.vulnerabilities.add(vulnId);
    }

}
