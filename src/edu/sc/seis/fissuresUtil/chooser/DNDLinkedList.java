package edu.sc.seis.fissuresUtil.chooser;

import java.awt.datatransfer.*;
import java.util.LinkedList;
import java.util.Collection;
import java.io.IOException;

public class DNDLinkedList extends LinkedList implements Transferable {

    public DNDLinkedList() {
	super();
    }

    public DNDLinkedList(Collection c) {
	super(c);
    }

    public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] df = new DataFlavor[1];
	df[0] = listDataFlavor;
	return df;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(listDataFlavor);
    }

    public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException,
	IOException {
	return this;
    }

    public static final DataFlavor listDataFlavor =
	new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+
		       "; class="+java.util.List.class.getName(), 
		       "List");

}
