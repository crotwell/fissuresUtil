
package edu.sc.seis.fissuresUtil.chooser;

import edu.sc.seis.fissuresUtil.cache.*;
import java.awt.*;
import java.awt.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.display.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.apache.log4j.*;


/**
 * ChannelChooser.java
 * 
 * Description: This class creates a list of networks and their respective stations and channels. A non-null NetworkDC reference must be supplied in the constructor, then use the get methods to obtain the necessary information that the user clicked on with the mouse. It takes care of action listeners and single click mouse button.
 *
 * @author Philip Crotwell
 * @version $Id: ChannelChooser.java 1589 2002-05-03 18:24:07Z crotwell $
 *
 */


public class ChannelChooser extends JPanel{

    public ChannelChooser(NetworkDC netdcgiven) {
	this(netdcgiven,
	     false,
	     defaultSelectableOrientations,
	     defaultAutoSelectedOrientation,
	     defaultSelectableBand,
	     defaultAutoSelectBand);
    }

    public ChannelChooser(NetworkDC netdcgiven, 
			  boolean showSites,
			  int[] selectableOrientations,
			  int autoSelectedOrientation,
			  String[] selectableBandGain,
			  String[] autoSelectBandGain){
	this.showSites = showSites;
	this.selectableOrientations = selectableOrientations;
	this.autoSelectedOrientation = autoSelectedOrientation;
	this.selectableBandGain = selectableBandGain;
	this.autoSelectBandGain = autoSelectBandGain;
	bundle = ResourceBundle.getBundle(ChannelChooser.class.getName());
        initFrame();
	setNetworkDC(netdcgiven);
    }

    public void setNetworkDC(NetworkDC netdcgiven) {
	netdc = netdcgiven;
	channels.clear();
	sites.clear();
	stations.clear();
	networks.clear();
	Thread networkLoader = new Thread() {
		public void run() {
		    CacheNetworkAccess cache;
		    NetworkAccess[] nets = 
			netdc.a_finder().retrieve_all();
		    logger.debug("Got networks");
		    for (int i=0; i<nets.length; i++) {
			cache = new CacheNetworkAccess(nets[i]);
			NetworkAttr attr = cache.get_attributes();
			logger.debug("Got attributes "+attr.get_code());
			// preload attributes
			networks.addElement(cache);
		    }
		    if (nets.length == 1) {
			networkList.getSelectionModel().setSelectionInterval(0,0);
		    } // end of if (nets.length = 1)
		    
		}
	    };
	networkLoader.start();
    }

