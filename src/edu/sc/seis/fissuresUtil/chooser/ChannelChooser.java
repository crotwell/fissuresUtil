
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



/**
 * ChannelChooser.java
 * 
 * Description: This class creates a list of networks and their respective stations and channels. A non-null NetworkDC reference must be supplied in the constructor, then use the get methods to obtain the necessary information that the user clicked on with the mouse. It takes care of action listeners and single click mouse button.
 *
 * @author Philip Crotwell
 * @version $Id: ChannelChooser.java 1567 2002-05-01 21:06:02Z crotwell $
 *
 */


public class ChannelChooser extends JPanel{

    public ChannelChooser(NetworkDC netdcgiven) {
	this(netdcgiven,
	     false,
	     THREE_COMPONENT,
	     defaultSelectable,
	     defaultAutoSelect);
    }

    public ChannelChooser(NetworkDC netdcgiven, 
			  boolean showSites,
			  int orientationConfig,
			  String[] selectableBandGain,
			  String[] autoSelectBandGain){
        initFrame();
	setNetworkDC(netdcgiven);
	this.orientationConfig = orientationConfig;
    }

    public void setNetworkDC(NetworkDC netdcgiven) {
	netdc = netdcgiven;
	channels.clear();
	sites.clear();
	stations.clear();
	networks.clear();
	Thread networkLoader = new Thread() {
		public void run() {
		    NetworkAccess[] nets = 
			netdc.a_finder().retrieve_all();
		    for (int i=0; i<nets.length; i++) {
			networks.addElement(new CacheNetworkAccess(nets[i]));
		    }
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

	JLabel netLabel = new JLabel("NETWORKS  ");
	JLabel staLabel = new JLabel("STATIONS   ");
	JLabel siLabel = new JLabel("SITES   ");
	JLabel chLabel = new JLabel("CHANNELS");
	netLabel.setToolTipText(lnettip);
	staLabel.setToolTipText(lstatip);
	siLabel.setToolTipText(lsittip);
	chLabel.setToolTipText(lchatip);
	
	add(netLabel, gbc);
	gbc.gridx++;
	add(staLabel, gbc);
	gbc.gridx++;
	add(siLabel, gbc);
	gbc.gridx++;
	add(chLabel, gbc);
	gbc.gridx++;
	
	gbc.gridy++;
	gbc.gridx = 0;
	gbc.weighty = 1.0;
	ListCellRenderer renderer = new NameListCellRenderer(true);

	netList = new JList(networks);
	netList.setCellRenderer(renderer);
	netList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	netList.addListSelectionListener(new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
		    if(e.getValueIsAdjusting()){
			return;
		    }
		    NetworkAccess[] nets = getSelectedNetworks();
		    stations.clear();
		    for (int i=0; i<nets.length; i++) {
			Station[] newStations = nets[i].retrieve_stations();
			for (int j=0; j<newStations.length; j++) {
			    stations.addElement(newStations[j]);
			}
		    } // end of for ((int i=0; i<nets.length; i++)
		}
	    }
					 );

	JScrollPane scroller = new JScrollPane(netList);
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
 
	bandList = new JList(sites);
	bandList.setCellRenderer(renderer);
	bandList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	scroller = new JScrollPane(bandList);
	add(scroller, gbc);
	gbc.gridx++;
	
	channelList = new JList(channels);
	channelList.setCellRenderer(renderer);
	channelList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
	return (NetworkAccess[])netList.getSelectedValues();
    }      

    public Station[]  getSelectedStations(){
        return castStationArray(stationList.getSelectedValues());
    }

    public Site[]  getSelectedSites(){
        return castSiteArray(siteList.getSelectedValues());
    }

    public Channel[]  getSelectedChannels(){
	Channel[] inChannels = getChannels();
	LinkedList outChannels = new LinkedList();
	Object[] selectedChannelCodes = channelList.getSelectedValues();
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
	
        return (Channel[])outChannels.toArray(new Channel[0]);
    }

   /*================Class Variables===============*/

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
 
    private int orientationConfig;
    public static final int THREE_COMPONENT = 0;
    public static final int VERTICAL_ONLY = 1;
    public static final int HORIZONTAL_ONLY = 2;
    public static final int ALL_COMPONENTS = 3;

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
    public static final HashMap bandCodeNames;

    static {
	bandCodeNames = new HashMap();
	bandCodeNames.put(EXTREMELY_SHORT_PERIOD, "EXTREMELY_SHORT_PERIOD");
	bandCodeNames.put(SHORT_PERIOD , "SHORT_PERIOD");
	bandCodeNames.put(HIGH_BROAD_BAND , "HIGH_BROAD_BAND");
	bandCodeNames.put(BROAD_BAND , "BROAD_BAND");
	bandCodeNames.put(MID_PERIOD , "MID_PERIOD");
	bandCodeNames.put(LONG_PERIOD , "LONG_PERIOD");
	bandCodeNames.put(VERY_LONG_PERIOD , "VERY_LONG_PERIOD");
	bandCodeNames.put(ULTRA_LONG_PERIOD , "ULTRA_LONG_PERIOD");
	bandCodeNames.put(EXTREMELY_LONG_PERIOD , "EXTREMELY_LONG_PERIOD");
	bandCodeNames.put(ADMINISTRATIVE , "ADMINISTRATIVE");
	bandCodeNames.put(WEATHER_ENVIRONMENTAL , "WEATHER_ENVIRONMENTAL");
	bandCodeNames.put(EXPERIMENTAL , "EXPERIMENTAL");
    }

    private static final String[] defaultSelectable = { BROAD_BAND, LONG_PERIOD };
    private static final String[] defaultAutoSelect = { LONG_PERIOD };


    protected JList netList;
    protected JList stationList;
    protected JList siteList;
    protected JList bandList;
    protected JList channelList;

    protected DefaultListModel networks = new DefaultListModel();
    protected DefaultListModel stations = new DefaultListModel();
    protected DefaultListModel sites = new DefaultListModel();
    protected DefaultListModel channels = new DefaultListModel();
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

} // ChannelGUI



