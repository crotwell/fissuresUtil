package edu.sc.seis.fissuresUtil.namingService;

import edu.iris.Fissures.Dimension;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfPlottable.PlottableDCOperations;
import edu.iris.Fissures.IfPlottable.PlottableDCPOA;
import edu.iris.Fissures.IfPlottable.PlottableNotAvailable;
import edu.iris.Fissures.IfPlottable.UnsupportedDimension;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import junit.framework.TestCase;


/**
 * @author crotwell
 * Created on Jan 7, 2005
 */
public class FissuresNamingServiceTest extends TestCase {

    public void testGetInterfaceName() {
        String out = FissuresNamingService.getInterfaceNameForClass(PlottableDCPOA.class);
        assertEquals("PlottableDC", out);
        PlottableDCOperations dummy = new PlottableDCOperations() {

            public boolean custom_sizes() {
                // TODO Auto-generated method stub
                return false;
            }

            public Plottable[] get_plottable(RequestFilter request, Dimension pixel_size) throws PlottableNotAvailable, UnsupportedDimension, NotImplemented {
                // TODO Auto-generated method stub
                return null;
            }

            public Dimension[] get_whole_day_sizes() {
                // TODO Auto-generated method stub
                return null;
            }

            public Plottable[] get_for_day(ChannelId channel_id, int year, int jday, Dimension pixel_size) throws PlottableNotAvailable, UnsupportedDimension {
                // TODO Auto-generated method stub
                return null;
            }

            public Dimension[] get_event_sizes() {
                // TODO Auto-generated method stub
                return null;
            }

            public Plottable[] get_for_event(EventAccess event, ChannelId channel_id, Dimension pixel_size) throws PlottableNotAvailable, UnsupportedDimension {
                // TODO Auto-generated method stub
                return null;
            }
            
        };
        out = FissuresNamingService.getInterfaceNameForClass(dummy.getClass());
        assertEquals("PlottableDC", out);
    }
    

     
}
