package edu.sc.seis.fissuresUtil.display;

import java.awt.*;

import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.utility.Logger;
import edu.sc.seis.fissuresUtil.display.drawable.EventFlagPlotter;
import edu.sc.seis.fissuresUtil.display.drawable.PlottableSelection;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

/**
 * PlottableDisplay.java
 *
 *
 * Created: Wed Jul 18 11:08:24 2001
 *
 * @author Srinivasa Telukutla
 * Modified: Georgina Coleman
 * @version
 */


public  class PlottableDisplay extends JComponent {


    public PlottableDisplay() {
        super();
        removeAll();
        final Color bg = Color.white;
        final Color fg = Color.yellow;
        //Initialize drawing colors, border, opacity.
        setBackground(bg);
        setForeground(fg);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                                     BorderFactory.createLoweredBevelBorder()));
        setLayout(new BorderLayout());
        selection = new PlottableSelection(this);
        configChanged();
    }

    public void setOffset(int offset) {
        if(plotOffset != offset){
            plotOffset = offset;
            configChanged();
        }
    }

    public void setEvents(EventAccessOperations[] eventAccess) {
        this.eventAccess = eventAccess;
        eventPlotterList = new LinkedList();
        addEventPlotterInfo(eventAccess);
        repaint();
    }

    public void setAmpScale(float ampScalePercent) {
        if (this.ampScalePercent != ampScalePercent) {
            this.ampScalePercent = ampScalePercent;
            configChanged();
        }
    }

    void configChanged() {
        image = null;
        // signal any drawing thread to stop
        currentImageGraphics = null;
        int newWidth = totalWidth/rows +2*LABEL_X_SHIFT;
        int newHeight = rowHeight +(plotOffset * (rows-1))+TITLE_Y_SHIFT;
        setPreferredSize(new Dimension(newWidth, newHeight));
        repaint();
    }

    public void setPlottable(Plottable[] clientPlott,
                             String nameofstation,
                             String orientationName,
                             Date date,
                            ChannelId channelId) {
        eventPlotterList = new LinkedList();
        removeAll();
        selection = new PlottableSelection(this);


        // signal any drawing thread to stop
        currentImageGraphics = null;

        this.arrayplottable = clientPlott;
        int[] minmax = findMinMax(arrayplottable);
        ampScale = rowHeight*1f/(minmax[1] - minmax[0]);

        if (arrayplottable == null) {
            Logger.warning("setPlottable:Plottable is NULL.");
            this.arrayplottable = new Plottable[0];
        }

        stationName = nameofstation;
        this.orientationName = orientationName;
        dateName = dateFormater.format(date);
        this.date = date;
        this.channelId = channelId;
        plottableShape = makeShape(clientPlott);
        configChanged();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);      //clears the background
        if (image == null) {
            image = createImage();
        }
        g.drawImage(image, 0, 0, this);
        drawSelection(g);
        drawEventFlags(g);
    }

    protected void drawComponent(Graphics g) {
        drawTitle(g);
        drawTimeTicks(g);

        if (arrayplottable== null ) {
            Logger.warning("Plottable is NULL.");
            return;
        }

        // for time label on left and title at top
        g.translate(LABEL_X_SHIFT, TITLE_Y_SHIFT);
        g.clipRect(0, 0,
                   totalWidth/rows,
                   rowHeight +(plotOffset * (rows-1)));
        drawPlottableNew(g);
        drawSelection(g);
    }


    void drawTitle(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(Color.blue);
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D stringBounds;
        Rectangle2D orientationBounds = fm.getStringBounds("Orientation: " , g2);
        if(stationName.length() > 0){
            g2.setFont(DisplayUtils.BOLD_FONT);
            stringBounds = fm.getStringBounds("Station: " , g2);
            int x = LABEL_X_SHIFT +
                (int)(orientationBounds.getWidth() - stringBounds.getWidth());
            g2.drawString("Station: ", x, 20);
            g2.setFont(DisplayUtils.DEFAULT_FONT);
            g2.drawString(stationName, (int)(x + stringBounds.getWidth()), 20);
        }else{
            g2.drawString("On the menu above, choose a SCEPP station from the SC icon and then click 'load data'.", 50, 20);
        }
        if(dateName.length() > 0){
            g2.setFont(DisplayUtils.BOLD_FONT);
            stringBounds = fm.getStringBounds("Date: " , g2);
            int x = LABEL_X_SHIFT +
                (int)(orientationBounds.getWidth() - stringBounds.getWidth());
            g2.drawString("Date: ", x, (int)(20 +stringBounds.getHeight()));
            g2.setFont(DisplayUtils.DEFAULT_FONT);
            g2.drawString(dateName,(int)(x + stringBounds.getWidth()),
                              (int)(20 +stringBounds.getHeight()));
        }
        if(orientationName.length() > 0){
            g2.setFont(DisplayUtils.BOLD_FONT);
            g2.drawString("Orientation: ",
                          LABEL_X_SHIFT,
                              (int)(20 + 2*orientationBounds.getHeight()));
            g2.setFont(DisplayUtils.DEFAULT_FONT);
            g2.drawString(orientationName,
                              (int)(LABEL_X_SHIFT + orientationBounds.getWidth()),
                              (int)(20 + 2*orientationBounds.getHeight()));
        }
        String myt = "Time";
        String mygmt = "GMT";

        g2.setPaint(Color.black);
        g2.drawString(myt, 10, 40);
        g2.drawString(myt,widthRow + LABEL_X_SHIFT, 40);
        g2.drawString(mygmt, 10, 50);
        g2.drawString(mygmt,widthRow + LABEL_X_SHIFT, 50);

        return;
    }

    void drawTimeTicks(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        int hour=0;
        String minutes = ":00 ";
        int hourinterval=totalHours/rows;
        String hourmin = hour+minutes;;

        int houroffset=10;
        int xShift = totalWidth/rows+LABEL_X_SHIFT;
        for (int currRow = 0; currRow < rows; currRow++) {

            if (currRow % 2 == 0) {
                g2.setPaint(Color.black);
            } else {
                g2.setPaint(Color.blue);
            }
            g2.drawString(hourmin,
                          houroffset,
                          TITLE_Y_SHIFT+rowHeight/2 + plotOffset*currRow);

            hour+=hourinterval;
            hourmin = hour+minutes;
            if(hour>=10) {  houroffset = 5; }
            g2.drawString(hour+minutes,
                          xShift+houroffset,
                          TITLE_Y_SHIFT+rowHeight/2 + plotOffset*currRow);
        }

    }

    void drawPlottableNew(Graphics g) {
        int xShift = totalWidth/rows;
        int mean = getMean();
        // get new graphics to avoid messing up original
        Graphics2D newG = (Graphics2D)g.create();

        for (int currRow = 0; currRow < rows && g == currentImageGraphics; currRow++) {
            // shift for row (left so time is in window,
            //down to correct row on screen, plus
            AffineTransform original = newG.getTransform();
            AffineTransform affine = newG.getTransform();

            affine.concatenate(affine.getTranslateInstance(-1*xShift*currRow,
                                                           rowHeight/2+plotOffset*currRow));
            // account for graphics y positive down
            affine.concatenate(affine.getScaleInstance(1, -1));

            newG.setTransform(affine);
            newG.setPaint(Color.red);

            newG.drawLine(0, 0, 6000, 0);
            AlphaComposite originalComposite = (AlphaComposite)newG.getComposite();
            newG.setComposite(originalComposite);
            affine.concatenate(affine.getScaleInstance(1, ampScale));
            affine.concatenate(affine.getScaleInstance(1, ampScalePercent));

            // translate max so mean is in middle
            affine.concatenate(affine.getTranslateInstance(0, -1*mean));
            newG.setTransform(affine);

            if (currRow % 2 == 0) {
                newG.setPaint(Color.black);
            } else {
                newG.setPaint(Color.blue);
            }
            if (plottableShape != null) {
                if (g != currentImageGraphics) return;
                newG.draw(plottableShape);
            } // end of if (plottableShape != null)

            newG.setTransform(original);

        }
        repaint();
        newG.dispose();
    }

    private Shape makeShape( Plottable[] plot) {
        final int SHAPESIZE = 100;
        GeneralPath wholeShape =
            new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for (int a=0; a<plot.length; a++) {
            if(plot[a].x_coor.length >= 2){
                GeneralPath currentShape =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                                    SHAPESIZE+1);
                currentShape.moveTo(plot[a].x_coor[0],
                                    plot[a].y_coor[0]);
                for(int i = 1; i < plot[a].x_coor.length; i++) {
                    //split into smaller shapes
                    if (i % SHAPESIZE == 0) {
                        // duplicate last point
                        if (plot[a].x_coor[i-1] == plot[a].x_coor[i]-1) {
                            currentShape.moveTo(plot[a].x_coor[i],
                                                plot[a].y_coor[i]);
                        } else {
                            currentShape.lineTo(plot[a].x_coor[i],
                                                plot[a].y_coor[i]);

                        } // end of else
                        wholeShape.append(currentShape, false);
                        currentShape =
                            new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                                            SHAPESIZE+1);
                    } // end of if (i % 100 == 0)

                    if (plot[a].x_coor[i-1] == plot[a].x_coor[i]-1) {
                        currentShape.moveTo(plot[a].x_coor[i],
                                            plot[a].y_coor[i]);
                    } else {
                        currentShape.lineTo(plot[a].x_coor[i],
                                            plot[a].y_coor[i]);

                    } // end of else
                }
                wholeShape.append(currentShape, false);

            } else if (plot[a].x_coor.length == 1){
                GeneralPath currentShape =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
                currentShape.moveTo(plot[a].x_coor[0],
                                    plot[a].y_coor[0]);
                currentShape.lineTo(plot[a].x_coor[0],
                                    plot[a].y_coor[0]);
                wholeShape.append(currentShape, false);
            }
        }
        return wholeShape;
    }

    public int getMean() {
        if (arrayplottable == null
            || arrayplottable.length == 0
            || arrayplottable[0].y_coor.length == 0) {
            return 0;
        }

        long mean=arrayplottable[0].y_coor[0];
        int numPoints=0;

        for (int i=0; i< arrayplottable.length; i++) {
            for (int j=0; j<arrayplottable[i].y_coor.length; j++) {
                mean += arrayplottable[i].y_coor[j];
            }
            numPoints += arrayplottable[i].y_coor.length;
        }
        mean = mean / numPoints;
        return (int)mean;
    }

    public Image createImage() {

        final int width = totalWidth/rows +2*LABEL_X_SHIFT;
        final int height = rowHeight +(plotOffset * (rows-1))+TITLE_Y_SHIFT;

        final Image offImg = super.createImage(width, height);

        Thread t = new Thread("Plottable Image Creator") {
            public void run() {
                Graphics2D g = (Graphics2D)offImg.getGraphics();
                currentImageGraphics = g;
                g.setBackground(Color.white);

                // clear canvas
                g.clearRect(0, 0, width, height);

                drawComponent(g);
                g.dispose();
                repaint();
            }
        };
        t.start();
        return offImg;
    }

    public int[] findMinMax(Plottable[] arrayplottable) {
        if (arrayplottable.length == 0) {
            int[] minandmax = new int[2];
            minandmax[0]= -1;
            minandmax[1]= 1;
            return minandmax;
        } // end of if (arrayplottable.length == 0)
        int min = arrayplottable[0].y_coor[0];
        int max = arrayplottable[0].y_coor[0];
        for(int arrayi=0; arrayi<arrayplottable.length ; arrayi++) {
            for(int ploti=0; ploti<arrayplottable[arrayi].y_coor.length ; ploti++) {
                min = Math.min(min, arrayplottable[arrayi].y_coor[ploti]);
                max = Math.max(max, arrayplottable[arrayi].y_coor[ploti]);
            }
        }
        int[] minandmax ={ min, max};
        return minandmax;
    }

    public void addToSelection(int x, int y) {
        selection.addXY(x, y);
        repaint();
    }

    public void setSelection(int x, int y) {
        selection.setXY(x, y);
        repaint();
    }

    public boolean indicatesExtract(int x, int y){
        if(selection.intersectsExtract(x, y)){
            return true;
        }
        return false;
    }

    private void drawSelection(Graphics g) {
        selection.draw(g);
    }

    private void drawEventFlags(Graphics g) {
        Iterator iterator = eventPlotterList.iterator();
        while(iterator.hasNext()) {
            EventFlagPlotter plotter = (EventFlagPlotter) iterator.next();
            plotter.draw((Graphics2D)g, null, null, null);
        }
    }

    public RequestFilter getRequestFilter() {
        return selection.getRequestFilter();
    }

    public void addEventPlotterInfo(EventAccessOperations[] eventAccessArray) {
        for(int counter = 0; counter < eventAccessArray.length; counter++) {
            eventPlotterList.add(new EventFlagPlotter(this, eventAccess[counter]));
        }
    }

    public Date getDate(){ return date; }

    public ChannelId getChannelId(){ return channelId; }

    /** Solely for use to determine if drawing thread is still current. */
    public Graphics2D getCurrentImageGraphics(){ return currentImageGraphics; }

    /* Defaults for plottable */
    public static final int ROWS = 12;

    public static final int TOTAL_WIDTH = 6000;

    public static final int ROW_HEIGHT = 200;

    public static final int OFFSET = 75;

    public static final int TITLE_Y_SHIFT = 40;

    public static final int LABEL_X_SHIFT = 50;

    //Plottable instance values
    public int getRows(){ return rows; }

    private int rows = ROWS;

    public int getRowWidth(){ return widthRow; }

    private int widthRow = TOTAL_WIDTH/ROWS;

    public int getOffset(){ return plotOffset; }

    private int plotOffset= OFFSET;

    public int getPlotWidth(){
        return totalWidth;
    }

    private int totalWidth = TOTAL_WIDTH;

    public int getRowHeight(){ return rowHeight; }

    private int rowHeight = ROW_HEIGHT;

    public int getTotalHours(){ return totalHours; }

    private int totalHours = 24;

    private ChannelId channelId;

    private float ampScale = 1.0f;

    private float ampScalePercent = 1.0f;

    private Date date;

    private static ColorFactory colorFactory = new ColorFactory();

    private PlottableSelection selection;

    private LinkedList eventPlotterList = new LinkedList();

    private Graphics2D currentImageGraphics = null;

    private EventAccessOperations[] eventAccess = new EventAccess[0];

    private Plottable[] arrayplottable = new Plottable[0];

    private String stationName = "";

    private String orientationName = "";

    private String dateName = "";

    private Image image = null;

    private Shape plottableShape = null;

    private static SimpleDateFormat dateFormater = new SimpleDateFormat("EEEE, d MMMM yyyy");
}
