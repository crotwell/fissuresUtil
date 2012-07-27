package edu.sc.seis.fissuresUtil.netConnChecker;

import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

/**
 * @author groves Created on Sep 1, 2004
 */
public class RootCorbaChecker extends CorbaChecker {

    public RootCorbaChecker(FissuresNamingService fisName, String description) {
        super(description);
        this.fisName = fisName;
    }

    public void run() {
        try {
            org.omg.CORBA.Object root = fisName.getRoot();
            super.obj = root;
            super.run();
        } catch(RuntimeException e) {
            cause = e;
            setFinished(true);
            setTrying(false);
            setSuccessful(false);
            fireStatusChanged(getDescription(), ConnStatus.FAILED);
        }
    }

    private FissuresNamingService fisName;
}