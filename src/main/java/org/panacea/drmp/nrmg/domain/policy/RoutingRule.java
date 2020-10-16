
package org.panacea.drmp.nrmg.domain.policy;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class RoutingRule {

    private String destination;
    private String gateway;
    private String iface;
    private String mask;
    private int metric;
    private long ruleID;

    public RoutingRule(String destination, String gateway, String iface, String mask, int metric) {
        this.destination = destination;
        if (destination.equals("0.0.0.0")) {
            this.destination = "default";
        } else {
            this.destination = destination;
        }
        if (gateway.equals("0.0.0.0")) {
            this.gateway = "*";
        } else {
            this.gateway = gateway;
        }
        this.iface = iface;
        this.mask = mask;
        this.metric = metric;
    }
}
