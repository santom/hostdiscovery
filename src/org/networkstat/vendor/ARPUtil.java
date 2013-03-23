package org.networkstat.vendor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class ARPUtil {

	public static HashMap<String, String> readARP() {
		//assuming this is a linux machine
		HashMap<String, String> map = new HashMap<String, String>();

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
							System.out.println("IP: " + ip + " MAC: " + mac);
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

		return map;
	}
	
	public static void main(String[] args) {
		
		readARP();
	}

}
