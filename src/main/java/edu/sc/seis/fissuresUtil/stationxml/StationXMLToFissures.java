package edu.sc.seis.fissuresUtil.stationxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.CoefficientErrored;
import edu.iris.Fissures.IfNetwork.CoefficientFilter;
import edu.iris.Fissures.IfNetwork.ComplexNumberErrored;
import edu.iris.Fissures.IfNetwork.Decimation;
import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.Gain;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Normalization;
import edu.iris.Fissures.IfNetwork.PoleZeroFilter;
import edu.iris.Fissures.IfNetwork.RecordingStyle;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.ClockImpl;
import edu.iris.Fissures.network.DataAcqSysImpl;
import edu.iris.Fissures.network.InstrumentationImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SensorImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.seisFile.stationxml.AbstractResponseType;
import edu.sc.seis.seisFile.stationxml.Channel;
import edu.sc.seis.seisFile.stationxml.Coefficients;
import edu.sc.seis.seisFile.stationxml.Epoch;
import edu.sc.seis.seisFile.stationxml.FIR;
import edu.sc.seis.seisFile.stationxml.InstrumentSensitivity;
import edu.sc.seis.seisFile.stationxml.Network;
import edu.sc.seis.seisFile.stationxml.Pole;
import edu.sc.seis.seisFile.stationxml.PoleZero;
import edu.sc.seis.seisFile.stationxml.PolesZeros;
import edu.sc.seis.seisFile.stationxml.ResponseList;
import edu.sc.seis.seisFile.stationxml.Station;
import edu.sc.seis.seisFile.stationxml.StationEpoch;
import edu.sc.seis.seisFile.stationxml.StationXMLException;
import edu.sc.seis.seisFile.stationxml.Zero;


public class StationXMLToFissures {
    
    public static NetworkAttrImpl convert(Network net) {
        return new NetworkAttrImpl(new NetworkId(net.getNetCode(), convertTime(net.getStartDate())),
                                   "",
                                   net.getDescription(),
                                   UNKNOWN,
                                   new TimeRange(convertTime(net.getStartDate()), convertTime(net.getEndDate())));
    }
    
    public static StationImpl convert(StationEpoch xml, NetworkAttrImpl netAttr, String staCode) {
        TimeRange effectiveTime = new TimeRange(new Time(xml.getStartDate(), -1), 
                                                new Time(xml.getEndDate(), -1));
        return new StationImpl(new StationId(netAttr.getId(), staCode, effectiveTime.start_time),
                                          xml.getSite().getCountry(),
                                          new Location(xml.getLat(), xml.getLon(),
                                                       new QuantityImpl(xml.getElevation(), UnitImpl.METER),
                                                       new QuantityImpl(0, UnitImpl.METER),
                                                       LocationType.GEOGRAPHIC),
                                          effectiveTime,
                                          UNKNOWN, UNKNOWN, UNKNOWN,
                                          netAttr);
    }
    
    public static List<StationImpl> convert(Station xml) throws StationXMLException {
        StationEpoch firstEpoch = xml.getStationEpochs().get(0);
        MicroSecondDate staBegin = new MicroSecondDate(firstEpoch.getStartDate());
        NetworkAttrImpl attr = new NetworkAttrImpl(new NetworkId(xml.getNetCode(), staBegin.getFissuresTime()), UNKNOWN, UNKNOWN, UNKNOWN);
        List<NetworkAttrImpl> nets = new ArrayList<NetworkAttrImpl>();
        nets.add(attr);
        List<StationChannelBundle> bundles = convert(xml, nets, false);
        List<StationImpl> out = new ArrayList<StationImpl>();
        for (StationChannelBundle b : bundles) {
            out.add(b.getStation());
        }
        return out;
    }
    
    public static List<StationChannelBundle> convert(Station xml, List<NetworkAttrImpl> knownNets, boolean extractChannels) throws StationXMLException {
        NetworkAttrImpl attr = null;
        for (NetworkAttrImpl net : knownNets) {
            if (xml.getNetCode().equals(net.get_code()))  {
                if (! NetworkIdUtil.isTemporary(net.get_id())) {
                    // found it
                    attr = net;
                    break;
                }  else {
                    StationEpoch firstEpoch = xml.getStationEpochs().get(0);
                    MicroSecondDate staBegin = new MicroSecondDate(firstEpoch.getStartDate());
                    MicroSecondDate netBegin = new MicroSecondDate(net.getBeginTime());
                    if (staBegin.after(netBegin) && staBegin.before(netBegin.add(new TimeInterval(2, UnitImpl.GREGORIAN_YEAR)))) {
                        // close enough
                        attr = net;
                        break;
                    }
                }
            }
        }
        if (attr == null) {
            // didn't find it
            throw new StationXMLException("Can't find network for "+xml.getNetCode());
        }
        return convert(xml, attr, extractChannels);
    }
    
