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
        NodeList colorNodes = DOMHelper.extractNodes(el, "color");
        if(colorNodes.getLength() > 0) {
            colors = new Color[colorNodes.getLength()];
            for(int i = 0; i < colors.length; i++) {
                colors[i] = new ColorConfiguration((Element)colorNodes.item(i)).createColor();
            }
        }
        if(el.getTagName().equals("flagColors")) {
            colorClass = Flag.class;
        } else if(el.getTagName().equals("traceColors")) {
            colorClass = DrawableSeismogram.class;
        }
    }

    public Class getColorClass() {
        return colorClass;
    }

    public Color[] getColors() {
        return colors;
    }

    private Class colorClass;

    private Color[] colors = new Color[] {Color.BLACK};
}