package edu.sc.seis.fissuresUtil.display.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.fissuresUtil.display.BorderedDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.borders.AmpBorder;
import edu.sc.seis.fissuresUtil.display.borders.Border;
import edu.sc.seis.fissuresUtil.display.borders.DistanceBorder;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.display.borders.TitleProvider;

/**
 * @author groves Created on Feb 17, 2005
 */
public class BorderConfiguration implements Cloneable {

    public void configure(Element element) {
        type = DOMHelper.extractText(element, "type", type);
        order = DOMHelper.extractText(element, "order", order);
        position = DOMHelper.extractText(element, "position", position);
        NodeList titleList = DOMHelper.extractNodes(element, "title");
        for(int i = 0; i < titleList.getLength(); i++) {
            titles.add(new BorderTitleConfiguration((Element)titleList.item(i)));
        }
        if(DOMHelper.hasElement(element, "titleFont")) {
            this.titleFont = FontConfiguration.create(DOMHelper.getElement(element,
                                                                           "titleFont"));
        }
        if(DOMHelper.hasElement(element, "clipTicks")) {
            clipTicks = true;
            Element tickConfig = DOMHelper.getElement(element, "clipTicks");
            minTickValue = new Double(DOMHelper.extractText(tickConfig, "min")).doubleValue();
            maxTickValue = new Double(DOMHelper.extractText(tickConfig, "max")).doubleValue();
        }
    }

    public static BorderConfiguration create(Element el,
                                             ColorConfiguration color,
                                             ColorConfiguration background) {
        BorderConfiguration c = null;
        if(defs.hasDefinition(el)) {
            BorderConfiguration base = (BorderConfiguration)defs.getDefinition(el);
            c = (BorderConfiguration)base.clone();
        } else {
            c = new BorderConfiguration();
        }
        c.configure(el);
        c.color = color;
        c.background = background;
        defs.updateDefinitions(el, c);
        return c;
    }

    public int getPosition() {
        if(position.equals(LEFT)) {
            return BorderedDisplay.CENTER_LEFT;
        } else if(position.equals(RIGHT)) {
            return BorderedDisplay.CENTER_RIGHT;
        } else if(position.equals(TOP)) {
            return BorderedDisplay.TOP_CENTER;
        } else {
            return BorderedDisplay.BOTTOM_CENTER;
        }
    }

    public JComponent createBorder(SeismogramDisplay disp) {
        Border b;
        int side = getSide(position);
        if(type.equals(AMP)) {
            b = new AmpBorder(disp, side, false);
        } else if(type.equals(TIME)) {
            b = new TimeBorder(disp);
        } else if(type.equals(DIST)) {
            b = new DistanceBorder(disp);
        } else {
            b = new TitleBorder(getSide(position), getOrder(order));
        }
        if(b instanceof TitleProvider && titleFont != null) {
            ((TitleProvider)b).setTitleFont(titleFont.createFont());
        }
        if(color != null) {
            b.setColor(color.createColor());
        }
        if(background != null) {
            b.setBackground(background.createColor());
        }
        b.setSide(side);
        b.setOrder(getOrder(order));
        if(clipTicks) {
            b.setClipTicks(minTickValue, maxTickValue);
        }
        Iterator it = titles.iterator();
        while(it.hasNext()) {
            b.add(((BorderTitleConfiguration)it.next()).createTitle());
        }
        return b;
    }

    private static int getOrder(String order) {
        if(order.equals(ASCENDING)) {
            return Border.ASCENDING;
        } else {
            return Border.DESCENDING;
        }
    }

    private static int getSide(String position) {
        if(position.equals(LEFT)) {
            return Border.LEFT;
        } else if(position.equals(RIGHT)) {
            return Border.RIGHT;
        } else if(position.equals(TOP)) {
            return Border.TOP;
        } else {
            return Border.BOTTOM;
        }
    }

    public String toString() {
        return "BorderConfiguration that produces " + type + " borders in "
                + order + " order in position " + position;
    }

    public Object clone() {
        BorderConfiguration clone = new BorderConfiguration();
        clone.background = background;
        clone.color = color;
        clone.titleFont = titleFont;
        clone.type = type;
        clone.order = order;
        clone.position = position;
        clone.clipTicks = clipTicks;
        clone.minTickValue = minTickValue;
        clone.maxTickValue = maxTickValue;
        clone.titles.addAll(titles);
        return this;
    }

    private String type = TITLE, order = ASCENDING, position = LEFT;

    private ColorConfiguration background, color;

    private FontConfiguration titleFont;

    private boolean clipTicks = false;

    private double minTickValue = Double.NEGATIVE_INFINITY;

    private double maxTickValue = Double.POSITIVE_INFINITY;

    private static ConfigDefinitions defs = new ConfigDefinitions();

    private static final String LEFT = "left", RIGHT = "right", TOP = "top",
            BOTTOM = "bottom", AMP = "amp", TIME = "time", DIST = "dist",
            TITLE = "title", ASCENDING = "ascending",
            DESCENDING = "descending";

    private List titles = new ArrayList();
}