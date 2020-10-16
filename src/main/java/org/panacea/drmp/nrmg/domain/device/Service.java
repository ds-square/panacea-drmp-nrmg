
package org.panacea.drmp.nrmg.domain.device;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class Service {

    private List<String> cpe;
    private String description;
    private String runningState;
    private String version;
    private List<String> vulnerabilities;

}
