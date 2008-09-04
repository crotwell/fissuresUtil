package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.PlottableDisplay;

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
        this.display = display;
    }

    public PlottableSelection(PlottableDisplay display, int x, int y){
        this(display);
        setXY(x, y, 10);
    }

    public void draw(Graphics g) {
        if(visible){
            // get new graphics to avoid messing up original
            Graphics2D newG = (Graphics2D)g.create();
            int rowHeight = display.getRowOffset();
            int yOffset = display.titleHeight;
            int xOffset = PlottableDisplay.LABEL_X_SHIFT;
            for (int currRow = startRow; currRow <= endRow; currRow++) {
                int x = xOffset;
                int width = display.getRowWidth();
                int y =  rowHeight*currRow + yOffset - rowHeight/2 + 5;
                int height = rowHeight - 10;
                if(currRow == startRow) {
                    x =  startRowX;
                    width -= x - xOffset;
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
                    y += 3 + height;
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
        if(extractLocation != null && extractLocation.contains(x, y)){
            return true;
        }
        return false;
    }

    private int[] getSelectedRows(int beginy, int endy) {
        if(beginy == -1 || endy == -1) return new int[0];
        ArrayList arrayList = new ArrayList();
        int selectionOffset = display.getRowOffset() / 2;
        for(int counter = 0; counter < display.getRows(); counter++) {
            int value =  (display.getRowOffset()/2 +
                              display.titleHeight +
                              display.getRowOffset()*counter);

            if( (beginy <= (value + selectionOffset)) &&
                   (endy > (value - selectionOffset))) {
                arrayList.add(new Integer(counter));
            }
        }
        int[] rtnValues = new int[arrayList.size()];
        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
        }
        return rtnValues;
    }

    public int getRow(int yPixel){
        int rowHeight = display.getRowOffset();
        if(yPixel - display.titleHeight + rowHeight/2 <0) return -1;
        for(int counter = 1; counter <= display.getRows(); counter++) {
            int value =  display.titleHeight + rowHeight*counter - rowHeight/2;
            if( yPixel <= (value)) {
                return counter-1;
            }
        }
        return -1;
    }


    public RequestFilter getRequestFilter() {
        if(!visible) return null;
        if(endRow == -1) return null;
        int rows = display.getRows();
        double rowWidth = display.getRowWidth();
        int offset = PlottableDisplay.LABEL_X_SHIFT;
        double beginPercentage = ((startRowX - offset)/rowWidth + startRow)/rows;
        double endPercentage = ((endRowX - offset)/rowWidth + endRow)/rows;
        return new RequestFilter(display.getChannelId(),
                                 getTime(beginPercentage).getFissuresTime(),
                                 getTime(endPercentage).getFissuresTime());
    }

    private MicroSecondDate getTime(double percentageOfPlottable){
        Date startTime = display.getDate();
        long milliSecondsPast = (long)(percentageOfPlottable * (display.getTotalHours() * 60 * 60 * 1000));
        return new MicroSecondDate(new Date(startTime.getTime() + milliSecondsPast));
    }

    public int[][] getSelectedArea(){
        int[][] selectedArea = new int[endRow - startRow + 1][3];
        if(visible && endRow != -1){
            for (int i = 0; i < selectedArea.length; i++) {
                selectedArea[i][0] = startRow + i;
                if(selectedArea[i][0] == startRow){
                    selectedArea[i][1] = startRowX;
                }else{
                    selectedArea[i][1] = 0;
                }
                if(selectedArea[i][0] == endRow){
                    selectedArea[i][2] = endRowX;
                }else{
                    selectedArea[i][2] = display.getRowWidth();
                }
            }
        }
        return selectedArea;
    }

    public void addXY(int x, int y){
        setExtractColor(Color.BLACK);
        if(x < PlottableDisplay.LABEL_X_SHIFT ||
           x > display.getRowWidth() + PlottableDisplay.LABEL_X_SHIFT){
            return;
        }
        int row = getRow(y);
        if(row > -1 && row < display.getRows()){
            int xToStart = Math.abs(x - startRowX);
            int xToEnd = Math.abs(x - endRowX);
            int rowsToStart = Math.abs(row - startRow);
            int rowsToEnd = Math.abs(row - endRow);
            if(startRow == endRow){
                if(row == startRow){
                    if(xToStart < xToEnd){
                        setStart(x, row);
                    }else{
                        setEnd(x, row);
                    }
                }else if(row < startRow){
                    if(xToStart > xToEnd){
                        setEnd(startRowX, startRow);
                    }
                    setStart(x, row);
                }else{
                    if(xToStart < xToEnd){
                        setStart(endRowX, endRow);
                    }
                    setEnd(x, row);
                }
            }else{
                if(endRow - startRow > 1){
                    if(rowsToStart < rowsToEnd){
                        setStart(x, row);
                    }else{
                        setEnd(x, row);
                    }
                }else{
                    if(row < startRow){
                        setStart(x, row);
                    }else if(row > endRow){
                        setEnd(x, row);
                    }else if(row == startRow && xToStart <= xToEnd + 3){
                        setStart(x, row);
                    }else if(row == endRow && xToEnd <= xToStart + 3){
                        setEnd(x, row);
                    }else if(row == startRow){
                        if(x < startRowX){
                            setEnd(startRowX, startRow);
                            setStart(x, row);
                        }else{
                            setEnd(x, row);
                        }
                    }else{
                        if(x > endRowX){
                            setStart(endRowX, endRow);
                            setEnd(x,row);
                        }else{
                            setStart(x, row);
                        }
                    }
                }
            }
        }
        placed = true;
    }

    private void setStart(int x, int row){
        startRowX = x;
        startRow = row;
    }

    private void setEnd(int x, int row){
        endRowX = x;
        endRow = row;
    }

    public void set(int[][] rowX){
        visible = true;
        setStart(rowX[0][1], rowX[0][0]);
        setEnd(rowX[1][1], rowX[1][0]);
    }

    public void setXY(int x, int y, int width){
        if(x < PlottableDisplay.LABEL_X_SHIFT ||
           x > display.getRowWidth() + PlottableDisplay.LABEL_X_SHIFT){
            if(!placed) visible = false;
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
            if(!placed) visible = false;
            return;
        }
        visible = true;
        startRow = endRow = row;
        startRowX = x - width/2;
        endRowX = x + width/2;
    }

    public void setExtractColor(Color newColor){
        extractColor = newColor;
        display.repaint();
    }

    public boolean borders(int x, int y){
        int row = getRow(y);
        if((row == startRow && Math.abs(x - startRowX) < 3) ||
               (row == endRow && Math.abs(x - endRowX) < 3)){
            return true;
        }
        return false;
    }

    public void setPlaced(boolean isPlaced){ placed = isPlaced; }

    private static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                                      1f);

    private static AlphaComposite TRANSPARENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                                           .4f);
    private Color color = Color.RED;

    private PlottableDisplay display;

    private int startRowX, endRowX, startRow = -1, endRow = -1;

    private boolean visible = false;

    private Color extractColor = Color.BLACK;

    private boolean placed;
}// PlottableSelection
