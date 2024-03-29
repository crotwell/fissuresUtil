package edu.sc.seis.fissuresUtil.stationxml;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.UnitBase;
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
import edu.iris.Fissures.model.TimeUtils;
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
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.seisFile.fdsnws.stationxml.BaseFilterType;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Coefficients;
import edu.sc.seis.seisFile.fdsnws.stationxml.FIR;
import edu.sc.seis.seisFile.fdsnws.stationxml.FloatType;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Pole;
import edu.sc.seis.seisFile.fdsnws.stationxml.PoleZero;
import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseList;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLException;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;
import edu.sc.seis.seisFile.fdsnws.stationxml.Zero;

public class StationXMLToFissures {

    public static NetworkAttrImpl convert(Network net) {
        // make name be first 80 chars of description
        String name = net.getDescription();
        if (name.length() > 80) {
            name = name.substring(0, 80);
        }
        Time startDate = convertTime(net.getStartDate(), WAY_PAST);
        return new NetworkAttrImpl(new NetworkId(net.getCode(), startDate),
                                   name,
                                   net.getDescription(),
                                   UNKNOWN,
                                   new TimeRange(startDate, convertTime(net.getEndDate(), WAY_FUTURE)));
    }

    public static StationImpl convert(Station xml, NetworkAttrImpl netAttr) throws StationXMLException {
        Time startDate = convertTime(xml.getStartDate(), WAY_PAST);
        TimeRange effectiveTime = new TimeRange(startDate, convertTime(xml.getEndDate(), WAY_FUTURE));
        String name = xml.getSite().getName();
        if (name == null) {
            name = (xml.getSite().getTown() != null ? xml.getSite().getTown() + " " : "")
                    + (xml.getSite().getRegion() != null ? xml.getSite().getRegion() + " " : "")
                    + (xml.getSite().getCountry() != null ? xml.getSite().getCountry() : "");
            name = name.trim();
        }
        if ("".equals(name)) {
            name = xml.getSite().getDescription();
        }
        if (null == name || "".equals(name)) {
            name = "";
        }
        return new StationImpl(new StationId(netAttr.getId(), xml.getCode(), effectiveTime.start_time),
                               name,
                               new Location(xml.getLatitude().getValue(),
                                            xml.getLongitude().getValue(),
                                            convertFloatType(xml.getElevation()),
                                            new QuantityImpl(0, UnitImpl.METER),
                                            LocationType.GEOGRAPHIC), effectiveTime, UNKNOWN, UNKNOWN, UNKNOWN, netAttr);
    }

    public static List<StationChannelBundle> convert(Station xml,
                                                     List<NetworkAttrImpl> knownNets,
                                                     boolean extractChannels) throws StationXMLException {
        NetworkAttrImpl attr = null;
        for (NetworkAttrImpl net : knownNets) {
            if (xml.getNetworkCode().equals(net.get_code())) {
                if (!NetworkIdUtil.isTemporary(net.get_id())) {
                    // found it
                    attr = net;
                    break;
                } else {
                    MicroSecondDate staBegin = new MicroSecondDate(xml.getStartDate());
                    MicroSecondDate netBegin = new MicroSecondDate(net.getBeginTime());
                    if (staBegin.after(netBegin) && staBegin.before(new MicroSecondDate(net.getEndTime()))) {
                        // close enough
                        attr = net;
                        break;
                    }
                }
            }
        }
        if (attr == null) {
            // didn't find it
            throw new StationXMLException("Can't find network for " + xml.getNetworkCode());
        }
        return convert(xml, attr, extractChannels);
    }

    public static List<StationChannelBundle> convert(Station xml, Network net, boolean extractChannels)
            throws StationXMLException {
        return convert(xml, convert(net), extractChannels);
    }

