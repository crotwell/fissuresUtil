package edu.sc.seis.fissuresUtil.display.registrar;

import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class LayoutData{
    public LayoutData(DataSetSeismogram seis, double startPercentage, double endPercentage){
        this.seis = seis;
        this.start = startPercentage;
        this.end = endPercentage;
    }

    public DataSetSeismogram getSeis(){ return seis; }

    public double getStart(){ return start; }

    public double getEnd(){ return end; }

    private final DataSetSeismogram seis;

    private final double start, end;

}

