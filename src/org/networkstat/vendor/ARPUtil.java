package org.networkstat.vendor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.networkstat.util.OSUtil;
import org.networkstat.util.OSUtil.OS;


public class ARPUtil {

	private OS os = OSUtil.getOS();
	
	public HashMap<String, String> readARP() {
		
		//assuming this is a linux machine
		HashMap<String, String> map = new HashMap<String, String>();

		switch(os) {
		
		case LINUX:
			try {
				BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
				String line;
				try {
					while((line = br.readLine()) != null) {
						String[] splitted = line.split(" +");
						if (splitted != null && splitted.length >= 4) {
							// Basic sanity check
							String mac = splitted[3];
							String ip = splitted[0];
							if (mac.matches("..:..:..:..:..:..") && !mac.equals("00:00:00:00:00:00")) {
								map.put(ip, mac);
//								System.out.println("IP: " + ip + " MAC: " + mac);
							}
						}
					}
					br.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}  
			break;
		
		case WINDOWS:
			Process p;
			try {
				p = Runtime.getRuntime().exec("arp -a");
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = stdInput.readLine()) != null) {
					if(line.toLowerCase().contains("dynamic"))
					{
						line = line.replaceAll(" +"," ");
						String[] tags = line.trim().split(" ");
						String ip = tags[0];
						ip = ip.trim();
						String mac = tags[1];
						mac = mac.trim();
						mac = mac.replaceAll("-",":");
						if (mac.matches("..:..:..:..:..:..") && !mac.equals("00:00:00:00:00:00")) {
							map.put(ip, mac);
//							System.out.println("IP: " + ip + " MAC: " + mac);
						}
						
					}
				}
				System.out.println("Cache cleared.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			break;
		case MAC:
			break;
		}

		return map;
	}
	
//	public static void main(String[] args) {
//		
//		QueryVendor qv = new QueryVendor();
//		HashMap<String, String> map = readARP();
//		System.out.println(qv.getVendor("e0:f8:47:17:92:70"));
//	}

}
