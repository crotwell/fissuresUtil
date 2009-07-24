package edu.sc.seis.fissuresUtil.cache;

import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import org.apache.log4j.Category;
import edu.iris.Fissures.Dimension;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.IfPlottable.PlottableDCOperations;
import edu.iris.Fissures.IfPlottable.PlottableNotAvailable;
import edu.iris.Fissures.IfPlottable.UnsupportedDimension;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;

/**
 * PlottableCache.java
 *
 *
 * Created: Thu May  9 09:45:01 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class CachePlottableDC implements ProxyPlottableDC {

    public CachePlottableDC (PlottableDCOperations plottableDC){
        this.plottable = plottableDC;
    }

    public boolean custom_sizes() {
        return plottable.custom_sizes();
    }

    public edu.iris.Fissures.Plottable[] get_plottable(RequestFilter request,
                                                       edu.iris.Fissures.Dimension pixel_size)
        throws PlottableNotAvailable,
        UnsupportedDimension,
        edu.iris.Fissures.NotImplemented {
        return plottable.get_plottable(request, pixel_size);
    }

    public edu.iris.Fissures.Dimension[] get_whole_day_sizes() {

        return plottable.get_whole_day_sizes();
    }

    public Plottable[] get_for_day(ChannelId channel_id, int year, int jDay,
                                   Dimension pixel_size)
        throws PlottableNotAvailable, UnsupportedDimension {

        //If getting plottable for today, don't use cache
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(new Date());
        int todayJDay = calendar.get(Calendar.DAY_OF_YEAR);
        int todayYear = calendar.get(Calendar.YEAR);
        if(jDay == todayJDay && todayYear == year){
            return plottable.get_for_day(channel_id, year, jDay, pixel_size);
        }

        //not for today, so attempt to get plottable from cache
        SoftReference ref;
        Plottable[] plottableArray;
        String key = ChannelIdUtil.toString(channel_id)+"."+Integer.toString(year)+"."+Integer.toString(jDay);
        ref = (SoftReference)dayCache.get(key);
        if(ref != null) {
            plottableArray = (Plottable[])ref.get();
            if(plottableArray != null) {
                return plottableArray;
            } else {
                dayCache.remove(key);
            }
        }
        //plottable is not in cache, fetch and store
        plottableArray = plottable.get_for_day(channel_id, year, jDay, pixel_size);

        dayCache.put(key, new SoftReference(plottableArray));
        return plottableArray;
    }

    public edu.iris.Fissures.Dimension[]
        get_event_sizes() {
        return plottable.get_event_sizes();
    }


    public edu.iris.Fissures.Plottable[]
        get_for_event(EventAccess event,
                      ChannelId channel_id,
                      edu.iris.Fissures.Dimension pixel_size)
        throws PlottableNotAvailable,
        UnsupportedDimension {
        return plottable.get_for_event(event, channel_id, pixel_size);
    }


    public PlottableDCOperations getWrappedDC() {
        return plottable;
    }

    public PlottableDCOperations getWrappedDC(Class wrappedClass) {
        if(getWrappedDC().getClass().equals(wrappedClass)){
            return getWrappedDC();
        }else if(getWrappedDC().getClass().equals(ProxySeismogramDC.class)){
            ((ProxySeismogramDC)getWrappedDC()).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("This doesn't contain a DC of class " + wrappedClass);
    }

    public void reset() {
        dayCache.clear();
        if (plottable instanceof ProxyPlottableDC) {
            ((ProxyPlottableDC)plottable).reset();
        }
    }

    public PlottableDC getCorbaObject() {
        if (plottable instanceof PlottableDC) {
            return (PlottableDC)plottable;
        } else if (plottable instanceof ProxyPlottableDC) {
            return ((ProxyPlottableDC)plottable).getCorbaObject();
        } else {
            throw new RuntimeException("subplottable not a PlottableDC or ProxyPlottableDC");
        }
    }

    /**
     HashMap to maintins the cache of the dayPlottables
     **/
    private HashMap dayCache = new HashMap();

    private PlottableDCOperations plottable;

    private static Category logger =
        Category.getInstance(CachePlottableDC.class.getName());

}// PlottableCache
