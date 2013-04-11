package org.networkstat.vendor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class QueryVendor {
	private static HashMap<String, String> vendor = new HashMap<String, String>();

	public QueryVendor () {
		try {
			BufferedReader br = new BufferedReader(new FileReader("res/vendorlist"));
			String line;
		
			while ((line = br.readLine()) != null){
				//System.out.println(line);
				String[] info = line.split("\t");
				if (info.length < 2)
					continue;
				
				if (info[0].indexOf(":") != -1){
					vendor.put(info[0], info[1]);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public String getVendor(String macAddr) {
		String[] prefixArr = macAddr.split(":");
		String prefix = (String) (prefixArr[0] + ":" + prefixArr[1] + ":" + prefixArr[2]);
		
		return vendor.get(prefix.toUpperCase());
	}

}
