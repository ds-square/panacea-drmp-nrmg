package org.panacea.drmp.nrmg;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.SubnetUtils;
import org.panacea.drmp.nrmg.domain.device.Device;
import org.panacea.drmp.nrmg.domain.device.NetworkInterface;
import org.panacea.drmp.nrmg.domain.policy.RoutingRule;
import org.panacea.drmp.nrmg.domain.reachability.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
public class ComputeReachability {
    private StructureInventory data_inventory;

    public ComputeReachability() {

    }

    private static void computeStrict(String host_orig, SubnetUtils dest_orig, String gw_orig, ArrayList<String> checked_hops, StructureInventory st, String next_hop){
        boolean add_hop = false;
        boolean dest_checked = false;
        if (!st.getDestinationForMiddlebox(host_orig).containsKey(dest_orig)) {
            HashMap<String, ArrayList<RoutingRule>> rules = new HashMap();
            rules.put("strict", new ArrayList<>());
            rules.put("default", new ArrayList<>());
            if (!checked_hops.contains(next_hop)) {
                for (RoutingRule route : st.getRoutesFromId(next_hop)) { // first check static rules
                    if (!route.getGateway().equals("*") && !route.getDestination().equals("default")) {
                        SubnetUtils netUtils = new SubnetUtils(route.getDestination(), route.getMask());
                        if (dest_orig.getInfo().getNetworkAddress().equals(route.getDestination()) || netUtils.getInfo().isInRange(dest_orig.getInfo().getNetworkAddress())) {
                            rules.get("strict").add(route);
                            add_hop = true;
                        }
                    }
                    if (route.getDestination().equals("default")) {
                        rules.get("default").add(route);
                    }
                }
                HashMap<String, NetworkInterface> net_ip_map = st.getNetworkInterfaceFromId(next_hop);
                if (net_ip_map == null) {
                    net_ip_map = new HashMap<>();
                }
                for (String net_ip : net_ip_map.keySet()) { //check directs
                    NetworkInterface net = st.getNetworkInterfaceFromId(next_hop).get(net_ip);
                    SubnetUtils netUtils = new SubnetUtils(net.getIPAddress(), net.getMask());
                    String net_addr = dest_orig.getInfo().getNetworkAddress();
                    String ipAddr = net.getIPAddress();
                    String interface_net = netUtils.getInfo().getNetworkAddress();
                    if (dest_orig.getInfo().getNetworkAddress().equals(net.getIPAddress()) || dest_orig.getInfo().isInRange(net.getIPAddress()) || netUtils.getInfo().isInRange(net_addr)) {
                        if (!add_hop) {
                            String hop = st.getInterfaceFromAddress(gw_orig) + ":" + next_hop + ":" + st.getInterfaceFromAddress(net_ip);
                            st.addHopInDestinationForMiddlebox(host_orig, dest_orig.getInfo().getCidrSignature(), hop);
                            dest_checked = true;
                            return;
                        }
                    }
                }
                if (rules.get("strict").size() > 0) {
                    for (RoutingRule route : rules.get("strict")) {
                        checked_hops.add(next_hop);
                        String hop = st.getInterfaceFromAddress(gw_orig) + ":" + next_hop + ":" + route.getIface();
                        st.addHopInDestinationForMiddlebox(host_orig, dest_orig.getInfo().getCidrSignature(), hop);
                        computeStrict(host_orig, dest_orig, route.getGateway(), checked_hops, st, st.getIdFromAddress(route.getGateway()));
                        checked_hops.remove(next_hop);
                    }
                } else {
                    if (rules.get("default").size() > 0) {
                        for (RoutingRule route : rules.get("default")) {
                            checked_hops.add(next_hop);
                            String hop = st.getInterfaceFromAddress(gw_orig) + ":" + next_hop + ":" + route.getIface();
                            st.addHopInDestinationForMiddlebox(host_orig, dest_orig.getInfo().getCidrSignature(), hop);
                            computeStrict(host_orig, dest_orig, route.getGateway(), checked_hops, st, st.getIdFromAddress(route.getGateway()));
                            checked_hops.remove(next_hop);
                        }
                    }
                }
            }
        }
    }

