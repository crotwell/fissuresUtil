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
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;

/**
 * @author groves Created on Feb 17, 2005
 */
public class SeismogramDisplayConfiguration implements Cloneable {

    public SeismogramDisplayConfiguration() {
        makeDefault = true;
    }

    public void configure(Element el) throws NoSuchFieldException {
        makeDefault = false;
        type = DOMHelper.extractText(el, "type", type);
        checkForColorClass(el, "flagColors");
        checkForColorClass(el, "traceColors");
        if(DOMHelper.hasElement(el, "borderBackground")) {
            borderBackground = ColorConfiguration.create(DOMHelper.getElement(el,
                                                                              "borderBackground"));
        }
        if(DOMHelper.hasElement(el, "borderColor")) {
            borderColor = ColorConfiguration.create(DOMHelper.getElement(el,
                                                                         "borderColor"));
        }
        NodeList borderNodes = DOMHelper.extractNodes(el, "border");
        for(int i = 0; i < borderNodes.getLength(); i++) {
            borders.add(BorderConfiguration.create((Element)borderNodes.item(i),
                                                   borderColor,
                                                   borderBackground));
        }
        if(DOMHelper.hasElement(el, "dontDrawNamedDrawableNames")) {
            drawNamesForNamedDrawables = false;
        }
        if(DOMHelper.hasElement(el, "swapAxes")) {
            swapAxes = true;
        }
        if(DOMHelper.hasElement(el, "timeConfig")) {
            Element timeConfigEl = DOMHelper.getElement(el, "timeConfig");
            tcConfig = TimeConfigConfiguration.create(timeConfigEl);
        }
        if(DOMHelper.hasElement(el, "ampConfig")) {
            Element ampConfigEl = DOMHelper.getElement(el, "ampConfig");
            acConfig = AmpConfigConfiguration.create(ampConfigEl);
        }
    }

    public static SeismogramDisplayConfiguration create(Element el)
            throws NoSuchFieldException {
        SeismogramDisplayConfiguration c = null;
        if(defs.hasDefinition(el)) {
            SeismogramDisplayConfiguration base = (SeismogramDisplayConfiguration)defs.getDefinition(el);
            c = (SeismogramDisplayConfiguration)base.clone();
        } else {
            c = new SeismogramDisplayConfiguration();
        }
        c.configure(el);
        defs.updateDefinitions(el, c);
        return c;
    }

    private void checkForColorClass(Element el, String tagName) {
        if(DOMHelper.hasElement(el, tagName)) {
            colorClasses.add(new ColorClassConfiguration(DOMHelper.getElement(el,
                                                                              tagName)));
        }
    }

    public SeismogramDisplay createDisplay() {
        AmpConfig ampConfig = acConfig.createAmpConfig();
        TimeConfig timeConfig = tcConfig.createTimeConfig();
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
        Iterator it = borders.iterator();
        while(it.hasNext()) {
            BorderConfiguration border = (BorderConfiguration)it.next();
            disp.add(border.createBorder(disp), border.getPosition());
        }
        if(borderBackground != null) {
            disp.setBackground(borderBackground.createColor());
        }
        it = colorClasses.iterator();
        while(it.hasNext()) {
            ColorClassConfiguration cur = (ColorClassConfiguration)it.next();
            disp.setColors(cur.getColorClass(), cur.getColors());
        }
        if(ampConfig != null) {
            disp.setAmpConfig(ampConfig);
        }
        if(timeConfig != null) {
            disp.setTimeConfig(timeConfig);
        }
        return disp;
    }

    public Object clone() {
        SeismogramDisplayConfiguration clone = new SeismogramDisplayConfiguration();
        clone.type = type;
        clone.colorClasses.addAll(colorClasses);
        clone.borders.addAll(borders);
        clone.swapAxes = swapAxes;
        clone.drawNamesForNamedDrawables = drawNamesForNamedDrawables;
        clone.borderBackground = borderBackground;
        clone.borderColor = borderColor;
        clone.acConfig = acConfig;
        clone.tcConfig = tcConfig;
        return clone;
    }

    private String type = "basic";

    private List borders = new ArrayList();

    private List colorClasses = new ArrayList();

    private TimeConfigConfiguration tcConfig;

    private AmpConfigConfiguration acConfig;

    private boolean swapAxes = false;

    private boolean drawNamesForNamedDrawables = true, makeDefault = false;

    private ColorConfiguration borderBackground, borderColor;

    private static ConfigDefinitions defs = new ConfigDefinitions();

    public String getType() {
        return type;
    }
}