
package org.panacea.drmp.nrmg.domain.reachability;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class SourceDevice {

    private String hostName;
    private String id;
    private List<ReachedInterface> reachedInterface;

}
