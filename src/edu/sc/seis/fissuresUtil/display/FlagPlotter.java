package edu.sc.seis.fissuresUtil.display;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.*;
import edu.iris.Fissures.model.MicroSecondDate;
import java.awt.Color;

/**
 * FlagPlotter.java
 *
 *
 * Created: Wed Jul  3 11:50:13 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class FlagPlotter implements Plotter{
    
    public FlagPlotter(MicroSecondDate flagTime, String name){
	this.flagTime = flagTime;
	this.name = name;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeSnapshot timeState, AmpSnapshot ampState){
	if(visible){
	    MicroSecondTimeRange timeRange = timeState.getTimeRange();
	    if(flagTime.before(timeRange.getBeginTime()) || flagTime.after(timeRange.getEndTime()))
		return;
	    double offset = flagTime.difference(timeRange.getBeginTime()).getValue()/timeRange.getInterval().getValue();
	    location = (int)(offset * (double)size.width);
	    Area pole = new Area(new Rectangle(location, 0, 1, size.height));
	    Rectangle2D.Float stringBounds = new Rectangle2D.Float();
	    stringBounds.setRect(canvas.getFontMetrics().getStringBounds(name, canvas));
	    Area flag = new Area(new Rectangle(location, 0, (int)stringBounds.width, (int)stringBounds.height));
	    flag.add(pole);
	    canvas.setColor(Color.red);
	    canvas.fill(flag);
	    canvas.setColor(Color.white);
	    canvas.drawString(name, location, stringBounds.height - 4);
	}
    }
    
    public void toggleVisibility(){ visible = !visible; }

    public void setVisibility(boolean b){ visible = b; }

    protected boolean visible = true;

    protected MicroSecondDate flagTime;
    
    protected String name;

    protected int location;
}// FlagPlotter
