package org.networkstat.discovery;
import org.networkstat.util.networkInfo;

public class HostDiscovery {

    static int countDiscoveredhosts = 0;
    static String[] discoveredHosts = null;
    static int countPossibleHosts = 0;
    
    //the total number of hosts to be checked for using one particular method
    static int totalHosts = 0;
    
    String[] possibleHosts= null;
    int discoveryMode = 0;
    static int progress = 0;
    String low = null;
    String high = null;
   // AsyncTask<Object[], Integer, Void> hd = null;
    static boolean started = false;
    static boolean scanned = false;
    networkInfo ni;
   // DiscoveryDBAdapter discoverydb;
    
    /**
     * Constructor
     * @param ni
     */
    public HostDiscovery(networkInfo ni) {
        this.ni = ni;
        init();
    }
    
    /**
     * Initializes host discovery
     * modifies countPossibleHosts, possibleHosts, totalHosts, low, high 
     */
    public void init()
    {
        reset();
     //  discoverydb = new DiscoveryDBAdapter(nsandroid.defaultInstance);
    //    discoverydb.open();
        
        if(ni==null) {
            String result = "Error in getting Network Information\n Make sure you are connected to atleast one network interface.";
        }
        else {
            countPossibleHosts = ni.getNodes();
            setTotalHosts(getMode());
            possibleHosts = ni.getRange();
            discoveredHosts = new String[countPossibleHosts];
            low = possibleHosts[0];
            high = possibleHosts[countPossibleHosts-1];
        }
    }
    
    
    /**
     * @param mode
     * Sets the discovery_mode
     */
    public void setMode(int mode) {
        this.discoveryMode = mode;
        setTotalHosts(mode);
    }
    
    
    /**
     * @return discovery_mode
     */
    public int getMode() { return discoveryMode; }
    /**
     * @return low
     */
    public String getLow() { return low; }
    /**
     * @return high
     */
    public String getHigh() { return high; }
    /**
     * @return started
     */
    public boolean isStarted(){ return started; }


    /**
     * Starts host discovery
     * modifies started, possibleHosts, countPossibleHosts, hd
     */
    public void start() {
        init();
        
        if(started == true) {
            String result = "Please wait for the current scan to finish or press stop.";
            return;
        }
        
        if(ni.isValid(low) && ni.isValid(high)) {
            possibleHosts = ni.getRange(low, high);
            countPossibleHosts = possibleHosts.length;
            
            if(possibleHosts != null) {
                String[] mode = {Integer.toString(getMode())};
                hd = new Discovery();
                started = true;
                hd.execute((Object[])possibleHosts, (Object[])mode);
            }
            else {
                String result = "Error in getting Network Information\n Make sure you are connected to atleast one network interface.";
                nsandroid.resultPublish(result);
                nsandroid.makeToast(result);
            }
        }
        else{
            String result = "Invalid IP. Please re-enter";
            nsandroid.makeToast(result);
            return;
        }
        scanned = true;

    }
    
    
    /**
     * Stop host discovery.
     * Modifies started, hd
     */
    public void stop() {
                
        if(started == false) {
            String result = "Discovery not running";
            Toast.makeText(nsandroid.defaultInstance, result, Toast.LENGTH_LONG).show();
            return;
        }
        
        hd.cancel(true);
        started = false;
        String result = "Host Discovery interrupted\nDiscovered " + countDiscoveredhosts + " hosts.";
        nsandroid.resultPublish(result);
        nsandroid.makeToast(result);
        scanned = true;
    }
    

    /**
     * Resets host discovery
     */
    public void reset() {
        countDiscoveredhosts = 0;
        possibleHosts = null;
        countPossibleHosts = 0;
        discoveredHosts = null;
        low = null;
        high = null;
        //hd = null;
        started = false;
        scanned = false;
        progress = 0;
       // nsandroid.resetList(); 
    }
    
    
    /**
     * @param which
     * sets the number of possible nodes based on the discovery methods
     * modified possibleNodes
     */
    private void setTotalHosts(int which) {
        switch(which){
        case 0: totalHosts = countPossibleHosts; break;
        case 1: totalHosts = countPossibleHosts *2; break;
        case 2: totalHosts = countPossibleHosts *5; break;
        }
    }
    

    /**
     * @param ipaddress
     * Stati method.
     * If a host is discovered, this method is called
     * modifies discovered[], hosts
     */
    public static void addHosts(String ipaddress) {
        int flag = 0;
        for(int i=0; i<countDiscoveredhosts; i++) {
            if(ipaddress.equals(discoveredHosts[i]))
                flag = 1;
        }
        
        if(flag == 0) {
            discoveredHosts[countDiscoveredhosts] = ipaddress;
            nsandroid.resultPublish(ipaddress);
            nsandroid.addToList(ipaddress);
            updateProgress();
            countDiscoveredhosts++;
        }
    }

    
    /**
     * To update the progress bar
     */
    public static void updateProgress() {
        progress++;
        nsandroid.updateProgressBar((int)(progress*100.0/(float)totalHosts));
                
        if(progress==totalHosts) {
            String result = "Done Host Discovery\nFound " + countDiscoveredhosts + " hosts.";
            started = false;
            nsandroid.resultPublish(result);
            nsandroid.resetProgressBar();
            nsandroid.makeToast(result);
        }
    }
    
    
    /**
     * @param ip
     */
    public static void publishHost(String ip){
        nsandroid.resultPublish(ip);
    }

    public void saveDiscovery(String name) {
        if(scanned == false) {
            nsandroid.makeToast("Please run a scan first. Nothing to save.");
            return;
        }

        if(started == true) {
            nsandroid.makeToast("Scan running. Wait before trying to save or stop the scan and then save.");
            return;
        }
 
        String target = ni.getIp();
        String range = ni.getRange()[0] + "-" + ni.getRange()[ni.getRange().length-1];
        String total = Integer.toString(ni.getNodes());
        String type = Integer.toString(getMode());
        String args = "";
        String hosts = "";
        
        for(int i = 0; i<countDiscoveredhosts; i++) {
            if(!discoveredHosts.equals("null"))
                hosts = hosts + discoveredHosts[i] + "-";
        }
        
        discoverydb.save(name, type, target, range, total, args, hosts);
        nsandroid.resultPublish("Saved!");
    }

    public void destroy() {
        if(discoverydb !=null)
        {
            discoverydb.close();
        }
    }
}
