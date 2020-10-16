package org.panacea.drmp.nrmg.service;


import org.panacea.drmp.nrmg.domain.device.DeviceInventory;
import org.panacea.drmp.nrmg.domain.policy.PolicyInventory;

public interface NRMGInputRequestService {

    DeviceInventory performDeviceInventoryRequest(String snapshotId);

    PolicyInventory performPolicyInventoryRequest(String snapshotId);

}
