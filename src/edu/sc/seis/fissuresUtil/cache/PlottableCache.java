package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;
import java.util.*;
import java.lang.ref.*;

/**
 * PlottableCache.java
 *
 *
 * Created: Thu May  9 09:45:01 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PlottableCache implements PlottableDCOperations{
    public PlottableCache (PlottableDC plottableDC){
	this.plottableDC = plottableDC;
    }
    //
    // IDL:iris.edu/Fissures/IfPlottable/PlottableDC/custom_sizes:1.0
    //
    /** Whether or not the PlottableDC can create custom dimensioned
     *Plottables. Clients should be aware that even if the server is
     *capable of custom dimensions, it may be faster to us the cached
     *dimensions for events and whole day Plottables. */

    public boolean custom_sizes() {
	return true;
    }

    //
    // IDL:iris.edu/Fissures/IfPlottable/PlottableDC/get_plottable:1.0
    //
    /** Gets a Plottable for a specific time window for a channel at the
     *given size. Because of the extra overhead of handling custom 
     *time ranges, this functionality is optional. */
    public edu.iris.Fissures.Plottable[] get_plottable(
						edu.iris.Fissures.IfSeismogramDC.RequestFilter request,
						edu.iris.Fissures.Dimension pixel_size)
	throws PlottableNotAvailable,
	UnsupportedDimension,
	edu.iris.Fissures.NotImplemented
    {

	return null;
    }
    //
    // IDL:iris.edu/Fissures/IfPlottable/PlottableDC/get_whole_day_sizes:1.0
    //
    /** Gets the sizes of cached plottables for a whole day of data.
     *A PlottableDC may be able
     *to generate Plottables of arbitrary dimensions, but for
     *performance reasons may only cache certain dimensions. A client
     *should use cached dimensions if possible. */

    public edu.iris.Fissures.Dimension[] get_whole_day_sizes() {

	return null;
    }
    //
    // IDL:iris.edu/Fissures/IfPlottable/PlottableDC/get_for_day:1.0
    //
    /** Gets a Plottable for an entire day, for example for a helicorder
     *display. For faster response, the client should use one of the
     *cached dimensions. */

    public edu.iris.Fissures.Plottable[]
	get_for_day(edu.iris.Fissures.IfNetwork.ChannelId channel_id,
		    int year,
		    int jday,
		    edu.iris.Fissures.Dimension pixel_size)
        throws PlottableNotAvailable,
	UnsupportedDimension {
	SoftReference ref;
	Plottable[] plottableArray;
	String key = ChannelIdUtil.toString(channel_id)+"."+Integer.toString(year)+"."+Integer.toString(jday);
	ref = (SoftReference)dayCache.get(key);
	if(ref != null) {
	    plottableArray = (Plottable[])ref.get();
	    if(plottableArray != null) {
		return plottableArray;
	    } else {
		dayCache.remove(ChannelIdUtil.toString(channel_id));
	    }
	}
	plottableArray = plottableDC.get_for_day(channel_id, year, jday, pixel_size);
	
	dayCache.put(key, new SoftReference(plottableArray));
	return plottableArray;
    }

    //
    // IDL:iris.edu/Fissures/IfPlottable/PlottableDC/get_event_sizes:1.0
    //
    /** Gets the sizes of cached plottables for an event's data.
     *A PlottableDC may be able
     *to generate Plottables of arbitrary dimensions, but for
     *performance reasons may only cache certain dimensions. A client
     *should use cached dimensions if possible. */

    public edu.iris.Fissures.Dimension[]
	get_event_sizes() {
	return null;
    }

    //
    // IDL:iris.edu/Fissures/IfPlottable/PlottableDC/get_for_event:1.0
    //
    /** Gets a Plottable for a particular seismic event.
     *For faster response, the client should use one of the
     *cached dimensions. */

    public edu.iris.Fissures.Plottable[]
	get_for_event(edu.iris.Fissures.IfEvent.EventAccess event,
		      edu.iris.Fissures.IfNetwork.ChannelId channel_id,
		      edu.iris.Fissures.Dimension pixel_size)
        throws PlottableNotAvailable,
	UnsupportedDimension {
	return null;
    }

    private HashMap dayCache = new HashMap();
    private PlottableDC plottableDC;
    
}// PlottableCache
