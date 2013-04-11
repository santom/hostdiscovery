package org.networkstat.discovery;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class HostPinger implements Runnable{
	
	String ipaddress_to_ping;
	
	public HostPinger(String ip){
		ipaddress_to_ping = ip;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			udpEcho(ipaddress_to_ping);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private boolean udpEcho(String ip) {
        boolean r = false;
        try {
            ByteBuffer msg = ByteBuffer.wrap("Hello".getBytes());
            
            InetSocketAddress sockaddr = new InetSocketAddress(ip, 7);
            DatagramChannel dgc = DatagramChannel.open();
            dgc.configureBlocking(false);
            dgc.connect(sockaddr);
            dgc.send(msg, sockaddr);
//            System.out.println("Sending packet to " + ip);
            }
        catch (Exception e){
            return false;
        }
        return r;
    }

	
}


