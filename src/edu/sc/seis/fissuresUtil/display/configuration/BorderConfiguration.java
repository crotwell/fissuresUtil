package edu.sc.seis.fissuresUtil.display.configuration;

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
public class BorderConfiguration {

    public BorderConfiguration(Element element, ColorConfiguration color,
            ColorConfiguration background) {
        type = DOMHelper.extractText(element, "type", TITLE);
        order = DOMHelper.extractText(element, "order", "ascending");
        position = DOMHelper.extractText(element, "position", LEFT);
        NodeList titleList = DOMHelper.extractNodes(element, "title");
        titles = new BorderTitleConfiguration[titleList.getLength()];
        for(int i = 0; i < titleList.getLength(); i++) {
            titles[i] = new BorderTitleConfiguration((Element)titleList.item(i));
        }
        this.color = color;
        this.background = background;
        this.titleFont = FontConfiguration.create(element, "titleFont");
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
        b.setSide(side);
        b.setOrder(getOrder(order));
        for(int i = 0; i < titles.length; i++) {
            b.add(titles[i].createTitle());
        }
        if(background != null) {
            b.setBackground(background.createColor());
        }
        if(color != null) {
            b.setColor(color.createColor());
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

    private String type, order, position;

    private ColorConfiguration background, color;

    private FontConfiguration titleFont;

    private static final String LEFT = "left", RIGHT = "right", TOP = "top",
            BOTTOM = "bottom", AMP = "amp", TIME = "time", DIST = "dist",
            TITLE = "title", ASCENDING = "ascending",
            DESCENDING = "descending";

    private BorderTitleConfiguration[] titles;
}