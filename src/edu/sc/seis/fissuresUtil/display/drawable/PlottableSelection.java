package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * PlottableSelection.java
 *
 *
 * Created: Fri Mar 28 09:10:59 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PlottableSelection{
    public PlottableSelection (PlottableDisplay display){
        this.plottableDisplay = display;
    }

    public PlottableSelection(PlottableDisplay display, int x, int y){
        this(display);
        setXY(x, y);
    }

    public void draw(Graphics g) {
        if(visible){
            // get new graphics to avoid messing up original
            Graphics2D newG = (Graphics2D)g.create();
            int yTrans = plottableDisplay.getOffset();
            int offset = PlottableDisplay.TITLE_Y_SHIFT + yTrans - 5;
            for (int currRow = startRow; currRow <= endRow; currRow++) {
                int x = PlottableDisplay.LABEL_X_SHIFT;
                int width = plottableDisplay.getRowWidth();
                int y =  yTrans*currRow + offset;
                int height = plottableDisplay.getOffset() - 10;
                if(currRow == startRow) {
                    x =  startRowX;
                    width = PlottableDisplay.LABEL_X_SHIFT + width - x;
                }
                if(currRow == endRow) {
                    width = endRowX - x;
                }
                newG.setPaint(color);
                newG.setComposite(TRANSPARENT);
                newG.fillRect(x, y, width, height);
                newG.setComposite(OPAQUE);
                newG.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                newG.drawRect(x, y, width, height);
                if(currRow == startRow){
                    newG.setFont(DisplayUtils.BIG_BOLD_FONT);
                    FontMetrics fm = newG.getFontMetrics();
                    Rectangle2D bounds = fm.getStringBounds("Extract", newG);
                    x -= 3;
                    y += 3;
                    extractLocation = bounds;
                    extractLocation.setRect(x - 2, y - bounds.getHeight() + 5,
                                            bounds.getWidth() + 4,
                                            bounds.getHeight() + 2);
                    newG.setColor(Color.WHITE);
                    newG.fill(extractLocation);
                    newG.setColor(extractColor);
                    newG.drawString("Extract", x, y);
                    newG.draw(extractLocation);
                }
            }//end of for
            newG.dispose();
        }
    }

    private Rectangle2D extractLocation;

    public boolean intersectsExtract(int x, int y){
        if(extractLocation.contains(x, y)){
            return true;
        }
        return false;
    }

    private int[] getSelectedRows(int beginy, int endy) {
        if(beginy == -1 || endy == -1) return new int[0];
        ArrayList arrayList = new ArrayList();
        int selectionOffset = plottableDisplay.getOffset() / 2;
        for(int counter = 0; counter < plottableDisplay.getRows(); counter++) {
            int value =  (plottableDisplay.getRowHeight()/2 +
                              PlottableDisplay.TITLE_Y_SHIFT +
                              plottableDisplay.getOffset()*counter);

            if( (beginy <= (value + selectionOffset)) &&
                   (endy > (value - selectionOffset))) {
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

    public int getRow(int yPixel){
        if(yPixel - PlottableDisplay.TITLE_Y_SHIFT <0) return -1;
        int selectionOffset = plottableDisplay.getOffset() / 2;
        for(int counter = 0; counter < plottableDisplay.getRows(); counter++) {
            int value =  (plottableDisplay.getRowHeight()/2 +
                              PlottableDisplay.TITLE_Y_SHIFT +
                              plottableDisplay.getOffset()*counter);

            if( yPixel <= (value + selectionOffset)) {
                return counter;
            }
        }
        return -1;
    }


    public RequestFilter getRequestFilter() {
        if(!visible) return null;
        if(endRow == -1) return null;
        int rows = plottableDisplay.getRows();
        int rowvalue = plottableDisplay.getTotalHours()/rows;
        float plotwidth = plottableDisplay.getPlotWidth()/rows;
        float beginvalue = startRowX/plotwidth * rowvalue + startRow * rowvalue;
        float endvalue = endRowX/plotwidth * rowvalue + endRow * rowvalue;
        return new RequestFilter(plottableDisplay.getChannelId(),
                                 getTime(beginvalue).getFissuresTime(),
                                 getTime(endvalue).getFissuresTime());
    }

    public void addXY(int x, int y){
        setExtractColor(Color.BLACK);
        if(x < PlottableDisplay.LABEL_X_SHIFT ||
           x > plottableDisplay.getRowWidth() + PlottableDisplay.LABEL_X_SHIFT){
            return;
        }
        int row = getRow(y);
        if(row > -1 && row < plottableDisplay.getRows()){
            if(row == startRow){
                if(row == endRow &&
                       (Math.abs(startRowX - x) > Math.abs(endRowX - x))){
                    endRowX = x;
                }else{
                    startRowX = x;
                }
            }else if(row == endRow){
                endRowX = x;
            }else{
                if(row < startRow){
                    startRow = row;
                    startRowX = x;
                }else if(row > endRow){
                    endRow = row;
                    endRowX = x;
                }else{
                    if(Math.abs(startRow - row) == Math.abs(endRow - row)){
                        if(x - startRowX > endRowX - x){
                            startRowX = x;
                            startRow = row;
                        }else{
                            endRowX = x;
                            endRow = row;
                        }
                    }else if(Math.abs(startRow - row) > Math.abs(endRow - row)){
                        endRow = row;
                        endRowX = x;
                    }else{
                        startRow = row;
                        startRowX = x;
                    }
                }
            }
            placed = true;
        }
    }

    public void setXY(int x, int y) {
        if(x < PlottableDisplay.LABEL_X_SHIFT ||
           x > plottableDisplay.getRowWidth() + PlottableDisplay.LABEL_X_SHIFT){
            return;
        }
        if(placed){
            if(intersectsExtract(x, y)){
                setExtractColor(Color.RED);
            }else{
                setExtractColor(Color.BLACK);
            }
            return;
        }
        int row = getRow(y);
        if(row < 0){
            return;
        }
        visible = true;
        startRow = endRow = row;
        startRowX = x - 5;
        endRowX = x + 5;
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
        calendar.setTime(plottableDisplay.getDate());
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

    public void setExtractColor(Color newColor){
        extractColor = newColor;
        plottableDisplay.repaint();
    }

    public void setPlaced(boolean isPlaced){ placed = isPlaced; }

    private static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                                      1f);

    private static AlphaComposite TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                                           .4f);
    private Color color = Color.RED;

    private PlottableDisplay plottableDisplay;

    private int startRowX, endRowX, startRow = -1, endRow = -1;

    private boolean visible = false;

    private Color extractColor = Color.BLACK;

    private boolean placed;
}// PlottableSelection
