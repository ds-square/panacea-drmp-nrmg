
package org.panacea.drmp.nrmg.domain.policy;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class FirewallChain {

    private String defaultPolicy;
    private List<FwRule> fwRules;
    private String type;

}
