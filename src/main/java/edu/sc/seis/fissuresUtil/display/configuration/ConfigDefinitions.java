package edu.sc.seis.fissuresUtil.display.configuration;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author groves Created on Mar 1, 2005
 */
public class ConfigDefinitions {
    
    public boolean referencesDefinition(Element el){
        return el.hasAttribute("base");
    }

    public static String definitionName(Element el) {
        return el.getAttribute("base");
    }

    public boolean hasDefinition(Element el) {
        return idsToConfigs.containsKey(definitionName(el));
    }

    public Object getDefinition(Element el) {
        if(!hasDefinition(el)) {
            String elementPath = el.getTagName();
            Node parent = el.getParentNode();
            while(parent != null && parent instanceof Element){
                elementPath = ((Element)parent).getTagName() + "/" + elementPath;
                parent = parent.getParentNode();
            }
            throw new RuntimeException(elementPath
                    + " references an undefined definition " + definitionName(el));
        }
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
        } else if(el.hasAttribute("overwritingId")) {
            idsToConfigs.put(el.getAttribute("overwritingId"), o);
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConfigDefinitions.class);

    private Map idsToConfigs = new HashMap();
}