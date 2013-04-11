package org.networkstat.discovery;

import org.networkstat.sniffer.PacketSniffer;
import org.networkstat.util.NetworkInfo;

public class NetworkStat {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//1. Get the Network information.
		NetworkInfo ninfo = new NetworkInfo();
		
		//2. Get the entire range of Addresses in the subnet
		String[] possibleHosts = ninfo.getRange();
		System.out.println("Number of possible hosts: " + possibleHosts.length);
//		for(String i: possibleHosts) {
//			System.out.println(i);
//		}
		
		//3. Initialize an instance of New_HostDiscovery passing this string array as parameters
		
		HostDiscovery discoverer = new HostDiscovery(possibleHosts);
		discoverer.startScan();
		
		PacketSniffer sniffer = new PacketSniffer();
		HashMap<String, Integer> osMap = sniffer.startSniffing(ninfo.getIp(), ninfo.getDevice(), 1000);
		
		
//		ARPUtil arpreader = new ARPUtil();
//		arpreader.readARP();	
	}
}
