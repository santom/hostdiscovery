package org.networkstat.sniffer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.network.Ip6;
import org.jnetpcap.protocol.tcpip.Udp;

public class PacketSniffer {

	public enum OS {
		APPLE,
		LINUX, 
		MICROSOFT
	}
	
	private static int MDNS = 5353;
	private static int LLMNR = 5355;

	
	public HashMap<String, Integer> startSniffing(String deviceIP, PcapIf device, int packets) {
		StringBuilder errbuf = new StringBuilder(); // For any error msgs  
		
		final HashMap<String, Integer> osMap = new HashMap<String, Integer>();
		/*************************************************************************** 
		 * Second we open up the selected device 
		 **************************************************************************/  
		int snaplen = 64 * 1024;           // Capture all packets, no trucation  
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets  
		int timeout = 10 * 1000;           // 10 seconds in millis  
		Pcap pcap =  
				Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);  

		if (pcap == null) {  
			System.err.printf("Error while opening device for capture: "  
					+ errbuf.toString());
			return;
		}

		PcapBpfProgram program = new PcapBpfProgram();
		//we dont want to sniff our own packets
		String expression = "not host " + deviceIP;
		int optimize = 0;         // 0 = false
		int netmask = 0xFFFFFF00; // 255.255.255.0

		if (pcap.compile(program, expression, optimize, netmask) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return;
		}

		if (pcap.setFilter(program) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return;		
		}

		/*************************************************************************** 
		 * Third we create a packet handler which will receive packets from the 
		 * libpcap loop. 
		 **************************************************************************/  
		final Ethernet eth = new Ethernet();
		final Ip4 ip4 = new Ip4();
		final Ip6 ip6 = new Ip6();
//		final Html html = new Html();
		final Udp udp = new Udp();


		PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {  

			byte[] destination = new byte[4];
			byte[] source = new byte[4];
			byte[] destination6 = new byte[16];
			byte[] source6 = new byte[16];
			int totalPackets = 0;
			int udpPackets = 0;
			public void nextPacket(PcapPacket packet, String user) {
				totalPackets ++;
				
				if(packet.hasHeader(udp)) {
					if (packet.hasHeader(ip4)) {
						source = ip4.source();
						destination = ip4.destination();
						String sourceIP = FormatUtils.ip(source);
						String destinationIP = FormatUtils.ip(destination);
//						System.out.println("#" + totalPackets +  ": from " + sourceIP + " to " + destinationIP);
//						System.out.println("#" + udpPackets + ": from "+  udp.source() + " to " + udp.destination());
						if(udp.destination() == MDNS) {
//							System.out.println("from " + sourceIP + " to " + destinationIP);
//							System.out.println("from "+  udp.source() + " to " + udp.destination());
//							int len = udp.getPayloadLength();
//							byte[] buffer = new byte[10];
////							
////							udp.transferPayloadTo(buffer);
////							System.out.println("meow : " + Arrays.toString(buffer));
////							System.out.println("hex : " + bytesToHex(buffer));
//							try {
//								System.out.println("testing " + new String(buffer, "UTF-8"));
//							} catch (UnsupportedEncodingException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							System.out.println(sourceIP + ": Apple");
							osMap.put(sourceIP, OS.APPLE.ordinal());
						}
						
						if(udp.destination() == LLMNR) {
//							System.out.println("from " + sourceIP + " to " + destinationIP);
//							System.out.println("from "+  udp.source() + " to " + udp.destination());
//							System.out.println(sourceIP + ": Microsoft");
							osMap.put(sourceIP, OS.MICROSOFT.ordinal());
						}
					}					
					udpPackets ++;
				}
			}  
		};

		/*************************************************************************** 
		 * Fourth we enter the loop and tell it to capture 10 packets. The loop 
		 * method does a mapping of pcap.datalink() DLT value to JProtocol ID, which 
		 * is needed by JScanner. The scanner scans the packet buffer and decodes 
		 * the headers. The mapping is done automatically, although a variation on 
		 * the loop method exists that allows the programmer to sepecify exactly 
		 * which protocol ID to use as the data link type for this pcap interface. 
		 **************************************************************************/  
		pcap.loop(1000, jpacketHandler, "");  

		/*************************************************************************** 
		 * Last thing to do is close the pcap handle 
		 **************************************************************************/  
		pcap.close();
		
		//done with sniffing. Now we read the results.
//		for(Entry<String, Integer> entry: osMap.entrySet()) {
//			System.out.println(entry.getKey() + ": " + entry.getValue());
//		}
		return osMap;
	}
	
	public static String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}	
}
