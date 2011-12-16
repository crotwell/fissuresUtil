package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Graphics2D;
import java.util.List;

import edu.iris.Fissures.model.UnitRangeImpl;

/**
 * @author groves Created on Apr 21, 2005
 */
public abstract class NoTickBorder extends Border {

    public NoTickBorder(int side, int order) {
        super(side, order);
        labelTickLength = 0;
        tickLength = 0;
        setSide(side);
        bf = createNoTickFormat();
    }

    protected void paintBorder(Graphics2D g2d) {
        bf.draw(getRange(), g2d);
    }

    public String getMaxLengthFormattedString() {
        return "";
    }

    protected List createFormats() {
        return null;
    }

    protected UnitRangeImpl getRange() {
        return null;
    }

    protected abstract BorderFormat createNoTickFormat();

    private BorderFormat bf;
}
