package edu.sc.seis.fissuresUtil.display.configuration;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.OriginAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.PhaseAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RTTimeRangeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RelativeTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;

/**
 * @author danala Created on Mar 10, 2005
 */
public class TimeConfigConfiguration implements Cloneable {

    public void configure(Element element) throws NoSuchFieldException {
        type = DOMHelper.extractText(element, "type", type);
        if(type.equals("autoAdvance")) {
            configureRealTimeConfig(element);
        } else if(type.equals("phaseAligned")) {
            phaseName = DOMHelper.extractText(element, "phaseName");
        }
    }

    private void configureRealTimeConfig(Element element)
            throws NoSuchFieldException {
        if(DOMHelper.hasElement(element, "timeConfig")) {
            Element el = DOMHelper.getElement(element, "timeConfig");
            timeConfig = getTimeConfig(el);
        }
        if(DOMHelper.hasElement(element, "advanceInterval")) {
            Element intervalEl = DOMHelper.getElement(element,
                                                      "advanceInterval");
            advanceInterval = getAdvanceInterval(intervalEl);
        }
        if(DOMHelper.hasElement(element, "advancesPerSecond")) {
            advPerSec = new Float(DOMHelper.extractText(element,
                                                        "advancesPerSecond")).floatValue();
        }
    }

    public static TimeInterval getAdvanceInterval(Element el)
            throws NoSuchFieldException {
        double value = new Double(DOMHelper.extractText(el, "value")).doubleValue();
        String unit = DOMHelper.extractText(el, "unit");
        UnitImpl units = UnitImpl.getUnitFromString(unit);
        return new TimeInterval(value, units);
    }

    public TimeConfig getTimeConfig(Element el) throws NoSuchFieldException {
        TimeConfigConfiguration tConfig = TimeConfigConfiguration.create(el);
        return tConfig.getTimeConfig();
    }

    private TimeConfig getTimeConfig() {
        return this.timeConfig;
    }

    public static TimeConfigConfiguration create(Element el)
            throws NoSuchFieldException {
        TimeConfigConfiguration c = null;
        if(defs.hasDefinition(el)) {
            TimeConfigConfiguration base = (TimeConfigConfiguration)defs.getDefinition(el);
            c = (TimeConfigConfiguration)base.clone();
        } else {
            c = new TimeConfigConfiguration();
        }
        c.configure(el);
        defs.updateDefinitions(el, c);
        return c;
    }

    public TimeConfig createTimeConfig() {
        TimeConfig tc = null;
        if(type.equals("basic")) {
            tc = new BasicTimeConfig();
        } else if(type.equals("relative")) {
            tc = new RelativeTimeConfig();
        } else if(type.equals("originAligned")) {
            tc = new OriginAlignedTimeConfig();
        } else if(type.equals("phaseAligned")) {
            tc = new PhaseAlignedTimeConfig();
            TauP_Time tauP = new TauP_Time();
            tauP.setPhaseNames(new String[] {phaseName});
            ((PhaseAlignedTimeConfig)tc).setTauP(tauP);
        } else if(type.equals("autoAdvance")) {
            tc = new RTTimeRangeConfig(timeConfig, advanceInterval, advPerSec);
        }
        return tc;
    }

    public Object clone() {
        TimeConfigConfiguration clone = new TimeConfigConfiguration();
        clone.type = type;
        clone.phaseName = phaseName;
        clone.timeConfig = timeConfig;
        clone.advanceInterval = advanceInterval;
        clone.advPerSec = advPerSec;
        return clone;
    }

    private String type;

    private String phaseName;

    private TimeConfig timeConfig;

    private TimeInterval advanceInterval = RTTimeRangeConfig.DEFAULT_REFRESH;

    float advPerSec = 1;

    private static ConfigDefinitions defs = new ConfigDefinitions();
}