    public static List<StationChannelBundle> convert(Station xml, Network net, boolean extractChannels) throws StationXMLException {
        return convert(xml, convert(net), extractChannels);
    }
    
    public static List<StationChannelBundle> convert(Station xml, NetworkAttrImpl attr, boolean extractChannels) throws StationXMLException {
        List<StationChannelBundle> out = new ArrayList<StationChannelBundle>();
        Iterator<StationEpoch> it = xml.getStationEpochs().iterator();
        while (it.hasNext()) {
            StationEpoch staEpoch = it.next();
            StationImpl sta = convert(staEpoch, attr, xml.getStaCode());
            StationChannelBundle bundle = new StationChannelBundle(sta);
            out.add(bundle);
            if (extractChannels) {
                for (Channel xmlChan : staEpoch.getChannelList()) {
                    List<ChannelSensitivityBundle> chans = convert(xmlChan, sta);
                    bundle.getChanList().addAll(chans);
                    if ( ! chans.get(0).getChan().getStationImpl().getNetworkAttrImpl().get_code().equals(xml.getNetCode()) ||
                            !  chans.get(0).getChan().getStationImpl().get_code().equals(xml.getStaCode())) {
                        throw new StationXMLException("Chan doesn't match station or net: "+ChannelIdUtil.toStringNoDates(chans.get(0).getChan())+"  "+xml.getNetCode()+"."+xml.getStaCode()+"  attr:"+NetworkIdUtil.toStringNoDates(attr));
                    }
                }
            }
        }
        return out;
    }
    
    public static ChannelSensitivityBundle convertChannel(Epoch chanEpoch, Channel parentChannel, StationImpl station) {
        SamplingImpl samp;
        if (Math.abs(Math.round(chanEpoch.getSampleRate()) - chanEpoch.getSampleRate()) < 0.0001f) {
            // looks like an int samples per second
            samp = new SamplingImpl(Math.round(chanEpoch.getSampleRate()), ONE_SECOND);
        } else {
            samp = new SamplingImpl(1, new TimeInterval(1/chanEpoch.getSampleRate(), UnitImpl.SECOND));
        }
        TimeRange chanTimeRange = new TimeRange(convertTime(chanEpoch.getStartDate()),
                                                convertTime(chanEpoch.getEndDate()));
        ChannelImpl chan = new ChannelImpl(new ChannelId(station.getId().network_id,
                                                         station.get_code(),
                                                         parentChannel.getLocCode(),
                                                         parentChannel.getChanCode(),
                                                         convertTime(chanEpoch.getStartDate())),
                                            UNKNOWN,
                                            new Orientation(chanEpoch.getAzimuth(), chanEpoch.getDip()),
                                            samp,
                                            chanTimeRange,
                                            new SiteImpl(new SiteId(station.getId().network_id,
                                                                    station.get_code(),
                                                                    parentChannel.getLocCode(),
                                                                    convertTime(chanEpoch.getEndDate())),
                                                         new Location(chanEpoch.getLat(),
                                                                      chanEpoch.getLon(),
                                                                      new QuantityImpl(chanEpoch.getElevation(), UnitImpl.METER),
                                                                      new QuantityImpl(chanEpoch.getDepth(), UnitImpl.METER),
                                                                      LocationType.GEOGRAPHIC),
                                                         chanTimeRange,
                                                         station, UNKNOWN));
        QuantityImpl sensitivity = null;
        if ( chanEpoch.getInstrumentSensitivity() != null &&
                chanEpoch.getInstrumentSensitivity().getSensitivityUnits().trim().length() != 0) {
            try {
            sensitivity = new QuantityImpl(chanEpoch.getInstrumentSensitivity().getSensitivityValue(),
                                                        convertUnit(chanEpoch.getInstrumentSensitivity().getSensitivityUnits()) );
            } catch (StationXMLException e) {
                logger.warn("Unable to extract unit: "+ChannelIdUtil.toStringFormatDates(chan.getId()), e);
            }
        } else {
            logger.warn("No sensitivity for "+ChannelIdUtil.toStringFormatDates(chan.getId()));
        }
        return new ChannelSensitivityBundle(chan, sensitivity);
    }
    
    public static List<ChannelSensitivityBundle> convert(Channel xml, StationImpl station) throws StationXMLException {
        List<ChannelSensitivityBundle> out = new ArrayList<ChannelSensitivityBundle>();
        for (Epoch chanEpoch : xml.getChanEpochList()) {
            out.add(convertChannel(chanEpoch, xml, station));
        }
        return out;
    }
    