    public static List<StationChannelBundle> convert(Station xml, NetworkAttrImpl attr, boolean extractChannels)
            throws StationXMLException {
        List<StationChannelBundle> out = new ArrayList<StationChannelBundle>();
        StationImpl sta = convert(xml, attr);
        StationChannelBundle bundle = new StationChannelBundle(sta);
        out.add(bundle);
        if (extractChannels) {
            for (Channel xmlChan : xml.getChannelList()) {
                ChannelSensitivityBundle chanSens = convert(xmlChan, sta);
                bundle.getChanList().add(chanSens);
                if (!chanSens
                        .getChan()
                        .getStationImpl()
                        .getNetworkAttrImpl()
                        .get_code()
                        .equals(xml.getNetworkCode())
                        || !chanSens.getChan().getStationImpl().get_code().equals(xml.getCode())) {
                    throw new StationXMLException("Chan doesn't match station or net: "
                            + ChannelIdUtil.toStringNoDates(chanSens.getChan()) + "  " + xml.getNetworkCode() + "."
                            + xml.getCode() + "  attr:" + NetworkIdUtil.toStringNoDates(attr));
                }
            }
        }
        return out;
    }

    public static ChannelSensitivityBundle convert(Channel channel, StationImpl station) throws StationXMLException {
        SamplingImpl samp;
        if (Math.abs(Math.round(channel.getSampleRate().getValue()) - channel.getSampleRate().getValue()) < 0.0001f) {
            // looks like an int samples per second
            samp = new SamplingImpl(Math.round(channel.getSampleRate().getValue()), ONE_SECOND);
        } else {
            samp = new SamplingImpl(1, new TimeInterval(1 / channel.getSampleRate().getValue(), UnitImpl.SECOND));
        }
        TimeRange chanTimeRange = new TimeRange(convertTime(channel.getStartDate(), WAY_PAST),
                                                convertTime(channel.getEndDate(), WAY_FUTURE));
        Orientation orientation;
        if (channel.getAzimuth() != null && channel.getDip() != null) {
            orientation = new Orientation(channel.getAzimuth().getValue(), channel.getDip().getValue());
        } else {
            orientation = new Orientation(0,0);
        }
        ChannelImpl chan = new ChannelImpl(new ChannelId(station.getId().network_id,
                                                         station.get_code(),
                                                         channel.getLocCode(),
                                                         channel.getCode(),
                                                         chanTimeRange.start_time),
                                           UNKNOWN,
                                           orientation,
                                           samp,
                                           chanTimeRange,
                                           new SiteImpl(new SiteId(station.getId().network_id,
                                                                   station.get_code(),
                                                                   channel.getLocCode(),
                                                                   chanTimeRange.end_time),
                                                        new Location(channel.getLatitude().getValue(),
                                                                     channel.getLon().getValue(),
                                                                     convertFloatType(channel.getElevation()),
                                                                     convertFloatType(channel.getDepth()),
                                                                     LocationType.GEOGRAPHIC),
                                                        chanTimeRange,
                                                        station,
                                                        UNKNOWN));
        QuantityImpl sensitivity = null;
        if (channel.getResponse().getInstrumentSensitivity() != null
                && channel.getResponse().getInstrumentSensitivity().getInputUnits() != null
            && channel.getResponse().getInstrumentSensitivity().getOutputUnits() != null) {
            try {
                sensitivity = new QuantityImpl(channel.getResponse().getInstrumentSensitivity().getSensitivityValue(),
                                               convertUnit(channel.getResponse().getInstrumentSensitivity().getInputUnits()));
            } catch(StationXMLException e) {
                logger.warn("Unable to extract unit: " + ChannelIdUtil.toStringFormatDates(chan.getId()), e);
            }
        } else {
            logger.info("No sensitivity for " + ChannelIdUtil.toStringFormatDates(chan.getId()));
        }
        return new ChannelSensitivityBundle(chan, sensitivity);
    }

