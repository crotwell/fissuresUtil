package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import edu.iris.Fissures.model.UnitRangeImpl;

/**
 * @author groves Created on Feb 15, 2005
 */
public class TitleBorder extends Border {

    public TitleBorder(int side, int order) {
        super(side, order);
        labelTickLength = 0;
        tickLength = 0;
        setSide(side);
    }

    public TitleBorder(int side, int order, String title) {
        this(side, order);
        add(new UnchangingTitleProvider(title));
    }

    protected void paintBorder(Graphics2D g2d) {
        bf.draw(getRange(), g2d);
    }

    public String getMaxLengthFormattedString() {
        return "";
    }

    private class TitleBorderFormat extends BorderFormat {

        public TitleBorderFormat() {
            super(0, 0);
        }

        public String getMaxString() {
            return null;
        }

        public String getLabel(double value) {
            return null;
        }
    }

    protected List createFormats() {
        return null;
    }

    protected UnitRangeImpl getRange() {
        return null;
    }

    private BorderFormat bf = new TitleBorderFormat();
}