
package edu.sc.seis.fissuresUtil.chooser;

import java.awt.datatransfer.*;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAccess;


public class DNDNetworkAccess extends CacheNetworkAccess implements Transferable {


    public DNDNetworkAccess(NetworkAccess net) {
        super(net);
	df = new DataFlavor[2];
	df[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+
			       "; class="+DNDNetworkAccess.class.getName(), 
			       "Seismic Network");
	df[1] = DataFlavor.stringFlavor;
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
	
	return get_attributes().name;
	
    }

    private DataFlavor[] df;
}
