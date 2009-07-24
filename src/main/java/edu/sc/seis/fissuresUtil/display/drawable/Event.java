package edu.sc.seis.fissuresUtil.display.drawable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;



/**
 * Event.java
 *
 * @author Created by Charlie Groves
 */

public class Event implements NamedDrawable{
    public Event(Arrival[] arrivals, MicroSecondDate originTime, DrawableSeismogram seis){
        this(arrivals, originTime, seis, null);
    }
    
    public Event(Arrival[] arrivals, MicroSecondDate originTime,
                 DrawableSeismogram seis, String name){
        flags = new ArrayList(arrivals.length);
        for (int i = 0; i < arrivals.length; i++){
            MicroSecondDate time = new MicroSecondDate((long)(arrivals[i].getTime() * 1000000) +
                                                           originTime.getMicroSecondTime());
            flags.add(new Flag(time, arrivals[i].getName(), seis));
        }
        flags.add(new Flag(originTime, "Origin", seis));
        this.name = name;
    }
    
    public void setVisibility(boolean b) { visible = b; }
    
    public void setColor(Color color){
        Iterator it = flags.iterator();
        while(it.hasNext()){
            ((Flag)it.next()).setColor(color);
        }
        this.color = color;
    }
    
    public Color getColor(){ return color; }
    
    public Rectangle2D drawName(Graphics2D canvas, int xPosition, int yPosition) {
        if(visible && name != null){
            canvas.drawString(name, xPosition, yPosition);
            FontMetrics fm = canvas.getFontMetrics();
            Rectangle2D stringBounds = fm.getStringBounds(name, canvas);
            return stringBounds;
        }
        return DisplayUtils.EMPTY_RECTANGLE;
    }
    
    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if(visible){
            Iterator it = flags.iterator();
            while(it.hasNext()){
                ((Drawable)it.next()).draw(canvas, size, currentTime, currentAmp);
            }
        }
    }
    
    private Color color = Color.RED;
    
    private List flags;
    
    private boolean visible = true;
    
    private String name;
}
