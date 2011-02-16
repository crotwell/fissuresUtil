package edu.sc.seis.fissuresUtil.display;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.WeakHashMap;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventLoadedListener;
import edu.sc.seis.fissuresUtil.cache.EventLoader;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * EventTableModel.java
 * 
 * 
 * Created: Mon Jan 8 15:59:05 2001
 * 
 * @author Philip Crotwell
 * @version $Id: EventTableModel.java 22054 2011-02-16 16:51:38Z crotwell $
 */
public class EventTableModel extends AbstractTableModel implements
        EventLoadedListener {

    /**
     * Creates a table model without any events. Events can be added later with
     * the updateEvents method.
     */
    public EventTableModel() {
        this(new ProxyEventAccessOperations[0]);
    }

    public EventTableModel(ProxyEventAccessOperations[] events) {
        updateEvents(events);
        columnNames = new String[9];
        columnNames[LATITUDE] = "Latitude";
        columnNames[LONGITUDE] = "Longitude";
        columnNames[DEPTH] = "Depth";
        columnNames[ORIGINTIME] = "Origin Time";
        columnNames[MAGVALUE] = "Magnitude";
        columnNames[CATALOG] = "Catalog";
        columnNames[CONTRIBUTOR] = "Contributor";
        // columnNames[NAME] = "Name";
        columnNames[FEREGION] = "Region";
        // /setColumnSizes();
    }

    public int getColumnCount() {
        return 8;
    }

    public int getRowCount() {
        return getAllEvents().length;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public Object getValueAt(int row, int col) {
        if(!isRowCached(row)) {
            return "...";
        }
        EventAccessOperations cache = getEventForRow(row);
        try {
            switch(col){
                case NAME:
                    if(cache.get_attributes() == null) {
                        return "";
                    }
                    return cache.get_attributes().name;
                case FEREGION:
                    if(cache.get_attributes() == null
                            || cache.get_attributes().region == null) {
                        return "";
                    }
                    return FERegions.getGeographicRegionName(cache.get_attributes().region.number);
                case CATALOG:
                    return cache.get_preferred_origin().getCatalog();
                case CONTRIBUTOR:
                    return cache.get_preferred_origin().getContributor();
                case LATITUDE:
                    return new Float(cache.get_preferred_origin().getLocation().latitude);
                case LONGITUDE:
                    return new Float(cache.get_preferred_origin().getLocation().longitude);
                case DEPTH:
                    QuantityImpl q = (QuantityImpl)cache.get_preferred_origin().getLocation().depth;
                    q = q.convertTo(UnitImpl.KILOMETER);
                    return depthFormat.format(q.getValue()) + " km";
                case ORIGINTIME:
                    edu.iris.Fissures.Time fisDate = cache.get_preferred_origin().getOriginTime();
                    MicroSecondDate msd = new MicroSecondDate(fisDate);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return sdf.format(msd);
                // return msd.toString();
                case MAGVALUE:
                    if(cache.get_preferred_origin().getMagnitudes().length == 0) {
                        return "none";
                    }
                    String type = cache.get_preferred_origin().getMagnitudes()[0].type;
                    if(type.equals(edu.iris.Fissures.MB_MAG_TYPE.value)) {
                        type = "mb";
                    }
                    if(type.equals(edu.iris.Fissures.ML_MAG_TYPE.value)) {
                        type = "ml";
                    }
                    if(type.equals(edu.iris.Fissures.MBMLE_MAG_TYPE.value)) {
                        type = "mbmle";
                    }
                    if(type.equals(edu.iris.Fissures.MO_MAG_TYPE.value)) {
                        type = "MO";
                    }
                    if(type.equals(edu.iris.Fissures.MS_MAG_TYPE.value)) {
                        type = "Ms";
                    }
                    if(type.equals(edu.iris.Fissures.MSMLE_MAG_TYPE.value)) {
                        type = "msmle";
                    }
                    if(type.equals(edu.iris.Fissures.MW_MAG_TYPE.value)) {
                        type = "MW";
                    }
                    return new Float(cache.get_preferred_origin().getMagnitudes()[0].value).toString()
                            + " " + type;
                default:
                    return "XXXX";
            }
        } catch(NoPreferredOrigin e) {
            return "No Pref Origin";
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Got exception in Table model: getValueAt("
                                                  + row + ", " + col + ")",
                                          e);
            return "error";
        } // end of catch
    }

    public EventAccessOperations getEventForRow(int row) {
        synchronized(events) {
            if(isRowCached(row)) {
                return (EventAccessOperations)cachedEvents.get(events[row]);
            } else {
                ProxyEventAccessOperations cache = (ProxyEventAccessOperations)backgrounded.get(events[row]);
                if(cache == null) {
                    cache = (ProxyEventAccessOperations)cachedEvents.get(events[row]);
                }
                return cache;
            }
        }
    }

    public boolean isRowCached(int row) {
        synchronized(events) {
            if(backgrounded.containsKey(events[row])) {
                return false;
            }
            if(!cachedEvents.containsKey(events[row])) {
                // load in background
                ProxyEventAccessOperations cache = BulletproofVestFactory.vestEventAccess(events[row]);
                backgrounded.put(events[row], cache);
                rowNumber.put(events[row], new Integer(row));
                EventLoader backLoader = new EventLoader(((CacheEvent)cache.getWrappedEventAccess(CacheEvent.class)),
                                                         this);
                WorkerThreadPool.getDefaultPool().invokeLater(backLoader);
                return false;
            } else {
                return true;
            }
        }
    }

    public void updateEvents(EventAccessOperations[] updateEvents) {
        synchronized(events) {
            cachedEvents.clear();
            rowNumber.clear();
            this.events = updateEvents;
        }
        fireEventDataCleared();
        fireTableDataChanged();
        fireEventDataChanged(events);
    }

    public void appendEvents(EventAccessOperations[] appendEvents) {
        synchronized(events) {
            ProxyEventAccessOperations[] tmp = new ProxyEventAccessOperations[events.length
                    + appendEvents.length];
            System.arraycopy(events, 0, tmp, 0, events.length);
            System.arraycopy(appendEvents,
                             0,
                             tmp,
                             events.length,
                             appendEvents.length);
            this.events = tmp;
        }
        fireTableDataChanged();
        fireEventDataAppended(events);
    }

    public void eventLoaded(ProxyEventAccessOperations cache) {
        cachedEvents.put(cache, cache);
        backgrounded.remove(cache);
        Integer rowNum = (Integer)rowNumber.get(cache);
        if(rowNum != null) {
            fireTableRowsUpdated(rowNum.intValue(), rowNum.intValue());
        }
    }

    public void addEventDataListener(EventDataListener edl) {
        listenerList.add(EventDataListener.class, edl);
    }
    public void removeEventDataListener(EventDataListener edl) {
        listenerList.remove(EventDataListener.class, edl);
    }

    private void fireEventDataChanged(EventAccessOperations[] events) {
        EQDataEvent eqDataEvent = null;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == EventDataListener.class) {
                // Lazily create the event:
                if(eqDataEvent == null)
                    eqDataEvent = new EQDataEvent(events);
                ((EventDataListener)listeners[i + 1]).eventDataChanged(eqDataEvent);
            }
        }
    }

    private void fireEventDataAppended(EventAccessOperations[] events) {
        EQDataEvent eqDataEvent = null;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == EventDataListener.class) {
                // Lazily create the event:
                if(eqDataEvent == null)
                    eqDataEvent = new EQDataEvent(events);
                ((EventDataListener)listeners[i + 1]).eventDataAppended(eqDataEvent);
            }
        }
    }

    private void fireEventDataCleared() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == EventDataListener.class) {
                // Lazily create the event:
                ((EventDataListener)listeners[i + 1]).eventDataCleared();
            }
        }
    }

    public EventAccessOperations[] getAllEvents() {
        synchronized(events) {
            return events;
        }
    }

    public int getRowForEvent(EventAccessOperations event) {
        synchronized(events) {
            EventAccessOperations[] copyEvents = getAllEvents();
            for(int i = 0; i < copyEvents.length; i++) {
                if(event.equals(copyEvents[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected static ParseRegions FERegions = ParseRegions.getInstance();

    protected EventAccessOperations[] events = new EventAccessOperations[0];

    protected WeakHashMap cachedEvents = new WeakHashMap();

    protected WeakHashMap backgrounded = new WeakHashMap();

    protected HashMap rowNumber = new HashMap();

    protected NumberFormat depthFormat = new DecimalFormat("0.0");

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

    private static Logger logger = LoggerFactory.getLogger(EventTableModel.class.getName());

} // EventTableModel
