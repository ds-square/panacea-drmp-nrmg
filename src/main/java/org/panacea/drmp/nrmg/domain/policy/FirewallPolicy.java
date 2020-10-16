
package org.panacea.drmp.nrmg.domain.policy;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class FirewallPolicy {

    private String deviceId;
    private List<FirewallChain> firewallChains;

}
