
package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.*;
import java.util.*;
import javax.swing.*;

import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.DataCenterRouter;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Category;


/**
 * ChannelChooser.java
 *
 * Description: This class creates a list of networks and their respective stations and channels. A non-null NetworkDC reference must be supplied in the constructor, then use the get methods to obtain the necessary information that the user clicked on with the mouse. It takes care of action listeners and single click mouse button.
 *
 * @author Philip Crotwell
 * @version $Id: ChannelChooser.java 5969 2003-10-02 01:45:10Z crotwell $
 *
 */


public class ChannelChooser extends JPanel {

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
                          String[] autoSelectBand) {
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

    public void setShowCodes(boolean val) {
        showCodes = val;
        AvailableDataStationRenderer stationRenderer =
            new AvailableDataStationRenderer(showNames,
                                             showCodes,
                                             codeIsFirst);
        stationRenderer.setJList(stationList);
        setStationListCellRenderer(stationRenderer);
        setNetworkListCellRenderer(new NameListCellRenderer(showNames,
                                                            showCodes,
                                                            codeIsFirst));
    }

    public void setShowNames(boolean val) {
        showNames = val;
        AvailableDataStationRenderer stationRenderer =
            new AvailableDataStationRenderer(showNames,
                                             showCodes,
                                             codeIsFirst);
        stationRenderer.setJList(stationList);
        setStationListCellRenderer(stationRenderer);
        setNetworkListCellRenderer(new NameListCellRenderer(showNames,
                                                            showCodes,
                                                            codeIsFirst));
    }

    public void setSeismogramDC(DataCenterRouter dcops) {
        stationRenderer =
            new AvailableDataStationRenderer(showNames,
                                             showCodes,
                                             codeIsFirst,
                                             dcops,
                                             this);
        stationRenderer.setJList(stationList);
        setStationListCellRenderer(stationRenderer);
    }

    public void setAvailbleDataOrigin(Origin origin) {
        if (stationRenderer != null){
            stationRenderer.setOrigin(origin);
        }
    }

    public void addAvailableStationDataListener(AvailableStationDataListener dataListener){
        if (stationRenderer != null){
            stationRenderer.addAvailableStationDataListener(dataListener);
        } else {
            // this should probably never be allowed to happen???
            throw new NullPointerException("StationRenderer is null, so cannot add availableStationDataListener");
        }
    }

    public void recheckNetworkAvailability(){
        if (stationRenderer != null){
            stationRenderer.recheckNetworks();
        } else {
            // this should probably never be allowed to happen???
            throw new NullPointerException("StationRenderer is null, so cannot recheckNetworkAvailability");
        }
    }

    public void setStationListCellRenderer(ListCellRenderer r) {
        stationList.setCellRenderer(r);
    }

    public void setNetworkListCellRenderer(ListCellRenderer r) {
        networkList.setCellRenderer(r);
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

    public boolean isNetworkLoadingFinished() {
        return (networkLoaderList.size() == 0);
    }

    public void setNetworkDCs(NetworkDCOperations[] netdcgiven) {
        netdc = netdcgiven;
        channels.clear();
        sites.clear();
        clearStations();
        networks.clear();
        for (int i=0; i<netdcgiven.length; i++) {
            NetworkLoader networkLoader = new NetworkLoader(netdcgiven[i]);

            if ( ! showNetworks) {
                networkLoader.setDoSelect(true);
            }
            else {
                networkLoader.setDoSelect(false);
            } // end of else
            networkLoader.start();
        } // end of for (int i=0; i<netdcgiven.length; i++)
    }

    public void addStationAcceptor(StationAcceptor sAccept) {
        stationAcceptors.add(sAccept);
    }

    public void initFrame() {
        initWidgets();
        layoutWidgets();
    }

    private void layoutWidgets() {
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JScrollPane scroller;

        // networks
        if ( showNetworks) {
            gbc.gridwidth = 2;
            add(netLabel, gbc);
            gbc.gridy++;

            scroller = new JScrollPane(networkList);
            gbc.weighty = 1.0;
            add(scroller, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.weighty = 0;
        } // end of if ()

        // station
        gbc.gridwidth = 2;
        add(staLabel, gbc);
        gbc.gridy++;
        scroller = new JScrollPane(stationList);
        gbc.weighty = 1.0;
        add(scroller, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weighty = 0;

        // sites (aka loc id)
        if (showSites) {
            add(siLabel, gbc);
            gbc.gridy++;

            scroller = new JScrollPane(siteList);
            gbc.weighty = 1.0;
            add(scroller, gbc);
            gbc.gridx++;
            gbc.gridy--;
            gbc.weighty = 0;
        }

        // orientation
        add(orientationLabel, gbc);
        gbc.gridy++;
        scroller = new JScrollPane(orientationList);
        gbc.weighty = .25;
        add(scroller, gbc);
        gbc.gridx++;
        gbc.gridy--;
        gbc.weighty = 0;

        // channel or band
        add(chLabel, gbc);
        gbc.gridy++;
        scroller = new JScrollPane(channelList);
        gbc.weighty = .25;
        add(scroller, gbc);
        gbc.gridx++;
        gbc.gridy--;
        gbc.weighty = 0;

        // progress bar
        gbc.gridy++;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 0.1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(progressBar, gbc);

    }

    private void initWidgets() {
        //  setSize(new java.awt.Dimension (mywidth, myheight));
        //setPreferredSize(new java.awt.Dimension (mywidth, myheight));

        netLabel = new JLabel(bundle.getString("LABEL_NETWORKS"));
        staLabel = new JLabel(bundle.getString("LABEL_STATIONS"));
        siLabel = new JLabel(bundle.getString("LABEL_SITES"));
        orientationLabel = new JLabel(bundle.getString("LABEL_ORIENTATIONS"));
        chLabel = new JLabel(bundle.getString("LABEL_CHANNELS"));

        netLabel.setToolTipText(lnettip);
        staLabel.setToolTipText(lstatip);
        siLabel.setToolTipText(lsittip);
        chLabel.setToolTipText(lchatip);

        //networkList = new JList(networks);
        networkList = new DNDJList(networks);
        networkList.setCellRenderer(renderer);
        networkList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        networkList.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {
                        if(e.getValueIsAdjusting()) {
                            return;
                        }
                        NetworkAccess[] selected = getSelectedNetworks();
                        clearStations();
                        StationLoader t = new StationLoader(ChannelChooser.this, selected);
                        Iterator it = stationAcceptors.iterator();
                        while (it.hasNext()) {
                            t.addStationAcceptor((StationAcceptor)it.next());
                        }
                        setStationLoader(t);
                        t.start();
                    }
                } );
        stationList = new DNDJList(stationNames);
        // set a default cell renederer, but this can be overridden
        // with the setStationListCellRenderer method
        stationList.setCellRenderer(renderer);
        stationList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        stationList.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {
                        if(e.getValueIsAdjusting()) {
                            return;
                        }
                        ChannelLoader t = new ChannelLoader(e);
                        setChannelLoader(t);
                        t.start();

                        //added to see if selection will work properly in the map
                        fireStationSelectedEvent(e);
                    }
                });

        siteList = new JList(sites);
        siteList.setCellRenderer(renderer);
        siteList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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
                        if(e.getValueIsAdjusting()) {
                            return;
                        }
                        String selected = (String)orientationList.getSelectedValue();
                        if ((selected.equals("THREE_COMPONENT") ||
                                 selected.equals("VERTICAL_ONLY") ||
                                 selected.equals("HORIZONTAL_ONLY"))
                            && channelList.getModel() != bandListModel) {
                            channelList.setModel(bandListModel);
                            channelList.setCellRenderer(bundleRenderer);
                        }
                        else if (selected.equals("INDIVIDUAL_CHANNELS")
                                 && channelList.getModel() != channels) {
                            channelList.setModel(channels);
                            channelList.setCellRenderer(renderer);
                        }
                    }
                });

        if ( selectableBand != null) {
            for ( int i=0; i<selectableBand.length; i++) {
                bandListModel.addElement(selectableBand[i]);
            } // end of for ()
        }
        else {
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
        }
        else {
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

    }

    public NetworkAccess[] getNetworks() {
        Object[] objArray = networks.toArray();
        return castNetworkArray(objArray);
    }

    protected NetworkAccess[] castNetworkArray(Object[] objArray) {
        NetworkAccess[] nets
            = new NetworkAccess[objArray.length];
        for (int i=0; i<nets.length; i++) {
            nets[i] = (NetworkAccess)objArray[i];
        }
        return nets;
    }

    public void addNetworkDataListener(NetworkDataListener s) {
        listenerList.add(NetworkDataListener.class, s);
    }

    protected void fireNetworkDataChangedEvent(NetworkAccess net) {
        NetworkDataEvent networkDataEvent = null;
        //logger.debug("fireStationDataEvent called");
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==NetworkDataListener.class) {
                // Lazily create the event:
                if (networkDataEvent == null)
                    networkDataEvent = new NetworkDataEvent(this, net);
                ((NetworkDataListener)listeners[i+1]).networkDataChanged(networkDataEvent);

                //logger.debug("fireStationDataEvent!");
            }
        }
    }

    private void addStation(Station sta) {
        if ( ! stationMap.containsKey(sta.name)) {
            stationNames.addElement(sta);
        } // end of if ()
        LinkedList staList = (LinkedList)stationMap.get(sta.name);
        if ( staList == null) {
            staList = new LinkedList();
            stationMap.put(sta.name, staList);
        } // end of if ()
        staList.add(sta);
        //logger.debug("size of stationNames: " + stationNames.getSize());
        //logger.debug("progress: " + progressBar.getString());
    }

    protected void addStations(Station[] stations) {
        for (int i = 0; i < stations.length; i++) {
            addStation(stations[i]);
        }
        //logger.debug("addStations(): size of stationNames: " + stationNames.size());
        //logger.debug("addStations(): size of stationMap: " + stationMap.size());
        fireStationDataChangedEvent(stations);
    }

    /** Adds a stations, but using SwingUtilities.invokeLater. This allows
     threads beside the event dispatch thread to interact with the
     swing widgets.
     */
    protected void addStationsFromThread(final Station[] sta) {
        SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        addStations(sta);
                    }
                });
    }

    public void addStationDataListener(StationDataListener s) {
        listenerList.add(StationDataListener.class, s);
        s.stationDataCleared();
        s.stationDataChanged(new StationDataEvent(this, getStations()));
    }

    public void addStationSelectionListener(StationSelectionListener s){
        listenerList.add(StationSelectionListener.class, s);
        s.stationSelectionChanged(new StationSelectionEvent(this, getSelectedStations()));
    }

    protected void fireStationDataChangedEvent(Station[] stations) {
        StationDataEvent stationDataEvent = null;
        //logger.debug("fireStationDataEvent called");
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==StationDataListener.class) {
                // Lazily create the event:
                if (stationDataEvent == null)
                    stationDataEvent = new StationDataEvent(this, stations);
                ((StationDataListener)listeners[i+1]).stationDataChanged(stationDataEvent);

                //logger.debug("fireStationDataEvent!");
            }
        }
    }

    protected void fireStationDataClearedEvent() {
        StationDataEvent stationDataEvent = null;
        //logger.debug("fireStationDataEvent called");
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==StationDataListener.class) {
                // Lazily create the event:
                if (stationDataEvent == null)
                    stationDataEvent = new StationDataEvent(this, null);
                ((StationDataListener)listeners[i+1]).stationDataCleared();

                //logger.debug("fireStationDataEvent!");
            }
        }
    }

    protected void fireStationSelectedEvent(ListSelectionEvent e) {
        StationSelectionEvent stationSelectionEvent = null;
        //logger.debug("fireStationDataEvent called");
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==StationSelectionListener.class) {
                // Lazily create the event:
                if (stationSelectionEvent == null)
                    stationSelectionEvent =
                        new StationSelectionEvent(this, getSelectedStations());
                ((StationSelectionListener)listeners[i+1]).stationSelectionChanged(stationSelectionEvent);

                //logger.debug("fireStationDataEvent!");
            }
        }
    }

    public Station[] getStations() {
        LinkedList out = new LinkedList();
        Object[] objArray = stationNames.toArray();
        //logger.debug("Object array length: " + objArray.length);
        //logger.debug("stationNames size: " + stationNames.getSize());
        for ( int i=0; i<objArray.length; i++) {
            String name = ((Station)objArray[i]).name;
            LinkedList staList = (LinkedList)stationMap.get(name);
            if (staList == null) {
                logger.warn("no stations for name="+name);
            } else {
                out.addAll(staList);
            }
        } // end of for ()

        return (Station[])out.toArray(new Station[0]);
    }

    protected void clearStations() {
        stationNames.clear();
        stationMap.clear();
        fireStationDataClearedEvent();
    }

    protected void clearStationsFromThread() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            clearStations();
                        }
                    });

        }
        catch (InterruptedException e) {
            logger.warn("Caught exception while clearing stations, will continue. Hope all is well...", e);
            // oh well...
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            logger.warn("Caught exception while clearing stations, will continue. Hope all is well...", e);
            // oh well...
        } // end of try-catch
    }

    public Site[]  getSites() {
        Object[] objArray = sites.toArray();
        HashMap outSites = new HashMap();
        for (int i=0; i<objArray.length; i++) {
            for (int j=0; j<1; j++) {

            } // end of for (int j=0; j<1; j++)

        } // end of for (int i=0; i<objArray.length; i++)

        return castSiteArray(objArray);
    }

    protected Site[] castSiteArray(Object[] objArray) {
        Site[] site
            = new Site[objArray.length];
        for (int i=0; i<site.length; i++) {
            site[i] = (Site)objArray[i];
        }
        return site;
    }

    public Channel[]  getChannels() {
        Channel[] outChannels =
            (Channel[])channelMap.values().toArray(new Channel[0]);
        return outChannels;
    }

    public Channel[] getChannels(Station station){
        NetworkAccess net = (NetworkAccess)netIdToNetMap.get(NetworkIdUtil.toString(station.get_id().network_id));
        Channel[] staChans = net.retrieve_for_station(station.get_id());
        return staChans;
    }

    protected Channel[] castChannelArray(Object[] objArray) {
        Channel[] chan
            = new Channel[objArray.length];
        for (int i=0; i<chan.length; i++) {
            chan[i] = (Channel)objArray[i];
        }
        return chan;
    }

    /** returns selected items from channel list. May be full codes like BHZ or
     just band codes like B */
    public String[] getSelectedChanCodes() {
        Object[] vals = channelList.getSelectedValues();
        String[] out = new String[vals.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = (String)vals[i];
        }
        return out;
    }

    public NetworkAccess[] getSelectedNetworks() {
        if ( ! showNetworks) {
            return getNetworks();
        } // end of if ()

        return castNetworkArray(networkList.getSelectedValues());
    }

    public Station[]  getSelectedStations() {
        LinkedList out = new LinkedList();
        Object[] selected = stationList.getSelectedValues();
        for ( int i=0; i<selected.length; i++) {
            LinkedList staList =
                (LinkedList)stationMap.get(((Station)selected[i]).name);
            out.addAll(staList);
        } // end of for ()
        return (Station[])out.toArray(new Station[0]);
    }

    public Station[] getSelectedStations(MicroSecondDate when) {
        Station[] in = getSelectedStations();
        LinkedList out = new LinkedList();
        for ( int i=0; i<in.length; i++) {
            MicroSecondDate b = new MicroSecondDate(in[i].effective_time.start_time);
            MicroSecondDate e = new MicroSecondDate(in[i].effective_time.end_time);
            if (when.after(b) && when.before(e)) {
                out.add(in[i]);
            }
        } // end of for ()
        return (Station[])out.toArray(new Station[0]);
    }

    public void clearStationSelection(){
        stationList.getSelectionModel().clearSelection();
    }

    public void toggleStationSelected(Station stat){
        ListSelectionModel selModel = stationList.getSelectionModel();
        ListModel listModel = stationList.getModel();
        for (int i = 0; i < listModel.getSize(); i++){
            Station cur = (Station)listModel.getElementAt(i);
            if(cur.equals(stat)){
                if(selModel.isSelectedIndex(i)){
                    selModel.removeSelectionInterval(i, i);
                }else{
                    selModel.addSelectionInterval(i, i);
                }
                stationList.ensureIndexIsVisible(i);
                return;
            }
        }
    }
    public Site[]  getSelectedSites() {
        return castSiteArray(siteList.getSelectedValues());
    }

    /** Gets the selected channels, but only if they overlap the given time.
     */
    public Channel[]  getSelectedChannels(MicroSecondDate when) {
        return getSelectedChannels(when, true);
    }

    public Channel[]  getSelectedChannels() {
        return getSelectedChannels(ClockUtil.now(), true);
    }

    public NetworkAccess getNetworkAccess(NetworkId netid) {
        NetworkAccess net = (NetworkAccess)
            netIdToNetMap.get(NetworkIdUtil.toString(netid));
        if (net == null) {
            // may not be loaded yet, try to get from dc?
            NetworkDCOperations[] netdc = getNetworkDCs();
            for (int i = 0; i < netdc.length; i++) {
                try {
                    net = netdc[i].a_finder().retrieve_by_id(netid);
                    if (net != null) {
                        return new CacheNetworkAccess(net);
                    }
                } catch (NetworkNotFound e) {
                    // oh well, try next
                }
            }
        }
        return net;
    }

    /** Gets the best selected channels from the given list
     */
    protected  Channel[]  getSelectedChannels(MicroSecondDate when,
                                             boolean pruneStations) {
        LinkedList outChannels = new LinkedList();
        Station[] selectedStations = getSelectedStations(when);
        if (pruneStations) {
            LinkedList outStations = new LinkedList();
            for (int i = 0; i < selectedStations.length-1; i++) {
                boolean foundDup = false;
                for (int j = i+1; j < selectedStations.length; j++) {
                    if ( selectedStations[i].name.equals(selectedStations[j].name) &&
                        selectedStations[i].my_location.latitude == selectedStations[j].my_location.latitude &&
                        selectedStations[i].my_location.longitude == selectedStations[j].my_location.longitude) {
                        foundDup=true;
                        break;
                    }
                }
                if (! foundDup) {
                    outStations.add(selectedStations[i]);
                }
            }
            if (selectedStations.length > 0) {
                outStations.add(selectedStations[selectedStations.length-1]);
            }
            selectedStations = (Station[])outStations.toArray(new Station[outStations.size()]);
        }

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
                }
                else if (orientationList.getSelectedValue().equals("THREE_COMPONENT")) {
                    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
                        // selected channel codes in this case are really "band code names"
                        String bc = (String)selectedChannelCodes[bandNum];
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
                }
                else if (orientationList.getSelectedValue().equals("VERTICAL_ONLY")) {
                    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
                        String bc = (String)selectedChannelCodes[bandNum];
                        Channel tmp = BestChannelUtil.getVerticalChannel(staChans,
                                                                         bc);
                        if (tmp != null) {
                            outChannels.add(tmp);
                        } // end of if (tmp != null)
                    }
                }
                else if (orientationList.getSelectedValue().equals("HORIZONTAL_ONLY")) {
                    for (int bandNum=0; bandNum<selectedChannelCodes.length; bandNum++) {
                        String bc = (String)selectedChannelCodes[bandNum];
                        Channel[] tmp =
                            BestChannelUtil.getHorizontalChannels(staChans,
                                                                  bc);
                        if (tmp != null) {
                            outChannels.add(tmp[0]);
                            outChannels.add(tmp[1]);
                        } // end of if (tmp != null)
                    } // end of else
                }
            }
            else {
                // pay attention to selected Sites
                Channel[] inChannels = getChannels();
                inChannels = BestChannelUtil.pruneChannels(inChannels, when);
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

    protected boolean showCodes = false;
    protected boolean showNames = true;
    protected boolean codeIsFirst = true;

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

    private JLabel netLabel;
    private JLabel staLabel;
    private JLabel siLabel;
    private JLabel orientationLabel;
    private JLabel chLabel;

    protected JList networkList;
    protected JList stationList;
    protected JList siteList;
    protected JList orientationList;
    protected JList channelList;
    protected JProgressBar progressBar = new JProgressBar(0, 100);

    protected LinkedList stationAcceptors = new LinkedList();

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

    final ListCellRenderer renderer = new NameListCellRenderer(true,
                                                               false,
                                                               true);



    class BundleListCellRenderer extends DefaultListCellRenderer {
        BundleListCellRenderer() {

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
            }
            catch (java.util.MissingResourceException e) {
                try {
                    // try NAME_value
                    name = bundle.getString("BANDNAME_"+(String)value);
                    useValue = name;
                }
                catch (java.util.MissingResourceException ee) {
                    // use default value???
                    useValue = value;
                } // end of try-catch
            } // end of try-catch

            if (useValue.equals(name) && (name == null || name.length() == 0)) {
                useValue = value;
            } // end of if (name == null || name.length == 0)

            return super.getListCellRendererComponent(list,
                                                      useValue,
                                                      index,
                                                      isSelected,
                                                      cellHasFocus);
        }


    }

    private List networkLoaderList = Collections.synchronizedList(new LinkedList());

    class NetworkLoader extends Thread {
        NetworkDCOperations netdc;
        boolean doSelect = true;
        public NetworkLoader(NetworkDCOperations netdc) {
            this.netdc = netdc;
            networkLoaderList.add(this);
        }
        public void setDoSelect(boolean b) {
            doSelect = b;
        }

        public void run() {
            setProgressOwner(this);
            CacheNetworkAccess cache;
            if(configuredNetworks == null || configuredNetworks.length == 0) {
                NetworkAccess[] nets =
                    netdc.a_finder().retrieve_all();

                // I don't think this should ever happen, but...
                if (nets == null) { nets = new NetworkAccess[0]; }

                netDCToNetMap.put(netdc, nets);

                setProgressMax(this, nets.length+1);
                int progressVal = 1;
                setProgressValue(this, progressVal);
                progressVal++;
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
                    }
                    else {
                        logger.warn("a networkaccess returned from NetworkFinder.retrieve_all() is null, skipping.");
                    } // end of else
                    setProgressValue(this, progressVal);
                    progressVal++;
                }
            }
            else {
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
                                // this code is here because DNDNetworkAccess
                                // was failing to initialize under java web start
                                // on some windows machines. Drag and Drop was disabled

                                // preload attributes
                                cache = new CacheNetworkAccess(nets[subCounter]);
                                //cache = new DNDNetworkAccess(nets[subCounter]);
                                NetworkAttr attr = cache.get_attributes();

                                // this is BAD CODE, but prevents the scepp
                                // network, SP, from being loaded from the DMC
                                if (attr.get_code().equals("SP") &&
                                    attr.get_id().begin_time.date_time.equals("20010911010000.0000GMT")) {
                                    // come from dmc so skip
                                    continue;
                                }

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
                                networkAdd(cache);
                                totalNetworks++;
                            }
                            else {
                                logger.warn("a networkaccess returned from NetworkFinder.retrieve_by_code is null, skipping.");
                            } // end of else
                        }//end of inner for subCounter = 0;
                    }
                    catch(NetworkNotFound nnfe) {
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
            networkLoaderList.remove(this);
        }

        void networkAdd(final NetworkAccess n) {
            SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            networks.addElement(n);
                            fireNetworkDataChangedEvent(n);
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
                    ((Station)stationNames.getElementAt(i)).name;
                LinkedList stations = (LinkedList)stationMap.get(staName);
                Iterator it = stations.iterator();
                while ( it.hasNext()) {
                    Station sta = (Station)it.next();

                    // unselected station, remove all channels from this station
                    synchronized (ChannelChooser.this) {
                        if (this.equals(getChannelLoader())) {
                            if ( ! stationList.isSelectedIndex(i)) {
                                removeChannels(sta);
                                continue;
                            } // end of else
                        }
                        else {
                            // no loner active channel loader
                            return;
                        } // end of else
                    }

                    Channel[] chans = null;
                    NetworkAccess net = (NetworkAccess)
                        netIdToNetMap.get(NetworkIdUtil.toString(sta.get_id().network_id));
                    if ( net != null) {
                        chans =
                            net.retrieve_for_station(sta.get_id());
                    }
                    else {
                        logger.warn("Unable to find network server for station "+sta.name+" "+StationIdUtil.toString(sta.get_id()));
                        continue;
                    } // end of if ()


                    synchronized (ChannelChooser.this) {
                        if (this.equals(getChannelLoader())) {
                            if (stationList.isSelectedIndex(i)) {
                                addChannels(chans);
                            } // end of else
                        }
                        else {
                            // no loner active channel loader
                            return;
                        } // end of else
                    }
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
            fireChannelSelectionEvent(new ChannelSelectionEvent(getSelectedChannels()));
        }

        void removeChannels(Channel[] chans) {

            for (int j=0; j<chans.length; j++) {
                String chanKey = ChannelIdUtil.toString(chans[j].get_id());
                if ( channelMap.containsKey(chanKey)) {
                    channelMap.remove(chanKey);
                }
            }
        }

        void removeChannels(Station station) {
            String stationPrefix =
                NetworkIdUtil.toString(station.get_id().network_id)+"."+
                station.get_id().station_code;
            Iterator it = channelMap.keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = (String)it.next();
                if (key.startsWith(stationPrefix)) {
                    it.remove();
                }
            }
        }

    }

    private void fireChannelSelectionEvent(ChannelSelectionEvent e){
        Iterator it = channelSelectionListeners.iterator();
        while(it.hasNext()){
            ((ChannelSelectionListener)it.next()).channelSelectionChanged(e);
        }
    }

    public void addChannelSelectionListener(ChannelSelectionListener csl){
        channelSelectionListeners.add(csl);
    }

    public void removeChannelSelectionListener(ChannelSelectionListener csl){
        channelSelectionListeners.remove(csl);
    }

    private AvailableDataStationRenderer stationRenderer;

    private List channelSelectionListeners = new ArrayList();
    static Category logger =
        Category.getInstance(ChannelChooser.class.getName());

} // ChannelChooser











