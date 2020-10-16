
package org.panacea.drmp.nrmg.domain.policy;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class PolicyInventory {

    private String environment;
    private String fileType;
    private List<FirewallPolicy> firewallPolicies;
    private List<RoutingPolicy> routingPolicies;
    private String snapshotId;
    private String snapshotTime;

}
