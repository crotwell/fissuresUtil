package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * EventFlagPlotter.java
 *
 *
 * Created: Fri Mar 28 14:20:17 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EventFlag{
    public EventFlag (final PlottableDisplay plottableDisplay,
                      EventAccessOperations eventAccess){
        this.plottableDisplay = plottableDisplay;
        this.eventAccess = eventAccess;
    }

    public int getX(){
        double xPercentage = getEventXPercent(eventAccess);
        return (int)(xPercentage * plottableDisplay.getRowWidth());
    }

    public int getY(){
        int row = getRow(eventAccess);
        return row * plottableDisplay.getRowOffset() + plottableDisplay.titleHeight;
    }

    public void drawEvents(Graphics g) {
        // get new graphics to avoid messing up original
        Graphics2D g2 = (Graphics2D)g.create();
        g2.translate(0, getY());
        g2.setStroke(new BasicStroke(3));
        g2.setPaint(color);
        int halfOffset =  plottableDisplay.getRowOffset()/2;
        int xLoc = getX();
        g2.drawLine(xLoc, -halfOffset, xLoc, halfOffset);
    }

    public boolean isSelected(int[][] rowAndX) {
        int row = getRow();
        int x = getX();
        for (int i = 0; i < rowAndX.length; i++) {
            int[] cur = rowAndX[i];
            if(row == cur[0]){
                if(x <= cur[2] && x >= cur[1]){
                    return true;
                }
            }
        }
        return false;
    }

    private int getRow(){
        return getRow(eventAccess);
    }

    private int getRow(EventAccessOperations eventAccess) {
        try {
            if(eventOrigin == null) {
                eventOrigin = eventAccess.get_preferred_origin();
            }
            Time time = eventOrigin.origin_time;

            long microSeconds =  ( new MicroSecondDate(time)).getMicroSecondTime();
            Calendar calendar = Calendar.getInstance();
            Date date = new Date(microSeconds/1000);
            calendar.setTime(date);
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            return hours/2;

        } catch(Exception e) {

        }
        return -1;
    }

    private double getEventXPercent(EventAccessOperations eventAccess){
        if(eventOrigin == null) {
            try {
                eventOrigin = eventAccess.get_preferred_origin();
            } catch (NoPreferredOrigin e) {
                eventOrigin = eventAccess.get_origins()[0];
            }
        }
        Time time = eventOrigin.origin_time;
        long microSeconds =  ( new MicroSecondDate(time)).getMicroSecondTime();
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(microSeconds/1000);
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        double hoursPerRow = 24/plottableDisplay.getRows();
        double leftoverHours = hours%hoursPerRow;
        leftoverHours += minutes/60.0;
        leftoverHours += seconds/60.0/60.0;
        return leftoverHours/hoursPerRow;
    }

    public void setTitleLoc(int x, int y, int width, int height){
        titleX = x;
        titleY = y;
        titleWidth = width;
        titleHeight = height;
    }

    public Rectangle getTitleLoc(){
        return new Rectangle(titleX, titleY, titleWidth, titleHeight);
    }

    public String getTitle(){
        if(eventTitle == null){
            eventTitle = EventLayer.getEventInfo(eventAccess);
        }
        return eventTitle;
    }

    public EventAccessOperations getEvent(){ return eventAccess; }

    public Color getColor(){ return color; }

    private int titleX, titleY, titleWidth, titleHeight;

    private String eventTitle;

    private PlottableDisplay plottableDisplay;

    private EventAccessOperations eventAccess;

    private Origin eventOrigin;

    private Color color = SeismogramDisplay.colors[++colorCount%SeismogramDisplay.colors.length];

    private static int colorCount = 0;
}// EventFlagPlotter
