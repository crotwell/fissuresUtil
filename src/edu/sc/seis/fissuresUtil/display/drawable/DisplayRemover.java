package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;



/**
 * DisplayRemove.java
 *
 * @author Created by Charlie Groves
 */

public class DisplayRemover extends BigX{

    public DisplayRemover(SeismogramDisplay display){
        super(display);
        this.display = display;
    }

    public void clicked(){
        display.clear();
    }

    private SeismogramDisplay display;
}
