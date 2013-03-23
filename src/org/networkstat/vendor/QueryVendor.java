package org.networkstat.vendor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class QueryVendor {
	private static HashMap<String, String> vendor = null;
	
	QueryVendor () throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("vendor"));
		String line;
		while ((line = br.readLine()) != null){
			String[] info = line.split(" ");
			if (info[0].indexOf(":") != -1){
				vendor.put(info[0], info[1]);
			}
		}
	}
	
	public String getVendor(String macAddr) throws Exception {
		String[] prefixArr = macAddr.split(":");
		String prefix = (String) (prefixArr[0] + ":" + prefixArr[1] + ":" + prefixArr[2]);
		
		return vendor.get(prefix);
	}
	
}
