
package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.cache.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.apache.log4j.*;

/**
 * EventTableModel.java
 *
 *
 * Created: Mon Jan  8 15:59:05 2001
 *
 * @author Philip Crotwell
 * @version $Id: EventTableModel.java 1788 2002-06-04 20:23:16Z crotwell $
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
        columnNames = new String[10];
        columnNames[LATITUDE] = "Latitude";
        columnNames[LONGITUDE] = "Longitude";
	columnNames[DEPTH] = "Depth";
        columnNames[ORIGINTIME] = "Origin Time";
	//columnNames[MAGTYPE] = "MagType";
        columnNames[MAGVALUE] = "Magnitude";
	columnNames[CATALOG] = "Catalog";
	columnNames[CONTRIBUTOR] = "Contributor";
	//columnNames[NAME] = "Name";
        columnNames[FEREGION] = "Region";
	///setColumnSizes();

    }

   
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
		if (cache.get_attributes() == null) {
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
		//return type;
		//case MAGVALUE:
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
	    CacheEvent cache = new CacheEvent(events[row]);
	    backgrounded.put(events[row], cache);
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
	System.out.println("TableModel Got new events "+events.length);
	fireTableDataChanged();
    }

    public void eventLoaded(CacheEvent cache) {
	cachedEvents.put(cache.getEventAccess(), cache);
	backgrounded.remove(cache.getEventAccess());
	Integer rowNum = (Integer)rowNumber.get(cache.getEventAccess());
	fireTableRowsUpdated(rowNum.intValue(), rowNum.intValue());
	System.out.println("loaded event "+cache.get_attributes().name);
    }

    protected ParseRegions FERegions = new ParseRegions();

    protected EventAccessOperations[] events;

    protected WeakHashMap cachedEvents = new WeakHashMap();

    protected WeakHashMap backgrounded = new WeakHashMap();

    protected HashMap rowNumber = new HashMap();

    protected NumberFormat depthFormat = new DecimalFormat("0.0");

    protected EventBackgroundLoaderPool loader;

    protected static final int LATITUDE = 2;
    protected static final int LONGITUDE = 3;
    protected static final int DEPTH = 4;
    protected static final int ORIGINTIME = 1;
    protected static final int MAGTYPE = 9;
    protected static final int MAGVALUE = 5;
    protected static final int CATALOG = 6;
    protected static final int CONTRIBUTOR = 7;
    protected static final int NAME = 8;
    protected static final int FEREGION = 0;


    protected static final int LATITUDESIZE = 50;
    protected static final int LONGITUDESIZE = 50;
    protected static final int DEPTHSIZE = 50;
    protected static final int ORIGINTIMESIZE = 150;
    protected static final int MAGTYPESIZE = 9;
    protected static final int MAGVALUESIZE = 100;
    protected static final int CATALOGSIZE = 100;
    protected static final int CONTRIBUTORSIZE = 100;
    protected static final int NAMESIZE = 8;
    protected static final int FEREGIONSIZE = 150;



    protected String[] columnNames;

    static Category logger = Category.getInstance(EventTableModel.class.getName());

} // EventTableModel
