package edu.sc.seis.fissuresUtil.display;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Area;
import org.apache.log4j.*;
import edu.iris.Fissures.model.MicroSecondDate;
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

    public Shape draw(Dimension size, TimeSnapshot imageState){
	if(visible){
	    MicroSecondTimeRange overTimeRange = imageState.getTimeRange().
		getOversizedTimeRange(BasicSeismogramDisplay.OVERSIZED_SCALE);
	    if(flagTime.before(overTimeRange.getBeginTime()) || flagTime.after(overTimeRange.getEndTime()))
		return new GeneralPath();
	    double offset = flagTime.difference(overTimeRange.getBeginTime()).getValue()/overTimeRange.getInterval().getValue();
	    location = (int)(offset * (double)size.width);
	    Area pole = new Area(new Rectangle(location, 0, 1, size.height));
	    Area flag = new Area(new Rectangle(location, 0, name.length() * 12, 13));
	    flag.add(pole);
	    return flag; 
	}
	return new GeneralPath();
    }

    public String getName(){ return name; }
    
    public int getStringX(){ return (location + 3); }

    public void toggleVisibility(){ visible = !visible; }

    public void setVisibility(boolean b){ visible = b; }

    protected boolean visible = true;

    protected MicroSecondDate flagTime;
    
    protected TimeConfigRegistrar timeRegistrar;

    protected String name;

    protected int location;
}// FlagPlotter