    public static InstrumentationImpl convertInstrumentation(Epoch xmlChan) throws StationXMLException {

        ClockImpl clock = new ClockImpl(0,
                                        "unknown",
                                        "unknown",
                                        "unknown",
                                        "unknown");
        SensorImpl sensor = new SensorImpl(0,
                                           "unknown",
                                           "unknown",
                                           "unknown",
                                           0, 0);
        if (xmlChan.getSensor() != null) {
            sensor = new SensorImpl(0,
                                    xmlChan.getSensor().getManufacturer(),
                                    xmlChan.getSensor().getSerialNumber(),
                                    xmlChan.getSensor().getModel(),
                                    0, 0);
        }
        DataAcqSysImpl dataLogger = new DataAcqSysImpl(0,
                                                       "unknown",
                                                       "unknown",
                                                       "unknown",
                                                       RecordingStyle.UNKNOWN);
        if (xmlChan.getDataLogger() != null) {
            dataLogger = new DataAcqSysImpl(0,
                                            xmlChan.getDataLogger().getManufacturer(),
                                            xmlChan.getDataLogger().getSerialNumber(),
                                            xmlChan.getDataLogger().getModel(),
                                            RecordingStyle.UNKNOWN);
        }
        TimeRange chanTimeRange = new TimeRange(convertTime(xmlChan.getStartDate()),
                                                convertTime(xmlChan.getEndDate()));
        InstrumentationImpl out = new InstrumentationImpl(convert(xmlChan.getResponseList(), xmlChan.getInstrumentSensitivity()),
                                                          chanTimeRange,
                                                          clock,
                                                          sensor,
                                                          dataLogger);
        return out;
    }
    
    public static Response convert(List<edu.sc.seis.seisFile.stationxml.Response> respList, InstrumentSensitivity overallGain) throws StationXMLException {
        Sensitivity sense = null;
        if (overallGain != null) {
        sense = new Sensitivity(overallGain.getSensitivityValue(), overallGain.getFrequency());
        } else {
            for (edu.sc.seis.seisFile.stationxml.Response response : respList) {
                if (response.getStage() == 0) {
                    sense = new Sensitivity(response.getStageSensitivity().getSensitivityValue(), 
                                            response.getStageSensitivity().getFrequency());
                    break;
                }
            }
        }
        // assume stages are in order, but maybe should sort???
        List<Stage> stages = new ArrayList<Stage>();
        for (edu.sc.seis.seisFile.stationxml.Response response : respList) {
            if (response.getStage() != 0) {
                Decimation[] dec = new Decimation[0];
                if (response.getDecimation() != null) {
                    dec = new Decimation[] {convertDecimation(response.getDecimation())};
                }
                Normalization[] norm = new Normalization[0];
                Filter[] filt;
                UnitImpl inputUnits;
                UnitImpl outputUnits;
                if (response.getResponseItem() == null) {
                    // no responseItem, so units have not changed, find previous stage and reuse units
                    inputUnits = (UnitImpl)stages.get(stages.size()-1).output_units;
                    outputUnits = inputUnits;
                    if (response.getDecimation() != null) {
                        filt = new Filter[] {UNITY_COEFFICENT_FILTER};
                    } else {
                        // usually means an analog gain only stage
                        filt = new Filter[] {UNITY_POLE_ZERO};
                    }
                } else {
                    filt = new Filter[] {convertFilter(response.getResponseItem())};
                    inputUnits = convertUnit(response.getResponseItem().getInputUnits());
                    outputUnits = convertUnit(response.getResponseItem().getOutputUnits());
                    if (response.getResponseItem() instanceof PolesZeros) {
                        norm = new Normalization[] {convertNormalization((PolesZeros)response.getResponseItem())};
                    }
                }
                stages.add(new Stage(getTransferType(response),
                                     inputUnits,
                                     outputUnits,
                                     norm,
                                     new Gain(response.getStageSensitivity().getSensitivityValue(),
                                              response.getStageSensitivity().getFrequency()),
                                     dec,
                                     filt));
            }
        }
        return new Response(sense,
                            stages.toArray(new Stage[0]));
    }
    
    public static Decimation convertDecimation(edu.sc.seis.seisFile.stationxml.Decimation dec) {
        return new Decimation(new SamplingImpl(1, new TimeInterval(1/dec.getInputSampleRate(), UnitImpl.SECOND)),
                              dec.getFactor(),
                              dec.getOffset(),
                              new TimeInterval(dec.getDelay(), UnitImpl.SECOND),
                              new TimeInterval(dec.getCorrection(), UnitImpl.SECOND));
    }
    
