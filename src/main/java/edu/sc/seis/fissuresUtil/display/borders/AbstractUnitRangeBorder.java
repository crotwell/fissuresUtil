package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.fissuresUtil.display.DisplayUtils;

public abstract class AbstractUnitRangeBorder extends Border implements
        TitleProvider {

    public AbstractUnitRangeBorder(int side, int order) {
        super(side, order);
        add((TitleProvider)this);
        setPreferredSize(new Dimension(80, 50));
    }

    protected List createFormats() {
        List formats = new ArrayList();
        for(double i = .000000001; i <= 100000000; i *= 10) {
            formats.add(new UnitRangeFormatter(i, 2));
            formats.add(new UnitRangeFormatter(i * 2, 2));
            formats.add(new UnitRangeFormatter(i * 3, 3));
            formats.add(new UnitRangeFormatter(i * 5, 5));
        }
        return formats;
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font f) {
        titleFont = f;
    }

    public String getMaxLengthFormattedString() {
        return ("0.000E00");
    }

    private class UnitRangeFormatter extends BorderFormat {

        public UnitRangeFormatter(double division, int ticksPerDivision) {
            super(division, ticksPerDivision);
            if(division < .01) {
                // exponential notation
                df = new DecimalFormat("#.###E00");
            } else
                if(division < 10 && division != 0) {
                    df = new DecimalFormat("0.00###");
                } else
                    if(division < 100000) {
                        df = new DecimalFormat("#.####");
                    } else {
                        // exponential notation
                        df = new DecimalFormat("#.###E00");
                    }
        }

        public String getMaxString() {
            return df.format(divSize * 10);
        }

        public String getLabel(double value) {
            return df.format(value);
        }

        public String toString() {
            return "UnitRange " + getDivSize() + " " + ticksPerDiv;
        }

        private DecimalFormat df;
    }

    public Color getTitleColor() {
        return c;
    }

    public void setTitleColor(Color newColor) {
        this.c = newColor;
    }

    private Color c = null;

    private Font titleFont = DisplayUtils.DEFAULT_FONT;
}