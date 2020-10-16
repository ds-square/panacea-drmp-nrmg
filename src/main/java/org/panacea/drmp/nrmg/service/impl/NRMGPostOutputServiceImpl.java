package org.panacea.drmp.nrmg.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.nrmg.domain.reachability.ReachabilityInventory;
import org.panacea.drmp.nrmg.service.NRMGPostOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
public class NRMGPostOutputServiceImpl implements NRMGPostOutputService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${reachabilityInventory.endpoint}")
    private String reachabilityInventoryURL;

    @Value("${reachabilityInventory.fn}")
    private String reachabilityInventoryFn;


    @Override
    public void postReachabilityInventory(ReachabilityInventory inventory) {

        HttpEntity<ReachabilityInventory> requestEntity
                = new HttpEntity<>(inventory);

        String endPointUrl = reachabilityInventoryURL + '/'; // + inventory.getSnapshotId() + '/';

        log.info("POST ReachabilityInventory to " + endPointUrl);
        ResponseEntity<String> response = null;
        RestTemplate restTemplate = new RestTemplate();
        try {
            response = restTemplate
                    .postForEntity(endPointUrl, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
           log.info("Response from storage service: " + response);
            byte[] bytes = e.getResponseBodyAsByteArray();

            //Convert byte[] to String
            String s = new String(bytes);

            log.error(s);
            e.printStackTrace();

        }

    }
}
