
package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;

import javax.swing.*;
import javax.swing.text.*;

//drag and drop
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events
import java.util.*;
import edu.iris.Fissures.display.*;

/**
 * NetInfoDisplay.java
 *
 *
 * Created: Thu Mar  1 22:00:08 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class NetInfoDisplay extends TextInfoDisplay {
    
    public NetInfoDisplay() {
	super();
    }
        
    public void displayNetwork(NetworkAttr attr) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(attr, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendNetwork(NetworkAttr attr)
	throws BadLocationException 
    {
	appendNetwork(attr, textPane.getDocument());
    }

    protected void appendNetwork(NetworkAttr attr, Document doc)
	throws BadLocationException 
    {
	appendHeader(doc, "Network");
	appendLabelValue(doc, "Code", attr.get_code());
	appendLabelValue(doc, "Name", attr.name);
	appendLabelValue(doc, "Description", attr.description);
	appendLabelValue(doc, "Owner", attr.owner);
	appendLabelValue(doc, "Begin", attr.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", attr.effective_time.end_time.date_time);
	appendLine(doc, "");
    }

    public void displayStation(Station sta) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(sta.my_network, doc);
	    appendStation(sta, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendStation(Station sta) 
	throws BadLocationException 
    {
	appendStation(sta, textPane.getDocument());
    }

    protected void appendStation(Station sta, Document doc) 
	throws BadLocationException 
    {
	appendHeader(doc, "Station");
	appendLabelValue(doc, "Code", sta.get_id().station_code);
	appendLabelValue(doc, "Name", sta.name);
	appendLabelValue(doc, "Location", "("+sta.my_location.latitude+
			 ", "+sta.my_location.longitude+")");
	appendLabelValue(doc, "Begin", sta.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", sta.effective_time.end_time.date_time);
	appendLabelValue(doc, "Operator", sta.operator);
	appendLabelValue(doc, "Description", sta.description);
	appendLabelValue(doc, "Comment", sta.comment);
	appendLine(doc, "");
    }

    public void displaySite(Site site) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(site.my_station.my_network, doc);
	    appendStation(site.my_station, doc);
	    appendSite(site, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendSite(Site site) 
	throws BadLocationException 
    {
	appendSite(site, textPane.getDocument());
    }

    protected void appendSite(Site site, Document doc) 
	throws BadLocationException 
    {
	appendHeader(doc, "Site");
	appendLabelValue(doc, "Location", "("+site.my_location.latitude+
			 ", "+site.my_location.longitude+")");
	appendLabelValue(doc, "Begin", 
			 site.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", 
			 site.effective_time.end_time.date_time);
	appendLabelValue(doc, "Comment", site.comment);
	appendLine(doc, "");
    }

    public void displayChannel(Channel chan) {
	    Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(chan.my_site.my_station.my_network, doc);
	    appendStation(chan.my_site.my_station, doc);
	    appendSite(chan.my_site, doc);
	    appendChannel(chan, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendChannel(Channel chan) 
	throws BadLocationException 
    {
	appendChannel(chan, textPane.getDocument());
    }

    protected void appendChannel(Channel chan, Document doc) 
	throws BadLocationException 
    {
	appendHeader(doc, "Channel");
	appendLabelValue(doc, "Chan Code", chan.get_code());
	appendLabelValue(doc, "Orientation", 
			 "("+chan.an_orientation.azimuth+
			 ", "+chan.an_orientation.dip+")");
	if (chan.sampling_info != null) {
	    appendLabelValue(doc, "Sampling", 
			     chan.sampling_info.numPoints+" points in "+
			     chan.sampling_info.interval.toString()); 
	} else {
	    appendLabelValue(doc, "Sampling", "null value");
	}
	appendLabelValue(doc, "Begin", 
			 chan.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", 
			 chan.effective_time.end_time.date_time);
	appendLine(doc, "");
    }

    public void displayResponse(ChannelId chan, Instrumentation inst) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    doc.insertString(doc.getLength(), 
			     ResponsePrint.printResponse(chan, inst),
			     textPane.getStyle("response"));
            toTop();
	} catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        } 
    }

    // Drag and Drop...

    public void drop(DropTargetDropEvent e) {
	//System.err.println("[Target] drop");
        DropTargetContext targetContext = e.getDropTargetContext();

        boolean outcome = false;

//         if ((e.getSourceActions() & DnDConstants.ACTION_COPY) != 0) {
// 	    System.out.println("Action.COPY & was ok");
//             e.acceptDrop(DnDConstants.ACTION_COPY);
//         } else {
// 	    System.out.println("Action.COPY & didn't");
//             e.rejectDrop();
//             return;
//         }

	e.acceptDrop(DnDConstants.ACTION_COPY);


        DataFlavor[] dataFlavors = e.getCurrentDataFlavors();
        DataFlavor   transferDataFlavor = null;
	try {
	    for (int i = 0; i < dataFlavors.length; i++) {
		if (networkDataFlavor.equals(dataFlavors[i])) {
		    System.err.println("matched network");
		    transferDataFlavor = dataFlavors[i];
		    Transferable t  = e.getTransferable();
		    NetworkAccess net = 
			(NetworkAccess)t.getTransferData(transferDataFlavor);
		    appendNetwork(net.get_attributes());
		    outcome = true;
		    break;
		}

		System.err.println(dataFlavors[i].getMimeType());
		clear();
		if (edu.sc.seis.fissuresUtil.chooser.DNDLinkedList.listDataFlavor.equals(dataFlavors[i])) {
		    System.err.println("matched list");
		    transferDataFlavor = dataFlavors[i];
		    Transferable t  = e.getTransferable();
		    LinkedList list = 
			(LinkedList)t.getTransferData(transferDataFlavor);
		    Iterator it = list.iterator();
		    Object obj;
		    while (it.hasNext()) {
			obj = it.next();
			if (obj instanceof NetworkAccess) {
			    appendNetwork(((NetworkAccess)obj).get_attributes());
			    outcome = true;
			} else if (obj instanceof Station) {
			    appendStation((Station)obj);
			    outcome = true;
			} else if (obj instanceof Site) {
			    appendSite((Site)obj);
			    outcome = true;
			} else if (obj instanceof Channel) {
			    appendChannel((Channel)obj);
			    outcome = true;
			} // end of else
			
		    } // end of while (it.hasNext())
		    break;
		}
		
	    }

	} catch (BadLocationException bl) {
	    bl.printStackTrace();
	    System.err.println(bl.getMessage());
	    targetContext.dropComplete(false);
	    return;
	} catch (java.io.IOException ioe) {
	    ioe.printStackTrace();
	    System.err.println(ioe.getMessage());
	    targetContext.dropComplete(false);
	    return;
	} catch (UnsupportedFlavorException ufe) {
	    ufe.printStackTrace();
	    System.err.println(ufe.getMessage());
	    targetContext.dropComplete(false);
	    return;
	} // end of try-catch
	targetContext.dropComplete(outcome);
    }

    public void dragScroll(DropTargetDragEvent e) {
	System.err.println("[Target] dropScroll");
    }

    public void dropActionChanged(DropTargetDragEvent e) {
        System.err.println("[Target] dropActionChanged");
    }


    private DataFlavor networkDataFlavor =
	new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+
		       "; class="+edu.sc.seis.fissuresUtil.chooser.DNDNetworkAccess.class.getName(), 
		       "Seismic Network");

} // NetInfoDisplay
