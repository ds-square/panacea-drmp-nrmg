package org.panacea.drmp.nrmg;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;
import org.panacea.drmp.nrmg.domain.device.*;
import org.panacea.drmp.nrmg.domain.notifications.DataNotification;
import org.panacea.drmp.nrmg.domain.policy.*;
import org.panacea.drmp.nrmg.domain.reachability.ReachabilityInventory;
import org.panacea.drmp.nrmg.domain.reachability.SourceDevice;
import org.panacea.drmp.nrmg.domain.reachability.StructureInventory;
import org.panacea.drmp.nrmg.exception.NRMGException;
import org.panacea.drmp.nrmg.service.NRMGInputRequestService;
import org.panacea.drmp.nrmg.service.NRMGPostOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NRMGenerator {
    @Autowired
    NRMGInputRequestService nrmgInputRequestService;

    @Autowired
    NRMGPostOutputService nrmgPostOutputService;

    private DeviceInventory deviceInventory;
    private PolicyInventory policyInventory;
    //    private ReachabilityInventory reachabilityInventory;

    @Synchronized
    public void generateNRMG(DataNotification notification) {
        try {
            this.getInput(notification.getSnapshotId());
        } catch (NRMGException e) {
            log.error(e.getMessage());
        }
        long ni_time = (new Date()).getTime();
        StructureInventory data_inventory = new StructureInventory();
        String lanID = "1";
        for (Device d : deviceInventory.getDevices()) {
            String deviceId = d.getId();
//            data_inventory.from_hostname_to_id.put(hostname, deviceId);
//            data_inventory.from_id_to_hostname.put(deviceId, hostname);
            // FIX Try to not create a second device
            List<NetworkInterface> interfaces = d.getNetworkInterfaces();
//            ArrayList<NetworkInterface> interfaces_json = new ArrayList<NetworkInterface>();
            data_inventory.initializeStructureFromIdToAddresses(deviceId);
            for (NetworkInterface networkInterface : interfaces) {
                if (networkInterface.getIPAddress() != null) {
                    String interfaceName = networkInterface.getName();
                    String macAddress = networkInterface.getMacAddress();
                    String ipAddress = networkInterface.getIPAddress();
                    String version = networkInterface.getVersion();
                    String mask = networkInterface.getMask();
                    networkInterface.setReachedDevices(new ArrayList());
                    data_inventory.from_address_to_interface.put(ipAddress, interfaceName);
                    data_inventory.setIdForAddress(ipAddress, deviceId);
                    data_inventory.initializeStructureFromAddressesToPorts(ipAddress);
                    data_inventory.putAddressIntoStructureFromIdToAddresses(deviceId, ipAddress, networkInterface);
                    SubnetUtils netUtils = new SubnetUtils(ipAddress, mask);
                    String network = netUtils.getInfo().getNetworkAddress();
                    String cidr_orig = netUtils.getInfo().getCidrSignature();
                    String[] mask_cidr = cidr_orig.split("/");
                    String cidr = network + "/" + mask_cidr[1];
                    SubnetUtils netUtils2 = new SubnetUtils(network, mask);
                    data_inventory.from_cidr_to_subnetUtils.put(cidr, netUtils2);
                    data_inventory.from_ip_to_cidrLan.put(ipAddress, cidr);
                    if (!data_inventory.from_lan_to_id.containsKey(cidr)) {
                        data_inventory.from_lan_to_id.put(cidr, lanID);
                        int nId = Integer.parseInt(lanID);
                        nId = nId + 1;
                        lanID = String.valueOf(nId);
                    }
                    data_inventory.initializeStructureFromCIDRToAddresses(cidr);
                    data_inventory.addAddressToCIDR(cidr, ipAddress);
                    for (Port p : networkInterface.getPorts()) {
                        data_inventory.addPortFromAddress(ipAddress, p);
                    }
                }
            }
            String type = d.getType();
            if (data_inventory.isMiddlebox(type)) {
                data_inventory.addMiddlebox(deviceId);
            } else {
                data_inventory.addTerminal(deviceId);
            }
            OperatingSystem os = d.getOperatingSystem();
            if (os != null) {
                List<String> vulnList = os.getOsVulnerabilities();
                if (vulnList != null) {
                    for (String v : vulnList) {
                        d.addSingleVulnerability(v);
                    }
                }
            }
            data_inventory.addInfoFromId(deviceId, d);
        }
        long comp_t = (new Date()).getTime();

        //Here I read all the routing rules and create the structures: from_hostname_to_routes
        for (RoutingPolicy rp : policyInventory.getRoutingPolicies()) {
            String deviceId = rp.getDeviceId();
            data_inventory.initializeStructureFromIdToRoutes(deviceId);
            data_inventory.initializeStructureFromHostToDefaults(deviceId);
            data_inventory.initializeStructureFromHostToDirects(deviceId);
            data_inventory.initializeStructureFromIdToStatics(deviceId);
            for (RoutingRule rule : rp.getRoutingRules()) {
                if (rule.getGateway().equals("*")) {
//                    data_inventory.from_host_to_directs.get(hostname).add(new Route(destination, gateway, mask, metric, iface));
                    data_inventory.from_id_to_directs_routing_rule.get(deviceId).add(rule);
                }
                if (!rule.getGateway().equals("*") && (!rule.getDestination().equals("default"))) {
                    data_inventory.from_id_to_statics.get(deviceId).add(rule);
                }
                if (rule.getDestination().equals("default")) {
                    data_inventory.from_id_to_defaults.get(deviceId).add(rule);
                }
                if (!rule.getDestination().equals("default") && !rule.getGateway().equals("*")) {
                    SubnetUtils utils = new SubnetUtils(rule.getDestination(), rule.getMask());
                    data_inventory.from_cidr_to_subnetUtils.put(utils.getInfo().getCidrSignature(), utils);
                }
                data_inventory.addRoute(deviceId, rule);
            }
        }

        //Here I read all the firewall rules and create the structures: from_hostname_to_firewallChains
        for (FirewallPolicy fwPolicy : policyInventory.getFirewallPolicies()) {
            String firewallId = fwPolicy.getDeviceId();
            for (FirewallChain fwChain : fwPolicy.getFirewallChains()) {
                String type = fwChain.getType();
                List<FwRule> fwRuleList = fwChain.getFwRules();
                if (fwRuleList.size() == 0) {
                    FwRule fwRule = null;
                    String defaultPolicy = fwChain.getDefaultPolicy().toLowerCase();
                    switch (defaultPolicy) {
                        case "deny":
                            fwRule = new FwRule("Deny", "any", "any", "any", "any", "any", "any", "any", "any", "any", "any", "any");
                            break;
                        case "accept":
                            fwRule = new FwRule("Allow", "any", "any", "any", "any", "any", "any", "any", "any", "any", "any", "any");
                            break;
                        default:
                    }
                    data_inventory.addFirewallRule(firewallId, type, fwRule.getSource(), fwRule.getDestination(), fwRule);
                }
                for (FwRule fwRule : fwChain.getFwRules().stream().sorted().collect(Collectors.toList())) {
                    data_inventory.addFirewallRule(firewallId, type, fwRule.getSource(), fwRule.getDestination(), fwRule);
                }
            }
        }
        long acl_time = (new Date()).getTime();
        log.info("Time to read Network Inventory: " + (comp_t - ni_time) + "ms");
        log.info("Time to read ACL Rules: " + (acl_time - comp_t) + "ms");
        ComputeReachability reachability = new ComputeReachability();
        reachability.computeReachabilityMbxToLAN(data_inventory);
        long reach_mbx = (new Date()).getTime();
        log.info("Time to compute Reachability Middleboxes to LANs: " + (reach_mbx - acl_time) + "ms");
        reachability.checkAndCleanReachabilityMbxToLans(data_inventory);
        long reach_firewall = (new Date()).getTime();
        ArrayList<SourceDevice> sourceDevices = new ArrayList<>();
        reachability.computeReachabilityHostToHost(data_inventory, sourceDevices);
        long reach_host_to_host = (new Date()).getTime();
        log.info("Time to compute Reachability Firewalls Analysis: " + (reach_host_to_host - reach_firewall) + "ms");
        log.info("Time to compute Total Reachability Matrix Generator: " + (reach_host_to_host - ni_time) + "ms");
        ReachabilityInventory inventory = new ReachabilityInventory(notification.getEnvironment(), notification.getSnapshotId(), notification.getSnapshotTime(), sourceDevices);
        nrmgPostOutputService.postReachabilityInventory(inventory);
    }


    private void getInput(String version) {
        // get input data from REST service
        this.deviceInventory = nrmgInputRequestService.performDeviceInventoryRequest(version);
        this.policyInventory = nrmgInputRequestService.performPolicyInventoryRequest(version);

//        log.info(deviceInventory.toString());
//        log.info(policyInventory.toString());
    }
}
