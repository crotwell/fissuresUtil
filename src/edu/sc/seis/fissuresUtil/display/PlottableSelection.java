package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfSeismogramDC.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

/**
 * PlottableSelection.java
 *
 *
 * Created: Fri Mar 28 09:10:59 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PlottableSelection implements Plotter{
    public PlottableSelection (PlottableDisplay display){
        this.plottableDisplay = display;
        this.plottableDisplay.date = Calendar.getInstance().getTime();
    }

    public PlottableSelection(PlottableDisplay display, Color color) {
        this(display);
        this.color = color;
    }
    

    public void draw(Graphics2D canvas, java.awt.Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        //in this draw of PlottableSelection the following are not used
        //currentTime , currentAmp, size.
        //actually this extends the interface Plotter just to be consistent
        // with others plotters in the display package.
        drawHighlightRegion(canvas);
        
    }

    public void toggleVisibility() {

    }

    public void setVisibility(boolean b) {

    }

    private void drawHighlightRegion(Graphics g) {
        // get new graphics to avoid messing up original
        Graphics2D newG = (Graphics2D)g.create(); 
      
        if(g != plottableDisplay.currentImageGraphics) {
            newG.translate(plottableDisplay.labelXShift,
                           plottableDisplay.titleYShift);
            newG.clipRect(0, 0, 
                          plottableDisplay.plot_x/plottableDisplay.plotrows, 
                          plottableDisplay.plot_y +(plottableDisplay.plotoffset * (plottableDisplay.plotrows-1)));
        }

        int xShift = plottableDisplay.plot_x/plottableDisplay.plotrows;
        int mean = plottableDisplay.getMean();
        int[] selectedRows = getSelectedRows(beginy, endy);
        for (int currRow = 0; currRow < plottableDisplay.plotrows; currRow++) {

            // shift for row (left so time is in window, 
            //down to correct row on screen, plus
            //	    newG.translate(xShift*currRow, plot_y/2 + plotoffset*currRow);
            java.awt.geom.AffineTransform original = newG.getTransform();
            java.awt.geom.AffineTransform affine = newG.getTransform();
	  
            affine.concatenate(affine.getTranslateInstance(-1*xShift*currRow,
                                                           plottableDisplay.plot_y/2+plottableDisplay.plotoffset*currRow));
            // account for graphics y positive down
            affine.concatenate(affine.getScaleInstance(1, -1));

            newG.setTransform(affine);
            AlphaComposite originalComposite = (AlphaComposite)newG.getComposite();
            AlphaComposite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                                                                     .4f);
            Point2D.Float beginPoint = new Point2D.Float(beginx, endx);
            if(this.color == null) {
                this.color = Color.green;
            }
            newG.setPaint(this.color);
            newG.setComposite(newComposite);
            if(isRowSelected(selectedRows, currRow)) {
                int bx = 0;
                int ex = 0;
                int by =  -plottableDisplay.plotoffset/2 + 10;
                int ey = plottableDisplay.plotoffset - 10;
                if(currRow == selectedRows[0] ) {
                    //System.out.println("Calculating values for start row");
                    bx =  beginx + xShift*currRow - plottableDisplay.labelXShift;// + beginx;
                    if(selectedRows.length  != 1) {
                        ex = 6000;
                    } else {
                        ex = endx - beginx;
                    }
                } else if(currRow == selectedRows[selectedRows.length -1 ]) {
                    //System.out.println("Caculating values for end row "+currRow);
                    bx = xShift*currRow - plottableDisplay.labelXShift;;
                    ex = (endx);
                } else {
                    bx = 0;
                    ex = 6000;
                }
        
                //System.out.println("NOW DRAW THE rectangle for row "+currRow);
                newG.drawRect(bx, by, ex, ey);
                newG.fillRect(bx, by, ex, ey);
                newG.setComposite(originalComposite);
                newG.setPaint(this.color);
                newG.setStroke(new BasicStroke(2.0f));
                newG.drawLine(bx, by, bx, by+ey);
                newG.drawLine((bx+ex), by, (bx+ex), by+ey);
            }//end of if
            newG.setTransform(original);
        }//end of for
        //getRequestFilter();
	    newG.dispose();
	   
    }

    private int[] getSelectedRows(int beginy, int endy) {
        
        if(beginy == -1 || endy == -1) return new int[0];
        // beginy = (int)(beginy * this.ampScalePercent);
        //endy = (int)(endy * this.ampScalePercent);
        ArrayList arrayList = new ArrayList();
        int selectionOffset = plottableDisplay.plotoffset / 2;
        for(int counter = 0; counter < plottableDisplay.plotrows; counter++) {
            int value =  (plottableDisplay.plot_y/2 + 
                          plottableDisplay.titleYShift + 
                          plottableDisplay.plotoffset*counter);
   
            //if(((value - selectionOffset) <= beginy) &&
            if( (beginy <= (value + selectionOffset)) &&
                // (endy >= beginy) && 
                (endy > (value - selectionOffset))) {
                //(endy <= (value + selectionOffset))) { 
                arrayList.add(new Integer(counter));
            }
        }
        int[] rtnValues = new int[arrayList.size()];
        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
            //System.out.println("The row selected is "+rtnValues[counter]);
        }
        return rtnValues;
     }

   
     public RequestFilter getRequestFilter() {
         if(endx == -1) return null;
         int[] selectedRows = getSelectedRows(beginy, endy);
         if(selectedRows.length == 0) return null;
         int rowvalue = 24/plottableDisplay.plotrows;
         int plotwidth = plottableDisplay.plot_x/plottableDisplay.plotrows;
         float beginvalue = ((beginx/(float)plotwidth)) * rowvalue + selectedRows[0] * rowvalue;
         float endvalue = (endx/(float)plotwidth) * rowvalue + selectedRows[selectedRows.length - 1] * rowvalue;
         return new RequestFilter(plottableDisplay.channelId, 
                                  getTime(beginvalue).getFissuresTime(),
                                  getTime(endvalue).getFissuresTime());
     }


    public void setSelectedRectangle(int beginx, int beginy, int endx, int endy) {
        if(getSelectedRows(beginy, endy).length == 1) {
            this.beginx = Math.min(beginx, this.beginx);
            //this.endx = Math.max(beginx, endx);
            this.endx = endx;
        } else {
            this.beginx = beginx;
            this.endx = endx;
        }
        this.beginy = beginy;
        this.endy = endy;
    }

    public void setXY(int currx, int curry) {
        setSelectedRectangle(beginx, beginy, currx, curry);
    }


    public void startXY(int beginx, int beginy) {
        this.beginx = beginx;
        this.beginy = beginy;
    }

    
    private MicroSecondDate getTime(float rowoffsetvalue) {
	
        int tempmilliseconds =(int) (rowoffsetvalue * 60 * 60 * 1000);
        int hours = tempmilliseconds / (60 * 60 * 1000);
        tempmilliseconds = tempmilliseconds - hours * 60 * 60 * 1000;
        int minutes = tempmilliseconds / (60 * 1000);
        tempmilliseconds = tempmilliseconds - minutes * 60 * 1000;
        int seconds = tempmilliseconds / 1000;
        tempmilliseconds = tempmilliseconds - seconds * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(plottableDisplay.date);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),
                                                                    calendar.get(Calendar.MONTH),
                                                                    calendar.get(Calendar.DATE),
                                                                    hours,
                                                                    minutes,
                                                                    seconds);
								    
								    

        gregorianCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new MicroSecondDate(gregorianCalendar.getTime());
    }

    private boolean isRowSelected(int[] rows, int currrow) {
        for(int counter = 0; counter < rows.length; counter++) {
            if(rows[counter] == currrow) return true;
        }
        return false;
    }

    public boolean isSelectionSelected(int currx, int curry) {
        if( (currx > (endx - 10) && currx < (endx + 10) &&
            curry >= beginy && curry <= endy) || 
            (currx > (beginx - 10) && currx < (beginx + 10) &&
             curry >= beginy && curry <= endy) 
            ) {
            System.out.println("returning true");
            return true;
        }
        System.out.println("return false");
        return false;
    }

    private Color color;

    private PlottableDisplay plottableDisplay;
    
    private int beginx;
    
    private int endx;
    
    private int beginy;

    private int  endy;
    
}// PlottableSelection
