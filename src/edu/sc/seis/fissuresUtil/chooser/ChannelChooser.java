
package edu.sc.seis.fissuresUtil.chooser;

import edu.sc.seis.fissuresUtil.cache.*;
import java.awt.*;
import java.awt.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
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
 * @version $Id: ChannelChooser.java 3247 2003-02-06 03:19:29Z crotwell $
 *
 */


public class ChannelChooser extends JPanel{

    public ChannelChooser(NetworkDCOperations[] netdcgiven) {
        this(netdcgiven,
             false);
    }
  
    public ChannelChooser(NetworkDCOperations[] netdcgiven,
			  boolean showSites) {
        this(netdcgiven,
             showSites,
	     new String[0]);
    }
   
    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
			  String[] configuredNetworks) {
        this(netdcgiven,
             false,
	     configuredNetworks);
    }
    
    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
                          boolean showSites,
			  String[] configuredNetworks) {
	this(netdcgiven, 
	     showSites, 
	     true,
	     configuredNetworks);
    }

    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
                          boolean showSites,
			  boolean showNetworks,
			  String[] configuredNetworks) {
        this(netdcgiven,
	     showSites,
	     showNetworks,
	     configuredNetworks,
             defaultSelectableBand,
             defaultAutoSelectBand);
    }

    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
                          boolean showSites,
			  String[] configuredNetworks,
                          String[] selectableBand,
                          String[] autoSelectBand) {
        this(netdcgiven,
	     showSites,
	     true,
	     configuredNetworks,
             selectableBand,
             autoSelectBand);
    }

    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
                          boolean showSites,
			  boolean showNetworks,
			  String[] configuredNetworks,
                          String[] selectableBand,
                          String[] autoSelectBand){
        this(netdcgiven,
	     showSites,
	     showNetworks,
	     configuredNetworks,
             selectableBand,
             autoSelectBand, 
	     defaultSelectableOrientations,
             defaultAutoSelectedOrientation);
    }

    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
                          boolean showSites,
			  String[] configuredNetworks,
                          String[] selectableBand,
                          String[] autoSelectBand,
                          int[] selectableOrientations,
                          int autoSelectedOrientation) {
        this(netdcgiven,
	     showSites,
	     true,
	     configuredNetworks,
             selectableBand,
             autoSelectBand,
             selectableOrientations,
             autoSelectedOrientation);
    }

    public ChannelChooser(NetworkDCOperations[] netdcgiven, 
                          boolean showSites,
			  boolean showNetworks,
			  String[] configuredNetworks,
                          String[] selectableBand,
                          String[] autoSelectBand,
                          int[] selectableOrientations,
                          int autoSelectedOrientation) {
        this.showSites = showSites;
        this.showNetworks = showNetworks;
        this.selectableOrientations = selectableOrientations;
        this.autoSelectedOrientation = autoSelectedOrientation;
        this.selectableBand = selectableBand;
        this.autoSelectBand = autoSelectBand;
	progressBar.setValue(0);
	progressBar.setStringPainted(true);;
        bundle = ResourceBundle.getBundle(ChannelChooser.class.getName());
        initFrame();
	setConfiguredNetworks(configuredNetworks);
        setNetworkDCs(netdcgiven);
    }

    public Map getNetDCToNetMap() {
	return netDCToNetMap;
    }

    public void setConfiguredNetworks(String[] configuredNetworks) {
	this.configuredNetworks = new String[configuredNetworks.length];
	System.arraycopy(configuredNetworks, 0, 
			 this.configuredNetworks, 0,
			 configuredNetworks.length);
			 
    }

    public NetworkDCOperations[] getNetworkDCs() {
	return netdc;
    }

    public void setNetworkDCs(NetworkDCOperations[] netdcgiven) {
        netdc = netdcgiven;
        channels.clear();
        sites.clear();
        clearStations();
        networks.clear();
	for (int i=0; i<netdcgiven.length; i++) {
	    NetworkLoader networkLoader = new NetworkLoader(netdcgiven[i]);
	    if (netdcgiven.length == 1 || ! showNetworks) {
		networkLoader.setDoSelect(true);
	    } else {
		networkLoader.setDoSelect(false);
	    } // end of else
	    networkLoader.start();
	} // end of for (int i=0; i<netdcgiven.length; i++)
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

	if ( showNetworks ) {
	    add(netLabel, gbc);
	    gbc.gridx++;
	} // end of if ()
	
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

        //networkList = new JList(networks);
        networkList = new DNDJList(networks);
        networkList.setCellRenderer(renderer);
        networkList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        networkList.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if(e.getValueIsAdjusting()){
                        return;
                    }
		    NetworkAccess[] selected = getSelectedNetworks();
                    StationLoader t = new StationLoader(selected);
		    setStationLoader(t);
		    t.start();
                }
            } );

	JScrollPane scroller;
	if ( showNetworks) {
	    scroller = new JScrollPane(networkList);
	    add(scroller, gbc);
	    gbc.gridx++;
	} // end of if ()

        stationList = new DNDJList(stationNames);
        stationList.setCellRenderer(renderer);
        stationList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        stationList.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if(e.getValueIsAdjusting()){
                        return;
                    }
		    ChannelLoader t = new ChannelLoader(e);
		    setChannelLoader(t);
		    t.start();
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

	if ( selectableBand != null) {
	    for ( int i=0; i<selectableBand.length; i++) {
		bandListModel.addElement(selectableBand[i]);		
	    } // end of for ()
	} else {
	    bandListModel.addElement("L");
	    bandListModel.addElement("B");
	    bandListModel.addElement("S");
	    bandListModel.addElement("V");
	    bandListModel.addElement("U");
	    bandListModel.addElement("R");
	    bandListModel.addElement("M");
	    bandListModel.addElement("E");
	    bandListModel.addElement("H");
	    bandListModel.addElement("A");
	    bandListModel.addElement("W");
	    bandListModel.addElement("X");
	} // end of else

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
        for (int j=0; j<autoSelectBand.length; j++) {
            for (int i=0; i<chanListModel.getSize(); i++) {
                String listElement = (String)chanListModel.getElementAt(i);
                if (listElement.equals(autoSelectBand[j])) {
                    channelSelctionModel.addSelectionInterval(i,i);
                    break;
                }
            } // end of for (int i=0; i<chanListModel; i++)
        } // end of for (int j=0; j<autoSelectBand.length; j++)
	
        scroller = new JScrollPane(channelList);
        add(scroller, gbc);
        gbc.gridx++;

	gbc.gridy++;
	gbc.gridx = 0;
	gbc.weighty = 0.1;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	add(progressBar, gbc);
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

    protected void addStation(Station sta) {
	if ( ! stationNames.contains(sta.name)) {
	    stationNames.addElement(sta.name);
	} // end of if ()
	LinkedList staList = (LinkedList)stationMap.get(sta.name);
	if ( staList == null) {
	    staList = new LinkedList();
	    stationMap.put(sta.name, staList);
	} // end of if ()
	staList.add(sta);
    }

    public Station[] getStations(){
	LinkedList out = new LinkedList();
        Object[] objArray = stationNames.toArray();
	for ( int i=0; i<objArray.length; i++) {
	    LinkedList staList = (LinkedList)stationMap.get(objArray[i]);
	    out.addAll(staList);
	} // end of for ()
	
        return (Station[])out.toArray(new Station[0]);
    }

    protected void clearStations() {
	stationNames.clear();
	stationMap.clear();
    }

    protected void clearStationsFromThread() {
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			clearStations();
		    }
		});
	     
	} catch (InterruptedException e) {
	    logger.warn("Caught exception while clearing stations, will continue. Hope all is well...", e);
	    // oh well...	    
	} catch (java.lang.reflect.InvocationTargetException e) {
	    logger.warn("Caught exception while clearing stations, will continue. Hope all is well...", e);
	    // oh well...	    
	} // end of try-catch
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
	if ( ! showNetworks) {
	    return getNetworks();
	} // end of if ()

        return castNetworkArray(networkList.getSelectedValues());
    }      

    public Station[]  getSelectedStations(){
	LinkedList out = new LinkedList();
        Object[] selected = stationList.getSelectedValues();
	for ( int i=0; i<selected.length; i++) {
	    LinkedList staList = (LinkedList)stationMap.get(selected[i]);
	    out.addAll(staList);
	} // end of for ()
	return (Station[])out.toArray(new Station[0]);
    }

    public Site[]  getSelectedSites(){
        return castSiteArray(siteList.getSelectedValues());
    }

    /** Gets the selected channels, but only if they overlap the given time.
     */
    public Channel[]  getSelectedChannels(MicroSecondDate when){
        Channel[] inChannels = getChannels();
	logger.debug("before prune channels, length="+inChannels.length);
	inChannels = BestChannelUtil.pruneChannels(inChannels, when);
	logger.debug("after prune channels, length="+inChannels.length);
	return getSelectedChannels(inChannels, when);
    }

    public Channel[]  getSelectedChannels(){
        Channel[] inChannels = getChannels();
	return getSelectedChannels(inChannels, new MicroSecondDate());
    }

    /** Gets the best selected channels from the given list
     */
    protected  Channel[]  getSelectedChannels(Channel[] inChannels,
					      MicroSecondDate when) {
        LinkedList outChannels = new LinkedList();
        Station[] selectedStations = getSelectedStations();
        Object[] selectedChannelCodes = channelList.getSelectedValues();

        // assume only one selected network
        NetworkAccess[] nets = getSelectedNetworks();
        NetworkAccess net;
        String[] siteCodeHeuristic = BestChannelUtil.getSiteCodeHeuristic();
        for (int staNum=0; staNum<selectedStations.length; staNum++) {
	    net = (NetworkAccess)netIdToNetMap.get(NetworkIdUtil.toString(selectedStations[staNum].get_id().network_id));

	    MicroSecondDate stationStart = new MicroSecondDate(selectedStations[staNum].effective_time.start_time);
	    MicroSecondDate stationEnd = new MicroSecondDate(selectedStations[staNum].effective_time.end_time);
	    if (when.before(stationStart) || when.after(stationEnd) ) {
		logger.debug("station "+StationIdUtil.toString(selectedStations[staNum].get_id())+" did not exist at "+when+" "+selectedStations[staNum].effective_time.end_time.date_time+"="+stationEnd);
		continue;
	    } // end of if ()
	    
	    logger.debug("Checking station "+StationIdUtil.toString(selectedStations[staNum].get_id()));
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
                        // selected channel codes in this case are really "band code names"
                        String bandName = (String)selectedChannelCodes[bandNum];
                        String bc = bundle.getString("CODE_"+bandName);
                        Channel[] tmpH = 
                            BestChannelUtil.getHorizontalChannels(staChans,
                                                                  bc);
                        Channel tmpV = null;
                        if (tmpH != null && tmpH.length != 0) {
                            // look for channel with same band, site and gain, 
                            // but with orientation code Z
                            tmpV = BestChannelUtil.getChannel(staChans, 
                                                              bc,
                                                              "Z",
                                                              tmpH[0].my_site.get_code(),
                                                              tmpH[0].get_code().substring(1,2));
			     
                        } // end of if (tmpH != null && tmpH.length != 0)
			
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
                        String bandName = (String)selectedChannelCodes[bandNum];
                        String bc = bundle.getString("CODE_"+bandName);
                        Channel tmp = BestChannelUtil.getVerticalChannel(staChans, 
                                                                         bc);
                        if (tmp != null) {
                            outChannels.add(tmp);
                        } // end of if (tmp != null)
                    }
                } else if (orientationList.getSelectedValue().equals("HORIZONTAL_ONLY")) {
                    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
                        String bandName = (String)selectedChannelCodes[bandNum];
                        String bc = bundle.getString("CODE_"+bandName);
                        Channel[] tmp = 
                            BestChannelUtil.getHorizontalChannels(staChans, 
                                                                  bc);
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
	
        logger.debug("Found "+outChannels.size()+" chanels");
        return (Channel[])outChannels.toArray(new Channel[0]);
    }

    /**
     * Get the value of stationLoader.
     * @return value of stationLoader.
     */
    protected synchronized StationLoader getStationLoader() {
	return stationLoader;
    }
    
    /**
     * Set the value of stationLoader.
     * @param v  Value to assign to stationLoader.
     */
    protected synchronized void setStationLoader(StationLoader  v) {
	this.stationLoader = v;
    }
    
    /**
     * Get the value of channelLoader.
     * @return value of channelLoader.
     */
    protected  synchronized ChannelLoader getChannelLoader() {
	return channelLoader;
    }
    
    /**
     * Set the value of channelLoader.
     * @param v  Value to assign to channelLoader.
     */
    protected  synchronized void setChannelLoader(ChannelLoader  v) {
	this.channelLoader = v;
    }

    /** sets this thread as the owner of the progress bar. It is the only
	thread that can update the progress bar. Also resets the value to 0;
    */
    protected synchronized void setProgressOwner(Thread t) {
	progressBar.setValue(0);
	progressOwner = t;
    }
    
    protected synchronized void setProgressValue(Thread t, int value) {
	if (t.equals(progressOwner)) {
	    progressBar.setValue(value);
	}
    }

    protected synchronized void setProgressMax(Thread t, int max) {
	if (t.equals(progressOwner)) {
	    progressBar.setMaximum(max);
	}
    }

    /*================Class Variables===============*/

    protected boolean showSites;
    protected boolean showNetworks;
    protected String[] selectableBand;
    protected String[] autoSelectBand;
    private String[] configuredNetworks;
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
    private static final String[] defaultAutoSelectBand = { BROAD_BAND };
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
    protected JProgressBar progressBar = new JProgressBar(0, 100);

    protected DefaultListModel networks = new DefaultListModel();
    protected DefaultListModel stationNames = new DefaultListModel();
    protected HashMap stationMap = new HashMap();
    protected DefaultListModel sites = new DefaultListModel();
    protected DefaultListModel channels = new DefaultListModel();
    protected DefaultListModel bandListModel = new DefaultListModel();
    protected HashMap channelMap = new HashMap();

    private NetworkDCOperations[] netdc;
    protected HashMap netDCToNetMap = new HashMap();
    protected HashMap netIdToNetMap = new HashMap();

    private StationLoader stationLoader = null;
    private ChannelLoader channelLoader = null;
    private Thread progressOwner = null;

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
                    // assume name of length 1 isn't a name
                    if (name == null || name.length() <= 1) {
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
                    name = bundle.getString("BANDNAME_"+(String)value);
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

    class NetworkLoader extends Thread {
	NetworkDCOperations netdc;
	boolean doSelect = true;
	public NetworkLoader(NetworkDCOperations netdc) {
	    this.netdc = netdc;
	}
	public void setDoSelect(boolean b) {
	    doSelect = b;
	}
		
	public void run() {
	    setProgressOwner(this);
	    CacheNetworkAccess cache;
	    logger.debug("Before networks");
	    if(configuredNetworks == null || configuredNetworks.length == 0) {
		NetworkAccess[] nets = 
		    netdc.a_finder().retrieve_all();
		netDCToNetMap.put(netdc, nets);

		setProgressMax(this, nets.length+1);
		int progressVal = 1;
		setProgressValue(this, progressVal);
		progressVal++;
		logger.debug("Got all networks, num="+nets.length);
		for (int i=0; i<nets.length; i++) {
		    // skip null networks...probably a bug on the server
		    if (nets[i] != null) {
			//  cache = new CacheNetworkAccess(nets[i]);
			cache = new DNDNetworkAccess(nets[i]);
			NetworkAttr attr = cache.get_attributes();
			netIdToNetMap.put(NetworkIdUtil.toString(cache.get_attributes().get_id()),
					  cache);
			logger.debug("Got attributes "+attr.get_code());
			// preload attributes
			networkAdd(cache);
		    } else {
			logger.warn("a networkaccess returned from NetworkFinder.retrieve_all() is null, skipping.");
		    } // end of else
		    setProgressValue(this, progressVal);
                    progressVal++;    
		}
	    } else {
		//when the channelChooser is configured with networkCodes....
		int totalNetworks = 0;
		setProgressMax(this, configuredNetworks.length);
		for(int counter = 0; counter < configuredNetworks.length; counter++) {
		    try {
                        logger.debug("Getting network for "+configuredNetworks[counter]);
			NetworkAccess[] nets = 
			    netdc.a_finder().retrieve_by_code(configuredNetworks[counter]);
                        logger.debug("Got "+nets.length+" networks for "+configuredNetworks[counter]);
			for(int subCounter = 0; subCounter < nets.length; subCounter++) {
			    if (nets[subCounter] != null) {
				//  cache = new CacheNetworkAccess(nets[subCounter]);
				// preload attributes
				cache = new DNDNetworkAccess(nets[subCounter]);
				NetworkAttr attr = cache.get_attributes();
				NetworkAccess[] storedNets = 
				    (NetworkAccess[])netDCToNetMap.get(netdc);
				if ( storedNets == null) {
				    storedNets = new NetworkAccess[1];
				    storedNets[0] = cache;
				    netDCToNetMap.put(netdc, storedNets);
				} else {
				    NetworkAccess[] tmp = 
					new NetworkAccess[storedNets.length+1];
				    System.arraycopy(storedNets, 0, tmp, 0, storedNets.length);
				    tmp[storedNets.length] = cache;
				    netDCToNetMap.put(netdc, tmp);
				} // end of else

				netIdToNetMap.put(NetworkIdUtil.toString(cache.get_attributes().get_id()),
						  cache);
				logger.debug("Got attributes "+attr.get_code());
				networkAdd(cache);
				totalNetworks++;
			    } else {
				logger.warn("a networkaccess returned from NetworkFinder.retrieve_by_code is null, skipping.");
			    } // end of else
			}//end of inner for subCounter = 0;
		    }catch(NetworkNotFound nnfe) {
			logger.warn("Network "+configuredNetworks[counter]+" not found while getting network access uding NetworkFinder.retrieve_by_code");
		    }
		    setProgressValue(this, counter+1);
		}//end of outer for counter = 0;
	    }//end of if else checking for configuredNetworks == null 
	    if (doSelect) {
		// need to do this later to give java Event thread time to set
		// up network list before setting selection
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    networkList.getSelectionModel().setSelectionInterval(0,
										 networkList.getModel().getSize()-1);
			}
		    });
	    } // end of if ()
	    setProgressValue(this, progressBar.getMaximum());
	}

	void networkAdd(final NetworkAccess n) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			networks.addElement(n);
		    }
		});
	}
    }

    class StationLoader extends Thread {
	public StationLoader(NetworkAccess[] n) {
	    this.nets = n;
	}

	NetworkAccess[] nets;

	public void run() {
	    setProgressOwner(this);
	    setProgressMax(this, 100);
	    logger.debug("There are "+nets.length+" selected networks.");
	    try {
		synchronized (ChannelChooser.this) {
		    if (this.equals(getStationLoader())) {
			clearStationsFromThread();
		    }
		}
		int netProgressInc = 50 / nets.length;
		int progressValue = 10;
		setProgressValue(this, progressValue);
		for (int i=0; i<nets.length; i++) {
		    logger.debug("Before get stations");
		    Station[] newStations = nets[i].retrieve_stations();
		    logger.debug("got "+newStations.length+" stations");
		    setProgressValue(this, progressValue+netProgressInc/2);

		    for (int j=0; j<newStations.length; j++) {
			synchronized (ChannelChooser.this) {
			    if (this.equals(getStationLoader())) {
				stationAdd(newStations[j]);
				try {
				    sleep((int)(.01*1000)); 
				} catch (InterruptedException e) {
				    
				} // end of try-catch
			    } else {
				// no longer the active station loader
				return;
			    } // end of else
			    
			}
			setProgressValue(this, progressValue+netProgressInc/2-
					 (newStations.length-j)/newStations.length);
		    }
		    setProgressValue(this, 100);
		    logger.debug("finished adding stations");
		    try {
			sleep((int)(.01*1000)); 
		    } catch (InterruptedException e) {
						
		    } // end of try-catch
					    
		} // end of for ((int i=0; i<nets.length; i++)
		logger.debug("There are "+stationNames.getSize()+" items in the station list model");
		// stationList.validate();
		//stationList.repaint();
	    } catch (Exception e) {
		edu.sc.seis.fissuresUtil.exceptionHandlerGUI.ExceptionHandlerGUI.handleException(e);
	    } // end of try-catch	}
	}

	void stationAdd(final Station s) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			addStation(s);
		    }
		});
	}
    }


    class ChannelLoader extends Thread {
	public ChannelLoader(ListSelectionEvent e) {
	    this.e = e;
	}

	ListSelectionEvent e;

	public void run() {
	    setProgressOwner(this);
	    ListSelectionModel selModel = stationList.getSelectionModel();
	    // assume only one selected network at at time...
	    NetworkAccess[] nets = getSelectedNetworks();
	    setProgressMax(this, e.getLastIndex()-e.getFirstIndex()+1);

	    for (int i=e.getFirstIndex(); i<=e.getLastIndex(); i++) {
		String staName =
		    (String)stationNames.getElementAt(i);
		LinkedList stations = (LinkedList)stationMap.get(staName);
		Iterator it = stations.iterator();
		while ( it.hasNext()) {
		    Station sta = (Station)it.next();
		    Channel[] chans = null;
		    NetworkAccess net = (NetworkAccess)
			netIdToNetMap.get(NetworkIdUtil.toString(sta.get_id().network_id));
		    if ( net != null) {
			chans =
			    net.retrieve_for_station(sta.get_id());
		    } else {
			logger.warn("Unable to find network server for station "+sta.name+" "+StationIdUtil.toString(sta.get_id()));
			continue;
		    } // end of if ()
		    
		    
		    synchronized (ChannelChooser.this) {
			if (this.equals(getChannelLoader())) {
			    if (stationList.isSelectedIndex(i)) {
				addChannels(chans);
			    } else {
				removeChannels(chans);
			    } // end of else
			} else {
			    // no loner active channel loader
			    return;
			} // end of else
		    } // end of while ()
		}
		setProgressValue(this, i-e.getFirstIndex());
	    } // end of for (int i=e.getFirstIndex(); i<e.getLastIndex(); i++)
	    progressBar.setValue(progressBar.getMaximum());
	}

	void addChannels(Channel[] chans) {
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
	}

	void removeChannels(Channel[] chans) {

	    for (int j=0; j<chans.length; j++) {
		String chanKey = ChannelIdUtil.toString(chans[j].get_id());
		if ( channelMap.containsKey(chanKey)) {
		    channelMap.remove(chanKey);
		}
	    }
	}
    }

    static Category logger = 
        Category.getInstance(ChannelChooser.class.getName());

} // ChannelChooser



