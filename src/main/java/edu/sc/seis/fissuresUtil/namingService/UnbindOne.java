package edu.sc.seis.fissuresUtil.namingService;

import org.apache.log4j.BasicConfigurator;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;


import edu.sc.seis.fissuresUtil.simple.Initializer;


public class UnbindOne {

    public static void main(String[] args) throws NotFound, CannotProceed, InvalidName {
        BasicConfigurator.configure();
        Initializer.init(args);
        FissuresNamingService unbindFrom = Initializer.getNS();
        String type = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-n")) {
                type = FissuresNamingService.NETWORKDC;
            } else if (args[i].equals("-e")) {
                type = FissuresNamingService.EVENTDC;
            } else if (args[i].equals("-s")) {
                type = FissuresNamingService.SEISDC;
            } else if (args[i].equals("-d")) { // can't use p for plottable, so use d for day view
                type = FissuresNamingService.PLOTTABLEDC;
            } else if (args[i].equals("-h")) {
                System.out.println("Usage: unbindone -[ensd] dns name");
                System.exit(0);
            }
        }
        if (type != null) {
            String dns = args[args.length-2];
            String name = args[args.length-1];
            System.out.println("Unbinding "+type+" "+dns+" "+name);
            unbindFrom.unbind(dns, type, name);
        } else {
            System.out.println("Type is null, use one of -n, -e, -s or -p");
        }
    }

}
