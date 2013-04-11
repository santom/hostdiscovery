package org.networkstat.sniffer;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Udp;
import org.networkstat.discovery.NetworkStat;

public class PacketSniffer {
	
	//Apple
	private static int MDNS = 5353;
	
	//Windows
	private static int LLMNR = 5355;
	private static int SSDP = 1900;

	
	public HashMap<String, NetworkStat.HOST_TYPE> startSniffing(String deviceIP, PcapIf device, int packets) {
		StringBuilder errbuf = new StringBuilder(); // For any error msgs  
		
		final HashMap<String, NetworkStat.HOST_TYPE> osMap = new HashMap<String, NetworkStat.HOST_TYPE>();

		int snaplen = 64 * 1024;           // Capture all packets, no trucation  
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets  
		int timeout = 10 * 1000;           // 10 seconds in millis  
		Pcap pcap =  
				Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);  

		if (pcap == null) {  
			System.err.printf("Error while opening device for capture: "  
					+ errbuf.toString());
			return null;
		}

		PcapBpfProgram program = new PcapBpfProgram();
		//we dont want to sniff our own packets
		String expression = "not host " + deviceIP;
		int optimize = 0;         // 0 = false
		int netmask = 0xFFFFFF00; // 255.255.255.0

		if (pcap.compile(program, expression, optimize, netmask) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return null;
		}

		if (pcap.setFilter(program) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return null;		
		}

		final Ip4 ip4 = new Ip4();
		final Udp udp = new Udp();


		PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {  

			byte[] destination = new byte[4];
			byte[] source = new byte[4];

			int totalPackets = 0;
			
			public void nextPacket(PcapPacket packet, String user) {
				totalPackets ++;
				if(totalPackets % 100 == 0) {
					System.out.println("Sniffed " + totalPackets + " packets");
				}
				
				if(packet.hasHeader(udp)) {
					if (packet.hasHeader(ip4)) {
						source = ip4.source();
						destination = ip4.destination();
						String sourceIP = FormatUtils.ip(source);
						String destinationIP = FormatUtils.ip(destination);						
						if(udp.destination() == MDNS) {
							int len = udp.getPayloadLength();
							byte[] buffer = new byte[len];

							udp.transferPayloadTo(buffer);
							String payload = "";
							try {
								payload = new String(buffer, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							payload = payload.replaceAll("[^\\x00-\\x7F]", "");
							payload = payload.replaceAll("\\p{Cntrl}", "");
							payload = payload.toLowerCase();
							
							NetworkStat.HOST_TYPE host;
							if(payload.contains("ipod")) host = NetworkStat.HOST_TYPE.IPOD;
							else if(payload.contains("iphone")) host = NetworkStat.HOST_TYPE.IPHONE;
							else if(payload.contains("ipad")) host = NetworkStat.HOST_TYPE.IPAD;
							else if(payload.contains("macbook")) {
								if(payload.contains("air")) host = NetworkStat.HOST_TYPE.AIR;
								else if(payload.contains("pro")) host = NetworkStat.HOST_TYPE.PRO;
								else host = NetworkStat.HOST_TYPE.MACBOOK;
							}
							else {
								if(osMap.get(sourceIP)==null) {
									host = NetworkStat.HOST_TYPE.APPLE;
								} else {
									return;
								}
							}
							
							osMap.put(sourceIP, host);
						}
						
						if(udp.destination() == LLMNR || udp.destination() == SSDP) {
							osMap.put(sourceIP,  NetworkStat.HOST_TYPE.PC);
						}
						
					}					
				}
			}  
		};

		pcap.loop(packets, jpacketHandler, "");  

		pcap.close();
		
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
