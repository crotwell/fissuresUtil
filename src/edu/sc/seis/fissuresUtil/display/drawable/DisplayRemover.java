package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;



/**
 * DisplayRemove.java
 *
 * @author Created by Charlie Groves
 */

public class DisplayRemover extends BigX{
    public DisplayRemover(SeismogramDisplay display){
        super(display.getCenter());
        this.display = display;
        display.getCenter().addComponentListener(new ComponentAdapter(){
                    public void componentResized(ComponentEvent e){setXY();}
                });
        setXY();
    }

    private void setXY(){
        setXY(display.getCenter().getWidth() - 10, 5);
    }

    public void clicked(){ display.clear(); }

    private SeismogramDisplay display;
}
