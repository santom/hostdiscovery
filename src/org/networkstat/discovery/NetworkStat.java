package org.networkstat.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import org.networkstat.sniffer.PacketSniffer;
import org.networkstat.util.NetworkInfo;
import org.networkstat.util.OSUtil;
import org.networkstat.util.OSUtil.OS;
import org.networkstat.vendor.ARPUtil;

public class NetworkStat {

	public enum HOST_TYPE {
		APPLE,
		LINUX, 
		MICROSOFT
	}
	
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
		clearCache();
		discoverer.startScan();
		
		PacketSniffer sniffer = new PacketSniffer();
		HashMap<String, HOST_TYPE> osMap = sniffer.startSniffing(ninfo.getIp(), ninfo.getDevice(), 1000);

		ARPUtil arpreader = new ARPUtil();
		HashMap<String, String> macMap = arpreader.readARP();
		for(Entry<String, String> entry: macMap.entrySet()) {
			System.out.println(entry.getKey() + ": " + " : " + osMap.get(entry.getKey()) + " : " + entry.getValue());
		}
	}
	
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
//					System.out.println(line);
				}
				System.out.println("Cache cleared.");
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
				
				System.out.println("Cache cleared.");
				break;
				
			} catch (IOException e) {
			
			}                                                                                                                                                     
			
		default:
			break;
		}
	}

}
