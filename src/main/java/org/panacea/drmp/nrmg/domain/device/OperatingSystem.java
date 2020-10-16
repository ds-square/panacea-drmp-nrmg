
package org.panacea.drmp.nrmg.domain.device;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class OperatingSystem {

    private String family;
    private String generation;
    private List<LocalService> localServices;
    private List<OsUser> osUsers;
    private List<String> osVulnerabilities;
    private String vendor;

}