    private static void computeDefault(String host_orig, ArrayList<String> own_nets, HashMap<String, ArrayList<String>> found_nets, String gw_orig, ArrayList<String> checked_hops, StructureInventory st, String next_hop, ArrayList<String> hops){
        if(!checked_hops.contains(next_hop)){
            checked_hops.add(next_hop);
            if (st.getDestinationForMiddlebox(next_hop) == null) { // next destination not already computed
                for (RoutingRule route : st.getRoutesFromId(next_hop)) {
                    if (route.getGateway().equals("*")) { // direct lans in this hop
                        SubnetUtils netUtils = new SubnetUtils(route.getDestination(), route.getMask());
                        String ss = netUtils.getInfo().getCidrSignature();
                        if (st.getDestinationForMiddlebox(host_orig).containsKey(netUtils.getInfo().getCidrSignature())) {
                            continue;
                        } else {
                            String hop = st.from_address_to_interface.get(gw_orig) + ":" + next_hop + ":" + route.getIface();
                            hops.add(hop);
                            st.addDestinationForMiddlebox(host_orig, netUtils.getInfo().getCidrSignature());
                            for (String h : hops) {
                                st.addHopInDestinationForMiddlebox(host_orig, netUtils.getInfo().getCidrSignature(), h);
                            }
                            hops.remove(hop);
                        }
                    } else if (route.getDestination().equals("default") && !route.getGateway().equals("*")) { // go to the next hop
                        String hop = st.from_address_to_interface.get(gw_orig) + ":" + next_hop + ":" + route.getIface();
                        hops.add(hop);
                        computeDefault(host_orig, own_nets, found_nets, route.getGateway(), checked_hops, st, st.getIdFromAddress(route.getGateway()), hops);
                        hops.remove(hop);
                    } else {
                        boolean already_seen = false;
                        SubnetUtils netUtils = new SubnetUtils(route.getDestination(), route.getMask());
                        for (String strict_seen : st.getDestinationForMiddlebox(host_orig).keySet()) {
                            if (netUtils.getInfo().getCidrSignature().equals(strict_seen) || st.getDestinationForMiddlebox(host_orig).containsValue(route.getDestination())) {
                                already_seen = true;
                                break;
                            }
                        }
                        if (!already_seen){
                            String hop = st.from_address_to_interface.get(gw_orig) + ":" + next_hop + ":" + route.getIface();
                            hops.add(hop);
                            st.addDestinationForMiddlebox(host_orig, netUtils.getInfo().getCidrSignature());
                            for (String h : hops){
                                st.addHopInDestinationForMiddlebox(host_orig, netUtils.getInfo().getCidrSignature(), h);
                            }
                            computeStrict(host_orig, netUtils, route.getGateway(), checked_hops, st, st.getIdFromAddress(route.getGateway()));
                            checked_hops.remove(next_hop);
                            hops.remove(hop);
                        }
                    }
                }
            }else{ //the next hop is a middlebox already computed
                Set<String> lans_orig = st.getDestinationForMiddlebox(host_orig).keySet();
                Set<String> lans_hop = st.getDestinationForMiddlebox(next_hop).keySet();
                HashMap<String, ArrayList<String>> to_add = new HashMap();
                for (String hop_dest : lans_hop){
                    boolean found = false;
                    ArrayList<String> dest_to_add = new ArrayList();
                    for (String orig_dest : lans_orig) {
                        SubnetUtils util_hop = st.from_cidr_to_subnetUtils.get(hop_dest);
                        SubnetUtils util_orig = st.from_cidr_to_subnetUtils.get(orig_dest);
                        if (orig_dest.equals(hop_dest)) {
                            found = true;
                        } else {
                            if (util_hop == null || util_orig == null) {
                                continue;
                            } else if (util_hop.getInfo().isInRange(util_orig.getInfo().getNetworkAddress())) {
                                found = true;
                            }
                        }
                    }
                    if (found == false){
                        ArrayList<String> hops_to_add = st.getHopsFromDestinationForMiddlebox(next_hop, hop_dest);
                        for (String h: hops){ //add the first interface from orig host
                            dest_to_add.add(h);
                        }
                        for (int i=0; i < hops_to_add.size(); i++) {
                            if (i == 0) {
                                String upd = st.from_address_to_interface.get(gw_orig) + ":" + hops_to_add.get(i);
                                dest_to_add.add(upd);
                            } else {
                                dest_to_add.add(hops_to_add.get(i));
                            }
                        }
                        to_add.put(hop_dest, dest_to_add);
                    }
                }
                if (to_add.size() > 0) {
                    for (String net : to_add.keySet()) {
                        st.addHopSInDestinationForMiddlebox(host_orig, net, to_add.get(net));
                    }
                }
            }
        }
    }

