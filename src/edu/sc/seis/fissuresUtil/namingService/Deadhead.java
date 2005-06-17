package edu.sc.seis.fissuresUtil.namingService;

import org.apache.log4j.BasicConfigurator;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.cache.ServerNameDNS;
import edu.sc.seis.fissuresUtil.netConnChecker.ConnStatus;
import edu.sc.seis.fissuresUtil.netConnChecker.CorbaChecker;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class Deadhead {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Initializer.init(args);
        unbindFrom = Initializer.getNS();
        unbindDead(unbindFrom.getAllEventDC());
        unbindDead(unbindFrom.getAllSeismogramDC());
        unbindDead(unbindFrom.getAllNetworkDC());
        unbindDead(unbindFrom.getAllPlottableDC());
    }

    private static void unbindDead(ServerNameDNS[] servers) throws NotFound,
            CannotProceed, InvalidName {
        for(int i = 0; i < servers.length; i++) {
            CorbaChecker checker = new CorbaChecker(servers[i].getCorbaObject(),
                                                    servers[i].toString());
            checker.run();
            if(checker.getStatus().getStatus() == ConnStatus.FAILED) {
                unbindFrom.unbind(servers[i].getServerDNS(),
                                  servers[i].getServerName(),
                                  servers[i].getCorbaObject());
            }
        }
    }

    private static FissuresNamingService unbindFrom;
}
