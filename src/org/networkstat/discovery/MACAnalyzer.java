package org.networkstat.discovery;

import org.networkstat.util.New_networkInfo;
import org.networkstat.vendor.ARPUtil;


public class MACAnalyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//1. Get the Network information.
		New_networkInfo ninfo = new New_networkInfo();
		
		//2. Get the entire range of Addresses in the subnet
		String[] possibleHosts = ninfo.getRange();
//		
//		for(String i: possibleHosts) {
//			System.out.println(i);
//		}
//		
		//3. Initialize an instance of New_HostDiscovery passing this string array as parameters
		
		New_HostDiscovery discoverer = new New_HostDiscovery(possibleHosts);
		discoverer.startScan();
		
		ARPUtil arpreader = new ARPUtil();
		arpreader.readARP();
		
	}

}
