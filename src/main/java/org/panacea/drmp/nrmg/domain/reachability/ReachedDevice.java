
package org.panacea.drmp.nrmg.domain.reachability;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class ReachedDevice {

    private String address;
    private String hostName;
    private String deviceId;
    private String ifaceName;
    private String lanID;
    private List<String> path;
    private List<ReachedPort> reachedPorts;

}
