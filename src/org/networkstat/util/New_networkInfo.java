package org.networkstat.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.networkstat.util.SubnetUtils;
import org.networkstat.util.SubnetUtils.SubnetInfo;

public class New_networkInfo {
	
	
	   SubnetUtils su = null;
	   SubnetInfo si =  null;	   
	 
	   String ipAddress;
	   String subnet;
	

	// CONSTRUCTOR. INITIALIZES THE HOST DISCOVERY.
	public New_networkInfo() {
		
    //1. Find Local IP Address and Subnetmask		
     try{
	 
     Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
     InetAddress localhost = null;
     for (; n.hasMoreElements();){
         NetworkInterface e = n.nextElement();
                    
         Enumeration<InetAddress> a = e.getInetAddresses();
         for (; a.hasMoreElements();){
            InetAddress addr = a.nextElement();
            if(addr instanceof Inet4Address && (!addr.getHostAddress().startsWith("127."))){
                localhost = addr;
                this.ipAddress = localhost.getHostAddress();
                break;
            }
         }
     }
     // THE IP ADDRESS OF THE NODE IS STORED IN localhost
     //2. Get Subnetmask
     
     NetworkInterface nit = NetworkInterface.getByInetAddress(localhost);
     this.subnet = getSubnetMask(nit);
     
     //3. Once local IP address and subnet mask has been attained, initialise SubnetUtils object
     
     this.su = new SubnetUtils(getIp(), getSubnet());
     this.si = su.getInfo();      
     
     }catch(Exception e){
             //throw Exception
     }
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
	
	
	// Returns subnet mask in string form if found else returns null.
     private String getSubnetMask(NetworkInterface netint) throws SocketException{
		
    	String subnetmask = null;
	    String  ipAddr  = null;
		int     cidr    = 0;
		System.out.printf("\n Trying to obtain the subnet mask ");   
	     
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();    
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
		       subnetmask = long2ip(-1 <<(32 -cidr));
		       System.out.printf("Subnet Mask: %s%n", subnetmask);      
		      
		 }
		
		return subnetmask;
		     	   
	}
	
	private static void showNetInfo(NetworkInterface netint) throws SocketException {
	     
	    String  ipAddr  = null;
	    String[] range = new String[2];
	    int     cidr    = 0;
	     
	    System.out.printf("Display name: %s%n", netint.getDisplayName());
	    System.out.printf("Name: %s%n", netint.getName());
	     
	    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();    
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
	
	//-----------------------------------------------------------------------------//
	
	// METHODS AS A PART OF NETWORKINFO CLASS.
	
    public String getSubnet()
    {
        return subnet;
    }
    public String getIp()
    {
        return ipAddress;
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
	
	
}
