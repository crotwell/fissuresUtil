package edu.sc.seis.fissuresUtil.namingService;

import org.apache.log4j.BasicConfigurator;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.sc.seis.fissuresUtil.simple.Initializer;


/**
 * @author crotwell
 * Created on Aug 22, 2005
 */
public class CreateNameContexts {

    /**
     *
     */
    public CreateNameContexts() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws NotFound, CannotProceed, InvalidName {
        BasicConfigurator.configure();
        Initializer.init(args);
        String dns = null;
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-dns") && i < args.length-1) {
                dns = args[i+1];
            } else if (args[0].equals("-h") || args[0].equals("--help")) {
                printHelp();
                return;
            }
        }
        if (dns == null) {
            printHelp();
            return;
        }
        String dnsWithKind = FissuresNamingService.appendKindNames(dns);
        FissuresNamingService fisName = Initializer.getNS();
        NamingContextExt topLevel = fisName.getNameService();
        NameComponent[] name = topLevel.to_name(dnsWithKind);
        NamingContext lastContext = topLevel;
        NameComponent[] subName = new NameComponent[1];
        for(int i = 0; i < name.length; i++) {
            try {
                logger.debug("Binding " + name[i].id
                        + "." + name[i].kind
                        + " as new context");
                System.arraycopy(name,
                                 i,
                                 subName,
                                 0,
                                 subName.length);
                logger.debug("NameComponent: "+nameComponentToString(subName));
                lastContext = lastContext.bind_new_context(subName);
            } catch(AlreadyBound e) {
                // already there so go to next name in list
                // but need to resolve as lastContext
                lastContext = NamingContextHelper.narrow(lastContext.resolve(subName));
            }
        }
        for(int i = 0; i < interfaces.length; i++) {
            try {
                subName[0] = new NameComponent(interfaces[i], fisName.INTERFACE);
                lastContext.bind_new_context(subName);
            } catch (AlreadyBound e) {
                // oh well...
            }
        }
    }
    
    static String[] interfaces = new String[] {"PlottableDC", "NetworkDC", "DataCenter", "EventDC"};
    
    static String nameComponentToString(NameComponent[] name) {
        String out = "";
        for(int i = 0; i < name.length; i++) {
            out += "/"+name[i].id+"."+name[i].kind;
        }
        return out;
    }
    
    static void printHelp() {
        System.out.println("Options: -props propfile\n    -dns dnsname\n\nfor example: java ...CreateNameContext -props server.props -dns edu/sc/seis");
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CreateNameContexts.class);
}
