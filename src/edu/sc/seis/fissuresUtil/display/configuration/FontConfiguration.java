package edu.sc.seis.fissuresUtil.display.configuration;

import java.awt.Font;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author groves Created on Feb 18, 2005
 */
public class FontConfiguration implements Cloneable {

    private FontConfiguration() {}

    public void configure(Element el) {
        size = DOMHelper.extractText(el, "size", size);
        name = DOMHelper.extractText(el, "name", name);
        styleString = DOMHelper.extractText(el, "style", styleString);
    }

    public static FontConfiguration create(Element el) {
        FontConfiguration c = null;
        if(defs.referencesDefinition(el)) {
            try {
                FontConfiguration base = (FontConfiguration)defs.getDefinition(el);
                c = (FontConfiguration)base.clone();
            } catch(CloneNotSupportedException e) {
                GlobalExceptionHandler.handle("But I added clone to this object....");
            }
        } else {
            c = new FontConfiguration();
        }
        c.configure(el);
        defs.updateDefinitions(el, c);
        return c;
    }

    public Font createFont() {
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
        return new Font(name, style, Integer.parseInt(size));
    }

    public String toString() {
        return "FontConfiguration of size " + size + " font " + name
                + " with style " + styleString;
    }

    private String size = "12", name = "Serif", styleString = "plain";

    private static ConfigDefinitions defs = new ConfigDefinitions();
}