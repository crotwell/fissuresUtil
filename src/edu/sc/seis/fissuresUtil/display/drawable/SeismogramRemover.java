package edu.sc.seis.fissuresUtil.display.drawable;

/**
 * SeismogramRemove.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Graphics2D;

public class SeismogramRemover extends BigX{
    public SeismogramRemover(DataSetSeismogram seis, SeismogramDisplay display){
        super(display);
        this.display = display;
        this.seismogram = seis;
    }

    public void clicked() {
        DataSetSeismogram[] seisArray = { seismogram};
        display.remove(seisArray);
    }

    public void draw(Graphics2D canvas, int x, int y){
        setXY(x, y);
        draw(canvas, null, null, null);
    }

    DataSetSeismogram seismogram;

    SeismogramDisplay display;
}

