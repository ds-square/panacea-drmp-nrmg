
package org.panacea.drmp.nrmg.domain.device;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class DeviceInventory {

    private List<Device> devices;
    private String environment;
    private String fileType;
    private String snapshotId;
    private String snapshotTime;

}
