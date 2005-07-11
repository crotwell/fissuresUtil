package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Graphics2D;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.drawable.BigX;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;


/**
 * @author hedx
 * Created on Jul 5, 2005
 */
public class ParticleMotionRemover extends BigX{
    
    public ParticleMotionRemover(DataSetSeismogram[] seis, SeismogramDisplay display){
        super(display.getCenter());
        this.display = display;
        this.seismogram = seis;
    }

    public void clicked() {
        display.remove(seismogram);
    }

    public void draw(Graphics2D canvas, int x, int y){
        setXY(x, y);
        draw(canvas, null, null, null);
    }

    DataSetSeismogram seismogram[];

    SeismogramDisplay display;
}
