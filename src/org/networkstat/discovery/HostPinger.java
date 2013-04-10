package org.networkstat.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostPinger implements Runnable{
	
	String ipaddress_to_ping;
	
	public HostPinger(String ip){
		
		ipaddress_to_ping = ip;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			InetAddress iaddr = InetAddress.getByName(ipaddress_to_ping);
			iaddr.isReachable(2000);			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
