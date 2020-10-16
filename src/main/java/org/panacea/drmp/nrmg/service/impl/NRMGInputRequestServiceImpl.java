package org.panacea.drmp.nrmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.nrmg.domain.device.DeviceInventory;
import org.panacea.drmp.nrmg.domain.policy.PolicyInventory;
import org.panacea.drmp.nrmg.service.NRMGInputRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service

public class NRMGInputRequestServiceImpl implements NRMGInputRequestService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${reachabilityInventory.endpoint}")
    private String reachabilityInventoryURL;

    @Value("${reachabilityInventory.fn}")
    private String reachabilityInventoryFn;

    @Value("${deviceInventory.endpoint}")
    private String deviceInventoryURL;

    @Value("${deviceInventory.fn}")
    private String deviceInventoryFn;

    @Value("${policyInventory.endpoint}")
    private String policyInventoryURL;

    @Value("${policyInventory.fn}")
    private String policyInventoryFn;

    public DeviceInventory performDeviceInventoryRequest(String snapshotId) {

        ResponseEntity<DeviceInventory> responseEntity = restTemplate.exchange(
                deviceInventoryURL + '/' + snapshotId, //'/' + deviceInventoryFn,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<DeviceInventory>() {
                });

        DeviceInventory deviceInventory = responseEntity.getBody();

        return deviceInventory;
    }


    public PolicyInventory performPolicyInventoryRequest(String snapshotId) {

        ResponseEntity<PolicyInventory> responseEntity = restTemplate.exchange(
                policyInventoryURL + '/' + snapshotId, // + '/' + policyInventoryFn,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PolicyInventory>() {
                });
        PolicyInventory reachabilityInventory = responseEntity.getBody();

        return reachabilityInventory;
    }
}