    public static InstrumentationImpl convertInstrumentation(Channel xmlChan) throws StationXMLException {
        ClockImpl clock = new ClockImpl(0, "unknown", "unknown", "unknown", "unknown");
        SensorImpl sensor = new SensorImpl(0, "unknown", "unknown", "unknown", 0, 0);
        if (xmlChan.getSensor() != null) {
            String model = makeNoNull(xmlChan.getSensor().getModel());
            if (model.length()==0) {
                model = makeNoNull(xmlChan.getSensor().getType());
            }
            if (model.length()==0) {
                model = makeNoNull(xmlChan.getSensor().getDescription());
            }
            sensor = new SensorImpl(0,
                                    makeNoNull(xmlChan.getSensor().getManufacturer()),
                                    makeNoNull(xmlChan.getSensor().getSerialNumber()),
                                    model,
                                    0,
                                    0);
        }
        DataAcqSysImpl dataLogger = new DataAcqSysImpl(0, "unknown", "unknown", "unknown", RecordingStyle.UNKNOWN);
        if (xmlChan.getDataLogger() != null) {
            String model = makeNoNull(xmlChan.getDataLogger().getModel());
            if (model.length()==0) {
                model = makeNoNull(xmlChan.getDataLogger().getType());
            }
            dataLogger = new DataAcqSysImpl(0,
                                            makeNoNull(xmlChan.getDataLogger().getManufacturer()),
                                            makeNoNull(xmlChan.getDataLogger().getSerialNumber()),
                                            model,
                                            RecordingStyle.UNKNOWN);
        }
        TimeRange chanTimeRange = new TimeRange(convertTime(xmlChan.getStartDate(), WAY_PAST), convertTime(xmlChan.getEndDate(), WAY_FUTURE));
        if (xmlChan.getResponse().getResponseStageList().size() != 0) {
        InstrumentationImpl out = new InstrumentationImpl(convert(xmlChan.getResponse().getResponseStageList(),
                                                                  xmlChan.getResponse().getInstrumentSensitivity()),
                                                          chanTimeRange,
                                                          clock,
                                                          sensor,
                                                          dataLogger);
        return out;
        } else {
            throw new StationXMLException("Response not available");
        }
    }

