package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.Vector;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * EventInfoDisplay.java
 *
 *
 * Created: Fri May 31 10:01:21 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version $Id: EventInfoDisplay.java 4355 2003-06-10 22:26:25Z oliverpa $
 */

public class EventInfoDisplay extends TextInfoDisplay
    implements DropTargetListener {

    public EventInfoDisplay (){
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void displayEvent(EventAccessOperations event) {
		displayEventStation(event, null);
    }

    public void displayEventStation(EventAccessOperations event, Station[] station) {
		Document doc = textPane.getDocument();
		try {
			doc.remove(0, doc.getLength());
			appendEvent(event, doc);
			if (station != null) {
				appendEventStation(event, station, doc);
			} // end of if (station != null)

			toTop();
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert message.");
		}
    }

    public void appendEvent(EventAccessOperations event) {
		try {
			appendEvent(event, getDocument());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert message.");
		}
    }

    public void appendEventStation(EventAccessOperations event, Station[] station) {
		try {
			Document doc = getDocument();
			appendEvent(event, doc);
			appendEventStation(event, station, getDocument());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert message.");
		}
    }

    protected void appendEvent(EventAccessOperations event, Document doc)
		throws BadLocationException {
		if ( event != null) {
			appendEventAttr(event.get_attributes(), doc);
			try {
				appendOrigin(event.get_preferred_origin(), doc);
			} catch (NoPreferredOrigin e) {

			} // end of try-catch
		} else {
			appendLine(doc, "No earthquake to display.");
		} // end of else


    }

    protected void appendEventStation(EventAccessOperations event,
									  Station[] station,
									  Document doc)
		throws BadLocationException {
		edu.sc.seis.TauP.SphericalCoords sph =
			new edu.sc.seis.TauP.SphericalCoords();
		appendLine(doc, "");
		appendHeader(doc, "Event to Station");
		double dist = -1;
		double baz = -1;
		appendLabelValue(doc, "    ", "\t\t\t\t\tAzimuth");
		appendLabelValue(doc, "    ", "\t Lat\tLong\tDist\t Dist\t  to");
		appendLabelValue(doc, "    ", "\t\t\t\t\t Event");
		appendLabelValue(doc, "    ", "\t(deg)\t(deg)\t(deg)\t (km)\t (deg)");
		appendLabelValue(doc, "----", "-------------------------------------------------------");

		try{
			station = sortStationsByDistance(station, event, sph);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		for (int i=0; i<station.length; i++) {
			try {
				if ( event != null) {
					dist = sph.distance(event.get_preferred_origin().my_location.latitude,
										event.get_preferred_origin().my_location.longitude,
										station[i].my_location.latitude,
										station[i].my_location.longitude);
					baz = sph.azimuth(station[i].my_location.latitude,
									  station[i].my_location.longitude,
									  event.get_preferred_origin().my_location.latitude,
									  event.get_preferred_origin().my_location.longitude);
					appendLabelValue(doc, station[i].get_code(),
									 '\t' + twoDecimal.format(station[i].my_location.latitude)+'\t'+
										 twoDecimal.format(station[i].my_location.longitude)+ '\t'+
										 twoDecimal.format(dist)+ '\t'+
										 twoDecimal.format(dist*111.19)+ '\t'+
										 twoDecimal.format(baz));
				} else {
					appendLabelValue(doc, station[i].get_code(),
									 twoDecimal.format(station[i].my_location.latitude)+
										 " "+twoDecimal.format(station[i].my_location.longitude)+
										 "--- ,  ---");

				} // end of else

			} catch (NoPreferredOrigin e) {
				appendLabelValue(doc, station[i].get_code(),
								 twoDecimal.format(station[i].my_location.latitude)+
									 " "+twoDecimal.format(station[i].my_location.longitude)+
									 "--- ,  ---");
			} // end of try-catch

		} // end of for (int i=0; i<station.length; i++)
		appendLine(doc, "");
	}


	protected void appendEventAttr(EventAttr attr)
		throws BadLocationException {
		appendEventAttr(attr, getDocument());
	}

	protected void appendEventAttr(EventAttr attr, Document doc)
		throws BadLocationException {
		appendHeader(doc, "Event");
		appendLabelValue(doc, "Name\t", attr.name);
		if (attr.region.number > 0) {
			appendLabelValue(doc, "Region\t", feRegions.getRegionName(attr.region)+" ("+attr.region.number+")");
		} else {
			appendLabelValue(doc, "Region\t", "Unknown ("+attr.region.number+")");
		} // end of else


		appendLine(doc, "");
	}

	protected void appendOrigin(Origin origin)
		throws BadLocationException {
		appendOrigin(origin, getDocument());
	}

	protected void appendOrigin(Origin origin, Document doc)
		throws BadLocationException {
		appendHeader(doc, "Origin");
		appendLabelValue(doc, "Location\t", "latitude="+
							 twoDecimal.format(origin.my_location.latitude)+
							 ",  longitude="+
							 twoDecimal.format(origin.my_location.longitude));
		MicroSecondDate oTime = new ISOTime(origin.origin_time.date_time).getDate();
		appendLabelValue(doc, "Time\t", dateFormat.format(oTime));
		QuantityImpl depth = (QuantityImpl)origin.my_location.depth;
		depth = depth.convertTo(UnitImpl.KILOMETER);
		appendLabelValue(doc, "Depth\t",
						 twoDecimal.format(depth.value)+" kilometers");
		//  ((UnitImpl)depth.the_units).toString());
		//	appendLabelValue(doc, "ID", origin.get_id());
		//appendLabelValue(doc, "Catalog", origin.catalog);
		//appendLabelValue(doc, "Contributor", origin.contributor);

		appendLine(doc, "");
		for (int i=0; i<origin.magnitudes.length; i++) {
			appendMagnitude(origin.magnitudes[i], doc);
		} // end of for (int i=0; i<origin.magnitudes.length; i++)

	}

	protected void appendMagnitude(Magnitude mag)
		throws BadLocationException {
		appendMagnitude(mag, getDocument());
	}

	protected void appendMagnitude(Magnitude mag, Document doc)
		throws BadLocationException {
		appendLabelValue(doc, "Magnitude\t", mag.value+" "+mag.type+"  "+mag.contributor);
	}

	public static Station[] sortStationsByDistance(Station[] stations,
												   EventAccessOperations event,
												   edu.sc.seis.TauP.SphericalCoords sph)
		throws NoPreferredOrigin{
		try{
			Station[] temp = new Station[stations.length];
			System.arraycopy(stations, 0, temp, 0, stations.length);

			int indexOfNextSmallest;

			for (int i = 0; i < temp.length - 1; i++) {
				indexOfNextSmallest = indexOfClosestStation(temp, i, event, sph);
				interchange(i, indexOfNextSmallest, temp);
			}

			return temp;
		}
		catch(NoPreferredOrigin n){
			System.out.println("Stations not sorted because event has no origin.");
			return stations;
		}
	}

	public static void interchange(int i, int j, Station[] s){
		Station temp;

		temp = s[i];
		s[i] = s[j];
		s[j] = temp;
	}

	public static int indexOfClosestStation(Station[] stations, int startIndex,
											EventAccessOperations event,
											edu.sc.seis.TauP.SphericalCoords sph)
		throws NoPreferredOrigin{

		int index = startIndex;
		double currentDistance = Double.POSITIVE_INFINITY;
		double closestDistance = Double.POSITIVE_INFINITY;
		Station currentStation;


		for (int i = startIndex; i < stations.length; i++){
			currentStation = stations[i];
			currentDistance = sph.distance(event.get_preferred_origin().my_location.latitude,
										   event.get_preferred_origin().my_location.longitude,
										   currentStation.my_location.latitude,
										   currentStation.my_location.longitude);
			if (currentDistance < closestDistance){
				closestDistance = currentDistance;
				index = i;
			}
		}
		System.out.println("Size of stations: " + stations.length);
		return index;
	}

	static ParseRegions feRegions = new ParseRegions();


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
						if (obj instanceof Origin) {
							appendOrigin((Origin)obj);
							outcome = true;
						} else if (obj instanceof EventAccessOperations) {
							appendEvent((EventAccessOperations)obj);
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

	DecimalFormat twoDecimal = new DecimalFormat("0.00");
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.S z");

}// EventInfoDisplay

