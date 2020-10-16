
package org.panacea.drmp.nrmg.domain.policy;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class FwRule implements Comparable {

    private String action;
    private String dMask;
    private String dPorts;
    private String destination;
    private String index;
    private String inputIface;
    private String options;
    private String outputIface;
    private String protocol;
    private String sMask;
    private String sPorts;
    private String source;

    public FwRule(String action, String dMask, String dPorts, String destination, String inputIface, String outputIface, String protocol, String sMask, String sPorts, String source, String index, String options) {
        this.action = action;
        this.dMask = dMask;
        this.dPorts = dPorts;
        this.destination = destination;
        this.inputIface = inputIface;
        this.outputIface = outputIface;
        this.protocol = protocol;
        this.sMask = sMask;
        this.sPorts = sPorts;
        this.source = source;
        this.index = index;
        this.options = options;
    }

    @Override
    public int compareTo(Object o) {
        int currentIndex = (int) Double.parseDouble(this.index);
        int to_compare = 0;
        if (o.getClass() == FwRule.class) {
            to_compare = (int) Double.parseDouble(((FwRule) o).index);
        }
        if (currentIndex < to_compare)
            return -1;
        if (currentIndex == to_compare) {
            return 0;
        }
        return 1;
    }
}
