package edu.sc.seis.fissuresUtil.display.configuration;

import java.awt.Font;
import org.w3c.dom.Element;

/**
 * @author groves Created on Feb 18, 2005
 */
public class FontConfiguration {

    public FontConfiguration(Element el) {
        String size = DOMHelper.extractText(el, "size", "12");
        String name = DOMHelper.extractText(el, "name", "Serif");
        String styleString = DOMHelper.extractText(el, "style", "plain");
        int style;
        if(styleString.equals("plain")) {
            style = Font.PLAIN;
        } else if(styleString.equals("bold")) {
            style = Font.BOLD;
        } else if(styleString.equals("italic")) {
            style = Font.ITALIC;
        } else {
            throw new IllegalArgumentException("The value of style must be plain, bold or italic.  You specified "
                    + styleString + ".");
        }
        f = new Font(name, style, Integer.parseInt(size));
    }

    /**
     * Creates a font configuration from the element named name that is a child
     * of el if one exists. Returns null if no such element is found
     */
    public static FontConfiguration create(Element el, String name) {
        if(DOMHelper.hasElement(el, name)) { return new FontConfiguration(DOMHelper.getElement(el,
                                                                                               name)); }
        return null;
    }

    public Font createFont() {
        return f;
    }

    private Font f;
}