package edu.sc.seis.fissuresUtil.display.configuration;

import java.awt.Color;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Flag;

/**
 * @author groves Created on Feb 24, 2005
 */
public class ColorClassConfiguration {

    public ColorClassConfiguration(Element el) {
        if(defs.hasDefinition(el)) {
            ColorClassConfiguration definer = (ColorClassConfiguration)defs.getDefinition(el);
            colorClass = definer.colorClass;
            colors = definer.colors;
        } else {
            Color[] configColors = extractColors(el);
            if(configColors.length > 0) {
                colors = configColors;
            }
            if(el.getTagName().equals("flagColors")) {
                colorClass = Flag.class;
            } else if(el.getTagName().equals("traceColors")) {
                colorClass = DrawableSeismogram.class;
            }
            defs.updateDefinitions(el, this);
        }
    }

    public static Color[] extractColors(Element el) {
        NodeList colorNodes = DOMHelper.extractNodes(el, "color");
        Color[] colors = new Color[colorNodes.getLength()];
        for(int i = 0; i < colors.length; i++) {
            colors[i] = ColorConfiguration.create((Element)colorNodes.item(i))
                    .createColor();
        }
        return colors;
    }

    public Class getColorClass() {
        return colorClass;
    }

    public Color[] getColors() {
        return colors;
    }

    private Class colorClass;

    private static ConfigDefinitions defs = new ConfigDefinitions();

    private Color[] colors = new Color[] {Color.BLACK};
}