package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUnitRangeBorder extends Border implements TitleProvider{
    public AbstractUnitRangeBorder(int side, int order){
        super(side, order);
        add((TitleProvider)this);
        setPreferredSize(new Dimension(width, 50));
    }

    protected List createFormats() {
        List formats = new ArrayList();
        for (double i = .000000001; i <= 100000000; i *= 10) {
            formats.add(new UnitRangeFormatter(i, 2));
            formats.add(new UnitRangeFormatter(i*5, 5));
        }
        return formats;
    }
    public int getWidth(){
        return width;
    }

    public abstract String getTitle();
    
    private int width=80;

    private class UnitRangeFormatter extends BorderFormat{

        public UnitRangeFormatter(double division, int ticksPerDivision){
            super(division, ticksPerDivision);
            if(division < .01){
                // exponential notation
                df = new DecimalFormat("#.###E00");
            }else if (division < 10 && division != 0 ) {
                df = new DecimalFormat("0.00###");
            } else if(division < 100000){
                df = new DecimalFormat("#.####");
            }else{
                // exponential notation
                df = new DecimalFormat("#.###E00");
            }
        }

        public String getMaxString() { return df.format(divSize * 10); }

        public String getLabel(double value) { return df.format(value); }

        public String toString() {
            return "UnitRange "+getDivSize();
        }

        private DecimalFormat df;
    }
}

