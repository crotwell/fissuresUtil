
package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventBackgroundLoaderPool;
import edu.sc.seis.fissuresUtil.cache.EventLoadedListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.WeakHashMap;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Category;
import edu.iris.Fissures.IfEvent.EventAccess;

/**
 * EventTableModel.java
 *
 *
 * Created: Mon Jan  8 15:59:05 2001
 *
 * @author Philip Crotwell
 * @version $Id: EventTableModel.java 4633 2003-07-03 17:28:36Z oliverpa $
 */

public class EventTableModel
    extends AbstractTableModel
    implements EventLoadedListener {

    /** Creates a table model without any events. Events can be
     *  added later with the updateEvents method. */
    public EventTableModel() {
        this(new EventAccessOperations[0]);
    }

    public EventTableModel(EventAccessOperations[] events) {
        loader = new EventBackgroundLoaderPool(5, this);
        updateEvents(events);
        columnNames = new String[9];
        columnNames[LATITUDE] = "Latitude";
        columnNames[LONGITUDE] = "Longitude";
        columnNames[DEPTH] = "Depth";
        columnNames[ORIGINTIME] = "Origin Time";
        columnNames[MAGVALUE] = "Magnitude";
        columnNames[CATALOG] = "Catalog";
        columnNames[CONTRIBUTOR] = "Contributor";
        //columnNames[NAME] = "Name";
        columnNames[FEREGION] = "Region";
        ///setColumnSizes();

    }

    public EventBackgroundLoaderPool getLoader(){ return loader; }

    public int getColumnCount() { return 8; }

    public int getRowCount() { return events.length; }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public Object getValueAt(int row, int col) {
        if ( ! isRowCached(row)) {
            return "...";
        }
        CacheEvent cache = getEventForRow(row);
        try {
            switch (col) {
                case NAME:
                    if (cache.get_attributes() == null) {
                        return "";
                    }
                    return cache.get_attributes().name;
                case FEREGION:
                    if (cache.get_attributes() == null ||
                        cache.get_attributes().region == null) {
                        return "";
                    }
                    return FERegions.getGeographicRegionName(cache.get_attributes().region.number);
                case CATALOG:
                    return cache.get_preferred_origin().catalog;
                case CONTRIBUTOR:
                    return cache.get_preferred_origin().contributor;
                case LATITUDE:
                    return new Float(cache.get_preferred_origin().my_location.latitude);
                case LONGITUDE:
                    return new Float(cache.get_preferred_origin().my_location.longitude);
                case DEPTH:
                    QuantityImpl q = (QuantityImpl)cache.get_preferred_origin().my_location.depth;
                    q = q.convertTo(UnitImpl.KILOMETER);
                    return depthFormat.format(q.getValue())+" km";
                case ORIGINTIME:
                    edu.iris.Fissures.Time fisDate =
                        cache.get_preferred_origin().origin_time;
                    MicroSecondDate msd = new MicroSecondDate(fisDate);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return sdf.format(msd);
                    //return msd.toString();
                case MAGVALUE:
                    if (cache.get_preferred_origin().magnitudes.length == 0) {
                        return "none";
                    }

                    String type =  cache.get_preferred_origin().magnitudes[0].type;
                    if (type.equals(edu.iris.Fissures.MB_MAG_TYPE.value)) {
                        type = "mb";
                    }
                    if (type.equals(edu.iris.Fissures.ML_MAG_TYPE.value)) {
                        type = "ml";
                    }
                    if (type.equals(edu.iris.Fissures.MBMLE_MAG_TYPE.value)) {
                        type = "mbmle";
                    }
                    if (type.equals(edu.iris.Fissures.MO_MAG_TYPE.value)) {
                        type = "MO";
                    }
                    if (type.equals(edu.iris.Fissures.MS_MAG_TYPE.value)) {
                        type = "Ms";
                    }
                    if (type.equals(edu.iris.Fissures.MSMLE_MAG_TYPE.value)) {
                        type = "msmle";
                    }
                    if (type.equals(edu.iris.Fissures.MW_MAG_TYPE.value)) {
                        type = "MW";
                    }
                    return new Float(cache.get_preferred_origin().magnitudes[0].value).toString() + " "+type;
                default:
                    return "XXXX";
            }
        } catch (NoPreferredOrigin e) {
            return "No Pref Origin";
        } catch (Exception e) {
            logger.warn("Got exception in Table model: getValueAt("+row+", "+ col+")", e);
            e.printStackTrace();
            return "error";
        } // end of catch

    }

    public CacheEvent getEventForRow(int row) {
        if (isRowCached(row) ) {
            return (CacheEvent)cachedEvents.get(events[row]);
        } else {
            CacheEvent cache = (CacheEvent)backgrounded.get(events[row]);
            if (cache == null) {
                cache = (CacheEvent)cachedEvents.get(events[row]);
            }
            return cache;
        }
    }

    public boolean isRowCached(int row) {
        if (backgrounded.containsKey(events[row])) {
            return false;
        }
        if ( ! cachedEvents.containsKey(events[row])) {
            // load in background
            CacheEvent cache;
            if (events[row] instanceof CacheEvent){
                cache = (CacheEvent)events[row];
            }
            else{
                cache = new CacheEvent(events[row]);
            }
            backgrounded.put(events[row], cache);
            System.out.println("ETM 173: events[row]: " + events[row]);
            System.out.println("ETM 174: cache: " + cache);
            rowNumber.put(events[row], new Integer(row));
            loader.getEvent(events[row], cache, this);
            return false;
        } else {
            return true;
        }
    }

    public void updateEvents(EventAccessOperations[] events) {
        this.events = events;
        cachedEvents.clear();
        rowNumber.clear();
        fireEventDataCleared();
        System.out.println("TableModel Got new events "+events.length);
        fireTableDataChanged();
        fireEventDataChanged(events);
    }

    public void eventLoaded(CacheEvent cache) {
        cachedEvents.put(cache, cache);
        backgrounded.remove(cache);
        Integer rowNum = (Integer)rowNumber.get(cache);
        if (rowNum != null){
            fireTableRowsUpdated(rowNum.intValue(), rowNum.intValue());
        }
    }

    public void addEventDataListener(EventDataListener edl){
        listenerList.add(EventDataListener.class, edl);
    }

    private void fireEventDataChanged(EventAccessOperations[] events){
        EQDataEvent eqDataEvent = null;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==EventDataListener.class) {
                // Lazily create the event:
                if (eqDataEvent == null)
                    eqDataEvent = new EQDataEvent(this, events);
                ((EventDataListener)listeners[i+1]).eventDataChanged(eqDataEvent);
            }
        }
    }

    private void fireEventDataCleared(){
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==EventDataListener.class) {
                // Lazily create the event:
                ((EventDataListener)listeners[i+1]).eventDataCleared();
            }
        }
    }

	public void addEQSelectionListener(EQSelectionListener eqSelListener){
		listenerList.add(EQSelectionListener.class, eqSelListener);
	}

	private void fireEQSelectionChanged(){
        EQSelectionEvent eqSelectionEvent = null;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==EQSelectionListener.class) {
                // Lazily create the event:
                if (eqSelectionEvent == null)
                    eqSelectionEvent = new EQSelectionEvent(this, selectedEvent);
                ((EQSelectionListener)listeners[i+1]).eqSelectionChanged(eqSelectionEvent);
            }
        }
	}

    public EventAccessOperations[] getAllEvents(){
        EventAccessOperations[] eaos = new EventAccessOperations[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            eaos[i] = getEventForRow(i);
        }
        return eaos;
    }

	public EventAccessOperations selectEvent(int row){
		selectedEvent = getEventForRow(row);
		fireEQSelectionChanged();
		return selectedEvent;
	}

    protected ParseRegions FERegions = new ParseRegions();

    protected EventAccessOperations[] events;

    protected WeakHashMap cachedEvents = new WeakHashMap();

    protected WeakHashMap backgrounded = new WeakHashMap();

    protected HashMap rowNumber = new HashMap();

    protected NumberFormat depthFormat = new DecimalFormat("0.0");

    protected EventBackgroundLoaderPool loader;

	protected EventAccessOperations selectedEvent;

    protected static final int LATITUDE = 4;
    protected static final int LONGITUDE = 5;
    protected static final int DEPTH = 3;
    protected static final int ORIGINTIME = 2;
    protected static final int MAGVALUE = 1;
    protected static final int CATALOG = 6;
    protected static final int CONTRIBUTOR = 7;
    protected static final int NAME = 8;
    protected static final int FEREGION = 0;


    protected static final int LATITUDESIZE = 50;
    protected static final int LONGITUDESIZE = 50;
    protected static final int DEPTHSIZE = 50;
    protected static final int ORIGINTIMESIZE = 150;
    protected static final int MAGVALUESIZE = 100;
    protected static final int CATALOGSIZE = 100;
    protected static final int CONTRIBUTORSIZE = 100;
    protected static final int NAMESIZE = 8;
    protected static final int FEREGIONSIZE = 150;



    protected String[] columnNames;

    static Category logger = Category.getInstance(EventTableModel.class.getName());

} // EventTableModel


