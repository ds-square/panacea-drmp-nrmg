
package org.panacea.drmp.nrmg.domain.policy;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class RoutingPolicy {

    private String deviceId;
    private List<RoutingRule> routingRules;

}
