
package edu.sc.seis.fissuresUtil.chooser;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.display.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.*;
import java.io.*;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import java.util.*;
import java.text.*;

/**
 * ChannelChooser.java
 *
 *
 * @author: Georgina Coleman 
 * @version
 * 12/04/2001
 */


public class ChannelChooser{


    public ChannelChooser(NetworkDCOperations netdcgiven){
	netdc = (NetworkDC)netdcgiven;

 
    }

    public ChannelChooser(NetworkFinder netgiven){
	netfound = netgiven;

 
    }

     protected void findChannels() {

	 /* Changed based on the new IDL with an array of Plottable*/
	BoxAreaImpl box = new BoxAreaImpl(20, 40, -100, -70);
        int max=10;
	edu.iris.Fissures.IfNetwork.ChannelGroupIterHolder iterholder = new edu.iris.Fissures.IfNetwork.ChannelGroupIterHolder();

        ChannelId[][] allChans =netdc.a_explorer().locate_all(box, max, iterholder);
        /* end of change */

	Set netSet = new HashSet();
	Set staSet = new HashSet();
	Set siteSet = new HashSet();
	Set chanSet = new HashSet();
	
	for (int i=0; i<allChans.length; i++) {
	    for (int j=0; j<allChans[i].length; j++) {
		//System.out.println(ChannelIdUtil.toStringNoDates(allChans[i][j]));
		allchanMap.put(ChannelIdUtil.toStringNoDates(allChans[i][j]),
			    allChans[i][j]);
		netSet.add(allChans[i][j].network_id.network_code);
		staSet.add(allChans[i][j].station_code);
		siteSet.add(allChans[i][j].site_code);
		chanSet.add(allChans[i][j].channel_code);
	    }
	}

	TreeSet staTree = new TreeSet(staSet);
	stations = (String[])staTree.toArray(new String[0]);

	networks = (String[])netSet.toArray(new String[0]);
	//stations = (String[])staSet.toArray(new String[0]);
	sites = (String[])siteSet.toArray(new String[0]);
	channels = (String[])chanSet.toArray(new String[0]);

	/* set up channels zne instead of enz */
	for (int i=0; i<channels.length; i++) {
	    if (channels[i].endsWith("Z")) {
		String tmp = channels[0];
		channels[0] = channels[i];
		channels[i] = tmp;
	    } /* end of if (channels[i].endsWith("Z")) */
	}	    
    }

    protected void  setNetDC(NetworkDCOperations netdcgiven) {
	netdc = (NetworkDC)netdcgiven;	
    }

    protected void  setNetworks() {

	edu.iris.Fissures.IfNetwork.NetworkAccess[] allNets = new
                             edu.iris.Fissures.IfNetwork.NetworkAccess[0];

        System.out.println("1setNetworks() ");
        if(netdc != null) {
	    edu.iris.Fissures.IfNetwork.NetworkFinder netfromdc = netdc.a_finder();
        
            System.out.println("2setNetworks() ");

            allNets = netfromdc.retrieve_all();
            System.out.println("3 retrieve_all() ");

        } else if(netfound != null) {
            allNets = netfound.retrieve_all();
	} else { 	   
	    System.out.println("netdc or netfinder is null");
	    return;
	}


	Set netSet = new HashSet();

	for (int i=0; i< allNets.length; i++) {
	    NetworkAttr attr = allNets[i].get_attributes();
System.out.println("4 allNets[i].get_attributes() ");

	    NetworkId netid = attr.get_id();
System.out.println("5 attr.get_id() ");

	    String netCode = netid.network_code;
System.out.println("6 netid.network_code ");

	    // store NetworkId/NetworkAccess in a Map to get latter
	    netMap.put(netCode, allNets[i]);
System.out.println("7 netMap.put ");

	    netSet.add(netCode);
System.out.println("8 netSet.add ");

	}

	TreeSet netTree = new TreeSet(netSet);
	networks = (String[])netTree.toArray(new String[0]);
 
	// networks JList add (netCode);

    }


    public void  setStations(String netcode) {


	edu.iris.Fissures.IfNetwork.NetworkAccess netAccess = (edu.iris.Fissures.IfNetwork.NetworkAccess) netMap.get(netcode);
	edu.iris.Fissures.IfNetwork.Station[] allstations = netAccess.retrieve_stations();

	Set staSet = new HashSet();

	for (int i=0; i < allstations.length; i++) {
	    StationId id = allstations[i].get_id();
	    String stationCode = id.station_code;
	    NetworkAttr attr = netAccess.get_attributes();

	    staMap.put(stationCode, allstations[i]);

	    staSet.add(stationCode);// maybe needs station_id
	} 


	TreeSet staTree = new TreeSet(staSet);
	stations = (String[])staTree.toArray(new String[0]);

	// add to station JList


    }

    public void setChannels(String netCode, String stationCode) {

	
	NetworkAccess netAccess = (edu.iris.Fissures.IfNetwork.NetworkAccess) netMap.get(netCode);
	Station station = (edu.iris.Fissures.IfNetwork.Station) staMap.get(stationCode);

	Channel[] allChannels = netAccess.retrieve_for_station(station.get_id());


	
	for (int i=0; i<allChannels.length; i++) {

	    edu.iris.Fissures.IfNetwork.Site thesite = allChannels[i].my_site;
	    edu.iris.Fissures.IfNetwork.SiteId thesiteid = thesite.get_id();
	    String siteCode = thesiteid .site_code;
	    sitMap.put(siteCode, thesite);
        
	    edu.iris.Fissures.IfNetwork.ChannelId thechannelid = allChannels[i].get_id();
	    String channelCode = thechannelid.channel_code;
	    chanMap.put(channelCode, allChannels[i]);
            
	}
	
 	TreeSet sitTree = new TreeSet(sitMap.keySet());
	sites = (String[])sitTree.toArray(new String[0]);

 	TreeSet chaTree = new TreeSet(chanMap.keySet());
	channels = (String[])chaTree.toArray(new String[0]);

    }

    public String[] getNetworks() {
	return networks;

    }


    public String[] getStations() {
	return stations;
    }


    public String[] getSites() {
	return sites;
    }


    public String[] getChannels() {
	return channels;
    }

   

    /*================Class Variables===============*/

    protected HashMap allchanMap = new HashMap(); 

    private NetworkDC netdc;
    private NetworkFinder netfound;
    private PlottableDC plottableRef;

 
    protected String[] networks;
    protected String[] stations;
    protected String[] sites;
    protected String[] channels;

    protected TreeMap netMap = new TreeMap();
    protected TreeMap staMap = new TreeMap();
    protected TreeMap sitMap = new TreeMap();  
    protected TreeMap chanMap = new TreeMap();  


} // PlottableClient



