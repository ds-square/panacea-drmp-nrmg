package org.panacea.drmp.nrmg.domain.reachability;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;
import org.panacea.drmp.nrmg.domain.device.Device;
import org.panacea.drmp.nrmg.domain.device.NetworkInterface;
import org.panacea.drmp.nrmg.domain.device.Port;
import org.panacea.drmp.nrmg.domain.device.Service;
import org.panacea.drmp.nrmg.domain.policy.FwRule;
import org.panacea.drmp.nrmg.domain.policy.RoutingRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class StructureInventory {
    public HashMap<String, ArrayList<RoutingRule>> from_id_to_directs_routing_rule = new HashMap(); //structure that contains all the directs for each deviceId
    public HashMap<String, ArrayList<RoutingRule>> from_id_to_statics = new HashMap(); //structure that contains all the statics for each deviceId
    public HashMap<String, Device> from_id_to_info = new HashMap(); //structure that contains info for each deviceId
    public HashMap<String, String> from_address_to_interface = new HashMap(); //structure that contains the name of the interface for each IP address
    public HashMap<String, ArrayList<RoutingRule>> from_id_to_defaults = new HashMap(); //structure that contains all the defaults for each deviceId
    private HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<FwRule>>>>> from_id_to_firewallChains = new HashMap(); // Structure that contain for each Chain the list of the firewall rules
    public HashMap<String, ArrayList<String>> from_cidr_to_addresses = new HashMap(); //structure that contains all the IP addresses for each CIDR
    private HashMap<String, HashMap<String, NetworkInterface>> from_id_to_addresses = new HashMap(); //Structure that contains the list of addresses for each deviceId
    private ArrayList<String> middleboxes = new ArrayList<String>(); //structure that contains all the middleboxes
    private ArrayList<String> terminals = new ArrayList<String>(); //structure that contains all the terminals
    private HashMap<String, String> from_addresses_to_id = new HashMap(); //structure that contains the deviceId for each IP address
    private HashMap<String, ArrayList<RoutingRule>> from_id_to_routes = new HashMap(); //Structure that contains the list of routing rules for each deviceId
    private HashMap<String, HashMap<String, ArrayList<ReachedPort>>> from_address_to_ports = new HashMap(); //structure that contains the list of TCP and UDP ports for each IP address
    private HashMap<String, HashMap<String, ArrayList<String>>> from_mbx_to_destinations = new HashMap(); //structure that contains all the destinations and the relative path for each middlebox
    private HashMap<String, ArrayList<String>> from_id_to_directs = new HashMap(); //structure that contains the ID for each deviceId
    public HashMap<String, SubnetUtils> from_cidr_to_subnetUtils = new HashMap(); //structure that contains the SubnetUtil for each cidr net
    public HashMap<String, String> from_lan_to_id = new HashMap(); //structure that contains the ID for each LAN
    public HashMap<String, String> from_ip_to_cidrLan = new HashMap(); //structure that contains the CIDR of the LAN for each IP

    public HashMap<String, HashMap<String, ArrayList<String>>> getFromMbxToDestination() {
        return this.from_mbx_to_destinations;
    }

    public void initializeStructureFromIdToDirects(String deviceId) {
        if (!this.from_id_to_directs.containsKey(deviceId)) {
            this.from_id_to_directs.put(deviceId, new ArrayList());
        }
    }

    public void initializeStructureFromIdToStatics(String deviceId) {
        if (!this.from_id_to_statics.containsKey(deviceId)) {
            this.from_id_to_statics.put(deviceId, new ArrayList());
        }
    }

    public void initializeStructureFromHostToDefaults(String deviceId) {
        if (!this.from_id_to_defaults.containsKey(deviceId)) {
            this.from_id_to_defaults.put(deviceId, new ArrayList());
        }
    }

    public void initializeStructureFromHostToDirects(String deviceId) {
        if (!this.from_id_to_directs_routing_rule.containsKey(deviceId)) {
            this.from_id_to_directs_routing_rule.put(deviceId, new ArrayList());
        }
    }

    public void initializeStructureFromIdToRoutes(String deviceId) {
        if (!this.from_id_to_routes.containsKey(deviceId)) {
            this.from_id_to_routes.put(deviceId, new ArrayList());
        }
    }

    public void initializeStructureFromIdToAddresses(String deviceId) {
        if (!this.from_id_to_addresses.containsKey(deviceId)) {
            this.from_id_to_addresses.put(deviceId, new HashMap());
        }
    }

    public void initializeStructureFromCIDRToAddresses(String cidr) {
        if (!this.from_cidr_to_addresses.containsKey(cidr)) {
            this.from_cidr_to_addresses.put(cidr, new ArrayList());
        }
    }

    public void initializeStructureFromMiddleboxesToDestinations(String middlebox) {
        if (!this.from_mbx_to_destinations.containsKey(middlebox)) {
            this.from_mbx_to_destinations.put(middlebox, new HashMap());
        }
    }

    public void addAddressToCIDR(String cidr, String address) {
        this.from_cidr_to_addresses.get(cidr).add(address);
    }

    public void addDirectToId(String deviceId, String direct) {
        this.from_id_to_directs.get(deviceId).add(direct);
    }

    public ArrayList<String> getDirectsFromId(String deviceId) {
        return this.from_id_to_directs.get(deviceId);
    }

    public void addRoute(String deviceId, RoutingRule route) {
        this.from_id_to_routes.get(deviceId).add(route);
    }

    public void addFirewallRule(String firewall_host, String chain, String source_ip, String destination_ip, FwRule firewallRule) {
        if (!this.from_id_to_firewallChains.containsKey(firewall_host)) {
            this.from_id_to_firewallChains.put(firewall_host, new HashMap());
        }
        if (!((HashMap) this.from_id_to_firewallChains.get(firewall_host)).containsKey(chain)) {
            ((HashMap) this.from_id_to_firewallChains.get(firewall_host)).put(chain, new HashMap());
        }
        if (!((HashMap) ((HashMap) this.from_id_to_firewallChains.get(firewall_host)).get(chain)).containsKey(source_ip)) {
            ((HashMap) ((HashMap) this.from_id_to_firewallChains.get(firewall_host)).get(chain)).put(source_ip, new HashMap());
        }
        if (!((HashMap) ((HashMap) ((HashMap) this.from_id_to_firewallChains.get(firewall_host)).get(chain)).get(source_ip)).containsKey(destination_ip)) {
            ((HashMap) ((HashMap) ((HashMap) this.from_id_to_firewallChains.get(firewall_host)).get(chain)).get(source_ip)).put(destination_ip, new ArrayList());
        }
        ((ArrayList) ((HashMap) ((HashMap) ((HashMap) this.from_id_to_firewallChains.get(firewall_host)).get(chain)).get(source_ip)).get(destination_ip)).add(firewallRule);
    }

    public HashMap<String, HashMap <String, ArrayList<FwRule>>> getChainFirewallRulesFromFirewall (String firewall, String chain) {
        if (this.from_id_to_firewallChains.containsKey(firewall)) {
            if (this.from_id_to_firewallChains.get(firewall).containsKey(chain)) {
                return this.from_id_to_firewallChains.get(firewall).get(chain);
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

    public void setIdForAddress(String addr, String deviceId) {
        this.from_addresses_to_id.put(addr, deviceId);
    }

    public void initializeStructureFromAddressesToPorts(String addr) {
        this.from_address_to_ports.put(addr, new HashMap());
        ((HashMap) this.from_address_to_ports.get(addr)).put("TCP", new ArrayList());
        ((HashMap) this.from_address_to_ports.get(addr)).put("UDP", new ArrayList());
    }

    public void putAddressIntoStructureFromIdToAddresses(String deviceId, String addr, NetworkInterface IPInterface) {
        ((HashMap) this.from_id_to_addresses.get(deviceId)).put(addr, IPInterface);
    }

    public void addPortFromAddress(String addr, Port p) {
        int port = p.getNumber();
        String protocol = p.getTransportProtocol();
        Service service = p.getService();
        List<String> cpeList = new ArrayList<>();
        if (service != null) {
            cpeList = service.getCpe();
        }
        ReachedPort rp = new ReachedPort(port, cpeList);
        ((ArrayList) ((HashMap) this.from_address_to_ports.get(addr)).get(protocol)).add(rp);
        try {
            Collections.sort((List) ((HashMap) this.from_address_to_ports.get(addr)).get(protocol));
        } catch (Exception e) {
            log.info("ECCOCI");
        }
    }

    public HashMap<String, ArrayList<ReachedPort>> getPortsFromAddress(String addr) {
        return this.from_address_to_ports.get(addr);
    }

    public void addInfoFromId(String deviceId, Device device_info) {
        this.from_id_to_info.put(deviceId, device_info);
    }

    public Device getInfoFromId(String deviceId) {
        return this.from_id_to_info.get(deviceId);
    }

    public void addMiddlebox(String host) {
        this.middleboxes.add(host);
    }

    public void addTerminal(String host) {
        this.terminals.add(host);
    }

    public ArrayList<String> getTerminals() {
        return this.terminals;
    }

    public ArrayList<String> getMiddleboxes() {
        return this.middleboxes;
    }

    public ArrayList<RoutingRule> getRoutesFromId(String deviceId) {
        return this.from_id_to_routes.getOrDefault(deviceId, new ArrayList<>());
    }

    public void addDestinationForMiddlebox(String middleboxe, String address_cidr) {
        if (!((HashMap) this.from_mbx_to_destinations.get(middleboxe)).containsKey(address_cidr)) {
            ((HashMap) this.from_mbx_to_destinations.get(middleboxe)).put(address_cidr, new ArrayList());
        }
    }

    public void addHopInDestinationForMiddlebox(String middleboxe, String address_cidr, String hop) {
//        System.out.println(hop);
        ((ArrayList)((HashMap)this.from_mbx_to_destinations.get(middleboxe)).get(address_cidr)).add(hop);
    }
    public void addHopSInDestinationForMiddlebox(String middleboxe, String net,  ArrayList<String> dest_hops) {
        this.from_mbx_to_destinations.get(middleboxe).put(net, dest_hops);
    }

    public void removeDestinationForMiddlebox(String middleboxe, String net) {
        this.from_mbx_to_destinations.get(middleboxe).remove(net);
    }

    public ArrayList getHopsFromDestinationForMiddlebox(String middleboxe, String address_cidr) {
        return (ArrayList) this.from_mbx_to_destinations.get(middleboxe).get(address_cidr).clone();
    }

    public HashMap<String, ArrayList<String>> getDestinationForMiddlebox(String middleboxe) {
        return this.from_mbx_to_destinations.get(middleboxe);
    }

    public String getIdFromAddress(String addr) {
        return (String) this.from_addresses_to_id.get(addr);
    }

    public HashMap<String, NetworkInterface> getNetworkInterfaceFromId(String deviceId) {
        return (HashMap) this.from_id_to_addresses.get(deviceId);
    }

    public String getInterfaceFromAddress(String addr) { return from_address_to_interface.get(addr); }

    public HashMap<String, ArrayList<ReachedPort>> getPortsFromOutputChain(String source, String destination, String firewall, StructureInventory st) {
        if (st.getChainFirewallRulesFromFirewall(st.from_addresses_to_id.get(source), "Output") != null) {
            HashMap<String, ArrayList<ReachedPort>> outputPorts = new HashMap();
            ArrayList<ReachedPort> tcpPorts = st.getPortsFromAddress(source).get("TCP");
            ArrayList<ReachedPort> udpPorts = st.getPortsFromAddress(source).get("UDP");
            ArrayList<ReachedPort> validTCPports = new ArrayList();
            ArrayList<ReachedPort> validUDPports = new ArrayList();
            for (String from : st.getChainFirewallRulesFromFirewall(firewall, "Output").keySet()) {
                for (String to : st.getChainFirewallRulesFromFirewall(firewall, "Output").get(from).keySet()) {
                    for (FwRule rule : st.getChainFirewallRulesFromFirewall(firewall, "Output").get(from).get(to)) {
                        int subnet_source = -1;
                        int subnet_dest = -1;
                        if (!rule.getSource().equalsIgnoreCase("any")) {
                            SubnetUtils fw_net_source = new SubnetUtils(rule.getSource(), rule.getSMask());
                            if (fw_net_source.getInfo().isInRange(source) || rule.getSource().equals(source)) {
                                subnet_source = 1;
                            }
                        }
                        if (!rule.getDestination().equalsIgnoreCase("any")) {
                            SubnetUtils fw_net_dest = new SubnetUtils(rule.getDestination(), rule.getDMask());
                            if (fw_net_dest.getInfo().isInRange(destination) || rule.getDestination().equals(destination)){
                                subnet_dest = 1;
                            }
                        }
                        if ((rule.getSource().equalsIgnoreCase("any") || subnet_source == 1) && (rule.getDestination().equalsIgnoreCase("any") || subnet_dest == 1)){
                            if (rule.getProtocol().equalsIgnoreCase("any")){
                                continue;
                            }
                            if (rule.getProtocol().equalsIgnoreCase("TCP")) {
                                String ports = rule.getDPorts();
                                String[] portsList = ports.split(",");
                                for (String port : portsList) {
                                    if (port.contains("-")) {
                                        String[] limits = port.split("-");
                                        int min = Integer.parseInt(limits[0]);
                                        int max = Integer.parseInt(limits[1]);
                                        for (ReachedPort p : tcpPorts) {
                                            if (p.getPort() > min && p.getPort() < max) {
                                                if (!validTCPports.contains(p)) {
                                                    validTCPports.add(p);
                                                }
                                            }
                                        }
                                    } else {
                                        for (ReachedPort p : tcpPorts) {
                                            if (port.equals(Integer.toString(p.getPort()))) {
                                                if (!validTCPports.contains(p)) {
                                                    validTCPports.add(p);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (rule.getProtocol().equalsIgnoreCase("UDP")) {
                                String ports = rule.getDPorts();
                                String[] portsList = ports.split(",");
                                for (String port : portsList) {
                                    if (port.contains("-")) {
                                        String[] limits = port.split("-");
                                        int min = Integer.parseInt(limits[0]);
                                        int max = Integer.parseInt(limits[1]);
                                        for (ReachedPort p : udpPorts) {
                                            if (p.getPort() > min && p.getPort() < max) {
                                                if (!validUDPports.contains(p)) {
                                                    validUDPports.add(p);
                                                }

                                            }
                                        }
                                    } else {
                                        for (ReachedPort p : udpPorts) {
                                            if (port.equals(Integer.toString(p.getPort()))) {
                                                if (!validUDPports.contains(p)) {
                                                    validUDPports.add(p);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            outputPorts.put("TCP", validTCPports);
            outputPorts.put("UDP", validUDPports);
            return outputPorts;
        } else {
            return null;
        }
    }

    public HashMap<String, ArrayList<ReachedPort>> getPortsFromInputChain(String source, String destination, String firewall, StructureInventory st, HashMap<String, ArrayList<ReachedPort>> forwardPorts) {
        HashMap<String, ArrayList<ReachedPort>> inputPorts = new HashMap();
        ArrayList<ReachedPort> tcpPorts = forwardPorts.get("TCP");
        ArrayList<ReachedPort> udpPorts = forwardPorts.get("UDP");
        ArrayList<ReachedPort> validTCPports = new ArrayList();
        ArrayList<ReachedPort> validUDPports = new ArrayList();
        if (tcpPorts.size() != 0 || udpPorts.size() != 0) {
            for (String from : st.getChainFirewallRulesFromFirewall(firewall, "Input").keySet()) {
                for (String to : st.getChainFirewallRulesFromFirewall(firewall, "Input").get(from).keySet()) {
                    for (FwRule rule : st.getChainFirewallRulesFromFirewall(firewall, "Input").get(from).get(to)) {
                        int subnet_source = -1;
                        int subnet_dest = -1;
                        if (!rule.getSource().equalsIgnoreCase("any")) {
                            SubnetUtils fw_net_source = new SubnetUtils(rule.getSource(), rule.getSMask());
                            if (fw_net_source.getInfo().isInRange(source) || rule.getSource().equals(source)) {
                                subnet_source = 1;
                            }
                        }
                        if (!rule.getDestination().equalsIgnoreCase("any")) {
                            SubnetUtils fw_net_dest = new SubnetUtils(rule.getDestination(), rule.getDMask());
                            if (fw_net_dest.getInfo().isInRange(destination) || rule.getDestination().equals(destination)) {
                                subnet_dest = 1;
                            }
                        }
                        if ((rule.getSource().equalsIgnoreCase("any") || subnet_source == 1) && (rule.getDestination().equalsIgnoreCase("any") || subnet_dest == 1)) {
                            if (rule.getProtocol().equalsIgnoreCase("any")) {
                                validTCPports = new ArrayList(tcpPorts);
                                validUDPports = new ArrayList(udpPorts);
                            }
                            if (rule.getProtocol().equalsIgnoreCase("TCP")) {
                                String ports = rule.getDPorts();
                                String[] portsList = ports.split(",");
                                for (String port : portsList) {
                                    if (port.contains("-")) {
                                        String[] limits = port.split("-");
                                        int min = Integer.parseInt(limits[0]);
                                        int max = Integer.parseInt(limits[1]);
                                        for (ReachedPort p : tcpPorts) {
                                            if (p.getPort() > min && p.getPort() < max) {
                                                if (!validTCPports.contains(p)) {
                                                    validTCPports.add(p);
                                                }
                                            }
                                        }
                                    } else {
                                        for (ReachedPort p : tcpPorts) {
                                            if (port.equals(Integer.toString(p.getPort()))) {
                                                if (!validTCPports.contains(p)) {
                                                    validTCPports.add(p);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (rule.getProtocol().equalsIgnoreCase("UDP")) {
                                String ports = rule.getDPorts();
                                String[] portsList = ports.split(",");
                                for (String port : portsList) {
                                    if (port.contains("-")) {
                                        String[] limits = port.split("-");
                                        int min = Integer.parseInt(limits[0]);
                                        int max = Integer.parseInt(limits[1]);
                                        for (ReachedPort p : udpPorts) {
                                            if (p.getPort() > min && p.getPort() < max) {
                                                if (!validUDPports.contains(p)) {
                                                    validUDPports.add(p);
                                                }
                                            }
                                        }
                                    } else {
                                        for (ReachedPort p : udpPorts) {
                                            if (port.equals(Integer.toString(p.getPort()))) {
                                                if (!validUDPports.contains(p)) {
                                                    validUDPports.add(p);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        inputPorts.put("TCP", validTCPports);
        inputPorts.put("UDP", validUDPports);
        return inputPorts;
    }

    public HashMap<String, ArrayList<ReachedPort>> getPortsFromForwardChain(String source, String destination, ArrayList<String> firewalls, StructureInventory st, HashMap<String, ArrayList<ReachedPort>> outputPorts) {
        HashMap<String, ArrayList<ReachedPort>> forwardPorts = new HashMap();
        ArrayList<ReachedPort> tcpPorts = outputPorts.get("TCP");
        ArrayList<ReachedPort> udpPorts = outputPorts.get("UDP");
        ArrayList<ReachedPort> validTCPports = new ArrayList();
        ArrayList<ReachedPort> validUDPports = new ArrayList();
        for (String fw : firewalls) {
            if (st.getChainFirewallRulesFromFirewall(fw, "Forward") != null) {
                if (tcpPorts.size() != 0 || udpPorts.size() != 0) {
                    for (String from : st.getChainFirewallRulesFromFirewall(fw, "Forward").keySet()) {
                        for (String to : st.getChainFirewallRulesFromFirewall(fw, "Forward").get(from).keySet()) {
                            for (FwRule rule : st.getChainFirewallRulesFromFirewall(fw, "Forward").get(from).get(to)) {
                                if (rule.getAction().equals("Deny")) {
                                    continue;
                                }
                                int subnet_source = -1;
                                int subnet_dest = -1;
                                if (!rule.getSource().equalsIgnoreCase("any")){
                                    SubnetUtils fw_net_source = new SubnetUtils(rule.getSource(), rule.getSMask());
                                    if (fw_net_source.getInfo().isInRange(source) || rule.getSource().equals(source)){
                                        subnet_source = 1;
                                    }
                                }
                                if (!rule.getDestination().equalsIgnoreCase("any")){
                                    SubnetUtils fw_net_dest = new SubnetUtils(rule.getDestination(), rule.getDMask());
                                    if (fw_net_dest.getInfo().isInRange(destination) || rule.getDestination().equals(destination)){
                                        subnet_dest = 1;
                                    }
                                }
                                if ((rule.getSource().equalsIgnoreCase("any") || subnet_source == 1) && (rule.getDestination().equalsIgnoreCase("any") || subnet_dest == 1)){
                                    if (rule.getProtocol().equalsIgnoreCase("any")){
                                        for (ReachedPort p : tcpPorts) {
                                            if (!validTCPports.contains(p)) {
                                                validTCPports.add(p);
                                            }
                                        }
                                        for (ReachedPort p : udpPorts) {
                                            if (!validUDPports.contains(p)) {
                                                validUDPports.add(p);
                                            }
                                        }
                                    }
                                    String ports = rule.getDPorts();
                                    if (rule.getProtocol().equalsIgnoreCase("TCP")) {
                                        String[] portsList = ports.split(",");
                                        for (String port : portsList) {
                                            if (port.contains("-")) {
                                                String[] limits = port.split("-");
                                                int min = Integer.parseInt(limits[0]);
                                                int max = Integer.parseInt(limits[1]);
                                                for (ReachedPort p : tcpPorts) {
                                                    if (p.getPort() > min && p.getPort() < max) {
                                                        if (!validTCPports.contains(p)) {
                                                            validTCPports.add(p);
                                                        }

                                                    }
                                                }
                                            } else {
                                                for (ReachedPort p : tcpPorts) {
                                                    if (port.equals(Integer.toString(p.getPort()))) {
                                                        if (!validTCPports.contains(p)) {
                                                            validTCPports.add(p);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    if (rule.getProtocol().equalsIgnoreCase("UDP")) {
                                        String[] portsList = ports.split(",");
                                        for (String port : portsList) {
                                            if (port.contains("-")) {
                                                String[] limits = port.split("-");
                                                int min = Integer.parseInt(limits[0]);
                                                int max = Integer.parseInt(limits[1]);
                                                for (ReachedPort p : udpPorts) {
                                                    if (p.getPort() > min && p.getPort() < max) {
                                                        if (!validUDPports.contains(p)) {
                                                            validUDPports.add(p);
                                                        }

                                                    }
                                                }
                                            } else {
                                                for (ReachedPort p : udpPorts) {
                                                    if (port.equals(Integer.toString(p.getPort()))) {
                                                        if (!validUDPports.contains(p)) {
                                                            validUDPports.add(p);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tcpPorts = new ArrayList(validTCPports);
                udpPorts = new ArrayList(validUDPports);
            }
        }
        forwardPorts.put("TCP", tcpPorts);
        forwardPorts.put("UDP", udpPorts);
        return forwardPorts;
    }


    public boolean isMiddlebox(String type) {
        if (type.equalsIgnoreCase("firewall") || type.equalsIgnoreCase("router") || type.equalsIgnoreCase("server") || type.equalsIgnoreCase("switch")) {
            return true;
        } else {
            return false;
        }
    }
}
