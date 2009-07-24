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

    public String toString(){
        if(name == null){
            name = "LayoutData for " + seis + " perecentages: " + start + ", "  + end;
        }
        return name;
    }

    public boolean equals(Object obj){
        if(getClass() == obj.getClass() &&
           name.equals(obj.toString()) &&
           seis.equals(((LayoutData)obj).getSeis())){
            return true;
        }
        return false;
    }

    public int hashCode(){
        if(hashCode == 0){
            hashCode = 67 * seis.hashCode();
            hashCode += start + end;
        }
        return hashCode;
    }

    private int hashCode = 0;

    private String name;

    private final DataSetSeismogram seis;

    private final double start, end;

}

