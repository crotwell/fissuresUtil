package edu.sc.seis.fissuresUtil.chooser;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * ChannelChooser.java
 * 
 * @author Philip Crotwell
 * @version $Id: ChannelChooser.java 22054 2011-02-16 16:51:38Z crotwell $
 */
/**
 * @author Charlie Groves
 */
public class ChannelChooser extends JPanel {

    public ChannelChooser(List<ChannelChooserSource> netDC) {
        this(netDC, false);
    }

    public ChannelChooser(List<ChannelChooserSource> netDC, boolean showSites) {
        this(netDC, showSites, new String[0]);
    }

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
                          String[] configuredNetworks) {
        this(netdcgiven, false, configuredNetworks);
    }

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
                          boolean showSites,
                          String[] configuredNetworks) {
        this(netdcgiven, showSites, true, configuredNetworks);
    }

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
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

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
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

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
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
             DEFAULT_SELECTABLE_ORIENTATIONS,
             defaultAutoSelectedOrientation);
    }

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
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

    public ChannelChooser(List<ChannelChooserSource> netdcgiven,
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
        progressBar.setStringPainted(true);
        bundle = ResourceBundle.getBundle(ChannelChooser.class.getName());
        initFrame();
        setConfiguredNetworks(configuredNetworks);
        setNetworkDCs(netdcgiven);
    }

    public void setSeismogramDC(ChannelChooserSeisSource seismogramSourceLocator) {
        if(stationRenderer != null) {
            stationRenderer.stopChecking();
        }
        stationRenderer = new AvailableDataStationRenderer(showNames,
                                                           showCodes,
                                                           codeIsFirst,
                                                           seismogramSourceLocator,
                                                           this);
        stationRenderer.setJList(stationList);
        setStationListCellRenderer(stationRenderer);
    }

    public void setShowCodes(boolean showCodes) {
        this.showCodes = showCodes;
        if(stationRenderer != null) {
            stationRenderer.setUseCodes(showCodes);
        }
    }

    public void setAvailbleDataOrigin(Origin origin) {
        if(stationRenderer != null) {
            stationRenderer.setOrigin(origin);
        }
    }

    public void addAvailableStationDataListener(AvailableStationDataListener dataListener) {
        if(stationRenderer != null) {
            stationRenderer.addAvailableStationDataListener(dataListener);
        } else {
            // this should probably never be allowed to happen???
            throw new NullPointerException("StationRenderer is null, so cannot add availableStationDataListener");
        }
    }

    public void recheckNetworkAvailability() {
        if(stationRenderer != null) {
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

    public Map<ChannelChooserSource, List<NetworkAttrImpl>> getNetDCToNetMap() {
        return netDCToNetMap;
    }

    public void setConfiguredNetworks(String[] configuredNetworks) {
        this.configuredNetworks = new String[configuredNetworks.length];
        System.arraycopy(configuredNetworks,
                         0,
                         this.configuredNetworks,
                         0,
                         configuredNetworks.length);
    }

    public List<ChannelChooserSource> getNetworkDCs() {
        return netdc;
    }

    public void setNetworkDCs(List<ChannelChooserSource> netdcgiven) {
        netdc = netdcgiven;
        channels.clear();
        sites.clear();
        clearStations();
        networks.clear();
        for (ChannelChooserSource channelChooserSource : netdcgiven) {
            NetworkLoader networkLoader = new NetworkLoader(channelChooserSource);
            if(!showNetworks) {
                networkLoader.setDoSelect(true);
            } else {
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

    public void appendNetwork(NetworkFromSource netsource) {
        netIdToNetMap.put(NetworkIdUtil.toString(netsource.getNetAttr().get_id()),
                          netsource.getSource());
        networks.addElement(netsource);
        fireNetworkDataChangedEvent(netsource);
        int index = networks.indexOf(netsource.getNetAttr());
        networkList.getSelectionModel().addSelectionInterval(index, index);
    }

    public void selectAllNetworks() {
        networkList.getSelectionModel()
                .setSelectionInterval(0, networkList.getModel().getSize() - 1);
    }

    private void layoutWidgets() {
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JScrollPane scroller;
        // networks
        if(showNetworks) {
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
        if(showSites) {
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
        // setSize(new java.awt.Dimension (mywidth, myheight));
        // setPreferredSize(new java.awt.Dimension (mywidth, myheight));
        netLabel = new JLabel(bundle.getString("LABEL_NETWORKS"));
        staLabel = new JLabel(bundle.getString("LABEL_STATIONS"));
        siLabel = new JLabel(bundle.getString("LABEL_SITES"));
        orientationLabel = new JLabel(bundle.getString("LABEL_ORIENTATIONS"));
        chLabel = new JLabel(bundle.getString("LABEL_CHANNELS"));
        netLabel.setToolTipText(lnettip);
        staLabel.setToolTipText(lstatip);
        siLabel.setToolTipText(lsittip);
        chLabel.setToolTipText(lchatip);
        networkList = new JList(networks);
        networkList.setCellRenderer(renderer);
        networkList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        networkList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) {
                    return;
                }
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                ListSelectionModel selections = networkList.getSelectionModel();
                for(int i = first; i <= last; i++) {
                    NetworkFromSource net = (NetworkFromSource)networks.get(i);
                    if(selections.isSelectedIndex(i)) {
                        // i is selected, so must have not been selected
                        // before. Note this may not be a good assumption
                        NetworkFromSource[] nets = {net};
                        StationLoader t = new StationLoader(ChannelChooser.this,
                                                            nets);
                        Iterator<StationAcceptor> it = stationAcceptors.iterator();
                        while(it.hasNext()) {
                            t.addStationAcceptor(it.next());
                        }
                        setStationLoader(t);
                        t.start();
                    } else {
                        // must have been deselected, maybe???
                        // probably should remove stations
                        List<StationImpl> newStations;
                        try {
                            newStations = net.getSource().getStations(net.getNetAttr());
                        for (StationImpl stationImpl : newStations) {
                            stationNames.removeElement(stationImpl.getName());
                            stationMap.remove(stationImpl.getName());
                        }
                        } catch(ChannelChooserException e1) {
                            GlobalExceptionHandler.handle("Unable to get stations.", e1);
                        }
                    }
                }
            }
        });
        stationList = new SortedStationJList(stationNames);
        stationList.sort();
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
                // added to see if selection will work properly in the map
                fireStationSelectedEvent(e);
            }
        });
        siteList = new JList(sites);
        siteList.setCellRenderer(renderer);
        siteList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        final ListCellRenderer bundleRenderer = new BundleListCellRenderer();
        String[] orientationTypes = new String[5];
        orientationTypes[THREE_COMPONENT] = "THREE_COMPONENT";
        orientationTypes[VERTICAL_ONLY] = "VERTICAL_ONLY";
        orientationTypes[HORIZONTAL_ONLY] = "HORIZONTAL_ONLY";
        orientationTypes[INDIVIDUAL_CHANNELS] = "INDIVIDUAL_CHANNELS";
        orientationTypes[BEST_CHANNELS] = "BEST_CHANNELS";
        orientationList = new JList(orientationTypes);
        orientationList.setCellRenderer(bundleRenderer);
        orientationList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        orientationList.getSelectionModel()
                .setSelectionInterval(autoSelectedOrientation,
                                      autoSelectedOrientation);
        orientationList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) {
                    return;
                }
                String selected = (String)orientationList.getSelectedValue();
                if((selected.equals("THREE_COMPONENT")
                        || selected.equals("VERTICAL_ONLY") || selected.equals("HORIZONTAL_ONLY"))
                        && channelList.getModel() != bandListModel) {
                    channelList.setModel(bandListModel);
                    channelList.setCellRenderer(bundleRenderer);
                } else if(selected.equals("INDIVIDUAL_CHANNELS")
                        && channelList.getModel() != channels) {
                    channelList.setModel(channels);
                    channelList.setCellRenderer(renderer);
                }
            }
        });
        if(selectableBand != null) {
            for(int i = 0; i < selectableBand.length; i++) {
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
        if(autoSelectedOrientation == INDIVIDUAL_CHANNELS) {
            channelList = new JList(channels);
            channelList.setCellRenderer(renderer);
        } else {
            channelList = new JList(bandListModel);
            channelList.setCellRenderer(bundleRenderer);
        } // end of else
        channelList.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ListModel chanListModel = channelList.getModel();
        ListSelectionModel channelSelctionModel = channelList.getSelectionModel();
        for(int j = 0; j < autoSelectBand.length; j++) {
            for(int i = 0; i < chanListModel.getSize(); i++) {
                String listElement = (String)chanListModel.getElementAt(i);
                if(listElement.equals(autoSelectBand[j])) {
                    channelSelctionModel.addSelectionInterval(i, i);
                    break;
                }
            } // end of for (int i=0; i<chanListModel; i++)
        } // end of for (int j=0; j<autoSelectBand.length; j++)
    }

    public List<NetworkFromSource> getNetworks() {
        List<NetworkFromSource> out = new ArrayList<NetworkFromSource>();
        Object[] objArray = networks.toArray();
        for (int i = 0; i < objArray.length; i++) {
            out.add((NetworkFromSource)objArray[i]);
        }
        return out;
    }

    public void addNetworkDataListener(NetworkDataListener s) {
        listenerList.add(NetworkDataListener.class, s);
    }

    protected void fireNetworkDataChangedEvent(NetworkFromSource netSource) {
        NetworkDataEvent networkDataEvent = null;
        // logger.debug("fireStationDataEvent called");
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == NetworkDataListener.class) {
                // Lazily create the event:
                if(networkDataEvent == null)
                    networkDataEvent = new NetworkDataEvent(this, netSource);
                ((NetworkDataListener)listeners[i + 1]).networkDataChanged(networkDataEvent);
                // logger.debug("fireStationDataEvent!");
            }
        }
    }

    protected void addStations(List<StationImpl> stations) {
        boolean addedStation = false;
        for (StationImpl sta : stations) {
            if(!stationMap.containsKey(sta.getName())) {
                stationNames.addElement(sta);
                addedStation = true;
            } // end of if ()
            List<StationImpl> staList = stationMap.get(sta.getName());
            if(staList == null) {
                staList = new ArrayList<StationImpl>();
                stationMap.put(sta.getName(), staList);
            } // end of if ()
            staList.add(sta);
        }
        if(addedStation) {
            stationList.sort();
        }
        fireStationDataChangedEvent(stations.toArray(new StationImpl[0]));
    }

    /**
     * Adds a stations, but using SwingUtilities.invokeLater. This allows
     * threads beside the event dispatch thread to interact with the swing
     * widgets.
     */
    protected void addStationsFromThread(final List<StationImpl> sta) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                addStations(sta);
            }
        });
    }

    public void addStationDataListener(StationDataListener s) {
        listenerList.add(StationDataListener.class, s);
        s.stationDataCleared();
        s.stationDataChanged(new StationDataEvent(getStations()));
    }

    public void addStationSelectionListener(StationSelectionListener s) {
        listenerList.add(StationSelectionListener.class, s);
        s.stationSelectionChanged(new StationSelectionEvent(this,
                                                            getSelectedStations()));
    }

    protected void fireStationDataChangedEvent(Station[] stations) {
        StationDataEvent stationDataEvent = new StationDataEvent(stations);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == StationDataListener.class) {
                ((StationDataListener)listeners[i + 1]).stationDataChanged(stationDataEvent);
            }
        }
    }

    protected void fireStationDataClearedEvent() {
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == StationDataListener.class) {
                ((StationDataListener)listeners[i + 1]).stationDataCleared();
            }
        }
    }

    protected void fireStationSelectedEvent(ListSelectionEvent e) {
        StationSelectionEvent stationSelectionEvent = new StationSelectionEvent(this,
                                                                                getSelectedStations());
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == StationSelectionListener.class) {
                ((StationSelectionListener)listeners[i + 1]).stationSelectionChanged(stationSelectionEvent);
            }
        }
    }

    public Station[] getStations() {
        LinkedList<StationImpl> out = new LinkedList<StationImpl>();
        Object[] objArray = stationNames.toArray();
        // logger.debug("Object array length: " + objArray.length);
        // logger.debug("stationNames size: " + stationNames.getSize());
        for(int i = 0; i < objArray.length; i++) {
            String name = ((Station)objArray[i]).getName();
            List<StationImpl> staList = stationMap.get(name);
            if(staList == null) {
                logger.warn("no stations for name=" + name);
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
        } catch(InterruptedException e) {
            logger.warn("Caught exception while clearing stations, will continue. Hope all is well...",
                        e);
            // oh well...
        } catch(InvocationTargetException e) {
            logger.warn("Caught exception while clearing stations, will continue. Hope all is well...",
                        e);
            // oh well...
        } // end of try-catch
    }
    
    public Channel getChannel(ChannelId chanId) {
        return channelMap.get(ChannelIdUtil.toString(chanId));
    }

    public Channel[] getChannels() {
        Channel[] outChannels = (Channel[])channelMap.values()
                .toArray(new Channel[0]);
        return outChannels;
    }

    public List<ChannelImpl> getChannels(StationImpl station) throws ChannelChooserException {
        ChannelChooserSource net = netIdToNetMap.get(NetworkIdUtil.toString(station.get_id().network_id));
        List<ChannelImpl> staChans = net.getChannels(station);
        return staChans;
    }

    public List<ChannelImpl> getChannels(StationImpl station, MicroSecondDate when) throws ChannelChooserException {
        List<ChannelImpl> in = getChannels(station);
        List<ChannelImpl> out = new ArrayList<ChannelImpl>();
        for (ChannelImpl channelImpl : in) {
            MicroSecondDate b = new MicroSecondDate(channelImpl.getBeginTime());
            MicroSecondDate e = new MicroSecondDate(channelImpl.getEndTime());
            if(when.after(b) && when.before(e)) {
                out.add(channelImpl);
            }
        } // end of for ()
        return out;
    }

    /**
     * returns selected items from channel list. May be full codes like BHZ or
     * just band codes like B
     */
    public String[] getSelectedChanCodes() {
        Object[] vals = channelList.getSelectedValues();
        String[] out = new String[vals.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = (String)vals[i];
        }
        return out;
    }

    public List<NetworkFromSource> getSelectedNetworks() {
        if(!showNetworks) {
            return getNetworks();
        }
        List<NetworkFromSource> out = new ArrayList<NetworkFromSource>();
        Object[] selected = networkList.getSelectedValues();
        for (int i = 0; i < selected.length; i++) {
            out.add((NetworkFromSource)selected[i]);
        }
        return out;
    }

    public StationImpl[] getSelectedStations() {
        List<StationImpl> out = new LinkedList<StationImpl>();
        Object[] selected = stationList.getSelectedValues();
        for(int i = 0; i < selected.length; i++) {
            List<StationImpl> staList = stationMap.get(((Station)selected[i]).getName());
            out.addAll(staList);
        } // end of for ()
        return (StationImpl[])out.toArray(new StationImpl[0]);
    }

    public StationImpl[] getSelectedStations(MicroSecondDate when) {
        StationImpl[] in = getSelectedStations();
        return getStationsThatExistOnDate(when, in);
    }

    public static StationImpl[] getStationsThatExistOnDate(MicroSecondDate when,
                                                       StationImpl[] in) {
        List<StationImpl> out = new ArrayList<StationImpl>();
        for(int i = 0; i < in.length; i++) {
            MicroSecondDate b = new MicroSecondDate(in[i].getBeginTime());
            MicroSecondDate e = new MicroSecondDate(in[i].getEndTime());
            if(when.after(b) && when.before(e)) {
                out.add(in[i]);
            }
        } // end of for ()
        return (StationImpl[])out.toArray(new StationImpl[out.size()]);
    }

    public void clearStationSelection() {
        stationList.getSelectionModel().clearSelection();
    }

    public void toggleStationSelected(Station stat) {
        ListSelectionModel selModel = stationList.getSelectionModel();
        int i = findIndex(stat);
        if(selModel.isSelectedIndex(i)) {
            selModel.removeSelectionInterval(i, i);
        } else {
            selModel.addSelectionInterval(i, i);
        }
        stationList.ensureIndexIsVisible(i);
    }

    public void select(Station stat) {
        ListSelectionModel selModel = stationList.getSelectionModel();
        int i = findIndex(stat);
        selModel.addSelectionInterval(i, i);
        stationList.ensureIndexIsVisible(i);
    }

    public void deselect(Station stat) {
        ListSelectionModel selModel = stationList.getSelectionModel();
        int i = findIndex(stat);
        selModel.removeSelectionInterval(i, i);
        stationList.ensureIndexIsVisible(i);
    }

    private int findIndex(Station stat) {
        ListModel listModel = stationList.getModel();
        for(int i = 0; i < listModel.getSize(); i++) {
            Station cur = (Station)listModel.getElementAt(i);
            if(cur.equals(stat)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSourceKnown(NetworkId netid) {
        ChannelChooserSource net = netIdToNetMap.get(NetworkIdUtil.toString(netid));
        if(net == null) {
            return false;
        } else {
            return true;
        }
    }

    public ChannelChooserSource getSource(NetworkId netid) {
        ChannelChooserSource net = netIdToNetMap.get(NetworkIdUtil.toString(netid));
        if(net == null) {
            logger.debug("source for " + NetworkIdUtil.toString(netid)
                    + " is not yet loaded, trying from remote server");
            // may not be loaded yet, try to get from dc?
            List<ChannelChooserSource> retrievedDCs = getNetworkDCs();
            for (ChannelChooserSource channelChooserSource : retrievedDCs) {
                try {
                    List<? extends NetworkAttrImpl> netsFromSource = channelChooserSource.getNetworks();
                    for (NetworkAttrImpl networkAttrImpl : netsFromSource) {
                        if (NetworkIdUtil.areEqual(networkAttrImpl.get_id(), netid)) {
                            return channelChooserSource;
                        }
                    }
                } catch(org.omg.CORBA.COMM_FAILURE e) {
                    // oh well, try next
                } catch(ChannelChooserException e) {
                    // oh well, try next
                    logger.warn("unable to get networks from "+channelChooserSource.toString());
                }
            }
        }
        return net;
    }

    /**
     * @return selected channels whose stations are active now
     * @throws ChannelChooserException 
     */
    public ChannelImpl[] getSelectedChannels() throws ChannelChooserException {
        return getSelectedChannels(ClockUtil.now());
    }

    /**
     * Gets the selected channels, but only if they overlap the given time.
     * @throws ChannelChooserException 
     */
    public ChannelImpl[] getSelectedChannels(MicroSecondDate when) throws ChannelChooserException {
        List<ChannelImpl> outChannels = new LinkedList<ChannelImpl>();
        StationImpl[] selectedStations = getSelectedStations(when);
        logger.debug(selectedStations.length + " stations before pruning");
        List<StationImpl> outStations = new LinkedList<StationImpl>();
        for(int i = 0; i < selectedStations.length - 1; i++) {
            boolean foundDup = false;
            for(int j = i + 1; j < selectedStations.length; j++) {
                if(selectedStations[i].getName().equals(selectedStations[j].getName())
                        && selectedStations[i].get_code()
                                .equals(selectedStations[j].get_code())
                        && selectedStations[i].getLocation().latitude == selectedStations[j].getLocation().latitude
                        && selectedStations[i].getLocation().longitude == selectedStations[j].getLocation().longitude) {
                    foundDup = true;
                    break;
                }
            }
            if(!foundDup) {
                outStations.add(selectedStations[i]);
            }
        }
        if(selectedStations.length > 0) {
            outStations.add(selectedStations[selectedStations.length - 1]);
        }
        selectedStations = (StationImpl[])outStations.toArray(new StationImpl[outStations.size()]);
        logger.debug(selectedStations.length + " stations after pruning");
        Object[] selectedChannelCodes = channelList.getSelectedValues();
        String[] siteCodeHeuristic = bestChanUtil.getSiteCodeHeuristic();
        for(int staNum = 0; staNum < selectedStations.length; staNum++) {
            ChannelChooserSource net = netIdToNetMap.get(NetworkIdUtil.toString(selectedStations[staNum].get_id().network_id));
            ChannelImpl[] staChans = ChannelImpl.implize(net.getChannels(selectedStations[staNum]).toArray(new ChannelImpl[0]));
            if(!showSites) {
                if(orientationList.getSelectedValue()
                        .equals("INDIVIDUAL_CHANNELS")) {
                    // use real channel codes
                    bandSearch : for(int bandNum = 0; bandNum < selectedChannelCodes.length; bandNum++) {
                        for(int chanNum = 0; chanNum < staChans.length; chanNum++) {
                            for(int h = 0; h < siteCodeHeuristic.length; h++) {
                                if(staChans[chanNum].getSite().get_code()
                                        .equals(siteCodeHeuristic[h])
                                        && staChans[chanNum].get_code()
                                                .equals(selectedChannelCodes[bandNum])) {
                                    outChannels.add(staChans[chanNum]);
                                    continue bandSearch;
                                }
                            }
                        }
                    }
                    // end of if INDIVIDUAL_CHANNELS
                } else {
                    String orientation = (String)orientationList.getSelectedValue();
                    for(int i = 0; i < selectedChannelCodes.length; i++) {
                        String bc = (String)selectedChannelCodes[i];
                        ChannelImpl[] chans = null;
                        if(orientation.equals("VERTICAL_ONLY")) {
                            chans = getVerticalChannel(staChans, bc);
                        } else if(orientation.equals("HORIZONTAL_ONLY")) {
                            chans = getHorizontalChannels(staChans, bc);
                        } else if(orientation.equals("THREE_COMPONENT")) {
                            chans =  bestChanUtil.getBestMotionVector(bestChanUtil.getAllBand(Arrays.asList(staChans), bc));
                        } else if(orientation.equals("BEST_CHANNELS")) {
                            chans =  bestChanUtil.getBestMotionVector(bestChanUtil.getAllBand(Arrays.asList(staChans), bc));
                            if (chans == null || chans.length == 0) {
                                chans = new ChannelImpl[] {bestChanUtil.getBestChannel(bestChanUtil.getAllBand(Arrays.asList(staChans), bc))};
                            }
                        }
                        if(chans != null) {
                            for(int j = 0; j < chans.length; j++) {
                                ChannelImpl channel = chans[j];
                                if(channel != null) {
                                    outChannels.add(channel);
                                }
                            }
                        }
                    }
                }
            } else {
                // pay attention to selected Sites
                ChannelImpl[] inChannels = ChannelImpl.implize(getChannels());
                inChannels = BestChannelUtil.pruneChannels(Arrays.asList(inChannels), when).toArray(new ChannelImpl[0]);
                Object[] selectedSiteCodes = siteList.getSelectedValues();
                search : for(int i = 0; i < inChannels.length; i++) {
                    for(int j = 0; j < selectedSiteCodes.length; j++) {
                        for(int k = 0; k < selectedChannelCodes.length; k++) {
                            if(inChannels[i].getSite().get_code()
                                    .equals(selectedSiteCodes[j])
                                    && inChannels[i].get_code()
                                            .equals(selectedChannelCodes[k])) {
                                outChannels.add(inChannels[i]);
                                continue search;
                            }
                        }
                    }
                }
            } // end of if (showSites)
        } // end of for (int staNum=0; staNum<selStation.length; staNum++)
        logger.debug("Found " + outChannels.size() + " channels");
        return (ChannelImpl[])outChannels.toArray(new ChannelImpl[0]);
    }

    private ChannelImpl[] getVerticalChannel(ChannelImpl[] chanArray, String bandCode) {
        return new ChannelImpl[] {bestChanUtil.getBestChannel(BestChannelUtil.getAllBand(Arrays.asList(chanArray),
                                                                                  bandCode))};
    }

    private ChannelImpl[] getHorizontalChannels(ChannelImpl[] allChannels,
                                            String bandCode) {
        ChannelImpl[] vector = bestChanUtil.getBestMotionVector(BestChannelUtil.getAllBand(Arrays.asList(allChannels), bandCode));
        return bestChanUtil.getAllHorizontal(Arrays.asList(vector)).toArray(new ChannelImpl[0]);
    }

    /**
     * Get the value of stationLoader.
     * 
     * @return value of stationLoader.
     */
    protected synchronized StationLoader getStationLoader() {
        return stationLoader;
    }

    /**
     * Set the value of stationLoader.
     * 
     * @param v
     *            Value to assign to stationLoader.
     */
    protected synchronized void setStationLoader(StationLoader v) {
        this.stationLoader = v;
    }

    /**
     * Get the value of channelLoader.
     * 
     * @return value of channelLoader.
     */
    protected synchronized ChannelLoader getChannelLoader() {
        return channelLoader;
    }

    /**
     * Set the value of channelLoader.
     * 
     * @param v
     *            Value to assign to channelLoader.
     */
    protected synchronized void setChannelLoader(ChannelLoader v) {
        this.channelLoader = v;
    }

    /**
     * sets this thread as the owner of the progress bar. It is the only thread
     * that can update the progress bar. Also resets the value to 0;
     */
    protected synchronized void setProgressOwner(Thread t) {
        progressBar.setValue(0);
        progressOwner = t;
    }

    protected synchronized void setProgressValue(Thread t, int value) {
        if(t.equals(progressOwner)) {
            progressBar.setValue(value);
        }
    }

    protected synchronized void setProgressMax(Thread t, int max) {
        if(t.equals(progressOwner)) {
            progressBar.setMaximum(max);
        }
    }

    /* ================Class Variables=============== */
    
    protected BestChannelUtil bestChanUtil = new BestChannelUtil();
    
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

    public static final int BEST_CHANNELS = 0;

    public static final int VERTICAL_ONLY = 1;

    public static final int HORIZONTAL_ONLY = 2;

    public static final int INDIVIDUAL_CHANNELS = 3;

    public static final int THREE_COMPONENT = 4;

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

    private static final String[] defaultSelectableBand = {BROAD_BAND,
                                                           LONG_PERIOD};

    private static final String[] defaultAutoSelectBand = {BROAD_BAND};

    public static final int[] DEFAULT_SELECTABLE_ORIENTATIONS = {BEST_CHANNELS,
                                                                 VERTICAL_ONLY,
                                                                 HORIZONTAL_ONLY,
                                                                 INDIVIDUAL_CHANNELS,
                                                                 THREE_COMPONENT};

    private static final int defaultAutoSelectedOrientation = BEST_CHANNELS;

    private JLabel netLabel;

    private JLabel staLabel;

    private JLabel siLabel;

    private JLabel orientationLabel;

    private JLabel chLabel;

    protected JList networkList;

    protected SortedStationJList stationList;

    protected JList siteList;

    protected JList orientationList;

    protected JList channelList;

    protected JProgressBar progressBar = new JProgressBar(0, 100);

    protected List<StationAcceptor> stationAcceptors = new LinkedList<StationAcceptor>();

    protected DefaultListModel networks = new DefaultListModel();

    protected DefaultListModel stationNames = new DefaultListModel();

    protected HashMap<String, List<StationImpl>> stationMap = new HashMap<String, List<StationImpl>>();

    protected DefaultListModel sites = new DefaultListModel();

    protected DefaultListModel channels = new DefaultListModel();

    protected DefaultListModel bandListModel = new DefaultListModel();

    protected HashMap<String, Channel> channelMap = new HashMap<String, Channel>();

    private List<ChannelChooserSource> netdc;

    protected HashMap<ChannelChooserSource, List<NetworkAttrImpl>> netDCToNetMap = new HashMap<ChannelChooserSource, List<NetworkAttrImpl>>();

    protected HashMap<String, ChannelChooserSource> netIdToNetMap = new HashMap<String, ChannelChooserSource>();

    private StationLoader stationLoader = null;

    private ChannelLoader channelLoader = null;

    private Thread progressOwner = null;

    private GridBagConstraints gbc;

    final ListCellRenderer renderer = new NameListCellRenderer(true,
                                                               false,
                                                               true);

    class BundleListCellRenderer extends DefaultListCellRenderer {

        BundleListCellRenderer() {}

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
            } catch(java.util.MissingResourceException e) {
                try {
                    // try NAME_value
                    name = bundle.getString("BANDNAME_" + (String)value);
                    useValue = name;
                } catch(java.util.MissingResourceException ee) {
                    // use default value???
                    useValue = value;
                } // end of try-catch
            } // end of try-catch
            if(useValue.equals(name) && (name == null || name.length() == 0)) {
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

        ChannelChooserSource netDC;

        boolean doSelect = true;

        public NetworkLoader(ChannelChooserSource channelChooserSource) {
            this.netDC = channelChooserSource;
        }

        public void setDoSelect(boolean b) {
            doSelect = b;
        }

        public void run() {
            setProgressOwner(this);
            if(configuredNetworks == null || configuredNetworks.length == 0) {
                List<NetworkAttrImpl> nets;
                try {
                    logger.info("before get networks: "+netDC);
                    nets = netDC.getNetworks();
                    // I don't think this should ever happen, but...
                    if(nets == null) {
                        nets = new ArrayList<NetworkAttrImpl>();
                        logger.warn("the array returned from NetworkFinder.retrieve_all() is null.  this is wrong.");
                    }
                    netDCToNetMap.put(netDC, nets);
                    setProgressMax(this, nets.size() + 1);
                    int progressVal = 1;
                    setProgressValue(this, progressVal);
                    progressVal++;
                    for (NetworkAttrImpl networkAttrImpl : nets) {
                        // skip null networks...probably a bug on the server
                        if(networkAttrImpl != null) {
                            networkAdd(new NetworkFromSource(networkAttrImpl, netDC));
                        } else {
                            logger.warn("a networkaccess returned from NetworkFinder.retrieve_all() is null, skipping.");
                        } // end of else
                        setProgressValue(this, progressVal);
                        progressVal++;
                    }
                } catch(ChannelChooserException e) {
                    logger.warn("Unable to get networks from "+netDC, e);
                }
            } else {
                // when the channelChooser is configured with networkCodes....
                int totalNetworks = 0;
                setProgressMax(this, configuredNetworks.length);
                for(int counter = 0; counter < configuredNetworks.length; counter++) {
                    try {
                        List<NetworkAttrImpl> netList = netDC.getNetworks();
                        for (NetworkAttrImpl netAttr : netList) {
                            if (netAttr.get_code().equals(configuredNetworks[counter])) {
                                List<NetworkAttrImpl> storedNets = netDCToNetMap.get(netDC);
                                if(storedNets == null) {
                                    storedNets = new ArrayList<NetworkAttrImpl>();
                                    storedNets.add(netAttr);
                                    netDCToNetMap.put(netDC, storedNets);
                                } else {
                                    netDCToNetMap.get(netDC).add(netAttr);
                                } // end of else
                                networkAdd(new NetworkFromSource(netAttr, netDC));
                                totalNetworks++;
                            } else {
                                logger.warn("a networkaccess returned from NetworkFinder.retrieve_by_code is null, skipping.");
                            } // end of else
                        }// end of inner for subCounter = 0;
                    } catch(ChannelChooserException e) {
                        logger.warn("Problem with Network "
                                + configuredNetworks[counter]
                                + " from source "+netDC);
                    }
                    setProgressValue(this, counter + 1);
                }// end of outer for counter = 0;
            }// end of if else checking for configuredNetworks == null
            if(doSelect) {
                // need to do this later to give java Event thread time to set
                // up network list before setting selection
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        selectAllNetworks();
                    }
                });
            } // end of if ()
            setProgressValue(this, progressBar.getMaximum());
        }

        void networkAdd(final NetworkFromSource netSource) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    appendNetwork(netSource);
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
            setProgressMax(this, e.getLastIndex() - e.getFirstIndex() + 1);
            for(int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                String staName = ((Station)stationNames.getElementAt(i)).getName();
                List<StationImpl> stations = (List<StationImpl>)stationMap.get(staName);
                Iterator<StationImpl> it = stations.iterator();
                while(it.hasNext()) {
                    StationImpl sta = it.next();
                    // unselected station, remove all channels from this station
                    synchronized(ChannelChooser.this) {
                        if(this.equals(getChannelLoader())) {
                            if(!stationList.isSelectedIndex(i)) {
                                removeChannels(sta);
                                continue;
                            } // end of else
                        } else {
                            // no loner active channel loader
                            return;
                        } // end of else
                    }
                    List<ChannelImpl> chans = null;
                    ChannelChooserSource net = netIdToNetMap.get(NetworkIdUtil.toString(sta.get_id().network_id));
                    if(net != null) {
                        try {
                            chans = net.getChannels(sta);
                        } catch(ChannelChooserException e1) {
                            GlobalExceptionHandler.handle(e1);
                        }
                    } else {
                        logger.warn("Unable to find network server for station "
                                + sta.getName()
                                + " "
                                + StationIdUtil.toString(sta.get_id()));
                        continue;
                    } // end of if ()
                    synchronized(ChannelChooser.this) {
                        if(this.equals(getChannelLoader())) {
                            if(stationList.isSelectedIndex(i)) {
                                try {
                                    addChannels(chans);
                                } catch(ChannelChooserException e1) {
                                    GlobalExceptionHandler.handle(e1);
                                }
                            } // end of else
                        } else {
                            // no loner active channel loader
                            return;
                        } // end of else
                    }
                }
                setProgressValue(this, i - e.getFirstIndex());
            } // end of for (int i=e.getFirstIndex(); i<e.getLastIndex(); i++)
            progressBar.setValue(progressBar.getMaximum());
        }

        void addChannels(List<ChannelImpl> chans) throws ChannelChooserException {
            for (ChannelImpl channelImpl : chans) {
                String chanKey = ChannelIdUtil.toString(channelImpl.get_id());
                if(!channelMap.containsKey(chanKey)) {
                    channelMap.put(chanKey, channelImpl);
                    if(!sites.contains(channelImpl.getSite().get_code())) {
                        sites.addElement(channelImpl.getSite().get_code());
                    }
                    if(!channels.contains(channelImpl.get_code())) {
                        channels.addElement(channelImpl.get_code());
                    }
                }
            } // end of for (int j=0; j<chans.length; j++)
            fireChannelSelectionEvent(new ChannelSelectionEvent(getSelectedChannels()));
        }

        void removeChannels(Channel[] chans) {
            for(int j = 0; j < chans.length; j++) {
                String chanKey = ChannelIdUtil.toString(chans[j].get_id());
                if(channelMap.containsKey(chanKey)) {
                    channelMap.remove(chanKey);
                }
            }
        }

        void removeChannels(Station station) {
            String stationPrefix = NetworkIdUtil.toString(station.get_id().network_id)
                    + "." + station.get_id().station_code;
            Iterator<String> it = channelMap.keySet().iterator();
            String key;
            while(it.hasNext()) {
                key = it.next();
                if(key.startsWith(stationPrefix)) {
                    it.remove();
                }
            }
        }
    }

    private void fireChannelSelectionEvent(ChannelSelectionEvent e) {
        Iterator<ChannelSelectionListener> it = channelSelectionListeners.iterator();
        while(it.hasNext()) {
            ((ChannelSelectionListener)it.next()).channelSelectionChanged(e);
        }
    }

    public void addChannelSelectionListener(ChannelSelectionListener csl) {
        channelSelectionListeners.add(csl);
    }

    public void removeChannelSelectionListener(ChannelSelectionListener csl) {
        channelSelectionListeners.remove(csl);
    }

    private AvailableDataStationRenderer stationRenderer;

    private List<ChannelSelectionListener> channelSelectionListeners = new ArrayList<ChannelSelectionListener>();

    private static Logger logger = LoggerFactory.getLogger(ChannelChooser.class.getName());
} // ChannelChooser
