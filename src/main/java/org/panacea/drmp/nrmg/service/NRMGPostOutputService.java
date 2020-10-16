package org.panacea.drmp.nrmg.service;


import org.panacea.drmp.nrmg.domain.reachability.ReachabilityInventory;

public interface NRMGPostOutputService {
    void postReachabilityInventory(ReachabilityInventory inventory);
}