    public static Filter convertFilter(AbstractResponseType resp) throws StationXMLException {
        Filter out = new Filter();
        if (resp instanceof PolesZeros) {
            PolesZeros pz = (PolesZeros)resp;
            ComplexNumberErrored[] poles = new ComplexNumberErrored[pz.getPoleList().size()];
            int i=0;
            for (Pole p : pz.getPoleList()) {
                poles[i++] = convertComplex(p);
            }
            ComplexNumberErrored[] zeros = new ComplexNumberErrored[pz.getZeroList().size()];
            i = 0;
            for (Zero p : pz.getZeroList()) {
                zeros[i++] = convertComplex(p);
            }
            out.pole_zero_filter(new PoleZeroFilter(poles, zeros));
        } else if (resp instanceof Coefficients) {
            Coefficients coef = (Coefficients)resp;
            CoefficientErrored[] num = new CoefficientErrored[coef.getNumeratorList().size()];
            int i=0;
            for (Float p : coef.getNumeratorList()) {
                num[i++] = new CoefficientErrored(p, 0);
            }
            CoefficientErrored[] denom = new CoefficientErrored[coef.getDenominatorList().size()];
            i = 0;
            for (Float p : coef.getDenominatorList()) {
                denom[i++] = new CoefficientErrored(p, 0);
            }
            out.coeff_filter(new CoefficientFilter(num, denom)); 
        } else if (resp instanceof ResponseList) {
            throw new StationXMLException("Can only handle PolesZeros or FIR response types. "+resp.getClass());   
        } else {
            throw new StationXMLException("Can only handle PolesZeros or FIR response types. "+resp.getClass());   
        }
        return out;
    }
    
    public static Normalization convertNormalization(PolesZeros poleZero) {
        return new Normalization(poleZero.getNormalizationFactor(), poleZero.getNormalizationFreq());
    }
    
    public static ComplexNumberErrored convertComplex(PoleZero pz) {
        return new ComplexNumberErrored((float)pz.getReal(), 0, (float)pz.getImaginary(), 0);
    }
    
    public static TransferType getTransferType(edu.sc.seis.seisFile.stationxml.Response response) throws StationXMLException {
        if (response.getResponseItem() instanceof PolesZeros) {
            return TransferType.ANALOG;
        } else if (response.getResponseItem() instanceof Coefficients) {
                return TransferType.ANALOG;
        } else if (response.getResponseItem() instanceof FIR) {
            return TransferType.DIGITAL;
        } else if (response.getResponseItem() == null) {
            if (response.getDecimation() != null) {
                return TransferType.DIGITAL;
            } else {
                // usually means an analog gain only stage
                return TransferType.ANALOG;
            }
        } else {
            throw new StationXMLException("Can only handle PolesZeros, Coefficients or FIR response types. "+response.getResponseItem().getClass());   
        }
    }
    
    public static UnitImpl convertUnit(String xml) throws StationXMLException {
        String unitString;
        if (xml.indexOf(" - ") != -1) {
            unitString = xml.substring(0, xml.indexOf(" - ")).trim();
        } else {
            unitString = xml.trim(); // probably won't work, but might as well try
        }
        if (unitString.length() == 0) {
            // no unit, probalby means unknown response
            
        }
        if (unitString.equalsIgnoreCase("M")) {
            return UnitImpl.METER;
        } else if (unitString.equalsIgnoreCase("M/S")) {
            return UnitImpl.METER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("NM/S")) {
            return UnitImpl.NANOMETER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("M/S/S") || unitString.equalsIgnoreCase("M/S**2")) {
            return UnitImpl.METER_PER_SECOND_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("V")) {
            return UnitImpl.VOLT;
        } else if (unitString.equalsIgnoreCase("COUNTS")) {
            return UnitImpl.COUNT;
        } else {
            try {
                return UnitImpl.getUnitFromString(unitString);
            } catch (NoSuchFieldException e) {
                throw new StationXMLException("Unknown unit: '"+xml+"'");
            }
        }
    }
    
    public static Time convertTime(String xml) {
        return new MicroSecondDate(xml).getFissuresTime();
    }

    public static final Filter UNITY_POLE_ZERO = new Filter();
    public static final Filter UNITY_COEFFICENT_FILTER = new Filter();
    
    static {
        UNITY_POLE_ZERO.pole_zero_filter(new PoleZeroFilter(new ComplexNumberErrored[] { new ComplexNumberErrored(0,0,0,0)},
                                                                            new ComplexNumberErrored[] { new ComplexNumberErrored(0,0,0,0)}));
        UNITY_COEFFICENT_FILTER.coeff_filter(new CoefficientFilter(new CoefficientErrored[] {new CoefficientErrored(1,0)},
                                                                   new CoefficientErrored[0]));
    }
    
    public static final String UNKNOWN = "";
    
    public static final TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationXMLToFissures.class);
}
