package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;

/**
 * Draws a box around the event on a plottable from the origin to the arrival of
 * 4kmps. It expects a 4kmps arrival to be in the arrivals in the plottable. If
 * the 4kmps arrival at this station is less than 5 minutes past the origin, the
 * box is from the origin to 5 minutes past it
 * 
 * @author groves Created on May 9, 2005
 */
public class EventBoxes extends EventFlag {

    public EventBoxes(final PlottableDisplay plottableDisplay,
            EventAccessOperations eventAccess, Arrival[] arrivals) {
        super(plottableDisplay, eventAccess, arrivals);
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setStroke(new BasicStroke(3));
        g2.setPaint(getColor());
        List boxes = getBoxes();
        Iterator it = boxes.iterator();
        while(it.hasNext()) {
            g2.draw((Rectangle)it.next());
        }
    }

    public List getBoxes() {
        List boxes = new ArrayList();
        int originX = getOriginX();
        int originY = getOriginY();
        Arrival kmpsArrival = get4kmpsArrival();
        int lastX = getX(kmpsArrival);
        int lastY = getY(kmpsArrival);
        //If the arrival is less than five minutes, make the box time 5 minutes
        if(kmpsArrival.getTime() < 5 * 60) {
            MicroSecondDate arrivalTime = getOriginTime().add(new TimeInterval(5,
                                                                               UnitImpl.MINUTE));
            lastX = getX(arrivalTime);
            lastY = getY(getRow(arrivalTime));
        }
        if(originY == lastY) {
            boxes.add(makeRect(originX, lastX, originY));
        } else {
            int rowRightEdge = PlottableDisplay.LABEL_X_SHIFT
                    + getDisplay().getRowWidth();
            boxes.add(makeRect(originX, rowRightEdge, originY));
            originY += getDisplay().getRowOffset();
            while(originY != lastY) {
                boxes.add(makeRect(PlottableDisplay.LABEL_X_SHIFT,
                                   rowRightEdge,
                                   originY));
                originY += getDisplay().getRowOffset();
            }
            if(lastX != PlottableDisplay.LABEL_X_SHIFT) {
                boxes.add(makeRect(PlottableDisplay.LABEL_X_SHIFT,
                                   lastX,
                                   originY));
            }
        }
        return boxes;
    }

    private Arrival get4kmpsArrival() {
        Arrival kmpsArrival = null;
        for(int i = 0; i < arrivals.length; i++) {
            if(arrivals[i].getName().equals("3kmps")) {
                if(kmpsArrival == null) {
                    kmpsArrival = arrivals[i];
                } else if(kmpsArrival.getDist() > arrivals[i].getDist()) {
                    kmpsArrival = arrivals[i];
                }
            }
        }
        if(kmpsArrival == null) {
            throw new IllegalStateException("To use Event boxes, a 3kmps arrival must be included with the arrivals passed into PlottableDisplay");
        }
        return kmpsArrival;
    }

    private Rectangle makeRect(int leftX, int rightX, int centerY) {
        int halfOffset = getDisplay().getRowOffset() / 2;
        return new Rectangle(leftX,
                             centerY - halfOffset,
                             rightX - leftX,
                             getDisplay().getRowOffset());
    }
}
