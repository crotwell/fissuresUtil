package edu.sc.seis.fissuresUtil.display.configuration;

import java.awt.Color;
import org.w3c.dom.Element;

/**
 * @author groves Created on Feb 23, 2005
 */
public class ColorConfiguration {

    public ColorConfiguration(Element el) {
        r = Integer.parseInt(DOMHelper.extractText(el, "red", "255"));
        g = Integer.parseInt(DOMHelper.extractText(el, "green", "255"));
        b = Integer.parseInt(DOMHelper.extractText(el, "blue", "255"));
        a = Integer.parseInt(DOMHelper.extractText(el, "alpha", "255"));
    }

    /**
     * Creates a color configuration from the element named name that is a child
     * of el if one exists. Returns null if no such element is found
     */
    public static ColorConfiguration create(Element el, String name) {
        if(DOMHelper.hasElement(el, name)) { return new ColorConfiguration(DOMHelper.getElement(el,
                                                                                                name)); }
        return null;
    }

    public Color createColor() {
        return new Color(r, g, b, a);
    }

    private int r, g, b, a;
}