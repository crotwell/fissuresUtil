package edu.sc.seis.fissuresUtil.display.configuration;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.BasicAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;

/**
 * @author danala Created on Mar 10, 2005
 */
public class AmpConfigConfiguration implements Cloneable {

    public void configure(Element element) {
        type = DOMHelper.extractText(element, "type");
        if(type.equals("individual")) {
            if(DOMHelper.hasElement(element, "ampConfig")) {
                Element ampConfigEl = DOMHelper.getElement(element, "ampConfig");
                ampConfigForIndividualizedInternal = AmpConfigConfiguration.create(ampConfigEl);
            }
        }
    }

    public AmpConfig createAmpConfig() {
        AmpConfig ac = null;
        if(type.equals("basic")) {
            ac = new BasicAmpConfig();
        } else if(type.equals("rmean")) {
            ac = new RMeanAmpConfig();
        } else if(type.equals("individual")) {
            ac = new IndividualizedAmpConfig(ampConfigForIndividualizedInternal.createAmpConfig());
        }
        return ac;
    }

    public static AmpConfigConfiguration create(Element el) {
        AmpConfigConfiguration c = null;
        if(defs.hasDefinition(el)) {
            AmpConfigConfiguration base = (AmpConfigConfiguration)defs.getDefinition(el);
            c = (AmpConfigConfiguration)base.clone();
        } else {
            c = new AmpConfigConfiguration();
        }
        c.configure(el);
        defs.updateDefinitions(el, c);
        return c;
    }

    public Object clone() {
        AmpConfigConfiguration clone = new AmpConfigConfiguration();
        clone.type = type;
        clone.ampConfigForIndividualizedInternal = ampConfigForIndividualizedInternal;
        return clone;
    }

    private String type;

    private AmpConfigConfiguration ampConfigForIndividualizedInternal;

    private static ConfigDefinitions defs = new ConfigDefinitions();
}