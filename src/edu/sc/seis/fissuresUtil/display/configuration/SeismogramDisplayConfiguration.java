package edu.sc.seis.fissuresUtil.display.configuration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;

/**
 * @author groves Created on Feb 17, 2005
 */
public class SeismogramDisplayConfiguration {

    public SeismogramDisplayConfiguration() {
        makeDefault = true;
    }

    public SeismogramDisplayConfiguration(Element el) {
        type = DOMHelper.extractText(el, "type", "basic");
        borderBackground = ColorConfiguration.create(el, "borderBackground");
        ColorConfiguration borderColor = ColorConfiguration.create(el,
                                                                   "borderColor");
        NodeList borderNodes = DOMHelper.extractNodes(el, "border");
        borders = new BorderConfiguration[borderNodes.getLength()];
        for(int i = 0; i < borders.length; i++) {
            borders[i] = new BorderConfiguration((Element)borderNodes.item(i),
                                                 borderColor,
                                                 borderBackground);
        }
        checkForColorClass(el, "flagColors");
        checkForColorClass(el, "traceColors");
        if(DOMHelper.hasElement(el, "dontDrawNamedDrawableNames")) {
            drawNamesForNamedDrawables = false;
        }
    }

    private void checkForColorClass(Element el, String tagName) {
        if(DOMHelper.hasElement(el, tagName)) {
            colorClasses.add(new ColorClassConfiguration(DOMHelper.getElement(el,
                                                                              tagName)));
        }
    }

    public SeismogramDisplay createDisplay() {
        if(makeDefault) { return new BasicSeismogramDisplay(); }
        SeismogramDisplay disp;
        if(type.equals("recordSection")) {
            disp = new RecordSectionDisplay(swapAxes);
        } else if(type.equals("basic")) {
            disp = new BasicSeismogramDisplay();
        } else {
            try {
                Class dispClass = Class.forName(type);
                Constructor constructor = dispClass.getConstructor(new Class[] {});
                disp = (SeismogramDisplay)constructor.newInstance(new Object[0]);
            } catch(Exception e) {
                throw new RuntimeException("Problem instantiating "
                        + type
                        + ".  It must be in the classpath, take a constructor with no arguments, and extend SeismogramDisplay");
            }
        }
        disp.setDrawNamesForNamedDrawables(drawNamesForNamedDrawables);
        disp.clearBorders();
        for(int i = 0; i < borders.length; i++) {
            BorderConfiguration border = borders[i];
            disp.add(border.createBorder(disp), border.getPosition());
        }
        if(borderBackground != null) {
            disp.setBackground(borderBackground.createColor());
        }
        Iterator it = colorClasses.iterator();
        while(it.hasNext()) {
            ColorClassConfiguration cur = (ColorClassConfiguration)it.next();
            disp.setColors(cur.getColorClass(), cur.getColors());
        }
        return disp;
    }

    private String type;

    private BorderConfiguration[] borders = new BorderConfiguration[0];

    private List colorClasses = new ArrayList();

    private boolean swapAxes = true;

    private boolean drawNamesForNamedDrawables = true, makeDefault = false;

    private ColorConfiguration borderBackground;

    public String getType() {
        return type;
    }
}