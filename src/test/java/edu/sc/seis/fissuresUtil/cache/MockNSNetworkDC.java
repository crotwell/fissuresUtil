package edu.sc.seis.fissuresUtil.cache;

public class MockNSNetworkDC extends NSNetworkDC {

    public MockNSNetworkDC(boolean initialEvil) {
        super("edu/sc/seis", "mockServer", null);
        this.evil = initialEvil;
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if(netDC == null) {
            System.out.println("netDC is null.  \"regetting\" from nameservice");
            netDC = new SynchronizedNetworkDC(new SpottyNetworkDC(evil));
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
