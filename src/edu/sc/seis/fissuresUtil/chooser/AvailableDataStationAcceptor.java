/**
 * AvailableDataStationAcceptor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;



public class AvailableDataStationAcceptor implements StationAcceptor {

    public AvailableDataStationAcceptor(DataCenterOperations dc,
                                        TimeRange range) {
        this.dc = dc;
        this.range = range;
    }
    public boolean accept(Station station) {
        RequestFilter[] request = createFakeRequest(station.get_id());
        if (dc.available_data(request).length != 0) {
            return true;
        } else {
            return false;
        }
    }

    /** Creates a request filter with several common channel ids to try
     *  and check for a station existing in a data center.
     */
    public RequestFilter[] createFakeRequest(StationId stationId) {

        RequestFilter[] request = new RequestFilter[4];
        request[0] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "00",
                                                     "BHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        request[1] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "00",
                                                     "BHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        request[2] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "  ",
                                                     "LHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        request[3] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "  ",
                                                     "LHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        return request;
    }

    protected DataCenterOperations dc;

    protected TimeRange range;

}

