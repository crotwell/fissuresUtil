package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkDC;


public class MockNSNetworkDC extends NSNetworkDC {
    
    public MockNSNetworkDC(boolean initialEvil) {
        super("edu/sc/seis", "mockServer", null);
        this.evil = initialEvil;
    }
    
    public synchronized NetworkDC getNetworkDC() {
        if(netDC == null) {
            System.out.println("netDC is null.  \"regetting\" from nameservice");
            netDC = new SpottyNetworkDC(evil);
            evil = !evil;
        } // end of if ()
        return netDC;
    }
    
    public synchronized void reset() {
        System.out.println("reset called!");
        super.reset();
    }
    
    private boolean evil;
}
