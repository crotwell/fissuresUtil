
package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import org.apache.log4j.Logger;


public class DNDNetworkAccess extends CacheNetworkAccess implements Transferable {


    public DNDNetworkAccess(NetworkAccess net) {
        super(net);
        logger.debug("after super");
    df = new DataFlavor[2];
    df[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+
                   "; class="+DNDNetworkAccess.class.getName(),
                   "Seismic Network");
        logger.debug("after data flavor for class");
    df[1] = DataFlavor.stringFlavor;
        logger.debug("after data flavor for string");
    }

    public DataFlavor[] getTransferDataFlavors() {

    return df;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(df[0]) || flavor.equals(df[1]);
    }


    public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException,
    java.io.IOException {
    if (flavor.equals(df[0])) {
        return this;
    } // end of if (flavor.equals(df[0]))
    logger.debug("getTransferData before get_attributes");
    return get_attributes().name;

    }

    private DataFlavor[] df;

    static Logger logger = Logger.getLogger(DNDNetworkAccess.class);

}
