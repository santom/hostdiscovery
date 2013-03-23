package org.networkstat.discovery;

import java.awt.List;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class HostScanner {

	public static void main(String[] args) {
		
		 try
		 {
			 //System.out.println("Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
             Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
             InetAddress localhost = null;
             for (; n.hasMoreElements();)
             {
                     NetworkInterface e = n.nextElement();
                    
                     Enumeration<InetAddress> a = e.getInetAddresses();
                     for (; a.hasMoreElements();)
                     {
                             InetAddress addr = a.nextElement();
                             if(addr instanceof Inet4Address && (!addr.getHostAddress().startsWith("127.")))
                             {
//                            	 System.out.println("Interface: " + e.getName());
//                            	 System.out.println("  " + addr.getHostAddress());
                            	 localhost = addr;
                            	 break;
                             }
                     }
             }
             NetworkInterface nit = NetworkInterface.getByInetAddress(localhost);
             showNetInfo(nit);
//            InetAddress localhost = InetAddress.getLocalHost();
//            System.out.println(localhost.getHostAddress());
		 }catch(Exception e)
		 {
             //throw Exception
		 }
	}
	
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
	
	private static void showNetInfo(NetworkInterface netint) throws SocketException {
	     
	    String  ipAddr  = null;
	    String[] range = new String[2];
	    int     cidr    = 0;
	     
	    System.out.printf("Display name: %s%n", netint.getDisplayName());
	    System.out.printf("Name: %s%n", netint.getName());
	     
	    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
//	    for (InetAddress inetAddress : Collections.list(inetAddresses)){
//	        System.out.printf("InetAddress: %s%n", inetAddress);
//	    }
	     
//	    System.out.printf("Parent: %s%n", netint.getParent());
//	    System.out.printf("Up? %s%n", netint.isUp());
//	    System.out.printf("Loopback?  %s%n", netint.isLoopback());
//	    System.out.printf("PointToPoint? %s%n", netint.isPointToPoint());
//	    System.out.printf("Supports multicast? %s%n", netint.isVirtual());
//	    System.out.printf("Virtual? %s%n", netint.isVirtual());
//	    System.out.printf("Hardware Address: %s%n", Arrays.toString(netint.getHardwareAddress()));
//	    System.out.printf("MTU %s%n", netint.getMTU());
	     
	    java.util.List<InterfaceAddress> interfaceAddresses = netint.getInterfaceAddresses();
	 
	    for (InterfaceAddress addr : interfaceAddresses) {
	    	if(!(addr.getAddress() instanceof Inet4Address)) continue;
	        System.out.printf("InterfaceAddress: %s%n", addr.getAddress());
	        ipAddr = addr.getAddress().toString();
	         
	        // Remove leading /
	        if (ipAddr.startsWith("/")){
	            ipAddr = ipAddr.substring(1, ipAddr.length());
	        }
	         
	        System.out.printf("Broadcast: %s%n", addr.getBroadcast());
	        System.out.printf("Network Prefix: %s%n", addr.getNetworkPrefixLength());
	        cidr = addr.getNetworkPrefixLength();
	        System.out.printf("Subnet Mask: %s%n", long2ip(-1 <<(32 -cidr)));
	         
	      
	    }
	    System.out.printf("%n");
	     
	    Enumeration<NetworkInterface> subInterfaces = netint.getSubInterfaces();
	    for (NetworkInterface networkInterface : Collections.list(subInterfaces)) {
	        System.out.printf("%nSubInterface%n");
	        showNetInfo(networkInterface);
	    }
	    System.out.printf("%n");
	}
}