    public void initFrame(){
	//	setSize(new java.awt.Dimension (mywidth, myheight));
	//setPreferredSize(new java.awt.Dimension (mywidth, myheight));
   
	setLayout(new GridBagLayout());
	gbc = new GridBagConstraints();
	gbc.fill = gbc.BOTH;
	gbc.weightx = 1.0;
	gbc.weighty = 0.0;
	gbc.gridx = 0;
	gbc.gridy = 0;

	JLabel netLabel = new JLabel(bundle.getString("LABEL_NETWORKS"));
	JLabel staLabel = new JLabel(bundle.getString("LABEL_STATIONS"));
	JLabel siLabel = new JLabel(bundle.getString("LABEL_SITES"));
	JLabel orientationLabel = new JLabel(bundle.getString("LABEL_ORIENTATIONS"));
	JLabel chLabel = new JLabel(bundle.getString("LABEL_CHANNELS"));
	netLabel.setToolTipText(lnettip);
	staLabel.setToolTipText(lstatip);
	siLabel.setToolTipText(lsittip);
	chLabel.setToolTipText(lchatip);
	
	add(netLabel, gbc);
	gbc.gridx++;
	add(staLabel, gbc);
	gbc.gridx++;
	if (showSites) {
	    add(siLabel, gbc);
	    gbc.gridx++;
	} // end of if (showSites)
	add(orientationLabel, gbc);
	gbc.gridx++;
	add(chLabel, gbc);
	gbc.gridx++;
	
	gbc.gridy++;
	gbc.gridx = 0;
	gbc.weighty = 1.0;
	final ListCellRenderer renderer = new NameListCellRenderer(true);

	networkList = new JList(networks);
	networkList.setCellRenderer(renderer);
	networkList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	networkList.addListSelectionListener(new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
		    if(e.getValueIsAdjusting()){
			return;
		    }
		    final NetworkAccess[] nets = getSelectedNetworks();
		    Thread stationLoader = new Thread() {
			    public void run() {
				try {
				    stations.clear();
				    for (int i=0; i<nets.length; i++) {
					Station[] newStations = nets[i].retrieve_stations();
					logger.debug("got "+newStations.length+" stations");
					stations.clear();
					for (int j=0; j<newStations.length; j++) {
					    stations.addElement(newStations[j]);
					    try {
						sleep(1*1000); 
					    } catch (InterruptedException e) {
						
					    } // end of try-catch
					    
					}
					logger.debug("finished adding stations");
					     try {
						sleep(1*1000); 
					    } catch (InterruptedException e) {
						
					    } // end of try-catch
					    
				    } // end of for ((int i=0; i<nets.length; i++)
				    logger.debug("There are "+stations.getSize()+" items in the station list model");
				    // stationList.validate();
				    //stationList.repaint();
				} catch (Exception e) {
				    edu.sc.seis.fissuresUtil.exceptionHandlerGUI.ExceptionHandlerGUI.handleException(e);
				} // end of try-catch
			    }
			};
		    stationLoader.start();
		}
	    } );

	JScrollPane scroller = new JScrollPane(networkList);
	add(scroller, gbc);
	gbc.gridx++;

	stationList = new JList(stations);
	stationList.setCellRenderer(renderer);
	stationList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	stationList.addListSelectionListener(new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
		    if(e.getValueIsAdjusting()){
			return;
		    }
		    ListSelectionModel selModel = stationList.getSelectionModel();
		    // assume only one selected network at at time...
		    NetworkAccess[] nets = getSelectedNetworks();
		    NetworkAccess net = nets[0];
		    for (int i=e.getFirstIndex(); i<=e.getLastIndex(); i++) {
			if (stationList.isSelectedIndex(i)) {
			    Station selectedStation = 
				(Station)stations.getElementAt(i);
			    Channel[] chans =
				net.retrieve_for_station(selectedStation.get_id());
			    for (int j=0; j<chans.length; j++) {
				String chanKey = ChannelIdUtil.toString(chans[j].get_id());
				if ( ! channelMap.containsKey(chanKey)) {
				    channelMap.put(chanKey, chans[j]);
				    if ( ! sites.contains(chans[j].my_site.get_code())) {
					sites.addElement(chans[j].my_site.get_code());
				    }
				    if ( ! channels.contains(chans[j].get_code())) {
					channels.addElement(chans[j].get_code());
				    }
				}
			    } // end of for (int j=0; j<chans.length; j++)
			    
			} else {
			    Station selectedStation = 
				(Station)stations.getElementAt(i);
			    Channel[] chans =
				net.retrieve_for_station(selectedStation.get_id());
			    for (int j=0; j<chans.length; j++) {
				String chanKey = ChannelIdUtil.toString(chans[j].get_id());
				if ( channelMap.containsKey(chanKey)) {
				    channelMap.remove(chanKey);
				}
			    }
			}
			
		    } // end of for (int i=e.getFirstIndex(); i<e.getLastIndex(); i++)
		}
	    }
					 );
	scroller = new JScrollPane(stationList);
	add(scroller, gbc);
	gbc.gridx++;
 
	siteList = new JList(sites);
	siteList.setCellRenderer(renderer);
	siteList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	scroller = new JScrollPane(siteList);
	if (showSites) {
	    add(scroller, gbc);
	    gbc.gridx++;
	}	

	final ListCellRenderer bundleRenderer = new BundleListCellRenderer();

	String[] orientationTypes = new String[4];
	orientationTypes[THREE_COMPONENT] = "THREE_COMPONENT"; 
	orientationTypes[VERTICAL_ONLY] = "VERTICAL_ONLY";
	orientationTypes[HORIZONTAL_ONLY] = "HORIZONTAL_ONLY";
	orientationTypes[INDIVIDUAL_CHANNELS] = "INDIVIDUAL_CHANNELS";

	orientationList = new JList(orientationTypes);
	orientationList.setCellRenderer(bundleRenderer);
	orientationList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	orientationList.getSelectionModel().setSelectionInterval(autoSelectedOrientation, autoSelectedOrientation);	
	orientationList.addListSelectionListener(new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
		    if(e.getValueIsAdjusting()){
			return;
		    }
		    String selected = (String)orientationList.getSelectedValue();
		    if ((selected.equals("THREE_COMPONENT") ||
			 selected.equals("VERTICAL_ONLY") ||
			 selected.equals("HORIZONTAL_ONLY"))
			&& channelList.getModel() != bandListModel) {
			channelList.setModel(bandListModel);
			channelList.setCellRenderer(bundleRenderer);
		    } else if (selected.equals("INDIVIDUAL_CHANNELS") 
			       && channelList.getModel() != channels) {
			channelList.setModel(channels);
			channelList.setCellRenderer(renderer);
		    }
		}
	    });
	scroller = new JScrollPane(orientationList);
	add(scroller, gbc);
	gbc.gridx++;

	bandListModel.addElement("LONG_PERIOD");
	bandListModel.addElement("BROAD_BAND");
	bandListModel.addElement("SHORT_PERIOD");
	bandListModel.addElement("VERY_LONG_PERIOD");
	bandListModel.addElement("ULTRA_LONG_PERIOD");
	bandListModel.addElement("EXTREMELY_LONG_PERIOD");
	bandListModel.addElement("MID_PERIOD");
	bandListModel.addElement("EXTREMELY_SHORT_PERIOD");
	bandListModel.addElement("HIGH_BROAD_BAND");
	bandListModel.addElement("ADMINISTRATIVE");
	bandListModel.addElement("WEATHER_ENVIRONMENTAL");
	bandListModel.addElement("EXPERIMENTAL");

	if (autoSelectedOrientation == INDIVIDUAL_CHANNELS) {
	    channelList = new JList(channels);
	    channelList.setCellRenderer(renderer);
	} else {
	    channelList = new JList(bandListModel);
	    channelList.setCellRenderer(bundleRenderer);
	} // end of else
	
	channelList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	ListModel chanListModel = channelList.getModel();
	ListSelectionModel channelSelctionModel 
	    = channelList.getSelectionModel();
	for (int j=0; j<autoSelectBandGain.length; j++) {
	    for (int i=0; i<chanListModel.getSize(); i++) {
		String listElement = (String)chanListModel.getElementAt(i);
		System.out.println(bundle.getString("CODE_"+listElement)+" = "+autoSelectBandGain[j]);
		if (bundle.getString("CODE_"+listElement).equals(autoSelectBandGain[j])) {
		    channelSelctionModel.addSelectionInterval(i,i);
		    break;
		}
	    } // end of for (int i=0; i<chanListModel; i++)
	} // end of for (int j=0; j<autoSelectBandGain.length; j++)
	
	scroller = new JScrollPane(channelList);
	add(scroller, gbc);
	gbc.gridx++;
    }

    public NetworkAccess[] getNetworks(){
	Object[] objArray = networks.toArray();
	return castNetworkArray(objArray);
    }      

    protected NetworkAccess[] castNetworkArray(Object[] objArray){
	NetworkAccess[] nets 
	    = new NetworkAccess[objArray.length];
	for (int i=0; i<nets.length; i++) {
	    nets[i] = (NetworkAccess)objArray[i];
	}
	return nets;
    }

    public Station[]  getStations(){
	Object[] objArray = stations.toArray();
	return castStationArray(objArray);
    }

    protected Station[] castStationArray(Object[] objArray){
	Station[] sta 
	    = new Station[objArray.length];
	for (int i=0; i<sta.length; i++) {
	    sta[i] = (Station)objArray[i];
	}
	return sta;
    }

    public Site[]  getSites(){
	Object[] objArray = sites.toArray();
	HashMap outSites = new HashMap();
	for (int i=0; i<objArray.length; i++) {
	    for (int j=0; j<1; j++) {
		 
	    } // end of for (int j=0; j<1; j++)
	    
	} // end of for (int i=0; i<objArray.length; i++)
	
	return castSiteArray(objArray);
    }

    protected Site[] castSiteArray(Object[] objArray){
	Site[] site 
	    = new Site[objArray.length];
	for (int i=0; i<site.length; i++) {
	    site[i] = (Site)objArray[i];
	} 
	return site;
    }

    public Channel[]  getChannels(){
	Channel[] outChannels = 
	    (Channel[])channelMap.values().toArray(new Channel[0]);
	return outChannels;
    }

    protected Channel[] castChannelArray(Object[] objArray){
	Channel[] chan 
	    = new Channel[objArray.length];
	for (int i=0; i<chan.length; i++) {
	    chan[i] = (Channel)objArray[i];
	} 
	return chan;
    }

    public NetworkAccess[] getSelectedNetworks(){
	return castNetworkArray(networkList.getSelectedValues());
    }      

    public Station[]  getSelectedStations(){
        return castStationArray(stationList.getSelectedValues());
    }

    public Site[]  getSelectedSites(){
        return castSiteArray(siteList.getSelectedValues());
    }

    public static final String[] siteCodeHeuristic = { "00", "  ", "01" };

    public Channel[]  getSelectedChannels(){
	Channel[] inChannels = getChannels();
	LinkedList outChannels = new LinkedList();
	Station[] selectedStations = getSelectedStations();
	Object[] selectedChannelCodes = channelList.getSelectedValues();

	// assume only one selected network
	NetworkAccess[] nets = getSelectedNetworks();
	NetworkAccess net = nets[0];
	for (int staNum=0; staNum<selectedStations.length; staNum++) {
	    Channel[] staChans = 
		net.retrieve_for_station(selectedStations[staNum].get_id());
	    if ( ! showSites) {
		if (orientationList.getSelectedValue().equals("INDIVIDUAL_CHANNELS")) {
		    // use real channel codes
		    bandSearch:
		    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
			for (int chanNum=0; chanNum< staChans.length; chanNum++) {
			    for (int h=0; h<siteCodeHeuristic.length; h++) {
				if (staChans[chanNum].my_site.get_code().equals(siteCodeHeuristic[h]) && staChans[chanNum].get_code().equals(selectedChannelCodes[bandNum])) {
				    outChannels.add(staChans[chanNum]);
				    continue bandSearch;
				}
			    }
			}
		    }
		    
		    // end of if INDIVIDUAL_CHANNELS
		} else if (orientationList.getSelectedValue().equals("THREE_COMPONENT")) {
		    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
			Channel tmpV = getBestVerticalChannel(staChans, 
						      (String)selectedChannelCodes[bandNum]);
			Channel[] tmpH = getBestHorizontalChannels(staChans, 
							(String)selectedChannelCodes[bandNum]);
			if (tmpV != null && 
			    tmpH != null && 
			    tmpV.my_site.get_code().equals(tmpH[0].my_site.get_code()) && 
			    tmpV.get_code().substring(1,2).equals(tmpH[0].get_code().substring(1,2)) &&
			    tmpV.my_site.get_code().equals(tmpH[1].my_site.get_code()) && 
			    tmpV.get_code().substring(1,2).equals(tmpH[1].get_code().substring(1,2))) {
			    outChannels.add(tmpV);
			    outChannels.add(tmpH[0]);
			    outChannels.add(tmpH[1]);
			} // end of if (tmp != null)
		    } // end of else
		} else if (orientationList.getSelectedValue().equals("VERTICAL_ONLY")) {
		    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
			Channel tmp = getBestVerticalChannel(staChans, 
						      (String)selectedChannelCodes[bandNum]);
			if (tmp != null) {
			    outChannels.add(tmp);
			} // end of if (tmp != null)
		    }
		} else if (orientationList.getSelectedValue().equals("HORIZONTAL_ONLY")) {
		    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
			Channel[] tmp = getBestHorizontalChannels(staChans, 
							(String)selectedChannelCodes[bandNum]);
			if (tmp != null) {
			    outChannels.add(tmp[0]);
			    outChannels.add(tmp[1]);
			} // end of if (tmp != null)
		    } // end of else
		}		
	    } else {
		// pay attention to selected Sites
		Object[] selectedSiteCodes = siteList.getSelectedValues();
		search:
		for (int i=0; i<inChannels.length; i++) {
		    for (int j=0; j<selectedSiteCodes.length; j++) {
			for (int k=0; k<selectedChannelCodes.length; k++) {
			    if (inChannels[i].my_site.get_code().equals(selectedSiteCodes[j]) 
				&& inChannels[i].get_code().equals(selectedChannelCodes[k])) {
				outChannels.add(inChannels[i]);
				continue search;
			    }
			}
		    }
		}
	

	    } // end of if (showSites)
	     
	} // end of for (int staNum=0; staNum<selStation.length; staNum++)
	
	System.out.println("Found "+outChannels.size()+" chanels");
        return (Channel[])outChannels.toArray(new Channel[0]);
    }

    /** finds the best vertical channel for the band code. All channels are
     * assumed to come from the same station. 
     * @returns best vertical channel, or null if no vertical can be found
    */	
    protected Channel getBestVerticalChannel(Channel[] inChan, 
					     String bandCode) {
	return getBestChannel(inChan, bandCode, "Z");
    }

    /** finds the best vertical channel for the band code. All channels are
     * assumed to come from the same station. Makes sure that the 2 channels
     * have the same gain and site.
     * @returns best vertical channel, or null if no vertical can be found
    */	
    protected Channel[] getBestHorizontalChannels(Channel[] inChan, 
						  String bandCode) {
	// try to find N,E
	Channel north = getBestChannel(inChan, bandCode, "N");
	Channel east = getBestChannel(inChan, bandCode, "E");
	if (north != null && 
	    east != null && 
	    north.my_site.get_code().equals(east.my_site.get_code()) && 
	    north.get_code().substring(1,2).equals(east.get_code().substring(1,2))) {
	    Channel[] tmp = new Channel[2];
	    tmp[0] = north;
	    tmp[1] = east;
	    return tmp;
	}
									     
	// try to find 1,2
	north = getBestChannel(inChan, bandCode, "1");
	east = getBestChannel(inChan, bandCode, "2");
	if (north != null && 
	    east != null && 
	    north.my_site.get_code().equals(east.my_site.get_code()) && 
	    north.get_code().substring(1,2).equals(east.get_code().substring(1,2))) {
	    Channel[] tmp = new Channel[2];
	    tmp[0] = north;
	    tmp[1] = east;
	    return tmp;
	}
									     
	return null;
    }

    protected Channel getBestChannel(Channel[] inChan, 
				     String bandCode,
				     String orientationCode) {
	System.out.println("getBestChannel"+ bandCode+orientationCode);
	String bc = bundle.getString("CODE_"+bandCode);
	for (int h=0; h<siteCodeHeuristic.length; h++) {
	    for (int chanNum=0; chanNum<inChan.length; chanNum++) {
		if (inChan[chanNum].my_site.get_code().equals(siteCodeHeuristic[h]) 
		    && inChan[chanNum].get_code().endsWith(orientationCode)
		    && inChan[chanNum].get_code().startsWith(bc)) {
		    return inChan[chanNum];
				}
	    }
	}

	// can't find one by hueristic, just find one
	for (int chanNum=0; chanNum<inChan.length; chanNum++) {
	    if (inChan[chanNum].get_code().endsWith(orientationCode)
		&& inChan[chanNum].get_code().startsWith(bc)) {
		return inChan[chanNum];
	    }
	}
	// oh well, return null
	System.out.println("can't find"+ bc+orientationCode);
	return null;
    }

   /*================Class Variables===============*/

    protected boolean showSites;
    protected String[] selectableBandGain;
    protected String[] autoSelectBandGain;
    protected int[] selectableOrientations;
    protected int autoSelectedOrientation;

    protected ResourceBundle bundle;

    String lnettip = "Source of data";
    String lstatip = "Station";
    String lsittip = "Seismometer site";
    String lchatip = "Seismometer channels"; 
    String gotip = "Searches and retrieves a seismogram";
    String closetip = "Hide this window";
    String thistip = "Select date and location to obtain seismogram";
    String bhztip = "B=Broad Band | H=High Gain Seismometer | Z=Vertical"; 
    String bhetip = "B=Broad Band | H=High Gain Seismometer | E=East-West";
    String bhntip = "B=Broad Band | H=High Gain Seismometer | N=North-South";
 
    public static final int THREE_COMPONENT = 0;
    public static final int VERTICAL_ONLY = 1;
    public static final int HORIZONTAL_ONLY = 2;
    public static final int INDIVIDUAL_CHANNELS = 3;

    public static final String EXTREMELY_SHORT_PERIOD = "E";
    public static final String SHORT_PERIOD = "S";
    public static final String HIGH_BROAD_BAND = "H";
    public static final String BROAD_BAND = "B";
    public static final String MID_PERIOD = "M";
    public static final String LONG_PERIOD = "L";
    public static final String VERY_LONG_PERIOD = "V";
    public static final String ULTRA_LONG_PERIOD = "U";
    public static final String EXTREMELY_LONG_PERIOD = "R";
    public static final String ADMINISTRATIVE = "A";
    public static final String WEATHER_ENVIRONMENTAL = "W";
    public static final String EXPERIMENTAL = "X";

    private static final String[] defaultSelectableBand = { BROAD_BAND, 
							LONG_PERIOD };
    private static final String[] defaultAutoSelectBand = { LONG_PERIOD };
    private static final int[] defaultSelectableOrientations 
	= { THREE_COMPONENT, 
	    VERTICAL_ONLY, 
	    HORIZONTAL_ONLY, 
	    INDIVIDUAL_CHANNELS };
    private static final int defaultAutoSelectedOrientation = THREE_COMPONENT;

    protected JList networkList;
    protected JList stationList;
    protected JList siteList;
    protected JList bandList;
    protected JList orientationList;
    protected JList channelList;

    protected DefaultListModel networks = new DefaultListModel();
    protected DefaultListModel stations = new DefaultListModel();
    protected DefaultListModel sites = new DefaultListModel();
    protected DefaultListModel channels = new DefaultListModel();
    protected DefaultListModel bandListModel = new DefaultListModel();
    protected HashMap channelMap = new HashMap();

    private NetworkDC netdc;

    private GridBagConstraints gbc;
    int x_leftcorner=0;
    int y_leftcorner=0;

    int mywidth = 400;
    int myheight = 200;

    class NameListCellRenderer extends DefaultListCellRenderer {
	NameListCellRenderer(boolean useNames){
	    this.useNames = useNames;
	}

	public Component getListCellRendererComponent(JList list,
						      Object value,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus) {
	    String name = "XXXX";
	    if (value instanceof NetworkAccess) {
		if (useNames) {
		    name = ((NetworkAccess)value).get_attributes().name;
		    if (name == null || name.length() == 0) {
			name = ((NetworkAccess)value).get_attributes().get_code();
			if (name.startsWith("X") || name.startsWith("Y") || name.startsWith("Z")) {
			    edu.iris.Fissures.Time start = 
				((NetworkAccess)value).get_attributes().get_id().begin_time;
			    name += start.date_time.substring(2,4);
			} // end of if (name.startsWith("X"))
			
		    }
		} else {
		    name = ((NetworkAccess)value).get_attributes().get_code();
		}
	    }
	    if (value instanceof Station) {
		if (useNames) {
		    name = ((Station)value).name;
		    if (name == null || name.length() == 0) {
			name = ((Station)value).get_code();
		    }
		} else {
		    name = ((Station)value).get_code();
		}
	    }
	    if (value instanceof Site) {
		if (useNames) {
		    name = ((Site)value).get_code();
		    if (name == null || name.length() == 0) {
			name = ((Site)value).get_code();
		    }
		} else {
		    name = ((Site)value).get_code();
		}
	    }
	    
	    if (value instanceof Channel) {
		if (useNames) {
		    name = ((Channel)value).name;
		    if (name == null || name.length() == 0) {
			name = ((Channel)value).get_code();
		    }
		} else {
		    name = ((Channel)value).get_code();
		}
	    }

	    if (value instanceof String) {
		name = (String)value;
	    } // end of if (value instanceof String)
	    
	    
	    return super.getListCellRendererComponent(list, 
						      name, 
						      index, 
						      isSelected, 
						      cellHasFocus);
	}
	boolean useNames;
    }


    class BundleListCellRenderer extends DefaultListCellRenderer {
	BundleListCellRenderer(){
	   
	}

	public Component getListCellRendererComponent(JList list,
						      Object value,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus) {
	    Object useValue = value;
	    String name = "XXXX";
	    try {
		name = bundle.getString((String)value);
		useValue = name;
	    } catch (java.util.MissingResourceException e) {
		try {
		    // try NAME_value
		    name = bundle.getString("NAME_"+(String)value);
		    useValue = name;
		} catch (java.util.MissingResourceException ee) {
		    // use default value???
		    useValue = value;
		} // end of try-catch
	    } // end of try-catch
	    
	    if (useValue.equals(name) && (name == null || name.length() == 0)){
		useValue = value;
	    } // end of if (name == null || name.length == 0)
	    
	    return super.getListCellRendererComponent(list, 
						      useValue, 
						      index, 
						      isSelected, 
						      cellHasFocus);
	}


    }

    static Category logger = 
	Category.getInstance(ChannelChooser.class.getName());
} // ChannelChooser