    public void computeReachabilityMbxToLAN(StructureInventory st) {
        for (String hostname : st.getMiddleboxes()) {
            ArrayList<RoutingRule> stricts = new ArrayList<RoutingRule>();
            ArrayList<RoutingRule> directs = new ArrayList<RoutingRule>();
            ArrayList<RoutingRule> defaults = new ArrayList<RoutingRule>();
            st.initializeStructureFromMiddleboxesToDestinations(hostname);
            List<RoutingRule> routingRulesList = st.getRoutesFromId(hostname);
            for (RoutingRule route : routingRulesList) {
                if (route.getGateway().equals("*")) {
                    directs.add(route);
                }
                if (!route.getGateway().equals("*") && (!route.getDestination().equals("default"))) {
                    stricts.add(route);
                }
                if (route.getDestination().equals("default")) {
                    defaults.add(route);
                }
            }
            st.initializeStructureFromIdToDirects(hostname);
            for (RoutingRule direct : directs) {
                String dest = direct.getDestination();
                String mask = direct.getMask();
                SubnetUtils netUtils = new SubnetUtils(dest, mask);
                String cidr_net = netUtils.getInfo().getCidrSignature();
                String first_hop_mbx = hostname + ":" + direct.getIface();
                st.addDestinationForMiddlebox(hostname, cidr_net);
                st.addHopInDestinationForMiddlebox(hostname, cidr_net, first_hop_mbx);
                st.addDirectToId(hostname, cidr_net);
            }
            for (RoutingRule strict : stricts) {
                String dest_orig = strict.getDestination();
                String gw_orig = strict.getGateway();
                String mask_orig = strict.getMask();
                SubnetUtils netUtils = new SubnetUtils(dest_orig, mask_orig);
                String cidr_net = netUtils.getInfo().getCidrSignature();
                String first_hop_mbx = hostname + ":" + strict.getIface();
                st.addDestinationForMiddlebox(hostname, cidr_net);
                st.addHopInDestinationForMiddlebox(hostname, cidr_net, first_hop_mbx);
                ArrayList<String> checked_hops = new ArrayList();
                checked_hops.add(hostname);
                String next_hop = st.getIdFromAddress(gw_orig);
                computeStrict(hostname, netUtils, gw_orig, checked_hops, st, next_hop);
            }
            for (RoutingRule defaul : defaults) {
                ArrayList<String> own_nets = st.getDirectsFromId(hostname);
                HashMap<String, ArrayList<String>> found_nets = new HashMap<>();
                String gw_orig = defaul.getGateway();
                String mask_orig = defaul.getMask();
                String first_hop_mbx = hostname + ":" + defaul.getIface();
                ArrayList<String> checked_hops = new ArrayList();
                checked_hops.add(hostname);
                String next_hop = st.getIdFromAddress(gw_orig);
                ArrayList<String> hops = new ArrayList();
                hops.add(first_hop_mbx);
                if (next_hop != null) {
                    computeDefault(hostname, own_nets, found_nets, gw_orig, checked_hops, st, next_hop, hops);
                }
            }
        }

    }

