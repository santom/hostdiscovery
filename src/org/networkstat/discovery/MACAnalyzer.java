package org.networkstat.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.networkstat.util.New_networkInfo;
import org.networkstat.util.OSUtil;
import org.networkstat.util.OSUtil.OS;
import org.networkstat.vendor.ARPUtil;


public class MACAnalyzer {

	private static void clearCache()
	{
		OS os = OSUtil.getOS();
		String line;
		BufferedReader stdInput;
		Process p;
		
		switch(os)
		{
		
		case MAC:
		case LINUX:
			//need sudo access
			try {
				
				p = Runtime.getRuntime().exec("sudo ip -s -s  neigh flush all");
				stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = stdInput.readLine()) != null) {
					System.out.println(line);
				}
				System.out.println("Cache cleared...");
				break;
				
			} catch (IOException e) {
			
			}                                                                                                                                                     
			
			
		case WINDOWS:
			
			try {
				
				p = Runtime.getRuntime().exec("netsh interface ip delete arpcache");
				stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = stdInput.readLine()) != null) {
					System.out.println(line);
				}
				
				System.out.println("Cache cleared...");
				break;
				
			} catch (IOException e) {
			
			}                                                                                                                                                     
			
		default:
			break;
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		clearCache();
		
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
