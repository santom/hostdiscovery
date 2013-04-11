package org.networkstat.discovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.networkstat.sniffer.PacketSniffer;
import org.networkstat.util.NetworkInfo;
import org.networkstat.util.OSUtil;
import org.networkstat.util.OSUtil.OS;
import org.networkstat.vendor.ARPUtil;
import org.networkstat.vendor.QueryVendor;

public class NetworkStat {

	public enum HOST_TYPE {
		IPHONE,
		IPAD,
		IPOD,
		AIR,
		PRO,
		MACBOOK,
		PC,
		APPLE
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		QueryVendor qv = new QueryVendor();

		//1. Get the Network information.
		NetworkInfo ninfo = new NetworkInfo();

		//2. Get the entire range of Addresses in the subnet
		String[] possibleHosts = ninfo.getRange();
		System.out.println("Number of possible hosts: " + possibleHosts.length);

		//3. Initialize an instance of New_HostDiscovery passing this string array as parameters
		HostDiscovery discoverer = new HostDiscovery(possibleHosts);
		clearCache();
		discoverer.startScan();

		PacketSniffer sniffer = new PacketSniffer();
		System.out.println("Starting Scan!");

		HashMap<String, HOST_TYPE> osMap = 
				sniffer.startSniffing(ninfo.getIp(), ninfo.getDevice(), 1000);

		ARPUtil arpreader = new ARPUtil();
		HashMap<String, String> macMap = arpreader.readARP();
		System.out.println("Found: " + macMap.size());

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("out.csv"));
			String s = ninfo.getIp() + "," + "123," + OSUtil.getOS().name() + "," + "wlan0,1000,\n";
			bw.write(s);
			bw.flush();

			for(Entry<String, String> entry: macMap.entrySet()) {
				String str = entry.getKey() + "," + entry.getValue() + 
						"," + osMap.get(entry.getKey()) + "," + qv.getVendor(entry.getValue()).split(" ")[0] + ",\n";
				bw.write(str);
				bw.flush();
				System.out.println(entry.getKey() + ": " + " : " + 
						osMap.get(entry.getKey()) + " : " + entry.getValue() + " : " + qv.getVendor(entry.getValue()).split(" ")[0]);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		discoverer.stop();

		try {
			URI uri = new URI("http://ec2-54-234-7-38.compute-1.amazonaws.com/upload.php");
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(uri);
			MultipartEntity entity = new MultipartEntity();

			FileInputStream fsi = null;
			String mimeType = "text/csv";
			String filePath = "out.csv";

			fsi = new FileInputStream(filePath);

			ContentBody fbody = new InputStreamBody(fsi, mimeType,filePath);
			entity.addPart("file", fbody);

			httppost.setEntity(entity);
			HttpResponse response = null;
			response = httpclient.execute(httppost);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private static void clearCache() {
		OS os = OSUtil.getOS();
		Process p;

		switch(os) {
		case MAC:
		case LINUX:
			//need sudo access
			try {
				p = Runtime.getRuntime().exec("sudo ip -s -s  neigh flush all");
				System.out.println("Cache cleared");
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}                                                                                                                                              

		case WINDOWS:
			try {		
				
				
				p = Runtime.getRuntime().exec("netsh interface ip delete arpcache");
				System.out.println("Cache cleared.");
				
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}                                                                                                                                                     
		default:
			break;
		}
	}
}