    public void checkAndCleanReachabilityMbxToLans(StructureInventory st) { //check those cases when a LAN is in the list but the path is not correct (missing routing rules)
        ArrayList<String> del_lans = new ArrayList();
        for (String mbx : st.getFromMbxToDestination().keySet()) {
            for (String lan : st.getFromMbxToDestination().get(mbx).keySet()) {
                boolean found = false;
                ArrayList<String> path = st.getFromMbxToDestination().get(mbx).get(lan);
                String mbx_ifaces = path.get(path.size() - 1);
                String[] arr_prov = mbx_ifaces.split(":");
                String last_mbx = "";
                if (arr_prov.length >= 3) {
                    last_mbx = arr_prov[1];
                } else if (arr_prov.length == 2) {
                    last_mbx = arr_prov[0];
                }
                Device last = st.getInfoFromId(last_mbx);
                SubnetUtils netUtils = st.from_cidr_to_subnetUtils.get(lan);
                if (last == null) {
                    continue;
                }
                for (NetworkInterface eth : last.getNetworkInterfaces()) {
                    SubnetUtils netUtils_eth = new SubnetUtils(eth.getIPAddress(), eth.getMask());
                    if (lan.contains("/32")) { //is a host in the direct connected lan of the last mbx
                        String ip = lan.split("/")[0];
                        if (netUtils_eth.getInfo().isInRange(ip)) {
                            found = true;
                            break;
                        }
                    }
                    if (netUtils != null) {
                        if (netUtils.getInfo().isInRange(eth.getIPAddress())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found == false){
                    del_lans.add(mbx + ":" + lan);
                }
            }
        }
        for (String l : del_lans){
            String[] prov = l.split(":");
            st.removeDestinationForMiddlebox(prov[0], prov[1]);
        }
    }


    public void computeReachabilityHostToHost(StructureInventory st, List<SourceDevice> sourceDevices) {
        for (String dev : st.from_id_to_info.keySet()) {
            Device device = st.from_id_to_info.get(dev);
            SourceDevice sourceDevice = new SourceDevice();
            sourceDevice.setHostName(device.getHostName());
            sourceDevice.setId(dev);
            List<ReachedInterface> ifaces = new ArrayList();
            HashMap<String, List<ReachedInterface>> listIfaces = new HashMap();
            for (NetworkInterface eth : device.getNetworkInterfaces()) {
                ReachedInterface iface = new ReachedInterface();
                iface.setAddress(eth.getIPAddress());
                iface.setLanID(st.from_lan_to_id.get(st.from_ip_to_cidrLan.get(eth.getIPAddress())));
                iface.setName(eth.getName());
                ArrayList<ReachedDevice> reached_nodes = new ArrayList();
                if (st.isMiddlebox(device.getType())) { //computes reach host to host for middlebox
                    for (String lan : st.getDestinationForMiddlebox(dev).keySet()) {
//                        if(dev.equals("POCT1")){
//                            log.info("DEBUG");
//                        }
                        ArrayList<String> path = st.getHopsFromDestinationForMiddlebox(dev, lan);
                        String first_iface = path.get(0).split(":")[1];
                        if (eth.getName().equals(first_iface)) {
                            ArrayList<String> nodes = st.from_cidr_to_addresses.get(lan);
                            if (lan.contains("/32")) {
                                nodes = new ArrayList();
                                nodes.add(lan.split("/")[0]);
                            }
                            for (String node : nodes) {
                                String deviceIdReach = st.getIdFromAddress(node);
                                ReachedDevice destReached = new ReachedDevice();
                                destReached.setAddress(node);
                                destReached.setIfaceName(st.from_address_to_interface.get(node));
                                destReached.setLanID(st.from_lan_to_id.get(st.from_ip_to_cidrLan.get(node)));
                                destReached.setHostName(st.from_id_to_info.get(deviceIdReach).getHostName());
                                destReached.setDeviceId(deviceIdReach);
                                String lastIface = "";
                                String last_hop = "";
                                if (path.size() > 1) {
                                    lastIface = path.get(path.size() - 1).split(":")[2];
                                    last_hop = path.get(path.size() - 1).split(":")[1];
                                } else {
                                    lastIface = path.get(path.size() - 1).split(":")[1];
                                    last_hop = path.get(path.size() - 1).split(":")[0];
                                }
                                ArrayList<String> pathForFirewall;
                                if (lastIface.equals(st.from_address_to_interface.get(node)) && path.size() > 1 && last_hop.equals(st.getIdFromAddress(node))) {
                                    ArrayList<String> final_path = new ArrayList(path);
                                    destReached.setPath(final_path);
                                    pathForFirewall = new ArrayList(final_path);
                                } else {
                                    String newHop = st.from_address_to_interface.get(node) + ":" + deviceIdReach;
                                    ArrayList<String> final_path = new ArrayList(path);
                                    final_path.add(newHop);
                                    destReached.setPath(final_path);
                                    pathForFirewall = new ArrayList(final_path);
                                }
                                ArrayList<ReachedPort> ports = new ArrayList();
                                ArrayList<String> listFws = new ArrayList();
                                int two_ways = 0;
                                List<String> firstPath = new ArrayList();
                                List<String> secondPath = new ArrayList();
                                int index_1 = -1;
                                int index_2 = -1;
                                for (int i = 0; i < pathForFirewall.size(); i ++){
                                    if (pathForFirewall.get(i).contains(dev) && !dev.equals(deviceIdReach)) {
                                        two_ways++;
                                        if (index_1 == -1) {
                                            index_1 = i;
                                        } else {
                                            index_2 = i;
                                        }
                                    }
                                }
                                if (two_ways == 2){
                                    firstPath = pathForFirewall.subList(index_1, index_2);
                                    secondPath = pathForFirewall.subList(index_2, pathForFirewall.size());
                                    String iface1 = firstPath.get(0).split(":")[1];
                                    String iface2 = secondPath.get(0).split(":")[1];
                                    if (iface1.equals(eth.getName())){
                                        pathForFirewall = new ArrayList(firstPath);
                                    } else {
                                        pathForFirewall = new ArrayList(secondPath);
                                    }
                                }
                                for (int i = 0; i < pathForFirewall.size(); i++) {
                                    if (!(i == 0 || i == pathForFirewall.size() - 1)) {
                                        String[] fw = pathForFirewall.get(i).split(":");
//                                        if(dev.equals("PC1")){
//                                            System.out.println("PC1");
//                                        }
                                        listFws.add(fw[1]);
                                    }
                                }
                                HashMap<String, ArrayList<ReachedPort>> outputPorts = st.getPortsFromOutputChain(eth.getIPAddress(), node, dev, st);
                                HashMap<String, ArrayList<ReachedPort>> inputPorts;
                                if (outputPorts != null) {
                                    inputPorts = new HashMap(outputPorts);
                                } else {
                                    inputPorts = new HashMap(st.getPortsFromAddress(node));
                                    outputPorts = new HashMap(st.getPortsFromAddress(node));
                                }
                                if (listFws.size() > 0) {
                                    HashMap<String, ArrayList<ReachedPort>> forwardPorts = st.getPortsFromForwardChain(eth.getIPAddress(), node, listFws, st, outputPorts);
                                    if (st.getChainFirewallRulesFromFirewall(deviceIdReach, "Input") != null) {
                                        inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, deviceIdReach, st, forwardPorts);
                                    } else {
                                        //TODO FIX FORWARD PORT REPORT ON NRMG
//                                        if(dev.equals("PC1")){
//                                            System.out.println("PC1");
//                                        }
                                        inputPorts = forwardPorts;
                                    }
                                } else {
                                    if (st.getChainFirewallRulesFromFirewall(deviceIdReach, "Input") != null) {
                                        inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, deviceIdReach, st, outputPorts);
                                    }
                                }
                                ArrayList<ReachedPort> tcpPorts = inputPorts.get("TCP");
                                ArrayList<ReachedPort> udpPorts = inputPorts.get("UDP");
                                if (tcpPorts.size() > 0) {
                                    for (ReachedPort p : tcpPorts) {
                                        p.setProtocol("TCP");
                                        ports.add(p);
                                    }
                                }
                                if (udpPorts.size() > 0) {
                                    for (ReachedPort p : udpPorts) {
                                        p.setProtocol("UDP");
                                        ports.add(p);
                                    }
                                }
                                destReached.setReachedPorts(ports);
                                if (!(ports.size() == 0)){
                                    reached_nodes.add(destReached);
                                }
                                //}
                            }
                        }
                    }
                }else { //computes reach host to host for terminal
                    ArrayList<String> noDuplicatesLAN = new ArrayList();
                    List<RoutingRule> staticList = st.from_id_to_statics.get(dev);
                    staticList = staticList == null ? new ArrayList<RoutingRule>() : staticList;
                    for (RoutingRule route : staticList) { // check static routes (DORETE)
                        String next_hop = st.getIdFromAddress(route.getGateway());
                        SubnetUtils netUtils = new SubnetUtils(route.getDestination(), route.getMask());
                        String cidr_net = netUtils.getInfo().getCidrSignature();
                        noDuplicatesLAN.add(cidr_net);
                        boolean find_static = false;
                        if (eth.getName().equals(route.getIface())) {
                            if (st.getDestinationForMiddlebox(next_hop) != null) {
                                for (String lan : st.getDestinationForMiddlebox(next_hop).keySet()) {
                                    if (cidr_net.equals(lan) || st.from_cidr_to_subnetUtils.get(lan).getInfo().isInRange(route.getDestination())) {
                                        find_static = true;
                                    }
                                }
                            }
                            if (find_static) {
                                ArrayList<String> path_terminal = new ArrayList();
                                path_terminal.add(dev + ":" + eth.getName());
                                ArrayList<String> path_mbx = st.getHopsFromDestinationForMiddlebox(next_hop, cidr_net);
                                String first_eth_mbx = st.from_address_to_interface.get(route.getGateway()) + ":" + path_mbx.get(0);
                                path_mbx.set(0, first_eth_mbx);
                                path_terminal.addAll(path_mbx);
                                ArrayList<String> nodes = st.from_cidr_to_addresses.get(cidr_net);
                                for (String node : nodes) {
//                                    if(dev.equals("PC2") && node.equals("192.168.1.207")){
//                                        log.info("CHECK");
//                                    }
                                    String reachedDeviceId = st.getIdFromAddress(node);
                                    ReachedDevice destReached = new ReachedDevice();
                                    destReached.setAddress(node);
                                    destReached.setIfaceName(st.from_address_to_interface.get(node));
                                    destReached.setLanID(st.from_lan_to_id.get(st.from_ip_to_cidrLan.get(node)));
                                    destReached.setHostName(st.from_id_to_info.get(reachedDeviceId).getHostName());
                                    destReached.setDeviceId(reachedDeviceId);
                                    boolean iface_inside_path = false;
                                    //FIX EXTENDED CHECK WITH HOSTNAME AND INTERFACENAME
                                    if (st.isMiddlebox(st.getInfoFromId(reachedDeviceId).getType())) {
                                        if (path_terminal.get(path_terminal.size() - 1).contains(st.from_address_to_interface.get(node) + ":" + reachedDeviceId)) {
                                            iface_inside_path = true;
                                        }
                                    }
                                    ArrayList<String> final_path = new ArrayList();
                                    if (!iface_inside_path) {
                                        final_path.addAll(path_terminal);
                                        final_path.add(st.from_address_to_interface.get(node) + ":" + reachedDeviceId);
                                    } else {
                                        final_path.addAll(path_terminal);
                                    }
                                    destReached.setPath(final_path);
                                    ArrayList<String> listFws = new ArrayList();
                                    for (int i = 0; i < final_path.size(); i++) {
                                        if (!(i == 0 || i == final_path.size() - 1)) {
                                            String[] fw = final_path.get(i).split(":");
//                                            if(dev.equals("PC1")){
//                                                log.info("PC1");
//                                            }
                                            listFws.add(fw[1]);
                                        }
                                    }
                                    ArrayList<ReachedPort> ports = new ArrayList();
                                    HashMap<String, ArrayList<ReachedPort>> outputPorts = st.getPortsFromOutputChain(eth.getIPAddress(), node, dev, st);
                                    HashMap<String, ArrayList<ReachedPort>> inputPorts;
                                    // flow < OuputPorts -> ForwardPorts -> InputPorts >
                                    if (outputPorts != null) {
                                        inputPorts = new HashMap(outputPorts);
                                    } else {
                                        inputPorts = new HashMap(st.getPortsFromAddress(node));
                                        outputPorts = new HashMap(st.getPortsFromAddress(node));
                                    }
                                    if (listFws.size() > 0) {
//                                        if(dev.equals("PC1")){
//                                            log.info("PC1");
//                                        }
                                        HashMap<String, ArrayList<ReachedPort>> forwardPorts = st.getPortsFromForwardChain(eth.getIPAddress(), node, listFws, st, outputPorts);
                                        if (st.getChainFirewallRulesFromFirewall(reachedDeviceId, "Input") != null) {
                                            inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, reachedDeviceId, st, forwardPorts);
                                        } else {
                                            //FIX ADDED ALSO HERE, NEED TO TEST
//                                            log.info("FIX ADDED AFTER EMPTY RULE PROBLEM, NEED TEST - 1");
                                            inputPorts = forwardPorts;
                                        }
                                    } else {
                                        if (st.getChainFirewallRulesFromFirewall(reachedDeviceId, "Input") != null) {
                                            inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, reachedDeviceId, st, outputPorts);
                                        }
                                    }
                                    ArrayList<ReachedPort> tcpPorts = inputPorts.get("TCP");
                                    ArrayList<ReachedPort> udpPorts = inputPorts.get("UDP");
                                    if (tcpPorts.size() > 0) {
                                        for (ReachedPort p : tcpPorts) {
                                            ReachedPort tcpPort = new ReachedPort();
                                            tcpPort.setProtocol("TCP");
                                            tcpPort.setPort(p.getPort());
                                            tcpPort.setService(p.getService());
                                            ports.add(tcpPort);
                                        }
                                    }
                                    if (udpPorts.size() > 0) {
                                        for (ReachedPort p : udpPorts) {
                                            ReachedPort udpPort = new ReachedPort();
                                            udpPort.setProtocol("UDP");
                                            udpPort.setPort(p.getPort());
                                            udpPort.setService(p.getService());
                                            ports.add(udpPort);
                                        }
                                    }
                                    destReached.setReachedPorts(ports);
                                    if (!(ports.size() == 0)) {
                                        reached_nodes.add(destReached);
                                    }
                                }
                            }
                        }
                    }
                    List<RoutingRule> directList = st.from_id_to_directs_routing_rule.get(dev);
                    directList = directList == null ? new ArrayList<RoutingRule>() : directList;
                    for (RoutingRule route : directList) { //check Directs routes
                        if (eth.getName().equals(route.getIface())) {
                            SubnetUtils netUtils = new SubnetUtils(route.getDestination(), route.getMask());
                            String cidr_net = netUtils.getInfo().getCidrSignature();
                            noDuplicatesLAN.add(cidr_net);
                            ArrayList<String> nodes = st.from_cidr_to_addresses.get(cidr_net);
                            if (nodes == null) {
                                continue;
                            }
                            for (String node : nodes) {
//                                if(dev.equals("PC2") && node.equals("192.168.1.207")){
//                                    log.info("CHECK");
//                                }
                                String reachedDeviceId = st.getIdFromAddress(node);
                                ReachedDevice destReached = new ReachedDevice();
                                destReached.setAddress(node);
                                destReached.setIfaceName(st.from_address_to_interface.get(node));
                                destReached.setLanID(st.from_lan_to_id.get(st.from_ip_to_cidrLan.get(node)));
                                destReached.setHostName(st.from_id_to_info.get(reachedDeviceId).getHostName());
                                destReached.setDeviceId(reachedDeviceId);
                                ArrayList<String> final_path = new ArrayList();
                                final_path.add(dev + ":" + eth.getName());
                                final_path.add(st.from_address_to_interface.get(node) + ":" + reachedDeviceId);
                                destReached.setPath(final_path);
                                ArrayList<ReachedPort> ports = new ArrayList();
                                HashMap<String, ArrayList<ReachedPort>> outputPorts = st.getPortsFromOutputChain(eth.getIPAddress(), node, dev, st);
                                HashMap<String, ArrayList<ReachedPort>> inputPorts;
                                // flow < OuputPorts -> InputPorts >
                                if (outputPorts != null) {
                                    inputPorts = new HashMap(outputPorts);
                                } else {
                                    inputPorts = new HashMap(st.getPortsFromAddress(node));
                                    outputPorts = new HashMap(st.getPortsFromAddress(node));
                                }
                                if (st.getChainFirewallRulesFromFirewall(reachedDeviceId, "Input") != null) {
                                    inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, reachedDeviceId, st, outputPorts);
                                }
                                ArrayList<ReachedPort> tcpPorts = inputPorts.get("TCP");
                                ArrayList<ReachedPort> udpPorts = inputPorts.get("UDP");
                                if (reachedDeviceId.equals("bf1813fc35aa56f90beb3c150bf59237827ec3f2")) {
                                    log.info("found");
                                }
                                if (tcpPorts.size() > 0) {
                                    for (ReachedPort p : tcpPorts) {
                                        ReachedPort tcpPort = new ReachedPort();
                                        tcpPort.setProtocol("TCP");
                                        tcpPort.setPort(p.getPort());
                                        tcpPort.setService(p.getService());
                                        ports.add(tcpPort);
                                    }
                                }

                                if (udpPorts.size() > 0){
                                    for (ReachedPort p : udpPorts) {
                                        ReachedPort udpPort = new ReachedPort();
                                        udpPort.setProtocol("UDP");
                                        udpPort.setPort(p.getPort());
                                        udpPort.setService(p.getService());
                                        ports.add(udpPort);
                                    }
                                }
                                destReached.setReachedPorts(ports);
                                if (!(ports.size() == 0)) {
                                    reached_nodes.add(destReached);
                                }
                            }

                        }
                    }
                    List<RoutingRule> defaultList = st.from_id_to_defaults.get(dev);
                    defaultList = defaultList == null ? new ArrayList<RoutingRule>() : defaultList;
                    for (RoutingRule route : defaultList) { //check defaults routes
                        String next_gw = st.getIdFromAddress(route.getGateway());
                        if (eth.getName().equals(route.getIface())) {
                            ArrayList<String> final_path = new ArrayList();
                            final_path.add(dev + ":" + eth.getName());
                            for (String lan : st.getDestinationForMiddlebox(next_gw).keySet()) {
                                if (!noDuplicatesLAN.contains(lan)) {
                                    ArrayList<String> path = st.getHopsFromDestinationForMiddlebox(next_gw, lan);
                                    ArrayList<String> nodes = st.from_cidr_to_addresses.get(lan);
                                    for (String node : nodes) {
//                                        if(dev.equals("PC2") && node.equals("192.168.1.207")){
//                                            log.info("CHECK");
//                                        }
                                        String hostNameReach = st.getIdFromAddress(node);
                                        ReachedDevice destReached = new ReachedDevice();
                                        destReached.setAddress(node);
                                        destReached.setIfaceName(st.from_address_to_interface.get(node));
                                        destReached.setLanID(st.from_lan_to_id.get(st.from_ip_to_cidrLan.get(node)));
                                        destReached.setDeviceId(hostNameReach);
                                        destReached.setHostName(st.from_id_to_info.get(hostNameReach).getHostName());

                                        String lastIface = "";
                                        //FIX SISTEMA CODICE
                                        String lastIfaceHost = "";
                                        if (path.size() > 1) {
                                            lastIface = path.get(path.size() - 1).split(":")[2];
                                            lastIfaceHost = path.get(path.size() - 1).split(":")[1];
                                        } else {
                                            lastIface = path.get(path.size() - 1).split(":")[1];
                                        }
                                        ArrayList<String> pathForFirewall;
                                        ArrayList<String> final_path2 = new ArrayList(final_path);
                                        for (String p : path) {
                                            final_path2.add(p);
                                        }
                                        if (lastIface.equals(st.from_address_to_interface.get(node)) && path.size() > 1 && hostNameReach.equals(lastIfaceHost)) {
                                            destReached.setPath(final_path2);
                                            pathForFirewall = new ArrayList(final_path2);
                                        } else {
                                            String newHop = st.from_address_to_interface.get(node) + ":" + hostNameReach;
                                            final_path2.add(newHop);
                                            destReached.setPath(final_path2);
                                            pathForFirewall = new ArrayList(final_path2);
                                        }
                                        ArrayList<ReachedPort> ports = new ArrayList();
                                        ArrayList<String> listFws = new ArrayList();
                                        int two_ways = 0;
                                        List<String> firstPath = new ArrayList();
                                        List<String> secondPath = new ArrayList();
                                        int index_1 = -1;
                                        int index_2 = -1;
                                        for (int i = 0; i < pathForFirewall.size(); i ++){
                                            if (pathForFirewall.get(i).contains(dev) && !dev.equals(hostNameReach)){
                                                two_ways ++;
                                                if (index_1 == -1){
                                                    index_1 = i;
                                                }else{
                                                    index_2 = i;
                                                }
                                            }
                                        }
                                        if (two_ways == 2){
                                            firstPath = pathForFirewall.subList(index_1, index_2);
                                            secondPath = pathForFirewall.subList(index_2, pathForFirewall.size());
                                            String iface1 = firstPath.get(0).split(":")[1];
                                            String iface2 = secondPath.get(0).split(":")[1];
                                            if (iface1.equals(eth.getName())){
                                                pathForFirewall = new ArrayList(firstPath);
                                            } else {
                                                pathForFirewall = new ArrayList(secondPath);
                                            }
                                        }
                                        for (int i = 0; i < pathForFirewall.size(); i++) {
                                            if (!(i == 0 || i == pathForFirewall.size() - 1)) {
                                                String[] fw = pathForFirewall.get(i).split(":");
                                                listFws.add(fw[1]);
                                            }
                                        }
                                        HashMap<String, ArrayList<ReachedPort>> outputPorts = st.getPortsFromOutputChain(eth.getIPAddress(), node, dev, st);
                                        HashMap<String, ArrayList<ReachedPort>> inputPorts;
                                        if (outputPorts != null) {
                                            inputPorts = new HashMap(outputPorts);
                                        } else {
                                            inputPorts = new HashMap(st.getPortsFromAddress(node));
                                            outputPorts = new HashMap(st.getPortsFromAddress(node));
                                        }
                                        if (listFws.size() > 0) {
                                            HashMap<String, ArrayList<ReachedPort>> forwardPorts = st.getPortsFromForwardChain(eth.getIPAddress(), node, listFws, st, outputPorts);
                                            if (st.getChainFirewallRulesFromFirewall(hostNameReach, "Input") != null) {
                                                inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, hostNameReach, st, forwardPorts);
                                            } else {
                                                //FIX ADDED ALSO HERE, NEED TO TEST
//                                                log.info("FIX ADDED AFTER EMPTY RULE PROBLEM, NEED TEST - 2");
                                                inputPorts = forwardPorts;
                                            }
                                        } else {
                                            if (st.getChainFirewallRulesFromFirewall(hostNameReach, "Input") != null) {
                                                inputPorts = st.getPortsFromInputChain(eth.getIPAddress(), node, hostNameReach, st, outputPorts);
                                            }
                                        }
                                        ArrayList<ReachedPort> tcpPorts = inputPorts.get("TCP");
                                        ArrayList<ReachedPort> udpPorts = inputPorts.get("UDP");

                                        if (tcpPorts.size() > 0) {
                                            for (ReachedPort p : tcpPorts) {
                                                p.setProtocol("TCP");
                                                ports.add(p);
                                            }
                                        }

                                        if (udpPorts.size() > 0) {
                                            for (ReachedPort p : udpPorts) {
                                                p.setProtocol("UDP");
                                                ports.add(p);
                                            }
                                        }
                                        destReached.setReachedPorts(ports);
                                        if (!(ports.size() == 0)){
                                            reached_nodes.add(destReached);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                iface.setReachedDevices(reached_nodes);
                ifaces.add(iface);
            }
            sourceDevice.setReachedInterface(ifaces);
            sourceDevices.add(sourceDevice);
        }

    }

}
