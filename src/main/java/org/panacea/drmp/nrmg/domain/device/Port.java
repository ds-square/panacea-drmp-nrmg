
package org.panacea.drmp.nrmg.domain.device;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Port {

    private int number;
    private Service service;
    private String state;
    private String transportProtocol;

    public Port(int number, Service service, String state, String transportProtocol) {
        this.number = number;
        this.service = service;
        this.state = state;
        this.transportProtocol = transportProtocol.toUpperCase();
    }
}
