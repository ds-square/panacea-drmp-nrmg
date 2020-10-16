
package org.panacea.drmp.nrmg.domain.reachability;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class ReachabilityInventory {

    private String environment;
    private String fileType;
    private String snapshotId;
    private String snapshotTime;
    private List<SourceDevice> sourceDevices;

    public ReachabilityInventory(String environment, String snapshotId, String snapshotTime, List<SourceDevice> sourceDevices) {
        this.environment = environment;
        this.fileType = "ReachabilityInventory";
        this.snapshotId = snapshotId;
        this.snapshotTime = snapshotTime;
        this.sourceDevices = sourceDevices;
    }
}
