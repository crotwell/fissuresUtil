package edu.sc.seis.fissuresUtil.display.configuration;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

/**
 * @author groves Created on Mar 1, 2005
 */
public class ConfigDefinitions {

    public boolean hasDefinition(Element el) {
        return el.hasAttribute("base");
    }

    public Object getDefinition(Element el) {
        return idsToConfigs.get(el.getAttribute("base"));
    }

    public void updateDefinitions(Element el, Object o) {
        if(el.hasAttribute("id")) {
            String id = el.getAttribute("id");
            if(idsToConfigs.containsKey(id)) {
                logger.debug("Overwriting " + idsToConfigs.get(id) + " with "
                        + o + " on id " + id);
            }
            idsToConfigs.put(id, o);
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConfigDefinitions.class);

    private Map idsToConfigs = new HashMap();
}