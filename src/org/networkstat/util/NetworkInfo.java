package org.networkstat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapAddr;
import org.jnetpcap.PcapIf;
import org.jnetpcap.PcapSockAddr;
import org.networkstat.util.SubnetUtils.SubnetInfo;

public class NetworkInfo {


	SubnetUtils su = null;
	SubnetInfo si =  null;	   

	private String ipAddress;
	private String subnet;
	private PcapIf device;

	public NetworkInfo() {
		List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs  
		StringBuilder errbuf = new StringBuilder(); // For any error msgs  

		int r = Pcap.findAllDevs(alldevs, errbuf);  
		if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
			System.err.printf("Can't read list of devices. Either you are not running" +
					"this program as root, or there is something else wrong (%s)", errbuf  
					.toString(), r);
			return;  
		}

		System.out.println("Network devices found:");  

		int i = 0;  
		for (PcapIf device : alldevs) {
			String deviceIp;
			if(device.getAddresses().size() == 0) {
				deviceIp = "No Address";
			} else {
				deviceIp = getDeviceAddresses(device.getAddresses());
			}

			String description =  
					(device.getDescription() != null) ? device.getDescription()  
							: "No description available";  
					System.out.printf("#%d: %s [%s]\nAddresses: %s\n", i++, device.getName(), description, deviceIp);  
		}

		Scanner s = new Scanner(System.in);
		System.out.println("Choose a device from the above list");
		i = s.nextInt();
		while(i >= alldevs.size()) {
			System.err.printf("Error. Device %d does not exist in the give list. Choose again", i);
			i = s.nextInt();
		}

		device = alldevs.get(i);

		String[] addr = getDeviceIPandNetmask(device.getAddresses());
		System.out.printf("\nChoosing '%s' %s %s:\n",  
				(device.getDescription() != null) ? device.getDescription()  
						: device.getName(), addr[0], addr[1]);
		ipAddress = addr[0];
		subnet = addr[1];
		this.su = new SubnetUtils(getIp(), getSubnet());
		this.si = su.getInfo();
	}

	//---- METHODS RELATED TO HOSTSCANNER----//

	private static String long2ip(long l) {           // Use bitwise bit shifting to build the new IP
		return  ((  l >> 24 )  & 0xFF) + "." +
		((  l >> 16 )  & 0xFF) + "." +
		((  l >> 8 )   & 0xFF) + "." +
		(   l        & 0xFF);
	}

	private static long ip2long(String addr) {
		String[] addrArray = addr.split("\\.");

		long num = 0;

		for (int i=0; i<addrArray.length; i++){
			int power = 3 - i;
			num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256,power)));
		}
		return num;
	}

	//-----------------------------------------------------------------------------//

	// METHODS AS A PART OF NETWORKINFO CLASS.

	public String getSubnet() {
		return subnet;
	}

	public String getIp() {
		return ipAddress;
	}

	public PcapIf getDevice() {
		return device;
	}

	public String[] getRange()
	{
		String a[] = new String[1];
		if(si != null) {
			if(getSubnet().equals("255.255.255.255")) {
				a[0] = getIp();
				return a;
			}
			return si.getAllAddresses();
		}
		else return null;
	}


	public String[] getRange(String low, String high)
	{
		if(si!=null)
			return si.getAllAddressess(si.toInteger(low), si.toInteger(high));
		else return null;
	}

	public boolean isValid(String ip)
	{
		if (ip == null) return false;
		ip = ip.trim();
		if ((ip.length() < 6) & (ip.length() > 15)) return false;

		try {
			Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
			Matcher matcher = pattern.matcher(ip);
			if(matcher.matches() && si!=null) return si.isInRange(ip);
		} catch (PatternSyntaxException ex) {
			return false;
		}
		return false;
	}

	public int getNodes()
	{
		if(getRange() != null)
			return getRange().length;
		else return 0;
	}

	public String getDeviceAddresses(List<PcapAddr> addresses) {
		String address = "";

		for(PcapAddr a: addresses) {
			address += a.toString() + ", ";
		}
		return address;
	}

	public String[] getDeviceIPandNetmask(List<PcapAddr> addresses) {
		for(PcapAddr a: addresses) {
			PcapSockAddr ip = a.getAddr();
			PcapSockAddr netmask = a.getNetmask();
			if(ip != null && netmask != null) {
				if(ip.toString().length() > 15) {
					String[] addr = new String[2];
					addr[0] = ip.toString().substring(7, ip.toString().indexOf(']'));
					addr[1] = netmask.toString().substring(7, netmask.toString().indexOf(']'));
					return addr;
				}
			}
		}
		return null;
	}

	public String getDeviceIPAddress(List<PcapAddr> addresses) {
		for(PcapAddr a: addresses) {
			String addr = a.getAddr().toString();
			if(addr.length() > 15) {
				return addr.substring(7, addr.indexOf(']'));
			}
		}
		return null;
	}
}
