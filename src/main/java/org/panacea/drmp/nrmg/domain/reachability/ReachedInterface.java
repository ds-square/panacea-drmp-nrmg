
package org.panacea.drmp.nrmg.domain.reachability;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class ReachedInterface {

    private String address;
    private String lanID;
    private String name;
    private List<ReachedDevice> reachedDevices;

}
