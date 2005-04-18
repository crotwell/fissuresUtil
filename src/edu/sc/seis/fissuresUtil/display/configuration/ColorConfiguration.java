package edu.sc.seis.fissuresUtil.display.configuration;

import java.awt.Color;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author groves Created on Feb 23, 2005
 */
public class ColorConfiguration implements Cloneable {

    public void configure(Element el) {
        r = DOMHelper.extractText(el, "red", r);
        g = DOMHelper.extractText(el, "green", g);
        b = DOMHelper.extractText(el, "blue", b);
        a = DOMHelper.extractText(el, "alpha", a);
    }

    public static ColorConfiguration create(Element el) {
        ColorConfiguration c = null;
        if(defs.hasDefinition(el)) {
            try {
                ColorConfiguration base = (ColorConfiguration)defs.getDefinition(el);
                if(base == null) { throw new NullPointerException("Unable to find a base for "
                        + el.getAttribute("base")); }
                c = (ColorConfiguration)base.clone();
            } catch(CloneNotSupportedException e) {
                GlobalExceptionHandler.handle("But I added clone to this object....");
            }
        } else {
            c = new ColorConfiguration();
        }
        c.configure(el);
        defs.updateDefinitions(el, c);
        return c;
    }

    public Color createColor() {
        return new Color(Integer.parseInt(r),
                         Integer.parseInt(g),
                         Integer.parseInt(b),
                         Integer.parseInt(a));
    }

    public String toString() {
        return "ColorConfiguration that produces " + createColor();
    }

    private static ConfigDefinitions defs = new ConfigDefinitions();

    private String r = "255", g = "255", b = "255", a = "255";
}