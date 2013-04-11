package org.networkstat.discovery;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class New_HostDiscovery {    


	String[] possiblenodes;
	ExecutorService threadPool;

	/**
	 * Constructor
	 * 
	 */
	public New_HostDiscovery(String[] possible) {    	
		this.possiblenodes = possible;
		threadPool = Executors.newFixedThreadPool(10); 
	}

	// This function starts scanning the entire range of the network
	public void startScan(){ 

		if(this.possiblenodes != null){
			for (String ip: possiblenodes){
				HostPinger hp = new HostPinger(ip);
				threadPool.execute(hp);
			}
		}

	}

}
