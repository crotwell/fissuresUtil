
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
 * ChannelChooserGUI.java
 * 
 * Description: This class creates a list of networks and their respective stations and channels. A non-null NetworkDC reference must be supplied in the constructor, then use the get methods to obtain the necessary information that the user clicked on with the mouse. It takes care of action listeners and single click mouse button.
Use as the follwoing:
 * ChannelChooserGUI channelChooser = new ChannelChooserGUI(netDC);

 *
 * @author <a href="mailto:georginamc@prodigy.net">Georgina Coleman</a>
 * @version
 * Date: 12/04/2001
 */


public class ChannelChooserGUI extends JPanel{


    public ChannelChooserGUI(NetworkDC netdcgiven){
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

	netlist = new JList(networks);
	netlist.setCellRenderer(renderer);
	netlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	netlist.addListSelectionListener(new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
		    if(e.getValueIsAdjusting()){
			return;
		    }
		    NetworkAccess net = getSelectedNetwork();
		    Station[] newStations = net.retrieve_stations();
		    stations.clear();
		    for (int i=0; i<newStations.length; i++) {
			stations.addElement(newStations[i]);
		    }
		}
	    }
					 );

	JScrollPane scroller = new JScrollPane(netlist);
	add(scroller, gbc);
	gbc.gridx++;

	stalist = new JList(stations);
	stalist.setCellRenderer(renderer);
	stalist.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	stalist.addListSelectionListener(new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
		    if(e.getValueIsAdjusting()){
			return;
		    }
		    ListSelectionModel selModel = stalist.getSelectionModel();
		    for (int i=e.getFirstIndex(); i<=e.getLastIndex(); i++) {
			if (stalist.isSelectedIndex(i)) {
			    NetworkAccess net = getSelectedNetwork();
			    Station selectedStation = 
				(Station)stations.getElementAt(i);
			    Channel[] chans =
				net.retrieve_for_station(selectedStation.get_id());
			    for (int j=0; j<chans.length; j++) {
				String chanKey = ChannelIdUtil.toString(chans[j].get_id());
				System.out.println(chanKey+" is selected");
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
			    NetworkAccess net = getSelectedNetwork();
			    Station selectedStation = 
				(Station)stations.getElementAt(i);
			    Channel[] chans =
				net.retrieve_for_station(selectedStation.get_id());
			    for (int j=0; j<chans.length; j++) {
				String chanKey = ChannelIdUtil.toString(chans[j].get_id());
				System.out.println(chanKey+" is not selected");
				if ( channelMap.containsKey(chanKey)) {
				    channelMap.remove(chanKey);
				}
			    }
			}
			
		    } // end of for (int i=e.getFirstIndex(); i<e.getLastIndex(); i++)
		}
	    }
					 );
	scroller = new JScrollPane(stalist);
	add(scroller, gbc);
	gbc.gridx++;
 
	sitlist = new JList(sites);
	sitlist.setCellRenderer(renderer);
	sitlist.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	scroller = new JScrollPane(sitlist);
	add(scroller, gbc);
	gbc.gridx++;
	
	chalist = new JList(channels);
	chalist.setCellRenderer(renderer);
	chalist.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	scroller = new JScrollPane(chalist);
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
	Object[] objArray = channels.toArray();
	return castChannelArray(objArray);
    }

    protected Channel[] castChannelArray(Object[] objArray){
	Channel[] chan 
	    = new Channel[objArray.length];
	for (int i=0; i<chan.length; i++) {
	    chan[i] = (Channel)objArray[i];
	} 
	return chan;
    }

    public NetworkAccess getSelectedNetwork(){
	return (NetworkAccess)netlist.getSelectedValue();
    }      

    public Station[]  getSelectedStations(){
        return castStationArray(stalist.getSelectedValues());
    }

    public Site[]  getSelectedSites(){
        return castSiteArray(sitlist.getSelectedValues());
    }

    public Channel[]  getSelectedChannels(){
        return castChannelArray(chalist.getSelectedValues());
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
 

    protected JList netlist;
    protected JList stalist;
    protected JList sitlist;
    protected JList chalist;

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



