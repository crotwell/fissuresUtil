package edu.sc.seis.fissuresUtil.namingService;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
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
        unbindDead(unbindFrom.getAllEventDC(), FissuresNamingService.EVENTDC);
        unbindDead(unbindFrom.getAllSeismogramDC(),
                   FissuresNamingService.SEISDC);
        unbindDead(unbindFrom.getAllNetworkDC(),
                   FissuresNamingService.NETWORKDC);
        unbindDead(unbindFrom.getAllPlottableDC(),
                   FissuresNamingService.PLOTTABLEDC);
    }

    private static void unbindDead(ServerNameDNS[] servers, String interfaceName)
            throws NotFound, CannotProceed, InvalidName {
        for(int i = 0; i < servers.length; i++) {
            org.omg.CORBA.Object object;
            try {
                object = servers[i].getCorbaObject();
            } catch(SystemException e) {
                logger.info(servers[i] + " threw " + e + " Unbinding");
                unbindFrom.unbind(servers[i].getServerDNS(),
                                  interfaceName,
                                  servers[i].getServerName());
                continue;
            }
            CorbaChecker checker = new CorbaChecker(object,
                                                    servers[i].toString());
            checker.run();
            if(checker.getStatus().getStatus() == ConnStatus.FAILED) {
                logger.info("Unbinding " + servers[i]);
                unbindFrom.unbind(servers[i].getServerDNS(),
                                  interfaceName,
                                  servers[i].getServerName());
            }
        }
    }

    private static Logger logger = Logger.getLogger(Deadhead.class);

    private static FissuresNamingService unbindFrom;
}
