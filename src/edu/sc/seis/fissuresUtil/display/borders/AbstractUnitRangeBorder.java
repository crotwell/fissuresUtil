package edu.sc.seis.fissuresUtil.display.borders;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUnitRangeBorder extends Border implements TitleProvider{
    public AbstractUnitRangeBorder(int side, int order){
        super(side, order);
        add((TitleProvider)this);
        setPreferredSize(new Dimension(80, 50));
    }

    protected List createFormats() {
        List formats = new ArrayList();
        for (double i = .1; i <= 100000000; i *= 10) {
            formats.add(new UnitRangeFormatter(i, 2));
            formats.add(new UnitRangeFormatter(i*5, 5));
        }
        return formats;
    }

    public abstract String getTitle();

    private class UnitRangeFormatter extends BorderFormat{

        public UnitRangeFormatter(double division, int ticksPerDivision){
            super(division, ticksPerDivision);
            if (division < 10 && division != 0 ) {
                // exponential notation
                df = new DecimalFormat("0.00###");
            } else {
                df = new DecimalFormat("#.####");
            }
        }

        public String getMaxString() { return df.format(divSize * 10); }

        public String getLabel(double value) { return df.format(value); }

        private DecimalFormat df;
    }
}