    public static Response convert(List<ResponseStage> respList,
                                   InstrumentSensitivity overallGain) throws StationXMLException {
        Sensitivity sense = null;
        if (overallGain != null) {
            sense = new Sensitivity(overallGain.getSensitivityValue(), overallGain.getFrequency());
        } else {
            for (ResponseStage response : respList) {
                if (response.getNumber() == 0) {
                    sense = new Sensitivity(response.getStageSensitivity().getSensitivityValue(),
                                            response.getStageSensitivity().getFrequency());
                    break;
                }
            }
        }
        // assume stages are in order, but maybe should sort???
        List<Stage> stages = new ArrayList<Stage>();
        for (ResponseStage response : respList) {
            if (response.getNumber() != 0) {
                Decimation[] dec = new Decimation[0];
                if (response.getDecimation() != null) {
                    dec = new Decimation[] {convertDecimation(response.getDecimation())};
                }
                Normalization[] norm = new Normalization[0];
                Filter[] filt;
                UnitImpl inputUnits;
                UnitImpl outputUnits;
                if (response.getResponseItem() == null) {
                    // no responseItem, so units have not changed, find previous
                    // stage and reuse units
                    if (stages.size() == 0) {
                        // uh oh, no units on first stage
                        throw new StationXMLException("No units on stage 0, cannot convert response");
                    }
                    inputUnits = (UnitImpl)stages.get(stages.size() - 1).output_units;
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
                Gain g;
                // stage gain might be missing, we will assume a unity gain in this case.
                if (response.getStageSensitivity() != null) {
                    g = new Gain(response.getStageSensitivity().getSensitivityValue(),
                                 response.getStageSensitivity().getFrequency());
                } else {
                    g = new Gain(1, sense.frequency); // null, so assume gain 1 at overall sensitivity freq
                }
                stages.add(new Stage(getTransferType(response),
                                     inputUnits,
                                     outputUnits,
                                     norm,
                                     g, dec, filt));
            }
        }
        return new Response(sense, stages.toArray(new Stage[0]));
    }

    public static Decimation convertDecimation(edu.sc.seis.seisFile.fdsnws.stationxml.Decimation dec) {
        return new Decimation(new SamplingImpl(1, new TimeInterval(1 / dec.getInputSampleRate(), UnitImpl.SECOND)),
                              dec.getFactor(),
                              dec.getOffset(),
                              new TimeInterval(dec.getDelay().getValue(), UnitImpl.SECOND),
                              new TimeInterval(dec.getCorrection().getValue(), UnitImpl.SECOND));
    }

    public static Filter convertFilter(BaseFilterType filterType) throws StationXMLException {
        Filter out = new Filter();
        if (filterType instanceof PolesZeros) {
            PolesZeros pz = (PolesZeros)filterType;
            ComplexNumberErrored[] poles = new ComplexNumberErrored[pz.getPoleList().size()];
            int i = 0;
            for (Pole p : pz.getPoleList()) {
                poles[i++] = convertComplex(p);
            }
            ComplexNumberErrored[] zeros = new ComplexNumberErrored[pz.getZeroList().size()];
            i = 0;
            for (Zero p : pz.getZeroList()) {
                zeros[i++] = convertComplex(p);
            }
            out.pole_zero_filter(new PoleZeroFilter(poles, zeros));
        } else if (filterType instanceof Coefficients) {
            Coefficients coef = (Coefficients)filterType;
            CoefficientErrored[] num = new CoefficientErrored[coef.getNumeratorList().size()];
            int i = 0;
            for (FloatType p : coef.getNumeratorList()) {
                float error = 0;
                if (p.hasPlusError()) {error = Math.max(p.getPlusError(), error); }
                if (p.hasMinusError()) {error = Math.max(p.getMinusError(), error); }
                num[i++] = new CoefficientErrored(p.getValue(), error);
            }
            CoefficientErrored[] denom = new CoefficientErrored[coef.getDenominatorList().size()];
            i = 0;
            for (FloatType p : coef.getDenominatorList()) {
                float error = 0;
                if (p.hasPlusError()) {error = Math.max(p.getPlusError(), error); }
                if (p.hasMinusError()) {error = Math.max(p.getMinusError(), error); }
                denom[i++] = new CoefficientErrored(p.getValue(), error);
            }
            out.coeff_filter(new CoefficientFilter(num, denom));
        } else if (filterType instanceof FIR) {
            FIR fir = (FIR)filterType;
            CoefficientErrored[] num;
            int numListSize = fir.getNumeratorCoefficientList().size();
            if (fir.getSymmetry().equals(StationXMLTagNames.NONE)) {
                num = new CoefficientErrored[numListSize];
                int i = 0;
                for (Float p : fir.getNumeratorCoefficientList()) {
                    float error = 0;
                    num[i++] = new CoefficientErrored(p, error);
                }
            } else if (fir.getSymmetry().equals(StationXMLTagNames.ODD)) {
                num = new CoefficientErrored[ 2 * numListSize - 1];
                int i = 0;
                for (Float p : fir.getNumeratorCoefficientList()) {
                    float error = 0;
                    num[i] = new CoefficientErrored(p, error);
                    num[2*numListSize-1 - i - 1] = new CoefficientErrored(p, error);
                    i++;
                }
            } else if (fir.getSymmetry().equals(StationXMLTagNames.EVEN)) {
                num = new CoefficientErrored[ 2 * numListSize];
                int i = 0;
                for (Float p : fir.getNumeratorCoefficientList()) {
                    float error = 0;
                    num[i] = new CoefficientErrored(p, error);
                    num[2*numListSize - i -1] = new CoefficientErrored(p, error);
                    i++;
                }
            } else {
                throw new StationXMLException("Unknown FIR symmetry: "+fir.getSymmetry());
            }
            CoefficientErrored[] denom = new CoefficientErrored[0];
            out.coeff_filter(new CoefficientFilter(num, denom));
        } else if (filterType instanceof ResponseList) {
            throw new StationXMLException("Can only handle PolesZeros or FIR response types. " + filterType.getClass());
        } else {
            throw new StationXMLException("Can only handle PolesZeros or FIR response types. " + filterType.getClass());
        }
        return out;
    }

    public static Normalization convertNormalization(PolesZeros poleZero) {
        return new Normalization(poleZero.getNormalizationFactor(), poleZero.getNormalizationFreq());
    }

    public static ComplexNumberErrored convertComplex(PoleZero pz) {
        return new ComplexNumberErrored((float)pz.getReal(), 0, (float)pz.getImaginary(), 0);
    }

    public static TransferType getTransferType(ResponseStage response)
            throws StationXMLException {
        if (response.getResponseItem() instanceof PolesZeros) {
            PolesZeros pz = (PolesZeros)response.getResponseItem();
            if (pz.getPzTransferType().equals(StationXMLTagNames.POLEZERO_LAPLACE_RAD_PER_SEC)) {
                return TransferType.LAPLACE;
            } else if (pz.getPzTransferType().equals(StationXMLTagNames.POLEZERO_LAPLACE_HERTZ)) {
                return TransferType.ANALOG;
            } else if (pz.getPzTransferType().equals(StationXMLTagNames.POLEZERO_DIGITAL)) {
                return TransferType.DIGITAL;
            } else {
                throw new StationXMLException("Unknown PoleZero transfer type: "+
                        ((PolesZeros)response.getResponseItem()).getPzTransferType());
            }
        } else if (response.getResponseItem() instanceof Coefficients) {
            Coefficients coeff = (Coefficients)response.getResponseItem();
            if (coeff.getCfTransferType().equals(StationXMLTagNames.COEFFICIENT_ANALOG_RAD_PER_SEC)) {
                return TransferType.LAPLACE;
            } else if (coeff.getCfTransferType().equals(StationXMLTagNames.COEFFICIENT_ANALOG_HERTZ)) {
            return TransferType.ANALOG;
            } else if (coeff.getCfTransferType().equals(StationXMLTagNames.COEFFICIENT_DIGITAL)) {
                return TransferType.DIGITAL;
            } else {
                throw new StationXMLException("Unknown Coefficients transfer type: "+
                        coeff.getCfTransferType());
            }
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
            throw new StationXMLException("Can only handle PolesZeros, Coefficients or FIR response types. "
                    + response.getResponseItem().getClass());
        }
    }
    
    public static QuantityImpl convertFloatType(FloatType val) throws StationXMLException {
        return new QuantityImpl(val.getValue(), convertUnit(val.getUnit(), ""));
    }

    public static UnitImpl convertUnit(Unit unit) throws StationXMLException {
        String unitString;
        if (unit.getName().indexOf(" - ") != -1) {
            unitString = unit.getName().substring(0, unit.getName().indexOf(" - ")).trim();
        } else {
            unitString = unit.getName().trim(); // probably won't work, but might as well
                                     // try
        }
        if (unitString.length() == 0) {
            // no unit, probably means unknown response
            throw new StationXMLException("Unknown unit: "+unit.getName()+" "+unit.getDescription());
        }
        return convertUnit(unitString, unit.getDescription());
    }

    public static UnitImpl convertUnit(String unitString, String unitDescription) throws StationXMLException {
        if (unitDescription == null) {
            unitDescription = "";
        }
        unitString = unitString.trim();
        unitDescription = unitDescription.trim();
        if (unitString.equalsIgnoreCase("M") && ! unitDescription.trim().equalsIgnoreCase("minute")) {
            return UnitImpl.METER;
        } else if (unitString.equalsIgnoreCase("M") && unitDescription.trim().equalsIgnoreCase("minute")) {
            return UnitImpl.MINUTE;
        } else if (unitString.equalsIgnoreCase("NM")) {
            return UnitImpl.NANOMETER;
        } else if (unitString.equalsIgnoreCase("M/S")) {
            return UnitImpl.METER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("NM/S") || unitString.equalsIgnoreCase("NM/SEC")) {
            return UnitImpl.NANOMETER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("CM/SEC**2")) {
            return UnitImpl.CENTIMETER_PER_SECOND_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("M/S/S")
                || unitString.equalsIgnoreCase("M/S**2")
                || unitString.equalsIgnoreCase("M/(S**2)")
                || unitString.equalsIgnoreCase("M/S**2/ACCELERATION")) {
            return UnitImpl.METER_PER_SECOND_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("PA") || unitString.equalsIgnoreCase("PASSCAL") || unitString.equalsIgnoreCase("PASSCALS")) {
            return UnitImpl.PASCAL;
        } else if (unitString.equalsIgnoreCase("HPA")  || unitString.equalsIgnoreCase("HECTOPASCALS")) {
            return UnitImpl.HECTOPASCAL;
        } else if (unitString.equalsIgnoreCase("KPA") || unitString.equalsIgnoreCase("KILOPASCALS")) {
            return UnitImpl.KILOPASCAL;
        } else if (unitString.equalsIgnoreCase("H/M**2*S")) {
            return UnitImpl.multiply(UnitImpl.SQUARE_METER, UnitImpl.SECOND).inverse("hail intensity in hits per meter squared second");
        } else if (unitString.equalsIgnoreCase("PERCENT") || unitString.equalsIgnoreCase("P") || unitString.equalsIgnoreCase("%")) {
            return new UnitImpl(UnitBase.COUNT, -2, "PERCENT", 1, 1);
        } else if (unitString.equalsIgnoreCase("MBAR")) {
            return UnitImpl.MILLIBAR;
        } else if (unitString.equalsIgnoreCase("C") || unitString.equalsIgnoreCase("TC") || unitString.equalsIgnoreCase("CELSIUS")
                || unitString.equalsIgnoreCase("DEGC")) {
            return UnitImpl.CELSIUS;
        } else if (unitString.equalsIgnoreCase("S") || unitString.equalsIgnoreCase("SEC")) {
            return UnitImpl.SECOND;
        } else if (unitString.equalsIgnoreCase("USEC")) {
            return UnitImpl.MICROSECOND;
        } else if (unitString.equalsIgnoreCase("A") || unitString.equalsIgnoreCase("AMPERES")) {
            return UnitImpl.AMPERE;
        } else if (unitString.equalsIgnoreCase("T")) {
            return UnitImpl.TESLA;
        } else if (unitString.equalsIgnoreCase("NT")) {
            return UnitImpl.multiply(0.000000001, UnitImpl.TESLA, "NANOTESLA");
        } else if (unitString.equalsIgnoreCase("V")
                || unitString.equalsIgnoreCase("VOLTS")
                || unitString.equalsIgnoreCase("VOLT_UNIT")) {
            return UnitImpl.VOLT;
        } else if (unitString.equalsIgnoreCase("MILLIVOLTS")) {
            return UnitImpl.multiply(.001, UnitImpl.VOLT, "MILLIVOLT");
        } else if (unitString.equalsIgnoreCase("V/M")) {
            return UnitImpl.VOLT_PER_METER;
        } else if (unitString.equalsIgnoreCase("W/M2") || unitString.equalsIgnoreCase("WATTS/M^2") ) {
            return UnitImpl.divide(UnitImpl.WATT, UnitImpl.SQUARE_METER);
        } else if (unitString.equalsIgnoreCase("RAD") || 
                unitString.equalsIgnoreCase("RADIAN") || 
                unitString.equalsIgnoreCase("RADIANS") || 
                unitString.equalsIgnoreCase("TILT")) {
            return UnitImpl.RADIAN;
        } else if (unitString.equalsIgnoreCase("MICRORADIANS")) {
            return UnitImpl.multiply(.000001, UnitImpl.RADIAN, "MICRORADIAN");
        } else if (unitString.equalsIgnoreCase("RAD/S")) {
            return UnitImpl.RADIAN_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("MM/HOUR")) {
            return UnitImpl.divide(UnitImpl.MILLIMETER, UnitImpl.HOUR);
        } else if (unitString.equalsIgnoreCase("D") || unitString.equalsIgnoreCase("DEGREES")) {
            return UnitImpl.DEGREE;
        } else if (unitString.equalsIgnoreCase("DEGC")) {
            return UnitImpl.CELSIUS;
        } else if (unitString.equalsIgnoreCase("COUNTS") || unitString.equalsIgnoreCase("COUNT_UNIT")) {
            return UnitImpl.COUNT;
        } else if (unitString.equalsIgnoreCase("REBOOTS")
                || unitString.equalsIgnoreCase("CYCLES")
                || unitString.equalsIgnoreCase("ERROR")
                || unitString.equalsIgnoreCase("BYTES")
                || unitString.equalsIgnoreCase("GAPS")) {
            return UnitImpl.COUNT;
        } else if (unitString.equalsIgnoreCase("B") && unitDescription.trim().equalsIgnoreCase("boolean")) {
            return UnitImpl.DIMENSONLESS;
        } else if (unitString.equalsIgnoreCase("1") || unitString.equalsIgnoreCase("M/M") || unitString.equalsIgnoreCase("NULL")) {
            return UnitImpl.divide(UnitImpl.METER, UnitImpl.METER);
        } else if (unitString.equalsIgnoreCase("M**3/M**3")) {
            return UnitImpl.CUBIC_METER_PER_CUBIC_METER;
        } else if (unitString.equalsIgnoreCase("BITS/SEC")) {
            return UnitImpl.divide(UnitImpl.COUNT, UnitImpl.SECOND);
        } else if (unitString.equalsIgnoreCase("C/S")) {
            return UnitImpl.divide(UnitImpl.COULOMB, UnitImpl.SECOND);
        } else {
            try {
                return UnitImpl.getUnitFromString(unitString);
            } catch(NoSuchFieldException e) {
                throw new StationXMLException("Unknown unit: '" + unitString + "' described as "+unitDescription);
            }
        }
    }

    public static Time convertTime(String xml) {
        return new MicroSecondDate(xml).getFissuresTime();
    }

    public static Time convertTime(String xml, String defaultTime) {
        String s = xml;
        if (xml == null) {
            s = defaultTime;
        }
        return new MicroSecondDate(s).getFissuresTime();
    }

    public static String makeNoNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static final Filter UNITY_POLE_ZERO = new Filter();

    public static final Filter UNITY_COEFFICENT_FILTER = new Filter();
    static {
        UNITY_POLE_ZERO.pole_zero_filter(new PoleZeroFilter(new ComplexNumberErrored[] {new ComplexNumberErrored(0,
                                                                                                                 0,
                                                                                                                 0,
                                                                                                                 0)},
                                                            new ComplexNumberErrored[] {new ComplexNumberErrored(0,
                                                                                                                 0,
                                                                                                                 0,
                                                                                                                 0)}));
        UNITY_COEFFICENT_FILTER.coeff_filter(new CoefficientFilter(new CoefficientErrored[] {new CoefficientErrored(1,
                                                                                                                    0)},
                                                                   new CoefficientErrored[0]));
    }

    public static final String UNKNOWN = "";

    public static final TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);
    
    public static final String WAY_FUTURE = "24990101T00:00:00.000";
    
    public static final String WAY_PAST = "10010101T00:00:00.000";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationXMLToFissures.class);
}
