package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import org.apache.log4j.Category;

/**
 * FlagPlotter.java
 *
 *
 * Created: Wed Jul  3 11:50:13 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Flag implements Drawable{
    
    public Flag(MicroSecondDate flagTime, String name){
        this(flagTime, name, null);
    }
    
    public Flag(MicroSecondDate flagTime, String name, DataSetSeismogram seis){
        this.flagTime = flagTime;
        this.name = name;
        this.seis = seis;
    }
    
    public void draw(Graphics2D canvas, Dimension size, TimeEvent timeEvent, AmpEvent ampEvent){
        if(visible){
            canvas.setFont(DisplayUtils.BOLD_FONT);
            MicroSecondTimeRange timeRange = timeEvent.getTime();
            if(seis != null)
                timeRange = timeEvent.getTime(seis);
            if(flagTime.before(timeRange.getBeginTime()) || flagTime.after(timeRange.getEndTime()))
                return;
            double offset = flagTime.difference(timeRange.getBeginTime()).getValue()/timeRange.getInterval().getValue();
            int location = (int)(offset * (double)size.width);
            Area pole = new Area(new Rectangle(location, 0, 1, size.height));
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(canvas.getFontMetrics().getStringBounds(name, canvas));
            Area flag = new Area(new Rectangle(location, 0,
                                                   (int)(stringBounds.width + PADDING),
                                                   (int)(stringBounds.height + PADDING)));
            flag.add(pole);
            canvas.setColor(color);
            canvas.fill(flag);
                canvas.setColor(Color.BLACK);
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            canvas.draw(flag);
            if(BasicSeismogramDisplay.PRINTING)
                canvas.setColor(Color.WHITE);
            canvas.drawString(name, location + PADDING/2, stringBounds.height - PADDING/2);
        }
    }
    
    public Color getColor(){ return color; }
    
    public void setColor(Color color){ this.color = color; }
    
    public void setVisibility(boolean b){ visible = b; }
    
    public MicroSecondDate getFlagTime(){ return flagTime; }
    
    public void setFlagTime(MicroSecondDate flagTime){
        this.flagTime = flagTime;
    }
    
    private Color color = Color.RED;
    
    private boolean visible = true;
    
    private MicroSecondDate flagTime;
    
    private String name;
    
    private DataSetSeismogram seis;
    
    //pixels of space of flag around the font
    private static final int PADDING = 4;
    
    private static Category logger = Category.getInstance(Flag.class.getName());
    
}// FlagPlotter
